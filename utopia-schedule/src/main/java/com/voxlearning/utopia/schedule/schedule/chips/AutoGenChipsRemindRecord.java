package com.voxlearning.utopia.schedule.schedule.chips;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.ai.api.ChipsActiveService;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishClazzService;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishContentLoader;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClass;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClassUserRef;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 薯条英语 生成催课提醒记录
 *
 * @author zhuxuan
 */
@Named
@ScheduledJobDefinition(
        jobName = "薯条英语 生成催课提醒记录",
        jobDescription = "每天 0 点执行",
        disabled = {Mode.STAGING},
        cronExpression = "0 0 0 * * ?"
)
@ProgressTotalWork(100)
public class AutoGenChipsRemindRecord extends ScheduledJobWithJournalSupport {

    @ImportService(interfaceClass = ChipsActiveService.class)
    private ChipsActiveService chipsActiveService;

    @ImportService(interfaceClass = ChipsEnglishClazzService.class)
    private ChipsEnglishClazzService chipsEnglishClazzService;

    @ImportService(interfaceClass = ChipsEnglishContentLoader.class)
    private ChipsEnglishContentLoader chipsEnglishContentLoader;
    @AlpsQueueProducer(queue = "utopia.chips.active.service.remind.message.send.queue")
    private MessageProducer producer;

    /**
     * 如果没有班级 跑所有的班级
     * 如果有班级 没有userIdCols 跑该班级下所有的
     * 如果有班级 且有userIdCols 这跑 班级下的用户和userIdCols 的交集部分
     *
     * @param jobJournalLogger
     * @param startTimestamp
     * @param parameters       可以不传，数据样例 {"unitDate":"2019-02-26","clazzId":4,"userIds":[0,1,2,3,4,5,6,7,8,9]}
     * @param progressMonitor
     * @throws Exception
     */
    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        Object clazzObj = parameters.get("clazzId");
        Object unitDateObj = parameters.get("unitDate");
        Object userIds = parameters.get("userIds");
        long clazzId = clazzObj == null ? 0l : Long.parseLong(clazzObj.toString());
        //如果没有传unitDate 则使用DayRange.current().previous().getStartDate()
        Date unitDate = unitDateObj == null ? DayRange.current().previous().getStartDate() : DateUtils.parseDate(unitDateObj.toString(), DateUtils.FORMAT_SQL_DATE);
        Collection<Long> userCol = null;
        if (userIds != null) {
            List<Object> temp = (List) userIds;
            userCol = temp.stream().map(e -> Long.valueOf(e.toString())).collect(Collectors.toSet());
        }
        autoGenChipsRemindRecord(clazzId, userCol, unitDate);
    }

    public void autoGenChipsRemindRecord(Long clazzId, Collection<Long> userIdCols, Date unitData) {
        if (clazzId == null || clazzId == 0l) {//执行所有班级对应的unitData的单元数据
            List<ChipsEnglishClass> clazzList = chipsEnglishClazzService.loadAllChipsEnglishClass();
            for (ChipsEnglishClass clazz : clazzList) {
                handleByClazz(clazz, null, unitData);
            }
        } else {
            ChipsEnglishClass clazz = chipsEnglishClazzService.selectChipsEnglishClassById(clazzId);
            if (clazz == null) {
                return;
            }
            handleByClazz(clazz, userIdCols, unitData);
        }
    }

    /**
     * 处理一个班级下的
     *
     * @param clazz
     * @param userIdCols
     * @param unitData
     */
    private void handleByClazz(ChipsEnglishClass clazz, Collection<Long> userIdCols, Date unitData) {
        String unitId = loadUnitId(clazz, unitData);
        if (StringUtils.isBlank(unitId)) {
            return;
        }

        List<ChipsEnglishClassUserRef> refs = chipsEnglishClazzService.selectChipsEnglishClassUserRefByClazzId(clazz.getId());
        if (CollectionUtils.isEmpty(refs)) {
            return;
        }
        List<Long> userIdList;
        if (CollectionUtils.isEmpty(userIdCols)) {//跑该班级下的所有用户对应的unitData的单元数据
            userIdList = refs.stream().map(ChipsEnglishClassUserRef::getUserId).collect(Collectors.toList());
        } else {//跑该班级下的所有用户和userIdCols的交集 对应的unitData的单元数据
            userIdList = refs.stream().map(ChipsEnglishClassUserRef::getUserId).filter(e -> userIdCols.contains(e)).collect(Collectors.toList());
        }
//        logger.info("AutoGenChipsRemindRecord handle clazz: " + clazz.getId() + " ; unitId : " + unitId + "; userIdList size : " + userIdList.size());
        sendQueue(clazz, userIdList, unitId);
    }

    private void sendQueue(ChipsEnglishClass clazz, Collection<Long> userIdCols, String unitId) {
        if (CollectionUtils.isEmpty(userIdCols)) {
            return;
        }
        int index = 1;
        for (Long userId : userIdCols) {
            Map<String, Object> message = new HashMap<>();
            message.put("clazzId", clazz.getId());
            message.put("userId", userId);
            message.put("productId", clazz.getProductId());
            message.put("unitId", unitId);
            producer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
            if (index % 100 == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            index++;
        }
    }

    private String loadUnitId(ChipsEnglishClass clazz, Date unitData) {
        ChipsEnglishProductTimetable timetable = chipsEnglishContentLoader.loadChipsEnglishProductTimetableById(clazz.getProductId());
        if (timetable == null) {
            return null;
        }
        // 在开始时间之前 或者 在结束时间之后，不生成
        if (timetable.getBeginDate().after(unitData) || timetable.getEndDate().before(unitData)) {
            return null;
        }
        // 单元id
        String unitId = timetable.getCourses().stream()
                .filter(course -> unitData.equals(course.getBeginDate()))
                .map(ChipsEnglishProductTimetable.Course::getUnitId)
                .findFirst()
                .orElse(null);
        return unitId;
    }
}
