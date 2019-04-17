package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.newhomework.api.BasicReviewHomeworkReportLoader;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.BasicReviewHomeworkCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.BasicReviewHomeworkDetailCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.BasicReviewHomeworkHistory;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.clazz.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.clazz.utils.SentenceAnalysis;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.clazz.utils.StudentPersonalAnalysis;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.clazz.utils.WordAnalysis;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.crm.PackageHomeworkDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.personal.BasicReviewHomeworkPersonalEnglishReport;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.personal.BasicReviewHomeworkPersonalMathReport;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.personal.BasicReviewHomeworkStagePersonalReportBrief;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.personal.SubjectToBasicReviewHomework;
import com.voxlearning.utopia.service.newhomework.consumer.cache.BasicReviewHomeworkShareCacheManager;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = BasicReviewHomeworkReportLoader.class)
@ExposeService(interfaceClass = BasicReviewHomeworkReportLoader.class)
public class BasicReviewHomeworkReportLoaderImpl extends NewHomeworkSpringBean implements BasicReviewHomeworkReportLoader {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    //班级关卡信息 app
    @Override
    public MapMessage fetchStageListToClazz(String packageId) {
        if (StringUtils.isBlank(packageId)) {
            logger.error("fetch Stage List To Clazz failed :packageId {}", packageId);
            return MapMessage.errorMessage("作业ID错误");
        }
        BasicReviewHomeworkPackage basicReviewHomeworkPackage = basicReviewHomeworkPackageDao.load(packageId);
        if (basicReviewHomeworkPackage == null || SafeConverter.toBoolean(basicReviewHomeworkPackage.getDisabled())) {
            logger.error("fetch Stage List To Clazz failed :packageId {}", packageId);
            return MapMessage.errorMessage("作业PACKAGE_ID错误");
        }
        List<BasicReviewStage> stages = basicReviewHomeworkPackage.getStages();
        if (CollectionUtils.isEmpty(stages)) {
            logger.error("fetch Stage List To Clazz failed :packageId {}", packageId);
            return MapMessage.errorMessage("关卡不存在");
        }
        try {
            //key homeworkId ，用户汇聚数据
            Map<String, BasicReviewHomeworkStageClazzReportBrief> briefMap = new LinkedHashMap<>();
            List<BasicReviewHomeworkStageClazzReportBrief> stageBriefList = new LinkedList<>();
            for (BasicReviewStage stage : stages) {
                BasicReviewHomeworkStageClazzReportBrief brief = new BasicReviewHomeworkStageClazzReportBrief(stage.getStageId(), stage.getHomeworkId(), stage.getStageName());
                briefMap.put(stage.getHomeworkId(), brief);
                String detailUrl = UrlUtils.buildUrlQuery("/view/termreview/share",
                        MapUtils.m(
                                "homeworkId", stage.getHomeworkId(),
                                "packageId", packageId));
                brief.setDetailUrl(detailUrl);
                stageBriefList.add(brief);
            }
            Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(basicReviewHomeworkPackage.getClazzGroupId())
                    .stream()
                    .collect(Collectors
                            .toMap(LongIdEntity::getId, Function.identity()));
            Set<Long> userIds = userMap.keySet();
            Map<Long, BasicReviewHomeworkCacheMapper> basicReviewHomeworkCacheMapperMap = basicReviewHomeworkCacheLoader.loadBasicReviewHomeworkCacheMapper(packageId, userIds);
            //学生关卡完成情况数据
            for (BasicReviewHomeworkCacheMapper mapper : basicReviewHomeworkCacheMapperMap.values()) {
                if (mapper.getHomeworkDetail() == null)
                    continue;
                for (BasicReviewHomeworkDetailCacheMapper detailCacheMapper : mapper.getHomeworkDetail().values()) {
                    if (briefMap.containsKey(detailCacheMapper.getHomeworkId())) {
                        BasicReviewHomeworkStageClazzReportBrief brief = briefMap.get(detailCacheMapper.getHomeworkId());
                        //统计关卡完成的人数
                        brief.setFinishUserNum(1 + brief.getFinishUserNum());
                        brief.setBegin(true);
                    }
                }
            }
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("stageBriefList", stageBriefList);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch Stage List To Clazz failed : packageId {}", packageId, e);
            return MapMessage.errorMessage();
        }
    }

    //老师有作业包的班级列表 h5 and app
    @Override
    public MapMessage fetchBasicReviewClazzInfo(Teacher teacher, boolean fromPc) {
        if (teacher == null) {
            return MapMessage.errorMessage();
        }
        try {
            //一个老师一个clazzId 对应着一个group
            List<BasicReviewClazzInfo> clazzInfoList = new LinkedList<>();
            List<GroupTeacherMapper> groupTeacherMappers = groupLoaderClient.loadTeacherGroups(teacher.getId(), false);
            Set<Long> gids = new LinkedHashSet<>();
            Set<Long> clazzIds = new LinkedHashSet<>();
            //班级ID到班级
            Map<Long, GroupTeacherMapper> mapperMap = new LinkedHashMap<>();
            for (GroupTeacherMapper g : groupTeacherMappers) {
                gids.add(g.getId());
                mapperMap.put(g.getClazzId(), g);
                clazzIds.add(g.getClazzId());
            }
            Map<Long, List<BasicReviewHomeworkPackage>> map = basicReviewHomeworkPackageDao.loadBasicReviewHomeworkPackageByClazzGroupIds(gids);
            //班级需要排序，所以根据先拿班级，再根据班级拿group信息
            List<Clazz> clazzList = deprecatedClazzLoaderClient.getRemoteReference()
                    .loadClazzs(clazzIds)
                    .values()
                    .stream()
                    .filter(c -> !c.isTerminalClazz())
                    .sorted(new Clazz.ClazzLevelAndNameComparator()).collect(Collectors.toList());
            for (Clazz c : clazzList) {
                if (!mapperMap.containsKey(c.getId())) {
                    continue;
                }
                if (!mapperMap.containsKey(c.getId()))
                    continue;
                GroupMapper groupMapper = mapperMap.get(c.getId());
                if (!map.containsKey(groupMapper.getId())) {
                    continue;
                }
                //班级没有对应数据
                List<BasicReviewHomeworkPackage> basicReviewHomeworkPackages = map.get(groupMapper.getId());
                if (basicReviewHomeworkPackages.size() <= 0) {
                    continue;
                }
                BasicReviewHomeworkPackage homeworkPackage = basicReviewHomeworkPackages.get(0);
                BasicReviewClazzInfo clazzInfo = new BasicReviewClazzInfo();
                clazzInfo.setClazzName(c.formalizeClazzName());
                clazzInfo.setClazzId(c.getId());
                clazzInfo.setPackageId(homeworkPackage.getId());
                //pc 和 app 公用接口，但是app不要卡片
                //fromPc 表示是否是pc
                if (CollectionUtils.isNotEmpty(homeworkPackage.getStages()) && fromPc) {
                    for (BasicReviewStage s : homeworkPackage.getStages()) {
                        BasicReviewClazzInfo.Stage stage = new BasicReviewClazzInfo.Stage();
                        stage.setHomeworkId(s.getHomeworkId());
                        stage.setStageName(s.getStageName());
                        clazzInfo.getStagesList().add(stage);
                    }
                }
                clazzInfoList.add(clazzInfo);
            }
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("clazzInfoList", clazzInfoList);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch basic review clazz info failed : tid {}", teacher.getId(), e);
            return MapMessage.errorMessage();
        }

    }

    //班级一份作业信息 h5
    @Override
    public MapMessage fetchReportToClazz(String packageId, String homeworkId) {
        if (StringUtils.isAnyBlank(packageId, homeworkId)) {
            logger.error("fetch report to clazz failed : packageId {},hid {}", packageId, homeworkId);
            return MapMessage.errorMessage("参数错误");
        }
        BasicReviewHomeworkPackage basicReviewHomeworkPackage = basicReviewHomeworkPackageDao.load(packageId);
        if (basicReviewHomeworkPackage == null || SafeConverter.toBoolean(basicReviewHomeworkPackage.getDisabled())) {
            return MapMessage.errorMessage("此作业已被老师删除");
        }
        List<BasicReviewStage> stages = basicReviewHomeworkPackage.getStages();
        //作业对应卡片
        BasicReviewStage target = null;
        for (BasicReviewStage s : stages) {
            if (Objects.equals(s.getHomeworkId(), homeworkId)) {
                target = s;
                break;
            }
        }
        if (target == null) {
            logger.error("fetch report to clazz failed : packageId {},hid {}", packageId, homeworkId);
            return MapMessage.errorMessage("包不存在这份作业");
        }
        NewHomework newHomework = newHomeworkLoader.loadNewHomework(homeworkId);
        if (newHomework == null) {
            logger.error("fetch report to clazz failed : packageId {},hid {}", packageId, homeworkId);
            return MapMessage.errorMessage("作业不存在");
        }
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(basicReviewHomeworkPackage.getTeacherId());
        if (teacher == null) {
            logger.error("fetch report to clazz failed : packageId {},hid {}", packageId, homeworkId);
            return MapMessage.errorMessage("老师不存在");
        }
        if (!grayFunctionManagerClient.getTeacherGrayFunctionManager()
                .isWebGrayFunctionAvailable(teacher, "BasicReview", "TeacherWhiteList")) {
            return MapMessage.errorMessage("作业报告暂时无法查看，请明天查看");
        }
        Group group = raikouSDK.getClazzClient().getGroupLoaderClient()
                ._loadGroup(newHomework.getClazzGroupId()).firstOrNull();
        if (group == null) {
            logger.error("fetch report to clazz failed : packageId {},hid {}", packageId, homeworkId);
            return MapMessage.errorMessage("班组不存在");
        }
        Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(group.getClazzId());
        if (clazz == null) {
            logger.error("fetch report to clazz failed : packageId {},hid {}", packageId, homeworkId);
            return MapMessage.errorMessage("班级不存在");
        }
        try {
            if (Objects.equals(newHomework.getSubject(), Subject.ENGLISH)) {
                //英语作业报告
                MapMessage mapMessage = fetchEnglishReport(clazz, target, newHomework, basicReviewHomeworkPackage);
                if (!mapMessage.isSuccess()) {
                    logger.error("fetch report to clazz failed : packageId {},hid {}", packageId, homeworkId);
                }
                mapMessage.add("teacherName", teacher.fetchRealname());
                mapMessage.add("teacherImageUrl", teacher.fetchImageUrl());

                return mapMessage;
            } else if (Objects.equals(newHomework.getSubject(), Subject.MATH)) {
                //数学作业报告
                MapMessage mapMessage = fetchMathReport(clazz, target, newHomework, basicReviewHomeworkPackage);
                if (!mapMessage.isSuccess()) {
                    logger.error("fetch report to clazz failed : packageId {},hid {}", packageId, homeworkId);
                }
                mapMessage.add("teacherName", teacher.fetchRealname());
                mapMessage.add("teacherImageUrl", teacher.fetchImageUrl());
                return mapMessage;
            } else if (Objects.equals(newHomework.getSubject(), Subject.CHINESE)) {
                //语文作业报告
                MapMessage mapMessage = fetchChineseReport(clazz, target, newHomework, basicReviewHomeworkPackage);
                if (!mapMessage.isSuccess()) {
                    logger.error("fetch report to clazz failed : packageId {},hid {}", packageId, homeworkId);
                }
                mapMessage.add("teacherName", teacher.fetchRealname());
                mapMessage.add("teacherImageUrl", teacher.fetchImageUrl());
                return mapMessage;
            }
            return MapMessage.errorMessage("学科错误");
        } catch (Exception e) {
            logger.error("fetch report to clazz failed : packageId {},hid {}", packageId, homeworkId, e);
            return MapMessage.errorMessage();
        }
    }

