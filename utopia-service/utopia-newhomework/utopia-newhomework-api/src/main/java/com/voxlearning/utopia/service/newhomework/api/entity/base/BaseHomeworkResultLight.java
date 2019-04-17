package com.voxlearning.utopia.service.newhomework.api.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * @author xuesong.zhang
 * @since 2017/1/11
 */
@Getter
@Setter
public class BaseHomeworkResultLight implements Serializable {

    private static final long serialVersionUID = -4164209136921103249L;

    public String homeworkId;
    public SchoolLevel schoolLevel;
    public Subject subject;                                                        // 学科
    public String actionId;                                                        // 在批量布置的时候一定要保持这个id一致,拼接方法:"teacherId_${批量布置时间点}"
    public Long clazzGroupId;                                                      // 班组id，有问题问长远
    public Long userId;                                                            // 用户id，根据大作业的趋势，以后做题的会变成各种角色
    public Date finishAt;
    public String comment;                                                         // 作业评语
    public String audioComment;                                                    // 作业音频评语url
    public Integer rewardIntegral;                                                 // 奖励学豆
    public Integer integral;                                                       // 完成作业奖励学豆
    public Integer energy;                                                         // 完成作业奖励能量
    public Integer credit;                                                         // 完成作业奖励学分
    public Date userStartAt;                                                       // 开始做作业的时间

    public LinkedHashMap<ObjectiveConfigType, BaseHomeworkResultAnswer> practices;  // 做作业的内容<作业形式, <试题id, 作业明细id(homework_process的id)>>

    @JsonIgnore
    public boolean isFinished() {
        return finishAt != null;
    }

    @JsonIgnore
    public boolean isFinishedOfObjectiveConfigType(ObjectiveConfigType objectiveConfigType) {
        return practices != null && practices.containsKey(objectiveConfigType) && practices.get(objectiveConfigType).isFinished();
    }
}
