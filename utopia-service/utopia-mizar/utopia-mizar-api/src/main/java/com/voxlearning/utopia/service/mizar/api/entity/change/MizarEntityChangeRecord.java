package com.voxlearning.utopia.service.mizar.api.entity.change;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.mizar.api.constants.MizarAuditStatus;
import com.voxlearning.utopia.service.mizar.api.constants.MizarEntityType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xiang.lv on 2016/10/9.
 * 　记录机构,品牌,课程变更信息
 *
 * @author xiang.lv
 * @date 2016/10/9   15:25
 */
@Setter
@Getter
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "mizar_entity_change_record")
@DocumentIndexes({
        @DocumentIndex(def = "{'entityType':1}", background = true),
        @DocumentIndex(def = "{'applicantId':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161009")
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class MizarEntityChangeRecord implements Serializable {
    private static final long serialVersionUID = 2425834859791748456L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.OBJECT_ID)
    private String id;
    private String targetId;               // 关联实体id
    private String desc;                   // 实体名称
    private String content;                // 实体内容json
    private String entityType;             // 实体类型 MizarEntityType
    private String applicantId;            // 变更申请人ID
    private String applicant;              // 变更申请人姓名
    private Date auditTime;                // 审核时间
    private String auditorId;              // 审核人ID
    private String auditor;                // 审核人
    private String auditorNote;            // 审核意见
    private String auditStatus;            // 审核状态 MizarAuditStatus

    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(MizarEntityChangeRecord.class, id);
    }

    public static String ck_applicant(String operatorId) {
        return CacheKeyGenerator.generateCacheKey(MizarEntityChangeRecord.class, "APP", operatorId);
    }

    public static String ck_entity(String entityType) {
        return CacheKeyGenerator.generateCacheKey(MizarEntityChangeRecord.class, "T", entityType);
    }

    public static MizarEntityChangeRecord newBrandInstance(String userId, String userRealName) {
        MizarEntityChangeRecord record = new MizarEntityChangeRecord();
        record.setAuditStatus(MizarAuditStatus.PENDING.name());
        record.setEntityType(MizarEntityType.BRAND.getCode());
        record.setApplicant(userRealName);
        record.setApplicantId(userId);
        return record;
    }

    public static MizarEntityChangeRecord newShopInstance(String userId, String userRealName) {
        MizarEntityChangeRecord record = new MizarEntityChangeRecord();
        record.setAuditStatus(MizarAuditStatus.PENDING.name());
        record.setEntityType(MizarEntityType.SHOP.getCode());
        record.setApplicant(userRealName);
        record.setApplicantId(userId);
        return record;
    }

    public static MizarEntityChangeRecord newGoodsInstance(String userId, String userRealName) {
        MizarEntityChangeRecord record = new MizarEntityChangeRecord();
        record.setAuditStatus(MizarAuditStatus.PENDING.name());
        record.setEntityType(MizarEntityType.GOODS.getCode());
        record.setApplicant(userRealName);
        record.setApplicantId(userId);
        return record;
    }

}
