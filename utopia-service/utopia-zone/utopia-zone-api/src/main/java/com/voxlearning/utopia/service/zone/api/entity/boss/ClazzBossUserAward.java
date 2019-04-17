package com.voxlearning.utopia.service.zone.api.entity.boss;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author : kai.sun
 * @version : 2018-11-05
 * @description :
 **/

@Getter
@Setter
public class ClazzBossUserAward implements Serializable {

    private static final long serialVersionUID = 7993922252524150692L;

    private String clazzBossAwardId; //对应的奖励id

    private Boolean receive; //奖励是否领取

}
