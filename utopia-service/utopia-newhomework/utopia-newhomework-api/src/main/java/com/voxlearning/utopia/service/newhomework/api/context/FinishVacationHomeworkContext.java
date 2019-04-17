package com.voxlearning.utopia.service.newhomework.api.context;

import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import lombok.Getter;
import lombok.Setter;

/**
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Getter
@Setter
public class FinishVacationHomeworkContext extends AbstractContext<FinishVacationHomeworkContext> {
    private static final long serialVersionUID = 3875274977900431028L;

    // in
    private Long userId; // 用户ID
    private User user; // 用户
    private Long clazzId; // 班级ID
    private Clazz clazz; // 班级
    private Long clazzGroupId; // 组ID
    private GroupMapper clazzGroup; // 组
    private String clientType;  // 客户端类型:pc,mobile
    private String clientName;  // 客户端名称:***app
    private String ipImei; // ip or imei
    private String vacationHomeworkId; // 假期作业ID
    private VacationHomework vacationHomework; // 假期作业
    private NewHomeworkType newHomeworkType; // 作业类型
    private ObjectiveConfigType objectiveConfigType; //作业类型
    private Boolean supplementaryData; //是否是修复数据（当线上系统崩溃了NewHomeworkResult的数据没有正常完成finishAt的更新）

    // middle
    private VacationHomeworkResult result; // 假期作业中间结果
    private boolean practiceFinished = false; // 当前类型是否全部完成
    private boolean homeworkFinished = false; // 当前作业是否全部完成
    private Double practiceScore = null; // 某个练习的分数，如果该练习类型是没有分数的，为null
    private Long practiceDuration; // 某个练习的耗时
}
