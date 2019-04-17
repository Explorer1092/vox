package com.voxlearning.utopia.mizar.utils;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.DynaBean;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.PropertyUtilsBean;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.voxlearning.alps.repackaged.org.apache.commons.beanutils.PropertyUtils.*;

/**
 * Created by xiang.lv on 2016/10/11.
 *
 * @author xiang.lv
 * @date 2016/10/11   10:32
 */
@Slf4j
public class BeanUtils {
    /**
     * Used to access properties
     */
    private final PropertyUtilsBean propertyUtilsBean;

    private final static BeanUtils instance = new BeanUtils();

    public static BeanUtils getInstance() {
        return instance;
    }

    private BeanUtils() {
        this.propertyUtilsBean = new PropertyUtilsBean();
    }


    /**
     * 将　orig　中同名属性复制到对象　dest　同名属性中
     *
     * @param dest
     * @param orig
     */
    public void copy(Object dest, Map<String, String[]> orig) {
        if (Objects.isNull(dest) || Objects.isNull(orig)) {
            log.warn("dest and orig one is null");
            return;
        }
        for (Map.Entry<String, String[]> entry : orig.entrySet()) {
            String name = entry.getKey();
            if (getPropertyUtils().isWriteable(dest, name)) {
                try {
                    copyProperty(dest, name, entry.getValue());
                } catch (Exception e) {
                    log.error("将 map中同名属性复制到Bean出错,bean=" + dest, e);
                }

            }
        }
    }

    /**
     * 返回两个实体不相同字段拼成的实体
     */
    public <T> T beanDiff(T orig, T dest) {
        try {
            if (dest == null) {
                throw new IllegalArgumentException("No destination bean specified");
            }
            if (orig == null) {
                throw new IllegalArgumentException("No origin bean specified");
            }
            if (Objects.equals(orig, dest)) {
                return orig;
            }
            T beanDiff = (T) orig.getClass().newInstance();
            if (orig instanceof DynaBean || dest instanceof DynaBean) {
                throw new IllegalArgumentException("Not support for DynaBean yet");
            } else if (orig instanceof Map) {
                for (Object o : ((Map<?, ?>) orig).entrySet()) {
                    Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
                    String name = (String) entry.getKey();
                    if (isWriteable(beanDiff, name)) {
                        Object origValue = ((Map) orig).get(name);
                        Object destValue = ((Map) dest).get(name);
                        if (!Objects.equals(origValue, destValue)) {
                            setSimpleProperty(beanDiff, name, destValue);
                        }
                    }
                }
            } else /* if (orig is a standard JavaBean) */ {
                PropertyDescriptor[] origDescriptors = getPropertyDescriptors(orig);
                for (PropertyDescriptor origDescriptor : origDescriptors) {
                    String name = origDescriptor.getName();
                    if (isReadable(orig, name) && isReadable(dest, name) && isWriteable(beanDiff, name)) {
                        Object origValue = getSimpleProperty(orig, name);
                        Object destValue = getSimpleProperty(dest, name);
                        if (!Objects.equals(origValue, destValue)) {
                            setSimpleProperty(beanDiff, name, destValue);
                        }
                    }
                }
            }
            return beanDiff;
        } catch (Exception ex) {
            log.error("Bean Diff Failed", ex);
            return null;
        }
    }

