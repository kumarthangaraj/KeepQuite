package com.superio.keepquite;

import android.app.Activity;
import android.graphics.Color;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.superio.keepquite.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kumar_thangaraj on 04/10/15.
 */
public class QuiteListAdapter extends BaseAdapter {
    private ArrayList<HashMap<String,String>> list;
    Activity activity;
    TextView timeFrame;
    TextView freqLabel;

    public QuiteListAdapter(Activity activity){
        super();
        this.activity = activity;
        this.list = new ArrayList<HashMap<String,String>>();;
    }

    public void clear(){
        this.list.clear();
    }

    public void add(HashMap<String,String> rec){
        this.list.add(rec);
    }

    public ArrayList getList(){
        return list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();

        convertView = inflater.inflate(R.layout.keep_quite_list,null);
        timeFrame = (TextView)convertView.findViewById(R.id.timeFrame);
        freqLabel = (TextView)convertView.findViewById(R.id.freqLabel);

        HashMap<String, String> map = list.get(position);
        setTimeFrame(timeFrame,map);
        setFreqLabel(freqLabel,map);
        return convertView;
    }
    private void setTimeFrame(TextView timeFrame, HashMap<String, String> map){
        String fromHour = map.get("fromHour");
        String fromMin = map.get("fromMin");
        String fromAmPm = map.get("fromAmPm");

        String toHour = map.get("toHour");
        String toMin = map.get("toMin");
        String toAmPm = map.get("toAmPm");

        String timeFrameText = fromHour+":"+fromMin+" "+fromAmPm+"       "+toHour+":"+toMin+" "+toAmPm;

        SpannableString ss = new SpannableString(timeFrameText);
        ss.setSpan(new RelativeSizeSpan(2f),0,5,0);
        ss.setSpan(new RelativeSizeSpan(2f),15,20,0);
        if(map.get("quiteMode").equals("Off")) {
            if(((MainActivity)activity).getThemeName() == R.style.BlackTheme || ((MainActivity)activity).getThemeName() == 0)
                ss.setSpan(new ForegroundColorSpan(Color.CYAN), 0, 23, 0);
            else
                ss.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 23, 0);
        }

        timeFrame.setText(ss);
    }

    private void setFreqLabel(TextView freqLabel, HashMap<String, String> map){
        String freqLabelText = "";
        String[] days = activity.getResources().getStringArray(R.array.shortWeekDays);
        boolean stringStarted = false;

        if(map.get("freqLabel").equals("")){
            freqLabelText = "Once";
        }else if(map.get("freqLabel").equals("Daily")){
            freqLabelText = "Daily";
        }else {
            String[] weekly = map.get("freqLabel").split("\\|");
            for(int i=0; i<7; i++) {
                if (weekly[i].equals("Y")) {
                    if (!stringStarted)
                        stringStarted = true;
                    else
                        freqLabelText = freqLabelText + ",";
                    freqLabelText = freqLabelText + days[i];
                }
            }
        }
        freqLabelText = "<font size="+12+">"+freqLabelText+"</font>";
        freqLabel.setText(Html.fromHtml(freqLabelText));
    }
}

