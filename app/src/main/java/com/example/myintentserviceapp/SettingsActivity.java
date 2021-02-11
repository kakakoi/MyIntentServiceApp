package com.example.myintentserviceapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    public static final String KEY_SMB_IP = "smb_ip";
    public static final String KEY_SMB_PASS = "smb_pass";
    public static final String KEY_SYNC = "sync";
    public static final String KEY_SMB_USER = "smb_user";
    public static final String KEY_SMB_DIR = "smb_dir";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            EditTextPreference numberPreference = findPreference(KEY_SMB_IP);

            if (numberPreference != null) {
                numberPreference.setOnBindEditTextListener(
                        new EditTextPreference.OnBindEditTextListener() {
                            @Override
                            public void onBindEditText(@NonNull EditText editText) {
                                editText.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
                            }
                        });
            }

            EditTextPreference passPreference = findPreference(KEY_SMB_PASS);
            if (passPreference != null) {
                passPreference.setOnBindEditTextListener(
                        new EditTextPreference.OnBindEditTextListener() {
                            @Override
                            public void onBindEditText(@NonNull EditText editText) {
                                editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                                // Replace -> android:inputType="textPassword"
                                // For hiding text
                                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());

                                // Replace -> android:selectAllOnFocus="true"
                                // On password field, you cannot make a partial selection with .setSelection(start, stop)
                                editText.selectAll();

                                // Replace -> android:maxLength="99"
                                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(99)});
                            }
                        }
                );
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        }

        private SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        if (key.equals(KEY_SMB_DIR)) {
                            String newValue = sharedPreferences.getString(KEY_SMB_DIR, "");
                            String prefixExp = "^(\\w)";
                            String prefixRep = "/$1";
                            String suffixExp = "(\\w)$";
                            String suffixRep = "$1/";

                            if (!TextUtils.isEmpty(newValue)) {
                                String afterStr = newValue.trim()
                                        .replaceFirst(prefixExp, prefixRep)
                                        .replaceFirst(suffixExp, suffixRep);
                                sharedPreferences.edit().putString(KEY_SMB_DIR, afterStr).apply();
                            }
                        }
                    }
                };
    }
}