    /**
     * 将字段不同的内容复制到实体里
     * 将dest 中变动的字段复制到orig中
     */
    public <T> void copyDiff(T orig, T dest) {
        try {
            if (dest == null) {
                throw new IllegalArgumentException("No destination bean specified");
            }
            if (orig == null) {
                throw new IllegalArgumentException("No origin bean specified");
            }
            if (Objects.equals(orig, dest)) {
                return;
            }
            if (orig instanceof DynaBean || dest instanceof DynaBean) {
                throw new IllegalArgumentException("Not support for DynaBean yet");
            } else if (orig instanceof Map) {
                for (Object o : ((Map<?, ?>) orig).entrySet()) {
                    Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
                    String name = (String) entry.getKey();
                    if (isWriteable(orig, name)) {
                        Object origValue = ((Map) orig).get(name);
                        Object destValue = ((Map) dest).get(name);
                        if (destValue != null && !Objects.equals(origValue, destValue)) {
                            setSimpleProperty(orig, name, destValue);
                        }
                    }
                }
            } else /* if (orig is a standard JavaBean) */ {
                PropertyDescriptor[] origDescriptors = getPropertyDescriptors(orig);
                for (PropertyDescriptor origDescriptor : origDescriptors) {
                    String name = origDescriptor.getName();
                    if (isReadable(orig, name) && isReadable(dest, name) && isWriteable(orig, name)) {
                        Object origValue = getSimpleProperty(orig, name);
                        Object destValue = getSimpleProperty(dest, name);
                        if (destValue != null && !Objects.equals(origValue, destValue)) {
                            setSimpleProperty(orig, name, destValue);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Bean Diff Failed", ex);
        }
    }

    /**
     * 比较两个实体，返回比较之后结果Map
     * key: fieldName, 字段名
     * before: origValue, 变更前字段值
     * after: destValue, 变更后值
     * diff: boolean, 是否变更
     */
    public <T> Map<String, Map<String, Object>> analyse(T before, T after) {
        if (Objects.isNull(before) || Objects.isNull(after)) {
            log.warn("dest and orig one is null");
            return Collections.emptyMap();
        }
        Map<String, Map<String, Object>> analysis = new HashMap<>();
        try {
            if (before instanceof DynaBean || after instanceof DynaBean) {
                throw new IllegalArgumentException("Not support for DynaBean yet");
            } else if (before instanceof Map) {
                for (Object o : ((Map<?, ?>) before).entrySet()) {
                    Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
                    String key = (String) entry.getKey();
                    Map<String, Object> analyseMap = new HashMap<>();
                    Object origValue = ((Map) before).get(key);
                    Object destValue = ((Map) after).get(key);
                    analyseMap.put("before", origValue);
                    analyseMap.put("after", destValue);
                    analyseMap.put("diff", destValue != null && !Objects.equals(origValue, destValue));
                    analysis.put(key, analyseMap);
                }
            } else /* if (orig is a standard JavaBean) */ {
                PropertyDescriptor[] origDescriptors = getPropertyDescriptors(before);
                for (PropertyDescriptor origDescriptor : origDescriptors) {
                    String key = origDescriptor.getName();
                    if (isReadable(before, key) && isReadable(after, key)) {
                        Map<String, Object> analyseMap = new HashMap<>();
                        Object origValue = getSimpleProperty(before, key);
                        Object destValue = getSimpleProperty(after, key);
                        analyseMap.put("before", origValue);
                        analyseMap.put("after", destValue);
                        analyseMap.put("diff", destValue != null && !Objects.equals(origValue, destValue));
                        analysis.put(key, analyseMap);
                    }
                }
            }
        } catch (Exception ignored) {

        }
        return analysis;
    }

    /**
     * 比较两个实体，返回比较之后结果Map
     * key: fieldName, 字段名
     * before: origValue, 变更前字段值
     * after: destValue, 变更后值
     * diff: boolean, 是否变更
     */
    public <T> boolean beanEquals(T orig, T dest, List<String> fields) {
        if (Objects.isNull(orig) || Objects.isNull(dest) || CollectionUtils.isEmpty(fields)) {
            return false;
        }
        boolean isEquals = true;
        try {
            if (orig instanceof DynaBean || dest instanceof DynaBean) {
                return false;
            } else if (orig instanceof Map) {
                for (String field : fields) {
                    Object origValue = ((Map) orig).get(field);
                    Object destValue = ((Map) dest).get(field);
                    boolean equals = fieldEquals(origValue, destValue);
//                    if (!equals) log.warn("Field {} not equals, orig={}, dest={}", field, origValue, destValue);
                    isEquals &= equals;
                }
            } else /* if (orig is a standard JavaBean) */ {
                for (String field : fields) {
                    try {
                        if (isReadable(orig, field) && isReadable(dest, field)) {
                            Object origValue = getSimpleProperty(orig, field);
                            Object destValue = getSimpleProperty(dest, field);
                            boolean equals = fieldEquals(origValue, destValue);
//                            if (!equals) log.warn("Field {} not equals, orig={}, dest={}", field, origValue, destValue);
                            isEquals &= equals;
                        }
                    } catch (Exception ignored) {
                        log.warn("Field {} not exists", field);
                    }
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return isEquals;
    }

    private PropertyUtilsBean getPropertyUtils() {
        return propertyUtilsBean;
    }

    private void copyProperty(Object dest, String name, String[] value) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (null == value || 0 == value.length) {
            log.warn(name + "　没有对应的属性值");
            return;
        }
        PropertyDescriptor propertyDescriptor = propertyUtilsBean.getPropertyDescriptor(dest, name);
        Class<?> propertyType = propertyDescriptor.getPropertyType();
//        System.out.println(propertyType);

//        System.out.println(propertyType.getTypeName()+" "+propertyType.getClass());
        //有write方法
        if (propertyType == List.class) {
            //暂未处理　
            Method writeMethod = propertyDescriptor.getWriteMethod();
            Type[] types = writeMethod.getGenericParameterTypes();
            if (null != types && 1 == types.length) {
                ParameterizedType parameterizedType = (ParameterizedType) types[0];
                String typeName = parameterizedType.getActualTypeArguments()[0].getTypeName();
                if (StringUtils.equals(String.class.getName(), typeName)) {
                    List<String> valueList = Stream.of(value).collect(Collectors.toList());
                    propertyUtilsBean.setProperty(dest, name, valueList);
                } else if (StringUtils.equals(Integer.class.getName(), typeName)) {
                    List<Integer> valueList = Stream.of(value).map(Integer::valueOf).collect(Collectors.toList());
                    propertyUtilsBean.setProperty(dest, name, valueList);
                } else if (StringUtils.equals(Long.class.getName(), typeName)) {
                    List<Long> valueList = Stream.of(value).map(Long::valueOf).collect(Collectors.toList());
                    propertyUtilsBean.setProperty(dest, name, valueList);
                }
            }
        } else if (propertyType == String.class) {
            propertyUtilsBean.setProperty(dest, name, value[0]);
        } else if (propertyType == Integer.class) {
            propertyUtilsBean.setProperty(dest, name, SafeConverter.toInt(value[0]));
        } else if (propertyType == Long.class) {
            propertyUtilsBean.setProperty(dest, name, SafeConverter.toLong(value[0]));
        } else if (propertyType == Double.class) {
            propertyUtilsBean.setProperty(dest, name, SafeConverter.toDouble(value[0]));
        } else if (propertyType == java.util.Date.class || StringUtils.equalsIgnoreCase(propertyType.getTypeName(), "java.util.Date")) {
            propertyUtilsBean.setProperty(dest, name, SafeConverter.toDate(value[0]));
        } else if (propertyType == Boolean.class) {
            propertyUtilsBean.setProperty(dest, name, SafeConverter.toBoolean(value[0]));
        }

    }

    private boolean fieldEquals(Object origValue, Object destValue) {
        boolean equals;
        if (origValue instanceof List || destValue instanceof List) {
            // FIXME 对于 List 类型认为 null 和 [] 等价
            if (origValue == null) {
                List destList = (List) destValue;
                equals = destList.isEmpty();
            } else if (destValue == null) {
                List origList = (List) origValue;
                equals = origList.isEmpty();
            } else {
                equals = origValue.equals(destValue);
            }
        } else if (origValue instanceof String || destValue instanceof String) {
            // FIXME 对于 String 类型 认为 null 和 "" 等价
            String origStr = SafeConverter.toString(origValue, "");
            String destStr = SafeConverter.toString(destValue, "");
            equals = StringUtils.equals(origStr, destStr);
        } else if (origValue instanceof Boolean || destValue instanceof Boolean) {
            // FIXME 对于 Boolean 类型 认为 null 和 false 等价
            equals = (Boolean.TRUE.equals(origValue) == Boolean.TRUE.equals(destValue));
        } else {
            equals = Objects.equals(origValue, destValue);
        }
        return equals;
    }

    public static void main(String[] args) {
        List<Map<String, Object>> f1 = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Map<String, Object> info = new HashMap<>();
            info.put(String.valueOf(i), i);
            f1.add(info);
        }

        List<Map<String, Object>> f2 = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Map<String, Object> info = new HashMap<>();
            info.put(String.valueOf(i), i);
            f2.add(info);
        }

        List<String> fields = Arrays.asList("fullName", "baiduGps", "firstCategory");

        MizarShop s1 = new MizarShop();
//        s1.setFullName("A"); // String
        s1.setRatingStar(6); // Digit
//        s1.setBaiduGps(false); // Boolean
//        s1.setFirstCategory(Arrays.asList("A", "B")); // List<String>
        s1.setFaculty(f1); // List<Map>

        MizarShop s2 = new MizarShop();
        s2.setFullName(""); // String
        s2.setRatingStar(6); // Digit
        s2.setBaiduGps(false); // Boolean
        s2.setFirstCategory(new ArrayList<>()); // List<String>
        s2.setFaculty(f2); // List<Map>

        System.out.println("S1 equals S2? " + BeanUtils.getInstance().beanEquals(s1, s2, fields));

    }

}
