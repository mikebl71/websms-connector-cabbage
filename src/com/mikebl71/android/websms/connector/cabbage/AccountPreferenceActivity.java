/*
 * Copyright (C) 2012 Mikhail Blinov
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package com.mikebl71.android.websms.connector.cabbage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Activity for configuring account preferences.
 */
public final class AccountPreferenceActivity extends PreferenceActivity {

	public static final int RESULT_DELETE = 5;

	public static final int DIALOG_DELETE_CONFIRM_ID  = 11;
	public static final int DIALOG_INVALID_CONFIRM_ID = 12;

	private static final String SCR_PREF_LABEL = "account_label";
	private static final String SCR_PREF_PROVIDER = "account_provider";
	private static final String SCR_PREF_CUSTOM_PROVIDER = "account_custom_provider";
	private static final String SCR_PREF_USERNAME = "account_username";
	private static final String SCR_PREF_PASSWORD = "account_password";

	private static final String CUSTOM_PROVIDER_VALUE = "custom";

	private String accId;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.account_prefs);

		// populate values from bundle
		Bundle accBundle;
		if (savedInstanceState != null) {
			accBundle = savedInstanceState;
		} else {
			accBundle = getIntent().getBundleExtra(AccountPreferences.ACCOUNT_BUNDLE);
		}
		initFromBundle(accBundle);

		// show preference value in the preference summary
		SetSummaryPreferenceChangeListener.register(getPreferenceScreen());

		// special treatment for providers
		final ListPreference providerPref = (ListPreference) getPreferenceScreen().findPreference(SCR_PREF_PROVIDER);
		final EditTextPreference customProviderPref = (EditTextPreference) getPreferenceScreen().findPreference(SCR_PREF_CUSTOM_PROVIDER);

		providerPref.setOnPreferenceChangeListener(new SetSummaryPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// custom provider field is only enabled if the "custom" provider is selected
				if (newValue.equals(CUSTOM_PROVIDER_VALUE)) {
					customProviderPref.setEnabled(true);
				} else {
					customProviderPref.setEnabled(false);
					customProviderPref.setText(null);
					customProviderPref.setSummary(null);
				}
				return super.onPreferenceChange(preference, newValue);
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		extractIntoBundle(savedInstanceState);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.account_prefs_menu, menu);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_delete_account:
			// confirm deleting an account
			showDialog(DIALOG_DELETE_CONFIRM_ID);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onBackPressed () {
		Bundle accBundle = new Bundle();
		extractIntoBundle(accBundle);

		if (AccountPreferences.isValid(accBundle)) {
			finishWithOk();

		} else if (AccountPreferences.isEmpty(accBundle)) {
			// no values were populated so it looks like user did not really want to create this account - delete
			finishWithDeleteAccount();

		} else {
			// some values were populates but they are invalid - display an alert
			showDialog(DIALOG_INVALID_CONFIRM_ID);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder;
		switch (id) {
		case DIALOG_DELETE_CONFIRM_ID:
			// confirm deleting an account
			builder = new AlertDialog.Builder(this);
			builder
				.setMessage(R.string.delete_account_conf)
				.setCancelable(true)

				.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// delete the account
						finishWithDeleteAccount();
					}
				})

				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// return to edit
						dialog.cancel();
					}
				});
			return builder.create();

		case DIALOG_INVALID_CONFIRM_ID:
			// display an alert that the preferences are invalid 
			builder = new AlertDialog.Builder(this);
			builder
				.setMessage(R.string.invalid_account_conf)
				.setCancelable(true)

				.setPositiveButton(R.string.fix, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// return to edit
						dialog.cancel();
					}
				})

				.setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// delete the account
						finishWithDeleteAccount();
					}
				});
			return builder.create();
		}
		return null;
	}


	/**
	 * Finishes activity with OK.
	 */
	private void finishWithOk() {
		Intent intent = new Intent();
		Bundle accBundle = new Bundle();
		extractIntoBundle(accBundle);
		intent.putExtra(AccountPreferences.ACCOUNT_BUNDLE, accBundle);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	/**
	 * Finishes activity and requests the parent to delete the account.
	 */
	private void finishWithDeleteAccount() {
		Intent intent = new Intent();
		Bundle accBundle = new Bundle();
		extractIntoBundle(accBundle);
		intent.putExtra(AccountPreferences.ACCOUNT_BUNDLE, accBundle);
		setResult(RESULT_DELETE, intent);
		finish();
	}

	/**
	 * Initialises preferences from account preference bundle.
	 */
	private void initFromBundle(Bundle accBundle) {
		if (accBundle != null) {
			String value;
			EditTextPreference pref;

			accId = AccountPreferences.getId(accBundle);

			value = AccountPreferences.getLabel(accBundle);
			if (isSet(value)) {
				pref = (EditTextPreference) getPreferenceScreen().findPreference(SCR_PREF_LABEL);
				pref.setText(value);
				pref.setSummary(value);
			}

			value = AccountPreferences.getUsername(accBundle);
			if (isSet(value)) {
				pref = (EditTextPreference) getPreferenceScreen().findPreference(SCR_PREF_USERNAME);
				pref.setText(value);
				pref.setSummary(value);
			}

			value = AccountPreferences.getPassword(accBundle);
			if (isSet(value)) {
				pref = (EditTextPreference) getPreferenceScreen().findPreference(SCR_PREF_PASSWORD);
				pref.setText(value);
				pref.setSummary("***");	
			}

			ListPreference providerPref = (ListPreference) getPreferenceScreen().findPreference(SCR_PREF_PROVIDER);
			EditTextPreference customProviderPref = (EditTextPreference) getPreferenceScreen().findPreference(SCR_PREF_CUSTOM_PROVIDER);

			String listedProvValue = AccountPreferences.getListedProvider(accBundle);
			String provValue = AccountPreferences.getProvider(accBundle);

			if (!isSet(listedProvValue)) {
				customProviderPref.setEnabled(false);

			} else if (listedProvValue.equals(CUSTOM_PROVIDER_VALUE)) {
				providerPref.setValue(listedProvValue);
				providerPref.setSummary(providerPref.getEntry());

				customProviderPref.setText(provValue);
				customProviderPref.setSummary(provValue);
				customProviderPref.setEnabled(true);

			} else {
				providerPref.setValue(listedProvValue);
				providerPref.setSummary(providerPref.getEntry());

				customProviderPref.setEnabled(false);
			}
		}
	}

	/**
	 * Extracts preferences into account preference bundle.
	 */
	private void extractIntoBundle(Bundle accBundle) {
		EditTextPreference pref;

		AccountPreferences.setId(accBundle, accId);

		pref = (EditTextPreference) getPreferenceScreen().findPreference(SCR_PREF_LABEL);
		AccountPreferences.setLabel(accBundle, pref.getText());

		pref = (EditTextPreference) getPreferenceScreen().findPreference(SCR_PREF_USERNAME);
		AccountPreferences.setUsername(accBundle, pref.getText());

		pref = (EditTextPreference) getPreferenceScreen().findPreference(SCR_PREF_PASSWORD);
		AccountPreferences.setPassword(accBundle, pref.getText());

		ListPreference providerPref = (ListPreference) getPreferenceScreen().findPreference(SCR_PREF_PROVIDER);
		String listedProvValue = providerPref.getValue();

		if (!isSet(listedProvValue)) {
			AccountPreferences.setProvider(accBundle, null);
			AccountPreferences.setListedProvider(accBundle, null);
			AccountPreferences.setDisplayProvider(accBundle, null);

		} else if (listedProvValue.equals(CUSTOM_PROVIDER_VALUE)) {
			EditTextPreference customProviderPref = (EditTextPreference) getPreferenceScreen().findPreference(SCR_PREF_CUSTOM_PROVIDER);
			AccountPreferences.setProvider(accBundle, customProviderPref.getText());
			AccountPreferences.setListedProvider(accBundle, listedProvValue);
			AccountPreferences.setDisplayProvider(accBundle, customProviderPref.getText());

		} else {
			AccountPreferences.setProvider(accBundle, listedProvValue);
			AccountPreferences.setListedProvider(accBundle, listedProvValue);
			AccountPreferences.setDisplayProvider(accBundle, providerPref.getEntry().toString());
		}
	}


	private static boolean isSet(String value) {
		return value != null && value.length() > 0;
	}

}
