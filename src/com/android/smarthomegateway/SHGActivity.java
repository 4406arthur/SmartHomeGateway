package com.android.smarthomegateway;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SHGActivity extends Activity {
	private Button startButton;
    private Button stopButton;
    private Button showButton;
    private EditText portEditText;
    
    private TextView idtext;
    private TextView keytext;

	//private SQLite sqlite;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		startButton = (Button) findViewById(R.id.startbutton);
        stopButton = (Button) findViewById(R.id.stopbutton);
        showButton = (Button) findViewById(R.id.show);
        
        portEditText = (EditText) findViewById(R.id.editText1);
        idtext = (TextView) findViewById(R.id.GW_ID);
        keytext = (TextView) findViewById(R.id.GW_SECRET);
        
        startButton.setOnClickListener(startClickListener);
        stopButton.setOnClickListener(stopClickListener);
        showButton.setOnClickListener(showClickListener);
	}
	
	private Button.OnClickListener startClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            //啟動服務
        	Intent intent = new Intent(SHGActivity.this, SHGService.class);
        	intent.putExtra("ip_port", portEditText.getText());
            startService(intent);
        }
    };

    private Button.OnClickListener stopClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            //停止服務
        	Intent intent = new Intent(SHGActivity.this, SHGService.class);
            stopService(intent);
        }
    };
    
    private Button.OnClickListener showClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            //停止服務
        	SQLiteAction sql = new SQLiteAction();
        	idtext.setText("GW_ID:" + sql.GetGatewayID());
        	keytext.setText("GW_SECRET:" + sql.GetSecretKey());
        }
    };
}
