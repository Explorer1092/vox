package com.voxlearning.utopia.agent.persist.entity.loginlog;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yaguang.wang
 * on 2017/5/26.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "vox_login_log")
public class AgentUserLoginLog implements Serializable, TimestampTouchable {
    private static final long serialVersionUID = 334384482309689292L;

    @DocumentId private String id;
    @DocumentCreateTimestamp private Date createTime;           //创建时间
    @DocumentUpdateTimestamp private Date updateTime;           //更新时间
    private String newDeviceString;                             //新设备ID
    private String oldDeviceString;                             //旧设备ID
    private Long optUserId;                                     //操作ID
    private String optUserName;                                 //操作人名称
    private Date optTime;                                       //操作时间
}
