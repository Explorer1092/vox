package com.voxlearning.utopia.service.newexam.api.entity;

import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据上报，博文在用，有事找他
 * 注：这个数据为上报数据，所以不需要缓存
 * @Description:
 * @author: Mr_VanGogh
 * @date: 2019/4/10 下午4:41
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-journal")
@DocumentDatabase(database = "vox-journal")
@DocumentCollection(collection = "student_homework_{}", dynamic = true)
@DocumentRangeable(range = DateRangeType.M)
public class JournalStudentNewExam implements Serializable {
    private static final long serialVersionUID = 8167791460343837487L;

    @DocumentId
    private String id;
    private String homeworkId;
    private String schoolLevel;
    //private NewHomeworkType type;       // 作业类型，数学有类题作业
    //private HomeworkTag homeworkTag;
    private String actionId;
    private Long teacherId;
    private Long studentId;
    private Integer subjectId;
    private Long groupId;
    private Date homeworkCreateAt;
    private Date homeworkFinishAt;
    private Date studentStartAt;        // 学生开始作业时间
    private Date homeworkStartAt;       // 作业有效开始时间
    private Date homeworkEndAt;         // 作业有效结束时间
    private Double avgScore;            // 作业平均分，参考值
    private Long duration;              // 作业总时长（单位：毫秒），参考值
    private String ip;
    private Boolean repair;
    private Integer star;               // 星级（自主练习特有属性）
    private Integer rightRate;          // 正确率（自主练习特有属性）
    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;

    //private LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices;

    private String env;
}
