package com.android.smarthomegateway;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class SQLite extends SQLiteOpenHelper implements TablePara {
	private static final String DB_NAME = "shgDatabase.db";
	private static final int DB_VERSION = 1;
	private static final int TB_NUMBER = 7;
	
    public SQLite(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }
	
	public SQLite(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, DB_VERSION);
		// TODO 自動產生的建構子 Stub
	}

	@Override	
	public void onCreate(SQLiteDatabase db) {
		// TODO 自動產生的方法 Stub
		
		String [] CREATE_TABLE = new String [TB_NUMBER];
		
		CREATE_TABLE[0] = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME[0]
				+ " (" 
				+ name + " TEXT, "
				+ val + " TEXT);";
		
		CREATE_TABLE[1] = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME[1]
				+ " ("
				+ Did + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ Mid + " TEXT, "
				+ MTid + " NUMBER, "
				+ SVid + " TEXT, "
				+ Cid + " NUMBER, "
				+ status + " NUMBER, "
				+ des + " TEXT);";
		
		CREATE_TABLE[2] = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME[2]
				+ " ("
				+ Did + " NUMBER, "
				+ name + " TEXT, "
				+ val + " NUMBER, "
				+ status +  " NUMBER);";
		
		CREATE_TABLE[3] = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME[3]
				+ " (" 
				+ Did + " NUMBER, "
				+ ser_id + " NUMBER, "
				+ min + " NUMBER, "
				+ max + " NUMBER);";
		
		CREATE_TABLE[4] = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME[4]
				+ " ("
				+ Did + " NUMBER, "
				+ Dtime + " NUMBER, "
				+ Ddata + " TEXT);";
		
		CREATE_TABLE[5] = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME[5]
				+ " (" 
				+ look_up_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ name + " TEXT);";
		
		CREATE_TABLE[6] = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME[6]
				+ " (" 
				+ look_up_id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ name + " TEXT);";
		
		for(int i = 0; i < TB_NUMBER; i++)
			db.execSQL(CREATE_TABLE[i]);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String DROP_TABLE;
		// TODO 自動產生的方法 Stub		
		for(int i = 0; i < TB_NUMBER; i++) {
			DROP_TABLE = "drop table if exists " + TABLE_NAME[i];
			db.execSQL(DROP_TABLE);
		}
	}
}
