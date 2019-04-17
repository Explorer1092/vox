package com.voxlearning.utopia.schedule.schedule.lightcourse;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.equator.service.configuration.client.GeneralConfigServiceClient;
import com.voxlearning.galaxy.service.groupon.api.GrouponLoader;
import com.voxlearning.galaxy.service.groupon.api.GrouponOrderLoader;
import com.voxlearning.galaxy.service.groupon.api.GrouponOrderService;
import com.voxlearning.galaxy.service.groupon.api.constant.GrouponGroupState;
import com.voxlearning.galaxy.service.groupon.api.constant.lightcourse.LightCourseActivity;
import com.voxlearning.galaxy.service.groupon.api.entity.Groupon;
import com.voxlearning.galaxy.service.groupon.api.entity.GrouponGroup;
import com.voxlearning.galaxy.service.groupon.api.entity.lightcourse.LightCourseGrouponOrderInfo;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import org.slf4j.Logger;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fugui.chang
 * @since 2019/3/8.
 * 需求:: http://wiki.17zuoye.net/pages/viewpage.action?pageId=45233182
 */
@Named
@ScheduledJobDefinition(
        jobName = "家长通全年级轻课拼团活动-撮合拼团",
        jobDescription = "每天11点和15点撮合拼团",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING},
        cronExpression = "0 0 11,15 * * ?"
)
@ProgressTotalWork(100)
public class ParentLightCourseMatchGroupJob extends ScheduledJobWithJournalSupport {
    private static final Logger logger = LoggerFactory.getLogger(ParentLightCourseMatchGroupJob.class);

    @ImportService(interfaceClass = GrouponOrderService.class)
    private GrouponOrderService grouponOrderService;
    @ImportService(interfaceClass = GrouponOrderLoader.class)
    private GrouponOrderLoader grouponOrderLoader;
    @ImportService(interfaceClass = GrouponLoader.class)
    private GrouponLoader grouponLoader;
    @Inject
    private GeneralConfigServiceClient generalConfigServiceClient;


    //一次在从库中读取的数据量
    static final private int pageSize = RuntimeMode.current().le(Mode.TEST) ? 10 : 1000;
    //可以进行匹配的剩余时间，测试环境假定是5分钟，线上环境是24小时
    static final private long leftTimeDelta = RuntimeMode.current().le(Mode.TEST) ? 5 * 60 * 1000 : 24 * 60 * 60 * 1000;


    //暂时单个线程处理，根据之后的情况在考虑多个线程并发处理
    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        String grouponId = LightCourseActivity.ALL_GRADE_LIGHT_COURSE.getCourseConfig().fetchGrouponId();
        Groupon groupon = grouponLoader.loadGroupon(grouponId);
        if (null == groupon) {
            logger.error("失败::全年级轻课撮合拼团,不能定位到拼团配置,grouponId=" + grouponId);
            return;
        }

        //pv次数限制，staging这样设置的原因是为了staging环境过滤出达到运行条件的数据(不过staging环境不允许更新数据)
        int pvCount = 0;
        if (!RuntimeMode.isStaging()) {
            //从配置中读取设置
            String pvCountStr = generalConfigServiceClient.loadConfigValueFromClientBuffer("all_grade_light_course_pvcount");
            if (StringUtils.isNotBlank(pvCountStr) && StringUtils.isNumeric(pvCountStr)) {
                pvCount = SafeConverter.toInt(pvCountStr);
            } else {
                logger.error("ParentLightCourseMatchGroupJob pvCount 全年级轻课撮合拼团,配置有误");
                return;
            }
        }


        Long totalSize = grouponOrderLoader.findLightCourseGrouponOrderInfoSize();
        if (totalSize == null || totalSize == 0) {
            return;
        }


        //进度条设置
        progressMonitor.worked(4);
        ISimpleProgressMonitor monitor = progressMonitor.subTask(96, totalSize.intValue());

        Set<String> ids = new HashSet<>();
        int actualProcessSize = 0;
        List<LightCourseGrouponOrderInfo> leftList = new ArrayList<>();

        String lastId = "";//一次在从库中读取的数据中，id排序最大的那个
        while (true) {
            List<LightCourseGrouponOrderInfo> lightCourseGrouponOrderInfos = grouponOrderLoader.findFromSecondary(lastId, pageSize);
            if (CollectionUtils.isEmpty(lightCourseGrouponOrderInfos)) {
                break;
            }

            //lastId重置
            lightCourseGrouponOrderInfos = lightCourseGrouponOrderInfos.stream().sorted(Comparator.comparing(LightCourseGrouponOrderInfo::getId)).collect(Collectors.toList());
            lastId = lightCourseGrouponOrderInfos.get(lightCourseGrouponOrderInfos.size() - 1).getId();
            ids.addAll(lightCourseGrouponOrderInfos.stream().map(LightCourseGrouponOrderInfo::getId).collect(Collectors.toSet()));


            //处理匹配
            int batchSize = processBatchMatch(groupon, lightCourseGrouponOrderInfos, leftList, pvCount);
            actualProcessSize = actualProcessSize + batchSize;
            monitor.worked(pageSize);
        }

