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

package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzIntegralPool;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.config.consumer.BadWordCheckerClient;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.content.consumer.EnglishContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.PracticeLoaderClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkPublishMessageType;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NatureSpellingType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.constant.PictureBookPracticeType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.newhomework.api.mapper.NaturalSpellingSentence;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkWinterPlanCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.service.VacationHomeworkService;
import com.voxlearning.utopia.service.newhomework.cache.HomeworkCache;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.*;
import com.voxlearning.utopia.service.newhomework.impl.loader.VacationHomeworkCacheLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.pubsub.NewHomeworkPublisher;
import com.voxlearning.utopia.service.newhomework.impl.service.helper.VacationHomeworkBigDataHelper;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.vacation.VacationHomeworkResultProcessor;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.AutoAssignVacationHomeworkProcessor;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignvacation.AssignVacationHomeworkProcess;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.WinterDayPlan;
import com.voxlearning.utopia.service.question.consumer.DubbingLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.constants.ClazzIntegralType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.ClazzIntegralHistory;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tanguohong
 * @since 2016/11/30
 */
@Named
@Service(interfaceClass = VacationHomeworkService.class)
@ExposeService(interfaceClass = VacationHomeworkService.class)
public class VacationHomeworkServiceImpl extends SpringContainerSupport implements VacationHomeworkService {

    @Inject
    private ClazzIntegralServiceClient clazzIntegralServiceClient;
    @Inject
    private AssignVacationHomeworkProcess assignVacationHomeworkProcess;
    @Inject
    private VacationHomeworkPackageDao vacationHomeworkPackageDao;
    @Inject
    private VacationHomeworkDao vacationHomeworkDao;
    @Inject
    private VacationHomeworkBookDao vacationHomeworkBookDao;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject
    private VacationHomeworkBigDataHelper vacationHomeworkBigDataHelper;
    @Inject
    private VacationHomeworkCacheLoaderImpl vacationHomeworkCacheLoader;
    @Inject
    private VacationHomeworkResultDao vacationHomeworkResultDao;
    @Inject
    private VacationHomeworkProcessResultDao vacationHomeworkProcessResultDao;
    @Inject
    private VacationHomeworkResultProcessor vacationHomeworkResultProcessor;
    @Inject
    private UserAggregationLoaderClient userAggregationLoaderClient;
    @Inject
    private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject
    private AutoAssignVacationHomeworkProcessor autoAssignVacationHomeworkProcessor;
    @Inject
    private BadWordCheckerClient badWordCheckerClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private QuestionLoaderClient questionLoaderClient;
    @Inject
    private PracticeLoaderClient practiceLoaderClient;
    @Inject
    private AppMessageServiceClient appMessageServiceClient;
    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    private NewHomeworkPublisher newHomeworkPublisher;
    @Inject
    private EnglishContentLoaderClient englishContentLoaderClient;
    @Inject
    private DubbingLoaderClient dubbingLoaderClient;


    @Inject private RaikouSDK raikouSDK;

    @ImportService(interfaceClass = UserIntegralService.class)
    private UserIntegralService userIntegralService;

    @Override
    public MapMessage assignHomework(Teacher teacher, HomeworkSource source, HomeworkSourceType homeworkSourceType) {
        try {
            AssignVacationHomeworkContext assignVacationHomeworkContext = new AssignVacationHomeworkContext();
            assignVacationHomeworkContext.setTeacher(teacher);
            assignVacationHomeworkContext.setHomeworkSourceType(homeworkSourceType);
            assignVacationHomeworkContext.setSource(source);

            AssignVacationHomeworkContext context = AtomicLockManager.instance().wrapAtomic(assignVacationHomeworkProcess)
                    .keys(teacher.getId(), teacher.getSubject().getId())
                    .proxy()
                    .process(assignVacationHomeworkContext);

            return context.transform()
                    .add("integral", context.getIntegral())
                    .add("lotteryNumber", context.getLotteryNumber());
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("假期作业布置中，请不要重复布置!").setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
        } catch (Exception ex) {
            logger.error("failed to save vacation homework, teacher id {}, homework_data {}", teacher.getId(), source, ex);
            return MapMessage.errorMessage("布置假期作业失败").setErrorCode(ErrorCodeConstants.ERROR_CODE_COMMON);
        }
    }

    @Override
    public MapMessage deleteHomework(Long teacherId, String id) {
        try {
            return AtomicLockManager.instance().wrapAtomic(this)
                    .keys(teacherId, id)
                    .proxy()
                    .internalDeleteHomework(teacherId, id);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("假期作业删除中，请不要重复删除!").setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
        } catch (Exception ex) {
            logger.error("failed to delete vacation homework, teacher id {}, homeworkId {}", teacherId, id, ex);
        }
        return MapMessage.errorMessage("删除假期作业失败").setErrorCode(ErrorCodeConstants.ERROR_CODE_COMMON);
    }

    public MapMessage internalDeleteHomework(Long teacherId, String id) {
        VacationHomeworkPackage vacationHomeworkPackage = vacationHomeworkPackageDao.load(id);
        if (vacationHomeworkPackage == null) {
            return MapMessage.errorMessage("假期作业不存在");
        }
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacherId, vacationHomeworkPackage.getClazzGroupId())) {
            return MapMessage.errorMessage("您没有权限删除此假期作业");
        }

