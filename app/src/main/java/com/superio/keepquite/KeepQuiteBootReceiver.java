package com.superio.keepquite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by kumar_thangaraj on 22/10/15.
 */
public class KeepQuiteBootReceiver extends BroadcastReceiver {
    QuietHandler quietHandler;
    @Override
    public void onReceive(Context context, Intent intent) {
        ListData.setListAdapter(null);
        ListData.formListData(context);
        quietHandler = new QuietHandler();
        restoreAllQuietsAgain(context, intent);
    }

    /* Removes all expited Quiets when the mobile was switched off
    * Set All valid quiets back to Alarm Manager*/
    private void restoreAllQuietsAgain(Context context,Intent intent){
        ArrayList list = ListData.listAdapter.getList();
        if(list.size()==0)
            return;
        for(int i=0; i<list.size(); i++){
            HashMap map = (HashMap)list.get(i);
            String stateOfQuiet = getStateOfQuiet(map);
            if(stateOfQuiet.equals("Expired")) {
                quietHandler.removeKeepQuietRec(context, map);
            }
            if(stateOfQuiet.equals("Active")){
                setActiveQuietOnce(context, map,intent);
            }
            if(stateOfQuiet.equals("Frequent")){
                setActiveQuietFrequent(context,map,intent);
            }
        }
    }

    /* It sets back frequency based quitest based on its time component */
    private void setActiveQuietFrequent(Context context, HashMap map, Intent intent){
        String quiteMode = (String)map.get("quiteMode");
        if(quiteMode.equals("On")){
            setAlarmToQuiteOn(context,map,intent);
        }else
            setAlarmToQuiteOff(context, map, intent);
    }

    /* It sets back one time quiets based on current time component */
    private void setActiveQuietOnce(Context context,HashMap map, Intent intent){
        if(setAlarmToQuiteOn(context,map,intent)) return;

        quietHandler.removeKeepQuietRec(context,map);
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

    }

    /* Sets alarm to quite on its similar to activity sets alarm once user selected the details */
    private boolean setAlarmToQuiteOn(Context context, HashMap map, Intent intent){
        if(isElegibleToSetOnToday(map) || isElegibleToSetOnFrequent(map)) {
            quietHandler.setAlarmOn(context, map, intent);
            return true;
        }
        if(isElegibleToSetOffToday(map)) {
            quietHandler.setAlarmOff(context, map, intent);
            return true;
        }
        return false;
    }

    /* sets alarm to quite off after the time duration. */
    private boolean setAlarmToQuiteOff(Context context, HashMap map, Intent intent){
        if(isElegibleToSetOffToday(map))
            quietHandler.setAlarmOff(context,map,intent);
        else if(isElegibleToSetOnFrequent(map))
            quietHandler.setAlarmOn(context,map,intent);
        else return false;

        return true;
    }

    /* It checks whether the queits frequent based */
    private boolean isElegibleToSetOnFrequent(HashMap map){
        String freqFlag = (String)map.get("freqLabel");
        if(freqFlag == null || freqFlag.equals(""))
            return false;
        if(freqFlag.equals("Daily")) {
            if (isElegibleToSetOnToday(map)) return true;
            else if (isElegibleToSetOffToday(map)) return false;
            else return true;
        }
        else {
            int dayCount = quietHandler.getDayCountToSet(map);
            if(dayCount == 0){
                if (isElegibleToSetOnToday(map)) return true;
                else return false;
            }else {
                return true;

            }
        }
    }

    /* Checks whetehr current time is eligible to set the alarm in off mode */
    private boolean isElegibleToSetOffToday(HashMap map){
        Calendar toCal = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND,0);
        if(quietHandler.getToTimeInMillis(toCal,map)>cal.getTimeInMillis())
            return true;
        else
            return false;
    }

    /* Checks whetehr current time is eligible to set the alarm in off mode today */
    private boolean isElegibleToSetOnToday(HashMap map){
        Calendar fromCal = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND,0);
        if(quietHandler.getFromTimeInMillis(fromCal, map) > cal.getTimeInMillis())
            return true;
        else
            return false;
    }

    /* Get status of all quiets available in shared preferences. */
    private String getStateOfQuiet(HashMap map){
        String status = "Active";
        String freqLabel = (String)map.get("freqLabel");
        if(!freqLabel.equals(""))
            return status="Frequent";

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND,0);

        Calendar toCal = Calendar.getInstance();

        long toTime = quietHandler.getToTimeInMillis(toCal,map);

        if(toTime <= cal.getTimeInMillis()) {
            return status = "Expired";
        }
        return status;
    }
}
