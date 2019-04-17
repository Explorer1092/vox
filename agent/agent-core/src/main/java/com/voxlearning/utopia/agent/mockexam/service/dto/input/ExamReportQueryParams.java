package com.voxlearning.utopia.agent.mockexam.service.dto.input;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: peng.zhang
 * @Date: 2018/9/28
 */
@Data
public class ExamReportQueryParams implements Serializable {

    private String examId;
    private String moduleName;
    private String reportType;
    private String paperId;
    private String regionLevel;
    private String regionCode;
}
