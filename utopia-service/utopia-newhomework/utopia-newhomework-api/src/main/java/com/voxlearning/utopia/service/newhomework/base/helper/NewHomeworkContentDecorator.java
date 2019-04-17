package com.voxlearning.utopia.service.newhomework.base.helper;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.newhomework.api.constant.OralCommunicationClazzLevel;
import com.voxlearning.utopia.service.newhomework.api.constant.OralCommunicationContentType;
import com.voxlearning.utopia.service.newhomework.api.constant.PictureBookClazzLevel;
import com.voxlearning.utopia.service.newhomework.api.constant.PictureBookPracticeType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.TotalAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.constant.PictureBookNewClazzLevel;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.api.entity.stone.data.StoneBufferedData;
import com.voxlearning.utopia.service.question.api.entity.stone.data.oralpractice.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/7/19
 */
public class NewHomeworkContentDecorator {

    private static final String PICTURE_BOOK_NEW_CLAZZ_LEVEL_DESCRIPTION_TEMPLATE = "<html><body><p><fontcolor=\"#595757\"size=\"14\">1.读物难度：{}。</p><p><fontcolor=\"#595757\"size=\"14\">2.读物文体：{}。</p><p><fontcolor=\"#595757\"size=\"14\">3.阅读习惯：{}。</p><p><fontcolor=\"#595757\"size=\"14\">4.阅读能力：{}。</p><p><fontcolor=\"#595757\"size=\"14\">5.阅读体验：{}。</p><p><fontcolor=\"#595757\"size=\"14\">6.累计阅读量：{}。</p></body></html>";
    private static final Integer CHINESE_READING_CONTENT_TYPE_ID = 1010008;
    private static final Map<Integer, String> CHINESE_READING_DIFFICULTY_MAP = new LinkedHashMap<>();

    static {
        CHINESE_READING_DIFFICULTY_MAP.put(0, "未知");
        CHINESE_READING_DIFFICULTY_MAP.put(1, "容易");
        CHINESE_READING_DIFFICULTY_MAP.put(2, "容易");
        CHINESE_READING_DIFFICULTY_MAP.put(3, "中等");
        CHINESE_READING_DIFFICULTY_MAP.put(4, "中等");
        CHINESE_READING_DIFFICULTY_MAP.put(5, "中等");
    }

    private static String processDifficultyName(Integer contentTypeId, Integer difficulty) {
        if (Objects.equals(contentTypeId, CHINESE_READING_CONTENT_TYPE_ID)) {
            return CHINESE_READING_DIFFICULTY_MAP.get(difficulty);
        } else {
            return QuestionConstants.newDifficultyMap.get(difficulty);
        }
    }

    public static Map<String, Object> decorateNewQuestion(NewQuestion question,
                                                          Map<Integer, NewContentType> contentTypeMap,
                                                          Map<String, TotalAssignmentRecord> totalAssignmentRecordMap,
                                                          TeacherAssignmentRecord teacherAssignmentRecord,
                                                          EmbedBook book) {
        return MapUtils.m(
                "id", question.getId(),
                "questionTypeId", question.getContentTypeId(),
                "questionType", contentTypeMap.get(question.getContentTypeId()) != null ? contentTypeMap.get(question.getContentTypeId()).getName() : "无题型",
                "difficulty", question.getDifficultyInt(),
                "difficultyName", processDifficultyName(question.getContentTypeId(), question.getDifficultyInt()),
                "seconds", question.getSeconds(),
                // 该题被使用次数和该老师布置过的次数
                "assignTimes", totalAssignmentRecordMap.get(question.getId()) != null ? totalAssignmentRecordMap.get(question.getId()).getAssignTimes() : 0,
                "teacherAssignTimes", teacherAssignmentRecord != null ? teacherAssignmentRecord.getQuestionInfo().getOrDefault(question.getDocId(), 0) : 0,
                "upImage", question.getSubmitWays().stream().flatMap(Collection::stream).anyMatch(i -> Objects.equals(i, 1) || Objects.equals(i, 2)),
                "book", buildBookMapper(book),
                "submitWay", question.getSubmitWays(),
                "knowledgePoints", question.mainNewKnowledgePointList());
    }

