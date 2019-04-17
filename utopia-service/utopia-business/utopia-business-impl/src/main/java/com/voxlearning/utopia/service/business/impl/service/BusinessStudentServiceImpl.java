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

package com.voxlearning.utopia.service.business.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.business.api.BusinessStudentService;
import com.voxlearning.utopia.business.api.entity.BizStudentVoice;
import com.voxlearning.utopia.entity.activity.StudentLuckyBagRecord;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorReportStudentFeedback;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.business.api.entity.ExamAnswer;
import com.voxlearning.utopia.service.business.api.entity.ExamAnswerRecord;
import com.voxlearning.utopia.service.business.impl.dao.StudentLuckyBagRecordPersistence;
import com.voxlearning.utopia.service.business.impl.service.student.StudentAppInteractiveInfoService;
import com.voxlearning.utopia.service.business.impl.service.student.StudentParentRewardService;
import com.voxlearning.utopia.service.business.impl.service.student.StudentVoiceService;
import com.voxlearning.utopia.service.business.impl.service.student.buffer.UserLoaderBuffer;
import com.voxlearning.utopia.service.business.impl.service.student.internal.StudentIndexDataContext;
import com.voxlearning.utopia.service.business.impl.service.student.internal.StudentIndexDataLoader;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.StudentAppIndexDataContext;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.StudentAppIndexDataLoader;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.StudentIndexDataLoaderForSpg;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.campaign.client.MissionServiceClient;
import com.voxlearning.utopia.service.campaign.client.MothersDayServiceClient;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.user.api.constants.UserBehaviorType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeType;
import com.voxlearning.utopia.temp.LuckyBagActivity;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.Subject.*;

@Named
@Service(interfaceClass = BusinessStudentService.class)
@ExposeService(interfaceClass = BusinessStudentService.class)
public class BusinessStudentServiceImpl extends BusinessServiceSpringBean implements BusinessStudentService {

    @Inject private MissionServiceClient missionServiceClient;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private StudentAppIndexDataLoader studentAppIndexDataLoader;
    @Inject private StudentAppInteractiveInfoService studentAppInteractiveInfoService;
    @Inject private StudentIndexDataLoader studentIndexDataLoader;
    @Inject private StudentIndexDataLoaderForSpg studentIndexDataLoaderForSpg;
    @Inject private StudentLuckyBagRecordPersistence studentLuckyBagRecordPersistence;
    @Inject private StudentParentRewardService studentParentRewardService;
    @Inject private StudentVoiceService studentVoiceService;

    @Inject private AmbassadorServiceClient ambassadorServiceClient;
    @Inject private FlowerServiceClient flowerServiceClient;
    @Inject private MothersDayServiceClient mothersDayServiceClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    @Override
    public MapMessage loadStudentIndexData(StudentDetail student) {
        if (student == null) {
            return MapMessage.errorMessage();
        }

        StudentIndexDataContext context = new StudentIndexDataContext(student);
        context.__userLoaderBuffer = new UserLoaderBuffer(student, userLoaderClient);

        Map<String, Object> indexData = studentIndexDataLoader.process(context);
        return MapMessage.successMessage().add("indexData", indexData);
    }

    @Override
    public Map<String, Object> loadStudentAppIndexData(StudentDetail student, String ver, String sys) {
        if (student == null) {
            return Collections.emptyMap();
        }
        return studentAppIndexDataLoader.process(new StudentAppIndexDataContext(student, ver, sys));
    }

    @Override
    public Map<String, Object> loadStudentIndexDataForSpg(StudentDetail student) {
        if (student == null) {
            return Collections.emptyMap();
        }
        return studentIndexDataLoaderForSpg.process(new StudentAppIndexDataContext(student, "", ""));
    }

