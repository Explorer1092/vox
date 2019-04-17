package com.voxlearning.washington.mapper;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class AfentiReportData implements Serializable {
    private static final long serialVersionUID = -619869024642113567L;
    private Subject subject;//学科
    private int wrongNum;//错题数
    private String studentName;//学生名字
    private Long studentId;


    private int totalTarget;//学生目标
    private int needToImprove;//有待提高
    private int improved;//有提高空间
    private int unPractice;//未练习
    private String city;//市

    private String afentiDesc;//

    private List<Map<String, Object>> purchaseInfos = new LinkedList<>();

    private boolean bought;//是否购买
    private int dayToExpire;//剩余有效天数

    //阿芬提报告
    private int exercisesNum;//题数
    private int exercisesKnowledgeNum;//练习知识点
    private int totalExercisesKnowledgeNum;//累计练习知识点


}
