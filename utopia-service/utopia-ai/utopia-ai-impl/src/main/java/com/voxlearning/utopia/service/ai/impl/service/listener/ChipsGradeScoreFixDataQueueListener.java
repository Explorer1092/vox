package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.ai.cache.manager.ChipsGradeRankCacheManager;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.data.GradeReportConfig;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClass;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClassUserRef;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishClassPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishClassUserRefPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishProductTimetableDao;
import com.voxlearning.utopia.service.ai.internal.ChipsContentService;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 修复定级报告计算排名
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.user.grade.score.rank.fix.data.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.user.grade.score.rank.fix.data.queue")
        },
        maxPermits = 4
)
public class ChipsGradeScoreFixDataQueueListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private ChipsGradeRankCacheManager chipsGradeRankCacheManager;

    @Inject
    private ChipsEnglishClassUserRefPersistence chipsEnglishClassUserRefPersistence;

    @Inject
    private ChipsEnglishClassPersistence chipsEnglishClassPersistence;

    @Inject
    private ChipsEnglishProductTimetableDao chipsEnglishProductTimetableDao;

    @Inject
    private ChipsContentService chipsContentService;

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            logger.error("grade score handle queue no message");
            return;
        }
        Object body = message.decodeBody();

        if (body instanceof String) {
            String json = (String) body;
            Map<String, Object> param = JsonUtils.fromJson(json);
            if (param == null) {
                logger.error("ChipsGradeScoreFixDataQueueListener error. message:{}", body);
                return;
            }

            long clazzId = SafeConverter.toLong(param.get("C"));
            ChipsEnglishClass clazz = chipsEnglishClassPersistence.load(clazzId);
            if (clazz == null) {
                return;
            }
            ChipsEnglishProductTimetable chipsEnglishProductTimetable = chipsEnglishProductTimetableDao.load(clazz.getProductId());
            if (chipsEnglishProductTimetable == null || CollectionUtils.isEmpty(chipsEnglishProductTimetable.getCourses())) {
                return;
            }

            List<GradeReportConfig> cfgList = chipsContentService.loadGradeReportConfig();
            if (CollectionUtils.isEmpty(cfgList)) {
                return;
            }

            List<ChipsEnglishClassUserRef> classUserRefs = chipsEnglishClassUserRefPersistence.loadByClassId(clazzId);
            if (CollectionUtils.isEmpty(classUserRefs)) {
                return;
            }

            chipsGradeRankCacheManager.reset(clazzId);
            for(ChipsEnglishProductTimetable.Course course : chipsEnglishProductTimetable.getCourses()) {
                GradeReportConfig cfg = cfgList.stream().filter(e -> e.getBook().equals(course.getBookId())).findFirst().orElse(null);
                if (cfg == null || CollectionUtils.isEmpty(cfg.getUnits())) {
                    continue;
                }
                Set<String>  unitSet = cfg.getUnits().subList(0, Math.min(4, cfg.getUnits().size())).stream().collect(Collectors.toSet());
                if (!unitSet.contains(course.getUnitId())) {
                    continue;
                }
                classUserRefs.forEach(userRef -> {
                    int score = chipsContentService.processScoreByLessonType(userRef.getUserId(), Collections.singleton(course.getUnitId()), LessonType.video_conversation, LessonType.task_conversation, LessonType.Dialogue, LessonType.Task);
                    if (score > 0) {
                        chipsGradeRankCacheManager.updateRank(userRef.getUserId(), clazzId, score);
                    }
                });
            }

        }
    }
}
