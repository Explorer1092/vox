package com.voxlearning.utopia.service.crm.api.entities.crm;

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
 * Created by wangshichao on 16/8/23.
 */

@Getter
@Setter
@DocumentConnection(configName = "mongo-jxt")
@NoArgsConstructor
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "vox_push_trace")
public class PushTask implements Serializable, TimestampTouchable {

    private static final long serialVersionUID = 8124889408268338570L;

    @DocumentId
    private String Id;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;
}
