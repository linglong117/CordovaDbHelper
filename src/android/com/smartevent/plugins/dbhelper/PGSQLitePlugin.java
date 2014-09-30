package com.smartevent.plugins.dbhelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
			Log.e("PGSQLitePlugin", "updateQuery");
			String dbName = data.getString(0);
			String tableName = data.getString(1);
			JSONObject values = (JSONObject) data.get(2);
			String where = getStringAt(data, 3, "1");
			JSONArray whereArgs = getJSONArrayAt(data, 4);

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
			Log.e("PGSQLitePlugin", "updateQuery::count=" + count);

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

	/*public PluginResult insertQuery(JSONArray data) {
		PluginResult result = null;
		try {
			String dbName = data.getString(0);
			String tableName = data.getString(1);
			JSONObject values = (JSONObject) data.get(2);
			JSONArray names = values.names();
			int vLen = names.length();
			SQLiteDatabase db = getDb(dbName);
			ContentValues _values = new ContentValues();
			for (int i = 0; i < vLen; i++) {
				String name = names.getString(i);
				_values.put(name, values.getString(name));
			}
			long id = db.insert(tableName, null, _values);
			if (id == -1) {
				result = new PluginResult(PluginResult.Status.ERROR,
						"Insert error");
			} else {
				result = new PluginResult(PluginResult.Status.OK, id);
			}
		} catch (Exception e) {
			Log.e("PGSQLitePlugin", e.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
		}
		return result;
	}*/
	
	public PluginResult insertQuery(JSONArray data) {
		PluginResult result = null;
		try {
			String dbName = data.getString(0);
			String tableName = data.getString(1);
			JSONArray columns=data.getJSONArray(2);
			JSONArray values=data.getJSONArray(3);
		
			long id = -1;
			SQLiteDatabase db = getDb(dbName);
			ContentValues _values = null;
			JSONArray values_=null;
			for (int i = 0; i < values.length(); i++) {
				values_=values.getJSONArray(i);
				_values=new ContentValues();
				for (int j = 0; j < values_.length(); j++) {
					_values.put(columns.getString(j), values_.getString(j));
				}
				 id = db.insert(tableName, null, _values);
			}
			if (id == -1) {
				result = new PluginResult(PluginResult.Status.ERROR,
						"Insert error");
			} else {
				result = new PluginResult(PluginResult.Status.OK, id);
			}
		} catch (Exception e) {
			Log.e("PGSQLitePlugin", e.getMessage());
			result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
		}
		return result;
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
			String dbName = data.getString(0);
			JSONObject options = getJSONObjectAt(data, 1);
			if (options != null) {
				storage = options.getString("storage");
			}
			if (storage.equals(PGSQLitePlugin.USE_EXTERNAL)
					&& !Environment.getExternalStorageState().equals(
							Environment.MEDIA_MOUNTED)) {
				// new PluginResult(PluginResult.Status.ERROR,
				// "SDCard not mounted")
				return;
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
					} catch (Exception e) {
						Log.e("PGSQLitePlugin",
								"error get db from assets=" + e.getMessage());
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
				}else {
					
					deleteFile(dbFile);//TODO
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
				}else if (file.isDirectory()) {// 如果它是一个目录
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

	/**
	 * 
	 * @param oldPath
	 *            String ԭ�ļ�·�� �磺c:/fqf.txt
	 * @param newPath
	 *            String ���ƺ�·�� �磺f:/fqf.txt
	 */
	public void copyFile(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			InputStream inStream = ctx.getResources().getAssets().open(oldPath);
			FileOutputStream fs = new FileOutputStream(newPath);
			byte[] buffer = new byte[1444];
			while ((byteread = inStream.read(buffer)) != -1) {
				bytesum += byteread; // �ֽ��� �ļ���С
				System.out.println(bytesum);
				fs.write(buffer, 0, byteread);
			}
			fs.flush();
			fs.close();
			inStream.close();
		} catch (Exception e) {
			System.out.println("���Ƶ����ļ���������");
			e.printStackTrace();

		}

	}
}
