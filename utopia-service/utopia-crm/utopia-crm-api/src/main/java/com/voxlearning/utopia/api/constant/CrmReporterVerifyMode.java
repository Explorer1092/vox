package com.voxlearning.utopia.api.constant;

/**
 * Created by jiang wei on 2016/7/26.
 */
public enum CrmReporterVerifyMode {
    SYS("系统验证"),
    MANU("人工审核"),
    NULL("空"),
    WUPIPEI("无匹配生成");



    public final String value;

    CrmReporterVerifyMode(String value) {
        this.value = value;
    }

    public static CrmReporterVerifyMode nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }

}