        if (!leftList.isEmpty()) {
            int batchSize = processBatchMatch(groupon, leftList, null, pvCount);
            actualProcessSize = actualProcessSize + batchSize;
        }

        logger.info("全年级轻课撮合成团运行结束,totalSize={}, secondarySize={}, actualProcessSize={}", totalSize, ids.size(), actualProcessSize);
    }

    private int processBatchMatch(Groupon groupon, List<LightCourseGrouponOrderInfo> lightCourseGrouponOrderInfos, List<LightCourseGrouponOrderInfo> leftList, int pvCount) {
        //获取已支付且未退款且未开课的团组订单信息
        List<LightCourseGrouponOrderInfo> grouponOrderInfoList = lightCourseGrouponOrderInfos.stream()
                .filter(t -> t.getPayTime() != null && t.getPayTime() > 0)//已经付款的
                .filter(t -> t.getOpenCourse() == null || !t.getOpenCourse())//没有开课
                .filter(t -> t.getRefund() == null || !t.getRefund())//没有退款的
                .collect(Collectors.toList());
        if (grouponOrderInfoList.isEmpty()) {
            return 0;
        }

        //根据团组的被浏览次数过滤
        List<String> groupIds = grouponOrderInfoList.stream().map(LightCourseGrouponOrderInfo::getGroupId).collect(Collectors.toList());
        Map<String, Integer> pvMap = grouponOrderLoader.loadGroupsPv(groupIds);
        grouponOrderInfoList = grouponOrderInfoList.stream()
                .filter(t -> pvMap.getOrDefault(t.getGroupId(), 0) >= pvCount)
                .collect(Collectors.toList());
        if (grouponOrderInfoList.isEmpty()) {
            return 0;
        }

        //根据进行中状态以及剩余时间进行过滤
        long now = System.currentTimeMillis();
        groupIds = grouponOrderInfoList.stream().map(LightCourseGrouponOrderInfo::getGroupId).collect(Collectors.toList());
        Map<String, GrouponGroup> grouponGroupMap = grouponLoader.loadGrouponGroups(groupIds);
        grouponOrderInfoList = grouponOrderInfoList.stream()
                .filter(t -> grouponGroupMap.containsKey(t.getGroupId()))
                .filter(t -> grouponGroupMap.get(t.getGroupId()).getState() == GrouponGroupState.NORMAL)
                .filter(t -> {
                    long endTime = LightCourseActivity.parseGroupEndTime(grouponGroupMap.get(t.getGroupId()), groupon);
                    return endTime > now && endTime - now <= leftTimeDelta; //进行中且剩余时间在leftTimeDelta内
                })
                .collect(Collectors.toList());
        if (grouponOrderInfoList.isEmpty()) {
            return 0;
        }


        //撮合拼团
        int orderInfoSize = grouponOrderInfoList.size();
        for (int index = 0; index < orderInfoSize; index = index + 2) {
            if (index + 1 < orderInfoSize) {
                LightCourseGrouponOrderInfo aGruponOrderInfo = grouponOrderInfoList.get(index);
                LightCourseGrouponOrderInfo bGruponOrderInfo = grouponOrderInfoList.get(index + 1);
                //避免staging环境更新数据，手工可以触发job，原因staging运行会造成线上的缓存和数据库数据不一致
                if (!RuntimeMode.isStaging()) {
                    MapMessage mapMessage = grouponOrderService.matchGroup(aGruponOrderInfo.getId(), bGruponOrderInfo.getId());
                    if (!mapMessage.isSuccess()) {
                        logger.error("全年级轻课撮合拼团记录一条失败，ids=[{},{}];groupIds=[{},{}]", aGruponOrderInfo.getId(), bGruponOrderInfo.getId(), aGruponOrderInfo.getGroupId(), bGruponOrderInfo.getGroupId());
                    }
                }

                //调试数据使用
                if (RuntimeMode.current().le(Mode.STAGING)) {
                    logger.info("全年级轻课撮合成团,调试统计的数据,aUserId={},aOrderInfoId={},aOrderFixId={},aOldGroupId={}; bUserId={},bOrderInfoId={},bOrderFixId={},bOldGroupId={}; ",
                            aGruponOrderInfo.fetchUserId(), aGruponOrderInfo.getId(), aGruponOrderInfo.getOrderFixId(), aGruponOrderInfo.getGroupId(),
                            bGruponOrderInfo.fetchUserId(), bGruponOrderInfo.getId(), bGruponOrderInfo.getOrderFixId(), bGruponOrderInfo.getGroupId());
                }
            }
        }

        if (orderInfoSize % 2 == 1 && leftList != null) {
            leftList.add(grouponOrderInfoList.get(orderInfoSize - 1));
            return grouponOrderInfoList.size() - 1;
        }

        return grouponOrderInfoList.size();
    }


}
