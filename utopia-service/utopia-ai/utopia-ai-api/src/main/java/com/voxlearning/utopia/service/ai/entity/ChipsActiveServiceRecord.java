package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.ai.constant.ChipsActiveServiceType;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * 主动服务记录
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-sochi")
@DocumentDatabase(database = "vox-chips")
@DocumentCollection(collection = "vox_chips_english_ai_unit_service_record")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190412")
public class ChipsActiveServiceRecord implements Serializable {
    private static final long serialVersionUID = 4220629153086112904L;

    @DocumentId
    private String id;          // serviceType-classId-userId-unitId
    @DocumentField(value = "service_type")
    private String serviceType; // 服务类型
    @DocumentField(value = "class_id")
    private Long classId;     // 班级ID
    @DocumentField(value = "user_id")
    private Long userId;        // 用户ID
    @DocumentField(value = "unit_id")
    private String unitId;      // 单元ID
    @DocumentField(value = "serviced")
    private Boolean serviced;   // 是否服务
    @DocumentCreateTimestamp
    private Date createDate;    // 单元完成时间
    @DocumentUpdateTimestamp
    private Date updateDate;    // 更新时间
    @DocumentField(value = "user_video_id")
    private String userVideoId;//对应的AIUserVideo的id,只有有视频的才会有值
    @DocumentField(value = "examine_status")
    private Boolean examineStatus;//一对一视频是否审核 true 审核,false :未审核
    @DocumentField(value = "video_url")
    private String videoUrl;//一对一视频地址;审核通过才有值
    @DocumentField(value = "remark_status")
    private Integer remarkStatus;//视频筛选中:0,筛选完成:1,待审核:2,合成中：3,待服务:4, 完成：5

    public static String ck_serviceType_classId(String serviceType, Long classId) {
        return CacheKeyGenerator.generateCacheKey(ChipsActiveServiceRecord.class,
                new String[]{"serviceType", "classId"},
                new Object[]{serviceType, classId});
    }

    public static String ck_classId(Long classId) {
        return CacheKeyGenerator.generateCacheKey(ChipsActiveServiceRecord.class, "CID", classId);
    }

    public static ChipsActiveServiceRecord valueOf(ChipsActiveServiceType serviceType, Long classId, Long userId, String unitId) {
        ChipsActiveServiceRecord chipsActiveServiceRecord = new ChipsActiveServiceRecord();
        String id = genId(serviceType, classId, userId, unitId);
        chipsActiveServiceRecord.setServiceType(serviceType.name());
        chipsActiveServiceRecord.setClassId(classId);
        chipsActiveServiceRecord.setId(id);
        chipsActiveServiceRecord.setUserId(userId);
        chipsActiveServiceRecord.setUnitId(unitId);
        chipsActiveServiceRecord.setServiced(Boolean.FALSE);
        return chipsActiveServiceRecord;
    }

    public static ChipsActiveServiceRecord valueOf(ChipsActiveServiceType serviceType, Long classId, Long userId, String unitId, RemarkStatus remarkStatus) {
        ChipsActiveServiceRecord chipsActiveServiceRecord = new ChipsActiveServiceRecord();
        String id = genId(serviceType, classId, userId, unitId);
        chipsActiveServiceRecord.setServiceType(serviceType.name());
        chipsActiveServiceRecord.setClassId(classId);
        chipsActiveServiceRecord.setId(id);
        chipsActiveServiceRecord.setUserId(userId);
        chipsActiveServiceRecord.setUnitId(unitId);
        chipsActiveServiceRecord.setServiced(Boolean.FALSE);
        chipsActiveServiceRecord.setRemarkStatus(remarkStatus.val);
        return chipsActiveServiceRecord;
    }

    public static String genId(ChipsActiveServiceType serviceType, Long classId, Long userId, String unitId) {
        return serviceType.name() + "-" + classId + "-" + userId + "-" + unitId;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum RemarkStatus {

        Zero("完课",0),
        One("待审核",1),
        Two("无审核视频",2),
        Three("图片音频合成中",3),
        Four("视频合成中",4),
        Five("视频合成完成",5);
        @Getter
        private final String description;
        private final int val;

        public int getVal() {
            return val;
        }

        public static RemarkStatus safeOf(String name) {
            try {
                return valueOf(name);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
