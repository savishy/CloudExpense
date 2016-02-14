package com.vish.cloudexpense;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GoogleApiFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GoogleApiFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * References:
 * http://stackoverflow.com/a/28849764/682912
 */
public class GoogleApiFragment extends Fragment {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO why am I doing this?
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Common.showSimpleDialog(getActivity(),"onActivityCreated","Activity has been created");

    }
}
