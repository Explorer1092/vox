package com.voxlearning.washington.support;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author jiangpeng
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ParentUserCenterNativeFunction {

    //所有的顺序都可配置吧。不要写死在枚举里了。

    SYSTEM_NOTIFY(-1, "系统通知", null),

    RESET_CHILD_PASSWORD(-1, "重置孩子密码", null),

    CHILD_VOICE_RECORD(-1, "孩子录音", null),

    /**
     * 这个比较特殊,配置返回url。
     */
    ORDER_LIST(-1, "订单列表", null),

    H5(-1, "跳转到h5-注意需要自己设置顺序", null),

    /**
     * 这个类型有点恶心,壳要求这个类型返回H5的类型。
     * 所以在处理的时候,加一个关联枚举,如果有关联枚举,则给壳返回关联的类型
     * 因为这个类型处理的时候需要根据这个类型处理title....
     */
    CHILD_INFO(-1, "孩子个人信息", H5),

    STUDENT_APP_CONFIG(-1, "一起作业学生端配置", null),

    ROUTER(-1, "客户端路由", null),

    UNKNOWN(-100, "未知", null);

    @Getter
    private final Integer order;

    @Getter
    private final String desc;

    @Getter
    private final ParentUserCenterNativeFunction linkFunction;

    public static ParentUserCenterNativeFunction of(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return UNKNOWN;
        }
    }
}
