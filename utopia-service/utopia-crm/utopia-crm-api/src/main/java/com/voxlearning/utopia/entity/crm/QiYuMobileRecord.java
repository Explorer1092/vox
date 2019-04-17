package com.voxlearning.utopia.entity.crm;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.Date;

/**
 * 七鱼通话记录
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_qiyu_mobile_record")
@UtopiaCacheRevision("20181128")

public class QiYuMobileRecord implements Serializable, CacheDimensionDocument {

    @DocumentId
    private Long sessionId;

    private Long startTime; // 通话开始时间

    private Long endTime; // 通话结束时间

    private Long connectionStartTime; // 通话接通时间

    private Long waitingDuration; // 通话等待时长

    private Long callDuration; // 通话时长

    private Integer direction; // 呼叫方向 1-呼入（客服为被叫），2-呼出（客服为主叫）

    private String callOutNum; // 被叫号码

    private String callInNum; // 来电呼入号码

    private Integer status; // 电话状态

    private String evaluation; // 服务评价

    private String recordurl; // 录音地址

    private Long staffId; // 客服ID

    private String staffName; // 客服姓名

    private String staffNum; // 客服坐席号码

    private Date createTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }
}
