package com.voxlearning.utopia.service.newhomework.impl.template.report.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.wordspractice.ImageTextRhymeHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypeParameter;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.TypePartContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.WordTeachAndPracticeTypePart;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 语文-字词讲练
 * @author: Mr_VanGogh
 * @date: 2018/12/24 下午2:08
 */
@Named
public class AppWordTeachAndPracticeObjectiveConfigTypeProcessorTemplate extends AppObjectiveConfigTypeProcessorTemplate {

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.WORD_TEACH_AND_PRACTICE;
    }

    @Override
    public void fetchTypePart(TypePartContext typePartContext) {
        ObjectiveConfigType type = typePartContext.getType();
        NewHomework newHomework = typePartContext.getNewHomework();
        Map<ObjectiveConfigType, Object> result = typePartContext.getResult();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = typePartContext.getNewHomeworkResultMap();
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        WordTeachAndPracticeTypePart wordTeachAndPracticeTypePart = new WordTeachAndPracticeTypePart();
        wordTeachAndPracticeTypePart.setType(type);
        wordTeachAndPracticeTypePart.setTypeName(type.getValue());
        wordTeachAndPracticeTypePart.setShowScore(true);
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap
                .values()
                .stream()
                .filter(Objects::nonNull)
                .filter(o -> o.isFinishedOfObjectiveConfigType(type))
                .collect(Collectors.toList());

        //章节信息
        Set<String> sectionIds = target.getApps()
                .stream()
                .filter(n -> n.getSectionId() != null)
                .map(NewHomeworkApp::getSectionId)
                .collect(Collectors.toSet());
        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(sectionIds);

        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            wordTeachAndPracticeTypePart.setSubContent("班平均分-- 完成人数--");
            List<WordTeachAndPracticeTypePart.WordTeachSectionDataPart> wordTeachStoneDataParts = new LinkedList<>();
            for (NewHomeworkApp newHomeworkApp : target.getApps()) {
                WordTeachAndPracticeTypePart.WordTeachSectionDataPart wordTeachStoneDataPart;
                // 课时&题包关系重新绑定，同一课时下，课时名称只出现在第一个题包的第一个模块
                String sectionId = newHomeworkApp.getSectionId();
                boolean containWordExercise = CollectionUtils.isNotEmpty(newHomeworkApp.getWordExerciseQuestions());
                boolean containImageTextRhyme = CollectionUtils.isNotEmpty(newHomeworkApp.getImageTextRhymeQuestions());
                boolean containChineseCharacterCultureCourse = CollectionUtils.isNotEmpty(newHomeworkApp.getChineseCharacterCultureCourseIds());
                int j = 0;
                if (containWordExercise) {
                    j = 1;
                }
                if (!containWordExercise && containImageTextRhyme) {
                    j = 2;
                }
                if (!containWordExercise && !containImageTextRhyme & containChineseCharacterCultureCourse) {
                    j = 3;
                }

                String sectionName = "";
                if (MapUtils.isNotEmpty(newBookCatalogMap) && newBookCatalogMap.get(sectionId) != null) {
                    sectionName = newBookCatalogMap.get(sectionId).getName();
                }

                // 字词训练
                if (containWordExercise) {
                    wordTeachStoneDataPart = new WordTeachAndPracticeTypePart.WordTeachSectionDataPart();
                    wordTeachStoneDataPart.setTypeName("字词训练");
                    wordTeachStoneDataPart.setTapType(2);
                    wordTeachStoneDataPart.setShowUrl(true);
                    wordTeachStoneDataPart.setShowScore(false);
                    wordTeachStoneDataPart.setSubContent("班平均分-- 平均用时--");
                    wordTeachStoneDataPart.setRateContent("题目正确率：统计首次作答及做错干预讲解后正确率");
                    wordTeachStoneDataPart.setTitle(sectionName);
                    wordTeachStoneDataParts.add(wordTeachStoneDataPart);
                }
                // 图文入韵
                if (containImageTextRhyme) {
                    wordTeachStoneDataPart = new WordTeachAndPracticeTypePart.WordTeachSectionDataPart();
                    wordTeachStoneDataPart.setTypeName("图文入韵");
                    wordTeachStoneDataPart.setTapType(5);
                    wordTeachStoneDataPart.setShowUrl(false);
                    if (j == 2) {
                        wordTeachStoneDataPart.setTitle(sectionName);
                    }
                    wordTeachStoneDataParts.add(wordTeachStoneDataPart);
                }
                // 汉字文化
                if (containChineseCharacterCultureCourse) {
                    wordTeachStoneDataPart = new WordTeachAndPracticeTypePart.WordTeachSectionDataPart();
                    wordTeachStoneDataPart.setTypeName("汉字文化");
                    wordTeachStoneDataPart.setTapType(5);
                    wordTeachStoneDataPart.setShowUrl(false);
                    if (j == 3) {
                        wordTeachStoneDataPart.setTitle(sectionName);
                    }
                    wordTeachStoneDataParts.add(wordTeachStoneDataPart);
                }
            }
            wordTeachAndPracticeTypePart.setTypes(wordTeachStoneDataParts);
            result.put(type, wordTeachAndPracticeTypePart);
            return;
        }


        String homeworkId = newHomework.getId();
        //不同题包的完成人数
        Map<String, Integer> stoneIdFinishStudentMap = new HashMap<>();
        for (NewHomeworkResult newHomeworkResult : newHomeworkResults) {
            if (MapUtils.isEmpty(newHomeworkResult.getPractices().get(type).getAppAnswers())) {
                continue;
            }
            LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = newHomeworkResult.getPractices().get(type).getAppAnswers();
            for (NewHomeworkResultAppAnswer appAnswer : appAnswers.values()) {
                if (!appAnswer.isFinished()) {
                    continue;
                }
                String stoneId = appAnswer.getStoneId();
                if (stoneIdFinishStudentMap.get(stoneId) != null) {
                    stoneIdFinishStudentMap.put(stoneId, stoneIdFinishStudentMap.get(stoneId) + 1);
                } else {
                    stoneIdFinishStudentMap.put(appAnswer.getStoneId(), 1);
                }
            }
        }

        // 课时 & 题包的关系
        LinkedHashMap<String, Set<String>> sectionStoneMap = new LinkedHashMap<>();
        for (NewHomeworkApp newHomeworkApp : target.getApps()) {
            Set<String> stoneIds = new HashSet<>();
            if (sectionStoneMap.get(newHomeworkApp.getSectionId()) != null) {
                stoneIds = sectionStoneMap.get(newHomeworkApp.getSectionId());
                stoneIds.add(newHomeworkApp.getStoneDataId());
                sectionStoneMap.put(newHomeworkApp.getSectionId(), stoneIds);
            } else {
                stoneIds.add(newHomeworkApp.getStoneDataId());
                sectionStoneMap.put(newHomeworkApp.getSectionId(), stoneIds);
            }
        }


        //汉子文化 课程名称
        Set<String> courseIds = target.getApps()
                .stream()
                .filter(n -> CollectionUtils.isNotEmpty(n.getChineseCharacterCultureCourseIds()))
                .map(NewHomeworkApp::getChineseCharacterCultureCourseIds)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        Map<String, IntelDiagnosisCourse> courseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(courseIds);

        // 题包信息
        List<WordTeachAndPracticeTypePart.WordTeachSectionDataPart> wordTeachStoneDataParts = new LinkedList<>();
        for (NewHomeworkApp newHomeworkApp : target.getApps()) {
            String stoneId = newHomeworkApp.getStoneDataId();
            if (stoneIdFinishStudentMap.get(stoneId) == null) {
                continue;
            }
            WordTeachAndPracticeTypePart.WordTeachSectionDataPart wordTeachStoneDataPart;

            // TODO: 2019/1/8 正常情况下，不会出现一个课时下边挂两个题包的现象。这里需要针对特殊情况做处理
            // 课时&题包关系重新绑定，同一课时下，课时名称只出现在第一个题包的第一个模块
            String sectionId = newHomeworkApp.getSectionId();
            boolean containWordExercise = CollectionUtils.isNotEmpty(newHomeworkApp.getWordExerciseQuestions());
            boolean containImageTextRhyme = CollectionUtils.isNotEmpty(newHomeworkApp.getImageTextRhymeQuestions());
            boolean containChineseCharacterCultureCourse = CollectionUtils.isNotEmpty(newHomeworkApp.getChineseCharacterCultureCourseIds());
            int j = 0;
            if (containWordExercise) {
                j = 1;
            }
            if (!containWordExercise && containImageTextRhyme) {
                j = 2;
            }
            if (!containWordExercise && !containImageTextRhyme & containChineseCharacterCultureCourse) {
                j = 3;
            }

            String sectionName = "";
            if (MapUtils.isNotEmpty(newBookCatalogMap) && newBookCatalogMap.get(sectionId) != null) {
                sectionName = newBookCatalogMap.get(sectionId).getName();
            }

            // 字词训练
            if (containWordExercise) {
                wordTeachStoneDataPart = new WordTeachAndPracticeTypePart.WordTeachSectionDataPart();
                List<NewHomeworkQuestion> wordExerciseQuestions = newHomeworkApp.getWordExerciseQuestions();

                Map<String, WordTeachAndPracticeTypePart.WordExerciseQuestion> map = wordExerciseQuestions.stream()
                        .collect(Collectors.toMap(NewHomeworkQuestion::getQuestionId, o -> {
                            WordTeachAndPracticeTypePart.WordExerciseQuestion question = new WordTeachAndPracticeTypePart.WordExerciseQuestion();
                            question.setQid(o.getQuestionId());
                            return question;
                        }));
                Map<String, NewHomeworkProcessResult> newHomeworkProcessResultMap = typePartContext.getNewHomeworkProcessResultMap();
                double totalScore = 0d;
                long totalDuration = 0;
                for (NewHomeworkProcessResult newHomeworkProcessResult : newHomeworkProcessResultMap.values()) {
                    if (map.containsKey(newHomeworkProcessResult.getQuestionId())) {
                        WordTeachAndPracticeTypePart.WordExerciseQuestion question = map.get(newHomeworkProcessResult.getQuestionId());
                        totalScore += SafeConverter.toDouble(newHomeworkProcessResult.getScore());
                        totalDuration += SafeConverter.toLong(newHomeworkProcessResult.getDuration());
                        question.setNum(1 + question.getNum());
                        question.setInterventionReSubmit(newHomeworkProcessResult.isIntervention());
                        if (SafeConverter.toBoolean(newHomeworkProcessResult.getGrasp())) {
                            question.setInterventionRightNum(1 + question.getInterventionRightNum());
                        }
                        if (!SafeConverter.toBoolean(newHomeworkProcessResult.isIntervention()) && SafeConverter.toBoolean(newHomeworkProcessResult.getGrasp())) {
                            question.setRightNum(1 + question.getRightNum());
                        }
                    }
                }

                //******* begin 每一题的正确率*************//
                map.values()
                        .stream()
                        .filter(o -> o.getNum() > 0)
                        .forEach(o -> {
                            int firstRate = new BigDecimal(100 * o.getRightNum()).divide(new BigDecimal(o.getNum()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                            if (o.isInterventionReSubmit()) {
                                o.setFirstRate(firstRate);
                                int rate = new BigDecimal(100 * o.getInterventionRightNum()).divide(new BigDecimal(o.getNum()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                                o.setRate(rate);
                            } else {
                                o.setRate(firstRate);
                            }
                        });
                //******* end 每一题的正确率*************//

                List<WordTeachAndPracticeTypePart.WordExerciseQuestion> questions = wordExerciseQuestions.stream().filter(o -> map.containsKey(o.getQuestionId())).map(o -> map.get(o.getQuestionId())).collect(Collectors.toList());

                //******* begin 每一题的分享地址添加*************//
                for (int k = 0; k < questions.size(); k++) {
                    WordTeachAndPracticeTypePart.WordExerciseQuestion question = questions.get(k);
                    question.setIndex(k);
                    String url = UrlUtils.buildUrlQuery("/view/reportv5/clazz/singlequestiondetail",
                            MapUtils.m(
                                    "homeworkId", newHomework.getId(),
                                    "type", type,
                                    "subject", newHomework.getSubject(),
                                    "qid", question.getQid(),
                                    "stoneDataId", stoneId,
                                    "index", question.getIndex()));
                    question.setUrl(url);
                }

                int averScore = new BigDecimal(totalScore).divide(new BigDecimal(questions.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                long averDuration = new BigDecimal(totalDuration).divide(new BigDecimal(1000 * 60 * newHomeworkResults.size()), 0, BigDecimal.ROUND_UP).longValue();

                wordTeachStoneDataPart.setTypeName("字词训练");
                wordTeachStoneDataPart.setTapType(2);
                //wordTeachStoneDataPart.setClazzGroupId(clazzGroupId);
                wordTeachStoneDataPart.setShowUrl(true);
                ObjectiveConfigTypeParameter param = new ObjectiveConfigTypeParameter();
                param.setStoneId(stoneId);
                String url = UrlUtils.buildUrlQuery("/view/reportv5/clazz/questionsdetail",
                        MapUtils.m(
                                "homeworkId", newHomework.getId(),
                                "type", type,
                                "subject", newHomework.getSubject(),
                                "param", JsonUtils.toJson(param)));
                wordTeachStoneDataPart.setUrl(url);
                wordTeachStoneDataPart.setShowScore(false);
                wordTeachStoneDataPart.setAverScore(averScore);
                wordTeachStoneDataPart.setAverDuration(averDuration);
                wordTeachStoneDataPart.setHasFinishUser(true);
                wordTeachStoneDataPart.setSubContent("班平均分" + averScore + " 平均用时" + averDuration + "min");
                wordTeachStoneDataPart.setRateContent("题目正确率：统计首次作答及做错干预讲解后正确率");
                wordTeachStoneDataPart.setQuestion(questions);
                wordTeachStoneDataPart.setTitle(sectionName);
                wordTeachStoneDataParts.add(wordTeachStoneDataPart);
            }

            // 图文入韵
            if (containImageTextRhyme) {
                List<ImageTextRhymeHomework> imageTextRhymeQuestions = newHomeworkApp.getImageTextRhymeQuestions();
                wordTeachStoneDataPart = new WordTeachAndPracticeTypePart.WordTeachSectionDataPart();
                List<WordTeachAndPracticeTypePart.TabInfoPart> tabs = new LinkedList<>();
                int x = 0;
                for (ImageTextRhymeHomework imageTextRhymeQuestion : imageTextRhymeQuestions) {
                    WordTeachAndPracticeTypePart.TabInfoPart imageTextRhymePart = new WordTeachAndPracticeTypePart.TabInfoPart();
                    imageTextRhymePart.setTabName(imageTextRhymeQuestion.getTitle());
                    imageTextRhymePart.setTabValue(stoneIdFinishStudentMap.get(stoneId) + "人完成");
                    imageTextRhymePart.setShowUrl(true);
                    imageTextRhymePart.setUrl(UrlUtils.buildUrlQuery("/view/reportv5/clazz/wordteachpracticedetail",
                            MiscUtils.m("homeworkId", homeworkId, "stoneId", stoneId, "wordTeachModuleType", WordTeachModuleType.IMAGETEXTRHYME, "index", x)));
                    tabs.add(imageTextRhymePart);
                    x++;
                }

                wordTeachStoneDataPart.setTypeName("图文入韵");
                wordTeachStoneDataPart.setTapType(5);
                wordTeachStoneDataPart.setShowUrl(false);
                wordTeachStoneDataPart.setHasFinishUser(true);
                if (j == 2) {
                    wordTeachStoneDataPart.setTitle(sectionName);
                }
                WordTeachAndPracticeTypePart.TabPart tabPart = new WordTeachAndPracticeTypePart.TabPart();
                tabPart.setTabs(tabs);
                wordTeachStoneDataPart.setTabs(Collections.singletonList(tabPart));
                wordTeachStoneDataParts.add(wordTeachStoneDataPart);
            }

            // 汉字文化
            if (containChineseCharacterCultureCourse) {
                List<String> chineseCharacterCultureCourseIds = newHomeworkApp.getChineseCharacterCultureCourseIds();
                wordTeachStoneDataPart = new WordTeachAndPracticeTypePart.WordTeachSectionDataPart();
                List<WordTeachAndPracticeTypePart.TabInfoPart> tabs = new LinkedList<>();
                int x = 0;
                for (String courseId : chineseCharacterCultureCourseIds) {
                    WordTeachAndPracticeTypePart.TabInfoPart chineseCharacterCulturePart = new WordTeachAndPracticeTypePart.TabInfoPart();
                    chineseCharacterCulturePart.setTabName(courseMap.get(courseId) != null ? courseMap.get(courseId).getName() : "");
                    chineseCharacterCulturePart.setTabValue(stoneIdFinishStudentMap.get(stoneId) + "人完成");
                    chineseCharacterCulturePart.setShowUrl(true);
                    chineseCharacterCulturePart.setUrl(UrlUtils.buildUrlQuery("/view/reportv5/clazz/wordteachpracticedetail",
                            MiscUtils.m("homeworkId", homeworkId, "stoneId", stoneId, "wordTeachModuleType", WordTeachModuleType.CHINESECHARACTERCULTURE, "index", x)));
                    tabs.add(chineseCharacterCulturePart);
                    x++;
                }
                wordTeachStoneDataPart.setTypeName("汉字文化");
                wordTeachStoneDataPart.setTapType(5);
                wordTeachStoneDataPart.setShowUrl(false);
                wordTeachStoneDataPart.setHasFinishUser(true);
                if (j == 3) {
                    wordTeachStoneDataPart.setTitle(sectionName);
                }
                WordTeachAndPracticeTypePart.TabPart tabPart = new WordTeachAndPracticeTypePart.TabPart();
                tabPart.setTabs(tabs);
                wordTeachStoneDataPart.setTabs(Collections.singletonList(tabPart));
                wordTeachStoneDataParts.add(wordTeachStoneDataPart);
            }
        }

        int totalScore = 0;
        int finishedUserCount = 0;
        int totalDuration = 0;
        for (NewHomeworkResult r : newHomeworkResults) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = r.getPractices().get(type);
            if (newHomeworkResultAnswer.isFinished()) {
                totalScore += SafeConverter.toInt(newHomeworkResultAnswer.processScore(type));
                totalDuration += SafeConverter.toInt(newHomeworkResultAnswer.processDuration());
                finishedUserCount++;
            }
        }

        int averScore = new BigDecimal(totalScore).divide(new BigDecimal(finishedUserCount), 0, BigDecimal.ROUND_HALF_UP).intValue();
        long averDuration = new BigDecimal(totalDuration).divide(new BigDecimal(60 * newHomeworkResults.size()), 0, BigDecimal.ROUND_UP).longValue();
        wordTeachAndPracticeTypePart.setHasFinishUser(true);
        wordTeachAndPracticeTypePart.setFinishedUserCount(finishedUserCount);
        wordTeachAndPracticeTypePart.setAverScore(averScore);
        wordTeachAndPracticeTypePart.setAverDuration(averDuration);
        wordTeachAndPracticeTypePart.setShowCorrectUrl(false);
        wordTeachAndPracticeTypePart.setShowUrl(false);
        wordTeachAndPracticeTypePart.setUrl("");
        wordTeachAndPracticeTypePart.setSubContent("班平均分" + averScore + " 完成人数" + finishedUserCount);
        wordTeachAndPracticeTypePart.setTypes(wordTeachStoneDataParts);

        result.put(type, wordTeachAndPracticeTypePart);
    }
}
