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

import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.text.InputType;

/**
 * Populates preference summary with preference value.
 */
public class SetSummaryPreferenceChangeListener	implements Preference.OnPreferenceChangeListener {

	public boolean onPreferenceChange(Preference pref, Object newValue) {
		if (pref instanceof ListPreference) {
			ListPreference listPref = (ListPreference) pref;
			int valueIdx = listPref.findIndexOfValue(String.valueOf(newValue));
			pref.setSummary(valueIdx >= 0 ? listPref.getEntries()[valueIdx] : null);

		} else if (pref instanceof EditTextPreference) { 
			EditTextPreference textPref = (EditTextPreference) pref;
			if ((textPref.getEditText().getInputType() & InputType.TYPE_TEXT_VARIATION_PASSWORD) == 0) {
				pref.setSummary(newValue.toString());
			} else {
				pref.setSummary("***");
			}
		} else {
			pref.setSummary(newValue.toString());
		}
		return true;
	}
	
	public static void register(PreferenceGroup prefGroup) {
		for (int idx = 0; idx < prefGroup.getPreferenceCount(); idx++) {
			Preference pref = prefGroup.getPreference(idx);
			
			if (pref instanceof PreferenceGroup) {
				register((PreferenceGroup)pref);
			} else {
				pref.setOnPreferenceChangeListener(new SetSummaryPreferenceChangeListener());
			}
		}
	}

}
