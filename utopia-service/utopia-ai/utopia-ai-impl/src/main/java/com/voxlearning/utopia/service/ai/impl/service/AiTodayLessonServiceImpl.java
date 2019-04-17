package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.api.AiTodayLessonService;
import com.voxlearning.utopia.service.ai.constant.ChipsActiveServiceType;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.ai.impl.persistence.*;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;


@Named
@ExposeServices({
        @ExposeService(interfaceClass = AiTodayLessonService.class, version = @ServiceVersion(version = "20180803")),
        @ExposeService(interfaceClass = AiTodayLessonService.class, version = @ServiceVersion(version = "20181115"))
})
public class AiTodayLessonServiceImpl implements AiTodayLessonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiTodayLessonServiceImpl.class);

    private static final String DEFAULT_BOOK_ID = "BK_10300003451674";

    @Inject
    private AITodayLessonDao aiTodayLessonDao;
    @Inject
    private AIActiveServiceTemplateDao aiActiveServiceTemplateDao;
    @Inject
    private ActiveServiceQuestionTemplateDao activeServiceQuestionTemplateDao;
    @Inject
    private ActiveServiceUserQuestionTemplateDao activeServiceUserQuestionTemplateDao;
    @Inject
    private ChipsActiveServiceRecordDao chipsActiveServiceRecordDao;
    @Inject
    private ChipsEnglishClassUserRefPersistence chipsEnglishClassUserRefPersistence;
    @Inject
    private ChipsEnglishClassPersistence chipsEnglishClassPersistence;
    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    private ChipsActiveServiceImpl chipsActiveService;

    @Override
    public AITodayLesson load(String id) {
        if (StringUtils.isNotBlank(id)) {
            return aiTodayLessonDao.load(id);
        }
        return null;
    }

    @Override
    public List<AITodayLesson> findByUnitId(String unitId) {
        if (StringUtils.isNotBlank(unitId)) {
            return aiTodayLessonDao.loadByUnitId(unitId);
        }
        return Collections.emptyList();
    }

    @Override
    public MapMessage save(AITodayLesson po) {

        if (po == null) {
            return MapMessage.errorMessage("数据不能为空");
        }


        if (StringUtils.isBlank(po.getBookId())) {
            po.setBookId(DEFAULT_BOOK_ID);
        }


        if (po.getId() == null) {
            List list = findByUnitId(po.getUnitId());
            if (list.size() > 0) {
                return MapMessage.errorMessage("unit id 已经存在");
            }

            aiTodayLessonDao.insert(po);
        } else {
            AITodayLesson oldPo = load(po.getId());

            if (oldPo != null) {
                if (!StringUtils.equals(oldPo.getUnitId(), po.getUnitId())) {
                    // check unitId
                    List list = findByUnitId(po.getUnitId());
                    if (list.size() > 0) {
                        return MapMessage.errorMessage("unit id 已经存在");
                    }
                }
                aiTodayLessonDao.replace(po);
            } else {
                return MapMessage.errorMessage("记录不存在");
            }
        }
        return MapMessage.successMessage();
    }

    @Override
    public boolean remove(String id) {
        return aiTodayLessonDao.remove(id);
    }

    @Override
    public AIActiveServiceTemplate loadAIActiveServiceTemplate(String id) {
        return aiActiveServiceTemplateDao.load(id);
    }

    @Override
    public AIActiveServiceTemplate loadAIActiveServiceTemplateByBookIdUnitId(String bookId, String unitId) {
        List<AIActiveServiceTemplate> templateList = aiActiveServiceTemplateDao.loadByBookIdAndUnitId(bookId, unitId);
        if (CollectionUtils.isEmpty(templateList)) {
            return null;
        }
        return templateList.get(0);
    }

    @Override
    public MapMessage saveAIActiveServiceTemplate(AIActiveServiceTemplate template) {
        if (template == null) {
            return MapMessage.errorMessage("数据不能为空");
        }
        if (StringUtils.isBlank(template.getBookId())) {
            return MapMessage.errorMessage("bookId 不能为空");
        }
        if (StringUtils.isBlank(template.getUnitId())) {
            return MapMessage.errorMessage("unitId 不能为空");
        }
        if (template.getId() == null) {
            List<AIActiveServiceTemplate> templateList = aiActiveServiceTemplateDao.loadByBookIdAndUnitId(template.getBookId(), template.getUnitId());
            if (CollectionUtils.isNotEmpty(templateList)) {
                return MapMessage.errorMessage("(bookId,unitId) 已经存在");
            }
            aiActiveServiceTemplateDao.insert(template);
        } else {
            AIActiveServiceTemplate old = aiActiveServiceTemplateDao.load(template.getId());
            if (old != null) {
                if (!StringUtils.equals(old.getUnitId(), template.getUnitId()) || !StringUtils.equals(old.getBookId(), template.getBookId())) {
                    // check unitId
                    List list = aiActiveServiceTemplateDao.loadByBookIdAndUnitId(template.getBookId(), template.getUnitId());
                    if (list.size() > 0) {
                        return MapMessage.errorMessage("(booId, unitId) 已经存在");
                    }
                }
                aiActiveServiceTemplateDao.replace(template);
            } else {
                return MapMessage.errorMessage("记录不存在");
            }
        }
        return MapMessage.successMessage();

    }

    @Override
    public boolean removeAIActiveServiceTemplate(String id) {
        return aiActiveServiceTemplateDao.remove(id);
    }

    @Override
    public List<Map<String, Object>> findByBookIdUnitId(String bookId, String unitId) {
        List<AITodayLesson> aiTodayLessonList;
        List<AIActiveServiceTemplate> templateList;
        if (StringUtils.isBlank(bookId) && StringUtils.isBlank(unitId)) {
            aiTodayLessonList = aiTodayLessonDao.loadByBookId(DEFAULT_BOOK_ID);
            templateList = aiActiveServiceTemplateDao.query();
        } else if (StringUtils.isBlank(bookId)) {
            aiTodayLessonList = aiTodayLessonDao.loadByUnitId(unitId);
            templateList = aiActiveServiceTemplateDao.loadByUnitId(unitId);
        } else if (StringUtils.isBlank(unitId)) {
            aiTodayLessonList = aiTodayLessonDao.loadByBookId(bookId);
            templateList = aiActiveServiceTemplateDao.loadByBookId(bookId);
        } else {
            aiTodayLessonList = aiTodayLessonDao.loadByBookIdAndUnitId(bookId, unitId);
            templateList = aiActiveServiceTemplateDao.loadByBookIdAndUnitId(bookId, unitId);
        }
        return merge(aiTodayLessonList, templateList);
    }

    @NotNull
    private List<Map<String, Object>> merge(List<AITodayLesson> aiTodayLessonList, List<AIActiveServiceTemplate> templateList) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(aiTodayLessonList)) {
            aiTodayLessonList.forEach(l -> {
                Map<String, Object> m = new HashMap<>();
                m.put("bookName", "旅行口语");
                m.put("unitId", l.getUnitId());
                m.put("title", l.getTitle());
                m.put("updateDate", DateUtils.dateToString(l.getUpdateDate(), DateUtils.FORMAT_SQL_DATETIME));
                m.put("createDate", DateUtils.dateToString(l.getCreateDate(), DateUtils.FORMAT_SQL_DATETIME));
                m.put("type", "short");
                m.put("id", l.getId());
                list.add(m);
            });
        }
        if (CollectionUtils.isNotEmpty(templateList)) {
            templateList.forEach(t -> {
                Map<String, Object> m = new HashMap<>();
                m.put("bookName", t.getBookName());
                m.put("unitId", t.getUnitId());
                m.put("title", t.getTitle());
                m.put("updateDate", DateUtils.dateToString(t.getUpdateDate(), DateUtils.FORMAT_SQL_DATETIME));
                m.put("createDate", DateUtils.dateToString(t.getCreateDate(), DateUtils.FORMAT_SQL_DATETIME));
                m.put("type", "official");
                m.put("id", t.getId());
                list.add(m);
            });
        }
        return list;
    }

    @Override
    public ActiveServiceQuestionTemplate loadActiveServiceQuestionTemplateById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return activeServiceQuestionTemplateDao.load(id);
    }

    @Override
    public Map<String, ActiveServiceQuestionTemplate> loadActiveServiceQuestionTemplateByIds(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        Map<String, ActiveServiceQuestionTemplate> map = activeServiceQuestionTemplateDao.loads(ids);
        if (map == null) {
            return Collections.emptyMap();
        }
        return map;
    }

    @Override
    public MapMessage saveActiveServiceQuestionTemplate(ActiveServiceQuestionTemplate template) {
        if (template == null) {
            return MapMessage.errorMessage("数据不能为空");
        }
        activeServiceQuestionTemplateDao.upsert(template);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage deleteActiveServiceUserQuestionTemplate(String userId, String qid) {
        activeServiceUserQuestionTemplateDao.remove(userId + "-" + qid);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage saveActiveServiceUserQuestionTemplate(ActiveServiceUserQuestionTemplate template) {
        activeServiceUserQuestionTemplateDao.upsert(template);

        try {
            Long userId = Long.parseLong(template.getUserId());
            Long classId = chipsActiveService.obtainClassId(userId, template.getBookId());
            chipsActiveServiceRecordDao.updateToSerivced(ChipsActiveServiceType.SERVICE, classId, userId, template.getUnitId());
        } catch (Exception e) {
            LOGGER.error("try to update chipsActiveServiceRecord error, type=" + ChipsActiveServiceType.SERVICE.getDesc() +", userId=" + template.getUserId() +
                    ", bookId=" + template.getBookId() + ", unitId=" + template.getUnitId(), e);
        }

        return MapMessage.successMessage();
    }

    @Override
    public ActiveServiceUserQuestionTemplate queryActiveServiceUserQuestionTemplateByUserIdQid(Long userId, String qid) {
        return activeServiceUserQuestionTemplateDao.load(userId + "-" + qid);
    }

}
