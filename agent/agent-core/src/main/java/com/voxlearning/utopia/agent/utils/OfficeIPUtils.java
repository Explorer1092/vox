package com.voxlearning.utopia.agent.utils;

import com.voxlearning.alps.core.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * OfficeIPUtils
 *
 * @author song.wang
 * @date 2016/8/22
 */
public class OfficeIPUtils {

    private static final Set<String> OFFICE_IP_LIST;

    static {
        OFFICE_IP_LIST = new HashSet<>();
        OFFICE_IP_LIST.add("43.227.252.50");
        OFFICE_IP_LIST.add("36.110.113.66");
        OFFICE_IP_LIST.add("222.35.191.32");
        OFFICE_IP_LIST.add("120.131.75.234");
        OFFICE_IP_LIST.add("1.119.6.30");
        OFFICE_IP_LIST.add("0.0.0.0");
        OFFICE_IP_LIST.add("127.0.0.1");
        OFFICE_IP_LIST.add("123.126.115.162");

    }

    public static boolean isOfficeIp(String ip){
        if(StringUtils.isBlank(ip)){
            return false;
        }
        if(OFFICE_IP_LIST.contains(ip) || ip.startsWith("192.168.") || ip.startsWith("10.")){
            return true;
        }
        return false;
    }
}
