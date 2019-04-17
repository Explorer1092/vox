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
import com.mongodb.MongoCommandException;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.DocumentAccessException;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.support.MongoExceptionUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.common.ICharset;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.storage.aliyunoss.module.config.AliyunOSSConfig;
import com.voxlearning.alps.storage.aliyunoss.module.config.AliyunossConfigManager;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.core.LongIdEntity;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzIntegralPool;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.config.consumer.BadWordCheckerClient;
import com.voxlearning.utopia.service.homework.api.constant.CheatingTeacherStatus;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkPublishMessageType;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkLivecastLoader;
import com.voxlearning.utopia.service.newhomework.api.constant.*;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.*;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewStage;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.shard.ShardHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.shard.ShardHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.entity.shard.ShardHomeworkPractice;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.BaseVoiceRecommend;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.DubbingRecommend;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.ImageTextRecommend;
import com.voxlearning.utopia.service.newhomework.api.mapper.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.LightInteractionCourseResp;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.MicroVideoResp;
import com.voxlearning.utopia.service.newhomework.api.service.NewHomeworkService;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.consumer.cache.OralCommunicationRemindAssignCacheManager;
import com.voxlearning.utopia.service.newhomework.impl.dao.*;
import com.voxlearning.utopia.service.newhomework.impl.dao.basicreview.BasicReviewHomeworkPackageDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.classifyimages.OcrClassifyImagesPersistence;
import com.voxlearning.utopia.service.newhomework.impl.dao.shard.ShardHomeworkBookDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.shard.ShardHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.shard.ShardHomeworkPracticeDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkBookDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend.DubbingRecommendDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend.ImageTextRecommendDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend.VoiceRecommendDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.HomeworkCommentLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.pubsub.NewHomeworkPublisher;
import com.voxlearning.utopia.service.newhomework.impl.queue.BatchRewardIntegralQueueProducer;
import com.voxlearning.utopia.service.newhomework.impl.queue.DubbingSyntheticQueueProducer;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.HomeworkResultProcessor;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.AutoAssignBasicReviewHomeworkProcessor;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.AssignBasicReviewHomeworkProcessor;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check.CheckHomeworkProcessor;
import com.voxlearning.utopia.service.newhomework.impl.service.queue.NewHomeworkQueueServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.support.HomeworkTransform;
import com.voxlearning.utopia.service.newhomework.impl.template.assign.AssignHomeworkServiceFactory;
import com.voxlearning.utopia.service.newhomework.impl.template.assign.AssignHomeworkTemplate;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardService;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardType;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.KnowledgePointMaterial;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.EmbedPage;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.video.MicroVideoTask;
import com.voxlearning.utopia.service.question.api.entity.stone.data.ImageTextRhyme;
import com.voxlearning.utopia.service.question.api.entity.stone.data.StoneBufferedData;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.constants.ClazzIntegralType;
import com.voxlearning.utopia.service.user.api.constants.RefStatus;
import com.voxlearning.utopia.service.user.api.constants.UserTagEventType;
import com.voxlearning.utopia.service.user.api.constants.UserTagType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.ClazzIntegralHistory;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import org.apache.commons.collections.map.HashedMap;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.homework.api.constant.HomeworkType.MATH;
import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.DETAIL_COURSE_NAME;

/**
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/6
 */
@Named
@Service(interfaceClass = NewHomeworkService.class)
@ExposeService(interfaceClass = NewHomeworkService.class)
public class NewHomeworkServiceImpl extends SpringContainerSupport implements NewHomeworkService {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private AsyncUserCacheServiceClient asyncUserCacheServiceClient;
    @Inject
    private AmbassadorServiceClient ambassadorServiceClient;
    @Inject
    private ClazzIntegralServiceClient clazzIntegralServiceClient;
    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    private AppMessageServiceClient appMessageServiceClient;
    @Inject
    private CheckHomeworkProcessor checkHomeworkProcessor;
    @Inject
    private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject
    private HomeworkCommentLoaderImpl homeworkCommentLoader;
    @Inject
    private HomeworkCommentServiceImpl homeworkCommentService;
    @Inject
    private HomeworkResultProcessor homeworkResultProcessor;
    @Inject
    private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;
    @Inject
    private NewHomeworkProcessResultServiceImpl newHomeworkProcessResultService;
    @Inject
    private SubHomeworkDao subHomeworkDao;
    @Inject
    private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject
    private NewHomeworkResultServiceImpl newHomeworkResultService;
    @Inject
    private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject
    private PossibleCheatingHomeworkDao possibleCheatingHomeworkDao;
    @Inject
    private PossibleCheatingTeacherDao possibleCheatingTeacherDao;
    @Inject
    private QuestionLoaderClient questionLoaderClient;
    @Inject
    private StudentHomeworkStatPersistence studentHomeworkStatPersistence;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private VoiceRecommendDao voiceRecommendDao;
    @Inject
    private NewHomeworkCacheServiceImpl newHomeworkCacheService;
    @Inject
    private SubHomeworkBookDao subHomeworkBookDao;
    @Inject
    private HomeworkTaskRecordDao homeworkTaskRecordDao;
    @Inject
    private AssignHomeworkServiceFactory assignHomeworkServiceFactory;
    @Inject
    private BadWordCheckerClient badWordCheckerClient;
    @Inject
    private DubbingSyntheticHistoryDao dubbingSyntheticHistoryDao;
    @Inject
    private AsyncAvengerHomeworkServiceImpl asyncAvengerHomeworkService;
    @Inject
    private NewHomeworkPublisher newHomeworkPublisher;
    @Inject
    private NewHomeworkQueueServiceImpl newHomeworkQueueService;
    @Inject
    private AssignBasicReviewHomeworkProcessor assignBasicReviewHomeworkProcessor;
    @Inject
    private BasicReviewHomeworkPackageDao basicReviewHomeworkPackageDao;
    @Inject
    private BatchRewardIntegralQueueProducer batchRewardIntegralQueueProducer;
    @Inject
    private DubbingSyntheticQueueProducer dubbingSyntheticQueueProducer;
    @Inject
    private PictureBookPlusDubbingDao pictureBookPlusDubbingDao;
    @Inject
    private NewHomeworkLivecastLoader newHomeworkLivecastLoader;
    @Inject
    private AccessDeniedRecordPersistence accessDeniedRecordPersistence;
    @Inject
    private UserServiceClient userServiceClient;
    @Inject
    private IntelDiagnosisClient intelDiagnosisClient;
    @Inject
    private ShardHomeworkDao shardHomeworkDao;
    @Inject
    private ShardHomeworkBookDao shardHomeworkBookDao;
    @Inject
    private ShardHomeworkPracticeDao shardHomeworkPracticeDao;
    @Inject
    private DubbingCollectionRecordDao dubbingCollectionRecordDao;
    @Inject
    private DubbingRecommendDao dubbingRecommendDao;
    @Inject
    private AutoAssignBasicReviewHomeworkProcessor autoAssignBasicReviewHomeworkProcessor;
    @Inject
    private OcrClassifyImagesPersistence ocrClassifyImagesPersistence;
    @Inject
    private SubHomeworkResultDao subHomeworkResultDao;
    @Inject
    private UploaderResourceLibraryDao uploaderResourceLibraryDao;
    @Inject
    private ImageTextRecommendDao imageTextRecommendDao;
    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;
    @Inject
    private RemindAssignHomeworkTeacherPersistence remindAssignHomeworkTeacherPersistence;

    @Inject private RaikouSDK raikouSDK;

    @ImportService(interfaceClass = ParentRewardService.class)
    private ParentRewardService parentRewardService;

    private UserIntegralService userIntegralService;

    @ImportService(interfaceClass = UserIntegralService.class)
    public void setUserIntegralService(UserIntegralService userIntegralService) {
        this.userIntegralService = userIntegralService;
    }

    @Override
    public MapMessage assignHomework(Teacher teacher, HomeworkSource homeworkSource, HomeworkSourceType homeworkSourceType, NewHomeworkType newHomeworkType, HomeworkTag homeworkTag) {

        AssignHomeworkTemplate template = assignHomeworkServiceFactory.getTemplate(newHomeworkType);
        if (template == null) {
            return MapMessage.errorMessage("布置作业错误").setErrorCode(ErrorCodeConstants.ERROR_CODE_ASSIGN_HOMEWORK_TEMPLATE_NOT_EXIST);
        }
        // 期末复习作业形式特殊处理（因为native传过来的homeworkTag是写死的，所以改成后端根据当前时间所在的学期判断）
        if (NewHomeworkType.TermReview.equals(newHomeworkType)) {
            Term term = SchoolYear.newInstance().currentTerm();
            if (term.equals(Term.上学期)) {
                homeworkTag = HomeworkTag.Last_TermReview;
            } else {
                homeworkTag = HomeworkTag.Next_TermReview;
            }
        }
        try {
            AssignHomeworkContext assignHomeworkContext = AtomicLockManager.instance().wrapAtomic(template)
                    .keys(teacher.getId(), teacher.getSubject().getId())
                    .proxy()
                    .assignHomework(teacher, homeworkSource, homeworkSourceType, newHomeworkType, homeworkTag);
            MapMessage mapMessage = assignHomeworkContext.transform();
            if (mapMessage.isSuccess()) {
                if (newHomeworkType.getTypeId() == NewHomeworkType.ThirdPartyType
                        || newHomeworkType.getTypeId() == NewHomeworkType.SelfStudyType) {
                    mapMessage.set("homeworkIds", assignHomeworkContext.getHomeworkIds());
                } else {
                    mapMessage.set("homeworkIds", assignHomeworkContext.getAssignedGroupHomework().values().stream().map(NewHomework::getId).collect(Collectors.toList()));
                }
                return mapMessage;
            } else {
                return mapMessage;
            }
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("作业布置中，请不要重复布置!").setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
        } catch (Exception ex) {
            logger.error("failed to save homework, teacher id {}, homework_data {}", teacher.getId(), homeworkSource, ex);
            return MapMessage.errorMessage("布置作业失败！").setErrorCode(ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK);
        }
    }


    public MapMessage checkHomework(Teacher teacher, String homeworkId, HomeworkSourceType homeworkSourceType) {
        try {
            CheckHomeworkContext checkHomeworkContext = new CheckHomeworkContext();
            checkHomeworkContext.setHomeworkId(homeworkId);
            checkHomeworkContext.setTeacher(teacher);
            checkHomeworkContext.setCheckHomeworkSource(homeworkSourceType);

            return AtomicLockManager.instance().wrapAtomic(checkHomeworkProcessor)
                    .keys(teacher.getId(), homeworkId)
                    .proxy()
                    .process(checkHomeworkContext).transform();

        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("处理中，请不要重复点击!");
        } catch (Exception ex) {
            logger.error("Check homework error, hid:{}, tid:{}", homeworkId, teacher.getId(), ex);
        }
        return MapMessage.errorMessage("检查作业失败");
    }

    public MapMessage batchCheckHomework(Teacher teacher, String homeworkIds, HomeworkSourceType homeworkSourceType) {
        try {
            return AtomicLockManager.instance().wrapAtomic(this)
                    .keys(teacher.getId())
                    .proxy()
                    .internalBatchCheckHomework(teacher, homeworkIds, homeworkSourceType);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("处理中，请不要重复点击!");
        } catch (Exception ex) {
            logger.error("Batch Check homework error, hids:{}, tid:{}", homeworkIds, teacher.getId(), ex);
            return MapMessage.errorMessage("批量检查失败", ex);
        }
    }

