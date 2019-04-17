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

package com.voxlearning.utopia.service.business.impl.service.teacher;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.api.concurrent.AlpsFutureBuilder;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.AmbassadorCompetitionScoreType;
import com.voxlearning.utopia.api.constant.SmartClazzRewardItem;
import com.voxlearning.utopia.api.constant.StudentType;
import com.voxlearning.utopia.api.legacy.MemcachedKeyConstants;
import com.voxlearning.utopia.business.api.entity.SmartClazzQuestionLib;
import com.voxlearning.utopia.business.api.entity.SmartClazzQuestionReport;
import com.voxlearning.utopia.business.api.mapper.SmartClazzStudentResult;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzIntegralHistory;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzIntegralPool;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzQuestionRef;
import com.voxlearning.utopia.mapper.SmartClazzHistoryMapper;
import com.voxlearning.utopia.mapper.SmartClazzRank;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.business.impl.dao.SmartClazzQuestionLibDao;
import com.voxlearning.utopia.service.business.impl.dao.SmartClazzQuestionRefPersistence;
import com.voxlearning.utopia.service.business.impl.dao.SmartClazzQuestionReportDao;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.clazz.client.AsyncTinyGroupServiceClient;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.clazz.client.SmartClazzServiceClient;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.ParentLoader;
import com.voxlearning.utopia.service.user.api.constants.ClazzIntegralType;
import com.voxlearning.utopia.service.user.api.constants.SystemRobot;
import com.voxlearning.utopia.service.user.api.constants.UserTagEventType;
import com.voxlearning.utopia.service.user.api.constants.UserTagType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.client.ZoneQueueServiceClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author Maofeng Lu
 * @since 14-6-27 下午3:39
 */
@Named
@Slf4j
public class TeacherSmartClazzServiceImpl extends BusinessServiceSpringBean {

    @Inject private AmbassadorServiceClient ambassadorServiceClient;
    @Inject private AsyncTinyGroupServiceClient asyncTinyGroupServiceClient;
    @Inject private ClazzIntegralServiceClient clazzIntegralServiceClient;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private SmartClazzServiceClient smartClazzServiceClient;
    @Inject private ZoneQueueServiceClient zoneQueueServiceClient;

    @Inject private SmartClazzQuestionLibDao smartClazzQuestionLibDao;
    @Inject private SmartClazzQuestionRefPersistence smartClazzQuestionRefPersistence;
    @Inject private SmartClazzQuestionReportDao smartClazzQuestionReportDao;
    @Inject private FlowerServiceClient flowerServiceClient;

    @Inject private RaikouSDK raikouSDK;

    public List<SmartClazzRank> findSmartClazzIntegralHistory(Long groupId, Date createDatetime) {
        if (groupId == null) {
            return Collections.emptyList();
        }

        List<SmartClazzIntegralHistory> clazzIntegralHistories = smartClazzServiceClient.getSmartClazzService()
                .findSmartClazzIntegralHistoryListByGroupId(groupId)
                .getUninterruptibly()
                .stream()
                .filter(p -> createDatetime == null || p.getCreateDatetime().after(createDatetime))
                .filter(p -> Objects.equals(p.getDisplay(), Boolean.TRUE))
                .collect(Collectors.toList());

        Map<Long, Integer> rewardIntegralMap = clazzIntegralHistories.stream()
                .collect(Collectors.groupingBy(p -> p.getUserId(), Collectors.summingInt(p -> p.getIntegral())));

        List<User> studentList = studentLoaderClient.loadGroupStudents(groupId);
        if (CollectionUtils.isEmpty(studentList)) {
            return Collections.emptyList();
        }

        Map<Long, String> tgid_name_map = new HashMap<>();
        Map<Long, Long> sid_tgid_map = new HashMap<>();
        fill(tgid_name_map, sid_tgid_map, groupId, studentList);

        List<SmartClazzRank> smartClazzRankList = new LinkedList<>();
        for (User user : studentList) {
            SmartClazzRank smartClazzRank = new SmartClazzRank();
            smartClazzRank.setStudentId(user.getId());
            smartClazzRank.setStudentName(user.fetchRealname());
            smartClazzRank.setStudentImg(user.fetchImageUrl());
            smartClazzRank.setIntegral(rewardIntegralMap.containsKey(user.getId()) ? rewardIntegralMap.get(user.getId()) : 0);
            smartClazzRank.setStudentWeight(10);
            smartClazzRankList.add(smartClazzRank);
            if (sid_tgid_map.containsKey(user.getId())) {
                Long tgid = sid_tgid_map.get(user.getId());
                smartClazzRank.setTinyGroupId(tgid);
                smartClazzRank.setTinyGroupName(StringUtils.defaultString(tgid_name_map.get(tgid)));
            }
        }
        return smartClazzRankList;
    }

