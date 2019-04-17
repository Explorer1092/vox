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
import com.voxlearning.alps.api.concurrent.AlpsFutureBuilder;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.StudentType;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.clazz.client.AsyncTinyGroupServiceClient;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.user.api.constants.LatestType;
import com.voxlearning.utopia.service.user.api.constants.UserTagType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.latest.Latest_TinyGroupStar;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserTagLoaderClient;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;

/**
 * @author RuiBao
 * @since 11/13/2015
 */
@Named
@ScheduledJobDefinition(
        jobName = "每周小组执行任务",
        jobDescription = "每周一运行一次，评选小组之星，必须在小组长轮组任务之前",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 30 1 ? * MON")
@ProgressTotalWork(100)
public class AutoTinyGroupStarJob extends ScheduledJobWithJournalSupport {

    @Inject private AsyncTinyGroupServiceClient asyncTinyGroupServiceClient;
    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;

    @Inject private EmailServiceClient emailServiceClient;

    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserServiceClient userServiceClient;
    @Inject private UserTagLoaderClient userTagLoaderClient;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;

    @Inject private RaikouSDK raikouSDK;

    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        Date date = WeekRange.current().previous().getStartDate();
        if (parameters.containsKey("date")) {
            date = WeekRange.newInstance(DateUtils.stringToDate(SafeConverter.toString(parameters.get("date"))).getTime()).getStartDate();
        }
        String str = DateUtils.dateToString(date, FORMAT_SQL_DATE);

        // 获取数据
        List<List<SourceData>> sources = getSources();
        if (CollectionUtils.isEmpty(sources)) {
            return;
        }
        int total = sources.stream().mapToInt(List::size).sum();
        if (total == 0) {
            return;
        }
        progressMonitor.worked(20);

        ISimpleProgressMonitor monitor = progressMonitor.subTask(80, total);
        int threadCount = sources.size();
        logger.info("SPLIT SOURCE DATA INTO {} THREADS", threadCount);

        final CountDownLatch latch = new CountDownLatch(threadCount);
        for (List<SourceData> source : sources) {
            AlpsThreadPool.getInstance().submit(() -> {
                try {
                    handleTinyGroup(source, str, monitor);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await(1, TimeUnit.HOURS);
        } catch (InterruptedException ignored) {
            logger.warn(ignored.getMessage(), ignored);
        }
        progressMonitor.done();
    }

    private void handleTinyGroup(List<SourceData> sources, String date, ISimpleProgressMonitor monitor) {
        Set<Long> tids = sources.stream().map(SourceData::getTeacherId).collect(Collectors.toSet());
        Map<Long, User> teachers = userLoaderClient.loadUsers(tids);
        for (SourceData source : sources) {
            try {
                User teacher = teachers.get(source.getTeacherId());
                if (teacher == null) continue;
                List<TinyGroupData> datas = source.getTinyGroups();
                if (CollectionUtils.isEmpty(datas)) continue;

                List<Map<String, Object>> detail = new ArrayList<>();
                Map<Long, List<TinyGroupData>> groups = datas.stream().collect(Collectors.groupingBy(TinyGroupData::getGroupId));
                Map<Long, GroupMapper> groupMap = groupLoaderClient.loadGroups(groups.keySet(), false);
                Map<Long, Clazz> clazzs = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazzs(groupMap.values().stream().map(GroupMapper::getClazzId).collect(Collectors.toSet()))
                        .stream()
                        .collect(Collectors.toMap(Clazz::getId, Function.identity()));

                for (Long groupId : groups.keySet()) {
                    GroupMapper group = groupMap.get(groupId);
                    if (group == null) continue;
                    Clazz clazz = clazzs.get(group.getClazzId());
                    if (clazz == null) continue;
                    List<TinyGroupData> list = groups.get(groupId); // teacher的groupId组里的所有小组
                    if (CollectionUtils.isEmpty(list)) continue;

                    Set<Long> tinyGroupIds = list.stream().map(TinyGroupData::getTinyGroupId).collect(Collectors.toSet());
                    Map<Long, List<TinyGroupStudentRef>> ref_map = AlpsFutureBuilder.<Long, List<TinyGroupStudentRef>>newBuilder()
                            .ids(tinyGroupIds)
                            .generator(id -> asyncTinyGroupServiceClient.getAsyncTinyGroupService()
                                    .findTinyGroupStudentRefsByTinyGroupId(id))
                            .buildMap()
                            .regularize();
                    TinyGroupSortData best = getBest(list, ref_map, group.getSubject(), date);
                    if (best == null) continue;

                    Map<String, Object> map = new HashMap<>();
                    map.put("clazzId", clazz.getId());
                    map.put("clazzName", clazz.formalizeClazzName());
                    map.put("groupId", groupId);
                    map.put("rotate", list.stream().noneMatch(e -> e.getUserType() == 1));
                    map.put("tinyGroupId", best.getTinyGroupId());
                    TinyGroup tg = asyncTinyGroupServiceClient.getAsyncTinyGroupService()
                            .loadTinyGroup(best.getTinyGroupId())
                            .getUninterruptibly();
                    String tinyGroupName = tg == null ? "" : tg.getTinyGroupName();
                    List<TinyGroupStudentRef> refs = best.getRefs();
                    Set<Long> sids = refs.stream().map(TinyGroupStudentRef::getStudentId).collect(Collectors.toSet());
                    Map<Long, User> ss = userLoaderClient.loadUsers(sids);
                    List<Map<String, Object>> members = new ArrayList<>();
                    Long leader = 0L;
                    for (TinyGroupStudentRef ref : refs) {
                        User user = ss.get(ref.getStudentId());
                        if (user == null) continue;
                        Map<String, Object> stu = new HashMap<>();
                        stu.put("sid", user.getId());
                        stu.put("sname", user.fetchRealnameIfBlankId());
                        stu.put("simg", user.fetchImageUrl());
                        boolean isLeader = ref.getType() == StudentType.TINY_GROUP_LEADER;
                        stu.put("isLeader", isLeader);
                        if (isLeader) leader = ref.getStudentId();
                        if (StringUtils.isBlank(tinyGroupName) && isLeader) {
                            tinyGroupName = user.fetchRealnameIfBlankId() + "组";
                        }
                        members.add(stu);
                    }

                    String cache = best.getTinyGroupId() + "|" + JsonUtils.toJson(sids) + "|" + leader;
                    asyncUserCacheServiceClient.getAsyncUserCacheService()
                            .TinyGroupStarCacheManager_resetTinyGroupStar(groupId, cache)
                            .awaitUninterruptibly();

                    Collections.sort(members, ((o1, o2) -> {
                        boolean b1 = ConversionUtils.toBool(o1.get("isLeader"));
                        boolean b2 = ConversionUtils.toBool(o2.get("isLeader"));
                        return Boolean.compare(b2, b1);
                    }));
                    map.put("members", members);
                    map.put("tinyGroupName", StringUtils.isBlank(tinyGroupName) ? "未命名组" : tinyGroupName);
                    detail.add(map);
                }

                if (!detail.isEmpty()) {
                    Latest_TinyGroupStar latest = new Latest_TinyGroupStar();
                    latest.setUserId(teacher.getId());
                    latest.setUserName(teacher.fetchRealname());
                    latest.setUserImg(teacher.fetchImageUrl());
                    latest.setDetail(detail);
                    userServiceClient.createTeacherLatest(teacher.getId(), LatestType.TINY_GROUP_STAR).withDetail(latest).send();
                }
            } catch (Exception ex) {
                logger.error("TEACHER {} SEND TINY GROUP STAR LATEST ERROR", source.getTeacherId(), ex);
            } finally {
                monitor.worked(1);
            }
        }
    }

    private TinyGroupSortData getBest(List<TinyGroupData> list, Map<Long, List<TinyGroupStudentRef>> ref_map, Subject subject, String date) {
        return list.stream()
                .map(e -> transform(e, ref_map, subject, date))
                .filter(e -> e != null)
                .sorted((o1, o2) -> {
                    if (Objects.equals(o1.getHwCount(), o2.getHwCount())) {
                        return Long.compare(o2.getBindCount(), o1.getBindCount());
                    } else {
                        return Integer.compare(o2.getHwCount(), o1.getHwCount());
                    }
                })
                .findFirst()
                .orElse(null);
    }

    private TinyGroupSortData transform(TinyGroupData data, Map<Long, List<TinyGroupStudentRef>> ref_map, Subject subject, String date) {
        TinyGroupSortData sd = new TinyGroupSortData();
        sd.setTinyGroupId(data.getTinyGroupId());
        List<TinyGroupStudentRef> refs = ref_map.get(data.getTinyGroupId());
        if (CollectionUtils.isEmpty(refs)) return null;
        sd.setRefs(refs);
        Set<Long> sids = refs.stream().map(TinyGroupStudentRef::getStudentId).collect(Collectors.toSet());
        Map<String, Integer> info = asyncUserCacheServiceClient.getAsyncUserCacheService()
                .WeekFinishHAQCountCacheManager_count(sids, subject, date)
                .getUninterruptibly();
        int hwCount = info.get("hcount");
        if (RuntimeMode.ge(Mode.STAGING) && (hwCount < 5)) return null;
        sd.setHwCount(hwCount);
        sd.setBindCount(userTagLoaderClient.loadUserTags(sids).values()
                .stream()
                .filter(s -> {
                    UserTag.Tag tag = s.fetchTag(UserTagType.S_BIND_MOBILE_OR_HIS_P_BIND_MOBILE.name());
                    return tag != null && Boolean.TRUE.equals(ConversionUtils.toBool(tag.getValue()));
                })
                .count());
        return sd;
    }

    private List<List<SourceData>> getSources() {
        String sql = "SELECT t1.ID AS tgid, t1.GROUP_ID AS gid, t2.TEACHER_ID AS tid, t1.CREATOR_USER_TYPE AS ut " +
                "FROM VOX_TINY_GROUP t1 " +
                "INNER JOIN VOX_GROUP_TEACHER_REF t2 ON t1.GROUP_ID=t2.CLAZZ_GROUP_ID AND t2.DISABLED=0 " +
                "WHERE t1.DISABLED=0";

        List<TinyGroupData> result = utopiaSql.withSql(sql).queryAll((rs, i) -> {
            TinyGroupData data = new TinyGroupData();
            data.setTeacherId(rs.getLong("tid"));
            data.setGroupId(rs.getLong("gid"));
            data.setTinyGroupId(rs.getLong("tgid"));
            data.setUserType(rs.getInt("ut"));
            return data;
        });
        logger.info("TOTAL {} TINY GROUP FOUND: " + result.size());

        // 发个报警邮件
        if (RuntimeMode.isProduction() && result.size() > 1000000) {
            Map<String, Object> content = new HashMap<>();
            content.put("info", "小组数量超过百万，任务需要重写了~~");
            emailServiceClient.createTemplateEmail(EmailTemplate.office)
                    .to("rui.bao@17zuoye.com;xiaopeng.yang@17zuoye.com")
                    .subject("小组数量报警")
                    .content(content)
                    .send();
        }

        // 按照教师分组
        Map<Long, List<TinyGroupData>> teacherMap = result.stream().collect(Collectors.groupingBy(TinyGroupData::getTeacherId));
        logger.info("THERE ARE TOTAL {} TEACHER FOUND.", teacherMap.size());

        // 准备分片
        List<SourceData> sourceDataList = new ArrayList<>(teacherMap.size());
        for (Long teacherId : teacherMap.keySet()) {
            SourceData sd = new SourceData();
            sd.setTeacherId(teacherId);
            sd.setTinyGroups(teacherMap.get(teacherId));
            sourceDataList.add(sd);
        }

        return CollectionUtils.splitList(sourceDataList, 10);
    }

    @Data
    private static class SourceData {
        private Long teacherId;
        private List<TinyGroupData> tinyGroups;
    }

    @Data
    private static class TinyGroupData {
        Long tinyGroupId;
        Long groupId;
        Long teacherId;
        Integer userType;
    }

    @Data
    private static class TinyGroupSortData {
        Long tinyGroupId;
        Integer hwCount;
        Long bindCount;
        List<TinyGroupStudentRef> refs;
    }
}