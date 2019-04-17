package com.voxlearning.utopia.service.vendor.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.vendor.api.DPMySelfStudyService;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastIndexRefinedLessons;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastIndexRemind;
import com.voxlearning.utopia.service.vendor.impl.queue.LiveCastIndexDataQueueProducer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 给楼上微课堂直播客用的
 * @author jiangpeng
 * @since 2017-05-22 下午5:52
 **/
@Slf4j
@Named
@Service(interfaceClass = DPMySelfStudyService.class)
@ExposeService(interfaceClass = DPMySelfStudyService.class)
public class DPMySelfStudyServiceImpl implements DPMySelfStudyService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private MySelfStudyServiceImpl mySelfStudyService;

    @Inject
    private LiveCastIndexDataQueueProducer liveCastIndexDataQueueProducer;

    @Deprecated
    @Override
    public Boolean bookLiveCast(Long parentId) {
        try {
            mySelfStudyService.updateShow(parentId, SelfStudyType.LIVECAST, true);
        }catch (Exception e){
            logger.error("bookLiveCast error !", e);
            return false;
        }
        return true;
    }

    @Override
    public Boolean liveCastText(Long parentId, String text) {
        try {
            mySelfStudyService.updateSelfStudyProgress(parentId, SelfStudyType.LIVECAST, text);
        }catch (Exception e){
            logger.error("liveCastText error", e);
            return false;
        }
        return true;
    }

    @Override
    public MapMessage upstairsEntryShow(Long parentId, String entryKey, Boolean show) {
        if (parentId == null || show == null || StringUtils.isBlank(entryKey))
            return MapMessage.errorMessage("错误的参数");

        SelfStudyType selfStudyType = SelfStudyType.of(entryKey);
        if (selfStudyType != SelfStudyType.LIVECAST && selfStudyType != SelfStudyType.SMALL_CLASS
                && selfStudyType != SelfStudyType.REFINED_LESSON)
            return MapMessage.successMessage("错误的entryKey");
        try {
            mySelfStudyService.updateShow(parentId, selfStudyType, show);
        }catch (Exception e){
            logger.error("upstairsEntryShow error !", e);
            return MapMessage.errorMessage(e.getMessage());
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage upstairsEntryGlobalMsg(String entryKey, String msg) {
        SelfStudyType selfStudyType = SelfStudyType.of(entryKey);
        if (selfStudyType == SelfStudyType.UNKNOWN)
            return MapMessage.successMessage("错误的entryKey");
        try {
            mySelfStudyService.globalMsg(selfStudyType, msg);
        }catch (Exception e){
            logger.error("upstairsEntryGlobalMsg error !" , e);
            return MapMessage.errorMessage(e.getMessage());
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage updateStudyAppUserNotify(String entryKey, Long userId, String notifyContent, String notifyUniqueId) {
        SelfStudyType selfStudyType = SelfStudyType.of(entryKey);
        if (selfStudyType == null || selfStudyType == SelfStudyType.UNKNOWN)
            return MapMessage.errorMessage("error appKey");
        try {
            mySelfStudyService.updateUserNotify(userId, selfStudyType, notifyContent, notifyUniqueId);
        }catch (Exception e){
            logger.error("updateStudyAppUserNotify error !" , e);
            return MapMessage.errorMessage(e.getMessage());
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage pushJztIndexRemind(String data) {
        if (StringUtils.isBlank(data))
            return MapMessage.errorMessage("没有数据");
        LiveCastIndexRemind liveCastIndexRemind = JsonUtils.fromJson(data, LiveCastIndexRemind.class);
        if (liveCastIndexRemind == null)
            return MapMessage.errorMessage("数据格式错误");
        if (liveCastIndexRemind.getTarget() == null)
            return MapMessage.errorMessage("没有 target");
        if (liveCastIndexRemind.getTarget().getType() == null)
            return MapMessage.errorMessage("target type 错误");
        if (liveCastIndexRemind.getTarget().getValue() == null)
            return MapMessage.errorMessage("target value 错误");
        if (liveCastIndexRemind.getExtra() == null)
            return MapMessage.errorMessage("没有 extra");
        if (liveCastIndexRemind.getExtra().getPriority() == null)
            return MapMessage.errorMessage("extra priority 错误");
        if (liveCastIndexRemind.getRemindContent() == null)
            return MapMessage.errorMessage("没有通知内容");
        if (StringUtils.isBlank(liveCastIndexRemind.getRemindContent().getText()))
            return MapMessage.errorMessage("通知内容是空串？");
        if (StringUtils.isBlank(liveCastIndexRemind.getRemindContent().getUrl()))
            return MapMessage.errorMessage("跳转连接是空的？");
        liveCastIndexDataQueueProducer.getProducer().produce(Message.newMessage().writeObject(liveCastIndexRemind));
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage pushJztIndexRefinedLessons(String data) {
        if (StringUtils.isBlank(data))
            return MapMessage.errorMessage("没有数据");
        LiveCastIndexRefinedLessons liveCastIndexRefinedLessons = JsonUtils.fromJson(data, LiveCastIndexRefinedLessons.class);
        if (liveCastIndexRefinedLessons == null)
            return MapMessage.errorMessage("数据格式错误");
        if (liveCastIndexRefinedLessons.getTarget() == null)
            return MapMessage.errorMessage("没有 target");
        if (liveCastIndexRefinedLessons.getTarget().getType() == null)
            return MapMessage.errorMessage("target type 错误");
        if (liveCastIndexRefinedLessons.getTarget().getValue() == null)
            return MapMessage.errorMessage("target value 错误");
        if (liveCastIndexRefinedLessons.getExtra() == null)
            return MapMessage.errorMessage("没有 extra");
        if (liveCastIndexRefinedLessons.getExtra().getPriority() == null)
            return MapMessage.errorMessage("extra priority 错误");

        if (CollectionUtils.isEmpty(liveCastIndexRefinedLessons.getLessonInfoList()))
            return MapMessage.errorMessage("没有推荐课程？");

        for (LiveCastIndexRefinedLessons.LessonInfo lessonInfo : liveCastIndexRefinedLessons.getLessonInfoList()) {
            String s = validateLessonInfo(lessonInfo);
            if (StringUtils.isNotBlank(s))
                return MapMessage.errorMessage(s);
        }
        liveCastIndexDataQueueProducer.getProducer().produce(Message.newMessage().writeObject(liveCastIndexRefinedLessons));
        return MapMessage.successMessage();
    }

    private String validateLessonInfo(LiveCastIndexRefinedLessons.LessonInfo lessonInfo){
        if (lessonInfo == null)
            return "课程信息为空";
        if (StringUtils.isBlank(lessonInfo.getCoverUrl()))
            return "课程封面为空";
        if (StringUtils.isBlank(lessonInfo.getTitle()))
            return "课程 title 为空";
        if (StringUtils.isBlank(lessonInfo.getLabel()))
            return "lable 为Null";
        if (StringUtils.isBlank(lessonInfo.getDesc()))
            return "desc 为 null";
        if (StringUtils.isBlank(lessonInfo.getUrl()))
            return "url 为 null";
        if (lessonInfo.getNeedLogin() == null)
            return "needLogin 为 null";
        return "";

    }

}