    private void fill(Map<Long, String> tgid_name_map, Map<Long, Long> sid_tgid_map, Long groupId, List<User> students) {
        List<TinyGroup> tinyGroups = asyncTinyGroupServiceClient.getAsyncTinyGroupService()
                .findTinyGroupsByGroupId(groupId)
                .getUninterruptibly();
        if (CollectionUtils.isNotEmpty(tinyGroups)) {
            Map<Long, User> uid_u_map = students.stream().collect(Collectors.toMap(User::getId, Function.identity()));
            Map<Long, Long> tgid_lid_map = new HashMap<>();
            Set<Long> tgids = tinyGroups.stream().map(TinyGroup::getId).collect(Collectors.toSet());

            List<TinyGroupStudentRef> refList = AlpsFutureBuilder.<Long, List<TinyGroupStudentRef>>newBuilder()
                    .ids(tgids)
                    .generator(id -> asyncTinyGroupServiceClient.getAsyncTinyGroupService()
                            .findTinyGroupStudentRefsByTinyGroupId(id))
                    .buildMap()
                    .regularize()
                    .values()
                    .stream()
                    .filter(CollectionUtils::isNotEmpty)
                    .flatMap(List::stream)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            for (TinyGroupStudentRef ref : refList) {
                sid_tgid_map.put(ref.getStudentId(), ref.getTinyGroupId());
                if (ref.getType() == StudentType.TINY_GROUP_LEADER) {
                    tgid_lid_map.put(ref.getTinyGroupId(), ref.getStudentId());
                }
            }
            for (TinyGroup tinyGroup : tinyGroups) {
                String name = "未命名组";
                if (StringUtils.isNotBlank(tinyGroup.getTinyGroupName())) {
                    name = tinyGroup.getTinyGroupName();
                } else {
                    Long leaderId = tgid_lid_map.get(tinyGroup.getId());
                    if (leaderId == null) {
                        logger.error("TINYGROUP {} DOES NOT HAVE A LEADER", tinyGroup.getId());
                    } else {
                        User leader = uid_u_map.get(leaderId);
                        if (leader == null) {
                            logger.error("TINYGROUP {} DOES NOT HAVE A LEADER", tinyGroup.getId());
                        } else {
                            name = (StringUtils.isBlank(leader.fetchRealname()) ? leader.getId() : leader.fetchRealname()) + "组";
                        }
                    }
                }
                tgid_name_map.put(tinyGroup.getId(), name);
            }
        }
    }

