package com.experiments.tictactoe.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.CallSuper;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager;



public abstract class BaseActivity extends AppCompatActivity {

    private boolean wasNetworkDisconnected;


    @CallSuper
    @Override
    protected void onStart() {
        super.onStart();
        wasNetworkDisconnected = false;
        IntentFilter connectivityFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, connectivityFilter);
    }

    @CallSuper
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(networkReceiver);
    }


    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isDestroyed()) return;
            if (isInternetAvailable(context)) {
                if (!wasNetworkDisconnected) return;
                onNetworkConnected();
                wasNetworkDisconnected = false;
            } else {
                if (wasNetworkDisconnected) return;
                onNetworkDisconnected();
                wasNetworkDisconnected = true;
            }
        }
    };

    protected final boolean isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isAvailable() && activeNetworkInfo.isConnected();
    }

    protected final void showMessage(String error) {
        Snackbar.make(
                findViewById(android.R.id.content),
                error,
                Snackbar.LENGTH_SHORT
        ).show();
    }

    protected final void hideKeyBoard(){
        if (getCurrentFocus()!=null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromInputMethod(getCurrentFocus().getWindowToken(), 0);
        }
    }

    protected abstract void init();

    protected abstract void onNetworkConnected();

    protected abstract void onNetworkDisconnected();


}
