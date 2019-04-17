package com.voxlearning.utopia.service.newhomework.api.entity.outside;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 学生阅读成就
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-siberia")
@DocumentDatabase(database = "vox-outside")
@DocumentCollection(collection = "outside_reading_achievement")
@UtopiaCacheExpiration(604800)
@UtopiaCacheRevision("20181114")
public class OutsideReadingAchievement implements Serializable {
    private static final long serialVersionUID = -2037557597368813448L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private Long id;                    //学生ID

    private List<Double> levelReadingCount; // 各个年级阅读成就字数, 栗子: 三年级阅读成就1.5w字 [0, 0, 1.5, 0, 0, 0]
    private Integer goldenWordsCount;      // 收藏好词好句数量
    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(OutsideReadingAchievement.class, id);
    }

    /**
     * 获取学生总的阅读成就字数
     * @return
     */
    @JsonIgnore
    public Double getTotalReadingCount() {
        return levelReadingCount.stream().mapToDouble(o -> o).sum();
    }

}
