/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.fragments.main;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.Arrays;

import co.sahr.android.picturesque.R;
import co.sahr.android.picturesque.data.remote.Api;

public class SortDialogFragment extends DialogFragment {
    // load method arg key
    public static String ARG_SORT_CHOICE = "sort_order";
    // Instance of the interface to deliver action events to WallActivity
    SortDialogListener mListener;

    /**
     * Maps R.array.sort_choice to {@link Api} sorts
     */
    private String[] mSortChoices = {Api.MOST_RECENT, Api.MOST_POPULAR};
    private int mCheckedItem;


    public SortDialogFragment() {
        // Required empty public constructor
    }

    public static SortDialogFragment newInstance(String sortChoice) {
        SortDialogFragment fragment = new SortDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SORT_CHOICE, sortChoice);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            // Resolve the checked item which equals to the index of the sort choice in the
            // sort choices array (sort choices array indexes implicitly map to the dialog items)
            mCheckedItem = Arrays.asList(mSortChoices)
                                 .indexOf(getArguments().getString(ARG_SORT_CHOICE));
        }
    }

    /**
     * Override to build your own custom Dialog container.  This is typically
     * used to show an AlertDialog instead of a generic Dialog; when doing so,
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} does not need
     * to be implemented since the AlertDialog takes care of its own content.
     * <p>
     * <p>This method will be called after {@link #onCreate(Bundle)} and
     * before {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.  The
     * default implementation simply instantiates and returns a {@link Dialog}
     * class.
     * <p>
     * <p><em>Note: DialogFragment own the {@link Dialog#setOnCancelListener
     * Dialog.setOnCancelListener} and {@link Dialog#setOnDismissListener
     * Dialog.setOnDismissListener} callbacks.  You must not set them yourself.</em>
     * To find out about these events, override {@link #onCancel(DialogInterface)}
     * and {@link #onDismiss(DialogInterface)}.</p>
     *
     * @param savedInstanceState The last saved instance state of the Fragment,
     *                           or null if this is a freshly created Fragment.
     * @return Return a new Dialog instance to be displayed by the Fragment.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style
                .AppTheme_dialog_alert);
        builder.setTitle(R.string.title_dialog_sort_by)
               .setSingleChoiceItems(R.array.sort_choices, mCheckedItem, new DialogInterface
                       .OnClickListener() {
                   public void onClick(DialogInterface dialog, int selectedItem) {
                       // The 'selected' argument contains the index position
                       // of the selected item

                       // Check if listener is attached and the selected is not already loaded
                       if (mListener != null && mCheckedItem != selectedItem) {
                           // Pass selected sort choice to interface callback
                           // Use implicit mapping to resolve selected choice from the selected item
                           mListener.onSortDialogClick(mSortChoices[selectedItem]);
                           // dismiss the dialog once selected
                           dismiss();
                       }
                   }
               });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SortDialogListener) {
            mListener = (SortDialogListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement " +
                    "SortDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface SortDialogListener {
        void onSortDialogClick(String sortOrder);
    }

}
