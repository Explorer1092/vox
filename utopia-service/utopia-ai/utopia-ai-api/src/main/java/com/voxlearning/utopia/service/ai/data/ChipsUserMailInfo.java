package com.voxlearning.utopia.service.ai.data;

import com.voxlearning.utopia.service.ai.entity.ChipsEnglishUserExtSplit;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author zhuxuan
 * 用户邮寄信息
 */
@Setter
@Getter
public class ChipsUserMailInfo implements Serializable {
    private static final long serialVersionUID = -965905030551092747L;

    private Long id;               //用户id
    private String recipientName;  //收货人姓名
    private String recipientTel;   //收货人电话
    private String recipientAddr;  //收货人地址

    public static ChipsUserMailInfo valueOf(ChipsEnglishUserExtSplit split) {
        ChipsUserMailInfo result = new ChipsUserMailInfo();
        result.id = split.getId();
        result.recipientName = split.getRecipientName();
        result.recipientTel = split.getRecipientTel();
        result.recipientAddr = split.getRecipientAddr();
        return result;
    }
}