    public static Map<String, Object> decoratePictureBook(PictureBook pictureBook,
                                                          Map<String, PictureBookSeries> pictureBookSeriesMap,
                                                          Map<String, PictureBookTopic> pictureBookTopicMap,
                                                          EmbedBook book,
                                                          TeacherAssignmentRecord teacherAssignmentRecord) {
        List<String> pictureBookTopicIdList = pictureBook.getTopicIds();
        List<String> pictureBookTopicNameList = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(pictureBookTopicIdList)) {
            pictureBookTopicNameList = pictureBookTopicIdList.stream()
                    .filter(pictureBookTopicMap::containsKey)
                    .map(id -> pictureBookTopicMap.get(id).getName())
                    .collect(Collectors.toList());
        }
        List<Integer> clazzLevelList = pictureBook.getClazzLevels();
        List<String> pictureClazzLevelList = Arrays.stream(PictureBookClazzLevel.values())
                .filter(pictureBookClazzLevel -> clazzLevelList.stream().anyMatch(clazzLevel -> Objects.equals(clazzLevel, pictureBookClazzLevel.getClazzLevel())))
                .map(PictureBookClazzLevel::getShowName)
                .collect(Collectors.toList());
        String pictureBookSeries = "";
        if (pictureBookSeriesMap.containsKey(pictureBook.getSeriesId())) {
            pictureBookSeries = pictureBookSeriesMap.get(pictureBook.getSeriesId()).fetchName();
            if (pictureBook.getVolume() != null) {
                pictureBookSeries += pictureBook.getVolume();
            }
        }
        int teacherAssignTimes = 0;
        if (teacherAssignmentRecord != null) {
            String docId = pictureBook.getDocId();
            for (Map.Entry<String, Integer> entry : teacherAssignmentRecord.getPictureBookInfo().entrySet()) {
                if (Objects.equals(docId, TeacherAssignmentRecord.id2DocId(entry.getKey()))) {
                    teacherAssignTimes += SafeConverter.toInt(entry.getValue());
                }
            }
        }
        return MapUtils.m(
                "pictureBookId", pictureBook.getId(),
                "pictureBookName", pictureBook.getName(),
                "pictureBookSeries", pictureBookSeries,
                "pictureBookClazzLevels", pictureClazzLevelList,
                "pictureBookTopics", pictureBookTopicNameList,
                "pictureBookImgUrl", SafeConverter.toString(pictureBook.getCoverUrl(), ""),
                "pictureBookThumbImgUrl", SafeConverter.toString(pictureBook.getCoverThumbnail1Uri(), ""),
                "keywords", pictureBook.keyWords().stream().filter(Objects::nonNull).map(PictureBookContent.EmbedKeyword::getEntext).collect(Collectors.toCollection(LinkedHashSet::new)),
                "seconds", SafeConverter.toInt(pictureBook.getSeconds()),
                "book", buildBookMapper(book),
                "teacherAssignTimes", teacherAssignTimes,
                "hasOral", CollectionUtils.isNotEmpty(pictureBook.getOralQuestions()));
    }

    public static Map<String, Object> decorateDubbing(Dubbing dubbing,
                                                      DubbingCategory album,
                                                      TeacherAssignmentRecord teacherAssignmentRecord,
                                                      EmbedBook book,
                                                      Date newStartDate,
                                                      ObjectiveConfigType objectiveConfigType,
                                                      Map<String, String> dubbingThemeMap) {
        return MapUtils.m(
                "dubbingId", dubbing.getId(),
                "name", dubbing.getVideoName(),
                "showAssigned", teacherAssignmentRecord != null && teacherAssignmentRecord.getAppInfo().getOrDefault(dubbing.getDocId(), 0) > 0,
                "isNew", newStartDate != null && dubbing.getOlCreatedAt() != null && dubbing.getOlCreatedAt().after(newStartDate),
                "clazzLevel", ClazzLevel.getDescription(dubbing.getDifficult()),
                "sentenceSize", dubbing.getSentences().size(),
                "videoUrl", dubbing.getVideoUrl(),
                "coverUrl", dubbing.getCoverUrl(),
                "albumName", album != null ? album.getName() : "",
                "topics", CollectionUtils.isNotEmpty(dubbing.getThemeIds()) ? dubbing.getThemeIds().stream().map(dubbingThemeMap::get).filter(StringUtils::isNotEmpty).collect(Collectors.toList()) : Collections.emptyList(),
                "teacherAssignTimes", teacherAssignmentRecord != null ? teacherAssignmentRecord.getAppInfo().getOrDefault(dubbing.getDocId(), 1) : 1,
                "summary", dubbing.getVideoSummary(),
                "keyWords", dubbing.getKeyWords(),
                "keyGrammars", Collections.emptyList(),
                "seconds", dubbing.getVideoSeconds(),
                "book", buildBookMapper(book),
                "objectiveConfigType", objectiveConfigType.name(),
                "objectiveConfigTypeName", objectiveConfigType.getValue());
    }

    public static Map<String, Object> decoratePictureBookPlus(PictureBookPlus pictureBookPlus,
                                                              Map<String, PictureBookSeries> pictureBookSeriesMap,
                                                              Map<String, PictureBookTopic> pictureBookTopicMap,
                                                              EmbedBook book,
                                                              TeacherAssignmentRecord teacherAssignmentRecord,
                                                              String sys,
                                                              String appVersion) {
        List<String> pictureBookTopicIdList = pictureBookPlus.getTopicIds();
        List<String> pictureBookTopicNameList = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(pictureBookTopicIdList)) {
            pictureBookTopicIdList = pictureBookTopicIdList.stream()
                    .filter(pictureBookTopicMap::containsKey)
                    .collect(Collectors.toList());
            pictureBookTopicNameList = pictureBookTopicIdList.stream()
                    .map(id -> pictureBookTopicMap.get(id).getName())
                    .collect(Collectors.toList());
        }
        String pictureBookSeries = "";
        if (pictureBookSeriesMap.containsKey(pictureBookPlus.getSeriesId())) {
            pictureBookSeries = pictureBookSeriesMap.get(pictureBookPlus.getSeriesId()).fetchName();
            if (pictureBookPlus.getVolume() != null) {
                pictureBookSeries += pictureBookPlus.getVolume();
            }
        }
        int teacherAssignTimes = 0;
        if (teacherAssignmentRecord != null) {
            for (Map.Entry<String, Integer> entry : teacherAssignmentRecord.getPictureBookInfo().entrySet()) {
                if (Objects.equals(pictureBookPlus.getId(), TeacherAssignmentRecord.id2DocId(entry.getKey()))) {
                    teacherAssignTimes += SafeConverter.toInt(entry.getValue());
                }
            }
        }

        Subject subject = Subject.fromSubjectId(pictureBookPlus.getSubjectId());
        boolean isEnglish = Subject.ENGLISH == subject;
        List<Map<String, Object>> practices = new ArrayList<>();
        List<Map<String, Object>> questionPractices = new ArrayList<>();
        List<Map<String, Object>> expandPractices = new ArrayList<>();

        List<String> allPractices = new ArrayList<>();
        allPractices.add(PictureBookPracticeType.READING.getTypeName());
        if (isEnglish) {
            allPractices.add(PictureBookPracticeType.WORDS.getTypeName());
        }
        // 安卓并且版本小于1.7.3不选中，其余全部默认选中
        boolean selectQuestionPractices = !(StringUtils.equalsIgnoreCase("android", sys) && VersionUtil.compareVersion(appVersion, "1.7.3") < 0);
        if (isEnglish && CollectionUtils.isNotEmpty(pictureBookPlus.getOralQuestions())) {
            PictureBookPracticeType practiceType = PictureBookPracticeType.ORAL;
            // 跟读时长 跟读句子的音频总时长*1.2 直接取绘本上的oralSeconds字段
            Map<String, Object> practiceMapper = MapUtils.m("type", practiceType.name(), "typeName", practiceType.getTypeName(), "description", practiceType.getDescription(), "seconds", SafeConverter.toInt(pictureBookPlus.getOralSeconds(), 300), "isSelect", selectQuestionPractices);
            practices.add(practiceMapper);
            questionPractices.add(practiceMapper);
            allPractices.add(PictureBookPracticeType.ORAL.getTypeName());
        }
        if (CollectionUtils.isNotEmpty(pictureBookPlus.getPracticeQuestions())) {
            PictureBookPracticeType practiceType = PictureBookPracticeType.EXAM;
            // 习题时长 20秒*题量
            Map<String, Object> practiceMapper = MapUtils.m("type", practiceType.name(), "typeName", practiceType.getTypeName(), "description", practiceType.getDescription(), "seconds", 20 * pictureBookPlus.getPracticeQuestions().size(), "isSelect", selectQuestionPractices);
            practices.add(practiceMapper);
            questionPractices.add(practiceMapper);
            allPractices.add(PictureBookPracticeType.EXAM.getTypeName());
        }
        if (isEnglish) {
            // 添加配音形式
            Map<String, Object> practiceMapper = MapUtils.m(
                    "type", PictureBookPracticeType.DUBBING.name(),
                    "typeName", PictureBookPracticeType.DUBBING.getTypeName(),
                    "description", PictureBookPracticeType.DUBBING.getDescription(),
                    "seconds", SafeConverter.toInt(pictureBookPlus.getPredictedDubbingTime(), 300),
                    "isSelect", false);
            expandPractices.add(practiceMapper);
            allPractices.add(PictureBookPracticeType.DUBBING.getTypeName());
        }
        List<Map<String, Object>> newWords = pictureBookPlus.allNewWords()
                .stream()
                .map(sentenceWord -> MapUtils.m("enText", sentenceWord.getEntext(), "cnText", sentenceWord.getCntext()))
                .collect(Collectors.toList());
        PictureBookNewClazzLevel clazzLevel = null;
        if (CollectionUtils.isNotEmpty(pictureBookPlus.getNewClazzLevels())) {
            clazzLevel = pictureBookPlus.getNewClazzLevels().get(0);
        }
        // 计算绘本时长 阅读模块时长(绘本音频总时长*1.2系数，直接取绘本seconds字段) + 高频词模块时长(10秒*高频词个数)
        int seconds = SafeConverter.toInt(pictureBookPlus.getRecommendTime(), 300) + 10 * pictureBookPlus.allOftenUsedWords().size();
        String pictureBookClazzLevelName;
        if (isEnglish) {
            pictureBookClazzLevelName = clazzLevel == null ? "" : clazzLevel.getLevelName();
        } else {
            pictureBookClazzLevelName = clazzLevel == null ? "" : NewHomeworkUtils.processChinesePictureBookClazzLevel(clazzLevel.name());
        }
        List<String> pictureBookAbilitiesList = new ArrayList<>();
        List<PictureBookPlus.QuestionAttributes> questionAttributes = pictureBookPlus.getQuestionAttributes();
        if (CollectionUtils.isNotEmpty(questionAttributes)) {
            List<List<String>> abilities = questionAttributes.stream().map(PictureBookPlus.QuestionAttributes::getAbilities).collect(Collectors.toList());
            abilities.forEach(pictureBookAbilitiesList::addAll);
        }
        Map<String, Object> pbMapper = MapUtils.m(
                "pictureBookId", pictureBookPlus.getId(),
                "pictureBookName", pictureBookPlus.getEname(),
                "pictureBookSeries", pictureBookSeries,
                "pictureBookSeriesId", pictureBookPlus.getSeriesId(),
                "pictureBookClazzLevel", clazzLevel == null ? "" : clazzLevel.name(),
                "pictureBookClazzLevelName", pictureBookClazzLevelName,
                "pictureBookTopics", pictureBookTopicNameList,
                "pictureBookTopicIds", pictureBookTopicIdList,
                "pictureBookImgUrl", SafeConverter.toString(pictureBookPlus.getCoverUrl(), ""),
                "pictureBookThumbImgUrl", SafeConverter.toString(pictureBookPlus.getCoverThumbnailUrl(), ""),
                "seconds", seconds,
                "book", buildBookMapper(book),
                "showAssigned", teacherAssignTimes > 0,
                "practices", practices,
                "questionPractices", questionPractices,
                "expandPractices", expandPractices,
                "allPractices", allPractices,
                "newWords", newWords,
                "newWordsCount", newWords.size(),
                "wordsCount", pictureBookPlus.getWordsLength(),
                "isNew", false,
                "pictureBookSummary", pictureBookPlus.getBookSummary(),
                "screenMode", pictureBookPlus.getScreenMode(),
                "lexiler", pictureBookPlus.getLexiler() != null ? SafeConverter.toInt(pictureBookPlus.getLexiler().getPriceEnd()) : 0,
                "cnName", pictureBookPlus.getCname(),
                "pictureBookAbilities", pictureBookAbilitiesList.stream().distinct().collect(Collectors.toList()));
        if (isEnglish) {
            pbMapper.put("pictureBookClazzLevelDescription", clazzLevel == null ? "" : StringUtils.formatMessage(PICTURE_BOOK_NEW_CLAZZ_LEVEL_DESCRIPTION_TEMPLATE,
                    clazzLevel.getReadingDifficulty(), clazzLevel.getReadingStyle(), clazzLevel.getReadingHabits(), clazzLevel.getReadingAbility(), clazzLevel.getReadingExperience(), clazzLevel.getReadingAmount()));
        }
        return pbMapper;
    }

    public static Map<String, Object> decorateOralCommunicationSummary(StoneBufferedData stoneBufferedData, EmbedBook book, TeacherAssignmentRecord teacherAssignmentRecord) {
        BaseOralPractice baseOralPractice = null;
        InteractivePictureBook interactivePictureBook = stoneBufferedData.getInteractivePictureBook();
        InteractiveVideo interactiveVideo = stoneBufferedData.getInteractiveVideo();
        OralPracticeConversion oralPracticeConversion = stoneBufferedData.getOralPracticeConversion();
        if (interactivePictureBook != null) {
            baseOralPractice = interactivePictureBook;
        } else if (interactiveVideo != null) {
            baseOralPractice = interactiveVideo;
        } else if (oralPracticeConversion != null) {
            baseOralPractice = oralPracticeConversion;
        }
        if (baseOralPractice == null) {
            return null;
        }
        List<String> sentences = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(baseOralPractice.getKeySentences())) {
            sentences.addAll(baseOralPractice.getKeySentences().stream().map(KeySentence::getText).collect(Collectors.toList()));
        }
        OralCommunicationClazzLevel communicationClazzLevel = OralCommunicationClazzLevel.ofGrade(baseOralPractice.getGrade());
        return MapUtils.m(
                "thumbUrl", SafeConverter.toString(baseOralPractice.getThumbUrl()),
                "oralCommunicationId", stoneBufferedData.getId(),
                "oralCommunicationName", SafeConverter.toString(baseOralPractice.getTopicTrans()),
                "clazzLevel", communicationClazzLevel != null ? communicationClazzLevel.getName() : "",
                "sentences", sentences,
                "showAssigned", teacherAssignmentRecord != null && MapUtils.isNotEmpty(teacherAssignmentRecord.getAppInfo()) && SafeConverter.toInt(teacherAssignmentRecord.getAppInfo().get(stoneBufferedData.getId())) > 0,
                "seconds", SafeConverter.toInt(baseOralPractice.getExpectedDuration(), 60),
                "book", buildBookMapper(book)
        );
    }

    public static Map<String, Object> decorateOralCommunicationDetail(StoneBufferedData stoneBufferedData, EmbedBook book, TeacherAssignmentRecord teacherAssignmentRecord) {
        BaseOralPractice baseOralPractice = null;
        InteractivePictureBook interactivePictureBook = stoneBufferedData.getInteractivePictureBook();
        InteractiveVideo interactiveVideo = stoneBufferedData.getInteractiveVideo();
        OralPracticeConversion oralPracticeConversion = stoneBufferedData.getOralPracticeConversion();
        OralCommunicationContentType type = OralCommunicationContentType.INTERACTIVE_PICTURE_BOOK;
        if (interactivePictureBook != null) {
            baseOralPractice = interactivePictureBook;
        } else if (interactiveVideo != null) {
            type = OralCommunicationContentType.INTERACTIVE_VIDEO;
            baseOralPractice = interactiveVideo;
        } else if (oralPracticeConversion != null) {
            type = OralCommunicationContentType.INTERACTIVE_CONVERSATION;
            baseOralPractice = oralPracticeConversion;
        }
        if (baseOralPractice == null) {
            return null;
        }
        List<String> sentences = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(baseOralPractice.getKeySentences())) {
            sentences.addAll(baseOralPractice.getKeySentences().stream().map(KeySentence::getText).collect(Collectors.toList()));
        }
        OralCommunicationClazzLevel communicationClazzLevel = OralCommunicationClazzLevel.ofGrade(baseOralPractice.getGrade());
        int wordsCount = 0;
        List<Map<String, Object>> words = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(baseOralPractice.getKeyWords())) {
            wordsCount = baseOralPractice.getKeyWords().size();
            words = baseOralPractice.getKeyWords()
                    .stream()
                    .map(word -> MapUtils.m("enText", word.getText(), "cnText", word.getTrans()))
                    .collect(Collectors.toList());
        }
        return MapUtils.m(
                "coverUrl", SafeConverter.toString(baseOralPractice.getCoverUrl()),
                "oralCommunicationId", stoneBufferedData.getId(),
                "oralCommunicationName", SafeConverter.toString(baseOralPractice.getTopicTrans()),
                "oralCommunicationType", type.name(),
                "clazzLevel", communicationClazzLevel != null ? communicationClazzLevel.getName() : "",
                "sentences", sentences,
                "wordsCount", wordsCount,
                "words", words,
                "description", SafeConverter.toString(baseOralPractice.getTopicDesc()),
                "showAssigned", teacherAssignmentRecord != null && MapUtils.isNotEmpty(teacherAssignmentRecord.getAppInfo()) && SafeConverter.toInt(teacherAssignmentRecord.getAppInfo().get(stoneBufferedData.getId())) > 0,
                "seconds", SafeConverter.toInt(baseOralPractice.getExpectedDuration(), 60),
                "book", buildBookMapper(book),
                "wordLabel", "重点单词量",
                "toastLabel", "重点单词表"
        );
    }

    public static Map<String, Object> buildBookMapper(EmbedBook book) {
        Map<String, Object> bookMapper = new LinkedHashMap<>();
        if (book != null) {
            if (book.getBookId() != null) {
                bookMapper.put("bookId", book.getBookId());
            }
            if (book.getUnitId() != null) {
                bookMapper.put("unitId", book.getUnitId());
            }
            if (book.getLessonId() != null) {
                bookMapper.put("lessonId", book.getLessonId());
            }
            if (book.getSectionId() != null) {
                bookMapper.put("sectionId", book.getSectionId());
            }
        }
        return bookMapper;
    }

    public static OralCommunicationClazzLevel getOralOralCommunicationLevel(StoneBufferedData data) {
        BaseOralPractice baseOralPractice = null;
        if (data.getOralPracticeConversion() != null) {
            baseOralPractice = data.getOralPracticeConversion();
        }
        if (data.getInteractivePictureBook() != null) {
            baseOralPractice = data.getInteractivePictureBook();
        }
        if (data.getInteractiveVideo() != null) {
            baseOralPractice = data.getInteractiveVideo();
        }
        if (baseOralPractice == null) {
            return OralCommunicationClazzLevel.ALL;
        }
        OralCommunicationClazzLevel level = OralCommunicationClazzLevel.ofGrade(baseOralPractice.getGrade());
        if (level == null) {
            return OralCommunicationClazzLevel.ALL;
        }
        return level;
    }

}
