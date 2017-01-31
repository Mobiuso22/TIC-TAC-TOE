package com.experiments.tictactoe;


import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;


public class DialogActivity extends Activity {

    private Button buttonClick;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.custom_dialog_main);

        buttonClick = (Button) findViewById(R.id.declineButton);

        // add listener to button
        buttonClick.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                // Create custom dialog object
                final Dialog dialog = new Dialog(DialogActivity.this);
                // Include dialog.xml file
                dialog.setContentView(R.layout.dialog);
                // Set dialog title
                dialog.setTitle("Custom Dialog");

                // set values for custom dialog components - text, image and button
                TextView text = (TextView) dialog.findViewById(R.id.textDialog);
                text.setText("Custom dialog Android example.");
                ImageView image = (ImageView) dialog.findViewById(R.id.imageDialog);
                //image.setImageResource(R.drawable.image0);

                dialog.show();

                Button declineButton = (Button) dialog.findViewById(R.id.declineButton);
                // if decline button is clicked, close the custom dialog
                declineButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Close dialog
                        dialog.dismiss();
                    }
                });


            }

        });

    }

}