package com.android.smarthomegateway;

public interface TablePara {
	
	public static final String TABLE_NAME[] = {
		"SHG_Profile"			//0
		,"Device_Feature"		//1
		,"Device_Attribute"		//2
		,"Device_Event"			//3
		,"Device_Data"			//4
		,"Device_Class"			//5
		,"Device_Model"			//6
	};
	
	public static final String gw_id = "GW_ID";
	public static final String fb_id = "FB_ID";
	public static final String ser_key = "GW_SECRET";
	public static final String swot = "SWOT_IP";
	public static final String ipv4 = "IP";
	public static final String Dnum = "DEVICE_NUMBER";
	
	public static final String Did = "DEVICE_ID";
	public static final String name = "NAME";
	public static final String val = "VALUE";
	public static final String status = "STATUS";
	public static final String ser_id = "SERVICE_ID";
	public static final String max = "MAX_THRESHOLD";
	public static final String min = "MIN_THRESHOLD";
	public static final String look_up_id = "ID";
	public static final String Dtime = "TIME";
	public static final String Ddata = "DATA";
	public static final String MTid = "MASTER_ID";
	public static final String SVid = "SLAVE_ID";
	public static final String Cid = "CLASS_ID";
	public static final String Mid = "MODEL_ID";
	public static final String des = "DESCRIPTION";
	
	public static final String default_ip = "0.0.0.0";
}