package com.voxlearning.utopia.service.newhomework.impl.template.report.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypeParameter;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.ReadReciteTypePart;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.TypePartContext;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class AppReadReciteWithScoreObjectiveConfigTypeProcessorTemplate extends AppReadReciteObjectiveConfigTypeProcessorTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.READ_RECITE_WITH_SCORE;
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

        //*********** end 初始化数据准备 *********** //


        //*********** begin ReadReciteTypePart 是返回数据结构 *********** //
        //1>设置类型数据
        //2>判断是否有学生完成
        /**
         * tapType 壳渲染方式
         * 1: 基础练习
         * 2：同步练习
         * 3：口算
         * 4：绘本
         * 5：朗读背诵
         * 6:
         * 7:字词讲练
         */
        ReadReciteTypePart readReciteTypePart = new ReadReciteTypePart();
        readReciteTypePart.setType(type);
        readReciteTypePart.setTypeName(type.getValue());
        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            result.put(type, readReciteTypePart);
            return;
        }
        //*********** end ReadReciteTypePart 是返回数据结构 *********** //


        List<String> lessonIds = new LinkedList<>();
        Map<String, NewHomeworkApp> appMap = new LinkedHashMap<>();
        for (NewHomeworkApp app : target.getApps()) {
            lessonIds.add(app.getLessonId());
            appMap.put(app.getQuestionBoxId(), app);
        }

        //newBookCatalogMap 准备放在这避免没必要的查询，前面有可能直接return
        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);

        // *********** begin 初始化ReadReciteTypePart.TabObject 是朗读背诵的每个课文的结构****//
        List<ReadReciteTypePart.TabObject> tabObjects = target.getApps()
                .stream()
                .map(o -> {
                    if (!newBookCatalogMap.containsKey(o.getLessonId())) {
                        return null;
                    }
                    ReadReciteTypePart.TabObject tabObject = new ReadReciteTypePart.TabObject();
                    ObjectiveConfigTypeParameter param = new ObjectiveConfigTypeParameter();
                    param.setQuestionBoxId(o.getQuestionBoxId());
                    String url = UrlUtils.buildUrlQuery("/view/reportv5/clazz/readrecitewithscoredetail",
                            MapUtils.m(
                                    "homeworkId", newHomework.getId(),
                                    "subject", newHomework.getSubject(),
                                    "type", type,
                                    "param", JsonUtils.toJson(param)));
                    tabObject.setTabName(SafeConverter.toString(newBookCatalogMap.get(o.getLessonId()).getName()));
                    tabObject.setQuestionBoxId(o.getQuestionBoxId());
                    tabObject.setQuestionBoxType(o.getQuestionBoxType());
                    tabObject.setUrl(url);
                    tabObject.setLessonId(o.getLessonId());
                    return tabObject;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // *********** end 初始化ReadReciteTypePart.TabObject 是朗读背诵的读，每个课文的结构****//

        Map<String, ReadReciteTypePart.TabObject> tabObjectMap = tabObjects.stream().collect(Collectors.toMap(ReadReciteTypePart.TabObject::getQuestionBoxId, Function.identity()));

        //******** begin newHomeworkResults 数据处理 *******//
        //tabObjectMap 的key 特殊对于着
        for (NewHomeworkResult r : newHomeworkResults) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = r.getPractices().get(type);
            if (newHomeworkResultAnswer != null && MapUtils.isNotEmpty(newHomeworkResultAnswer.getAppAnswers())) {
                for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : newHomeworkResultAnswer.getAppAnswers().entrySet()) {
                    if (!tabObjectMap.containsKey(entry.getKey()))
                        continue;
                    ReadReciteTypePart.TabObject tabObject = tabObjectMap.get(entry.getKey());
                    if (!appMap.containsKey(entry.getKey()))
                        continue;
                    NewHomeworkApp app = appMap.get(entry.getKey());
                    if (CollectionUtils.isEmpty(app.getQuestions()))
                        continue;
                    //判断是否达标
                    double value = new BigDecimal(SafeConverter.toInt(entry.getValue().getStandardNum()) * 100).divide(new BigDecimal(app.getQuestions().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    if (value >= NewHomeworkConstants.READ_RECITE_STANDARD) {
                        tabObject.setStandardNum(1 + tabObject.getStandardNum());
                    }
                }
            }
        }
        //******** end newHomeworkResults 数据处理 *******//

        // *** begin 后处理***//
        tabObjects.forEach(o -> o.setTabValue(o.getStandardNum() + "/" + newHomeworkResults.size() + "人达标"));
        // *** end 后处理***//


        //****** begin 构建 read Recite结构 *******//
        // 这部分似乎可以放在最开始，
        ReadReciteTypePart.ReadReciteType read = new ReadReciteTypePart.ReadReciteType();
        ReadReciteTypePart.ReadReciteType recite = new ReadReciteTypePart.ReadReciteType();
        read.setTabName("课文朗读");
        read.setQuestionBoxType(QuestionBoxType.READ);
        recite.setTabName("课文背诵");
        recite.setQuestionBoxType(QuestionBoxType.RECITE);
        for (ReadReciteTypePart.TabObject o : tabObjects) {
            if (o.getQuestionBoxType() == QuestionBoxType.READ) {
                read.getTabs().add(o);
                continue;
            }
            if (o.getQuestionBoxType() == QuestionBoxType.RECITE) {
                recite.getTabs().add(o);
            }
        }
        if (CollectionUtils.isNotEmpty(read.getTabs())) {
            readReciteTypePart.getTabs().add(read);
        }
        if (CollectionUtils.isNotEmpty(recite.getTabs())) {
            readReciteTypePart.getTabs().add(recite);
        }
        //****** end 构建 read Recite结构 *******//
        readReciteTypePart.setHasFinishUser(true);
        result.put(type, readReciteTypePart);
    }
}
