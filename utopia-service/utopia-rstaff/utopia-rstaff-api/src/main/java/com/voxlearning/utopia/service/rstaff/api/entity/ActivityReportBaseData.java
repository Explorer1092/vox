package com.voxlearning.utopia.service.rstaff.api.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.utopia.core.ObjectIdEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@DocumentTable(table = "VOX_ACTIVITY_REPORT_BASE_DATA")
@DocumentConnection(configName = "hs_misc")
public class ActivityReportBaseData  extends ObjectIdEntityWithDisabledField {

    private static final long serialVersionUID = -2356058130649234133L;

    private Integer provinceCode;
    private String provinceName;
    private Integer cityCode;
    private String cityName;
    private Integer regionCode;
    private String regionName;
    private Long schoolId;
    private String schoolName;
    private Integer clazzLevel;
    private Long clazzId;
    private String clazzName;           //班级名称
    private Long userId;                //学生ID
    private String userName;            //用户名称
    private String activityId;          //活动code
    private String activityType;        //活动类型
    private String playActivityDate;    //玩游戏的当天的时间
    private Long takeTimes;             //花费时间数，秒为单位
    private Integer score;               //单词活动分数
    private Integer exercises;          //单词活动做题数量
    private Integer isTopScore;        //最高分标志
    private Long skipCount;             //跳过次数
    private Long resetCount;            //重置次数
    private Date endTime;

}
