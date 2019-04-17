package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.poetry;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.raikou.service.user.api.data.StudentParentRefCapsule;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.newhomework.api.constant.ModelType;
import com.voxlearning.utopia.service.newhomework.api.context.AncientPoetryProcessContext;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMission;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMissionResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.AncientPoetryResultCacheMapper;
import com.voxlearning.utopia.service.newhomework.consumer.cache.AncientPoetryResultCacheManager;
import com.voxlearning.utopia.service.newhomework.impl.dao.poetry.AncientPoetryMissionResultDao;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkCacheServiceImpl;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author majianxin
 */
@Named
public class AP_FinishActivity extends SpringContainerSupport implements AncientPoetryResultTask {

    @Inject private RaikouSystem raikouSystem;
    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;
    @Inject private AncientPoetryMissionResultDao ancientPoetryMissionResultDao;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    @Override
    public void execute(AncientPoetryProcessContext context) {
        AlpsThreadPool.getInstance().submit(() -> finishActivity(context));
    }

    private void finishActivity(AncientPoetryProcessContext context) {
        AncientPoetryResultCacheMapper cacheMapper = context.getCacheMapper();
        //没有完成活动并且不是订正的时候才需要判定是否完成所有关卡
        if (!cacheMapper.isFinished() && ((context.getModelType().equals(ModelType.FUN) && !context.isCorrect() && context.getMissionResult().isFinished()) || context.isParentMission())) {
            Set<String> missionResultIds = context.getPoetryActivity().getMissions()
                    .stream()
                    .map(mission -> AncientPoetryMissionResult.generateId(context.getActivityId(), mission.getMissionId(), context.getStudentId(), Boolean.FALSE))
                    .collect(Collectors.toSet());
            long finishMissionCount = ancientPoetryMissionResultDao.loads(missionResultIds).values().stream().filter(AncientPoetryMissionResult::isFinished).count();
            cacheMapper.setFinishMissionCount(SafeConverter.toInt(finishMissionCount));
            //助力关卡是-1（AncientPoetryMission.getHelpMissionId）,只有当助力关卡和普通关卡全都完成的情况下才算通关
            boolean isParentMissionFinished = cacheMapper.getMissionCache().containsKey(AncientPoetryMission.getHelpMissionId());
            if (finishMissionCount == missionResultIds.size() && isParentMissionFinished) {
                cacheMapper.setFinishAt(context.getCurrentDate());
            }

            // 学生完成3关，但是家长未助力，发送短信
            // 判断是否绑定家长，未绑定，给孩子手机号发短信，已绑定，给家长手机号发短信
            if (finishMissionCount == 3 && !isParentMissionFinished) {
                sendSms(context);
            }
        }

        // 更新缓存
        AncientPoetryResultCacheManager cacheManager = newHomeworkCacheService.getAncientPoetryResultCacheManager();
        String cacheKey = cacheManager.getCacheKey(context.getActivityId(), context.getStudentId());
        cacheManager.addPoetryResult(cacheKey, cacheMapper);
    }

    private void sendSms(AncientPoetryProcessContext context) {
        String content = "亲子诗词大会火热进行中，老师已经报名，打开一起学为孩子助力赢得荣誉！";
        Long sendSmsUserId = context.getStudentId();
        if (context.getParentId() == null) {
            StudentParentRefCapsule parentRefCapsule = raikouSystem.findStudentParentRefs(context.getStudentId());
            List<StudentParentRef> parentRefs = parentRefCapsule.asStudentIdGroup().get(context.getStudentId());
            if (CollectionUtils.isNotEmpty(parentRefs)) {
                StudentParentRef parentRef = parentRefs.stream().filter(StudentParentRef::isKeyParent).findFirst().orElse(parentRefs.get(0));
                sendSmsUserId = parentRef.getParentId();
            } else {
                content = "亲子诗词大会火热进行中，老师已经报名，请家长打开一起学助力赢得荣誉！";
            }
        } else {
            sendSmsUserId = context.getParentId();
        }
        String mobile = sensitiveUserDataServiceClient.loadUserMobile(sendSmsUserId);
        if (StringUtils.isNotBlank(mobile)) {
            SmsMessage smsMessage = new SmsMessage();
            smsMessage.setMobile(mobile);
            smsMessage.setType(SmsType.EDU_BIZ_DEVELOP_BU.name());
            smsMessage.setSmsContent(content + " https://17zyw.cn/iuRnQFuj");
            smsServiceClient.getSmsService().sendSms(smsMessage);
        }
    }
}
