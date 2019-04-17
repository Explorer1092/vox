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
 * 企信通短信回调接口
 * Created by Alex on 17/4/24.
 */
@Controller
@RequestMapping("/callback/sms")
@Slf4j
public class QxtSmsCallbackController extends AbstractController {

    @RequestMapping(value = "qxt.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String onCallback() {
        // 状态报告请以content与mobile组合勾兑，采用post方式推送，一次推送最多50条，用分号拆分。
        // 返回参数：接收到状态报告之后返回一个OK
        String report = getRequestString("args");
        if (StringUtils.isBlank(report)) {
            logger.warn("empty sms status report callback received.");
            return "ERROR";
        }

        // FIXME temp log
        // logger.info("sms callback from qxt received. content:{}", report);
        
        String[] items = report.split(";");
        for (String item : items) {
            updateSmsStatus(item);
        }

        return "OK";
    }

    private void updateSmsStatus(String content) {
        String[] items = content.split(",");
        if (items.length != 5) {
            logger.warn("unknown sms status report callback content [{}] received.", content);
            return;
        }

        if (RuntimeMode.isTest()) {
            logger.info("sms status report received. type:{}, mobile:{}, status:{}, msgId:{}, receiveTime:{}",
                    items[0],
                    items[1],
                    items[2],
                    items[3],
                    items[4]);
        }

        List<String> smsClients = Arrays.asList(SmsClientType.qxtlt.name(),
//                SmsClientType.qxtdx.name(),
                SmsClientType.qxtrjy.name(),
                SmsClientType.qxtlt_notice.name(),
                SmsClientType.qxtrjy_notice.name());

        smsServiceClient.getSmsService().updateSmsReceived(items[3], smsClients, items[4], items[2], "");
    }
}
