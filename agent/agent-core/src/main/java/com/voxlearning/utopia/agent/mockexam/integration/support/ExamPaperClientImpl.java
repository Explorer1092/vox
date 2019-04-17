package com.voxlearning.utopia.agent.mockexam.integration.support;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPlan;
import com.voxlearning.utopia.agent.mockexam.integration.AbstractHttpClient;
import com.voxlearning.utopia.agent.mockexam.integration.ExamPaperClient;
import com.voxlearning.utopia.agent.mockexam.integration.StringUtil;
import com.voxlearning.utopia.agent.mockexam.service.dto.ErrorCode;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.agent.mockexam.integration.AbstractHttpClient.Service.PAPER_QUERY;

/**
 * 试卷客户端实现
 *
 * @author xiaolei.li
 * @version 2018/8/4
 */
@Service
public class ExamPaperClientImpl extends AbstractHttpClient implements ExamPaperClient {

    @Override
    public CreateResponse create(CreateRequest request) {
        CreateResponse response;
        try {
            POST post = build(Service.PAPER_CREATE);
            post.contentType("application/json");
            post.json(request);
            AlpsHttpResponse _response = post.execute();
            int statusCode = _response.getStatusCode();
            if (-1 == statusCode) {
                throw new BusinessException(ErrorCode.PAPER_CREATE, "创建试卷失败,发生超时");
            } else {
                HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
                switch (httpStatus) {
                    case OK: {
                        String responseText = StringUtil.unicodeToString(_response.getResponseString());
                        response = JSON.parseObject(responseText, CreateResponse.class);
                        break;
                    }
                    default: {
                        throw new BusinessException(ErrorCode.PAPER_CREATE,
                                String.format("%s服务错误,status = %s", Service.PAPER_CREATE.desc, httpStatus));
                    }
                }
            }
        } catch (Exception e) {
            response = CreateResponse.error(e);
        }
        return response;
    }

    @Override
    public CheckResponse check(ExamPlan plan) {
        CheckResponse response;
        CheckRequest request = CheckRequest.Builder.build(plan);
        try {
            POST post = build(Service.PAPER_CHECK);
            post.contentType("application/json");
            post.json(request);
            AlpsHttpResponse _response = post.execute();
            int statusCode = _response.getStatusCode();
            if (-1 == statusCode) {
                throw new BusinessException(ErrorCode.PAPER_CHECK, String.format("[%s]服务超时", Service.PAPER_CHECK.desc));
            } else {
                HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
                switch (httpStatus) {
                    case OK: {
                        String responseText = StringUtil.unicodeToString(_response.getResponseString());
                        response = JSON.parseObject(responseText, CheckResponse.class);
                        break;
                    }
                    default: {
                        throw new BusinessException(ErrorCode.PAPER_CHECK,
                                String.format("[%s]服务错误,status = %s", Service.PAPER_CHECK.desc, httpStatus));
                    }
                }
            }
        } catch (Exception e) {
            response = CheckResponse.error(request, e);
        }
        return response;
    }

    @Override
    public PaperPageResponse queryPage(PaperRequest request) {
        PaperPageResponse paperResponse;
        try {
            POST post = build(PAPER_QUERY);
            post.contentType("application/x-www-form-urlencoded");
            Map<Object, Object> parameters = PaperRequest.Builder.build(request);
            post.addParameter(parameters);
            AlpsHttpResponse response = post.execute();
            int status = response.getStatusCode();
            if (-1 == status) {
                throw new BusinessException(ErrorCode.PAPER_QUERY_ERROR, String.format("[%s]服务超时", Service.PAPER_QUERY.desc));
            } else {
                HttpStatus httpStatus = HttpStatus.valueOf(status);
                switch (httpStatus) {
                    case OK: {
                        String responseText = StringUtil.unicodeToString(response.getResponseString());
                        paperResponse = JSON.parseObject(responseText, PaperPageResponse.class);
                        if (!paperResponse.isSuccess()) {
                            throw new BusinessException(ErrorCode.PAPER_QUERY_ERROR,
                                    String.format("[%s]服务错误,response = %s", Service.PAPER_QUERY.desc, paperResponse));
                        }
                        break;
                    }
                    default: {
                        throw new BusinessException(ErrorCode.PAPER_QUERY_ERROR,
                                String.format("[%s]服务错误,status = %s", Service.PAPER_QUERY.desc, httpStatus));
                    }
                }
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PAPER_QUERY_ERROR, e.getMessage());
        }
        return paperResponse;
    }

    @Override
    public Map<String, PaperInfo> queryByIds(ExamPlanEnums.Subject subject, List<String> paperIds) {
        Map<String, PaperInfo> map = Maps.newHashMap();
        for (String paperId : paperIds) {
            PaperRequest request = new PaperRequest();
            request.setPaperId(paperId);
            request.setSubject(subject);
            request.setPage(1);
            PaperPageResponse response = queryPage(request);
            List<PaperInfo> items = response.getItems();
            if (null != items && !items.isEmpty()) {
                map.put(paperId, items.get(0));
            } else {
                map.put(paperId, null);
            }
        }
        return map;
    }
}
