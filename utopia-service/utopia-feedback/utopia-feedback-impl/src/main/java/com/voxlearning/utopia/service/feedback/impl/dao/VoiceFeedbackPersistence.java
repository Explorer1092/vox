package com.voxlearning.utopia.service.feedback.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.feedback.api.entities.ExamFeedback;
import com.voxlearning.utopia.service.feedback.api.entities.VoiceFeedback;

import javax.inject.Named;
import java.util.Collection;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/9/17
 * \* Time: 下午2:36
 * \* Description:老师端作业报告增加语音题目反馈
 * \
 */
@Named("com.voxlearning.utopia.service.feedback.impl.dao.VoiceFeedbackPersistence")
@CacheBean(type = VoiceFeedback.class)
public class VoiceFeedbackPersistence extends AlpsStaticJdbcDao<VoiceFeedback, Long> {
    @Override
    protected void calculateCacheDimensions(VoiceFeedback document, Collection<String> dimensions) {
        dimensions.add(VoiceFeedback.ck_id(document.getId()));
    }
}
