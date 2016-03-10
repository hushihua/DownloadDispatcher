/**
 * 
 */
package android.library.downloadcenter;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.library.downloadcenter.adv.DownLoadSchedule;
import android.util.Log;

/**
 * @author hushihua
 *
 */
public class DownLoadDBManager {

	private static DownLoadDBManager _manager;
	private DownLoadDBHelper _dbHelper;
	private SQLiteDatabase _db;
	
	private DownLoadDBManager(){
		
	}
	
	public static DownLoadDBManager getInstance(){
		if (_manager == null) {
			_manager = new DownLoadDBManager();
		}
		return _manager;
	}
	
	/**
	 * 先调用一下
	 * @param context
	 */
	public void initDB(Context context){
		if (_db == null) {
			_dbHelper = new DownLoadDBHelper(context);
			_db = _dbHelper.getWritableDatabase();
		}
	}
	
	public synchronized ArrayList<AppDownLoadInfo> loadAllSchedule(){
		Log.i("test", "-- loadAllSchedule --");
		Cursor cursor = _db.rawQuery("select * from schedule", null);
		ArrayList<AppDownLoadInfo> list = new ArrayList<AppDownLoadInfo>();
		while (cursor.moveToNext()) {
			AppDownLoadInfo info = new AppDownLoadInfo();
			info.appName = cursor.getString(cursor.getColumnIndex("appName"));
			info.appPackageName = cursor.getString(cursor.getColumnIndex("packageName"));
			info.versionCode = cursor.getString(cursor.getColumnIndex("versionName"));
			info.iconUri = cursor.getString(cursor.getColumnIndex("iconUrl"));
			info.path = cursor.getString(cursor.getColumnIndex("path"));
			info.downLoadUrl = cursor.getString(cursor.getColumnIndex("downLoadUrl"));
			info.totalSize = cursor.getInt(cursor.getColumnIndex("totalSize"));
			info.completeSize = cursor.getInt(cursor.getColumnIndex("complateSize"));
			info.property = cursor.getInt(cursor.getColumnIndex("property"));
			info.appId = cursor.getString(cursor.getColumnIndex("appId"));
			info.lastestVersionId = cursor.getString(cursor.getColumnIndex("lastestVersionId"));
			list.add(info);
		}
		return list;
	}
	
	public synchronized void insertSchedule(AppDownLoadInfo info){
		Log.i("test", "-- insertSchedule:" + info.toString());
		_db.execSQL("insert into schedule("
				+ "appId,"
				+ "lastestVersionId,"
				+ "appName,"
				+ "packageName,"
				+ "versionName,"
				+ "iconUrl,"
				+ "path,"
				+ "downLoadUrl,"
				+ "totalSize,"
				+ "complateSize,"
				+ "property) values(?,?,?,?,?,?,?,?,?,?,?)", 
				new Object[]{info.appId,
						info.lastestVersionId,
						info.appName, 
						info.appPackageName, 
						info.versionCode,
						info.iconUri, 
						info.path, 
						info.downLoadUrl,
						info.totalSize,
						info.completeSize, 
						info.property});
	}
	
	public synchronized void updateSchedule(DownLoadSchedule info){
//		Log.i("test", "-- updateSchedule:" + info.toString());
		_db.execSQL("update schedule set complateSize=?,totalSize=? where downLoadUrl=?", new Object[]{ info.completeSize, info.totalSize, info.downLoadUrl });
	}
	
	public synchronized void deleteSchedule(AppDownLoadInfo info){
		Log.i("test", "-- deleteSchedule:" + info.toString());
		_db.execSQL("delete from schedule where downLoadUrl=?", new Object[]{ info.downLoadUrl });
	}
}