    @Override
    public List<Map<String, Object>> getStudentSelfStudyDefaultBooks(StudentDetail student) {

        if (student.getClazz() == null || ClazzLevel.PRIMARY_GRADUATED.equals(student.getClazzLevel()) || ClazzLevel.MIDDLE_GRADUATED.equals(student.getClazzLevel()) || student.isJuniorStudent()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new LinkedList<>();

        List<GroupMapper> groups = groupLoaderClient.loadStudentGroups(student.getId(), false);
        GroupMapper englishGroup = groups.stream().filter(g -> g.getSubject() == Subject.ENGLISH).findFirst().orElse(null);

        NewBookProfile englishBookProfile = clazzBookLoaderClient.loadDefaultEnglishBook(student, englishGroup, null);
        //是否有点读机资源
//        boolean isPicListen = questionLoaderClient.hasPicListenResource(englishBook.getId());
        Map<String, Object> englishBookMap = new HashMap<>();
        if (englishBookProfile != null) {
            englishBookMap.put("bookId", englishBookProfile.getOldId());
            englishBookMap.put("isPicListen", false);
            englishBookMap.put("bookName", englishBookProfile.getName());
            englishBookMap.put("classLevel", englishBookProfile.getClazzLevel());
            englishBookMap.put("bookSubject", ENGLISH);
            englishBookMap.put("bookImg", StringUtils.contains(englishBookProfile.getImgUrl(), "catalog_new") ? englishBookProfile.getImgUrl() : StringUtils.replace(englishBookProfile.getImgUrl(), "catalog", "catalog_new"));
            englishBookMap.put("latestVersion", englishBookProfile.getLatestVersion() == 1);
            NewBookCatalog bookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(englishBookProfile.getSeriesId());

            BookPress bookPress = BookPress.getBySubjectAndPress(Subject.fromSubjectId(englishBookProfile.getSubjectId()), bookCatalog.getName());
            if (bookPress != null) {
                englishBookMap.put("viewContent", bookPress.getViewContent());
                englishBookMap.put("color", bookPress.getColor());
            }
            result.add(englishBookMap);
        }

        GroupMapper mathGroup = groups.stream().filter(g -> g.getSubject() == Subject.MATH).findFirst().orElse(null);
        NewBookProfile mathBook = clazzBookLoaderClient.loadDefaultMathBook(student, mathGroup);
        Map<String, Object> mathBookMap = new HashMap<>();
        if (mathBook != null) {
            mathBookMap.put("bookId", mathBook.getOldId());
            mathBookMap.put("isPicListen", false);
            mathBookMap.put("bookName", mathBook.getName());
            mathBookMap.put("classLevel", mathBook.getClazzLevel());
            mathBookMap.put("bookSubject", MATH);
            mathBookMap.put("bookImg", StringUtils.contains(mathBook.getImgUrl(), "catalog_new") ? mathBook.getImgUrl() : StringUtils.replace(mathBook.getImgUrl(), "catalog", "catalog_new"));
            mathBookMap.put("latestVersion", mathBook.getLatestVersion() == 1);
            NewBookCatalog mathBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(mathBook.getSeriesId());
            BookPress bookPress = BookPress.getBySubjectAndPress(Subject.MATH, mathBookCatalog.getName());
            if (bookPress != null) {
                mathBookMap.put("viewContent", bookPress.getViewContent());
                mathBookMap.put("color", bookPress.getColor());
            }
            result.add(mathBookMap);
        }

        GroupMapper chineseGroup = groups.stream().filter(g -> g.getSubject() == Subject.CHINESE).findFirst().orElse(null);
        NewBookProfile chineseBook = clazzBookLoaderClient.loadDefaultChineseBook(student, chineseGroup);
        if (chineseBook != null) {
            Map<String, Object> chineseBookMap = new HashMap<>();
            chineseBookMap.put("bookId", chineseBook.getOldId());
            chineseBookMap.put("isPicListen", false);
            chineseBookMap.put("bookName", chineseBook.getName());
            chineseBookMap.put("classLevel", chineseBook.getClazzLevel());
            chineseBookMap.put("bookSubject", CHINESE);
            chineseBookMap.put("bookImg", StringUtils.contains(chineseBook.getImgUrl(), "catalog_new") ? chineseBook.getImgUrl() : StringUtils.replace(chineseBook.getImgUrl(), "catalog", "catalog_new"));
            chineseBookMap.put("latestVersion", chineseBook.getLatestVersion() == 1);
            NewBookCatalog chineseBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(chineseBook.getSeriesId());
            BookPress bookPress = BookPress.getBySubjectAndPress(Subject.CHINESE, chineseBookCatalog.getName());
            if (bookPress != null) {
                chineseBookMap.put("viewContent", bookPress.getViewContent());
                chineseBookMap.put("color", bookPress.getColor());
            }
            result.add(chineseBookMap);
        }

        return result;
    }

    @Override
    public Map<String, Object> getEnglishSelfStudyBook(Long bookId) {
        NewBookProfile englishBookProfile = newContentLoaderClient.loadNewBookProfileByOldId(Subject.ENGLISH, bookId);

        if (englishBookProfile == null)
            return null;

        Map<String, Object> englishBookMap = new HashMap<>();
        englishBookMap.put("bookId", englishBookProfile.getOldId());
        englishBookMap.put("isPicListen", false);
        englishBookMap.put("bookName", englishBookProfile.getName());
        englishBookMap.put("classLevel", englishBookProfile.getClazzLevel());
        englishBookMap.put("bookSubject", ENGLISH);
        englishBookMap.put("bookImg", StringUtils.contains(englishBookProfile.getImgUrl(), "catalog_new") ? englishBookProfile.getImgUrl() : StringUtils.replace(englishBookProfile.getImgUrl(), "catalog", "catalog_new"));
        englishBookMap.put("latestVersion", englishBookProfile.getLatestVersion() == 1);
        NewBookCatalog bookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(englishBookProfile.getSeriesId());

        BookPress bookPress = BookPress.getBySubjectAndPress(Subject.fromSubjectId(englishBookProfile.getSubjectId()), bookCatalog.getName());
        if (bookPress != null) {
            englishBookMap.put("viewContent", bookPress.getViewContent());
            englishBookMap.put("color", bookPress.getColor());
        }

        return englishBookMap;
    }

    // FIXME 现在不支持按班级ID换班了
    @Override
    public MapMessage joinClazz_findClazzInfo(Long id, Set<Ktwelve> allowedKtwelve) {
        // 读取老师信息
        Teacher teacher = teacherLoaderClient.loadTeacher(id);
        if (teacher == null || teacher.isDisabledTrue()) {// 输入的有可能是手机号
            UserAuthentication ua = userLoaderClient.loadMobileAuthentication(String.valueOf(id), UserType.TEACHER);
            if (ua != null) {
                teacher = teacherLoaderClient.loadTeacher(ua.getId());
            }
        }
        if ((teacher == null || teacher.isDisabledTrue())) {
            return MapMessage.errorMessage("老师号错误！");
        }
        if (teacher.getSubject() == null) {
            return MapMessage.errorMessage("老师未选择任教科目");
        }
        if (!allowedKtwelve.contains(teacher.getKtwelve())) {
            return MapMessage.errorMessage("不能换到" + SafeConverter.toString(teacher.getKtwelve().getDescription()));
        }
        Long teacherId = teacher.getId();

        // 读取老师班级信息
        List<Clazz> clazzList = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId).stream()
                .filter(c -> c.isPublicClazz() || c.isWalkingClazz())
                .filter(c -> !c.isTerminalClazz())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(clazzList)) {
            return MapMessage.errorMessage("此老师还没有加入任何班级！");
        }

