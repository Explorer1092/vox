package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 *
 *
 * @author song.wang
 * @date 2016/7/26
 */
@Getter
@Setter
public class VisitSchoolResultDetailData {
    private Long schoolId;
    private String schoolName; // 学校名称
    private Integer schoolSize; // 学校规模
    private Integer day;  // 进校日期

    private SchoolLevel schoolLevel; // 学校级别

    private Integer visitSchoolMonthCount = 0; // 本月进校次数
    private List<Integer> visitDayList; // 本月进校日期列表

    private Integer preStuRegNum = 0; // 拜访前注册数
    private Integer preStuAuthNum = 0;// 拜访前认证数
    private Integer preSascData = 0; // 拜访前单活
    private Integer preDascData = 0;// 拜访前双活

    private Long addStuRegNum = 0L; // 注册增长
    private Long addStuAuthNum = 0L; // 认证增长
    private Integer addSascData = 0;  // 单活增长
    private Integer addDascData = 0;  // 双活增长

    private Integer forecastStuRegNum = 0; // 预测注册数据
    private Integer forecastStuAuthNum = 0; // 预测认证数据
    private Long forecastSascData = 0L; // 预测单活数据
    private Long forecastDascData = 0L; // 预测双活数据

    private List<TeacherInfo> teacherInfoList;  //未使用的英语老师

    @Getter
    @Setter
    public class TeacherInfo{
        private Long teacherId;
        private String teacherName;
        private Boolean usedFlg;
    }


}


