package com.android.smarthomegateway;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

public class SQLiteAction implements TablePara {
	
	static final private String curDBPath = "/data/com.android.smarthomegateway/databases/shgDatabase.db";
	static final private String newDBPath = "/htdocs/shgDatabase.db";
	
	public SQLiteAction() {}
	
	public void Initial() {

		ChangePath();
		
		//Initialization
		Insert(TABLE_NAME[0],
				name, gw_id,
				val, "None");
		Insert(TABLE_NAME[0],
				name, fb_id,
				val, "None");
		Insert(TABLE_NAME[0],
				name, ser_key,
				val, "None");
		Insert(TABLE_NAME[0],
				name, swot,
				val, "140.138.150.52");
		Insert(TABLE_NAME[0],
				name, ipv4,
				val, default_ip);
		Insert(TABLE_NAME[0],
				name, Dnum,
				val, "0");
		
		Insert(TABLE_NAME[5],
				look_up_id, "1",
				name, "Sensor");
		Insert(TABLE_NAME[5],
				look_up_id, "2",
				name, "Actuator");
		Insert(TABLE_NAME[5],
				look_up_id, "3",
				name, "Multimedia Device");
	}
	
	public boolean IfDatabaseExist() {
		File db = new File(Environment.getExternalStorageDirectory() + "/htdocs/", "shgDatabase.db");
		return db.exists();		
	}
	
	public int IfDeviceSupport(String model_id) {		
		SQLiteDatabase dbHelper = SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory() + newDBPath, null, SQLiteDatabase.OPEN_READWRITE);
		Cursor cursor = dbHelper.query(TABLE_NAME[6], new String [] {look_up_id}, name + "= ?", new String [] {model_id}, null, null, null, null);
		if (cursor.moveToFirst()) {
			int index = Integer.parseInt(cursor.getString(0));
			
			dbHelper.close();
			return index;
		}
		
