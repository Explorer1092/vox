/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.client.CampaignServiceClient;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.content.api.constant.NewBookType;
import com.voxlearning.utopia.service.content.api.entity.*;
import com.voxlearning.utopia.service.content.consumer.ChineseContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewClazzBookLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewEnglishContentLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.VacationHomeworkCacheLoader;
import com.voxlearning.utopia.service.newhomework.api.VacationHomeworkLoader;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.VacationHomeworkLevel;
import com.voxlearning.utopia.service.newhomework.api.constant.VacationHomeworkPlannedDaysType;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkBlackWhiteList;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.mapper.AssignableValidationResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkDetailCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkPlannedDaysMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkWinterPlanCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.consumer.cache.RemindStudentVacationProgressCacheManager;
import com.voxlearning.utopia.service.newhomework.consumer.cache.ShareVacationReportCacheManager;
import com.voxlearning.utopia.service.newhomework.consumer.cache.ShareWeiXinVacationReportCacheManager;
import com.voxlearning.utopia.service.newhomework.impl.dao.HomeworkBlackWhiteListDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkPackageDao;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkCacheServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.DoVacationHomeworkProcessor;
import com.voxlearning.utopia.service.newhomework.impl.template.internal.content.NewHomeworkBasicAppContentLoader;
import com.voxlearning.utopia.service.newhomework.impl.template.internal.content.NewHomeworkNaturalSpellingContentLoader;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.consumer.DubbingLoaderClient;
import com.voxlearning.utopia.service.question.consumer.PictureBookLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionContentTypeLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.extension.ExClazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.ClazzGroup;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * @author tanguohong
 * @since 2016/11/29
 */
@Named
@Service(interfaceClass = VacationHomeworkLoader.class)
@ExposeService(interfaceClass = VacationHomeworkLoader.class)
public class VacationHomeworkLoaderImpl extends SpringContainerSupport implements VacationHomeworkLoader {

    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject private VacationHomeworkDao vacationHomeworkDao;
    @Inject private VacationHomeworkPackageDao vacationHomeworkPackageDao;
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private NewClazzBookLoaderClient newClazzBookLoaderClient;
    @Inject private NewContentLoaderClient newContentLoaderClient;
    @Inject private VacationHomeworkCacheLoader vacationHomeworkCacheLoader;
    @Inject private DoVacationHomeworkProcessor doVacationHomeworkProcessor;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject private QuestionLoaderClient questionLoaderClient;
    @Inject private QuestionContentTypeLoaderClient questionContentTypeLoaderClient;
    @Inject private PictureBookLoaderClient pictureBookLoaderClient;
    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject private NewHomeworkBasicAppContentLoader newHomeworkBasicAppContentLoader;
    @Inject private NewHomeworkNaturalSpellingContentLoader newHomeworkNaturalSpellingContentLoader;
    @Inject private NewEnglishContentLoaderClient newEnglishContentLoaderClient;
    @Inject private DubbingLoaderClient dubbingLoaderClient;
    @Inject private ChineseContentLoaderClient chineseContentLoaderClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private CampaignServiceClient campaignServiceClient;
    @Inject private HomeworkBlackWhiteListDao homeworkBlackWhiteListDao;

    @Inject private RaikouSDK raikouSDK;


    private static final String BANNER_URL = "resources/app/17teacher/res/homework/vacation/vacation_banner_20181222.png";

    @Override
    public Map<Long, List<VacationHomework.Location>> loadVacationHomeworksByClazzGroupIds(Collection<Long> groupIds) {
        return vacationHomeworkDao.loadVacationHomeworkByClazzGroupIds(groupIds);
    }

    @Override
    public Map<Long, List<VacationHomeworkPackage.Location>> loadVacationHomeworkPackageByClazzGroupIds(Collection<Long> groupIds) {
        return vacationHomeworkPackageDao.loadVacationHomeworkPackageByClazzGroupIds(groupIds);
    }

    @Override
    public List<ExClazz> findTeacherClazzsCanBeAssignedHomework(Teacher teacher) {
        if (teacher == null || teacher.getId() == null || teacher.getSubject() == null) {
            return Collections.emptyList();
        }
        Subject subject = teacher.getSubject();
        List<ExClazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherExClazzsWithSpecifiedSubject(teacher.getId(), subject, false, false);
        if (CollectionUtils.isEmpty(clazzs)) {
            logger.warn("Teacher '{}' has no non-terminal clazzs, ignore", teacher.getId());
            return Collections.emptyList();
        }
        Map<Long, ExClazz> clazzCandidate = getCandidateSystemClazz(subject, clazzs);

        return clazzCandidate.values().stream()
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());
    }

    /**
     * 获得可布置假期作业的系统自建班级
     */
    private Map<Long, ExClazz> getCandidateSystemClazz(Subject subject, List<ExClazz> clazzs) {
        Map<Long, ExClazz> clazzCandidate = clazzs.stream()
                .collect(Collectors.toMap(ExClazz::getId, Function.identity()));

        // 这里已经保证所有的都是clazz id -> group id对
        Set<ClazzGroup> cg = clazzs.stream()
                .filter(e -> CollectionUtils.isNotEmpty(e.getCurTeacherGroups()))
                .map(e -> new ClazzGroup(e.getId(), e.getCurTeacherGroups().get(0).getId()))
                .collect(Collectors.toSet());

        AssignableValidationResult avr = validate(subject, cg);

        for (Iterator<Map.Entry<Long, ExClazz>> it = clazzCandidate.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Long, ExClazz> entry = it.next();
            Long cid = entry.getKey();
            List<GroupMapper> groupMappers = entry.getValue().getCurTeacherGroups();
            if (CollectionUtils.isEmpty(groupMappers)) {
                continue;
            }
            List<GroupMapper> arrangeableGroups = entry.getValue().getCurTeacherArrangeableGroups();
            if (arrangeableGroups == null) {
                arrangeableGroups = new LinkedList<>();
                entry.getValue().setCurTeacherArrangeableGroups(arrangeableGroups);
            }
            for (GroupMapper groupMapper : groupMappers) {
                ClazzGroup tcg = new ClazzGroup(cid, groupMapper.getId());
                if (avr.getAssignables().contains(tcg)) {
                    arrangeableGroups.add(groupMapper);
                }
            }
            // 当无可布置作业分组时，班级同样是无法布置作业状态
            if (CollectionUtils.isEmpty(arrangeableGroups)) {
                it.remove();
            } else {
                Collections.sort(arrangeableGroups);
            }
        }
        return clazzCandidate;
    }

    // 默认所有的都有分组
    private AssignableValidationResult validate(Subject subject, Collection<ClazzGroup> clazzGroups) {
        AssignableValidationResult result = new AssignableValidationResult();
        if (CollectionUtils.isEmpty(clazzGroups)) return result;

        Set<Long> groupIds = clazzGroups.stream().map(ClazzGroup::getGroupId).collect(Collectors.toSet());

        // 验证每个分组是否有学生
        Map<Long, List<Long>> groupStudentIds = studentLoaderClient.loadGroupStudentIds(groupIds);
        for (ClazzGroup clazzGroup : clazzGroups) {
            Long clazzId = clazzGroup.getClazzId();
            Long groupId = clazzGroup.getGroupId();
            // 这里实际上只需要判断学生是否存在，不需要拿学生的具体信息
            List<Long> studentIds = groupStudentIds.get(groupId);
            if (CollectionUtils.isEmpty(studentIds)) {
                logger.debug("Clazz/group {}/{} has no students, non-assignable", clazzId, groupId);
                result.getNonAssignables().add(clazzGroup);
            }
        }

        // 学生数没有问题了
        // 验证是否已经布置假期作业
        Map<Long, List<VacationHomeworkPackage.Location>> groupLocationMap = loadVacationHomeworkPackageByClazzGroupIds(groupIds);
        for (ClazzGroup clazzGroup : clazzGroups) {
            if (result.getNonAssignables().contains(clazzGroup)) {
                continue;
            }

            Long groupId = clazzGroup.getGroupId();
            long assignCount = groupLocationMap.get(groupId) == null ? 0 : groupLocationMap.get(groupId).stream().filter(e -> e.getSubject().equals(subject)).collect(Collectors.toList()).size();
            if (assignCount > 0) {
                logger.debug("Group {} has unchecked homework, non-assignable", groupId);
                result.getNonAssignables().add(clazzGroup);
            }
        }

        // 把所有剩余的班级/群组归入到可布置的集合
        clazzGroups.forEach(clazzGroup -> {
            if (!result.getNonAssignables().contains(clazzGroup)) {
                result.getAssignables().add(clazzGroup);
            }
        });

        return result;
    }

    @Override
    public MapMessage loadTeachersClazzListForApp(Collection<Long> teacherIds, String domain) {
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

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherIds.iterator().next());
        boolean mustUseLastTermBook = grayFunctionManagerClient.getTeacherGrayFunctionManager()
                .isWebGrayFunctionAvailable(teacherDetail, "VacationHW", "LastTermBook");

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
        Map<Long, List<VacationHomeworkPackage.Location>> groupLocationMap = loadVacationHomeworkPackageByClazzGroupIds(groupIds);
        // 班级
        Date now = new Date();
        List<Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
