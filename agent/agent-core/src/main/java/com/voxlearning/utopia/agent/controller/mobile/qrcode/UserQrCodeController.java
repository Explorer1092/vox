package com.voxlearning.utopia.agent.controller.mobile.qrcode;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.constants.QRCodeBusinessType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.qrcode.UserQrCodeService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * UserQrCodeController
 *
 * @author song.wang
 * @date 2018/12/17
 */
@Controller
@RequestMapping("/mobile/qrcode")
public class UserQrCodeController extends AbstractAgentController {

    @Inject
    private UserQrCodeService userQrCodeService;

    @RequestMapping(value = "get_user_qr_code.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getUserRrCode() {
        return userQrCodeService.getQRCode(getCurrentUserId(), QRCodeBusinessType.LIVE_ENROLLMENT);
    }
}
