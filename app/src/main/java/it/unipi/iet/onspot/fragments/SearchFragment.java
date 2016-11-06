package it.unipi.iet.onspot.fragments;


import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import it.unipi.iet.onspot.R;

//TODO: vedere se cambiando il layout si può rendere più guardabile
public class SearchFragment extends DialogFragment {

    private EditText locationSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        locationSearch = (EditText) view.findViewById(R.id.search_text);
        return view;
    }

    public String getLocation() {
        return locationSearch.getText().toString();
    }

}
