package com.cuisec.mshield.utils;


import android.app.Activity;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by licz on 16/6/14.
 */
public class ActivityManager {

    private static ActivityManager instance = null;
    private HashMap<String, Activity> activityMap = null;

    private ActivityManager() {

        activityMap = new HashMap<String, Activity>();
    }

    //单例模式
    public static synchronized ActivityManager getInstance() {

        if (instance == null) {
            instance = new ActivityManager();
        }
        return instance;
    }

    //将activity添加到管理器。
    public void putActivity(String activityName, Activity activity) {

        activityMap.put(activityName, activity);
    }

    //获取activity
    public Activity getActivity(String activityName) {

        return activityMap.get(activityName);
    }

    //管理器的Activity是否为空
    public boolean isEmpty() {

        return activityMap.isEmpty();
    }

    //管理器中Activity对象的个数
    public int size() {

        return activityMap.size();
    }

    //管理器中是否包含指定的名字
    public boolean containsName(String activityName) {

        return activityMap.containsKey(activityName);
    }

    //管理器中是否包含指定的Activity
    public boolean containsActivity(Activity activity) {

        return activityMap.containsValue(activity);
    }

    //关闭指定的activity
    public void closeActivity(String activityName) {

        Activity activity = activityMap.remove(activityName);
        if (activity != null) {
            activity.finish();
            activity = null;
        }
    }

    //关闭所有活动的Activity除了指定的一个之外
    public void closeAllActivityExceptOne(String activityName) {

        Set<String> names = activityMap.keySet();
        Activity activityEx = activityMap.get(activityName);
        for (String name : names) {
            if (!name.equals(activityName)) {

                Activity activity = activityMap.get(name);
                if (activity != null) {

                    activity.finish();
                    activity = null;
                }
            }
        }
        activityMap.clear();
        activityMap.put(activityName, activityEx);
    }

    //关闭所有的activity
    public void closeAllActivity() {

        Set<String> names = activityMap.keySet();
        for (String name : names) {

            Activity activity = activityMap.get(name);
            if (activity != null) {

                activity.finish();
                activity = null;
            }
        }

        activityMap.clear();
    }
}
