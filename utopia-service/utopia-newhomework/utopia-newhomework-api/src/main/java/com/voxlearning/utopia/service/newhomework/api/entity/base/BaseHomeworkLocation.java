package com.voxlearning.utopia.service.newhomework.api.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

/**
 * @author xuesong.zhang
 * @since 2017/1/10
 */
@Getter
@Setter
public class BaseHomeworkLocation implements Serializable {

    private static final long serialVersionUID = -7395719624103211191L;

    public NewHomeworkType type;                                           // 作业类型
    public HomeworkTag homeworkTag;                                        // 作业标签
    public String sourceHomeworkId;                                        // 原作业id，和类题作业类型成对出现
    public SchoolLevel schoolLevel;                                        // 猜猜
    public Subject subject;                                                // 学科
    public String actionId;                                                // 在批量布置的时候一定要保持这个id一致,拼接方法:"teacherId_${批量布置时间点}"
    public String title;                                                   // 作业名称
    public String des;                                                     // 预留，作业描述

    public Long teacherId;                                                 // 老师id，此处未来可能会变为fromUserId，布置作业的用户角色不会仅仅是老师
    public Long clazzGroupId;                                              // 班组id，有问题问长远

    public Long duration;                                                  // 标准时长（单位：秒）
    public String remark;                                                  // 备注
    public HomeworkSourceType source;                                      // 布置作业来源

    public Boolean checked;                                                // 是否检查
    public Date checkedAt;                                                 // 检查时间
    public HomeworkSourceType checkHomeworkSource;                         // 检查作业的端信息（大数据用）

    public Date startTime;                                                 // 作业起始时间
    public Date endTime;                                                   // 作业结束时间
    public Boolean disabled;                                               // 默认false，删除true
    public Boolean includeSubjective;                                      // 是否包含需要主观作答的试题
    public Boolean includeIntelligentTeaching;                             // 是否包含重点讲练测
    public Boolean remindCorrection;                                       // 是否已推荐巩固

    public Map<String, String> additions;                                  // 扩展字段

    @JsonIgnore
    public boolean isDisabledTrue() {
        return Boolean.TRUE.equals(disabled);
    }

    @JsonIgnore
    public NewHomeworkType getNewHomeworkType() {
        return type == null ? NewHomeworkType.Normal : type;
    }

    /**
     * 判断是否已检查
     */
    @JsonIgnore
    public boolean isHomeworkChecked() {
        return Boolean.TRUE.equals(checked);
    }

    /**
     * 判断是否已过期
     */
    @JsonIgnore
    public boolean isHomeworkTerminated() {
        return isHomeworkChecked() || System.currentTimeMillis() > endTime.getTime();
    }

    @JsonIgnore
    public List<String> getReportShareParts() {
        if (!isHomeworkChecked()) {
            return Collections.emptyList();
        }
        if (additions == null || additions.isEmpty()) {
            return Collections.emptyList();
        }
        String reportShareParts = additions.get("reportShareParts");
        if (reportShareParts == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(reportShareParts.split("_"));
    }
}
