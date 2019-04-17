package com.voxlearning.utopia.agent.mockexam.integration.support;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPlan;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamStudentScore;
import com.voxlearning.utopia.agent.mockexam.integration.AbstractHttpClient;
import com.voxlearning.utopia.agent.mockexam.integration.ExamClient;
import com.voxlearning.utopia.agent.mockexam.integration.StringUtil;
import com.voxlearning.utopia.agent.mockexam.service.dto.ErrorCode;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamScoreQueryParams;
import com.voxlearning.utopia.service.newexam.api.DPNewExamService;
import com.voxlearning.utopia.service.newexam.api.loader.NewExamRegistrationLoader;
import com.voxlearning.utopia.service.newexam.api.mapper.NewExamRegistrationLoaderMapper;
import org.apache.http.Consts;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 考试客户端实现
 *
 * @author xiaolei.li
 * @version 2018/8/6
 */
@Service
public class ExamClientImpl extends AbstractHttpClient implements ExamClient {

    @ImportService(interfaceClass = DPNewExamService.class)
    DPNewExamService examService;

    @ImportService(interfaceClass = NewExamRegistrationLoader.class)
    NewExamRegistrationLoader scoreClient;

    @Resource
    ApplyRequest.Builder builder;

    @Override
    public ApplyResponse apply(ExamPlan plan) {
        ApplyResponse _response;
        try {
            POST post = build(Service.EXAM_APPLY);
            post.contentType("application/x-www-form-urlencoded");
            ApplyRequest request = builder.build(plan);
            Map<Object, Object> parameters = ApplyRequest.Builder.build(request);
            post.addParameter(parameters);
            AlpsHttpResponse response = post.execute();
            int statusCode = response.getStatusCode();
            if (-1 == statusCode) {
                throw new BusinessException(ErrorCode.EXAM_CREATE,
                        String.format("[%s]服务超时", Service.EXAM_APPLY.desc));
            } else {
                HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
                switch (httpStatus) {
                    case OK: {
                        String responseText = StringUtil.unicodeToString(response.getResponseString(Consts.UTF_8));
                        _response = JSON.parseObject(responseText, ApplyResponse.class);
                        if (!_response.isSuccess()) {
                            throw new BusinessException(ErrorCode.EXAM_CREATE,
                                    StringUtils.isNotBlank(_response.getInfo()) ? _response.getInfo() : Service.EXAM_APPLY.desc + "发生错误");
                        }
                        break;
                    }
                    default: {
                        throw new BusinessException(ErrorCode.EXAM_CREATE,
                                String.format("[%s]服务错误,status = %s", Service.EXAM_APPLY, httpStatus));
                    }
                }
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EXAM_CREATE, e);
        }
        return _response;
    }

