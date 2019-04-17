package com.voxlearning.wechat.constants;

import lombok.Getter;

/**
 * @author Xin Xin
 * @since 10/16/15
 */
public enum AuthType {
    TEACHER_HOMEWORK_ASSIGN("assignhomework"), //老师布置作业
    TEACHER_HOMEWORK_HISTORY("homeworkhistory"),//老师作业历史
    TEACHER_CLAZZMGN("teacherclazzmgn"),//老师班级管理

    HOMEWORK("homework"), //最新作业
    REPORT("report"), //学习报告
    PARADISE("paradise"), //课外乐园
    SMART("smart"), //课堂表现
    @Deprecated
    STAR_REWARD("starreward"), //星星奖励
    PARENT_REWARD("parentreward"),//家长奖励
    UCENTER("ucenter"), //会员中心
    PARENT_ACTIVITY("parent_activity"), //热门活动
    NT_PARENT_REWARD("nt_parent_reward"), //家长奖励消息通知
    NT_PARENT_ORDER_CONFIRM("nt_parent_order_confirm"), //未支付订单通知
    NT_PARENT_ORDER_LIST("nt_parent_order_list"), //每日消息提醒通知
    NT_PARENT_CHECK_FLOWER_THANKS("nt_parent_check_flower_thanks"), //查看教师送花感谢通知
    NT_PRODUCT_SHOPPING_INFO("nt_product_shopping_info"),//产品购买页面，需指定productType、sid
    NT_PRODUCT_AMERICA("nt_product_america"), //查看走遍美国
    NT_PRODUCT_SANGUODMZ("nt_product_sanguodmz"), //查看三国
    NT_PRODUCT_SPG("nt_product_spg"), //洛亚传说
    NT_TRUSTEE_SIGN("nt_trustee_sign"),//托管所签到
    NT_TRUSTEE_SKUPAY("nt_trustee_skupay"), //托管所支付页面
    NT_TRUSTEE_RESERVE("nt_trustee_reserve"), //托管所首页
    NT_TRUSTEE_PRESENT("nt_trustee_present"), //公开课首页
    NT_TRUSTEE_BRANCHDETAIL("nt_trustee_branchdetail"), //托管机构详情页面
    NT_TRUSTEE_ORDER("nt_trustee_order"), //公开课订单
    NT_TRUSTEE_ORDER_DETAIL("nt_trustee_order_detail"), //公开课订单详情
    TRUSTEE_COUNTDOWN("trustee_countdown"), //托管班倒计时
    TRUSTEE_HOME("trustee_home"), //托管班首页
    VIPKID_PROMOT("vipkid_promot"),// vipkid导流测试
    VIPKID_PROMOT_PRO("vipkid_promot_pro"),// vipkid导流测试
    USTALK_PROMOT_SHOT_ONE("ustalk_promot_shot_one"),// USTALK导流首发
    USTALK_WECHAT_MENU("ustalk_wechat_menu"),//ustalk微信菜单常规化
    MICRO_COURSE("micro_course"),//微课堂
    MICRO_COURSE_CURRICULUM("micro_course_curriculum"),//微课堂具体系列课程购买
    PIC_LISTEN("pic_listen"),//点读机
    NT_SEATTLE("nt_seattle"),//一起奥数推广，涉及付费，需要使用主站cookie
    CHIPS_CENTER("chips_center"), //薯条英语中心
    CHIPS_CRATE_ORDER("chips_create_order"),//薯条英语下单页
    CHIPS_INVITATION("chips_invitation"), //薯条英语邀请
    CHIPS_STUDY_REWARD("chips_study_reward"), //薯条英语神秘奖励
    CHIPS_STUDY_SUMMARY("chips_study_summary"), //薯条英语学习总结
    CHIPS_STUDY_LIST("chips_study_list"), //薯条英语学习列表
    CHIPS_STUDY_FINISH("chips_study_finish"), //薯条英语学习列表
    CHIPS_STUDY_CERTIFICATE("chips_study_certificate"),//薯条英语学习列表
    CHIPS_OFFICIAL_PRODUCT_AD("CHIPS_OFFICIAL_PRODUCT_AD"),//薯条英语正式课的广告
    CHIPS_STUDY_FINAL_REPORT("chips_study_final_report"),//薯条英语定级报告
    CHIPS_FORMAL_AD_1("CHIPS_FORMAL_AD_1"),//薯条英语等级1的广告页
    CHIPS_FORMAL_AD_2("CHIPS_FORMAL_AD_2"),//薯条英语等级2的广告页
    CHIPS_FORMAL_AD_3("CHIPS_FORMAL_AD_3"),//薯条英语等级3的广告页
    CHIPS_FORMAL_AD_4("CHIPS_FORMAL_AD_4"),//薯条英语4广告页
    CHIPS_FORMAL_AD_5_1("CHIPS_FORMAL_AD_5_1"),//薯条英语广告页5
    CHIPS_FORMAL_AD_5_2("CHIPS_FORMAL_AD_5_2"),//薯条英语广告页5
    CHIPS_OPEN_AD_6_COUNCIL_SCHOOL("CHIPS_OPEN_AD_6_COUNCIL_SCHOOL"), //公立系统广告页
    CHIPS_OPEN_AD_7_GRADE("CHIPS_OPEN_AD_7_GRADE"), //
    CHIPS_OPEN_ADDRESS_CHECK("CHIPSOPENADDRESSCHECK"), // 填写页
    CHIPS_GROUP_SHOPPING("CHIPSGROUPSHOPPING"), //
    CHIPS_ROBIN_NEW("chipsrobinnew"), //地推用的广告页
    CHIPS_INVITATION2("chipsinvitationv2"), //薯条英语邀请v2
    CHIPS_INVITATION_PIC("chipsinvitationpic"), //薯条英语邀请
    CHIPS_INVITATION_BE("chipsinvitationbe"), //薯条英语邀请广告页
    CHIPS_CRATE_ORDERV2("chipscreateorderv2"),//薯条英语下单页v2
    CHIPS_PERSONAL_REWARD("chipspersonalreward"),//薯条英语个人有奖中心
    CHIPS_ACTIVITY_LEAD("chipsactivitylead"),//薯条英语活动引导
    CHIPS_DRAWING_TASK_JOIN("CHIPSDRAWINGTASKJOIN"),//薯条英语图鉴任务
    CHIPS_UGC("chips_ugc"),// 薯条英语问卷
    CHIPS_UGC_ORAL("chips_ugc_oral"),// 薯条英语电话口语测试问卷
    CHIPS_UGC_MAIL("chips_ugc_mail"),// 薯条英语邮寄地址问卷
    CHIPS_ACTIVE_SERVICE("chipsActiveService"),// 薯条英语主动服务完课点评
    CHIPS_OTHER_SERVICE("chipsOtherService"),// 薯条英语其他模板主动服务
    CHIPS_RENEW("chipsRenew"),// 薯条英语续费提醒
    CHIPS_REPORT_V2("chipsReportV2"),// 薯条英语定级报告
    CHIPS_STUDY_INFORMATION("chipsStudyInformation");// 学习相关信息流内容

    @Getter
    private String type;

    AuthType(String type) {
        this.type = type;
    }

    public static AuthType of(String type) {
        for (AuthType t : values()) {
            if (t.type.equals(type)) {
                return t;
            }
        }
        return null;
    }

}
