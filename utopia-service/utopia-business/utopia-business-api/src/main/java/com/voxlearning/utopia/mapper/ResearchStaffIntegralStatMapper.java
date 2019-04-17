package com.voxlearning.utopia.mapper;

import lombok.Data;

import java.io.Serializable;

/**
 * 教研员金币统计
 *
 * @author Maofeng Lu
 * @since 13-8-18 下午1:32
 */
@Data
public class ResearchStaffIntegralStatMapper implements Serializable {

    private static final long serialVersionUID = 9184138603516024729L;
    private String paperId;
    private String paperName;
    private String bookName;
    //发布状态  unopened:未发布，opening：开放
    private String releaseState;
    //试卷创建时间
    private String createDate;
    //布置教研员试卷的认证老师人数
    private int authTeacherCntHasExamPaper;
    //完成学生人数
    private int completeStudentCnt;
    //金币数量
    private int integralCount;
    //是否显示通知按钮
    private boolean displayBtnFlag;
    //试卷发送全区通知状态 true :已发送 false : 未发送
    private boolean notifyRegion;
}
