package com.voxlearning.utopia.service.parent.homework.impl.template.base;

import com.google.common.collect.Maps;
import com.voxlearning.alps.lang.util.MapMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 上下文基类
 *
 * @author Wenlong Meng
 * @since Feb 25, 2019
 */
@Setter
@Getter
public class BaseContext {

    //local variables
    private Map<String, Object> extInfos;
    private MapMessage mapMessage;
    private boolean terminate;

    //Logic
    /**
     * 添加扩展属性
     *
     * @param key
     * @param value
     */
    public void set(String key, Object value){
        if(extInfos == null){
            extInfos = Maps.newHashMap();
        }
        extInfos.put(key, value);
    }

    /**
     * 获取扩展属性
     *
     * @param key
     * @return value
     */
    public <T> T get(String key){
        if(extInfos != null){
            return (T)extInfos.get(key);
        }
        return null;
    }
}
