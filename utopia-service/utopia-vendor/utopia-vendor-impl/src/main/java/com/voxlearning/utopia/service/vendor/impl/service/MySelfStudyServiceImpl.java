package com.voxlearning.utopia.service.vendor.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.MySelfStudyService;
import com.voxlearning.utopia.service.vendor.api.entity.*;
import com.voxlearning.utopia.api.constant.MySelfStudyActionEvent;
import com.voxlearning.utopia.api.constant.MySelfStudyActionType;
import com.voxlearning.utopia.service.vendor.impl.dao.LiveCastIndexRefinedLessonsDao;
import com.voxlearning.utopia.service.vendor.impl.dao.LiveCastIndexRemindDao;
import com.voxlearning.utopia.service.vendor.impl.dao.MySelfStudyDataDao;
import com.voxlearning.utopia.service.vendor.impl.dao.StudyAppDataDao;
import com.voxlearning.utopia.service.vendor.impl.handler.MySelfStudyEventHandler;
import com.voxlearning.utopia.service.vendor.impl.queue.MyselfStudyQueueProducer;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ApplicationObjectSupport;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * 我的自学
 *
 * @author jiangpeng
 * @since 2016-10-20 上午11:48
 **/
@Named
@Service(interfaceClass = MySelfStudyService.class)
@ExposeServices({
        @ExposeService(interfaceClass = MySelfStudyService.class, version = @ServiceVersion(version = "20180703"))
})
public class MySelfStudyServiceImpl extends ApplicationObjectSupport implements MySelfStudyService, InitializingBean {

    private final EnumMap<MySelfStudyActionType, MySelfStudyEventHandler> handlers = new EnumMap<>(MySelfStudyActionType.class);

    @Inject
    private MySelfStudyDataDao mySelfStudyDataDao;

    @Inject private MyselfStudyQueueProducer myselfStudyQueueProducer;

    @Inject private StudyAppDataDao studyAppDataDao;

    @Inject
    private MySelfStudyGlobalMsgServiceImpl mySelfStudyGlobalMsgService;

    @Inject
    private LiveCastIndexRefinedLessonsDao liveCastIndexRefinedLessonsDao;

