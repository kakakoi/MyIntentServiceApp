package com.example.myintentserviceapp;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myintentserviceapp.data.Photo;
import com.example.myintentserviceapp.data.PhotoViewModel;
import com.example.myintentserviceapp.delegate.AnimationDelegate;
import com.example.myintentserviceapp.helper.SnackbarHelper;
import com.example.myintentserviceapp.network.Smb;
import com.example.myintentserviceapp.util.RecyclerFastScroller;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class GridFragment extends Fragment implements ViewModelStoreOwner {

    public static final String BROADCAST_ACTION = "com.example.myintentserviceapp.GridFragment.broadcast";
    public static final String BROADCAST_ACTION_MSG = "com.example.myintentserviceapp.GridFragment.broadcast.msg";
    public static final String PHOTO = "photo";
    public static final String MSG = "msg";
    private final Fragment mFragment = null;
    private Activity mActivity = null;
    private RecyclerView mRecyclerView = null;
    private Menu mMenu;

    private int mDownloadPhotosCount = 0;
    private int mSearchedPhotosCount = 0;
    private String mSyncStatus = null;

    private Snackbar mSnackbarSyncPhotoState = null;
    /**
     * BroadcastReceiverでMyIntentServiceからの返答を受け取ろうと思います
     */
    private final BroadcastReceiver msgReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            mSyncStatus = intent.getStringExtra(Smb.BROADCAST_TAG_STATUS);
            ActionMenuItemView menuItemView = (ActionMenuItemView) mActivity.findViewById(R.id.action_sync);
            //同期アイコンがアニメーションしてなければ動かす
            if (menuItemView != null && menuItemView.getAnimation() == null) {
                AnimationDelegate.startAnimSyncIcon(mActivity, mMenu.findItem(R.id.action_sync), menuItemView);
            }
        }
    };

    private final BroadcastReceiver errorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //同期アニメーションを止める
            ActionMenuItemView menuItemView = (ActionMenuItemView) mActivity.findViewById(R.id.action_sync);
            if (menuItemView != null) {
                menuItemView.clearAnimation();
                //setIcon(id)のため別経路から再取得
                MenuItem syncMenu = mMenu.findItem(R.id.action_sync);
                syncMenu.setIcon(R.drawable.ic_baseline_sync_problem_24);
                SnackbarHelper.INSTANCE.withStartActivity(
                        mActivity.findViewById(R.id.main_activity)
                        , mActivity
                        , SettingsActivity.class
                        , intent.getStringExtra(MSG)
                );
            } else {
                Intent settingsIntent = new Intent(mActivity, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        }
    };

    private RecyclerFastScroller mRecyclerFastScroller = null;
    private PhotoViewModel mPhotoViewModel;
    private GridLayoutManager mLayoutManager = null;

    public GridFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        mMenu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_grid, container, false);
        mLayoutManager = new GridLayoutManager(mActivity, 4, GridLayoutManager.VERTICAL, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerFastScroller = (RecyclerFastScroller) view.findViewById((R.id.fast_scroller));

        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerFastScroller.setRecyclerView(mRecyclerView);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewModelProvider.AndroidViewModelFactory factory = PhotoViewModelFactory.getInstance(requireActivity().getApplication());
        mPhotoViewModel = new ViewModelProvider(this::getViewModelStore, factory).get(PhotoViewModel.class);

        LiveData<List<Photo>> lPhotos = mPhotoViewModel.getAllPhotos();
        List<Photo> photos = lPhotos.getValue();

        CustomAdapter adapter = null;
        if (photos != null) {
            adapter = new CustomAdapter(createDataset(photos));
        } else {
            adapter = new CustomAdapter(this.createDataset());
        }
        mRecyclerView.setAdapter(adapter);

        mPhotoViewModel.getAllPhotos().observe(getViewLifecycleOwner(), new Observer<List<Photo>>() {
            @Override
            public void onChanged(List<Photo> photos) {
                mDownloadPhotosCount = photos.size();
                if (mDownloadPhotosCount > 0) {
                    CustomAdapter adapter = (CustomAdapter) mRecyclerView.getAdapter();
                    adapter.setData(photos);

                    showSyncStateSnackbar();
                }
            }
        });

        mPhotoViewModel.getCount().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                mSearchedPhotosCount = integer.intValue();
                if (mSearchedPhotosCount > 0) {
                    showSyncStateSnackbar();
                }
            }
        });
    }

    //同期進捗SnackBarを表示
    private void showSyncStateSnackbar() {
        StringBuilder sb = new StringBuilder();
        sb.append(mDownloadPhotosCount);
        sb.append("/");
        sb.append(mSearchedPhotosCount);
        if (!TextUtils.isEmpty(mSyncStatus)) {
            sb.append(" \n");
            sb.append(mSyncStatus);
        }
        String statusStr = sb.toString();
        if (mSnackbarSyncPhotoState == null) {
            mSnackbarSyncPhotoState = Snackbar.make(mActivity.findViewById(R.id.main_activity)
                    , statusStr
                    , Snackbar.LENGTH_INDEFINITE);
        }
        TextView snackTextView =
                mSnackbarSyncPhotoState.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        snackTextView.setText(statusStr);
        snackTextView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        //snackTextView.setSingleLine();

        mSnackbarSyncPhotoState.setAction(R.string.close, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSnackbarSyncPhotoState.dismiss();
            }
        });
        mSnackbarSyncPhotoState.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter statusIntentFilter = new IntentFilter(
                MyIntentService.BROADCAST_ACTION_MSG);
        IntentFilter errorIntentFilter = new IntentFilter(
                MyIntentService.BROADCAST_ACTION_ERROR);

        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(errorReceiver, errorIntentFilter);
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(msgReceiver, statusIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this.getContext()).unregisterReceiver(errorReceiver);
        LocalBroadcastManager.getInstance(this.getContext()).unregisterReceiver(msgReceiver);
    }

    private List<Photo> createDataset() {

        List<Photo> dataset = new ArrayList<>();
//        Photo data = new Photo();
//        dataset.add(data);
        return dataset;
    }

    private List<Photo> createDataset(List photoList) {

        List<Photo> dataset = new ArrayList<>();
        for (Object o :
                photoList) {
            if (o.getClass() == String.class) {
                Photo data = new Photo();
                data.fileName = (((String) o));
                dataset.add(data);
            } else if (o.getClass() == Photo.class) {
                dataset.add(((Photo) o));
            }
        }
        return dataset;
    }

    public class PhotoViewModelFactory extends ViewModelProvider.AndroidViewModelFactory {

        private Application mApplication;

        /**
         * Creates a {@code AndroidViewModelFactory}
         *
         * @param application an application to pass in {@link AndroidViewModel}
         */
        public PhotoViewModelFactory(@NonNull Application application) {
            super(application);
            mApplication = application;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass == PhotoViewModel.class) {
                return (T) new PhotoViewModel(mApplication);
            }
            return super.create(modelClass);
        }
    }
}