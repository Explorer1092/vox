package com.voxlearning.utopia.admin.util;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.PropertyUtilsBean;

import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Yuechen.Wang on 2016/10/11.
 *
 * @date 2017-02-03  10:32
 */
public class EntityUtils {
    /**
     * Used to access properties
     */
    private final PropertyUtilsBean propertyUtilsBean;

    private final static EntityUtils instance = new EntityUtils();

    public static EntityUtils getInstance() {
        return instance;
    }

    private EntityUtils() {
        this.propertyUtilsBean = new PropertyUtilsBean();
    }

    /**
     * 将　src　中同名属性复制到对象　dest　同名属性中
     */
    public <T> void parse(Map<String, String[]> src, T entity) {
        if (Objects.isNull(src) || Objects.isNull(entity)) {
            return;
        }
        for (Map.Entry<String, String[]> entry : src.entrySet()) {
            String name = entry.getKey();
            if (propertyUtilsBean.isWriteable(entity, name)) {
                try {
                    copyProperty(entity, name, entry.getValue());
                } catch (Exception ignored) {

                }
            }
        }
    }

    private void copyProperty(Object dest, String name, String[] value) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (null == value || 0 == value.length) {
            return;
        }
        PropertyDescriptor propertyDescriptor = propertyUtilsBean.getPropertyDescriptor(dest, name);
        Class<?> propertyType = propertyDescriptor.getPropertyType();
        if (propertyType == String.class) {
            propertyUtilsBean.setProperty(dest, name, value[0]);
        } else if (propertyType == Integer.class) {
            propertyUtilsBean.setProperty(dest, name, SafeConverter.toInt(value[0]));
        } else if (propertyType == Long.class) {
            propertyUtilsBean.setProperty(dest, name, SafeConverter.toLong(value[0]));
        } else if (propertyType == Double.class) {
            propertyUtilsBean.setProperty(dest, name, SafeConverter.toDouble(value[0]));
        } else if (propertyType == Boolean.class) {
            propertyUtilsBean.setProperty(dest, name, SafeConverter.toBoolean(value[0]));
        } else if (propertyType == Date.class || StringUtils.equalsIgnoreCase(propertyType.getTypeName(), "java.util.Date")) {
            propertyUtilsBean.setProperty(dest, name, SafeConverter.toDate(value[0]));
        } else if (propertyType == List.class) {
            Method writeMethod = propertyDescriptor.getWriteMethod();
            Type[] types = writeMethod.getGenericParameterTypes();
            if (null != types && 1 == types.length) {
                ParameterizedType parameterizedType = (ParameterizedType) types[0];
                String typeName = parameterizedType.getActualTypeArguments()[0].getTypeName();
                if (StringUtils.equals(String.class.getName(), typeName)) {
                    List<String> valueList = Stream.of(value).collect(Collectors.toList());
                    propertyUtilsBean.setProperty(dest, name, valueList);
                } else if (StringUtils.equals(Integer.class.getName(), typeName)) {
                    List<Integer> valueList = Stream.of(value).map(SafeConverter::toInt).collect(Collectors.toList());
                    propertyUtilsBean.setProperty(dest, name, valueList);
                } else if (StringUtils.equals(Long.class.getName(), typeName)) {
                    List<Long> valueList = Stream.of(value).map(SafeConverter::toLong).collect(Collectors.toList());
                    propertyUtilsBean.setProperty(dest, name, valueList);
                }
            }
        }
    }

    /**
     *
     * @param <T>
     * @param oldObject
     * @param newObject
     * @param ignoreFields
     * @return
     */
    public <T> String compareAndGetResult(String fieldPrefix, T oldObject, T newObject, String... ignoreFields) throws IllegalAccessException {
        if (oldObject == null && newObject == null)
            throw new IllegalArgumentException("old and new value can not be null at same time!");
        Class<?> tClass;
        if (oldObject == null)
            tClass = newObject.getClass();
        else
            tClass = oldObject.getClass();
        Field[] declaredFields = tClass.getDeclaredFields();
        List<String> ignoreFieldList = Arrays.asList(ignoreFields);
        StringBuilder stringBuilder = new StringBuilder();
        for (Field field : declaredFields) {
            String fieldName = field.getName();
            if (ignoreFieldList.contains(fieldName))
                continue;
            field.setAccessible(true);
            Object oldValue = oldObject == null ? "" : field.get(oldObject);
            Object newValue = newObject == null ? "" : field.get(newObject);
            boolean equals = judgeEquals(field.getType(), oldValue, newValue);

            if (!equals){
                if (StringUtils.isNotBlank(fieldPrefix))
                    fieldName = fieldPrefix + "." + fieldName;
                if (isBasicType(field)) {
                    stringBuilder.append("\n").append("字段：<").append(fieldName)
                            .append(">").append(",   原值：\"").append(oldValue).append("\"")
                            .append("    ---->    新值：\"").append(newValue).append("\"");
                }else {
                    stringBuilder.append(compareAndGetResult(fieldName, oldValue, newValue));
                }
            }
        }
        return stringBuilder.toString();
    }

    //String类型字段， Null 和 "" 按相同处理
    private static boolean judgeEquals(Type type, Object oldValue, Object newValue) {
        boolean equals = Objects.equals(oldValue, newValue);
        if (equals)
            return true;
        if (type.equals(String.class)){
            return SafeConverter.toString(oldValue, "").equals(SafeConverter.toString(newValue, ""));
        }else
            return false;
    }

    private static boolean isBasicType(Field field){
        if (field.getType().isPrimitive())
            return true;
        if (isWrapClass(field.getType()))
            return true;
        if (field.getType().equals(String.class))
            return true;
        if (field.getType().isEnum())
            return true;
        return false;
    }

    public static boolean isWrapClass(Class clz) {
        try {
            return ((Class) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) throws IllegalAccessException {
        System.out.println(judgeEquals(String.class, null, null));
    }

}
