package com.superio.keepquite;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;

import com.example.superio.keepquite.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by kumar_thangaraj on 25/10/15.
 */
public class QuietHandler {
    /* Returns milliseconds for the input calendar */
    private long getTimeInMillis(int hour, int min, String amPm, Calendar cal){
        cal.set(Calendar.HOUR,hour);
        cal.set(Calendar.MINUTE,min);
        cal.set(Calendar.SECOND,0);
        if(amPm.equals("AM"))
            cal.set(Calendar.AM_PM,Calendar.AM);
        else
            cal.set(Calendar.AM_PM,Calendar.PM);
        return cal.getTimeInMillis();
    }

    /* Converts fromTime to milliseconds to return */
    public long getFromTimeInMillis(Calendar cal, HashMap map){
        int fromHour = Integer.parseInt((String) map.get("fromHour"));
        int fromMin = Integer.parseInt((String) map.get("fromMin"));
        String fromAmPm = (String)map.get("fromAmPm");
        return getTimeInMillis(fromHour, fromMin, fromAmPm, cal);
    }

    /* Converts toTime to milliseconds to return */
    public long getToTimeInMillis(Calendar cal, HashMap map){
        Calendar fromCal = cal.getInstance();
        int fromHour = Integer.parseInt((String)map.get("fromHour"));
        int fromMin = Integer.parseInt((String)map.get("fromMin"));
        String fromAmPm = (String)map.get("fromAmPm");
        long fromTime = getTimeInMillis(fromHour,fromMin,fromAmPm,fromCal);
        int toHour = Integer.parseInt((String) map.get("toHour"));
        int toMin = Integer.parseInt((String) map.get("toMin"));
        String toAmPm = ((String)map.get("toAmPm"));
        long toTime =  getTimeInMillis(toHour, toMin, toAmPm, cal);
        if(toTime < fromTime) {
            cal.add(Calendar.DATE, 1);
            toTime = getTimeInMillis(toHour, toMin, toAmPm, cal);
        }
        return toTime;
    }

    /* It removes all expired records from shared Preferences rec */
    public void removeKeepQuietRec(Context context,HashMap map){
        String prefName = (String)map.get("prefName");
        removeKeepQuitePrefName(context, prefName);
    }

    public void removeKeepQuitePrefName(Context context, String prefName){
        SharedPreferences locPref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        locPref.edit().clear().commit();
    }

    /* Last step function to set alarm to move the ringer mode to Silent */
    public void setAlarmOn(Context context, HashMap map, Intent intent){
        Calendar cal = Calendar.getInstance();
        long currentTime = cal.getTimeInMillis();
        cal.set(Calendar.SECOND, 0);
        if(getDayCountToSet(map) > 0)
            cal.add(Calendar.DATE,getDayCountToSet(map));
        Intent alarmIntent = createIntentForAlarm(context, map);
        alarmIntent.putExtra("quiteMode", "On");
        alarmIntent.putExtra("isActive", "No");
        String prefName = (String)map.get("prefName");
        SharedPreferences locPref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        long fromTime = getFromTimeInMillis(cal, map);
        long toTime = getToTimeInMillis(cal,map);
        if(currentTime < fromTime
                || currentTime < toTime
                || getDayCountToSet(map)>0) {
            setAlarm(context, alarmIntent, fromTime, prefName);

            locPref.edit().putString("quiteMode", "On").commit();
            locPref.edit().putString("isActive", "No").commit();
        }
        else {
            removeKeepQuietRec(context, map);
        }
        if(intent != null) {
            if(ListData.checkRunningQuiet(prefName) == null) {
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                setNotification(context, map,"End");
            }
        }
    }

    /* send notification */
    public void setNotification(Context context,HashMap map,String event){
        SharedPreferences pref = context.getSharedPreferences("KQ",Context.MODE_PRIVATE);
        String notify;
        if(event.equals("Start"))
            notify = pref.getString("notifyStart","No");
        else
            notify = pref.getString("notifyEnd","No");
        if(notify == null || !notify.equals("Yes"))
            return;
        NotificationCompat.Builder kqBuilder = new NotificationCompat.Builder(context);
        kqBuilder.setSmallIcon(R.drawable.keepquitesmall);
        kqBuilder.setContentTitle(context.getResources().getString(R.string.alertTitle));
        if(event.equals("Start"))
            kqBuilder.setContentText(context.getResources().getString(R.string.alertContentStart));
        else
            kqBuilder.setContentText(context.getResources().getString(R.string.alertContentEnd));
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        kqBuilder.setSound(soundUri);
        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(0, kqBuilder.build());
    }

