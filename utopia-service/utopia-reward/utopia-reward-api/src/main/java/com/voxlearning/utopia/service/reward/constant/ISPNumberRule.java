package com.voxlearning.utopia.service.reward.constant;

import lombok.AllArgsConstructor;

/**
 * 运营商号码规则
 * Created by haitian.gan on 2017/4/18.
 */
@AllArgsConstructor
public enum ISPNumberRule {

    ALL("三网通","^(1)\\d{10}$"),
    UNICOM("联通","^(((13)[0-2])|((15)[5,6])|(18[5,6])|145|176)[0-9]{8}$"),
    MOBILE("移动","^(((13)[4-9])|((15)[0,1,2,7,8,9])|(18[2,3,4,7,8])|147|178)[0-9]{8}$"),
    TELECOM("电信","^(133|153|177|173|(18[0,1,9]))[0-9]{8}$"),
    UNKNOWN("未知","");

    private String desc;
    private String matchRegx;

    public static ISPNumberRule parse(String value){
        try{
            return valueOf(value);
        }catch(Exception e){
            return ISPNumberRule.UNKNOWN;
        }
    }

    public boolean isLegalNumber(String number){
        return number != null && number.matches(matchRegx);
    }

}
