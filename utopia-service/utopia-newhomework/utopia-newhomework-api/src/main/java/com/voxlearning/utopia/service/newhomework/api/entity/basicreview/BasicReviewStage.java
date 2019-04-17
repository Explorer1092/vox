package com.voxlearning.utopia.service.newhomework.api.entity.basicreview;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author guoqiang.li
 * @since 2017/11/8
 */
@Getter
@Setter
public class BasicReviewStage implements Serializable {

    private static final long serialVersionUID = 5452093949615890883L;


    private Integer stageId;        // 关卡id
    private String stageName;       // 关卡名
    private String homeworkId;      // 关联的作业id
}