    /**
     * 智慧教室奖励学生
     */
    public MapMessage rewardSmartClazzStudent(TeacherDetail teacherDetail, Clazz clazz, List<User> userList,
                                              int rewardIntegralCnt, SmartClazzRewardItem item,
                                              String customContent) {
        try {
            SmartClazzIntegralPool smartClazzIntegralPool;
            GroupMapper group = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacherDetail.getId(), clazz.getId(), false);
            if (group == null) {
                return MapMessage.errorMessage("组信息不存在");
            }
            smartClazzIntegralPool = clazzIntegralServiceClient.getClazzIntegralService()
                    .loadClazzIntegralPool(group.getId())
                    .getUninterruptibly();
            if (smartClazzIntegralPool == null) {
                return MapMessage.errorMessage("班级学豆池不能为空");
            }
            //奖励的总学豆
            int rewardTotalIntegral = userList.size() * rewardIntegralCnt;
            //班级中剩余学豆
            int totalIntegral = smartClazzIntegralPool.fetchTotalIntegral();
            int diffValue = totalIntegral - rewardTotalIntegral;
            if (diffValue < 0) {
                return MapMessage.errorMessage("学豆数量不足，您可以用园丁豆兑换成学豆继续给学生奖励哦");
            }
            List<IntegralHistory> integralHistoryList = new LinkedList<>();

            ClazzIntegralHistory history = new ClazzIntegralHistory();
            history.setGroupId(group.getId());
            history.setClazzIntegralType(ClazzIntegralType.智慧教室奖励学生.getType());
            history.setIntegral(-rewardTotalIntegral);
            history.setComment(ClazzIntegralType.智慧教室奖励学生.getDescription());
            if (clazzIntegralServiceClient.getClazzIntegralService().changeClazzIntegral(history).getUninterruptibly().isSuccess()) {
                List<Long> missUserList = new LinkedList<>();
                for (User user : userList) {
                    //给学生账户加学豆
                    IntegralHistory integralHistory = new IntegralHistory(user.getId(), IntegralType.智慧教室老师奖励学生, rewardIntegralCnt);
                    integralHistory.setAddIntegralUserId(teacherDetail.getId());
                    integralHistory.setComment(StringUtils.formatMessage("您在{}课堂中{}获得学豆", teacherDetail.getSubject().getValue(), (item == SmartClazzRewardItem.CUSTOM_TAG) ? customContent : item.getValue()));
                    integralHistoryList.add(integralHistory);
                    if (userIntegralService.changeIntegral(integralHistory).isSuccess()) {
                        String comment;
                        if (item == SmartClazzRewardItem.CUSTOM_TAG) {
                            comment = StringUtils.formatMessage(item.getDescription(), user.getProfile().getRealname(), customContent, rewardIntegralCnt);
                        } else {
                            comment = StringUtils.formatMessage(item.getDescription(), user.getProfile().getRealname(), rewardIntegralCnt);
                        }
                        //添加学生奖励记录
                        clazzIntegralService.saveSmartClazzIntegralHistory(user.getId(), teacherDetail.getId(), comment, rewardIntegralCnt, group.getId(), item);
                    } else {
                        missUserList.add(user.getId());
                    }
                }
                if (missUserList.size() > 0) {
                    logger.warn("智慧教室老师奖励学生，以下学生奖励失败,学生:{}", JsonUtils.toJson(missUserList));
                }
            } else {
                throw new RuntimeException(StringUtils.formatMessage("更新班级{}池失败", clazz.getId()));
            }

            //发送班级空间动态
            if (userList.size() == 1) {
                User user = userList.get(0);
//                X同学表现非常棒，得到X老师N个学豆的奖励！
//                X老师还有X学豆未奖，好好表现，让老师奖励你！
                String content = StringUtils.formatMessage(
                        "{}同学表现非常棒，得到{}老师{}个学豆的奖励！<br/>" +
                                "{}老师还有{}学豆未奖，好好表现，让老师奖励你！",
                        user.getProfile().getRealname(),
                        teacherDetail.getProfile().getRealname(),
                        rewardIntegralCnt,
                        teacherDetail.getProfile().getRealname(),
                        smartClazzIntegralPool.fetchTotalIntegral());
                zoneQueueServiceClient.createClazzJournal(clazz.getId())
                        .withUser(user.getId())
                        .withUser(user.fetchUserType())
                        .withClazzJournalType(ClazzJournalType.SMARTCLAZZ_STUDENT_REWARD)
                        .withClazzJournalCategory(ClazzJournalCategory.MISC)
                        .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content)))
                        .withGroup(group.getId())
                        .commit();
            } else if (userList.size() > 1) {
//                A、B、C、D等（最多显示4个）同学表现非常棒，每个人得到X老师N个学豆的奖励！
//                X老师还有X学豆未奖，好好表现，让老师奖励你！
                StringBuilder sb = new StringBuilder();
                int size = 0;
                for (User user : userList) {
                    if (size < 4) {
                        sb.append(user.getProfile().getRealname());
                        sb.append("、");
                        size++;
                    } else {
                        break;
                    }
                }
                sb.replace(sb.length() - 1, sb.length(), "等");
                String content = StringUtils.formatMessage(
                        "{}同学表现非常棒，每个人得到{}老师{}个学豆的奖励！<br/>" +
                                "{}老师还有{}学豆未奖，好好表现，让老师奖励你！",
                        sb.toString(),
                        teacherDetail.getProfile().getRealname(),
                        rewardIntegralCnt,
                        teacherDetail.getProfile().getRealname(),
                        smartClazzIntegralPool.fetchTotalIntegral());
                zoneQueueServiceClient.createClazzJournal(clazz.getId())
                        .withUser(SystemRobot.getInstance().getId())
                        .withUser(SystemRobot.getInstance().fetchUserType())
                        .withClazzJournalType(ClazzJournalType.SYSTEM_NOTICE)
                        .withClazzJournalCategory(ClazzJournalCategory.MISC)
                        .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content)))
                        .withGroup(group.getId())
                        .commit();
            }

            List<Long> userIds = userList.stream().map(User::getId).collect(Collectors.toList());
            AlpsThreadPool.getInstance().submit(() -> {
                //加活跃值
                batchUpdateTeacherLevelValue(integralHistoryList);
                // 发送app消息
                String notifyContent = "老师发奖励啦，快去看看";
                String link = "/studentMobile/center/teacherreward.vpage";
                Map<String, Object> extInfo = MiscUtils.m("link", link, "t", "h5", "key", "j");
                extInfo.put("s", StudentAppPushType.TEACHER_REWARD_REMIND.getType());
                appMessageServiceClient.sendAppJpushMessageByIds(notifyContent, AppMessageSource.STUDENT, userIds, extInfo);

                List<AppMessage> appUserMessageList = new ArrayList<>();
                for (User user : userList) {
                    if (user == null) {
                        continue;
                    }
                    //新消息中心用户消息
                    AppMessage message = new AppMessage();
                    message.setUserId(user.getId());
                    message.setMessageType(StudentAppPushType.TEACHER_REWARD_REMIND.getType());
                    message.setTitle(teacherDetail.fetchRealname() + "老师奖励" + user.fetchRealname());
                    message.setContent("因为" + user.fetchRealname() + "的表现," + teacherDetail.fetchRealname() + "奖励了" + rewardIntegralCnt + "个学豆");
                    message.setLinkUrl(link);
                    message.setLinkType(1);//站内的相对地址
                    appUserMessageList.add(message);
                }
                //新消息中心写入
                appUserMessageList.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);

                //点亮智慧课堂奖励学生
                ambassadorServiceClient.getAmbassadorService().recordAmbassadorMentor(teacherDetail.getId(), MiscUtils.map(UserTagType.AMBASSADOR_MENTOR_SMART_CLAZZ, UserTagEventType.AMBASSADOR_MENTOR_SMART_CLAZZ));
                // 发送家长消息
//                parentMessageServiceClient.smartClazzReward(teacherDetail.fetchRealname(), userList, parentLoaderClient);
                sendParentAppMessage(teacherDetail.fetchRealname(), userList, parentLoaderClient);
                // 发送课堂奖励家长可送鲜花
                userIds.forEach(sid -> {
                    flowerServiceClient.getFlowerConditionService().rewardStudent(sid, teacherDetail.getId(), group.getId());
                });
            });
        } catch (Exception ex) {
            log.error("Failed to update smart clazz {} integral,teacherId:{}, subject: {} ", clazz.getId(), teacherDetail.getId(), teacherDetail.getSubject());
            return MapMessage.errorMessage("奖励失败");
        }
        return MapMessage.successMessage("奖励成功");
    }

    private void batchUpdateTeacherLevelValue(List<IntegralHistory> integralHistorys) {
        if (CollectionUtils.isEmpty(integralHistorys)) {
            return;
        }
        // 包班制支持
        // 这里切换回主账号是因为这块有缓存次数限制
        Set<Long> teacherIds = integralHistorys.stream().map(IntegralHistory::getAddIntegralUserId).collect(Collectors.toSet());
        Map<Long, Long> mainTeacherIds = teacherLoaderClient.loadMainTeacherIds(teacherIds);
        for (IntegralHistory history : integralHistorys) {
            Long teacherId = history.getAddIntegralUserId();
            if (mainTeacherIds.get(teacherId) != null) {
                teacherId = mainTeacherIds.get(teacherId);
            }
            updateTeacherLevelValue(teacherId, history.getUserId());
        }
    }

    private void updateTeacherLevelValue(Long teacherId, Long userId) {
        String key = MemcachedKeyConstants.TEACHER_REWARD_STUDENT_BEANS_DAY_COUNT + teacherId; // getAddIntegralUserId 这里是老师ID

        CacheObject<Set<Long>> cacheObject = businessCacheSystem.CBS.unflushable.get(key);
        if (cacheObject == null) {
            return;
        }

        if (cacheObject.getValue() == null) {
            Set<Long> userIds = new HashSet<>();
            userIds.add(userId);
            businessCacheSystem.CBS.unflushable.add(key, DateUtils.getCurrentToDayEndSecond(), userIds);
            return;
        }

        Set<Long> temp = new LinkedHashSet<>(cacheObject.getValue());
        temp.add(userId);
        businessCacheSystem.CBS.unflushable.replace(key, DateUtils.getCurrentToDayEndSecond(), temp);
        if (temp.size() == 5) {//5个人的时候 加分 只加一次
            // 判断只加一次
            String oneKey = "ADD_SOME_SCORE_ONE_DAY_ONECE:" + teacherId;
            CacheObject<String> oneObject = businessCacheSystem.CBS.unflushable.get(oneKey);
            if (oneObject != null && StringUtils.isNotBlank(oneObject.getValue())) {
                // 加过了
                return;
            }
            // 预备大使添加努力值
            ambassadorServiceClient.getAmbassadorService().addCompetitionScore(teacherId, 0L, AmbassadorCompetitionScoreType.SMART_CLAZZ);
            // 正式大使添加积分
            ambassadorServiceClient.getAmbassadorService().addAmbassadorScore(teacherId, 0L, AmbassadorCompetitionScoreType.SMART_CLAZZ);
            // 记录加过了
            businessCacheSystem.CBS.unflushable.set(oneKey, DateUtils.getCurrentToDayEndSecond(), "Once");
        }
    }

    /**
     * 老师兑换学豆
     */
    @Deprecated
    public MapMessage saveSmartClazzExchangeIntegral(Teacher teacher, Long clazzId, int integralCnt) {
        return clazzIntegralService.saveSmartClazzExchangeIntegral(teacher, clazzId, integralCnt);
    }

    /**
     * 查询发放记录（只取最新50条数据）和学生学豆排行榜
     */
    public Map<String, Object> findSmartClazzRewardHistory(Long clazzId, Long teacherId, Subject subject, final Date startDate, final Date endDate) {
        if (clazzId == null || subject == null || startDate == null || endDate == null) {
            return Collections.emptyMap();
        }
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        List<SmartClazzIntegralHistory> historyList;
        if (clazz.isSystemClazz()) {
            GroupMapper group = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacherId, clazzId, false);
            historyList = smartClazzServiceClient.getSmartClazzService()
                    .findSmartClazzIntegralHistoryListByGroupId(group.getId())
                    .getUninterruptibly();
        } else {
            historyList = smartClazzServiceClient.getSmartClazzService()
                    .findSmartClazzIntegralHistoryListByClazzId(clazzId)
                    .getUninterruptibly()
                    .stream()
                    .filter(e -> e.getSubject() == subject)
                    .collect(Collectors.toList());
        }

        //时间过滤
        historyList = historyList.stream().filter(source ->
                source.getCreateDatetime().getTime() >= startDate.getTime() && source.getCreateDatetime().getTime() < endDate.getTime()
        ).collect(Collectors.toList());

        //按时间降序排列
        Collections.sort(historyList, new SmartClazzIntegralHistory.CREATETIME_DESC());
        Map<Long, Integer> studentRankMap = new LinkedHashMap<>();
        Map<Long, User> teacherMap = new LinkedHashMap<>();
        Map<Long, User> studentMap = new LinkedHashMap<>();
        List<SmartClazzHistoryMapper> historyMapperList = new LinkedList<>();
        int totalIntegral = 0;
        for (SmartClazzIntegralHistory history : historyList) {
            Long studentId = history.getUserId();
            if (studentId == null) continue;
            int integral = (history.getIntegral() == null ? 0 : history.getIntegral());
            if (!studentRankMap.containsKey(studentId)) {
                studentRankMap.put(studentId, 0);
            }
            //每个学生的学豆数
            int sumIntegral = studentRankMap.get(studentId);
            sumIntegral = sumIntegral + integral;
            studentRankMap.put(studentId, sumIntegral);
            if (!teacherMap.containsKey(history.getAddIntegralUserId())) {
                teacherMap.put(history.getAddIntegralUserId(), userLoaderClient.loadUser(history.getAddIntegralUserId()));
            }
            if (!studentMap.containsKey(studentId)) {
                studentMap.put(studentId, userLoaderClient.loadUser(studentId));
            }
            totalIntegral += integral;
            if (historyMapperList.size() <= 50) {
                SmartClazzHistoryMapper mapper = new SmartClazzHistoryMapper();
                User student = studentMap.get(studentId);
                mapper.setStudentId(studentId);
                mapper.setStudentName(student != null ? student.fetchRealname() : "");
                mapper.setStudentImg(student != null ? student.fetchImageUrl() : "");
                mapper.setComment(history.getComment());
                mapper.setCreateDate(history.getCreateDatetime());
                mapper.setAddIntegralUserId(history.getAddIntegralUserId());
                User teacher = teacherMap.get(history.getAddIntegralUserId());
                mapper.setAddIntegralUserName(teacher != null ? teacher.fetchRealname() : "");
                historyMapperList.add(mapper);
            }
        }

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //发放记录时,取前50条
        resultMap.put("rewardhistoryList", historyMapperList);
        //查询条件下总学豆数
        resultMap.put("totalIntegral", totalIntegral);
        //班级排行
        List<User> clazzStudentList = userAggregationLoaderClient.loadTeacherStudentsByClazzId(clazzId, teacherId);