//                .filter(e -> {
//                    // 能布置作业时过滤6年级与54制5年级
//                    if (now.after(NewHomeworkConstants.VH_START_DATE_LATEST)) {
//                        return true;
//                    }
//                    int clazzLevel = e.getClazzLevel().getLevel();
//                    EduSystemType eduSystemType = e.getEduSystem();
//                    return !(clazzLevel == 6 || (EduSystemType.P5 == eduSystemType && clazzLevel == 5));
//                })
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());
        //获取班级组课本ID
        Map<Long, List<NewClazzBookRef>> clazzBookRefMap = newClazzBookLoaderClient.loadGroupBookRefs(groupIds)
                .toList()
                .stream()
                .collect(Collectors.groupingBy(NewClazzBookRef::getGroupId, Collectors.toList()));


        List<Map<String, Object>> sharePackageInfo = new LinkedList<>();
        List<Map<String, Object>> clazzMap = new ArrayList<>();
        clazzs.stream()
                .filter(clazz -> clazzIdGroupIdMap.containsKey(clazz.getId()))
                .forEach((Clazz c) -> {
                    List<Long> gids = clazzIdGroupIdMap.get(c.getId());
                    List<Long> sortedGroupIds = gids.stream()
                            .sorted((a, b) -> {
                                Subject subjectA = groupIdSubjectMap.get(a);
                                Subject subjectB = groupIdSubjectMap.get(b);
                                // 按照英语、数学、语文来排序
                                if (subjectA != subjectB) {
                                    if (Subject.ENGLISH == subjectA) {
                                        return -1;
                                    } else if (Subject.MATH == subjectA) {
                                        return Subject.ENGLISH == subjectB ? 1 : -1;
                                    } else if (Subject.CHINESE == subjectA) {
                                        return 1;
                                    } else {
                                        return 0;
                                    }
                                }
                                return 0;
                            })
                            .collect(Collectors.toList());
                    sortedGroupIds.forEach((Long gid) -> {
                        if (CollectionUtils.isNotEmpty(groupStudentIds.get(gid))) {
                            Subject subject = groupIdSubjectMap.get(gid);
                            //取出已经布置的作业
                            VacationHomeworkPackage.Location location = null;
                            List<VacationHomeworkPackage.Location> locations = groupLocationMap.get(gid) != null ? groupLocationMap.get(gid)
                                    .stream()
                                    .filter(o -> o.getSubject().equals(subject))
                                    .collect(Collectors.toList()) : null;
                            if (CollectionUtils.isNotEmpty(locations)) {
                                location = locations.get(0);
                            }
                            //班级人数
                            List<Long> studentIds = groupStudentIds.get(gid);
                            //详情页面地址
                            String detailUrl = location != null ? UrlUtils.buildUrlQuery("/view/mobile/common/vacationreport/clazzreport",
                                    MapUtils.m(
                                            "packageId", location.getId())) : "";
                            if (location != null && location.getStartTime() < new Date().getTime()) {
                                //二十四小时内是否分享了家长端
                                ShareVacationReportCacheManager shareVacationReportCacheManager = newHomeworkCacheService.getShareVacationReportCacheManager();
                                String cacheKey = shareVacationReportCacheManager.getCacheKey(location.getId());
                                Integer cacheValue = shareVacationReportCacheManager.load(cacheKey);
                                boolean jztShare = cacheValue != null;
                                //二十四小时内是否分享了QQ或者微信
                                ShareWeiXinVacationReportCacheManager shareWeiXinVacationReportCacheManager = newHomeworkCacheService.getShareWeiXinVacationReportCacheManager();
                                cacheKey = shareWeiXinVacationReportCacheManager.getCacheKey(location.getId());
                                cacheValue = shareWeiXinVacationReportCacheManager.load(cacheKey);
                                boolean weiXinShare = cacheValue != null;
                                //二十四小时内是否提醒了学生
                                RemindStudentVacationProgressCacheManager remindStudentVacationProgressCacheManager = newHomeworkCacheService.getRemindStudentVacationProgressCacheManager();
                                cacheKey = remindStudentVacationProgressCacheManager.getCacheKey(location.getId());
                                cacheValue = remindStudentVacationProgressCacheManager.load(cacheKey);
                                boolean remindStudent = cacheValue != null;

                                Map<String, Object> m = MapUtils.m(
                                        "packageId", location.getId(),
                                        "jztTodayHasShare", jztShare,
                                        "weiXinTodayHasShare", weiXinShare,
                                        "remindStudent", remindStudent,
                                        "clazzName", c.formalizeClazzName(),
                                        "shareWeiXinQQUrl", domain + detailUrl,
                                        "subject", subject,
                                        "subjectName", subject.getValue()
                                );
                                sharePackageInfo.add(m);
                            }


                            //课本赋值
                            String bookId = null;
                            if (location != null) {
                                bookId = location.getBookId();
                            } else {
                                NewClazzBookRef newClazzBookRef = clazzBookRefMap.get(gid) != null ? clazzBookRefMap.get(gid)
                                        .stream()
                                        .filter(o -> StringUtils.equalsIgnoreCase(o.getSubject(), subject.name()))
                                        .sorted((o1, o2) -> Long.compare(o2.fetchUpdateTimestamp(), o1.fetchUpdateTimestamp()))
                                        .findFirst().orElse(null) : null;
                                if (newClazzBookRef != null) {
                                    bookId = newClazzBookRef.getBookId();
                                } else {
                                    School school = schoolLoaderClient.getSchoolLoader()
                                            .loadSchool(c.getSchoolId())
                                            .getUninterruptibly();
                                    if (school != null) {
                                        bookId = newContentLoaderClient.initializeClazzBook(subject, c.getClazzLevel(), school.getRegionCode());
                                    }
                                }
                            }

                            // 强制使用上册教材的地区特殊处理默认教材
                            if (mustUseLastTermBook) {
                                NewBookProfile book = newContentLoaderClient.loadBook(bookId);
                                if (book != null && Objects.equals(book.getTermType(), Term.下学期.getKey())) {
                                    List<NewBookProfile> books = newContentLoaderClient
                                            .loadBooksByClassLevelAndTermAndSeriesIdAndBookType(subject, ClazzLevel.parse(book.getClazzLevel()), Term.上学期, book.getSeriesId(), NewBookType.TEXTBOOK.name())
                                            .stream()
                                            .sorted((o1, o2) -> Integer.compare(o2.getLatestVersion(), o1.getLatestVersion()))
                                            .collect(Collectors.toList());
                                    if (CollectionUtils.isNotEmpty(books)) {
                                        bookId = books.get(0).getId();
                                    }
                                }
                            }

                            // 计算参与人数和完成人数
                            int beganCount = 0;
                            int finishedCount = 0;
                            List<VacationHomeworkCacheMapper> vacationHomeworkCacheMappers = vacationHomeworkCacheLoader.loadVacationHomeworkCacheMappers(gid);
                            if (CollectionUtils.isNotEmpty(vacationHomeworkCacheMappers)) {
                                beganCount = vacationHomeworkCacheMappers.size();
                                for (VacationHomeworkCacheMapper vacationHomeworkCacheMapper : vacationHomeworkCacheMappers) {
                                    if (vacationHomeworkCacheMapper.isFinished()) {
                                        finishedCount++;
                                    }
                                }
                            }

                            String status = location == null ? now.after(NewHomeworkConstants.VH_START_DATE_LATEST) ? "EXPIRED" : "NOT_ASSIGN" : "ASSIGNED";
                            int statusRank = 1;
                            switch (status) {
                                case "NOT_ASSIGN":
                                    statusRank = 2;
                                    break;
                                case "EXPIRED":
                                    statusRank = 3;
                                    break;
                                default:
                                    long currentTimeMillis = System.currentTimeMillis();
                                    if (currentTimeMillis < location.getStartTime()) {
                                        // 布置了，但是未到开始时间
                                        status = "NOT_BEGIN";
                                    } else if (currentTimeMillis > location.getEndTime()) {
                                        // 布置了，但是已经超过截止时间
                                        status = "TERMINATED";
                                    }
                                    break;
                            }

                            NewBookProfile bookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
                            Map<String, Object> m = MapUtils.m(
                                    "clazz", MapUtils.m("groupId", gid, "clazzId", c.getId(), "clazzName", c.formalizeClazzName(), "clazzLevel", c.getClazzLevel().getLevel()),
                                    "vacationHomeworkId", location != null ? location.getId() : null,
                                    "startTime", location != null ? location.getStartTime() : null,
                                    "closeTime", location != null ? location.getEndTime() : null,
                                    "studentCount", studentIds != null ? studentIds.size() : 0,
                                    "beganCount", beganCount,
                                    "finishedCount", finishedCount,
                                    "bookId", bookId,
                                    "bookName", bookProfile != null ? bookProfile.getName() : null,
                                    "status", status,
                                    "statusRank", statusRank,
                                    "detailUrl", detailUrl);
                            if (teacherIds.size() > 1) {
                                m.put("subject", subject);
                                m.put("subjectName", subject.getValue());
                            }
                            clazzMap.add(m);
                        }
                    });
                });
        clazzMap.sort(Comparator.comparingInt(c -> SafeConverter.toInt(c.get("statusRank"))));
        if (CollectionUtils.isEmpty(clazzMap)) {
            return MapMessage.errorMessage("无班级");
        } else {
            // 获取主学科
            Long teacherId = teacherIds.iterator().next();
            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
            if (mainTeacherId == null) {
                mainTeacherId = teacherId;
            }
            int lotteryNumber = campaignServiceClient.getCampaignService().getTeacherLotteryFreeChance(CampaignType.SUMMER_VOCATION_LOTTERY_2018, mainTeacherId);
            //暑假作业计划天数集合
            List<VacationHomeworkPlannedDaysType> plannedDaysTypes = Arrays.stream(VacationHomeworkPlannedDaysType.values()).filter(r -> !Objects.equals(r.getType(), 1)).collect(Collectors.toList());
            //返回假期作业的计划天数列表
            List<VacationHomeworkPlannedDaysMapper> plannedDaysMappers = new ArrayList<>();
            for (VacationHomeworkPlannedDaysType plannedDaysType : plannedDaysTypes) {
                VacationHomeworkPlannedDaysMapper plannedDaysMapper = new VacationHomeworkPlannedDaysMapper();
                if (plannedDaysType.getDays().equals(20)) {
                    plannedDaysMapper.setIsChoose(Boolean.TRUE);
                }
                plannedDaysMapper.setDays(plannedDaysType.getDays());
                plannedDaysMapper.setText(plannedDaysType.getText());
                plannedDaysMappers.add(plannedDaysMapper);
            }

            List<VacationHomeworkPlannedDaysMapper> resultPlannedDaysMappers = new ArrayList<>();
            for (VacationHomeworkPlannedDaysMapper plannedDaysMapper : plannedDaysMappers) {
                if (plannedDaysMapper.getDays().equals(20)) {
                    resultPlannedDaysMappers.add(plannedDaysMapper);
                    break;
                }
            }
            for (VacationHomeworkPlannedDaysMapper plannedDaysMapper : plannedDaysMappers) {
                if (plannedDaysMapper.getDays().equals(25)) {
                    resultPlannedDaysMappers.add(plannedDaysMapper);
                    break;
                }
            }
            for (VacationHomeworkPlannedDaysMapper plannedDaysMapper : plannedDaysMappers) {
                if (plannedDaysMapper.getDays().equals(30)) {
                    resultPlannedDaysMappers.add(plannedDaysMapper);
                    break;
                }
            }


            return MapMessage.successMessage()
                    .add("items", clazzMap)
                    .add("sharePackageInfo", sharePackageInfo)
                    .add("lotteryNumber", lotteryNumber)
                    .add("bannerUrl", BANNER_URL)
                    //.add("showLotteryEntrance", now.after(NewHomeworkConstants.VH_LOTTERY_ENTRANCE_ONLINE_DATE) && now.before(NewHomeworkConstants.VH_LOTTERY_ENTRANCE_OFFLINE_DATE))
                    .add("plannedDays", resultPlannedDaysMappers);
        }
    }

    /**
     * 通过bookId取布置假期作业周内容
     *
     * @param bookId
     * @return
     */
    @Override
    public MapMessage loadBookPlanInfo(String bookId) {
        NewBookProfile book = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        if (book == null) {
            return MapMessage.errorMessage("课本不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_BOOK_NOT_EXIST);
        }

        //初始化课本信息
        Subject subject = Subject.fromSubjectId(book.getSubjectId());
        Map<String, Object> bookMap = new HashMap<>();
        bookMap.put("bookId", bookId);
        bookMap.put("name", book.getName());
        bookMap.put("imgUrl", book.getImgUrl());
        bookMap.put("clazzLevel", book.getClazzLevel());
        bookMap.put("termType", book.getTermType());
        bookMap.put("latestVersion", book.getLatestVersion()); //当为1的时候表示最新课本
        bookMap.put("subjectId", book.getSubjectId());
        bookMap.put("seriesId", book.getSeriesId());

        //初始化周计划
        List<Map> weekPlans = new ArrayList<>();
        VacationHomeworkWinterPlanCacheMapper winterPlan = vacationHomeworkCacheLoader.loadVacationHomeworkWinterPlanCacheMapper(bookId);

        if (winterPlan != null) {
            if (MapUtils.isEmpty(winterPlan.getDayPlan()) || winterPlan.getDayPlan().size() != 30) {
                return MapMessage.errorMessage(book.getName() + "假期作业内容错误，请更换教材!");
            }
            for (WinterWeekPlan winterWeekPlan : winterPlan.getWeekPlan().values()) {
                String weekRank = SafeConverter.toString(winterWeekPlan.getWeekRank());
                List<String> unitNames = new ArrayList<>();
                winterWeekPlan.getUnit().forEach(e -> unitNames.add(e.getUnitName()));
                String title = "第" + NewHomeworkUtils.transferToChinese(SafeConverter.toString(winterWeekPlan.getWeekRank())) + "周";
                List<Map> dayPlans = new ArrayList<>();
                List<String> weekDays = winterPlan.getWeekPlanDays().get(weekRank);
                for (String dayRank : weekDays) {
                    String weekDayPanKey = StringUtils.join(Arrays.asList(weekRank, dayRank), "-");
                    WinterDayPlan winterDayPlan = winterPlan.getDayPlan().get(weekDayPanKey);
                    if (winterDayPlan == null) continue;
                    dayPlans.add(MiscUtils.m("desc", winterDayPlan.getDesc(),
                            "name", winterDayPlan.getName(),
                            "dayRank", winterDayPlan.getDayRank()));
                }
                weekPlans.add(MiscUtils.m("scope", winterWeekPlan.getName(),
                        "title", title,
                        "weekRank", winterWeekPlan.getWeekRank(),
                        "dayPlans", dayPlans));
            }
        }

        return MapMessage.successMessage()
                .add("book", bookMap)
                .add("weekPlans", weekPlans);

    }

    /**
     * 假期作业预览
     *
     * @param teacherDetail
     * @param bookId
     * @param weekRank
     * @param dayRank
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public MapMessage loadDayPlanElements(TeacherDetail teacherDetail, String bookId, Integer weekRank, Integer dayRank) {
        VacationHomeworkWinterPlanCacheMapper winterPlan = vacationHomeworkCacheLoader.loadVacationHomeworkWinterPlanCacheMapper(bookId);
        if (winterPlan == null) {
            return MapMessage.errorMessage("课本计划不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_WINTER_PLAN_NOT_EXIST);
        }
        String weekDayKey = StringUtils.join(Arrays.asList(weekRank, dayRank), "-");
        WinterDayPlan winterDayPlan = winterPlan.getDayPlan().get(weekDayKey);
        if (winterDayPlan == null) {
            return MapMessage.errorMessage("课本计划不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_WINTER_PLAN_NOT_EXIST);
        }

        String version = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_TEACHER.name(), "BASIC_APP_VIEW_H5_VERSION");
        if (StringUtils.isBlank(version)) {
            version = "V2_5_0";
        }

        List<Map<String, Object>> elements = winterDayPlan.getElements();
        List<Map<String, Object>> processedElements = new ArrayList<>();
        // 题型
        Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
        for (Map<String, Object> element : elements) {
            ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(SafeConverter.toString(element.get("objectiveConfigType")));
            if (objectiveConfigType != null) {
                element.put("name", objectiveConfigType.getValue());
                Map<String, Object> reading = (Map<String, Object>) element.get("reading");
                List<String> questionIds = (List<String>) element.get("questionIds");
                List<Map<String, Object>> practices = (List<Map<String, Object>>) element.get("practices");
                List<String> dubbingIds = (List<String>) element.get("dubbingIds");
                List<Map<String, Object>> packages = (List<Map<String, Object>>) element.get("packages");
                switch (objectiveConfigType) {
                    case BASIC_APP:
                        List<Map<String, Object>> basicAppPractices = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(practices)) {
                            for (Map<String, Object> practice : practices) {
                                String lessonId = SafeConverter.toString(practice.get("lessonId"));
                                List<Integer> categoryIds = (List<Integer>) practice.get("category_ids");
                                if (StringUtils.isNotBlank(lessonId) && CollectionUtils.isNotEmpty(categoryIds)) {
                                    Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(Collections.singleton(lessonId));
                                    Map<String, List<Sentence>> lessonSentenceMap = newEnglishContentLoaderClient.loadEnglishLessonSentences(Collections.singleton(lessonId));
                                    Map<String, Object> lessonCategoryMap = newHomeworkBasicAppContentLoader
                                            .buildLessonCategoryMap(bookId, null, lessonId, new LinkedHashSet<>(categoryIds), lessonMap, lessonSentenceMap, null);
                                    if (MapUtils.isNotEmpty(lessonCategoryMap)) {
                                        basicAppPractices.add(lessonCategoryMap);
                                    }
                                }
                            }
                        }
                        if (CollectionUtils.isNotEmpty(basicAppPractices)) {
                            element.put("practices", basicAppPractices);
                            element.put("selfViewUrl", "/flash/loader/newselfstudymobile.vpage");
                            element.put("version", version);
                            processedElements.add(element);
                        }
                        break;
                    case NATURAL_SPELLING:
                        List<Map<String, Object>> naturalSpellingPractices = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(practices)) {
                            naturalSpellingPractices = newHomeworkNaturalSpellingContentLoader.processContent(practices, teacherDetail, null, bookId);
                        }
                        if (CollectionUtils.isNotEmpty(naturalSpellingPractices)) {
                            element.put("practices", naturalSpellingPractices);
                            processedElements.add(element);
                        }
                        break;
                    case DUBBING:
                    case DUBBING_WITH_SCORE:
                        List<Map<String, Object>> dubbingPractices = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(dubbingIds)) {
                            List<Dubbing> dubbingList = new ArrayList<>(dubbingLoaderClient.loadDubbingByDocIds(dubbingIds).values());
                            Map<String, String> dubbingThemeMap = dubbingLoaderClient.loadAllDubbingThemes()
                                    .stream()
                                    .collect(toMap(DubbingTheme::getId, DubbingTheme::getName));
                            if (CollectionUtils.isNotEmpty(dubbingList)) {
                                Set<String> albumIds = dubbingList.stream().map(Dubbing::getCategoryId).collect(Collectors.toSet());
                                Map<String, DubbingCategory> albums = dubbingLoaderClient.loadDubbingCategoriesByIds(albumIds);
                                EmbedBook book = new EmbedBook();
                                book.setBookId(bookId);
                                dubbingPractices = dubbingList
                                        .stream()
                                        .map(dubbing -> NewHomeworkContentDecorator.decorateDubbing(dubbing, albums.get(dubbing.getCategoryId()), null, book, null, objectiveConfigType, dubbingThemeMap))
                                        .collect(Collectors.toList());
                            }
                        }
                        if (CollectionUtils.isNotEmpty(dubbingPractices)) {
                            element.put("practices", dubbingPractices);
                            processedElements.add(element);
                        }
                        break;
                    case READING:
                        List<Map<String, Object>> readingPractices = new ArrayList<>();
                        if (MapUtils.isNotEmpty(reading)) {
                            String readingId = SafeConverter.toString(reading.get("id"));
                            PictureBook pictureBook = pictureBookLoaderClient.loadPictureBookByDocIds(Collections.singleton(readingId)).get(readingId);
                            if (pictureBook != null) {
                                Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookLoaderClient.loadAllPictureBookSeries()
                                        .stream()
                                        .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));
                                Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookLoaderClient.loadAllPictureBookTopics()
                                        .stream()
                                        .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));
                                EmbedBook book = new EmbedBook();
                                book.setBookId(bookId);
                                Map<String, Object> pictureBookMapper = NewHomeworkContentDecorator.decoratePictureBook(pictureBook, pictureBookSeriesMap, pictureBookTopicMap, book, null);
                                readingPractices.add(pictureBookMapper);
                            }
                        }
                        if (CollectionUtils.isNotEmpty(readingPractices)) {
                            element.put("practices", readingPractices);
                            element.put("selfViewUrl", "/flash/loader/newselfstudymobile.vpage");
                            element.put("version", version);
                            processedElements.add(element);
                        }
                        break;
                    case MENTAL:
                        if (CollectionUtils.isNotEmpty(questionIds)) {
                            List<NewQuestion> questions = questionLoaderClient.loadQuestionByDocIds(questionIds);
                            List<String> questionContents = questions.stream()
                                    .filter(q -> q.getContent() != null)
                                    .filter(q -> CollectionUtils.isNotEmpty(q.getContent().getSubContents()))
                                    .map(q -> q.getContent().getSubContents().get(0).getContent())
                                    .collect(Collectors.toList());
                            element.put("questions", questionContents);
                            processedElements.add(element);
                        }
                        break;
                    case MENTAL_ARITHMETIC:
                        if (CollectionUtils.isNotEmpty(questionIds)) {
                            List<NewQuestion> questions = questionLoaderClient.loadQuestionByDocIds(questionIds);
                            questionIds = questions.stream()
                                    .map(NewQuestion::getId)
                                    .collect(Collectors.toList());
                            element.put("questionIds", questionIds);
                            processedElements.add(element);
                        }
                        break;
                    case EXAM:
                    case INTELLIGENCE_EXAM:
                        if (CollectionUtils.isNotEmpty(questionIds)) {
                            List<NewQuestion> questions = questionLoaderClient.loadQuestionByDocIds(questionIds);
                            List<Map<String, Object>> questionMapperList = questions
                                    .stream()
                                    .map(question -> MapUtils.m(
                                            "id", question.getId(),
                                            "questionType", contentTypeMap.get(question.getContentTypeId()) != null ? contentTypeMap.get(question.getContentTypeId()).getName() : "无题型",
                                            "difficultyName", QuestionConstants.newDifficultyMap.get(question.getDifficultyInt())
                                    ))
                                    .collect(Collectors.toList());
                            element.put("questions", questionMapperList);
                            processedElements.add(element);
                        }
                        break;
                    case INTERESTING_PICTURE:
                        if (MapUtils.isNotEmpty(reading)) {
                            questionIds = (List<String>) reading.get("questionIds");
                            if (CollectionUtils.isNotEmpty(questionIds)) {
                                List<NewQuestion> questions = questionLoaderClient.loadQuestionByDocIds(questionIds);
                                List<Map<String, Object>> questionMapperList = questions.stream()
                                        .map(question -> MapUtils.m(
                                                "id", question.getId(),
                                                "questionType", contentTypeMap.get(question.getContentTypeId()) != null ? contentTypeMap.get(question.getContentTypeId()).getName() : "无题型",
                                                "difficultyName", QuestionConstants.newDifficultyMap.get(question.getDifficultyInt())
                                        ))
                                        .collect(Collectors.toList());
                                reading.put("questions", questionMapperList);
                                processedElements.add(element);
                            }
                        }
                        break;
                    case BASIC_KNOWLEDGE:
                    case CHINESE_READING:
                        if (CollectionUtils.isNotEmpty(questionIds)) {
                            List<NewQuestion> questions = questionLoaderClient.loadQuestionByDocIds(questionIds);
                            List<Map<String, Object>> questionMapperList = questions.stream()
                                    .map(question -> MapUtils.m(
                                            "id", question.getId(),
                                            "questionType", contentTypeMap.get(question.getContentTypeId()) != null ? contentTypeMap.get(question.getContentTypeId()).getName() : "无题型",
                                            "difficultyName", QuestionConstants.newDifficultyMap.get(question.getDifficultyInt())
                                    ))
                                    .collect(Collectors.toList());
                            if (CollectionUtils.isNotEmpty(questionMapperList)) {
                                element.put("questions", questionMapperList);
                                processedElements.add(element);
                            }
                        }
                        break;
                    case NEW_READ_RECITE:
                    case READ_RECITE_WITH_SCORE:
                        if (CollectionUtils.isNotEmpty(packages)) {
                            List<Map<String, Object>> packageMapperList = new ArrayList<>();
                            for (Map<String, Object> questionPackage : packages) {
                                String questionBoxId = SafeConverter.toString(questionPackage.get("id"));
                                questionIds = (List<String>) questionPackage.get("questionIds");
                                List<NewQuestion> questions = questionLoaderClient.loadQuestionByDocIds(questionIds);
                                if (StringUtils.isNotEmpty(questionBoxId) && CollectionUtils.isNotEmpty(questions)) {
                                    String packageName = "";
                                    NewQuestion firstQuestion = questions.iterator().next();
                                    int contentTypeId = SafeConverter.toInt(firstQuestion.getContentTypeId());
                                    if (contentTypeId == 1010014) {
                                        packageName = "课文朗读";
                                    } else if (contentTypeId == 1010015) {
                                        packageName = "课文背诵";
                                    }
                                    if (StringUtils.isNotEmpty(packageName)) {
                                        Map<String, List<Long>> qidSentenceIdsMap = questions
                                                .stream()
                                                .collect(Collectors.toMap(NewQuestion::getDocId, NewQuestion::getSentenceIds));

                                        List<Long> chineseSentenceIds = questions
                                                .stream()
                                                .filter(question -> CollectionUtils.isNotEmpty(question.getSentenceIds()))
                                                .map(NewQuestion::getSentenceIds)
                                                .flatMap(Collection::stream)
                                                .collect(Collectors.toList());
                                        List<ChineseSentence> chineseSentences = chineseContentLoaderClient.loadChineseSentenceByIds(chineseSentenceIds);
                                        //句子所在的章节号
                                        Map<Long, Integer> sentenceIdSection = chineseSentences
                                                .stream()
                                                .collect(Collectors.toMap(ChineseSentence::getId, ChineseSentence::getParagraphContinuous));
                                        //句子所在章节的段落号
                                        Map<Long, Integer> sentenceIdParagraph = chineseSentences
                                                .stream()
                                                .collect(Collectors.toMap(ChineseSentence::getId, ChineseSentence::getParagraph));
                                        //题的章节号
                                        Map<String, Integer> qidSectionMap = new HashMap<>();
                                        //题的段落号
                                        Map<String, Integer> qidParagraphMap = new HashMap<>();

                                        if (MapUtils.isNotEmpty(qidSentenceIdsMap)) {
                                            for (Map.Entry<String, List<Long>> entry : qidSentenceIdsMap.entrySet()) {
                                                String questionDocId = entry.getKey();
                                                List<Long> sentenceIds = entry.getValue();
                                                Long sentenceId = 0L;
                                                if (CollectionUtils.isNotEmpty(sentenceIds)) {
                                                    sentenceId = sentenceIds.iterator().next();
                                                }
                                                qidSectionMap.put(questionDocId, sentenceIdSection.get(sentenceId));
                                                qidParagraphMap.put(questionDocId, sentenceIdParagraph.get(sentenceId));
                                            }
                                        }

                                        Map<Long, ChineseSentence> mapChineseSentences = chineseSentences
                                                .stream()
                                                .collect(Collectors.toMap(ChineseSentence::getId, o -> o));

                                        //重点句子id
                                        Set<Long> keyPointSentenceIds = new HashSet<>();
                                        for (Map.Entry<Long, ChineseSentence> entry : mapChineseSentences.entrySet()) {
                                            ChineseSentence chineseSentence = entry.getValue();
                                            if (chineseSentence != null) {
                                                if (SafeConverter.toBoolean(chineseSentence.getReciteParagraph())) {
                                                    keyPointSentenceIds.add(entry.getKey());
                                                }
                                            }
                                        }

                                        //计算重点段落
                                        Map<String, Boolean> qidKeyPointMap = new HashMap<>();
                                        if (MapUtils.isNotEmpty(qidSentenceIdsMap)) {
                                            for (Map.Entry<String, List<Long>> entry : qidSentenceIdsMap.entrySet()) {
                                                String qid = entry.getKey();
                                                List<Long> sentenceIds = entry.getValue();
                                                boolean tag = true;
                                                if (CollectionUtils.isNotEmpty(sentenceIds)) {
                                                    for (Long sentenceId : sentenceIds) {
                                                        if (!keyPointSentenceIds.contains(sentenceId)) {
                                                            tag = false;
                                                            break;
                                                        }
                                                    }
                                                    qidKeyPointMap.put(qid, tag);
                                                }
                                            }
                                        }

                                        Comparator<Map<String, Object>> comparator = Comparator.comparingInt(a -> SafeConverter.toInt(a.get("sectionNumber")));
                                        comparator = comparator.thenComparingInt(a -> SafeConverter.toInt(a.get("paragraphNumber")));
                                        List<Map<String, Object>> questionMapperList = questions.stream()
                                                .map(newQuestion -> {
                                                    Map<String, Object> question = NewHomeworkContentDecorator.decorateNewQuestion(newQuestion, contentTypeMap,
                                                            Collections.emptyMap(), null, null);
                                                    question.put("sectionNumber", qidSectionMap.get(newQuestion.getDocId()));
                                                    question.put("paragraphNumber", qidParagraphMap.get(newQuestion.getDocId()));
                                                    question.put("paragraphImportant", qidKeyPointMap.get(newQuestion.getDocId()));
                                                    List<String> listenUrls = new ArrayList<>();
                                                    List<NewQuestionOralDictOptions> options = newQuestion.getContent().getSubContents().get(0).getOralDict().getOptions();
                                                    if (CollectionUtils.isNotEmpty(options)) {
                                                        options.stream()
                                                                .filter(e -> StringUtils.isNotBlank(e.getListenUrl()))
                                                                .forEach(e -> listenUrls.add(e.getListenUrl()));
                                                    }
                                                    question.put("listenUrls", listenUrls);
                                                    return question;
                                                })
                                                .sorted(comparator)
                                                .collect(Collectors.toList());
                                        Map<String, Object> packageMapper = MapUtils.m(
                                                "questionBoxId", questionBoxId,
                                                "questionBoxName", packageName,
                                                "questionNum", questions.size(),
                                                "questions", questionMapperList
                                        );
                                        packageMapperList.add(packageMapper);
                                    }
                                }
                            }
                            if (CollectionUtils.isNotEmpty(packageMapperList)) {
                                element.put("packages", packageMapperList);
                                processedElements.add(element);
                            }
                        }
                        break;
                    case LEVEL_READINGS:
                        List<Map<String, Object>> levelReadingsPractices = new ArrayList<>();
                        if (MapUtils.isNotEmpty(reading)) {
                            String id = SafeConverter.toString(reading.get("id"));
                            PictureBookPlus pictureBookPlus = pictureBookPlusServiceClient.loadByIds(Collections.singleton(id)).get(id);
                            if (pictureBookPlus != null) {
                                Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookLoaderClient.loadAllPictureBookSeries()
                                        .stream()
                                        .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));
                                Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookLoaderClient.loadAllPictureBookTopics()
                                        .stream()
                                        .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));
                                Map<String, Object> pictureBookPlusMapper = NewHomeworkContentDecorator.decoratePictureBookPlus(pictureBookPlus, pictureBookSeriesMap, pictureBookTopicMap, null, null, null, null);
                                levelReadingsPractices.add(pictureBookPlusMapper);
                            }
                        }
                        if (CollectionUtils.isNotEmpty(levelReadingsPractices)) {
                            element.put("practices", levelReadingsPractices);
                            processedElements.add(element);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
//        // 处理趣味关卡
//        if (StringUtils.isNotEmpty(winterDayPlan.getInterestingPictureUrl())) {
//            elements.add(
//                    MapUtils.m(
//                            "objectiveConfigType", "MATH_INTERESTING_PICTURE",
//                            "name", "数学思维拓展",
//                            "interestingPictureName", winterDayPlan.getInterestingPictureName(),
//                            "interestingPictureUrl", winterDayPlan.getInterestingPictureUrl())
//            );
//        }
        return MapMessage.successMessage().add("dayPlanElements", processedElements);
    }

    /**
     * 假期作业任务包
     *
     * @param packageId
     * @param studentId
     * @return
     */
    @Override
    public MapMessage loadStudentDayPackages(String packageId, Long studentId) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        VacationHomeworkPackage vacationHomeworkPackage = vacationHomeworkPackageDao.load(packageId);
        if (vacationHomeworkPackage == null) {
            return MapMessage.errorMessage("vacationHomeworkPackage is null").setErrorCode(ErrorCodeConstants.ERROR_CODE_VACATION_HOMEWORK_PACKAGE_NOT_EXIST);
        }
        if (vacationHomeworkPackage.isDisabledTrue()) {
            return MapMessage.errorMessage("假期作业已删除").setErrorCode(ErrorCodeConstants.ERROR_CODE_VACATION_HOMEWORK_PACKAGE_NOT_EXIST);
        }
        VacationHomeworkWinterPlanCacheMapper winterPlan = vacationHomeworkCacheLoader.loadVacationHomeworkWinterPlanCacheMapper(vacationHomeworkPackage.getBookId());
        if (winterPlan == null) {
            return MapMessage.errorMessage("课本计划不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_WINTER_PLAN_NOT_EXIST);
        }

        VacationHomeworkCacheMapper vacationHomeworkCacheMapper = vacationHomeworkCacheLoader.loadVacationHomeworkCacheMapper(vacationHomeworkPackage.getClazzGroupId(), studentId);
        LinkedHashMap<String, VacationHomeworkDetailCacheMapper> homeworkDetail = new LinkedHashMap<>();
        List<String> finishDays = new ArrayList<>();

        if (vacationHomeworkCacheMapper != null && vacationHomeworkCacheMapper.getHomeworkDetail() != null) {
            homeworkDetail = vacationHomeworkCacheMapper.getHomeworkDetail();
            for (VacationHomeworkDetailCacheMapper mapper : homeworkDetail.values()) {
                if (mapper.getFinishAt() != null) {
                    finishDays.add(DateUtils.dateToString(mapper.getFinishAt(), DateUtils.FORMAT_SQL_DATE));
                }
            }
        }

        LinkedHashMap<String, WinterDayPlan> weekDayRank = winterPlan.getDayPlan();
        int finishedCount = 0;
        int index = 0;
        Date currentDate = new Date();
        List<Map<String, Object>> dayPackages = new ArrayList<>();

        // 是否是白名单
        boolean isWhiteList = homeworkBlackWhiteListDao.isBlackWhiteList(Arrays.asList(
                HomeworkBlackWhiteList.generateId("VACATION_DAY_PACKAGE", "STUDENT_ID", String.valueOf(studentId)),
                HomeworkBlackWhiteList.generateId("VACATION_DAY_PACKAGE", "GROUP_ID", String.valueOf(vacationHomeworkPackage.getClazzGroupId()))
        ));

        /**
         * 作业包开放规则
         * 每天最多一个任务包，例如今天做了一半，明天完成后一半后就不能再做下一个任务了要等后天做
         * 自2017-02-01 00:00起，全部假期作业打开，不再受每天一个任务包限制（ps：即使不受每天一个限制，也是作完一个后，下一个包才能变成开启状态，不能跳！）
         */
        for (String weekDayKey : weekDayRank.keySet()) {
            String[] strs = weekDayKey.split("-");
            if (strs.length == 2) {
                Long dayRank = SafeConverter.toLong(strs[1]);
                // 根据老师选择的计划天数，进行过滤
                if (dayRank <= vacationHomeworkPackage.getPlannedDays()) {
                    WinterDayPlan winterDayPlan = weekDayRank.get(weekDayKey);
                    if (winterDayPlan == null) {
                        continue;
                    }
                    // 作业ID
                    String detailKey = StringUtils.join(Arrays.asList(packageId, weekDayKey, studentId), "-");
                    VacationHomeworkDetailCacheMapper detailCacheMapper = homeworkDetail.get(detailKey);
                    boolean locked = true;
                    boolean finished = false;
                    if ((index == 0 && currentDate.after(vacationHomeworkPackage.getStartTime())) || (detailCacheMapper != null && detailCacheMapper.isFinished())) {
                        locked = false;
                        if (detailCacheMapper != null && detailCacheMapper.isFinished()) {
                            finished = true;
                            finishedCount++;
                        }
                    }
                    /**
                     * 1、当前作业包不是第一个作业包
                     * 2、当前包前面的包的个数和完成包的个数相等
                     * 3、当前包还是锁着的
                     * 4、当天没有完成过包 或者 当前时间在全部假期作业放开时间之后 或者 是白名单用户(两种配置) 或者 当前时间在前15个包开放时间之后且是前15个包
                     * 满足上面四个条件则这个包被打开
                     */
                    if (index > 0 && index == finishedCount && locked &&
                            (!finishDays.contains(DateUtils.dateToString(currentDate, DateUtils.FORMAT_SQL_DATE))
                                    || grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "VacationHW", "WhiteList") || isWhiteList
                                    || (currentDate.after(NewHomeworkConstants.VH_OPEN_FIFTEEN_LIMIT_DATE) && index < 15))) {
                        locked = false;
                    }

                    //if (index > 0 && index == finishedCount && locked &&
                    //        (!finishDays.contains(DateUtils.dateToString(currentDate, DateUtils.FORMAT_SQL_DATE))
                    //                || currentDate.after(NewHomeworkConstants.VH_OPEN_LIMIT_DATE)
                    //                || grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "VacationHW", "WhiteList")
                    //                || (currentDate.after(NewHomeworkConstants.VH_OPEN_HALF_LIMIT_DATE) && index < 10))) {
                    //    locked = false;
                    //}

                    Map<String, Object> packageMap = MiscUtils.m(
                            "packageName", "Day" + winterDayPlan.getDayRank(),
                            "integral", NewHomeworkConstants.FINISH_VACATION_HOMEWORK_INTEGRAL_REWARD,
                            "homeworkId", StringUtils.join(Arrays.asList(packageId, weekDayKey, studentId), "-"),
                            "locked", locked,
                            "finished", finished);
                    dayPackages.add(packageMap);
                    index++;
                }
            }
        }

        //初始化关卡状态
        Map<String, Object> levelState = levelState(studentId, packageId, finishedCount);

        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentId);
        return MapMessage.successMessage()
                .add("levelState", levelState)
                .add("subject", vacationHomeworkPackage.getSubject())
                .add("endDate", NewHomeworkConstants.VH_END_DATE_LATEST.getTime())
                .add("integral", NewHomeworkConstants.ASSIGN_VACATION_HOMEWORK_INTEGRAL_REWARD / 10)
                .add("dayPackages", dayPackages)
                .add("isInPaymentBlackListRegion", studentDetail.isInPaymentBlackListRegion())
                .add("newProcess", grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "StudentHomework", "NewIndexUrl"))
                .add("fairylandClosed", studentExtAttribute != null && (studentExtAttribute.fairylandClosed() || studentExtAttribute.vapClosed()));

    }

    /**
     * 假期作业:初始化关卡状态
     *
     * @param studentId
     * @param packageId
     * @param finishedCount
     * @return
     */
    @Override
    public Map<String, Object> levelState(Long studentId, String packageId, Integer finishedCount) {
        VacationHomeworkPackage vacationHomeworkPackage = vacationHomeworkPackageDao.load(packageId);
        if (vacationHomeworkPackage == null) {
            return null;
        }
        int planDays = SafeConverter.toInt(vacationHomeworkPackage.getPlannedDays(), 30);
        int level1EndDay = planDays == 30 ? 15 : 10;

        boolean level1Finished = finishedCount >= level1EndDay;
        boolean level1Received = newHomeworkCacheService.vacationHomeworkIntegralCacheManager_studentRewarded(studentId, packageId, VacationHomeworkLevel.LEVEL_1.getLevel());

        boolean level2Finished = finishedCount >= planDays;
        boolean level2Received = newHomeworkCacheService.vacationHomeworkIntegralCacheManager_studentRewarded(studentId, packageId, VacationHomeworkLevel.LEVEL_2.getLevel());

        String levelDescription = "完成{}-{}天作业，可获取{}学豆";
        return MapUtils.m(
                "level1", MapUtils.m("finished", level1Finished, "received", level1Received, "description", StringUtils.formatMessage(levelDescription, 1, level1EndDay, VacationHomeworkLevel.LEVEL_1.getStudentIntegral()), "studentIntegral", VacationHomeworkLevel.LEVEL_1.getStudentIntegral(), "days", level1EndDay),
                "level2", MapUtils.m("finished", level2Finished, "received", level2Received, "description", StringUtils.formatMessage(levelDescription, level1EndDay + 1, planDays, VacationHomeworkLevel.LEVEL_2.getStudentIntegral()), "studentIntegral", VacationHomeworkLevel.LEVEL_2.getStudentIntegral(), "days", planDays - level1EndDay)
        );
    }

    @Override
    public VacationHomework loadVacationHomeworkIncludeDisabled(String id) {
        return vacationHomeworkDao.load(id);
    }

    @Override
    public Map<String, VacationHomework> loadVacationHomeworksIncludeDisabled(Collection<String> ids) {
        return vacationHomeworkDao.loads(ids);
    }

    @Override
    public VacationHomework loadVacationHomeworkById(String id) {
        return vacationHomeworkDao.load(id);
    }

    @Override
    public Map<String, Object> indexData(String homeworkId, Long studentId) {
        return doVacationHomeworkProcessor.index(homeworkId, studentId);
    }

    @Override
    public Map<String, Object> loadQuestionAnswer(ObjectiveConfigType objectiveConfigType, String homeworkId, Integer categoryId, String lessonId, String videoId, String questionBoxId) {
        return doVacationHomeworkProcessor.questionAnswer(objectiveConfigType, homeworkId, categoryId, lessonId, videoId, questionBoxId);
    }

    @Override
    public Map<String, Object> loadHomeworkQuestions(String homeworkId, ObjectiveConfigType objectiveConfigType, Integer categoryId, String lessonId, String videoId, String questionBoxId) {
        return doVacationHomeworkProcessor.loadHomeworkQuestions(homeworkId, objectiveConfigType, categoryId, lessonId, videoId, questionBoxId);
    }

    @Override
    public VacationHomeworkPackage loadVacationHomeworkPackageById(String packageId) {
        return vacationHomeworkPackageDao.load(packageId);
    }
}