//        Date currentDate = new Date();
//        if (currentDate.after(vacationHomeworkPackage.getStartTime())) {
//            return MapMessage.errorMessage("假期作业已开始，作业无法删除");
//        }

        try {

            Boolean delete = vacationHomeworkPackageDao.updateDisabledTrue(id);
            if (delete) {
                vacationHomeworkCacheLoader.removeVacationHomeworkCacheMapper(vacationHomeworkPackage.getClazzGroupId());

                //这里取主学科的ID
                Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
                if (mainTeacherId == null) {
                    mainTeacherId = teacherId;
                }
                //删除作业老师广播
                Map<String, Object> teacherPublisherMap = new HashMap<>();
                teacherPublisherMap.put("messageType", HomeworkPublishMessageType.deleted);
                teacherPublisherMap.put("homeworkId", vacationHomeworkPackage.getId());
                teacherPublisherMap.put("teacherId", mainTeacherId);
                teacherPublisherMap.put("groupId", vacationHomeworkPackage.getClazzGroupId());
                teacherPublisherMap.put("subject", vacationHomeworkPackage.getSubject());
                teacherPublisherMap.put("homeworkType", NewHomeworkType.WinterVacation);
                newHomeworkPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(teacherPublisherMap)));
                return MapMessage.successMessage("删除假期作业成功");
            } else {
                return MapMessage.errorMessage("删除假期作业失败");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    /**
     * 删除假期作业
     * For：CRM
     *
     * @param id
     * @return
     */
    @Override
    public MapMessage crmDeleteVacationHomework(String id) {
        VacationHomeworkPackage vacationHomeworkPackage = vacationHomeworkPackageDao.load(id);
        if (vacationHomeworkPackage == null) {
            return MapMessage.errorMessage("假期作业不存在");
        }
        try {
            Boolean delete = vacationHomeworkPackageDao.updateDisabledTrue(id);
            if (delete) {
                vacationHomeworkCacheLoader.removeVacationHomeworkCacheMapper(vacationHomeworkPackage.getClazzGroupId());

                //这里取主学科的ID
                Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(vacationHomeworkPackage.getTeacherId());
                if (mainTeacherId == null) {
                    mainTeacherId = vacationHomeworkPackage.getTeacherId();
                }
                //删除作业老师广播
                Map<String, Object> teacherPublisherMap = new HashMap<>();
                teacherPublisherMap.put("messageType", HomeworkPublishMessageType.deleted);
                teacherPublisherMap.put("homeworkId", vacationHomeworkPackage.getId());
                teacherPublisherMap.put("teacherId", mainTeacherId);
                teacherPublisherMap.put("groupId", vacationHomeworkPackage.getClazzGroupId());
                teacherPublisherMap.put("subject", vacationHomeworkPackage.getSubject());
                teacherPublisherMap.put("homeworkType", NewHomeworkType.WinterVacation);
                newHomeworkPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(teacherPublisherMap)));
                return MapMessage.successMessage("删除假期作业成功");
            } else {
                return MapMessage.errorMessage("删除假期作业失败");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    /**
     * 恢复假期作业
     * For：CRM
     */
    @Override
    public MapMessage resumeVacationHomework(String packageId) {
        VacationHomeworkPackage vacationHomeworkPackage = vacationHomeworkPackageDao.load(packageId);
        if (vacationHomeworkPackage == null) {
            return MapMessage.errorMessage("假期作业不存在");
        }
        if (!SafeConverter.toBoolean(vacationHomeworkPackage.getDisabled())) {
            return MapMessage.errorMessage("假期作业未被删除");
        }

        //校验当前班组是否存在假期作业
        List<VacationHomeworkPackage.Location> vacationHomeworkPackageList = vacationHomeworkPackageDao.loadVacationHomeworkPackageByClazzGroupIds(Collections.singleton(vacationHomeworkPackage.getClazzGroupId())).get(vacationHomeworkPackage.getClazzGroupId());
        if (CollectionUtils.isEmpty(vacationHomeworkPackageList)) {
            vacationHomeworkPackageDao.resumeVacationHomework(packageId);
        } else {
            return MapMessage.errorMessage("该班级中已存在假期作业，当前假期作业不允许被恢复！");
        }

        return MapMessage.successMessage();
    }

    /**
     * 生成假期作业
     *
     * @param packageId
     * @param weekRank
     * @param dayRank
     * @param studentId
     * @return
     */
    public VacationHomework generateVacationHomework(String packageId, Integer weekRank, Integer dayRank, Long studentId) {
        try {
            return AtomicLockManager.instance().wrapAtomic(this)
                    .keyPrefix("VACATION")
                    .keys(packageId, studentId)
                    .proxy()
                    .generateVacationHomeworkAtomic(packageId, weekRank, dayRank, studentId);
        } catch (CannotAcquireLockException ex) {
            return null;
        } catch (Exception ex) {
            logger.error("generate vacation homework, packageId {}, studentId {}", packageId, studentId, ex);
        }
        return null;
    }

    /**
     * 这个方法不能改为private
     */
    public VacationHomework generateVacationHomeworkAtomic(String packageId, Integer weekRank, Integer dayRank, Long studentId) {
        VacationHomework.ID vid = new VacationHomework.ID(packageId, weekRank, dayRank, studentId);
        VacationHomework vacationHomework = vacationHomeworkDao.load(vid.toString());
        if (vacationHomework != null) {
            return vacationHomework;
        }
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
        if (clazz == null) {
            return null;
        }
        VacationHomeworkPackage vacationHomeworkPackage = vacationHomeworkPackageDao.load(packageId);
        if (vacationHomeworkPackage == null) {
            return null;
        }
        String bookId = vacationHomeworkPackage.getBookId();
        VacationHomeworkWinterPlanCacheMapper vacationHomeworkWinterPlanCacheMapper = vacationHomeworkCacheLoader.loadVacationHomeworkWinterPlanCacheMapper(bookId);
        if (vacationHomeworkWinterPlanCacheMapper == null) {
            return null;
        }
        WinterDayPlan winterDayPlan = vacationHomeworkWinterPlanCacheMapper.getDayPlan().get(StringUtils.join(Arrays.asList(weekRank, dayRank), "-"));
        if (winterDayPlan == null) {
            return null;
        }
        //List<String> unitIds = winterDayPlan.getUnit().stream().map(WinterUnit::getUnitId).collect(Collectors.toList());
        //if (CollectionUtils.isEmpty(unitIds)) {
        //    return null;
        //}
        Date d = new Date();

        vacationHomework = new VacationHomework();
        vacationHomework.setClazzGroupId(vacationHomeworkPackage.getClazzGroupId());
        vacationHomework.setTeacherId(vacationHomeworkPackage.getTeacherId());
        vacationHomework.setStudentId(studentId);
        vacationHomework.setWeekRank(weekRank);
        vacationHomework.setDayRank(dayRank);
        vacationHomework.setPackageId(vacationHomeworkPackage.getId());
        vacationHomework.setSubject(vacationHomeworkPackage.getSubject());
        vacationHomework.setActionId(vacationHomeworkPackage.getActionId());
        vacationHomework.setType(NewHomeworkType.WinterVacation);
        vacationHomework.setHomeworkTag(HomeworkTag.Normal);
        vacationHomework.setSource(vacationHomeworkPackage.getSource());
        vacationHomework.setDuration(0L);
        vacationHomework.setIncludeSubjective(false);

        vacationHomework.setCreateAt(d);
        vacationHomework.setUpdateAt(d);

        String id = new VacationHomework.ID(vacationHomework.getPackageId(), vacationHomework.getWeekRank(), vacationHomework.getDayRank(), vacationHomework.getStudentId()).toString();
        VacationHomeworkBook vacationHomeworkBook = new VacationHomeworkBook();
        vacationHomeworkBook.setId(id);
        vacationHomeworkBook.setCreateAt(d);
        vacationHomeworkBook.setUpdateAt(d);

        // 地区信息
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        //组装数据
        vacationHomeworkBigDataHelper.generateVacationHomework(vacationHomework, vacationHomeworkBook, winterDayPlan, bookId, studentDetail.getCityCode());

        if (CollectionUtils.isEmpty(vacationHomework.getPractices())) {
            throw new RuntimeException("vacation homework practices is empty");
        }
        //初始化表信息
        vacationHomeworkDao.insert(vacationHomework);
        vacationHomeworkBookDao.insert(vacationHomeworkBook);
        vacationHomeworkResultDao.initVacationHomeworkResult(vacationHomework.toLocation(), studentId);
        //相关数据写入缓存
        vacationHomeworkCacheLoader.addOrModifyVacationHomeworkCacheMapper(vacationHomework);
        return vacationHomework;
    }

    public MapMessage processVacationHomeworkResult(VacationHomeworkResultContext context) {
        try {
            AtomicLockManager.getInstance().wrapAtomic(vacationHomeworkResultProcessor)
                    .keys(context.getUserId(), context.getVacationHomeworkId())
                    .proxy()
                    .process(context);
            return context.transform().add("result", context.getResult());
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("数据提交中，请不要重复点击!").setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @Override
    public MapMessage vacationHomeworkComment(String homeworkId, String comment, String audioComment) {

        if (StringUtils.isBlank(comment) && StringUtils.isBlank(audioComment)) {
            return MapMessage.errorMessage("评语失败");
        }

        if (badWordCheckerClient.containsConversationBadWord(comment)) {
            return MapMessage.errorMessage("评语失败");
        }
        VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.load(homeworkId);

        if (vacationHomeworkResult == null) {
            return MapMessage.errorMessage("评语失败");
        }
        VacationHomework vacationHomework = vacationHomeworkDao.load(homeworkId);
        if (vacationHomework == null) {
            return MapMessage.errorMessage("假期作业包不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_VACATION_DAY_RANK_NOT_EXISTS);
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(vacationHomework.getTeacherId());
        if (teacher == null) {
            return MapMessage.errorMessage("老师不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_TEACHER_NOT_EXIST);
        }

        Boolean commitComment = vacationHomeworkResultDao.saveVacationHomeworkComment(homeworkId, comment, audioComment);
        if (commitComment) {
            Long studentId = vacationHomeworkResult.parseID().getUserId();
            // 学生端JPush
            String link = UrlUtils.buildUrlQuery("/studentMobile/homework/vacation/skip.vpage", MapUtils.m("homeworkId", homeworkId));
            if (StringUtils.isNotBlank(audioComment)) {
                comment = "[语音评语]";
            }
            String content = teacher.respectfulName() + "点评了你的假期作业:\n" + comment;
            Map<String, Object> extInfo = new HashMap<>();
            extInfo.put("link", link);
            extInfo.put("t", "h5");
            extInfo.put("key", "j");
            extInfo.put("s", StudentAppPushType.HOLIDAY_HOMEWORK_WRITE_COMMENT_REMIND.getType());
            extInfo.put("title", "老师评语");
            appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.STUDENT, Collections.singletonList(studentId), extInfo);
            // 学生端消息中心
            AppMessage message = new AppMessage();
            message.setUserId(studentId);
            message.setMessageType(StudentAppPushType.HOMEWORK_WRITE_COMMENT.getType());
            message.setTitle("老师评语");
            message.setContent(content);
            message.setLinkUrl(link);
            message.setLinkType(1); // 站内的相对地址
            messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
            return MapMessage.successMessage("评语成功");
        } else {
            return MapMessage.successMessage("评语失败");
        }

    }

    @Override
    public void removeCache(List<String> keys) {
        HomeworkCache.getHomeworkCacheFlushable().delete(keys);
    }

    @Override
    public MapMessage vacationHomeworkCommentRewardIntegral(TeacherDetail teacherDetail, String homeworkId, Integer rewardIntegral) {
        VacationHomework vacationHomework = vacationHomeworkDao.load(homeworkId);
        if (vacationHomework == null) {
            return MapMessage.errorMessage("假期作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_VACATION_HOMEWORK_NOT_EXIST);
        }
        Long clazzId = raikouSDK.getClazzClient()
                .getGroupLoaderClient()
                ._loadGroup(vacationHomework.getClazzGroupId())
                .asList()
                .stream()
                .map(Group::getClazzId)
                .findFirst()
                .orElse(null);

        Long studentId = vacationHomework.getStudentId();
        Set<Long> studentIds = userAggregationLoaderClient.loadTeacherStudentsByClazzId(clazzId, teacherDetail.getId())
                .stream().map(User::getId).collect(Collectors.toSet());

        if (!studentIds.contains(studentId)) return MapMessage.errorMessage("操作失败，请重试");
        Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId);
        if (clazz == null) {
            return MapMessage.errorMessage("班级信息不能为空");
        }
        GroupMapper group = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacherDetail.getId(), clazzId, false);
        if (group == null) {
            return MapMessage.errorMessage("组信息不能为空");
        }
        try {
            // 走奖励学豆逻辑
            SmartClazzIntegralPool pool = clazzIntegralServiceClient.getClazzIntegralService()
                    .loadClazzIntegralPool(group.getId())
                    .getUninterruptibly();
            if (pool == null) {
                return MapMessage.errorMessage("班级学豆池不能为空");
            }
            //班级中剩余学豆
            int totalIntegral = pool.fetchTotalIntegral();
            if (totalIntegral < rewardIntegral) {
                // 去兑换 看看差几个
                int diff = rewardIntegral - totalIntegral;
                int deductGold = diff / 5 + (diff % 5 > 0 ? 1 : 0);
                if (teacherDetail.getUserIntegral().getUsable() < deductGold) {
                    return MapMessage.errorMessage("园丁豆数量不足");
                }
                // 扣减老师金币
                IntegralHistory integralHistory = new IntegralHistory();
                integralHistory.setIntegral(deductGold * -10);
                integralHistory.setComment("假期作业报告奖励学生");
                integralHistory.setIntegralType(IntegralType.智慧教室老师兑换学豆.getType());
                integralHistory.setUserId(teacherDetail.getId());
                MapMessage msg = userIntegralService.changeIntegral(teacherDetail, integralHistory);
                if (!msg.isSuccess()) {
                    return MapMessage.errorMessage("扣减园丁豆失败");
                }
                // 先充值
                ClazzIntegralHistory history = new ClazzIntegralHistory();
                history.setGroupId(group.getId());
                history.setClazzIntegralType(ClazzIntegralType.老师兑换班级学豆.getType());
                history.setIntegral(deductGold * 5);
                history.setComment(ClazzIntegralType.老师兑换班级学豆.getDescription());
                history.setAddIntegralUserId(teacherDetail.getId());
                MapMessage message = clazzIntegralServiceClient.getClazzIntegralService()
                        .changeClazzIntegral(history)
                        .getUninterruptibly();
                if (!message.isSuccess()) {
                    logger.warn("change clazz pool fail, groupId {}", group.getId());
                    return MapMessage.errorMessage("假期作业奖励失败");
                }
                // 执行发放
                ClazzIntegralHistory decrHistory = new ClazzIntegralHistory();
                decrHistory.setGroupId(group.getId());
                decrHistory.setClazzIntegralType(ClazzIntegralType.作业报告奖励学生.getType());
                decrHistory.setIntegral(-rewardIntegral);
                decrHistory.setComment(ClazzIntegralType.作业报告奖励学生.getDescription());
                message = clazzIntegralServiceClient.getClazzIntegralService()
                        .changeClazzIntegral(decrHistory)
                        .getUninterruptibly();
                if (!message.isSuccess()) {
                    logger.warn("change clazz pool fail, groupId {}", group.getId());
                    return MapMessage.errorMessage("假期奖励失败");
                }
            } else {
                ClazzIntegralHistory decrHistory = new ClazzIntegralHistory();
                decrHistory.setGroupId(group.getId());
                decrHistory.setClazzIntegralType(ClazzIntegralType.作业报告奖励学生.getType());
                decrHistory.setIntegral(-rewardIntegral);
                decrHistory.setComment(ClazzIntegralType.作业报告奖励学生.getDescription());
                MapMessage message = clazzIntegralServiceClient.getClazzIntegralService()
                        .changeClazzIntegral(decrHistory)
                        .getUninterruptibly();
                if (!message.isSuccess()) {
                    logger.warn("change clazz pool fail, groupId {}", group.getId());
                    return MapMessage.errorMessage("假期奖励失败");
                }
            }
            //给学生账户加学豆
            IntegralHistory integralHistory = new IntegralHistory(studentId, IntegralType.学生收到老师在作业报告发放的学豆_产品平台, rewardIntegral);
            integralHistory.setAddIntegralUserId(teacherDetail.getId());
            integralHistory.setComment("老师假期作业奖励学豆");
            if (userIntegralService.changeIntegral(integralHistory).isSuccess()) {
                Boolean commitRewardIntegral = vacationHomeworkResultDao.saveVacationHomeworkRewardIntegral(homeworkId, rewardIntegral);
                if (commitRewardIntegral) {
                    // 学生端JPush
                    String link = UrlUtils.buildUrlQuery("/studentMobile/homework/vacation/skip.vpage", MapUtils.m("homeworkId", homeworkId));
                    String content = "假期作业完成的不错，" + teacherDetail.respectfulName() + "奖励你" + rewardIntegral + "学豆，继续加油哦！";
                    Map<String, Object> extInfo = new HashMap<>();
                    extInfo.put("link", link);
                    extInfo.put("t", "h5");
                    extInfo.put("key", "j");
                    extInfo.put("s", StudentAppPushType.HOLIDAY_HOMEWORK_REWARD_INTEGRAL_REMIND.getType());
                    extInfo.put("title", "获得老师奖励");
                    appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.STUDENT, Collections.singletonList(studentId), extInfo);
                    // 学生端消息中心
                    AppMessage message = new AppMessage();
                    message.setUserId(studentId);
                    message.setMessageType(StudentAppPushType.HOMEWORK_SEND_INTEGRAL.getType());
                    message.setTitle("获得老师奖励");
                    message.setContent(content);
                    message.setLinkUrl(link);
                    message.setLinkType(1); // 站内的相对地址
                    messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
                    return MapMessage.successMessage("奖励成功");
                } else {
                    return MapMessage.successMessage("奖励失败");
                }
            }
        } catch (Exception ex) {
            logger.error("teacher vacation reward student error, teacher {}, clazz {}, error {}", teacherDetail.getId(), clazzId, ex.getMessage());
            return MapMessage.errorMessage("奖励失败");
        }
        return MapMessage.errorMessage("奖励失败");
    }

    @Override
    public MapMessage autoAssign(Teacher teacher) {
        return autoAssignVacationHomeworkProcessor.autoAssign(teacher);
    }

    @Override
    public MapMessage loadSubjectiveFiles(String homeworkId, ObjectiveConfigType objectiveConfigType, String questionId) {
        VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.load(homeworkId);
        try {
            if (vacationHomeworkResult != null && MapUtils.isNotEmpty(vacationHomeworkResult.getPractices())) {
                LinkedHashMap<String, String> answerMap = vacationHomeworkResult
                        .getPractices()
                        .get(objectiveConfigType)
                        .getAnswers();
                if (MapUtils.isEmpty(answerMap)) {
                    LinkedHashMap<String, String> appAnswerMap = new LinkedHashMap<>();
                    LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswers = vacationHomeworkResult
                            .getPractices()
                            .get(objectiveConfigType)
                            .getAppAnswers();
                    if (MapUtils.isNotEmpty(appAnswers)) {
                        appAnswers.values().forEach(o -> appAnswerMap.putAll(o.getAnswers()));
                    }
                    answerMap = appAnswerMap;
                }
                String processId = answerMap.get(questionId);
                if (StringUtils.isNotBlank(processId)) {
                    VacationHomeworkProcessResult processResult = vacationHomeworkProcessResultDao.load(processId);
                    return MapMessage.successMessage().add("files", processResult.getFiles());
                }
            }
        } catch (Exception ignored) {
        }
        return MapMessage.successMessage().add("files", new ArrayList<>());
    }

    @Override
    public MapMessage autoSubmitVacationHomework(String homeworkId, Long userId, ObjectiveConfigType type) {
        Student student = studentLoaderClient.loadStudent(userId);
        if (student == null) {
            return MapMessage.errorMessage("学生id错误");
        }
        VacationHomework vacationHomework = vacationHomeworkDao.load(homeworkId);
        if (vacationHomework == null) {
            return MapMessage.errorMessage("作业id错误");
        }
        VacationHomeworkBook vacationHomeworkBook = vacationHomeworkBookDao.load(homeworkId);
        Map<ObjectiveConfigType, NewHomeworkPracticeContent> practiceContentMap = vacationHomework.findPracticeContents();
        VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.load(homeworkId);
        if (vacationHomeworkResult != null && vacationHomeworkResult.isFinished()) {
            return MapMessage.errorMessage("该作业已完成");
        }
        for (Map.Entry<ObjectiveConfigType, NewHomeworkPracticeContent> entry : practiceContentMap.entrySet()) {
            ObjectiveConfigType objectiveConfigType = entry.getKey();
            if (type != null && objectiveConfigType != type) {
                continue;
            }
            NewHomeworkPracticeContent newHomeworkPracticeContent = entry.getValue();
            MapMessage mapMessage;
            switch (objectiveConfigType) {
                // 同步习题类的
                case EXAM:
                case MENTAL:
                case INTERESTING_PICTURE:
                case BASIC_KNOWLEDGE:
                case CHINESE_READING:
                case INTELLIGENCE_EXAM:
                    mapMessage = processExam(objectiveConfigType, newHomeworkPracticeContent, userId, student, homeworkId, vacationHomeworkBook);
                    if (!mapMessage.isSuccess()) {
                        return MapMessage.errorMessage(mapMessage.getInfo());
                    }
                    break;
                // 重难点视频是特殊的一类
                case KEY_POINTS:
                    mapMessage = processKeyPoints(objectiveConfigType, newHomeworkPracticeContent, userId, student, homeworkId);
                    if (!mapMessage.isSuccess()) {
                        return MapMessage.errorMessage(mapMessage.getInfo());
                    }
                    break;
                case BASIC_APP:
                    mapMessage = processBasicApp(objectiveConfigType, newHomeworkPracticeContent, userId, student, homeworkId);
                    if (!mapMessage.isSuccess()) {
                        return MapMessage.errorMessage(mapMessage.getInfo());
                    }
                    break;
                case NATURAL_SPELLING:
                    mapMessage = processNaturalSpelling(objectiveConfigType, newHomeworkPracticeContent, userId, student, homeworkId);
                    if (!mapMessage.isSuccess()) {
                        return MapMessage.errorMessage(mapMessage.getInfo());
                    }
                    break;
                case READING:
                    mapMessage = processReading(objectiveConfigType, newHomeworkPracticeContent, userId, student, homeworkId);
                    if (!mapMessage.isSuccess()) {
                        return MapMessage.errorMessage(mapMessage.getInfo());
                    }
                    break;
                case DUBBING:
                    mapMessage = processDubbing(objectiveConfigType, newHomeworkPracticeContent, userId, student, homeworkId);
                    if (!mapMessage.isSuccess()) {
                        return MapMessage.errorMessage(mapMessage.getInfo());
                    }
                    break;
                case READ_RECITE:
                    mapMessage = processReadRecite(objectiveConfigType, newHomeworkPracticeContent, userId, student, homeworkId);
                    if (!mapMessage.isSuccess()) {
                        return MapMessage.errorMessage(mapMessage.getInfo());
                    }
                    break;
                case NEW_READ_RECITE:
                    mapMessage = processNewReadRecite(objectiveConfigType, newHomeworkPracticeContent, userId, student, homeworkId);
                    if (!mapMessage.isSuccess()) {
                        return MapMessage.errorMessage(mapMessage.getInfo());
                    }
                    break;
                case READ_RECITE_WITH_SCORE:
                    mapMessage = processReadReciteWithScore(objectiveConfigType, newHomeworkPracticeContent, userId, student, homeworkId);
                    if (!mapMessage.isSuccess()) {
                        return MapMessage.errorMessage(mapMessage.getInfo());
                    }
                    break;
                case LEVEL_READINGS:
                    mapMessage = processLevelReadings(objectiveConfigType, newHomeworkPracticeContent, userId, student, homeworkId);
                    if (!mapMessage.isSuccess()) {
                        return MapMessage.errorMessage(mapMessage.getInfo());
                    }
                    break;
                case MENTAL_ARITHMETIC:
                    mapMessage = processMentalArithmetic(objectiveConfigType, newHomeworkPracticeContent, userId, student, homeworkId);
                    if (!mapMessage.isSuccess()) {
                        return MapMessage.errorMessage(mapMessage.getInfo());
                    }
                    break;
                default:
                    return MapMessage.errorMessage("不支持的类型:" + objectiveConfigType);
            }
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage autoSubmitDubbingHomework(String homeworkId) {
        VacationHomework vacationHomework = vacationHomeworkDao.load(homeworkId);
        if (vacationHomework == null) {
            return MapMessage.errorMessage("作业id错误");
        }
        Student student = studentLoaderClient.loadStudent(vacationHomework.getStudentId());
        if (student == null) {
            return MapMessage.errorMessage("学生id错误");
        }
        Map<ObjectiveConfigType, NewHomeworkPracticeContent> practiceContentMap = vacationHomework.findPracticeContents();
        VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.load(homeworkId);
        if (vacationHomeworkResult != null && vacationHomeworkResult.isFinished()) {
            return MapMessage.errorMessage("该作业已完成");
        }
        NewHomeworkPracticeContent dubbingContent = practiceContentMap.get(ObjectiveConfigType.DUBBING);
        NewHomeworkPracticeContent dubbingWithScoreContent = practiceContentMap.get(ObjectiveConfigType.DUBBING_WITH_SCORE);
        ObjectiveConfigType containedDubbingType = null;
        NewHomeworkPracticeContent containedAllDubbingPractise = null;
        if ((dubbingContent == null || CollectionUtils.isEmpty(dubbingContent.getApps()))
                && (dubbingWithScoreContent == null || CollectionUtils.isEmpty(dubbingWithScoreContent.getApps()))) {
            return MapMessage.errorMessage("该作业没有趣味配音内容");
        }
        if (dubbingContent != null) {
            containedAllDubbingPractise = practiceContentMap.get(ObjectiveConfigType.DUBBING);
            containedDubbingType = ObjectiveConfigType.DUBBING;
        }
        if (dubbingWithScoreContent != null) {
            containedAllDubbingPractise = practiceContentMap.get(ObjectiveConfigType.DUBBING_WITH_SCORE);
            containedDubbingType = ObjectiveConfigType.DUBBING_WITH_SCORE;
        }
        NewHomeworkResultAnswer containedDubbingResultAnswer = null;
        if (vacationHomeworkResult != null && MapUtils.isNotEmpty(vacationHomeworkResult.getPractices())) {
            containedDubbingResultAnswer = vacationHomeworkResult.getPractices().get(ObjectiveConfigType.DUBBING);
        }
        if (vacationHomeworkResult != null && MapUtils.isNotEmpty(vacationHomeworkResult.getPractices())) {
            containedDubbingResultAnswer = vacationHomeworkResult.getPractices().get(ObjectiveConfigType.DUBBING_WITH_SCORE);
        }
        if ((containedDubbingResultAnswer != null && containedDubbingResultAnswer.isFinished())
        ) {
            return MapMessage.errorMessage("趣味配音内容已完成");
        }
        Set<String> dubbingIds = containedAllDubbingPractise.getApps()
                .stream()
                .map(NewHomeworkApp::getDubbingId)
                .collect(Collectors.toSet());
        Map<String, Dubbing> dubbingMap = dubbingLoaderClient.loadDubbingByIdsIncludeDisabled(dubbingIds);

        for (NewHomeworkApp app : containedAllDubbingPractise.getApps()) {
            String dubbingId = app.getDubbingId();
            if (containedDubbingResultAnswer != null && MapUtils.isNotEmpty(containedDubbingResultAnswer.getAppAnswers())) {
                NewHomeworkResultAppAnswer appAnswer = containedDubbingResultAnswer.getAppAnswers().get(dubbingId);
                if (appAnswer != null && appAnswer.isFinished()) {
                    continue;
                }
            }
            Dubbing dubbing = dubbingMap.get(dubbingId);
            if (dubbing == null) {
                return MapMessage.errorMessage("配音{}不存在", dubbingId);
            }
            List<NewHomeworkQuestion> newHomeworkQuestions = app.getQuestions();
            if (CollectionUtils.isEmpty(newHomeworkQuestions)) {
                return MapMessage.errorMessage("配音{}题目列表为空", dubbingId);
            }
            long consumerTime = 5000L * app.getQuestions().size();
            VacationHomeworkResultContext homeworkResultContext = new VacationHomeworkResultContext();
            homeworkResultContext.setUserId(vacationHomework.getStudentId());
            homeworkResultContext.setUser(student);
            homeworkResultContext.setVacationHomeworkId(homeworkId);
            homeworkResultContext.setObjectiveConfigType(containedDubbingType);
            homeworkResultContext.setDubbingId(app.getDubbingId());
            homeworkResultContext.setVideoUrl(dubbing.getVideoUrl());
            homeworkResultContext.setConsumeTime(consumerTime);
            homeworkResultContext.setClientType("crm");
            homeworkResultContext.setClientName("crm");
            homeworkResultContext.setLearningType(StudyType.vacationHomework);

            List<StudentHomeworkAnswer> studentHomeworkAnswers = new ArrayList<>();
            for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkQuestions) {
                StudentHomeworkAnswer studentHomeworkAnswer = new StudentHomeworkAnswer();
                studentHomeworkAnswer.setQuestionId(newHomeworkQuestion.getQuestionId());
                studentHomeworkAnswer.setDurationMilliseconds(5000L);
                if (containedDubbingType == ObjectiveConfigType.DUBBING_WITH_SCORE) {
                    studentHomeworkAnswer.setVoiceScoringMode("Normal");
                    studentHomeworkAnswer.setVoiceEngineType(VoiceEngineType.Vox17);
                    List<List<NewHomeworkProcessResult.OralDetail>> oralScoreDetails = Lists.newArrayList();
                    NewHomeworkProcessResult.OralDetail oralDetail = new NewHomeworkProcessResult.OralDetail();
                    oralDetail.setMacScore(100);
                    oralDetail.setStandardScore(8);
                    oralScoreDetails.add(Lists.newArrayList(oralDetail));
                    studentHomeworkAnswer.setOralScoreDetails(oralScoreDetails);
                }
                studentHomeworkAnswers.add(studentHomeworkAnswer);
                homeworkResultContext.setStudentHomeworkAnswers(studentHomeworkAnswers);
            }

            MapMessage mapMessage = processVacationHomeworkResult(homeworkResultContext);
            if (!mapMessage.isSuccess()) {
                return MapMessage.errorMessage(mapMessage.getInfo());
            }
        }
        return MapMessage.successMessage();
    }

    private MapMessage processExam(ObjectiveConfigType objectiveConfigType, NewHomeworkPracticeContent newHomeworkPracticeContent,
                                   Long userId, Student student, String homeworkId, VacationHomeworkBook vacationHomeworkBook) {
        List<NewHomeworkQuestion> newHomeworkQuestions = newHomeworkPracticeContent.getQuestions();
        Set<String> questionIds = new LinkedHashSet<>();
        for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkQuestions) {
            String qid = newHomeworkQuestion.getQuestionId();
            questionIds.add(qid);
        }
        Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        Map<String, NewHomeworkBookInfo> bookInfoMap = new HashMap<>();
        if (vacationHomeworkBook != null && MapUtils.isNotEmpty(vacationHomeworkBook.getPractices())) {
            List<NewHomeworkBookInfo> newHomeworkBookInfos = vacationHomeworkBook.getPractices().get(objectiveConfigType);
            if (newHomeworkBookInfos != null) {
                for (NewHomeworkBookInfo info : newHomeworkBookInfos) {
                    if (CollectionUtils.isNotEmpty(info.getQuestions())) {
                        for (String qid : info.getQuestions()) {
                            bookInfoMap.put(qid, info);
                        }
                    }
                }
            }
        }
        for (String qid : questionIds) {
            VacationHomeworkResultContext homeworkResultContext = new VacationHomeworkResultContext();
            homeworkResultContext.setUserId(userId);
            homeworkResultContext.setUser(student);
            homeworkResultContext.setVacationHomeworkId(homeworkId);
            homeworkResultContext.setObjectiveConfigType(objectiveConfigType);
            homeworkResultContext.setClientType("pc");
            homeworkResultContext.setClientName("pc");
            homeworkResultContext.setLearningType(StudyType.vacationHomework);

            List<StudentHomeworkAnswer> studentHomeworkAnswers = new ArrayList<>();
            NewQuestion question = questionMap.get(qid);
            StudentHomeworkAnswer studentHomeworkAnswer = new StudentHomeworkAnswer();
            studentHomeworkAnswer.setQuestionId(qid);
            studentHomeworkAnswer.setAnswer(question.getAnswers());
            studentHomeworkAnswer.setDurationMilliseconds(2000L);
            studentHomeworkAnswers.add(studentHomeworkAnswer);
            homeworkResultContext.setStudentHomeworkAnswers(studentHomeworkAnswers);

            NewHomeworkBookInfo newHomeworkBookInfo = bookInfoMap.get(qid);
            if (newHomeworkBookInfo != null) {
                homeworkResultContext.setBookId(newHomeworkBookInfo.getBookId());
                homeworkResultContext.setUnitId(newHomeworkBookInfo.getUnitId());
                homeworkResultContext.setSectionId(newHomeworkBookInfo.getSectionId());
            }

            MapMessage mapMessage = processVacationHomeworkResult(homeworkResultContext);
            if (!mapMessage.isSuccess()) {
                return MapMessage.errorMessage(mapMessage.getInfo());
            }
        }
        return MapMessage.successMessage();
    }

    private MapMessage processKeyPoints(ObjectiveConfigType objectiveConfigType, NewHomeworkPracticeContent newHomeworkPracticeContent,
                                        Long userId, Student student, String homeworkId) {
        List<NewHomeworkApp> newHomeworkApps = newHomeworkPracticeContent.getApps();
        if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
            Set<String> questionIds = newHomeworkApps
                    .stream()
                    .filter(app -> CollectionUtils.isNotEmpty(app.getQuestions()))
                    .map(NewHomeworkApp::getQuestions)
                    .flatMap(Collection::stream)
                    .map(NewHomeworkQuestion::getQuestionId)
                    .collect(Collectors.toSet());
            Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
            for (NewHomeworkApp newHomeworkApp : newHomeworkApps) {
                List<NewHomeworkQuestion> newHomeworkQuestions = newHomeworkApp.getQuestions();
                if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                    for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkQuestions) {
                        VacationHomeworkResultContext homeworkResultContext = new VacationHomeworkResultContext();
                        homeworkResultContext.setUserId(userId);
                        homeworkResultContext.setUser(student);
                        homeworkResultContext.setVacationHomeworkId(homeworkId);
                        homeworkResultContext.setObjectiveConfigType(objectiveConfigType);
                        homeworkResultContext.setVideoId(newHomeworkApp.getVideoId());
                        homeworkResultContext.setClientType("pc");
                        homeworkResultContext.setClientName("pc");
                        homeworkResultContext.setLearningType(StudyType.vacationHomework);

                        List<StudentHomeworkAnswer> studentHomeworkAnswers = new ArrayList<>();
                        NewQuestion question = questionMap.get(newHomeworkQuestion.getQuestionId());
                        StudentHomeworkAnswer studentHomeworkAnswer = new StudentHomeworkAnswer();
                        studentHomeworkAnswer.setQuestionId(newHomeworkQuestion.getQuestionId());
                        studentHomeworkAnswer.setAnswer(question.getAnswers());
                        studentHomeworkAnswer.setDurationMilliseconds(2000L);
                        studentHomeworkAnswers.add(studentHomeworkAnswer);
                        homeworkResultContext.setStudentHomeworkAnswers(studentHomeworkAnswers);

                        MapMessage mapMessage = processVacationHomeworkResult(homeworkResultContext);
                        if (!mapMessage.isSuccess()) {
                            return MapMessage.errorMessage(mapMessage.getInfo());
                        }
                    }
                }
            }
        }
        return MapMessage.successMessage();
    }

    private MapMessage processBasicApp(ObjectiveConfigType objectiveConfigType, NewHomeworkPracticeContent newHomeworkPracticeContent,
                                       Long userId, Student student, String homeworkId) {
        List<NewHomeworkApp> newHomeworkApps = newHomeworkPracticeContent.getApps();
        if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
            Set<String> questionIds = newHomeworkApps
                    .stream()
                    .filter(app -> CollectionUtils.isNotEmpty(app.getQuestions()))
                    .map(NewHomeworkApp::getQuestions)
                    .flatMap(Collection::stream)
                    .map(NewHomeworkQuestion::getQuestionId)
                    .collect(Collectors.toSet());
            Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
            for (NewHomeworkApp newHomeworkApp : newHomeworkApps) {
                List<NewHomeworkQuestion> newHomeworkQuestions = newHomeworkApp.getQuestions();
                if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                    VacationHomeworkResultContext homeworkResultContext = new VacationHomeworkResultContext();
                    homeworkResultContext.setUserId(userId);
                    homeworkResultContext.setUser(student);
                    homeworkResultContext.setVacationHomeworkId(homeworkId);
                    homeworkResultContext.setObjectiveConfigType(objectiveConfigType);
                    homeworkResultContext.setPracticeId(newHomeworkApp.getPracticeId());
                    homeworkResultContext.setLessonId(newHomeworkApp.getLessonId());
                    homeworkResultContext.setClientType("pc");
                    homeworkResultContext.setClientName("pc");
                    homeworkResultContext.setLearningType(StudyType.vacationHomework);

                    List<StudentHomeworkAnswer> studentHomeworkAnswers = new ArrayList<>();
                    List<PracticeType> practiceTypes = practiceLoaderClient.loadCategoriedIdPractices(newHomeworkApp.getCategoryId());
                    boolean needRecord = practiceTypes.get(0).getNeedRecord();
                    for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkQuestions) {
                        NewQuestion question = questionMap.get(newHomeworkQuestion.getQuestionId());
                        StudentHomeworkAnswer studentHomeworkAnswer = new StudentHomeworkAnswer();
                        studentHomeworkAnswer.setQuestionId(newHomeworkQuestion.getQuestionId());
                        studentHomeworkAnswer.setAnswer(question.getAnswers());
                        studentHomeworkAnswer.setDurationMilliseconds(2000L);
                        if (needRecord) {
                            studentHomeworkAnswer.setVoiceEngineType(VoiceEngineType.ChiVox);
                            studentHomeworkAnswer.setVoiceScoringMode("Normal");
                            List<List<NewHomeworkProcessResult.OralDetail>> oralScoreDetails = new ArrayList<>();
                            List<NewHomeworkProcessResult.OralDetail> oralDetails = new ArrayList<>();
                            NewHomeworkProcessResult.OralDetail oralDetail = new NewHomeworkProcessResult.OralDetail();
                            oralDetail.setMacScore(100);
                            oralDetail.setPronunciation(100);
                            oralDetail.setFluency(100);
                            oralDetail.setIntegrity(100);
                            oralDetail.setAudio("download.cloud.chivox.com:8002/593663f62dfd5e9d4b0ab4b5");
                            oralDetails.add(oralDetail);
                            oralScoreDetails.add(oralDetails);
                            studentHomeworkAnswer.setOralScoreDetails(oralScoreDetails);
                        }
                        studentHomeworkAnswers.add(studentHomeworkAnswer);
                        homeworkResultContext.setStudentHomeworkAnswers(studentHomeworkAnswers);
                    }

                    MapMessage mapMessage = processVacationHomeworkResult(homeworkResultContext);
                    if (!mapMessage.isSuccess()) {
                        return MapMessage.errorMessage(mapMessage.getInfo());
                    }
                }
            }
        }
        return MapMessage.successMessage();
    }

    private MapMessage processNaturalSpelling(ObjectiveConfigType objectiveConfigType,
                                              NewHomeworkPracticeContent newHomeworkPracticeContent,
                                              Long userId, Student student, String homeworkId) {
        List<NewHomeworkApp> newHomeworkApps = newHomeworkPracticeContent.getApps();
        if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
            Set<String> questionIds = newHomeworkApps
                    .stream()
                    .filter(app -> CollectionUtils.isNotEmpty(app.getQuestions()))
                    .map(NewHomeworkApp::getQuestions)
                    .flatMap(Collection::stream)
                    .map(NewHomeworkQuestion::getQuestionId)
                    .collect(Collectors.toSet());
            Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
            for (NewHomeworkApp newHomeworkApp : newHomeworkApps) {
                List<NewHomeworkQuestion> newHomeworkQuestions = newHomeworkApp.getQuestions();
                if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                    VacationHomeworkResultContext homeworkResultContext = new VacationHomeworkResultContext();
                    homeworkResultContext.setUserId(userId);
                    homeworkResultContext.setUser(student);
                    homeworkResultContext.setVacationHomeworkId(homeworkId);
                    homeworkResultContext.setObjectiveConfigType(objectiveConfigType);
                    homeworkResultContext.setPracticeId(newHomeworkApp.getPracticeId());
                    homeworkResultContext.setLessonId(newHomeworkApp.getLessonId());
                    homeworkResultContext.setClientType("pc");
                    homeworkResultContext.setClientName("pc");
                    homeworkResultContext.setLearningType(StudyType.vacationHomework);
                    List<StudentHomeworkAnswer> studentHomeworkAnswers = new ArrayList<>();
                    List<PracticeType> practiceTypes = practiceLoaderClient.loadCategoriedIdPractices(newHomeworkApp.getCategoryId());
                    boolean needRecord = practiceTypes.get(0).getNeedRecord();
                    for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkQuestions) {
                        NewQuestion question = questionMap.get(newHomeworkQuestion.getQuestionId());
                        StudentHomeworkAnswer studentHomeworkAnswer = new StudentHomeworkAnswer();
                        studentHomeworkAnswer.setQuestionId(newHomeworkQuestion.getQuestionId());
                        studentHomeworkAnswer.setAnswer(question.getAnswers());
                        studentHomeworkAnswer.setDurationMilliseconds(2000L);
                        if (needRecord) {
                            studentHomeworkAnswer.setVoiceEngineType(VoiceEngineType.ChiVox);
                            studentHomeworkAnswer.setVoiceScoringMode("Normal");
                            List<List<NewHomeworkProcessResult.OralDetail>> oralScoreDetails = new ArrayList<>();
                            List<NewHomeworkProcessResult.OralDetail> oralDetails = new ArrayList<>();
                            NewHomeworkProcessResult.OralDetail oralDetail = new NewHomeworkProcessResult.OralDetail();
                            oralDetail.setMacScore(100);
                            oralDetail.setPronunciation(100);
                            oralDetail.setFluency(100);
                            oralDetail.setIntegrity(100);
                            oralDetail.setStandardScore(8);
                            oralDetail.setBusinessLevel(1.5F);
                            oralDetail.setAudio("https://edu.hivoice.cn/WebAudio-1.0-SNAPSHOT/audio/play/6CEDD66D-1A41-456A-837C-48593744D50B/1512734460202528001/sh");
                            if (NatureSpellingType.TONGUE_TWISTER.getCategoryId() == practiceTypes.get(0).getCategoryId()) {
                                List<Long> sentenceIds = question.getSentenceIds();
                                List<NaturalSpellingSentence> naturalSpellingSentences = new ArrayList<>();
                                Map<Long, Sentence> sentenceMap = englishContentLoaderClient.loadEnglishSentences(sentenceIds);
                                if (MapUtils.isNotEmpty(sentenceMap)) {
                                    for (Long sentenceId : sentenceIds) {
                                        NaturalSpellingSentence naturalSpellingSentence = new NaturalSpellingSentence();
                                        Sentence sentence = sentenceMap.get(sentenceId);
                                        if (sentence != null) {
                                            String enText = sentence.getEnText();
                                            naturalSpellingSentence.setSample(enText);
                                            naturalSpellingSentence.setScore(100D);
                                            naturalSpellingSentence.setStandardScore(8);
                                            List<NaturalSpellingSentence.Word> words = new ArrayList<>();
                                            if (StringUtils.isNotBlank(enText)) {
                                                String[] enTextStr = enText.trim().replaceAll("[\\pP‘’“”]", "").split("\\s+");
                                                if (enTextStr.length > 0) {
                                                    for (String s : enTextStr) {
                                                        NaturalSpellingSentence.Word word = new NaturalSpellingSentence.Word();
                                                        word.setText(s);
                                                        word.setScore(10F);
                                                        words.add(word);
                                                    }
                                                }
                                            }
                                            naturalSpellingSentence.setWords(words);
                                        }
                                        naturalSpellingSentences.add(naturalSpellingSentence);
                                    }
                                }
                                oralDetail.setSentences(naturalSpellingSentences);
                            }
                            oralDetails.add(oralDetail);
                            oralScoreDetails.add(oralDetails);
                            studentHomeworkAnswer.setOralScoreDetails(oralScoreDetails);
                        }
                        studentHomeworkAnswers.add(studentHomeworkAnswer);
                        homeworkResultContext.setStudentHomeworkAnswers(studentHomeworkAnswers);
                    }
                    MapMessage mapMessage = processVacationHomeworkResult(homeworkResultContext);
                    if (!mapMessage.isSuccess()) {
                        return MapMessage.errorMessage(mapMessage.getInfo());
                    }
                }
            }
        }
        return MapMessage.successMessage();
    }

    private MapMessage processReading(ObjectiveConfigType objectiveConfigType, NewHomeworkPracticeContent newHomeworkPracticeContent,
                                      Long userId, Student student, String homeworkId) {
        List<NewHomeworkApp> newHomeworkApps = newHomeworkPracticeContent.getApps();
        if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
            Set<String> questionIds = newHomeworkApps
                    .stream()
                    .filter(app -> CollectionUtils.isNotEmpty(app.getQuestions()))
                    .map(NewHomeworkApp::getQuestions)
                    .flatMap(Collection::stream)
                    .map(NewHomeworkQuestion::getQuestionId)
                    .collect(Collectors.toSet());
            Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
            for (NewHomeworkApp newHomeworkApp : newHomeworkApps) {
                List<NewHomeworkQuestion> newHomeworkQuestions = newHomeworkApp.getQuestions();
                List<NewHomeworkQuestion> newHomeworkOralQuestions = newHomeworkApp.getOralQuestions();
                if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                    VacationHomeworkResultContext homeworkResultContext = new VacationHomeworkResultContext();
                    homeworkResultContext.setUserId(userId);
                    homeworkResultContext.setUser(student);
                    homeworkResultContext.setVacationHomeworkId(homeworkId);
                    homeworkResultContext.setObjectiveConfigType(objectiveConfigType);
                    homeworkResultContext.setPictureBookId(newHomeworkApp.getPictureBookId());
                    homeworkResultContext.setConsumeTime(132000L);
                    homeworkResultContext.setClientType("pc");
                    homeworkResultContext.setClientName("pc");
                    homeworkResultContext.setLearningType(StudyType.vacationHomework);

                    List<StudentHomeworkAnswer> studentHomeworkAnswers = new ArrayList<>();
                    for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkQuestions) {
                        NewQuestion question = questionMap.get(newHomeworkQuestion.getQuestionId());
                        StudentHomeworkAnswer studentHomeworkAnswer = new StudentHomeworkAnswer();
                        studentHomeworkAnswer.setQuestionId(newHomeworkQuestion.getQuestionId());
                        studentHomeworkAnswer.setAnswer(question.getAnswers());
                        studentHomeworkAnswer.setDurationMilliseconds(2000L);
                        studentHomeworkAnswers.add(studentHomeworkAnswer);
                    }
                    homeworkResultContext.setStudentHomeworkAnswers(studentHomeworkAnswers);

                    if (CollectionUtils.isNotEmpty(newHomeworkOralQuestions)) {
                        List<StudentHomeworkAnswer> studentHomeworkOralAnswers = new ArrayList<>();
                        for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkOralQuestions) {
                            StudentHomeworkAnswer studentHomeworkAnswer = new StudentHomeworkAnswer();
                            studentHomeworkAnswer.setQuestionId(newHomeworkQuestion.getQuestionId());
                            studentHomeworkAnswer.setVoiceScoringMode("Normal");
                            studentHomeworkAnswer.setVoiceEngineType(VoiceEngineType.ChiVox);
                            studentHomeworkAnswer.setVoiceMode("");
                            studentHomeworkAnswer.setDurationMilliseconds(2000L);
                            studentHomeworkAnswer.setVoiceCoefficient("");
                            List<List<NewHomeworkProcessResult.OralDetail>> oralScoreDetails = new ArrayList<>();
                            List<NewHomeworkProcessResult.OralDetail> oralDetails = new ArrayList<>();
                            NewHomeworkProcessResult.OralDetail oralDetail = new NewHomeworkProcessResult.OralDetail();
                            oralDetail.setMacScore(100);
                            oralDetail.setPronunciation(100);
                            oralDetail.setFluency(100);
                            oralDetail.setIntegrity(100);
                            oralDetail.setAudio("download.cloud.chivox.com:8002/593663f62dfd5e9d4b0ab4b5");
                            oralDetails.add(oralDetail);
                            oralScoreDetails.add(oralDetails);
                            studentHomeworkAnswer.setOralScoreDetails(oralScoreDetails);
                            studentHomeworkOralAnswers.add(studentHomeworkAnswer);
                        }
                        homeworkResultContext.setStudentHomeworkOralAnswers(studentHomeworkOralAnswers);
                    }

                    MapMessage mapMessage = processVacationHomeworkResult(homeworkResultContext);
                    if (!mapMessage.isSuccess()) {
                        return MapMessage.errorMessage(mapMessage.getInfo());
                    }
                }
            }
        }
        return MapMessage.successMessage();
    }

    private MapMessage processDubbing(ObjectiveConfigType objectiveConfigType, NewHomeworkPracticeContent newHomeworkPracticeContent,
                                      Long userId, Student student, String homeworkId) {
        List<NewHomeworkApp> newHomeworkApps = newHomeworkPracticeContent.getApps();
        if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
            Set<String> questionIds = newHomeworkApps
                    .stream()
                    .filter(app -> CollectionUtils.isNotEmpty(app.getQuestions()))
                    .map(NewHomeworkApp::getQuestions)
                    .flatMap(Collection::stream)
                    .map(NewHomeworkQuestion::getQuestionId)
                    .collect(Collectors.toSet());
            Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
            for (NewHomeworkApp newHomeworkApp : newHomeworkApps) {
                List<NewHomeworkQuestion> newHomeworkQuestions = newHomeworkApp.getQuestions();
                if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                    VacationHomeworkResultContext homeworkResultContext = new VacationHomeworkResultContext();
                    homeworkResultContext.setUserId(userId);
                    homeworkResultContext.setUser(student);
                    homeworkResultContext.setVacationHomeworkId(homeworkId);
                    homeworkResultContext.setObjectiveConfigType(objectiveConfigType);
                    homeworkResultContext.setDubbingId(newHomeworkApp.getDubbingId());
                    homeworkResultContext.setVideoUrl("https://17zy-homework.oss-cn-beijing.aliyuncs.com/dubbing/prod/D_10300001379952-1_1509102715176.mp4");
                    homeworkResultContext.setConsumeTime(132000L);
                    homeworkResultContext.setClientType("pc");
                    homeworkResultContext.setClientName("pc");
                    homeworkResultContext.setLearningType(StudyType.vacationHomework);

                    List<StudentHomeworkAnswer> studentHomeworkAnswers = new ArrayList<>();
                    for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkQuestions) {
                        NewQuestion question = questionMap.get(newHomeworkQuestion.getQuestionId());
                        StudentHomeworkAnswer studentHomeworkAnswer = new StudentHomeworkAnswer();
                        studentHomeworkAnswer.setQuestionId(newHomeworkQuestion.getQuestionId());
                        studentHomeworkAnswer.setAnswer(question.getAnswers());
                        studentHomeworkAnswer.setDurationMilliseconds(2000L);
                        studentHomeworkAnswers.add(studentHomeworkAnswer);
                        homeworkResultContext.setStudentHomeworkAnswers(studentHomeworkAnswers);
                    }

                    MapMessage mapMessage = processVacationHomeworkResult(homeworkResultContext);
                    if (!mapMessage.isSuccess()) {
                        return MapMessage.errorMessage(mapMessage.getInfo());
                    }
                }
            }
        }
        return MapMessage.successMessage();
    }

    private MapMessage processReadRecite(ObjectiveConfigType objectiveConfigType, NewHomeworkPracticeContent newHomeworkPracticeContent,
                                         Long userId, Student student, String homeworkId) {
        List<NewHomeworkQuestion> newHomeworkQuestions = newHomeworkPracticeContent.getQuestions();
        Set<String> questionIds = new LinkedHashSet<>();
        for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkQuestions) {
            String qid = newHomeworkQuestion.getQuestionId();
            questionIds.add(qid);
        }
        Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        for (String qid : questionIds) {
            VacationHomeworkResultContext homeworkResultContext = new VacationHomeworkResultContext();
            homeworkResultContext.setUserId(userId);
            homeworkResultContext.setUser(student);
            homeworkResultContext.setVacationHomeworkId(homeworkId);
            homeworkResultContext.setObjectiveConfigType(objectiveConfigType);
            homeworkResultContext.setClientType("pc");
            homeworkResultContext.setClientName("pc");
            homeworkResultContext.setLearningType(StudyType.vacationHomework);

            List<StudentHomeworkAnswer> studentHomeworkAnswers = new ArrayList<>();
            NewQuestion question = questionMap.get(qid);
            StudentHomeworkAnswer studentHomeworkAnswer = new StudentHomeworkAnswer();
            studentHomeworkAnswer.setQuestionId(qid);
            studentHomeworkAnswer.setAnswer(question.getAnswers());
            studentHomeworkAnswer.setDurationMilliseconds(2000L);
            List<List<String>> fileUrls = new ArrayList<>();
            List<String> files = new ArrayList<>();
            files.add("https://oss-data.17zuoye.com/test2017/06/06/20170606182529238196.mp3");
            fileUrls.add(files);
            studentHomeworkAnswer.setFileUrls(fileUrls);
            studentHomeworkAnswers.add(studentHomeworkAnswer);
            homeworkResultContext.setStudentHomeworkAnswers(studentHomeworkAnswers);

            MapMessage mapMessage = processVacationHomeworkResult(homeworkResultContext);
            if (!mapMessage.isSuccess()) {
                return MapMessage.errorMessage(mapMessage.getInfo());
            }
        }
        return MapMessage.successMessage();
    }

    private MapMessage processNewReadRecite(ObjectiveConfigType objectiveConfigType, NewHomeworkPracticeContent newHomeworkPracticeContent,
                                            Long userId, Student student, String homeworkId) {
        List<NewHomeworkApp> newHomeworkApps = newHomeworkPracticeContent.getApps();
        if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
            for (NewHomeworkApp newHomeworkApp : newHomeworkApps) {
                List<NewHomeworkQuestion> newHomeworkQuestions = newHomeworkApp.getQuestions();
                if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                    for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkQuestions) {
                        VacationHomeworkResultContext homeworkResultContext = new VacationHomeworkResultContext();
                        homeworkResultContext.setUserId(userId);
                        homeworkResultContext.setUser(student);
                        homeworkResultContext.setVacationHomeworkId(homeworkId);
                        homeworkResultContext.setObjectiveConfigType(objectiveConfigType);
                        homeworkResultContext.setQuestionBoxId(newHomeworkApp.getQuestionBoxId());
                        homeworkResultContext.setLessonId(newHomeworkApp.getLessonId());
                        homeworkResultContext.setQuestionBoxType(newHomeworkApp.getQuestionBoxType());
                        homeworkResultContext.setClientType("pc");
                        homeworkResultContext.setClientName("pc");
                        homeworkResultContext.setLearningType(StudyType.vacationHomework);

                        List<StudentHomeworkAnswer> studentHomeworkAnswers = new ArrayList<>();
                        StudentHomeworkAnswer studentHomeworkAnswer = new StudentHomeworkAnswer();
                        studentHomeworkAnswer.setQuestionId(newHomeworkQuestion.getQuestionId());
                        studentHomeworkAnswer.setAnswer(Collections.singletonList(Collections.emptyList()));
                        studentHomeworkAnswer.setFileUrls(Collections.singletonList(Collections.singletonList("https://oss-data.17zuoye.com/test2017/12/06/20171206185327067860.mp3")));
                        studentHomeworkAnswer.setDurationMilliseconds(132998L);
                        studentHomeworkAnswers.add(studentHomeworkAnswer);
                        homeworkResultContext.setStudentHomeworkAnswers(studentHomeworkAnswers);

                        MapMessage mapMessage = processVacationHomeworkResult(homeworkResultContext);
                        if (!mapMessage.isSuccess()) {
                            return MapMessage.errorMessage(mapMessage.getInfo());
                        }
                    }
                }
            }
        }
        return MapMessage.successMessage();
    }

    private MapMessage processReadReciteWithScore(ObjectiveConfigType objectiveConfigType, NewHomeworkPracticeContent newHomeworkPracticeContent,
                                                  Long userId, Student student, String homeworkId) {
        List<NewHomeworkApp> newHomeworkApps = newHomeworkPracticeContent.getApps();
        if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
            for (NewHomeworkApp newHomeworkApp : newHomeworkApps) {
                List<NewHomeworkQuestion> newHomeworkQuestions = newHomeworkApp.getQuestions();
                if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                    for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkQuestions) {
                        VacationHomeworkResultContext homeworkResultContext = new VacationHomeworkResultContext();
                        homeworkResultContext.setUserId(userId);
                        homeworkResultContext.setUser(student);
                        homeworkResultContext.setVacationHomeworkId(homeworkId);
                        homeworkResultContext.setObjectiveConfigType(objectiveConfigType);
                        homeworkResultContext.setQuestionBoxId(newHomeworkApp.getQuestionBoxId());
                        homeworkResultContext.setLessonId(newHomeworkApp.getLessonId());
                        homeworkResultContext.setQuestionBoxType(newHomeworkApp.getQuestionBoxType());
                        homeworkResultContext.setClientType("pc");
                        homeworkResultContext.setClientName("pc");
                        homeworkResultContext.setLearningType(StudyType.vacationHomework);

                        List<StudentHomeworkAnswer> studentHomeworkAnswers = new ArrayList<>();
                        StudentHomeworkAnswer studentHomeworkAnswer = new StudentHomeworkAnswer();
                        studentHomeworkAnswer.setQuestionId(newHomeworkQuestion.getQuestionId());
                        studentHomeworkAnswer.setDurationMilliseconds(132998L);
                        studentHomeworkAnswer.setVoiceScoringMode("Normal");
                        studentHomeworkAnswer.setVoiceEngineType(VoiceEngineType.Unisound);
                        studentHomeworkAnswer.setSentenceType(0);
                        List<List<NewHomeworkProcessResult.OralDetail>> oralScoreDetails = new ArrayList<>();
                        NewHomeworkProcessResult.OralDetail oralDetail = new NewHomeworkProcessResult.OralDetail();
                        oralDetail.setFluency(100);
                        oralDetail.setIntegrity(100);
                        oralDetail.setPronunciation(100);
                        oralDetail.setAudio("https://edu.hivoice.cn/WebAudio-1.0-SNAPSHOT/audio/play/905B7C0F-0BD6-4EBC-9149-D5A0E73FB24A/1528117324125632536/bj");
                        oralDetail.setMacScore(100);
                        oralDetail.setStandardScore(6);
                        oralDetail.setBusinessLevel(1.4F);
                        studentHomeworkAnswer.setOralScoreDetails(oralScoreDetails);
                        oralScoreDetails.add(Collections.singletonList(oralDetail));
                        studentHomeworkAnswer.setOralScoreDetails(oralScoreDetails);
                        studentHomeworkAnswers.add(studentHomeworkAnswer);
                        homeworkResultContext.setStudentHomeworkAnswers(studentHomeworkAnswers);
                        MapMessage mapMessage = processVacationHomeworkResult(homeworkResultContext);
                        if (!mapMessage.isSuccess()) {
                            return MapMessage.errorMessage(mapMessage.getInfo());
                        }
                    }
                }
            }
        }
        return MapMessage.successMessage();
    }

    private MapMessage processLevelReadings(ObjectiveConfigType objectiveConfigType, NewHomeworkPracticeContent newHomeworkPracticeContent,
                                            Long userId, Student student, String homeworkId) {
        List<NewHomeworkApp> newHomeworkApps = newHomeworkPracticeContent.getApps();
        if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
            Set<String> questionIds = newHomeworkApps
                    .stream()
                    .filter(app -> CollectionUtils.isNotEmpty(app.getQuestions()))
                    .map(NewHomeworkApp::getQuestions)
                    .flatMap(Collection::stream)
                    .map(NewHomeworkQuestion::getQuestionId)
                    .collect(Collectors.toSet());
            Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
            for (NewHomeworkApp newHomeworkApp : newHomeworkApps) {
                VacationHomeworkResultContext homeworkResultContext = new VacationHomeworkResultContext();
                homeworkResultContext.setUserId(userId);
                homeworkResultContext.setUser(student);
                homeworkResultContext.setVacationHomeworkId(homeworkId);
                homeworkResultContext.setObjectiveConfigType(objectiveConfigType);
                homeworkResultContext.setPictureBookId(newHomeworkApp.getPictureBookId());
                homeworkResultContext.setClientType("pc");
                homeworkResultContext.setClientName("pc");
                homeworkResultContext.setLearningType(StudyType.vacationHomework);

                List<NewHomeworkQuestion> newHomeworkQuestions = newHomeworkApp.getQuestions();
                List<NewHomeworkQuestion> newHomeworkOralQuestions = newHomeworkApp.getOralQuestions();
                Map<String, Long> durations = new LinkedHashMap<>();
                durations.put(PictureBookPracticeType.READING.name(), 8319L);
                durations.put(PictureBookPracticeType.WORDS.name(), 16878L);
                if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                    durations.put(PictureBookPracticeType.EXAM.name(), 11583L);
                }
                if (CollectionUtils.isNotEmpty(newHomeworkOralQuestions)) {
                    durations.put(PictureBookPracticeType.ORAL.name(), 15250L);
                }
                homeworkResultContext.setDurations(durations);

                if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                    List<StudentHomeworkAnswer> studentHomeworkAnswers = new ArrayList<>();
                    for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkQuestions) {
                        NewQuestion question = questionMap.get(newHomeworkQuestion.getQuestionId());
                        StudentHomeworkAnswer studentHomeworkAnswer = new StudentHomeworkAnswer();
                        studentHomeworkAnswer.setQuestionId(newHomeworkQuestion.getQuestionId());
                        studentHomeworkAnswer.setAnswer(question.getAnswers());
                        studentHomeworkAnswer.setDurationMilliseconds(2000L);
                        studentHomeworkAnswers.add(studentHomeworkAnswer);
                    }
                    homeworkResultContext.setStudentHomeworkAnswers(studentHomeworkAnswers);
                }
                if (CollectionUtils.isNotEmpty(newHomeworkOralQuestions)) {
                    List<StudentHomeworkAnswer> studentHomeworkOralAnswers = new ArrayList<>();
                    for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkOralQuestions) {
                        StudentHomeworkAnswer studentHomeworkAnswer = new StudentHomeworkAnswer();
                        studentHomeworkAnswer.setQuestionId(newHomeworkQuestion.getQuestionId());
                        studentHomeworkAnswer.setVoiceScoringMode("Normal");
                        studentHomeworkAnswer.setVoiceEngineType(VoiceEngineType.Unisound);
                        studentHomeworkAnswer.setVoiceMode("Normal");
                        studentHomeworkAnswer.setDurationMilliseconds(2000L);
                        studentHomeworkAnswer.setVoiceCoefficient("1.5");
                        List<List<NewHomeworkProcessResult.OralDetail>> oralScoreDetails = new ArrayList<>();
                        List<NewHomeworkProcessResult.OralDetail> oralDetails = new ArrayList<>();
                        NewHomeworkProcessResult.OralDetail oralDetail = new NewHomeworkProcessResult.OralDetail();
                        oralDetail.setMacScore(100);
                        oralDetail.setPronunciation(100);
                        oralDetail.setFluency(100);
                        oralDetail.setIntegrity(100);
                        oralDetail.setBusinessLevel(1.5F);
                        oralDetail.setAudio("https://edu.hivoice.cn/WebAudio-1.0-SNAPSHOT/audio/play/BBDCE004-C7C3-4818-A0DD-79D810614D91/1528115442407018372/bj");
                        oralDetails.add(oralDetail);
                        oralScoreDetails.add(oralDetails);
                        studentHomeworkAnswer.setOralScoreDetails(oralScoreDetails);
                        studentHomeworkOralAnswers.add(studentHomeworkAnswer);
                    }
                    homeworkResultContext.setStudentHomeworkOralAnswers(studentHomeworkOralAnswers);
                }
                MapMessage mapMessage = processVacationHomeworkResult(homeworkResultContext);
                if (!mapMessage.isSuccess()) {
                    return MapMessage.errorMessage(mapMessage.getInfo());
                }
            }
        }
        return MapMessage.successMessage();
    }

    private MapMessage processMentalArithmetic(ObjectiveConfigType objectiveConfigType, NewHomeworkPracticeContent newHomeworkPracticeContent,
                                               Long userId, Student student, String homeworkId) {
        List<NewHomeworkQuestion> newHomeworkQuestions = newHomeworkPracticeContent.getQuestions();
        Set<String> questionIds = new LinkedHashSet<>();
        for (NewHomeworkQuestion newHomeworkQuestion : newHomeworkQuestions) {
            String qid = newHomeworkQuestion.getQuestionId();
            questionIds.add(qid);
        }
        Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        VacationHomeworkResultContext homeworkResultContext = new VacationHomeworkResultContext();
        homeworkResultContext.setUserId(userId);
        homeworkResultContext.setUser(student);
        homeworkResultContext.setVacationHomeworkId(homeworkId);
        homeworkResultContext.setObjectiveConfigType(objectiveConfigType);
        homeworkResultContext.setClientType("pc");
        homeworkResultContext.setClientName("pc");
        homeworkResultContext.setLearningType(StudyType.vacationHomework);
        List<StudentHomeworkAnswer> studentHomeworkAnswers = new ArrayList<>();
        for (String qid : questionIds) {
            NewQuestion question = questionMap.get(qid);
            StudentHomeworkAnswer studentHomeworkAnswer = new StudentHomeworkAnswer();
            studentHomeworkAnswer.setQuestionId(qid);
            studentHomeworkAnswer.setAnswer(question.getAnswers());
            studentHomeworkAnswer.setDurationMilliseconds(2000L);
            studentHomeworkAnswers.add(studentHomeworkAnswer);
        }
        homeworkResultContext.setStudentHomeworkAnswers(studentHomeworkAnswers);
        return processVacationHomeworkResult(homeworkResultContext);
    }
}
