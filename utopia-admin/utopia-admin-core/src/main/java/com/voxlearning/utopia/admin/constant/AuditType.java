package com.voxlearning.utopia.admin.constant;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by shuai.huan on 2014/5/4.
 */
public class AuditType {
    public static final Integer add = 0;
    public static final Integer del = 1;
    public static final Integer edit = 2;
    public static final Map<String, String> AuditTypeMap;

    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(String.valueOf(add), "增加知识点");
        map.put(String.valueOf(del), "删除知识点");
        map.put(String.valueOf(edit), "编辑知识点");
        AuditTypeMap = Collections.unmodifiableMap(map);
    }
}
