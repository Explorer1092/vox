package com.voxlearning.utopia.service.newhomework.api.entity.outside;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 好词好句
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-siberia")
@DocumentDatabase(database = "vox-outside")
@DocumentCollection(collection = "outside_reading_collection")
@UtopiaCacheExpiration(604800)
@UtopiaCacheRevision("20181114")
public class OutsideReadingCollection implements Serializable {
    private static final long serialVersionUID = -7105118833149073021L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;                  //好词好句收藏ID(studentId-bookId-missionId-goldenWordsIndex)

    private Long studentId;
    private String bookId;
    private String missionId;
    private List<String> labels;         //标签
    private String goldenWordsContent;  //好词好句内容
    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;
    private Boolean disabled;           //是否删除

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(OutsideReadingCollection.class, id);
    }

    public static String ck_studentId(Long studentId) {
        return CacheKeyGenerator.generateCacheKey(
                OutsideReadingCollection.class,
                new String[]{"studentId"},
                new Object[]{studentId}
        );
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"studentId", "bookId", "missionId", "goldenWordsIndex"})
    @AllArgsConstructor
    public static class ID implements Serializable {

        private static final long serialVersionUID = -3484840509385339281L;
        private Long studentId;
        private String bookId;
        private String missionId;           // 关卡id
        private Integer goldenWordsIndex;   // 好词好句在关卡中的索引

        @Override
        public String toString() {
            return StringUtils.join(studentId, "-", bookId, "-", missionId, "-", goldenWordsIndex);
        }
    }
}
