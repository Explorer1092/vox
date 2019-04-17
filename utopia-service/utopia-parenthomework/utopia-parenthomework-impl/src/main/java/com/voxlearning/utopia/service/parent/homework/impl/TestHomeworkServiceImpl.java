package com.voxlearning.utopia.service.parent.homework.impl;

import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.athena.api.recom.entity.EliteSchoolPackage;
import com.voxlearning.athena.api.recom.loader.ParentRecommendLoader;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.MQUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.integral.api.mapper.IntegralInfo;
import com.voxlearning.utopia.service.integral.client.IntegralServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkActivityService;
import com.voxlearning.utopia.service.parent.homework.api.entity.*;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkDao;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkPracticeDao;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkUserActivityDao;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkUserRefDao;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;
import com.voxlearning.utopia.service.parent.homework.impl.util.HttpUtil;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import lombok.extern.log4j.Log4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATETIME;
import static com.voxlearning.utopia.service.parent.homework.impl.util.Constants.ACTIVITY_ID;
import static com.voxlearning.utopia.service.parent.homework.impl.util.Constants.HOMEWORK_TOPIC;
import static com.voxlearning.utopia.service.parent.homework.impl.util.Constants.QRCODE_URL;

/**
 * 仅供测试布置作业使用，TODO delete
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-19
 */
@Named
@ExposeService(interfaceClass = TestHomeworkService.class)
@Log4j
public class TestHomeworkServiceImpl extends SpringContainerSupport implements TestHomeworkService {

    //local variable
    @Inject private HomeworkDao homeworkDao;

    @Inject private HomeworkUserRefDao homeworkUserRefDao;

    @Inject private HomeworkPracticeDao homeworkPracticeDao;

    @Inject private QuestionLoaderClient questionLoaderClient;

    @Inject private NewContentLoaderClient newContentLoaderClient;

    @ImportService(interfaceClass = ParentRecommendLoader.class)
    private ParentRecommendLoader parentRecommendLoader;
    @Inject private ParentLoaderClient parentLoaderClient;
    @Inject private HomeworkActivityService activityService;
    @Inject private HomeworkUserActivityDao userActivityDao;

    //Logic

    /**
     * 保存活动
     *
     * @param activity
     * @return
     */
    public MapMessage saveActivity(Activity activity){
        int result = activityService.save(activity);
        LoggerUtils.debug("saveActivity", activity, result);
        return MapMessage.successMessage().set("data", result);
    }


    /**
     * 保存用户活动
     *
     * @param userActivity
     * @return
     */
    public MapMessage saveUserActivity(UserActivity userActivity){
        Activity activity = activityService.load(userActivity.getActivityId());
        LoggerUtils.debug("loadActivity", activity);
        if(activity == null){
            return MapMessage.errorMessage("活动不存在").set("status", -9);
        }
        if(activity.getStatus() == -1 ||activity.getStartTime().after(new Date())){
            return MapMessage.errorMessage("活动未开始").set("status", -1);
        }
        String id = HomeworkUtil.generatorID(userActivity.getStudentId(), userActivity.getActivityId());
        UserActivity ouserActivity = userActivityDao.load(id);
        LoggerUtils.debug("loadUserActivity", id,ouserActivity);
//        if(ouserActivity != null){
//            return MapMessage.successMessage("已报名").set("data", ouserActivity);
//        }
        userActivity.setId(id);
        userActivity.setFinished(Boolean.FALSE);
        userActivity.setStatus(1);
        if(ouserActivity == null){
            userActivity.setCreateTime(new Date());
        }
        userActivity.setUpdateTime(new Date());
        LoggerUtils.debug("upsert", userActivity);
        UserActivity result = userActivityDao.upsert(userActivity);
        return MapMessage.successMessage().set("data", result);
    }

    /**
     * 删除用户活动
     *
     * @return
     */
    @Override
    public MapMessage deleteUserActivity(String id){
        userActivityDao.remove(id);
        return MapMessage.successMessage().set("data", id);
    }

    /**
     * 布置作业， 默认数学、北京、110111区
     *
     * @param studentId
     * @param bookId
     * @param unitId
     * @param regionCodeStr
     * @return
     */
    @Override
    public MapMessage assignExam(Long studentId, String bookId, String unitId, String regionCodeStr) {
        Long cityCode = 110100L;//默认北京
        Long regionCode = SafeConverter.toLong(regionCodeStr, 110111);
        String subject = Subject.MATH.name();
        Long userId = studentId;
        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);
        Long formUserId = ObjectUtils.get(()->studentParents.get(0).getParentUser().getId(), -1L);
        //获取单元
        NewBookCatalog newBookCatalog = getUnit(bookId, unitId);
        if(newBookCatalog == null){
            return MapMessage.errorMessage("未找到单元");
        }

