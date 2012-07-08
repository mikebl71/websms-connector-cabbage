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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.ub0r.android.websms.connector.common.BasicConnector;
import de.ub0r.android.websms.connector.common.ConnectorCommand;
import de.ub0r.android.websms.connector.common.ConnectorSpec;
import de.ub0r.android.websms.connector.common.Utils;
import de.ub0r.android.websms.connector.common.ConnectorSpec.SubConnectorSpec;
import de.ub0r.android.websms.connector.common.Log;
import de.ub0r.android.websms.connector.common.WebSMSException;

/**
 * Main class for Cabbage Connector.
 * Receives commands from WebSMS and acts upon them.
 */
public class CabbageConnector extends BasicConnector {

	/** Logging tag */
	public static final String TAG = "cabbage";

	/** Prefix used by resources with error messages */
	private static final String ERR_MESSAGE_PREFIX = "cabbage_err_";

	/** Id of the dummy subconnector */
	private static final String DUMMY_SUB_CONNECTOR_ID = "0";

	private static final Pattern FIRST_NUMBER = Pattern.compile("^(-?\\d+)");

	private volatile String cabbageScriptUrl = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ConnectorSpec initSpec(final Context context) {
		final String connectorName = context.getString(R.string.connector_cabbage_name);

		// create ConnectorSpec
		ConnectorSpec connectorSpec = new ConnectorSpec(connectorName);
		connectorSpec.setAuthor(context.getString(R.string.connector_cabbage_author));
		connectorSpec.setBalance(null);

		connectorSpec.setCapabilities(ConnectorSpec.CAPABILITIES_UPDATE
				| ConnectorSpec.CAPABILITIES_SEND
				| ConnectorSpec.CAPABILITIES_PREFS);
		
		// init subconnectors
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		List<String> accIds = AccountPreferences.getAccountIds(prefs);

		if (accIds.size() > 0) {
			for (String accId : accIds) {
				connectorSpec.addSubConnector(accId, 
						AccountPreferences.getLabel(prefs, accId),
						SubConnectorSpec.FEATURE_MULTIRECIPIENTS);
			}
			Log.d(TAG, "initSpec: inited with " + accIds.size() + " subconnectors");
		} else {
			// WebSMS requires connectors to have at least one subconnector hence creating a dummy one
			connectorSpec.addSubConnector(DUMMY_SUB_CONNECTOR_ID, 
					"dummy",
					SubConnectorSpec.FEATURE_NONE);
			Log.d(TAG, "initSpec: inited with dummy subconnector");
		}

		return connectorSpec;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ConnectorSpec updateSpec(final Context context,
			final ConnectorSpec connectorSpec) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		if (CabbageConnectorPreferences.isEnabled(prefs) && CabbageConnectorPreferences.isValid(prefs)) {
			connectorSpec.setReady();
			Log.d(TAG, "updateSpec: set ready");
		} else {
			connectorSpec.setStatus(ConnectorSpec.STATUS_INACTIVE);
			Log.d(TAG, "updateSpec: set inactive");
		}

		// TODO this should be done in getUrlSend but, unfortunately, 
		//      getUrlSend doesn't get the context, so has to be done beforehand
		cabbageScriptUrl = CabbageConnectorPreferences.getCabbageUrl(prefs);

		return connectorSpec;
	}

	@Override
	protected String getUrlSend(ArrayList<BasicNameValuePair> d) {
		// TODO cabbage script url should come from preferences but, unfortunately, 
		//      getUrlSend doesn't get the context, so had to be retrieved beforehand
		return cabbageScriptUrl;
	}

	@Override
	protected String getUrlBalance(ArrayList<BasicNameValuePair> d) {
		addParam(d, "c", "1");
		// TODO cabbage script url should come from preferences but, unfortunately, 
		//      getUrlBalance doesn't get the context, so had to be retrieved beforehand
		return cabbageScriptUrl;
	}

	@Override
	protected String getParamUsername() {
		return "u";
	}

	@Override
	protected String getParamPassword() {
		return "p";
	}

	@Override
	protected String getParamRecipients() {
		return "d";
	}

	@Override
	protected String getParamText() {
		return "m";
	}

	@Override
	protected String getParamSender() {
		return "name";
	}

	@Override
	protected String getEncoding() {
		return "UTF-8";
	}

	@Override
	protected String getUsername(Context context, ConnectorCommand command, ConnectorSpec cs) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String accId = getAccountId(command, prefs);
		return AccountPreferences.getUsername(prefs, accId);
	}

	@Override
	protected String getPassword(Context context, ConnectorCommand command, ConnectorSpec cs) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String accId = getAccountId(command, prefs);
		return AccountPreferences.getPassword(prefs, accId);
	}

	@Override
	protected String getSender(Context context, ConnectorCommand command, ConnectorSpec cs) {
		return command.getDefSender();
	}

	@Override
	protected String getRecipients(ConnectorCommand command) {
		return Utils.joinRecipientsNumbers(command.getRecipients(), ",", false /*oldFormat*/);
	}

	@Override
	protected void addExtraArgs(final Context context, 
			final ConnectorCommand command, 
			final ConnectorSpec cs, 
			final ArrayList<BasicNameValuePair> d) {

		// populate mobile provider selection parameter
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String accId = getAccountId(command, prefs);
		addParam(d, "s", AccountPreferences.getProvider(prefs, accId));
	}
	
	@Override
	protected void parseResponse(Context context, ConnectorCommand command, ConnectorSpec cs, String htmlText) {
		Log.d(TAG, "parseResponse: received response: [" + htmlText + "]");

		// some free php hosting sites add a trailer to all pages, so take the first number from the response
		Matcher m = FIRST_NUMBER.matcher(htmlText);
		if (!m.find()) {
			throw new WebSMSException(context.getString(R.string.cabbage_err_unexpected));
		}
		String retCode = m.group();
		int retNumCode = Integer.parseInt(retCode);
		
		if (retNumCode >= 0) {
			cs.setBalance(retCode);
		} else {
			throw new WebSMSException(getErrorMessage(context, retNumCode));
		}
	}

	private String getErrorMessage(Context context, int retNumCode)
	{
		String msgIdStr = ERR_MESSAGE_PREFIX + Integer.toString(-retNumCode);
		int msgId = context.getResources().getIdentifier(msgIdStr, "string", context.getPackageName());
		if (msgId > 0) {
			return context.getString(msgId);
		} else {
			String msgTemplate = context.getString(R.string.cabbage_err_N);
			return MessageFormat.format(msgTemplate, retNumCode);
		}
	}
	
	private String getAccountId(ConnectorCommand command, SharedPreferences prefs) {
		String accId = command.getSelectedSubConnector();
		if (accId == null) {
			// TODO  for updateBalance, the SubConnector is not set.  Bug in WebSMS ?
			accId = AccountPreferences.getAccountIds(prefs).get(0);
		}
		return accId;
	}
}