    @Inject
    private LiveCastIndexRemindDao liveCastIndexRemindDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), MySelfStudyEventHandler.class, false, true)
                .values().forEach(e -> handlers.put(e.getMySelfStudyActionType(), e));
    }


    @Override
    public void handleEvent(MySelfStudyActionEvent event) {
        if (event == null)
            return;

        if (event.getUserId() == null || event.getUserId() == 0)
            return;
        if (event.getSelfStudyType() == null || SelfStudyType.UNKNOWN == event.getSelfStudyType())
            return;

        handlers.getOrDefault(event.getMySelfStudyActionType(), MySelfStudyEventHandler.NOP).handle(event);
    }

    @Override
    public List<MySelfStudyData> loadMySelfStudyDateBySId(Long studentId) {
        return mySelfStudyDataDao.loadByStudentId(studentId);
    }

    @Override
    public AlpsFuture<StudyAppData> loadStudyAppData(Long userId, SelfStudyType selfStudyType) {
        return new ValueWrapperFuture<>(studyAppDataDao.loadUserStudyAppData(userId, selfStudyType));
    }

    @Override
    public AlpsFuture<LiveCastIndexRemind.RemindContent> loadStudentLiveCastRemind(StudentDetail studentDetail) {
        LiveCastIndexRemind uninterruptibly = loadStudentLiveCastRemindV2(studentDetail).getUninterruptibly();
        return new ValueWrapperFuture<>(uninterruptibly == null ?  null :uninterruptibly.getRemindContent());
    }

    @Override
    public AlpsFuture<LiveCastIndexRemind> loadStudentLiveCastRemindV2(StudentDetail studentDetail){
        if (studentDetail == null)
            return null;
        List<String> tags = processTag(studentDetail);
        Date date = new Date();
        Map<String, LiveCastIndexRemind> loads = liveCastIndexRemindDao.loads(tags);
        MySelfStudyEntryGlobalMsg mySelfStudyEntryGlobalMsg = mySelfStudyGlobalMsgService.getMySelfStudyEntryGlobalMsgMap().get(SelfStudyType.LIVECAST);

        List<LiveCastIndexRemind> list = new ArrayList<>(loads.values());
        if (mySelfStudyEntryGlobalMsg != null && mySelfStudyEntryGlobalMsg.getIndexRemind() != null)
            list.add(mySelfStudyEntryGlobalMsg.getIndexRemind());
        LiveCastIndexRemind liveCastIndexRemind = list
                .stream().filter(t -> t.getExtra().getEndTime() != null && date.before(t.getExtra().getEndTime()))
                .sorted((o1, o2) -> o2.getExtra().getPriority().compareTo(o1.getExtra().getPriority())).findFirst().orElse(null);
        if (liveCastIndexRemind == null)
            return new ValueWrapperFuture<>(null);
        return new ValueWrapperFuture<>(liveCastIndexRemind);
    }


    @Override
    public AlpsFuture<List<LiveCastIndexRefinedLessons.LessonInfo>> loadStudentLiveCastRefinedLessons(StudentDetail studentDetail) {
        if (studentDetail == null)
            return null;
        List<String> tags = processTag(studentDetail);
        Date date = new Date();
        Map<String, LiveCastIndexRefinedLessons> loads = liveCastIndexRefinedLessonsDao.loads(tags);
        MySelfStudyEntryGlobalMsg mySelfStudyEntryGlobalMsg = mySelfStudyGlobalMsgService.getMySelfStudyEntryGlobalMsgMap().get(SelfStudyType.LIVECAST);
        List<LiveCastIndexRefinedLessons> list = new ArrayList<>(loads.values());
        if (mySelfStudyEntryGlobalMsg != null && mySelfStudyEntryGlobalMsg.getRefinedLessons() != null)
            list.add(mySelfStudyEntryGlobalMsg.getRefinedLessons());
        LiveCastIndexRefinedLessons liveCastIndexRefinedLessons = list
                .stream().filter(t -> t.getExtra().getEndTime() != null && date.before(t.getExtra().getEndTime()))
                .sorted((o1, o2) -> o2.getExtra().getPriority().compareTo(o1.getExtra().getPriority())).findFirst().orElse(null);
        if (liveCastIndexRefinedLessons == null)
            return new ValueWrapperFuture<>(Collections.emptyList());
        return new ValueWrapperFuture<>(liveCastIndexRefinedLessons.getLessonInfoList());
    }


    private List<String> processTag(StudentDetail studentDetail){
        List<String> tags = new ArrayList<>(6);
        tags.add(LiveCastTargetAndExtra.generateId(LiveCastTargetAndExtra.Target.Type.sid, studentDetail.getId()));
        Clazz clazz = studentDetail.getClazz();
        if (clazz != null){
            int level = clazz.getClazzLevel().getLevel();
            tags.add(LiveCastTargetAndExtra.generateId(LiveCastTargetAndExtra.Target.Type.clazzLevel, level));
        }
        Integer provinceId = studentDetail.getRootRegionCode();
        if (provinceId != null)
            tags.add(LiveCastTargetAndExtra.generateId(LiveCastTargetAndExtra.Target.Type.regionId, provinceId));
        Integer countyId = studentDetail.getStudentSchoolRegionCode();
        if (countyId != null)
            tags.add(LiveCastTargetAndExtra.generateId(LiveCastTargetAndExtra.Target.Type.regionId, countyId));
        Integer cityCode = studentDetail.getCityCode();
        if (cityCode != null)
            tags.add(LiveCastTargetAndExtra.generateId(LiveCastTargetAndExtra.Target.Type.regionId, cityCode));
        tags.add(LiveCastTargetAndExtra.generateId(LiveCastTargetAndExtra.Target.Type.all, 0));
        return tags;
    }


    @Override
    public void sendMyselfstudyMessage(Message message) {
        if (message != null) {
            myselfStudyQueueProducer.getProducer().produce(message);
        }
    }
}
