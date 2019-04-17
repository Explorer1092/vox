package com.voxlearning.washington.controller.callback.sms;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.sms.api.constant.SmsClientType;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

/**
 * 安信捷上行短信回调接口
 * Created by Alex on 17/3/31.
 */
@Controller
@RequestMapping("/callback/sms")
@Slf4j
public class AxjSmsCallbackController extends AbstractController {

    @RequestMapping(value = "axjvc.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String onCallback(){
        // 状态报告请以content与mobile组合勾兑，采用post方式推送，一次推送最多50条，用分号拆分。
        // 返回参数：接收到状态报告之后返回一个OK
        String report = getRequestString("report");
        if (StringUtils.isBlank(report)) {
            logger.warn("empty sms status report callback received.");
            return "ERROR";
        }

        String[] items = report.split(";");
        for (String item : items) {
            updateSmsStatus(item);
        }

        return "OK";
    }

    private void updateSmsStatus(String content) {
        String[] items = content.split(",");
        if (items.length != 6) {
            logger.warn("unknown sms status report callback content [{}] received.", content);
            return;
        }

        if (RuntimeMode.isTest()) {
            logger.info("sms status report received. type:{}, mobile:{}, msgId:{}, sendTime:{}, receiveTime:{}, status:{}",
                    items[0],
                    items[1],
                    items[2],
                    items[3],
                    items[4],
                    items[5]);
        }

        // 发送过来的数据是yyMMddHHmm格式，拼上yy 和 ss
        String sendTime = "20" + items[4] + "00";

        List<String> smsClients = Arrays.asList(SmsClientType.axjrjy.name(), SmsClientType.axjyd.name(),
                SmsClientType.axjrjy_notice.name());

        smsServiceClient.getSmsService().updateSmsReceived(items[2], smsClients, sendTime, items[5], "");
    }

}
