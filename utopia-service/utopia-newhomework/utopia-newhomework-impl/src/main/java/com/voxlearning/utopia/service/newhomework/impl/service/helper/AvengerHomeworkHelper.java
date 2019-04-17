package com.voxlearning.utopia.service.newhomework.impl.service.helper;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.PropertiesUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReading;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.avenger.AvengerHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.avenger.AvengerHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.mapper.avenger.AvengerHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.avenger.AvengerHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.AvengerQueueServiceImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2017/6/19
 */
@Named
public class AvengerHomeworkHelper {

    @Inject private AvengerQueueServiceImpl avengerQueueService;

    public AvengerHomework generateAvengerHomework(NewHomework homework, BaseHomeworkBook homeworkBook) {
        if (homework == null) {
            return null;
        }

        AvengerHomework avengerHomework = new AvengerHomework();
        BeanUtils.copyProperties(homework, avengerHomework, "practices");
        avengerHomework.setId(null);
        avengerHomework.setHomeworkId(homework.getId());
        avengerHomework.setUpdateAt(new Date());

        Set<ObjectiveConfigType> typeSet = homework.getPractices()
                .stream()
                .filter(o -> o.getType() != null)
                .map(NewHomeworkPracticeContent::getType)
                .collect(Collectors.toSet());

        // 转换homeworkBook
        Map<ObjectiveConfigType, Map<String, BookInfo>> homeworkBookMap = processHomeworkBookPracticeToMap(homework.getId(), homeworkBook, typeSet);
        // 拼装practice
        Map<ObjectiveConfigType, AvengerHomeworkPracticeContent> practiceContentMap = processHomeworkPracticeToMap(homework, homeworkBookMap);
        if (MapUtils.isNotEmpty(practiceContentMap)) {
            avengerHomework.setPractices(practiceContentMap);
            avengerHomework.setEnv(RuntimeMode.getCurrentStage());
            avengerHomework.setTimeFiled(new Date());
            avengerQueueService.sendHomework(avengerHomework);
        }

        return avengerHomework;
    }

    public AvengerHomework generateAvengerHomeworkForBasicReview(NewHomework homework) {
        if (homework == null || !Objects.equals(homework.getType(), NewHomeworkType.BasicReview)) {
            return null;
        }
        AvengerHomework avengerHomework = new AvengerHomework();
        BeanUtils.copyProperties(homework, avengerHomework, "practices");
        avengerHomework.setId(null);
        avengerHomework.setHomeworkId(homework.getId());
        avengerHomework.setUpdateAt(new Date());
        avengerHomework.setEnv(RuntimeMode.getCurrentStage());
        avengerHomework.setTimeFiled(new Date());
        avengerHomework.setPackageId(homework.getBasicReviewPackageId());

        Map<ObjectiveConfigType, Map<String, BookInfo>> homeworkBookMap = new LinkedHashMap<>();
        homework.getPractices()
                .stream()
                .filter(o -> o.getType() != null)
                .forEach(o -> homeworkBookMap.put(o.getType(), new HashMap<>()));
        // 拼装practice
        Map<ObjectiveConfigType, AvengerHomeworkPracticeContent> practiceContentMap = processHomeworkPracticeToMap(homework, homeworkBookMap);
        if (MapUtils.isNotEmpty(practiceContentMap)) {
            avengerHomework.setPractices(practiceContentMap);
        }
//        avengerHomeworkDao.saveEntity(avengerHomework);
        avengerQueueService.sendHomework(avengerHomework);
        return avengerHomework;
    }