        //题包
        List<EliteSchoolPackage> eliteSchoolPackages = parentRecommendLoader.eliteSchoolQuestion(
                subject,
                bookId,
                unitId,
                userId,
                null,
                null,
                cityCode,
                regionCode,
                Lists.newArrayList("BASE","CRUX"));

        if (CollectionUtils.isEmpty(eliteSchoolPackages)) {
            return MapMessage.errorMessage("未获取到题包");
        }
        List<String> homeworkIds = new ArrayList<>();
        for (EliteSchoolPackage filterPackage : eliteSchoolPackages) {
            List<NewQuestion> newQuestions = questionLoaderClient.loadQuestionByDocIds(filterPackage.getDocIds());
            if (CollectionUtils.isEmpty(newQuestions)) {
                return MapMessage.errorMessage();
            }

            Homework homework = new Homework();
            homework.setActionId(filterPackage.getId()); // 题包id
            homework.setSubject(subject);
            homework.setDuration(filterPackage.getDuration());
            homework.setGrade(3);
            homework.setFromUserId(formUserId);
            homework.setId(HomeworkUtil.generatorDayID());
            NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
            homework.setAdditions(MapUtils.m("bookId", bookId,
                    "unitId", unitId,
                    "bookName", newBookProfile != null ? newBookProfile.getName() : "",
                    "unitName", newBookCatalog.getName(),
                    "level", filterPackage.getName()));
            homework.setType(NewHomeworkType.Normal.name());
            homework.setHomeworkTag(HomeworkTag.Normal.name());
            homework.setStartTime(new Date());
            homework.setEndTime(DateUtils.getTodayEnd());
            homework.setQuestionCount(newQuestions.size());
            homework.setScore(100d);
            homework.setSource("parent");
            homeworkDao.insert(homework);

            HomeworkPractice homeworkPractice = new HomeworkPractice();
            List<Practices> practicesList = new ArrayList<>();
            Practices practices = new Practices();
            practices.setType(ObjectiveConfigType.EXAM.name()); // 同步习题
            List<Questions> questionsList = new ArrayList<>();
            Map<String, Double> questionScoreMap = questionLoaderClient.parseExamScoreByQuestions(newQuestions, homework.getScore());
            newQuestions.forEach(newQuestion -> {
                Questions questions = new Questions();
                questions.setQuestionBoxId(filterPackage.getId());
                questions.setSeconds(newQuestion.getSeconds());
                questions.setQuestionId(newQuestion.getId());
                questions.setDocId(newQuestion.getDocId());
                questions.setQuestionVersion(newQuestion.getVersion());
                questions.setSubmitWay(newQuestion.getSubmitWays());
                questions.setScore(questionScoreMap.get(newQuestion.getId()));
                questionsList.add(questions);
            });
            practices.setQuestions(questionsList);
            practicesList.add(practices);
            homeworkPractice.setPractices(practicesList);
            homeworkPractice.setId(homework.getId());
            homeworkPracticeDao.insert(homeworkPractice);

            //保存作业与学生关系
            HomeworkUserRef homeworkUserRef = new HomeworkUserRef();
            homeworkUserRef.setHomeworkId(homework.getId());
            homeworkUserRef.setUserId(userId);
            homeworkUserRef.setId(HomeworkUtil.generatorID(homework.getId(), userId));
            homeworkUserRefDao.insert(homeworkUserRef);
            homeworkIds.add(HomeworkUtil.generatorID(homework.getId(), userId));
            // 发消息
            Map<String, Object> message = MapUtils.m(
                    "messageType", "assign",
                    "studentId", userId,
                    "subject", subject,
                    "startTime", DateUtils.dateToString(homework.getStartTime()),
                    "endTime", DateUtils.dateToString(homework.getEndTime()),
                    "homeworkIds", Collections.singleton(homework.getId())
            );
            MQUtils.send(HOMEWORK_TOPIC, message);
        }

