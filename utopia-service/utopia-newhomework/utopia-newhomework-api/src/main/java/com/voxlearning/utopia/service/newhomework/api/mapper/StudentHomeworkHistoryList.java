package com.voxlearning.utopia.service.newhomework.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 学生历史列表页
 *
 * @author xuesong.zhang
 * @since 2016-03-21
 */
@Getter
@Setter
public class StudentHomeworkHistoryList implements Serializable {

    private static final long serialVersionUID = -6557951919507799773L;

    private Integer totalHomeworkCount; // 总作业次数
    private Integer finishHomeworkCount;// 完成作业次数

    private List<HomeworkHistoryMapper> homeworkList; // 作业历史


}
