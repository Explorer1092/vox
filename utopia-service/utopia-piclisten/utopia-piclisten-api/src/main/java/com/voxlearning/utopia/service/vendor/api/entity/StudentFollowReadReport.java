package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author jiangpeng
 * @since 2017-03-21 下午5:09
 **/
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "student_follow_read_report")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class StudentFollowReadReport implements CacheDimensionDocument {
    private static final long serialVersionUID = -9187344680197357274L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private Long id; // 学生id

    private List<UnitResult> unitResultList;

    @DocumentCreateTimestamp
    private Date createTime;

    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
            newCacheKey(id)
        };
    }

    public static StudentFollowReadReport newInstance(Long studentId){
        StudentFollowReadReport studentFollowReadReport = new StudentFollowReadReport();
        studentFollowReadReport.setId(studentId);
        studentFollowReadReport.setUnitResultList(new ArrayList<>());
        return studentFollowReadReport;
    }


    @Getter
    @Setter
    public static class UnitResult implements Serializable{
        private static final long serialVersionUID = 630531219537076527L;
        private Integer averageScore;
        private String unitId;
        private String unitName;
        private Integer unitRank;
        private String bookId;
        private String bookName;
        private String moduleId;
        private String moduleName;
        private Integer moduleRank;
        private Long totalSentenceCount;
        private Long readSentenceCount;
        List<String> lastReadSentenceResultIds;
    }
}
