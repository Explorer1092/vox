package com.voxlearning.utopia.service.newhomework.api.entity.bonus;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

/**
 * 能力测评题目数据
 *
 * @author lei.liu
 * @version 18-10-31
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-siberia")
@DocumentDatabase(database = "vox-bonus-collect")
@DocumentCollection(collection = "vox_ability_exam_question_{}", dynamic = true)  // 按学生id尾号分表
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 86400 * 15)
@UtopiaCacheRevision(value = "20181031")
public class AbilityExamQuestion implements CacheDimensionDocument {
    private static final long serialVersionUID = -3437782104461302573L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE) private String id;    // studentId
    @DocumentCreateTimestamp private Date ct;
    @DocumentUpdateTimestamp private Date ut;

    private Map<String, Boolean> finishedQuestions; // 完成的题目
    private Date finishedDate;                      // 全部完成时间

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id)
        };
    }

}
