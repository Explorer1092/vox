package com.voxlearning.utopia.agent.mockexam.integration.support;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.GET;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.integration.AbstractHttpClient;
import com.voxlearning.utopia.agent.mockexam.integration.MiddleSchoolExamPaperClient;
import com.voxlearning.utopia.agent.mockexam.integration.StringUtil;
import com.voxlearning.utopia.agent.mockexam.service.dto.ErrorCode;
import org.springframework.http.HttpStatus;

import javax.inject.Named;
import java.util.Map;

import static com.voxlearning.utopia.agent.mockexam.integration.AbstractHttpClient.Service.MIDDLE_SCHOOL_EXAM_PAPER;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2019-03-18 17:44
 **/
@Named
public class MiddleSchoolExamPaperClientImpl extends AbstractHttpClient implements MiddleSchoolExamPaperClient{
    @Override
    public PaperPageResponse queryPage(PaperRequest request) {
        PaperPageResponse paperResponse;
        try {
            POST post = build(MIDDLE_SCHOOL_EXAM_PAPER);
            post.contentType("application/x-www-form-urlencoded");
            Map<Object, Object> parameters = PaperRequest.Builder.build(request);
            post.addParameter(parameters);
            AlpsHttpResponse response = post.execute();
            int status = response.getStatusCode();
            if (-1 == status) {
                throw new BusinessException(ErrorCode.PAPER_QUERY_ERROR, String.format("[%s]服务超时", AbstractHttpClient.Service.MIDDLE_SCHOOL_EXAM_PAPER.desc));
            } else {
                HttpStatus httpStatus = HttpStatus.valueOf(status);
                switch (httpStatus) {
                    case OK: {
                        String responseText = StringUtil.unicodeToString(response.getResponseString());
                        paperResponse = JSON.parseObject(responseText,PaperPageResponse.class);
                        if (!paperResponse.isSuccess()) {
                            throw new BusinessException(ErrorCode.PAPER_QUERY_ERROR,
                                    String.format("[%s]服务错误,response = %s", AbstractHttpClient.Service.MIDDLE_SCHOOL_EXAM_PAPER.desc, paperResponse));
                        }
                        break;
                    }
                    default: {
                        throw new BusinessException(ErrorCode.PAPER_QUERY_ERROR,
                                String.format("[%s]服务错误,status = %s", AbstractHttpClient.Service.MIDDLE_SCHOOL_EXAM_PAPER.desc, httpStatus));
                    }
                }
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PAPER_QUERY_ERROR, e.getMessage());
        }
        return paperResponse;
    }

    @Override
    public PaperSearchItemResponse querySearchItems() {
        PaperSearchItemResponse searchItemResponse;
        try {
            GET get = buildGet(Service.MIDDLE_SCHOOL_PAPER_SEARCH_ITEM);
            get.contentType("application/x-www-form-urlencoded");
            AlpsHttpResponse response = get.execute();
            int status = response.getStatusCode();
            if (-1 == status) {
                throw new BusinessException(ErrorCode.MIDDLESCHOOL_SEARCHITEM_NOT_EXIST_ERROR, String.format("[%s]服务超时", AbstractHttpClient.Service.MIDDLE_SCHOOL_PAPER_SEARCH_ITEM.desc));
            } else {
                HttpStatus httpStatus = HttpStatus.valueOf(status);
                switch (httpStatus) {
                    case OK: {
                        String responseText = StringUtil.unicodeToString(response.getResponseString());
                        searchItemResponse = JSON.parseObject(responseText,PaperSearchItemResponse.class);
                        break;
                    }
                    default: {
                        throw new BusinessException(ErrorCode.MIDDLESCHOOL_SEARCHITEM_NOT_EXIST_ERROR,
                                String.format("[%s]服务错误,status = %s", AbstractHttpClient.Service.MIDDLE_SCHOOL_PAPER_SEARCH_ITEM.desc, httpStatus));
                    }
                }
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PAPER_QUERY_ERROR, e.getMessage());
        }
        return searchItemResponse;
    }
}
