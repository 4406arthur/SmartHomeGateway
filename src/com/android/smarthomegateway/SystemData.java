package com.android.smarthomegateway;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Enumeration;

import android.annotation.SuppressLint;
import android.util.Log;

public class SystemData {
	
	SystemData() {}
	
	@SuppressWarnings("rawtypes")
	public static String getIPAddress() { 
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        String ipAddress=inetAddress.getHostAddress().toString();
                        Log.e("IP address",""+ipAddress);
                        return ipAddress;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("Socket exception in GetIP Address of Utilities", ex.toString());
        }
        return null; 
	}
	
	@SuppressLint("SimpleDateFormat")
	public static String getCurrentTime()
	{
		SimpleDateFormat time1 = new SimpleDateFormat("MMddHHmmss");
		SimpleDateFormat time2 = new SimpleDateFormat("yyyy");
		String curTime =  time1.format(new Date(System.currentTimeMillis()));
		String year =  time2.format(new Date(System.currentTimeMillis()));
		curTime = String.valueOf(Integer.parseInt(year) - 1911) + curTime;
				
		return curTime;		
	}
}