    public AvengerHomework generateAvengerHomeworkForOutsideReading(OutsideReading outsideReading) {
        if (outsideReading == null) {
            return null;
        }
        AvengerHomework avengerHomework = new AvengerHomework();
        BeanUtils.copyProperties(outsideReading, avengerHomework, "practices");
        avengerHomework.setId(null);
        avengerHomework.setHomeworkId(avengerHomework.getId());
        avengerHomework.setUpdateAt(new Date());
        avengerHomework.setEnv(RuntimeMode.getCurrentStage());
        avengerHomework.setTimeFiled(new Date());
        avengerHomework.setType(NewHomeworkType.OutsideReading);
        avengerQueueService.sendHomework(avengerHomework);
        return avengerHomework;
    }

    /**
     * 转换教材结构
     * <p>
     * 基础训练的结构 <appId, BookInfo>, appId=lessonId|category
     * 绘本类的结构   <pictureBookId, BookInfo>
     * 重难点视频     <videoId, BookInfo>
     * 趣味配音       <dubbingId, BookInfo>
     * 同步习题的结构 <qid, BookInfo>
     * 课文读背的结构 <questionBoxId, BookInfo>
     * 纸质口算的结构 <OCR_MENTAL_ARITHMETIC, BookInfo>
     * 生字认读的结构 <questionBoxId, BookInfo>
     */
    private Map<ObjectiveConfigType, Map<String, BookInfo>> processHomeworkBookPracticeToMap(String homeworkId, BaseHomeworkBook homeworkBook, Set<ObjectiveConfigType> typeSet) {
        if (StringUtils.isBlank(homeworkId) || homeworkBook == null || MapUtils.isEmpty(homeworkBook.getPractices())) {
            return Collections.emptyMap();
        }

        LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> tempPracticeMap = homeworkBook.getPractices();

        Map<ObjectiveConfigType, Map<String, BookInfo>> practiceMap = new HashMap<>();
        for (ObjectiveConfigType type : typeSet) {
            Map<String, BookInfo> bookPracticeMap = new HashMap<>();

            switch (type) {
                case BASIC_APP:
                case LS_KNOWLEDGE_REVIEW:
                case NATURAL_SPELLING:
                    try {
                        for (NewHomeworkBookInfo info : tempPracticeMap.get(type)) {
                            if (info != null && CollectionUtils.isNotEmpty(info.getAppIds())) {
                                BookInfo bookInfo = new BookInfo();
                                PropertiesUtils.copyProperties(bookInfo, info);
                                info.getAppIds().forEach(a -> bookPracticeMap.put(a, bookInfo));
                            } else {
                                sendErrorLog("HomeworkBookError", type.name(), homeworkId, "");
                            }
                        }
                        practiceMap.put(type, bookPracticeMap);
                    } catch (Exception e) {
                        sendErrorLog("HomeworkBookError", type.name(), homeworkId, e.getMessage());
                    }
                    break;
                case READING:
                case LEVEL_READINGS:
                    try {
                        for (NewHomeworkBookInfo info : tempPracticeMap.get(type)) {
                            if (info != null && CollectionUtils.isNotEmpty(info.getPictureBooks())) {
                                BookInfo bookInfo = new BookInfo();
                                PropertiesUtils.copyProperties(bookInfo, info);
                                info.getPictureBooks().forEach(p -> bookPracticeMap.put(p, bookInfo));
                            } else {
                                sendErrorLog("HomeworkBookError", type.name(), homeworkId, "");
                            }
                        }
                        practiceMap.put(type, bookPracticeMap);
                    } catch (Exception e) {
                        sendErrorLog("HomeworkBookError", type.name(), homeworkId, e.getMessage());
                    }
                    break;
                case KEY_POINTS:
                    try {
                        for (NewHomeworkBookInfo info : tempPracticeMap.get(type)) {
                            if (info != null && CollectionUtils.isNotEmpty(info.getVideos())) {
                                BookInfo bookInfo = new BookInfo();
                                PropertiesUtils.copyProperties(bookInfo, info);
                                info.getVideos().forEach(v -> bookPracticeMap.put(v, bookInfo));
                            } else {
                                sendErrorLog("HomeworkBookError", type.name(), homeworkId, "");
                            }
                        }
                        practiceMap.put(type, bookPracticeMap);
                    } catch (Exception e) {
                        sendErrorLog("HomeworkBookError", type.name(), homeworkId, e.getMessage());
                    }
                    break;
                case DUBBING:
                case DUBBING_WITH_SCORE:
                    try {
                        for (NewHomeworkBookInfo info : tempPracticeMap.get(type)) {
                            if (info != null && CollectionUtils.isNotEmpty(info.getDubbingIds())) {
                                BookInfo bookInfo = new BookInfo();
                                PropertiesUtils.copyProperties(bookInfo, info);
                                info.getDubbingIds().forEach(d -> bookPracticeMap.put(d, bookInfo));
                            } else {
                                sendErrorLog("HomeworkBookError", type.name(), homeworkId, "");
                            }
                        }
                        practiceMap.put(type, bookPracticeMap);
                    } catch (Exception e) {
                        sendErrorLog("HomeworkBookError", type.name(), homeworkId, e.getMessage());
                    }
                    break;
                case OCR_MENTAL_ARITHMETIC:
                    try {
                        for (NewHomeworkBookInfo info : tempPracticeMap.get(type)) {
                            if (info != null) {
                                BookInfo bookInfo = new BookInfo();
                                PropertiesUtils.copyProperties(bookInfo, info);
                                bookPracticeMap.put(type.name(), bookInfo);
                            } else {
                                sendErrorLog("HomeworkBookError", type.name(), homeworkId, "");
                            }
                        }
                        practiceMap.put(type, bookPracticeMap);
                    } catch (Exception e) {
                        sendErrorLog("HomeworkBookError", type.name(), homeworkId, e.getMessage());
                    }
                    break;
                case NEW_READ_RECITE:
                case READ_RECITE_WITH_SCORE:
                case WORD_RECOGNITION_AND_READING:
                    try {
                        for (NewHomeworkBookInfo info : tempPracticeMap.get(type)) {
                            if (info != null && CollectionUtils.isNotEmpty(info.getQuestionBoxIds())) {
                                BookInfo bookInfo = new BookInfo();
                                PropertiesUtils.copyProperties(bookInfo, info);
                                info.getQuestionBoxIds().forEach(d -> bookPracticeMap.put(d, bookInfo));
                            } else {
                                sendErrorLog("HomeworkBookError", type.name(), homeworkId, "");
                            }
                        }
                        practiceMap.put(type, bookPracticeMap);
                    } catch (Exception e) {
                        sendErrorLog("HomeworkBookError", type.name(), homeworkId, e.getMessage());
                    }
                    break;

                case EXAM:
                case INTELLIGENCE_EXAM:
                case MENTAL:
                case UNIT_QUIZ:
                case WORD_PRACTICE:
                case LISTEN_PRACTICE:
                case READ_RECITE:
                case ORAL_PRACTICE:
                case BASIC_KNOWLEDGE:
                case CHINESE_READING:
                case INTERESTING_PICTURE:
                case KNOWLEDGE_REVIEW:
                case FALLIBILITY_QUESTION:
                case RW_KNOWLEDGE_REVIEW:
                case INTELLIGENT_TEACHING:
                case ORAL_INTELLIGENT_TEACHING:
                default:
                    try {
                        for (NewHomeworkBookInfo info : tempPracticeMap.get(type)) {
                            if (info != null && CollectionUtils.isNotEmpty(info.getQuestions())) {
                                BookInfo bookInfo = new BookInfo();
                                PropertiesUtils.copyProperties(bookInfo, info);
                                info.getQuestions().forEach(q -> bookPracticeMap.put(q, bookInfo));
                            } else {
                                sendErrorLog("HomeworkBookError", type.name(), homeworkId, "");
                            }
                        }
                        practiceMap.put(type, bookPracticeMap);
                    } catch (Exception e) {
                        sendErrorLog("HomeworkBookTypeUnknownException", type.name(), homeworkId, e.getMessage());
                    }
                    break;
            }
        }
        return practiceMap;
    }

