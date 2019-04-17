package com.voxlearning.utopia.service.newhomework.api.mapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/6
 */
public class HomeworkSource extends LinkedHashMap<String, Object> {

    private static final long serialVersionUID = 2253138173849906079L;
    public static HomeworkSource newInstance(Map<String, Object> map) {
        HomeworkSource inst = new HomeworkSource();
        if (map != null)
            inst.putAll(map);
        return inst;
    }
}
