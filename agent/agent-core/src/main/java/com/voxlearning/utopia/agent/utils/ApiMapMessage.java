package com.voxlearning.utopia.agent.utils;

import com.voxlearning.alps.core.util.StringUtils;

import java.util.LinkedHashMap;

/**
 * ApiMapMessage
 *
 * @author song.wang
 * @date 2018/8/9
 */
public class ApiMapMessage extends LinkedHashMap<String, Object> {

    public ApiMapMessage setResult(Object v) {
        put("result", v);
        return this;
    }

    public ApiMapMessage setMessage(Object v){
        put("message", v);
        return this;
    }
    public static ApiMapMessage successMessage(){
        return new ApiMapMessage().setResult("success");
    }

    public static ApiMapMessage successMessage(String message){
        return new ApiMapMessage().setResult("success").setMessage(message);
    }

    public static ApiMapMessage errorMessage(String errorCode){
        return new ApiMapMessage().setResult(errorCode);
    }

    public static ApiMapMessage errorMessage(String errorCode, String message){
        return new ApiMapMessage().setResult(errorCode).setMessage(message);
    }

    public boolean isSuccess(){
        Object v = get("result");
        if(v == null){
            return false;
        }
        if(v instanceof String){
            return "success".equals(v);
        }
        return false;
    }
}