    @Override
    public OnlineResponse online(OnlineRequest request) {
        OnlineResponse _response;
        try {
            POST post = build(Service.EXAM_ONLINE);
            post.contentType("application/json");
            post.json(request);
            AlpsHttpResponse response = post.execute();
            int statusCode = response.getStatusCode();
            if (-1 == statusCode) {
                throw new BusinessException(ErrorCode.EXAM_ONLINE,
                        String.format("[%s]服务超时", Service.EXAM_ONLINE.desc));
            } else {
                HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
                switch (httpStatus) {
                    case OK: {
                        String responseText = StringUtil.unicodeToString(response.getResponseString());
                        _response = JSON.parseObject(responseText, OnlineResponse.class);
                        if (!_response.isSuccess()) {
                            throw new BusinessException(ErrorCode.EXAM_ONLINE,
                                    StringUtils.isNotBlank(_response.getInfo()) ? _response.getInfo() : Service.EXAM_ONLINE.desc + "发生错误");
                        }
                        break;
                    }
                    default: {
                        throw new BusinessException(ErrorCode.EXAM_ONLINE,
                                String.format("[%s]服务错误,status = %s", Service.EXAM_ONLINE, httpStatus));
                    }
                }
            }

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EXAM_ONLINE, e);
        }
        return _response;
    }

    @Override
    public OfflineResponse offline(OfflineRequest request) {
        OfflineResponse _response;
        try {
            POST post = build(Service.EXAM_OFFLINE);
            post.contentType("application/json");
            post.json(request);
            AlpsHttpResponse response = post.execute();
            int statusCode = response.getStatusCode();
            if (-1 == statusCode) {
                throw new BusinessException(ErrorCode.EXAM_OFFLINE,
                        String.format("[%s]服务超时", Service.EXAM_OFFLINE.desc));
            } else {
                HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
                switch (httpStatus) {
                    case OK: {
                        String responseText = StringUtil.unicodeToString(response.getResponseString());
                        _response = JSON.parseObject(responseText, OfflineResponse.class);
                        if (!_response.isSuccess()) {
                            throw new BusinessException(ErrorCode.EXAM_OFFLINE,
                                    StringUtils.isNotBlank(_response.getErrorMessage()) ? _response.getErrorMessage() : Service.EXAM_OFFLINE.desc + "发生错误");
                        }
                        break;
                    }
                    default: {
                        throw new BusinessException(ErrorCode.EXAM_OFFLINE,
                                String.format("[%s]服务错误,status = %s", Service.EXAM_OFFLINE, httpStatus));
                    }
                }
            }

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EXAM_OFFLINE, e);
        }
        return _response;
    }

    @Override
    public WithdrawResponse withdraw(WithdrawRequest request) {
        WithdrawResponse _response;
        try {
            POST post = build(Service.EXAM_WITHDRAW);
            post.contentType("application/json");
            post.json(request);
            AlpsHttpResponse response = post.execute();
            int statusCode = response.getStatusCode();
            if (-1 == statusCode) {
                throw new BusinessException(ErrorCode.EXAM_WITHDRAW,
                        String.format("[%s]服务超时", Service.EXAM_WITHDRAW.desc));
            } else {
                HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
                switch (httpStatus) {
                    case OK: {
                        String responseText = StringUtil.unicodeToString(response.getResponseString());
                        _response = JSON.parseObject(responseText, WithdrawResponse.class);
                        if (!_response.isSuccess()) {
                            throw new BusinessException(ErrorCode.EXAM_WITHDRAW,
                                    StringUtils.isNotBlank(_response.getInfo()) ? _response.getInfo() : Service.EXAM_WITHDRAW.desc + "发生错误");
                        }
                        break;
                    }
                    default: {
                        throw new BusinessException(ErrorCode.EXAM_WITHDRAW,
                                String.format("[%s]服务错误,status = %s", Service.EXAM_WITHDRAW, httpStatus));
                    }
                }
            }

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EXAM_OFFLINE, e);
        }
        return _response;
    }

    @Override
    public Map<Long, String> retry(RetryRequest request) {
        try {
            Map<Long, String> response = Maps.newHashMap();
            request.getStudentIds().forEach(i -> {
                MapMessage message = examService.handlerStudentExaminationAuthorityV2(
                        i, request.getExamId(), request.getMakeUp());
                if (!message.isSuccess())
                    response.put(i, message.getInfo());
                else
                    response.put(i, "OK");
            });
            return response;
        } catch (Exception e) {
            if (request.getMakeUp())
                throw new BusinessException(ErrorCode.EXAM_MAKEUP, "调用考试侧补考服务失败");
            else
                throw new BusinessException(ErrorCode.EXAM_REPLENISH, "调用考试侧重考服务失败");
        }
    }

    @Override
    public ExamStudentScore queryScore(ExamScoreQueryParams params) {
        NewExamRegistrationLoaderMapper request = new NewExamRegistrationLoaderMapper();
        request.setNewExamId(params.getExamId());
        request.setStudentId(params.getStudentId());
        MapMessage message = scoreClient.loadByNewExamIdAndPage(request);
        if (!message.isSuccess())
            throw new BusinessException(ErrorCode.EXAM_SCORE, message.getInfo());

        if (null == message.get("totalCount") || StringUtils.isBlank(message.get("totalCount").toString()))
            throw new BusinessException(ErrorCode.EXAM_SCORE,
                    StringUtils.isBlank(message.getInfo()) ? "结果集总数totalCount为空" : message.getInfo());
        Long totalCount = Long.valueOf(message.get("totalCount").toString());
        if (totalCount <= 0)
            return null;
        Object _list = message.get("registrationList");
        List<HashMap> list = (ArrayList<HashMap>) _list;
        HashMap map = list.get(0);
        if (null == map)
            return null;
        try {
            ExamStudentScore score = JSON.parseObject(JSON.toJSONString(map), ExamStudentScore.class);
            // 计算耗时
            Date startAt = score.getStartAt();
            Date submitAt = score.getSubmitAt();
            if (null != startAt && null != submitAt)
                score.setDuration((submitAt.getTime() - startAt.getTime()) / (1000L * 60));
            return score;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EXAM_SCORE, "解析成绩时发生json错误,原文 = " + map.toString());
        }
    }

    @Override
    public int countExamStudent(String examId) {
        try {
            return examService.loadNewExamStudentCount(examId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EXAM_COUNT_STUDENT,
                    String.format("查询参考学生人数发生错误，考试id=%s", examId));
        }
    }
}
