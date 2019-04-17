package com.voxlearning.utopia.service.newhomework.api.entity.bonus;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.newhomework.api.context.bonus.QuestionDataAnswer;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 能力测评答案详细数据
 *
 * @author lei.liu
 * @version 18-10-31
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-siberia")
@DocumentDatabase(database = "vox-bonus-collect")
@DocumentCollection(collection = "vox_ability_exam_answer_{}", dynamic = true)  // 按学生id尾号分表
@UtopiaCacheExpiration
@UtopiaCacheRevision(value = "20181031")
public class AbilityExamAnswer implements CacheDimensionDocument {
    private static final long serialVersionUID = 158556525725935876L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE) private String id;    // studentId-questionId
    @DocumentCreateTimestamp private Date ct;
    @DocumentUpdateTimestamp private Date ut;

    private Integer grade;
    private String paperId;
    private String questionId;
    private Boolean grasp;
    private List<List<Boolean>> subGrasp;
    private QuestionDataAnswer questionDataAnswer;

    public static AbilityExamAnswer newInstance(Long userId, String questionId, String paperId, Integer grade, Boolean grasp, List<List<Boolean>> subGrasp, QuestionDataAnswer questionDataAnswer) {
        AbilityExamAnswer abilityExamAnswer = new AbilityExamAnswer();
        abilityExamAnswer.setId(userId + "-" + questionId);

        abilityExamAnswer.setPaperId(paperId);
        abilityExamAnswer.setQuestionId(questionId);
        abilityExamAnswer.setGrade(grade);
        abilityExamAnswer.setGrasp(grasp);
        abilityExamAnswer.setSubGrasp(subGrasp);
        abilityExamAnswer.setQuestionDataAnswer(questionDataAnswer);
        return abilityExamAnswer;
    }

    public Long fetchUserId() {
        return SafeConverter.toLong(StringUtils.split(id, "-")[0]);
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{newCacheKey(id)};
    }
}
