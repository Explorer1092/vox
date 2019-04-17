package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 学生历史列表页-作业列表
 *
 * @author xuesong.zhang
 * @since 2016-03-21
 */
@Getter
@Setter
public class HomeworkHistoryMapper implements Serializable {

    private static final long serialVersionUID = 5961120330189072372L;

    private String homeworkId;                          // 作业id
    private Subject subject;                            // 学科
    private Date createDate;                            // 作业布置时间
    private String title;                               // 作业标题

    private List<HomeworkHistoryBookMapper> books;      // 教材信息

    // 作业的三种状态
    // 1.开始作业2，学生并没有做完 && 未检查 && 在可做范围内
    // 2.补做作业1，学生并没有做完 && (作业检查完||作业过期)
    // 3.查看作业0，学生做完
    // 4.禁止作业3，为完成 && 创建时间早于ALLOW_UPDATE_HOMEWORK_START_TIME
    private Integer homeworkStatus;                     // 作业状态
    private Boolean isRepair;                           // 是否需要补做
    private List<HomeworkCommentMapper> comments;       // 评论
    private Date finishAt;                              // 作业完成时间

    private Date startTime;                             // 作业允许做的开始时间，对应newhomework中的startTime
    private Date endTime;                               // 作业允许做的结束时间，对应newhomework中的endTime
    private Integer totalNeedCorrectedNum;                   // 需要订正数目
    private Integer finishCorrectedCount;            //已经订正的数目


}
