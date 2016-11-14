package it.unipi.iet.onspot.fragments;


import android.content.res.TypedArray;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import it.unipi.iet.onspot.R;


public class SearchFragment extends DialogFragment {

    private EditText locationSearch;

    public SearchFragment() {
        // Empty constructor is required for DialogFragment
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        locationSearch = (EditText) view.findViewById(R.id.search_text);

        return view;
    }

    @Override
    public void onResume() {

        // Change dialog style
        Window window = getDialog().getWindow();
        window.setGravity(Gravity.TOP);
        ViewGroup.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes((android.view.WindowManager.LayoutParams) params);
        final TypedArray styledAttributes = getContext().getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        int mActionBarSize = (int) styledAttributes.getDimension(0, 0);
        //styledAttributes.recycle();
        WindowManager.LayoutParams par = window.getAttributes();
        par.y = -mActionBarSize;
        window.setAttributes(par);

        // Call super onResume after sizing
        super.onResume();
    }

    public String getLocation() {
        return locationSearch.getText().toString();
    }

}
