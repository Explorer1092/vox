package com.voxlearning.washington.controller.callback.sms;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.mapper.xml.XmlUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
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
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 秒信短信回调接口
 * Created by Alex on 17/3/31.
 */
@Controller
@RequestMapping("/callback/sms")
@Slf4j
public class MiaoxinSmsCallbackController extends AbstractController {

    @Inject private SmsServiceClient smsServiceClient;

    @RequestMapping(value = "miaoxinvc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String onCallback(@RequestBody String content){

        if (RuntimeMode.isTest()) {
            logger.info("callback data from miaoxin recived. {}", JsonUtils.fromJson(content));
        }

        if (StringUtils.isBlank(content)) {
            return "success";
        }

        try {
            Map<String, Object> extInfo = JsonUtils.fromJson(content);
            if (extInfo == null || !extInfo.containsKey("reports")) {
                logger.warn("Unknown callback data from miaoxin recived. {}", content);
                return "success";
            }

            List<Map<String, Object>> reports = (List<Map<String, Object>>) extInfo.get("reports");
            if (CollectionUtils.isEmpty(reports)) {
                logger.warn("Empty callback data from miaoxin recived. {}", content);
                return "success";
            }

            for (Map<String, Object> report : reports) {
                updateSmsStatus(report);
            }

            return "success";

        } catch (Exception ex) {
            logger.error("handle callback data error. data:{}", content, ex);
            return "failure";
        }

    }

    private void updateSmsStatus(Map<String, Object> report) {
        if (MapUtils.isEmpty(report) || !report.containsKey("order_id")) {
            logger.warn("Unknown report data item from miaoxin recived. {}", report);
            return;
        }

        String msgId = SafeConverter.toString(report.get("order_id"));
        String receiveTime = SafeConverter.toString(report.get("done_time"));
        String code = SafeConverter.toString(report.get("status"));
        String desc = SafeConverter.toString(report.get("msg"));

        List<String> smsClients = Arrays.asList(SmsClientType.miaoxin.name());

        smsServiceClient.getSmsService().updateSmsReceived(msgId, smsClients, receiveTime, code, desc);
    }

}
