package com.voxlearning.utopia.admin.data;

import lombok.Data;

import java.util.Date;

/**
 * @author xuanzhu
 * 班级问卷信息
 */
@Data
public class ClazzCrmOralTestPojo {
    private Long id;                 //用户id
    private String name;             //名字
    private String testDay;            //口测日期
    private String testRegion;   //口测时间段
    private Date updateTime;     //填写时间
}
