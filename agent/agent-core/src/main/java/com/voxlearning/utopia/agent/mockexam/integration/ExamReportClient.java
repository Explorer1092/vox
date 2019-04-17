package com.voxlearning.utopia.agent.mockexam.integration;

import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamReportQueryParams;
import lombok.Data;

import java.io.Serializable;

/**
 * 测评报告客户端
 *
 * @Author: peng.zhang
 * @Date: 2018/9/28
 */
public interface ExamReportClient {

    /**
     * 查询报告是否存在
     * @param params 查询参数
     * @return 响应
     */
    ReportResponse queryReportExistInfo(ExamReportQueryParams params);

    /**
     * 报告响应
     */
    @Data
    class ReportResponse implements Serializable{
        private String success;
        private String msg;
    }
}