//        List<User> clazzStudentList = studentLoaderClient.loadClazzStudents(clazzId);
        List<SmartClazzRank> smartClazzRankList = new LinkedList<>();
        //没有名字的学生跳过
        for (User user : clazzStudentList) {
            if (StringUtils.isBlank(user.fetchRealname())) {
                continue;
            }
            SmartClazzRank studentRank = new SmartClazzRank();
            studentRank.setStudentId(user.getId());
            studentRank.setStudentName(user.fetchRealname());
            studentRank.setStudentImg(user.fetchImageUrl());
            studentRank.setIntegral(studentRankMap.get(user.getId()) == null ? 0 : studentRankMap.get(user.getId()));
            smartClazzRankList.add(studentRank);
        }
        if (smartClazzRankList.size() > 0) {
            Collections.sort(smartClazzRankList, new Comparator<SmartClazzRank>() {
                @Override
                public int compare(SmartClazzRank o1, SmartClazzRank o2) {
                    Integer t1 = o1.getIntegral();
                    Integer t2 = o2.getIntegral();
                    return t2.compareTo(t1);
                }
            });
        }
        resultMap.put("smartClazzRankList", smartClazzRankList);
        return resultMap;
    }


    /**
     * 老师创建或编辑题目
     */
    public MapMessage saveSmartClazzQuestion(Teacher teacher, TeacherDetail teacherDetail, Map<String, Object> jsonMap) {
        if (teacher == null) {
            return MapMessage.errorMessage("老师信息为空");
        }
        if (teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {//老师未认证，不可创建题目, 培训学校老师可以
            return MapMessage.errorMessage("非认证用户，不可创建题目");
        }
        if (jsonMap == null || jsonMap.isEmpty()) {
            return MapMessage.errorMessage("所传参数为空");
        }
        final Long clazzId = ConversionUtils.toLong(jsonMap.get("clazzId"));
        GroupMapper group = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacher.getId(), clazzId, false);
        try {
            Validate.notNull(jsonMap.get("answer"), "未设置正确答案");
            Validate.notNull(jsonMap.get("topicContent"), "未填写题目题干");
            Validate.notNull(jsonMap.get("options"), "未设置选项");

            SmartClazzQuestionLib lib = new SmartClazzQuestionLib();
            lib.setCreator(teacher.getId());
            lib.setSubject(teacher.getSubject());
            lib.setTopicContent(ConversionUtils.toString(jsonMap.get("topicContent")));
            lib.setAnswer(ConversionUtils.toString(jsonMap.get("answer")));
            lib.setQuestionType(ConversionUtils.toString(jsonMap.get("questionType")));
            lib.setOptions((Map<String, String>) jsonMap.get("options"));
            String questionId = ConversionUtils.toString(jsonMap.get("id"));
            String newQuestionId;
            if (StringUtils.isNotBlank(questionId) && !StringUtils.equals("null", questionId)) {
                SmartClazzQuestionLib smartClazzQuestionLib = smartClazzQuestionLibDao.load(questionId);
                if (smartClazzQuestionLib == null) {
                    newQuestionId = smartClazzQuestionLibDao.insert(lib);
                } else {
                    newQuestionId = smartClazzQuestionLibDao.update(questionId, lib).getId();
                }
            } else {
                newQuestionId = smartClazzQuestionLibDao.insert(lib);
            }
            if (!StringUtils.equals(questionId, newQuestionId)) {
                SmartClazzQuestionRef ref = new SmartClazzQuestionRef();
                ref.setClazzId(clazzId);
                ref.setQuestionId(newQuestionId);
                ref.setSubject(teacher.getSubject());
                ref.setGroupId(group == null ? 0 : group.getId());
                smartClazzQuestionRefPersistence.persist(ref);
            }
        } catch (Exception ex) {
            log.error("Failed to save smartclazz {} question,teacherId:{}, subject: {},message:{} ", clazzId, teacher.getId(), teacher.getSubject(), ex.getMessage());
            return MapMessage.errorMessage("保存失败," + ex.getMessage());
        }
        return MapMessage.successMessage("保存成功");
    }

    /**
     * 查找智慧课堂的班级题目列表
     */
    @Deprecated
    public Page<SmartClazzQuestionLib> findSmartClazzQuestionPage(Long clazzId, Subject subject, Pageable pageable) {
        if (clazzId == null || subject == null) {
            return new PageImpl<>(Collections.emptyList());
        }

        Page<SmartClazzQuestionRef> refPage = smartClazzQuestionRefPersistence.pagingFindRefByClazzIdAndSubject(clazzId, subject, pageable);

        List<SmartClazzQuestionLib> lib = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(refPage.getContent())) {
            Set<String> questionSet = new LinkedHashSet<>();
            for (SmartClazzQuestionRef ref : refPage.getContent()) {
                CollectionUtils.addNonNullElement(questionSet, ref.getQuestionId());
            }

            lib = smartClazzQuestionLibDao.loads(questionSet)
                    .values()
                    .stream()
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(lib, pageable, refPage.getTotalElements());
    }

    public Page<SmartClazzQuestionLib> findSmartClazzQuestionPage(Long groupId, Pageable pageable) {
        if (groupId == null) {
            return new PageImpl<>(Collections.emptyList());
        }

        Page<SmartClazzQuestionRef> refPage = smartClazzQuestionRefPersistence.pagingFindRefByGroupId(groupId, pageable);

        List<SmartClazzQuestionLib> lib = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(refPage.getContent())) {
            Set<String> questionSet = new LinkedHashSet<>();
            for (SmartClazzQuestionRef ref : refPage.getContent()) {
                CollectionUtils.addNonNullElement(questionSet, ref.getQuestionId());
            }

            lib = smartClazzQuestionLibDao.loads(questionSet)
                    .values()
                    .stream()
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(lib, pageable, refPage.getTotalElements());
    }

    public List<SmartClazzQuestionLib> findSmartClazzQuestionById(Set<String> ids) {
        if (CollectionUtils.isEmpty(ids)) return Collections.emptyList();
        return smartClazzQuestionLibDao.loads(ids)
                .values()
                .stream()
                .collect(Collectors.toList());
    }

    public MapMessage addSmartClazzQuestionRef(Teacher teacher, final String questionId, List<Long> clazzIds) {
        if (StringUtils.isBlank(questionId)) {
            return MapMessage.errorMessage("question id must not be null");
        }
        if (CollectionUtils.isEmpty(clazzIds)) {
            return MapMessage.errorMessage("clazz list must not be null");
        }
        List<SmartClazzQuestionRef> refList = smartClazzQuestionRefPersistence.findSmartClazzQuestionRefByQid(questionId);
        Map<Long, SmartClazzQuestionRef> map = new LinkedHashMap<>();
        for (SmartClazzQuestionRef ref : refList) {
            map.put(ref.getClazzId(), ref);
        }
        try {
            final List<SmartClazzQuestionRef> saveList = new LinkedList<>();
            final List<SmartClazzQuestionRef> updateList = new LinkedList<>();

            Map<Long, GroupMapper> groups = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzIds(teacher.getId(), clazzIds, false);

            for (Long clazzId : clazzIds) {
                SmartClazzQuestionRef ref = map.get(clazzId);
                if (ref == null) {
                    ref = new SmartClazzQuestionRef();
                    ref.setClazzId(clazzId);
                    ref.setQuestionId(questionId);
                    ref.setDisabled(false);
                    ref.setSubject(teacher.getSubject());

                    if (groups.get(clazzId) != null) {
                        ref.setGroupId(groups.get(clazzId).getId());
                    } else {
                        logger.error("no group found for clazz {}, teacher {}", clazzId, teacher.getId());
                        ref.setGroupId(0L);
                    }

                    saveList.add(ref);
                } else {
                    ref.setDisabled(false);
                    updateList.add(ref);
                }
            }
            smartClazzQuestionRefPersistence.batchUpdateAndEvictCache(questionId, updateList);
            smartClazzQuestionRefPersistence.batchInsert(saveList);
            businessCacheSystem.CBS.flushable.delete(CacheKeyGenerator.generateCacheKey(SmartClazzQuestionRef.class, "questionId", questionId));
            return MapMessage.successMessage("保存成功");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MapMessage.errorMessage("保存失败," + e.getMessage());
        }
    }

    public List<SmartClazzQuestionRef> findSmartClazzQuestionRefByQId(String questionId) {
        if (StringUtils.isBlank(questionId)) return Collections.emptyList();
        return smartClazzQuestionRefPersistence.findSmartClazzQuestionRefByQid(questionId);
    }


    public MapMessage disabledSmartClazzQuestionRef(Long teacherId, Long clazzId, String questionId) {
        if (teacherId == null || clazzId == null || StringUtils.isBlank(questionId)) {
            return MapMessage.errorMessage("invalid param");
        }

        SmartClazzQuestionRef ref = smartClazzQuestionRefPersistence.findQuestionByClazzIdAndQuestionId(clazzId, questionId);
        if (ref == null) {
            log.warn("the update records you want are blank,clazzId: {} questionId:{}, ingore", clazzId, questionId);
            return MapMessage.successMessage("删除成功");
        }

        Long groupId = ref.getGroupId();
        List<Teacher> teachers;
        if (groupId != null) {
            // 判断老师是否有该组的管理权限
            teachers = teacherLoaderClient.loadGroupTeacher(groupId);
        } else {
            teachers = ClazzTeacher.toTeacherList(teacherLoaderClient.loadClazzTeachers(clazzId));
        }
        Set<Long> availableTeacherIds = teachers.stream()
                .filter(e -> e != null && e.getId() != null)
                .map(Teacher::getId)
                .collect(Collectors.toSet());
        if (!availableTeacherIds.contains(teacherId)) {
            log.warn("teacher {} no permission to clazz {}", teacherId, clazzId);
            return MapMessage.errorMessage("您没有该班级的管理权限");
        }
        ref.setDisabled(true);
        smartClazzQuestionRefPersistence.update(ref.getId(), ref);
        return MapMessage.successMessage("删除成功");
    }

    /**
     * 生成智慧课堂答题报告
     */
    public MapMessage generateSmartClazzQuestionReport(Long teacherId, SmartClazzQuestionReport smartClazzQuestionReport) {
        if (smartClazzQuestionReport == null) {
            return MapMessage.errorMessage("invalid param generate report");
        }
        Long clazzId = ConversionUtils.toLong(smartClazzQuestionReport.getClazzId());
        List<Teacher> teachers = ClazzTeacher.toTeacherList(teacherLoaderClient.loadClazzTeachers(clazzId));
        Set<Long> availableTeacherIds = teachers.stream()
                .filter(e -> e != null && e.getId() != null)
                .map(Teacher::getId)
                .collect(Collectors.toSet());
        if (!availableTeacherIds.contains(teacherId)) {
            log.warn("teacher {} no permission to clazz {}", teacherId, clazzId);
            return MapMessage.errorMessage("您没有该班级的管理权限");
        }
        Subject subject = smartClazzQuestionReport.getSubject();
        if (subject == null || subject == Subject.UNKNOWN) {
            return MapMessage.errorMessage("subject unknown,subject:" + subject);
        }
        String questionId = smartClazzQuestionReport.getQuestionId();
        if (StringUtils.isBlank(questionId)) {
            return MapMessage.errorMessage("invalid questionId param, questionId:" + questionId);
        }
        if (ConversionUtils.toInt(smartClazzQuestionReport.getStudentAnswerCount()) <= 0) {
            return MapMessage.errorMessage("学生答题数必须大于零");
        }
        if (StringUtils.isBlank(smartClazzQuestionReport.getAnswer())) {
            return MapMessage.errorMessage("试题必须有正答案");
        }
        //从缓存中取
        SmartClazzQuestionReport report = smartClazzQuestionReportDao.findReportByClazzIdAndQuestionId(clazzId, subject, questionId);

        switch (StringUtils.upperCase(smartClazzQuestionReport.getAnswer())) {
            case "A":
                smartClazzQuestionReport.setCorrectAnswerCount(smartClazzQuestionReport.getAnswerCountA());
                break;
            case "B":
                smartClazzQuestionReport.setCorrectAnswerCount(smartClazzQuestionReport.getAnswerCountB());
                break;
            case "C":
                smartClazzQuestionReport.setCorrectAnswerCount(smartClazzQuestionReport.getAnswerCountC());
                break;
            case "D":
                smartClazzQuestionReport.setCorrectAnswerCount(smartClazzQuestionReport.getAnswerCountD());
                break;
            default:
                smartClazzQuestionReport.setCorrectAnswerCount(0);
        }
        if (smartClazzQuestionReport.getCorrectAnswerCount() == null) {
            return MapMessage.successMessage("答对学生数不能为空");
        }
        if (report != null) {
            smartClazzQuestionReport.setId(report.getId());
            //比较学生的答题结果是否有变化,若有，是存储最新的
            List<SmartClazzStudentResult> oldResultList = report.getStudents();
            List<String> oldStudentAnswers = new LinkedList<>();
            for (SmartClazzStudentResult result : oldResultList) {
                CollectionUtils.addNonNullElement(oldStudentAnswers, StringUtils.join(result.getStudentId(), "_", result.getStudentAnswer()));
            }

            List<SmartClazzStudentResult> newResultList = smartClazzQuestionReport.getStudents();
            //过滤有卡片号没有对应学生的数据
            newResultList = newResultList.stream().filter(source -> source.getStudentId() != null)
                    .collect(Collectors.toList());

            List<String> newStudentAnswers = new LinkedList<>();
            for (SmartClazzStudentResult result : newResultList) {
                CollectionUtils.addNonNullElement(newStudentAnswers, StringUtils.join(result.getStudentId(), "_", result.getStudentAnswer()));
            }
            if (!CollectionUtils.isEqualCollection(newStudentAnswers, oldStudentAnswers)) {
                smartClazzQuestionReportDao.update(smartClazzQuestionReport.getId(), smartClazzQuestionReport);
            }
        } else {
            smartClazzQuestionReportDao.insert(smartClazzQuestionReport);
        }

        return MapMessage.successMessage("生成报告成功");
    }

    @Deprecated
    public Page<SmartClazzQuestionReport> findSmartClazzQuestionReport(Long clazzId, Subject subject, Date startDate, Date endDate, Pageable pageable) {
        return smartClazzQuestionReportDao.pagingFindReport(clazzId, subject, startDate, endDate, pageable);
    }

    public Page<SmartClazzQuestionReport> findSmartClazzQuestionReport(Long groupId, Date startDate, Date endDate, Pageable pageable) {
        return smartClazzQuestionReportDao.pagingFindReport(groupId, startDate, endDate, pageable);
    }

    public SmartClazzQuestionReport findQuestionReportById(String id) {
        return smartClazzQuestionReportDao.load(id);
    }

    private void sendParentAppMessage(String teacherName, List<User> students,
                                      ParentLoader parentLoader) {
        if (CollectionUtils.isEmpty(students)) {
            return;
        }
        Map<Long, User> iStudents = new HashMap<>();
        students.forEach(student -> iStudents.put(student.getId(), student));
        Map<Long, List<StudentParent>> studentParents = parentLoader.loadStudentParents(new HashSet<>(iStudents.keySet()));
        if (MapUtils.isEmpty(studentParents)) {
            return;
        }
        String teacher = teacherName == null ? "" : teacherName;
        String content = "家长您好：\n" + teacher + "老师刚刚在课堂上奖励您的孩子。";
        String linkUrl = "/parentMobile/homework/loadsmart.vpage?sid=";
        for (Long studentId : studentParents.keySet()) {
            List<StudentParent> iParents = studentParents.get(studentId);
            if (CollectionUtils.isNotEmpty(iParents)) {
                List<Long> parentIds = iParents.stream().map(StudentParent::getParentUser).map(User::getId).collect(Collectors.toList());
                String iContent = iStudents.get(studentId).fetchRealname() + content;
                String iLink = "/parentMobile/homework/loadsmart.vpage?sid=" + SafeConverter.toString(studentId);
                List<AppMessage> messageList = new ArrayList<>();
                Map<String, Object> extInfo = new HashMap<>();
                extInfo.put("studentId", studentId);
                extInfo.put("tag", ParentMessageTag.课堂奖励.name());
                extInfo.put("type", ParentMessageType.REMINDER.name());
                extInfo.put("senderName", teacher);
                for (Long parentId : parentIds) {
                    //新消息中心
                    AppMessage message = new AppMessage();
                    message.setUserId(parentId);
                    message.setContent(iContent);
                    message.setLinkType(1);
                    message.setLinkUrl(iLink);
                    message.setImageUrl("");
                    message.setExtInfo(extInfo);
                    message.setMessageType(ParentMessageType.REMINDER.getType());
                    messageList.add(message);
                }
                messageList.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
                //发送jpush
                Map<String, Object> extras = new HashMap<>();
                extras.put("studentId", studentId);
                extras.put("url", iLink);
                extras.put("tag", ParentMessageTag.课堂奖励.name());
                //新的push参数
                extras.put("s", ParentAppPushType.CLASS_REWARD.name());
                appMessageServiceClient.sendAppJpushMessageByIds(iContent, AppMessageSource.PARENT, parentIds, extras);
            }
        }
    }

}
