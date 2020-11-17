package ca.cmpt276.project.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import ca.cmpt276.project.R;

public class DialogFragment extends AppCompatDialogFragment {
    private static final String TAG = "DialogFragmentTag";
    private UpdateDialogListener dialogListener;
    boolean userInput = true;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // code for alert dialog via fragment from Dr Brian Fraser's video https://www.youtube.com/watch?v=y6StJRn-Y-A
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.update_dialog_layout, null);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        userInput = true;
                        dialogListener.sendInput(userInput);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        userInput = false;
                        dialogListener.sendInput(userInput);
                        break;
                }
            }
        };
        return new AlertDialog.Builder(getActivity())
                .setTitle("New Update")
                .setView(v)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, listener)
                .create();
    }
    // code to send data from dialog to activity from https://www.youtube.com/watch?v=ARezg1D9Zd0
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