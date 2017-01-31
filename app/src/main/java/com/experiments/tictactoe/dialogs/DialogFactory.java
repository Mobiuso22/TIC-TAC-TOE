package com.experiments.tictactoe.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.experiments.tictactoe.R;

import java.util.ArrayList;
import java.util.List;



public class DialogFactory {

    private List<Dialog> activeDialogs;
    private Context context;

    private DialogFactory(Context context) {
        this.context = context;
        activeDialogs = new ArrayList<>();
    }

    public static DialogFactory create(Context context) {
        return new DialogFactory(context);
    }

    public void dismissDialogs() {
        for (Dialog activeDialog : activeDialogs) {
            if (activeDialog != null && activeDialog.isShowing()) {
                activeDialog.dismiss();
            }
        }
    }

    public void showRoomIdDialog(String title, String message, final RoomDialogListener roomDialogListener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.copy, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                roomDialogListener.onCopy();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.show();
        activeDialogs.add(dialog);
    }

    public void showWinLooseDialog(String title, String message, final WinLooseDialogListener winLooseDialogListener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.start_new_game, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                winLooseDialogListener.onNewGame();
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.show();
        activeDialogs.add(dialog);
    }

    public interface WinLooseDialogListener{
        void onNewGame();
    }

    public interface RoomDialogListener {
        void onCopy();
    }
}
