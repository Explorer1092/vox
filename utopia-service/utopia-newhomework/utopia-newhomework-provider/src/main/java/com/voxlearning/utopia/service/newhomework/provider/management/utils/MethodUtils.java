package com.voxlearning.utopia.service.newhomework.provider.management.utils;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.provider.management.data.MethodDetailMapper;
import com.voxlearning.utopia.service.newhomework.provider.management.data.ParameterMapper;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2018/4/9
 */
public class MethodUtils {
    static public List<MethodDetailMapper> fetchAvailableEntranceMethods(Class<?> aClass) {

        Set<String> unDisplayMethods = Arrays.stream(Object.class.getMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());

        unDisplayMethods.addAll(Arrays.stream(SpringContainerSupport.class.getMethods())
                .map(Method::getName)
                .collect(Collectors.toSet()));

        List<Method> methodList = Arrays.stream(aClass.getMethods())
                .filter(method -> !unDisplayMethods.contains(method.getName()))
                .collect(Collectors.toList());
        return methodList.stream().map(MethodDetailMapper::newInstance)
                .collect(Collectors.toList());

    }

    public static List<ParameterMapper> fetchParameterMapper(HttpServletRequest httpServletRequest, Method invokeMethod) {
        List<ParameterMapper> parameterMappers = new ArrayList<>();
        ParameterNameDiscoverer parameterNameDiscoverer =
                new LocalVariableTableParameterNameDiscoverer();
        String[] parameters = parameterNameDiscoverer.getParameterNames(invokeMethod);
        Class<?>[] parameterTypes = invokeMethod.getParameterTypes();


        for (int i = 0; i < parameters.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            String parameter = parameters[i];
            String valueStr = httpServletRequest.getParameter(parameter);
            Object value = matchValue(parameterType, valueStr);
            parameterMappers.add(new ParameterMapper(parameter, parameterType, parameterType.getName(), value, valueStr));
        }
        return parameterMappers;
    }

    static private Object matchValue(Class<?> parameterType, String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        if (parameterType == Long.class) {
            if (value.lastIndexOf('L') == value.length() - 1 || value.lastIndexOf('l') == value.length() - 1) {
                value = value.substring(0, value.length() - 1);
            }
            return Long.valueOf(value);
        }

        if (parameterType == String.class) {
            return SafeConverter.toString(value);
        }
        if (parameterType == Integer.class) {
            return Integer.valueOf(value);
        }
        if (parameterType == Boolean.class) {
            return Boolean.valueOf(value);
        }
        if (parameterType == Date.class) {
            return DateUtils.stringToDate(value);
        }
        Class<?> superclass = parameterType.getSuperclass();
        if (superclass == Enum.class) {
            String finalValue = value;
            return Arrays.stream(parameterType.getEnumConstants())
                    .filter(p -> StringUtils.equals(p.toString(), finalValue))
                    .findFirst()
                    .orElse(null);
        }
        return JsonUtils.fromJson(value, parameterType);
    }
}
