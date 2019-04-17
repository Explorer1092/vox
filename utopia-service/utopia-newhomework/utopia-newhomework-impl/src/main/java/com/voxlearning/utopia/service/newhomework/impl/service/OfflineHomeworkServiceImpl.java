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
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.mapper.ReminderQueueCommand;
import com.voxlearning.utopia.mapper.ScoreCircleQueueCommand;
import com.voxlearning.utopia.service.config.consumer.BadWordCheckerClient;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.api.entity.NewClazzBookRef;
import com.voxlearning.utopia.service.content.consumer.NewClazzBookLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.OfflineHomeworkContentType;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.service.OfflineHomeworkService;
import com.voxlearning.utopia.service.newhomework.api.util.OfflineHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.dao.OfflineHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.OfflineHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.queue.NewHomeworkParentQueueProducer;
import com.voxlearning.utopia.service.newhomework.impl.queue.ReminderQueueNewHomeworkProducer;
import com.voxlearning.utopia.service.newhomework.impl.service.helper.HomeworkShareChannelHelper;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkVendorMessageSender;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.api.constant.HomeworkVendorMessageType;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/9/8
 */
@Named
@Service(interfaceClass = OfflineHomeworkService.class)
@ExposeService(interfaceClass = OfflineHomeworkService.class)
public class OfflineHomeworkServiceImpl extends SpringContainerSupport implements OfflineHomeworkService {

    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject
    private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject
    private OfflineHomeworkDao offlineHomeworkDao;
    @Inject
    private OfflineHomeworkLoaderImpl offlineHomeworkLoader;
    @Inject
    private NewHomeworkVendorMessageSender newHomeworkVendorMessageSender;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @Inject
    private NewClazzBookLoaderClient newClazzBookLoaderClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private ReminderQueueNewHomeworkProducer reminderQueueNewHomeworkProducer;
    @Inject
    private AppMessageServiceClient appMessageServiceClient;
    @Inject
    private NewHomeworkParentQueueProducer newHomeworkParentQueueProducer;
    @Inject
    protected BadWordCheckerClient badWordCheckerClient;
    @Inject
    private HomeworkShareChannelHelper homeworkShareChannelHelper;

    @Inject private RaikouSDK raikouSDK;

