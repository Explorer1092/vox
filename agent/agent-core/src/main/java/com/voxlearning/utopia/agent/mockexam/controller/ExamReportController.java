package com.voxlearning.utopia.agent.mockexam.controller;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.mockexam.service.ExamReferenceService;
import com.voxlearning.utopia.agent.mockexam.service.dto.Result;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ReportUrlDto;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * 测评报告 url 下载列表
 *
 * @Author: peng.zhang
 * @Date: 2018/8/17
 */
@RequestMapping("mockexam/report")
public class ExamReportController {

    @Inject
    private ExamReferenceService examReferenceService;

    /**
     * 根据登录人所属区域、测评Id，返回下载列表
     *
     * @param regionCode 区域编码
     * @param planId     测评id
     * @return 测评报告 url 下载列表
     */
    @RequestMapping(value = "reports.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage queryReport(@RequestParam(name = "regionCode") String regionCode,
                                  @RequestParam(name = "planId") Long planId) {
        Result<ReportUrlDto> dtoResult = examReferenceService.reportUrl(regionCode, planId);
        return ViewBuilder.fetch(dtoResult);
    }
}
