package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTaskStatus;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTaskType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2017/4/17
 */
@Getter
@Setter
public class NewHomeworkCard implements Serializable {
    private static final long serialVersionUID = -5068102584532682591L;

    // 通用数据
    private HomeworkTaskType taskType;
    private String taskId;
    private String taskName;
    private Long teacherId;
    private Subject subject;
    private Integer integralCount;
    private HomeworkTaskStatus taskStatus;
    private String taskDescription;
    private String progress;
    private List<String> taskRules;
    private String pcImgUrl;
    private String h5ImgUrl;
    private String nativeImgUrl;

    // 详情数据
    private Map<String, Object> taskDetails;
}