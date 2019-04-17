package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author shiwe.liao
 * @since 2016-8-9
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newhomework")
@DocumentCollection(collection = "homework_study_master")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_month)
@UtopiaCacheRevision("20171109")
@UtopiaCachePrefix(prefix = "NewHomeworkStudyMaster")
public class NewHomeworkStudyMaster implements CacheDimensionDocument {

    private static final long serialVersionUID = 3560988901943694309L;

    @DocumentId
    private String id;      //直接用newHomeworkId
    private Subject subject;    //学科
    private List<MasterStudent> masterStudentList;  //学霸列表
    private List<Long> excellentList;  //[学科]之星：star of excellent
    private List<Long> calculationList;  //口算之星 ：star of calculation
    private List<Long> focusList;  //专注之星：star of focus
    private List<Long> positiveList;  //积极之星：star of positive
    @DocumentCreateTimestamp
    private Date createTime;    //创建时间
    @DocumentUpdateTimestamp
    private Date updateTime;    //更新时间

    public static String generateCacheKey(String id){
        return CacheKeyGenerator.generateCacheKey(NewHomeworkStudyMaster.class,id);
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id)
        };
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "userId")
    public static class MasterStudent implements Serializable {

        private static final long serialVersionUID = 4414610742574240807L;

        private Long userId;    //学生ID
        private String userName;    //学生姓名
    }
}