        return MapMessage.successMessage(JsonUtils.toJson(homeworkIds));
    }

    /**
     * 布置作业， 默认数学、北京、110111区
     *
     * @param studentId
     * @param bookId
     * @param unitId
     * @param regionCodeStr
     * @return
     */
    @Override
    public MapMessage assignMentalArithmetic(Long studentId, String bookId, String unitId, String regionCodeStr) {
        Long cityCode = 110100L;//默认北京
        Long regionCode = SafeConverter.toLong(regionCodeStr, 110111);
        String subject = Subject.MATH.name();
        Long userId = studentId;
        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);
        Long formUserId = ObjectUtils.get(()->studentParents.get(0).getParentUser().getId(), -1L);
        //获取单元
        NewBookCatalog newBookCatalog = getUnit(bookId, unitId);
        if(newBookCatalog == null){
            return MapMessage.errorMessage("未找到单元");
        }

        //题包
        List<EliteSchoolPackage> eliteSchoolPackages = parentRecommendLoader.eliteSchoolQuestion(
                subject,
                bookId,
                unitId,
                userId,
                null,
                null,
                cityCode,
                regionCode,
                Lists.newArrayList("BASE","CRUX"));

        if (CollectionUtils.isEmpty(eliteSchoolPackages)) {
            return MapMessage.errorMessage("未获取到题包");
        }
        List<String> homeworkIds = new ArrayList<>();
        for (EliteSchoolPackage filterPackage : eliteSchoolPackages) {
            List<NewQuestion> newQuestions = questionLoaderClient.loadQuestionByDocIds(filterPackage.getDocIds());
            if (CollectionUtils.isEmpty(newQuestions)) {
                return MapMessage.errorMessage();
            }

            Homework homework = new Homework();
            homework.setActionId(filterPackage.getId()); // 题包id
            homework.setSubject(subject);
            homework.setDuration(filterPackage.getDuration());
            homework.setGrade(3);
            homework.setFromUserId(formUserId);
            homework.setId(HomeworkUtil.generatorDayID());
            NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
            homework.setAdditions(MapUtils.m("bookId", bookId,
                    "unitId", unitId,
                    "bookName", newBookProfile != null ? newBookProfile.getName() : "",
                    "unitName", newBookCatalog.getName(),
                    "level", filterPackage.getName()));
            homework.setType(NewHomeworkType.Normal.name());
            homework.setHomeworkTag(HomeworkTag.Normal.name());
            homework.setStartTime(new Date());
            homework.setEndTime(DateUtils.getTodayEnd());
            homework.setQuestionCount(newQuestions.size());
            homework.setScore(100d);
            homework.setSource("parent");
            homeworkDao.insert(homework);

            HomeworkPractice homeworkPractice = new HomeworkPractice();
            List<Practices> practicesList = new ArrayList<>();
            Practices practices = new Practices();
            practices.setType(ObjectiveConfigType.EXAM.name()); // 同步习题
            List<Questions> questionsList = new ArrayList<>();
            Map<String, Double> questionScoreMap = questionLoaderClient.parseExamScoreByQuestions(newQuestions, homework.getScore());
            newQuestions.forEach(newQuestion -> {
                Questions questions = new Questions();
                questions.setQuestionBoxId(filterPackage.getId());
                questions.setSeconds(newQuestion.getSeconds());
                questions.setQuestionId(newQuestion.getId());
                questions.setDocId(newQuestion.getDocId());
                questions.setQuestionVersion(newQuestion.getVersion());
                questions.setSubmitWay(newQuestion.getSubmitWays());
                questions.setScore(questionScoreMap.get(newQuestion.getId()));
                questionsList.add(questions);
            });
            practices.setQuestions(questionsList);
            practicesList.add(practices);
            homeworkPractice.setPractices(practicesList);
            homeworkPractice.setId(homework.getId());
            homeworkPracticeDao.insert(homeworkPractice);

            //保存作业与学生关系
            HomeworkUserRef homeworkUserRef = new HomeworkUserRef();
            homeworkUserRef.setHomeworkId(homework.getId());
            homeworkUserRef.setUserId(userId);
            homeworkUserRef.setId(HomeworkUtil.generatorID(homework.getId(), userId));
            homeworkUserRefDao.insert(homeworkUserRef);
            homeworkIds.add(HomeworkUtil.generatorID(homework.getId(), userId));
            // 发消息
            Map<String, Object> message = MapUtils.m(
                    "messageType", "assign",
                    "studentId", userId,
                    "subject", subject,
                    "startTime", DateUtils.dateToString(homework.getStartTime()),
                    "endTime", DateUtils.dateToString(homework.getEndTime()),
                    "homeworkIds", Collections.singleton(homework.getId())
            );
            MQUtils.send(HOMEWORK_TOPIC, message);
        }

        return MapMessage.successMessage(JsonUtils.toJson(homeworkIds));
    }

    /**
     * 获取单元列表 unitList
     *
     * @param bookId 教材id
     * @param unitId 单元id
     * @return 选择的单元信息
     */
    private NewBookCatalog getUnit(String bookId, String unitId) {
        List<NewBookCatalog> units = newContentLoaderClient.loadChildren(Collections.singleton(bookId), BookCatalogType.UNIT).
                getOrDefault(bookId, Collections.emptyList()).stream().
                sorted(new NewBookCatalog.RankComparator()).collect(Collectors.toList());
        return units.stream().filter(u -> u.getId().equals(unitId)).findFirst().orElse(null);
    }

    /**
     * latext to pdf
     *
     * @return
     */
    @Override
    public MapMessage latex() {
        List<String> points = Arrays.asList("KP_10200100889440,KP_10200100890751,KP_10200101008531".split(","));
        List<String> questionIds = questionLoaderClient.loadRandomQuestionIdsByNewKnowledgePointId(points,
                QuestionConstants.mentalIncludeContentTypeIds, null, 200, true, true, true, true);
        List<String> mockIds = Lists.newArrayList("Q_10212980950567-1", "Q_10212980951306-1");//, "Q_10212980956933","Q_10212980955722", "Q_10212980954930", "Q_10212980953532");
        questionIds.addAll(0, mockIds);
        questionIds = questionIds.subList(0, 30);
        Map<String, NewQuestion> questions = questionLoaderClient.loadQuestions(questionIds);
        for(NewQuestion newQuestion : questions.values()){
            System.out.println(newQuestion.getContent().getSubContents().get(0).getContent());
        }

        return MapMessage.successMessage();
    }

    /**
     * test
     *
     * @param integralInfo
     * @return
     */
    @Override
    public MapMessage integral(IntegralInfo integralInfo, String command) {
        switch (command){
            case "reward":
                return integralServiceClient.reward(integralInfo);
            case "exchange":
                return integralServiceClient.exchange(integralInfo);
            case "cancel":
                return integralServiceClient.cancel(integralInfo);
            case "checkDuplicate":
                return MapMessage.successMessage().set("info", integralServiceClient.checkDuplicate(integralInfo));
            default:
                return MapMessage.errorMessage("info", "command is error");
        }

    }
    @Inject private IntegralServiceClient integralServiceClient;

    /**
     * mq sender
     *
     * @param topic
     * @param message
     * @return
     */
    @Override
    public MapMessage mq(String topic, Map message) {
        MQUtils.send(topic, message);
        return MapMessage.successMessage().set("topic", topic).set("message", message);
    }

    /**
     * 查询完成任务用户
     *
     * @param activityId 活动id
     * @param startTime
     * @param endTime
     * @return
     */
    public List<Long> loadDoneUsers(String activityId, String startTime, String endTime){
        return this.userActivityDao.loadDoneUsers(activityId, DateUtils.stringToDate(startTime), DateUtils.stringToDate(endTime)).stream().map(UserActivity::getParentId).collect(Collectors.toList());
    }

    public static void main(String[] args) throws Exception {
//        String url1 = "https://cdn-live-image.17zuoye.cn/training/acf/20190228/19db470a6b0f4815b71ba0ec653abb1a";
//        url1 = "https://cdn-live-image.17zuoye.cn/training/acf/20190301/50afca82165646f78ddfefda8a83fd25";
//        url1 = "https://cdn-live-image.17zuoye.cn/training/acf/20190301/e4d3b5becaec4dee8096a95f2f3b161a";
//        String url = QRCODE_URL + java.net.URLEncoder.encode(url1, "UTF-8");
//        String r = HttpUtil.post(url, Collections.EMPTY_MAP);
//        System.out.println("url:" + url1);
//        System.out.println("result:" + r);
//        System.out.println(HttpUtil.url((String)JsonUtils.fromJson(r).get("data")));
        Activity activity = new Activity();
        activity.setId(ACTIVITY_ID);
        activity.setCreateTime(new Date());
        activity.setUpdateTime(new Date());
        activity.setDesc("纸质口算运营活动");
        activity.setName("纸质口算运营活动");
        Date startTime = DateUtils.stringToDate("2019-03-04 00:00:00", FORMAT_SQL_DATETIME);
        Date endTime = DateUtils.addDays(startTime, 28);
        activity.setStartTime(startTime);
        activity.setEndTime(endTime);
        activity.setPeriodUnit("WEEK");
        activity.setStatus(0);
        activity.setType("OCR_MENTAL_ARITHMETIC");
        System.out.println(JsonUtils.toJson(activity));
        System.out.println(DateUtils.dateToString(new Date(1554208465422L)));
//        System.out.println(activity.periodCount());
//
//        UserActivity userActivity = new UserActivity();
//        userActivity.setCreateTime(new Date());
//        userActivity.setUpdateTime(new Date());
//        userActivity.setStudentId(171717L);
//        userActivity.setParentId(666L);
//        userActivity.setFinished(false);
//        userActivity.setStatus(1);
//        userActivity.setActivityId(ACTIVITY_ID);
//        System.out.println(JsonUtils.toJson(userActivity));

    }


}