        // 读取学校信息
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("没有学校信息");
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Clazz c : clazzList) {
            Map<String, Object> clazzMap = new HashMap<>();
            List<Teacher> teachers;
            teachers = new LinkedList<>(teacherLoaderClient.loadSharedTeachers(teacherId, c.getId(), true));
            teachers.add(teacher);

            List<String> teacherInfos = teachers.stream()
                    .filter(t -> t != null && StringUtils.isNotBlank(t.fetchRealname()) && t.getSubject() != null)
                    .map(t -> t.fetchRealname().substring(0, 1) + "老师(" + t.getSubject().getValue() + ")")
                    .collect(Collectors.toList());

            clazzMap.put("schoolName", school.getCname());
            clazzMap.put("clazzName", c.formalizeClazzName());
            clazzMap.put("teachers", StringUtils.join(teacherInfos, " "));
            clazzMap.put("clazzId", c.getId());
            clazzMap.put("ktwelve", c.getEduSystem().getKtwelve());
            if (c.getCreateBy() != null) {
                clazzMap.put("creatorType", c.getCreateBy().name());
            }
            list.add(clazzMap);
        }

        return MapMessage.successMessage().add("clazzList", list).add("teacher", teacher);
    }

//    @Override
//    public List<Map<String, Object>> findByStudentNameAndSchoolId(Long schoolId, String studentName) {
//        return studentClazzService.findByStudentNameAndSchoolId(schoolId, studentName);
//    }

    @Override
    public List<BizStudentVoice> loadClazzStudentVoices(Collection<Long> clazzIds) {
        return studentVoiceService.loadClazzStudentVoices(clazzIds);
    }

    // ========================================================================
    // StudentInviteSendLogService
    // ========================================================================

//    @Override
//    @Deprecated
//    public List<StudentInviteSendLog> loadStudentInviteSendLogBySenderIdAndCreateDatetime(Long senderId, Date createDatetime) {
//        if (senderId == null || createDatetime == null) {
//            return Collections.emptyList();
//        }
//        return studentInviteSendLogServiceClient.getStudentInviteSendLogService()
//                .findStudentInviteSendLogsBySenderId(senderId)
//                .getUninterruptibly()
//                .stream()
//                .filter(e -> {
//                    long c = e.fetchCreateTimestamp();
//                    return c >= createDatetime.getTime();
//                })
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    @Deprecated
//    public List<StudentInviteSendLog> loadStudentInviteSendLogByMobileAndCreateDatetime(String mobile, Date createDatetime) {
//        if (mobile == null || createDatetime == null) {
//            return Collections.emptyList();
//        }
//        return studentInviteSendLogServiceClient.getStudentInviteSendLogService()
//                .findStudentInviteSendLogsByMobile(mobile)
//                .getUninterruptibly()
//                .stream()
//                .filter(e -> {
//                    long c = e.fetchCreateTimestamp();
//                    return c >= createDatetime.getTime();
//                })
//                .collect(Collectors.toList());
//    }

