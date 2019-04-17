package com.voxlearning.washington.data.requestParam;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 评价信息参数
 *
 * @Author: peng.zhang
 * @Date: 2018/10/12
 */
@Data
public class EvaluationRequestParam implements Serializable {

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
}
