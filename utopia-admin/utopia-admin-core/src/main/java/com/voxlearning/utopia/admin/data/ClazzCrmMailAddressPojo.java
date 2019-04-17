package com.voxlearning.utopia.admin.data;

import com.voxlearning.alps.annotation.dao.DocumentField;
import lombok.Data;

import java.util.Date;

/**
 * @author xuanzhu
 * 班级问卷信息
 */
@Data
public class ClazzCrmMailAddressPojo {
    private Long id;                 //用户id
    private String name;             //名字
    private String recipientName; //收货人姓名
    @DocumentField(value = "recipient_tel")
    private String recipientTel; //收货人电话
    @DocumentField(value = "recipient_addr")
    private String recipientAddr; //收货人地址
    @DocumentField(value = "course_level")
    private String courseLevel;//后续课程级别
    private Date updateTime;     //填写时间
}
