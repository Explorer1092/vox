package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 对话交互的结果
 * @author songtao
 * @since 2018/5/23
 */
@Getter
@Setter
public class AITalkSceneResult implements Serializable {
    private static final long serialVersionUID = -1449098979155107435L;
    private boolean user;
    private String qid;
    private String result;
}
