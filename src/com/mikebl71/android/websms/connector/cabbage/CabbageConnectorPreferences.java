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

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper class for getting/setting connector preferences in the SharedPreferences.
 */
public class CabbageConnectorPreferences {

	/** Default location of the Cabbage Gateway */
	public static final String DEFAULT_CABBAGE_URL = "http://cabbagetexter.com/send.php";

	// Keys for connector preferences
	public static final String PREFS_ENABLED = "enable_connector";
	public static final String PREFS_CABBAGE_URL_DEFAULT = "cabbage_url_default";
	public static final String PREFS_CABBAGE_URL_CUSTOM = "cabbage_url_custom";


	/**
	 * Returns if the connector is enabled.
	 */
	public static boolean isEnabled(SharedPreferences prefs) {
		return prefs.getBoolean(PREFS_ENABLED, false);
	}

	/**
	 * Returns URL of the Cabbage Gateway.
	 */
	public static String getCabbageUrl(SharedPreferences prefs) {
		if (prefs.getBoolean(PREFS_CABBAGE_URL_DEFAULT, true)) {
			return DEFAULT_CABBAGE_URL;

		} else {
			return prefs.getString(PREFS_CABBAGE_URL_CUSTOM, null);
		}
	}

	/**
	 * Checks if the preference values are valid.
	 */
	public static boolean isValid(SharedPreferences prefs) {
		return AccountPreferences.getAccountIds(prefs).size() > 0 
				&& getCabbageUrl(prefs) != null;
	}

	/**
	 * Returns a reason why preferences were deemed invalid.
	 */
	public static String getInvalidityReason(Context context, SharedPreferences prefs) {
		if (AccountPreferences.getAccountIds(prefs).size() <= 0) {
			return context.getString(R.string.pref_err_no_accounts);

		} else if (getCabbageUrl(prefs) == null) {
			return context.getString(R.string.pref_err_empty_cabbage_url);

		} else {
			return "";
		}
	}

}