    public MapMessage internalBatchCheckHomework(Teacher teacher, String homeworkIds, HomeworkSourceType homeworkSourceType) {
        try {
            for (String hid : StringUtils.split(homeworkIds, ",")) {
                CheckHomeworkContext checkHomeworkContext = new CheckHomeworkContext();
                checkHomeworkContext.setHomeworkId(hid);
                checkHomeworkContext.setTeacher(teacher);
                checkHomeworkContext.setCheckHomeworkSource(homeworkSourceType);
                checkHomeworkProcessor.process(checkHomeworkContext);
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("批量检查失败", ex);
        }
    }

    @Override
    public MapMessage adjustHomework(Long teacherId, String id, Date end) {

        NewHomework newHomework = newHomeworkLoader.load(id);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacherId, newHomework.getClazzGroupId())) {
            return MapMessage.errorMessage("您没有权限调整此作业");
        }
        if (newHomework.getCreateAt() != null && newHomework.getCreateAt().before(NewHomeworkConstants.ALLOW_UPDATE_HOMEWORK_START_TIME)) {
            return MapMessage.errorMessage("此份作业已不允许调整");
        }
        long startTime = newHomework.getStartTime().getTime();
        if (end.getTime() < startTime) {
            return MapMessage.errorMessage("结束时间错误");
        }
        long effectiveTime = end.getTime() - startTime;
        if (effectiveTime >= NewHomeworkConstants.MAX_EFFECTIVE_MILLISECONDS) {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", teacherId,
                    "mod2", ErrorCodeConstants.ERROR_CODE_EFFECTIVE_TIME_ERROR,
                    "op", "adjust homework"
            ));
            return MapMessage.errorMessage("作业有效期超过1年，请重新设置").setErrorCode(ErrorCodeConstants.ERROR_CODE_EFFECTIVE_TIME_ERROR);
        }
        try {
            Boolean adjust = updateNewHomeworkTime(id, end);
            if (adjust) {
                MapMessage mapMessage = MapMessage.successMessage("调整作业成功");
                newHomework = newHomeworkLoader.load(id);
                mapMessage.add("checked", newHomework.isHomeworkChecked());
                // 显示检查按钮的条件
                Map<String, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.findByHomeworkForReport(newHomework);
                int finishedCount = 0;
                for (String userId : newHomeworkResultMap.keySet()) {
                    NewHomeworkResult newHomeworkResult = newHomeworkResultMap.get(userId);
                    if (newHomeworkResult.getFinishAt() != null) {
                        finishedCount++;
                    }
                }
                GroupMapper groupMapper = raikouSDK.getClazzClient()
                        .getGroupLoaderClient()
                        .loadGroupDetail(newHomework.getClazzGroupId(), true)
                        .firstOrNull();
                int userCount = groupMapper != null ? groupMapper.getStudents().size() : 0;
                mapMessage.add("showCheck", !newHomework.isHomeworkChecked()
                        && (DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE).equals(DateUtils.dateToString(newHomework.getEndTime(), DateUtils.FORMAT_SQL_DATE))
                        || System.currentTimeMillis() > newHomework.getEndTime().getTime()
                        || finishedCount >= userCount));

                // 发kafka
                NewHomeworkBook homeworkBook = newHomeworkLoader.loadNewHomeworkBook(id);
                asyncAvengerHomeworkService.informHomeworkToBigData(newHomework, homeworkBook);

                // 发送广播
                Map<String, Object> map = new HashMap<>();
                map.put("messageType", HomeworkPublishMessageType.adjust);
                map.put("groupId", newHomework.getClazzGroupId());
                map.put("homeworkId", newHomework.getId());
                map.put("subject", newHomework.getSubject());
                map.put("teacherId", newHomework.getTeacherId());
                map.put("createAt", newHomework.getCreateAt().getTime());
                map.put("startTime", newHomework.getStartTime().getTime());
                map.put("endTime", newHomework.getEndTime().getTime());
                map.put("homeworkType", newHomework.getNewHomeworkType());
                map.put("homeworkTag", newHomework.getHomeworkTag());
                newHomeworkPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));

                return mapMessage;
            } else {
                return MapMessage.errorMessage("调整作业失败");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    private Boolean updateNewHomeworkTime(String id, Date end) {
        if (NewHomeworkUtils.isShardHomework(id)) {
            return shardHomeworkDao.updateShardHomeworkTime(id, null, end);
        }
        return subHomeworkDao.updateSubHomeworkTime(id, null, end);
    }

    public Boolean updateDisabledTrue(String id) {
        if (NewHomeworkUtils.isShardHomework(id)) {
            return shardHomeworkDao.updateDisabledTrue(id);
        }
        return subHomeworkDao.updateDisabledTrue(id);
    }

    @Override
    public MapMessage deleteHomework(Long teacherId, String id) {
        NewHomework newHomework = newHomeworkLoader.load(id);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacherId, newHomework.getClazzGroupId())) {
            return MapMessage.errorMessage("您没有权限删除此作业");
        }
        if (newHomework.getCreateAt() != null && newHomework.getCreateAt().before(NewHomeworkConstants.ALLOW_UPDATE_HOMEWORK_START_TIME)) {
            return MapMessage.errorMessage("此份作业已不允许删除");
        }
        try {
            boolean delete = NewHomeworkUtils.isShardHomework(id) ? shardHomeworkDao.updateDisabledTrue(id) : subHomeworkDao.updateDisabledTrue(id);
            if (delete) {
                // 发kafka
                newHomework = newHomeworkLoader.load(id);
                NewHomeworkBook homeworkBook = newHomeworkLoader.loadNewHomeworkBook(id);
                asyncAvengerHomeworkService.informHomeworkToBigData(newHomework, homeworkBook);
                //删除作业老师广播
                Map<String, Object> teacherPublisherMap = new HashMap<>();
                teacherPublisherMap.put("messageType", HomeworkPublishMessageType.deleted);
                teacherPublisherMap.put("homeworkId", newHomework.getId());
                teacherPublisherMap.put("teacherId", newHomework.getTeacherId());
                teacherPublisherMap.put("groupId", newHomework.getClazzGroupId());
                teacherPublisherMap.put("subject", newHomework.getSubject());
                teacherPublisherMap.put("homeworkType", newHomework.getNewHomeworkType());
                teacherPublisherMap.put("homeworkTag", newHomework.getHomeworkTag());
                teacherPublisherMap.put("createAt", newHomework.getCreateAt());
                newHomeworkPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(teacherPublisherMap)));
                return MapMessage.successMessage("删除作业成功");
            } else {
                return MapMessage.errorMessage("删除作业失败");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @Override
    public MapMessage processorHomeworkResult(HomeworkResultContext homeworkResultContext) {
        try {
            HomeworkResultContext context = AtomicLockManager.getInstance().wrapAtomic(homeworkResultProcessor)
                    .keys(homeworkResultContext.getUserId(), homeworkResultContext.getHomeworkId())
                    .proxy()
                    .process(homeworkResultContext);
            MapMessage msg = context.transform().add("result", context.getResult());
            if (context.getErrorCode().equals(ErrorCodeConstants.ERROR_CODE_IMMEDIATE_INTERVENTION)) {
                msg.add("subMaster", context.getSubGrasp().values().iterator().next())
                        .add(NewHomeworkConstants.HINT_ID, context.getHintId())
                        .add("hintTag", context.getHintTag())
                        .add("hintOptType", context.getHintOptType());
            }
            return msg;
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("数据提交中，请不要重复点击!").setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    /**
     * 一键/批量批改
     * TODO 这个有点复杂，可以考虑改成直接更新明细表，而不从中间表关联。xuesong.zhang
     *
     * @param homeworkId 作业id
     */
    @Override
    public void batchSaveHomeworkCorrect(String homeworkId, Long teacherId) {

        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return;
        }

        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacherId, newHomework.getClazzGroupId())) {
            return;
        }

        // 1. 判断老师本次布置的作业中是否含有需要主观批改的试题
        if (!Objects.equals(newHomework.getIncludeSubjective(), Boolean.TRUE)) {
            // 不包含主观作答类习题
            return;
        }
        // 2. 取出这个老师的班组下的所有学生
        List<Long> userIds = studentLoaderClient.loadGroupStudentIds(newHomework.getClazzGroupId());
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        // 3. 开始循环啦，根据homeworkId和userId取出作业

        Map<String, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.findByHomework(newHomework);

        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap.values()
                .stream()
                .filter(o -> MapUtils.isNotEmpty(o.getPractices()))
                .collect(Collectors.toList());

        for (NewHomeworkResult newHomeworkResult : newHomeworkResults) {

            Map<ObjectiveConfigType, Map<String, String>> studentMap = newHomeworkResultLoader.getCorrectQuestions(newHomework, newHomeworkResult);
            for (ObjectiveConfigType objectiveConfigType : newHomeworkResult.getPractices().keySet()) {
                if (objectiveConfigType.isSubjective() && newHomeworkResult.getPractices().get(objectiveConfigType).isFinished()) {
                    if (objectiveConfigType == ObjectiveConfigType.NEW_READ_RECITE) {
                        NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(objectiveConfigType);
                        for (String appId : newHomeworkResultAnswer.getAppAnswers().keySet()) {
                            newHomeworkResultService.finishCorrectToAppForSubHomeworkResult(newHomework.getId(), newHomeworkResult.getId(), objectiveConfigType, appId, true, null, null, "", true);
                        }

                        newHomeworkResultService.finishCorrect(newHomework, newHomeworkResult.getUserId(), objectiveConfigType, true, false);

                    } else {
                        Map<String, String> tempMap = studentMap.get(objectiveConfigType);
                        for (Map.Entry<String, String> entry1 : tempMap.entrySet()) {
                            String qid = entry1.getKey();
                            String processId = entry1.getValue();

                            // 并且有上传过文件的
                            NewQuestion question = questionLoaderClient.loadQuestionIncludeDisabled(qid);
                            NewHomeworkProcessResult processResult = newHomeworkProcessResultLoader.load(homeworkId, processId);

                            if (processResult != null && CollectionUtils.isNotEmpty(processResult.findAllFiles()) && Objects.equals(Boolean.TRUE, question.isSubjective())) {
                                newHomeworkProcessResultService.updateCorrection(processId, newHomework.getId(), qid, newHomeworkResult.getUserId(), true, null, null, "", true);
                            }
                        }
                        // 本次作业形式的批改完成
                        Set<String> qids = tempMap.keySet();
                        if (isSubjective(qids)) {
                            newHomeworkResultService.finishCorrect(newHomework, newHomeworkResult.getUserId(), objectiveConfigType, true, false);
                        }

                    }
                }
            }

            // 本次作业批改完成
            if (MapUtils.isNotEmpty(studentMap)) {
                newHomeworkResultService.finishCorrect(newHomework, newHomeworkResult.getUserId(), null, false, true);
            }

        }

    }

    @Override
    public MapMessage batchSaveNewHomeworkComment(Teacher teacher, String homeworkId, Set<Long> userIds, String comment, String audioComment) {
        try {
            return AtomicLockManager.instance().wrapAtomic(this)
                    .keys(teacher.getId(), homeworkId)
                    .proxy()
                    .internalBatchSaveNewHomeworkComment(teacher, homeworkId, userIds, comment, audioComment);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("处理中，请不要重复点击!");
        } catch (Exception ex) {
            logger.error("batch save homework comment error, hid:{}, tid:{}", homeworkId, teacher.getId(), ex);
            return MapMessage.errorMessage("评语失败", ex);
        }
    }

    public MapMessage internalBatchSaveNewHomeworkComment(Teacher teacher, String homeworkId, Set<Long> userIds, String comment, String audioComment) {
        NewHomework newhomework = newHomeworkLoader.load(homeworkId);
        NewHomework.Location location = newhomework.toLocation();
        if (location == null) {
            return MapMessage.errorMessage("评语失败，练习不存在。");
        }
        if (newhomework.getCreateAt() != null && newhomework.getCreateAt().before(NewHomeworkConstants.ALLOW_UPDATE_HOMEWORK_START_TIME)) {
            return MapMessage.errorMessage("评语失败，此份练习已过期不允许评语。");
        }
        if (CollectionUtils.isEmpty(userIds)) {
            return MapMessage.errorMessage("评语失败，请选择学生。");
        }
        if (StringUtils.isBlank(comment) && StringUtils.isBlank(audioComment)) {
            return MapMessage.errorMessage("评语失败，评语内容为空。");
        }
        if (badWordCheckerClient.containsConversationBadWord(comment)) {
            return MapMessage.errorMessage("评语失败，内容包含敏感词。");
        }

        //flag为true表示音频评语
        boolean flag = StringUtils.isNotBlank(audioComment);
        if (flag) {
            comment = "[语音评语]";
        }

        // 消息跳转地址
        String link = UrlUtils.buildUrlQuery("/studentMobile/homework/app/skip.vpage", MapUtils.m("homeworkId", homeworkId));
        // 消息内容
        String content = StringUtils.join(Arrays.asList(teacher.respectfulName(), "点评了你", DateUtils.dateToString(newhomework.getCreateAt(), "MM月dd日"), "的", newhomework.getSubject().getValue(), "作业：\n", comment), "");

        // 这个地方需要三写
        List<HomeworkComment> homeworkComments = new LinkedList<>();
        // 消息中心
        List<AppMessage> messages = new ArrayList<>();

        for (Long userId : userIds) {
            HomeworkComment homeworkComment = new HomeworkComment();
            homeworkComment.setStudentId(userId);
            homeworkComment.setTeacherId(teacher.getId());
            homeworkComment.setComment(comment);
            homeworkComment.setRewardIntegral(0);
            homeworkComment.setHomeworkId(location.getId());
            homeworkComment.setHomeworkType(location.getSubject().name());
            homeworkComments.add(homeworkComment);

            AppMessage message = new AppMessage();
            message.setUserId(userId);
            message.setMessageType(StudentAppPushType.HOMEWORK_WRITE_COMMENT.getType());
            message.setTitle("老师评语");
            message.setContent(content);
            message.setLinkUrl(link);
            message.setLinkType(1); // 站内的相对地址
            messages.add(message);

            // 写中间表
            newHomeworkResultService.saveNewHomeworkComment(newhomework, userId, comment, audioComment);
        }
        // 调用老的评论双写mysql的HomeworkComment和 mongo的unreadHomeworkComment
        if (!flag) {
            homeworkCommentService.createHomeworkComments(homeworkComments);
        }
        // 老师点亮评语图标
        ambassadorServiceClient.getAmbassadorService().recordAmbassadorMentor(teacher.getId(), MapUtils.map(UserTagType.AMBASSADOR_MENTOR_COMMENT, UserTagEventType.AMBASSADOR_MENTOR_COMMENT));
        // 判断只加一次
        ambassadorServiceClient.getAmbassadorService().addScoreOncePerDay(teacher.getId(), 0L, AmbassadorCompetitionScoreType.WRITE_COMMENT);
        // 发送消息中心
        messages.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
        Map<String, Object> extInfo = MiscUtils.m("link", link, "t", "h5", "key", "j", "s", StudentAppPushType.HOMEWORK_WRITE_COMMENT.getType(), "title", "老师评语");
        appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.STUDENT, new ArrayList<>(userIds), extInfo);
        List<String> commentTemplates = NewHomeworkConstants.commentTemplate(teacher.getSubject());
        if (!commentTemplates.contains(comment) && !flag) {
            newHomeworkCacheService.teacherNewHomeworkCommentLibraryManager_addComment(teacher.getId(), comment);
        }

        // 发送广播
        Map<String, Object> map = new HashMap<>();
        map.put("messageType", NewHomeworkPublishMessageType.comment);
        map.put("groupId", newhomework.getClazzGroupId());
        map.put("homeworkId", newhomework.getId());
        map.put("subject", newhomework.getSubject());
        map.put("teacherId", newhomework.getTeacherId());
        map.put("createAt", newhomework.getCreateAt().getTime());
        map.put("startTime", newhomework.getStartTime().getTime());
        map.put("endTime", newhomework.getEndTime().getTime());
        map.put("homeworkType", newhomework.getNewHomeworkType());
        map.put("homeworkTag", newhomework.getHomeworkTag());
        map.put("comment", comment);
        map.put("audioComment", audioComment);
        map.put("studentIds", userIds);
        newHomeworkPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));

        return MapMessage.successMessage("评语成功");
    }


    private boolean isSubjective(Set<String> qids) {
        if (CollectionUtils.isNotEmpty(qids)) {
            // 需要主观作答的题目
            List<String> questions = questionLoaderClient.loadQuestionsIncludeDisabled(qids)
                    .values().stream()
                    .filter(NewQuestion::isSubjective)
                    .map(NewQuestion::getId)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(questions)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Page<HomeworkHistoryMapper> loadStudentHomeworkHistory(Long clazzGroupId, Subject subject, Long userId, Pageable pageable) {
        if (clazzGroupId == null || subject == null || userId == null) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        Page<NewHomework.Location> newHomeworkPage = newHomeworkLoader.loadGroupNewHomeworks(Collections.singletonList(clazzGroupId), subject, pageable);
        if (newHomeworkPage == null || CollectionUtils.isEmpty(newHomeworkPage.getContent())) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 根据NewHomework id 拼出所需要查找的中间表NewHomeworkResult
        List<String> newHomeworkResultIds = new ArrayList<>();
        for (NewHomework.Location location : newHomeworkPage.getContent()) {
            String day = DayRange.newInstance(location.getCreateTime()).toString();
            NewHomeworkResult.ID tempId = new NewHomeworkResult.ID(day, subject, location.getId(), userId.toString());
            newHomeworkResultIds.add(tempId.toString());
        }

        List<HomeworkHistoryMapper> content = buildHomeworkHistoryMapper(newHomeworkResultIds, userId);
        if (CollectionUtils.isNotEmpty(content)) {
            content = content.stream()
                    .sorted((o1, o2) -> Long.compare(o2.getCreateDate().getTime(), o1.getCreateDate().getTime()))
                    .collect(Collectors.toList());
        }
        return new PageImpl<>(content, pageable, newHomeworkPage.getTotalElements());
    }


    //pc学生历史报告接口
    public Page<HomeworkHistoryMapper> loadStudentHomeworkHistoryWithTimeLimit(Long clazzGroupId, Subject subject, Date startDate, Date endDate, Long userId, Pageable pageable) {
        if (clazzGroupId == null || subject == null || userId == null) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        Page<NewHomework.Location> newHomeworkPage = newHomeworkLoader.loadGroupNewHomeworksWithTimeLimit(Collections.singletonList(clazzGroupId), subject, startDate, endDate, pageable);
        if (newHomeworkPage == null || CollectionUtils.isEmpty(newHomeworkPage.getContent())) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 根据NewHomework id 拼出所需要查找的中间表NewHomeworkResult
        List<String> newHomeworkResultIds = new ArrayList<>();
        for (NewHomework.Location location : newHomeworkPage.getContent()) {
            String day = DayRange.newInstance(location.getCreateTime()).toString();
            NewHomeworkResult.ID tempId = new NewHomeworkResult.ID(day, subject, location.getId(), userId.toString());
            newHomeworkResultIds.add(tempId.toString());
        }

        List<HomeworkHistoryMapper> content = buildHomeworkHistoryMapper(newHomeworkResultIds, userId);
        if (CollectionUtils.isNotEmpty(content)) {
            content = content.stream()
                    .sorted((o1, o2) -> Long.compare(o2.getCreateDate().getTime(), o1.getCreateDate().getTime()))
                    .collect(Collectors.toList());
        }
        return new PageImpl<>(content, pageable, newHomeworkPage.getTotalElements());
    }


    /**
     * 初始化学生作业列表
     * xuesong.zhang
     *
     * @since 2016-03-22
     */
    private List<HomeworkHistoryMapper> buildHomeworkHistoryMapper(List<String> newHomeworkResultIds, Long userId) {
        if (CollectionUtils.isEmpty(newHomeworkResultIds)) {
            return Collections.emptyList();
        }

        Map<String, NewHomeworkResult> newHomeworkResultMap = newHomeworkResultLoader.loads(newHomeworkResultIds, false);

        List<String> homeworkIds = newHomeworkResultIds.stream().map(o -> StringUtils.split(o, "-")[2]).collect(Collectors.toList());

        // 初始化作业信息
        Map<String, NewHomework> newHomeworkMap = newHomeworkLoader.loads(homeworkIds);
        Map<String, NewHomeworkBook> newHomeworkBookMap = newHomeworkLoader.loadNewHomeworkBooks(homeworkIds);

        // 初始化评论信息
        List<HomeworkCommentMapper> commentMapperList = buildHomeworkCommentMapper(new HashSet<>(homeworkIds), userId);
        Map<String, List<HomeworkCommentMapper>> commentMap = commentMapperList.stream().collect(Collectors.groupingBy(HomeworkCommentMapper::getHomeworkId));

        // 拼数据
        List<HomeworkHistoryMapper> result = new ArrayList<>();
        newHomeworkResultIds.forEach(o -> {
            String hid = StringUtils.split(o, "-")[2];
            NewHomework newHomework = newHomeworkMap.get(hid);
            NewHomeworkBook newHomeworkBook = newHomeworkBookMap.get(hid);
            if (newHomework != null) {
                HomeworkHistoryMapper mapper = new HomeworkHistoryMapper();
                mapper.setCreateDate(newHomework.getCreateAt());
                mapper.setTitle(newHomework.getTitle());

                // 作业的三种状态
                // 1.开始作业2，学生并没有做完 && 未检查 && 在可做范围内
                // 2.补做作业1，学生并没有做完 && (作业检查完||作业过期)
                // 3.查看作业0，学生做完
                // 4.禁止作业3，未完成 && 创建时间早于ALLOW_UPDATE_HOMEWORK_START_TIME
                NewHomeworkResult tempResult = newHomeworkResultMap.get(o);
                if (tempResult != null) {
                    if (Objects.equals(Boolean.FALSE, tempResult.isFinished()) && newHomework.createAt.before(NewHomeworkConstants.ALLOW_UPDATE_HOMEWORK_START_TIME)) {
                        mapper.setHomeworkStatus(3);
                    } else if (Objects.equals(Boolean.FALSE, tempResult.isFinished()) && !newHomework.isHomeworkTerminated()) {
                        mapper.setHomeworkStatus(2);
                    } else if (Objects.equals(Boolean.FALSE, tempResult.isFinished()) && newHomework.isHomeworkTerminated()) {
                        mapper.setHomeworkStatus(1);
                    } else {
                        mapper.setHomeworkStatus(0);
                    }
                } else {
                    if (newHomework.createAt.before(NewHomeworkConstants.ALLOW_UPDATE_HOMEWORK_START_TIME)) {
                        mapper.setHomeworkStatus(3);
                    } else if (!newHomework.isHomeworkTerminated()) {
                        mapper.setHomeworkStatus(2);
                    } else {
                        mapper.setHomeworkStatus(1);
                    }
                }


                mapper.setComments(commentMap.get(newHomework.getId()));
                mapper.setHomeworkId(newHomework.getId());
                mapper.setSubject(newHomework.getSubject());
                mapper.setBooks(buildHomeworkHistoryBookMapper(newHomeworkBook));
                mapper.setStartTime(newHomework.getStartTime());
                mapper.setEndTime(newHomework.getEndTime());


                result.add(mapper);
            }
        });

        return result;
    }

    /**
     * 初始化作业的评论信息
     * xuesong.zhang
     *
     * @param homeworkIds 作业ids
     * @return Map(作业id list)
     */
    private List<HomeworkCommentMapper> buildHomeworkCommentMapper(Set<String> homeworkIds, Long userId) {
        List<HomeworkCommentMapper> result = new ArrayList<>();
        List<HomeworkComment> commentList = homeworkCommentLoader.loadHomeworkComments(homeworkIds)
                .homeworkType(MATH)
                .filter(t -> Objects.equals(t.getStudentId(), userId))
                .toList();

        Set<Long> teacherIds = commentList.stream().map(HomeworkComment::getTeacherId).collect(Collectors.toSet());
        Map<Long, User> teachers = userLoaderClient.loadUsers(teacherIds);

        commentList.stream()
                .sorted((o1, o2) -> Long.compare(o2.getCreateDatetime().getTime(), o1.getCreateDatetime().getTime()))
                .forEach(o -> {
                    HomeworkCommentMapper mapper = new HomeworkCommentMapper();
                    mapper.setHomeworkId(o.getHomeworkId());
                    mapper.setTeacherId(o.getTeacherId());
                    mapper.setTeacherName(teachers.get(o.getTeacherId()).fetchRealname());
                    mapper.setStudentId(o.getStudentId());
                    mapper.setComment(o.getComment());
                    mapper.setCreateDate(o.getCreateDatetime());

                    result.add(mapper);
                });

        return result;
    }

    @Override
    public HomeworkHistoryDetail loadStudentHomeworkHistoryDetail(String homeworkId, Long userId) {
        if (StringUtils.isBlank(homeworkId) || userId == null) {
            return null;
        }

        HomeworkHistoryDetail result = new HomeworkHistoryDetail();
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return null;
        }
        NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(homeworkId);
        NewHomework.Location location = newHomework.toLocation();
        String day = DayRange.newInstance(location.getCreateTime()).toString();
        NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, location.getSubject(), location.getId(), userId.toString());
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loads(Collections.singletonList(id.toString()), true).get(id.toString());
        if (newHomeworkResult == null || !newHomeworkResult.isFinished()) {
            return null;
        }
        result.setHomeworkId(newHomework.getId());
        result.setSubject(newHomework.getSubject());
        result.setTitle(newHomework.getTitle());
        result.setCreateDate(newHomework.getCreateAt());
        result.setFinishAt(newHomeworkResult.getFinishAt());
        result.setStartTime(newHomework.getStartTime());
        result.setEndTime(newHomework.getEndTime());

        // 教材信息
        List<HomeworkHistoryBookMapper> bookMapperList = buildHomeworkHistoryBookMapper(newHomeworkBook);
        result.setBooks(bookMapperList);

        // 评论
        List<HomeworkCommentMapper> commentMapperList = buildHomeworkCommentMapper(Collections.singleton(homeworkId), userId);
        result.setComments(commentMapperList);
        // 每种作业形式的做题信息
        LinkedHashMap<ObjectiveConfigType, HomeworkHistoryPractice> practices = buildHomeworkHistoryPractices(newHomeworkResult, newHomework);
        if (practices.isEmpty()) {
            return null;
        }
        result.setPractices(practices);
        Map<String, String> objectiveConfigTypes = new LinkedHashMap<>();
        List<String> objectiveConfigTypeRanks = new LinkedList<>();
        List<ObjectiveConfigType> types = practices.keySet().stream().sorted(new ObjectiveConfigType.ObjectiveConfigTypeComparator(newHomework.getSubject())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(types)) {
            types.forEach(key -> {
                objectiveConfigTypes.put(key.name(), key.getValue());
                objectiveConfigTypeRanks.add(key.name());
            });
        }

        if (newHomework.getNewHomeworkType() == NewHomeworkType.Normal && newHomework.getSubject() == Subject.MATH && practices.containsKey(ObjectiveConfigType.EXAM)) {
            result.setTotalNeedCorrectedNum(practices.get(ObjectiveConfigType.EXAM).getTotalNeedCorrectedNum());
            result.setFinishCorrectedCount(practices.get(ObjectiveConfigType.EXAM).getFinishCorrectedCount());
        } else {
            result.setTotalNeedCorrectedNum(0);
        }
        result.setObjectiveConfigTypes(objectiveConfigTypes);
        result.setObjectiveConfigTypeRanks(objectiveConfigTypeRanks);
        return result;
    }

    /**
     * 拼装教材信息
     * xuesong.zhang
     *
     * @param newHomeworkBook 作业教材信息
     * @return List
     */
    private List<HomeworkHistoryBookMapper> buildHomeworkHistoryBookMapper(NewHomeworkBook newHomeworkBook) {
        LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practices = newHomeworkBook == null || newHomeworkBook.getPractices() == null || newHomeworkBook.getPractices().isEmpty() ? new LinkedHashMap<>() : newHomeworkBook.getPractices();
        List<NewHomeworkBookInfo> list = practices.values().stream()
                .flatMap(Collection::stream)
                .filter(o -> StringUtils.isNotBlank(o.getUnitId()))
                .sorted(Comparator.comparing(NewHomeworkBookInfo::getUnitId))
                .collect(Collectors.toList());

        Map<String, List<NewHomeworkBookInfo>> map = list.stream()
                .filter(o -> StringUtils.isNotBlank(o.getUnitId()))
                .collect(Collectors.groupingBy(NewHomeworkBookInfo::getUnitId));

        List<HomeworkHistoryBookMapper> result = new ArrayList<>();
        for (Map.Entry<String, List<NewHomeworkBookInfo>> entry : map.entrySet()) {
            String unitId = entry.getKey();
            List<NewHomeworkBookInfo> infoList = entry.getValue();

            HomeworkHistoryBookMapper bookMapper = new HomeworkHistoryBookMapper();
            bookMapper.setUnitId(unitId);
            bookMapper.setUnitName(infoList.get(0).getUnitName());
            bookMapper.setSectionNames(infoList.stream().filter(o -> StringUtils.isNotBlank(o.getSectionName())).map(NewHomeworkBookInfo::getSectionName).collect(Collectors.toSet()));
            result.add(bookMapper);
        }

        result.sort(Comparator.comparing(HomeworkHistoryBookMapper::getUnitId));

        return result;
    }

    /**
     * 拼装作业形式数据
     * xuesong.zhang
     *
     * @param newHomeworkResult 作业中间表数据
     * @return Map
     */
    private LinkedHashMap<ObjectiveConfigType, HomeworkHistoryPractice> buildHomeworkHistoryPractices(NewHomeworkResult newHomeworkResult, NewHomework newHomework) {
        LinkedHashMap<ObjectiveConfigType, HomeworkHistoryPractice> result = new LinkedHashMap<>();
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practiceMap = newHomeworkResult.getPractices();
        if (practiceMap == null) {
            return result;
        }
        for (Map.Entry<ObjectiveConfigType, NewHomeworkResultAnswer> entry : practiceMap.entrySet()) {
            Boolean isSubjective = entry.getKey().isSubjective();
            if (ObjectiveConfigType.BASIC_APP == entry.getKey()
                    || ObjectiveConfigType.READING == entry.getKey()
                    || ObjectiveConfigType.LS_KNOWLEDGE_REVIEW == entry.getKey()
                    || ObjectiveConfigType.NATURAL_SPELLING == entry.getKey()) {
                HomeworkHistoryPractice practice = buildHomeworkHistoryBaseAppReadingPractice(entry.getValue(), entry.getKey());
                result.put(entry.getKey(), practice);
            } else if (ObjectiveConfigType.READ_RECITE_WITH_SCORE == entry.getKey()) {
                NewHomeworkResultAnswer newHomeworkResultAnswer = entry.getValue();
                if (MapUtils.isNotEmpty(newHomeworkResultAnswer.getAppAnswers())) {
                    HomeworkHistoryPractice practice = new HomeworkHistoryPractice();
                    int rightCount = 0;
                    int wrongCount = 0;
                    for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                        if (MapUtils.isEmpty(appAnswer.getAnswers()))
                            continue;
                        double value = new BigDecimal(SafeConverter.toInt(appAnswer.getStandardNum()) * 100).divide(new BigDecimal(appAnswer.getAnswers().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        if (value >= NewHomeworkConstants.READ_RECITE_STANDARD) {
                            rightCount++;
                        } else {
                            wrongCount++;
                        }
                    }
                    practice.setRightCount(rightCount);
                    practice.setWrongCount(wrongCount);
                    result.put(entry.getKey(), practice);
                }
            } else if (ObjectiveConfigType.OCR_MENTAL_ARITHMETIC == entry.getKey()) {
                NewHomeworkResultAnswer newHomeworkResultAnswer = entry.getValue();
                if (newHomeworkResultAnswer.isFinished()) {
                    HomeworkHistoryPractice practice = new HomeworkHistoryPractice();
                    int rightCount = SafeConverter.toInt(newHomeworkResultAnswer.getOcrMentalCorrectQuestionCount());
                    int wrongCount = SafeConverter.toInt(newHomeworkResultAnswer.getOcrMentalQuestionCount()) - rightCount;
                    practice.setRightCount(rightCount);
                    practice.setWrongCount(wrongCount);
                    result.put(entry.getKey(), practice);
                }
            } else if (ObjectiveConfigType.OCR_DICTATION == entry.getKey()) {
                NewHomeworkResultAnswer newHomeworkResultAnswer = entry.getValue();
                if (newHomeworkResultAnswer.isFinished()) {
                    HomeworkHistoryPractice practice = new HomeworkHistoryPractice();
                    int rightCount = SafeConverter.toInt(newHomeworkResultAnswer.getOcrDictationCorrectQuestionCount());
                    int wrongCount = SafeConverter.toInt(newHomeworkResultAnswer.getOcrDictationQuestionCount()) - rightCount;
                    practice.setRightCount(rightCount);
                    practice.setWrongCount(wrongCount);
                    result.put(entry.getKey(), practice);
                }
            } else {
                HomeworkHistoryPractice practice = buildHomeworkHistoryPractice(entry.getValue(), isSubjective, entry.getKey(), newHomework);
                result.put(entry.getKey(), practice);
            }
        }
        return result;
    }

    private HomeworkHistoryPractice buildHomeworkHistoryBaseAppReadingPractice(NewHomeworkResultAnswer answer, ObjectiveConfigType type) {
        HomeworkHistoryPractice result = new HomeworkHistoryPractice();
        long seconds = 0L;
        int completePracticeCount = 0;
        for (NewHomeworkResultAppAnswer nhra : answer.getAppAnswers().values()) {
            completePracticeCount++;
            seconds += nhra.processDuration();
        }
        result.setDuration(new BigDecimal(seconds).divide(new BigDecimal(60000), 0, BigDecimal.ROUND_UP).longValue());
        result.setCompletePracticeCount(completePracticeCount);
        Integer score = 0;
        if (Objects.nonNull(answer.processScore(type))) {
            score = answer.processScore(type);
        }
        result.setScore(score);
        return result;
    }

    /**
     * 计算每种作业形式中的数据
     * xuesong.zhang
     *
     * @param answer 答案信息
     * @return HomeworkHistoryPractice
     */
    private HomeworkHistoryPractice buildHomeworkHistoryPractice(NewHomeworkResultAnswer answer, Boolean isSubjective, ObjectiveConfigType objectiveConfigType, NewHomework newHomework) {
        HomeworkHistoryPractice result = new HomeworkHistoryPractice();
        List<String> processId = new ArrayList<>(answer.processAnswers().values());
        Map<String, NewHomeworkProcessResult> map = newHomeworkProcessResultLoader.loads(newHomework.getId(), processId);

        int right = 0;
        int wrong = 0;
        long seconds = 0L;

        List<NewHomeworkProcessResult> tempList = new ArrayList<>(map.values());

        String state = "未批改";

        if (isSubjective) {
            // 主观类
            if (objectiveConfigType == ObjectiveConfigType.NEW_READ_RECITE) {
                if (answer.getAppAnswers() != null) {
                    right = answer.getAppAnswers().size();
                    if (answer.isCorrected()) {
                        state = "已批改";
                    }
                }
            } else if (ObjectiveConfigType.DUBBING.equals(objectiveConfigType)) {
                int completePracticeCount = 0;
                if (MapUtils.isNotEmpty(answer.getAppAnswers()) && CollectionUtils.isNotEmpty(answer.getAppAnswers().values())) {
                    for (NewHomeworkResultAppAnswer nhra : answer.getAppAnswers().values()) {
                        completePracticeCount++;
                        seconds += nhra.processDuration();
                    }
                }
                result.setDuration(new BigDecimal(seconds).divide(new BigDecimal(60000), 0, BigDecimal.ROUND_UP).longValue());
                result.setCompletePracticeCount(completePracticeCount);
                return result;
            } else {
                for (NewHomeworkProcessResult o : tempList) {
                    if (CollectionUtils.isNotEmpty(o.getFiles())) {
                        List<NewHomeworkQuestionFile> fileList = o.getFiles().stream().flatMap(Collection::stream).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(fileList)) {
                            right++;
                        } else {
                            wrong++;
                        }
                    } else {
                        wrong++;
                    }
                }
            }
            seconds = SafeConverter.toLong(answer.processDuration()) * 1000;
        } else {
            if (objectiveConfigType == ObjectiveConfigType.ORAL_COMMUNICATION) {
                Integer score = 0;
                if (Objects.nonNull(answer.processScore(objectiveConfigType))) {
                    score = answer.processScore(objectiveConfigType);
                }
                result.setScore(score);
                result.setDuration(new BigDecimal(answer.processDuration()).divide(new BigDecimal(60000), 0, BigDecimal.ROUND_UP).longValue());
                result.setRightCount(MapUtils.isNotEmpty(answer.getAppAnswers()) ? answer.getAppAnswers().size() : 0);
                return result;
            }
            // 非主观类
            for (NewHomeworkProcessResult o : tempList) {
                if (SafeConverter.toBoolean(o.getGrasp())) {
                    right++;
                } else {
                    wrong++;
                }
                seconds += o.getDuration();
            }
        }

        result.setState(state);
        result.setRightCount(right);
        result.setWrongCount(wrong);
        result.setDuration(new BigDecimal(seconds).divide(new BigDecimal(60000), 0, BigDecimal.ROUND_UP).longValue());
        Integer score = 0;
        if (Objects.nonNull(answer.processScore(objectiveConfigType))) {
            score = answer.processScore(objectiveConfigType);
        }
        result.setScore(score);

        result.setCompletePracticeCount(right + wrong);

        if ((right + wrong) != 0) {
            BigDecimal dr = new BigDecimal(right);
            BigDecimal dw = new BigDecimal(wrong);
            BigDecimal d = (dr.divide(dr.add(dw), 2, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal(100));
            result.setRate(d.intValue());
        }
        return result;
    }

    @Override
    public MapMessage incFinishHomeworkCount(Long teacherId, Long clazzId, Long studentId) {
        if (teacherId == null || clazzId == null || studentId == null)
            return MapMessage.errorMessage();
        // FIXME: optimization is required
        boolean ret = studentHomeworkStatPersistence.incFinishHomeworkCount(teacherId, clazzId, studentId);
        if (!ret) {
            StudentHomeworkStat stat = new StudentHomeworkStat();
            stat.setTeacherId(teacherId);
            stat.setClazzId(clazzId);
            stat.setStudentId(studentId);
            stat.setFinishHomeworkCount(1L);
            studentHomeworkStatPersistence.persist(stat);
        }
        asyncUserCacheServiceClient.getAsyncUserCacheService()
                .NewUserTaskCacheManager_cleanupTeacherNewUserTaskCache(Collections.singletonList(teacherId))
                .awaitUninterruptibly();
        return MapMessage.successMessage();
    }

    @Override
    public void updatePossibleCheatingHomeworkIntegral(String id) {
        possibleCheatingHomeworkDao.updatePossibleCheatingHomeworkIntegral(id);
    }

    @Override
    public void persistPossibleCheatingTeacher(PossibleCheatingTeacher pct) {
        if (pct == null) return;
        possibleCheatingTeacherDao.insert(pct);
    }

    @Override
    public void updateLastCheatDateAndStatus(String id, CheatingTeacherStatus status) {
        possibleCheatingTeacherDao.updateLastCheatDateAndStatus(id, new Date(), status);
    }

    @Override
    public boolean isCheatingTeacher(Long teacherId) {
        return possibleCheatingTeacherDao.loadByTeacherId(teacherId) != null;
    }

    @Override
    public void insertPossibleCheatingHomework(PossibleCheatingHomework homework) {
        possibleCheatingHomeworkDao.insert(homework);
    }

    @Override
    public void disabledPossibleCheatingTeacherById(String id) {
        possibleCheatingTeacherDao.disabledById(id);
    }

    @Override
    public void insertPossibleCheatingTeacher(PossibleCheatingTeacher teacher) {
        possibleCheatingTeacherDao.insert(teacher);
    }

    @Override
    public void updatePossibleCheatingTeacherStatus(String id, CheatingTeacherStatus status) {
        possibleCheatingTeacherDao.updateStatus(id, status);
    }

    @Override
    public void washTeacher(String id) {
        possibleCheatingTeacherDao.washTeacher(id);
    }

    @Override
    public MapMessage batchRewardStudentIntegral(Long teacherId, Map<String, Object> jsonMap) {
        try {
            return AtomicLockManager.instance().wrapAtomic(this)
                    .keys(teacherId)
                    .proxy()
                    .internalBatchRewardStudentIntegral(teacherId, jsonMap);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("处理中，请不要重复点击!");
        } catch (Exception ex) {
            logger.error("batch reword homework integral error, tid:{}, jsonMap:{}", teacherId, JsonUtils.toJson(jsonMap), ex);
            return MapMessage.errorMessage("奖励学豆失败");
        }
    }

    public MapMessage internalBatchRewardStudentIntegral(Long teacherId, Map<String, Object> jsonMap) {
        final TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacherDetail == null) {
            return MapMessage.errorMessage("老师不存在");
        }
        if (!jsonMap.containsKey("details")) {
            return MapMessage.errorMessage("学生列表不能为空");
        }
        if (!jsonMap.containsKey("homeworkId")) {
            return MapMessage.errorMessage("参数homeworkId不存在");
        }
        Long clazzId = SafeConverter.toLong(jsonMap.get("clazzId"));
        String homeworkId = SafeConverter.toString(jsonMap.get("homeworkId"));
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("练习不存在");
        }
        if (newHomework.getCreateAt() != null && newHomework.getCreateAt().before(NewHomeworkConstants.ALLOW_UPDATE_HOMEWORK_START_TIME)) {
            return MapMessage.errorMessage("此份练习已不允许奖励学豆");
        }
        @SuppressWarnings("unchecked")
        List<Map> details = (List<Map>) jsonMap.get("details");
        Map<Long, User> userMap = studentLoaderClient.loadGroupStudents(newHomework.getClazzGroupId())
                .stream()
                .collect(Collectors
                        .toMap(LongIdEntity::getId, Function.identity()));


        //奖励的总学豆
        int rewardTotalIntegral = 0;
        for (Map detail : details) {
            Long studentId = SafeConverter.toLong(detail.get("studentId"));
            Integer count = SafeConverter.toInt(detail.get("count"));
            if (!userMap.containsKey(studentId)) return MapMessage.errorMessage("操作失败，请重试");
            if (count <= 0) return MapMessage.errorMessage("奖励学豆数必须大于0");
            rewardTotalIntegral += count;
        }
        final Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId);
        if (clazz == null) {
            return MapMessage.errorMessage("班级信息不能为空");
        }
        GroupMapper group = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacherId, clazzId, false);
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
            if (totalIntegral < rewardTotalIntegral) {
                // 去兑换 看看差几个
                int diff = rewardTotalIntegral - totalIntegral;
                int deductGold = diff / 5 + (diff % 5 > 0 ? 1 : 0);
                if (teacherDetail.getUserIntegral().getUsable() < deductGold) {
                    return MapMessage.errorMessage("园丁豆数量不足");
                }
                // 扣减老师金币
                IntegralHistory integralHistory = new IntegralHistory();
                integralHistory.setIntegral(deductGold * -10);
                integralHistory.setComment("练习报告奖励学生");
                integralHistory.setIntegralType(IntegralType.智慧教室老师兑换学豆.getType());
                integralHistory.setUserId(teacherId);
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
                history.setAddIntegralUserId(teacherId);
                MapMessage message = clazzIntegralServiceClient.getClazzIntegralService()
                        .changeClazzIntegral(history)
                        .getUninterruptibly();
                if (!message.isSuccess()) {
                    logger.warn("change clazz pool fail, groupId {}", group.getId());
                    return MapMessage.errorMessage("奖励失败");
                }
                // 执行发放
                ClazzIntegralHistory decrHistory = new ClazzIntegralHistory();
                decrHistory.setGroupId(group.getId());
                decrHistory.setClazzIntegralType(ClazzIntegralType.作业报告奖励学生.getType());
                decrHistory.setIntegral(-rewardTotalIntegral);
                decrHistory.setComment(ClazzIntegralType.作业报告奖励学生.getDescription());
                message = clazzIntegralServiceClient.getClazzIntegralService()
                        .changeClazzIntegral(decrHistory)
                        .getUninterruptibly();
                if (!message.isSuccess()) {
                    logger.warn("change clazz pool fail, groupId {}", group.getId());
                    return MapMessage.errorMessage("奖励失败");
                }
            } else {
                ClazzIntegralHistory decrHistory = new ClazzIntegralHistory();
                decrHistory.setGroupId(group.getId());
                decrHistory.setClazzIntegralType(ClazzIntegralType.作业报告奖励学生.getType());
                decrHistory.setIntegral(-rewardTotalIntegral);
                decrHistory.setComment(ClazzIntegralType.作业报告奖励学生.getDescription());
                MapMessage message = clazzIntegralServiceClient.getClazzIntegralService()
                        .changeClazzIntegral(decrHistory)
                        .getUninterruptibly();
                if (!message.isSuccess()) {
                    logger.warn("change clazz pool fail, groupId {}", group.getId());
                    return MapMessage.errorMessage("奖励失败");
                }
            }
            // 消息跳转地址
            String link = UrlUtils.buildUrlQuery("/studentMobile/homework/app/skip.vpage", MapUtils.m("homeworkId", homeworkId));
            // 消息内容
            String content = StringUtils.join(Arrays.asList(DateUtils.dateToString(newHomework.getCreateAt(), "MM月dd日"), newHomework.getSubject().getValue(), "作业完成的不错，", teacherDetail.respectfulName(), "奖励你"), "");

            Map<String, Object> extInfo = MapUtils.m("link", link, "t", "h5", "key", "j", "s", StudentAppPushType.HOMEWORK_SEND_INTEGRAL.getType(), "title", "获得老师奖励");

            // 给学生发放奖励学豆
            for (Map detail : details) {
                Long studentId = SafeConverter.toLong(detail.get("studentId"));
                Integer count = SafeConverter.toInt(detail.get("count"));
                Map<String, Object> message = new LinkedHashMap<>();
                message.put("studentId", studentId);
                message.put("count", count);
                message.put("teacherId", teacherId);
                message.put("homeworkLocation", newHomework.toLocation());
                message.put("content", content);
                message.put("link", link);
                message.put("extInfo", extInfo);
                String json = JsonUtils.toJson(message);
                Message msg = Message.newMessage().writeBinaryBody(json.getBytes(ICharset.DEFAULT_CHARSET));
                batchRewardIntegralQueueProducer.getProducer().produce(msg);
            }
        } catch (Exception ex) {
            logger.error("teacher reward student error, teacher {}, clazz {}, error {}", teacherId, clazzId, ex.getMessage());
            return MapMessage.errorMessage("奖励失败");
        }
        return MapMessage.successMessage("奖励成功");
    }

    /**
     * 老师推荐音频
     */
    @Override
    public MapMessage submitVoiceRecommend(String homeworkId, List<VoiceRecommend.RecommendVoice> recommendVoiceList, String recommendComment) {
        NewHomework homework = newHomeworkLoader.load(homeworkId);
        if (homework == null || Subject.ENGLISH != homework.getSubject()) {
            return MapMessage.errorMessage("错误的作业id");
        }
        if (!homework.isHomeworkChecked()) {
            return MapMessage.errorMessage("作业未检查，无法推荐录音");
        }
        if (CollectionUtils.isEmpty(recommendVoiceList)) {
            return MapMessage.errorMessage("请选择推荐内容");
        }
        VoiceRecommend voiceRecommend = voiceRecommendDao.load(homeworkId);
        if (voiceRecommend != null && voiceRecommend.hasRecommend()) {
            return MapMessage.errorMessage("已有推荐内容，无法重复推荐");
        }
        if (voiceRecommend == null) {
            voiceRecommend = new VoiceRecommend();
        }
        voiceRecommend.setId(homeworkId);
        voiceRecommend.setGroupId(homework.getClazzGroupId());
        voiceRecommend.setTeacherId(homework.getTeacherId());
        voiceRecommend.setHomeworkCreateTime(homework.getCreateAt());
        voiceRecommend.setHomeworkId(homeworkId);
        voiceRecommend.setRecommendComment(recommendComment);
        voiceRecommend.setRecommendVoiceList(recommendVoiceList);
        try {
            voiceRecommendDao.upsert(voiceRecommend);
        } catch (DocumentAccessException ex) {
            if (ex.getCause() != null && ex.getCause() instanceof MongoCommandException && ((MongoCommandException) ex.getCause()).getErrorCode() == 11000) {
                logger.error("Failed to upsert voiceRecommend!Duplicate id:{}", homeworkId);
            } else {
                logger.error("Failed to upsert voiceRecommend!id:{}", homeworkId, ex);
            }
        }
//        String voiceRecommendMsgContent = recommendComment;
//        Set<String> studentName = new LinkedHashSet<>();
//        recommendVoiceList.forEach(recommendVoice -> studentName.add(recommendVoice.getStudentName()));
//        voiceRecommendMsgContent += "\n老师推荐以下同学的优秀语音：" + StringUtils.join(studentName, "，");
//        Long teacherId = voiceRecommend.getTeacherId();
//        Teacher teacher = teacherLoaderClient.loadTeacher(voiceRecommend.getTeacherId());
//        //这里只是取发送人的ID
//        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
//        teacherId = mainTeacherId == null ? teacherId : mainTeacherId;
//        //这里才是取所有的学科
//        Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
//        List<Subject> subjectList = teacherLoaderClient.loadTeachers(relTeacherIds).values().stream().map(Teacher::getSubject).collect(Collectors.toList());
//        List<String> subjectStrList = subjectList.stream().sorted(Comparator.comparingInt(Subject::getId)).map(Subject::getValue).collect(Collectors.toList());
//        String subjectsStr = "（" + StringUtils.join(subjectStrList.toArray(), "，") + "）";
//        String teacherName = teacher == null ? "" : teacher.fetchRealname();
//        String em_push_title = teacherName + subjectsStr + "老师：" + voiceRecommendMsgContent;

//        //新的极光push
//        Map<String, Object> jpushExtInfo = new HashMap<>();
//        jpushExtInfo.put("studentId", "");
//        jpushExtInfo.put("s", ParentAppPushType.VOICE_RECOMMEND.name());
//        jpushExtInfo.put("url", "/view/mobile/common/voice_recommend?recommend_id=" + homeworkId);
//        appMessageServiceClient.sendAppJpushMessageByTags(em_push_title,
//                AppMessageSource.PARENT,
//                Collections.singletonList(JpushUserTag.CLAZZ_GROUP_REFACTOR.generateTag(SafeConverter.toString(homework.getClazzGroupId()))),
//                null,
//                jpushExtInfo);

        Map<Long, List<VoiceRecommend.RecommendVoice>> studentRecommendVoiceMap = recommendVoiceList.stream()
                .collect(Collectors.groupingBy(VoiceRecommend.RecommendVoice::getStudentId));
        List<AppMessage> appUserMessageDynamicList = studentRecommendVoiceMap.entrySet().stream()
                .map(e -> {
                    AppMessage appUserMessageDynamic = new AppMessage();
                    appUserMessageDynamic.setUserId(e.getKey());
                    appUserMessageDynamic.setMessageType(StudentAppPushType.VOICE_RECOMMEND_REMIND.getType());
                    appUserMessageDynamic.setTitle("练习被推荐");
                    appUserMessageDynamic.setContent("恭喜！你的练习完成的很优秀，被老师推荐了！要继续努力哦");
                    //学生端和老师端、家长端的UI不一样。所以单独区分是否是学生打开
                    appUserMessageDynamic.setLinkUrl("/view/mobile/common/voice_recommend?recommend_id=" + homeworkId + "&user_type=" + UserType.STUDENT.name());
                    appUserMessageDynamic.setLinkType(1);
                    return appUserMessageDynamic;
                })
                .collect(Collectors.toList());
        appUserMessageDynamicList.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
        List<Long> userIdList = new ArrayList<>(studentRecommendVoiceMap.keySet());
        String link = "/view/mobile/common/voice_recommend?recommend_id=" + homeworkId + "&user_type=" + UserType.STUDENT.name();
        appMessageServiceClient.sendAppJpushMessageByIds("恭喜！你的练习完成的很优秀，被老师推荐了！要继续努力哦。", AppMessageSource.STUDENT, userIdList,
                MapUtils.m("t", "h5", "key", "j", "link", link));

        if (CollectionUtils.isNotEmpty(recommendVoiceList)) {
            Map<String, Object> ext = new HashMap<>();
            ext.put("recommend_id", homeworkId);
            Set<Long> sids = recommendVoiceList.stream().map(VoiceRecommend.RecommendVoice::getStudentId).collect(Collectors.toSet());
            for (Long sid : sids) {
                parentRewardService.generateParentReward(sid, ParentRewardType.VOICE_RECOMMEND.name(), ext);
            }
        }

//        //发往Parent provider
//        ScoreCircleQueueCommand circleQueueCommand = new ScoreCircleQueueCommand();
//        circleQueueCommand.setGroupId(homework.getClazzGroupId());
//        circleQueueCommand.setCreateDate(new Date());
//        circleQueueCommand.setGroupCircleType("VOICE_RECOMMEND");
//        circleQueueCommand.setTypeId(voiceRecommend.getId());
//        circleQueueCommand.setImgUrl("");
//        circleQueueCommand.setLinkUrl("/view/mobile/common/voice_recommend?recommend_id=" + homeworkId);
//        if (CollectionUtils.isNotEmpty(studentName)) {
//            String subContent = studentName.size() > 1 ? studentName.iterator().next() + "等" + studentName.size() + "人" : studentName.iterator().next();
//            circleQueueCommand.setCardSubContent(subContent);
//        }
//        circleQueueCommand.setContent(voiceRecommendMsgContent);
//        newHomeworkParentQueueProducer.getProducer().produce(Message.newMessage().writeObject(circleQueueCommand));
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage submitReadReciteVoiceRecommend(String homeworkId, List<VoiceRecommend.ReadReciteVoice> recommendVoiceList, String recommendComment) {
        NewHomework homework = newHomeworkLoader.load(homeworkId);
        if (homework == null || Subject.CHINESE != homework.getSubject()) {
            return MapMessage.errorMessage("错误的作业id");
        }
        if (!homework.isHomeworkChecked()) {
            return MapMessage.errorMessage("作业未检查，无法推荐录音");
        }
        if (CollectionUtils.isEmpty(recommendVoiceList)) {
            return MapMessage.errorMessage("请选择推荐内容");
        }
        VoiceRecommend voiceRecommend = voiceRecommendDao.load(homeworkId);
        if (voiceRecommend != null && voiceRecommend.hasRecommend()) {
            return MapMessage.errorMessage("已有推荐内容，无法重复推荐");
        }
        if (voiceRecommend == null) {
            voiceRecommend = new VoiceRecommend();
        }
        voiceRecommend.setId(homeworkId);
        voiceRecommend.setGroupId(homework.getClazzGroupId());
        voiceRecommend.setTeacherId(homework.getTeacherId());
        voiceRecommend.setHomeworkCreateTime(homework.getCreateAt());
        voiceRecommend.setHomeworkId(homeworkId);
        voiceRecommend.setRecommendComment(recommendComment);
        voiceRecommend.setReadReciteVoices(recommendVoiceList);
        try {
            voiceRecommendDao.upsert(voiceRecommend);
        } catch (DocumentAccessException ex) {
            if (ex.getCause() != null && ex.getCause() instanceof MongoCommandException && ((MongoCommandException) ex.getCause()).getErrorCode() == 11000) {
                logger.error("Failed to upsert voiceRecommend!Duplicate id:{}", homeworkId);
            } else {
                logger.error("Failed to upsert voiceRecommend!id:{}", homeworkId, ex);
            }
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage submitDubbingVoiceRecommend(String homeworkId, List<VoiceRecommend.DubbingWithScore> dubbingVoiceList, String recommendComment) {
        NewHomework homework = newHomeworkLoader.load(homeworkId);
        if (homework == null || Subject.ENGLISH != homework.getSubject()) {
            return MapMessage.errorMessage("错误的作业id");
        }
        if (!homework.isHomeworkChecked()) {
            return MapMessage.errorMessage("作业未检查，无法推荐录音");
        }
        if (CollectionUtils.isEmpty(dubbingVoiceList)) {
            return MapMessage.errorMessage("请选择推荐内容");
        }
        DubbingRecommend dubbingRecommend = dubbingRecommendDao.load(homeworkId);
        if (dubbingRecommend != null && dubbingRecommend.hasRecommendDubbing()) {
            return MapMessage.errorMessage("已有推荐内容，无法重复推荐");
        }
        if (dubbingRecommend == null) {
            dubbingRecommend = new DubbingRecommend();
        }
        dubbingRecommend.setId(homeworkId);
        dubbingRecommend.setGroupId(homework.getClazzGroupId());
        dubbingRecommend.setTeacherId(homework.getTeacherId());
        dubbingRecommend.setHomeworkCreateTime(homework.getCreateAt());
        dubbingRecommend.setHomeworkId(homeworkId);
        dubbingRecommend.setRecommendComment(recommendComment);
        dubbingRecommend.setExcellentDubbingStu(dubbingVoiceList);
        try {
            dubbingRecommendDao.upsert(dubbingRecommend);
        } catch (DocumentAccessException ex) {
            if (ex.getCause() != null && ex.getCause() instanceof MongoCommandException && ((MongoCommandException) ex.getCause()).getErrorCode() == 11000) {
                logger.error("Failed to upsert submitDubbingVoiceRecommend !Duplicate id:{}", homeworkId);
            } else {
                logger.error("Failed to upsert submitDubbingVoiceRecommend !id:{}", homeworkId, ex);
            }
        }

//        String voiceRecommendMsgContent = recommendComment;
//        Set<String> studentName = new LinkedHashSet<>();
//        dubbingVoiceList.forEach(recommendVoice -> studentName.add(recommendVoice.getUserName()));
//        voiceRecommendMsgContent += "\n老师推荐以下同学的优秀配音：" + StringUtils.join(studentName, "，");
//        Long teacherId = dubbingRecommend.getTeacherId();
//        Teacher teacher = teacherLoaderClient.loadTeacher(dubbingRecommend.getTeacherId());
//        //这里只是取发送人的ID
//        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
//        teacherId = mainTeacherId == null ? teacherId : mainTeacherId;
//        //这里才是取所有的学科
//        Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
//        List<Subject> subjectList = teacherLoaderClient.loadTeachers(relTeacherIds).values().stream().map(Teacher::getSubject).collect(Collectors.toList());
//        List<String> subjectStrList = subjectList.stream().sorted(Comparator.comparingInt(Subject::getId)).map(Subject::getValue).collect(Collectors.toList());
//        String subjectsStr = "（" + StringUtils.join(subjectStrList.toArray(), "，") + "）";
//        String teacherName = teacher == null ? "" : teacher.fetchRealname();
//        String em_push_title = teacherName + subjectsStr + "老师：" + voiceRecommendMsgContent;
//
//        //新的极光push
//        Map<String, Object> jpushExtInfo = new HashMap<>();
//        jpushExtInfo.put("studentId", "");
//        jpushExtInfo.put("s", ParentAppPushType.VOICE_RECOMMEND.name());
//        jpushExtInfo.put("url", "/view/mobile/common/dubbing_recommend?recommend_id=" + homeworkId);
//        appMessageServiceClient.sendAppJpushMessageByTags(em_push_title,
//                AppMessageSource.PARENT,
//                Collections.singletonList(JpushUserTag.CLAZZ_GROUP_REFACTOR.generateTag(SafeConverter.toString(homework.getClazzGroupId()))),
//                null,
//                jpushExtInfo);

        Map<Long, List<VoiceRecommend.DubbingWithScore>> studentRecommendVoiceMap = dubbingVoiceList.stream()
                .collect(Collectors.groupingBy(VoiceRecommend.DubbingWithScore::getUserId));
        List<AppMessage> appUserMessageDynamicList = studentRecommendVoiceMap.entrySet().stream()
                .map(e -> {
                    AppMessage appUserMessageDynamic = new AppMessage();
                    appUserMessageDynamic.setUserId(e.getKey());
                    appUserMessageDynamic.setMessageType(StudentAppPushType.VOICE_RECOMMEND_REMIND.getType());
                    appUserMessageDynamic.setTitle("作业被推荐");
                    appUserMessageDynamic.setContent("恭喜！你的作业完成的很优秀，被老师推荐了！要继续努力哦");
                    //学生端和老师端、家长端的UI不一样。所以单独区分是否是学生打开
                    appUserMessageDynamic.setLinkUrl("/view/mobile/common/dubbing_recommend?recommend_id=" + homeworkId + "&user_type=" + UserType.STUDENT.name());
                    appUserMessageDynamic.setLinkType(1);
                    return appUserMessageDynamic;
                })
                .collect(Collectors.toList());
        appUserMessageDynamicList.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
        List<Long> userIdList = new ArrayList<>(studentRecommendVoiceMap.keySet());
        String link = "/view/mobile/common/dubbing_recommend?recommend_id=" + homeworkId + "&user_type=" + UserType.STUDENT.name();
        appMessageServiceClient.sendAppJpushMessageByIds("恭喜！你的作业完成的很优秀，被老师推荐了！要继续努力哦。", AppMessageSource.STUDENT, userIdList,
                MapUtils.m("t", "h5", "key", "j", "link", link));

        if (CollectionUtils.isNotEmpty(dubbingVoiceList)) {
            Map<String, Object> ext = new HashMap<>();
            ext.put("recommend_id", homeworkId);
            Set<Long> sids = dubbingVoiceList.stream().map(VoiceRecommend.DubbingWithScore::getUserId).collect(Collectors.toSet());
            for (Long sid : sids) {
                parentRewardService.generateParentReward(sid, ParentRewardType.VOICE_RECOMMEND.name(), ext);
            }
        }

//        //发往Parent provider
//        ScoreCircleQueueCommand circleQueueCommand = new ScoreCircleQueueCommand();
//        circleQueueCommand.setGroupId(homework.getClazzGroupId());
//        circleQueueCommand.setCreateDate(new Date());
//        circleQueueCommand.setGroupCircleType("VOICE_RECOMMEND");
//        circleQueueCommand.setTypeId(dubbingRecommend.getId());
//        circleQueueCommand.setImgUrl("");
//        circleQueueCommand.setLinkUrl("/view/mobile/common/dubbing_recommend?recommend_id=" + homeworkId);
//        if (CollectionUtils.isNotEmpty(studentName)) {
//            String subContent = studentName.size() > 1 ? studentName.iterator().next() + "等" + studentName.size() + "人" : studentName.iterator().next();
//            circleQueueCommand.setCardSubContent(subContent);
//        }
//        circleQueueCommand.setContent(voiceRecommendMsgContent);
//        newHomeworkParentQueueProducer.getProducer().produce(Message.newMessage().writeObject(circleQueueCommand));
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage submitImageTextRecommend(String homeworkId, List<BaseVoiceRecommend.ImageText> imageTextList) {
        NewHomework homework = newHomeworkLoader.load(homeworkId);
        if (homework == null || Subject.CHINESE != homework.getSubject()) {
            return MapMessage.errorMessage("错误的作业id");
        }
        if (!homework.isHomeworkChecked()) {
            return MapMessage.errorMessage("作业未检查，无法推荐录音");
        }
        ImageTextRecommend imageTextRecommend = imageTextRecommendDao.load(homeworkId);
        if (imageTextRecommend != null && CollectionUtils.isNotEmpty(imageTextRecommend.getImageTextList())) {
            return MapMessage.errorMessage("已有推荐内容，无法重复推荐");
        }
        if (imageTextRecommend == null) {
            imageTextRecommend = new ImageTextRecommend();
        }
        imageTextRecommend.setId(homeworkId);
        imageTextRecommend.setGroupId(homework.getClazzGroupId());
        imageTextRecommend.setTeacherId(homework.getTeacherId());
        imageTextRecommend.setHomeworkCreateTime(homework.getCreateAt());
        imageTextRecommend.setHomeworkId(homeworkId);
        imageTextRecommend.setRecommendComment("");
        imageTextRecommend.setImageTextList(imageTextList);
        try {
            imageTextRecommendDao.upsert(imageTextRecommend);
        } catch (DocumentAccessException ex) {
            if (ex.getCause() != null && ex.getCause() instanceof MongoCommandException && ((MongoCommandException) ex.getCause()).getErrorCode() == 11000) {
                logger.error("Failed to upsert submitImageTextRecommend !Duplicate id:{}", homeworkId);
            } else {
                logger.error("Failed to upsert submitImageTextRecommend !id:{}", homeworkId, ex);
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 家长请求老师推荐音频
     */
    @Override
    public MapMessage addVoiceRecommendRequestParent(String homeworkId, Long parentId, String parentName) {
        NewHomework homework = newHomeworkLoader.load(homeworkId);
        if (homework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        VoiceRecommend voiceRecommend = voiceRecommendDao.load(homeworkId);
        if (voiceRecommend == null) {
            voiceRecommend = new VoiceRecommend();
            voiceRecommend.setId(homeworkId);
            voiceRecommend.setHomeworkId(homeworkId);
            voiceRecommend.setTeacherId(homework.getTeacherId());
            voiceRecommend.setHomeworkCreateTime(homework.getCreateAt());
            voiceRecommend.setGroupId(homework.getClazzGroupId());
        }
        if (voiceRecommend.hasRecommend()) {
            return MapMessage.errorMessage("已有推荐内容");
        }
        List<VoiceRecommend.RequestParent> requestParentList = voiceRecommend.getRequestParentList();
        if (requestParentList == null) {
            requestParentList = new ArrayList<>();
        }
        boolean hasRequest = requestParentList.stream().anyMatch(requestParent -> Objects.equals(parentId, requestParent.getParentId()));
        if (hasRequest) {
            return MapMessage.errorMessage("已请求过老师推荐，无需重复请求");
        }
        VoiceRecommend.RequestParent requestParent = new VoiceRecommend.RequestParent();
        requestParent.setParentId(parentId);
        requestParent.setParentName(parentName);
        requestParent.setRequestTime(new Date());
        requestParentList.add(requestParent);
        voiceRecommend.setRequestParentList(requestParentList);
        voiceRecommendDao.upsert(voiceRecommend);
        return MapMessage.successMessage();
    }

    /**
     * 家长作业报告改版2.0（英语一期）
     * 口语句子染红
     */
    @Override
    public MapMessage processSyllable(NewHomeworkSyllable newHomeworkSyllable, String day) {
        NewHomeworkSyllable.Sentence sentence = newHomeworkSyllable.getLines().get(0);
        if (sentence != null) {
            Double score = sentence.getScore();
            String level = UnisoundScoreLevel.processLevel(score).name();
            if (level.equals("A") || level.equals("B")) {
                return MapMessage.errorMessage("请勿上传已达标数据");
            }
        }

        String audio = newHomeworkSyllable.getAudio();
        if (StringUtils.isBlank(audio)) {
            return MapMessage.errorMessage("语音引擎打分详情为空");
        }
        String[] audioArr = audio.split("/");
        StringBuilder newAudio = new StringBuilder();
        int length = audioArr.length;
        if (length < 3) {
            return MapMessage.errorMessage("音频地址有误");
        }
        audio = newAudio.append(audioArr[length - 3]).append("_").append(audioArr[length - 2]).append("_").append(audioArr[length - 1]).toString();

        NewHomeworkSyllable.ID id = new NewHomeworkSyllable.ID(day, newHomeworkSyllable.getUserId(), newHomeworkSyllable.getHomeworkId(), audio);
        newHomeworkSyllable.setId(id.toString());
        Date date = new Date();
        newHomeworkSyllable.setCreateAt(date);
        newHomeworkSyllable.setUpdateAt(date);
        newHomeworkQueueService.saveHomeworkSyllable(Collections.singletonList(newHomeworkSyllable));
        return MapMessage.successMessage();
    }


    /**
     * 17奖学金抽奖，获取老师总的钥匙数量
     *
     * @param type     0：表示增加抽奖次数，1：表示增加钥匙数量
     * @param deltaKey 增量值
     */
    @Override
    public MapMessage processScholarship(Long teacherId, Integer type, Integer deltaKey) {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 17奖学金抽奖，首次查看学情评估、布置作业获取更多积分 赠送一把钥匙
     */
    @Override
    public MapMessage processScholarshipFirstClick(Long teacherId, String scholarKeyType) {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 17奖学金抽奖，老师钥匙记录
     */
    @Override
    public MapMessage getScholarshipKeyRecord(Long teacherId) {
        return MapMessage.errorMessage("功能已下线");
    }

    @Override
    public MapMessage rewardHomeworkTaskIntegral(Teacher teacher, String recordId) {
        // 获取主学科老师id
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacher.getId());
        if (mainTeacherId == null) {
            mainTeacherId = teacher.getId();
        }
        HomeworkTaskRecord taskRecord = homeworkTaskRecordDao.load(recordId);
        if (taskRecord == null) {
            return MapMessage.errorMessage("任务记录不存在");
        }
        if (!Objects.equals(taskRecord.getTeacherId(), mainTeacherId)) {
            return MapMessage.errorMessage("没有领取权限");
        }
        if (taskRecord.getTaskStatus() == HomeworkTaskStatus.UNFINISHED) {
            return MapMessage.errorMessage("任务未完成");
        } else if (taskRecord.getTaskStatus() == HomeworkTaskStatus.REWARDED) {
            return MapMessage.errorMessage("奖励已领取");
        }
        HomeworkTaskType taskType = taskRecord.getTaskType();
        if (taskType == null) {
            return MapMessage.errorMessage("任务记录错误");
        }
        String comment = "老师完成作业任务领取奖励";
        int integralCount = SafeConverter.toInt(taskRecord.getIntegralCount());
        if (taskType != HomeworkTaskType.ACTIVITY_HOMEWORK) {
            comment = "老师完成" + taskType.getDescription() + "任务领取奖励";
        }
        IntegralHistory integralHistory = new IntegralHistory(mainTeacherId, IntegralType.TEACHER_ASSIGN_DAILY_HOMEWORK_REWARD, integralCount * 10);
        integralHistory.setComment(comment);
        integralHistory.setUniqueKey(recordId);
        try {
            MapMessage message = userIntegralService.changeIntegral(integralHistory);
            if (message.isSuccess()) {
                taskRecord.setTaskStatus(HomeworkTaskStatus.REWARDED);
                homeworkTaskRecordDao.upsert(taskRecord);
            } else {
                return MapMessage.errorMessage("领取失败：" + message.getInfo());
            }
            return MapMessage.successMessage().add("integralCount", integralCount);
        } catch (Exception e) {
            logger.error("reward homework task integral error", e);
            return MapMessage.errorMessage("领取失败，请稍候重试");
        }
    }

    @Override
    public void insertNewHomeworkBooks(Collection<NewHomeworkBook> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }

        List<SubHomeworkBook> subList = new ArrayList<>();
        List<ShardHomeworkBook> shardList = new ArrayList<>();

        entities.forEach(o -> {
            if (NewHomeworkUtils.isSubHomework(o.getId())) {
                subList.add(HomeworkTransform.NewHomeworkBookToSub(o));
            } else if (NewHomeworkUtils.isShardHomework(o.getId())) {
                shardList.add(HomeworkTransform.NewHomeworkBookToShard(o));
            }
        });

        if (CollectionUtils.isNotEmpty(subList)) {
            subHomeworkBookDao.inserts(subList);
        }
        if (CollectionUtils.isNotEmpty(shardList)) {
            shardHomeworkBookDao.inserts(shardList);
        }
    }

    public void inserts(Collection<NewHomework> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }

        Collection<ShardHomework> shardHomeworks = new ArrayList<>();
        Collection<ShardHomeworkPractice> shardHomeworkPractices = new ArrayList<>();
        entities.forEach(o -> {
            ShardHomework shardHomework = HomeworkTransform.NewHomeworkToShard(o);
            ShardHomeworkPractice shardHomeworkPractice = HomeworkTransform.NewHomeworkToShardHomeworkPractice(o);
            if (shardHomework != null && shardHomeworkPractice != null) {
                String month = MonthRange.newInstance(System.currentTimeMillis()).toString();
                String id = new ShardHomework.ID(month).toString();
                shardHomework.setId(id);
                shardHomeworkPractice.setId(id);
                shardHomeworks.add(shardHomework);
                shardHomeworkPractices.add(shardHomeworkPractice);
                o.setId(id);
            }
        });
        shardHomeworkPracticeDao.inserts(shardHomeworkPractices);
        shardHomeworkDao.inserts(shardHomeworks);
    }

    public Boolean updateNewHomeworkChecked(String id, Boolean checked, Date checkTime, HomeworkSourceType checkHomeworkSource) {
        if (NewHomeworkUtils.isShardHomework(id)) {
            return shardHomeworkDao.updateShardHomeworkChecked(id, checked, checkTime, checkHomeworkSource);
        }
        return subHomeworkDao.updateSubHomeworkChecked(id, checked, checkTime, checkHomeworkSource);
    }

    /**
     * 优秀录音推荐，首次分享到微信/QQ 增加2个园丁豆
     */
    public MapMessage addIntegral(Long teacherId, String homeworkId) {
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        if (mainTeacherId == null) {
            mainTeacherId = teacherId;
        }
        IntegralHistory integralHistory = new IntegralHistory(mainTeacherId, IntegralType.TEACHER_RECOMMEND_QUALITY_RECORD_REWARD, 2 * 10);
        integralHistory.setUniqueKey(homeworkId);
        integralHistory.setComment("老师推荐优秀录音奖励");
        try {
            MapMessage mapMessage;
            mapMessage = userIntegralService.changeIntegral(integralHistory);
            if (!mapMessage.isSuccess()) {
                return MapMessage.errorMessage("已经奖励过，不再奖励！").setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
            } else {
                return MapMessage.successMessage();
            }
        } catch (Exception ex) {
            logger.error("Failed to add integral,teacher {}, error {}", teacherId, ex.getMessage());
            return MapMessage.errorMessage("Failed to add integral" + ex.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<ObjectiveConfigType, Map<String, Object>> findAppsFromHomework(NewHomework newHomework, Collection<Long> groupIds, Map<ObjectiveConfigType, NewHomeworkPracticeContent> practiceContentMap) {
        Map<ObjectiveConfigType, Map<String, Object>> practiceMap = new LinkedHashMap<>();
        if (MapUtils.isEmpty(practiceContentMap)) {
            return practiceMap;
        }
        for (Map.Entry<ObjectiveConfigType, NewHomeworkPracticeContent> entry : practiceContentMap.entrySet()) {
            ObjectiveConfigType objectiveConfigType = entry.getKey();
            NewHomeworkPracticeContent newHomeworkPracticeContent = entry.getValue();
            Map<String, Object> practice = JsonUtils.safeConvertObjectToMap(newHomeworkPracticeContent);
            if (objectiveConfigType.isSpecial()) {
                List<Map<String, Object>> apps = (List<Map<String, Object>>) practice.get("apps");
                if (CollectionUtils.isNotEmpty(apps)) {
                    List<Map<String, Object>> copiedApps = new ArrayList<>();
                    for (Map<String, Object> app : apps) {
                        for (Long groupId : groupIds) {
                            Map<String, Object> copiedApp = new HashMap<>(app);
                            copiedApp.put("groupId", groupId);
                            copiedApps.add(copiedApp);
                        }
                    }
                    practice.put("apps", copiedApps);
                }
                List<Map<String, Object>> questions = (List<Map<String, Object>>) practice.get("questions");
                if (CollectionUtils.isNotEmpty(questions)) {
                    List<Map<String, Object>> copiedQuestions = new ArrayList<>();
                    for (Map<String, Object> question : questions) {
                        for (Long groupId : groupIds) {
                            Map<String, Object> copiedQuestion = new HashMap<>(question);
                            copiedQuestion.put("groupId", groupId);
                            copiedQuestions.add(copiedQuestion);
                        }
                    }
                    practice.put("questions", copiedQuestions);
                }
            }
            // 新绘本阅读特殊处理
            if (ObjectiveConfigType.LEVEL_READINGS == objectiveConfigType) {
                List<Map<String, Object>> apps = (List<Map<String, Object>>) practice.get("apps");
                if (CollectionUtils.isNotEmpty(apps)) {
                    List<Map<String, Object>> copiedApps = new ArrayList<>();
                    for (Map<String, Object> app : apps) {
                        List<Map<String, Object>> questions = (List<Map<String, Object>>) app.get("questions");
                        List<Map<String, Object>> oralQuestions = (List<Map<String, Object>>) app.get("oralQuestions");
                        List<String> practiceTypes = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(questions)) {
                            practiceTypes.add(PictureBookPracticeType.EXAM.name());
                        }
                        if (CollectionUtils.isNotEmpty(oralQuestions)) {
                            practiceTypes.add(PictureBookPracticeType.ORAL.name());
                        }
                        if (SafeConverter.toBoolean(app.get("containsDubbing"))) {
                            practiceTypes.add(PictureBookPracticeType.DUBBING.name());
                        }
                        Map<String, Object> copiedApp = new HashMap<>(app);
                        copiedApp.put("practiceTypes", practiceTypes);
                        copiedApps.add(copiedApp);
                    }
                    practice.put("apps", copiedApps);
                }
            } else if (ObjectiveConfigType.MENTAL_ARITHMETIC == objectiveConfigType) {
                // 口算训练特殊处理
                MentalArithmeticTimeLimit timeLimit = newHomeworkPracticeContent.getTimeLimit();
                int limitTime = timeLimit != null ? timeLimit.getTime() : 0;
                practice.put("timeLimit", limitTime);
            } else if (ObjectiveConfigType.DUBBING == objectiveConfigType && (
                    NewHomeworkType.MothersDay == newHomework.getType() || NewHomeworkType.Activity == newHomework.getType())) {
                // 趣味配音内容特殊处理
                List<Map<String, Object>> apps = (List<Map<String, Object>>) practice.get("apps");
                if (CollectionUtils.isNotEmpty(apps)) {
                    List<Map<String, Object>> copiedApps = new ArrayList<>();
                    for (Map<String, Object> app : apps) {
                        for (Long groupId : groupIds) {
                            Map<String, Object> copiedApp = new HashMap<>(app);
                            copiedApp.put("groupId", groupId);
                            copiedApps.add(copiedApp);
                        }
                    }
                    practice.put("apps", copiedApps);
                }
            } else if (ObjectiveConfigType.OCR_MENTAL_ARITHMETIC == objectiveConfigType) {
                // 纸质口算特殊处理
                List<Map<String, Object>> apps = new ArrayList<>();
                apps.add(MapUtils.m(
                        "workBookId", newHomeworkPracticeContent.getWorkBookId(),
                        "workBookName", newHomeworkPracticeContent.getWorkBookName(),
                        "homeworkDetail", newHomeworkPracticeContent.getHomeworkDetail()
                ));
                practice.put("apps", apps);
            } else if (ObjectiveConfigType.ORAL_COMMUNICATION == objectiveConfigType) {
                List<Map<String, Object>> apps = (List<Map<String, Object>>) practice.get("apps");
                if (CollectionUtils.isNotEmpty(apps)) {
                    List<Map<String, Object>> copiedApps = new ArrayList<>();
                    for (Map<String, Object> app : apps) {
                        Map<String, Object> copiedApp = new HashMap<>(app);
                        copiedApp.put("oralCommunicationId", app.get("stoneDataId"));
                        copiedApps.add(copiedApp);
                    }
                    practice.put("apps", copiedApps);
                }
            } else if (ObjectiveConfigType.WORD_TEACH_AND_PRACTICE == objectiveConfigType) {
                List<Map<String, Object>> apps = (List<Map<String, Object>>) practice.get("apps");
                if (CollectionUtils.isNotEmpty(apps)) {
                    List<Map<String, Object>> copiedApps = new ArrayList<>();
                    for (Map<String, Object> app : apps) {
                        Map<String, Object> copiedApp = new HashMap<>(app);
                        List<Map<String, Object>> wordExerciseQuestions = (List<Map<String, Object>>) app.get("wordExerciseQuestions");
                        List<Map<String, Object>> imageTextRhymeQuestions = (List<Map<String, Object>>) app.get("imageTextRhymeQuestions");
                        List<Map<String, Object>> chineseCharacterCultureCourseIds = (List<Map<String, Object>>) app.get("chineseCharacterCultureCourseIds");
                        List<Map> practiceTypes = new ArrayList<>();
                        Map practiceTypeMap;
                        if (CollectionUtils.isNotEmpty(wordExerciseQuestions)) {
                            practiceTypeMap = new HashedMap();
                            practiceTypeMap.put("seconds", 0);
                            practiceTypeMap.put("type", WordTeachModuleType.WORDEXERCISE.name());
                            practiceTypes.add(practiceTypeMap);
                        }
                        if (CollectionUtils.isNotEmpty(imageTextRhymeQuestions)) {
                            practiceTypeMap = new HashedMap();
                            practiceTypeMap.put("seconds", 0);
                            practiceTypeMap.put("type", WordTeachModuleType.IMAGETEXTRHYME.name());
                            practiceTypes.add(practiceTypeMap);
                        }
                        if (CollectionUtils.isNotEmpty(chineseCharacterCultureCourseIds)) {
                            practiceTypeMap = new HashedMap();
                            practiceTypeMap.put("seconds", 0);
                            practiceTypeMap.put("type", WordTeachModuleType.CHINESECHARACTERCULTURE.name());
                            practiceTypes.add(practiceTypeMap);
                        }
                        copiedApp.put("stoneDataId", app.get("stoneDataId"));
                        copiedApp.put("practiceTypes", practiceTypes);
                        copiedApps.add(copiedApp);
                    }
                    practice.put("apps", copiedApps);
                }
            } else if (ObjectiveConfigType.OCR_DICTATION == objectiveConfigType) {
                practice.put("ocrDictation", true);
            }
            practiceMap.put(objectiveConfigType, practice);
        }
        return practiceMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapMessage copyHomework(Teacher teacher, String homeworkId, Collection<Long> groupIds, String startTime, String endTime, HomeworkSourceType homeworkSourceType) {
        NewHomework newHomework = newHomeworkLoader.loadNewHomework(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        Map<Long, GroupTeacherMapper> teacherGroupMap = groupLoaderClient.loadTeacherGroups(teacher.getId(), false)
                .stream()
                .collect(Collectors.toMap(GroupTeacherMapper::getId, (gt) -> gt));
        for (Long groupId : groupIds) {
            if (!teacherGroupMap.containsKey(groupId)) {
                return MapMessage.errorMessage("没有班组{}的操作权限,请退出重新登录。", groupId);
            }
        }
        Map<ObjectiveConfigType, NewHomeworkPracticeContent> practiceContentMap = newHomework.findPracticeContents();
        Map<ObjectiveConfigType, Map<String, Object>> practiceMap = findAppsFromHomework(newHomework, groupIds, practiceContentMap);
        NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(homeworkId);
        Map<String, List<Map>> books = new LinkedHashMap<>();
        if (newHomeworkBook != null && newHomeworkBook.getPractices() != null) {
            LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practices = newHomeworkBook.getPractices();
            for (ObjectiveConfigType objectiveConfigType : practices.keySet()) {
                List<NewHomeworkBookInfo> bookInfos = practices.get(objectiveConfigType);
                List<Map> maps = bookInfos
                        .stream()
                        .map(JsonUtils::safeConvertObjectToMap)
                        .collect(Collectors.toList());
                books.put(objectiveConfigType.name(), maps);
            }
        }

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("duration", newHomework.getDuration());
        jsonMap.put("practices", practiceMap);
        jsonMap.put("books", books);
        List<String> clazzIds = new ArrayList<>();
        for (Long groupId : groupIds) {
            Long clazzId = teacherGroupMap.get(groupId).getClazzId();
            clazzIds.add(clazzId + "_" + groupId);
        }
        jsonMap.put("clazzIds", StringUtils.join(clazzIds, ","));
        jsonMap.put("homeworkType", newHomework.getNewHomeworkType());
        jsonMap.put("homeworkTag", newHomework.getHomeworkTag());
        jsonMap.put("startTime", startTime);
        jsonMap.put("endTime", endTime);
        jsonMap.put("subject", newHomework.getSubject());
        jsonMap.put("sourceHomeworkId", newHomework.getId());
        HomeworkSource homeworkSource = HomeworkSource.newInstance(jsonMap);
        return assignHomework(teacher, homeworkSource, HomeworkSourceType.App, newHomework.getNewHomeworkType(), newHomework.getHomeworkTag());
    }

    @Override
    public MapMessage assignBasicReviewHomework(Teacher teacher, HomeworkSource homeworkSource, HomeworkSourceType homeworkSourceType) {
        try {
            return AtomicLockManager.instance().wrapAtomic(assignBasicReviewHomeworkProcessor)
                    .keys(teacher.getIsInvite(), teacher.getSubject().getId())
                    .proxy()
                    .assign(teacher, homeworkSource, homeworkSourceType);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("作业布置中，请不要重复布置!").setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
        } catch (Exception ex) {
            logger.error("failed to save homework, teacher id {}, homework_data {}", teacher.getId(), homeworkSource, ex);
            return MapMessage.errorMessage("布置作业失败！").setErrorCode(ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK);
        }
    }

    @Override
    public MapMessage deleteBasicReviewHomework(Teacher teacher, String packageId) {
        BasicReviewHomeworkPackage homeworkPackage = basicReviewHomeworkPackageDao.load(packageId);
        if (homeworkPackage == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        if (!teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), homeworkPackage.getClazzGroupId())) {
            return MapMessage.errorMessage("您没有权限删除此作业");
        }
        try {
            Boolean success = basicReviewHomeworkPackageDao.updateDisableTrue(packageId);
            if (success) {
                if (CollectionUtils.isNotEmpty(homeworkPackage.getStages())) {
                    List<String> homeworkIds = homeworkPackage.getStages()
                            .stream()
                            .map(BasicReviewStage::getHomeworkId)
                            .collect(Collectors.toList());
                    for (String homeworkId : homeworkIds) {
                        success = updateDisabledTrue(homeworkId);
                        if (!success) {
                            break;
                        }
                    }
                }
                //删除作业老师广播
                Map<String, Object> teacherPublisherMap = new HashMap<>();
                teacherPublisherMap.put("messageType", HomeworkPublishMessageType.deleted);
                teacherPublisherMap.put("homeworkId", homeworkPackage.getId());
                teacherPublisherMap.put("teacherId", homeworkPackage.getTeacherId());
                teacherPublisherMap.put("groupId", homeworkPackage.getClazzGroupId());
                teacherPublisherMap.put("subject", homeworkPackage.getSubject());
                teacherPublisherMap.put("homeworkType", NewHomeworkType.BasicReview);
                newHomeworkPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(teacherPublisherMap)));
                return success ? MapMessage.successMessage("删除作业成功") : MapMessage.errorMessage("删除作业失败");
            }
            return MapMessage.errorMessage("删除作业失败");
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @Override
    public boolean updateNewHomeworkResultUrge(NewHomework.Location location, Long studentId, Long parentId, int beanNum) {
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(location, studentId, false);
        if (newHomeworkResult != null) {
            Date homeworkCreateAt = new Date(location.getCreateTime());
            if (homeworkCreateAt.before(NewHomeworkConstants.ALLOW_UPDATE_HOMEWORK_START_TIME)) {
                return false;
            }
            newHomeworkResult.setBeanNum(SafeConverter.toInt(beanNum));
            boolean updateSuccess = newHomeworkResultService.updateNewHomeworkResultUrge(newHomeworkResult);
            if (updateSuccess) {
                // 发送家长确认广播
                Map<String, Object> map = new HashMap<>();
                map.put("messageType", HomeworkPublishMessageType.confirm);
                map.put("homeworkId", location.getId());
                map.put("subject", location.getSubject());
                map.put("createAt", location.getCreateTime());
                map.put("startTime", location.getStartTime());
                map.put("endTime", location.getEndTime());
                map.put("homeworkType", location.getType());
                map.put("parentId", parentId);
                map.put("confirmAt", new Date().getTime());
                newHomeworkPublisher.getParentPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
            }
            return updateSuccess;
        }
        return false;
    }

    @Override
    public MapMessage uploaderDubbing(String id, String audioUrl, String videoUrl, String path) {
        AliyunossConfigManager configManager = AliyunossConfigManager.Companion.getInstance();
        AliyunOSSConfig config = configManager.getAliyunOSSConfig("homework");
        Objects.requireNonNull(config);
        String host = config.getHost();
        String bucket = config.getBucket();
        String endpoint = config.getPublicEndpoint();
        audioUrl = "https://" + host + audioUrl;

        DubbingSyntheticHistory dsh = new DubbingSyntheticHistory();
        dsh.setId(id);
        dsh.setAudioUrl(audioUrl);
        dsh.setVideoUrl(videoUrl);
        dsh.setSyntheticSuccess(false);
        dsh.setPath(path);
        dubbingSyntheticHistoryDao.upsert(dsh);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("id", id);
        message.put("audioUrl", audioUrl);
        message.put("videoUrl", videoUrl);
        message.put("bucket", bucket);
        message.put("endpoint", endpoint);
        message.put("path", path);
        message.put("fileName", id + ".mp4");
        String json = JsonUtils.toJson(message);
        Message msg = Message.newMessage().withPlainTextBody(json);
        dubbingSyntheticQueueProducer.getProducer().produce(msg);
        //返回阿里云视频地址
        return MapMessage.successMessage().add("ossVideoUrl", "https://" + bucket + "." + endpoint + "/" + path + "/" + id + ".mp4");
    }

    @Override
    public MapMessage uploadPictureBookPlusDubbing(String homeworkId, String pictureBookId, Long userId, List<PictureBookPlusDubbing.Content> contents, String screenMode) {
        List<PictureBookPlusDubbing.ParagraphSentence> sentences = contents.stream()
                .filter(content -> CollectionUtils.isNotEmpty(content.getParagraphs()))
                .map(PictureBookPlusDubbing.Content::getParagraphs)
                .flatMap(Collection::stream)
                .filter(paragraph -> CollectionUtils.isNotEmpty(paragraph.getSentences()))
                .map(PictureBookPlusDubbing.ContentParagraph::getSentences)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sentences)) {
            return MapMessage.errorMessage("绘本句子为空");
        }
        // 校验是否每句都有音频和分数
        for (PictureBookPlusDubbing.ParagraphSentence sentence : sentences) {
            if (StringUtils.isBlank(sentence.getUserAudioUrl())) {
                return MapMessage.errorMessage("存在音频为空的句子");
            }
            if (sentence.getMacScore() == null) {
                return MapMessage.errorMessage("存在打分为空的句子");
            }
        }
        // 计算得分和等级
        int totalScore = sentences.stream()
                .mapToInt(sentence -> SafeConverter.toInt(sentence.getMacScore()))
                .sum();
        double score = new BigDecimal(totalScore).divide(new BigDecimal(sentences.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        AppOralScoreLevel scoreLevel = AppOralScoreLevel.of(ChiVoxScoreLevel.processLevel(score).name());
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        String dubbingId = new PictureBookPlusDubbing.ID(homeworkId, pictureBookId, userId).toString();
        PictureBookPlusDubbing pictureBookPlusDubbing = new PictureBookPlusDubbing();
        pictureBookPlusDubbing.setId(dubbingId);
        pictureBookPlusDubbing.setScore(score);
        pictureBookPlusDubbing.setScoreLevel(scoreLevel);
        pictureBookPlusDubbing.setContents(contents);
        pictureBookPlusDubbing.setScreenMode(screenMode);
        try {
            pictureBookPlusDubbingDao.upsert(pictureBookPlusDubbing);
        } catch (Exception ex) {
            if (!MongoExceptionUtils.isDuplicateKeyError(ex)) {
                throw ex;
            }
        }
        return MapMessage.successMessage().add("dubbingId", dubbingId);
    }

    @Override
    public MapMessage uploadLiveCastPictureBookPlusDubbing(String homeworkId, String pictureBookId, Long userId, List<PictureBookPlusDubbing.Content> contents, String screenMode) {
        List<PictureBookPlusDubbing.ParagraphSentence> sentences = contents.stream()
                .filter(content -> CollectionUtils.isNotEmpty(content.getParagraphs()))
                .map(PictureBookPlusDubbing.Content::getParagraphs)
                .flatMap(Collection::stream)
                .filter(paragraph -> CollectionUtils.isNotEmpty(paragraph.getSentences()))
                .map(PictureBookPlusDubbing.ContentParagraph::getSentences)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sentences)) {
            return MapMessage.errorMessage("绘本句子为空");
        }
        // 校验是否每句都有音频和分数
        for (PictureBookPlusDubbing.ParagraphSentence sentence : sentences) {
            if (StringUtils.isBlank(sentence.getUserAudioUrl())) {
                return MapMessage.errorMessage("存在音频为空的句子");
            }
            if (sentence.getMacScore() == null) {
                return MapMessage.errorMessage("存在打分为空的句子");
            }
        }
        // 计算得分和等级
        int totalScore = sentences.stream()
                .mapToInt(sentence -> SafeConverter.toInt(sentence.getMacScore()))
                .sum();
        double score = new BigDecimal(totalScore).divide(new BigDecimal(sentences.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        AppOralScoreLevel scoreLevel = AppOralScoreLevel.of(ChiVoxScoreLevel.processLevel(score).name());
        LiveCastHomework liveCastHomework = newHomeworkLivecastLoader.loadLiveCastHomeworkIncludeDisabled(homeworkId);
        if (liveCastHomework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        String dubbingId = new PictureBookPlusDubbing.ID(homeworkId, pictureBookId, userId).toString();
        PictureBookPlusDubbing pictureBookPlusDubbing = new PictureBookPlusDubbing();
        pictureBookPlusDubbing.setId(dubbingId);
        pictureBookPlusDubbing.setScore(score);
        pictureBookPlusDubbing.setScoreLevel(scoreLevel);
        pictureBookPlusDubbing.setContents(contents);
        pictureBookPlusDubbing.setScreenMode(screenMode);
        pictureBookPlusDubbingDao.upsert(pictureBookPlusDubbing);
        return MapMessage.successMessage().add("dubbingId", dubbingId);
    }

    @Override
    public void saveAccessDeniedRecord(AccessDeniedRecord accessDeniedRecord) {
        Date curTime = new Date();
        String limitationKey = accessDeniedRecord.getLimitationKey();
        String comments = "{}({})请求接口{}超过{}次，当天禁止请求该接口";
        if (limitationKey.contains("API_MINUTE_LIMIT")) {
            comments = StringUtils.formatMessage(comments, "一分钟内", DateUtils.dateToString(curTime, "yyyyMMddHHmm"), accessDeniedRecord.getRequestPath(), accessDeniedRecord.getLimitationValue());
        } else if (limitationKey.contains("API_HOUR_LIMIT")) {
            comments = StringUtils.formatMessage(comments, "一小时内", DateUtils.dateToString(curTime, "yyyyMMddHH"), accessDeniedRecord.getRequestPath(), accessDeniedRecord.getLimitationValue());
        } else if (limitationKey.contains("API_DAY_LIMIT")) {
            comments = StringUtils.formatMessage(comments, "一天内", DateUtils.dateToString(curTime, "yyyyMMdd"), accessDeniedRecord.getRequestPath(), accessDeniedRecord.getLimitationValue());
        }
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(accessDeniedRecord.getUserId());
        userServiceRecord.setOperatorId("接口请求限制");
        userServiceRecord.setOperationType(UserServiceRecordOperationType.举报老师.name());
        userServiceRecord.setOperationContent("超出接口请求次数限制，当天禁止请求");
        userServiceRecord.setComments(comments);
        userServiceClient.saveUserServiceRecord(userServiceRecord);
        accessDeniedRecordPersistence.insert(accessDeniedRecord);
    }

    @Override
    public List<LightInteractionCourseResp> fetchLightInteractionCourse(Collection<String> courseIds) {

        if (CollectionUtils.isEmpty(courseIds)) {
            return Collections.emptyList();
        }
        Map<String, IntelDiagnosisCourse> intelDiagnosisCourseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(courseIds);
        if (MapUtils.isEmpty(intelDiagnosisCourseMap)) {
            return Collections.emptyList();
        }

        List<LightInteractionCourseResp> courseList = Lists.newArrayList();
        for (IntelDiagnosisCourse intelDiagnosisCourse : intelDiagnosisCourseMap.values()) {
            LightInteractionCourseResp resp = new LightInteractionCourseResp();
            String courseId = intelDiagnosisCourse.getId();
            resp.setId(courseId);

            resp.setName(StringUtils.isBlank(intelDiagnosisCourse.getName()) ? DETAIL_COURSE_NAME : intelDiagnosisCourse.getName());
            List<LightInteractionCourseResp.CoursePage> pages = Lists.newArrayList();
            List<EmbedPage> embedPages = intelDiagnosisCourse.getPages();
            if (CollectionUtils.isNotEmpty(embedPages)) {
                for (EmbedPage embedPage : embedPages) {
                    pages.add(new LightInteractionCourseResp.CoursePage(embedPage));
                }
            }
            resp.setPages(pages);
            resp.setTheme(intelDiagnosisCourse.getTheme());
            courseList.add(resp);
        }
        return courseList;
    }

    @Override
    public List<Map<String, Object>> fetchLightInteractionCourseV2(Collection<String> courseIds) {
        if (CollectionUtils.isEmpty(courseIds)) {
            return Collections.emptyList();
        }
        Map<String, IntelDiagnosisCourse> intelDiagnosisCourseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(courseIds);
        if (MapUtils.isEmpty(intelDiagnosisCourseMap)) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> courseList = Lists.newArrayList();
        for (IntelDiagnosisCourse intelDiagnosisCourse : intelDiagnosisCourseMap.values()) {
            Map<String, Object> course = new LinkedHashMap<>();
            Map<String, Object> theme = intelDiagnosisCourse.getTheme();
            String backgroundImage = "";
            if (MapUtils.isNotEmpty(theme)) {
                backgroundImage = SafeConverter.toString(theme.get("backgroundImage"), "");
            }
            course.put("backgroundImage", backgroundImage);
            List<EmbedPage> embedPages = intelDiagnosisCourse.getPages();
            if (CollectionUtils.isNotEmpty(embedPages)) {
                List<Map<String, Object>> pagesList = new LinkedList<>();
                for (EmbedPage embedPage : embedPages) {
                    pagesList.add(JsonUtils.convertJsonObjectToMap(embedPage.getPageContent()));
                }
                course.put("pages", pagesList);
            }
            courseList.add(course);
        }
        return courseList;
    }


    @Override
    public List<MicroVideoResp> fetchVideoCourse(Collection<String> videoIds) {

        if (CollectionUtils.isEmpty(videoIds)) {
            return Collections.emptyList();
        }
        Map<String, MicroVideoTask> videoTaskMap = intelDiagnosisClient.loadMicroVideoTaskByIdsIncludeDisabled(videoIds);
        if (MapUtils.isEmpty(videoTaskMap)) {
            return Collections.emptyList();
        }
        Set<String> kpMaterialIdSet = videoTaskMap.values().stream().map(MicroVideoTask::getKpMaterialId).collect(Collectors.toSet());
        Map<String, KnowledgePointMaterial> materialMap = intelDiagnosisClient.loadKnowledgePointMaterialByIdsIncludeDisabled(kpMaterialIdSet);

        List<MicroVideoResp> courseList = Lists.newArrayList();
        for (MicroVideoTask microVideo : videoTaskMap.values()) {
            KnowledgePointMaterial material = materialMap.get(microVideo.getKpMaterialId());
            if (material != null) {
                MicroVideoResp videoResp = new MicroVideoResp();
                videoResp.setId(microVideo.getId());
                videoResp.setName(microVideo.getName());
                videoResp.setSubjectId(microVideo.getSubjectId());
                videoResp.setImageUrl(material.getImageUrl());
                videoResp.setUrl(material.getVideoUrl());
                videoResp.setTaskType(microVideo.getTaskType());
                videoResp.setThreshold(microVideo.getThreshold());
                videoResp.setDraggable(microVideo.getDraggable());
                videoResp.setCorrectPhoneme(microVideo.getCorrectPhoneme());
                videoResp.setConfusablePhoneme(microVideo.getConfusablePhoneme());
                videoResp.setQuestions(microVideo.getQuestions());
                courseList.add(videoResp);
            }
        }
        return courseList;
    }

    @Override
    public MapMessage collectDubbing(TeacherDetail teacherDetail, String dubbingId) {
        Long teacherId = teacherDetail.getId();
        Subject subject = teacherDetail.getSubject();

        Map<String, Date> dubbingCollectionInfo = new HashMap<>();
        DubbingCollectionRecord dubbingCollectionRecord = dubbingCollectionRecordDao.loadDubbingCollectionRecord(teacherId, subject);

        if (dubbingCollectionRecord != null) {
            dubbingCollectionInfo = dubbingCollectionRecord.getDubbingCollectionInfo();
            Date collectDubbingDate = dubbingCollectionInfo.get(dubbingId);
            if (collectDubbingDate == null) {
                dubbingCollectionInfo.put(dubbingId, new Date());
            } else {
                dubbingCollectionInfo.put(dubbingId, null);
            }
            dubbingCollectionRecord.setDubbingCollectionInfo(dubbingCollectionInfo);
            dubbingCollectionRecordDao.upsert(dubbingCollectionRecord);
        } else {
            dubbingCollectionRecord = new DubbingCollectionRecord();
            String id = "{}-{}-{}-{}-{}";
            SchoolYear schoolYear = SchoolYear.newInstance();
            Integer year = schoolYear.year();
            Term term = schoolYear.currentTerm();
            id = StringUtils.formatMessage(id, teacherId, subject, year, term);
            dubbingCollectionInfo.put(dubbingId, new Date());

            dubbingCollectionRecord.setId(id);
            dubbingCollectionRecord.setSubject(subject);
            dubbingCollectionRecord.setTerm(term);
            dubbingCollectionRecord.setYear(year);
            dubbingCollectionRecord.setDubbingCollectionInfo(dubbingCollectionInfo);
            dubbingCollectionRecordDao.upsert(dubbingCollectionRecord);
        }

        return MapMessage.successMessage().add("dubbingId", dubbingId);
    }

    @Override
    public MapMessage loadNationalDayHomeworkAssignStatus(Teacher teacher) {
        return autoAssignBasicReviewHomeworkProcessor.loadAssignStatus(teacher);
    }

    @Override
    public MapMessage autoAssignNationalDayHomework(Teacher teacher) {
        try {
            return AtomicLockManager.instance().wrapAtomic(autoAssignBasicReviewHomeworkProcessor)
                    .keys(teacher.getId())
                    .proxy()
                    .autoAssign(teacher);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("作业布置中，请不要重复布置!").setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
        } catch (Exception ex) {
            logger.error("failed to save homework, teacher id {}", teacher.getId(), ex);
            return MapMessage.errorMessage("布置作业失败！").setErrorCode(ErrorCodeConstants.ERROR_CODE_SAVE_HOMEWORK);
        }
    }

    @Override
    public MapMessage deleteNationalDayHomework(Teacher teacher) {
        return autoAssignBasicReviewHomeworkProcessor.deleteNationalDayHomework(teacher);
    }

    @Override
    public MapMessage loadNationalDayClazzList(Teacher teacher) {
        return autoAssignBasicReviewHomeworkProcessor.loadNationalDayClazzList(teacher);
    }

    @Override
    public MapMessage loadNationalDaySummaryReport(Teacher teacher, String packageId) {
        return autoAssignBasicReviewHomeworkProcessor.loadNationDaySummaryReport(teacher, packageId);
    }

    @Override
    public MapMessage updateHomeworkRemindCorrection(String homeworkId) {
        try {
            Boolean update = shardHomeworkDao.updateHomeworkRemindCorrection(homeworkId);
            if (update) {
                return MapMessage.successMessage("推荐巩固练习成功");
            } else {
                return MapMessage.errorMessage("推荐巩固练习失败");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @Override
    public MapMessage getOriginImageUrlByProcessId(String processId) {
        return MapMessage.successMessage(ocrClassifyImagesPersistence.getOriginImageUrlByProcessId(processId));
    }

    @Override
    public MapMessage updateReportShareParts(String homeworkId, String shareParts) {
        ShardHomework shardHomework = shardHomeworkDao.load(homeworkId);
        if (shardHomework == null) {
            return MapMessage.errorMessage("作业id不存在");
        }
        List<String> addedShareParts = Arrays.asList(shareParts.split("_"));
        if (CollectionUtils.isEmpty(addedShareParts)) {
            return MapMessage.errorMessage("分享模块为空");
        }
        List<String> reportShareParts = shardHomework.getReportShareParts();
        Set<String> updateShareParts = new LinkedHashSet<>();
        updateShareParts.addAll(reportShareParts);
        updateShareParts.addAll(addedShareParts);
        shardHomework.getAdditions().put("reportShareParts", StringUtils.join(updateShareParts, "_"));
        shardHomeworkDao.upsert(shardHomework);
        return MapMessage.successMessage();
    }

    public MapMessage ocrMentalArithmeticCorrect(Long userId, String homeworkId, String url, String boxJson) {
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (newHomework == null) {
            return null;
        }
        Date currentDate = new Date();
        OcrMentalImageDetail.Box box = JsonUtils.fromJson(boxJson, OcrMentalImageDetail.Box.class);
        String day = DayRange.newInstance(newHomework.getCreateAt().getTime()).toString();
        NewHomeworkResult.ID id = new NewHomeworkResult.ID(day, newHomework.getSubject(), homeworkId, userId.toString());
        SubHomeworkResult subHomeworkResult = newHomeworkResultLoader.loadSubHomeworkResult(id.toString());
        boolean correct = false;
        String correctContent = "";
        if (subHomeworkResult.getPractices() != null && subHomeworkResult.getPractices().get(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC) != null) {
            BaseHomeworkResultAnswer resultAnswer = subHomeworkResult.getPractices().get(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC);
            Map<String, SubHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(resultAnswer.getOcrMentalAnswers());
            for (SubHomeworkProcessResult subHomeworkProcessResult : processResultMap.values()) {
                if (subHomeworkProcessResult.getOcrMentalImageDetail().getImg_url().equals(url)) {
                    List<OcrMentalImageDetail.Form> forms = subHomeworkProcessResult.getOcrMentalImageDetail().getForms();
                    for (OcrMentalImageDetail.Form form : forms) {
                        OcrMentalImageDetail.Box box1 = form.getBox();
                        if (box1.getWidth().equals(box.getWidth()) && box1.getHeight().equals(box.getHeight()) && box1.getLeft().equals(box.getLeft()) && box1.getTop().equals(box.getTop())) {
                            if (!form.isCorrect()) {
                                correctContent = form.getText();
                            }
                            form.setCorrectAt(currentDate);
                            correct = true;
                            break;
                        }
                    }
                    if (correct) {
                        // 更新omads
                        if (StringUtils.isNotBlank(correctContent) && subHomeworkProcessResult.getOcrMentalImageDetail().getOmads() != null) {
                            OcrMentalImageDetail.OcrMentalArithmeticDiagnosis ocrMentalArithmeticDiagnosis = subHomeworkProcessResult.getOcrMentalImageDetail().getOmads();
                            if (CollectionUtils.isNotEmpty(ocrMentalArithmeticDiagnosis.getItemPoints())) {
                                // 删掉第一个跟correctContent匹配的item
                                for (OcrMentalImageDetail.ItemPoint itemPoint : ocrMentalArithmeticDiagnosis.getItemPoints()) {
                                    if (itemPoint != null && StringUtils.equals(correctContent, itemPoint.getItemContent())) {
                                        ocrMentalArithmeticDiagnosis.getItemPoints().remove(itemPoint);
                                        break;
                                    }
                                }
                            }
                        }
                        Map<String, List<String>> textKpIds = new HashMap<>();
                        OcrMentalImageDetail.OcrMentalArithmeticDiagnosis omads = subHomeworkProcessResult.getOcrMentalImageDetail().getOmads();
                        if (omads != null && CollectionUtils.isNotEmpty(omads.getItemPoints())) {
                            for (OcrMentalImageDetail.ItemPoint itemPoint : omads.getItemPoints()) {
                                if (CollectionUtils.isNotEmpty(itemPoint.getPoints())) {
                                    for (OcrMentalImageDetail.Point point : itemPoint.getPoints()) {
                                        textKpIds.computeIfAbsent(itemPoint.getItemContent(), k -> new ArrayList<>()).add(point.getPointId());
                                    }
                                }
                            }
                        }
                        // 重新计算kpSymptoms
                        if (StringUtils.isNotBlank(correctContent) && MapUtils.isNotEmpty(subHomeworkProcessResult.getOcrMentalImageDetail().getKpSymptoms())) {
                            Map<String, List<String>> kpSymptoms = new HashMap<>();
                            for (OcrMentalImageDetail.Form form : subHomeworkProcessResult.getOcrMentalImageDetail().getForms()) {
                                String text = form.getText();
                                if (SafeConverter.toInt(form.getJudge(), -1) == 0
                                        && !form.isCorrect()
                                        && form.getSymptomAnalysis() != null
                                        && StringUtils.isNotEmpty(form.getSymptomAnalysis().getSymptom())) {
                                    OcrMentalImageDetail.OcrMentalArithmeticSymptomAnalysis symptomAnalysis = form.getSymptomAnalysis();
                                    List<String> kpIds = textKpIds.get(text);
                                    if (CollectionUtils.isNotEmpty(kpIds)) {
                                        for (String kpId : kpIds) {
                                            List<String> symptoms = kpSymptoms.computeIfAbsent(kpId, k -> new ArrayList<>());
                                            if (!symptoms.contains(symptomAnalysis.getSymptom())) {
                                                symptoms.add(symptomAnalysis.getSymptom());
                                            }
                                        }
                                    }
                                }
                            }
                            subHomeworkProcessResult.getOcrMentalImageDetail().setKpSymptoms(kpSymptoms);
                        }
                        newHomeworkProcessResultService.upsert(subHomeworkProcessResult);
                        break;
                    }
                }
            }
            if (correct) {
                int questionCount = 0;
                int correctQuestionCount = 0;
                for (SubHomeworkProcessResult processResult : processResultMap.values()) {
                    OcrMentalImageDetail ocrMentalImageDetail = processResult.getOcrMentalImageDetail();
                    if (ocrMentalImageDetail != null && CollectionUtils.isNotEmpty(ocrMentalImageDetail.getForms())) {
                        for (OcrMentalImageDetail.Form form : ocrMentalImageDetail.getForms()) {
                            if (form.getJudge() < 2) {
                                questionCount++;
                                if (Objects.equals(1, form.getJudge()) || form.getCorrectAt() != null) {
                                    correctQuestionCount++;
                                }
                            }
                        }
                    }
                }
                double score = 0D;
                if (questionCount != 0) {
                    score = new BigDecimal(correctQuestionCount * 100).divide(new BigDecimal(questionCount), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                }
                resultAnswer.setOcrMentalQuestionCount(questionCount);
                resultAnswer.setOcrMentalCorrectQuestionCount(correctQuestionCount);
                resultAnswer.setScore(score);
                resultAnswer.setCorrectedAt(new Date());
                subHomeworkResultDao.upsert(subHomeworkResult);
                return MapMessage.successMessage();
            }
        }
        return MapMessage.errorMessage("改判失败");
    }

    @Override
    public MapMessage uploaderResourceLibrary(UploaderResourceLibrary url) {
        uploaderResourceLibraryDao.insert(url);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage imageTextRhymeView(String stoneDataId, WordTeachModuleType wordTeachModuleType) {
        List<StoneBufferedData> stoneBufferedDataList = stoneDataLoaderClient.getStoneBufferedDataList(Collections.singleton(stoneDataId));
        if (CollectionUtils.isEmpty(stoneBufferedDataList)) {
            return MapMessage.errorMessage();
        }

        LinkedHashMap<String, Object> dataMap = new LinkedHashMap<>();
        StoneBufferedData stoneBufferedData = stoneBufferedDataList.get(0);
        if (stoneBufferedData != null && stoneBufferedData.getWordsPractice() != null && stoneBufferedData.getWordsPractice().getImageText() != null) {
            List<ImageTextRhyme> imageTextRhymes = stoneBufferedData.getWordsPractice().getImageText().getImageTextRhymes();
            if (CollectionUtils.isEmpty(imageTextRhymes)) {
                return MapMessage.errorMessage();
            }

            Set<String> allQuestionIds = new HashSet<>();
            imageTextRhymes.forEach(q -> {
                if (CollectionUtils.isNotEmpty(q.getQuestionIds())) {
                    allQuestionIds.addAll(q.getQuestionIds());
                }
            });
            // 题目信息
            Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionByDocIds(allQuestionIds)
                    .stream()
                    .collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));

            List<Map> practices = new LinkedList<>();
            List<Map> extraInfos = new LinkedList<>();
            for (ImageTextRhyme imageTextRhyme : imageTextRhymes) {
                LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();
                resultMap.put("chapterId", imageTextRhyme.getUuid());
                resultMap.put("title", imageTextRhyme.getTitle());
                resultMap.put("imageUrl", imageTextRhyme.getImageUrl());
                resultMap.put("questionCount", imageTextRhyme.getQuestionIds().size());
                resultMap.put("doCount", 0);
                resultMap.put("finished", false);
                practices.add(resultMap);
                LinkedHashMap<String, Object> questionMap = new LinkedHashMap<>();
                questionMap.put("chapterId", imageTextRhyme.getUuid());
                LinkedList<String> questionIds = new LinkedList<>();
                for (String questionId : imageTextRhyme.getQuestionIds()) {
                    if (allQuestionMap.get(questionId) != null) {
                        questionIds.add(allQuestionMap.get(questionId).getId());
                    }
                }
                questionMap.put("questionIds", questionIds);
                extraInfos.add(questionMap);
            }
            dataMap.put("practices", practices);
            dataMap.put("extraInfos", extraInfos);
            // 打分标准
            List<Map<String, Object>> sentenceScoreLevels = WordTeachUniSound7SentenceScoreLevel.levels;
            List<Map<String, Object>> scoreLevels = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(sentenceScoreLevels)) {
                for (Map<String, Object> scoreLevel : sentenceScoreLevels) {
                    scoreLevels.add(processScoreLevel(scoreLevel));
                }
            }
            dataMap.put("unisound7SentencescoreLevels", scoreLevels);
        }
        return MapMessage.successMessage().add("data", dataMap);
    }

    private Map<String, Object> processScoreLevel(Map<String, Object> scoreLevel) {
        String level = SafeConverter.toString(scoreLevel.get("level"));
        int minScore = SafeConverter.toInt(scoreLevel.get("minScore"));
        int maxScore = SafeConverter.toInt(scoreLevel.get("maxScore"));
        AppOralScoreLevel appOralScoreLevel = AppOralScoreLevel.of(level);
        return MapUtils.m(
                "level", level,
                "minScore", minScore,
                "maxScore", maxScore,
                "score", (int) appOralScoreLevel.getScore()
        );
    }

    @Override
    public MapMessage remindAssignOralCommunicationHomework(Long studentId) {
        List<Group> groupMappers = raikouSystem.loadStudentGroups(studentId);
        Group englishGroup = groupMappers.stream()
                .filter(g -> Subject.ENGLISH == g.getSubject())
                .findFirst()
                .orElse(null);
        if (englishGroup == null) {
            return MapMessage.errorMessage("没有英语班组");
        }
        Long groupId = englishGroup.getId();
        List<Teacher> englishTeachers = teacherLoaderClient.loadGroupTeacher(Collections.singleton(groupId), RefStatus.VALID).get(groupId);
        if (CollectionUtils.isEmpty(englishTeachers)) {
            return MapMessage.errorMessage("没有找到英语老师");
        }

        Long teacherId = englishTeachers.get(0).getId();
        RemindAssignHomeworkTeacher remindAssignHomeworkTeacher = new RemindAssignHomeworkTeacher();
        remindAssignHomeworkTeacher.setId(teacherId);
        remindAssignHomeworkTeacherPersistence.upsert(remindAssignHomeworkTeacher);

        OralCommunicationRemindAssignCacheManager cacheManager = newHomeworkCacheService.getOralCommunicationRemindAssignCacheManager();
        String cacheKey = cacheManager.getCacheKey(teacherId);
        List<Long> studentIds = cacheManager.load(cacheKey);
        if (CollectionUtils.isEmpty(studentIds)) {
            studentIds = new ArrayList<>();
        }
        if (!studentIds.contains(studentId)) {
            studentIds.add(studentId);
            cacheManager.set(cacheKey, studentIds);
        }
        return MapMessage.successMessage("提醒成功");
    }

    @Override
    public List<Long> loadRemindAssignTeacherIds() {
        Query query = Query.query(new Criteria());
        query.field().includes("ID");
        List<RemindAssignHomeworkTeacher> remindAssignHomeworkTeachers = remindAssignHomeworkTeacherPersistence.query(query);
        return remindAssignHomeworkTeachers.stream()
                .map(RemindAssignHomeworkTeacher::getId)
                .collect(Collectors.toList());
    }

    @Override
    public void sendRemindAssignMessage(List<Long> teacherIds) {
        OralCommunicationRemindAssignCacheManager cacheManager = newHomeworkCacheService.getOralCommunicationRemindAssignCacheManager();
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIds);
        for (Long teacherId : teacherIds) {
            String cacheKey = cacheManager.getCacheKey(teacherId);
            List<Long> studentIds = cacheManager.load(cacheKey);
            if (CollectionUtils.isNotEmpty(studentIds)) {
                cacheManager.evict(cacheKey);
                Teacher teacher = teacherMap.get(teacherId);
                if (teacher != null) {
                    sendSingleMessage(teacher, studentIds.size());
                }
            }
        }
    }

    private void sendSingleMessage(Teacher teacher, int studentCount) {
//        String linkUrl = "/view/mobile/teacher/junior/activity/oralcommunication.vpage?from=push";
        String content = "尊敬的" + teacher.fetchRealname() + "老师，英语口语交际上线啦，共有" + studentCount + "名同学邀请您给他们布置口语交际的作业。快去布置吧~";
        Map<String, Object> pushExtInfo = new HashMap<>();
        pushExtInfo.put("link", "");
        pushExtInfo.put("s", TeacherMessageType.ACTIVIY.getType());
        pushExtInfo.put("t", "h5");
        appMessageServiceClient.sendAppJpushMessageByIds(
                content,
                AppMessageSource.PRIMARY_TEACHER,
                Collections.singletonList(teacher.getId()),
                pushExtInfo
        );

        AppMessage message = new AppMessage();
        message.setUserId(teacher.getId());
        message.setTitle("提醒布置口语交际作业");
        message.setContent(content);
        message.setLinkType(1);
        message.setLinkUrl("/view/mobile/teacher/junior/activity/oralcommunication.vpage?from=message");
        message.setMessageType(TeacherMessageType.ACTIVIY.getType());
        messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
    }
}