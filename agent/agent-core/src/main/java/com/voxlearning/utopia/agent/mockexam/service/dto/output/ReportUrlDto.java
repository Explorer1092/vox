package com.voxlearning.utopia.agent.mockexam.service.dto.output;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: peng.zhang
 * @Date: 2018/8/17 15:10
 */
@Data
public class ReportUrlDto implements Serializable{

    /**
     * 报告下载地址
     */
    private String url;
}
