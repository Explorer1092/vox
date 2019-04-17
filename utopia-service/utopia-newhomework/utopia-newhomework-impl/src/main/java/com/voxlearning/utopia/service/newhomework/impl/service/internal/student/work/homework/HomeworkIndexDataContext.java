package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework;

import com.voxlearning.utopia.service.newhomework.api.context.AbstractContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * @author guohong.tan
 * @since 2017/6/29
 */
@Getter
@Setter
public class HomeworkIndexDataContext extends AbstractContext<HomeworkIndexDataContext> {
    private static final long serialVersionUID = 3374030915544164673L;

    private String homeworkId;
    private Long studentId;

    private NewHomework newHomework = null;
    private String unitName = "";
    private NewHomeworkResult newHomeworkResult = null;
    private NewAccomplishment newAccomplishment = null;
    private User teacher = null;
    private LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> doPractices = new LinkedHashMap<>();
    private Map<ObjectiveConfigType, NewHomeworkPracticeContent> practiceMap = new LinkedHashMap<>();
    private Boolean isCurrentDayFinished = false;
    private Integer undoPracticesCount = 0;
    private Boolean finished = false;
    private Boolean needFinish = false;
    /*
     * 总进度=已完成题目总数/作业题目总数
     * 基础训练每个应用算一道题
     * 绘本的话一个绘本算一个进度单位
     * 主观作业，一个题算一个单位
     * 纸质口算: 一个作业形式算一道题
     */
    private Integer totalQuestionCount = 0;
    private Integer doTotalQuestionCount = 0;
    private List<Map<String, Object>> practiceInfos = new ArrayList<>();

    private Map<String, Object> result = new HashMap<>();
}
