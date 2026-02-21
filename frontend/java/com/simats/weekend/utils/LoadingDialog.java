package com.simats.weekend.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import com.simats.weekend.R;

public class LoadingDialog {

    private final Activity activity;
    private AlertDialog dialog;

    public LoadingDialog(Activity myActivity) {
        activity = myActivity;
    }

    public void startLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_loading, null);

        // Set the custom color for the ProgressBar
        ProgressBar progressBar = dialogView.findViewById(R.id.progress_bar_dialog);
        progressBar.setIndeterminateTintList(ColorStateList.valueOf(Color.parseColor("#0D47A1")));

        builder.setView(dialogView);
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}