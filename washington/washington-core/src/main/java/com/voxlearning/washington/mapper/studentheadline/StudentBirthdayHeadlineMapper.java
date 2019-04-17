package com.voxlearning.washington.mapper.studentheadline;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/1/5
 * Time: 10:17
 * 生日头条
 */

@Getter
@Setter
public class StudentBirthdayHeadlineMapper extends StudentInteractiveHeadline {
    private static final long serialVersionUID = -7806151314171634713L;

    private String vId;            // 生日虚拟id 同动态id
    private Long relevantUserId;   // 过生日的同学
    private String headIcon;       // 头像
    private String headWearImg;    // 头饰

    // ============ 以上是过一段时间后可以清除的字段 2017-11-14 ==================

    private String title;          // 头条标题
    private String text;           // 头条内容
    private Boolean disabledBtn = Boolean.FALSE;   // 是否灰显祝福按钮 第三方视角
    private Boolean showBtn = Boolean.TRUE;        // 是否展示祝福按钮 当事人视角

    /**
     * 祝福列表
     * 学生id
     * 学生姓名
     * 学生头像
     * {"id":123,"name":"abc","headIcon":"http://images.17zuoye.com/user.icon"}
     */
    private List<Map<String, Object>> blessedList = new LinkedList<>();
}
