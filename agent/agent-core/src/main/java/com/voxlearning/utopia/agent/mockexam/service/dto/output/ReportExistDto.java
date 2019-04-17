package com.voxlearning.utopia.agent.mockexam.service.dto.output;

import com.voxlearning.utopia.agent.mockexam.integration.ExamReportClient;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: peng.zhang
 * @Date: 2018/9/28
 */
@Data
public class ReportExistDto implements Serializable {

    /**
     * 报告是否存在
     */
    private Boolean isExist;

    /**
     * 信息
     */
    private String info;

    public static ReportExistDto build(ExamReportClient.ReportResponse response){
        ReportExistDto dto = new ReportExistDto();
        if (null != response.getSuccess()){
            dto.setIsExist("true".equals(response.getSuccess()) ? Boolean.TRUE : Boolean.FALSE);
            dto.setInfo(response.getMsg());
        }
        return dto;
    }
}