    @Override
    public MapMessage loadIndexData(Teacher teacher, List<String> homeworkIds, List<Long> clazzGroupIds) {
        String bookId = null;
        String unitId = null;
        Date endTime = null;
        List<Map<String, Object>> homeworkContent = new LinkedList<>();
        Subject subject = teacher.getSubject();
        if (CollectionUtils.isNotEmpty(homeworkIds)) {
            // 通过作业发作业单，取作业的最后一个单元(如果是多个作业，取第一个作业的信息)
            Map<String, NewHomework> newHomeworkMap = newHomeworkLoader.loadNewHomeworks(homeworkIds);
            if (MapUtils.isNotEmpty(newHomeworkMap)) {
                NewHomework newHomework = newHomeworkMap.values().iterator().next();
                subject = newHomework.getSubject();
                endTime = newHomework.getEndTime();
                String homeworkId = newHomework.getId();
                NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(homeworkId);
                if (newHomeworkBook == null || MapUtils.isEmpty(newHomeworkBook.getPractices())) {
                    return MapMessage.errorMessage("作业数据错误");
                } else {
                    List<NewHomeworkBookInfo> bookInfos = newHomeworkBook.getPractices()
                            .values()
                            .stream()
                            .filter(CollectionUtils::isNotEmpty)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                    for (NewHomeworkBookInfo bookInfo : bookInfos) {
                        if (StringUtils.isNotEmpty(bookInfo.getBookId())) {
                            bookId = bookInfo.getBookId();
                            if (StringUtils.isNotEmpty(bookInfo.getUnitId())) {
                                unitId = bookInfo.getUnitId();
                            }
                        }
                    }
                }

                if (newHomework.getPractices() == null) {
                    return MapMessage.errorMessage("作业不存在");
                }
                for (NewHomeworkPracticeContent content : newHomework.getPractices()) {
                    if (Objects.equals(content.getType(), ObjectiveConfigType.FALLIBILITY_QUESTION)) {
                        continue;
                    }
                    // 纸质口算没有题目，写死一道
                    if (ObjectiveConfigType.OCR_MENTAL_ARITHMETIC == content.getType()) {
                        homeworkContent.add(MapUtils.m(
                                "type", content.getType(),
                                "typeName", content.getType().getValue(),
                                "count", 1
                        ));
                        continue;
                    }
                    int questionCnt;
                    if (content.getApps() != null) {
                        questionCnt = content.getApps().size();
                    } else {
                        questionCnt = content.processNewHomeworkQuestion(false).size();
                    }
                    homeworkContent.add(MapUtils.m(
                            "type", content.getType(),
                            "typeName", content.getType().getValue(),
                            "count", questionCnt
                    ));
                }
                //高频错题布置班级数
                int fallibilityQuestionClazzNum = 0;
                for (NewHomework n : newHomeworkMap.values()) {
                    if (n.getPractices() != null) {
                        for (NewHomeworkPracticeContent content : n.getPractices()) {
                            if (Objects.equals(content.getType(), ObjectiveConfigType.FALLIBILITY_QUESTION)) {
                                fallibilityQuestionClazzNum++;
                                break;
                            }
                        }
                    }
                }
                if (fallibilityQuestionClazzNum > 0) {
                    homeworkContent.add(MapUtils.m(
                            "type", ObjectiveConfigType.FALLIBILITY_QUESTION,
                            "typeName", ObjectiveConfigType.FALLIBILITY_QUESTION.getValue(),
                            "count", fallibilityQuestionClazzNum
                    ));
                }

            }
            if (StringUtils.isBlank(bookId)) {
                return MapMessage.errorMessage("作业数据错误");
            }
        } else {
            if (CollectionUtils.isEmpty(clazzGroupIds)) {
                return MapMessage.errorMessage("作业id、班组id不能同时为空");
            }
            // 班群通知发作业单，默认教材和单元取上次用过的单元
            // 所有班级的最新教材
            NewClazzBookRef newClazzBookRef = newClazzBookLoaderClient.loadGroupBookRefs(clazzGroupIds)
                    .subject(teacher.getSubject())
                    .toList()
                    .stream()
                    .sorted((o1, o2) -> Long.compare(o2.fetchUpdateTimestamp(), o1.fetchUpdateTimestamp()))
                    .findFirst()
                    .orElse(null);
            if (newClazzBookRef != null) {
                bookId = newClazzBookRef.getBookId();
                unitId = newClazzBookRef.getUnitId();
            }
            // 找不到班级已使用教材,推送默认教材
            // 推默认教材，按第一个班级推
            if (StringUtils.isBlank(bookId)) {
                Long groupId = clazzGroupIds.iterator().next();
                Group group = raikouSDK.getClazzClient()
                        .getGroupLoaderClient()
                        ._loadGroup(groupId)
                        .firstOrNull();
                if (group != null) {
                    Long clazzId = group.getClazzId();
                    Clazz clazz = raikouSDK.getClazzClient()
                            .getClazzLoaderClient()
                            ._loadClazz(clazzId)
                            .firstOrNull();
                    if (clazz != null) {
                        School school = schoolLoaderClient.getSchoolLoader()
                                .loadSchool(clazz.getSchoolId())
                                .getUninterruptibly();
                        if (school != null) {
                            bookId = newContentLoaderClient.initializeClazzBook(teacher.getSubject(), clazz.getClazzLevel(), school.getRegionCode());
                        }
                    }
                }
            }
            if (StringUtils.isBlank(bookId)) {
                return MapMessage.errorMessage("未找到合适的教材");
            }
        }
        NewBookProfile newBookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        if (newBookProfile == null) {
            return MapMessage.errorMessage("教材信息错误");
        }
        MapMessage mapMessage = MapMessage.successMessage();
        List<Map<String, Object>> unitMappers = Collections.emptyList();
        List<NewBookCatalog> unitList = newContentLoaderClient.loadChildren(Collections.singleton(bookId), BookCatalogType.UNIT).get(bookId);
        if (CollectionUtils.isNotEmpty(unitList)) {
            NewBookCatalog defaultUnit = MiscUtils.firstElement(unitList);
            if (StringUtils.isNotBlank(unitId)) {
                for (NewBookCatalog unit : unitList) {
                    if (Objects.equals(unitId, unit.getId())) {
                        defaultUnit = unit;
                        break;
                    }
                }
            }
            String defaultUnitId = defaultUnit.getId();
            unitMappers = unitList.stream()
                    .map(unit -> MapUtils.m(
                            "unitId", unit.getId(),
                            "unitName", unit.getAlias(),
                            "defaultUnit", Objects.equals(defaultUnitId, unit.getId()))
                    )
                    .collect(Collectors.toList());
        }
        mapMessage.add("bookId", bookId);
        mapMessage.add("bookName", newBookProfile.getName());
        mapMessage.add("units", unitMappers);
        mapMessage.add("homeworkContent", homeworkContent);
        mapMessage.add("contentTypes", OfflineHomeworkContentType.getSubjectTypes(subject)
                .stream()
                .map(type -> MapUtils.m("type", type, "typeName", type.getDescription()))
                .collect(Collectors.toList())
        );
        if (endTime == null) {
            Date minEndTime = new Date(System.currentTimeMillis() + 300000);
            mapMessage.add("minEndTime", DateUtils.dateToString(minEndTime));
            endTime = DayRange.newInstance(minEndTime.getTime()).getEndDate();
        }
        mapMessage.add("endTime", DateUtils.dateToString(endTime));
        mapMessage.add("subject", subject);
        return mapMessage;
    }

