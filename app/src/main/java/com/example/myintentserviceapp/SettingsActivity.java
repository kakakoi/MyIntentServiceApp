package com.example.myintentserviceapp;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

public class SettingsActivity extends AppCompatActivity {

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
            EditTextPreference numberPreference = findPreference("smb_ip");

            if (numberPreference != null) {
                numberPreference.setOnBindEditTextListener(
                        new EditTextPreference.OnBindEditTextListener() {
                            @Override
                            public void onBindEditText(@NonNull EditText editText) {
                                editText.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
                                //editText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                            }
                        });
            }

            EditTextPreference passPreference = findPreference("smb_pass");
            if(passPreference != null){
                passPreference.setOnBindEditTextListener(
                        new EditTextPreference.OnBindEditTextListener() {
                            @Override
                            public void onBindEditText(@NonNull EditText editText) {
                                editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD|InputType.TYPE_CLASS_TEXT);
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
    }
}