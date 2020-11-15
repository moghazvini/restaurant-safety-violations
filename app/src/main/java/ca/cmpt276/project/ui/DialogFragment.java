package ca.cmpt276.project.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import ca.cmpt276.project.R;

public class DialogFragment extends AppCompatDialogFragment {
    private static final String TAG = "DialogFragmentTag";
    private UpdateDialogListener dialogListener;
    boolean userInput = true;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // create view
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.update_dialog_layout, null);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Toast.makeText(getActivity(), "positive", Toast.LENGTH_SHORT).show();
                        userInput = true;
                        dialogListener.sendInput(userInput);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        Toast.makeText(getActivity(), "negative", Toast.LENGTH_SHORT).show();
                        userInput = false;
                        dialogListener.sendInput(userInput);
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            dialogListener = (UpdateDialogListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, "On attach: ClassCastException" + e.getMessage());
        }
    }

    public interface UpdateDialogListener{
        void sendInput(boolean input);
    }
}