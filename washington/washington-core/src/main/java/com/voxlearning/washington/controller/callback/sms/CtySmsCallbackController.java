package com.voxlearning.washington.controller.callback.sms;

/**
 * 畅天游短信发送报告接口
 * Created by Alex on 17/4/1.
 */

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.sms.api.constant.SmsClientType;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/callback/sms")
@Slf4j
public class CtySmsCallbackController extends AbstractController {

    @RequestMapping(value = "cty.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String onCallback(){
        String ctuId = getRequestString("ctuid");
        String state = getRequestString("stat");
        if (StringUtils.isBlank(ctuId) || StringUtils.isBlank(state)) {
            logger.warn("empty sms status report callback received. ctuid:{}, stat:{}", ctuId, state);
            return errorReturn();
        }

        updateSmsStatus(ctuId, state);

        return successReturn();
    }

    private String errorReturn() {
        return "<Result>-1</Result>";
    }

    private String successReturn() {
        return "<Result>1</Result>";
    }

    private void updateSmsStatus(String ctuId, String state) {
        // FIXME update sms status later
        if (RuntimeMode.isTest()) {
            logger.info("sms status report received. ctuid:{}, state:{}", ctuId, state);
        }

        List<String> smsClients = Collections.singletonList(SmsClientType.cty.name());
        smsServiceClient.getSmsService().updateSmsReceived(ctuId, smsClients, DateUtils.dateToString(new Date(), "yyyyMMddHHmmss"), state, "");
    }

}

