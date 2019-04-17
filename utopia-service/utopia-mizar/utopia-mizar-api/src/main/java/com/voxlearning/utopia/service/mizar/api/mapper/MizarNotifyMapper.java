package com.voxlearning.utopia.service.mizar.api.mapper;

import com.voxlearning.utopia.service.mizar.api.constants.MizarNotifyType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by Yuechen on 2016/12/06.
 */
@Getter
@Setter
public class MizarNotifyMapper implements Serializable {

    private static final long serialVersionUID = -4992343263157523892L;

    private String id;               // 对应的 MizarUserNotify 的 id
    private String notifyId;         // 消息id
    private String title;            // 标题
    private String content;          // 内容
    private MizarNotifyType type;    // 类型
    private String url;              // 跳转链接
    private String creator;          // 消息创建人
    private List<MizarFile> files;   // 附件
    private boolean read;            // 是否已读
    private Date createAt;           // 消息发送时间

}
