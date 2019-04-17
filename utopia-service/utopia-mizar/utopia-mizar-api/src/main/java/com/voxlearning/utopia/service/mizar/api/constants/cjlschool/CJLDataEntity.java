package com.voxlearning.utopia.service.mizar.api.constants.cjlschool;

import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by Yuechen.Wang on 2017/7/19.
 */
abstract public class CJLDataEntity {

    public static final String SEP = ",";

    abstract public CJLEntityType entity();

    @Getter @Setter
    @DocumentCreateTimestamp private Date createTime;  // 创建时间
    @Getter @Setter
    @DocumentUpdateTimestamp private Date updateTime;  // 最后更新时间

    @Getter @Setter private Integer syncStatus;        // 数据同步状态
    @Getter @Setter private String syncMessage;        // 数据同步信息

    public boolean notSync() {
        return syncStatus == null || syncStatus != 1;
    }
}
