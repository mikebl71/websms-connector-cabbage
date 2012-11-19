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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.ub0r.android.websms.connector.common.BasicConnector;
import de.ub0r.android.websms.connector.common.ConnectorCommand;
import de.ub0r.android.websms.connector.common.ConnectorSpec;
import de.ub0r.android.websms.connector.common.ConnectorSpec.SubConnectorSpec;
import de.ub0r.android.websms.connector.common.Log;
import de.ub0r.android.websms.connector.common.Utils;
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

	/** Timeout for establishing a connection and waiting for a response from the server */
	private static final int TIMEOUT_MS = 30000;

	private static final Pattern FIRST_NUMBER = Pattern.compile("^(-?\\d+)");

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

		return connectorSpec;
	}

	@Override
	protected int getTimeout() {
		return TIMEOUT_MS;
	}

	@Override
	protected int getMaxHttpConnections(Context context, ConnectorSpec cs) {
		return cs.getSubConnectorCount() + 1;
	}

	@Override
	protected String getUrlSend(final Context context, ArrayList<BasicNameValuePair> d) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return CabbageConnectorPreferences.getCabbageUrl(prefs);
	}

	@Override
	protected String getUrlBalance(final Context context, ArrayList<BasicNameValuePair> d) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		addParam(d, "c", "1");
		return CabbageConnectorPreferences.getCabbageUrl(prefs);
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
	protected String getUserAgent() {
		return "Mozilla/5.0";
	}

	@Override
	protected String getUsername(Context context, ConnectorCommand command, ConnectorSpec cs) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String accId = command.getSelectedSubConnector();
		return AccountPreferences.getUsername(prefs, accId);
	}

	@Override
	protected String getPassword(Context context, ConnectorCommand command, ConnectorSpec cs) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String accId = command.getSelectedSubConnector();
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
		String accId = command.getSelectedSubConnector();
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
			synchronized (SYNC_UPDATE) {
				cs.getSubConnector(command.getSelectedSubConnector()).setBalance(retCode);
			}
		} else {
			throw new WebSMSException(getErrorMessage(context, retNumCode));
		}
	}

	@Override
	protected void onNewRequest(final Context context,
			final ConnectorSpec reqSpec, final ConnectorCommand command) {
		// restore balance info that we might have lost from the request
		if (reqSpec != null) {
			ConnectorSpec connSpec = this.getSpec(context);
			SubConnectorSpec[] connSubs = connSpec.getSubConnectors();
			SubConnectorSpec[] reqSubs = reqSpec.getSubConnectors();
			
			for (int idx = 0; idx < connSubs.length && idx < reqSubs.length; idx++) {
				String connBalance = connSubs[idx].getBalance();
				String reqBalance = reqSubs[idx].getBalance();
	
				if (connBalance == null && reqBalance != null) {
					connSubs[idx].setBalance(reqBalance);
				}
			}
		}
	}

	@Override
	protected void doUpdate(final Context context, final Intent intent) {

		ConnectorSpec cs = this.getSpec(context);
		int subCount = cs.getSubConnectorCount();
		SubConnectorSpec[] subs = cs.getSubConnectors();

		List<Callable<Void>> tasks = new ArrayList<Callable<Void>>(subCount);
		for (SubConnectorSpec sub : subs) {
			final String subId = sub.getID();

			tasks.add(new Callable<Void>() {
				public Void call() throws Exception {
					// clone intent and assign it to this sub connector
					Intent subIntent = new Intent(intent);
					ConnectorCommand cmd = new ConnectorCommand(subIntent);
					cmd.setSelectedSubConnector(subId);
					cmd.setToIntent(subIntent);
					// update for this subconnector
					CabbageConnector.super.doUpdate(context, subIntent);
					return null;
				}
			});
		}

		try {
			ExecutorService executor = Executors.newFixedThreadPool(subCount);
			// execute all updates in parallel and wait till all are complete
			List<Future<Void>> results = executor.invokeAll(tasks);
			executor.shutdownNow();

			// if any of the updates failed then re-throw the first exception
			// (which will then be returned to WebSMS)
			for (int idx = 0; idx < results.size(); idx++) {
				Future<Void> result = results.get(idx);
				try {
					result.get();
				} catch (ExecutionException ex) {
					String subName = subs[idx].getName();
					throw new WebSMSException(subName + ": " + ConnectorSpec.convertErrorMessage(context, ex.getCause()));
				}
			}
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
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
	
}
