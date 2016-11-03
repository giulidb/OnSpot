package it.unipi.iet.onspot.utilities;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class ArrayAdapterWithIcon extends ArrayAdapter<String> {

    private TypedArray images;
    String TAG = "ArrayAdapterWithIcon";


    public ArrayAdapterWithIcon(Activity activity, String[] items,TypedArray images) {
        super(activity, android.R.layout.select_dialog_item, items);
        Log.d(TAG,"Constructur Called");
        this.images = images;
    }


    @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(18);
        Log.d(TAG,"getView: "+images.getResourceId(position,-1)+" "+position);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(images.getResourceId(position,-1), 0, 0, 0);
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(images.getResourceId(position,-1), 0, 0, 0);
        }
        textView.setCompoundDrawablePadding(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics()));
        return view;
           }

}