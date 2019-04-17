package com.voxlearning.utopia.agent.mockexam.integration.support;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.GET;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.map.HashedMap;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.integration.AbstractHttpClient;
import com.voxlearning.utopia.agent.mockexam.integration.ExamReportClient;
import com.voxlearning.utopia.agent.mockexam.integration.StringUtil;
import com.voxlearning.utopia.agent.mockexam.service.dto.ErrorCode;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamReportQueryParams;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author: peng.zhang
 * @Date: 2018/9/28
 */
@Service
public class ExamReportClientImpl extends AbstractHttpClient implements ExamReportClient {

    @Override
    public ReportResponse queryReportExistInfo(ExamReportQueryParams examParams){
        ReportResponse reportResponse;
        try {
            Map<String,Object> paramsMap = new HashedMap<>();
            if (StringUtils.isNotBlank( examParams.getPaperId())){
                paramsMap.put("paperId",examParams.getPaperId());
                paramsMap.put("moduleName","loadItemAnalysisStruct");
                paramsMap.put("reportType","itemReport");
            } else {
                if (null != examParams.getExamId()){
                    paramsMap.put("examId",examParams.getExamId());
                }
                if (StringUtils.isNotBlank( examParams.getRegionLevel())){
                    paramsMap.put("regionLevel",examParams.getRegionLevel());
                }
                if (StringUtils.isNotBlank( examParams.getRegionCode())){
                    paramsMap.put("regionCode",examParams.getRegionCode());
                }
                paramsMap.put("moduleName","loadEvaluationStruct");
                paramsMap.put("reportType","assessmentReport");
            }
            String url = "";
            if (RuntimeMode.ge(Mode.STAGING) || RuntimeMode.ge(Mode.PRODUCTION)) {
                url = UrlUtils.buildUrlQuery("http://yqc.17zuoye.net/api/v1/checkReportData", paramsMap);
            } else {
                url = UrlUtils.buildUrlQuery("http://10.7.4.240:8116/api/v1/checkReportData", paramsMap);
            }
            GET get = HttpRequestExecutor.defaultInstance().get(url);
            AlpsHttpResponse _response = get.execute();
            int statusCode = _response.getStatusCode();
            if (-1 == statusCode) {
                throw new BusinessException(ErrorCode.EXAM_REPORT, String.format("[%s]服务超时", Service.EXAM_REPORT.desc));
            } else {
                HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
                switch (httpStatus) {
                    case OK: {
                        String responseText = StringUtil.unicodeToString(_response.getResponseString());
                        reportResponse = JSON.parseObject(responseText, ReportResponse.class);
                        break;
                    }
                    default: {
                        throw new BusinessException(ErrorCode.EXAM_REPORT,
                                String.format("[%s]服务错误,status = %s", Service.EXAM_REPORT.desc, httpStatus));
                    }
                }
            }
        } catch (Exception e){
            throw new BusinessException(ErrorCode.EXAM_REPORT, e);
        }
        return reportResponse;
    }
}
