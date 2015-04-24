package com.alorma.github.ui.fragment.preference;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alorma.github.Interceptor;
import com.alorma.github.R;
import com.alorma.github.sdk.utils.GitskariosSettings;
import com.alorma.github.sdk.utils.PreferencesHelper;
import com.alorma.github.ui.activity.RepoDetailActivity;
import com.alorma.github.ui.fragment.ChangelogDialog;
import com.alorma.gitskarios.basesdk.client.StoreCredentials;

public class GitskariosPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String PREF_INTERCEPT = "pref_intercept";
    public static final String REPOS_SORT = "repos_sort";
    public static final String REPOS_FILE_TYPE = "repos_download_type";
    public static final String GITSKARIOS = "gitskarios";
    public static final String CHANGELOG = "changelog";
    public static final String NOTIF_SYNC = "notifications_sync";
    private String KEY_SYNC = "KEY_SYNC";

    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SYNC_INTERVAL_IN_MINUTES = 60L;
    public static final long SYNC_INTERVAL =
            SYNC_INTERVAL_IN_MINUTES *
                    SECONDS_PER_MINUTE;

    private StoreCredentials credentials;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.main_prefs);

        CheckBoxPreference intercetor = (CheckBoxPreference) findPreference(PREF_INTERCEPT);

        ComponentName componentName = new ComponentName(getActivity(), Interceptor.class);
        int componentEnabledSetting = getActivity().getPackageManager().getComponentEnabledSetting(componentName);
        intercetor.setChecked(componentEnabledSetting == PackageManager.COMPONENT_ENABLED_STATE_ENABLED);

        intercetor.setOnPreferenceChangeListener(this);

        findPreference(REPOS_SORT).setOnPreferenceChangeListener(this);

        findPreference(REPOS_FILE_TYPE).setOnPreferenceChangeListener(this);

        Preference gitskarios = findPreference(GITSKARIOS);
        gitskarios.setOnPreferenceClickListener(this);

        Preference changelog = findPreference(CHANGELOG);
        changelog.setOnPreferenceClickListener(this);

        Preference notifSync = findPreference(NOTIF_SYNC);
        notifSync.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(GITSKARIOS)) {
            Intent intent = RepoDetailActivity.createLauncherIntent(getActivity(), "gitskarios", "Gitskarios");
            startActivity(intent);
        } else if (preference.getKey().equals(CHANGELOG)) {
            ChangelogDialog dialog = ChangelogDialog.create(false, getResources().getColor(R.color.accent));
            dialog.show(getFragmentManager(), "changelog");
        } else if (preference.getKey().equals(NOTIF_SYNC)) {
            showNotifDialog();
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(PREF_INTERCEPT)) {
            Boolean value = (Boolean) newValue;

            int flag = value ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

            ComponentName componentName = new ComponentName(getActivity(), Interceptor.class);
            getActivity().getPackageManager().setComponentEnabledSetting(componentName, flag, PackageManager.DONT_KILL_APP);

        } else if (preference.getKey().equals(REPOS_SORT)) {
            GitskariosSettings settings = new GitskariosSettings(getActivity());
            settings.saveRepoSort(String.valueOf(newValue));
        } else if (preference.getKey().equals(REPOS_FILE_TYPE)) {
            GitskariosSettings settings = new GitskariosSettings(getActivity());
            settings.saveDownloadFileType(String.valueOf(newValue));
        }
        return true;
    }

    private void showNotifDialog() {
        AccountManager accountManager = AccountManager.get(getActivity());
        final Account[] accounts = accountManager.getAccountsByType(getString(R.string.account_type));

        if (accounts.length > 0) {
            if (accounts.length > 1) {
                String[] names = new String[accounts.length];
                for (int i = 0; i < accounts.length; i++) {
                    names[i] = accounts[i].name;
                }

                new MaterialDialog.Builder(getActivity())
                        .title(R.string.select_account)
                        .items(names)
                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                                Account account = accounts[i];
                                showNotifDialog(account);
                                return false;
                            }
                        })
                        .show();
            } else {
                showNotifDialog(accounts[0]);
            }
        }

    }

    private void showNotifDialog(final Account account) {
        final PreferencesHelper settings = new PreferencesHelper(getActivity());
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.title(R.string.sync_notifications);
        builder.content(R.string.sync_notifications_content);
        builder.positiveText(R.string.yes);
        builder.negativeText(R.string.no);
        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                settings.saveBooleanSetting(KEY_SYNC + "_" + account.name, true);
                ContentResolver.addPeriodicSync(
                        account,
                        getString(R.string.uri_content_provider),
                        Bundle.EMPTY,
                        SYNC_INTERVAL);
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                super.onNegative(dialog);
                settings.saveBooleanSetting(KEY_SYNC + "_" + account.name, false);
                ContentResolver.removePeriodicSync(
                        account,
                        getString(R.string.uri_content_provider),
                        Bundle.EMPTY);
            }
        })
                .show();
    }
}