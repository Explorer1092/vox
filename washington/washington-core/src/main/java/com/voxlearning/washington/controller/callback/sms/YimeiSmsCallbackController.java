package com.voxlearning.washington.controller.callback.sms;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.sms.api.constant.SmsClientType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 亿美短信回调接口
 * Created by Alex on 17/3/31.
 */
@Controller
@RequestMapping("/callback/sms")
@Slf4j
public class YimeiSmsCallbackController extends AbstractController {

    @Inject private SmsServiceClient smsServiceClient;

    @RequestMapping(value = "yimeivc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String onCallback(){

        String reports = getRequestString("reports");

        if (RuntimeMode.isTest()) {
            logger.info("callback data from yimei recived. reports: {}", reports);
        }

        if (StringUtils.isBlank(reports)) {
            return "success";
        }

        try {
            List<Map> smsInfos = JsonUtils.fromJsonToList(reports, Map.class);
            if (CollectionUtils.isEmpty(smsInfos)) {
                logger.warn("Unknown callback data from yimei recived. {}", reports);
                return "success";
            }

            for (Map report : smsInfos) {
                updateSmsStatus(report);
            }

            return "success";

        } catch (Exception ex) {
            logger.error("handle callback data error. data:{}", reports, ex);
            return "failure";
        }

    }

    private void updateSmsStatus(Map report) {
        if (MapUtils.isEmpty(report) || !report.containsKey("smsId")) {
            logger.warn("Unknown report data item from yimei recived. {}", report);
            return;
        }

        String msgId = SafeConverter.toString(report.get("smsId"));
        String receiveTime = SafeConverter.toString(report.get("receiveTime"));
        String code = SafeConverter.toString(report.get("state"));
        String desc = SafeConverter.toString(report.get("desc"));
        receiveTime = receiveTime.replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");

        List<String> smsClients = Arrays.asList(SmsClientType.yimei.name());

        smsServiceClient.getSmsService().updateSmsReceived(msgId, smsClients, receiveTime, code, desc);
    }

}