//    @Override
//    @Deprecated
//    public Long createStudentInviteSendLog(StudentInviteSendLog studentInviteSendLog) {
//        if (studentInviteSendLog == null) {
//            return null;
//        }
//        studentInviteSendLog = studentInviteSendLogServiceClient.getStudentInviteSendLogService()
//                .insertStudentInviteSendLog(studentInviteSendLog)
//                .getUninterruptibly();
//        return studentInviteSendLog == null ? null : studentInviteSendLog.getId();
//    }

    // ========================================================================
    // StudentAppInteractiveInfoService
    // ========================================================================

    @Override
    public MapMessage findClazzRank(Long clazzId, Long bookId, Long unitId, Long lessonId, Long practiceId) {
        return studentAppInteractiveInfoService.findClazzRank(clazzId, bookId, unitId, lessonId, practiceId);
    }

    @Override
    public MapMessage saveJuniorAppDetail(Long userId, Long bookId, Long unitId, Long lessonId, Long practiceId, Integer score, Map<String, Object> dataJson) {
        return studentAppInteractiveInfoService.saveStudentAppInteractiveInfo(userId, bookId, unitId, lessonId, practiceId, score, dataJson);
    }

    // ========================================================================
    // StudentParentRewardServcie
    // ========================================================================

    @Override
    public MapMessage studentMakeWish(Long studentId, WishType wishType, String wish) {
        return studentParentRewardService.studentMakeWish(studentId, wishType, wish);
    }

    @Override
    public MapMessage studentSendWechatNotice(Long studentId, Long missionId, String template) {
        return studentParentRewardService.studentSendWechatNotice(studentId, missionId, template);
    }

    @Override
    @Deprecated
    public MapMessage studentCheckDetail(Long studentId, Long missionId) {
        return studentParentRewardService.studentCheckDetail(studentId, missionId);
    }

    @Override
    @Deprecated
    public boolean updateMissionImg(Long missionId, String filename) {
        if (missionId == null || StringUtils.isBlank(filename)) {
            return false;
        }
        return missionServiceClient.getMissionService()
                .updateMissionImg(missionId, filename)
                .getUninterruptibly();
    }

    @Override
    public MapMessage parentSetMission(Long parentId, Long studentId, WishType wishType, String wish,
                                       Integer totalCount, String mission, MissionType missionType, Long missionId) {
        return studentParentRewardService.parentSetMission(parentId, studentId, wishType, wish,
                totalCount, mission, missionType, missionId);
    }

    @Override
    public MapMessage parentUpdateProgress(Long parentId, Long missionId) {
        return studentParentRewardService.parentUpdateProgress(parentId, missionId);
    }

    @Override
    public MapMessage parentUpdateComplete(Long parentId, Long missionId) {
        return studentParentRewardService.parentUpdateComplete(parentId, missionId);
    }

    @Override
    public boolean isCurrentMonthIntegralMissionArranged(Long studentId) {
        return studentParentRewardService.isCurrentMonthIntegralMissionArranged(studentId);
    }

    // ========================================================================
    // mothers day
    // ========================================================================

    @Override
    @Deprecated
    public MapMessage getMothersDayCard(User student, Boolean dataIncluded) {
        return mothersDayServiceClient.getMothersDayService()
                .getMothersDayCard(student, dataIncluded)
                .getUninterruptibly();
    }

    @Override
    @Deprecated
    public MapMessage giveMothersDayCardAsGift(User student, String image, String voice) {
        return mothersDayServiceClient.getMothersDayService()
                .giveMothersDayCardAsGift(student, image, voice)
                .getUninterruptibly();
    }

    @Override
    @Deprecated
    public MapMessage shareMothersDayCard(Long studentId) {
        return mothersDayServiceClient.getMothersDayService()
                .shareMothersDayCard(studentId)
                .getUninterruptibly();
    }

    @Override
    @Deprecated
    public void updateMothersDayCardSended(Long studentId) {
        mothersDayServiceClient.getMothersDayService()
                .updateMothersDayCardSended(studentId)
                .awaitUninterruptibly();
    }

    // ========================================================================
    // flower
    // ========================================================================

    @Override
    public List<Map<String, Object>> findCurrentMonthFlowerRankByTeacherIdAndClazzId(Long teacherId, Long clazzId) {
        if (teacherId == null || clazzId == null) {
            return Collections.emptyList();
        }
        List<Flower> flowers = flowerServiceClient.getFlowerService()
                .loadClazzFlowers(clazzId)
                .getUninterruptibly()
                .stream()
                .filter(t -> MonthRange.current().contains(t.fetchCreateTimestamp()))
                .collect(Collectors.toList());
        LinkedHashMap<Long, List<Flower>> flowerMap = flowers.stream()
                .filter(flower -> Long.compare(flower.getReceiverId(), teacherId) == 0)
                .collect(Collectors.groupingBy(Flower::getSenderId, Collectors.toList()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(new Comparator<List<Flower>>() {
                    @Override
                    public int compare(List<Flower> o1, List<Flower> o2) {
                        return Integer.compare(o2.size(), o1.size());
                    }
                }))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (x, y) -> {
                            throw new AssertionError();
                        },
                        LinkedHashMap::new));

        List<Map<String, Object>> rankList = new LinkedList<>();
        GroupMapper group = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacherId, clazzId, false);
        Map<Long, Student> studentMap = new LinkedHashMap<>();
        List<Long> clazzStudentIdSet = new ArrayList<>();
        if (group != null) {
            clazzStudentIdSet.addAll(studentLoaderClient.loadGroupStudentIds(group.getId()));
            studentMap.putAll(studentLoaderClient.loadStudents(clazzStudentIdSet));
        } else {
            studentMap.putAll(studentLoaderClient.loadStudents(flowerMap.keySet()));
        }
