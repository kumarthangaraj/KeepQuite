package com.superio.keepquite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

/**
 * Created by kumar_thangaraj on 02/10/15.
 */
public class KeepQuiteSetter extends BroadcastReceiver {
    QuietHandler quiteHandler;
    public void onReceive(Context context, Intent intent){

        if(ListData.listAdapter == null)
            ListData.setListAdapter(null);

        quiteHandler = new QuietHandler();
        ListData.formListData(context);

        String mode = intent.getStringExtra("quiteMode");

        String prefName = intent.getStringExtra("prefName");
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(mode.equals("On")) {
            if(quiteHandler.checkPrefNameExist(context, prefName)) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                quiteHandler.setAlarmOffForQuiteReceiver(context, intent);
            }
            else
                quiteHandler.removeKeepQuitePrefName(context,prefName);
        }
        else if(mode.equals("Off")) {
            quiteHandler.setAlarmOnForQuiteReceiver(context, intent);
        }
        ListData.formListData(context);
        ListData.listAdapter.notifyDataSetChanged();
    }
}

