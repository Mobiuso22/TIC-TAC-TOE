package com.experiments.tictactoe;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;


import com.experiments.tictactoe.activities.EnterGameActivity;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.normal_main);
		final Animation animScale = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.scale);
		final TextView text = (TextView) findViewById(R.id.tictac);
		text.startAnimation(animScale);

	}

	public void exit_click(View v)
	{
		AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
		//AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this,R.style.AppCompatAlertDialogStyle);
		//this.getWindow().setBackgroundDrawableResource(R.drawable.square);
		dlgAlert.setMessage("Do you really want to exit?");
		dlgAlert.setTitle("Exit");

		dlgAlert.setCancelable(true);
		dlgAlert.setPositiveButton("Ok",
			    new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) {
			        	finish();
			            System.exit(0);
			        }
			    });
		dlgAlert.create().show();
	}
	public void about_click(View v)
	{
		Intent myIntent = new Intent(MainActivity.this, About.class);
		MainActivity.this.startActivity(myIntent);
	}
	public void normal_click(View v)
	{
		Intent myIntent = new Intent(MainActivity.this,NormalActivity.class);
		MainActivity.this.startActivity(myIntent);
	}
	public void ultimate_click(View v)
	{
		Intent myIntent = new Intent(MainActivity.this,EnterGameActivity.class);
		MainActivity.this.startActivity(myIntent);
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	}
	

}
