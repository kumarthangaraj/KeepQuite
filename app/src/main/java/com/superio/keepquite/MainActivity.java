package com.superio.keepquite;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.superio.keepquite.R;
import com.superio.keepquite.KeepQuiteSetter;
import com.superio.keepquite.ListActivityFragment;
import com.superio.keepquite.ListData;
import com.superio.keepquite.MainActivityFragment;
import com.superio.keepquite.MultiTabAdapter;
import com.superio.keepquite.QuietHandler;
import com.superio.keepquite.QuiteFragment;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends FragmentActivity implements DialogInterface.OnClickListener{

    public String weekDaysSelected = new String("N|N|N|N|N|N|N|");
    public TimePicker timePicker;
    private String fromHour;
    private String fromMin;
    private String fromAmPm;
    private String toHour;
    private String toMin;
    private String toAmPm;
    MultiTabAdapter tabAdapter;
    ViewPager quiteViewPager;
    ActionBar actionBar;
    private String timePickerSource;
    QuietHandler quietHandler;
    int[] themeArray;

    public void setWeekDaysSelected(String value){
        weekDaysSelected = value;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        timePicker = new TimePicker(this);
        quietHandler = new QuietHandler();
        SharedPreferences pref = getSharedPreferences("KQ",MODE_PRIVATE);
        themeArray = new int[3];
        themeArray[0] = R.style.BlackTheme;
        themeArray[1] = R.style.WhiteTheme;
        themeArray[2] = R.style.PinkTheme;

        int theme = pref.getInt("theme",R.style.BlackTheme);

        setTheme(theme);

        super.setTheme(theme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // Check if no view has focus:
        quiteViewPager = (ViewPager)findViewById(R.id.pager);
        setupViewPager(quiteViewPager);
        ListData.setListAdapter(this);
        ListData.formListData(this);
        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.TabListener tabListener = new ActionBar.TabListener(){


            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                String tabName = (String)tab.getText();
                quiteViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };
        actionBar.addTab(actionBar.newTab().setText(getResources().getString(R.string.quick)).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(getResources().getString(R.string.schedule)).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(getResources().getString(R.string.existingTab)).setTabListener(tabListener));
    }

    private void setupViewPager(ViewPager viewPager){
        tabAdapter = new MultiTabAdapter(getSupportFragmentManager());

        tabAdapter.addFragment(new QuiteFragment(), getResources().getString(R.string.quick));
        tabAdapter.addFragment(new MainActivityFragment(), getResources().getString(R.string.schedule));
        tabAdapter.addFragment(new ListActivityFragment(), getResources().getString(R.string.existingTab));
        viewPager.setAdapter(tabAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                              @Override
                                              public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                                              }

                                              @Override
                                              public void onPageSelected(int position) {
                                                  actionBar.setSelectedNavigationItem(position);
                                              }

                                              @Override
                                              public void onPageScrollStateChanged(int state) {

                                              }
                                          }
        );
    }


    public void setPreDefinedTime(View view){
        Calendar cal = Calendar.getInstance();
        /* set from time */
        fromHour = cal.get(Calendar.HOUR)+"";
        fromMin = cal.get(Calendar.MINUTE)+"";
        int fromAmPmInt = cal.get(Calendar.AM_PM);
        fromAmPm =  (fromAmPmInt==0)?"AM":"PM";

        /* increment the time for the clicked button value */
        boolean extended = updateCalBasedOnButton(view, cal);

        /* set to time */
        toHour = cal.get(Calendar.HOUR)+"";
        toMin = cal.get(Calendar.MINUTE)+"";
        int toAmPmInt = cal.get(Calendar.AM_PM);
        toAmPm =  (toAmPmInt==0)?"AM":"PM";

        setTime(view, extended);
    }

    public void setPreRollTime(View view){
        Calendar cal = Calendar.getInstance();
        /* set from time */
        fromHour = cal.get(Calendar.HOUR)+"";
        fromMin = cal.get(Calendar.MINUTE)+"";
        int fromAmPmInt = cal.get(Calendar.AM_PM);
        fromAmPm =  (fromAmPmInt==0)?"AM":"PM";

        int value = ((NumberPicker)findViewById(R.id.quiteTimePicker)).getValue();
        int mins = 70+value*10;

        boolean extended = setEndTime(cal,mins);
        /* increment the cal set */

        /* set to time */
        toHour = cal.get(Calendar.HOUR)+"";
        toMin = cal.get(Calendar.MINUTE)+"";
        int toAmPmInt = cal.get(Calendar.AM_PM);
        toAmPm =  (toAmPmInt==0)?"AM":"PM";

        setTime(view,extended);
        TextView textView = (TextView)findViewById(R.id.setVal);
    }

    private void setTextViewMins(int mins, TextView textView){
        String text = "";
        if(mins/60 == 1)
            text = text+' '+getResources().getString(R.string.oneHr);
        else if(mins/60 >1)
            text = text+(mins/60)+" "+getResources().getString(R.string.hrs);

        if(mins%60 >= 0)
            text = text+(mins%60)+" "+getResources().getString(R.string.mins);
        textView.setText(text);
    }

    @Override
    public void onStart(){
        super.onStart();
        minsDisplayChanger();
    }

    public void minsDisplayChanger(){
        minsDisplayChanger("");
    }

    public void minsDisplayChanger(String prefNameIn){
        String prefName = ListData.checkRunningQuiet(prefNameIn);
        if(prefName == null || prefName.equals(""))
            prefName = prefNameIn;
        TextView setVal = (TextView)findViewById(R.id.setVal);
        final int mins = ListData.getEndTimeInMins(this, prefName);
        if(setVal != null)
            setTextViewMins(mins, setVal);
    }


    public boolean updateCalBasedOnButton(View view,Calendar cal){
        String ButtonClicked = getResources().getResourceName(view.getId());
        String[] min = ButtonClicked.split("_");
        boolean extended = setEndTime(cal,Integer.parseInt(min[1]));
        //setImageContent(findViewById(R.id.icon),min[1]+" Mins");
        TextView textView = (TextView)findViewById(R.id.setVal);
        setTextViewMins(Integer.parseInt(min[1]), textView);
        return extended;
    }
    public boolean setEndTime(Calendar cal, int mins){
        String prefName = "";
        prefName = ListData.checkRunningQuietToExtend(prefName);
        if(prefName != null && !prefName.equals("")) {
            SharedPreferences locPref  = this.getSharedPreferences(prefName,MODE_PRIVATE);
            fromHour = locPref.getString("fromHour","");
            fromMin = locPref.getString("fromMin","");
            fromAmPm = locPref.getString("fromAmPm","");
            ListData.incRunningQuite(getApplicationContext(), prefName, cal, mins);
            removeKeepQuitePrefName(prefName, false);
            return true;
        }
        else {
            cal.add(Calendar.MINUTE,mins);
            return false;
        }
    }

    public void removeKeepQuitePrefName(String prefName, boolean activate){
        SharedPreferences locPref = getSharedPreferences(prefName, Context.MODE_PRIVATE);
        Intent removeIntent = new Intent(this,KeepQuiteSetter.class);
        Map<String,?> prefHashMap = locPref.getAll();
        for(String key:prefHashMap.keySet()){
            Object locObj = prefHashMap.get(key);
            String[] dataType = (locObj.getClass().getName()).split("\\.");
            if(dataType[dataType.length-1].equals("String"))
                removeIntent.putExtra(key, locObj.toString());
            else if(dataType[dataType.length-1].equals("long"))
                removeIntent.putExtra(key, (long) locObj);
            else if(dataType[dataType.length-1].equals("int"))
                removeIntent.putExtra(key, (int) locObj);
            else if(dataType[dataType.length-1].equals("Boolean"))
                removeIntent.putExtra(key, (Boolean) locObj);
        }
        if(activate && (locPref.getString("isActive","No")).equals("Yes")){
            if(ListData.checkRunningQuiet(prefName) == null) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        }
        PendingIntent rmPendingIntent = PendingIntent.getBroadcast(this,0,removeIntent,0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(rmPendingIntent);
        locPref.edit().clear().commit();
    }

    public void setTime(View view) {
        setTime(view, false);
    }

    public void setTime(View view, boolean extended){
        if(!isValidInputParams(this,fromHour,fromMin,fromAmPm,"From"))
            return;
        if(!isValidInputParams(this,toHour,toMin,toAmPm,"To"))
            return;
        String prefName = getPrefernceName();
        setValuesIntoPref(prefName);
        HashMap dataMap = ListData.fillMapFormSharedPref(this,prefName);
        quietHandler.setAlarmOn(this, dataMap, null);
        ListData.formListData(this);
        ListData.listAdapter.notifyDataSetChanged();
        if(!extended)
            Toast.makeText(this, getResources().getString(R.string.quiteSet), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this,getResources().getString(R.string.quiteExtended),Toast.LENGTH_LONG).show();
        clearFieldsAlone(view);
        minsDisplayChanger(prefName);
    }

    private void setValuesIntoPref(String prefName){
        SharedPreferences pref = this.getSharedPreferences(prefName,MODE_PRIVATE);
        pref.edit().putString("prefName", prefName).commit();
        pref.edit().putBoolean("weekly", ((CheckBox) findViewById(R.id.weekly)).isChecked()).commit();
        pref.edit().putBoolean("daily", ((CheckBox) findViewById(R.id.daily)).isChecked()).commit();
        pref.edit().putString("selectedDays", weekDaysSelected).commit();
        pref.edit().putString("fromHour", fromHour).commit();
        pref.edit().putString("fromMin", fromMin).commit();
        pref.edit().putString("fromAmPm", fromAmPm).commit();
        pref.edit().putString("toHour", toHour).commit();
        pref.edit().putString("toMin", toMin).commit();
        pref.edit().putString("toAmPm", toAmPm).commit();
        pref.edit().putString("quiteMode", "On").commit();
        pref.edit().putString("isActive", "Off").commit();
        pref.edit().commit();
    }
    @Override
    public void onResume(){
        super.onResume();
    }

    private String getPrefernceName(){
        SharedPreferences locPref = getSharedPreferences("KQ", MODE_PRIVATE);
        int maxPref = locPref.getInt("MaxPref", 0);
        int i= 1;
        for(i=1; i<=maxPref; i++){
            SharedPreferences locPrefTest = getSharedPreferences("KQ_"+i, MODE_PRIVATE);
            String fromHour = locPrefTest.getString("fromHour","");
            if(fromHour.equals(""))
                break;
        }
        if(i<=maxPref)
            return "KQ_"+i;
        else
            locPref.edit().putInt("MaxPref",maxPref+1).commit();
        return "KQ_"+(maxPref+1);
    }

    private boolean isValidInputParams(Context context,String hour,String min, String amPm, String caller){
        if(hour == null || hour.equals("") || min == null || min.equals("") || amPm == null || amPm.equals("")) {
            Toast.makeText(context, caller + " "+getResources().getString(R.string.dtlError), Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            int intHour = Integer.parseInt(hour);
            int intMin = Integer.parseInt(min);
            if(intHour >12 || intHour <0){
                Toast.makeText(context,getResources().getString(R.string.invalid)+ " "+caller+" "
                        +getResources().getString(R.string.hrs), Toast.LENGTH_SHORT).show();
                return false;
            }
            if(intMin >59 || intMin <0) {
                Toast.makeText(context, getResources().getString(R.string.invalid) + " " + caller + " "
                        + getResources().getString(R.string.mins), Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
        catch(NumberFormatException e){
            Toast.makeText(context, caller + getResources().getString(R.string.invalidNumber), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /* handling onclick event of daily check box */
    public void handleDaily(View view){
        if(!((CheckBox)findViewById(R.id.daily)).isChecked())
            return;

        ((CheckBox)findViewById(R.id.weekly)).setChecked(false);
        weekDaysSelected = "N|N|N|N|N|N|N|";
    }

    public int getThemeName(){
        SharedPreferences pref = getSharedPreferences("KQ",MODE_PRIVATE);
        return pref.getInt("theme",R.style.BlackTheme);
    }

    /* showing week days to select */
    public void showWeekDays(View view){
        //if(!((CheckBox)findViewById(R.id.weekly)).isChecked())
          //  return;
        AlertDialog.Builder alertDialog;
        if(getThemeName() == R.style.BlackTheme)
            alertDialog = new AlertDialog.Builder(this);
        else
            alertDialog = new AlertDialog.Builder(this,R.style.customWhiteDialog);

        alertDialog.setTitle(this.getString(R.string.WeekDaysTitle));
        final boolean[] weekDaysSelectedBoolean = new boolean[7];
        String[] weekDaysSelectedArray = weekDaysSelected.split("\\|");
        for(int i=0; i<7; i++){
            if(weekDaysSelectedArray[i].equals("Y"))
                weekDaysSelectedBoolean[i] = true;
            else
                weekDaysSelectedBoolean[i] = false;
        }
        CharSequence[] weekDays = (CharSequence[])this.getResources().getStringArray(R.array.weekDays);
        alertDialog.setMultiChoiceItems(weekDays, weekDaysSelectedBoolean,new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which,boolean isChecked) {
                String[] weekDaysSelectedArray = weekDaysSelected.split("\\|");
                if(isChecked)
                    weekDaysSelectedBoolean[which] = true;
                else
                    weekDaysSelectedBoolean[which] = false;
            }
        })
                .setPositiveButton((CharSequence) this.getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int button) {
                                weekDaysSelected = "";
                                for (int i = 0; i < 7; i++) {
                                    if (weekDaysSelectedBoolean[i])
                                        weekDaysSelected = weekDaysSelected + "Y|";
                                    else
                                        weekDaysSelected = weekDaysSelected + "N|";
                                }
                                if (weekDaysSelected.equals("N|N|N|N|N|N|N|"))
                                    ((CheckBox) findViewById(R.id.weekly)).setChecked(false);
                                else {
                                    ((CheckBox) findViewById(R.id.weekly)).setChecked(true);
                                    ((CheckBox) findViewById(R.id.daily)).setChecked(false);
                                }
                            }
                        })
                .setNegativeButton((CharSequence) this.getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int button) {
                                if (weekDaysSelected.equals("N|N|N|N|N|N|N|"))
                                    ((CheckBox) findViewById(R.id.weekly)).setChecked(false);
                                else {
                                    ((CheckBox) findViewById(R.id.weekly)).setChecked(true);
                                    ((CheckBox) findViewById(R.id.daily)).setChecked(false);
                                }
                            }
                        });
        alertDialog.show();


    }

    /*Clearing fields */
    public void clearFieldsAlone(View view) {


        CheckBox daily = (CheckBox) findViewById(R.id.daily);
        CheckBox weekly = (CheckBox) findViewById(R.id.weekly);

        ((TextView)findViewById(R.id.fromTimeTextView)).setText("");
        ((TextView)findViewById(R.id.toTimeTextView)).setText("");


        /*Set checkBox to default value */
        daily.setChecked(false);
        weekDaysSelected = "N|N|N|N|N|N|N|";
        weekly.setChecked(false);
        fromHour = fromMin = fromAmPm = "";
        toHour = toMin = toAmPm = "";

    }

    /*clearing fields */
    public void clearFields(View view){

        CheckBox daily = (CheckBox)findViewById(R.id.daily);
        CheckBox weekly = (CheckBox)findViewById(R.id.weekly);

        fromHour = "";
        fromMin = "";
        toHour = "";
        toMin = "";

        /*Set checkBox to default value */
        daily.setChecked(false);
        if(weekly.isChecked())
            weekDaysSelected = "N|N|N|N|N|N|N|";
        weekly.setChecked(false);

        /* setting focus to first field in the page
        /*fromHour.requestFocus();*/
        SharedPreferences locPref = getSharedPreferences("KQ",MODE_PRIVATE);
        int maxPref = locPref.getInt("MaxPref",0);
        for(int i=0; i<maxPref; i++){
            SharedPreferences locPref1 = getSharedPreferences("KQ_"+(i+1),MODE_PRIVATE);
            locPref1.edit().remove("KQ_"+(i+1)).commit();
        }
        int theme = locPref.getInt("theme",0);
        String notifyStart = locPref.getString("notifyStart", "No");
        String notifyEnd = locPref.getString("notifyEnd","No");
        locPref.edit().clear().commit();
        locPref.edit().putInt("MaxPref", 0).commit();
        if(theme != 0)
            locPref.edit().putInt("theme",theme).commit();
        locPref.edit().putString("notifyStart", notifyStart).commit();
        locPref.edit().putString("notifyEnd",notifyEnd).commit();
        ListData.formListData(this);
        ListData.listAdapter.notifyDataSetChanged();
        AudioManager audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onClick(DialogInterface dialog, int which){
        String buttonText = (String)((AlertDialog)dialog).getButton(which).getText();

        if(getResources().getString(R.string.ok).equals(buttonText)){
            String hourS = "";
            String minS = "";
            String amPmS = "";
            int hour = timePicker.getCurrentHour();
            int min = timePicker.getCurrentMinute();

            if(hour >11) {
                hourS = (hour-12)+"";
                if(hourS.length()==1)
                    hourS = "0"+hourS;
                amPmS = "PM";
            }
            else{
                hourS = hour+"";
                amPmS = "AM";
            }
            minS = min+"";
            if(minS.length()==1)
                minS = "0"+minS;

            if(timePickerSource.equals("From")) {
                fromHour = hourS;
                fromMin = minS;
                fromAmPm = amPmS;
                String fromTime = fromHour+" : "+fromMin+" "+fromAmPm;
                ((TextView)findViewById(R.id.fromTimeTextView)).setText(fromTime);
            }
            else if(timePickerSource.equals("To")){
                toHour = hourS;
                toMin = minS;
                toAmPm = amPmS;
                String toTime = toHour+" : "+toMin+" "+toAmPm;
                ((TextView)findViewById(R.id.toTimeTextView)).setText(toTime);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showSettingsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setExistingDetails(View dialogView){
        SharedPreferences pref = getSharedPreferences("KQ",MODE_PRIVATE);
        String notifyStart = pref.getString("notifyStart", "No");
        String notifyEnd = pref.getString("notifyEnd","No");
        Spinner themeCode = (Spinner)dialogView.findViewById(R.id.themeCode);
        int position = 0;
        for(position = 0; position<themeArray.length; position++) {
            if (themeArray[position] == getThemeName())
                break;
        }
        if(position == themeArray.length)
            position = 0;
        themeCode.setSelection(position);
        if(notifyStart.equals("Yes"))
            ((CheckBox)dialogView.findViewById(R.id.notifyStart)).setChecked(true);
        else
            ((CheckBox)dialogView.findViewById(R.id.notifyStart)).setChecked(false);
        if(notifyEnd.equals("Yes"))
            ((CheckBox)dialogView.findViewById(R.id.notifyEnd)).setChecked(true);
        else
            ((CheckBox)dialogView.findViewById(R.id.notifyEnd)).setChecked(false);
    }

    private void showSettingsDialog() {
        AlertDialog.Builder alertDialog;
        if (getThemeName() == R.style.BlackTheme){
            alertDialog = new AlertDialog.Builder(this);
        }

        else {
            alertDialog = new AlertDialog.Builder(this, R.style.customWhiteDialog);
        }
        alertDialog.setTitle(this.getString(R.string.settings));
        final View dialogView = getLayoutInflater().inflate(R.layout.fragment_settings_dialog,null,true);
        setExistingDetails(dialogView);
        alertDialog.setView(dialogView);
        alertDialog.setNegativeButton((CharSequence) this.getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        alertDialog.setPositiveButton((CharSequence) this.getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Spinner displayTheme = (Spinner) ((AlertDialog) dialog).findViewById(R.id.themeCode);
                        int position = (int) displayTheme.getSelectedItemPosition();
                        setNotify(dialogView);
                        //int position = (int) ((Spinner)dialog.findViewById(R.id.themeCode)).getSelectedItem();
                        if (getThemeName() == themeArray[position])
                            return;
                        else
                            setThemeName(themeArray[position]);
                    }
                });
        alertDialog.show();
    }

    private void setNotify(View dialogView){
        CheckBox notifyStartCheck = (CheckBox)dialogView.findViewById(R.id.notifyStart);
        CheckBox notifyEndCheck = (CheckBox)dialogView.findViewById(R.id.notifyEnd);
        SharedPreferences pref = getSharedPreferences("KQ",MODE_PRIVATE);
        String notifyStart = "";
        String notifyEnd = "";
        if(notifyStartCheck.isChecked())
            notifyStart="Yes";
        else
            notifyStart="No";
        if(notifyEndCheck.isChecked())
            notifyEnd="Yes";
        else
            notifyEnd="No";
        pref.edit().putString("notifyStart", notifyStart).commit();
        pref.edit().putString("notifyEnd", notifyEnd).commit();
    }

    public void setThemeName(int themeId){
        SharedPreferences pref = getSharedPreferences("KQ",MODE_PRIVATE);
        pref.edit().putInt("theme", themeId).commit();
        this.recreate();
    }

    public void showTimePicker(View view){
        TextView textView = (TextView)view;
        String value = (String)textView.getText();
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        timePicker = new TimePicker(this);

        dialog.setView(timePicker);
        if(textView.getId() == R.id.fromTimeTextView) {
            dialog.setTitle(getResources().getString(R.string.startTime));
            timePickerSource = "From";
        }
        else if(textView.getId() == R.id.toTimeTextView) {
            dialog.setTitle(getResources().getString(R.string.endTime));
            timePickerSource = "To";
        }
        String[] valueSplit1 = value.split(" ");
        if(valueSplit1.length>2) {
            if (valueSplit1[3].equals("AM") || valueSplit1[3].equals("PM")) {

                if (valueSplit1[1].equals("PM"))
                    timePicker.setCurrentHour(Integer.parseInt(valueSplit1[0]) + 12);
                else
                    timePicker.setCurrentHour(Integer.parseInt(valueSplit1[0]));
                timePicker.setCurrentMinute(Integer.parseInt(valueSplit1[2]));
            }
        }

        dialog.setPositiveButton(getResources().getString(R.string.ok), this);
        dialog.setNegativeButton(getResources().getString(R.string.cancel), this);
        dialog.show();
    }

    public void setScheduleDtls(String fromHour, String fromMin, String fromAmPm,
                                String toHour, String toMin, String toAmPm){
        this.fromHour = fromHour;
        this.fromMin = fromMin;
        this.fromAmPm = fromAmPm;
        this.toHour = toHour;
        this.toMin = toMin;
        this.toAmPm = toAmPm;
    }
}
