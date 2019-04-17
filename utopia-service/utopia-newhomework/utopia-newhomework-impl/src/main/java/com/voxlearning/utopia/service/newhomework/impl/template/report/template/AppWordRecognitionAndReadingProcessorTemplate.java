package com.voxlearning.utopia.service.newhomework.impl.template.report.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypeParameter;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.TypePartContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.WordRecognitionAndReadingTypePart;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/7/23
 * \* Time: 下午3:37
 * \* Description: 处理生字认读报告模板实现
 * \
 */
@Named
public class AppWordRecognitionAndReadingProcessorTemplate extends AppReadReciteObjectiveConfigTypeProcessorTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.WORD_RECOGNITION_AND_READING;
    }
    @Override
    public void fetchTypePart(TypePartContext typePartContext) {
        //*********** begin 初始化数据准备 *********** //
        /**
         * 1:type =》 类型
         * 2:result =》 返回数据
         * 3:newHomeworkResults =》 完成该类型的中间表数据
         * 4:newHomework
         * 5:newBookCatalogMap
         */
        ObjectiveConfigType type = typePartContext.getType();
        Map<ObjectiveConfigType, Object> result = typePartContext.getResult();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = typePartContext.getNewHomeworkResultMap();
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap.values().stream().filter(o -> o.isFinishedOfObjectiveConfigType(type)).collect(Collectors.toList());
        NewHomework newHomework = typePartContext.getNewHomework();
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);

        WordRecognitionAndReadingTypePart wordRecognitionAndReadingTypePart = new WordRecognitionAndReadingTypePart();
        wordRecognitionAndReadingTypePart.setType(type);
        wordRecognitionAndReadingTypePart.setTypeName(type.getValue());
        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            result.put(type, wordRecognitionAndReadingTypePart);
            return;
        }
        //*********** end ReadReciteTypePart 是返回数据结构 *********** //
        List<String> lessonIds = new LinkedList<>();
        Map<String, NewHomeworkApp> appMap = new LinkedHashMap<>();
        for (NewHomeworkApp app : target.getApps()) {
            lessonIds.add(app.getLessonId());
            appMap.put(app.getQuestionBoxId(), app);
        }
        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
        List<WordRecognitionAndReadingTypePart.TabObject> tabObjects = target.getApps()
                .stream()
                .map(o -> {
                    if (!newBookCatalogMap.containsKey(o.getLessonId())) {
                        return null;
                    }
                    WordRecognitionAndReadingTypePart.TabObject tabObject = new WordRecognitionAndReadingTypePart.TabObject();
                    ObjectiveConfigTypeParameter param = new ObjectiveConfigTypeParameter();
                    param.setQuestionBoxId(o.getQuestionBoxId());
                    String url = UrlUtils.buildUrlQuery("/view/reportv5/clazz/wordrecognitionandreadingdetail",
                            MapUtils.m(
                                    "homeworkId", newHomework.getId(),
                                    "subject", newHomework.getSubject(),
                                    "type", type,
                                    "param", JsonUtils.toJson(param)));
                    tabObject.setTabName(SafeConverter.toString(newBookCatalogMap.get(o.getLessonId()).getName()));
                    tabObject.setQuestionBoxId(o.getQuestionBoxId());
                    tabObject.setUrl(url);
                    tabObject.setLessonId(o.getLessonId());
                    return tabObject;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<String, WordRecognitionAndReadingTypePart.TabObject> tabObjectMap = tabObjects.stream().collect(Collectors.toMap(WordRecognitionAndReadingTypePart.TabObject::getQuestionBoxId, Function.identity()));
        //******** begin newHomeworkResults 数据处理 *******//
        for (NewHomeworkResult r : newHomeworkResults) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = r.getPractices().get(type);
            if (newHomeworkResultAnswer != null && MapUtils.isNotEmpty(newHomeworkResultAnswer.getAppAnswers())) {
                for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : newHomeworkResultAnswer.getAppAnswers().entrySet()) {
                    if (!tabObjectMap.containsKey(entry.getKey()))
                        continue;
                    WordRecognitionAndReadingTypePart.TabObject tabObject = tabObjectMap.get(entry.getKey());
                    if (!appMap.containsKey(entry.getKey()))
                        continue;
                    NewHomeworkApp app = appMap.get(entry.getKey());
                    if (CollectionUtils.isEmpty(app.getQuestions()))
                        continue;
                    //判断是否达标：一篇中生字达标率达到80％以上，即该篇达标，否则未达标
                    double value = new BigDecimal(SafeConverter.toInt(entry.getValue().getStandardNum()) * 100).divide(new BigDecimal(app.getQuestions().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    if (value >= NewHomeworkConstants.WORD_RECOGNITION_AND_READING_STANDARD) {
                        tabObject.setStandardNum(1 + tabObject.getStandardNum());
                    }
                }
            }
        }
        //******** end newHomeworkResults 数据处理 *******//
        tabObjects.forEach(o -> o.setTabValue(o.getStandardNum() + "/" + newHomeworkResults.size() + "人达标"));
        // 兼容课文读背结构，避免移动端再新加一种模板
        WordRecognitionAndReadingTypePart.WordRecognitionType wordRecognitionType = new WordRecognitionAndReadingTypePart.WordRecognitionType();
        wordRecognitionType.setTabName("生字");
        wordRecognitionType.setTabs(tabObjects);
        wordRecognitionAndReadingTypePart.setTabs(Collections.singletonList(wordRecognitionType));
        wordRecognitionAndReadingTypePart.setHasFinishUser(true);
        result.put(type, wordRecognitionAndReadingTypePart);
    }
}
