package com.voxlearning.utopia.admin.entity;

import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author guangqing
 * @since 2018/7/17
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-madrid")
@DocumentDatabase(database = "vox-diagnosis")
@DocumentCollection(collection = "course_analysis_result")
public class CourseAnalysisResult implements Serializable{
    // 唯一id eq:  expGroupId + "_ + expId + "_" + courseId
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    //实验组Id
    private String expGroupId;
    //实验Id
    private String expId;
    //前侧题Id
    private String preQuestionIds;
    //课程Id
    private String courseId;
    //前测题做题人数 32-bit integer
    private Integer preQuestionDoNum;
    //前测题作对的人数 32-bit integer
    private Integer preQuestionRightNum;
    //前测题正确率
    private Double preQuestionRightRate;
    //课程命中数量 32-bit integer
    private Integer courseTargetNum;
    //课程命中率
    private Double courseTargetRate;
    //开始课程数量 32-bit integer
    private Integer courseBeginNum;
    //完成课程数量 32-bit integer
    private Integer courseFinishNum;
    //课程完成率
    private Double courseCompleteRate;
    //后测题Id
    private String postQuestionId;
    //后测题做题人数 32-bit integer
    private Integer postQuestionDoNum;
    //后测题完成率
    private Double postQuestionCompleteRate;
    //后侧题作对的人数 32-bit integer
    private Integer postQuestionRightNum;
    //后测题正确率
    private Double postQuestionRightRate;
    //开始统计时间
//    @DocumentCreateTimestamp
    private Date beginTime;
    //结束统计时间
//    @DocumentCreateTimestamp
    private Date endTime;

    @DocumentCreateTimestamp
    private Date createDate;

    @DocumentUpdateTimestamp
    private Date updateDate;

    private Boolean disabled;

    private String expName;

    private String courseName;
}
