package com.superio.keepquite;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.superio.keepquite.R;

/**
 * Created by kumar_thangaraj on 16/10/15.
 */
public class QuiteFragment extends Fragment {

    View view;
    public QuiteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_quite, container, false);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), R.array.amPM, android.R.layout.simple_spinner_item);
        ((MainActivity)getActivity()).minsDisplayChanger();
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        ((MainActivity)getActivity()).minsDisplayChanger();
    }

}
