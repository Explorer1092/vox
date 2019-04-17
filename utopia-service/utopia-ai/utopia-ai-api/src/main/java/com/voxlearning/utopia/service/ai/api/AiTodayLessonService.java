package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.entity.AIActiveServiceTemplate;
import com.voxlearning.utopia.service.ai.entity.AITodayLesson;
import com.voxlearning.utopia.service.ai.entity.ActiveServiceQuestionTemplate;
import com.voxlearning.utopia.service.ai.entity.ActiveServiceUserQuestionTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@ServiceVersion(version = "20181115")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface AiTodayLessonService extends IPingable {

    AITodayLesson load(String id);

    List<AITodayLesson> findByUnitId(String unitId);

    MapMessage save(AITodayLesson po);

    boolean remove(String id);

    AIActiveServiceTemplate loadAIActiveServiceTemplate(String id);

    AIActiveServiceTemplate loadAIActiveServiceTemplateByBookIdUnitId(String bookId, String unitId);

    MapMessage saveAIActiveServiceTemplate(AIActiveServiceTemplate template);

    boolean removeAIActiveServiceTemplate(String id);

    List<Map<String, Object>> findByBookIdUnitId(String bookId, String unitId);

    ActiveServiceQuestionTemplate loadActiveServiceQuestionTemplateById(String id);

    Map<String, ActiveServiceQuestionTemplate> loadActiveServiceQuestionTemplateByIds(Collection<String> ids);

    MapMessage saveActiveServiceQuestionTemplate(ActiveServiceQuestionTemplate template);

    MapMessage deleteActiveServiceUserQuestionTemplate(String userId, String qid);

    MapMessage saveActiveServiceUserQuestionTemplate(ActiveServiceUserQuestionTemplate template);

    ActiveServiceUserQuestionTemplate queryActiveServiceUserQuestionTemplateByUserIdQid(Long userId, String qid);
}
