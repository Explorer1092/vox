package com.voxlearning.utopia.service.campaign.api.mapper;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class ClazzMoralPerformance implements Serializable {

    private static final long serialVersionUID = -7238470982508055208L;

    private boolean previous;
    //下一天是否还有德育评价
    private boolean next;

    //private boolean previous;

    //教师ID
    private Long tid;

    private String tName;

    //德育标签文案
    private String moralDesc;

    private List<StudentMoral> list;

    @Data
    public static class StudentMoral implements Serializable {
        private static final long serialVersionUID = 6418305103923898920L;

        private Integer tagId;
        //德育勋章名称
        private String tagName;

        //德育勋章icon
        private String tagIcon;

        //学生id
        private Long sid;

        //学生头像
        private String sphoto;

        private String sname;

        private Integer count;

    }

    @Data
    public static class StudentMedal implements Serializable {
        private static final long serialVersionUID = 3358316935647077935L;

        private Date date;

        private Long sid;

        private Integer medalId;

        private Integer count;
    }


}
