package com.example.fitnesstracker.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;
import com.example.fitnesstracker.FitnessTrackerApp;
import com.example.fitnesstracker.R;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        // Ubacujemo PreferenceFragment u FrameLayout unutar našeg XML-a
        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new InnerSettingsFragment())
                    .commit();
        }
        
        return view;
    }

    public static class InnerSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            androidx.preference.Preference osmPref = findPreference("osm_attribution");
            if (osmPref != null) {
                osmPref.setOnPreferenceClickListener(preference -> {
                    android.content.Intent browserIntent = new android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://www.openstreetmap.org/copyright"));
                    startActivity(browserIntent);
                    return true;
                });
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if ("language".equals(key)) {
                String lang = sharedPreferences.getString(key, "sr");
                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                        androidx.core.os.LocaleListCompat.forLanguageTags(lang)
                );
            }
        }
    }
}