    @Override
    public MapMessage assignOfflineHomework(Teacher teacher, Map<String, Object> homeworkJson, HomeworkSourceType homeworkSourceType) {
        try {
            return AtomicLockManager.instance().wrapAtomic(this)
                    .keys(teacher.getId(), teacher.getSubject().getId())
                    .proxy()
                    .internalAssignOfflineHomework(teacher, homeworkJson, homeworkSourceType);
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("作业单发布中，请不要重复布置!");
        } catch (Exception ex) {
            logger.error("failed to save offlineHomework, teacher id {}, homework_json {}", teacher.getId(), JsonUtils.toJson(homeworkJson), ex);
            return MapMessage.errorMessage("推荐失败");
        }
    }

    public MapMessage internalAssignOfflineHomework(Teacher teacher, Map<String, Object> homeworkJson, HomeworkSourceType homeworkSourceType) {
        if (homeworkSourceType == null || homeworkSourceType == HomeworkSourceType.UNKNOWN) {
            logger.error("homeworkSourceType is null");
            return MapMessage.errorMessage("作业来源为空").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_SOURCE_TYPE_IS_NULL);
        }
        long currentTime = new Date().getTime();
        Date endTime = SafeConverter.toDate(homeworkJson.get("endTime"));
        if (endTime == null || endTime.getTime() < currentTime) {
            return MapMessage.errorMessage("作业结束时间错误").setErrorCode(ErrorCodeConstants.ERROR_CODE_ENT_TIME);
        }
        List<String> homeworkIds = StringUtils.toList(SafeConverter.toString(homeworkJson.get("homeworkIds")), String.class);
        Map<Long, String> groupIdHomeworkIdMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(homeworkIds)) {
            // 通过作业发作业单
            Map<String, NewHomework> newHomeworkMap = newHomeworkLoader.loadNewHomeworks(homeworkIds);
            if (MapUtils.isEmpty(newHomeworkMap)) {
                return MapMessage.errorMessage("无效的作业id");
            }
            if (newHomeworkMap.values().stream().anyMatch(NewHomework::isHomeworkTerminated)) {
                return MapMessage.errorMessage("作业已检查或已超过截止时间，无法发送作业单");
            }
            Map<String, OfflineHomework> offlineHomeworkMap = offlineHomeworkLoader.loadByNewHomeworkIds(homeworkIds);
            if (MapUtils.isNotEmpty(offlineHomeworkMap)) {
                return MapMessage.errorMessage("已经发过作业单");
            }
            // 判断这些作业是否都属于这个老师
//            if (newHomeworkMap.values().stream().anyMatch(homework -> !Objects.equals(homework.getTeacherId(), teacher.getId()))) {
//                return MapMessage.errorMessage("无效的作业id");
//            }
            for (NewHomework newHomework : newHomeworkMap.values()) {
                if (groupIdHomeworkIdMap.containsKey(newHomework.getClazzGroupId())) {
                    return MapMessage.errorMessage("存在两份属于同一个组的作业");
                }
                groupIdHomeworkIdMap.put(newHomework.getClazzGroupId(), newHomework.getId());
            }
        } else {
            List<Long> clazzGroupIds = StringUtils.toLongList(SafeConverter.toString(homeworkJson.get("clazzGroupIds")));
            if (CollectionUtils.isEmpty(clazzGroupIds)) {
                return MapMessage.errorMessage("作业id、班组id不能同时为空");
            }
            // 班群通知发作业单
            for (Long groupId : clazzGroupIds) {
                groupIdHomeworkIdMap.put(groupId, null);
            }
        }
        // 班组权限校验
        Set<Long> teacherGroupIds = groupLoaderClient.loadTeacherGroups(teacher.getId(), false)
                .stream()
                .filter(groupTeacherMapper -> teacher.getSubject() == groupTeacherMapper.getSubject())
                .map(GroupTeacherMapper::getId)
                .collect(Collectors.toSet());
        if (groupIdHomeworkIdMap.keySet().stream().anyMatch(groupId -> !teacherGroupIds.contains(groupId))) {
            return MapMessage.errorMessage("没有班组操作权限");
        }
        String practiceJson = JsonUtils.toJson(homeworkJson.get("practices"));
        Map<String, Object> practiceMap = JsonUtils.fromJson(practiceJson);
        List<OfflineHomeworkPracticeContent> practiceContents = processPractice(practiceMap);
        // 通过家校发送的作业单必须选择线下作业内容
        if (CollectionUtils.isEmpty(practiceContents) && CollectionUtils.isEmpty(homeworkIds)) {
            return MapMessage.errorMessage("请选择线下作业内容");
        }
        if (CollectionUtils.isNotEmpty(practiceContents)) {
            // 过滤敏感词
            for (OfflineHomeworkPracticeContent practiceContent : practiceContents) {
                if (StringUtils.isNoneBlank(practiceContent.getCustomContent()) && badWordCheckerClient.containsConversationBadWord(practiceContent.getCustomContent())) {
                    return MapMessage.errorMessage("您可能输入了不合适内容，请修改提交~");
                }
            }
        }

        Date currentDate = new Date();
        String actionId = StringUtils.join(Arrays.asList(teacher.getId(), currentDate.getTime()), "_");
        boolean needSign = SafeConverter.toBoolean(homeworkJson.get("needSign"));
        List<OfflineHomework> offlineHomeworkList = new ArrayList<>();
        for (Map.Entry<Long, String> entry : groupIdHomeworkIdMap.entrySet()) {
            String id = "OH_" + RandomUtils.nextObjectId();
            OfflineHomework offlineHomework = new OfflineHomework();
            offlineHomework.setPractices(practiceContents);
            offlineHomework.setTeacherId(teacher.getId());
            offlineHomework.setEndTime(endTime);
            offlineHomework.setNeedSign(needSign);
            offlineHomework.setSubject(teacher.getSubject());
            offlineHomework.setSource(homeworkSourceType);
            offlineHomework.setActionId(actionId);
            offlineHomework.setNewHomeworkId(entry.getValue());
            offlineHomework.setClazzGroupId(entry.getKey());
            offlineHomework.setId(id);
            offlineHomeworkList.add(offlineHomework);
            offlineHomework.setTeacherName(teacher.fetchRealname());
        }

        offlineHomeworkDao.inserts(offlineHomeworkList);
        sendMessage(teacher, offlineHomeworkList);
        //分享渠道
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
        List<Integer> channels = homeworkShareChannelHelper.loadHomeworkShareChannel(teacherDetail);
        return MapMessage.successMessage().add("ohids", offlineHomeworkList.stream().map(OfflineHomework::getId).collect(Collectors.toList())).add("channels", channels);
    }

    private List<OfflineHomeworkPracticeContent> processPractice(Map<String, Object> practiceMap) {
        if (MapUtils.isEmpty(practiceMap)) {
            return Collections.emptyList();
        }
        List<OfflineHomeworkPracticeContent> practiceContents = new ArrayList<>();
        for (String key : practiceMap.keySet()) {
            Map<String, Object> practice = JsonUtils.fromJson(JsonUtils.toJson(practiceMap.get(key)));
            OfflineHomeworkContentType contentType = OfflineHomeworkContentType.of(key);
            if (contentType == null) {
                continue;
            }
            OfflineHomeworkPracticeContent practiceContent = new OfflineHomeworkPracticeContent();
            practiceContent.setType(contentType);
            if (contentType.isNeedPracticeCount()) {
                if (!practice.containsKey("practiceCount")) {
                    return null;
                }
                // 遍数范围1~10
                int practiceCount = SafeConverter.toInt(practice.get("practiceCount"));
                if (practiceCount < 1 || practiceCount > 10) {
                    return null;
                }
                practiceContent.setPracticeCount(practiceCount);
            }
            if (contentType.isNeedBookUnit()) {
                if (!practice.containsKey("bookId") || !practice.containsKey("unitId")) {
                    return null;
                }
                String bookId = SafeConverter.toString(practice.get("bookId"));
                String unitId = SafeConverter.toString(practice.get("unitId"));
                Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(Arrays.asList(bookId, unitId));
                if (newBookCatalogMap.get(bookId) == null || newBookCatalogMap.get(unitId) == null) {
                    return null;
                }
                practiceContent.setBookId(bookId);
                practiceContent.setBookName(newBookCatalogMap.get(bookId).getName());
                practiceContent.setUnitId(unitId);
                practiceContent.setUnitName(newBookCatalogMap.get(unitId).getAlias());
            }
            if (contentType.isNeedCustomContent()) {
                if (!practice.containsKey("customContent")) {
                    return null;
                }
                practiceContent.setCustomContent(SafeConverter.toString(practice.get("customContent")));
            }
            practiceContents.add(practiceContent);
        }
        return practiceContents;
    }

    private void sendMessage(Teacher teacher, List<OfflineHomework> offlineHomeworkList) {
        OfflineHomework firstOfflineHomework = offlineHomeworkList.iterator().next();
        String newHomeworkId = firstOfflineHomework.getNewHomeworkId();
        NewHomework newHomework = newHomeworkLoader.loadNewHomework(newHomeworkId);
        NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(newHomeworkId);

        // 生成通知
        Map<Long, String> groupOfflineHomeworkIdMap = offlineHomeworkList.stream().collect(Collectors.toMap(OfflineHomework::getClazzGroupId, OfflineHomework::getId));
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("endTime", firstOfflineHomework.getEndTime());
        extInfo.put("teacherId", teacher.getId());
        extInfo.put("groupOfflineHomeworkIdMap", groupOfflineHomeworkIdMap);
        extInfo.put("content", OfflineHomeworkUtils.buildMessageContent(firstOfflineHomework, newHomework, newHomeworkBook, "<br>"));
        extInfo.put("needFeedBack", firstOfflineHomework.getNeedSign());
        newHomeworkVendorMessageSender.sendMessageToVendor(HomeworkVendorMessageType.OFFLINE_HOMEWORK, extInfo);

        // 发送班群消息
        Map<Long, GroupMapper> groupMap = groupLoaderClient.loadGroups(offlineHomeworkList.stream().map(OfflineHomework::getClazzGroupId).collect(Collectors.toList()), false);
        //往班群底部菜单发一个通知
        groupMap.values().stream().filter(p -> p != null).forEach(groupMapper -> {
            //生成班群底部菜单的一条提醒
            ReminderQueueCommand command = new ReminderQueueCommand();
            command.setCommandType("INCR");
            command.setTarget("CLAZZ_GROUP");
            command.setTargetId(SafeConverter.toString(groupMapper.getId()));
            command.setGroupId(groupMapper.getId());
            command.setPosition("PARENT_APP_EASEMOB_BOTTOM_MENU_NOTIFY");
            Message message = Message.newMessage().writeObject(command);
            reminderQueueNewHomeworkProducer.getProducer().produce(message);
        });
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(groupMap.values().stream().map(GroupMapper::getClazzId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        String subjectName = teacher.getSubject().getValue();
        for (OfflineHomework offlineHomework : offlineHomeworkList) {
            String clazzName = "";
            GroupMapper groupMapper = groupMap.get(offlineHomework.getClazzGroupId());
            if (groupMapper != null && groupMapper.getClazzId() != null && clazzMap.get(groupMapper.getClazzId()) != null) {
                clazzName = clazzMap.get(groupMapper.getClazzId()).formalizeClazzName();
            }
            String content = OfflineHomeworkUtils.buildMessageContent(firstOfflineHomework, newHomework, newHomeworkBook);
            String offlineHomeworkMsgContent = "家长好，" + clazzName + "今天的" + subjectName + "作业如下\n" + content;

            Long teacherId = teacher.getId();
            //这里只是取发送人的ID
            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
            teacherId = mainTeacherId == null ? teacherId : mainTeacherId;
            //这里才是取所有的学科
            Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
            List<Subject> subjectList = teacherLoaderClient.loadTeachers(relTeacherIds).values().stream().map(Teacher::getSubject).collect(Collectors.toList());
            List<String> subjectStrList = subjectList.stream().sorted(Comparator.comparingInt(Subject::getId)).map(Subject::getValue).collect(Collectors.toList());
            String subjectsStr = "（" + StringUtils.join(subjectStrList.toArray(), "，") + "）";
            String em_push_title = teacher.fetchRealnameIfBlankId() + subjectsStr + "：" + offlineHomeworkMsgContent;


            //新的极光push
            Map<String, Object> jpushExtInfo = new HashMap<>();
            jpushExtInfo.put("studentId", "");
            jpushExtInfo.put("s", ParentAppPushType.OFFLINE_HOMEWORK.name());
            jpushExtInfo.put("url", "/view/offlinehomework/detail?needTitle=true&ohids=" + offlineHomework.getId());
            appMessageServiceClient.sendAppJpushMessageByTags(em_push_title,
                    AppMessageSource.PARENT,
                    Collections.singletonList(JpushUserTag.CLAZZ_GROUP_REFACTOR.generateTag(SafeConverter.toString(offlineHomework.getClazzGroupId()))),
                    null,
                    jpushExtInfo);


            //发往Parent Provider
            //线下作业为空的不发作业单的消息
            if (CollectionUtils.isNotEmpty(offlineHomework.getPractices())) {
                ScoreCircleQueueCommand circleQueueCommand = new ScoreCircleQueueCommand();
                circleQueueCommand.setGroupId(offlineHomework.getClazzGroupId());
                circleQueueCommand.setContent(OfflineHomeworkUtils.buildMessageContent(firstOfflineHomework, newHomework, newHomeworkBook, "<br>"));
                circleQueueCommand.setCreateDate(firstOfflineHomework.getCreateAt());
                circleQueueCommand.setGroupCircleType("OFFLINE_HOMEWORK");
                circleQueueCommand.setTypeId(offlineHomework.getId());
                circleQueueCommand.setLinkUrl("/view/offlinehomework/detail?needTitle=true&ohids=" + offlineHomework.getId());
                ScoreCircleQueueCommand.ExtInfo info = new ScoreCircleQueueCommand.ExtInfo();
                info.setContent("截止" + DateUtils.dateToString(firstOfflineHomework.getEndTime(), "MM月dd日 HH:mm"));
                info.setExtType("EXPIRE_DATE");
                circleQueueCommand.setExtInfoList(Collections.singletonList(info));
                newHomeworkParentQueueProducer.getProducer().produce(Message.newMessage().writeObject(circleQueueCommand));
            }
        }
    }
}
