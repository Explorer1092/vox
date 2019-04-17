package com.voxlearning.utopia.service.zone.api.entity.boss;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author dongfeng.xue
 * @date 2018-11-06
 */
@Setter@Getter
public class RewordResponse implements Serializable {
    private static final long serialVersionUID = -208886623490733002L;
    private Integer type; // 1个人，2 班级
    private Integer count;//消灭错题数
    private List<AwardDetail> awards;
    private String clazzBossAwardId; //对应的奖励id
}
