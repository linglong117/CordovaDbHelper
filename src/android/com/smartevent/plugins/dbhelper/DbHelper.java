/*
 * Copyright (c) 2012-2013, Chris Brody
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */

package com.smartevent.plugins.dbhelper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.util.Log;

@SuppressLint("SdCardPath")
public class DbHelper extends CordovaPlugin {
	private PGSQLitePlugin db;

	/**
	 * NOTE: Using default constructor, no explicit constructor.
	 */

	/**
	 * Executes the request and returns PluginResult.
	 * 
	 * @param actionAsString
	 *            The action to execute.
	 * @param args
	 *            JSONArry of arguments for the plugin.
	 * @param cbc
	 *            Callback context from Cordova API
	 * @return Whether the action was valid.
	 */
	@Override
	public boolean execute(String actionAsString, JSONArray args,
			CallbackContext cbc) {
		Action action;
		try {
			// action = Action.valueOf(actionAsString);
			action = Action.valueOf(actionAsString);
		} catch (IllegalArgumentException e) {
			// shouldn't ever happen
			Log.e(DbHelper.class.getSimpleName(), "unexpected error", e);
			return false;
		}
		try {
			return executeAndPossiblyThrow(action, args.getJSONArray(0), cbc);
		} catch (JSONException e) {
			// TODO: signal JSON problem to JS
			Log.e(DbHelper.class.getSimpleName(), "unexpected error", e);
			return false;
		}
	}

	private boolean executeAndPossiblyThrow(Action action, JSONArray args,
			CallbackContext cbc) throws JSONException {
		boolean status = true;
		
		if(action ==Action.postArray || action == Action.deleteArray)
		{
			db = new PGSQLitePlugin(this.cordova.getActivity(), args.getJSONArray(0));
			db.openDatabese(args.getJSONArray(0));
		}else {
			db = new PGSQLitePlugin(this.cordova.getActivity(), args);
			db.openDatabese(args);
		}
		
		PluginResult pluginResult = null;
		switch (action) {
		case put:
		{
			pluginResult = db.insertQuery(args);
			cbc.sendPluginResult(pluginResult);
		}
			break;
		case get:
		{
			pluginResult = db.query(args);
			cbc.sendPluginResult(pluginResult);
		}
			break;
		case post:
		{
 			pluginResult =  db.updateQuery(args);
 			// String str = args.toString();
 			//cbc.success(args);
             cbc.sendPluginResult(pluginResult);
		}
			break;
        case postArray:
        {
             pluginResult = null;
              for(int i=0;i<args.length();i++)
              {
                 pluginResult = db.updateQuery(args.getJSONArray(i));
              }
                 //String str = args.toString();
             //cbc.success(args);
                 cbc.sendPluginResult(pluginResult);
        }
                break;
		case delete:
		{
			 pluginResult = db.deleteQuery(args);
			 //cbc.success(args);
             cbc.sendPluginResult(pluginResult);
		}
			break;
		case deleteArray:
		{
              pluginResult = null;
			 for(int i=0;i<args.length();i++)
			 {
			 	pluginResult=db.deleteQuery(args.getJSONArray(i));
			 }
			 //cbc.success(args);
              cbc.sendPluginResult(pluginResult);
		}
			break;
		default:
			break;
		}
		return status;
	}

	private JSONArray putData() {
		JSONArray array = new JSONArray();
		array.put("smartevent.db");
		array.put("tteventattendercheckins");
		JSONObject obj = new JSONObject();
		try {
			obj.put("TenantId", "001");
			obj.put("AttenderId", "001");
			obj.put("PlaceId", "001");
			obj.put("DtCheckin", "001");
			obj.put("Descr", "001");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		array.put(obj);
		return array;
	}

	private void initListen(final JSONArray args) {
		// TODO Auto-generated method stub
		ScheduledExecutorService scheduler;
		scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
			}
		}, 0, 10000, TimeUnit.MILLISECONDS);

	}

	/**
	 * Clean up and close all open databases.
	 */
	@Override
	public void onDestroy() {

	}

	/*
	 * put
	 */
	void put(Action action) {

		Log.d("put>>>>>", "put here ");

	}

	/*
	 * put
	 */
	void get(Action action) {

		Log.d("put>>>>>", "put here ");

	}
}
