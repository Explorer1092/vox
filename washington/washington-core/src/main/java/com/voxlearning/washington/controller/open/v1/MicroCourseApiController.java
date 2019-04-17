/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.mizar.api.mapper.talkfun.TK_CallbackData;
import com.voxlearning.utopia.service.mizar.api.service.talkfun.TalkFunService;
import com.voxlearning.utopia.service.mizar.api.utils.talkfun.TalkFunCommand;
import com.voxlearning.utopia.service.mizar.talkfun.TalkFunUtils;
import com.voxlearning.washington.controller.open.AbstractApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.TreeMap;

/**
 * 微课堂对外接口
 *
 * @author yuechen.wang
 * @since 2017-02-20
 */
@Controller
@RequestMapping(value = "/v1/microcourse")
public class MicroCourseApiController extends AbstractApiController {

    @ImportService(interfaceClass = TalkFunService.class)
    private TalkFunService talkFunService;

    // 参数列表
    private static final String REQ_OPEN_ID = "openID";
    private static final String REQ_TIMESTAMP = "timestamp";
    private static final String REQ_COMMAND = "cmd";
    private static final String REQ_PARAMETERS = "params";
    private static final String REQ_VERSION = "ver";
    private static final String REQ_SIGNATURE = "sign";

    /**
     * 课程结束下课提醒
     */
    @RequestMapping(value = "/talkfun/callback.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage finishClazz() {
        // 校验参数
        try {
            validateRequired(REQ_OPEN_ID, "openID");
            validateRequired(REQ_COMMAND, "接口名称");
            validateEnum(REQ_COMMAND, "接口名称", TalkFunCommand.COURSE_STOP.getCmd(), TalkFunCommand.REPLAY_DONE.getCmd());
            validateRequired(REQ_PARAMETERS, "参数表");
            // 校验签名
            validateRequired(REQ_SIGNATURE, "数字签名");
            TalkFunUtils.validateSign(getRequestParam(), getRequestString(REQ_SIGNATURE), RuntimeMode.current());
        } catch (IllegalArgumentException e) {
            return TalkFunUtils.errorResponse(e.getMessage());
        }
        TK_CallbackData data = TalkFunUtils.parseReturnData(getRequestString(REQ_PARAMETERS), TK_CallbackData.class);
        if (data == null || StringUtils.isBlank(data.getCourseId())) {
            return TalkFunUtils.errorResponse("无效的课程信息");
        }
        String cmd = getRequestString(REQ_COMMAND);
        MapMessage message = MapMessage.errorMessage();
        if (TalkFunCommand.COURSE_STOP.getCmd().equals(cmd)) {
            message = talkFunService.finishClazz(data.getCourseId());
        } else if (TalkFunCommand.REPLAY_DONE.getCmd().equals(cmd)) {
            message = talkFunService.replayDone(data.getCourseId());
        }
        if (!message.isSuccess()) {
            return TalkFunUtils.errorResponse("状态更新失败");
        }
        return TalkFunUtils.successResponse();
    }

    private Map<String, String> getRequestParam() {
        Map<String, String> paramMap = new TreeMap<>();
        paramMap.put(REQ_OPEN_ID, getRequestString(REQ_OPEN_ID)); // 合作方唯一标识码
        paramMap.put(REQ_TIMESTAMP, getRequestString(REQ_TIMESTAMP)); // 当前Unix时间戳
        paramMap.put(REQ_COMMAND, getRequestString(REQ_COMMAND));  // 调用接口的名称
        paramMap.put(REQ_VERSION, getRequestString(REQ_VERSION));// 协议版本号，默认1.0
        paramMap.put(REQ_PARAMETERS, getRequestString(REQ_PARAMETERS));// 接口参数
        return paramMap;
    }

}