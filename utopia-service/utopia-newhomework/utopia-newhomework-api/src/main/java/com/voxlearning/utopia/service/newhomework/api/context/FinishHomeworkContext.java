package com.voxlearning.utopia.service.newhomework.api.context;

import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 处理大作业完成逻辑的context
 *
 * @author Ruib
 * @version 0.1
 * @since 2016/1/14
 */
@Getter
@Setter
public class FinishHomeworkContext extends AbstractContext<FinishHomeworkContext> {
    private static final long serialVersionUID = 98530676365118075L;

    // in
    private Long userId; // 用户ID
    private User user; // 用户
    private Long clazzGroupId; // 组ID
    private GroupMapper clazzGroup; // 组
    private String clientType;  // 客户端类型:pc,mobile
    private String clientName;  // 客户端名称:***app
    private String ipImei; // ip or imei
    private String homeworkId; // 作业ID
    private NewHomework homework; // 作业
    private NewHomeworkType newHomeworkType; // 作业类型
    private ObjectiveConfigType objectiveConfigType; //作业类型
    private Boolean supplementaryData; //是否是修复数据（当线上系统崩溃了NewHomeworkResult的数据没有正常完成finishAt的更新）
    private List<String> ocrMentalAnswerIds; // 纸质口算答题详情ids
    private List<String> ocrDictationAnswerIds; // 纸质听写答题详情ids

    // middle
    private Long teacherId; // 教师ID
    private Teacher teacher; // 教师
    private NewHomeworkResult result; // 作业中间结果
    private boolean practiceFinished = false; // 当前类型是否全部完成
    private boolean homeworkFinished = false; // 当前作业是否全部完成
    private Double practiceScore = null; // 某个练习的分数，如果该练习类型是没有分数的，为null
    private Long practiceDureation; // 某个练习的耗时
    private Boolean mentalArithmeticProgressReward = false; // 口算训练有过程奖，默认false
    private Integer ocrMentalQuestionCount; // 纸质口算识别出来的题目总数量
    private Integer ocrMentalCorrectQuestionCount; // 纸质口算识别正确的题目总数量
    private Integer ocrDictationQuestionCount; // 纸质听写识别出来的题目总数量
    private Integer ocrDictationCorrectQuestionCount; // 纸质听写识别正确的题目总数量
}
