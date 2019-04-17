package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 点读作品集
 *
 * @author jiangpeng
 * @since 2017-03-10 下午8:34
 **/
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "follow_read_collection_{}", dynamic = true)
@DocumentIndexes({
        @DocumentIndex(def = "{'studentId':1}",background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 86400 * 30)
public class FollowReadCollection implements CacheDimensionDocument {


    private static final long serialVersionUID = -277304945501171795L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;

    private Long studentId;
    private String unitId;
    private List<String> resultIdList;

    private Long schoolId;
    private Integer cityId;


    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTIme;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
            newCacheKey(id), newCacheKey("SID", studentId)
        };
    }


    public FollowReadCollection generateId(){
        Objects.requireNonNull(studentId);
        id = studentId + "-" + RandomUtils.nextObjectId();
        return this;
    }

    public static String getObjectIdFromId(String id){
        if (StringUtils.isBlank(id))
            return null;
        String[] split = StringUtils.split(id, "-");
        if (split.length != 2)
            return null;
        return split[1];
    }

    public static Long getStudentIdFromId(String id){
        if (StringUtils.isBlank(id))
            return null;
        String[] split = StringUtils.split(id, "-");
        if (split.length != 2)
            return null;
        return SafeConverter.toLong(split[0]);
    }

    public static boolean validateId(String collectionId) {
        String objectIdFromId = getObjectIdFromId(collectionId);
        if (StringUtils.isBlank(objectIdFromId))
            return false;
        return ObjectId.isValid(objectIdFromId);
    }
}
