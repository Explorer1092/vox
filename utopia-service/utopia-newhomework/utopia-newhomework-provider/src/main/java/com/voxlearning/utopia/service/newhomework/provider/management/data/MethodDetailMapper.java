package com.voxlearning.utopia.service.newhomework.provider.management.data;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

/**
 * @author xuesong.zhang
 * @since 2018/4/9
 */
@Getter
@Setter
public class MethodDetailMapper {
    private String methodId;
    private String methodName;
    private String displayName;
    private Method method;

    public static MethodDetailMapper newInstance(Method method) {
        MethodDetailMapper methodDetailMapper = new MethodDetailMapper();

        StringBuilder methodId = new StringBuilder(method.getName() + "(");
        for (Class midClass : method.getParameterTypes()) {
            methodId.append(midClass.getSimpleName()).append(",");
        }
        if (methodId.toString().lastIndexOf(',') == methodId.length() - 1) {
            methodId = new StringBuilder(methodId.substring(0, methodId.length() - 1));
        }
        methodId.append(")");

        methodDetailMapper.setMethodId(methodId.toString());
        methodDetailMapper.setMethodName(method.getName());
        methodDetailMapper.setDisplayName(methodId.toString());
        methodDetailMapper.setMethod(method);
        return methodDetailMapper;
    }

}
