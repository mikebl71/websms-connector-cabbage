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

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * Helper class for getting/setting account preferences
 * in the SharedPreferences or in a Bundle.
 * 
 * Account preferences are stored in the SharedPreferences using keys in the form of
 * "account_N_pref" where N is the account id.
 * So, for example, a preference with key "account_2_label" will store the label of the account #2.
 * 
 * Bundles are used to pass account preferences between activities.
 * Account preferences are stored in a Bundle using keys in the form of "account__pref". 
 * So, for example, a preference with key "account__label" will store the account label.
 */
public class AccountPreferences {

	// --- Public constants ---

	/** Name of bundle with account preferences. */
	public static final String ACCOUNT_BUNDLE = "account_bundle";

	// Some important providers
	public static final String PROVIDER_VODAFONE = "v";

	// --- Private constants ---

	// Prefix used by all account preference keys
	private static final String ACC_PREF_PREFIX = "account_";

	// Available suffixes for account preference keys
	private static final String LABEL_ACC_PREF_SUFFIX = "_label";
	private static final String PROVIDER_ACC_PREF_SUFFIX = "_provider";
	private static final String LISTEDPROV_ACC_PREF_SUFFIX = "_listedprov";
	private static final String DISPLAYPROV_ACC_PREF_SUFFIX = "_displayprov";
	private static final String USERNAME_ACC_PREF_SUFFIX = "_username";
	private static final String PASSWORD_ACC_PREF_SUFFIX = "_password";

	// Suffixes for internal account preference keys
	private static final String COOKIES_ACC_PREF_SUFFIX = "_cookies";

	// Keys for storing account preferences in a Bundle
	private static final String ID_BUNDLE_KEY = ACC_PREF_PREFIX + "_id";
	private static final String LABEL_BUNDLE_KEY = ACC_PREF_PREFIX + LABEL_ACC_PREF_SUFFIX;
	private static final String PROVIDER_BUNDLE_KEY = ACC_PREF_PREFIX + PROVIDER_ACC_PREF_SUFFIX;
	private static final String LISTEDPROV_BUNDLE_KEY = ACC_PREF_PREFIX + LISTEDPROV_ACC_PREF_SUFFIX;
	private static final String DISPLAYPROV_BUNDLE_KEY = ACC_PREF_PREFIX + DISPLAYPROV_ACC_PREF_SUFFIX;
	private static final String USERNAME_BUNDLE_KEY = ACC_PREF_PREFIX + USERNAME_ACC_PREF_SUFFIX;
	private static final String PASSWORD_BUNDLE_KEY = ACC_PREF_PREFIX + PASSWORD_ACC_PREF_SUFFIX;

	// Helper list of all suffixes
	private static final String[] ACC_PREF_SUFFIXES = new String[] { 
		LABEL_ACC_PREF_SUFFIX, 
		PROVIDER_ACC_PREF_SUFFIX, 
		LISTEDPROV_ACC_PREF_SUFFIX, 
		DISPLAYPROV_ACC_PREF_SUFFIX, 
		USERNAME_ACC_PREF_SUFFIX, 
		PASSWORD_ACC_PREF_SUFFIX };

	private static final String[] INTERNAL_ACC_PREF_SUFFIXES = new String[] { 
		COOKIES_ACC_PREF_SUFFIX }; 


	// --- Getters/Setters for preferences stored in the SharedPreferences ---

	public static String getLabel(SharedPreferences prefs, String accId) {
		return prefs.getString(ACC_PREF_PREFIX + accId + LABEL_ACC_PREF_SUFFIX, null);
	}

	public static String getProvider(SharedPreferences prefs, String accId) {
		return prefs.getString(ACC_PREF_PREFIX + accId + PROVIDER_ACC_PREF_SUFFIX, null);
	}

	public static String getListedProvider(SharedPreferences prefs, String accId) {
		return prefs.getString(ACC_PREF_PREFIX + accId + LISTEDPROV_ACC_PREF_SUFFIX, null);
	}

