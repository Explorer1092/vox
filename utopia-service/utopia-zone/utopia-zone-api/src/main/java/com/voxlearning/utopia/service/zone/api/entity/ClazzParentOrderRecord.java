package com.voxlearning.utopia.service.zone.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 班级圈活动
 * @author chensn
 * @date 2018-10-29 20:13
 */
@Getter
@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_class_circle_parent_order_{}",dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
@UtopiaCacheRevision("20181030")
public class ClazzParentOrderRecord implements Serializable {
    private static final long serialVersionUID = 1094914312975896161L;
    //id_type
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    /**
     * 订单类型
     */
    private Integer type;

    private String subject;

    private Integer status;

    private Integer buyNum;
    @DocumentCreateTimestamp
    private Date ct;
    @DocumentUpdateTimestamp
    private Date ut;

}
