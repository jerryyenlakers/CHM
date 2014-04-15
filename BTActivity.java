package com.example.chm;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//androidplot
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.androidplot.Plot;
import com.androidplot.util.PlotStatistics;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

import com.example.chm.R;

public class BTActivity extends Activity implements OnItemClickListener, SensorEventListener {

	// androidplot
	private static final int HISTORY_SIZE = 300; // number of points to plot in
													// history
	private SensorManager sensorMgr = null;
	private Sensor orSensor = null;

	private XYPlot aprHistoryPlot = null;

	private CheckBox hwAcceleratedCb;
	private CheckBox showFpsCb;
	private SimpleXYSeries azimuthHistorySeries = null;
	private SimpleXYSeries pitchHistorySeries = null;
	private SimpleXYSeries rollHistorySeries = null;

	private Redrawer redrawer;

	ArrayAdapter<String> listAdapter;

	ListView listView;
	// comment Listview, errors delet.
	BluetoothAdapter btAdapter;
	Set<BluetoothDevice> devicesArray;
	ArrayList<String> pairedDevices;
	ArrayList<BluetoothDevice> devices;
	public static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static UUID new_UUID = (UUID) null;
	protected static final int SUCCESS_CONNECT = 0;
	protected static final int MESSAGE_READ = 1;
	private static final String TAG = "BTTEST";
	private TextView statusTV;

