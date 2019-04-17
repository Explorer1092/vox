package com.voxlearning.utopia.admin.data;

import lombok.Data;

import java.util.Date;

/**
 * @author xuanzhu
 * 班级问卷信息
 */
@Data
public class ClazzCrmQuestionnairePojo {
    private Long id;                 //用户id
    private String name;             //名字
    private String grade;            //年级，问卷第 1 题
    private String studyDuration;   //学习年限，问卷第 2 题
    @Deprecated
    private String interest;         //兴趣，问卷第 3 题
    @Deprecated
    private String mentor;           //谁辅导孩子，问卷第 4 题
    private String weekPoints;       //英语薄弱点，问卷第 5 题
    private String otherExtraRegistration; //其他课外报名，对应问卷第 6 题
    private String recentlyScore;    //最近成绩，对应问卷第 7 题
    private String expect;         //对薯条的期待，新问卷第 5 题
    private Date updateTime;
}
