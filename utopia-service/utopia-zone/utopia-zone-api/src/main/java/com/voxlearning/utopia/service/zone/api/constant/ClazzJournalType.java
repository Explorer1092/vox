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

package com.voxlearning.utopia.service.zone.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory.*;

/**
 * 班级动态类型
 * FIXME: 注意：description不要随便修改，用于班级动态的内容显示，如果修改，班级动态内容将会显示不正常
 *
 * @author RuiBao
 * @version 0.1
 * @since 13-8-7
 */

/* =================================================
 * 注意
 *
 * 如果没有特殊的需求，不需要给枚举加个什么INT类型的值。
 * 这种做法就是多余。枚举本身就是为了解决这类问题而产生的。
 *
 * 枚举的名字一旦确定，未来即便发现错误也请尽量不要修改。
 * 尽量不要删除已有的枚举定义。
 *
 * (1) 如果需要增加新的枚举，请随意。
 * (2) 尽量不要修改和删除现有的枚举，如果发现有错误，可以
 *     添加新的枚举值，将错误的标记为@Deprecated
 * ================================================= */

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ClazzJournalType {

    UNKOWN(0, "未知", MISC, 15),
    @Deprecated TEACHER_ASSIGN_HOMEWORK(1, "布置", MISC, 15),
    @Deprecated TEACHER_ADJUST_HOMEWORK(2, "调整", MISC, 15),
    @Deprecated TEACHER_DELETE_HOMEWORK(3, "删除", MISC, 15),
    @Deprecated STUDENT_FINISH_HOMEWORK(4, "完成", MISC, 15),
    @Deprecated STUDENT_MAKEUP_HOMEWORK(5, "补做", MISC, 15),
    @Deprecated STUDENT_FINISH_AFENTI(6, "完成阿分题练习", APPLICATION, 15),
    STUDENT_BUY_AFENTI(7, "购买阿分题", APPLICATION, 15),
    STUDENT_AFENTI_BUY_PK_PRODUCT(8, "阿分题购买PK套装", APPLICATION, 15),
    @Deprecated STUDENT_AFENTI_ARENA_CLAZZ_RANK(9, "阿分题竞技场班级周排行", APPLICATION, 15),
    @Deprecated STUDENT_AFENTI_ARENA_BOOK_RANK(10, "阿分题竞技场全国周排行", APPLICATION, 15),
    @Deprecated STUDENT_FINISH_ADVENTURE_ISLAND(11, "完成冒险岛练习", APPLICATION, 15),
    SEND_GIFT(12, "赠送礼物", MISC, 15),
    AFENTI_SECKILL(13, "阿分题秒杀", APPLICATION, 15),
    CLAZZ_MOST_FAVORITE(14, "班级最赞", MISC, 15),
    CLAZZ_WEALTHIEST(15, "班级土豪", MISC, 15),
    CHANGE_IMG(16, "更换头像", MISC, 15),
    CHANGE_BUBBLE(17, "更换气泡", MISC, 15),
    BIRTHDAY(18, "过生日", MISC, 15),
    STUDY_MASTER(19, "学霸", MISC, 15),
    SIGN_IN(20, "签到", MISC, 15),
    CLAZZ_LEAK(21, "班级爆料", MISC, 15),
    @Deprecated TALENT_PARKOUR_WEEK_RANK(22, "单词达人跑酷模式周排行", APPLICATION, 15),
    @Deprecated TALENT_PARKOUR_OPEN_WORDS(23, "单词达人跑酷模式开通单词包", APPLICATION, 15),
    @Deprecated TALENT_PARKOUR_MEDAL_UPLEVEL(24, "单词达人跑酷模式勋章升级", APPLICATION, 15),
    @Deprecated TALENT_PARKOUR_UPLEVEL(25, "单词达人跑酷模式等级升级", APPLICATION, 15),
    STUDENT_UPLOAD_PHOTO(26, "学生上传照片", MISC, 15),
    @Deprecated TALENT_PARKOUR_RESULT_SHARE(27, "单词达人跑酷模式游戏结果分享", APPLICATION, 15),
    @Deprecated TALENT_PARKOUR_RANKUP_SHARE(28, "单词达人跑酷模式排行上升分享", APPLICATION, 15),
    EXCHANGE_COUPON_LELANGLEDU(29, "学生兑换乐朗乐读优惠劵", APPLICATION, 15),
    APP_SHARE(30, "第三方应用班级空间分享", APPLICATION, 15),
    BABEL_WIN_PRIZE(31, "通天塔打倒boss获得道具奖励", APPLICATION, 15),
    SYSTEM_NOTICE(32, "系统通知", MISC, 15),
    SMARTCLAZZ_REWARD_SUMMARY_NOTICE(33, "智慧教室每天奖励汇总通知", MISC, 15),
    SMARTCLAZZ_REWARD_WEEKLY_RANK(34, "智慧教室奖励周排行", MISC, 15),
    STUDENT_BUY_TRAVEL_AMERICA(35, "购买走遍美国", APPLICATION, 15),
    WALKER_ADVENTURE(36, "沃克单词冒险之奇幻探险", MISC, 15),
    CLAZZ_LEVEL_NOTICE(37, "班级等级通知", MISC, 15),
    @Deprecated CAMPAIGN_SHARE(38, "平台活动分享", MISC, 15),
    TEACHER_ARRANGE_VH(39, "教师布置假期作业", MISC, 15),
    TEACHER_DELETE_VH(40, "教师删除假期作业", MISC, 15),
    GROUPON_INVITE(41, "团购邀请", MISC, 15),
    SMARTCLAZZ_STUDENT_REWARD(42, "智慧教室奖励学生学豆", MISC, 15),
    SANGUO_SHARE(43, "进击的三国班级空间分享", MISC, 15),
    BEST_TINY_GROUP(44, "最佳小组", MISC, 15),
    CREATE_TINY_GROUP(45, "创建小组，设立小组长", MISC, 15),
    RESET_TINY_GROUP_LEADER(46, "变更小组长", MISC, 15),
    SEND_BIRTHDAY_GIFT(47, "赠送生日礼物", MISC, 15),
    LEARNING_CYCLE_T1(48, "家长APP学习圈文本带链接动态", LEARNING_CYCLE, 15),
    LEARNING_CYCLE_T2(49, "家长APP学习圈图文文本带链接动态", LEARNING_CYCLE, 15),
    LEARNING_CYCLE_T3(50, "家长APP学习圈图文图片带链接动态", LEARNING_CYCLE, 15),
    SIGN_HEADLINE(51, "学生APP签到头条", APPLICATION_STD, 15),
    HOMEWORK_HEADLINE(52, "学生APP作业头条", APPLICATION_STD, 15),
    ACHIEVEMENT_HEADLINE(53, "学生APP成就头条", APPLICATION_STD, 15),
    CLAZZ_ACHIEVEMENT_HEADLINE(54, "学生APP班级成就头条", APPLICATION_STD, 15),
    BIRTHDAY_HEADLINE(55, "学生生日头条", APPLICATION_STD, 15),
    ACHIEVEMENT_SHARE_HEADLINE(56, "学生APP分享成就头条", APPLICATION_STD, 15),
    RESCUE_ANIMAL(57, "拯救小动物", APPLICATION_STD, 15),
    @Deprecated GROWN_WORD_NEW_PET(58, "成长世界获得新伙伴", APPLICATION_STD, 15),
    @Deprecated GROWN_WORD_PET_LEVEL_UP(59, "成长世界伙伴升级", APPLICATION_STD, 15),
    @Deprecated CLASS_BOSS_CHALLENGE_RANK(60, "BOSS活动班级贡献榜", APPLICATION_STD, 15),
    @Deprecated COMPETITION_ISLAND_LEVEL_UP(61, "竞技岛晋升段位", APPLICATION_STD, 15),
    @Deprecated COMPETITION_ISLAND_SEASON_CLASS_TOP3(62, "竞技岛赛季班级榜前三名", APPLICATION_STD, 15),
    @Deprecated WONDERLAND_NEW_MEDAL(63, "成长世界获得勋章", APPLICATION_STD, 15),
    @Deprecated WONDERLAND_MEDAL_GRADE(64, "成长世界达到某勋章等级", APPLICATION_STD, 15),
    @Deprecated NORMAL_CLASS_COMPETITION_INVITE_MATE(65, "班级竞技邀请好友动态", APPLICATION_STD, 15),
    @Deprecated RECESSIVE_CLASS_COMPETITION_INVITE_MATE(66, "班级竞技劣势邀请好友动态", APPLICATION_STD, 15),
    @Deprecated ADVENTURE_ISLAND_INVITE_CLASSMATE(67, "冒险岛邀请好友动态", APPLICATION_STD, 15),
    AFENTI_NEW_MEDAL(68, "阿分题获得勋章", APPLICATION_STD, 15),
    AFENTI_MEDAL_LEVEL_UP(69, "阿分题达到某勋章等级", APPLICATION_STD, 15),
    AFENTI_RANK_SHARE(70, "阿分题分享排行榜", APPLICATION_STD, 15),
    @Deprecated DREAM_CAREER_UPGRADE(71, "梦想职业晋升", APPLICATION_STD, 15),
    @Deprecated HOMELAND_SHARE_POSTCARD(72, "家园分享明信片", APPLICATION_STD, 15),
    IMAGE_TEXT_SHARE(73, "图文分享(可跳转)", APPLICATION_STD, 15),
    AFENTI_SPACECRAFT_UP(74, "阿分题分享动态到班级空间", APPLICATION_STD, 15),
    AFENTI_COMMON_TEXT(75, "阿分题分享到班级空间图文通用版", APPLICATION_STD, 15);

    @Getter
    private final int id;
    @Getter
    private final String description;
    @Getter
    private final ClazzJournalCategory category;
    @Getter
    private final int duration;               // 有效天数, 默认是15天

    private static final Map<Integer, ClazzJournalType> map;

    static {
        map = Arrays.stream(values()).collect(Collectors.toMap(ClazzJournalType::getId, Function.identity()));
    }

    public static ClazzJournalType safeParse(Integer id) {
        return map.getOrDefault(id, UNKOWN);
    }

}