    /**
     * 转换作业结构
     * <p>
     * 基础训练的结构 <appId, BookInfo>, appId=lessonId|category
     * 绘本类的结构   <pictureBookId, BookInfo>
     * 重难点视频     <videoId, BookInfo>
     * 趣味配音       <dubbingId, BookInfo>
     * 同步习题的结构 <qid, BookInfo>
     * 课文读背的结构 <questionBoxId, BookInfo>
     * 纸质口算的结构 <OCR_MENTAL_ARITHMETIC, BookInfo>
     * 生字认读的结构 <questionBoxId, BookInfo>
     */
    private Map<ObjectiveConfigType, AvengerHomeworkPracticeContent> processHomeworkPracticeToMap(NewHomework homework, Map<ObjectiveConfigType, Map<String, BookInfo>> homeworkBookMap) {
        if (homework == null || MapUtils.isEmpty(homework.findPracticeContents())) {
            return Collections.emptyMap();
        }
        LinkedHashMap<ObjectiveConfigType, NewHomeworkPracticeContent> tempPracticeMap = homework.findPracticeContents();

        Map<ObjectiveConfigType, AvengerHomeworkPracticeContent> practiceMap = new HashMap<>();
        for (Map.Entry<ObjectiveConfigType, NewHomeworkPracticeContent> entry : tempPracticeMap.entrySet()) {

            AvengerHomeworkPracticeContent content = new AvengerHomeworkPracticeContent();
            List<AvengerHomeworkQuestion> questions = new ArrayList<>();

            ObjectiveConfigType objectiveConfigType = entry.getKey();
            NewHomeworkPracticeContent practiceContent = entry.getValue();

            Map<String, BookInfo> bookInfoMap = homeworkBookMap.get(objectiveConfigType);

            switch (objectiveConfigType) {
                case BASIC_APP:
                case LS_KNOWLEDGE_REVIEW:
                case NATURAL_SPELLING:
                    try {
                        for (NewHomeworkApp app : practiceContent.getApps()) {
                            if (app != null && StringUtils.isNotBlank(app.getLessonId()) && app.getCategoryId() != null) {
                                for (NewHomeworkQuestion question : app.getQuestions()) {
                                    // 写入题目在作业中的属性
                                    AvengerHomeworkQuestion value = new AvengerHomeworkQuestion();
                                    PropertiesUtils.copyProperties(value, question);

                                    // 写入book信息
                                    String appId = app.getLessonId() + "|" + app.getCategoryId();
                                    if (MapUtils.isNotEmpty(bookInfoMap)) {
                                        BookInfo bookInfo = bookInfoMap.get(appId);
                                        if (bookInfo != null) {
                                            PropertiesUtils.copyProperties(value, bookInfo);
                                        }
                                    }
                                    questions.add(value);
                                }
                            } else {
                                sendErrorLog("HomeworkError", objectiveConfigType.name(), homework.getId(), "");
                            }
                        }
                        if (CollectionUtils.isNotEmpty(questions)) {
                            content.setQuestions(questions);
                            practiceMap.put(objectiveConfigType, content);
                        }
                    } catch (Exception e) {
                        sendErrorLog("HomeworkBookMapException", objectiveConfigType.name(), homework.getId(), e.getMessage());
                    }
                    break;
                case READING:
                case LEVEL_READINGS:
                    try {
                        for (NewHomeworkApp app : practiceContent.getApps()) {
                            if (app != null && StringUtils.isNotBlank(app.getPictureBookId())) {
                                List<NewHomeworkQuestion> readingQuestions = new ArrayList<>();
                                if (CollectionUtils.isNotEmpty(app.getQuestions())) {
                                    readingQuestions.addAll(app.getQuestions());
                                }
                                if (CollectionUtils.isNotEmpty(app.getOralQuestions())) {
                                    readingQuestions.addAll(app.getOralQuestions());
                                }

                                for (NewHomeworkQuestion question : readingQuestions) {
                                    // 写入题目在作业中的属性
                                    AvengerHomeworkQuestion value = new AvengerHomeworkQuestion();
                                    PropertiesUtils.copyProperties(value, question);

                                    if (MapUtils.isNotEmpty(bookInfoMap)) {
                                        // 写入book信息
                                        BookInfo bookInfo = bookInfoMap.get(app.getPictureBookId());
                                        if (bookInfo != null) {
                                            PropertiesUtils.copyProperties(value, bookInfo);
                                        }
                                    }
                                    value.setPictureBookId(app.getPictureBookId());
                                    questions.add(value);
                                }
                            }
                        }
                        if (CollectionUtils.isNotEmpty(questions)) {
                            content.setQuestions(questions);
                            practiceMap.put(objectiveConfigType, content);
                        }
                    } catch (Exception e) {
                        sendErrorLog("HomeworkBookMapException", objectiveConfigType.name(), homework.getId(), e.getMessage());
                    }
                    break;
                case KEY_POINTS:
                    try {
                        for (NewHomeworkApp app : practiceContent.getApps()) {
                            if (app != null && StringUtils.isNotBlank(app.getVideoId())) {
                                for (NewHomeworkQuestion question : app.getQuestions()) {

                                    // 写入题目在作业中的属性
                                    AvengerHomeworkQuestion value = new AvengerHomeworkQuestion();
                                    PropertiesUtils.copyProperties(value, question);
                                    if (MapUtils.isNotEmpty(bookInfoMap)) {
                                        // 写入book信息
                                        BookInfo bookInfo = bookInfoMap.get(app.getVideoId());
                                        if (bookInfo != null) {
                                            PropertiesUtils.copyProperties(value, bookInfo);
                                        }
                                    }
                                    value.setVideoId(app.getVideoId());
                                    questions.add(value);
                                }
                            } else {
                                sendErrorLog("HomeworkError", objectiveConfigType.name(), homework.getId(), "");
                            }
                        }
                        if (CollectionUtils.isNotEmpty(questions)) {
                            content.setQuestions(questions);
                            practiceMap.put(objectiveConfigType, content);
                        }
                    } catch (Exception e) {
                        sendErrorLog("HomeworkBookMapException", objectiveConfigType.name(), homework.getId(), e.getMessage());
                    }
                    break;
                case DUBBING:
                case DUBBING_WITH_SCORE:
                    try {
                        for (NewHomeworkApp app : practiceContent.getApps()) {
                            if (app != null && StringUtils.isNotBlank(app.getDubbingId())) {
                                for (NewHomeworkQuestion question : app.getQuestions()) {

                                    // 写入题目在作业中的属性
                                    AvengerHomeworkQuestion value = new AvengerHomeworkQuestion();
                                    PropertiesUtils.copyProperties(value, question);
                                    if (MapUtils.isNotEmpty(bookInfoMap)) {
                                        BookInfo bookInfo = bookInfoMap.get(app.getDubbingId());
                                        if (bookInfo != null) {
                                            PropertiesUtils.copyProperties(value, bookInfo);
                                        }
                                    }
                                    value.setDubbingId(app.getDubbingId());
                                    questions.add(value);
                                }
                            } else {
                                sendErrorLog("HomeworkError", objectiveConfigType.name(), homework.getId(), "");
                            }
                        }
                        if (CollectionUtils.isNotEmpty(questions)) {
                            content.setQuestions(questions);
                            practiceMap.put(objectiveConfigType, content);
                        }
                    } catch (Exception e) {
                        sendErrorLog("HomeworkBookMapException", objectiveConfigType.name(), homework.getId(), e.getMessage());
                    }
                    break;
                case OCR_MENTAL_ARITHMETIC:
                    try {
                        content.setWorkBookId(practiceContent.getWorkBookId());
                        content.setWorkBookName(practiceContent.getWorkBookName());
                        content.setHomeworkDetail(practiceContent.getHomeworkDetail());
                        practiceMap.put(objectiveConfigType, content);
                    } catch (Exception e) {
                        sendErrorLog("HomeworkBookMapException", objectiveConfigType.name(), homework.getId(), e.getMessage());
                    }
                    break;
                case ORAL_COMMUNICATION:
                    try {
                        List<AvengerHomeworkApp> oralCommunicationApps = new ArrayList<>();
                        for (NewHomeworkApp app : practiceContent.getApps()) {
                            if (app != null && StringUtils.isNotBlank(app.getStoneDataId())) {
                                AvengerHomeworkApp avengerHomeworkApp = new AvengerHomeworkApp();
                                avengerHomeworkApp.setStoneDataId(app.getStoneDataId());
                                avengerHomeworkApp.setStoneDataType(app.getOralCommunicationContentType().name());
                                oralCommunicationApps.add(avengerHomeworkApp);
                            } else {
                                sendErrorLog("HomeworkError", objectiveConfigType.name(), homework.getId(), "");
                            }
                        }
                        content.setApps(oralCommunicationApps);
                        practiceMap.put(objectiveConfigType, content);
                    } catch (Exception e) {
                        sendErrorLog("HomeworkBookMapException", objectiveConfigType.name(), homework.getId(), e.getMessage());
                    }
                    break;
                case WORD_TEACH_AND_PRACTICE:
                    try {
                        List<AvengerHomeworkApp> wordTeachAndPracticeApps = new ArrayList<>();
                        for (NewHomeworkApp app : practiceContent.getApps()) {
                            if (app != null && StringUtils.isNotBlank(app.getStoneDataId())) {
                                AvengerHomeworkApp avengerHomeworkApp = new AvengerHomeworkApp();
                                avengerHomeworkApp.setStoneDataId(app.getStoneDataId());
                                List<String> moduleTypes = new ArrayList<>();
                                if (CollectionUtils.isNotEmpty(app.getWordExerciseQuestions())) {
                                    moduleTypes.add(WordTeachModuleType.WORDEXERCISE.name());
                                }
                                if (CollectionUtils.isNotEmpty(app.getImageTextRhymeQuestions())) {
                                    moduleTypes.add(WordTeachModuleType.IMAGETEXTRHYME.name());
                                }
                                if (CollectionUtils.isNotEmpty(app.getChineseCharacterCultureCourseIds())) {
                                    moduleTypes.add(WordTeachModuleType.CHINESECHARACTERCULTURE.name());
                                }
                                avengerHomeworkApp.setModuleTypes(moduleTypes);
                                wordTeachAndPracticeApps.add(avengerHomeworkApp);
                            } else {
                                sendErrorLog("HomeworkError", objectiveConfigType.name(), homework.getId(), "");
                            }
                        }
                        content.setApps(wordTeachAndPracticeApps);
                        practiceMap.put(objectiveConfigType, content);
                    } catch (Exception e) {
                        sendErrorLog("HomeworkBookMapException", objectiveConfigType.name(), homework.getId(), e.getMessage());
                    }
                    break;
                case NEW_READ_RECITE:
                case READ_RECITE_WITH_SCORE:
                case WORD_RECOGNITION_AND_READING:
                    try {
                        for (NewHomeworkApp app : practiceContent.getApps()) {
                            if (app != null && StringUtils.isNotBlank(app.getQuestionBoxId())) {
                                for (NewHomeworkQuestion question : app.getQuestions()) {

                                    // 写入题目在作业中的属性
                                    AvengerHomeworkQuestion value = new AvengerHomeworkQuestion();
                                    PropertiesUtils.copyProperties(value, question);
                                    if (MapUtils.isNotEmpty(bookInfoMap)) {
                                        BookInfo bookInfo = bookInfoMap.get(app.getQuestionBoxId());
                                        if (bookInfo != null) {
                                            PropertiesUtils.copyProperties(value, bookInfo);
                                        }
                                    }
                                    value.setQuestionBoxId(app.getQuestionBoxId());
                                    questions.add(value);
                                }
                            } else {
                                sendErrorLog("HomeworkError", objectiveConfigType.name(), homework.getId(), "");
                            }
                        }
                        if (CollectionUtils.isNotEmpty(questions)) {
                            content.setQuestions(questions);
                            practiceMap.put(objectiveConfigType, content);
                        }
                    } catch (Exception e) {
                        sendErrorLog("HomeworkBookMapException", objectiveConfigType.name(), homework.getId(), e.getMessage());
                    }
                    break;
                case MENTAL_ARITHMETIC:
                case CALC_INTELLIGENT_TEACHING:
                    try {
                        content.setTimeLimit(practiceContent.getTimeLimit() == null ? 0 : practiceContent.getTimeLimit().getTime());
                        content.setMentalAward(practiceContent.getMentalAward());
                        content.setRecommend(practiceContent.getRecommend());
                    } catch (Exception e) {
                        sendErrorLog("HomeworkTypeUnknownException", objectiveConfigType.name(), homework.getId(), e.getMessage());
                    }
                case EXAM:
                case INTELLIGENCE_EXAM:
                case MENTAL:
                case UNIT_QUIZ:
                case WORD_PRACTICE:
                case LISTEN_PRACTICE:
                case READ_RECITE:
                case ORAL_PRACTICE:
                case BASIC_KNOWLEDGE:
                case CHINESE_READING:
                case INTERESTING_PICTURE:
                case KNOWLEDGE_REVIEW:
                case FALLIBILITY_QUESTION:
                case RW_KNOWLEDGE_REVIEW:
                case INTELLIGENT_TEACHING:
                case ORAL_INTELLIGENT_TEACHING:
                default:
                    try {
                        for (NewHomeworkQuestion question : practiceContent.getQuestions()) {
                            // 写入题目在作业中的属性
                            AvengerHomeworkQuestion value = new AvengerHomeworkQuestion();
                            PropertiesUtils.copyProperties(value, question);
                            // 写入book信息
                            if (StringUtils.isNotBlank(question.getQuestionId())) {
                                if (MapUtils.isNotEmpty(bookInfoMap)) {
                                    BookInfo bookInfo = bookInfoMap.get(question.getQuestionId());
                                    if (bookInfo != null) {
                                        PropertiesUtils.copyProperties(value, bookInfo);
                                    }
                                }
                                questions.add(value);
                            }
                        }

                        if (CollectionUtils.isNotEmpty(questions)) {
                            content.setQuestions(questions);
                            practiceMap.put(objectiveConfigType, content);
                        }
                    } catch (Exception e) {
                        sendErrorLog("HomeworkTypeUnknownException", objectiveConfigType.name(), homework.getId(), e.getMessage());
                    }
                    break;
            }
        }
        return practiceMap;
    }

    private void sendErrorLog(String mod1, String mod2, String mod3, String mod4) {
        LogCollector.info("backend-general", MapUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "mod1", mod1,
                "mod2", mod2,
                "mod3", mod3,
                "mod4", mod4,
                "op", "AvengerHomework"
        ));
    }

    // 下面这个内部类的public修饰别改成private
    @Data
    public static class BookInfo implements Serializable {
        private static final long serialVersionUID = 500255919764969263L;
        private String bookId;
        private String bookName;
        private String unitId;
        private String unitName;
        private String unitGroupId;
        private String unitGroupName;
        private String lessonId;
        private String lessonName;
        private String sectionId;
        private String sectionName;
        private String objectiveId;
        private String objectiveName;
    }
}
