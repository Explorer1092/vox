package com.voxlearning.washington.controller.client.upgrade;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class UpgradeParam implements Serializable {

    private String productId;
    private String productName;
    private String apkVer;
    private String sysVer;
    private String channel;
    private String region;
    private String ktwelve;
    private String school;
    private String clazz;
    private String subject;
    private String clazzLevel;
    private String user;
    private String userType;
    private String imei;
    private String brand;
    private String model;
    private String test;
    private Boolean isAuto;
    private String apkMD5;
    private String pluginStr;

    private Map<String, String> plugins;
    private Map<String, String> idVers;
    private List<String> reqPids;


}
