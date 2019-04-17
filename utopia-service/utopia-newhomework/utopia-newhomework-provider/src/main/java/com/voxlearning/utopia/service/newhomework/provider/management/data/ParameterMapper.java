package com.voxlearning.utopia.service.newhomework.provider.management.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author xuesong.zhang
 * @since 2018/4/9
 */
@Getter
@Setter
@AllArgsConstructor
public class ParameterMapper {
    private String name;
    private Class<?> type;
    private String typeStr;
    private Object value;
    private String valueStr;
}
