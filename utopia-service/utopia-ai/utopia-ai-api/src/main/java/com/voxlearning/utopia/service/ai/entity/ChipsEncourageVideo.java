package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author guangqing
 * @since 2019/1/4
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-chips")
@DocumentCollection(collection = "vox_chips_encourage_video")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190326")
public class ChipsEncourageVideo implements Serializable {

    private static final long serialVersionUID = -3636824433643510682L;
    @DocumentId
    private String id;//type-unitType-Level
    private String video;//视频讲解地址

    private String type; //外教鼓励语:"0",中教鼓励语:"1"
    private String unitType;//单元类型
    private String level;//外教:A,B

    @DocumentField("DISABLED")
    private Boolean disabled;
    @DocumentCreateTimestamp
    private Date createDate;
    @DocumentUpdateTimestamp
    private Date updateDate;

    public static String genId(String type, String unitType, String level) {
        return type + "-" + unitType + "-" + level;
    }
}
