package com.vadim;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;

import com.vadim.presenter.SettingsPresenter;
import com.vadim.qr_code.R;

import java.util.Objects;

import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        ButterKnife.bind(this);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDefaultDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.settings_activity_title);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
            implements SettingsPresenter.SettingsView {
        private TwoStatePreference deleteOfPhotoFolder;
        private SettingsPresenter settingsPresenter;
        private Context context;
        private AlertDialog alertDialog;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            context = SettingsFragment.this.requireContext();
            deleteOfPhotoFolder = findPreference("delete_photos");
            if (deleteOfPhotoFolder != null) {
                deleteOfPhotoFolder.setOnPreferenceClickListener(preference -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
                    builder.setTitle("Подтверждение")
                            .setMessage("Вы действительно хотите удалить папку /Picture/QrList с фотографиями?")
                            .setOnCancelListener(dialog -> deleteOfPhotoFolder.setChecked(false))
                            .setPositiveButton("OK", (dialog, which) -> {
                                settingsPresenter = new SettingsPresenter(this);
                                settingsPresenter.deleteDirectory();
                                settingsPresenter.detachView();
                            })
                            .setNegativeButton("Отмена", (dialog, which) ->
                                    deleteOfPhotoFolder.setChecked(false));
                    alertDialog  = builder.create();
                    alertDialog.show();
                    return true;
                });
            }

            PreferenceScreen preferenceScreen = findPreference("support");
            Objects.requireNonNull(preferenceScreen)
                    .setOnPreferenceClickListener(preference -> {
                        sendEmail();
                        return true;
                    });
        }

        @SuppressLint("IntentReset")
        private void sendEmail() {
            final Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL,
                    new String[] { "vadimlipakov@gmail.com" });
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Application QrList");
            try {
                startActivity(Intent.createChooser(emailIntent, "Send email"));
            }  catch (ActivityNotFoundException ex) {
                Toast.makeText(context,
                        "Невозможно отправить письмо.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            if (alertDialog != null) {
                deleteOfPhotoFolder.setChecked(false);
                alertDialog.dismiss();
            }
        }

        @Override
        public void notifyAboutDeleting() {
            Toast.makeText(context,
                    "Папка /Picture/QrList удалена",
                    Toast.LENGTH_SHORT).show();
            deleteOfPhotoFolder.setChecked(false);
        }

        @Override
        public void notifyAboutNotExisting() {
            Toast.makeText(context,
                    "На данный момент папка /Picture/QrList не существует",
                    Toast.LENGTH_SHORT).show();
            deleteOfPhotoFolder.setChecked(false);
        }
    }
}