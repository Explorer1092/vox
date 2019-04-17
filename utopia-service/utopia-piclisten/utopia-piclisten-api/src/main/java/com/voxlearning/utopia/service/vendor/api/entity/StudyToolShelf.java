package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2017-10-13 下午12:00
 **/
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "study_tool_shelf")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class StudyToolShelf implements CacheDimensionDocument {
    private static final long serialVersionUID = 1444128707090211370L;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(studentId)
        };
    }

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private Long studentId;

    private List<String> selfStudyTypeList;

    @DocumentCreateTimestamp
    private Date createTime;

    @DocumentUpdateTimestamp
    private Date updateTime;



    public List<SelfStudyType> toSelfStudyTypeList(){
        if (CollectionUtils.isEmpty(selfStudyTypeList))
            return Collections.emptyList();
        return selfStudyTypeList.stream().map(SelfStudyType::of)
                .filter(s -> s != SelfStudyType.UNKNOWN).collect(Collectors.toList());
    }
}
