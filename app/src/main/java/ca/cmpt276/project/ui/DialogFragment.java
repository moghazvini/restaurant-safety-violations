package ca.cmpt276.project.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDialogFragment;

import ca.cmpt276.project.R;

public class DialogFragment extends AppCompatDialogFragment {
    ProgressDialog progressDialog;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // create view
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.update_dialog_layout, null);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setMessage("Loading..."); // Setting Message
                        progressDialog.setTitle("ProgressDialog"); // Setting Title
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                        progressDialog.show(); // Display Progress Dialog
                        progressDialog.setCancelable(true);


                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    Thread.sleep(5000);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                progressDialog.dismiss();
                            }
                        }).start();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        Toast.makeText(getActivity(), "negative", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        AlertDialog alert = new AlertDialog.Builder(getActivity())
                .setTitle("New Update")
                .setView(v)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, listener)
                .create();


        return alert;

    }
}