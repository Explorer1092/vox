package com.voxlearning.utopia.service.campaign.api.mapper;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.calendar.DateUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 一起新讲堂课程
 */
@Getter
@Setter
@ToString
@UtopiaCacheRevision("20180918")
public class YiqiJTCourseMapper implements Serializable {
    private static final long serialVersionUID = -1265676885288478120L;

    private Long id;
    private String title;
    private String url;
    private Long price;
    private Date openTime;
    private String openTimeFormat;
    private boolean isOpen;
    private Integer activeTime;
    private Integer topNum;
    private Integer status;
    private Date createDatetime;
    private Date updateDatetime;
    private Long attendNum;
    private String lecturerUserName;
    private String lecturerIntroduction;
    private String videoPictureUrl;
    private String titlePictureUrl;
    private String appPictureUrl; // app 首页图
    private String textContent;
    private List<Integer> gradeList;
    private List<Integer> subjectList;
    private List<YiqiJTCourseCatalogMapper> cataloglist;
    private List<YiqiJTChoiceNoteMapper> choiceNoteList;
    private List<YiqiJTCourseOuterchainMapper> outerchainList;
    private Boolean featuring; // 是否首页展示
    private String label; // 标签
    private Long readCount;         // 阅读人数(已废弃且不维护,前端改为展示兑换次数)
    private Long collectCount;      // 收藏人数
    private Integer source;         // 归属来源 0 教学助手 1 江西教学助手
    private String category;        // 资源分类

    public String getOpenTimeFormat() {
        if (openTime == null) {
            return "";
        }
        return DateUtils.dateToString(openTime, "MM月dd日 HH:mm");
    }

    public String getOnlineTime() {
        if (openTime == null) {
            return "";
        }
        return DateUtils.dateToString(openTime, "yyyy-MM-dd");
    }
}
