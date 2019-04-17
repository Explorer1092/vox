

package com.voxlearning.utopia.service.newhomework.impl.template.report.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypeParameter;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.BasicAppTypePart;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.TypePartContext;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class AppBasicAppObjectiveConfigTypeProcessorTemplate extends AppObjectiveConfigTypeProcessorTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.BASIC_APP;
    }

    @Override
    public void fetchTypePart(TypePartContext typePartContext) {


        //*********** begin 初始化数据准备 *********** //
        /**
         * 1:type =》 类型
         * 2:result =》 返回数据
         * 3:newHomeworkResults =》 完成该类型的中间表数据
         */
        ObjectiveConfigType type = typePartContext.getType();
        Map<ObjectiveConfigType, Object> result = typePartContext.getResult();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = typePartContext.getNewHomeworkResultMap();
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap.values().stream().filter(o -> o.isFinishedOfObjectiveConfigType(type)).collect(Collectors.toList());
        //*********** end 初始化数据准备 *********** //


        //*********** begin BasicAppTypePart 是返回数据结构 *********** //
        //1>设置类型数据
        //2>判断是否有学生完成
        /**
         * tapType 壳渲染方式
         * 1: 基础练习
         * 2：同步练习
         * 3：口算
         * 4：绘本
         * 5：朗读背诵
         */
        BasicAppTypePart basicAppTypePart = new BasicAppTypePart();
        basicAppTypePart.setType(type);
        basicAppTypePart.setTypeName(type.getValue());
        basicAppTypePart.setShowScore(true);
        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            basicAppTypePart.setSubContent("班平均分-- 平均用时--");
            result.put(type, basicAppTypePart);
            return;
        }
        //*********** end BasicAppTypePart 是返回数据结构 *********** //

        NewHomework newHomework = typePartContext.getNewHomework();
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);


        //*********** begin BasicAppTypePart.Category 是对应category数据  *********** //
        //自然拼读的地址需要特殊处理

        List<BasicAppTypePart.Category> categories = target.getApps()
                .stream()
                .map(o -> {
                    BasicAppTypePart.Category category = new BasicAppTypePart.Category();
                    category.setLessonId(o.getLessonId());
                    category.setCategoryId(o.getCategoryId());
                    PracticeType practiceType = practiceLoaderClient.loadPractice(o.getPracticeId());
                    if (practiceType != null) {
                        category.setTabName(SafeConverter.toString(practiceType.getCategoryName()));
                    }
                    category.setKey(o.getCategoryId() + "-" + o.getLessonId());
                    ObjectiveConfigTypeParameter param = new ObjectiveConfigTypeParameter();
                    param.setCategoryId(o.getCategoryId());
                    param.setLessonId(o.getLessonId());
                    String url;
                    if (type == ObjectiveConfigType.NATURAL_SPELLING) {
                        url = UrlUtils.buildUrlQuery("/view/reportv5/clazz/naturalspellingdetail",
                                MapUtils.m(
                                        "homeworkId", newHomework.getId(),
                                        "type", type,
                                        "subject", newHomework.getSubject(),
                                        "param", JsonUtils.toJson(param)));
                    } else {
                        url = UrlUtils.buildUrlQuery("/view/reportv5/clazz/basicappdetail",
                                MapUtils.m(
                                        "homeworkId", newHomework.getId(),
                                        "type", type,
                                        "subject", newHomework.getSubject(),
                                        "param", JsonUtils.toJson(param)));
                    }
                    category.setUrl(url);
                    return category;
                })
                .collect(Collectors.toList());
        //*********** end BasicAppTypePart.Category 是对应category数据  *********** //

        Map<String, BasicAppTypePart.Category> categoryMap = categories.stream().collect(Collectors.toMap(BasicAppTypePart.Category::getKey, Function.identity()));
        int totalScore = 0;
        long totalDuration = 0;
        //********** begin newHomeworkResults 数据处理 *******//
        // categoryMap key 特殊，用于获取得到category；来记录数据
        for (NewHomeworkResult r : newHomeworkResults) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = r.getPractices().get(type);
            totalScore += SafeConverter.toInt(newHomeworkResultAnswer.processScore(type));
            totalDuration += SafeConverter.toLong(newHomeworkResultAnswer.processDuration());
            for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : newHomeworkResultAnswer.getAppAnswers().entrySet()) {
                if (categoryMap.containsKey(entry.getKey())) {
                    BasicAppTypePart.Category category = categoryMap.get(entry.getKey());
                    category.setNum(1 + category.getNum());
                    category.setTotalScore(category.getTotalScore() + SafeConverter.toDouble(entry.getValue().getScore()));
                }
            }
        }
        //********** end newHomeworkResults 数据处理 *******//

        //处理category的分数
        categories.stream()
                .filter(o -> o.getNum() > 0)
                .forEach(o -> {
                    int averScore1 = new BigDecimal(o.getTotalScore()).divide(new BigDecimal(o.getNum()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                    o.setAverScore(averScore1);
                    o.setTabValue(averScore1 + "分");
                });

        //*************** begin 三层树结构  **************//
        //TODO 用新的pc构建数据的方式去构建，阅读性和安全性更好
        //1、category
        //2、lesson
        //3、unit

        //******* begin 构建 lesson 结构:lessonMap*******//
        Map<String, List<BasicAppTypePart.Category>> lessonMap = new LinkedHashMap<>();

        for (NewHomeworkApp app : target.getApps()) {
            String key = app.getCategoryId() + "-" + app.getLessonId();
            lessonMap.computeIfAbsent(app.getLessonId(), m -> new LinkedList<>()).add(categoryMap.get(key));
        }
        //******* end 构建 lesson 结构*******//


        //******* begin 构建 unit 结构 :unitData*******//


        Map<String, NewBookCatalog> ms = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonMap.keySet());

        Map<String, String> lineData = NewHomeworkUtils.handleLessonIdToUnitId(ms);
        Map<String, List<BasicAppTypePart.Lesson>> unitData = new LinkedHashMap<>();
        for (String lessonId : lessonMap.keySet()) {
            BasicAppTypePart.Lesson lesson = new BasicAppTypePart.Lesson();
            lesson.setLessonId(lessonId);
            lesson.setLessonName(ms.get(lessonId).getAlias());
            lesson.setTabs(lessonMap.get(lessonId));
            unitData.computeIfAbsent(lineData.get(lessonId), m -> new LinkedList<>()).add(lesson);
        }

        ms = newContentLoaderClient.loadBookCatalogByCatalogIds(unitData.keySet());
        for (String ky : unitData.keySet()) {
            BasicAppTypePart.Unit unit = new BasicAppTypePart.Unit();
            unit.setUnitId(ky);
            unit.setUnitName(ms.containsKey(ky) ? ms.get(ky).getAlias() : "单元");
            unit.setLessons(unitData.get(ky));
            basicAppTypePart.getUnits().add(unit);
        }
        //******* end 构建 unit 结构*******//

        //*************** end 三层树结构 category lesson unit **************//


        int averScore = new BigDecimal(totalScore).divide(new BigDecimal(newHomeworkResults.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();
        long averDuration = new BigDecimal(totalDuration).divide(new BigDecimal(60 * newHomeworkResults.size()), 0, BigDecimal.ROUND_UP).longValue();
        basicAppTypePart.setAverScore(averScore);
        basicAppTypePart.setAverDuration(averDuration);
        basicAppTypePart.setSubContent("班平均分" + averScore + " 平均用时" + averDuration + "min");
        basicAppTypePart.setHasFinishUser(true);
        result.put(type, basicAppTypePart);


    }
}
