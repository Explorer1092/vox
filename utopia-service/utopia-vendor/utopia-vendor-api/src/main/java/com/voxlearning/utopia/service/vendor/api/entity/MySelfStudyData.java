package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 我的自学数据
 *
 * @author jiangpeng
 * @since 2016-10-20 下午1:58
 **/
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "vox_my_self_study")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161020")
public class MySelfStudyData implements Serializable {
    private static final long serialVersionUID = -5818986133657824660L;

    @DocumentId
    private String id;   //studentId_SelfStudyType

    private SelfStudyType selfStudyType; //自学产品类型

    private Long studentId;   //学生id

    @DocumentFieldIgnore
    private Date expireDate; //产品过期时间  库里不保存,冗余

    private Date  lastUseDate;  //最后使用时间

    private String studyProgress; //学习进度

    private Integer studyDayCount;

    @DocumentCreateTimestamp
    private Date createDate;

    @DocumentUpdateTimestamp
    private Date updateDate;


    public static MySelfStudyData newInstance(SelfStudyType selfStudyType, Long studentId) {
        MySelfStudyData instance = new MySelfStudyData();
        instance.setSelfStudyType(selfStudyType);
        instance.setStudentId(studentId);
        instance.generateId(studentId, selfStudyType);
        return instance;
    }
    public String generateId(Long studentId, SelfStudyType selfStudyType){
        this.id = studentId + "_" + selfStudyType.name();
        return this.id;
    }


    public static String generateCacheKeyById(String id) {
        return CacheKeyGenerator.generateCacheKey(MySelfStudyData.class, new String[]{"ID"}, new Object[]{id});
    }

    public static String generateCacheKeyBySId(Long studentId) {
        return CacheKeyGenerator.generateCacheKey(MySelfStudyData.class, new String[]{"SID"}, new Object[]{studentId});
    }


}
