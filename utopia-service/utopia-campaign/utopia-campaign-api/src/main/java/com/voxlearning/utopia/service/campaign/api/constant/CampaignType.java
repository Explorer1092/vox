/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.campaign.api.constant;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.spi.common.DateFormatParser;
import com.voxlearning.utopia.temp.NewSchoolYearActivity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 活动类型
 * Created by Shuai Huan on 2014/10/8.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum CampaignType {

    TRAVEL_AMERICA_BINDING(
            1,
            "走遍美国_微信绑定奖励",
            100,
            "[{\"type\":\"diamond\", \"amount\":100}]",
            0,
            stringToDate("2099-10-30 23:59:59"),
            "2014-10-10 00:00:00",
            "2099-01-01 23:59:59"
    ),
    PK(
            2,
            "PK馆_微信绑定奖励",
            10,
            "[{\"type\":\"vatality\", \"amount\":10}]",
            0,
            stringToDate("2099-10-30 23:59:59"),
            "2014-10-10 00:00:00",
            "2099-01-01 23:59:59"),
    TRAVEL_AMERICA_FIRST_VIP(
            3,
            "走遍美国_首充奖励",
            500,
            "[{\"type\":\"diamond\", \"amount\":500}]",
            0,
            stringToDate("2099-10-30 23:59:59"),
            "2014-10-10 00:00:00",
            "2099-01-01 23:59:59"),
    REWARD_JOIN_BINDING(
            4,
            "奖品中心_加入愿望盒",
            0,
            "[]",
            0,
            stringToDate("2099-10-30 23:59:59"),
            "2014-10-10 00:00:00",
            "2099-01-01 23:59:59"),
    TRAVEL_AMERICA_MAP_APPOINT_WSD(
            5,
            "走遍美国_新地图华盛顿预约奖励",
            0,
            "[{\"type\":\"diamond\", \"amount\":200},{\"type\":\"item\", \"id\":15, \"amount\":1}]",
            1,
            stringToDate("2014-11-04 23:59:59"),
            "2014-11-05 00:00:00",
            "2099-01-01 23:59:59"),
    WECHAT_BINDING_AWARD_TO_TEACHER(
            6,
            "微信_绑定微信给老师发奖励",
            100,//给老师奖励,学豆和金币的比例是1:10,此处奖励老师5金币(50学豆)
            "[{\"type\":\"integral\", \"amount\":100}]",
            0,
            stringToDate("2099-10-30 23:59:59"),
            "2014-10-27 00:00:00",
            "2099-01-01 23:59:59"),
    TEACHER_LOTTERY(
            7,
            "老师抽奖",
            0,
            "[]",
            0,
            stringToDate("2017-04-09 23:59:59"),
            "2014-11-05 00:00:00",
            "2099-01-01 23:59:59"),
    PK_COMRADES(
            8,
            "PK战友召集令",
            0,
            "[]",
            0,
            stringToDate("2014-12-31 23:59:59"),
            "2014-11-17 00:00:00",
            "2015-01-07 23:59:59"),
    XXT_PARENT_MESSAGE(
            9,
            "家长通知绑定微信",
            0,
            "[]",
            0,
            stringToDate("2014-12-31 23:59:59"),
            "2014-11-17 00:00:00",
            "2015-01-07 23:59:59"),
    CLAZZ_LEVEL_RANK(
            10,
            "班级排行活动",
            0,
            "[]",
            0,
            stringToDate("2015-03-31 23:59:59"),
            "2014-11-19 00:00:00",
            "2015-03-31 23:59:59"),
    TRAVEL_AMERICA_MAP_APPOINT_SANF(
            11,
            "走遍美国_新地图旧金山预约奖励",
            0,
            "[{\"type\":\"diamond\", \"amount\":200},{\"type\":\"item\", \"id\":5, \"amount\":1}]",
            1,
            stringToDate("2014-12-04 23:59:59"),
            "2014-12-05 00:00:00",
            "2099-01-01 23:59:59"),
    SHENGDAN_CARD_POPUP(
            12,
            "圣诞系列活动奖励",
            0,
            "[]",
            0,
            stringToDate("2014-12-04 23:59:59"),
            "2014-12-05 00:00:00",
            "2099-01-01 23:59:59"),
    WECHAT_SIGN_POPUP(
            13,
            "微信签到领学豆活动",
            0,
            "[]",
            0,
            stringToDate("2099-01-01 23:59:59"),
            "2014-12-05 00:00:00",
            "2099-01-01 23:59:59"),
    BINDING_SENG_STAR(
            14,
            "绑定送星星领取学豆",
            0,
            "[]",
            0,
            stringToDate("2099-01-01 23:59:59"),
            "2014-12-05 00:00:00",
            "2099-01-01 23:59:59"),
    TRAVEL_AMERICA_SANTACLAUS(
            15,
            "走遍美国_圣诞老人限时抢购",
            0,
            "[{\"type\":\"avatar\", \"id\":6, \"amount\":1}]",
            0,
            stringToDate("2015-01-08 23:59:59"),
            "2014-12-17 00:00:00",
            "2015-01-08 23:59:59"),
    WECHAT_LOTTERY(
            16,
            "微信抽奖活动",
            0,
            "[]",
            1,
            stringToDate("2014-12-21 23:59:59"),
            "2014-12-15 00:00:00",
            "2014-12-21 23:59:59"),
    STUDENT_INVITE_REG(
            17,
            "学生邀请奖励",
            0,
            "[{\"type\":\"diamond\", \"amount\":5}]",
            0,
            stringToDate("2014-12-31 23:59:59"),
            "2014-12-10 00:00:00",
            "2015-01-15 23:59:59"),
    FIRST_GRADE_ANSWER_APPLE(
            18,
            "一年级答题分享得学豆",
            0,
            "[{\"type\":\"integral\", \"amount\":1}]",
            0,
            stringToDate("2014-12-31 23:59:59"),
            "2014-12-12 00:00:00",
            "2014-12-31 23:59:59"),
    VIP_2014_EXPENSE_AWARD(
            19,
            "12月感恩送豪礼",
            0,
            "[]",
            0,
            stringToDate("2015-01-05 23:59:59"),
            "2015-01-04 00:00:00",
            "2015-02-04 23:59:59"),
    WECHAT_TEACHER_LOTTERY(
            20,
            "微信老师抽奖",
            0,
            "[]",
            0,
            stringToDate("2099-01-01 23:59:59"),
            "2014-11-05 00:00:00",
            "2099-01-01 23:59:59"),
    WECHAT_PARENT_REWARD(
            21,
            "家长奖励",
            0,
            "[]",
            0,
            stringToDate("2099-01-01 23:59:59"),
            "2015-01-15 00:00:00",
            "2099-01-01 23:59:59"),
    TEACHER_BONUS_RECEIVE(
            22,
            "老师领取新年红包",
            0,
            "[]",
            0,
            stringToDate("2099-01-01 23:59:59"),
            "2015-01-15 00:00:00",
            "2099-01-01 23:59:59"),
    TEACHER_NO_AUTH(
            23,
            "老师未认证首页",
            0,
            "[]",
            0,
            stringToDate("2099-01-01 23:59:59"),
            "2015-01-15 00:00:00",
            "2099-01-01 23:59:59"),
    TEACHER_SMART_CLAZZ_REWARD(
            24,
            "老师领取智慧教室学豆（班费）",
            0,
            "[]",
            0,
            stringToDate("2099-01-01 23:59:59"),
            "2015-03-09 00:00:00",
            "2099-01-01 23:59:59"),
    TEACHER_WECHAT_INVITE(
            25,
            "老师微信邀请首页弹窗",
            0,
            "[]",
            0,
            stringToDate("2099-01-01 23:59:59"),
            "2015-03-25 00:00:00",
            "2099-01-01 23:59:59"),
    IANDYOU_FIRST_OPEN(
            26,
            "爱儿优四月上线-可爱的你,可爱的数学",
            0,
            "[{\"type\":\"vip\", \"name\":\"iandyou100\", \"period\":7}]",
            0,
            stringToDate("2015-04-30 23:59:59"),
            "2015-03-27 00:00:00",
            "2015-04-30 23:59:59"),
    SANGUO_REWARD_ACTIVITY(
            27,
            "进击的三国上线奖励活动",
            0,
            "[]",
            0,
            stringToDate("2015-06-30 23:59:59"),
            "2015-06-30 00:00:00",
            "2099-01-01 23:59:59"),
    MOTHERS_DAY_CARD(
            28,
            "母亲节贺卡活动",
            0,
            "[]",
            0,
            stringToDate("2015-05-15 23:59:59"),
            "2015-06-30 00:00:00",
            "2099-01-01 23:59:59"),
    SANGUO_WARM_UP(
            29,
            "进击的三国预热活动",
            0,
            "[{\"type\":\"appoint\", \"user\":$userId}]",
            0,
            stringToDate("2015-05-12 23:59:59"),
            "2015-05-06 00:00:00",
            "2015-06-05 23:59:59"),
    TRAVEL_AMERICA_WARM_UP(
            30,
            "新版走遍美国预热活动",
            0,
            "[{\"type\":\"avatar\", \"id\":8, \"amount\":1}]",
            0,
            stringToDate("2015-05-14 23:59:59"),
            "2015-05-15 00:00:00",
            "2015-06-14 23:59:59"),
    TRAVEL_AMERICA_DRIVE_STUDENT_FHWQ_UP(
            31,
            "走遍美国拉动学生作业活跃",
            0,
            "[]",
            0,
            stringToDate("2015-05-20 23:59:59"),
            "2015-05-11 00:00:00",
            "2015-06-10 23:59:59"),
    AMBASSADOR_WECHAT_LOTTERY(
            32,
            "校园大使微信抽奖",
            0,
            "[]",
            0,
            stringToDate("2015-07-20 23:59:59"),
            "2015-06-30 00:00:00",
            "2099-01-01 23:59:59"),
    TEACHER_SOURCE_COLLECTION(
            33,
            "老师来源收集",
            0,
            "[]",
            0,
            stringToDate("2015-06-30 23:59:59"),
            "2015-06-24 00:00:00",
            "2099-01-01 23:59:59"),
    TEACHER_VH_LOTTERY_AFTER_2015(
            34,
            "教师假期作业抽奖，2015后注册教师",
            0,
            "[]",
            0,
            stringToDate("2015-08-31 23:59:59"),
            "2015-06-08 00:00:00",
            "2099-01-01 23:59:59"),
    TEACHER_VH_LOTTERY_BEFORE_2015_12(
            35,
            "教师假期作业抽奖，2015前注册教师，尾号12",
            0,
            "[]",
            0,
            stringToDate("2015-08-31 23:59:59"),
            "2015-06-08 00:00:00",
            "2099-01-01 23:59:59"),
    TEACHER_VH_LOTTERY_BEFORE_2015_34(
            36,
            "教师假期作业抽奖，2015前注册教师，尾号34",
            0,
            "[]",
            0,
            stringToDate("2015-08-31 23:59:59"),
            "2015-06-08 00:00:00",
            "2099-01-01 23:59:59"),
    TEACHER_VH_LOTTERY_BEFORE_2015_56(
            37,
            "教师假期作业抽奖，2015前注册教师，尾号56",
            0,
            "[]",
            0,
            stringToDate("2015-08-31 23:59:59"),
            "2015-06-08 00:00:00",
            "2099-01-01 23:59:59"),
    TEACHER_VH_LOTTERY_BEFORE_2015_78(
            38,
            "教师假期作业抽奖，2015前注册教师，尾号78",
            0,
            "[]",
            0,
            stringToDate("2015-08-31 23:59:59"),
            "2015-06-08 00:00:00",
            "2099-01-01 23:59:59"),
    TEACHER_VH_LOTTERY_BEFORE_2015_90(
            39,
            "教师假期作业抽奖，2015前注册教师，尾号90",
            0,
            "[]",
            0,
            stringToDate("2015-08-31 23:59:59"),
            "2015-06-08 00:00:00",
            "2099-01-01 23:59:59"),
    @Deprecated
    TEACHER_TERM_BEGIN_LOTTERY(
            40,
            "教师开学大礼包抽奖",
            0,
            "[]",
            0,
            NewSchoolYearActivity.getSummerEndDate(),
            "2015-06-08 00:00:00",
            "2099-01-01 23:59:59"),
    @Deprecated
    STUDENT_LOTTERY_12(
            41,
            "学生抽奖，学校id尾号为1、2",
            0,
            "[]",
            0,
            stringToDate("2099-01-01 23:59:59"),
            "2014-11-05 00:00:00",
            "2099-01-01 23:59:59"),
    @Deprecated
    STUDENT_LOTTERY_34(
            42,
            "学生抽奖，学校id尾号为3、4",
            0,
            "[]",
            0,
            stringToDate("2099-01-01 23:59:59"),
            "2014-11-05 00:00:00",
            "2099-01-01 23:59:59"),
    STUDENT_LOTTERY_56(
            43,
            "学生抽奖，学校id尾号为5、6",
            0,
            "[]",
            0,
            stringToDate("2099-01-01 23:59:59"),
            "2014-11-05 00:00:00",
            "2099-01-01 23:59:59"),
    @Deprecated
    STUDENT_LOTTERY_78(
            44,
            "学生抽奖，学校id尾号为7、8",
            0,
            "[]",
            0,
            stringToDate("2099-01-01 23:59:59"),
            "2014-11-05 00:00:00",
            "2099-01-01 23:59:59"),
    @Deprecated
    TEACHER_TERM_BEGIN_LOTTERY_2016_SPRING(
            45,
            "教师开学大礼包抽奖2016春季",
            0,
            "[]",
            0,
            NewSchoolYearActivity.getSummerEndDate(),
            "2016-09-01 00:00:00",
            "2099-03-20 23:59:59"),
    MIDDLE_TEACHER_LOTTERY(
            46,
            "中学老师抽奖",
            0,
            "[]",
            0,
            DateUtils.stringToDate("2017-04-09 23:59:59"),
            "2016-04-10 00:00:00",
            "2099-01-01 23:59:59"),

    TEACHER_TERM_BEGIN_LOTTERY_2016_SUMMER(
            47,
            "教师开学大礼包抽奖2016夏季",
            0,
            "[]",
            0,
            NewSchoolYearActivity.getSummerEndDate(),
            "2016-09-01 00:00:00",
            "2099-03-20 23:59:59"),
    @Deprecated
    TEACHER_TERM_BEGIN_LOTTERY_2017_SPRING(
            48,
            "教师开学大礼包抽奖2017春季",
            0,
            "[]",
            0,
            NewSchoolYearActivity.getSummerEndDate(),
            "2017-02-20 00:00:00",
            "2099-03-20 23:59:59"),
    STUDENT_GOSSIP_LOTTERY_BOX(
            49,
            "APP大爆料宝箱抽奖",
            0,
            "[]",
            0,
            NewSchoolYearActivity.getSummerEndDate(),
            "2017-02-20 00:00:00",
            "2099-03-20 23:59:59"),
    UNICORN_ACTIVITY_LOTTERY(
            50,
            "独角兽活动抽奖",
            0,
            "[]",
            0,
            NewSchoolYearActivity.getSummerEndDate(),
            "2017-03-23 00:00:00",
            "2099-03-20 23:59:59"),
    TEACHER_SCHOLARSHIP_GOLD_LOTTERY(
            51,
            "17奖学金抽奖金奖池",
            0,
            "[]",
            0,
            DateUtils.stringToDate("2017-05-31 23:59:59"),
            "2017-02-20 00:00:00",
            "2099-03-20 23:59:59"),
    TEACHER_SCHOLARSHIP_SILVER_LOTTERY(
            52,
            "17奖学金抽奖银奖池",
            0,
            "[]",
            0,
            DateUtils.stringToDate("2017-05-31 23:59:59"),
            "2017-02-20 00:00:00",
            "2099-03-20 23:59:59"),
    TEACHER_SCHOLARSHIP_COPPER_LOTTERY(
            53,
            "17奖学金抽奖铜奖池",
            0,
            "[]",
            0,
            DateUtils.stringToDate("2017-05-31 23:59:59"),
            "2017-02-20 00:00:00",
            "2099-03-20 23:59:59"),
    PARENT_PICLISTEN_LOTTERY_201761(
            54,
            "点读机六一活动",
            0,
            "[]",
            0,
            DateUtils.stringToDate("2017-06-06 23:59:59"),
            "2017-02-20 00:00:00",
            "2099-03-20 23:59:59"),
    AFENTI_PREPARATION_LOTTERY(
            55,
            "阿分题预习抽奖活动",
            0,
            "[]",
            0,
            DateUtils.stringToDate("2017-08-30 23:59:59"),
            "2017-02-20 00:00:00",
            "2099-03-20 23:59:59"),
    PICLISTENBOOK_ORDER_LOTTERY(
            56,
            "点读机打包购买抽奖活动",
            0,
            "[]",
            0,
            DateUtils.stringToDate("2017-09-30 23:59:59"),
            "2017-08-21 00:00:00",
            "2017-09-30 23:59:59"
    ),
    TEACHER_TERM_BEGIN_LOTTERY_2017_AUTUMN(
            57,
            "教师2017秋季开学抽奖活动",
            0,
            "[]",
            0,
            DateUtils.stringToDate("2017-09-30 23:59:59"),
            "2017-09-01 00:00:00",
            "2099-03-20 23:59:59"),
    AFENTI_21DAYS_LOTTERY(
            58,
            "阿分题英语21天学习活动抽奖",
            0,
            "[]",
            0,
            DateUtils.stringToDate("2017-12-04 23:59:59"),
            "2017-11-14 00:00:00",
            "2017-12-04 23:59:59"
    ),
    LIVECAST_1212_ACTIVITY(
            59,
            "直播双十二9.9抽奖活动",
            0,
            "[]",
            0,
            DateUtils.stringToDate("2017-12-14 23:59:59"),
            "2017-12-04 00:00:00",  //为了测试，提前开始，没有关系
            "2017-12-14 23:59:59"
    ),
    VOCATION_HOMEWORK_LOTTERY_2017(
            60,
            "2017假期作业抽奖",
            0,
            "[]",
            0,
            DateUtils.stringToDate("2018-07-22 23:59:59"),
            "2018-06-13 00:00:00",
            "2018-07-22 23:59:59"
    ), AFENTIMATH_WINTER_LOTTERY(
            61,
            "阿分题数学寒假练题大赛活动抽奖",
            0,
            "[]",
            0,
            DateUtils.stringToDate("2018-02-28 23:59:59"),
            "2018-01-15 00:00:00",
            "2018-02-28 23:59:59"
    ), TERM_BEGINS_ACTIVITY(
            62,
            "开学活动抽奖",
            0,
            "[]",
            0,
            DateUtils.stringToDate("2018-03-25 23:59:59"),
            "2018-03-05 00:00:00",
            "2018-03-25 23:59:59"
    ), STUDENT_APP_LOTTERY(
            63,
            "学生APP奖品中心抽奖",
            0,
            "[]",
            0,
            DateUtils.stringToDate("2020-03-27 23:59:59"),
            "2018-02-26 00:00:00",
            "2020-03-27 23:59:59"
    ), JUNIOR_ARRANGE_HOMEWORK_LOTTERY(
            64,
            "中学开学活动布置作业抽奖",
            0,
            "[]",
            0,
            DateUtils.stringToDate("2018-04-08 23:59:59"),
            "2018-03-12 00:00:00",
            "2018-04-08 23:59:59"
    ),
    SUMMER_VOCATION_LOTTERY_2018(
            65,
            "2018暑期作业抽奖",
            0,
            "[]",
            0,
            DateUtils.stringToDate("2018-07-22 23:59:59"),
            "2018-06-13 00:00:00",
            "2018-07-22 23:59:59"
    );

    private static Map<Integer, CampaignType> campaignTypeMap;

    static {
        campaignTypeMap = new LinkedHashMap<>();
        for (CampaignType campaignType : values()) {
            campaignTypeMap.put(campaignType.getId(), campaignType);
        }
    }

    @Getter
    private final int id;
    @Getter
    private final String name;
    @Getter
    private final int awardCount;
    @Getter
    private final String award;
    @Getter
    private final Integer lotteryTimes;
    @Getter
    private final Date expiredTime;
    @Getter
    private final String awardStartTime;
    @Getter
    private final String awardEndTime;

    public static CampaignType of(Integer campaignId) {
        return campaignTypeMap.get(campaignId);
    }

    private static Date stringToDate(String source) {
        return DateFormatParser.getInstance().parse(source, "yyyy-MM-dd HH:mm:ss");
    }

    public boolean isInAwardPeriod() {
        Date date = new Date();
        return date.before(expiredTime) && date.after(stringToDate(awardStartTime));
    }

}
