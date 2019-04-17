package com.voxlearning.utopia.data.sms;


import lombok.Data;

import java.io.Serializable;

/**
 * 希望将来验证码体系能更加统一干净，用context满足大多数需求。暂时没用起来。如果始终没用也可以删掉这个类....
 */
@Data
public class VerificationCodeContext implements Serializable {

    private static final long serialVersionUID = 8201663216166636499L;

    private String realRemoteAddr;
    private String verificationCode;
    private Long lastSendTime;
}
