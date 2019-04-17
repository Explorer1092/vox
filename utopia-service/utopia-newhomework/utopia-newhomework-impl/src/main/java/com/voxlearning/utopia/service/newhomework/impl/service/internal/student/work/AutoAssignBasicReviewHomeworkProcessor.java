package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.api.entity.NewClazzBookRef;
import com.voxlearning.utopia.service.content.consumer.NewClazzBookLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.MentalArithmeticTimeLimit;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewStage;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.BasicReviewHomeworkCacheMapper;
import com.voxlearning.utopia.service.newhomework.impl.dao.basicreview.BasicReviewHomeworkPackageDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.BasicReviewHomeworkCacheLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.BasicReviewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.AsyncAvengerHomeworkServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.helper.NationalDayContentHelper;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;
import com.voxlearning.utopia.service.question.api.entity.TermReview;
import com.voxlearning.utopia.service.question.api.mapper.review.MathReview;
import com.voxlearning.utopia.service.question.consumer.DubbingLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.question.consumer.TermReviewLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class AutoAssignBasicReviewHomeworkProcessor extends SpringContainerSupport {
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private BasicReviewHomeworkLoaderImpl basicReviewHomeworkLoader;
    @Inject
    private NewClazzBookLoaderClient newClazzBookLoaderClient;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @Inject
    private TermReviewLoaderClient termReviewLoaderClient;
    @Inject
    private QuestionLoaderClient questionLoaderClient;
    @Inject
    private NewHomeworkServiceImpl newHomeworkService;
    @Inject
    private BasicReviewHomeworkPackageDao basicReviewHomeworkPackageDao;
    @Inject
    private AsyncAvengerHomeworkServiceImpl asyncAvengerHomeworkService;
    @Inject
    private NationalDayContentHelper nationalDayContentHelper;
    @Inject
    private DubbingLoaderClient dubbingLoaderClient;
    @Inject
    private PictureBookPlusServiceClient pictureBookPlusServiceClient;
    @Inject
    private AppMessageServiceClient appMessageServiceClient;
    @Inject
    private BasicReviewHomeworkCacheLoaderImpl basicReviewHomeworkCacheLoader;

    @Inject private RaikouSDK raikouSDK;

    @ImportService(interfaceClass = UserIntegralService.class)
    private UserIntegralService userIntegralService;

    public MapMessage loadAssignStatus(Teacher teacher) {
        if (teacher == null) {
            return MapMessage.errorMessage("老师不能为空");
        }
        if (!teacher.isPrimarySchool()) {
            return MapMessage.errorMessage("不是小学老师");
        }
        List<Map<String, Object>> assignedGroupMapperList = loadAssignedGroupMappers(teacher);
        String status;
        Date current = new Date();
        if (CollectionUtils.isNotEmpty(assignedGroupMapperList)) {
            status = "SHOW_REPORT";
        } else {
            status = current.after(NewHomeworkConstants.NATIONAL_DAY_HOMEWORK_ASSIGN_END_DATE) ? "SHOW_END" : "SHOW_ASSIGNED";
        }
        return MapMessage.successMessage()
                .add("showAssignButton", true)
                .add("status", status);
    }

    public MapMessage autoAssign(Teacher teacher) {
        if (teacher == null) {
            return MapMessage.errorMessage("老师不能为空");
        }
        if (!teacher.isPrimarySchool()) {
            return MapMessage.errorMessage("不是小学老师");
        }
        List<Map<String, Object>> notAssignedGroupMapperList = loadNotAssignedGroupMappers(teacher, true);
        if (CollectionUtils.isEmpty(notAssignedGroupMapperList)) {
            return MapMessage.errorMessage("没有可布置的班级");
        }
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacher.getId());
        if (mainTeacherId == null) {
            mainTeacherId = teacher.getId();
        }
        Map<String, List<Map<String, Object>>> bookIdGroupMappersMap = new HashMap<>();
        notAssignedGroupMapperList.forEach(
                mapper -> {
                    String bookId = SafeConverter.toString(mapper.get("bookId"));
                    bookIdGroupMappersMap.computeIfAbsent(bookId, k -> new ArrayList<>()).add(mapper);
                }
        );
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBookProfilesIncludeDisabled(bookIdGroupMappersMap.keySet());
        return assign(bookIdGroupMappersMap, bookProfileMap, mainTeacherId);
    }

    public MapMessage deleteNationalDayHomework(Teacher teacher) {
        if (teacher == null) {
            return MapMessage.errorMessage("老师不能为空");
        }
        if (!teacher.isPrimarySchool()) {
            return MapMessage.errorMessage("不是小学老师");
        }
        Date current = new Date();
        if (current.after(NewHomeworkConstants.NATIONAL_DAY_HOMEWORK_END_DATE)) {
            return MapMessage.errorMessage("已超过国庆假期作业截止时间");
        }
        Set<Long> teacherIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
        Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(teacherIds, false);
        Set<Long> groupIds = teacherGroups.values()
                .stream()
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .map(GroupTeacherMapper::getId)
                .collect(Collectors.toSet());
        Map<Long, List<BasicReviewHomeworkPackage>> groupPackageMap = basicReviewHomeworkLoader.loadBasicReviewHomeworkPackageByClazzGroupIds(groupIds);
        Set<String> packageIds = groupPackageMap.values()
                .stream()
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .map(BasicReviewHomeworkPackage::getId)
                .collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(packageIds)) {
            for (String packageId : packageIds) {
                basicReviewHomeworkPackageDao.updateDisableTrue(packageId);
            }
            return MapMessage.successMessage("删除成功")
                    .add("packageIds", packageIds);
        } else {
            return MapMessage.errorMessage("没有已布置的作业");
        }
    }

    public MapMessage loadNationalDayClazzList(Teacher teacher) {
        if (teacher == null) {
            return MapMessage.errorMessage("老师不能为空");
        }
        if (!teacher.isPrimarySchool()) {
            return MapMessage.errorMessage("不是小学老师");
        }
        List<Map<String, Object>> clazzList = loadAssignedGroupMappers(teacher);
        if (CollectionUtils.isEmpty(clazzList)) {
            return MapMessage.errorMessage("没有布置过的班级");
        }
        return MapMessage.successMessage()
                .add("clazzList", clazzList);
    }

    public MapMessage loadNationDaySummaryReport(Teacher teacher, String packageId) {
        if (teacher == null) {
            return MapMessage.errorMessage("老师不能为空");
        }
        if (!teacher.isPrimarySchool()) {
            return MapMessage.errorMessage("不是小学老师");
        }
        BasicReviewHomeworkPackage basicReviewHomeworkPackage = basicReviewHomeworkLoader.load(packageId);
        if (basicReviewHomeworkPackage == null || basicReviewHomeworkPackage.isDisabledTrue()) {
            return MapMessage.errorMessage("国庆假期练习不存在或者已被删除");
        }
        List<BasicReviewStage> stages = basicReviewHomeworkPackage.getStages();
        if (CollectionUtils.isEmpty(stages)) {
            return MapMessage.errorMessage("关卡不存在");
        }
        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(basicReviewHomeworkPackage.getClazzGroupId())
                .stream()
                .collect(Collectors.toMap(LongIdEntity::getId, Function.identity()));
        Set<Long> userIds = userMap.keySet();
        Map<Long, BasicReviewHomeworkCacheMapper> basicReviewHomeworkCacheMapperMap = basicReviewHomeworkCacheLoader.loadBasicReviewHomeworkCacheMapper(packageId, userIds);
        List<Map<String, Object>> studentDetailList = new ArrayList<>();
        for (Long userId : userIds) {
            BasicReviewHomeworkCacheMapper cacheMapper = basicReviewHomeworkCacheMapperMap.get(userId);
            int finishedCount = cacheMapper != null ? SafeConverter.toInt(cacheMapper.getFinishPackageCount()) : 0;
            studentDetailList.add(
                    MapUtils.m(
                            "studentId", userId,
                            "studentName", userMap.get(userId).fetchRealname(),
                            "finishedCount", finishedCount,
                            "finishRate", finishedCount + "/4"
                    ));
        }
        if (CollectionUtils.isNotEmpty(studentDetailList)) {
            studentDetailList = studentDetailList
                    .stream()
                    .sorted((s1, s2) -> Integer.compare(SafeConverter.toInt(s2.get("finishedCount")), SafeConverter.toInt(s1.get("finishedCount"))))
                    .collect(Collectors.toList());
        }
        return MapMessage.successMessage()
                .add("studentDetailList", studentDetailList)
                .add("stageCount", stages.size());
    }

    @SuppressWarnings("unchecked")
    private MapMessage assign(Map<String, List<Map<String, Object>>> bookIdGroupMappersMap, Map<String, NewBookProfile> bookProfileMap, Long mainTeacherId) {
        Date current = new Date();
        // 每个班组一个package
        Map<Long, BasicReviewHomeworkPackage> packageMap = new LinkedHashMap<>();
        // 每个班组每个关卡一个homework
        Map<Long, Map<Integer, NewHomework>> groupHomeworkMap = new HashMap<>();
        // 方便插入的List
        List<NewHomework> newHomeworkList = new ArrayList<>();
        // 关卡id对应关卡名
        Map<Integer, String> stageIdNameMap = new HashMap<>();
        // 有作业内容的groupId
        Set<Long> groupIdSet = new LinkedHashSet<>();
        // 班组id对应的学生ids
        Map<Long, List<Long>> groupStudentIds = new HashMap<>();
        // 根据教材获取作业内容
        for (String bookId : bookIdGroupMappersMap.keySet()) {
            NewBookProfile newBookProfile = bookProfileMap.get(bookId);
            if (newBookProfile != null) {
                Subject subject = Subject.fromSubjectId(newBookProfile.getSubjectId());
                List<List<NewHomeworkPracticeContent>> newHomeworkPracticeContents = generatePracticeContent(bookId, subject, newBookProfile.getClazzLevel(), newBookProfile.getStartClazzLevel());
                if (CollectionUtils.isNotEmpty(newHomeworkPracticeContents)) {
                    for (Map<String, Object> mapper : bookIdGroupMappersMap.get(bookId)) {
                        Long groupId = SafeConverter.toLong(mapper.get("groupId"));
                        Long teacherId = SafeConverter.toLong(mapper.get("teacherId"));
                        List<Long> studentIds = (List<Long>) mapper.get("studentIds");
                        String actionId = StringUtils.join(Arrays.asList(teacherId, current.getTime()), "_");

                        groupIdSet.add(groupId);
                        groupStudentIds.put(groupId, studentIds);
                        // 创建package
                        String packageId = "BR_" + RandomUtils.nextObjectId();
                        BasicReviewHomeworkPackage basicReviewHomeworkPackage = new BasicReviewHomeworkPackage();
                        basicReviewHomeworkPackage.setId(packageId);
                        basicReviewHomeworkPackage.setActionId(actionId);
                        basicReviewHomeworkPackage.setSubject(subject);
                        basicReviewHomeworkPackage.setSource(HomeworkSourceType.App);
                        basicReviewHomeworkPackage.setTeacherId(teacherId);
                        basicReviewHomeworkPackage.setClazzGroupId(groupId);
                        basicReviewHomeworkPackage.setBookId(bookId);
                        basicReviewHomeworkPackage.setHomeworkDays(4);
                        basicReviewHomeworkPackage.setContentTypes(Collections.emptyList());
                        basicReviewHomeworkPackage.setDisabled(false);
                        basicReviewHomeworkPackage.setCreateAt(current);
                        basicReviewHomeworkPackage.setUpdateAt(current);
                        packageMap.put(groupId, basicReviewHomeworkPackage);

                        // 创建homework
                        for (int i = 1; i <= newHomeworkPracticeContents.size(); i++) {
                            String stageName = "第" + i + "关";
                            stageIdNameMap.put(i, stageName);

                            List<NewHomeworkPracticeContent> newHomeworkPracticeContent = newHomeworkPracticeContents.get(i - 1);
                            long duration = 0;
                            if (Subject.MATH == subject) {
                                duration += newHomeworkPracticeContent.get(0).getQuestions()
                                        .stream()
                                        .mapToInt(NewHomeworkQuestion::getSeconds)
                                        .sum();
                            } else {
                                duration = 5 * 60 * 1000;
                            }
                            NewHomework newHomework = new NewHomework();
                            newHomework.setActionId(actionId);
                            newHomework.setTeacherId(teacherId);
                            newHomework.setSubject(subject);
                            newHomework.setClazzGroupId(groupId);
                            newHomework.setStartTime(current);
                            newHomework.setEndTime(NewHomeworkConstants.NATIONAL_DAY_HOMEWORK_END_DATE);
                            newHomework.setDuration(duration);
                            newHomework.setCreateAt(current);
                            newHomework.setUpdateAt(current);
                            newHomework.setSource(HomeworkSourceType.App);
                            newHomework.setDisabled(false);
                            newHomework.setPractices(newHomeworkPracticeContent);
                            newHomework.setIncludeSubjective(false);
                            newHomework.setType(NewHomeworkType.BasicReview);
                            newHomework.setHomeworkTag(HomeworkTag.Last_TermReview);
                            newHomework.setSchoolLevel(SchoolLevel.JUNIOR);
                            Map<String, String> additions = new HashMap<>();
                            additions.put("basicReviewPackageId", packageId);
                            newHomework.setAdditions(additions);

                            groupHomeworkMap.computeIfAbsent(groupId, k -> new LinkedHashMap<>()).put(i, newHomework);
                            newHomeworkList.add(newHomework);
                        }
                    }
                } else {
                    logger.error("BasicReview homework content not exists, bookId : {}, groupId ", bookId);
                    return MapMessage.errorMessage(newBookProfile.getName() + "没有作业内容")
                            .add("bookId", bookId)
                            .add("mappers", bookIdGroupMappersMap.get(bookId));
                }
            }
        }
        List<String> packageIds = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(newHomeworkList)) {
            newHomeworkService.inserts(newHomeworkList);
            List<BasicReviewHomeworkPackage> packageList = new ArrayList<>();
            for (Long groupId : groupIdSet) {
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
                }
            }
            if (CollectionUtils.isNotEmpty(packageList)) {
                basicReviewHomeworkPackageDao.inserts(packageList);
                sendMobileNotification(packageList, groupStudentIds);
                toAvenger(newHomeworkList);
                packageIds = packageList.stream().map(BasicReviewHomeworkPackage::getId).collect(Collectors.toList());
                String comment = "老师布置国庆趣味作业奖励";
                IntegralHistory integralHistory = new IntegralHistory(mainTeacherId, IntegralType.TEACHER_ASSIGN_HOLIDAY_HOMEWORK_REWARD, 200);
                String uniqueKey = StringUtils.join(Arrays.asList(IntegralType.TEACHER_ASSIGN_HOLIDAY_HOMEWORK_REWARD.name(), mainTeacherId, "2018_NATIONAL_DAY"), "-");
                integralHistory.setComment(comment);
                integralHistory.setUniqueKey(uniqueKey);
                userIntegralService.changeIntegral(integralHistory);
            }
        }
        if (CollectionUtils.isEmpty(packageIds)) {
            logger.error("Failed to assign BasicReview homework, mainTeacherId : {}", mainTeacherId);
            return MapMessage.errorMessage("布置失败");
        } else {
            return MapMessage.successMessage().add("packageIds", packageIds);
        }
    }

    private void sendMobileNotification(List<BasicReviewHomeworkPackage> packageList, Map<Long, List<Long>> groupStudentIds) {
        AlpsThreadPool.getInstance().submit(() -> {
            Date current = new Date();
            for (BasicReviewHomeworkPackage basicReviewHomeworkPackage : packageList) {
                String t = "h5";
                String link = UrlUtils.buildUrlQuery("/resources/apps/hwh5/homework/V2_5_0/basic-review-2017-autumn/index.html",
                        MiscUtils.m("packageId", basicReviewHomeworkPackage.getId(), "subject", basicReviewHomeworkPackage.getSubject()));
                String content = "老师布置了国庆趣味作业，请按时完成";
                List<Long> studentIds = groupStudentIds.get(basicReviewHomeworkPackage.getClazzGroupId());
                Map<String, Object> extInfo = MiscUtils.m("s", StudentAppPushType.HOMEWORK_ASSIGN_REMIND.getType(), "link", link, "t", t, "key", "j", "title", StudentAppPushType.HOMEWORK_ASSIGN_REMIND.getDescription());
                if (current.before(NewHomeworkConstants.NATIONAL_DAY_HOMEWORK_PUSH_STAT_DATE)) {
                    appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.STUDENT, studentIds, extInfo, NewHomeworkConstants.NATIONAL_DAY_HOMEWORK_PUSH_STAT_DATE.getTime());
                } else {
                    appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.STUDENT, studentIds, extInfo);
                }
            }
        });
    }

    private void toAvenger(List<NewHomework> homeworkList) {
        for (NewHomework homework : homeworkList) {
            asyncAvengerHomeworkService.informBasicReviewHomeworkToBigData(homework);
        }
    }

    private List<List<NewHomeworkPracticeContent>> generatePracticeContent(String bookId, Subject subject, Integer clazzLevel, Integer startClazzLevel) {
        switch (subject) {
            case MATH:
                List<MathReview> mathReviewList = termReviewLoaderClient.getTermReviewLoader().loadMathReviews(bookId);
                if (CollectionUtils.isNotEmpty(mathReviewList)) {
                    List<List<NewHomeworkPracticeContent>> newHomeworkPracticeContents = new ArrayList<>();
                    for (MathReview mathReview : mathReviewList) {
                        NewHomeworkPracticeContent newHomeworkPracticeContent = generateMathPracticeContent(mathReview.getContents());
                        if (newHomeworkPracticeContent != null) {
                            newHomeworkPracticeContents.add(Collections.singletonList(newHomeworkPracticeContent));
                            if (newHomeworkPracticeContents.size() == 4) {
                                break;
                            }
                        }
                    }
                    return newHomeworkPracticeContents;
                }
                break;
            case ENGLISH:
                List<NationalDayContentHelper.ConfigContent> englishConfigContents = nationalDayContentHelper.loadEnglishConfigContent();
                if (CollectionUtils.isNotEmpty(englishConfigContents)) {
                    NationalDayContentHelper.ConfigContent matchConfigContent = null;
                    for (NationalDayContentHelper.ConfigContent content : englishConfigContents) {
                        if (Objects.equals(content.getClazzLevel(), clazzLevel) && Objects.equals(content.getStartClazzLevel(), startClazzLevel)) {
                            matchConfigContent = content;
                            break;
                        }
                    }
                    if (matchConfigContent != null) {
                        List<String> dubbingIds = matchConfigContent.getDubbingIds();
                        List<String> pictureBookPlusIds = matchConfigContent.getPictureBookPlusIds();
                        if (CollectionUtils.isNotEmpty(dubbingIds) && dubbingIds.size() == 4
                                && CollectionUtils.isNotEmpty(pictureBookPlusIds) && pictureBookPlusIds.size() == 4) {
                            Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByDocIds(dubbingIds);
                            Map<String, PictureBookPlus> pictureBookPlusMap = pictureBookPlusServiceClient.loadByIds(pictureBookPlusIds);
                            if (MapUtils.isNotEmpty(dubbingMap) && dubbingMap.size() == 4
                                    && MapUtils.isNotEmpty(pictureBookPlusMap) && pictureBookPlusMap.size() == 4) {
                                return generateEnglishPracticeContent(new ArrayList<>(dubbingMap.values()), new ArrayList<>(pictureBookPlusMap.values()));
                            }
                        }
                    }
                }
                break;
            case CHINESE:
                List<NationalDayContentHelper.ConfigContent> chineseConfigContents = nationalDayContentHelper.loadChineseConfigContent();
                if (CollectionUtils.isNotEmpty(chineseConfigContents)) {
                    NationalDayContentHelper.ConfigContent matchConfigContent = null;
                    for (NationalDayContentHelper.ConfigContent content : chineseConfigContents) {
                        if (Objects.equals(content.getClazzLevel(), clazzLevel)) {
                            matchConfigContent = content;
                            break;
                        }
                    }
                    if (matchConfigContent != null) {
                        List<String> pictureBookPlusIds = matchConfigContent.getPictureBookPlusIds();
                        if (CollectionUtils.isNotEmpty(pictureBookPlusIds) && pictureBookPlusIds.size() == 4) {
                            Map<String, PictureBookPlus> pictureBookPlusMap = pictureBookPlusServiceClient.loadByIds(pictureBookPlusIds);
                            if (MapUtils.isNotEmpty(pictureBookPlusMap) && pictureBookPlusMap.size() == 4) {
                                return generateChinesePracticeContent(new ArrayList<>(pictureBookPlusMap.values()));
                            }
                        }
                    }
                }
                break;
        }
        return null;
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

    private List<NewHomeworkQuestion> buildDubbingQuestions(List<String> qids) {
        if (CollectionUtils.isEmpty(qids)) {
            return Collections.emptyList();
        }

        List<NewQuestion> questions = questionLoaderClient.loadQuestionsIncludeDisabledAsList(qids);
        if (CollectionUtils.isEmpty(questions)) {
            return Collections.emptyList();
        }

        List<NewHomeworkQuestion> result = new ArrayList<>();
        for (NewQuestion q : questions) {
            NewHomeworkQuestion nhq = new NewHomeworkQuestion();
            nhq.setQuestionId(q.getId());
            nhq.setQuestionVersion(q.getOlUpdatedAt() != null ? q.getOlUpdatedAt().getTime() : q.getVersion());
            // 配音题不打分，这个字段没用
            nhq.setScore(100D);
            nhq.setSeconds(q.getSeconds());
            nhq.setSubmitWay(q.getSubmitWays());
            result.add(nhq);
        }
        return result;
    }

    private List<NewHomeworkQuestion> buildQuestions(List<String> qids, Map<String, NewQuestion> questionMap, Map<String, Double> scoreMap) {
        if (CollectionUtils.isEmpty(qids)) {
            return Collections.emptyList();
        }

        List<NewHomeworkQuestion> result = new ArrayList<>();
        for (String qid : qids) {
            NewQuestion newQuestion = questionMap.get(qid);
            if (newQuestion != null) {
                NewHomeworkQuestion nhq = new NewHomeworkQuestion();
                nhq.setQuestionId(newQuestion.getId());
                // 题目版本号
                nhq.setQuestionVersion(newQuestion.getOlUpdatedAt() != null ? newQuestion.getOlUpdatedAt().getTime() : newQuestion.getVersion());
                nhq.setScore(scoreMap.get(newQuestion.getId()));
                nhq.setSeconds(newQuestion.getSeconds());
                nhq.setSubmitWay(newQuestion.getSubmitWays());
                result.add(nhq);
            }
        }
        return result;
    }

    private List<List<NewHomeworkPracticeContent>> generateEnglishPracticeContent(List<Dubbing> dubbingList, List<PictureBookPlus> pictureBookPlusList) {
        List<List<NewHomeworkPracticeContent>> newHomeworkPracticeContents = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            Dubbing dubbing = dubbingList.get(i - 1);

            List<String> practiceQuestionIds = dubbing.getPracticeQuestions();
            List<NewHomeworkQuestion> newHomeworkQuestions = buildDubbingQuestions(practiceQuestionIds);
            if (CollectionUtils.isEmpty(newHomeworkQuestions)) {
                return null;
            }

            NewHomeworkApp newHomeworkApp = new NewHomeworkApp();
            newHomeworkApp.setDubbingId(dubbing.getId());
            newHomeworkApp.setQuestions(newHomeworkQuestions);

            List<NewHomeworkApp> newHomeworkApps = new ArrayList<>();
            newHomeworkApps.add(newHomeworkApp);

            NewHomeworkPracticeContent dubbingContent = new NewHomeworkPracticeContent();
            dubbingContent.setType(ObjectiveConfigType.DUBBING_WITH_SCORE);
            dubbingContent.setApps(newHomeworkApps);

            PictureBookPlus pictureBookPlus = pictureBookPlusList.get(i - 1);
            NewHomeworkPracticeContent pictureBookPlusContent = generatePictureBookPlusContent(pictureBookPlus, Subject.ENGLISH);
            if (pictureBookPlusContent == null) {
                return null;
            }

            List<NewHomeworkPracticeContent> newHomeworkPracticeContentList = new ArrayList<>();
            newHomeworkPracticeContentList.add(pictureBookPlusContent);
            newHomeworkPracticeContentList.add(dubbingContent);
            newHomeworkPracticeContents.add(newHomeworkPracticeContentList);
        }
        return newHomeworkPracticeContents;
    }

    private NewHomeworkPracticeContent generatePictureBookPlusContent(PictureBookPlus pictureBookPlus, Subject subject) {
        Set<String> questionIds = new HashSet<>();
        if (CollectionUtils.isNotEmpty(pictureBookPlus.getPracticeQuestions())) {
            questionIds.addAll(pictureBookPlus.getPracticeQuestions());
        }
        if (CollectionUtils.isNotEmpty(pictureBookPlus.getOralQuestions())) {
            questionIds.addAll(pictureBookPlus.getOralQuestions());
        }
        NewHomeworkApp newHomeworkApp = new NewHomeworkApp();
        newHomeworkApp.setContainsDubbing(Subject.ENGLISH == subject);
        Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        if (MapUtils.isEmpty(questionMap)) {
            return null;
        }
        // 计算每题的标准分
        Map<String, Double> scoreMap = questionLoaderClient.parseExamScoreByQuestions(new ArrayList<>(questionMap.values()), 40.00);
        if (CollectionUtils.isNotEmpty(pictureBookPlus.getPracticeQuestions())) {
            List<NewHomeworkQuestion> newHomeworkQuestions = buildQuestions(pictureBookPlus.getPracticeQuestions(), questionMap, scoreMap);
            if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                newHomeworkApp.setQuestions(newHomeworkQuestions);
            }
        }
        if (CollectionUtils.isNotEmpty(pictureBookPlus.getOralQuestions())) {
            List<NewHomeworkQuestion> newHomeworkQuestions = buildQuestions(pictureBookPlus.getOralQuestions(), questionMap, scoreMap);
            if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                newHomeworkApp.setOralQuestions(newHomeworkQuestions);
            }
        }
        newHomeworkApp.setPictureBookId(pictureBookPlus.getId());
        NewHomeworkPracticeContent newHomeworkPracticeContent = new NewHomeworkPracticeContent();
        newHomeworkPracticeContent.setApps(Collections.singletonList(newHomeworkApp));
        newHomeworkPracticeContent.setType(ObjectiveConfigType.LEVEL_READINGS);
        return newHomeworkPracticeContent;
    }

    private List<List<NewHomeworkPracticeContent>> generateChinesePracticeContent(List<PictureBookPlus> pictureBookPlusList) {
        List<List<NewHomeworkPracticeContent>> newHomeworkPracticeContents = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            PictureBookPlus pictureBookPlus = pictureBookPlusList.get(i - 1);
            NewHomeworkPracticeContent pictureBookPlusContent = generatePictureBookPlusContent(pictureBookPlus, Subject.CHINESE);
            if (pictureBookPlusContent == null) {
                return null;
            }

            List<NewHomeworkPracticeContent> newHomeworkPracticeContentList = new ArrayList<>();
            newHomeworkPracticeContentList.add(pictureBookPlusContent);
            newHomeworkPracticeContents.add(newHomeworkPracticeContentList);
        }
        return newHomeworkPracticeContents;
    }

    private List<Map<String, Object>> loadAssignedGroupMappers(Teacher teacher) {
        Set<Long> teacherIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
        Map<Subject, Long> subjectTeacherIdMap = teacherLoaderClient.loadTeachers(teacherIds)
                .values()
                .stream()
                .collect(Collectors.toMap(Teacher::getSubject, Teacher::getId));
        // 老师分组
        Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(teacherIds, false);
        // 分组id
        List<Long> groupIds = new LinkedList<>();
        // 班级id
        List<Long> clazzIds = new LinkedList<>();
        // clazz id -> group id map
        Map<Long, List<Long>> clazzIdGroupIdMap = new HashMap<>();
        // 学科 -> 分组id
        Map<Subject, List<Long>> subjectGroupIdsMap = new HashMap<>();
        // 分组id -> 学科
        Map<Long, Subject> groupIdSubjectMap = new HashMap<>();
        teacherGroups.forEach((teacherId, groups) -> groups.forEach(group -> {
            if (group.isTeacherGroupRefStatusValid(teacherId)) {
                // 分组id
                groupIds.add(group.getId());
                // 班级id
                clazzIds.add(group.getClazzId());
                // 学科 -> 分组id
                List<Long> subjectGroupIds = subjectGroupIdsMap.computeIfAbsent(group.getSubject(), k -> new ArrayList<>());
                subjectGroupIds.add(group.getId());
                // clazz id -> group id map
                List<Long> clazzIdGroupIds = clazzIdGroupIdMap.computeIfAbsent(group.getClazzId(), k -> new ArrayList<>());
                clazzIdGroupIds.add(group.getId());
                groupIdSubjectMap.put(group.getId(), group.getSubject());
            }
        }));

        // 分组学生
        Map<Long, List<Long>> groupStudentIds = studentLoaderClient.loadGroupStudentIds(groupIds);
        // 分组作业
        Map<Long, List<BasicReviewHomeworkPackage>> groupPackageMap = basicReviewHomeworkLoader.loadBasicReviewHomeworkPackageByClazzGroupIds(groupIds);
        List<Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());
        List<Map<String, Object>> assignedGroupMapperList = new ArrayList<>();
        clazzs.stream()
                .filter(clazz -> clazzIdGroupIdMap.containsKey(clazz.getId()))
                .forEach((Clazz c) -> {
                    List<Long> gids = clazzIdGroupIdMap.get(c.getId());
                    gids.forEach((Long gid) -> {
                        if (CollectionUtils.isNotEmpty(groupStudentIds.get(gid))) {
                            List<BasicReviewHomeworkPackage> packages = groupPackageMap.get(gid);
                            Subject subject = groupIdSubjectMap.get(gid);
                            // 未布置过的不管了
                            if (CollectionUtils.isNotEmpty(packages)) {
                                BasicReviewHomeworkPackage basicReviewHomeworkPackage = packages.get(0);
                                assignedGroupMapperList.add(MapUtils.m(
                                        "teacherId", subjectTeacherIdMap.getOrDefault(subject, teacher.getId()),
                                        "subject", subject,
                                        "clazzId", c.getId(),
                                        "clazzName", c.formalizeClazzName() + "(" + subject.getValue() + ")",
                                        "groupId", gid,
                                        "bookId", basicReviewHomeworkPackage.getBookId(),
                                        "packageId", basicReviewHomeworkPackage.getId()
                                ));
                            }
                        }
                    });
                });
        return assignedGroupMapperList;
    }

    private List<Map<String, Object>> loadNotAssignedGroupMappers(Teacher teacher, boolean needBookId) {
        Set<Long> teacherIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
        Map<Subject, Long> subjectTeacherIdMap = teacherLoaderClient.loadTeachers(teacherIds)
                .values()
                .stream()
                .collect(Collectors.toMap(Teacher::getSubject, Teacher::getId));
        // 老师分组
        Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(teacherIds, false);
        // 分组id
        List<Long> groupIds = new LinkedList<>();
        // 班级id
        List<Long> clazzIds = new LinkedList<>();
        // clazz id -> group id map
        Map<Long, List<Long>> clazzIdGroupIdMap = new HashMap<>();
        // 学科 -> 分组id
        Map<Subject, List<Long>> subjectGroupIdsMap = new HashMap<>();
        // 分组id -> 学科
        Map<Long, Subject> groupIdSubjectMap = new HashMap<>();

        teacherGroups.forEach((teacherId, groups) -> groups.forEach(group -> {
            if (group.isTeacherGroupRefStatusValid(teacherId)) {
                // 分组id
                groupIds.add(group.getId());
                // 班级id
                clazzIds.add(group.getClazzId());
                // 学科 -> 分组id
                List<Long> subjectGroupIds = subjectGroupIdsMap.computeIfAbsent(group.getSubject(), k -> new ArrayList<>());
                subjectGroupIds.add(group.getId());
                // clazz id -> group id map
                List<Long> clazzIdGroupIds = clazzIdGroupIdMap.computeIfAbsent(group.getClazzId(), k -> new ArrayList<>());
                clazzIdGroupIds.add(group.getId());
                groupIdSubjectMap.put(group.getId(), group.getSubject());
            }
        }));

        // 分组学生
        Map<Long, List<Long>> groupStudentIds = studentLoaderClient.loadGroupStudentIds(groupIds);
        // 分组作业
        Map<Long, List<BasicReviewHomeworkPackage>> groupPackageMap = basicReviewHomeworkLoader.loadBasicReviewHomeworkPackageByClazzGroupIds(groupIds);
        List<Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());
        //获取班级组课本ID
        Map<Long, List<NewClazzBookRef>> clazzBookRefMap = newClazzBookLoaderClient.loadGroupBookRefs(groupIds)
                .toList()
                .stream()
                .collect(Collectors.groupingBy(NewClazzBookRef::getGroupId, Collectors.toList()));
        List<Map<String, Object>> notAssignedGroupMapperList = new ArrayList<>();
        clazzs.stream()
                .filter(clazz -> clazzIdGroupIdMap.containsKey(clazz.getId()))
                .forEach((Clazz c) -> {
                    List<Long> gids = clazzIdGroupIdMap.get(c.getId());
                    gids.forEach((Long gid) -> {
                        if (CollectionUtils.isNotEmpty(groupStudentIds.get(gid))) {
                            List<BasicReviewHomeworkPackage> packages = groupPackageMap.get(gid);
                            // 已经布置过的不管了
                            if (CollectionUtils.isEmpty(packages)) {
                                Subject subject = groupIdSubjectMap.get(gid);
                                //课本赋值
                                String bookId = null;
                                if (needBookId) {
                                    NewClazzBookRef newClazzBookRef = clazzBookRefMap.get(gid) != null ? clazzBookRefMap.get(gid)
                                            .stream()
                                            .filter(o -> StringUtils.equalsIgnoreCase(o.getSubject(), subject.name()))
                                            .min((o1, o2) -> Long.compare(o2.fetchUpdateTimestamp(), o1.fetchUpdateTimestamp()))
                                            .orElse(null) : null;
                                    if (newClazzBookRef != null) {
                                        bookId = newClazzBookRef.getBookId();
                                        NewBookProfile newBookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
                                        if (newBookProfile == null || !newBookProfile.isOnline()) {
                                            bookId = null;
                                        }
                                    }
                                    if (bookId == null) {
                                        School school = schoolLoaderClient.getSchoolLoader()
                                                .loadSchool(c.getSchoolId())
                                                .getUninterruptibly();
                                        if (school != null) {
                                            bookId = newContentLoaderClient.initializeClazzBook(subject, c.getClazzLevel(), school.getRegionCode());
                                        }
                                    }
                                }
                                notAssignedGroupMapperList.add(MapUtils.m(
                                        "teacherId", subjectTeacherIdMap.getOrDefault(subject, teacher.getId()),
                                        "subject", subject,
                                        "clazzId", c.getId(),
                                        "clazzName", c.formalizeClazzName(),
                                        "groupId", gid,
                                        "bookId", bookId,
                                        "studentIds", groupStudentIds.get(gid)
                                ));
                            }
                        }
                    });
                });
        return notAssignedGroupMapperList;
    }
}
