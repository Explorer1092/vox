package com.voxlearning.utopia.service.business.api.mapper;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.common.FieldValueSerializer;
import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.utopia.business.api.constant.TeachingResourceUserType;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@UtopiaCacheRevision("20180911")
public class TeachingResourceRaw implements Serializable, Cloneable {

    private static final long serialVersionUID = -1265676885288478120L;

    private String id;
    private String name;
    private String subject;
    private String category;
    private String task;
    private String grade;
    private String image;
    private String appImage;
    private Boolean featuring;
    private Integer displayOrder;
    private Boolean online;
    private Integer validityPeriod;
    private String fileUrl;
    private String fileType; // 文件扩展名
    private String subHead;
    private TeachingResourceUserType visitLimited;
    private TeachingResourceUserType receiveLimited;

    private Date createAt;
    private Date updateAt;

    @FieldValueSerializer(serializer = "com.voxlearning.alps.lang.mapper.json.StringDateSerializer")
    private Date onlineAt;

    private Long readCount;         // 阅读人数(新讲堂收费课程存兑换人数免费课程存阅读人数)
    private Long collectCount;      // 收藏人数

    private TeachingResource.Label label;
    private TeachingResource.WorkType workType;

    private String delCollectId; // 删除收藏信息时使用

    private String lecturerUserName;        // 一起新讲堂讲师
    private String lecturerIntroduction;    // 一起新讲堂讲师简介

    private Long participateNum;            // 每周活动的参与人数
    private Long finishNum;                 // 每周活动的完成人数

    private String taskStatus;              // 任务状态
    private Integer source;                 // 归属来源 0 教学助手 1 江西教学助手
    private Boolean isCourse;                // 是否是视频课程(不能从栏目上区分,特设此字段)

    public String getOnlineDate() {
        if (this.onlineAt == null) {
            return "";
        }
        return DateFormatUtils.format(this.onlineAt, "yyyy-MM-dd");
    }

    @Override
    public TeachingResourceRaw clone() {
        try {
            return (TeachingResourceRaw) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new UnsupportedOperationException();
        }
    }
}
