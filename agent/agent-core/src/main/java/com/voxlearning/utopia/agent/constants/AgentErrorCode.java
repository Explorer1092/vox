package com.voxlearning.utopia.agent.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by Yuechen.Wang on 2016-07-26
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentErrorCode {
    AUTH_FAILED("100001", "登录状态失效，请重新登录", "/mobile/index.vpage"),

    // 我的页面相关错误代码
    NO_PERMISSION_TO_USE("200101", "没有查看权限", "/mobile/my/index.vpage"),

    // 计划相关错误代码
    AGENT_PLAN_ERROR("300101", "计划加载错误", ""),

    // 资源相关错误代码
    SCHOOL_RESOURCE_INFO_ERROR("400101", "学校资源加载异常", "/mobile/resource/school/list.vpage"),
    TEACHER_RESOURCE_INFO_ERROR("400201", "老师资源加载异常", "/mobile/resource/teacher/list.vpage"),
    TEACHER_CLAZZ_APPLY_ERROR("400203", "包班申请加载异常", "/mobile/resource/teacher/list.vpage"),
    SCHOOL_INFO_DETAIL_ERROR("400102", "学校详情页加载异常", "/mobile/resource/school/list.vpage"),

    // 工作记录相关错误代码
    INTO_SCHOOL_RECORD_ERROR("500101", "进校记录加载异常", ""),
    VISIT_RECORD_ERROR("500201", "拜访记录加载异常", ""),
    VISIT_RECORD_ERROR_1("500202", "拜访记录加载异常", "/mobile/notice/index.vpage"),
    VISIT_RECORD_LIST_ERROR("500202", "查询陪访的部门不存在", "/mobile/team/index.vpage"),
    MEETING_RECORD_REGION_ERROR("500203","所选择的组会地区不存在",""),

    // 团队相关错误
    AGENT_TEAM_ERROR("600101", "团队加载异常", ""),
    AGENT_TEAM_CITY_MANAGE_ERROR("600102", "团队市经理数据加载异常", "/mobile/team/index.vpage"),
    // 消息中心相关错误代码
    NOTIFY_TYPE_ERROR("700101", "消息类型异常", "/mobile/notice/index.vpage"),
    NOTIFY_CONTENT_ERROR("700102", "消息内容无法找到", ""),

    DATA_PACKET_ERROR("800101", "访问资料包异常", "/mobile/my/index.vpage"),

    // 我的信息异常
    MY_PAGE_ERROR("900101", "我的页无法找到用户信息", ""),

    // 教研员信息
    RESEARCHERS_ERROR("1000101", "教研员信息无法找到", ""),
    // 学校信息
    SCHOOL_INFO_UPDATE_ERROR("1100101", "学校信息编辑时未找到学校信息", "/mobile/resource/school/list.vpage"),
    SCHOOL_BASIC_INFO_ERROR("1100102", "学校基础信息编辑时未找到学校信息", "/mobile/resource/school/list.vpage"),
    SCHOOL_DETAIL_INFO_ERROR("1100103", "查询学校信息时未找到学校信息", "/mobile/resource/school/list.vpage"),
    SCHOOL_UPDATE_INFO_ERROR("1100104", "编辑学校信息时未找到学校信息", "/mobile/resource/school/list.vpage"),
    CLIENT_UPDATE_ERROR("4644444", "客户端下载出现异常", "/mobile/auth/login.vpage"),
    // 进校统计
    INTO_SCHOOL_GROUP_ERROR("5271822", "用户部门未找到", ""),
    INTO_SCHOOL_ROLE_ERROR("5271824", "进校查看未达标专员角色错误", ""),
    INTO_SCHOOL_NO_REACH_ERROR("5271824", "进校未达标专员时间错误", ""),
    NO_AUTH_AND_BACK("6271824", "暂无权限", "");


    @Getter private final String code;
    @Getter private final String desc;
    @Getter private final String returnUrl;

}
