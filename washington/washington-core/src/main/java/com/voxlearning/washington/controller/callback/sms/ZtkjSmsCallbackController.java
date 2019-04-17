package com.voxlearning.washington.controller.callback.sms;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.sms.api.constant.SmsClientType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * 助通科技短信回调接口
 * Created by Alex on 17/3/31.
 */
@Controller
@RequestMapping("/callback/sms")
@Slf4j
public class ZtkjSmsCallbackController extends AbstractController {

    @Inject private SmsServiceClient smsServiceClient;

    @RequestMapping(value = "ztkjvc.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String onCallback(){
        // 每次返回400(最大)组内容，客户被动接收状态报告并解析之
        // 返回参数：接收到状态报告之后返回一个OK
        String report = getRequestString("param");
        if (StringUtils.isBlank(report)) {
            logger.warn("empty sms status report callback received.");
            return "1";
        }

        String[] items = report.split(";");
        for (String item : items) {
            updateSmsStatus(item);
        }

        return "0";
    }

    private void updateSmsStatus(String content) {
        String[] items = content.split(",");
        if (items.length != 4) {
            logger.warn("unknown sms status report callback content [{}] received.", content);
            return;
        }

        // FIXME update sms status later
        if (RuntimeMode.isTest()) {
            logger.info("sms status report received. msgId:{}, mobile:{}, status:{}, sendTime:{}",
                    items[0],
                    items[1],
                    items[2],
                    items[3]);
        }

//        // 发送过来的数据HH:mm:ss格式，加上yyyy-MM-dd
//        String sendTime = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE) + " " + items[3];

        List<String> smsClients = Arrays.asList(SmsClientType.ztkj.name(), SmsClientType.ztkj_notice.name());

        smsServiceClient.getSmsService().updateSmsReceived(items[0], smsClients, items[3], items[2], "");
    }

}
