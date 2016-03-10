/**
 * 
 */
package android.library.downloadcenter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author hushihua
 *
 */
class DownLoadDBHelper extends SQLiteOpenHelper {

	private final String CREATE_TABLE = "create table if not exists schedule("
			+ "id integer primary key autoincrement,"
			+ "appId text,"
			+ "lastestVersionId text,"
			+ "appName text,"
			+ "packageName text,"
			+ "versionName text,"
			+ "iconUrl text,"
			+ "path text,"
			+ "downLoadUrl text,"
			+ "totalSize integer,"
			+ "complateSize integer,"
			+ "property integer)" ;
	
	public DownLoadDBHelper(Context context){
		super(context, "DownLoadDB.db", null, 1);
	}
	
	public DownLoadDBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
