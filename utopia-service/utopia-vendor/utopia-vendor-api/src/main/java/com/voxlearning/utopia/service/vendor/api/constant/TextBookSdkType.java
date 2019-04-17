package com.voxlearning.utopia.service.vendor.api.constant;

/**
 * @author jiangpeng
 * @since 2017-07-11 下午4:17
 **/
public enum  TextBookSdkType {

    waiyan,

    renjiao,

    hujiao,

    none;

    public Boolean hasSdk(){
        return this != none;
    }
}
