package com.example.chm;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class Newuser extends Activity {

	private EditText Name = null;
	private EditText Age = null;
	private EditText Phone = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newuser);

		final Spinner sex = (Spinner) this.findViewById(R.id.spinnerSex);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.sex_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sex.setAdapter(adapter);

		Name = (EditText) this.findViewById(R.id.editName);
		Age = (EditText) this.findViewById(R.id.editAge);
		Phone = (EditText) this.findViewById(R.id.editNumber);

		//final EditText EditSmsNumber = (EditText) findViewById(R.id.editNumber);
		//final EditText edittextSmsText = (EditText) findViewById(R.id.smstext);

		Button submit_button = (Button) this.findViewById(R.id.submit);
		
		submit_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Intent startSubmit = new Intent(Newuser.this, Ecg.class);
				Intent startSubmit = new Intent(Newuser.this, BTActivity.class);
				startSubmit.putExtra("phoneExtra", Phone.getText().toString());
				startSubmit.putExtra("nameExtra", Name.getText().toString());
				startSubmit.putExtra("ageExtra", Age.getText().toString());
				startSubmit.putExtra("sexExtra", sex.getSelectedItem()
						.toString());
				System.out.println("Age" + Age.toString());
				
				
				//SmsManager smsManager = SmsManager.getDefault();
				//String smsNumber = EditSmsNumber.getText().toString();
				////String smsText = edittextSmsText.getText().toString();
				//String smsText = "ECG Alert!!!";
				//smsManager.sendTextMessage(smsNumber, null, smsText, null, null);

				startActivity(startSubmit);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.newuser, menu);
		return true;
	}

}
