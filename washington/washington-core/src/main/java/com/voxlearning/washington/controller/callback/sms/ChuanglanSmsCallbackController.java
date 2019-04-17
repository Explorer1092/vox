package com.voxlearning.washington.controller.callback.sms;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.mapper.xml.XmlUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.sms.api.constant.SmsClientType;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 创蓝短信回调接口
 * Created by jane on 19/2/13.
 */
@Controller
@RequestMapping("/callback/sms")
@Slf4j
public class ChuanglanSmsCallbackController extends AbstractController {

    private static final String SUCCESS_RESULT = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<Response>\n   <statuscode>000000</statuscode>\n</Response>\n";
    private static final String ERROR_RESULT = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<Response>\n   <statuscode>111111</statuscode>\n</Response>\n";

    @Inject private SmsServiceClient smsServiceClient;

    @RequestMapping(value = "chuanglanvc.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String onCallback(@RequestBody HttpServletRequest request){

        if (RuntimeMode.isTest()) {
            logger.info("callback data from chuanglan recived. {}", request);
        }

        if (request == null) {
            logger.warn("Unknown report data item from chuanglan recived.");
            return ERROR_RESULT;
        }

        try {
            Map<String, String> params = UrlUtils.parseQueryString(request.getQueryString());

            // logger.info("sms call back " + JsonUtils.toJson(params));

            String msgId = SafeConverter.toString(params.get("msgid"));
            String receiveTime = SafeConverter.toString(params.get("reportTime"));
            if (StringUtils.isNoneBlank(receiveTime)) {
                receiveTime = "20" + receiveTime + "00";
            }
            String status = SafeConverter.toString(params.get("status"));
            String statusDesc = SafeConverter.toString(params.get("statusDesc"));

            List<String> smsClients = Arrays.asList(SmsClientType.chuanglan.name());

            smsServiceClient.getSmsService().updateSmsReceived(msgId, smsClients, receiveTime, status, statusDesc);
            return SUCCESS_RESULT;
        } catch (Exception ex) {
            logger.error("chuanglan MESSAGE CALLBACK HAS ERROR", ex);
            return ERROR_RESULT;
        }
    }

}
