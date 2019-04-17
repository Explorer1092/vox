package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTaskType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * @author guoqiang.li
 * @since 2017/4/14
 */
@Getter
@Setter
public class HomeworkTask {
    private Integer taskId;                         // 任务id
    private HomeworkTaskType homeworkTaskType;      // 任务类型
    private Date startTime;                         // 开始时间
    private Date endTime;                           // 结束时间
    private Integer integralCount;                  // 奖励园丁豆数量
    private String taskName;                        // 任务名称
    private List<Subject> taskSubjects;             // 任务对应的学科
    private String taskDescription;                 // 任务描述
    private List<String> taskRules;                 // 任务规则
    private String pcImgUrl;                        // pc端图片地址
    private String nativeImgUrl;                    // 原生图片地址
    private String h5ImgUrl;                        // h5图片地址
}
