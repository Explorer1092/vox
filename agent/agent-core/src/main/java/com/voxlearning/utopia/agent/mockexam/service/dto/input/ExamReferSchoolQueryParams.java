package com.voxlearning.utopia.agent.mockexam.service.dto.input;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 学校查询参数
 *
 * @Author: peng.zhang
 * @Date: 2018/8/14 17:18
 */
@Data
public class ExamReferSchoolQueryParams implements Serializable {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 隶属的区域编码
     */
    private List<Integer> cityCodes;

    /**
     * 学校ID
     */
    private List<Long> schoolIds;

}
