package com.smartevent.plugins.dbhelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import android.R.bool;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class PGSQLitePlugin {

	private Hashtable<String, SQLiteDatabase> openDbs = new Hashtable<String, SQLiteDatabase>();
	private Context ctx;
	private String seDbName = "";

	public PGSQLitePlugin(Activity activity, JSONArray obj) {
		// TODO Auto-generated constructor stub
		this.ctx = activity;
	}

	public SQLiteDatabase getDb(String path) {
		SQLiteDatabase db = (SQLiteDatabase) openDbs.get(path);
		return db;
	}

	public String getStringAt(JSONArray data, int position, String dret) {
		String ret = getStringAt(data, position);
		return (ret == null) ? dret : ret;
	}

	public String getStringAt(JSONArray data, int position) {
		String ret = null;
		try {
			ret = data.getString(position);
			// JSONArray convert JavaScript undefined|null to string "null", fix
			// it
			ret = (ret.equals("null")) ? null : ret;
		} catch (Exception er) {
		}
		return ret;
	}

	public JSONArray getJSONArrayAt(JSONArray data, int position) {
		JSONArray ret = null;
		try {
			ret = (JSONArray) data.get(position);
		} catch (Exception er) {
		}
		;
		return ret;
	}

	public JSONObject getJSONObjectAt(JSONArray data, int position) {
		JSONObject ret = null;
		try {
			ret = (JSONObject) data.get(position);
		} catch (Exception er) {
		}
		;
		return ret;
	}

	public PluginResult query(JSONArray data) {
		PluginResult result = null;
		try {
			//Log.e("PGSQLitePlugin", "query");
			String dbName = data.getString(0);
			String tableName = data.getString(1);
			JSONArray columns = getJSONArrayAt(data, 2);
			String where = getStringAt(data, 3);
			JSONArray whereArgs = getJSONArrayAt(data, 4);
			String groupBy = getStringAt(data, 5);
			String having = getStringAt(data, 6);
			String orderBy = getStringAt(data, 7);
			String limit = getStringAt(data, 8);

			String[] _whereArgs = null;
			if (whereArgs != null) {
				int vLen = whereArgs.length();
				_whereArgs = new String[vLen];
				for (int i = 0; i < vLen; i++) {
					_whereArgs[i] = whereArgs.getString(i);
				}
			}
			String[] _columns = null;
			if (columns != null) {
				int vLen = columns.length();
				_columns = new String[vLen];
				for (int i = 0; i < vLen; i++) {
					_columns[i] = columns.getString(i);
				}
			}

			SQLiteDatabase db = getDb(dbName);
			Cursor cs = db.query(tableName, _columns, where, _whereArgs,
					groupBy, having, orderBy, limit);
			if (cs != null) {
				JSONObject res = new JSONObject();
				JSONArray rows = new JSONArray();

				if (cs.moveToFirst()) {
					String[] names = cs.getColumnNames();
					int namesCoint = names.length;
					do {
						JSONObject row = new JSONObject();
						for (int i = 0; i < namesCoint; i++) {
							String name = names[i];
							row.put(name, cs.getString(cs.getColumnIndex(name)));
						}
						rows.put(row);
					} while (cs.moveToNext());
				}
				res.put("rows", rows);
				cs.close();
				Log.e("PGSQLitePlugin", "query::count=" + rows.length());
				result = new PluginResult(PluginResult.Status.OK, res);
			} else {
				result = new PluginResult(PluginResult.Status.ERROR,
						"Error execute query");
			}
		} catch (Exception e) {
			Log.e("PGSQLitePlugin", e.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
		}
		Log.e("=============", result.getMessage());
		return result;
	}

	
	public int query_having(JSONArray data) {
		PluginResult result = null;
		int rowCount =-1;
		try {
			//Log.e("PGSQLitePlugin", "query");
			String dbName = data.getString(0);
			String tableName = data.getString(1);
			JSONArray columns = getJSONArrayAt(data, 2);
			String where = getStringAt(data, 3);
			JSONArray whereArgs = getJSONArrayAt(data, 4);
//			String groupBy = getStringAt(data, 5);
//			String having = getStringAt(data, 6);
//			String orderBy = getStringAt(data, 7);
//			String limit = getStringAt(data, 8);

			String[] _whereArgs = null;
			if (whereArgs != null) {
				int vLen = whereArgs.length();
				_whereArgs = new String[vLen];
				for (int i = 0; i < vLen; i++) {
					_whereArgs[i] = whereArgs.getString(i);
				}
			}
			String[] _columns = null;
			if (columns != null) {
				int vLen = columns.length();
				_columns = new String[vLen];
				for (int i = 0; i < vLen; i++) {
					_columns[i] = columns.getString(i);
				}
			}

			SQLiteDatabase db = getDb(dbName);
			String[] ss = new String[2];
			Cursor cs = db.query(tableName, _columns, where, _whereArgs, "", "", "", "");
			
			if (cs != null) {
				JSONObject res = new JSONObject();
				JSONArray rows = new JSONArray();

				if (cs.moveToFirst()) {
					String[] names = cs.getColumnNames();
					int namesCoint = names.length;
					do {
						JSONObject row = new JSONObject();
						for (int i = 0; i < namesCoint; i++) {
							String name = names[i];
							row.put(name, cs.getString(cs.getColumnIndex(name)));
						}
						rows.put(row);
					} while (cs.moveToNext());
				}
				res.put("rows", rows);
				cs.close();
				//Log.e("PGSQLitePlugin", "query::count=" + rows.length());
				rowCount = rows.length();
				//result = new PluginResult(PluginResult.Status.OK, res);
			} else {
				//result = new PluginResult(PluginResult.Status.ERROR,"Error execute query");
				rowCount =-1;
				
			}
		} catch (Exception e) {
			Log.e("PGSQLitePlugin", e.getMessage());
			//result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
			rowCount=-1;
		}
		return rowCount;
	}

	public JSONArray query_having_in(JSONArray data) {
		PluginResult result = null;
		int rowCount =-1;
		JSONArray mresult = new JSONArray();
		try {
			Log.e("PGSQLitePlugin", "query");
			String dbName = data.getString(0);
			String tableName = data.getString(1);
			JSONArray columns = getJSONArrayAt(data, 2);
			String where = getStringAt(data, 3);
			//JSONArray whereArgs = getJSONArrayAt(data, 4);
			//String[] whereArgs = (String[]) data.get(4);
			String whereArgs = data.getString(4);
			
			String[] _columns = null;
			if (columns != null) {
				int vLen = columns.length();
				_columns = new String[vLen];
				for (int i = 0; i < vLen; i++) {
					_columns[i] = columns.getString(i);
				}
			}
				
			SQLiteDatabase db = getDb(dbName);
			String[] ss = new String[2];
			//Cursor cs = db.query(tableName, _columns, where, _whereArgs, "", "", "", "");
			//String strsql = "select " + _columns[0] +" from "+ tableName + " where attenderId in ( " + whereArgs + " )" ;
			String strsql = "select " + _columns[0] +" from "+ tableName + " where "+ _columns[0] + " in (" + whereArgs+ " )" ;
			
			Cursor  cs =	db.rawQuery(strsql, null);
			if (cs != null) {
				JSONObject res = new JSONObject();
				JSONArray rows = new JSONArray();
				if (cs.moveToFirst()) {
					String[] names = cs.getColumnNames();
					int namesCoint = names.length;
					do {
						JSONObject row = new JSONObject();
						JSONArray arr = new JSONArray();
						for (int i = 0; i < namesCoint; i++) {
							String name = names[i];
							row.put(name, cs.getString(cs.getColumnIndex(name)));
							arr.put(cs.getString(cs.getColumnIndex(name)));
						}
						rows.put(row);
						mresult.put(arr);
					} while (cs.moveToNext());
				}
				res.put("rows", rows);
				//mresult.put("rows", rows);
				cs.close();
				//Log.e("PGSQLitePlugin", "query::count=" + rows.length());
				rowCount = rows.length();
				//result = new PluginResult(PluginResult.Status.OK, res);
			} else {
				//result = new PluginResult(PluginResult.Status.ERROR,"Error execute query");
				rowCount =-1;
				
			}
		} catch (Exception e) {
			Log.e("PGSQLitePlugin", e.getMessage());
			//result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
			rowCount=-1;
		}
		return mresult;
	}
	
	public PluginResult queryScalar(JSONArray data) {
		PluginResult result = null;
		try {
			Log.e("PGSQLitePlugin", "query");
			String dbName = data.getString(0);
			String tableName = data.getString(1);
			JSONArray columns = getJSONArrayAt(data, 2);
			String where = getStringAt(data, 3);
			JSONArray whereArgs = getJSONArrayAt(data, 4);
			String groupBy = getStringAt(data, 5);
			String having = getStringAt(data, 6);
			String orderBy = getStringAt(data, 7);
			String limit = getStringAt(data, 8);

			String[] _whereArgs = null;
			if (whereArgs != null) {
				int vLen = whereArgs.length();
				_whereArgs = new String[vLen];
				for (int i = 0; i < vLen; i++) {
					_whereArgs[i] = whereArgs.getString(i);
				}
			}

			String[] _columns = null;
			if (columns != null) {
				int vLen = columns.length();
				_columns = new String[vLen];
				for (int i = 0; i < vLen; i++) {
					_columns[i] = columns.getString(i);
				}
			}

			SQLiteDatabase db = getDb(dbName);
			// if (db == null){
			// db=getDb("www/db/" + dbName);
			// }
			Cursor cs = db.query(tableName, _columns, where, _whereArgs,
					groupBy, having, orderBy, limit);

			if (cs != null) {
				JSONObject res = new JSONObject();
				JSONArray rows = new JSONArray();

				if (cs.moveToFirst()) {
					String[] names = cs.getColumnNames();
					int namesCoint = names.length;
					do {
						JSONObject row = new JSONObject();
						for (int i = 0; i < namesCoint; i++) {
							String name = names[i];
							row.put(name, cs.getString(cs.getColumnIndex(name)));
						}
						rows.put(row);
					} while (cs.moveToNext());
				}
				res.put("rows", rows);
				cs.close();
				Log.e("PGSQLitePlugin", "query::count=" + rows.length());
				result = new PluginResult(PluginResult.Status.OK, res);
			} else {
				result = new PluginResult(PluginResult.Status.ERROR,
						"Error execute query");
			}
		} catch (Exception e) {
			Log.e("PGSQLitePlugin", e.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
		}
		Log.e("=============", result.getMessage());
		return result;
	}

	public PluginResult updateQuery(JSONArray data) {
		PluginResult result = null;
		try {
			//Log.e("PGSQLitePlugin", "updateQuery");
			String dbName = data.getString(0);
			String tableName = data.getString(1);
			JSONObject values = (JSONObject) data.get(2);
			String where = getStringAt(data, 3, "1");
			JSONArray whereArgs = getJSONArrayAt(data, 4);
			
			//String strupdate = data.getString(4);

			String[] _whereArgs = null;
			if (whereArgs != null) {
				int vLen = whereArgs.length();
				_whereArgs = new String[vLen];
				for (int i = 0; i < vLen; i++) {
					_whereArgs[i] = whereArgs.getString(i);
				}
			}

			JSONArray names = values.names();
			int vLenVal = names.length();
			ContentValues _values = new ContentValues();
			for (int i = 0; i < vLenVal; i++) {
				String name = names.getString(i);
				_values.put(name, values.getString(name));
			}
			SQLiteDatabase db = getDb(dbName);
			long count = db.update(tableName, _values, where, _whereArgs);
			result = new PluginResult(PluginResult.Status.OK, count);
			//Log.e("PGSQLitePlugin", "updateQuery::count=" + count);

		} catch (Exception e) {
			Log.e("PGSQLitePlugin", e.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
		}

		return result;
	}
	
	public PluginResult updateQuery_in(JSONArray data) {
		PluginResult result = null;
		try {
			//Log.e("PGSQLitePlugin", "updateQuery");
			String dbName = data.getString(0);
			String tableName = data.getString(1);
			JSONObject values = (JSONObject) data.get(2);
			String where = getStringAt(data, 3, "1");
			//String where = getStringAt(data, 3, "1");

			//JSONArray whereArgs = getJSONArrayAt(data, 4);
			
			String strupdate = data.getString(4);

			JSONArray names = values.names();
			int vLenVal = names.length();
			ContentValues _values = new ContentValues();
			for (int i = 0; i < vLenVal; i++) {
				String name = names.getString(i);
				_values.put(name, values.getString(name));
			}

			SQLiteDatabase db = getDb(dbName);
			long count = db.update(tableName, _values, "attenderId in(?)", new String[]{"'3900d57b-636c-11e4-baec-f80f41fdc7f8','6a19faca-9d58-11e4-b656-f80f41fdc7f8'"});
			result = new PluginResult(PluginResult.Status.OK, count);
			//Log.e("PGSQLitePlugin", "updateQuery::count=" + count);
		} catch (Exception e) {
			Log.e("PGSQLitePlugin", e.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
		}

		return result;
	}

	public PluginResult deleteQuery(JSONArray data) {
		PluginResult result = null;
		try {
			Log.e("PGSQLitePlugin", "deleteQuery");
			String dbName = data.getString(0);
			String tableName = data.getString(1);
			String where = getStringAt(data, 2);
			JSONArray whereArgs = getJSONArrayAt(data, 3);
			String[] _whereArgs = null;
			if (whereArgs != null) {
				int vLen = whereArgs.length();
				_whereArgs = new String[vLen];
				for (int i = 0; i < vLen; i++) {
					_whereArgs[i] = whereArgs.getString(i);
				}
			}
			SQLiteDatabase db = getDb(dbName);
			long count = db.delete(tableName, where, _whereArgs);
			result = new PluginResult(PluginResult.Status.OK, count);
			Log.e("PGSQLitePlugin", "deleteQuery::count=" + count);

		} catch (Exception e) {
			Log.e("PGSQLitePlugin", e.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
		}

		return result;
	}

	/*
	 * public PluginResult insertQuery(JSONArray data) { PluginResult result =
	 * null; try { String dbName = data.getString(0); String tableName =
	 * data.getString(1); JSONObject values = (JSONObject) data.get(2);
	 * JSONArray names = values.names(); int vLen = names.length();
	 * SQLiteDatabase db = getDb(dbName); ContentValues _values = new
	 * ContentValues(); for (int i = 0; i < vLen; i++) { String name =
	 * names.getString(i); _values.put(name, values.getString(name)); } long id
	 * = db.insert(tableName, null, _values); if (id == -1) { result = new
	 * PluginResult(PluginResult.Status.ERROR, "Insert error"); } else { result
	 * = new PluginResult(PluginResult.Status.OK, id); } } catch (Exception e) {
	 * Log.e("PGSQLitePlugin", e.getMessage()); result = new
	 * PluginResult(PluginResult.Status.ERROR, e.getMessage()); } return result;
	 * }
	 */
	public PluginResult insertQuery(JSONArray data) {
		PluginResult result = null;
		try {
			Log.i("data length >>>> ", data.length()+"");
			String dbName = data.getString(0);
			String tableName = data.getString(1);
			JSONArray columns = data.getJSONArray(2);
			JSONArray values = data.getJSONArray(3);
			
			JSONArray  existsPkValuses = new JSONArray();
			String pk ="";
			JSONArray pkValues= new JSONArray();
			if(data.length()==6)
			{
				int rowCount=-1;
				pk = data.getString(4);
				pkValues = data.getJSONArray(5);
				//判断该条记录是否存在
				JSONArray selectData = new JSONArray();
				JSONArray updateData = new JSONArray();
				
				selectData.put(dbName);
				selectData.put(tableName);
				selectData.put(new JSONArray().put(pk));
				//selectData.put(pk+"=?");//where
				selectData.put(pk + "  in (?)");//where

				//				
				updateData.put(dbName);
				updateData.put(tableName);
				//updateData.put(tableName);
				
				//ContentValues _pkvalue =null; 
				JSONArray pkvalue = null;
				
				StringBuffer strbf_in = new StringBuffer();
				 String str_in="";
				String[] str_ins= null;
				str_ins = new String[pkValues.length()];
				for (int m = 0; m < pkValues.length(); m++) {
				    String column = pkValues.getJSONArray(m).getString(0).toString();
					column = "'"+column+"',";
				    strbf_in.append(column);
				    str_in = strbf_in.toString();
				    str_in = str_in.substring(0, str_in.length()-1);
				    
				    str_ins[m] = column;
				}
				
				selectData.put(str_in);
				//
				JSONArray  mresult  = query_having_in(selectData);
				String str_update="";
				if(mresult!=null && mresult.length()>0)
				{
					StringBuffer strbf_update= new StringBuffer();
					for (int m = 0; m < mresult.length(); m++) {
						pkvalue = pkValues.getJSONArray(m);
						existsPkValuses.put(pkvalue);
						
						String _pkvalue = mresult.getJSONArray(m).getString(0).toString();
						_pkvalue = "'" + _pkvalue + "',";
						strbf_update.append(_pkvalue);
					}
					str_update = strbf_update.toString();
					str_update = str_update.substring(0, str_update.length()-1);
					
					//更新时间
					JSONObject obj = new JSONObject();
					Date date = null;
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 获取当前时间，进一步转化为字符串
					date = new Date();
					String strTime = format.format(date);
					obj.put("SyncTime", strTime);
					
					SQLiteDatabase _db = getDb(dbName);
					String sql_update = "update " + tableName +" set SyncTime='"+ strTime +"' where  " +pk +" in("+str_update+")";
					_db.execSQL(sql_update);

//					updateData.put(obj);
//					updateData.put(pk + "in(?)");
//					updateData.put(str_update);
//				    // int r =	_db.update(tableName, obj, whereClause, whereArgs);
//					result = updateQuery(updateData);
					Log.d("update >>>> ", "  2015-1-20 12:28:17");
				}
				
				
					
//					// _pkvalue = new ContentValues();
//					pkvalue = pkValues.getJSONArray(m);
//					selectData.put(pkvalue);
//					// Cursor cs = db.query(tableName, _columns, where,
//					// _whereArgs, "", "", "", "");
//					if (pk.length() > 0) {
//						rowCount = query_having(selectData);
//						//Log.i("select  result >>>> ", rowCount + "");
//						if (rowCount > 0) {
//							existsPkValuses.put(pkvalue);
//							// update synctime
//							Date date = null;
//							SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 获取当前时间，进一步转化为字符串
//							date = new Date();
//							String str = format.format(date);
//							obj.put("SyncTime", str);
//
//							updateData.put(obj);
//							updateData.put(pk + "=?");
//							updateData.put(pkvalue);
//
//							result = updateQuery(updateData);
//							//Log.d("update  synctime result >>>>> ", m+ " >>  "+ result.getMessage());
//						}
//					}
					//Log.d("查询记录是否存在>>>>>","dddddsssss");
			
				//Log.d("查询记录是否存在>>>>>","dddd");

			}
			
			long id = 0;
			SQLiteDatabase db = getDb(dbName);
			ContentValues _values = null;
			JSONArray values_ = null;
			for (int i = 0; i < values.length(); i++) {
				values_ = values.getJSONArray(i);
				_values = new ContentValues();
				
				boolean fflag = false;
				for (int j = 0; j < values_.length(); j++) {
					if(pk.length()>0)
					{
						if(existsPkValuses.length()>0)
						{
							//判断 pk 是否已存在, 若存在跳出本次循环
							if(columns.getString(j).toLowerCase().equals(pk.toLowerCase()))
							{
								for(int n=0;n<existsPkValuses.length();n++)
								{
									//boolean flag = existsPkValuses.getJSONArray(n).getString(0).equals(values_.getString(j));
									if(existsPkValuses.getJSONArray(n).getString(0).equals(values_.getString(j)));
									{
										fflag=true;
										break;
									}
								}
							}
						}	
					}
					_values.put(columns.getString(j), values_.getString(j));
				}
				if(!fflag)
				{
					id = db.insert(tableName, null, _values);
				}
				//Log.d("i>>>>", i+"");
			}
			db.close();
			if(id==0){
				result = new PluginResult(PluginResult.Status.OK,
						"Insert zero");
			}else if (id == -1) {
				result = new PluginResult(PluginResult.Status.ERROR,
						"Insert error");
			} else {
				result = new PluginResult(PluginResult.Status.OK, id);
			}
		} catch (Exception e) {
			Log.e("PGSQLitePlugin", e.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
		}
		Log.i("500  rows  times >>>>>", "xxxx");
		return result;
	}

	private String getColumnStringByJsonArray(JSONArray columns)
	{
		String strColunm="";
		String[] _whereArgs = null;
		StringBuffer strbuf = new StringBuffer();
		try {
			if (columns != null) {
				int vLen = columns.length();
				_whereArgs = new String[vLen];
				for (int i = 0; i < vLen; i++) {
					String column = columns.getString(i).toString();
					column = column+"=? and ";
					strbuf.append(column);
				}
				strColunm = strbuf.toString();
				strColunm = strColunm.substring(0, strColunm.length()-4);
			}
		} catch (Exception e) {
			// TODO: handle exception
			return strColunm;
		}
		return strColunm;
	}
	
	public PluginResult batchRawQuery(JSONArray data) {
		return batchRawQuery(data, false);
	}

	PluginResult batchRawQuery(JSONArray data, boolean transaction) {
		PluginResult result = null;
		SQLiteDatabase db = null;
		try {
			Log.e("PGSQLitePlugin", "batchRawQuery");
			String dbName = data.getString(0);
			db = getDb(dbName);
			JSONArray batch = (JSONArray) data.get(1);
			int len = batch.length();
			if (transaction) {
				db.beginTransaction();
			}
			for (int i = 0; i < len; i++) {
				JSONObject el = (JSONObject) batch.get(i);
				String type = el.getString("type");
				JSONArray args = (JSONArray) el.get("opts");
				int len1 = args.length();
				JSONArray rData = new JSONArray();
				rData.put(dbName);
				for (int j = 0; j < len1; j++) {
					rData.put(args.get(j));
				}

				Log.e("PGSQLitePlugin", "batchRawQuery::type=" + type);

				if (type.equals("raw")) {
					result = rawQuery(rData);
				} else if (type.equals("insert")) {
					result = insertQuery(rData);
				} else if (type.equals("del")) {
					result = deleteQuery(rData);
				} else if (type.equals("query")) {
					result = query(rData);
				} else if (type.equals("update")) {
					result = updateQuery(rData);
				}
				if (result == null) {
					result = new PluginResult(PluginResult.Status.ERROR,
							"Unknow action");
					if (transaction) {
						db.endTransaction();
					}
					break;
				} else if (result.getStatus() != 1) {
					if (transaction) {
						db.endTransaction();
					}
					result = new PluginResult(PluginResult.Status.ERROR,
							result.getMessage());
					break;
				}
			}
			if (transaction) {
				db.setTransactionSuccessful();
				db.endTransaction();
			}
		} catch (Exception e) {
			if (db != null && db.inTransaction()) {
				db.endTransaction();
			}
			Log.e("PGSQLitePlugin", "error batch" + e.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
		}

		return result;
	}

	public PluginResult rawQuery(JSONArray data) {
		PluginResult result = null;
		try {
			String dbName = data.getString(0);
			String sql = data.getString(1);
			SQLiteDatabase db = getDb(dbName);

			Log.e("PGSQLitePlugin", "rawQuery action::sql=" + sql);

			Cursor cs = db.rawQuery(sql, new String[] {});
			JSONObject res = new JSONObject();
			JSONArray rows = new JSONArray();

			if (cs != null && cs.moveToFirst()) {
				String[] names = cs.getColumnNames();
				int namesCoint = names.length;
				do {
					JSONObject row = new JSONObject();
					for (int i = 0; i < namesCoint; i++) {
						String name = names[i];
						row.put(name, cs.getString(cs.getColumnIndex(name)));
					}
					rows.put(row);
				} while (cs.moveToNext());
				cs.close();
			}
			res.put("rows", rows);
			Log.e("PGSQLitePlugin", "rawQuery action::count=" + rows.length());
			result = new PluginResult(PluginResult.Status.OK, res);

		} catch (Exception e) {
			Log.e("PGSQLitePlugin", e.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
		}
		return result;
	}

	public PluginResult remove(JSONArray data) {
		PluginResult result = null;
		JSONObject ret = new JSONObject();
		try {
			Log.i("PGSQLitePlugin", "remove action");
			ret.put("status", 1);
			String dbName = data.getString(0);
			File dbFile = null;
			SQLiteDatabase db = getDb(dbName);
			if (db != null) {
				db.close();
				openDbs.remove(dbName);
			}

			dbFile = new File(ctx.getExternalFilesDir(null), dbName);
			if (!dbFile.exists()) {

				dbFile = ctx.getDatabasePath(dbName);
				if (!dbFile.exists()) {
					ret.put("message", "Database not exist");
					ret.put("status", 0);
					result = new PluginResult(PluginResult.Status.ERROR, ret);
				} else {
					if (dbFile.delete()) {
						Log.i("PGSQLitePlugin",
								"remove action::remove from internal");
						result = new PluginResult(PluginResult.Status.OK);
					} else {
						ret.put("message", "Can't remove db");
						ret.put("status", 2);
						result = new PluginResult(PluginResult.Status.ERROR,
								ret);
					}
				}
			} else {
				if (dbFile.delete()) {
					result = new PluginResult(PluginResult.Status.OK);
					Log.i("PGSQLitePlugin", "remove action::remove from sdcard");
				} else {
					ret.put("message", "Can't remove db");
					ret.put("status", 2);
					result = new PluginResult(PluginResult.Status.ERROR, ret);
				}
			}
		} catch (Exception e) {
			Log.e("PGSQLitePlugin", e.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR, ret);
		}
		return result;
	}

	@SuppressLint("SdCardPath")
	private String path = "/data/data/";
	private static final String USE_INTERNAL = "internal";
	private static final String USE_EXTERNAL = "external";

	@SuppressWarnings("deprecation")
	public void openDatabese(JSONArray data) {
		try {
			String storage = PGSQLitePlugin.USE_INTERNAL;
			// String storage = PGSQLitePlugin.USE_EXTERNAL;

			String dbName = data.getString(0);
			seDbName = dbName;
			JSONObject options = getJSONObjectAt(data, 1);
			if (options != null) {
				storage = options.getString("storage");
			}
			if (storage.equals(PGSQLitePlugin.USE_EXTERNAL)
					&& !Environment.getExternalStorageState().equals(
							Environment.MEDIA_MOUNTED)) {
				// new PluginResult(PluginResult.Status.ERROR,
				// "SDCard not mounted")
				Log.e("sdcard  >>>>>> ", "SDCard not mounted");
				return;
			} else {
				Log.e("sdcard  >>>>>> ", "SDCard mounted");

			}
			String _dbName = null;
			SQLiteDatabase db = getDb(dbName);
			File dbFile = null;
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)
					&& !storage.equals(PGSQLitePlugin.USE_INTERNAL)) {

				if (storage.equals(PGSQLitePlugin.USE_EXTERNAL)) {
					dbFile = new File(ctx.getExternalFilesDir(null), dbName);
					if (!dbFile.exists()) {
						dbFile.mkdirs();
					} else {
						Log.d(">>>>>>>>>>>>>>>>>>>>", "db  已存在");
					}

				} else {
					dbFile = ctx.getDatabasePath(dbName);
					if (!dbFile.exists()) {
						dbFile = new File(ctx.getExternalFilesDir(null), dbName);

						if (!dbFile.exists()) {
							StatFs stat = new StatFs("/data/");
							long blockSize = stat.getBlockSize();
							long availableBlocks = stat.getBlockCount();
							long size = blockSize * availableBlocks;
							if (size >= 1024 * 1024 * 1024) {
								dbFile = ctx.getDatabasePath(dbName);
							} else {
								dbFile = new File(
										ctx.getExternalFilesDir(null), dbName);
							}
							Log.i("blockSize * availableBlocks",
									Long.toString(size));
						}
					}
				}
			} else {
				dbFile = ctx.getDatabasePath(dbName);

				File file = null;
				file = Environment.getDataDirectory();
				//Log.e("file  path >>>> ","getDataDirectory()=" + file.getPath());
				String appDataPath = file.getAbsolutePath();
				// 获取当前程序路径
				String ss = ctx.getFilesDir().getAbsolutePath();
				// 获取该程序的安装包路径
				String path = ctx.getPackageResourcePath();

				// 获取程序默认数据库路径
				//ctx.getDatabasePath(ss).getAbsolutePath();
				//String mpath = ctx.getFilesDir().getAbsolutePath() + "/"+ dbName; 
				// data/data目录
				String dbpath = ctx.getDatabasePath(dbName).getAbsolutePath();
				String dbDirPath = "/data/data/" + ctx.getPackageName()+"/databases";
				File dbDir = new File(dbDirPath);
				if (!dbDir.exists()) {
					dbDir.mkdir();
				}
				//File mfile = new File(dbpath);
				File dbf = new File(dbpath);
				if (dbf.exists()) {
					// dbf.delete();
					// return;
					Log.d("=====================", "db  文件已存在");
				} else {
					Log.d("=====================", "db  文件不存在,拷贝asset/www/db文件");
					final String[] sample_dbName = { dbName };
					int assetDbSize = sample_dbName.length;
					//File databaseFile = new File("/data/data/com.simpleevent.checkin/databases/");
					// check if databases folder exists, if not create one and its
					// subfolders
					
//					if (!databaseFile.exists()) {
//						databaseFile.mkdir();
//					}
					for (int i = 0; i < assetDbSize; i++) {
						String outFilename = null;
						//outFilename = "/data/data/com.simpleevent.checkin/databases/" + dbName;
						outFilename = dbpath;
						File _dbfile = new File(outFilename);
						try {
							InputStream in = ctx.getAssets().open("www/" + dbName);
							OutputStream out = new FileOutputStream(outFilename);
							// Transfer bytes from the sample input file to the
							// sample output file
							byte[] buf = new byte[1024];
							int len;
							while ((len = in.read(buf)) > 0) {
								out.write(buf, 0, len);
							}
							out.flush();
							// Close the streams
							out.close();
							in.close();
						} catch (Exception e) {
							Log.e("PGSQLitePlugin",
									"error get db from assets=" + e.getMessage());
						}
					}
				}
			}

			_dbName = dbFile.getPath();
			int status = 0;
			if (db == null) {
				if (!dbFile.exists()) {
					status = 1;
					
					String dbpath = ctx.getDatabasePath(dbName).getAbsolutePath();
					String dbDirPath = "/data/data/" + ctx.getPackageName()+"/databases";
					File dbDir = new File(dbDirPath);
					if (!dbDir.exists()) {
						dbDir.mkdir();
					}
					File dbf = new File(dbpath);
					final String[] sample_dbName = { dbName };
					int assetDbSize = sample_dbName.length;
				
					for (int i = 0; i < assetDbSize; i++) {
						String outFilename = null;
						outFilename = dbpath;
						File sampleFile = new File(outFilename);
						try {
							InputStream in = ctx.getAssets().open("www/" + dbName);
							OutputStream out = new FileOutputStream(outFilename);
							// Transfer bytes from the sample input file to the
							// sample output file
							byte[] buf = new byte[1024];
							int len;
							while ((len = in.read(buf)) > 0) {
								out.write(buf, 0, len);
							}
							out.flush();
							// Close the streams
							out.close();
							in.close();
							status = 2;
						} catch (Exception e) {
							Log.e("PGSQLitePlugin",
									"error get db from assets=" + e.getMessage());
						}
					}
				}
				db = SQLiteDatabase.openDatabase(_dbName, null,
						SQLiteDatabase.CREATE_IF_NECESSARY);
				openDbs.put(dbName, db);
			}

			// copyFile(dbName, path + ctx.getPackageName() + "/databases/"
			// + dbName);
			// File dbfile = ctx.getDatabasePath(dbName);
			// SQLiteDatabase mydb = SQLiteDatabase.openOrCreateDatabase(dbfile,
			// null);
			// openDbs.put(dbName, mydb);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	@SuppressWarnings("deprecation")
	public boolean reOpenDatabese(JSONArray data) {
		boolean result = false;
		try {
			String storage = PGSQLitePlugin.USE_INTERNAL;
			String dbName = data.getString(0);
			seDbName = dbName;
			JSONObject options = getJSONObjectAt(data, 1);
			if (options != null) {
				storage = options.getString("storage");
			}
			if (storage.equals(PGSQLitePlugin.USE_EXTERNAL)
					&& !Environment.getExternalStorageState().equals(
							Environment.MEDIA_MOUNTED)) {
				// new PluginResult(PluginResult.Status.ERROR,
				// "SDCard not mounted")
				return false;
			}
			String _dbName = null;
			SQLiteDatabase db = getDb(dbName);
			File dbFile = null;
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)
					&& !storage.equals(PGSQLitePlugin.USE_INTERNAL)) {
				if (storage.equals(PGSQLitePlugin.USE_EXTERNAL)) {
					dbFile = new File(ctx.getExternalFilesDir(null), dbName);
					if (!dbFile.exists()) {
						dbFile.mkdirs();
					}
				} else {
					dbFile = ctx.getDatabasePath(dbName);
					if (!dbFile.exists()) {
						dbFile = new File(ctx.getExternalFilesDir(null), dbName);

						if (!dbFile.exists()) {
							StatFs stat = new StatFs("/data/");
							long blockSize = stat.getBlockSize();
							long availableBlocks = stat.getBlockCount();
							long size = blockSize * availableBlocks;
							if (size >= 1024 * 1024 * 1024) {
								dbFile = ctx.getDatabasePath(dbName);
							} else {
								dbFile = new File(
										ctx.getExternalFilesDir(null), dbName);
							}
							Log.i("blockSize * availableBlocks",
									Long.toString(size));
						}
					}
				}
			} else {
				dbFile = ctx.getDatabasePath(dbName);

				if (!dbFile.exists()) {
					dbFile = new File(ctx.getExternalFilesDir(null), dbName);

					if (!dbFile.exists()) {
						StatFs stat = new StatFs("/data/");
						long blockSize = stat.getBlockSize();
						long availableBlocks = stat.getBlockCount();
						long size = blockSize * availableBlocks;
						if (size >= 1024 * 1024 * 1024) {
							dbFile = ctx.getDatabasePath(dbName);
						} else {
							dbFile = new File(ctx.getExternalFilesDir(null),
									dbName);
						}
						Log.i("blockSize * availableBlocks",
								Long.toString(size));
					}
				}
			}
			_dbName = dbFile.getPath();
			int status = 0;
			if (db == null) {
				if (!dbFile.exists()) {
					status = 1;
					try {
						InputStream assetsDB = this.ctx.getAssets().open(
								"www/" + dbName);
						OutputStream dbOut = new FileOutputStream(_dbName);
						byte[] buffer = new byte[1024];
						int length;
						while ((length = assetsDB.read(buffer)) > 0) {
							dbOut.write(buffer, 0, length);
						}
						dbOut.flush();
						dbOut.close();
						assetsDB.close();
						status = 2;
						result = true;
					} catch (Exception e) {
						Log.e("PGSQLitePlugin",
								"error get db from assets=" + e.getMessage());
						return false;
					}
				} else {

					deleteFile(dbFile);// TODO
					status = 1;
					try {
						InputStream assetsDB = this.ctx.getAssets().open(
								"www/" + dbName);
						OutputStream dbOut = new FileOutputStream(_dbName);
						byte[] buffer = new byte[1024];
						int length;
						while ((length = assetsDB.read(buffer)) > 0) {
							dbOut.write(buffer, 0, length);
						}
						dbOut.flush();
						dbOut.close();
						assetsDB.close();
						status = 2;
						result = true;
					} catch (Exception e) {
						Log.e("PGSQLitePlugin",
								"error get db from assets=" + e.getMessage());
						result = false;
					}
				}
				db = SQLiteDatabase.openDatabase(_dbName, null,
						SQLiteDatabase.CREATE_IF_NECESSARY);
				openDbs.put(dbName, db);
			}

			// copyFile(dbName, path + ctx.getPackageName() + "/databases/"
			// + dbName);
			// File dbfile = ctx.getDatabasePath(dbName);
			// SQLiteDatabase mydb = SQLiteDatabase.openOrCreateDatabase(dbfile,
			// null);
			// openDbs.put(dbName, mydb);
		} catch (Exception e) {
			System.err.println(e);
			return false;
		}
		return result;
	}

	public static void deleteFile(File file) {
		String sdState = Environment.getExternalStorageState();
		if (sdState.equals(Environment.MEDIA_MOUNTED)) {
			if (file.exists()) {
				if (file.isFile()) {
					file.delete();
				} else if (file.isDirectory()) {// 如果它是一个目录
					// 声明目录下所有的文件 files[];
					File files[] = file.listFiles();
					for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
						deleteFile(files[i]); // 把每个文件 用这个方法进行迭代
					}
				}
				file.delete();
			}
		}
	}

	public PluginResult closeDatabese(JSONArray data) {
		PluginResult result = null;
		try {
			Log.e("PGSQLitePlugin", "close action");
			String dbName = data.getString(0);
			SQLiteDatabase db = getDb(dbName);
			if (db != null) {
				db.close();
				openDbs.remove(dbName);
			}
			result = new PluginResult(PluginResult.Status.OK);
		} catch (Exception e) {
			Log.e("PGSQLitePlugin", e.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
		}

		return result;
	}

	public void closeDatabeseSE(JSONArray data) {
		// PluginResult result = null;
		try {
			Log.e("PGSQLitePlugin", "close action");
			String dbName = data.getString(0);
			SQLiteDatabase db = getDb(dbName);
			if (db != null) {
				db.close();
				openDbs.remove(dbName);
			}
			// result = new PluginResult(PluginResult.Status.OK);
			Log.v("closeDatabeseSE >> ", "OK");
		} catch (Exception e) {
			Log.e("PGSQLitePlugin", e.getMessage());
			// result = new PluginResult(PluginResult.Status.ERROR,
			// e.getMessage());
			Log.v("closeDatabeseSE >> ", "ERROR");
		}

		// return result;
	}

	/**
	 * 
	 * @param oldPath
	 *            String 源路径c:/fqf.txt
	 * @param newPath
	 *            String 目标路径f:/fqf.txt
	 */
	public void copyFile(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			InputStream inStream = ctx.getResources().getAssets().open(oldPath);
			FileOutputStream fs = new FileOutputStream(newPath);
			byte[] buffer = new byte[1444];
			while ((byteread = inStream.read(buffer)) != -1) {
				bytesum += byteread; // 
				System.out.println(bytesum);
				fs.write(buffer, 0, byteread);
			}
			fs.flush();
			fs.close();
			inStream.close();
		} catch (Exception e) {
			System.out.println("copyFile" + e.toString());
			e.printStackTrace();

		}

	}
}
