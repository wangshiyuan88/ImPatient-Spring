package edu.wangshiyuan.impatient.util;

import java.util.Calendar;

public class Utils {
    public static String getCurrentDateString(){
        Calendar now = Calendar.getInstance();
        String month = convertTimeString(String.valueOf(now.get(Calendar.MONTH) + 1));
        String day =  convertTimeString(String.valueOf(now.get(Calendar.DAY_OF_MONTH)));
        return month+"-"+day+"-"+now.get(Calendar.YEAR);
    }
    
    public static String convertTimeString(String raw){
        String ret = Integer.parseInt(raw)>=10? raw : "0"+raw;
        return ret;
    }
}
