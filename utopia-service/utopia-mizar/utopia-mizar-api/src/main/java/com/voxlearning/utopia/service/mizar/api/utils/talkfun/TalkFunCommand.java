package com.voxlearning.utopia.service.mizar.api.utils.talkfun;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 欢拓云直播 命令集合
 *
 * @author yuechen.wang
 * @date 2017/01/09
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TalkFunCommand {

    //-----------------------------------------------
    //-----------        课程管理            --------
    //-----------------------------------------------
    ADD_COURSE("course.add", "新增课程"),
    UPDATE_COURSE("course.update", "更新课程"),
    DELETE_COURSE("course.delete", "删除课程"),
    COURSE_LIVE("course.access", "进入课程"),
    COURSE_REPLAY("course.access.playback", "课程回放"),
    COURSE_LAUNCH("course.launch", "获取直播器启动协议"),

    COURSE_STOP("live.stop", "课程回放"),
    REPLAY_DONE("live.playback", "直播回放生成"),

    //-----------------------------------------------
    //-----------        主播管理            --------
    //-----------------------------------------------
    ADD_TEACHER("course.zhubo.add", "新增老师"),
    UPDATE_TEACHER("course.zhubo.update", "更新老师"),

    //-----------------------------------------------
    //-----------        报表数据            --------
    //-----------------------------------------------
    VISITOR_LIVE("course.visitor.list", "课程直播访客列表"),
    VISITOR_REPLAY("course.visitor.playback", "课程回放访客列表"),
    ;

    private final String cmd;
    private final String desc;

}
