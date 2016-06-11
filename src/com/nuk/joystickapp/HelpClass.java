package com.nuk.joystickapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public class HelpClass extends Activity{
	WebView textWv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		
		String helpText = "Press the bluetooth icon to connect to the wheelchair<br/><br/>" +
				"Ensure there are not too many bluetooth devices in the " +
				"surrounding<br/><br/>Do not pair with any other device while connected " +
				"to the wheelchair<br/><br/>If the wheelchair is not detected in the scan press " +
				"bluetooth icon and try again. If still not detected " +
				"switch off the wheelchair's bluetooth and then on again<br/><br/>Detection may " +
				"take up to 10 seconds. Please be patient<br/><br/>If unable to connect in the 'Available Devices' " +
				"list then go back and scan again ";
		
		textWv = (WebView) findViewById(R.id.help_text_id);
		textWv.loadData
		("<font color=\"#ffffff\">" +
		     " <p style=\"text-align: justify\">"+ 
		           helpText +
		      "</p> " +
		"</font>"
				, "text/html", "UTF-8");
		textWv.setBackgroundColor(getResources()
				.getColor(R.color.transparent) );

	}
	
	public void dismiss(View v) {
		finish();
	}
}
