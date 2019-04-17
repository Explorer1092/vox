package com.voxlearning.utopia.service.reward.constant;

/**
 * Created by ganhaitian on 2018/6/11.
 */
public enum  PublicGoodModel {

    NONE("老版公益项目"),
    CLASS_ROOM("一起教室");

    private String desc;

    PublicGoodModel(String desc){
        this.desc = desc;
    }

    public static PublicGoodModel parse(String code){
        try{
            return valueOf(code);
        }catch (Exception e){
            return NONE;
        }
    }
}
