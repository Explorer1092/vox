package com.voxlearning.utopia.service.newhomework.api.context.livecast;

import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AbstractContext;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.third.ThirdPartyGroupMapper;
import lombok.Getter;
import lombok.Setter;

/**
 * @author xuesong.zhang
 * @since 2017/1/3
 */
@Getter
@Setter
public class FinishLiveCastHomeworkContext extends AbstractContext<FinishLiveCastHomeworkContext> {

    private static final long serialVersionUID = -8256215055882250592L;

    // in
    private Long userId; // 用户ID
    private User user; // 用户
    private Long clazzGroupId; // 组ID
    private ThirdPartyGroupMapper thirdPartyGroupMapper; // 第三方
    private String clientType;  // 客户端类型:pc,mobile
    private String clientName;  // 客户端名称:***app
    private String ipImei; // ip or imei
    private String homeworkId; // 作业ID
    private LiveCastHomework liveCastHomework; // 作业
    private NewHomeworkType newHomeworkType; // 作业类型
    private ObjectiveConfigType objectiveConfigType; //作业类型
    private Boolean supplementaryData; //是否是修复数据，LiveCast预留一下但是没用到

    // middle
    // private Long teacherId; // 教师ID
    // private Teacher teacher; // 教师
    private LiveCastHomeworkResult liveCastHomeworkResult; // 作业中间结果
    private boolean practiceFinished = false; // 当前类型是否全部完成
    private boolean homeworkFinished = false; // 当前作业是否全部完成
    private Double practiceScore = null; // 某个练习的分数，如果该练习类型是没有分数的，为null
    private Long practiceDureation; // 某个练习的耗时
}