	IntentFilter filter;
	BroadcastReceiver receiver;
	String tag = "debugging";
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			// TODO Auto-Generated method sub
			super.handleMessage(msg);
			switch (msg.what) {
			case SUCCESS_CONNECT:
				// DO something
				Log.v(TAG, "In handler");
				ConnectedThread connectedThread = new ConnectedThread(
						(BluetoothSocket) msg.obj);
				connectedThread.start();

				Toast.makeText(getApplicationContext(), "CONNECT", 0).show();
				String s = " Successfully Connected";
				// connectedThread.write(s.getBytes());

				break;
			case MESSAGE_READ:
				String str = (String) msg.obj;
				statusTV.setText(str);

				// Toast.makeText(getApplicationContext(), string, 0).show();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bt_activity);
		init();
		if (btAdapter == null) {
			Toast.makeText(getApplicationContext(), "No Bluetooth Detected", 0)
					.show();
			finish();
		} else {
			if (!btAdapter.isEnabled()) {
				turnOnBT();

			}

			getPairedDevices();
			startDiscovery();
		}
		// mHandler = new Handler();

		String name = getIntent().getExtras().getString("nameExtra");
		String age = getIntent().getExtras().getString("ageExtra");
		String sex = getIntent().getExtras().getString("sexExtra");

		TextView nameText = (TextView) this.findViewById(R.id.textName);
		TextView ageText = (TextView) this.findViewById(R.id.textAge);
		TextView sexText = (TextView) this.findViewById(R.id.textSex);

		nameText.setText("Name: " + name);
		ageText.setText("Age: " + age);
		sexText.setText("Sex: " + sex);

		// Button save_button = (Button) this.findViewById(R.id.button_save);

		Button menu_button = (Button) this.findViewById(R.id.button_menu);
		menu_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent startMenu = new Intent(BTActivity.this,
						MainActivity.class);
				startActivity(startMenu);
			}
		});

		Button sms_button = (Button) this.findViewById(R.id.button_sms);
		sms_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				SmsManager smsManager = SmsManager.getDefault();
				String phone = getIntent().getExtras().getString("phoneExtra");
				String smsText = "ECG Alert!!!";
				smsManager.sendTextMessage(phone, null, smsText, null, null);
				// startActivity();

			}
		});

		// androidplot
		// setup the APR History plot:
		aprHistoryPlot = (XYPlot) findViewById(R.id.aprHistoryPlot);

		azimuthHistorySeries = new SimpleXYSeries("Az.");
		azimuthHistorySeries.useImplicitXVals();
		pitchHistorySeries = new SimpleXYSeries("Pitch");
		pitchHistorySeries.useImplicitXVals();
		rollHistorySeries = new SimpleXYSeries("Roll");
		rollHistorySeries.useImplicitXVals();

		aprHistoryPlot.setRangeBoundaries(-180, 359, BoundaryMode.FIXED);
		aprHistoryPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
		aprHistoryPlot.addSeries(azimuthHistorySeries,
				new LineAndPointFormatter(Color.rgb(100, 100, 200), null, null,
						null));
		aprHistoryPlot.addSeries(pitchHistorySeries, new LineAndPointFormatter(
				Color.rgb(100, 200, 100), null, null, null));
		aprHistoryPlot.addSeries(rollHistorySeries, new LineAndPointFormatter(
				Color.rgb(200, 100, 100), null, null, null));
		aprHistoryPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
		aprHistoryPlot.setDomainStepValue(HISTORY_SIZE / 10);
		aprHistoryPlot.setTicksPerRangeLabel(3);
		aprHistoryPlot.setDomainLabel("Sample Index");
		aprHistoryPlot.getDomainLabelWidget().pack();
		aprHistoryPlot.setRangeLabel("Angle (Degs)");
		aprHistoryPlot.getRangeLabelWidget().pack();

		aprHistoryPlot.setRangeValueFormat(new DecimalFormat("#"));
		aprHistoryPlot.setDomainValueFormat(new DecimalFormat("#"));

		// setup checkboxes:
		hwAcceleratedCb = (CheckBox) findViewById(R.id.hwAccelerationCb);
		final PlotStatistics histStats = new PlotStatistics(1000, false);

		aprHistoryPlot.addListener(histStats);
		hwAcceleratedCb
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton compoundButton,
							boolean b) {
						if (b) {
							aprHistoryPlot.setLayerType(View.LAYER_TYPE_NONE,
									null);
						} else {
							aprHistoryPlot.setLayerType(
									View.LAYER_TYPE_SOFTWARE, null);
						}
					}
				});

		showFpsCb = (CheckBox) findViewById(R.id.showFpsCb);
		showFpsCb
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton compoundButton,
							boolean b) {
						histStats.setAnnotatePlotEnabled(b);
					}
				});

		// register for orientation sensor events:
		sensorMgr = (SensorManager) getApplicationContext().getSystemService(
				Context.SENSOR_SERVICE);
		for (Sensor sensor : sensorMgr.getSensorList(Sensor.TYPE_ORIENTATION)) {
			if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
				orSensor = sensor;
			}
		}

		// if we can't access the orientation sensor then exit:
		if (orSensor == null) {
			System.out.println("Failed to attach to orSensor.");
			cleanup();
		}

		sensorMgr.registerListener(this, orSensor,
				SensorManager.SENSOR_DELAY_UI);

		redrawer = new Redrawer(Arrays.asList(new Plot[] { aprHistoryPlot }),
				100, false);

	}

	private void startDiscovery() {
		// TODO Auto-generated method stub
		btAdapter.cancelDiscovery();
		btAdapter.startDiscovery();

	}

	private void turnOnBT() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(intent, 1);

	}

	private void getPairedDevices() {
		// TODO Auto-generated method stub
		devicesArray = btAdapter.getBondedDevices();
		if (devicesArray.size() > 0) {
			for (BluetoothDevice device : devicesArray) {
				pairedDevices.add(device.getName());

			}
		}

	}

	private void init() {

		// TODO Auto-generated method stub

		listView = (ListView) findViewById(R.id.ListView);
		listView.setOnItemClickListener(this);
		listAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, 0);

		listView.setAdapter(listAdapter);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		pairedDevices = new ArrayList<String>();
		filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		devices = new ArrayList<BluetoothDevice>();

		statusTV = (TextView) findViewById(R.id.tvPD);
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();

				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					BluetoothDevice device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					/*
					 * if(device.getName().contains("linvor")){ ConnectThread
					 * connect = new ConnectThread(device); connect.start(); }
					 */

					// ///////////////////////////////////////////////
					devices.add(device);

					String s = "";
					for (int a = 0; a < pairedDevices.size(); a++) {
						if (device.getName().equals(pairedDevices.get(a))) {
							// append

							s = "(Paired)";
							Toast.makeText(getApplicationContext(),
									"Attempting connection...",
									Toast.LENGTH_SHORT).show();
							// if(!pairedDevices.isEmpty())
							// new_UUID=UUID.fromString(pairedDevices.get(a));
							break;

						}
					}

					// if it is paired
					listAdapter.add(device.getName() + "  " + s + " " + "\n"
							+ device.getAddress());
				}

				// ///////////////////////////////////////////////////////////////////

				else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
					// run some code
					if (btAdapter.getState() == btAdapter.STATE_OFF) {
						turnOnBT();

					}
				}
			}
		};
		registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);

	}

	// androidplot
	@Override
	public void onResume() {
		super.onResume();
		redrawer.start();
	}

	@Override
	protected void onPause() {
		// androidplot
		redrawer.pause();
		// TODO auto-generated method stud
		super.onPause();
		unregisterReceiver(receiver);

	}

	@SuppressLint("ShowToast")
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CANCELED) {
			Toast.makeText(getApplicationContext(),
					"Bluetooth must be Enabled to continue", Toast.LENGTH_SHORT)
					.show();

			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.newuser, menu);
		return true;
	}

	// /////////////////////////////////////////////////////////////
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

		if (btAdapter.isDiscovering()) {
			btAdapter.cancelDiscovery();

		}
		if (listAdapter.getItem(arg2).contains("Paired")) {

			BluetoothDevice selectedDevice = devices.get(arg2);
			ConnectThread connect = new ConnectThread(selectedDevice);
			connect.start();
		} else {
			Toast.makeText(getApplicationContext(), "Device is not paired", 0)
					.show();

		}
	}

	// /////////////////////////////////////////////////////////////////////////

	private class ConnectThread extends Thread {

		private BluetoothSocket mmSocket;
		private BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server
				// code
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
				mmSocket = tmp;

			} catch (IOException e) {
				Log.e(TAG, e.toString());
				// mmSocket = null;
			}
			// mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			btAdapter.cancelDiscovery();

			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();
				Log.v(TAG, "Connected to the device");
				mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket)
						.sendToTarget();
			} catch (IOException connectException) {
				Log.e(TAG, "Fail to connect. " + connectException.toString());
				// Unable to connect; close the socket and get out

				try {
					mmSocket.close();
				} catch (IOException closeException) {
					Log.e(TAG, "Fail to close");
				}
				return;
			}

			// Do work to manage the connection (in a separate thread)

			// mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();

		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();

			} catch (IOException e) {
				Log.e(TAG, e.toString());
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer, buf; // buffer store for the stream
			// byte[] buffer1, buf1;
			String line = null;

			int bytes; // bytes returned from read()
			BufferedReader inbuf = new BufferedReader(new InputStreamReader(
					mmInStream));

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					/*
					 * // Read from the InputStream buffer = new byte[1024]; //
					 * buffer1 = (0xFF & buffer); // bytes =
					 * mmInStream.read(buffer); if ((line = inbuf.readLine()) !=
					 * null) { // Log.v(TAG, line);
					 * 
					 * mHandler.obtainMessage(MESSAGE_READ, -1, -1, line)
					 * .sendToTarget(); }
					 */

					char[] charBuf = new char[4];
					if ((inbuf.read(charBuf, 0, 1)) != 0) {
						Log.i(TAG, String.valueOf(charBuf[0]));

						mHandler.obtainMessage(MESSAGE_READ, 0, 0,
								String.valueOf(charBuf[0])).sendToTarget();
					}

					// Send the obtained bytes to the UI activity

				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		/*
		 * public void write(byte[] bytes) { try { mmOutStream.write(bytes); }
		 * catch (IOException e) { } }
		 */

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	@Override
	public void onDestroy() {
		redrawer.finish();
		super.onDestroy();
	}

	private void cleanup() {
		// aunregister with the orientation sensor before exiting:
		sensorMgr.unregisterListener(this);
		finish();
	}

	// Called whenever a new orSensor reading is taken.
	public synchronized void onSensorChanged(SensorEvent sensorEvent) {

		// get rid the oldest sample in history:
		if (rollHistorySeries.size() > HISTORY_SIZE) {
			rollHistorySeries.removeFirst();
			pitchHistorySeries.removeFirst();
			azimuthHistorySeries.removeFirst();
		}

		// add the latest history sample:
		azimuthHistorySeries.addLast(null, sensorEvent.values[0]);
		pitchHistorySeries.addLast(null, sensorEvent.values[1]);
		rollHistorySeries.addLast(null, sensorEvent.values[2]);
	}

	public void onAccuracyChanged(Sensor sensor, int i) {
		// Not interested in this event
	}
}
