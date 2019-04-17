package com.voxlearning.utopia.service.zone.api.entity.boss;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author : kai.sun
 * @version : 2018-11-05
 * @description : 班级boss 学生个人达标记录
 **/

@Getter
@Setter
public class ClazzBossSelfRecord implements Serializable {

    public static final int TYPE_FIRST = 1,TYPE_SECOND = 2,TYPE_THIRD = 3;

    public static final double TYPE_FIRST_RATIO = 0.6,TYPE_SECOND_RATIO = 0.8,TYPE_THIRD_RATIO = 1;

    private static final long serialVersionUID = -555678308995188723L;

    private Integer type; //不同类型 （对应不同奖励 1 2 3）

    private Integer currentProgress; //个人进度

    private Integer selfTarget; //个人目标（总）进度

    private Boolean receive; //是否领取

    private Long schoolProviderNum; //学校达标数

    private List<StudentInfo> stuList; //学生列表

    private List<AwardDetail> awards; //奖励

}
