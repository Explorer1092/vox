package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.question.api.entity.ObjectiveConfig;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @Description: 作业:LoadContent入参
 * @author: Mr_VanGogh
 * @date: 2018/6/13 下午3:17
 */
@Data
public class NewHomeworkContentLoaderMapper implements Serializable {

    private static final long serialVersionUID = 7785492483024532866L;

    private TeacherDetail teacher;                  //教师信息
    private Set<Long> groupIds;                     //班级IDs
    private ObjectiveConfig objectiveConfig;        //教学目标配置包
    private List<String> sectionIds;                //
    private String unitId;                          //单元ID
    private String bookId;                          //教材ID
    private Integer currentPageNum;                 //当前页
    private HomeworkSourceType homeworkSourceType;  //作业布置来源
    private String sys;                             //
    private String appVersion;                      //APP版本号
    private boolean waterfall;                      //是否瀑布流方式
}
