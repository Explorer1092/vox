package com.voxlearning.utopia.admin.data;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;


/**
 * 用于封装用户诊断，作业完成情况的pojo
 *
 * @author xuesong.zhang
 * @since 2015-6-18
 */
@Data
public class StudentHomeworkResultOptionCheck implements Serializable {
    private static final long serialVersionUID = -3493179091509686636L;

    private String id;
    //    private Map<HomeworkResultType, List<HomeworkResult>> resultDetail; // rds
//    private List<StudentHomeworkResultDetail> resultDetail; // rds
    //    private Map<HomeworkResultType, Integer> avgScore; // as
    /*
    *英语=(basiceAvgScore+readingAvgScore)/2*20% + examAvgScore*80%
    *数学=((calculateAvgScore+specialAvgScore)/2+examAvgScore)/2
    *语文=basicAvgScore
    */
    private Integer score;  //sc 得分
    private Boolean repair; //re 补做
    private Long finishTime; //ft 完成时长
    private Boolean finished; //fe 是否完成
    private Date createAt; //ca 创建时间
    private Date finishAt; // 单独属性，完成时间
    private Long userId; //ri 学生ID
    private Integer silver; // 银币
}
