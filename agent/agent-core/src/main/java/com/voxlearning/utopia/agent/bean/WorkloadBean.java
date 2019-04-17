package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Jia HuanYin
 * @since 2015/7/30
 */
@Getter
@Setter
@NoArgsConstructor
public class WorkloadBean implements Serializable {
    private static final long serialVersionUID = -1477495443396822518L;

    private int dayDone;
    private int dayRemain;
    private int monthDone;
    private int monthRate;
}
