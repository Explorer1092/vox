package com.voxlearning.utopia.service.campaign.api.constant;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: peng.zhang
 * @Date: 2018/10/12
 */
@Data
public class EvaluationParam implements Serializable {
    /**
     * 星级
     */
    private Integer star;

    /**
     * 标签列表
     */
    private List<String> labelList;

    /**
     * 关键字
     */
    private String keyWord;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 课件ID
     */
    private String coursewareId;

    /**
     * 是否是认证用户
     */
    private String isAuthentication;
}
