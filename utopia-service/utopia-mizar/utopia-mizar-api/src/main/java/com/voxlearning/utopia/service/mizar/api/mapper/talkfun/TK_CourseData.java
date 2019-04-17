package com.voxlearning.utopia.service.mizar.api.mapper.talkfun;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 欢拓新增课程返回参数
 *
 * @author yuechen.wang 2017/01/10
 */
@Data
public class TK_CourseData implements Serializable {

    private static final Long serialVersionUID = 1559355259683496377L;

    @TkField("partner_id") private String partnerId;    // 合作方id
    @TkField("bid") private String bid;                 // 主播id
    @TkField("course_name") private String courseName;  // 课程名称
    @TkField("zhubo_key") private String zhuboKey;      // 主播登录秘钥
    @TkField("admin_key") private String adminKey;      // 助教登录秘钥
    @TkField("user_key") private String userKey;        // 学生登录秘钥
    @TkField(value = "add_time", timestamp = true) private Date addTime;       // 课程创建时间
    @TkField("course_id") private String courseId;      // 课程id
    @TkField(value = "start_time", timestamp = true) private Date startTime;   // 开始时间戳
    @TkField(value = "end_time", timestamp = true) private Date endTime;       // 结束时间戳
}
