package com.voxlearning.utopia.admin.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * BeanMapUtils
 *
 * @author song.wang
 * @date 2016/9/7
 */
public class BeanMapUtils {

    public static Map<String, Object> tansBean2Map(Object obj){
        if(obj == null){
            return null;
        }
        Map<String, Object> retMap = new HashMap<>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for(PropertyDescriptor property : propertyDescriptors){
                String key = property.getName();
                if(!key.equals("class")){
                    Method getter = property.getReadMethod();
                    Object value = getter.invoke(obj);
                    retMap.put(key, value);
                }
            }
        } catch (Exception e) {
            retMap.clear();
        }
        return retMap;
    }
}