    //个人关卡信息 h5
    @Override
    public MapMessage fetchStageListToPersonal(String packageId, Long userId) {
        if (StringUtils.isBlank(packageId)) {
            logger.error("fetch stage list to personal failed : packageId {} , userId {}", packageId, userId);
            return MapMessage.errorMessage("PACKAGE_ID 错误");
        }
        if (SafeConverter.toLong(userId) <= 0) {
            logger.error("fetch stage list to personal failed : packageId {} , userId {}", packageId, userId);
            return MapMessage.errorMessage("USER_ID 错误");
        }
        BasicReviewHomeworkPackage basicReviewHomeworkPackage = basicReviewHomeworkPackageDao.load(packageId);
        if (basicReviewHomeworkPackage == null || SafeConverter.toBoolean(basicReviewHomeworkPackage.getDisabled())) {
            logger.error("fetch stage list to personal failed : packageId {} , userId {}", packageId, userId);
            return MapMessage.errorMessage("包不存在或者包删除");
        }
        List<BasicReviewStage> stages = basicReviewHomeworkPackage.getStages();
        if (CollectionUtils.isEmpty(stages)) {
            logger.error("fetch stage list to personal failed : packageId {} , userId {}", packageId, userId);
            return MapMessage.errorMessage("包不存在关卡");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        if (studentDetail == null) {
            logger.error("fetch stage list to personal failed : packageId {} , userId {}", packageId, userId);
            return MapMessage.errorMessage("学生ID不存在");
        }
        if (!grayFunctionManagerClient.getStudentGrayFunctionManager()
                .isWebGrayFunctionAvailable(studentDetail, "BasicReview", "StudentWhiteList")) {
            return MapMessage.errorMessage("作业报告暂时无法查看，请明天查看");
        }

        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(basicReviewHomeworkPackage.getClazzGroupId())
                .stream()
                .collect(Collectors
                        .toMap(LongIdEntity::getId, Function.identity()));
        if (!userMap.containsKey(userId)) {
            logger.error("fetch stage list to personal failed : packageId {} , userId {}", packageId, userId);
            return MapMessage.errorMessage("报告内容与当前选择的孩子帐号不符，请检查后重试~");
        }
        try {
            Map<String, BasicReviewHomeworkStagePersonalReportBrief> briefMap = new LinkedHashMap<>();
            List<BasicReviewHomeworkStagePersonalReportBrief> briefs = new LinkedList<>();
            List<String> basicReviewHomeworkReportIds = new LinkedList<>();
            //newHomeworkResultIds用于查询是否开始
            List<String> newHomeworkResultIds = new LinkedList<>();
            String day = DayRange.newInstance(basicReviewHomeworkPackage.getCreateAt().getTime()).toString();
            Map<Long, BasicReviewHomeworkCacheMapper> basicReviewHomeworkCacheMapperMap = basicReviewHomeworkCacheLoader.loadBasicReviewHomeworkCacheMapper(packageId, userMap.keySet());
            //班级完成最多的人的完成关卡数
            int maxStageNum = 0;
            for (BasicReviewHomeworkCacheMapper detail : basicReviewHomeworkCacheMapperMap.values()) {
                if (detail.getHomeworkDetail().size() > maxStageNum) {
                    maxStageNum = detail.getHomeworkDetail().size();
                }
            }
            BasicReviewHomeworkCacheMapper mapper = basicReviewHomeworkCacheMapperMap.get(userId);
            LinkedHashMap<String, BasicReviewHomeworkDetailCacheMapper> homeworkDetail = mapper.getHomeworkDetail();
            for (BasicReviewStage stage : stages) {
                BasicReviewHomeworkStagePersonalReportBrief brief = new BasicReviewHomeworkStagePersonalReportBrief();
                brief.setStageId(stage.getStageId());
                brief.setStageName(stage.getStageName());
                brief.setHomeworkId(stage.getHomeworkId());
                briefMap.put(stage.getHomeworkId(), brief);
                briefs.add(brief);
                String s = new NewHomeworkResult.ID(day, basicReviewHomeworkPackage.getSubject(),
                        stage.getHomeworkId(), userId.toString()).toString();
                if (homeworkDetail.containsKey(stage.getHomeworkId())) {
                    brief.setBegin(true);
                    brief.setFinished(true);
                    //完成的查询报告
                    basicReviewHomeworkReportIds.add(s);
                } else {
                    newHomeworkResultIds.add(s);
                }
            }
            Map<String, BasicReviewHomeworkReport> basicReviewHomeworkReportMap = basicReviewHomeworkReportDao.loads(basicReviewHomeworkReportIds);
            Map<String, Set<String>> homeworkIdToQids = new LinkedHashMap<>();
            Set<String> allQids = new LinkedHashSet<>();
            //关卡完成的，关卡的完成数
            for (BasicReviewHomeworkReport b : basicReviewHomeworkReportMap.values()) {
                if (!briefMap.containsKey(b.getHomeworkId()))
                    continue;
                BasicReviewHomeworkStagePersonalReportBrief brief = briefMap.get(b.getHomeworkId());
                if (Objects.equals(basicReviewHomeworkPackage.getSubject(), Subject.MATH)) {
                    //数学
                    if (b.getPractices() == null)
                        continue;
                    if (!b.getPractices().containsKey(ObjectiveConfigType.MENTAL_ARITHMETIC))
                        continue;
                    BasicReviewHomeworkReportDetail detail = b.getPractices().get(ObjectiveConfigType.MENTAL_ARITHMETIC);
                    if (detail.getAnswers() == null)
                        continue;
                    //错题数
                    long wrongNum = detail.getAnswers()
                            .values()
                            .stream()
                            .filter(o -> !SafeConverter.toBoolean(o.getGrasp()))
                            .count();
                    brief.setWrongNum((int) wrongNum);
                } else if (Objects.equals(basicReviewHomeworkPackage.getSubject(), Subject.ENGLISH)) {
                    //英语
                    if (b.getPractices() == null)
                        continue;
                    if (!b.getPractices().containsKey(ObjectiveConfigType.BASIC_APP))
                        continue;
                    BasicReviewHomeworkReportDetail detail = b.getPractices().get(ObjectiveConfigType.BASIC_APP);
                    if (detail.getAppAnswers() == null)
                        continue;
                    Set<String> qids = detail.getAppAnswers()
                            .values()
                            .stream()
                            .filter(o -> o.getAnswers() != null)
                            .map(o -> o.getAnswers().values())
                            .flatMap(Collection::stream)
                            .filter(o -> !SafeConverter.toBoolean(o.getGrasp()))
                            .filter(o -> StringUtils.isNotBlank(o.getQuestionId()))
                            .map(BasicReviewHomeworkAnswer::getQuestionId)
                            .collect(Collectors.toSet());
                    homeworkIdToQids.put(brief.getHomeworkId(), qids);
                    allQids.addAll(qids);
                } else if (Objects.equals(basicReviewHomeworkPackage.getSubject(), Subject.CHINESE)) {
                    // 语文
                    if (!briefMap.containsKey(b.getHomeworkId()))
                        continue;
                    if (Objects.equals(basicReviewHomeworkPackage.getSubject(), Subject.CHINESE)) {
                        //语文
                        if (b.getPractices() == null)
                            continue;
                        if (!b.getPractices().containsKey(ObjectiveConfigType.READ_RECITE_WITH_SCORE))
                            continue;
                        BasicReviewHomeworkReportDetail detail = b.getPractices().get(ObjectiveConfigType.READ_RECITE_WITH_SCORE);
                        if (MapUtils.isEmpty(detail.getAppAnswers()))
                            continue;
                        //总篇数
                        long textNum = detail.getAppAnswers().size();
                        //达标篇数
                        long graspTextNum = 0;
                        for (BasicReviewHomeworkAppAnswer basicReviewHomeworkAppAnswer : detail.getAppAnswers().values()) {
                            LinkedHashMap<String, BasicReviewHomeworkAnswer> answers = basicReviewHomeworkAppAnswer.getAnswers();
                            if (MapUtils.isNotEmpty(answers)) {
                                long graspNum = answers.values()
                                        .stream()
                                        .filter(o -> SafeConverter.toBoolean(o.getGrasp()))
                                        .count();
                                double value = new BigDecimal(SafeConverter.toInt(graspNum) * 100).divide(new BigDecimal(answers.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                if (value >= NewHomeworkConstants.READ_RECITE_STANDARD) {
                                    graspTextNum++;
                                }
                            }
                        }
                        brief.setWrongNum((int) graspTextNum);
                        brief.setStageQuestionNum((int) textNum);
                    }
                }
            }
            //英语单词和句子需要去重
            if (CollectionUtils.isNotEmpty(allQids)) {
                Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(allQids);
                Set<Long> sentenceIds = newQuestionMap.values()
                        .stream()
                        .filter(o -> CollectionUtils.isNotEmpty(o.getSentenceIds()))
                        .map(o -> o.getSentenceIds().get(0))
                        .collect(Collectors.toSet());
                Map<Long, Sentence> sentenceMap = englishContentLoaderClient.loadEnglishSentences(sentenceIds);
                for (BasicReviewHomeworkStagePersonalReportBrief brief : briefs) {
                    if (homeworkIdToQids.containsKey(brief.getHomeworkId())) {
                        Set<String> qids = homeworkIdToQids.get(brief.getHomeworkId());
                        int size = qids.stream()
                                .filter(newQuestionMap::containsKey)
                                .map(newQuestionMap::get)
                                .filter(o -> CollectionUtils.isNotEmpty(o.getSentenceIds()))
                                .map(o -> o.getSentenceIds().get(0)).
                                        filter(sentenceMap::containsKey)
                                .map(sentenceMap::get)
                                .map(Sentence::getEnText)
                                .collect(Collectors.toSet())
                                .size();
                        brief.setWrongNum(size);
                    }
                }
            }

            // 未完成的，但是判断是否开始关卡
            Map<String, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loads(newHomeworkResultIds, false);
            for (NewHomeworkResult n : newHomeworkResultMap.values()) {
                if (briefMap.containsKey(n.getHomeworkId())) {
                    BasicReviewHomeworkStagePersonalReportBrief brief = briefMap.get(n.getHomeworkId());
                    brief.setBegin(true);
                }
            }
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("stages", briefs);
            mapMessage.set("finishStageNum", homeworkDetail.size());
            mapMessage.add("maxStageNum", maxStageNum);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch stage list to personal failed : packageId {} , userId {}", packageId, userId, e);
            return MapMessage.errorMessage();

        }
    }

    //个人一份作业的报告 h5
    @Override
    public MapMessage fetchReportToPersonal(String packageId, String homeworkId, Long userId, User parent) {
        if (StringUtils.isAnyBlank(packageId, homeworkId) || SafeConverter.toLong(userId) <= 0L) {
            logger.error("fetch report to personal failed : packageId {} ,homeworkId {},user {}", packageId, homeworkId, userId);
            return MapMessage.errorMessage("参数错误");
        }
        BasicReviewHomeworkPackage basicReviewHomeworkPackage = basicReviewHomeworkPackageDao.load(packageId);
        if (basicReviewHomeworkPackage == null || SafeConverter.toBoolean(basicReviewHomeworkPackage.getDisabled())) {
            logger.error("fetch report to personal failed : packageId {} ,homeworkId {},user {}", packageId, homeworkId, userId);
            return MapMessage.errorMessage("包已经删除");
        }
        if (CollectionUtils.isEmpty(basicReviewHomeworkPackage.getStages())) {
            logger.error("fetch report to personal failed : packageId {} ,homeworkId {},user {}", packageId, homeworkId, userId);
            return MapMessage.errorMessage("关卡不存在");
        }
        BasicReviewStage target = null;
        for (BasicReviewStage stage : basicReviewHomeworkPackage.getStages()) {
            if (Objects.equals(stage.getHomeworkId(), homeworkId)) {
                target = stage;
                break;
            }
        }
        if (target == null) {
            logger.error("fetch report to personal failed : packageId {} ,homeworkId {},user {}", packageId, homeworkId, userId);
            return MapMessage.errorMessage("关卡不存在");
        }
        NewHomework newHomework = newHomeworkLoader.loadNewHomework(homeworkId);
        if (newHomework == null) {
            logger.error("fetch report to personal failed : packageId {} ,homeworkId {},user {}", packageId, homeworkId, userId);
            return MapMessage.errorMessage("作业失效");
        }
        List<User> users = studentLoaderClient.loadGroupStudents(basicReviewHomeworkPackage.getClazzGroupId());
        boolean flag = false;//该学生是否在该班级里面
        Map<Long, User> userMap = new LinkedHashMap<>();
        for (User u : users) {
            if (Objects.equals(u.getId(), userId)) {
                flag = true;
            }
            userMap.put(u.getId(), u);
        }
        if (!flag) {
            List<User> users1 = studentLoaderClient.loadParentStudents(parent.getId());
            List<Long> childrenIds = new LinkedList<>();
            for (User u : users1) {
                if (userMap.containsKey(u.getId())) {
                    childrenIds.add(u.getId());
                }
            }
            //多个孩子在班级里面的时候，随机取一个
            if (childrenIds.size() != 0) {
                flag = true;
                int v = (int) (Math.random() * childrenIds.size());
                if (v == childrenIds.size()) {
                    v--;
                }
                userId = childrenIds.get(v);
            }
        }
        //实在找不到
        if (!flag) {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", userId,
                    "mod1", homeworkId,
                    "mod2", "packageId",
                    "op", "basicReviewReport"
            ));
            return MapMessage.errorMessage("报告内容与当前选择的孩子帐号不符，请检查后重试~");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        if (!grayFunctionManagerClient.getStudentGrayFunctionManager()
                .isWebGrayFunctionAvailable(studentDetail, "BasicReview", "StudentWhiteList")) {
            return MapMessage.errorMessage("作业报告暂时无法查看，请明天查看");
        }

        Teacher teacher = teacherLoaderClient.loadTeacher(basicReviewHomeworkPackage.getTeacherId());
        if (teacher == null) {
            logger.error("fetch report to clazz failed : packageId {},hid {}", packageId, homeworkId);
            return MapMessage.errorMessage("老师不存在");
        }
        try {
            if (Objects.equals(newHomework.getSubject(), Subject.ENGLISH)) {
                //英语报告
                MapMessage mapMessage = fetchPersonalEnglishReport(target, userMap, studentDetail, newHomework);
                mapMessage.add("teacherName", teacher.fetchRealname());
                mapMessage.add("teacherImageUrl", teacher.fetchImageUrl());
                return mapMessage;
            } else if (Objects.equals(newHomework.getSubject(), Subject.MATH)) {
                //数学报告
                MapMessage mapMessage = fetchPersonalMathReport(target, userMap, studentDetail, newHomework);
                mapMessage.add("teacherName", teacher.fetchRealname());
                mapMessage.add("teacherImageUrl", teacher.fetchImageUrl());
                return mapMessage;
            } else if (Objects.equals(newHomework.getSubject(), Subject.CHINESE)) {
                //语文报告
                MapMessage mapMessage = fetchPersonalChineseReport(target, userMap, studentDetail, newHomework);
                mapMessage.add("teacherName", teacher.fetchRealname());
                mapMessage.add("teacherImageUrl", teacher.fetchImageUrl());
                return mapMessage;
            }
            return MapMessage.errorMessage("学科错误");
        } catch (Exception e) {
            logger.error("fetch report to personal failed : packageId {} ,homeworkId {},user {}", packageId, homeworkId, userId, e);
            return MapMessage.errorMessage();
        }
    }

    //个人有基础的学科 h5
    @Override
    public MapMessage fetchSubjectsToPersonal(Long userId) {
        if (SafeConverter.toLong(userId) <= 0) {
            logger.error("fetch subjects to personal failed : userId {}", userId);
            return MapMessage.errorMessage("学生ID有误");
        }
        Student student = studentLoaderClient.loadStudent(userId);
        if (student == null) {
            logger.error("fetch subjects to personal failed : userId {}", userId);
            return MapMessage.errorMessage("学生ID有误");
        }
        List<Group> groupMappers = raikouSystem.loadStudentGroups(userId);
        if (CollectionUtils.isEmpty(groupMappers)) {
            logger.error("fetch subjects to personal failed : userId {}", userId);
            return MapMessage.errorMessage("班组不存在");
        }
        Map<Long, Group> groupMapperMap = groupMappers.stream()
                .collect(Collectors.toMap(Group::getId, Function.identity()));
        try {
            Map<Long, List<BasicReviewHomeworkPackage>> map = basicReviewHomeworkPackageDao.loadBasicReviewHomeworkPackageByClazzGroupIds(groupMapperMap.keySet());
            Map<Subject, SubjectToBasicReviewHomework> subjectMap = new LinkedHashMap<>();
            for (Map.Entry<Long, List<BasicReviewHomeworkPackage>> entry : map.entrySet()) {
                Group groupMapper = groupMapperMap.get(entry.getKey());
                if (entry.getValue().size() > 0) {
                    SubjectToBasicReviewHomework s = new SubjectToBasicReviewHomework();
                    Subject subject = groupMapper.getSubject();
                    if (subject == null)
                        continue;
                    s.setSubject(subject);
                    s.setSubjectName(subject.getValue());
                    s.setPackageId(entry.getValue().get(0).getId());
                    subjectMap.put(subject, s);
                }
            }
            //排序：英语，数学
            List<SubjectToBasicReviewHomework> subjectList = new LinkedList<>();
            for (Subject subject : Arrays.asList(Subject.ENGLISH, Subject.MATH, Subject.CHINESE)) {
                if (subjectMap.containsKey(subject)) {
                    subjectList.add(subjectMap.get(subject));
                }
            }
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.add("subjectList", subjectList);
            return mapMessage;
        } catch (Exception e) {
            logger.error("fetch subjects to personal failed : userId {}", userId, e);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage pushBasicReviewReportMsgToJzt(Teacher teacher, String packageId, String homeworkId) {
        try {

            BasicReviewHomeworkShareCacheManager basicReviewHomeworkShareCacheManager = newHomeworkCacheService.getBasicReviewHomeworkShareCacheManager();
            String key = basicReviewHomeworkShareCacheManager.getCacheKey(packageId, homeworkId);
            Integer shareValue = basicReviewHomeworkShareCacheManager.load(key);
            if (shareValue != null) {
                return MapMessage.errorMessage("已经分享");
            } else {
                basicReviewHomeworkShareCacheManager.set(key, 1);
            }

            //************** begin 给班群发送消息 **************//
            NewHomework newHomework = newHomeworkLoader.load(homeworkId);
            if (newHomework == null) {
                logger.error("push BasicReview Report Msg To Jzt : tid {}  ,hid {},packageId {}", teacher.getId(), homeworkId, packageId);
                return MapMessage.errorMessage();
            }
            String iMContent = "家长好，转发这次期末复习-基础摸底班级整体情况，请查收！";

            String urlLink = UrlUtils.buildUrlQuery("/view/termreview/share",
                    MapUtils.m(
                            "homeworkId", homeworkId,
                            "packageId", packageId));
            if (teacher == null) {
                return MapMessage.errorMessage();
            }

            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacher.getId());
            Long teacherId = mainTeacherId == null ? teacher.getId() : mainTeacherId;
            //这里才是取所有的学科
            Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
            List<Subject> subjectList = teacherLoaderClient.loadTeachers(relTeacherIds).values().stream().map(Teacher::getSubject).collect(Collectors.toList());
            List<String> subjectStrList = subjectList.stream().sorted(Comparator.comparingInt(Subject::getId)).map(Subject::getValue).collect(Collectors.toList());
            String subjectsStr = "（" + StringUtils.join(subjectStrList.toArray(), "，") + "）";
            String em_push_title = teacher.fetchRealnameIfBlankId() + subjectsStr + "：" + iMContent;

            //新的push
            Map<String, Object> extras = new HashMap<>();
            extras.put("studentId", "");
            extras.put("url", urlLink);
            extras.put("s", ParentAppPushType.REPORT.name());
            appMessageServiceClient.sendAppJpushMessageByTags(
                    em_push_title,
                    com.voxlearning.utopia.service.push.api.constant.AppMessageSource.PARENT,
                    Collections.singletonList(JpushUserTag.CLAZZ_GROUP_REFACTOR.generateTag(SafeConverter.toString(newHomework.getClazzGroupId()))),
                    null,
                    extras);

            //************** end 给班群发送消息 **************//
            return MapMessage.successMessage();
        } catch (Exception e) {
            logger.error("push BasicReview Report Msg To Jzt : tid {}  ,hid {},packageId {}", teacher.getId(), homeworkId, packageId);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public Map<Subject, PackageHomeworkDetail> crmPackageHomeworkDetail(Long userId) {
        if (SafeConverter.toLong(userId) <= 0) {
            logger.error("crm Package  Homework Detail failed : userId {}", userId);
            return Collections.emptyMap();
        }
        Student student = studentLoaderClient.loadStudent(userId);
        if (student == null) {
            logger.error("crm Package  Homework Detail failed : userId {}", userId);
            return Collections.emptyMap();
        }
        List<Group> groupMappers = raikouSystem.loadStudentGroups(userId);
        if (CollectionUtils.isEmpty(groupMappers)) {
            logger.error("crm Package  Homework Detail failed : userId {}", userId);
            return Collections.emptyMap();
        }
        Map<Long, Group> groupMapperMap = groupMappers.stream()
                .collect(Collectors.toMap(Group::getId, Function.identity()));
        try {
            Map<Long, List<BasicReviewHomeworkPackage>> map = basicReviewHomeworkPackageDao.loadBasicReviewHomeworkPackageByClazzGroupIds(groupMapperMap.keySet());
            Map<Subject, PackageHomeworkDetail> subjectMap = new LinkedHashMap<>();
            for (Map.Entry<Long, List<BasicReviewHomeworkPackage>> entry : map.entrySet()) {
                Group groupMapper = groupMapperMap.get(entry.getKey());
                if (entry.getValue().size() > 0) {
                    PackageHomeworkDetail s = new PackageHomeworkDetail();
                    Subject subject = groupMapper.getSubject();
                    if (subject == null)
                        continue;
                    BasicReviewHomeworkPackage basicReviewHomeworkPackage = entry.getValue().get(0);
                    String packageId = basicReviewHomeworkPackage.getId();
                    BasicReviewHomeworkCacheMapper cacheMapper = basicReviewHomeworkCacheLoader.loadBasicReviewHomeworkCacheMapper(packageId, userId);
                    LinkedHashMap<String, BasicReviewHomeworkDetailCacheMapper> homeworkDetail = cacheMapper.getHomeworkDetail();
                    for (BasicReviewStage stage : basicReviewHomeworkPackage.getStages()) {
                        PackageHomeworkDetail.StageDetail stageDetail = new PackageHomeworkDetail.StageDetail();
                        stageDetail.setHomeworkId(stage.getHomeworkId());
                        stageDetail.setStageName(stage.getStageName());
                        stageDetail.setUserId(userId);
                        if (homeworkDetail.containsKey(stage.getHomeworkId())) {
                            stageDetail.setFinished(true);
                        }
                        s.getStages().add(stageDetail);
                    }
                    s.setSubject(subject);
                    s.setSubjectName(subject.getValue());
                    s.setPackageId(packageId);
                    subjectMap.put(subject, s);
                }
            }
            return subjectMap;
        } catch (Exception e) {
            logger.error("crm Package  Homework Detail failed : userId {}", userId, e);
            return Collections.emptyMap();
        }


    }

    private MapMessage fetchPersonalChineseReport(BasicReviewStage target, Map<Long, User> userMap, StudentDetail studentDetail, NewHomework newHomework) {
        BasicReviewHomeworkPersonalMathReport report = new BasicReviewHomeworkPersonalMathReport();
        report.setStageName(target.getStageName());
        report.setSubject(Subject.CHINESE);
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        Set<String> basicReviewHomeworkReportIds = new LinkedHashSet<>();
        for (Long userId : userMap.keySet()) {
            basicReviewHomeworkReportIds.add(new NewHomeworkResult.ID(day, newHomework.getSubject(), newHomework.getId(), userId.toString()).toString());
        }
        NewHomeworkPracticeContent targetContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.READ_RECITE_WITH_SCORE);
        if (targetContent == null) {
            return MapMessage.errorMessage("作业错误");
        }
        Map<String, BasicReviewHomeworkReport> basicReviewHomeworkReportMap = basicReviewHomeworkReportDao.loads(basicReviewHomeworkReportIds);
        String s = new NewHomeworkResult.ID(day, newHomework.getSubject(), newHomework.getId(), studentDetail.getId().toString()).toString();
        if (!basicReviewHomeworkReportMap.containsKey(s)) {
            //防止后处理数据丢失
            NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentDetail.getId(), true);
            if (newHomeworkResult != null && newHomeworkResult.isFinished()) {
                BasicReviewHomeworkReport basicReviewHomeworkReport = fh_createBasicReviewHomeworkReport.processBasicReviewHomeworkReport(newHomeworkResult, newHomework);
                basicReviewHomeworkReportMap.put(s, basicReviewHomeworkReport);
            } else {
                return MapMessage.errorMessage("学生未完成关卡");
            }
        }
        BasicReviewHomeworkPersonalMathReport.CalculationPart calculationPart = new BasicReviewHomeworkPersonalMathReport.CalculationPart();
        //个人达标篇数
        int graspTextNum = 0;
        BasicReviewHomeworkReport personalReport = basicReviewHomeworkReportMap.get(s);
        if (personalReport.getPractices() != null && personalReport.getPractices().containsKey(ObjectiveConfigType.READ_RECITE_WITH_SCORE)) {
            BasicReviewHomeworkReportDetail detail = personalReport.getPractices().get(ObjectiveConfigType.READ_RECITE_WITH_SCORE);
            if (MapUtils.isNotEmpty(detail.getAppAnswers())) {
                for (BasicReviewHomeworkAppAnswer basicReviewHomeworkAppAnswer : detail.getAppAnswers().values()) {
                    LinkedHashMap<String, BasicReviewHomeworkAnswer> answers = basicReviewHomeworkAppAnswer.getAnswers();
                    if (MapUtils.isNotEmpty(answers)) {
                        long graspNum = answers.values()
                                .stream()
                                .filter(o -> SafeConverter.toBoolean(o.getGrasp()))
                                .count();
                        double value = new BigDecimal(SafeConverter.toInt(graspNum) * 100).divide(new BigDecimal(answers.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        if (value >= NewHomeworkConstants.READ_RECITE_STANDARD) {
                            graspTextNum++;
                        }
                    }
                }


            }
        }
        //最好的学生的达标篇数
        int bestGraspTextNum = graspTextNum;
        for (BasicReviewHomeworkReport basicReviewHomeworkReport : basicReviewHomeworkReportMap.values()) {
            if (basicReviewHomeworkReport.getPractices() != null && basicReviewHomeworkReport.getPractices().containsKey(ObjectiveConfigType.READ_RECITE_WITH_SCORE)) {
                BasicReviewHomeworkReportDetail detail = basicReviewHomeworkReport.getPractices().get(ObjectiveConfigType.READ_RECITE_WITH_SCORE);
                if (MapUtils.isNotEmpty(detail.getAppAnswers())) {
                    long count = 0;
                    for (BasicReviewHomeworkAppAnswer basicReviewHomeworkAppAnswer : detail.getAppAnswers().values()) {
                        LinkedHashMap<String, BasicReviewHomeworkAnswer> answers = basicReviewHomeworkAppAnswer.getAnswers();
                        if (MapUtils.isNotEmpty(answers)) {
                            long graspNum = answers.values()
                                    .stream()
                                    .filter(o -> SafeConverter.toBoolean(o.getGrasp()))
                                    .count();
                            double value = new BigDecimal(SafeConverter.toInt(graspNum) * 100).divide(new BigDecimal(answers.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            if (value >= NewHomeworkConstants.READ_RECITE_STANDARD) {
                                graspTextNum++;
                            }
                        }
                    }

                    if (count < bestGraspTextNum) {
                        bestGraspTextNum = (int) count;
                    }
                }
            }
        }
        calculationPart.setTotalQuestionNum(targetContent.processNewHomeworkQuestion(false).size());
        calculationPart.setBestWrongNum(bestGraspTextNum);
        calculationPart.setPersonalWrongNum(graspTextNum);
        report.setCalculationPart(calculationPart);
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("report", report);
        return mapMessage;
    }

    private MapMessage fetchPersonalMathReport(BasicReviewStage target, Map<Long, User> userMap, StudentDetail studentDetail, NewHomework newHomework) {
        BasicReviewHomeworkPersonalMathReport report = new BasicReviewHomeworkPersonalMathReport();
        //************test**********//
        report.setStageName(target.getStageName());
        report.setSubject(Subject.MATH);
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        Set<String> basicReviewHomeworkReportIds = new LinkedHashSet<>();
        for (Long userId : userMap.keySet()) {
            basicReviewHomeworkReportIds.add(new NewHomeworkResult.ID(day, newHomework.getSubject(), newHomework.getId(), userId.toString()).toString());
        }
        NewHomeworkPracticeContent targetContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.MENTAL_ARITHMETIC);
        if (targetContent == null) {
            return MapMessage.errorMessage("作业错误");
        }
        Map<String, BasicReviewHomeworkReport> basicReviewHomeworkReportMap = basicReviewHomeworkReportDao.loads(basicReviewHomeworkReportIds);
        String s = new NewHomeworkResult.ID(day, newHomework.getSubject(), newHomework.getId(), studentDetail.getId().toString()).toString();
        if (!basicReviewHomeworkReportMap.containsKey(s)) {
            //防止后处理数据丢失
            NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentDetail.getId(), true);
            if (newHomeworkResult != null && newHomeworkResult.isFinished()) {
                BasicReviewHomeworkReport basicReviewHomeworkReport = fh_createBasicReviewHomeworkReport.processBasicReviewHomeworkReport(newHomeworkResult, newHomework);
                basicReviewHomeworkReportMap.put(s, basicReviewHomeworkReport);
            } else {
                return MapMessage.errorMessage("学生未完成关卡");
            }
        }
        BasicReviewHomeworkPersonalMathReport.CalculationPart calculationPart = new BasicReviewHomeworkPersonalMathReport.CalculationPart();
        //个人错题数
        int personalWrongNum = 0;
        BasicReviewHomeworkReport personalReport = basicReviewHomeworkReportMap.get(s);
        if (personalReport.getPractices() != null && personalReport.getPractices().containsKey(ObjectiveConfigType.MENTAL_ARITHMETIC)) {
            BasicReviewHomeworkReportDetail detail = personalReport.getPractices().get(ObjectiveConfigType.MENTAL_ARITHMETIC);
            if (detail.getAnswers() != null) {
                personalWrongNum = (int) detail.getAnswers().values().stream().filter(o -> !SafeConverter.toBoolean(o.getGrasp())).count();
            }
        }
        //最好的学生的错题数
        int bestWrongNum = personalWrongNum;
        for (BasicReviewHomeworkReport basicReviewHomeworkReport : basicReviewHomeworkReportMap.values()) {
            if (basicReviewHomeworkReport.getPractices() != null && basicReviewHomeworkReport.getPractices().containsKey(ObjectiveConfigType.MENTAL_ARITHMETIC)) {
                BasicReviewHomeworkReportDetail detail = basicReviewHomeworkReport.getPractices().get(ObjectiveConfigType.MENTAL_ARITHMETIC);
                if (detail.getAnswers() != null) {
                    long count = detail.getAnswers().values().stream().filter(o -> !SafeConverter.toBoolean(o.getGrasp())).count();
                    if (count < bestWrongNum) {
                        bestWrongNum = (int) count;
                    }
                }
            }
        }
        calculationPart.setTotalQuestionNum(targetContent.processNewHomeworkQuestion(false).size());
        calculationPart.setBestWrongNum(bestWrongNum);
        calculationPart.setPersonalWrongNum(personalWrongNum);
        report.setCalculationPart(calculationPart);
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("report", report);
        return mapMessage;
    }

    private MapMessage fetchPersonalEnglishReport(BasicReviewStage target, Map<Long, User> userMap, StudentDetail studentDetail, NewHomework newHomework) {
        BasicReviewHomeworkPersonalEnglishReport report = new BasicReviewHomeworkPersonalEnglishReport();
        report.setSubject(Subject.ENGLISH);
        report.setStageName(target.getStageName());
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        Set<String> basicReviewHomeworkReportIds = new LinkedHashSet<>();
        for (Long userId : userMap.keySet()) {
            basicReviewHomeworkReportIds.add(new NewHomeworkResult.ID(day, newHomework.getSubject(), newHomework.getId(), userId.toString()).toString());
        }
        NewHomeworkPracticeContent targetContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.BASIC_APP);
        Map<String, BasicReviewHomeworkReport> basicReviewHomeworkReportMap = basicReviewHomeworkReportDao.loads(basicReviewHomeworkReportIds);
        String personalBasicId = new NewHomeworkResult.ID(day, newHomework.getSubject(), newHomework.getId(), studentDetail.getId().toString()).toString();
        if (!basicReviewHomeworkReportMap.containsKey(personalBasicId)) {
            NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentDetail.getId(), true);
            if (newHomeworkResult != null && newHomeworkResult.isFinished()) {
                BasicReviewHomeworkReport basicReviewHomeworkReport = fh_createBasicReviewHomeworkReport.processBasicReviewHomeworkReport(newHomeworkResult, newHomework);
                basicReviewHomeworkReportMap.put(personalBasicId, basicReviewHomeworkReport);
            } else {
                return MapMessage.errorMessage("学生未完成关卡");
            }
        }

        List<String> allQuestionIds = targetContent.processNewHomeworkQuestion(false)
                .stream()
                .map(NewHomeworkQuestion::getQuestionId)
                .collect(Collectors.toList());
        Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(allQuestionIds);
        Map<String, Long> qidToSentenceMap = newQuestionMap
                .values()
                .stream()
                .filter(o -> CollectionUtils.isNotEmpty(o.getSentenceIds()))
                .collect(Collectors.toMap(NewQuestion::getId, o -> o.getSentenceIds().get(0)));
        Map<Long, Sentence> sentenceMap = englishContentLoaderClient.loadEnglishSentences(qidToSentenceMap.values());
        //全部单词：去重计算单词
        Set<String> words = new LinkedHashSet<>();
        //全部的句子：去重计算句子
        Set<String> sentences = new LinkedHashSet<>();
        Map<String, PracticeType> practiceTypeMap = new LinkedHashMap<>();
        for (NewHomeworkApp app : targetContent.getApps()) {
            PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(app.getPracticeId());
            if (practiceType == null)
                continue;
            practiceTypeMap.put(app.getCategoryId() + "-" + app.getLessonId(), practiceType);
            if (app.getQuestions() != null) {
                for (NewHomeworkQuestion question : app.getQuestions()) {
                    if (!newQuestionMap.containsKey(question.getQuestionId()))
                        continue;
                    if (!qidToSentenceMap.containsKey(question.getQuestionId()))
                        continue;
                    Long s = qidToSentenceMap.get(question.getQuestionId());
                    if (!sentenceMap.containsKey(s))
                        continue;
                    Sentence sentence = sentenceMap.get(s);
                    if (Objects.equals(practiceType.getCategoryName(), "单词拼写") || Objects.equals(practiceType.getCategoryName(), "看图识词")) {
                        words.add(SafeConverter.toString(sentence.getEnText()));
                    } else if (Objects.equals(practiceType.getCategoryName(), "连词成句")) {
                        sentences.add(SafeConverter.toString(sentence.getEnText()));
                    }
                }
            }
        }
        Map<Long, BasicReviewHomeworkPersonalEnglishReport.StudentDetail> studentDetailMap = new LinkedHashMap<>();
        for (BasicReviewHomeworkReport reviewHomeworkReport : basicReviewHomeworkReportMap.values()) {
            if (reviewHomeworkReport.getPractices() == null)
                continue;
            if (!reviewHomeworkReport.getPractices().containsKey(ObjectiveConfigType.BASIC_APP))
                continue;
            BasicReviewHomeworkReportDetail detail = reviewHomeworkReport.getPractices().get(ObjectiveConfigType.BASIC_APP);
            if (detail.getAppAnswers() == null)
                continue;
            BasicReviewHomeworkPersonalEnglishReport.StudentDetail studentDetail1 = new BasicReviewHomeworkPersonalEnglishReport.StudentDetail();
            studentDetailMap.put(reviewHomeworkReport.getUserId(), studentDetail1);
            for (Map.Entry<String, BasicReviewHomeworkAppAnswer> basicReviewHomeworkAppAnswerEntry : detail.getAppAnswers().entrySet()) {
                BasicReviewHomeworkAppAnswer basicReviewHomeworkAppAnswer = basicReviewHomeworkAppAnswerEntry.getValue();
                if (!practiceTypeMap.containsKey(basicReviewHomeworkAppAnswerEntry.getKey()))
                    continue;
                PracticeType practiceType = practiceTypeMap.get(basicReviewHomeworkAppAnswerEntry.getKey());
                if (basicReviewHomeworkAppAnswer.getAnswers() == null)
                    continue;
                for (Map.Entry<String, BasicReviewHomeworkAnswer> entry : basicReviewHomeworkAppAnswer.getAnswers().entrySet()) {
                    String questionId = entry.getKey();
                    if (SafeConverter.toBoolean(entry.getValue().getGrasp()))
                        continue;
                    if (!newQuestionMap.containsKey(questionId))
                        continue;
                    if (!qidToSentenceMap.containsKey(questionId))
                        continue;
                    Long s = qidToSentenceMap.get(questionId);
                    if (!sentenceMap.containsKey(s))
                        continue;
                    Sentence sentence = sentenceMap.get(s);
                    if (Objects.equals(practiceType.getCategoryName(), "单词拼写") || Objects.equals(practiceType.getCategoryName(), "看图识词")) {
                        studentDetail1.getWrongWords().add(sentence.getEnText());
                    } else if (Objects.equals(practiceType.getCategoryName(), "连词成句")) {
                        studentDetail1.getWrongSentences().add(sentence.getEnText());
                    }
                }
            }
        }
        //学生自己对应成绩
        BasicReviewHomeworkPersonalEnglishReport.StudentDetail studentDetail1 = studentDetailMap.get(studentDetail.getId());
        //最好学生的单词错误数
        int bestWrongWordNum = studentDetail1.getWrongWords().size();
        //最好学生的句子错误数
        int bestWrongSentenceNum = studentDetail1.getWrongSentences().size();
        for (BasicReviewHomeworkPersonalEnglishReport.StudentDetail s : studentDetailMap.values()) {
            if (bestWrongWordNum > s.getWrongWords().size()) {
                bestWrongWordNum = s.getWrongWords().size();
            }
            if (bestWrongSentenceNum > s.getWrongSentences().size()) {
                bestWrongSentenceNum = s.getWrongSentences().size();
            }
        }
        //单词部分
        if (words.size() > 0) {
            BasicReviewHomeworkPersonalEnglishReport.WordPart wordPart = new BasicReviewHomeworkPersonalEnglishReport.WordPart();
            report.setWordPart(wordPart);
            wordPart.setPersonalWrongNum(studentDetail1.getWrongWords().size());
            wordPart.setWords(studentDetail1.getWrongWords());
            wordPart.setTotalWord(words.size());
            wordPart.setBestWrongNum(bestWrongWordNum);
        }
        //句子部分
        if (sentences.size() > 0) {
            BasicReviewHomeworkPersonalEnglishReport.SentencePart sentencePart = new BasicReviewHomeworkPersonalEnglishReport.SentencePart();
            report.setSentencePart(sentencePart);
            sentencePart.setTotalSentenceNum(sentences.size());
            sentencePart.setSentences(studentDetail1.getWrongSentences());
            sentencePart.setPersonalWrongNum(studentDetail1.getWrongSentences().size());
            sentencePart.setBestWrongNum(bestWrongSentenceNum);
        }

        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("report", report);
        return mapMessage;
    }

    private MapMessage fetchChineseReport(Clazz clazz, BasicReviewStage stage, NewHomework newHomework, BasicReviewHomeworkPackage basicReviewHomeworkPackage) {
        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(basicReviewHomeworkPackage.getClazzGroupId())
                .stream()
                .collect(Collectors
                        .toMap(LongIdEntity::getId, Function.identity()));
        String day = DayRange.newInstance(basicReviewHomeworkPackage.getCreateAt().getTime()).toString();
        Set<String> basicReviewHomeworkReportIds = new LinkedHashSet<>();
        for (Long userId : userMap.keySet()) {
            basicReviewHomeworkReportIds.add(new NewHomeworkResult.ID(day, basicReviewHomeworkPackage.getSubject(), stage.getHomeworkId(), userId.toString()).toString());
        }
        Map<Long, BasicReviewHomeworkReport> basicReviewHomeworkReportMap = basicReviewHomeworkReportDao.loads(basicReviewHomeworkReportIds)
                .values()
                .stream()
                .collect(Collectors.toMap(BasicReviewHomeworkReport::getUserId, Function.identity()));
        BasicReviewHomeworkShareCacheManager basicReviewHomeworkShareCacheManager = newHomeworkCacheService.getBasicReviewHomeworkShareCacheManager();
        String key = basicReviewHomeworkShareCacheManager.getCacheKey(basicReviewHomeworkPackage.getId(), newHomework.getId());
        Integer shareValue = basicReviewHomeworkShareCacheManager.load(key);
        boolean share = shareValue != null;

        BasicReviewHomeworkClazzStageChineseReport report = new BasicReviewHomeworkClazzStageChineseReport();
        report.setShare(share);
        report.setClazzName(clazz.formalizeClazzName());
        report.setClazzId(clazz.getId());
        report.setSubject(Subject.CHINESE);
        report.setSubjectName(Subject.CHINESE.getValue());
        //学生掌握情况
        BasicReviewHomeworkClazzStageChineseReport.StudentPart studentPart = new BasicReviewHomeworkClazzStageChineseReport.StudentPart();
        report.setStudentPart(studentPart);

        for (User user : userMap.values()) {
            if (basicReviewHomeworkReportMap.containsKey(user.getId())) {
                BasicReviewHomeworkReport reviewHomeworkReport = basicReviewHomeworkReportMap.get(user.getId());
                //个人学生
                BasicReviewHomeworkClazzStageChineseReport.StudentPersonalAnalysis s = new BasicReviewHomeworkClazzStageChineseReport.StudentPersonalAnalysis();
                s.setUserId(user.getId());
                s.setUserName(user.fetchRealnameIfBlankId());
                if (reviewHomeworkReport.getPractices() == null)
                    continue;
                if (!reviewHomeworkReport.getPractices().containsKey(ObjectiveConfigType.READ_RECITE_WITH_SCORE))
                    continue;
                BasicReviewHomeworkReportDetail basicReviewHomeworkReportDetail = reviewHomeworkReport.getPractices().get(ObjectiveConfigType.READ_RECITE_WITH_SCORE);
                if (basicReviewHomeworkReportDetail.getAppAnswers() == null)
                    continue;
                //总篇数
                long textNum = basicReviewHomeworkReportDetail.getAppAnswers().size();
                //达标篇数
                long graspTextNum = 0;
                for (BasicReviewHomeworkAppAnswer basicReviewHomeworkAppAnswer : basicReviewHomeworkReportDetail.getAppAnswers().values()) {
                    LinkedHashMap<String, BasicReviewHomeworkAnswer> answers = basicReviewHomeworkAppAnswer.getAnswers();
                    if (MapUtils.isNotEmpty(answers)) {
                        long graspNum = answers.values()
                                .stream()
                                .filter(o -> SafeConverter.toBoolean(o.getGrasp()))
                                .count();
                        double value = new BigDecimal(SafeConverter.toInt(graspNum) * 100).divide(new BigDecimal(answers.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        if (value >= NewHomeworkConstants.READ_RECITE_STANDARD) {
                            graspTextNum++;
                        }
                    }
                }

                s.getDetails().add(graspTextNum + "/" + textNum);
                s.setGraspNum(graspTextNum);
                studentPart.getStudentPersonalAnalysisList().add(s);
            } else {
                BasicReviewHomeworkClazzStageChineseReport.StudentPersonalAnalysis s = new BasicReviewHomeworkClazzStageChineseReport.StudentPersonalAnalysis();
                s.setUserId(user.getId());
                s.setUserName(user.fetchRealnameIfBlankId());
                s.getDetails().add("--");
                studentPart.getStudentPersonalAnalysisList().add(s);
            }
        }
        studentPart.getStudentPersonalAnalysisList().sort((o1, o2) -> Long.compare(o2.getGraspNum(), o1.getGraspNum()));
        report.setStageId(stage.getStageId());
        report.setHomeworkId(newHomework.getId());
        report.setStageName(stage.getStageName());
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("report", report);
        return mapMessage;
    }

    private MapMessage fetchMathReport(Clazz clazz, BasicReviewStage stage, NewHomework newHomework, BasicReviewHomeworkPackage basicReviewHomeworkPackage) {
        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(basicReviewHomeworkPackage.getClazzGroupId())
                .stream()
                .collect(Collectors
                        .toMap(LongIdEntity::getId, Function.identity()));
        String day = DayRange.newInstance(basicReviewHomeworkPackage.getCreateAt().getTime()).toString();
        Set<String> basicReviewHomeworkReportIds = new LinkedHashSet<>();
        for (Long userId : userMap.keySet()) {
            basicReviewHomeworkReportIds.add(new NewHomeworkResult.ID(day, basicReviewHomeworkPackage.getSubject(), stage.getHomeworkId(), userId.toString()).toString());
        }
        Map<Long, BasicReviewHomeworkReport> basicReviewHomeworkReportMap = basicReviewHomeworkReportDao.loads(basicReviewHomeworkReportIds)
                .values()
                .stream()
                .collect(Collectors.toMap(BasicReviewHomeworkReport::getUserId, Function.identity()));
        BasicReviewHomeworkShareCacheManager basicReviewHomeworkShareCacheManager = newHomeworkCacheService.getBasicReviewHomeworkShareCacheManager();
        String key = basicReviewHomeworkShareCacheManager.getCacheKey(basicReviewHomeworkPackage.getId(), newHomework.getId());
        Integer shareValue = basicReviewHomeworkShareCacheManager.load(key);
        boolean share = shareValue != null;

        BasicReviewHomeworkClazzStageMathReport report = new BasicReviewHomeworkClazzStageMathReport();
        report.setShare(share);
        report.setClazzName(clazz.formalizeClazzName());
        report.setClazzId(clazz.getId());
        report.setSubject(Subject.MATH);
        report.setSubjectName(Subject.MATH.getValue());
        //学生掌握情况
        BasicReviewHomeworkClazzStageMathReport.StudentPart studentPart = new BasicReviewHomeworkClazzStageMathReport.StudentPart();
        report.setStudentPart(studentPart);

        for (User user : userMap.values()) {
            if (basicReviewHomeworkReportMap.containsKey(user.getId())) {
                BasicReviewHomeworkReport reviewHomeworkReport = basicReviewHomeworkReportMap.get(user.getId());
                //个人学生
                BasicReviewHomeworkClazzStageMathReport.StudentPersonalAnalysis s = new BasicReviewHomeworkClazzStageMathReport.StudentPersonalAnalysis();
                s.setUserId(user.getId());
                s.setUserName(user.fetchRealnameIfBlankId());
                if (reviewHomeworkReport.getPractices() == null)
                    continue;
                if (!reviewHomeworkReport.getPractices().containsKey(ObjectiveConfigType.MENTAL_ARITHMETIC))
                    continue;
                BasicReviewHomeworkReportDetail basicReviewHomeworkReportDetail = reviewHomeworkReport.getPractices().get(ObjectiveConfigType.MENTAL_ARITHMETIC);
                if (basicReviewHomeworkReportDetail.getAnswers() == null)
                    continue;
                //错误题数
                long wrongNum = basicReviewHomeworkReportDetail.getAnswers()
                        .values()
                        .stream()
                        .filter(o -> !SafeConverter.toBoolean(o.getGrasp()))
                        .count();
                s.getDetails().add(wrongNum + "");
                s.setWrongNum(wrongNum);
                studentPart.getStudentPersonalAnalysisList().add(s);
            } else {
                BasicReviewHomeworkClazzStageMathReport.StudentPersonalAnalysis s = new BasicReviewHomeworkClazzStageMathReport.StudentPersonalAnalysis();
                s.setUserId(user.getId());
                s.setUserName(user.fetchRealnameIfBlankId());
                s.getDetails().add("--");
                studentPart.getStudentPersonalAnalysisList().add(s);
            }

            List<BasicReviewHomeworkClazzStageMathReport.StudentPersonalAnalysis> studentPersonalAnalysises = studentPart.getStudentPersonalAnalysisList().stream()
                    .filter(o -> o.getWrongNum() > 0)
                    .sorted((o1, o2) -> Long.compare(o2.getWrongNum(), o1.getWrongNum()))
                    .collect(Collectors.toList());
            if (studentPersonalAnalysises.size() < 5) {
                List<String> wrongMostUserName = studentPersonalAnalysises.stream()
                        .map(BasicReviewHomeworkClazzStageMathReport.StudentPersonalAnalysis::getUserName)
                        .collect(Collectors.toList());
                studentPart.setWrongMostUserName(wrongMostUserName);
            } else {
                List<String> wrongMostUserName = studentPersonalAnalysises.subList(0, 5)
                        .stream()
                        .map(BasicReviewHomeworkClazzStageMathReport.StudentPersonalAnalysis::getUserName).collect(Collectors.toList());
                studentPart.setWrongMostUserName(wrongMostUserName);
            }


        }
        studentPart.getStudentPersonalAnalysisList().sort((o1, o2) -> Long.compare(o2.getWrongNum(), o1.getWrongNum()));
        report.setStageId(stage.getStageId());
        report.setHomeworkId(newHomework.getId());
        report.setStageName(stage.getStageName());
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("report", report);
        return mapMessage;
    }

    private MapMessage fetchEnglishReport(Clazz clazz, BasicReviewStage stage, NewHomework newHomework, BasicReviewHomeworkPackage basicReviewHomeworkPackage) {
        //TODO 类型配置起来
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.BASIC_APP);
        if (target == null) {
            return MapMessage.errorMessage("作业错误,不包含基础练习");
        }
        List<String> allQuestionIds = target.processNewHomeworkQuestion(false)
                .stream()
                .map(NewHomeworkQuestion::getQuestionId)
                .collect(Collectors.toList());
        Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(allQuestionIds);
        Map<String, Long> qidToSentenceMap = newQuestionMap
                .values()
                .stream()
                .filter(o -> CollectionUtils.isNotEmpty(o.getSentenceIds()))
                .collect(Collectors.toMap(NewQuestion::getId, o -> o.getSentenceIds().get(0)));
        Map<Long, Sentence> sentenceMap = englishContentLoaderClient.loadEnglishSentences(qidToSentenceMap.values());

        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(basicReviewHomeworkPackage.getClazzGroupId())
                .stream()
                .collect(Collectors
                        .toMap(LongIdEntity::getId, Function.identity()));
        String day = DayRange.newInstance(basicReviewHomeworkPackage.getCreateAt().getTime()).toString();
        Set<String> basicReviewHomeworkReportIds = new LinkedHashSet<>();
        for (Long userId : userMap.keySet()) {
            basicReviewHomeworkReportIds.add(new NewHomeworkResult.ID(day, basicReviewHomeworkPackage.getSubject(), stage.getHomeworkId(), userId.toString()).toString());
        }
        //单份作业完成情况的数据
        Map<String, BasicReviewHomeworkReport> basicReviewHomeworkReportMap = basicReviewHomeworkReportDao.loads(basicReviewHomeworkReportIds);
        //单词
        Map<String, WordAnalysis> wordAnalysisMap = new LinkedHashMap<>();
        //句子
        Map<String, SentenceAnalysis> sentenceAnalysisMap = new LinkedHashMap<>();
        //包含类型
        Map<String, PracticeType> practiceTypeMap = new LinkedHashMap<>();

        //学生个人情况
        List<StudentPersonalAnalysis> studentPersonalAnalysisList = new LinkedList<>();
        Map<Long, StudentPersonalAnalysis> studentPersonalAnalysisMap = new LinkedHashMap<>();
        boolean reviewWord = false;
        boolean reviewSentence = false;
        if (target.getApps() != null) {
            for (NewHomeworkApp app : target.getApps()) {
                PracticeType practiceType = practiceServiceClient.getPracticeBuffer().loadPractice(app.getPracticeId());
                if (practiceType == null)
                    continue;
                String key = app.getCategoryId() + "-" + app.getLessonId();
                practiceTypeMap.put(key, practiceType);
                if (app.getQuestions() != null) {
                    for (NewHomeworkQuestion question : app.getQuestions()) {
                        if (!newQuestionMap.containsKey(question.getQuestionId()))
                            continue;
                        if (!qidToSentenceMap.containsKey(question.getQuestionId()))
                            continue;
                        Long s = qidToSentenceMap.get(question.getQuestionId());
                        if (!sentenceMap.containsKey(s))
                            continue;
                        Sentence sentence = sentenceMap.get(s);
                        if (Objects.equals(practiceType.getCategoryName(), "单词拼写") || Objects.equals(practiceType.getCategoryName(), "看图识词")) {
                            reviewWord = true;
                            //单词
                            WordAnalysis wordAnalysis = new WordAnalysis();
                            wordAnalysis.setWord(SafeConverter.toString(sentence.getEnText()));
                            wordAnalysisMap.put(question.getQuestionId(), wordAnalysis);
                        } else if (Objects.equals(practiceType.getCategoryName(), "连词成句")) {
                            //句子
                            reviewSentence = true;
                            SentenceAnalysis sentenceAnalysis = new SentenceAnalysis();
                            sentenceAnalysis.setSentence(SafeConverter.toString(sentence.getEnText()));
                            sentenceAnalysisMap.put(question.getQuestionId(), sentenceAnalysis);
                        }
                    }
                }
            }
        }
        for (BasicReviewHomeworkReport basicReviewHomeworkReport : basicReviewHomeworkReportMap.values()) {
            if (basicReviewHomeworkReport.getPractices() == null)
                continue;
            if (!basicReviewHomeworkReport.getPractices().containsKey(ObjectiveConfigType.BASIC_APP))
                continue;
            BasicReviewHomeworkReportDetail basicReviewHomeworkReportDetail = basicReviewHomeworkReport.getPractices().get(ObjectiveConfigType.BASIC_APP);
            if (basicReviewHomeworkReportDetail.getAppAnswers() == null)
                continue;
            if (!userMap.containsKey(basicReviewHomeworkReport.getUserId()))
                continue;
            User user = userMap.get(basicReviewHomeworkReport.getUserId());
            //学生掌握情况
            StudentPersonalAnalysis studentPersonalAnalysis = new StudentPersonalAnalysis();
            studentPersonalAnalysis.setUserName(user.fetchRealnameIfBlankId());
            studentPersonalAnalysis.setUserId(user.getId());
            studentPersonalAnalysisList.add(studentPersonalAnalysis);
            studentPersonalAnalysisMap.put(user.getId(), studentPersonalAnalysis);
            for (Map.Entry<String, BasicReviewHomeworkAppAnswer> appAnswerEntry : basicReviewHomeworkReportDetail.getAppAnswers().entrySet()) {
                BasicReviewHomeworkAppAnswer appAnswer = appAnswerEntry.getValue();
                if (!practiceTypeMap.containsKey(appAnswerEntry.getKey()))
                    continue;
                if (appAnswer.getAnswers() == null)
                    continue;
                PracticeType practiceType = practiceTypeMap.get(appAnswerEntry.getKey());
                if (Objects.equals(practiceType.getCategoryName(), "单词拼写")) {
                    //单词拼写
                    studentPersonalAnalysis.setReviewWord(true);
                    for (Map.Entry<String, BasicReviewHomeworkAnswer> entry : appAnswer.getAnswers().entrySet()) {
                        //该题错误的时候记载
                        if (SafeConverter.toBoolean(entry.getValue().getGrasp()))
                            continue;
                        if (!wordAnalysisMap.containsKey(entry.getKey()))
                            continue;
                        WordAnalysis wordAnalysis = wordAnalysisMap.get(entry.getKey());
                        studentPersonalAnalysis.getWrongWords().add(wordAnalysis.getWord());
                        studentPersonalAnalysis.setWrongNum(1 + studentPersonalAnalysis.getWrongNum());
                        wordAnalysis.getWrongUserIds().add(basicReviewHomeworkReport.getUserId());
                        wordAnalysis.getMishearUserIds().add(basicReviewHomeworkReport.getUserId());
                    }
                } else if (Objects.equals(practiceType.getCategoryName(), "看图识词")) {
                    studentPersonalAnalysis.setReviewWord(true);
                    //看图识词
                    for (Map.Entry<String, BasicReviewHomeworkAnswer> entry : appAnswer.getAnswers().entrySet()) {
                        //该题错误的时候记载
                        if (SafeConverter.toBoolean(entry.getValue().getGrasp()))
                            continue;
                        if (!wordAnalysisMap.containsKey(entry.getKey()))
                            continue;
                        WordAnalysis wordAnalysis = wordAnalysisMap.get(entry.getKey());
                        studentPersonalAnalysis.setWrongNum(1 + studentPersonalAnalysis.getWrongNum());
                        studentPersonalAnalysis.getWrongWords().add(wordAnalysis.getWord());
                        wordAnalysis.getWrongUserIds().add(basicReviewHomeworkReport.getUserId());
                        wordAnalysis.getMisLookUserIds().add(basicReviewHomeworkReport.getUserId());
                    }
                } else if (Objects.equals(practiceType.getCategoryName(), "连词成句")) {
                    studentPersonalAnalysis.setReviewSentence(true);
                    //连词成句
                    for (Map.Entry<String, BasicReviewHomeworkAnswer> entry : appAnswer.getAnswers().entrySet()) {
                        if (SafeConverter.toBoolean(entry.getValue().getGrasp()))
                            continue;
                        if (!sentenceAnalysisMap.containsKey(entry.getKey()))
                            continue;
                        SentenceAnalysis sentenceAnalysis = sentenceAnalysisMap.get(entry.getKey());
                        studentPersonalAnalysis.getWrongSentences().add(sentenceAnalysis.getSentence());
                        studentPersonalAnalysis.setWrongNum(1 + studentPersonalAnalysis.getWrongNum());
                        sentenceAnalysis.getWrongUserIds().add(basicReviewHomeworkReport.getUserId());
                    }
                }
            }
        }
        //单词去重
        Map<String, WordAnalysis> wordAnalysisQuChongMap = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(wordAnalysisMap)) {
            for (Map.Entry<String, WordAnalysis> entry : wordAnalysisMap.entrySet()) {
                WordAnalysis value = entry.getValue();
                if (wordAnalysisQuChongMap.containsKey(value.getWord())) {
                    WordAnalysis wordAnalysis = wordAnalysisQuChongMap.get(value.getWord());
                    wordAnalysis.getMisLookUserIds().addAll(value.getMisLookUserIds());
                    wordAnalysis.getMishearUserIds().addAll(value.getMishearUserIds());
                    wordAnalysis.getWrongUserIds().addAll(value.getWrongUserIds());
                } else {
                    wordAnalysisQuChongMap.put(value.getWord(), value);
                }
            }
        }
        //句子去重
        Map<String, SentenceAnalysis> sentenceAnalysisQuChongMap = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(sentenceAnalysisMap)) {
            for (Map.Entry<String, SentenceAnalysis> entry : sentenceAnalysisMap.entrySet()) {
                SentenceAnalysis value = entry.getValue();
                if (sentenceAnalysisQuChongMap.containsKey(value.getSentence())) {
                    SentenceAnalysis sentenceAnalysis = sentenceAnalysisQuChongMap.get(value.getSentence());
                    sentenceAnalysis.getWrongUserIds().addAll(value.getWrongUserIds());
                } else {
                    sentenceAnalysisQuChongMap.put(value.getSentence(), value);
                }
            }
        }
        BasicReviewHomeworkShareCacheManager basicReviewHomeworkShareCacheManager = newHomeworkCacheService.getBasicReviewHomeworkShareCacheManager();
        String key = basicReviewHomeworkShareCacheManager.getCacheKey(basicReviewHomeworkPackage.getId(), newHomework.getId());
        Integer shareValue = basicReviewHomeworkShareCacheManager.load(key);
        boolean share = shareValue != null;
        BasicReviewHomeworkClazzStageEnglishReport report = new BasicReviewHomeworkClazzStageEnglishReport();

        report.setShare(share);
        report.setClazzName(clazz.formalizeClazzName());
        report.setClazzId(clazz.getId());
        report.setSubject(Subject.ENGLISH);
        report.setSubjectName(Subject.ENGLISH.getValue());
        int finishUserNum = basicReviewHomeworkReportMap.size();
        //内容掌握情况
        BasicReviewHomeworkClazzStageEnglishReport.ContentPart contentPart = new BasicReviewHomeworkClazzStageEnglishReport.ContentPart();
        report.setContentPart(contentPart);
        //单词列表
        if (MapUtils.isNotEmpty(wordAnalysisQuChongMap)) {
            contentPart.setWordFinishUserNum(finishUserNum);
            for (WordAnalysis wordAnalysis : wordAnalysisQuChongMap.values()) {
                int size = wordAnalysis.getWrongUserIds().size();
                //错误率大于百分之十的单词
                if (size > 0) {
                    int value = new BigDecimal(size * 100).divide(new BigDecimal(finishUserNum), 0, BigDecimal.ROUND_HALF_UP).intValue();
                    if (value > 10) {
                        contentPart.getWords().add(wordAnalysis.getWord());
                    }
                }
                int mishearNum = wordAnalysis.getMishearUserIds().size();
                int misLookNum = wordAnalysis.getMisLookUserIds().size();
                BasicReviewHomeworkClazzStageEnglishReport.WordAnalysis w = new BasicReviewHomeworkClazzStageEnglishReport.WordAnalysis();
                w.setMisLookNum(misLookNum);
                w.setMishearNum(mishearNum);
                w.setWrongNum(mishearNum + misLookNum);
                w.setWord(wordAnalysis.getWord());
                contentPart.getWordAnalysisList().add(w);
            }
            contentPart.getWordAnalysisList().sort((o1, o2) -> Integer.compare(o2.getWrongNum(), o1.getWrongNum()));
        }
        //句子列表
        if (MapUtils.isNotEmpty(sentenceAnalysisQuChongMap)) {
            contentPart.setSentenceFinishNum(finishUserNum);
            for (SentenceAnalysis sentenceAnalysis : sentenceAnalysisQuChongMap.values()) {
                int size = sentenceAnalysis.getWrongUserIds().size();
                //错误率大于百分之十的单词
                if (size > 0) {
                    int value = new BigDecimal(size * 100).divide(new BigDecimal(finishUserNum), 0, BigDecimal.ROUND_HALF_UP).intValue();
                    if (value > 10) {
                        contentPart.getSentences().add(sentenceAnalysis.getSentence());
                    }
                }
                BasicReviewHomeworkClazzStageEnglishReport.SentenceAnalysis s = new BasicReviewHomeworkClazzStageEnglishReport.SentenceAnalysis();
                s.setSentence(sentenceAnalysis.getSentence());
                s.setWrongNum(size);
                contentPart.getSentenceAnalysisList().add(s);
            }
            contentPart.getSentenceAnalysisList().sort((o1, o2) -> Integer.compare(o2.getWrongNum(), o1.getWrongNum()));
        }

        //学生掌握情况
        BasicReviewHomeworkClazzStageEnglishReport.StudentPart studentPart = new BasicReviewHomeworkClazzStageEnglishReport.StudentPart();
        report.setStudentPart(studentPart);
        if (CollectionUtils.isNotEmpty(studentPersonalAnalysisList)) {
            //错误最多的人数
            List<StudentPersonalAnalysis> studentPersonalAnalysises = studentPersonalAnalysisList.stream()
                    .filter(o -> o.getWrongNum() > 0)
                    .sorted((o1, o2) -> Integer.compare(o2.getWrongNum(), o1.getWrongNum()))
                    .collect(Collectors.toList());
            if (studentPersonalAnalysises.size() < 5) {
                List<String> wrongMostUserName = studentPersonalAnalysises.stream()
                        .map(StudentPersonalAnalysis::getUserName)
                        .collect(Collectors.toList());
                studentPart.setWrongMostUserName(wrongMostUserName);
            } else {
                List<String> wrongMostUserName = studentPersonalAnalysises.subList(0, 5)
                        .stream()
                        .map(StudentPersonalAnalysis::getUserName).collect(Collectors.toList());
                studentPart.setWrongMostUserName(wrongMostUserName);

            }
            if (reviewWord) {
                studentPart.getTabList().add("单词复习");
                studentPart.getTabList().add("错误单词");
            }
            if (reviewSentence) {
                studentPart.getTabList().add("句子复习");
                studentPart.getTabList().add("错误句子");
            }

            //列表数据
            for (User user : userMap.values()) {
                if (studentPersonalAnalysisMap.containsKey(user.getId())) {
                    StudentPersonalAnalysis s = studentPersonalAnalysisMap.get(user.getId());
                    BasicReviewHomeworkClazzStageEnglishReport.StudentPersonalAnalysis studentPersonalAnalysis = new BasicReviewHomeworkClazzStageEnglishReport.StudentPersonalAnalysis();
                    studentPersonalAnalysis.setUserId(user.getId());
                    studentPersonalAnalysis.setUserName(user.fetchRealnameIfBlankId());
                    if (s.isReviewWord()) {
                        studentPersonalAnalysis.getDetails().add("已复习");
                        studentPersonalAnalysis.getDetails().add(s.getWrongWords().size() + "");
                    }
                    if (s.isReviewSentence()) {
                        studentPersonalAnalysis.getDetails().add("已复习");
                        studentPersonalAnalysis.getDetails().add(s.getWrongSentences().size() + "");
                    }
                    studentPersonalAnalysis.setWrongNum(s.getWrongNum());
                    studentPart.getStudentPersonalAnalysisList().add(studentPersonalAnalysis);
                } else {
                    BasicReviewHomeworkClazzStageEnglishReport.StudentPersonalAnalysis studentPersonalAnalysis = new BasicReviewHomeworkClazzStageEnglishReport.StudentPersonalAnalysis();
                    studentPersonalAnalysis.setUserId(user.getId());
                    studentPersonalAnalysis.setUserName(user.fetchRealnameIfBlankId());
                    if (reviewWord) {
                        studentPersonalAnalysis.getDetails().add("未复习");
                        studentPersonalAnalysis.getDetails().add("--");
                    }
                    if (reviewSentence) {
                        studentPersonalAnalysis.getDetails().add("未复习");
                        studentPersonalAnalysis.getDetails().add("--");
                    }
                    studentPart.getStudentPersonalAnalysisList().add(studentPersonalAnalysis);
                }
            }
            studentPart.getStudentPersonalAnalysisList().sort((o1, o2) -> Integer.compare(o2.getWrongNum(), o1.getWrongNum()));
        }
        report.setHomeworkId(newHomework.getId());
        report.setStageId(stage.getStageId());
        report.setStageName(stage.getStageName());
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("report", report);
        return mapMessage;
    }

    /**
     * 期末复习-作业历史
     *
     * @param teacher
     * @return
     */
    @Override
    public List<BasicReviewHomeworkHistory> basicReviewHomeworkHistory(Teacher teacher) {
        try {
            List<BasicReviewHomeworkHistory> result = new LinkedList<>();

            // 获取教师的班级IDs
            Set<Long> groupIds = raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient()
                    .findByTeacherId(teacher.getId())
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(GroupTeacherTuple::isValidTrue)
                    .map(GroupTeacherTuple::getGroupId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (CollectionUtils.isNotEmpty(groupIds)) {
                Map<Long, List<BasicReviewHomeworkPackage>> basicReviewHomeworkPackageMap = basicReviewHomeworkPackageDao.loadBasicReviewHomeworkPackageByClazzGroupIds(groupIds);
                //获取教材ID
                List<String> bookIds = basicReviewHomeworkPackageMap.values()
                        .stream()
                        .flatMap(Collection::stream)
                        .filter(o -> !SafeConverter.toBoolean(o.getDisabled()))
                        .map(BasicReviewHomeworkPackage::getBookId)
                        .filter(StringUtils::isNoneBlank)
                        .collect(Collectors.toList());
                //获取教材信息
                Map<String, NewBookProfile> newBookProfileMap = newContentLoaderClient.loadBooks(bookIds);
                //班组相关信息
                Map<Long, GroupMapper> groupMapperMap = groupLoaderClient.loadGroups(basicReviewHomeworkPackageMap.keySet(), true);
                //班组对应班级ID，For获取班级信息
                Map<Long, Long> groupIdToClazzIdMap = new LinkedHashMap<>();
                Set<Long> clazzIds = new LinkedHashSet<>();
                for (GroupMapper groupMapper : groupMapperMap.values()) {
                    Long clazzId = groupMapper.getClazzId();
                    clazzIds.add(clazzId);
                    groupIdToClazzIdMap.put(groupMapper.getId(), clazzId);
                }

                Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazzs(clazzIds)
                        .stream()
                        .collect(Collectors.toMap(Clazz::getId, Function.identity()));
                Map<Long, List<User>> usersForGroup = studentLoaderClient.loadGroupStudents(basicReviewHomeworkPackageMap.keySet());
                //各个班组对应学生期末复习作业中间表记录
                Map<Long, List<BasicReviewHomeworkCacheMapper>> basicReviewHomeworkCacheMapper = groupIds
                        .stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(Function.identity(), basicReviewHomeworkCacheLoader::loadBasicReviewHomeworkCacheMappers));

                for (Long groupId : basicReviewHomeworkPackageMap.keySet()) {
                    if (!usersForGroup.containsKey(groupId)) {
                        continue;
                    }
                    if (!groupIdToClazzIdMap.containsKey(groupId)) {
                        continue;
                    }
                    if (!clazzMap.containsKey(groupIdToClazzIdMap.get(groupId))) {
                        continue;
                    }
                    if (basicReviewHomeworkPackageMap.get(groupId).size() != 1) {
                        continue;
                    }
                    //一个班组只有一份作业
                    BasicReviewHomeworkPackage homeworkPackage = basicReviewHomeworkPackageMap.get(groupId).get(0);
                    if (homeworkPackage.getSubject() == null) {
                        continue;
                    }

                    //总人数
                    Integer totalNum = usersForGroup.get(groupId).size();
                    int beginNum = 0;
                    int finishNum = 0;
                    if (basicReviewHomeworkCacheMapper.containsKey(homeworkPackage.getClazzGroupId())) {
                        List<BasicReviewHomeworkCacheMapper> basicReviewHomeworkCacheMappers = basicReviewHomeworkCacheMapper.get(homeworkPackage.getClazzGroupId());
                        if (CollectionUtils.isNotEmpty(basicReviewHomeworkCacheMappers)) {
                            for (BasicReviewHomeworkCacheMapper reviewHomeworkCacheMapper : basicReviewHomeworkCacheMappers) {
                                Long begin = reviewHomeworkCacheMapper.getHomeworkDetail().values()
                                        .stream()
                                        .filter(o -> Objects.equals(o.getStageId(), 1) && o.getFinishAt() != null)
                                        .count();
                                beginNum += begin;
                            }

                            finishNum = (int) basicReviewHomeworkCacheMappers.stream()
                                    .filter(Objects::nonNull)
                                    .filter(o -> o.getFinished() != null)
                                    .filter(BasicReviewHomeworkCacheMapper::getFinished)
                                    .count();
                        }
                    }
                    String bookId = "";
                    String bookName = "";
                    if (StringUtils.isNotBlank(homeworkPackage.getBookId()) && newBookProfileMap.containsKey(homeworkPackage.getBookId())) {
                        NewBookProfile newBookProfile = newBookProfileMap.get(homeworkPackage.getBookId());
                        if (newBookProfile != null && StringUtils.isNotBlank(newBookProfile.getName())) {
                            bookId = homeworkPackage.getBookId();
                            bookName = newBookProfile.getName();
                        }
                    }
                    Clazz clazz = clazzMap.get(groupIdToClazzIdMap.get(groupId));
                    BasicReviewHomeworkHistory basicReviewHomeworkHistory = new BasicReviewHomeworkHistory();
                    basicReviewHomeworkHistory.setClassName(clazz.formalizeClazzName());
                    basicReviewHomeworkHistory.setSubject(homeworkPackage.getSubject());
                    basicReviewHomeworkHistory.setSubjectName(homeworkPackage.getSubject().getValue());
                    basicReviewHomeworkHistory.setStartTime(DateUtils.dateToString(homeworkPackage.getCreateAt()));
                    basicReviewHomeworkHistory.setEndTime(DateUtils.dateToString(NewHomeworkConstants.BASIC_REVIEW_END_DATE));
                    basicReviewHomeworkHistory.setTotalNum(totalNum);
                    basicReviewHomeworkHistory.setBeginNum(beginNum);
                    basicReviewHomeworkHistory.setFinishNum(finishNum);
                    basicReviewHomeworkHistory.setPackageId(homeworkPackage.getId());
                    basicReviewHomeworkHistory.setBookId(bookId);
                    basicReviewHomeworkHistory.setBookName(bookName);
                    result.add(basicReviewHomeworkHistory);
                }
            }
            return result;
        } catch (Exception e) {
            logger.error("basic Review Homework History failed : teacherId {}", teacher.getId(), e);
            return Collections.emptyList();
        }
    }
}