    /* Inner most function to set alarm to execute it on frequently */
    public void setAlarm(Context context, Intent intent, long time,String prefName){
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        String[] prefNameArray = prefName.split("_");
        int identificationCode = Integer.parseInt(prefNameArray[1]);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, identificationCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    /* set alarm to quite off. Its called from quite setter */
    public void setAlarmOffForQuiteReceiver(Context context,Intent intent){
        setAlarmForQuiteReceiver(context, intent, "Off");
    }

    /* set alarm to quite On. Its called from quite setter */
    public void setAlarmOnForQuiteReceiver(Context context,Intent intent) {

        setAlarmForQuiteReceiver(context,intent,"On");
    }

    /*set alarm on/off based on the input from QuietReceiver */
    public void setAlarmForQuiteReceiver(Context context,Intent intent,String mode) {
        String prefName = intent.getStringExtra("prefName");
        HashMap map = ListData.fillMapFormSharedPref(context, prefName);

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (!checkPrefNameExist(context, prefName) || (map == null)) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            removeKeepQuietRec(context, map);
            return;
        }
        if (mode.equals("On"))
            setAlarmOn(context, map, intent);
        else if(mode.equals("Off"))
            setAlarmOff(context, map, intent);
        else{
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            removeKeepQuietRec(context, map);
        }

    }

    /* Last step function to set alarm to bring back the ringer mode to normal. */
    public void setAlarmOff(Context context, HashMap map, Intent intent){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND,0);
        Intent alarmIntent = createIntentForAlarm(context, map);
        long toTime = getToTimeInMillis(cal, map);
        String prefName = (String)map.get("prefName");
        alarmIntent.putExtra("quiteMode", "Off");
        alarmIntent.putExtra("isActive","Yes");
        setAlarm(context, alarmIntent, toTime, prefName);
        setNotification(context,map,"Start");
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        SharedPreferences locPref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        locPref.edit().putString("quiteMode","Off").commit();
        locPref.edit().putString("isActive","Yes").commit();
    }


    /* Intent to pass to pending intent to call alarm. It copy the data
        from Shared Preferences to Intent;
     */
    private Intent createIntentForAlarm(Context context,HashMap map){
        String prefName = (String)map.get("prefName");
        Intent alarmIntent = new Intent(context,KeepQuiteSetter.class);
        ListData.copyDataFromPrefToIntent(context, alarmIntent, prefName);
        return alarmIntent;
    }

    /* Retuns number of days to be increased in the calendar to set mode on */
    public int getDayCountToSet(HashMap map){
        String freqLabel = ((String)map.get("freqLabel"));
        if(freqLabel == null || freqLabel.equals(""))
            return 0;
        String[] dayArray = null;
        if(((String)map.get("freqLabel")).equals("Daily"))
            dayArray = "Y|Y|Y|Y|Y|Y|Y|".split("\\|");
        else
            dayArray = ((String)map.get("freqLabel")).split("\\|");
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK);
        int i = 0;
        while(i<7){
            if(day == 8)
                day = 1;
            if(dayArray[day-1].equals("Y")) {
                if(i==0){
                    Calendar fromCal = Calendar.getInstance();
                    Calendar toCal = Calendar.getInstance();
                    if((getFromTimeInMillis(fromCal,map) > cal.getTimeInMillis() ||
                            getToTimeInMillis(toCal,map) > cal.getTimeInMillis()))
                        break;
                }else break;
            }
            i++;
            day++;
        }
        return i;
    }

    /* Checks whether processing prefName is exist in the DB */
    public boolean checkPrefNameExist(Context context,String prefName){
        ListData.formListData(context);
        ArrayList list = ListData.listAdapter.getList();
        boolean exist = false;
        for(int i=0; i<list.size();i++){
            HashMap map = (HashMap)list.get(i);
            String locPrefName = (String)map.get("prefName");
            if(prefName.equals(locPrefName)) {
                exist = true;
                break;
            }
        }
        return exist;
    }
}
