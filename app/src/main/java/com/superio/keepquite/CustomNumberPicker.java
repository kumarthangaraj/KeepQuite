package com.superio.keepquite;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.NumberPicker;

/**
 * Created by kumar_thangaraj on 18/10/15.
 */
public class CustomNumberPicker extends NumberPicker {

    public CustomNumberPicker(Context context){
        super(context);
    }
    public CustomNumberPicker(Context context,AttributeSet attrs){
        super(context,attrs);
        processAttributesSet(attrs);
    }
    public CustomNumberPicker(Context context,AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);
        processAttributesSet(attrs);
    }
    private void processAttributesSet(AttributeSet attrs){
        int min = attrs.getAttributeIntValue(null,"min",10);
        int max = attrs.getAttributeIntValue(null,"max",60);
        int count = (max-min)/10+1;
        String[] values = new String[count];
        for(int i=0; i<count; i++){
            if(i==0) {
                values[0] = "";
                if ((min / 60) == 1)
                    values[0] = min / 60 + " hr ";
                else if(min/60 >1)
                    values[0] = min / 60 + " hrs ";
                if(min%60 > 0)
                    values[0] = values[0] + " " + min % 60 + " Mins";
            }else {
                int value = min+(i*10);
                values[i] = "";
                if ((value / 60)  == 1)
                    values[i] = value / 60 + " hr ";
                else if(value/60 > 1)
                    values[i] = value / 60 + " hrs ";
                if(value%60 !=0)
                    values[i] = values[i] + " " + value % 60 + " Mins";
            }
        }
        this.setDisplayedValues(values);
        this.setMinValue(0);
        this.setMaxValue(values.length - 1);
        this.setWrapSelectorWheel(true);
    }
}
