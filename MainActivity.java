package com.example.chm;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button newuser_button = (Button) this.findViewById(R.id.button_newuser);
		newuser_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent startNewuser = new Intent(MainActivity.this,
						Newuser.class);
				startActivity(startNewuser);
			}
		});



	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
