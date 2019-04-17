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

package com.voxlearning.utopia.service.action.api.event;

import lombok.Getter;

/**
 * Action event type definitions.
 * http://wiki.17zuoye.net/pages/viewpage.action?pageId=25593568
 *
 * @author Xiaohai Zhang
 * @since Aug 3, 2016
 */
public enum ActionEventType {

    /**
     * 成功唤醒同学
     */
    WakeupClassmate(0, "唤醒同学"),
    /**
     * 完成自学(自学天数)
     */
    FinishSelfLearning(0, "完成自学"),
    /**
     * 获得星星
     */
    ObtainStar(0, "获得星星"),
    /**
     * 改正错题
     */
    CorrectWrongIssue(0, "改正错题"),
    /**
     * 订正作业
     */
    CorrectHomework(0, "订正作业"),
    /**
     * PK胜利
     */
    WinPk(0, "PK胜利"),
    /**
     * 完成作业
     */
    FinishHomework(2, "完成作业"),
    /**
     * 作业成绩90分以上
     */
    HomeworkScore90(0, "作业成绩90分以上"),
    /**
     * 完成口算
     */
    FinishMental(0, "完成口算"),
    /**
     * 完成口语
     */
    FinishOral(0, "完成口语"),
    /**
     * 完成绘本
     */
    FinishReading(0, "完成绘本"),
    /**
     * 点赞
     */
    Like(0, "点赞"),
    /**
     * 成长等级奖励领取
     */
    ReceiveGrowthLevelReward(0, "领取成长等级奖励"),
    /**
     * 查看作业报告
     */
    LookHomeworkReport(2, "查看作业报告"),
    /**
     * afenti英语 提交答案
     */
    SubmitAfentiEnglishAnswer(2, "提交阿分题英语答案"),
    /**
     * afenti数学 提交答案
     */
    SubmitAfentiMathAnswer(2, "提交阿分题数学答案"),
    SubmitAfentiChineseAnswer(2, "提交阿分题语文答案"),
    /**
     * 走遍美国 提交答案
     */
    SubmitZoumeiAnswer(2, "提交走遍美国答案"),
    /**
     * 家长端 家长端-英语随身听-播放/开始播放
     */
    StartSelfStudyEnglishWalkman(2, "播放英语随身听"),
    /**
     * 家长端-英语课本点读机-点读/连读
     */
    ClickSelfStudyEnglishPicListen(2, "使用英语课本点读机"),
    /**
     * 家长端-语文课文朗读-录音并保存
     */
    SaveSelfStudyChineseTextRead(2, "使用语文课文朗读"),
    /**
     * 签到
     */
    ClazzAttendance(0, "学生班级签到"),
    /**
     * 班级新鲜事评论
     */
    HeadlineComment(0, "班级新鲜事评论"),
    StudentUserLevelUpgrade(0,"学生等级升级")
    ;

    /**
     * 单次成长值
     */
    @Getter
    private final Integer delta;
    @Getter
    private final String title;


    ActionEventType(Integer delta, String title) {
        this.delta = delta;
        this.title = title;
    }
}
