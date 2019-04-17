package com.voxlearning.utopia.service.push.api.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangshichao on 16/8/25.
 */
public enum  PushTarget {

    TAG,
    BATCH,
    UNKNOWN;


    private static  final Map<String,PushTarget> map;
    static{
        map = new HashMap<>();
        for(PushTarget target : PushTarget.values()){
            if(!map.containsKey(target.name())){
                map.put(target.name(),target);
            }
        }
    }

    public static PushTarget of(String targetName){
        if(map.containsKey(targetName)){
            return map.get(targetName);
        }
        return UNKNOWN;
    }
}
