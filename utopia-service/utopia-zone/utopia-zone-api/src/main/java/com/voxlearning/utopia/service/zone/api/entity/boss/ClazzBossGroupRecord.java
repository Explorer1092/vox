package com.voxlearning.utopia.service.zone.api.entity.boss;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author : kai.sun
 * @version : 2018-11-05
 * @description : 班级boss 班级维度记录
 **/

@Getter
@Setter
public class ClazzBossGroupRecord implements Serializable {

    private static final long serialVersionUID = -2844529460169828224L;

    private Integer type; //不同类型 （对应不同奖励 1 2 3）

    private Integer currentProgress; //班级当前贡献(Cache 查询)

    private Integer clazzTarget; //班级目标（总）进度 动态计算得出

    private Integer participateNum; //当前已参加人数(查询activity表)

    private Integer clazzStudentNum; //班级总人数(请求)

    private Integer selfProviderNum; //个人贡献(Cache)

    private Boolean receive; //是否领取

    private List<AwardDetail> awards;

}

