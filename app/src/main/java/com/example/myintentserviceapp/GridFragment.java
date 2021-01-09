package com.example.myintentserviceapp;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myintentserviceapp.data.Photo;
import com.example.myintentserviceapp.data.PhotoViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class GridFragment extends Fragment implements ViewModelStoreOwner {

    public static final String BROADCAST_ACTION = " jp.co.casareal.genintentservice.broadcast";
    public static final String PHOTO = "photo";
    private final Fragment mFragment = null;
    private Activity mActivity = null;
    private RecyclerView mRecyclerView = null;
    private TextView mCountTextView = null;
    private PhotoViewModel mPhotoViewModel;
    /**
     * BroadcastReceiverでMyIntentServiceからの返答を受け取ろうと思います
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            CustomAdapter adapter = (CustomAdapter) mRecyclerView.getAdapter();
            String photoName = intent.getStringExtra(PHOTO);

            if (adapter.getItemCount() > 1) {
                Photo data = new Photo();
                data.fileName = photoName;
                adapter.add(data);
                adapter.notifyItemInserted(adapter.getItemCount());
            } else {
                Photo data = new Photo();
                data.fileName = photoName;
                List<Photo> list = new ArrayList<>();
                list.add(data);
                mRecyclerView.setAdapter(new CustomAdapter(list));
            }

        }
    };
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_grid, container, false);
        //CustomAdapter adapter = new CustomAdapter(this.createDataset());
        mLayoutManager = new GridLayoutManager(mActivity, 4, GridLayoutManager.VERTICAL, false);

        mCountTextView = (TextView) view.findViewById(R.id.count_text);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_vew);

        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(mLayoutManager);

        //mRecyclerView.setAdapter(adapter);
        // Inflate the layout for this fragment


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewModelProvider.AndroidViewModelFactory factory = PhotoViewModelFactory.getInstance(requireActivity().getApplication());
        mPhotoViewModel = new ViewModelProvider(this::getViewModelStore, factory).get(PhotoViewModel.class);
        LiveData<List<Photo>> lPhotos = mPhotoViewModel.getAllPhotos();
        List<Photo> photos = lPhotos.getValue();

        CustomAdapter adapter =null;
        if (photos != null) {
            adapter = new CustomAdapter(createDataset(photos));
        }else {
            adapter = new CustomAdapter(this.createDataset());
        }
        mRecyclerView.setAdapter(adapter);

        mPhotoViewModel.getAllPhotos().observe(getViewLifecycleOwner(), new Observer<List<Photo>>() {
            @Override
            public void onChanged(List<Photo> photos) {
                //List<Photo> fileNames = new ArrayList<>();
                int count = photos.size();
                if(count> 0){
                    //mRecyclerView.setAdapter(new CustomAdapter(createDataset(photos)));
                    CustomAdapter adapter = (CustomAdapter) mRecyclerView.getAdapter();
                    adapter.setData(photos);
                    mCountTextView.setText(Integer.toString(count));
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.registerReceiver(receiver, new IntentFilter(BROADCAST_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        mActivity.unregisterReceiver(receiver);
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

    public ViewModelStore getViewModelStore() {
        return new ViewModelStore();
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