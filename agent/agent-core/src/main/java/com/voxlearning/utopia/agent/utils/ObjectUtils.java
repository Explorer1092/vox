package com.voxlearning.utopia.agent.utils;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.entity.crm.CrmSchoolClue;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 *  关于对象操作的一些工具
 * Created by yaguang,wang on 2016/10/11.
 */
public class ObjectUtils {
    // 对象克隆
    public static Object deepClone(Object cloneObject) throws IOException, ClassNotFoundException {
        //将对象写到流里
        ByteArrayOutputStream bo=new ByteArrayOutputStream();
        ObjectOutputStream oo=new ObjectOutputStream(bo);
        oo.writeObject(cloneObject);
        //从流里读出来
        ByteArrayInputStream bi=new ByteArrayInputStream(bo.toByteArray());
        ObjectInputStream oi=new ObjectInputStream(bi);
        return(oi.readObject());
    }

    public static Object twoObjectCopyField(Object copyObj, Object goalObj, List<String> copyFiled, List<String> goalField) {
        if (CollectionUtils.isEmpty(copyFiled) || CollectionUtils.isEmpty(goalField)) {
            return goalObj;
        }
        if (copyFiled.size() != goalField.size()) {
            return goalObj;
        }
        for (int i = 0; i < copyFiled.size(); i++) {
            try {
                PropertyDescriptor copyPd = new PropertyDescriptor(copyFiled.get(i), copyObj.getClass());
                Method getMethod = copyPd.getReadMethod();//获得get方法
                Object value = getMethod.invoke(copyObj);//执行get方法返回一个Object
                PropertyDescriptor goalPd = new PropertyDescriptor(goalField.get(i), goalObj.getClass());
                Method setMethod = goalPd.getWriteMethod();
                setMethod.invoke(goalObj, value);
            } catch (IntrospectionException | IllegalAccessException | InvocationTargetException ignored) {
            }
        }
        return goalObj;
    }

}
