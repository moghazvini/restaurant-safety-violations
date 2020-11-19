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

/**
 * Loading pop up that shows that the restaurant and inspections lists
 * are downloading.
 * Gives the user the option to cancel the download.
 */
public class LoadingDialogFragment extends AppCompatDialogFragment {
    private static final String TAG = "DialogFragmentTag";
    private CancelDialogListener cancelListener;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // create view
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.loading_dialog_layout, null);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    cancelListener.sendCancel(true);
                }
            }
        };
        // code for progress bar in loading_dialog_layout from https://www.youtube.com/watch?v=K5bFv_WDjVY
        return new AlertDialog.Builder(getActivity())
                .setTitle("Updating...")
                .setView(v)
                .setNegativeButton(android.R.string.cancel, listener)
                .create();

    }

    // code to send data from dialog to activity from https://www.youtube.com/watch?v=ARezg1D9Zd0
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            cancelListener = (CancelDialogListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, "On attach: ClassCastException" + e.getMessage());
        }
    }

    public interface CancelDialogListener{
        void sendCancel(boolean input);
    }
}
