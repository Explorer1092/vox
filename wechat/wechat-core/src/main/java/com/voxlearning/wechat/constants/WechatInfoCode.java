package com.voxlearning.wechat.constants;

import lombok.Getter;

/**
 * Created by xinxin on 18/11/2015.
 */
public enum WechatInfoCode {
    AUTH_FETCH_OPEN_ID_FROM_CODE_FAIL("00001001", "授权失败,请重新操作", null),

    PARENT_ORDER_ALREADY_PAYD("01001001", "订单已被支付", "/parent/ucenter/orderlist.vpage"),
    PARENT_ORDER_NOT_EXIST("01001002", "订单信息不存在", "/parent/ucenter/orderlist.vpage"),
    PARENT_ORDER_CREATE_FAIL("01001003", "订单生成失败", "/parent/ucenter/orderlist.vpage"),
    PARENT_ORDER_PREPAY_FAIL("01001004", "微信预下单失败", "/parent/ucenter/orderlist.vpage"),
    PARENT_ORDER_WECHAT_NOT_SUPPORT("01001005", "微信家长端暂不支持此产品", "/parent/ucenter/orderlist.vpage"),
    // Task#18426 By Wyc
    @Deprecated
    PARENT_ORDER_GMC_UID_NULL("01001006", "趣味数学挑战赛购买信息异常", null),
    PARENT_SEATTLE_ACTIVITY_NOT_EXIST("01001007", "活动已下线", null),

    CHIPS_ORDER_ALREADY_PAYD("01001008", "订单已被支付", "/chips/center/paymentsuccess.vpage"),
    CHIPS_ORDER_NOT_EXIST("01001009", "订单信息不存在", "/chips/center/index.vpage"),
    CHIPS_ORDER_CREATE_FAIL("01001010", "订单生成失败", "/chips/center/index.vpage"),
    CHIPS_ORDER_PREPAY_FAIL("01001011", "微信预下单失败", "/chips/center/index.vpage"),


    PARENT_REPORT_ALREADY_UPDATED("01002001", "报告数据已更新，快去关注最新作业报告吧～", "parent/homework/common.vpage?page=reportindex"),
    PARENT_REPORT_HAS_NO_DATA("01002002", "未查询到报告数据", "parent/homework/common.vpage?page=reportindex"),

    PARENT_INTEGRAL_PRESENT_NOT_ALLOW_WITHOUT_TEACHER_AUTH("01003001", "您孩子的老师未认证,暂时不支持学豆赠送", "/parent/homework/common.vpage?page=smart"),

    PARENT_LOGIN_CACHE_EXPIRED("01004001", "未查询到帐号,请返回重试", "/signup/parent/login.vpage"),
    PARENT_BIND_CACHE_EXPIRED("01004002", "未查询到帐号,请返回重试", "/parent/ucenter/index.vpage"),
    PARENT_BIND_CHILD_COUNT_LIMIT("01004003", "家长号最多关联3个孩子,请联系客服操作", "/parent/ucenter/index.vpage"),
    // Task#18426 By Wyc
    @Deprecated
    PARENT_VENDOR_GMC_FAIL("01004004", "趣味数学挑战赛登录信息保存失败", null),

    PARENT_TRUSTEE_ORDER_NOT_EXIST("01005001", "未查询到订单信息", "/parent/trustee/orderlist.vpage"),
    PARENT_TRUSTEE_ORDER_CANNOT_REFUND("01005002", "此订单状态不可申请退款", "/parent/trustee/orderlist.vpage"),
    PARENT_TRUSTEE_ORDER_REFUND_FAIL("01005003", "退款失败", "/parent/trustee/orderlist.vpage"),
    PARENT_TRUSTEE_GOODS_INVALID("01005004", "本产品已经下线", "/parent/trustee/index.vpage"),

    TEACHER_ACTIVITY_DOWN("01006001", "活动已下线", "/teacher/homework/index.vpage");

    @Getter
    private String code;
    @Getter
    private String desc;
    @Getter
    private String returnUrl;


    WechatInfoCode(String code, String desc, String url) {
        this.code = code;
        this.desc = desc;
        this.returnUrl = url;
    }
}
