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

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.router.SyncMongoClientRouterBuilder;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.consumer.VacationHomeworkLoaderClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.*;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author shiwei.liao
 * @since 2017-1-6
 */
@Named
@ScheduledJobDefinition(
        jobName = "固定日期推送寒假作业周报告",
        jobDescription = "根据指定表里的group定时发送周报告",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.TEST, Mode.STAGING},
        cronExpression = "0 0 8 * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoSendVacationMessageJob extends ScheduledJobWithJournalSupport {
    private static final Set<DayRange> jobDate = new HashSet<>();
    private static final Map<DayRange, String> contentMap = new HashMap<>();
    private static final String linkUrl = "/view/mobile/activity/parent/vacation?packageId={}&sid={}";

    static {
        jobDate.add(DayRange.parse("20170120"));
        jobDate.add(DayRange.parse("20170206"));
        contentMap.put(DayRange.parse("20170120"), "家长好，寒假已经开始一段时间了，{}的{}寒假作业完成得怎么样了？点击查看完成情况。");
        contentMap.put(DayRange.parse("20170206"), "家长好，寒假即将结束，请监督{}在开学前完成{}寒假作业哦！点击查看完成情况。");
    }

    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    @Inject
    private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject
    private ParentLoaderClient parentLoaderClient;
    @Inject
    private AppMessageServiceClient appMessageServiceClient;
    @Inject
    private VacationHomeworkLoaderClient vacationHomeworkLoaderClient;


    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        String day = SafeConverter.toString(parameters.get("day"));
        DayRange current;
        if (StringUtils.isNotBlank(day)) {
            current = DayRange.parse(day);
        } else {
            current = DayRange.current();
        }
        if (!jobDate.contains(current)) {
            return;
        }
        String messagePattern = contentMap.get(current);

        //测试ID
        Long testGroupId = SafeConverter.toLong(parameters.get("groupId"));

        MongoDatabase mongoDatabase = SyncMongoClientRouterBuilder.getInstance()
                .getSyncMongoClientRouter("mongo")
                .getDatabase("vox-winter-vacation-2017");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("vacation_homework_package");
        int totalGroupCount;
        int totalGroupSendCount = 0;
        int totalPackageCount = 0;
        Bson projection = new BsonDocument()
                .append("clazzGroupId", new BsonBoolean(true))
                .append("_id", new BsonBoolean(true));
        Bson filter = new BsonDocument("disabled", new BsonBoolean(false));
        Set<Long> groupIds = new HashSet<>();
        Map<Long, String> groupVacationPackageId = new HashMap<>();
        //填了测试的groupId就不去查数据库了。
        if (testGroupId != 0) {
            GroupMapper groupMapper = groupLoaderClient.loadGroup(testGroupId, false);
            if (groupMapper == null || groupMapper.getSubject() == null) {
                return;
            }
            Map<Long, List<VacationHomeworkPackage.Location>> packageByClazzGroupIds = vacationHomeworkLoaderClient.loadVacationHomeworkPackageByClazzGroupIds(Collections.singleton(testGroupId));
            if (MapUtils.isEmpty(packageByClazzGroupIds)) {
                return;
            }
            VacationHomeworkPackage.Location location = packageByClazzGroupIds.get(testGroupId).stream()
                    .filter(v -> v.getSubject().equals(groupMapper.getSubject()))
                    .findFirst()
                    .orElse(null);
            if (location == null) {
                return;
            }
            groupIds.add(testGroupId);
            groupVacationPackageId.put(testGroupId, location.getId());
        } else if (!RuntimeMode.isProduction()) {
//            不是线上环境且不填groupId。直接跳出
            return;
        } else {
            FindIterable<Document> documents = mongoCollection.find().filter(filter).projection(projection);
            //先汇总查出来的groupId
            for (Document document : documents) {
                if (document == null) {
                    break;
                }
                Long groupId = SafeConverter.toLong(document.get("clazzGroupId"));
                if (groupId == 0) {
                    continue;
                }
                String packageId = SafeConverter.toString(document.get("_id"));
                if (StringUtils.isBlank(packageId)) {
                    continue;
                }
                totalPackageCount++;
                groupIds.add(groupId);
                groupVacationPackageId.put(groupId, packageId);
            }
        }
        //如果一个groupId都没查出来。直接就跳出。并且不查了。
        if (CollectionUtils.isEmpty(groupIds)) {
            return;
        }
        progressMonitor.worked(10);
        //group ID总数
        totalGroupCount = groupIds.size();
        ISimpleProgressMonitor monitor = progressMonitor.subTask(90, totalGroupCount);
        //遍历group发送消息
        for (Long groupId : groupIds) {
            monitor.worked(1);
            GroupMapper groupMapper = groupLoaderClient.loadGroup(groupId, true);
            if (groupMapper == null) {
                continue;
            }

            List<GroupMapper.GroupUser> students = groupMapper.getStudents();
            //没有学生
            if (CollectionUtils.isEmpty(students)) {
                continue;
            }
            //没有寒假作业
            String packageId = groupVacationPackageId.get(groupId);
            if (StringUtils.isBlank(packageId)) {
                continue;
            }
            Set<Long> studentIds = students.stream().map(GroupMapper.GroupUser::getId).collect(Collectors.toSet());
            //GroupUser转成map
            //这里为了避免班级里面有重复数据。所以这样转一次
            Map<Long, GroupMapper.GroupUser> studentMap = new HashMap<>();
            for (GroupMapper.GroupUser user : students) {
                if (!studentMap.containsKey(user.getId())) {
                    studentMap.put(user.getId(), user);
                }
            }
            for (Long studentId : studentIds) {
                List<StudentParent> parentList = parentLoaderClient.loadStudentParents(studentId);
                if (CollectionUtils.isEmpty(parentList)) {
                    continue;
                }
                GroupMapper.GroupUser student = studentMap.get(studentId);
                if (student == null) {
                    continue;
                }
                String link = StringUtils.formatMessage(linkUrl, packageId, studentId);
                String content = StringUtils.formatMessage(messagePattern, student.getName(), groupMapper.getSubject().getValue());
                //极光
                Map<String, Object> jpushExtInfo = new HashMap<>();
                jpushExtInfo.put("ext_tab_message_type", ParentAppJxtExtTabTypeToNative.USER_MESSAGE.getType());
                jpushExtInfo.put("studentId", studentId);
                jpushExtInfo.put("url", link);
                jpushExtInfo.put("tag", ParentMessageTag.报告.name());
                jpushExtInfo.put("shareType", ParentMessageShareType.NO_SHARE_VIEW.name());
                jpushExtInfo.put("shareContent", "");
                jpushExtInfo.put("shareUrl", "");
                jpushExtInfo.put("s", ParentAppPushType.REPORT.name());
                appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.PARENT, parentList.stream().map(StudentParent::getParentUser).map(User::getId).collect(Collectors.toList()), jpushExtInfo);
                for (StudentParent studentParent : parentList) {
                    //站内信
                    AppMessage appUserMessage = new AppMessage();
                    appUserMessage.setUserId(studentParent.getParentUser().getId());
                    appUserMessage.setMessageType(ParentMessageType.REMINDER.getType());
                    appUserMessage.setLinkType(1);
                    appUserMessage.setLinkUrl(link);
                    appUserMessage.setContent(content);
                    Map<String, Object> extInfo = new HashMap<>();
                    extInfo.put("tag", ParentMessageTag.报告.name());
                    appUserMessage.setExtInfo(extInfo);
                    messageCommandServiceClient.getMessageCommandService().createAppMessage(appUserMessage);
                }
            }
            totalGroupSendCount++;
        }
        progressMonitor.done();
        jobJournalLogger.log("send_week_report_info:" + MiscUtils.m("totalPackageCount", totalPackageCount, "totalGroupCount", totalGroupCount, "totalGroupSendCount", totalGroupSendCount));
    }
}
