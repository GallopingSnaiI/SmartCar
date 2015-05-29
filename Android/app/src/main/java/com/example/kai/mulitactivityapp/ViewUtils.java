package com.example.kai.mulitactivityapp;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Tobias on 5/17/2015.
 */
public class ViewUtils {

    public static ViewGroup getParent(View view) {
        return (ViewGroup)view.getParent();
    }

    public static void removeView(View view) {
        ViewGroup parent = getParent(view);
        if(parent != null) {
            parent.removeView(view);
        }
    }


    public static void replaceView(View currentView, View newView) {

        currentView.setVisibility(View.GONE);
        newView.setVisibility(View.VISIBLE);
        /*ViewGroup parent = getParent(currentView);
        if(parent == null) {
            return;
        }
        final int index = parent.indexOfChild(currentView);
        removeView(currentView);
        removeView(newView);
        parent.addView(newView, index);*/
    }


}
