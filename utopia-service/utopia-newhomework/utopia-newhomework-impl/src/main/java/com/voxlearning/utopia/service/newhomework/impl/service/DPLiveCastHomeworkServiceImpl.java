


package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;

import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.service.DPLiveCastHomeworkService;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkResultDao;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.consumer.ThirdPartyGroupLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = DPLiveCastHomeworkService.class)
@ExposeService(interfaceClass = DPLiveCastHomeworkService.class)
public class DPLiveCastHomeworkServiceImpl extends SpringContainerSupport implements DPLiveCastHomeworkService {
    @Inject
    private LiveCastHomeworkDao liveCastHomeworkDao;
    @Inject
    private ThirdPartyGroupLoaderClient thirdPartyGroupLoaderClient;
    @Inject
    private LiveCastHomeworkResultDao liveCastHomeworkResultDao;

    @Inject
    private NewHomeworkLivecastServiceImpl newHomeworkLivecastService;

    @Override
    public MapMessage deleteHomework(Long teacherId, String hid) {
        try {
            return AtomicLockManager.instance().wrapAtomic(this)
                    .keys(teacherId, hid)
                    .proxy()
                    .internalDeleteHomework(teacherId, hid);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("作业删除中，请不要重复删除!").setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
        } catch (Exception ex) {
            logger.error("failed to delete live cast homework, teacher id {}, homeworkId {}", teacherId, hid, ex);
        }
        return MapMessage.errorMessage("删除作业失败").setErrorCode(ErrorCodeConstants.ERROR_CODE_COMMON);
    }

    @Override
    public MapMessage noteComment(Long teacherId, String comment, Collection<Long> useIds, String hid) {
        try {
            return AtomicLockManager.instance().wrapAtomic(this)
                    .keys(teacherId, hid)
                    .proxy()
                    .internalBatchSaveLiveCastHomeworkComment(teacherId, hid, useIds, comment);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("处理中，请不要重复点击!");
        } catch (Exception ex) {
            logger.error("batch save homework comment error, hid:{}, tid:{}", hid, teacherId, ex);
            return MapMessage.errorMessage("评语失败", ex);
        }
    }

    @Override
    public MapMessage correctQuestions(String homeworkId, Long studentId, Map<String, Object> correctInfoMap) {
        return newHomeworkLivecastService.correctQuestions(homeworkId, studentId, correctInfoMap);
    }


    @Override
    public MapMessage newCorrectQuestions(String homeworkId, Long studentId, Map<String, Object> correctInfoMap, ObjectiveConfigType type) {
        return newHomeworkLivecastService.newCorrectQuestions(homeworkId, studentId, correctInfoMap, type);
    }

    public MapMessage internalBatchSaveLiveCastHomeworkComment(Long teacherId, String hid, Collection<Long> useIds, String comment) {
        if (teacherId == null || hid == null || CollectionUtils.isEmpty(useIds) || comment == null) {
            return MapMessage.errorMessage("参数有误");
        }
        LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(hid);
        if (liveCastHomework == null) {
            return MapMessage.errorMessage("liveCastHomework is not existed");
        }
        if (liveCastHomework.getClazzGroupId() == null) {
            return MapMessage.errorMessage("liveCastHomework of clazzGroupId {} is not existed", liveCastHomework.getId());
        }

        List<Long> allUseIds = thirdPartyGroupLoaderClient.loadGroupStudentIds(liveCastHomework.getClazzGroupId());
        Set<Long> newUseIds = useIds.stream()
                .filter(allUseIds::contains)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(useIds)) {
            return MapMessage.errorMessage("useIds is error of {}", useIds);
        }
        LiveCastHomework.Location location = liveCastHomework.toLocation();
        String month = MonthRange.newInstance(location.getCreateTime()).toString();

        Map<Long, String> liveCastHomeworkResultIdMap = newUseIds
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Function.identity(), o -> new LiveCastHomeworkResult.ID(month, location.getSubject(), location.getId(), o).toString()));

        Map<String, LiveCastHomeworkResult> liveCastHomeworkResultMap = liveCastHomeworkResultDao.loads(liveCastHomeworkResultIdMap.values());
        List<Long> failedUseIds = new LinkedList<>();
        for (Map.Entry<Long, String> entry : liveCastHomeworkResultIdMap.entrySet()) {
            boolean flag;
            if (liveCastHomeworkResultMap.containsKey(entry.getValue())) {
                LiveCastHomeworkResult liveCastHomeworkResult = liveCastHomeworkResultMap.get(entry.getValue());
                flag = liveCastHomeworkResultDao.noteCommentToBeginStudent(liveCastHomeworkResult, comment);
            } else {
                flag = liveCastHomeworkResultDao.noteCommentToUnbeginStudent(location, entry.getKey(), comment);
            }
            if (!flag) {
                failedUseIds.add(entry.getKey());
            }
        }
        if (!failedUseIds.isEmpty()) {
            return MapMessage.errorMessage("note comment failed with useId {}", failedUseIds);
        }

        return MapMessage.successMessage("评语成功");
    }

    public MapMessage internalDeleteHomework(Long teacherId, String hid) {
        LiveCastHomework liveCastHomework = liveCastHomeworkDao.load(hid);
        if (liveCastHomework == null) {
            return MapMessage.errorMessage("作业不存在");
        }
        if (!Objects.equals(teacherId, liveCastHomework.getTeacherId())) {
            return MapMessage.errorMessage("您没有权限删除此作业");
        }
        try {
            Boolean delete = liveCastHomeworkDao.updateDisabledTrue(hid);
            if (delete) {
                return MapMessage.successMessage("删除作业成功");
            } else {
                return MapMessage.errorMessage("删除作业失败");
            }
        } catch (Exception ex) {
            logger.error("failed to delete live cast homework, teacher id {}, homeworkId {}", teacherId, hid, ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }
}
