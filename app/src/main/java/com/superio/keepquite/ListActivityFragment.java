package com.superio.keepquite;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.media.AudioManager;
import android.support.v4.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.superio.keepquite.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kumar_thangaraj on 04/10/15.
 */
public class ListActivityFragment extends Fragment {
    private ArrayList<HashMap<String, String>> list;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list, null);
        ListView listView = (ListView) view.findViewById(R.id.customListView);
        ListData.setListAdapter(getActivity());
        ListData.formListData(getActivity());
        listView.setAdapter(ListData.listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                showRecOptions(parent,view,position,id);
            }
        });
        return view;
    }

    public void editKeepQuite(int position){
        HashMap locHashMap = (HashMap)(ListData.listAdapter.getList()).get(position);
        String prefName = (String)locHashMap.get("prefName");
        SharedPreferences locPref = getActivity().getSharedPreferences(prefName, Context.MODE_PRIVATE);
        Intent removeIntent = new Intent(getActivity(),KeepQuiteSetter.class);
        Map<String,?> prefHashMap = locPref.getAll();
        String fromHour = (prefHashMap.get("fromHour")).toString();
        String fromMin = (prefHashMap.get("fromMin")).toString();
        String fromAmPm = (prefHashMap.get("fromAmPm")).toString();
        String toHour = (prefHashMap.get("toHour")).toString();
        String toMin = (prefHashMap.get("toMin")).toString();
        String toAmPm = (prefHashMap.get("toAmPm")).toString();
        boolean daily = ((Boolean)prefHashMap.get("daily"));
        boolean weekly = ((Boolean)prefHashMap.get("weekly"));
        String selectedWeekDays = (prefHashMap.get("selectedDays")).toString();
        removeKeepQuite(position);
        String fromTime = fromHour+" : "+fromMin+" "+fromAmPm;;
        ((TextView)getActivity().findViewById(R.id.fromTimeTextView)).setText(fromTime);
        String toTime = toHour+" : "+toMin+" "+toAmPm;
        ((MainActivity)getActivity()).setWeekDaysSelected(selectedWeekDays);
        ((TextView)getActivity().findViewById(R.id.toTimeTextView)).setText(toTime);
        ((CheckBox)getActivity().findViewById(R.id.daily)).setChecked(daily);
        ((CheckBox)getActivity().findViewById(R.id.weekly)).setChecked(weekly);
        ((ViewPager)getActivity().findViewById(R.id.pager)).setCurrentItem(1);
                (((MainActivity) getActivity()).tabAdapter).notifyDataSetChanged();
        ((MainActivity)getActivity()).setScheduleDtls(fromHour,fromMin,fromAmPm,toHour,toMin,toAmPm);
    }

    public void removeKeepQuite(int position){
        HashMap locHashMap = (HashMap)(ListData.listAdapter.getList()).get(position);
        String prefName = (String)locHashMap.get("prefName");
        removeKeepQuitePrefName(prefName,true);
        ListData.formListData(getActivity());
        ListData.listAdapter.notifyDataSetChanged();
    }

    public void removeKeepQuitePrefName(String prefName, boolean activate){
        SharedPreferences locPref = getActivity().getSharedPreferences(prefName, Context.MODE_PRIVATE);
        Intent removeIntent = new Intent(getActivity(),KeepQuiteSetter.class);
        Map<String,?> prefHashMap = locPref.getAll();
        for(String key:prefHashMap.keySet()){
            Object locObj = prefHashMap.get(key);
            String[] dataType = (locObj.getClass().getName()).split("\\.");
            if(dataType[dataType.length-1].equals("String"))
                removeIntent.putExtra(key, locObj.toString());
            else if(dataType[dataType.length-1].equals("Long"))
                removeIntent.putExtra(key, (long) locObj);
            else if(dataType[dataType.length-1].equals("Int"))
                removeIntent.putExtra(key, (int) locObj);
            else if(dataType[dataType.length-1].equals("Boolean"))
                removeIntent.putExtra(key, (Boolean) locObj);
            else
                Toast.makeText(getActivity(),"key not available " + key + " data type = " + dataType[dataType.length - 1],Toast.LENGTH_SHORT).show();
        }
        if(activate && (locPref.getString("isActive","No")).equals("Yes")){
            if(ListData.checkRunningQuiet(prefName) == null) {
                AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        }

        String[] prefNameArray = prefName.split("_");

        PendingIntent rmPendingIntent = PendingIntent.getBroadcast(getActivity(),Integer.parseInt(prefNameArray[1]),removeIntent,0);
        AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(rmPendingIntent);
        locPref.edit().clear().commit();
    }

    public void showRecOptions(AdapterView<?> parent,View view, int position, long id) {
        final int locPosition = position;
        final AlertDialog.Builder recordEditor = new AlertDialog.Builder(getActivity());
        recordEditor.setTitle(getResources().getString(R.string.recordEditorTitle));
        recordEditor.setCancelable(true);
        recordEditor.setPositiveButton(getResources().getString(R.string.edit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editKeepQuite(locPosition);
            }
        });
        recordEditor.setNegativeButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeKeepQuite(locPosition);
            }

        });
        recordEditor.setNeutralButton(getResources().getString(R.string.nothing), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        recordEditor.show();
    }
}
