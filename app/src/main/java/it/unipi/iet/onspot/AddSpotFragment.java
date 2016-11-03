package it.unipi.iet.onspot;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

/**
 *  BottomSheetDialogFragment to display in the bottom sheet when a user want to
 *  add a new spot on the map.
 */

public class AddSpotFragment extends BottomSheetDialogFragment {

    private ImageView preview;
    private Button play;
    private Button audio;
    private EditText category;
    private EditText description;
    CoordinatorLayout.Behavior behavior;


    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

    //create and associate a BottomSheetCallback to dismiss the Fragment when the sheet is hidden
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    // Inflate a new layout file and retrieve the BottomSheetBehavior of the container view
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_addspot, null);
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        behavior = params.getBehavior();

        if( behavior != null && behavior instanceof BottomSheetBehavior ) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addspot, container, false);
        preview = (ImageView) view.findViewById(R.id.load);
        preview.setClickable(false);
        play = (Button) view.findViewById(R.id.play);
        audio = (Button) view.findViewById(R.id.audio);
        category = (EditText) view.findViewById(R.id.category);
        description = (EditText) view.findViewById(R.id.description);

        return view;
    }

    public void set_preview(Bitmap bm){

        play.setVisibility(View.INVISIBLE);
        audio.setVisibility(View.INVISIBLE);
        preview.setVisibility(View.VISIBLE);
        preview.setImageBitmap(bm);
        preview.setClickable(true);
    }

    public void add_play_button(){
        preview.setClickable(false);
        play.setVisibility(View.VISIBLE);
    }

    public void add_audio_button(){
        preview.setVisibility(View.INVISIBLE);
        play.setVisibility(View.INVISIBLE);
        audio.setVisibility(View.VISIBLE);
    }

    public void setCategory(String cat) { category.setText(cat); }

    public void setHidden() {
        ((BottomSheetBehavior) behavior).setState(BottomSheetBehavior.STATE_HIDDEN);
    }


    /*
     * Functions to retrieve the contents added by the user
     */

    public String getDescription() { return description.getText().toString(); }

    public String getCategory() { return category.getText().toString(); }

}

