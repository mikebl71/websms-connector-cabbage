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

import java.text.MessageFormat;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import de.ub0r.android.websms.connector.common.ConnectorPreferenceActivity;

/**
 * Activity for configuring connector preferences.
 */
public final class CabbageConnectorPreferenceActivity extends ConnectorPreferenceActivity {

	private static final String PREFS_ACCOUNTS_CATEGORY = "accounts";

	public static final int DIALOG_INVALID_CONFIRM_ID = 12;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.connector_prefs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onResume() {
		super.onResume();
		populateAccountList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.connector_prefs_menu, menu);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_new_account:
			// launch activity to populate values for the new account
			Intent intent = new Intent(this, AccountPreferenceActivity.class);
			intent.putExtra(AccountPreferences.ACCOUNT_BUNDLE, 
					AccountPreferences.createNewBundle(getPreferenceManager().getSharedPreferences()));
			startActivityForResult(intent, 0);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		Bundle bundle = intent.getBundleExtra(AccountPreferences.ACCOUNT_BUNDLE);

		switch (resultCode) {
		case Activity.RESULT_OK:
			AccountPreferences.updateAccount(prefs, bundle);
			break;
			
		case AccountPreferenceActivity.RESULT_DELETE:
			AccountPreferences.deleteAccount(prefs, bundle);
			break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onBackPressed () {
		SharedPreferences prefs = getPreferenceManager().getSharedPreferences();

		// if enabled then validate the preferences and display an alert if they are invalid  
		if (CabbageConnectorPreferences.isEnabled(prefs) && !CabbageConnectorPreferences.isValid(prefs)) {
			showDialog(DIALOG_INVALID_CONFIRM_ID);

		} else {
			super.onBackPressed();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder;
		switch (id) {
		case DIALOG_INVALID_CONFIRM_ID:
			// display an alert that the preferences are invalid 
			builder = new AlertDialog.Builder(this);
			builder
				.setMessage(getErrorMessage(getApplicationContext()))
				.setCancelable(true)

				.setPositiveButton(R.string.disable, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// mark the connector as disabled
						finishWithDisable();
					}
				})

				.setNegativeButton(R.string.edit, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// return to edit
						dialog.cancel();
					}
				});
			return builder.create();
		}
		return null;
	}


	/**
	 * Returns message describing why preferences were deemed invalid.
	 */
	private String getErrorMessage(Context context)
	{
		SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		String invalidityReason = CabbageConnectorPreferences.getInvalidityReason(context, prefs);
		
		String msgTemplate = context.getString(R.string.invalid_connector_conf);

		return MessageFormat.format(msgTemplate, invalidityReason);
	}

	/**
	 * Marks the connector as disabled and finish.
	 */
	private void finishWithDisable() {
		SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(CabbageConnectorPreferences.PREFS_ENABLED, false);
		editor.commit();
		finish();
	}

	/**
	 * Populates list of known accounts on the preferences screen.
	 */
	private void populateAccountList() {
		SharedPreferences sharedPrefs = getPreferenceManager().getSharedPreferences();
		PreferenceCategory accPrefCategory = (PreferenceCategory) findPreference(PREFS_ACCOUNTS_CATEGORY);

		accPrefCategory.removeAll();

		List<String> accountIds = AccountPreferences.getAccountIds(sharedPrefs);
		for (String accId : accountIds) {
			Preference pref = new Preference(getApplicationContext());

			pref.setKey(accId.toString());
			pref.setTitle(AccountPreferences.getLabel(sharedPrefs, accId));

			pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				public boolean onPreferenceClick(final Preference preference) {
					// launch activity to edit account preferences
					Intent intent = new Intent(CabbageConnectorPreferenceActivity.this, AccountPreferenceActivity.class);
					intent.putExtra(AccountPreferences.ACCOUNT_BUNDLE, 
							AccountPreferences.extractToBundle(
									CabbageConnectorPreferenceActivity.this.getPreferenceManager().getSharedPreferences(), 
									preference.getKey()));
					startActivityForResult(intent, 0);
					return true;
				}
			});

			accPrefCategory.addPreference(pref);

			pref.setDependency(CabbageConnectorPreferences.PREFS_ENABLED);
		}
	}

}