	public static String getDisplayProvider(SharedPreferences prefs, String accId) {
		return prefs.getString(ACC_PREF_PREFIX + accId + DISPLAYPROV_ACC_PREF_SUFFIX, null);
	}

	public static String getUsername(SharedPreferences prefs, String accId) {
		return prefs.getString(ACC_PREF_PREFIX + accId + USERNAME_ACC_PREF_SUFFIX, null);
	}

	public static String getPassword(SharedPreferences prefs, String accId) {
		return prefs.getString(ACC_PREF_PREFIX + accId + PASSWORD_ACC_PREF_SUFFIX, null);
	}

	public static String getCookies(SharedPreferences prefs, String accId) {
		return prefs.getString(ACC_PREF_PREFIX + accId + COOKIES_ACC_PREF_SUFFIX, null);
	}

	public static void setCookies(SharedPreferences prefs, String accId, String cookies) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(ACC_PREF_PREFIX + accId + COOKIES_ACC_PREF_SUFFIX, cookies);
		editor.commit();
	}


	// --- Getters/setters for preferences stored in a Bundle ---

	public static String getId(Bundle bundle) {
		return bundle.getString(ID_BUNDLE_KEY);
	}

	public static String getLabel(Bundle bundle) {
		return bundle.getString(LABEL_BUNDLE_KEY);
	}

	public static String getProvider(Bundle bundle) {
		return bundle.getString(PROVIDER_BUNDLE_KEY);
	}

	public static String getListedProvider(Bundle bundle) {
		return bundle.getString(LISTEDPROV_BUNDLE_KEY);
	}

	public static String getDisplayProvider(Bundle bundle) {
		return bundle.getString(DISPLAYPROV_BUNDLE_KEY);
	}

	public static String getUsername(Bundle bundle) {
		return bundle.getString(USERNAME_BUNDLE_KEY);
	}

	public static String getPassword(Bundle bundle) {
		return bundle.getString(PASSWORD_BUNDLE_KEY);
	}

	public static void setId(Bundle bundle, String id) {
		bundle.putString(ID_BUNDLE_KEY, id);
	}

	public static void setLabel(Bundle bundle, String label) {
		bundle.putString(LABEL_BUNDLE_KEY, label);
	}

	public static void setProvider(Bundle bundle, String provider) {
		bundle.putString(PROVIDER_BUNDLE_KEY, provider);
	}

	public static void setListedProvider(Bundle bundle, String listedProvider) {
		bundle.putString(LISTEDPROV_BUNDLE_KEY, listedProvider);
	}

	public static void setDisplayProvider(Bundle bundle, String displayProvider) {
		bundle.putString(DISPLAYPROV_BUNDLE_KEY, displayProvider);
	}

	public static void setUsername(Bundle bundle, String userName) {
		bundle.putString(USERNAME_BUNDLE_KEY, userName);
	}

	public static void setPassword(Bundle bundle, String password) {
		bundle.putString(PASSWORD_BUNDLE_KEY, password);
	}


	// --- Methods  ---

	/**
	 * Returns list of all known account ids in the numerically ascending order. 
	 */
	public static List<String> getAccountIds(SharedPreferences prefs) {
		List<String> accIds = new ArrayList<String>();
		List<Integer> accNumIds = new ArrayList<Integer>();

		for (String key : prefs.getAll().keySet()) {
			if (key.startsWith(ACC_PREF_PREFIX) && key.endsWith(LABEL_ACC_PREF_SUFFIX)) {

				String accId = key.substring(ACC_PREF_PREFIX.length(), key.length() - LABEL_ACC_PREF_SUFFIX.length());
				int accNumId = Integer.parseInt(accId);

				// order numerically for consistency
				int idx = 0;
				for (; idx < accNumIds.size(); idx++) {
					if (accNumId < accNumIds.get(idx)) {
						break;
					}
				}
				if (idx < accNumIds.size()) {
					accNumIds.add(idx, accNumId);
					accIds.add(idx, accId);
				} else {
					accNumIds.add(accNumId);
					accIds.add(accId);
				}
			}
		}
		return accIds;
	}

	/**
	 * Returns the next free account id. 
	 */
	public static String getNextAccountId(SharedPreferences prefs) {
		int lastAccNumId = 0;

		for (String key : prefs.getAll().keySet()) {
			if (key.startsWith(ACC_PREF_PREFIX) && key.endsWith(LABEL_ACC_PREF_SUFFIX)) {

				String accId = key.substring(ACC_PREF_PREFIX.length(), key.length() - LABEL_ACC_PREF_SUFFIX.length());
				int accNumId = Integer.parseInt(accId);

				if (accNumId > lastAccNumId) {
					lastAccNumId = accNumId;
				}
			}
		}
		return Integer.toString(lastAccNumId + 1);
	}

	/**
	 * Updates account preferences in the SharedPreferences with preferences from the Bundle. 
	 */
	public static void updateAccount(SharedPreferences prefs, Bundle bundle) {
		String accId = getId(bundle);
		if (accId != null) {
			SharedPreferences.Editor editor = prefs.edit();

			for (String suffix : ACC_PREF_SUFFIXES) {
				editor.putString(ACC_PREF_PREFIX + accId + suffix, bundle.getString(ACC_PREF_PREFIX + suffix));
			}
			for (String suffix : INTERNAL_ACC_PREF_SUFFIXES) {
				editor.remove(ACC_PREF_PREFIX + accId + suffix);
			}

			editor.commit();
		}
	}

	/**
	 * Removes the account mentioned in the Bundle from the SharedPreferences. 
	 */
	public static void deleteAccount(SharedPreferences prefs, Bundle bundle) {
		String accId = getId(bundle);
		if (accId != null) {
			SharedPreferences.Editor editor = prefs.edit();

			for (String suffix : ACC_PREF_SUFFIXES) {
				editor.remove(ACC_PREF_PREFIX + accId + suffix);
			}
			for (String suffix : INTERNAL_ACC_PREF_SUFFIXES) {
				editor.remove(ACC_PREF_PREFIX + accId + suffix);
			}

			editor.commit();
		}
	}

	/**
	 * Checks if an account for the given provider exists.
	 */
	public static boolean isProviderConfigured(SharedPreferences prefs, String provider) {
		for (String key : prefs.getAll().keySet()) {
			if (key.startsWith(ACC_PREF_PREFIX) && key.endsWith(PROVIDER_ACC_PREF_SUFFIX)) {

				String accProvider = prefs.getString(key, "");
				if (accProvider.equals(provider)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Creates an empty account preferences Bundle with a new account id. 
	 */
	public static Bundle createNewBundle(SharedPreferences prefs) {
		Bundle bundle = new Bundle();
		setId(bundle, getNextAccountId(prefs));
		return bundle;
	}

	/**
	 * Extracts all preferences of the given account from the SharedPreferences into a Bundle. 
	 */
	public static Bundle extractToBundle(SharedPreferences prefs, String accId) {
		Bundle bundle = new Bundle();

		for (String suffix : ACC_PREF_SUFFIXES) {
			bundle.putString(ACC_PREF_PREFIX + suffix, prefs.getString(ACC_PREF_PREFIX + accId + suffix, null));
		}
		setId(bundle, accId);

		return bundle;
	}

	/**
	 * Checks if the account preferences Bundle contains no values.
	 */
	public static boolean isEmpty(Bundle bundle) {
		return TextUtils.isEmpty(getLabel(bundle))
				&& TextUtils.isEmpty(getProvider(bundle))
				&& TextUtils.isEmpty(getUsername(bundle))
				&& TextUtils.isEmpty(getPassword(bundle));
	}

	/**
	 * Checks if account preference values stored in the Bundle are valid.
	 */
	public static boolean isValid(Bundle bundle) {
		return !TextUtils.isEmpty(getLabel(bundle))
				&& !TextUtils.isEmpty(getProvider(bundle))
				&& !TextUtils.isEmpty(getUsername(bundle))
				&& !TextUtils.isEmpty(getPassword(bundle));
	}

}
