package com.voxlearning.utopia.agent.persist.entity.task;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 *维护老师导出数据
 *
 * @author deliang.che
 * @create 2018-05-29
 **/
@Getter
@Setter
public class AgentTaskSubBaseExportData implements Serializable {
    private String operatorName;    //执行人
    private String marketingName;   //市场部
    private String regionName;      //大区
    private String areaName;        //区域
    private String cityName;        //分区
    private Long schoolId;          //学校ID
    private String schoolName;      //学校名称
    private Long teacherId;         //老师ID
    private String teacherName;     //老师姓名
    private String subject;         //科目
    private String province;        //省
    private String city;            //市
    private String county;          //区
    private Boolean isHomework;      //任务期间是否布置作业

}
