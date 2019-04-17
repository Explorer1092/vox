package com.voxlearning.utopia.agent.bean.exam;

import com.voxlearning.athena.bean.bigexam.AutoApplyStatistic;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author chunlin.yu
 * @create 2018-04-19 22:25
 **/
@Getter
@Setter
public class AgentAutoApplyStatistic implements Serializable{
    private static final long serialVersionUID = 8801539847492206741L;

    private AutoApplyStatistic autoApplyStatistic;

}
