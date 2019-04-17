package com.voxlearning.utopia.agent.mockexam.service.dto;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * 错误码
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@AllArgsConstructor
public enum ErrorCode {

    MESSAGE_NOTIFY_ERROR("210", "发送消息异常"),

    FILE_UPLOAD_ERROR("310", "上传文件发生异常"),

    PAPER_CREATE("500", "创建试卷时发生错误"),
    PAPER_CHECK("501", "试卷约束性检查错误"),
    PAPER_OPEN_CLOSE("501", "试卷开发与关闭错误"),
    PLAN_CONSTRAINT("502", "违反模型约束"),
    PLAN_RETRIEVE("503", "获取计划详单错误"),
    PLAN_SUBMIT("504", "提交业务错误"),
    PLAN_PAPER_NOT_MATCH_SUBJECT("505", "当前学科下没有以下试卷"),
    PLAN_PAPER_UNKNOWN_STATUS("506", "未知状态"),

    PAPER_NOTIFY_STATE("510", "试卷状态通知发生异常,错误的试卷状态"),
    PAPER_NOTIFY_PAPER_LIST_EMPTY("520", "试卷状态通知发生异常,试卷列表为空"),

    EXAM_CREATE_ASSEMBLE_REQUEST("659", "申请创建考试发生异常"),
    EXAM_CREATE("660", "申请创建考试发生异常"),
    EXAM_MAKEUP("661", "补考发生异常"),
    EXAM_REPLENISH("662", "重考发生异常"),
    EXAM_ONLINE("663", "考试上线发生了异常"),
    EXAM_OFFLINE("664", "考试上线发生了异常"),
    EXAM_SCORE("665", "查询成绩发生了异常"),
    EXAM_COUNT_STUDENT("666", "查询参考学生人数发生了异常"),
    EXAM_REPORT("667","查询报告是否存在发生异常"),
    EXAM_UPDATE("668","更新考务信息发生异常"),
    EXAM_WITHDRAW("669","撤销考试发生异常"),
    EXAM_CREATE_SCORE_PUBLISHTIME("670", "“成绩发布时间”不能修改为当前天及以前的时间"),
    EXAM_CREATE_PAPER_NULL("671", "申请创建考试关联试卷为空"),
    EXAM_CREATE_BOOK_INVALID("672", "申请创建考试校验所属材料无效"),
    EXAM_CREATE_PARTTYPE_INVALID("673", "所关联试卷中无口语题，请将“测评类型”更改为非口语，或更换试卷"),
    EXAM_CREATE_TOTALSCORE_INVALID("674", "您所关联的多个试卷总分分别为“%s”，分数不一致，请重新关联"),

    PLAN_PAPER_DOC("700", "计划关联的试卷文档错误"),

    REFERENCE_REGION("700", "应用类型[区域]出现了错误"),
    REFERENCE_SCHOOL("701", "应用类型[学校]出现了错误"),
    REFERENCE_BOOK("702", "应用类型[教材]出现了错误"),


    PAPER_QUERY_ERROR("810", "查询试卷发生异常"),
    PAPER_NOT_EXIST_ERROR("820", "查询试卷发生异常"),
    MIDDLESCHOOL_SEARCHITEM_NOT_EXIST_ERROR("830", "搜索备选项信息查询发生异常"),

    UNKNOWN("999", "未处理的异常");
    public final String code;
    public final String desc;

    public static ErrorCode of(String code) {
        return Arrays.stream(values()).filter(i -> i.code.equals(code)).findFirst().orElse(UNKNOWN);
    }
}
