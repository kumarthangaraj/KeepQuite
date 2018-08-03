package com.superio.keepquite;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.superio.keepquite.QuietHandler;
import com.superio.keepquite.QuiteListAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kumar_thangaraj on 05/10/15.
 */
public class ListData {

    public static QuiteListAdapter listAdapter;

    public static void setListAdapter(Activity activity){
        listAdapter = new QuiteListAdapter(activity);
    }

    public static void formListData(Context context){
        SharedPreferences pref = context.getSharedPreferences("KQ", context.MODE_PRIVATE);
        int maxPref = pref.getInt("MaxPref", 0);
        if(listAdapter == null)
            return;
        listAdapter.clear();
        listAdapter.notifyDataSetInvalidated();
        if(maxPref == 0) {
            //values = new String[]{"No keep quite Available"};
            return;
        }
        for(int i=1;i<=maxPref;i++) {
            String locPrefName = "KQ_"+i;
            HashMap<String, String> locHashMap = fillMapFormSharedPref(context,locPrefName);
            if(locHashMap == null)
                continue;
            listAdapter.add(locHashMap);
        }
        for(int i=0; i<listAdapter.getList().size(); i++){
            HashMap locHashMap = (HashMap)((ArrayList) listAdapter.getList()).get(i);
        }
    }

    public static HashMap fillMapFormSharedPref(Context context, String prefName){
        HashMap<String, String> map = new HashMap<String,String>();
        SharedPreferences listPref=context.getSharedPreferences(prefName, context.MODE_PRIVATE);
        Map<String, ?> dataMap = listPref.getAll();

            /*set from Hour details */
        String fromHour = (String)dataMap.get("fromHour");
        if(fromHour == null)
            return null;
        if(fromHour.length()==1)
            fromHour = "0"+fromHour;

            /* set from Min details */
        String fromMin = (String)dataMap.get("fromMin");
        if(fromMin.length()==1)
            fromMin = "0"+fromMin;

            /* set from AM/PM details */
        String fromAmPm = (String)dataMap.get("fromAmPm");

            /* set to Hour details */
        String toHour = (String)dataMap.get("toHour");
        if(toHour.length()==1)
            toHour = "0"+toHour;

            /* set from Min details */
        String toMin = (String)dataMap.get("toMin");
        if(toMin.length()==1)
            toMin = "0"+toMin;

            /* set from AM/PM details */
        String toAmPm = (String)dataMap.get("toAmPm");
        String freqLabel = "";

            /* set daily and weekly details */
        if(dataMap.get("daily") != null && (Boolean)dataMap.get("daily"))
            freqLabel = "Daily";

        if(dataMap.get("weekly") != null && (Boolean)dataMap.get("weekly")) {
            String days = (String)dataMap.get("selectedDays");
            freqLabel = ""+days;
        }

        String toTime = "0";
        if(dataMap.get("toTime")!=null)
            toTime = dataMap.get("toTime").toString();
        map.put("fromHour",fromHour);
        map.put("fromMin",fromMin);
        map.put("fromAmPm",fromAmPm);
        map.put("toHour",toHour);
        map.put("toMin",toMin);
        map.put("toAmPm",toAmPm);
        map.put("freqLabel",freqLabel);
        map.put("prefName",(String)dataMap.get("prefName"));
        map.put("quiteMode",(String)dataMap.get("quiteMode"));
        map.put("isActive",(String)dataMap.get("isActive"));
        map.put("toTime",toTime);
        return map;
    }

    public static String checkRunningQuiet(String prefName){
        String runningPrefName = null;
        ArrayList list = listAdapter.getList();
        for(int i=0; i<list.size();i++){
            HashMap locHashMap = (HashMap)list.get(i);

            if(locHashMap.get("isActive") != null && locHashMap.get("isActive").equals("Yes")){
                if(prefName != null && prefName.equals(locHashMap.get("prefName")))
                    continue;
                else {
                    runningPrefName = (String) locHashMap.get("prefName");
                    break;
                }
                //}
            }
        }
        return runningPrefName;
    }
    public static String checkRunningQuietToExtend(String prefName){
        String runningPrefName = null;
        ArrayList list = listAdapter.getList();
        for(int i=0; i<list.size();i++){
            HashMap locHashMap = (HashMap)list.get(i);

            if(locHashMap.get("isActive") != null && locHashMap.get("isActive").equals("Yes")){
                if(locHashMap.get("freqLabel") == null || locHashMap.get("freqLabel").equals("")) {
                    runningPrefName = (String) locHashMap.get("prefName");
                    break;
                }
            }
        }
        return runningPrefName;
    }
    public static Calendar incRunningQuite(Context context,String prefName, Calendar cal, int mins){
        SharedPreferences listPref=context.getSharedPreferences(prefName, context.MODE_PRIVATE);
        Map<String, ?> dataMap = listPref.getAll();

            /*set from Hour details */
        String fromHour = (String)dataMap.get("fromHour");
        if(fromHour == null)
            return cal;

        int toHour = Integer.parseInt((String)dataMap.get("toHour"));
        int toMin = Integer.parseInt((String)dataMap.get("toMin"));
        String toAmPm = (String)dataMap.get("toAmPm");

        /* set to Hour details */
        cal.set(Calendar.HOUR,toHour);
        cal.set(Calendar.MINUTE, toMin);
        if(toAmPm.equals("AM"))
            cal.set(Calendar.AM_PM,Calendar.AM);
        else
            cal.set(Calendar.AM_PM, Calendar.PM);

        cal.add(Calendar.MINUTE,mins);
        return cal;

    }
    public static int getEndTimeInMins(Context context,String prefName){
        int secs = getEndTimeInSecs(context,prefName);
        if(secs == 0)
            return secs;
        else
            return secs/60;
    }
    public static int getEndTimeInSecs(Context context,String prefName){
        QuietHandler quietHandler = new QuietHandler();
        int secs = 0;
        HashMap locMap = fillMapFormSharedPref(context,prefName);
        if(locMap == null)
            return secs;
        Calendar cal = Calendar.getInstance();
        long currentTime = cal.getTimeInMillis();
        long endTime = quietHandler.getToTimeInMillis(cal,locMap);
        secs = (int)(endTime-currentTime)/1000;
        return secs;
    }
    public static void copyDataFromPrefToIntent(Context context,Intent intent, String prefName){
        SharedPreferences locPref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        Map<String,?> prefHashMap = locPref.getAll();
        for(String key:prefHashMap.keySet()){
            Object locObj = prefHashMap.get(key);
            String[] dataType = (locObj.getClass().getName()).split("\\.");
            if(dataType[dataType.length-1].equals("String"))
                intent.putExtra(key, locObj.toString());
            else if(dataType[dataType.length-1].equals("Long"))
                intent.putExtra(key, (long) locObj);
            else if(dataType[dataType.length-1].equals("Int"))
                intent.putExtra(key, (int) locObj);
            else if(dataType[dataType.length-1].equals("Boolean"))
                intent.putExtra(key, (Boolean) locObj);
            else
                Toast.makeText(context, "key not available " + key + " data type = " + dataType[dataType.length - 1], Toast.LENGTH_SHORT).show();
        }
    }
}
