package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 客户端版本管理
 * Created by shuang.li on 2018/09/17.
 */
@Getter
@RequiredArgsConstructor
public enum CrmClientAppUpdType {
    // 小学
    JUNIOR_STU_ANDROID(100, "一起作业学生端（Android）"),
    JUNIOR_STU_IOS(101, "一起作业学生端(iOS)"),
    JUNIOR_TEACHER_ANDROID(300, "一起作业老师（Android）"),
    JUNIOR_TEACHER_IOS(301, "一起作业老师(iOS)"),
    JUNIOR_PARENT_ANDROID(200, "一起作业家长端（Android）"),
    JUNIOR_PARENT_IOS(201, "一起作业家长端（iOS）"),
    // 天玑
    TIANJI_ANDROID(900, "天玑（安卓）"),
    TIANJI_IOS(901, "天玑（iOS）"),
    FENGCHAO_ANDROID(910, "蜂巢(android)"),
    FENGCHAO_IOS(911, "蜂巢（iOS）"),
    // 直播
    LIVE_TEACHER_ANDROID(400, "一起学老师端 （安卓）"),
    LIVE_TEACHER_IOS(401, "一起学老师端 （iOS）"),
    LIVE_CLOUD_ANDROID(701, "一起学云课堂（安卓）"),
    LIVE_CLOUD_IOS(700, "一起学云课堂（iOS）"),
    // 中学
    MID_STU_ANDROID(110, "中学学生端（安卓）"),
    MID_STU_IOS(111, "中学学生端（iOS）"),
    MID_TEACHER_ANDROID(310, "中学老师端（安卓）"),
    MID_TEACHER_IOS(311, "中学老师端（iOS）"),
    MID_PARENT_ANDROID(210, "中学家长端（安卓）"),
    MID_PARENT_IOS(211, "中学家长端（iOS）"),
    // u3d
    U3D_IOS(500, "u3d_ios"),
    U3D_ANDROID(501, "u3d_android"),
    U3D_IOS00(101500, "u3d_ios"),
    U3D_ANDROID00(100500, "u3d_android"),
    U3D_EBOOK_IOS(101501, "u3d_english_book_ios"),
    U3D_EBOOK_ANDROID(100501, "u3d_english_book_android"),
    U3D_CBOOK_IOS(101502, "u3d_chinese_book_ios"),
    U3D_CBOOK_ANDROID(100502, "u3d_chinese_book_android"),
    U3D_SPELLING_IOS(101503, "u3d_natural_spelling_ios"),
    U3D_SPELLING_ANDROID(100503, "u3d_natural_spelling_android"),
    U3D_BAOZOU_IOS(101504, "u3d_baozou_ios"),
    U3D_BAOZOU_ANDROID(100504, "u3d_baozou_android"),
    U3D_MAGICPHONICS_IOS(101505, "u3d_magicphonics_ios"),
    U3D_MAGICPHONICS_ANDROID(100505, "u3d_magicphonics_android"),
    U3D_MATHEMATICAL_THINKING_IOS(101506, "u3d_mathematical_thinking_ios"),
    U3D_MATHEMATICAL_THINKING_ANDROID(100506, "u3d_mathematical_thinking_android"),
    ANDROID_PLUGIN_AUDIO(100700, "配音插件"),
    ANDROID_PLUGIN_ARITHMETIC(100701, "速算脑力王"),
    ANDROID_PLUGIN_PAPERCALC(100702, "口算拍照"),
    ANDROID_PLUGIN_POINTREAD(100703, "点读机"),
    ANDROID_PLUGIN_ORALCOMM(100704, "口语交际"),
    ANDROID_PLUGIN_LIVEROOM(100705, "直播间"),
    ANDROID_PLUGIN_SHUTIAO(100706, "薯条");

    private final int id;
    private final String typeName;

    private static final Map<Integer, CrmClientAppUpdType> crmClientAppUpdMap;

    static {
        crmClientAppUpdMap = new HashMap<>();
        for (CrmClientAppUpdType type : CrmClientAppUpdType.values()) {
            crmClientAppUpdMap.put(type.getId(), type);
        }
    }

    public static CrmClientAppUpdType typeOf(Integer id) {
        if (id == null) {
            return null;
        }
        return crmClientAppUpdMap.get(id);
    }
}
