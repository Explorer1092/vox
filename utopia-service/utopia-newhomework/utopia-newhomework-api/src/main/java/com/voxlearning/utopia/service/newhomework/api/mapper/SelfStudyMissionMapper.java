package com.voxlearning.utopia.service.newhomework.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 自学任务阿包要的东西
 *
 * @author xuesong.zhang
 * @since 2017/2/11
 */
@Getter
@Setter
public class SelfStudyMissionMapper implements Serializable {

    private static final long serialVersionUID = -8616561682919778791L;

    private String homeworkId;      // 作业id
    private Integer questionCount;  // 题目数量
    private Date endDate;           // 阿包要的一个结束时间，这个和作业结束时间不一样
}
