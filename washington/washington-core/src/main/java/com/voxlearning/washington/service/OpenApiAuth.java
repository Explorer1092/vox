package com.voxlearning.washington.service;


import com.voxlearning.utopia.core.runtime.ProductConfig;
import org.apache.commons.codec.digest.DigestUtils;

import javax.inject.Named;

@Named
public class OpenApiAuth {

    //签名格式： "签名.[时间戳]" 例如  "abcdefg.13423812323"
    public String generateSign(Long userId) {
        Long t = System.currentTimeMillis() / 1000;
        String data = userId + "." + t + ProductConfig.get("openapi.sign_key");
        String sign = DigestUtils.sha1Hex(data) + "." + t;
        return sign;
    }

    //暂定有效期是1天以内
    public boolean isSignValid(String sign, String userId) {
        int index = sign.indexOf(".");
        if(index < 0) {
            return false;
        }
        String oldSign=sign.substring(0,index);
        String signTime=sign.substring(index+1);

        //检查sha1
        String data=userId+"."+signTime+ProductConfig.get("openapi.sign_key");
        String newSign = DigestUtils.sha1Hex(data);
        if(!oldSign.equals(newSign)){
            return false;
        }

        //检查时间
        Long t = Long.valueOf(signTime);
        Long now = System.currentTimeMillis() /1000;
        if((now - t ) - (24 * 60 *60)>0){ //有效期一天
            return false;
        }
        return true;
    }

}
