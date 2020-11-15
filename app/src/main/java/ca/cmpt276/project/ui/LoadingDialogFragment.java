package ca.cmpt276.project.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatDialogFragment;

import ca.cmpt276.project.R;

public class LoadingDialogFragment extends AppCompatDialogFragment {
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // create view
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.loading_dialog_layout, null);
        // code for progress bar in loading_dialog_layout from https://www.youtube.com/watch?v=K5bFv_WDjVY
        return new AlertDialog.Builder(getActivity())
                .setTitle("Updating...")
                .setView(v)
                .create();

    }
}