//        List<ClazzStudentRef> refs = clazzLoaderClient.getRemoteReference().loadClazzStudentRefs(clazzId);
//        Set<Long> clazzStudentIdSet = ClazzStudentRef.filter(refs).userIdSet();
//        Map<Long, Student> studentMap = studentLoaderClient.loadStudents(clazzStudentIdSet);

        Set<Long> sentFlowerStudentIdSet = flowerMap.keySet();
        // 先把送过花的学生排序好了放到结果里
        for (Long senderId : sentFlowerStudentIdSet) {
            Student student = studentMap.get(senderId);
            if (student == null) {
                student = studentLoaderClient.loadStudent(senderId);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", student.getId());
            map.put("senderName", student.getProfile().getRealname());
            map.put("count", flowerMap.get(senderId).size());
            rankList.add(map);
        }

        // 再把没送过花的学生填进去
        for (Long studentId : clazzStudentIdSet) {
            if (!sentFlowerStudentIdSet.contains(studentId)) {
                Student student = studentMap.get(studentId);
                if (student == null) {
                    continue;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("id", student.getId());
                map.put("senderName", student.getProfile().getRealname());
                map.put("count", 0);
                rankList.add(map);
            }
        }
        return rankList;
    }

    @Override
    public List<Map<String, Object>> findCurrentMonthFlowerRankBySchoolId(Long schoolId, Subject subject) {
        if (schoolId == null || subject == null) {
            return Collections.emptyList();
        }
        Date start = MonthRange.current().getStartDate();
        Date end = MonthRange.current().getEndDate();
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadSchoolTeachers(schoolId)
                .stream()
                .filter(t -> t.fetchCertificationState() == AuthenticationState.SUCCESS)
                .filter(t -> t.getSubject() == subject)
                .collect(Collectors.toMap(Teacher::getId, t -> t));
        if (MapUtils.isEmpty(teacherMap)) {
            return Collections.emptyList();
        }
        LinkedHashMap<Long, List<Flower>> flowerMap = buildRankList(teacherMap, start, end);

        List<Map<String, Object>> rankList = new LinkedList<>();
        // 先把收到过花的老师按排序放进结果里
        for (Long senderId : flowerMap.keySet()) {
            Teacher teacher = teacherMap.get(senderId);
            if (teacher == null) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", teacher.getId());
            map.put("senderName", teacher.getProfile().getRealname());
            map.put("count", flowerMap.get(senderId).size());
            rankList.add(map);
        }

        // 再把没收到过花的老师放进结果里
        for (Long teacherId : teacherMap.keySet()) {
            Teacher teacher = teacherMap.get(teacherId);
            if (teacher == null) {
                continue;
            }
            if (!flowerMap.keySet().contains(teacherId)) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", teacher.getId());
                map.put("senderName", teacher.getProfile().getRealname());
                map.put("count", 0);
                rankList.add(map);
            }
        }
        return rankList;
    }

    @Override
    public int findLastMonthFlowerRankInSchoolByTeacherId(Long teacherId) {

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        Long schoolId = teacherDetail.getTeacherSchoolId();
        String lastMonth = DateUtils.dateToString(MonthRange.current().previous().getEndDate(), DateUtils.FORMAT_YEAR_MONTH);
        String key = "LAST_MONTH_TEACHER_FLOWER_RANK_" + schoolId + "_" + teacherDetail.getSubject().name() + "_" + lastMonth;
        CacheObject<LinkedHashMap<Long, List<Flower>>> cacheObject = businessCacheSystem.CBS.flushable.get(key);
        LinkedHashMap<Long, List<Flower>> flowerMap;
        if (cacheObject != null && cacheObject.getValue() != null) {
            flowerMap = cacheObject.getValue();
        } else {
            Map<Long, Teacher> teacherMap = teacherLoaderClient.loadSchoolTeachers(schoolId)
                    .stream()
                    .filter(t -> t.fetchCertificationState() == AuthenticationState.SUCCESS)
                    .filter(t -> t.getSubject() == teacherDetail.getSubject())
                    .collect(Collectors.toMap(Teacher::getId, t -> t));
            if (MapUtils.isEmpty(teacherMap)) {
                return -1;
            }
            flowerMap = buildRankList(teacherMap, MonthRange.current().previous().getStartDate(), MonthRange.current().previous().getEndDate());
            businessCacheSystem.CBS.flushable.set(key, DateUtils.getCurrentToMonthEndSecond(), flowerMap);
        }

        int rank = 0;
        if (flowerMap.get(teacherId) == null) {
            rank = -1;
        } else {
            for (Long id : flowerMap.keySet()) {
                rank++;
                if (Long.compare(id, teacherId) == 0) {
                    break;
                }
            }
        }
        return rank;
    }

    private LinkedHashMap<Long, List<Flower>> buildRankList(Map<Long, Teacher> teacherMap, Date start, Date end) {
        DateRange range = new DateRange(start, end);
        List<Flower> flowers = flowerServiceClient.loadReceiverFlowers(teacherMap.keySet())
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(t -> range.contains(t.fetchCreateTimestamp()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(flowers)) {
            return new LinkedHashMap<>();
        }
        return flowers.stream()
                .collect(Collectors.groupingBy(Flower::getReceiverId, Collectors.toList()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(new Comparator<List<Flower>>() {
                    @Override
                    public int compare(List<Flower> o1, List<Flower> o2) {
                        return Integer.compare(o2.size(), o1.size());
                    }
                }))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (x, y) -> {
                            throw new AssertionError();
                        },
                        LinkedHashMap::new));
    }

    /**
     * @deprecated No more references
     */
    @Override
    @Deprecated
    public List<ExamAnswer> getExamAnswerByExamId(String examId) {
        if (StringUtils.isBlank(examId)) {
            return Collections.emptyList();
        }
        return examAnswerPersistence.findByExamId(examId);
    }

    @Override
    public String updateExamAnswerRecord(Map<String, Object> map) {
        ExamAnswerRecord record = JsonUtils.safeConvertMapToObject(map, ExamAnswerRecord.class);
        record.setStartTime(DateUtils.stringToDate(DateUtils.dateToString(new java.sql.Date(Long.valueOf((String) map.get("startTime"))), DateUtils.FORMAT_SQL_DATETIME)));
        record.setEndTime(DateUtils.stringToDate(DateUtils.dateToString(new java.sql.Date(Long.valueOf((String) map.get("endTime"))), DateUtils.FORMAT_SQL_DATETIME)));
        examAnswerRecordPersistence.insert(record);
        return record.getId();
    }

    @Override
    public void postParentMessage(Long studentId, Long missionId, String wechatNoticeTemplate) {
        studentParentRewardService.postParentMessage(studentId, missionId, WechatNoticeType.of(wechatNoticeTemplate));
    }

    @Override
    public MapMessage collectAmbassadorReportFeedback(Long englishId, Long mathId, boolean englishFlag, boolean mathFlag, Long studentId) {
        if (englishId != 0) {
            AmbassadorReportStudentFeedback feedback = new AmbassadorReportStudentFeedback();
            feedback.setTeacherId(englishId);
            feedback.setConfirm(englishFlag);
            feedback.setStudentId(studentId);
            ambassadorServiceClient.getAmbassadorService().$insertAmbassadorReportStudentFeedback(feedback);
        }
        if (mathId != 0) {
            AmbassadorReportStudentFeedback feedback = new AmbassadorReportStudentFeedback();
            feedback.setTeacherId(mathId);
            feedback.setConfirm(mathFlag);
            feedback.setStudentId(studentId);
            ambassadorServiceClient.getAmbassadorService().$insertAmbassadorReportStudentFeedback(feedback);
        }
        return MapMessage.successMessage();
    }

    //******************************** 开学福袋回流活动 开始

    @Override
    public StudentLuckyBagRecord loadLuckyBagByReceiverId(Long studentId) {
        Map<Long, StudentLuckyBagRecord> dataMap = studentLuckyBagRecordPersistence.loadByReceiverIds(Collections.singletonList(studentId));
        if (dataMap == null) {
            return null;
        } else {
            return dataMap.get(studentId);
        }
    }

    @Override
    public Map<Long, StudentLuckyBagRecord> loadLuckyBagByReceiverIds(List<Long> studentIds) {
        if (CollectionUtils.isEmpty(studentIds)) {
            return Collections.emptyMap();
        }
        return studentLuckyBagRecordPersistence.loadByReceiverIds(studentIds);
    }

    @Override
    public List<StudentLuckyBagRecord> loadLuckyBagBySenderId(Long studentId) {
        return studentLuckyBagRecordPersistence.loadBySenderId(studentId);
    }

    @Override
    public Map<String, Object> loadStudentLuckyBagIndexData(Long studentId, Long clazzId) {
        if (studentId == null || clazzId == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> data = new HashMap<>();
        // 获取我的同班同学
        List<User> classmates = userAggregationLoaderClient.loadLinkedClassmatesByClazzId(clazzId, studentId);
        List<Long> classmatesIds = classmates.stream().map(User::getId).collect(Collectors.toList());
        // 获取同学手上持有福袋情况
        Map<Long, StudentLuckyBagRecord> recordMap = studentLuckyBagRecordPersistence.loadByReceiverIds(classmatesIds);
        // 过滤出未获得过福袋的同学
        List<User> noBagUserList = classmates.stream().filter(u -> recordMap.get(u.getId()) == null).collect(Collectors.toList());
        List<Map<String, Object>> noBagUsers = new ArrayList<>();
        for (User u : noBagUserList) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("name", u.fetchRealname());
            userMap.put("img", u.fetchImageUrl());
            userMap.put("id", u.getId());
            noBagUsers.add(userMap);
        }
        data.put("noBagUsers", noBagUsers);
        // 获取是否有人传福袋给我
        Map<Long, StudentLuckyBagRecord> luckyBagRecordMap = studentLuckyBagRecordPersistence.loadByReceiverIds(Collections.singletonList(studentId));
        boolean hasBagPage = false;
        if (MapUtils.isNotEmpty(luckyBagRecordMap) && luckyBagRecordMap.get(studentId) != null) {
            hasBagPage = true;
            StudentLuckyBagRecord record = luckyBagRecordMap.get(studentId);
            if (record != null && record.getStatus() != LuckyBagStatus.OPEN) {
                User user = userLoaderClient.loadUser(record.getSenderId());
                if (user != null) {
                    Map<String, Object> sender = new HashMap<>();
                    sender.put("name", user.fetchRealname());
                    sender.put("img", user.fetchImageUrl());
                    data.put("sender", sender);
                }
            }
        } else {
            // 没有人给我福袋 需要看当前起点人数是否够了6个
            long startCount = recordMap.values().stream().filter(r -> r.getSenderId() == 0).count();
            // 没有福袋的人数
            if (startCount < 6 && noBagUserList.size() > 0) {
                // 还可以当起点
                hasBagPage = true;
            }
        }
        data.put("hasBagPage", hasBagPage);
        if (hasBagPage) {
            // 获取我是否把福袋传给了别人
            List<StudentLuckyBagRecord> senderList = studentLuckyBagRecordPersistence.loadBySenderId(studentId);
            if (CollectionUtils.isNotEmpty(senderList)) {
                data.put("firstStep", true);
                // 看看我传给的人是否都帮我打开了福袋
                if (senderList.stream().filter(r -> r.getStatus() == LuckyBagStatus.DELIVER).count() == 0) {
                    data.put("secondStep", true);
                } else {
                    // 列出我传递的人的福袋状态
                    List<Map<String, Object>> receiverList = new ArrayList<>();
                    String unOpenName = "";
                    for (StudentLuckyBagRecord r : senderList) {
                        Map<String, Object> receiverMap = new HashMap<>();
                        User receiver = userLoaderClient.loadUser(r.getReceiverId());
                        receiverMap.put("name", receiver == null ? "" : receiver.fetchRealname());
                        receiverMap.put("img", receiver == null ? "" : receiver.fetchImageUrl());
                        receiverMap.put("isOpen", r.getStatus() == LuckyBagStatus.OPEN);
                        receiverList.add(receiverMap);
                        if (r.getStatus() != LuckyBagStatus.OPEN) {
                            unOpenName = unOpenName + (receiver == null ? "" : receiver.fetchRealname()) + " ";
                        }
                    }
                    data.put("secondStep", false);
                    data.put("receivers", receiverList);
                    data.put("unOpenName", unOpenName);
                }
            } else {
                // 我没有传福袋给别人 查看是否还有人可以传
                if (noBagUsers.size() == 0) {
                    // 没人可以传了， 默认前两步直接过
                    data.put("firstStep", true);
                    data.put("secondStep", true);
                }
            }
            // 是否领取了10学豆奖励
            long count = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                    .unflushable_getUserBehaviorCount(UserBehaviorType.LUCKY_BAG_RECEIVE_REWARD, studentId)
                    .getUninterruptibly();
            if (count > 0) {
                data.put("thirdStep", true);
            }
        } else {
            // 获取没有传福袋 并且手上有福袋的同学
            List<Long> hasBagUserIds = recordMap.values().stream().map(StudentLuckyBagRecord::getReceiverId).collect(Collectors.toList());
            // 发出去的人
            Map<Long, List<StudentLuckyBagRecord>> senderMap = studentLuckyBagRecordPersistence.loadBySenderIds(hasBagUserIds);
            List<Long> hasBagNoSendUsersIds = recordMap.values().stream()
                    .filter(r -> CollectionUtils.isEmpty(senderMap.get(r.getReceiverId()))).map(StudentLuckyBagRecord::getReceiverId).collect(Collectors.toList());
            List<User> hasBagNoSendUsers = classmates.stream().filter(u -> hasBagNoSendUsersIds.contains(u.getId())).collect(Collectors.toList());
            List<Map<String, Object>> hasBagNoSendUsersMap = new ArrayList<>();
            for (User u : hasBagNoSendUsers) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("name", u.fetchRealname());
                userMap.put("img", u.fetchImageUrl());
                hasBagNoSendUsersMap.add(userMap);
            }
            data.put("hasBagNoSendUsersMap", hasBagNoSendUsersMap);
        }
        return data;
    }

    @Override
    public MapMessage sendLuckyBag(Long studentId, String receiverIds, Long clazzId) {
        String[] receiverIdArr = StringUtils.split(receiverIds, ",");
        if (receiverIdArr.length < 1) {
            return MapMessage.errorMessage("请选择要传递的同学");
        }
        if (receiverIdArr.length > 2) {
            return MapMessage.errorMessage("最多可以分享给两个同学哦");
        }
        // 校验本人是否已经分享过福袋了
        List<StudentLuckyBagRecord> senderList = studentLuckyBagRecordPersistence.loadBySenderId(studentId);
        if (CollectionUtils.isNotEmpty(senderList)) {
            return MapMessage.errorMessage("你已经分享过了，只能分享一次哦");
        }

        List<Long> realReceiverIds = new ArrayList<>();
        for (String receiverId : receiverIdArr) {
            realReceiverIds.add(SafeConverter.toLong(receiverId));
        }

        // 获取我的同班同学
        List<User> classmates = userAggregationLoaderClient.loadLinkedStudentsByClazzId(clazzId, studentId);
        List<Long> classmatesIds = classmates.stream().map(User::getId).collect(Collectors.toList());
        // 这里做个验证 防止不是一个班的学生进来
        for (Long rid : realReceiverIds) {
            if (!classmatesIds.contains(rid)) {
                return MapMessage.errorMessage("传递的同学和你不在一个班级，请重试");
            }
        }
        Map<Long, StudentLuckyBagRecord> recordMap = studentLuckyBagRecordPersistence.loadByReceiverIds(classmatesIds);
        // 我收到的福袋
        StudentLuckyBagRecord myRecord = recordMap.get(studentId);
        //看看我要传递的同学是否已经被别人传递了
        if (recordMap.values().stream().filter(r -> realReceiverIds.contains(r.getReceiverId())).count() > 0) {
            return MapMessage.errorMessage("该学生已经获得福袋，请重新选择");
        }
        // 看看同学是不是已经有6个学生为起点了
        if (recordMap.values().stream().filter(r -> r.getSenderId() == 0).count() < 6 && myRecord == null) {
            // 没够 需要给当前学生一条起点记录
            StudentLuckyBagRecord record = new StudentLuckyBagRecord();
            record.setReceiverId(studentId);
            record.setSenderId(0L);
            record.setStatus(LuckyBagStatus.OPEN);
            studentLuckyBagRecordPersistence.insert(record);
        } else {
            // 不是起点的情况下 判断
            // 我是否已经收到了福袋
            if (myRecord == null) {
                return MapMessage.errorMessage("你还没有收到福袋哦，赶快让同学传递给你吧");
            }
            // 过滤下看看自己是否帮助传给我的人打开了福袋
            if (myRecord.getStatus() != LuckyBagStatus.OPEN) {
                return MapMessage.errorMessage("你还没有帮同学打开福袋");
            }
        }
        // 执行传递
        String content = "有同学把福袋传递给你了，快去领取吧。<a href='/student/activity/luckybag.vpage'>查看详情</a>";
        String appContent = "有同学把福袋传递给你了，快去领取吧。";
        String link = "/student/activity/luckybag.vpage";
        for (Long receiver : realReceiverIds) {
            StudentLuckyBagRecord record = new StudentLuckyBagRecord();
            record.setReceiverId(receiver);
            record.setSenderId(studentId);
            record.setStatus(LuckyBagStatus.DELIVER);
            studentLuckyBagRecordPersistence.insert(record);

            // 发送右下角弹窗
            userPopupServiceClient.createPopup(receiver)
                    .content(content)
                    .type(PopupType.STUDENT_LUCKY_BAG_NOTICE)
                    .category(PopupCategory.LOWER_RIGHT)
                    .create();
            // 发送app内部通知
            AppMessage message = new AppMessage();
            message.setUserId(receiver);
            message.setMessageType(StudentAppPushType.ACTIVITY_REMIND.getType());
            message.setTitle("活动通知");
            message.setContent(appContent);
            message.setLinkUrl(link);
            message.setLinkType(1);//站内的相对地址
            messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
        }
        return MapMessage.successMessage("传递成功");
    }

    @Override
    public MapMessage openLuckyBag(Long studentId) {
        Map<Long, StudentLuckyBagRecord> luckyBagRecordMap = studentLuckyBagRecordPersistence.loadByReceiverIds(Collections.singletonList(studentId));
        if (MapUtils.isEmpty(luckyBagRecordMap) || luckyBagRecordMap.get(studentId) == null) {
            return MapMessage.errorMessage("还没有人传递福袋给你哦");
        }
        StudentLuckyBagRecord record = luckyBagRecordMap.get(studentId);
        if (record.getStatus() == LuckyBagStatus.OPEN) {
            return MapMessage.successMessage();
        }
        studentLuckyBagRecordPersistence.openLuckyBag(record.getId());
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage receiveLuckyBag(Long studentId) {
        if (studentId == null) {
            return MapMessage.errorMessage();
        }
        // 需要先给传给我的同学打开福袋 才可以领取
        Map<Long, StudentLuckyBagRecord> recordMap = studentLuckyBagRecordPersistence.loadByReceiverIds(Collections.singletonList(studentId));
        if (recordMap == null || recordMap.get(studentId) == null) {
            return MapMessage.errorMessage("你没有获得福袋，不能领取奖励");
        }
        if (recordMap.get(studentId).getStatus() != LuckyBagStatus.OPEN) {
            return MapMessage.errorMessage("你还没有帮助同学打开福袋哦");
        }
        long count = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .unflushable_getUserBehaviorCount(UserBehaviorType.LUCKY_BAG_HOMEWORK_COUNT, studentId)
                .getUninterruptibly();
        if (count <= 0) {
            return MapMessage.errorMessage("需要先做一次作业才能领取学豆奖励");
        }
        // 看看是否领取过
        long receiveCount = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .unflushable_getUserBehaviorCount(UserBehaviorType.LUCKY_BAG_RECEIVE_REWARD, studentId)
                .getUninterruptibly();
        if (receiveCount > 0) {
            return MapMessage.errorMessage("你已经领取过了");
        }
        // 领取10学豆
        IntegralHistory integralHistory = new IntegralHistory(studentId, IntegralType.开学福袋学生领取奖励_产品平台, 10);
        integralHistory.setComment("福袋奖励");
        if (userIntegralService.changeIntegral(integralHistory).isSuccess()) {
            // 记录
            asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                    .unflushable_incUserBehaviorCount(UserBehaviorType.LUCKY_BAG_RECEIVE_REWARD, studentId, 1L, LuckyBagActivity.getActivityEndDate())
                    .awaitUninterruptibly();
            return MapMessage.successMessage("领取成功");
        } else {
            return MapMessage.successMessage("领取失败");
        }
    }
}
