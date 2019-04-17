package com.voxlearning.utopia.service.newhomework.api.entity.bonus;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 能力测评基本数据
 *
 * @author lei.liu
 * @version 18-10-31
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-siberia")
@DocumentDatabase(database = "vox-bonus-collect")
@DocumentCollection(collection = "vox_ability_exam_basic_{}", dynamic = true)  // 按学生id尾号分表
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 86400 * 15)
@UtopiaCacheRevision(value = "20181031")
public class AbilityExamBasic implements CacheDimensionDocument {
    private static final long serialVersionUID = -6139360293042688923L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE) private String id; // studentId
    @DocumentCreateTimestamp private Date ct;
    @DocumentUpdateTimestamp private Date ut;

    private String paperId;             // 试卷ID
    private List<String> questionIds;   // 题目ID列表
    private Date completedTime;         // 完成时间，存在值则已经完成测试

    public static AbilityExamBasic newInstance(Long userId, String paperId, List<String> questionIds) {
        AbilityExamBasic abilityExamBasic = new AbilityExamBasic();
        abilityExamBasic.setId(String.valueOf(userId));
        abilityExamBasic.setPaperId(paperId);
        abilityExamBasic.setQuestionIds(questionIds);

        return abilityExamBasic;
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{newCacheKey(id)};
    }

    public boolean fetchFinished() {
        return completedTime != null;
    }

}
