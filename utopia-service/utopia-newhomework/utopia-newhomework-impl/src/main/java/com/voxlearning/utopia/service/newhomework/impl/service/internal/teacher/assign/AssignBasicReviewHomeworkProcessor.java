package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.content.api.constant.PracticeCategory;
import com.voxlearning.utopia.service.content.api.entity.*;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkPublishMessageType;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.*;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewStage;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.impl.service.AsyncAvengerHomeworkServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.TeachingObjective;
import com.voxlearning.utopia.service.question.api.entity.TermReview;
import com.voxlearning.utopia.service.question.api.mapper.review.ChineseBasicReview;
import com.voxlearning.utopia.service.question.api.mapper.review.EnglishReview;
import com.voxlearning.utopia.service.question.api.mapper.review.MathReview;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2017/11/8
 */
@Named
public class AssignBasicReviewHomeworkProcessor extends NewHomeworkSpringBean {

    @Inject private AsyncAvengerHomeworkServiceImpl asyncAvengerHomeworkService;

    @Inject private RaikouSDK raikouSDK;

    public MapMessage preview(String bookId, List<String> contentTypes, String cdnUrl) {
        if (StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("教材id错误");
        }
        if (CollectionUtils.isEmpty(contentTypes)) {
            return MapMessage.errorMessage("温习内容错误");
        }
        List<BasicReviewContentType> contentTypeList = new ArrayList<>();
        for (String contentType : contentTypes) {
            BasicReviewContentType basicReviewContentType = BasicReviewContentType.of(contentType);
            if (basicReviewContentType != null) {
                contentTypeList.add(basicReviewContentType);
            }
        }
        if (CollectionUtils.isEmpty(contentTypeList)) {
            return MapMessage.errorMessage("温习内容错误");
        }
        NewBookProfile newBookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        if (newBookProfile == null) {
            return MapMessage.errorMessage("教材不存在");
        }
        List<Map<String, Object>> types = contentTypeList.stream()
                .map(type -> MapUtils.m(
                        "contentType", type,
                        "contentTypeName", type.getName(),
                        "contentTypeDescription", type.getDescription()
                ))
                .collect(Collectors.toList());
        List<Map<String, Object>> contents = new ArrayList<>();
        Subject subject = Subject.fromSubjectId(newBookProfile.getSubjectId());

        // 根据教材获取关卡数据
        if (Subject.ENGLISH == subject) {
            TermReview.EnglishType englishType = getEnglishType(contentTypeList);
            List<EnglishReview> englishReviewList = termReviewLoaderClient.getTermReviewLoader().loadEnglishReviews(bookId, englishType);
            if (CollectionUtils.isNotEmpty(englishReviewList)) {
                Set<String> allLessonIdSet;
                allLessonIdSet = englishReviewList.stream()
                        .filter(Objects::nonNull)
                        .map(EnglishReview::getContents)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .map(TermReview.EnglishContent::getLessonId)
                        .collect(Collectors.toSet());
                Map<String, NewBookCatalog> allLessonsMap = newContentLoaderClient.loadBookCatalogByCatalogIds(allLessonIdSet);
                Map<String, List<Sentence>> allSentencesList = newEnglishContentLoaderClient.loadEnglishLessonSentences(allLessonIdSet);
                for (EnglishReview englishReview : englishReviewList) {
                    Integer stageId = englishReview.getRank();
                    String stageName = englishReview.getName();
                    List<TermReview.EnglishContent> englishContentList = englishReview.getContents();
                    if (CollectionUtils.isNotEmpty(englishContentList)) {
                        List<Map<String, Object>> lessonMapperList = new ArrayList<>();
                        for (TermReview.EnglishContent englishContent : englishContentList) {
                            String lessonId = englishContent.getLessonId();
                            NewBookCatalog lesson = allLessonsMap.get(lessonId);
                            List<Sentence> sentenceList = allSentencesList.get(lessonId);
                            List<Integer> categoryIds = englishContent.getCategoryIds();
                            if (lesson != null
                                    && CollectionUtils.isNotEmpty(sentenceList)
                                    && CollectionUtils.isNotEmpty(categoryIds)) {
                                Map<Integer, String> categoryIdNameMap = new LinkedHashMap<>();
                                for (Integer categoryId : categoryIds) {
                                    List<Long> practiceIds = PracticeCategory.categoryPracticeTypesMap.get(categoryId);
                                    if (CollectionUtils.isNotEmpty(practiceIds)) {
                                        PracticeType practiceType = practiceServiceClient
                                                .getPracticeBuffer()
                                                .loadPractice(practiceIds.get(0));
                                        if (practiceType != null) {
                                            categoryIdNameMap.put(categoryId, practiceType.getCategoryName());
                                        }
                                    }
                                }
                                if (MapUtils.isNotEmpty(categoryIdNameMap)) {
                                    List<Map<String, Object>> categories = new ArrayList<>();
                                    categoryIdNameMap.forEach((k, v) -> categories.add(MapUtils.m(
                                            "categoryId", k,
                                            "categoryName", v,
                                            "icon", cdnUrl + StringUtils.formatMessage("s17/commons//mobile/teacher/junior/images/new-english-icon/e-icons-{}.png", PracticeCategory.icon(v)))));
                                    lessonMapperList.add(MapUtils.m(
                                            "lessonId", lessonId,
                                            "sentences", sentenceList.stream().map(Sentence::getEnText).collect(Collectors.toList()),
                                            "categories", categories
                                    ));
                                }
                            }
                        }
                        if (CollectionUtils.isNotEmpty(lessonMapperList)) {
                            contents.add(MapUtils.m(
                                    "stageId", stageId,
                                    "stageName", stageName,
                                    "contentType", "app",
                                    "lessonList", lessonMapperList
                            ));
                        }
                    }
                }
            }
        } else if (Subject.MATH == subject) {
            List<MathReview> mathReviewList = termReviewLoaderClient.getTermReviewLoader().loadMathReviews(bookId);
            if (CollectionUtils.isNotEmpty(mathReviewList)) {
                for (MathReview mathReview : mathReviewList) {
                    Integer stageId = mathReview.getRank();
                    String stageName = mathReview.getName();
                    Integer questionCount = 0;
                    List<TermReview.MathContent> mathContentList = mathReview.getContents();
                    if (CollectionUtils.isNotEmpty(mathContentList)) {
                        for (TermReview.MathContent mathContent : mathContentList) {
                            if (StringUtils.isNotBlank(mathContent.getQuestionId())) {
                                questionCount++;
                            }
                        }
                    }
                    contents.add(MapUtils.m(
                            "stageId", stageId,
                            "stageName", stageName,
                            "contentType", "question",
                            "questionCount", questionCount
                    ));
                }
            }
        } else if (Subject.CHINESE == subject) {
            List<ChineseBasicReview> chineseBasicReviews = termReviewLoaderClient.getTermReviewLoader().loadChineseBasicReviews(bookId);
            if (CollectionUtils.isEmpty(chineseBasicReviews)) {
                return MapMessage.successMessage()
                        .add("types", types)
                        .add("contents", contents);
            }
            //section data
            List<String> sectionIdsList = chineseBasicReviews.stream()
                    .filter(r -> CollectionUtils.isNotEmpty(r.getContents()))
                    .map(ChineseBasicReview::getContents)
                    .flatMap(List::stream)
                    .filter(c -> !Objects.isNull(c))
                    .filter(c -> StringUtils.isNotEmpty(c.getSectionId()))
                    .map(TermReview.ChineseBasicContent::getSectionId)
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(sectionIdsList)) {
                return MapMessage.successMessage()
                        .add("types", types)
                        .add("contents", contents);
            }
            Map<String, NewBookCatalog> sectionMap = newContentLoaderClient.loadBookCatalogByCatalogIds(sectionIdsList);
            if (MapUtils.isEmpty(sectionMap)) {
                return MapMessage.successMessage()
                        .add("types", types)
                        .add("contents", contents);
            }
            // question data
            List<String> questionDocIds = chineseBasicReviews.stream()
                    .filter(r -> CollectionUtils.isNotEmpty(r.getContents()))
                    .map(ChineseBasicReview::getContents)
                    .flatMap(List::stream)
                    .filter(c -> !Objects.isNull(c))
                    .filter(c -> StringUtils.isNotEmpty(c.getSectionId()))
                    .filter(c -> CollectionUtils.isNotEmpty(c.getQuestionIds()))
                    .map(TermReview.ChineseBasicContent::getQuestionIds)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(questionDocIds)) {
                return MapMessage.successMessage()
                        .add("types", types)
                        .add("contents", contents);
            }
            Map<String, NewQuestion> newQuestionDocIdsMap = questionLoaderClient.loadLatestQuestionByDocIds(questionDocIds);
            if (MapUtils.isEmpty(newQuestionDocIdsMap)) {
                return MapMessage.successMessage()
                        .add("types", types)
                        .add("contents", contents);
            }
            for (ChineseBasicReview chineseReview : chineseBasicReviews) {
                Integer stageId = chineseReview.getRank();
                String stageName = chineseReview.getName();
                int questionCount = 0;
                List<TermReview.ChineseBasicContent> chineseContentList = chineseReview.getContents();
                List<String> descriptions = new ArrayList<>();
                if (CollectionUtils.isEmpty(chineseContentList)) {
                    continue;
                }
                for (TermReview.ChineseBasicContent chineseContent : chineseContentList) {
                    if (CollectionUtils.isEmpty(chineseContent.getQuestionIds())) {
                        continue;
                    }
                    questionCount += chineseContent.getQuestionIds().size();
                    int paraCount = CollectionUtils.isNotEmpty(chineseContent.getQuestionIds()) ? chineseContent.getQuestionIds().size() : 0;
                    NewBookCatalog section = sectionMap.get(chineseContent.getSectionId());
                    String lessonName = section != null ? section.getName() : "";
                    List<String> questionIds = chineseContent.getQuestionIds();
                    String questionBoxType = "";
                    if (CollectionUtils.isEmpty(questionIds)) {
                        continue;
                    }
                    NewQuestion question = newQuestionDocIdsMap.get(questionIds.get(0));
                    if (question == null) {
                        descriptions.add("【" + questionBoxType + "】" + lessonName + " 共" + paraCount + "段");
                        continue;
                    }
                    if (SafeConverter.toInt(question.getContentTypeId()) == 1010014) {
                        questionBoxType = QuestionBoxType.READ.getName();
                    }
                    if (SafeConverter.toInt(question.getContentTypeId()) == 1010015) {
                        questionBoxType = QuestionBoxType.RECITE.getName();
                    }
                    descriptions.add("【" + questionBoxType + "】" + lessonName + " 共" + paraCount + "段");
                }

                contents.add(MapUtils.m(
                        "stageId", stageId,
                        "stageName", stageName,
                        "contentType", "question",
                        "questionCount", questionCount,
                        "descriptions", descriptions
                ));
            }
        }
        return MapMessage.successMessage()
                .add("types", types)
                .add("contents", contents);
    }

    @SuppressWarnings("unchecked")
    public MapMessage assign(Teacher teacher, HomeworkSource homeworkSource, HomeworkSourceType homeworkSourceType) {
        String bookId = SafeConverter.toString(homeworkSource.get("bookId"));
        if (StringUtils.isBlank(bookId)) {
            return MapMessage.errorMessage("教材id错误");
        }
        String groupIds = SafeConverter.toString(homeworkSource.get("groupIds"));
        List<Long> groupIdList = StringUtils.toLongList(groupIds);
        if (CollectionUtils.isEmpty(groupIdList)) {
            return MapMessage.errorMessage("班组信息错误");
        }
        Set<Long> groupIdSet = new LinkedHashSet<>(groupIdList);
        groupIdList = new ArrayList<>(groupIdSet);
        int homeworkDays = SafeConverter.toInt(homeworkSource.get("homeworkDays"));
        if (homeworkDays <= 0) {
            return MapMessage.errorMessage("作业天数错误");
        }
        Subject subject = teacher.getSubject();
        NewBookProfile newBookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        if (newBookProfile == null) {
            return MapMessage.errorMessage("教材不存在");
        }
        if (!Objects.equals(newBookProfile.getSubjectId(), subject.getId())) {
            return MapMessage.errorMessage("学科信息错误");
        }
        // 校验班组权限
        for (Long groupId : groupIdList) {
            if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), groupId)) {
                return MapMessage.errorMessage("没有班组" + groupId + "操作权限");
            }
        }

        List<String> contentTypes = (List<String>) homeworkSource.get("contentTypes");
        List<BasicReviewContentType> basicReviewContentTypes = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(contentTypes)) {
            for (String contentType : contentTypes) {
                BasicReviewContentType basicReviewContentType = BasicReviewContentType.of(contentType);
                if (basicReviewContentType == null) {
                    return MapMessage.errorMessage("温习内容错误");
                }
                basicReviewContentTypes.add(basicReviewContentType);
            }
        }
        if (CollectionUtils.isEmpty(basicReviewContentTypes)) {
            return MapMessage.errorMessage("温习内容为空");
        }


        Map<Long, Long> groupIdClazzIdMap = groupLoaderClient.loadGroups(groupIdList, false)
                .values()
                .stream()
                .collect(Collectors.toMap(GroupMapper::getId, GroupMapper::getClazzId));
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(groupIdClazzIdMap.values())
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        Map<Long, String> groupIdClazzNameMap = new HashMap<>();
        for (Long groupId : groupIdList) {
            String clazzName = "";
            Long clazzId = groupIdClazzIdMap.get(groupId);
            if (clazzId != null) {
                Clazz clazz = clazzMap.get(clazzId);
                if (clazz != null) {
                    clazzName = clazz.formalizeClazzName();
                }
            }
            groupIdClazzNameMap.put(groupId, clazzName);
        }

        // 判断是否已经布置过
        Map<Long, List<BasicReviewHomeworkPackage>> assignedPackageMap = basicReviewHomeworkPackageDao.loadBasicReviewHomeworkPackageByClazzGroupIds(groupIdList);
        for (Long groupId : groupIdList) {
            List<BasicReviewHomeworkPackage> packageList = assignedPackageMap.get(groupId);
            if (CollectionUtils.isNotEmpty(packageList)) {
                return MapMessage.errorMessage(groupIdClazzNameMap.get(groupId) + "已经布置过期末基础复习");
            }
        }

        Date currentDate = new Date();
        Long teacherId = teacher.getId();
        String actionId = StringUtils.join(Arrays.asList(teacherId, currentDate.getTime()), "_");
        Map<String, Object> newHomeworkInfoMap = MapUtils.m(
                "actionId", actionId,
                "teacherId", teacherId,
                "subject", subject,
                "currentDate", currentDate,
                "homeworkSourceType", homeworkSourceType,
                "teacher", teacher);

        List<String> packageIds = Collections.emptyList();
        // 每个班组一个package
        Map<Long, BasicReviewHomeworkPackage> packageMap = new LinkedHashMap<>();
        // 由于homework里面需要带上packageId，所以先new出来package
        for (Long groupId : groupIdList) {
            String id = "BR_" + RandomUtils.nextObjectId();
            BasicReviewHomeworkPackage basicReviewHomeworkPackage = new BasicReviewHomeworkPackage();
            basicReviewHomeworkPackage.setId(id);
            basicReviewHomeworkPackage.setActionId(actionId);
            basicReviewHomeworkPackage.setSubject(subject);
            basicReviewHomeworkPackage.setSource(homeworkSourceType);
            basicReviewHomeworkPackage.setTeacherId(teacherId);
            basicReviewHomeworkPackage.setClazzGroupId(groupId);
            basicReviewHomeworkPackage.setBookId(bookId);
            basicReviewHomeworkPackage.setHomeworkDays(homeworkDays);
            basicReviewHomeworkPackage.setContentTypes(basicReviewContentTypes);
            basicReviewHomeworkPackage.setDisabled(false);
            basicReviewHomeworkPackage.setCreateAt(currentDate);
            basicReviewHomeworkPackage.setUpdateAt(currentDate);
            packageMap.put(groupId, basicReviewHomeworkPackage);
        }
        // 每个班组每个关卡一个homework
        Map<Long, Map<Integer, NewHomework>> groupHomeworkMap = new HashMap<>();
        // 方便插入的List
        List<NewHomework> newHomeworkList = new ArrayList<>();
        // 关卡id对应关卡名
        Map<Integer, String> stageIdNameMap = new HashMap<>();

        // 根据教材获取关卡数据
        long duration = 0;
        if (Subject.ENGLISH == subject) {
            TermReview.EnglishType englishType = getEnglishType(basicReviewContentTypes);
            List<EnglishReview> englishReviewList = termReviewLoaderClient.getTermReviewLoader().loadEnglishReviews(bookId, englishType);
            if (CollectionUtils.isNotEmpty(englishReviewList)) {
                for (EnglishReview englishReview : englishReviewList) {
                    Integer stageId = englishReview.getRank();
                    String stageName = englishReview.getName();
                    stageIdNameMap.put(stageId, stageName);
                    NewHomeworkPracticeContent newHomeworkPracticeContent;
                    List<TermReview.EnglishContent> englishContentList = englishReview.getContents();
                    newHomeworkPracticeContent = generateEnglishPracticeContent(englishContentList);
                    if (newHomeworkPracticeContent != null) {
                        for (NewHomeworkApp newHomeworkApp : newHomeworkPracticeContent.getApps()) {
                            duration += newHomeworkApp.getQuestions().stream()
                                    .mapToInt(NewHomeworkQuestion::getSeconds)
                                    .sum();
                        }
                    }
                    generateNewHomeworkList(stageId, duration, groupIdList, newHomeworkInfoMap,
                            newHomeworkPracticeContent, packageMap, groupHomeworkMap, newHomeworkList);
                }
            } else {
                return MapMessage.errorMessage("该教材温习内容为空，请联系客服");
            }
        } else if (Subject.MATH == subject) {
            List<MathReview> mathReviewList = termReviewLoaderClient.getTermReviewLoader().loadMathReviews(bookId);
            if (CollectionUtils.isNotEmpty(mathReviewList)) {
                for (MathReview mathReview : mathReviewList) {
                    Integer stageId = mathReview.getRank();
                    String stageName = mathReview.getName();
                    stageIdNameMap.put(stageId, stageName);
                    NewHomeworkPracticeContent newHomeworkPracticeContent;
                    List<TermReview.MathContent> mathContentList = mathReview.getContents();
                    newHomeworkPracticeContent = generateMathPracticeContent(mathContentList);
                    if (newHomeworkPracticeContent != null) {
                        duration += newHomeworkPracticeContent.getQuestions().stream()
                                .mapToInt(NewHomeworkQuestion::getSeconds)
                                .sum();
                    }
                    generateNewHomeworkList(stageId, duration, groupIdList, newHomeworkInfoMap,
                            newHomeworkPracticeContent, packageMap, groupHomeworkMap, newHomeworkList);
                }
            } else {
                return MapMessage.errorMessage("该教材温习内容为空，请联系客服");
            }
        } else if (Subject.CHINESE == subject) {
            List<ChineseBasicReview> chineseBasicReviews = termReviewLoaderClient.getTermReviewLoader().loadChineseBasicReviews(bookId);
            if (CollectionUtils.isNotEmpty(chineseBasicReviews)) {
                for (ChineseBasicReview chineseBasicReview : chineseBasicReviews) {
                    Integer stageId = chineseBasicReview.getRank();
                    String stageName = chineseBasicReview.getName();
                    stageIdNameMap.put(stageId, stageName);
                    NewHomeworkPracticeContent newHomeworkPracticeContent;
                    List<TermReview.ChineseBasicContent> chineseBasicContentList = chineseBasicReview.getContents();
                    newHomeworkPracticeContent = generateChinesePracticeContent(chineseBasicContentList);
                    if (newHomeworkPracticeContent != null) {
                        duration += newHomeworkPracticeContent.getApps()
                                .stream()
                                .map(app -> app.getQuestions())
                                .flatMap(List::stream)
                                .mapToInt(NewHomeworkQuestion::getSeconds)
                                .sum();
                    }
                    generateNewHomeworkList(stageId, duration, groupIdList, newHomeworkInfoMap,
                            newHomeworkPracticeContent, packageMap, groupHomeworkMap, newHomeworkList);
                }
            } else {
                return MapMessage.errorMessage("该教材温习内容为空，请联系客服");
            }
        }

        if (CollectionUtils.isNotEmpty(newHomeworkList)) {
            newHomeworkService.inserts(newHomeworkList);
            List<BasicReviewHomeworkPackage> packageList = new ArrayList<>();
            List<NewHomeworkBook> newHomeworkBookList = new ArrayList<>();
            for (Long groupId : groupIdList) {
                BasicReviewHomeworkPackage basicReviewHomeworkPackage = packageMap.get(groupId);
                Map<Integer, NewHomework> stageHomeworkMap = groupHomeworkMap.get(groupId);
                if (MapUtils.isNotEmpty(stageHomeworkMap)) {
                    List<BasicReviewStage> basicReviewStages = new ArrayList<>();
                    stageHomeworkMap.forEach((stageId, homework) -> {
                        BasicReviewStage stage = new BasicReviewStage();
                        stage.setStageId(stageId);
                        stage.setStageName(stageIdNameMap.get(stageId));
                        stage.setHomeworkId(homework.getId());
                        basicReviewStages.add(stage);
                    });
                    basicReviewHomeworkPackage.setStages(basicReviewStages);
                    packageList.add(basicReviewHomeworkPackage);
                    if (subject == Subject.CHINESE) {
                        //build homeworkbook info
                        generateHomeworkBookInfo(teacher, basicReviewHomeworkPackage, stageHomeworkMap, newHomeworkBookList);
                    }
                }

            }
            if (CollectionUtils.isNotEmpty(newHomeworkBookList)) {
                newHomeworkService.insertNewHomeworkBooks(newHomeworkBookList);
            }
            if (CollectionUtils.isNotEmpty(packageList)) {
                basicReviewHomeworkPackageDao.inserts(packageList);
                publishAssignedMessage(packageList);
                toAvenger(newHomeworkList);
                packageIds = packageList.stream().map(BasicReviewHomeworkPackage::getId).collect(Collectors.toList());
            }
        } else {
            return MapMessage.errorMessage("生成作业内容失败");
        }
        if (CollectionUtils.isEmpty(packageIds)) {
            return MapMessage.errorMessage("布置失败");
        } else {
            return MapMessage.successMessage().add("packageIds", packageIds);
        }
    }

    private void generateHomeworkBookInfo(Teacher teacher, BasicReviewHomeworkPackage basicReviewHomeworkPackage, Map<Integer, NewHomework> stageHomeworkMap, List<NewHomeworkBook> newHomeworkBookList) {
        if (basicReviewHomeworkPackage == null || MapUtils.isEmpty(stageHomeworkMap)) {
            return;
        }
        for (BasicReviewStage stage : basicReviewHomeworkPackage.getStages()) {
            NewHomeworkBook newHomeworkBook = new NewHomeworkBook();
            NewHomework newHomework = stageHomeworkMap.get(stage.getStageId());
            if (newHomework == null) {
                continue;
            }
            newHomeworkBook.setId(newHomework.getId());
            newHomeworkBook.setSubject(basicReviewHomeworkPackage.getSubject());
            newHomeworkBook.setActionId(basicReviewHomeworkPackage.getActionId());
            newHomeworkBook.setTeacherId(basicReviewHomeworkPackage.getTeacherId());
            newHomeworkBook.setClazzGroupId(newHomework.getClazzGroupId());
            LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> booInfoMap = new LinkedHashMap();
            List<NewHomeworkBookInfo> bookInfoList = new ArrayList<>();
            if (CollectionUtils.isEmpty(newHomework.getPractices())) {
                continue;
            }
            NewBookCatalog newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(basicReviewHomeworkPackage.getBookId());
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            newHomework.getPractices()
                    .stream()
                    .map(NewHomeworkPracticeContent::getApps)
                    .flatMap(List::stream).forEach(app -> {
                NewHomeworkBookInfo bookItem = new NewHomeworkBookInfo();
                String[] questionBoxStr = StringUtils.split(app.getQuestionBoxId(), "-");
                if (ArrayUtils.isNotEmpty(questionBoxStr)) {
                    bookItem.setSectionId(questionBoxStr[0]);
                    NewBookCatalog section = newContentLoaderClient.loadBookCatalogByCatalogId(questionBoxStr[0]);
                    bookItem.setSectionName(section.getName());
                    bookItem.setBookId(basicReviewHomeworkPackage.getBookId());
                    bookItem.setBookName(newBookCatalog.getName());
                    bookItem.setLessonId(section.lessonId());
                    NewBookCatalog lesson = newContentLoaderClient.loadBookCatalogByCatalogId(section.lessonId());
                    bookItem.setLessonName(lesson != null ? lesson.getName() : "");
                    List<NewBookCatalogAncestor> ancestors = section.getAncestors();
                    if (CollectionUtils.isNotEmpty(ancestors) && ancestors.size() > 2) {
                        ancestors.stream().filter(anc -> anc.getNodeType().equals("UNIT"))
                                .findFirst()
                                .ifPresent(a -> {
                                    bookItem.setUnitId(a.getId());
                                    NewBookCatalog unit = newContentLoaderClient.loadBookCatalogByCatalogId(a.getId());
                                    bookItem.setUnitName(unit != null ? unit.getName() : "");
                                    List<TeachingObjective> teachingObjectiveList = teachingObjectiveLoaderClient
                                            .loadLocalTeachingObjectiveByRegionAndUnit(teacherDetail.getRootRegionCode(),
                                                    teacherDetail.getCityCode(), teacherDetail.getRegionCode(), teacherDetail.getTeacherSchoolId(), a.getId());

                                    if (CollectionUtils.isNotEmpty(teachingObjectiveList)) {
                                        bookItem.setObjectiveId(teachingObjectiveList.get(0).getId());
                                        bookItem.setObjectiveName(teachingObjectiveList.get(0).getName());
                                    }
                                });
                        bookItem.setQuestionBoxIds(Arrays.asList(app.getQuestionBoxId()));
                        bookInfoList.add(bookItem);
                    }
                }
            });
            booInfoMap.put(ObjectiveConfigType.READ_RECITE_WITH_SCORE, bookInfoList);
            newHomeworkBook.setPractices(booInfoMap);
            newHomeworkBookList.add(newHomeworkBook);
        }

    }

    private void publishAssignedMessage(List<BasicReviewHomeworkPackage> basicReviewHomeworkPackages) {
        for (BasicReviewHomeworkPackage basicReviewHomeworkPackage : basicReviewHomeworkPackages) {
            Map<String, Object> map = new HashMap<>();
            map.put("messageType", HomeworkPublishMessageType.assign);
            map.put("groupId", basicReviewHomeworkPackage.getClazzGroupId());
            map.put("homeworkId", basicReviewHomeworkPackage.getId());
            map.put("subject", basicReviewHomeworkPackage.getSubject());
            map.put("teacherId", basicReviewHomeworkPackage.getTeacherId());
            map.put("createAt", basicReviewHomeworkPackage.getCreateAt().getTime());
            map.put("startTime", basicReviewHomeworkPackage.getCreateAt().getTime());
            map.put("endTime", NewHomeworkConstants.BASIC_REVIEW_END_DATE.getTime());
            map.put("homeworkType", NewHomeworkType.BasicReview);
            newHomeworkPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
        }
    }

    private void toAvenger(List<NewHomework> homeworkList) {
        for (NewHomework homework : homeworkList) {
            asyncAvengerHomeworkService.informBasicReviewHomeworkToBigData(homework);
        }
    }

    private TermReview.EnglishType getEnglishType(List<BasicReviewContentType> basicReviewContentTypes) {
        TermReview.EnglishType englishType = null;
        if (basicReviewContentTypes.size() == 1) {
            if (basicReviewContentTypes.contains(BasicReviewContentType.WORD)) {
                englishType = TermReview.EnglishType.WORD_ONLY;
            } else if (basicReviewContentTypes.contains(BasicReviewContentType.SENTENCE)) {
                englishType = TermReview.EnglishType.SENTENCE_ONLY;
            }
        } else if (basicReviewContentTypes.size() == 2) {
            if (basicReviewContentTypes.contains(BasicReviewContentType.WORD) && basicReviewContentTypes.contains(BasicReviewContentType.SENTENCE)) {
                englishType = TermReview.EnglishType.WORD_AND_SENTENCE;
            }
        }
        return englishType;
    }

    private void generateNewHomeworkList(Integer stageId,
                                         long duration,
                                         List<Long> groupIdList,
                                         Map<String, Object> newHomeworkInfoMap,
                                         NewHomeworkPracticeContent newHomeworkPracticeContent,
                                         Map<Long, BasicReviewHomeworkPackage> packageMap,
                                         Map<Long, Map<Integer, NewHomework>> groupHomeworkMap,
                                         List<NewHomework> newHomeworkList) {

        if (newHomeworkPracticeContent != null) {
            List<NewHomeworkPracticeContent> practices = new ArrayList<>();
            practices.add(newHomeworkPracticeContent);
            for (Long groupId : groupIdList) {
                NewHomework newHomework = new NewHomework();
                newHomework.setActionId(SafeConverter.toString(newHomeworkInfoMap.get("actionId")));
                newHomework.setTeacherId(SafeConverter.toLong(newHomeworkInfoMap.get("teacherId")));
                newHomework.setSubject((Subject) newHomeworkInfoMap.get("subject"));
                newHomework.setClazzGroupId(groupId);
                newHomework.setStartTime((Date) newHomeworkInfoMap.get("currentDate"));
                newHomework.setEndTime(NewHomeworkConstants.BASIC_REVIEW_END_DATE);
                newHomework.setDuration(duration);
                newHomework.setCreateAt((Date) newHomeworkInfoMap.get("currentDate"));
                newHomework.setUpdateAt((Date) newHomeworkInfoMap.get("currentDate"));
                newHomework.setSource((HomeworkSourceType) newHomeworkInfoMap.get("homeworkSourceType"));
                newHomework.setDisabled(false);
                newHomework.setPractices(practices);
                newHomework.setIncludeSubjective(false);
                newHomework.setType(NewHomeworkType.BasicReview);
                newHomework.setHomeworkTag(HomeworkTag.Last_TermReview);
                Teacher teacher = (Teacher) newHomeworkInfoMap.get("teacher");
                if (teacher.getKtwelve() != null) {
                    newHomework.setSchoolLevel(SchoolLevel.safeParse(teacher.getKtwelve().getLevel()));
                }
                Map<String, String> additions = new HashMap<>();
                additions.put("basicReviewPackageId", packageMap.get(groupId).getId());
                newHomework.setAdditions(additions);

                groupHomeworkMap.computeIfAbsent(groupId, k -> new LinkedHashMap<>()).put(stageId, newHomework);
                newHomeworkList.add(newHomework);
            }
        }
    }

    private NewHomeworkPracticeContent generateEnglishPracticeContent(List<TermReview.EnglishContent> lessonList) {
        if (CollectionUtils.isNotEmpty(lessonList)) {
            List<NewHomeworkApp> newHomeworkApps = new ArrayList<>();
            Set<String> newHomeworkAppKeySet = new HashSet<>();
            List<PracticeType> allPracticeList = practiceLoaderClient.loadPractices();
            Set<String> allLessonIdSet = lessonList.stream()
                    .filter(Objects::nonNull)
                    .map(TermReview.EnglishContent::getLessonId)
                    .collect(Collectors.toSet());
            Map<String, NewBookCatalog> allLessonsMap = newContentLoaderClient.loadBookCatalogByCatalogIds(allLessonIdSet);
            Map<String, List<Sentence>> allSentencesList = newEnglishContentLoaderClient.loadEnglishLessonSentences(allLessonIdSet);

            for (TermReview.EnglishContent englishContent : lessonList) {
                String lessonId = englishContent.getLessonId();
                NewBookCatalog lesson = allLessonsMap.get(lessonId);
                List<Sentence> sentenceList = allSentencesList.get(lessonId);
                List<Integer> categoryIds = englishContent.getCategoryIds();
                if (lesson != null && CollectionUtils.isNotEmpty(sentenceList) && CollectionUtils.isNotEmpty(categoryIds)) {
                    List<Long> sentenceIds = sentenceList.stream().map(Sentence::getId).collect(Collectors.toList());
                    // 调取题接口
                    Map<String, NewQuestion> questionMap = questionLoaderClient.loadRandomQuestionBySentenceIdsAndCategoryIds(sentenceIds, categoryIds, true);
                    for (Integer categoryId : categoryIds) {
                        PracticeType practiceType = allPracticeList.stream()
                                .filter(p -> Objects.equals(p.getCategoryId(), categoryId))
                                .findFirst()
                                .orElse(null);
                        List<NewQuestion> questions = sentenceIds.stream()
                                .map(sid -> questionMap.get(sid + "_" + categoryId))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        if (practiceType != null && CollectionUtils.isNotEmpty(questions)) {
                            List<NewHomeworkQuestion> newHomeworkQuestions = buildBasicAppQuestions(questions, practiceType);
                            NewHomeworkApp newHomeworkApp = new NewHomeworkApp();
                            newHomeworkApp.setCategoryId(categoryId);
                            newHomeworkApp.setPracticeId(practiceType.getId());
                            newHomeworkApp.setLessonId(lessonId);
                            newHomeworkApp.setQuestions(newHomeworkQuestions);

                            String appKey = lessonId + "-" + categoryId;
                            if (!newHomeworkAppKeySet.contains(appKey)) {
                                newHomeworkAppKeySet.add(appKey);
                                newHomeworkApps.add(newHomeworkApp);
                            }
                        }
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
                NewHomeworkPracticeContent newHomeworkPracticeContent = new NewHomeworkPracticeContent();
                newHomeworkPracticeContent.setType(ObjectiveConfigType.BASIC_APP);
                newHomeworkPracticeContent.setIncludeSubjective(false);
                newHomeworkPracticeContent.setApps(newHomeworkApps);
                return newHomeworkPracticeContent;
            }
        }
        return null;
    }

    private NewHomeworkPracticeContent generateChinesePracticeContent(List<TermReview.ChineseBasicContent> chineseBasicContents) {
        if (CollectionUtils.isEmpty(chineseBasicContents)) {
            return null;
        }
        List<NewHomeworkApp> apps = new ArrayList<>();
        for (TermReview.ChineseBasicContent chineseBasicContent : chineseBasicContents) {
            NewHomeworkApp app = new NewHomeworkApp();
            NewBookCatalog newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(chineseBasicContent.getSectionId());
            if (newBookCatalog == null) {
                continue;
            }
            app.setLessonId(newBookCatalog.lessonId());
            List<String> questionIds = chineseBasicContent.getQuestionIds();
            if (CollectionUtils.isEmpty(questionIds)) {
                continue;
            }
            List<NewQuestion> newQuestionList = questionLoaderClient.loadQuestionByDocIds(questionIds);
            if (CollectionUtils.isEmpty(newQuestionList)) {
                continue;
            }
            NewQuestion question = questionLoaderClient.loadQuestionByDocId(questionIds.get(0));
            if (question == null) {
                continue;
            }
            if (SafeConverter.toInt(question.getContentTypeId()) == 1010014) {
                app.setQuestionBoxType(QuestionBoxType.READ);
            }
            if (SafeConverter.toInt(question.getContentTypeId()) == 1010015) {
                app.setQuestionBoxType(QuestionBoxType.RECITE);
            }
            List<NewHomeworkQuestion> result = new ArrayList<>();
            Map<String, Double> scoreMap = questionLoaderClient.parseExamScoreByQuestions(newQuestionList, 100.00);
            for (NewQuestion q : newQuestionList) {
                NewHomeworkQuestion newHomeworkQuestion = new NewHomeworkQuestion();
                newHomeworkQuestion.setQuestionId(q.getId());
                newHomeworkQuestion.setQuestionVersion(q.getOlUpdatedAt() != null ? q.getOlUpdatedAt().getTime() : q.getVersion());
                newHomeworkQuestion.setScore(scoreMap.get(q.getId()));
                newHomeworkQuestion.setSeconds(q.getSeconds());
                newHomeworkQuestion.setSubmitWay(q.getSubmitWays());
                result.add(newHomeworkQuestion);
            }
            app.setQuestionBoxId(chineseBasicContent.getSectionId() + "-" + app.getQuestionBoxType().toString());
            app.setQuestions(result);
            apps.add(app);
        }
        NewHomeworkPracticeContent newHomeworkPracticeContent = new NewHomeworkPracticeContent();
        newHomeworkPracticeContent.setType(ObjectiveConfigType.READ_RECITE_WITH_SCORE);
        newHomeworkPracticeContent.setIncludeSubjective(false);
        newHomeworkPracticeContent.setApps(apps);
        return newHomeworkPracticeContent;

    }

    private NewHomeworkPracticeContent generateMathPracticeContent(List<TermReview.MathContent> questionList) {
        if (CollectionUtils.isNotEmpty(questionList)) {
            Map<String, String> questionKpMap = new LinkedHashMap<>();
            for (TermReview.MathContent mathContent : questionList) {
                String questionId = mathContent.getQuestionId();
                String kpId = mathContent.getKpId();
                questionKpMap.put(questionId, kpId);
            }
            List<NewQuestion> newQuestionList = questionLoaderClient.loadQuestionsIncludeDisabledAsList(questionKpMap.keySet());
            if (CollectionUtils.isNotEmpty(newQuestionList)) {
                Map<String, Double> scoreMap = questionLoaderClient.parseExamScoreByQuestions(newQuestionList, 100.00);
                List<NewHomeworkQuestion> result = new ArrayList<>();
                for (NewQuestion q : newQuestionList) {
                    NewHomeworkQuestion newHomeworkQuestion = new NewHomeworkQuestion();
                    newHomeworkQuestion.setQuestionId(q.getId());
                    newHomeworkQuestion.setQuestionVersion(q.getOlUpdatedAt() != null ? q.getOlUpdatedAt().getTime() : q.getVersion());
                    newHomeworkQuestion.setScore(scoreMap.get(q.getId()));
                    newHomeworkQuestion.setSeconds(q.getSeconds());
                    newHomeworkQuestion.setSubmitWay(q.getSubmitWays());
                    newHomeworkQuestion.setKnowledgePointId(questionKpMap.get(q.getId()));
                    result.add(newHomeworkQuestion);
                }
                NewHomeworkPracticeContent newHomeworkPracticeContent = new NewHomeworkPracticeContent();
                newHomeworkPracticeContent.setType(ObjectiveConfigType.MENTAL_ARITHMETIC);
                newHomeworkPracticeContent.setTimeLimit(MentalArithmeticTimeLimit.ZERO);
                newHomeworkPracticeContent.setMentalAward(Boolean.FALSE);
                newHomeworkPracticeContent.setRecommend(Boolean.FALSE);
                newHomeworkPracticeContent.setIncludeSubjective(false);
                newHomeworkPracticeContent.setQuestions(result);
                return newHomeworkPracticeContent;
            }
        }
        return null;
    }

    private List<NewHomeworkQuestion> buildBasicAppQuestions(List<NewQuestion> questions, PracticeType practiceType) {
        Map<String, Double> qScoreMap = questionLoaderClient.parseExamScoreByQuestions(questions, 100.00);
        // 组装基础训练题的部分 NewHomeworkQuestion
        List<NewHomeworkQuestion> homeworkQuestions = new ArrayList<>();
        for (NewQuestion q : questions) {
            NewHomeworkQuestion homeworkQuestion = new NewHomeworkQuestion();
            if (q != null) {
                homeworkQuestion.setQuestionId(q.getId());
                homeworkQuestion.setQuestionVersion(q.getOlUpdatedAt() != null ? q.getOlUpdatedAt().getTime() : q.getVersion());
                homeworkQuestion.setSeconds(q.getSeconds());
                //跟读类应用的标准分都是100，因为是由语音引擎打分
                if (practiceType.getNeedRecord()) {
                    homeworkQuestion.setScore(100.00);
                } else {
                    homeworkQuestion.setScore(qScoreMap.get(q.getId()));
                }
                homeworkQuestion.setSubmitWay(q.getSubmitWays());
                homeworkQuestions.add(homeworkQuestion);
            }
        }
        return homeworkQuestions;
    }
}
