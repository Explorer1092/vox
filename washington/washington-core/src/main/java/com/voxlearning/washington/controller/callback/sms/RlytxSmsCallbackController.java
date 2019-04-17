package com.voxlearning.washington.controller.callback.sms;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.xml.XmlUtils;
import com.voxlearning.utopia.service.sms.api.constant.SmsClientType;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Date;

/**
 * 容联云通讯 语音验证码发送结果
 * Created by alex on 2017/4/12.
 */
@Controller
@RequestMapping("/callback/sms")
@Slf4j
public class RlytxSmsCallbackController extends AbstractController {

    private static final String SUCCESS_RESULT = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<Response>\n   <statuscode>000000</statuscode>\n</Response>\n";
    private static final String ERROR_RESULT = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<Response>\n   <statuscode>111111</statuscode>\n</Response>\n";

    @RequestMapping(value = "rlytx.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String onCallback(@RequestBody String content) {
        if (StringUtils.isBlank(content)) return ERROR_RESULT;

        try {
            Document document = XmlUtils.parseDocument(new ByteArrayInputStream(content.getBytes()));
            Element request = document.getDocumentElement();
            if (request == null) return ERROR_RESULT;

            String action = XmlUtils.getChildElementText(request, "action");
            String callSid = XmlUtils.getChildElementText(request, "callSid");
            String state = XmlUtils.getChildElementText(request, "state");
            if (!StringUtils.equals(action, "VoiceCode")) return ERROR_RESULT;

            smsServiceClient.getSmsService().updateSmsReceived(callSid,
                    Collections.singleton(SmsClientType.rlytx.name()),
                    DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_TIME),
                    state,
                    "");
            return SUCCESS_RESULT;
        } catch (Exception ex) {
            logger.error("PARSE VOICE MESSAGE CALLBACK XML ERROR", ex);
            return ERROR_RESULT;
        }
    }
}
