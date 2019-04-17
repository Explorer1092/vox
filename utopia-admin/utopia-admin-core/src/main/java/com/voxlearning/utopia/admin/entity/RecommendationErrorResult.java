package com.voxlearning.utopia.admin.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xuerui.zhang
 * @since 2019/1/17 下午1:28
 */
@Data
public class RecommendationErrorResult implements Serializable {

    private static final long serialVersionUID = 1052092011544838972L;

    private Integer row;
    private String message;

}
