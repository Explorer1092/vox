package com.voxlearning.washington.controller.thirdparty.qiyukf.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 七鱼接口数据属性定义
 *
 * @author Wenlong Meng
 * @version 1.0.0
 * @date 2018-08-28
 */
@AllArgsConstructor
public enum QiYuDataAttr {

    ID("用户ID", "userId", null, false),
    NAME("姓名", "name", "real_name", true),
    IDENTITY("身份", "identity", null, false),
    CLAZZ("年级/班级", "clazzName", null, false),
    SCHOOL("所属学校", "schoolName", null, false),
    AUTH_STATE("认证状态", "authState", null, false),
    SUBJECT("学科", "subject", null, false),
    DEPARTMENT("部门", "department", null, false),
    MOBILE("手机","phone","mobile_phone",true),
    PARENT_ID("家长ID","parentId",null,false),
    PAY_STATE("付费状态","payState",null,false),
    LAST_LOGIN("最近登录","lastLogin",null,false);

    @Getter
    private String label;
    @Getter
    private String key;
    @Getter
    private String mapName;
    @Getter
    private boolean save;
}
