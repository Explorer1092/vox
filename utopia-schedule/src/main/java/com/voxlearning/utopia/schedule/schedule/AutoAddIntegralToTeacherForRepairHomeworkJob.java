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

package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.PopupCategory.LOWER_RIGHT;

/**
 * @author XiaoPeng.Yang
 * @since 14-11-7
 */
@Named
@ScheduledJobDefinition(
        jobName = "每周学生补做作业自动给老师加园丁豆任务",
        jobDescription = "每周一0点30运行，运行上周的数据",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 30 0 ? * MON")
@ProgressTotalWork(100)
public class AutoAddIntegralToTeacherForRepairHomeworkJob extends ScheduledJobWithJournalSupport {

    @Inject private RaikouSDK raikouSDK;

    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;

    @ImportService(interfaceClass = UserIntegralService.class) private UserIntegralService userIntegralService;

    private UtopiaSql utopiaSqlHomework;

    private static final int PAGE_SIZE = 1000;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSqlHomework = utopiaSqlFactory.getUtopiaSql("homework");
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        // 每个学科每个学生按一个headcount统计
        String non_nbSql = "SELECT distinct Y.STUDENT_ID, Y.SUBJECT" +
                "         FROM" +
                "            VOX_STUDENT_HOMEWORK_ACCOMPLISHMENT Y" +
                "        WHERE" +
                "            Y.ACCOMPLISH_TIME > :accomplishTime" +
                "        AND Y.ACCOMPLISH_TIME < :accomplishEndTime" +
                "        AND Y.`REPAIR` = 1" +
                "        AND Y.DISABLED = 0 " +
                "        GROUP BY" +
                "            Y.STUDENT_ID, Y.SUBJECT";
        List<Map<String, Object>> subjectStudentIds = utopiaSqlHomework.withSql(non_nbSql)
                .useParams(
                        MiscUtils.m("accomplishTime", WeekRange.current().previous().getStartDate(),
                                "accomplishEndTime", WeekRange.current().previous().getEndDate()))
                .queryAll();

        logger.info("student count: {}", subjectStudentIds.size());

        progressMonitor.worked(10);

        Map<Long, Integer> teacherStudentCount = new HashMap<>();

        List<List<Map<String, Object>>> lists = CollectionUtils.splitList(subjectStudentIds, subjectStudentIds.size() > PAGE_SIZE ? subjectStudentIds.size() / PAGE_SIZE : 1);
        final int[] studentSize = {0};
        Iterator<List<Map<String, Object>>> iterator = lists.iterator();
        while (iterator.hasNext()) {
            List<Map<String, Object>> list = iterator.next();
            Set<Long> studentIds = list.stream().map(m -> SafeConverter.toLong(m.get("STUDENT_ID"))).collect(Collectors.toSet());

            // 读取学生所在分组
            Map<Long, List<GroupMapper>> studentGroupIds = groupLoaderClient.loadStudentGroups(studentIds, false);

            // 这里需要统计一个老师对应了多少个补做的学生
            // 首先根据学生所在分组，得到一个分组对应多少个补做学生，注意，这里需要区分学科
            Map<Long, Integer> groupStudentCount = new HashMap<>();
            list.forEach(m -> {
                long sid = SafeConverter.toLong(m.get("STUDENT_ID"));
                Subject subject = Subject.of(m.get("SUBJECT").toString());

                List<GroupMapper> gs = studentGroupIds.get(sid);
                if (gs != null) {// 学生是有可能退出分组的
                    gs.stream()
                            .filter(g -> g.getSubject() == subject)
                            .forEach(g -> {
                                Long gid = g.getId();
                                Integer c = groupStudentCount.get(gid);
                                if (c == null) {
                                    c = 0;
                                }
                                c += 1;
                                groupStudentCount.put(gid, c);
                            });
                }
            });

            // 读取所有分组老师
            Map<Long, List<GroupTeacherTuple>> groupTeacherRefs = raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient()
                    .groupByGroupIds(groupStudentCount.keySet());

            // 根据老师分组关系，得到老师对应多少补做学生
            groupTeacherRefs.forEach((g, gtrs) -> {
                gtrs.forEach(gtr -> {
                    Long tid = gtr.getTeacherId();
                    Integer c = teacherStudentCount.get(tid);
                    if (c == null) {
                        c = 0;
                    }

                    teacherStudentCount.put(tid, c + groupStudentCount.get(g));
                });
            });

            studentSize[0] += list.size();
            logger.info("Currently {} students handled and has {} teachers", studentSize[0], teacherStudentCount.size());

            iterator.remove();
        }

        progressMonitor.worked(10);

        logger.info("teacher count: {}", teacherStudentCount.size());

        if (MapUtils.isEmpty(teacherStudentCount)) {
            progressMonitor.done();
            return;
        }

        // 读取老师信息
        Set<Long> teacherIds = teacherStudentCount.keySet();
        // 分组查询
        List<List<Long>> sources = CollectionUtils.splitList(teacherIds.stream().collect(Collectors.toList()), 100);
        Map<Long, User> teachers = new HashMap<>();
        for (List<Long> subList : sources) {
            Map<Long, User> subTeacherMap = userLoaderClient.loadUsers(subList);
            teachers.putAll(subTeacherMap);
        }

        progressMonitor.worked(10);

        ISimpleProgressMonitor monitor = progressMonitor.subTask(70, teacherStudentCount.size());
        //循环添加积分
        for (Map.Entry<Long, Integer> e : teacherStudentCount.entrySet()) {
            Long teacherId = e.getKey();
            Integer studentCount = e.getValue();
            User teacher = teachers.get(teacherId);
            if (teacher == null) {
                continue;
            }
            try {
                int integral = new BigDecimal(studentCount).divide(new BigDecimal(2), 0, RoundingMode.UP).multiply(new BigDecimal(10)).intValue();
                IntegralHistory integralHistory = new IntegralHistory(teacherId, IntegralType.学生补做作业奖励, integral);
                integralHistory.setComment("每周学生补做作业奖励");
                if (!userIntegralService.changeIntegral(integralHistory).isSuccess()) {
                    throw new RuntimeException();
                }
                //右下角弹窗
                String comment = "尊敬的" + teacher.fetchRealname() + "老师：" +
                        "上周共有" + studentCount + "名学生补做作业，您获得" + integral / 10 + "个园丁豆（每两个学生补做，可得1个园丁豆，每个学生补做多个作业只计算一次）。" +
                        "<a href='/teacher/center/mygold.vpage'>查看详情</a>";
                userPopupServiceClient.createPopup(teacherId)
                        .content(comment)
                        .type(PopupType.DEFAULT_AD)
                        .category(LOWER_RIGHT)
                        .create();
            } catch (Exception ex) {
                jobJournalLogger.log("加园丁豆失败，异常：{}， 老师ID{}", ex.getMessage(), teacherId);
            } finally {
                monitor.worked(1);
            }
        }
        progressMonitor.done();
        jobJournalLogger.log("执行完毕，共执行{}个老师", teacherStudentCount.size());
    }
}
