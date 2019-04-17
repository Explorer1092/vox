package com.voxlearning.utopia.admin.viewdata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 反馈列表视图
 * Created by yaguang.wang on 2017/2/27.
 */
@Getter
@Setter
@NoArgsConstructor
public class FeedbackListView {
    private String id;
    private String feedbackDate;
    private String feedbackPeople;
    private String feedbackPeopleId;
    private String subject;
    private String type;
    private String content;
    private Long teacherId;
    private String teacherName;
    private String teacherTelephone;
    private String threeCategory;
    private String status;
    private String pmData;
    private String onlineData;          // 预计上线时间
    private Boolean online;
    private Long workflowId;
    private Boolean callback;
    private String pic1Url;
    private String pic2Url;
    private String pic3Url;
    private String pic4Url;
    private String pic5Url;

    //以下为导出内容
    private String userPlatform;        // 来源 crm/天玑
    private String firstCategory;       // 一级分类
    private String secondCategory;      // 二级分类
    private String thirdCategory;
    private String regionName;          // 大区
    private String cityName;            // 部门
    private String soOpinion;           // 销运意见
    private String pmOpinion;           // 产品意见

    private String bookName; // 教材名称
    private String bookGrade; // 教材对应的年级
    private String bookUnit; // 教材单元
    private String bookCoveredArea; // 教材覆盖的地区
    private Integer bookCoveredStudentCount; // 教材覆盖的学生数
    private List<Long> relationIds = new ArrayList<>();
}
