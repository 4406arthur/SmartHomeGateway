package com.android.smarthomegateway;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class SHGService extends Service implements TablePara, StringPara{
	
	private Handler handler = new Handler();
	private int counter = 0;
	
	private String SHG_IP = null;
	private String SWoT_IP = "140.138.150.52";	//The IP address of the destination for HTTP command.
	private String key = null;				//The secret key from SWoT.
	private String SHG_ID = null;			//The gateway id from SWoT
	private String external_ip_port = null;
	private int gw_tcpport = 9876;
	private int app_tcpport = 6789;
	private EventListener event;
	private SQLiteAction action;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		Bundle b = intent.getExtras();
		external_ip_port = String.valueOf(b.getCharSequence("ip_port"));
		
		action = new SQLiteAction();
		
		if(!action.IfDatabaseExist()) {		//Decide if database've been created or not.
			new SQLite(this).getWritableDatabase().close();
			action.Initial();
			new UpdateDeviceModel().execute();
		}
		new UpdateExternalIP().execute("http://ip2country.sourceforge.net/ip2c.php?format=JSON");
		
		//Set the initialization for IP address.
		//action.Update(TABLE_NAME[0], new String [] {val, SystemData.getIPAddress()}, name + "= ?", new String [] {ipv4});
		//new UpdateExternalIP().execute("http://ip2country.sourceforge.net/ip2c.php?format=JSON");
		action.Update(TABLE_NAME[0], new String [] {val, "140.138.150.52"}, name + "= ?", new String [] {swot});
		
		//SWoT Initialization (GET GW_ID and GW_SECRET).
		if(!action.IfGatewayRegistered())	//Decide if registration is done or not.
			new RegisterGateway().execute("/task_manager/v1/register", "ip", "http://" + SHG_IP + "/");		//Register Gateway to SWoT.
		else {
			key = action.GetSecretKey();
    		SHG_ID = action.GetGatewayID();
		}
		
		Log.d("Key", SHG_ID + "_" + key);

		//The EventListener is for the management of devices. 
		event = new EventListener();
		event.execute(gw_tcpport);
		
		//handler.postDelayed(Monitor, 1000);
	}

	@Override
	public void onDestroy() {
		
		event.cancel(true);
		//handler.removeCallbacks(Monitor);
		super.onDestroy();
	}
	
	private class UpdateExternalIP extends AsyncTask<String, Integer, String> {
		
		protected String doInBackground(String... arg0) {			
			
	    	try {
	    		HttpClient httpclient = new DefaultHttpClient();			
				HttpGet get = new HttpGet(arg0[0]);

	    		// Execute HTTP Post Request
	    		HttpResponse response = httpclient.execute(get);
	    		
	    		//Parse response
	    		String responseBody = EntityUtils.toString(response.getEntity()); 
	    		String [] str = parseJson(responseBody, "ip");
	    		SHG_IP = str[0] + ":" + external_ip_port;
	    		action.Update(TABLE_NAME[0], new String [] {val, SHG_IP}, name + "= ?", new String [] {ipv4});
	    		Log.d("OUTPUT", SHG_IP);

	    	} catch (ClientProtocolException e) {
	 			// TODO Auto-generated catch block
	 		} catch (IOException e) {
	 			// TODO Auto-generated catch block
	 		}
	    	
	    	return null;	
		}
	}
	
	//The input example is /XXX/XXX, key1, val1, key2, val2, ...
	private class RegisterGateway extends AsyncTask<String, Integer, String> {
		
		String response_str [];		
		RegisterGateway(String ...strings) {
			response_str = new String [strings.length];
			for(int i = 0; i < strings.length; i++)
				response_str[i] = strings[i];
		}
		
		protected String doInBackground(String... arg0) {			
			
	    	try {
	    		HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost("http://" + SWoT_IP + "/task_manager/v1/register");
	    		
	    		// Add your data
	    		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
	    		 
	    		nameValuePairs.add(new BasicNameValuePair("ip", "http://" + SHG_IP + "/"));
	    		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	    		// Execute HTTP Post Request
	    		HttpResponse response = httpclient.execute(httppost);	    		 
	    			    		
	    		//Parse response
	    		String responseBody = EntityUtils.toString(response.getEntity()); 
	    		String [] str = parseJson(responseBody, "gateway_id", "gateway_secret");
	    		Log.d("output", str[0] + ", " + str[1]);
	    		action.Update(TABLE_NAME[0], new String [] {val, str[0]}, name + "= ?", new String [] {gw_id});
	    		action.Update(TABLE_NAME[0], new String [] {val, str[1]}, name + "= ?", new String [] {ser_key});
	    		key = action.GetSecretKey();
	    		SHG_ID = action.GetGatewayID();

	    	} catch (ClientProtocolException e) {
	 			// TODO Auto-generated catch block
	 		} catch (IOException e) {
	 			// TODO Auto-generated catch block
	 		}
	    	
	    	return null;	
		}
	}
	
	private boolean HTTPost(String url, String ...pair) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://" + SWoT_IP + url);
	    	
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(pair.length / 2);
	    	
			for(int i = 0; i < pair.length; i += 2)
				nameValuePairs.add(new BasicNameValuePair(pair[i], pair[i + 1]));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	    	// Execute HTTP Post Request
	    	HttpResponse response = httpclient.execute(httppost);
	    	return true;

	    } catch (ClientProtocolException e) {
	    	// TODO Auto-generated catch block
	    } catch (IOException e) {
	    	// TODO Auto-generated catch block
	    }
		
		return false;
	}
	
	private boolean HTTPut(HttpClient httpclient, String url, String ...pair) {
		try {
			HttpPost httppost = new HttpPost("http://" + SWoT_IP + url);
	    	
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(pair.length / 2);
	    	
			for(int i = 0; i < pair.length; i += 2)
				nameValuePairs.add(new BasicNameValuePair(pair[i], pair[i + 1]));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	    	// Execute HTTP Post Request
	    	HttpResponse response = httpclient.execute(httppost);
	    	return true;

	    } catch (ClientProtocolException e) {
	    	// TODO Auto-generated catch block
	    } catch (IOException e) {
	    	// TODO Auto-generated catch block
	    }
		
		return false;
	}
	
	
	private boolean CreateAttribute(HttpClient httpclient, String gwId, String gwSecret, String deviceModel, String deviceId)
	{
		try {
			Log.d("Wrong : ", gwId + "_" + gwSecret);
			HttpGet get = new HttpGet("http://" + SWoT_IP + "/task_manager/v1/attribute?gw_id=" + gwId + "&gw_secret=" + gwSecret + "&model=" + deviceModel);

    		// Execute HTTP Post Request
    		HttpResponse response = httpclient.execute(get);
    		
    		//Parse response
    		String responseBody = EntityUtils.toString(response.getEntity()); 
    		String [] str = parseJson(responseBody, "device_class_id", "devices");
    		action.Update(TABLE_NAME[1], new String [] {Cid, str[0]}, Did + "= ?", new String [] {deviceId});
    		
    		try {
				JSONArray jsonArray = new JSONArray(str[1]);
				for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						
						action.Insert(TABLE_NAME[2],
								Did, deviceId,
								name, jsonObject.getString("attribute_name"),
								val, jsonObject.getString("default_value"),
								status, "0");
						Log.d("output", jsonObject.getString("attribute_name") + "___" + jsonObject.getString("default_value"));
				}
				
				return true;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    	} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 		}	
		
		return false;
	}
	
	private class UpdateDeviceModel extends AsyncTask<Integer, Integer, String> {	//Notify the sensor to take certain action.
	     protected String doInBackground(Integer... arg0) {
	    	 
	    	 try {
	 			HttpClient httpclient = new DefaultHttpClient();
	 			HttpGet get = new HttpGet("http://" + SWoT_IP + "/task_manager/v1/model");

	     		// Execute HTTP Post Request
	     		HttpResponse response = httpclient.execute(get);
	     		
	     		//Parse response
	     		String responseBody = EntityUtils.toString(response.getEntity()); 
	     		String [] str = parseJson(responseBody, "message");
	     		String [] model = SpliteString(str[0]);	     		
	     		action.UpdateAttribute(model);

	     	} catch (ClientProtocolException e) {
	  			// TODO Auto-generated catch block
	  		} catch (IOException e) {
	  			// TODO Auto-generated catch block
	  		}	
	 		
	 		return null;
	     }
	}
	
	private class PostEvent extends AsyncTask<Integer, Integer, String> {	//Notify the sensor to take certain action.
	     protected String doInBackground(Integer... arg0) {
	    	 while(key.equals(null)){}
	    	 
	    	 try {
	    	        Socket s = new Socket("localhost", arg0[0]);
	    	        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
	    	        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
	    	        //send output msg
	    	        String outMsg = "!!!!!!!!TCP connecting to " + arg0[0] + System.getProperty("line.separator"); 
	    	        out.write(outMsg);
	    	        out.flush();
	    	        Log.i("TcpClient", "sent: " + outMsg);
	    	        //accept server response
	    	        String inMsg = in.readLine() + System.getProperty("line.separator");
	    	        Log.i("TcpClient", "received: " + inMsg);
	    	        //close connection
	    	        s.close();
	    	 } catch (UnknownHostException e) {
	    		 e.printStackTrace();
	    	 } catch (IOException e) {
	    		 e.printStackTrace();
	    	 } 
	    	 
	    	 return null;
	     }
	}
	
	private class EventListener extends AsyncTask<Integer, Integer, String> {
    	
		String incomingMsg;
    	protected String doInBackground(Integer... arg0) {
	    	 
    		ServerSocket ss = null;
    		try {
    			ss = new ServerSocket(arg0[0]);
    			Socket s;
    			//ss.setSoTimeout(10000);
    			//accept connections
    			while(true)
    			{
	    			s = ss.accept();
	    			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
	    			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
	    			//Receive a message.
	    			incomingMsg = in.readLine();
	    			Log.i("TcpServer", "GET message: " + incomingMsg);
	    			
	    			EventActuator(incomingMsg);
	    			
	    			//Response a message.
	    			String outgoingMsg = "Successful"+ System.getProperty("line.separator");
	    			out.write(outgoingMsg);
	    			out.flush();
	    			Log.i("TcpServer", "sent: " + outgoingMsg);
	    			s.close();
	    			
	    			//publishProgress(); 	//Call the UI thread (in onProgressUpdate)	    			
    			}    			
    		} catch (InterruptedIOException e) {
    			//if timeout occurs
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		} finally {
    			if (ss != null) {
    				try {
    					ss.close();
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    			}
    		}    	
	    	
    		return null;
    	}
    	
    	protected void onProgressUpdate(Integer... progress) {
    		 Toast.makeText(getBaseContext(), "RECEIVE...Message...", Toast.LENGTH_SHORT).show();
    	}
    }
	
	private String EventActuator(String data) {
		HttpClient httpclient = new DefaultHttpClient();
		
		String [] info = SpliteString(data);
		Command cmd = Command.valueOf(info[0]);
		
		String [] device = null;
		device = action.GetDevice(info[1], info[2]);	//info => 0: device_id, 1:master_id, 2:slave_id, 3:model_id, 4:class_id, 5:status, 6:description
		

		switch(cmd) {
			case INITIAL:
				if(device != null || action.IfDeviceSupport(info[3]) == -1)	//Determine if the device is supported or not
					break;
				
				//Create new device on the SHG.
				action.Insert(TABLE_NAME[1],
						Mid, info[3],
						MTid, info[1],
						SVid, info[2],
						Cid, null,
						status, "0",
						des, info[3]);
				
				//Get device profile.
				device = action.GetDevice(info[1], info[2]);
				
				this.CreateAttribute(httpclient, SHG_ID, key, device[1], device[0]);
				
				Log.d("Command", "INITIAL");
				
				//Register device to SWoT
				this.HTTPost("/task_manager/v1/device/register",
						"gw_id", SHG_ID,
						"gw_secret", key,
						"device_id", device[0],
						"model", device[1],
						"name", device[6]);
				
				Log.d("POST", SHG_ID + "," + key + "," + device[0] + "," + device[1] + "," + device[6]);

				break;
			case SEND_DATA:
				if(device == null)
					break;
				/*action.Insert(TABLE_NAME[4],
						Did, device[0],
						Dtime, info[3],
						Ddata, info[4]);*/
				if(device[4].equals("1")) {	//Class id is equal to 1. It's a sensor.
					if(action.getDataNumber(device[0]) == 0) {
						action.Insert(TABLE_NAME[4],
								Did, device[0],
								Dtime, info[3],
								Ddata, info[4]);
					}				
					else
						action.Update(TABLE_NAME[4], new String [] {
								Dtime, info[3],
								Ddata, info[4] },
								Did + "=?", new String [] {device[0]} );
				}
				else {	////Class id is equal to 3. It's a multi media device.
					if(action.getDataNumber(device[0]) < 15)
						action.Insert(TABLE_NAME[4],
								Did, device[0],
								Dtime, info[3],
								Ddata, "http://" + SHG_IP + "/" + device[1] + "_" + device[2] + "/" + info[4]);
					else
						action.UpdateNewestData(device[0], info[3], "http://" + SHG_IP + "/" + device[1] + "_" + device[2] + "/" + info[4]);
				}
				
				Log.d("Command", "SEND_DATA");
				



				if(info[4].equals("1")) {
					this.HTTPost("/task_manager/v1/postdata",
							"gw_id", SHG_ID,
							"gw_secret", key,
	
	
							"device_id", device[0],
							"data", info[4],
							"service_id", "3",
							"time", info[3]);
				}

				
				break;
			case SET_ATTRIBUTE:
				if(device == null)
					break;
				action.Update(TABLE_NAME[2], new String [] {val,info[4], status, "0"}, Did + "=? AND " + name + "=?", new String[]{device[0], info[3]});
				
				this.HTTPut(httpclient, "/task_manager/v1/device/register",
						"gw_id", SHG_ID,
						"gw_secret", key,
						"device_id", device[0],
						"model", device[1],
						"name", device[6]);
				
				Log.d("Command", "SET_ATTRIBUTE");
				
				break;
			case GET_ATTRIBUTE:
				if(device == null)
					break;
				action.getAttribute(device[0], info[3]);
				Log.d("Command", "GET_ATTRIBUTE");
				
				break;
		}
		
		return null;
	}

	private String [] SpliteString(String str)
	{
		String [] splite_str = null;		
		splite_str = str.split(":");
		
		return splite_str;
	}
	
	private String [] parseJson(String strJson, String ...para_name)
	{
        String [] output = null;
              
        try {
        	JSONObject object = (JSONObject) new JSONTokener(strJson).nextValue();        	
        	output = new String [para_name.length];
        	
        	for(int i = 0; i < para_name.length; i++)
        		output[i] = object.getString(para_name[i]);
        		
         } catch (JSONException e) {
  
             e.printStackTrace();
         }
		
		return output;
	}
	
	private enum Command {
		INITIAL, SEND_DATA, SET_ATTRIBUTE, GET_ATTRIBUTE
	}
	
	//**************** Monitor ****************
	
	private Runnable Monitor = new Runnable() {
        public void run() {
        	Toast.makeText(getBaseContext(), "Monitor", Toast.LENGTH_SHORT).show();
        	
        	if(counter == 0) {
	        	HTTPost("/task_manager/v1/monitor",
						"gw_id", SHG_ID,
						"gw_secret", key,
						"time", SystemData.getCurrentTime());
        	}
        	
        	counter = (counter + 1) % 60;
            handler.postDelayed(this, 1000);
        }
    };
}
