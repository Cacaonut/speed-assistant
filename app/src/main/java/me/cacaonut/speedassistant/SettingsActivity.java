package me.cacaonut.speedassistant;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

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

            Preference.OnPreferenceChangeListener soundChangeListener = (preference, newValue) -> {
                Context context = getActivity();
                if (context != null) {
                    int resId = getResources().getIdentifier((String) newValue, "raw", context.getPackageName());
                    final MediaPlayer mp = MediaPlayer.create(context, resId);
                    mp.start();
                }
                return true;
            };

            ListPreference soundHighPreference = findPreference("sound_high");
            if (soundHighPreference != null)
                soundHighPreference.setOnPreferenceChangeListener(soundChangeListener);

            ListPreference soundLowPreference = findPreference("sound_low");
            if (soundLowPreference != null)
                soundLowPreference.setOnPreferenceChangeListener(soundChangeListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }
}