		dbHelper.close();		
		return 0;
	}
	
	@SuppressWarnings("resource")
	public void ChangePath() {
		 try {
             File sd = Environment.getExternalStorageDirectory();
             File data = Environment.getDataDirectory();

             if (sd.canWrite()) {
                 File currentDB = new File(data, curDBPath);
                 File backupDB = new File(sd, newDBPath);

                     FileChannel src = new FileInputStream(currentDB).getChannel();
                     FileChannel dst = new FileOutputStream(backupDB).getChannel();
                     dst.transferFrom(src, 0, src.size());
                     src.close();
                     dst.close();

             }
         } catch (Exception e) {}
		
	}
	
	public void Insert(String ...arg0) {
		SQLiteDatabase dbHelper = SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory() + newDBPath, null, SQLiteDatabase.OPEN_READWRITE);
    	ContentValues values = new ContentValues();
    	for(int i = 1; i < arg0.length; i += 2)
    		values.put(arg0[i], arg0[i + 1]);
    	dbHelper.insert(arg0[0], null, values);
    	dbHelper.close();
    }
	
	public void Update(String tableName, String [] pair_val, String cond, String [] cond_val) {
		SQLiteDatabase dbHelper = SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory() + newDBPath, null, SQLiteDatabase.OPEN_READWRITE);
		ContentValues values = new ContentValues();
		for(int i = 0; i < pair_val.length; i += 2)
    		values.put(pair_val[i], pair_val[i + 1]);
		
		dbHelper.update(tableName, values, cond, cond_val);
    	dbHelper.close();
	}
	
	public void UpdateAttribute(String attribute[]){
		SQLiteDatabase dbHelper = SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory() + newDBPath, null, SQLiteDatabase.OPEN_READWRITE);
		for(int i = 0; i < attribute.length; i++)
			dbHelper.execSQL("INSERT OR REPLACE INTO " + TABLE_NAME[6] + " (" + name + ") VALUES ('" + attribute[i] + "');");
		dbHelper.close();
	}
	
	public void UpdateNewestData(String device_id, String time, String data) {	//Update the newest data in SHG
		SQLiteDatabase dbHelper = SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory() + newDBPath, null, SQLiteDatabase.OPEN_READWRITE);
		ContentValues values = new ContentValues();
    	values.put(Dtime, time);
    	values.put(Ddata, data);
		
    	Cursor cursor = dbHelper.query(TABLE_NAME[4], new String[] {Dtime}, Did + "= ?", new String[] {device_id}, null, null, Dtime + " ASC");	//Get the newest data time
    	cursor.moveToFirst();
    	
		dbHelper.update(TABLE_NAME[4], values, Dtime + "=?", new String [] {cursor.getString(0)});
    	dbHelper.close();
	}
	
	public int getDataNumber(String device_id)	//Get how many number of the data stored in SHG for a device 
	{
		SQLiteDatabase dbHelper = SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory() + newDBPath, null, SQLiteDatabase.OPEN_READWRITE);
		Cursor cursor = dbHelper.query(TABLE_NAME[4], new String[] {Did}, Did + "= ?", new String[] {device_id}, null, null, null, null);
		
		if (cursor.moveToFirst()) {
			dbHelper.close();
			return cursor.getCount();
		}
		
		dbHelper.close();
		return 0;
	}
	
	public String getAttribute(String device_id, String attribute_name)
	{
		SQLiteDatabase dbHelper = SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory() + newDBPath, null, SQLiteDatabase.OPEN_READWRITE);
		Cursor cursor = dbHelper.query(TABLE_NAME[2], new String[] {val}, Did + "= ? AND " + name + "= ?", new String[] {device_id, attribute_name}, null, null, null, null);
		if (cursor.moveToFirst()) {
			dbHelper.close();
	        return cursor.getString(0);
		}
		
		dbHelper.close();
		return null;
	}
	
	/*public int getDataMax(String device_id)
	{
		SQLiteDatabase dbHelper = SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory() + newDBPath, null, SQLiteDatabase.OPEN_READWRITE);
		Cursor cursor = dbHelper.query(TABLE_NAME[4], new String[] {device_id}, device_id + "=?", new String[] {device_id}, null, null, null, null);
		dbHelper.close();
		if (cursor != null)
	        return cursor.getCount();
		return 0;
	}*/
	
	@SuppressWarnings("null")
	public String [] GetDevice(String master_id, String slave_id)		//If the device is registered to the Smart Home Gateway
	{
		String [] str = null;
		
		SQLiteDatabase dbHelper = SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory() + newDBPath, null, SQLiteDatabase.OPEN_READWRITE);
		Cursor cursor = dbHelper.query(TABLE_NAME[1], new String [] {Did, Mid, MTid, SVid, Cid, status, des}, MTid + "= ? AND " + SVid + "= ?", new String [] {master_id, slave_id}, null, null, null, null);
		//Cursor cursor = dbHelper.rawQuery("select Did from " + TABLE_NAME[1] + " where " + Mid + "=?", new String[]{master_id}); 
		if (cursor.moveToFirst())
		{
			str = new String[cursor.getColumnCount()];
			for(int i = 0; i < cursor.getColumnCount(); i++)
			{
				str[i] = cursor.getString(i);
				Log.d("message", "Device : " + str[i]);
			}
			
			dbHelper.close();
			return str;
		}
		
		dbHelper.close();
		return null;
	}
	
	public boolean IfGatewayRegistered()
	{
		SQLiteDatabase dbHelper = SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory() + newDBPath, null, SQLiteDatabase.OPEN_READWRITE);
		Cursor cursor = dbHelper.query(TABLE_NAME[0], new String[] {val}, name + "= ?", new String[] {ser_key}, null, null, null, null);
		if (cursor.moveToFirst())
			Log.d("CURSOR", cursor.getString(0));
			if(!cursor.getString(0).equals("None")) {
				dbHelper.close();
				return true;
			}
		dbHelper.close();
		return false;
	}
	
	public boolean IfBindtoFacebook()
	{
		SQLiteDatabase dbHelper = SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory() + newDBPath, null, SQLiteDatabase.OPEN_READWRITE);
		Cursor cursor = dbHelper.query(TABLE_NAME[0], new String[] {val}, name + "= ?", new String[] {fb_id}, null, null, null, null);
		if (cursor != null)
	        cursor.moveToFirst();

		if(!cursor.getString(0).equals("None"))
			return true;
		return false;		
	}
	
	public String GetSWoTIP()
	{
		String ip = null;
		SQLiteDatabase dbHelper = SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory() + newDBPath, null, SQLiteDatabase.OPEN_READWRITE);
		Cursor cursor = dbHelper.query(TABLE_NAME[0], new String[] {val}, name + "= ?", new String[] {swot}, null, null, null, null);
		if (cursor.moveToFirst()) {
			ip = cursor.getString(0);		
	    	dbHelper.close();
		}
		
		return ip;
	}
	
	public String GetSecretKey()
	{
		SQLiteDatabase dbHelper = SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory() + newDBPath, null, SQLiteDatabase.OPEN_READWRITE);
		Cursor cursor = dbHelper.query(TABLE_NAME[0], new String[] {val}, name + "= ?", new String[] {ser_key}, null, null, null, null);
		if (cursor.moveToFirst()) {
			dbHelper.close();
	        return cursor.getString(0);
		}
		
		dbHelper.close();
		return null;
	}
	
	public String GetGatewayID()
	{
		SQLiteDatabase dbHelper = SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory() + newDBPath, null, SQLiteDatabase.OPEN_READWRITE);
		Cursor cursor = dbHelper.query(TABLE_NAME[0], new String[] {val}, name + "= ?", new String[] {gw_id}, null, null, null, null);
		if (cursor.moveToFirst()) {
			dbHelper.close();
	        return cursor.getString(0);
		}
		
		dbHelper.close();
		return null;
	}
	
	public String GetServiceID(String device_id)
	{
		SQLiteDatabase dbHelper = SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory() + newDBPath, null, SQLiteDatabase.OPEN_READWRITE);
		Cursor cursor = dbHelper.query(TABLE_NAME[3], new String[] {ser_id}, Did + "= ?", new String[] {device_id}, null, null, null, null);
		if (cursor.moveToFirst()) {
			dbHelper.close();
	        return cursor.getString(0);
		}
		
		dbHelper.close();
		return null;
	}
}
