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

package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.advertisement.client.UserAdvertisementServiceClient;
import com.voxlearning.utopia.service.config.api.constant.AdvertisementPositionType;
import com.voxlearning.utopia.service.message.api.entity.AppGlobalMessage;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.AppMessageLoaderClient;
import com.voxlearning.utopia.service.message.client.MessageLoaderClient;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccounts;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.reminder.api.ReminderLoader;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.AdMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNewsPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppJxtExtTabTypeToNative;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.api.entity.JxtExtTab;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsPushRecord;
import com.voxlearning.utopia.service.vendor.consumer.JxtLoaderClient;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwe.liao
 * @since 2016/4/12
 */
@Controller
@Slf4j
@RequestMapping(value = "/v1/parent/jxt/")
public class ParentJxtApiController extends AbstractParentApiController {

    @Inject
    private UserAdvertisementServiceClient userAdvertisementServiceClient;

    @Inject
    private JxtLoaderClient jxtLoaderClient;
    @Inject
    private AppMessageLoaderClient appMessageLoaderClient;
    @Inject
    private MessageLoaderClient messageLoaderClient;
    @ImportService(interfaceClass = ReminderLoader.class)
    private ReminderLoader reminderLoader;

    /**
     * 聊天组列表
     */
    @RequestMapping(value = "/easemob_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getUserEaseMobList() {
        return failMessage(RES_RESULT_ERROR_NEED_UPGRADE);
    }

    /**
     * 针对单个孩子的聊天组列表接口
     * 现在这个接口又改来跟上面的接口逻辑一样了。但是为了以防还会改成按照孩子维度来处理。
     * 所以保留了这个接口以及sid的参数。以后可以做无缝切换
     *
     * @since 1.9.0
     */
    @RequestMapping(value = "/easemob_list_student.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getStudentEaseMobList() {
        return failMessage(RES_RESULT_ERROR_NEED_UPGRADE);
    }

    /**
     * 家校通新首页的banner
     */
    @RequestMapping(value = "/be.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getJxtIndexAdBanner() {
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalVendorUserException e) {
            return failMessage(e);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getCurrentParent();
        //学生与家长无关联
        if (!studentIsParentChildren(parent.getId(), studentId)) {
            return failMessage(RES_RESULT_STUDENT_NOT_RELATION_TO_PARENT);
        }
        List<AdMapper> data = userAdvertisementServiceClient.getUserAdvertisementService()
                .loadAdvertisementData(studentId, Collections.singletonList(AdvertisementPositionType.REWARD_PARENT_HOME_POLL.getType()));
        List<Map<String, Object>> mapList = generateAdMap(data);
        return successMessage().add(RES_RESULT_PARENT_API_AD, mapList);
    }

    /**
     * 扩展tab列表
     */
    @RequestMapping(value = "/ext_tab.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getJxtIndexExtTabList() {
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        Long parentId = getCurrentParentId();
        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parentId);

        if (VersionUtil.compareVersion(ver, "1.5.3") < 0) {
            //低于1.5.3版本仍然要判断登录状态和有无孩子。
            //高于1.5.3这俩都不限制了
            if (parentId == null) {
                return failMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
            }
            if (CollectionUtils.isEmpty(studentParentRefs)) {
                return failMessage(RES_RESULT_USER_UNBIND_STUDENT);
            }
        }
        //原生的tab
        List<ParentAppJxtExtTabTypeToNative> extTabTypeToNatives = new ArrayList<>();
        //导流的tab
        List<NewAdMapper> newAdMappers = new ArrayList<>();
        //非导流的h5tab
        List<JxtExtTab> jxtExtTabList;
        //公众号列表
        List<OfficialAccounts> accountsList = new ArrayList<>();
        String slotId = "220701";
        if (parentId != null) {
            //已登录的用户

            //跳原生的tab
            extTabTypeToNatives = ParentAppJxtExtTabTypeToNative.onlineTypeList(ver);
            //高版本去掉消息中心
            if (VersionUtil.compareVersion(ver, "1.8.2") > 0 && extTabTypeToNatives.contains(ParentAppJxtExtTabTypeToNative.USER_MESSAGE)) {
                extTabTypeToNatives.remove(ParentAppJxtExtTabTypeToNative.USER_MESSAGE);
            }
            //导流的tab
            newAdMappers = userAdvertisementServiceClient.getUserAdvertisementService()
                    .loadNewAdvertisementData(parentId, slotId, getRequestString(REQ_SYS), ver);
            //非导流的tab
            jxtExtTabList = jxtLoaderClient.getAllOnlineJxtExtTabList();

            if (CollectionUtils.isNotEmpty(studentParentRefs)) {
                Collections.sort(studentParentRefs, (o1, o2) -> o1.getCreateTime().compareTo(o2.getCreateTime()));
                Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet()));
                //取家长的前3个孩子
                studentParentRefs = studentParentRefs.subList(0, studentParentRefs.size() > 3 ? 3 : studentParentRefs.size());
                //过滤非导流的tab灰度和生效时间
                final List<StudentParentRef> finalStudentParentRefs = studentParentRefs;
                jxtExtTabList = jxtExtTabList.stream()
                        .filter(p -> p.getStartDate().before(new Date()))
                        .filter(p -> p.getEndDate().after(new Date()))
                        .filter(p -> StringUtils.isBlank(p.getMainFunctionName()) || StringUtils.isBlank(p.getSubFunctionName()) || finalStudentParentRefs.stream().anyMatch(ref -> studentDetailMap.get(ref.getStudentId()) != null && getGrayFunctionManagerClient().getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetailMap.get(ref.getStudentId()), p.getMainFunctionName(), p.getSubFunctionName(), true)))
                        .filter(p -> !Long.valueOf(10003L).equals(p.getTabType()) || (Long.valueOf(10003L).equals(p.getTabType()) && showTeachersDay(finalStudentParentRefs)))
                        .collect(Collectors.toList());
            } else {
                jxtExtTabList = jxtExtTabList.stream()
                        .filter(p -> p.getStartDate().before(new Date()))
                        .filter(p -> p.getEndDate().after(new Date()))
                        //没有孩子的家长不显示假期作业 也不显示教师节活动
                        .filter(p -> Long.compare(p.getTabType(), 10002L) != 0 && Long.compare(p.getTabType(), 10003L) != 0)
                        .collect(Collectors.toList());
            }
            if (extTabTypeToNatives.contains(ParentAppJxtExtTabTypeToNative.OFFICIAL_ACCOUNT) && parentId != 20001L) {
                // 公众号的tab
                accountsList = officialAccountsServiceClient.loadUserOfficialAccounts(getCurrentParentId());
            }
        } else {
            //没登录的用户只给看一个资讯
            jxtExtTabList = jxtLoaderClient.getAllOnlineJxtExtTabList().stream().filter(p -> p.getTabType().equals(10001L)).collect(Collectors.toList());
        }

        // 打点
        for (int i = 0; i < newAdMappers.size(); i++) {
            // log
            if (Boolean.FALSE.equals(newAdMappers.get(i).getLogCollected())) {
                continue;
            }
            LogCollector.info("sys_new_ad_show_logs",
                    MiscUtils.map(
                            "user_id", parentId,
                            "env", RuntimeMode.getCurrentStage(),
                            "version", getRequestString("version"),
                            "aid", newAdMappers.get(i).getId(),
                            "acode", newAdMappers.get(i).getCode(),
                            "index", i,
                            "slotId", slotId,
                            "client_ip", getWebRequestContext().getRealRemoteAddress(),
                            "time", DateUtils.dateToString(new Date()),
                            "agent", getRequest().getHeader("User-Agent"),
                            "uuid", UUID.randomUUID().toString(),
                            "system", getRequestString("sys"),
                            "system_version", getRequestString("sysVer")
                    ));
        }
        List<Map<String, Object>> mapList = generateExtTabList(parentId, extTabTypeToNatives, newAdMappers, jxtExtTabList, accountsList);
        return successMessage().add(RES_RESULT_JXT_EXT_TAB_LIST, mapList);
    }

    /**
     * 有关系为爸爸妈妈的孩子 并且至少有一个孩子有班级
     *
     * @param finalStudentParentRefs 家长学生关系
     */
    private boolean showTeachersDay(List<StudentParentRef> finalStudentParentRefs) {
        Set<Long> studentIds = finalStudentParentRefs.stream().filter(ref -> Objects.equals(CallName.妈妈.name(), ref.getCallName())
                || Objects.equals(CallName.爸爸.name(), ref.getCallName())).map(StudentParentRef::getStudentId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(studentIds))
            return false;
        Map<Long, List<GroupMapper>> longListMap = deprecatedGroupLoaderClient.loadStudentGroups(studentIds, false);
        Long count = longListMap.values().stream().filter(CollectionUtils::isNotEmpty).count();
        return count > 0;
    }

    @RequestMapping(value = "/top_notice.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getTopNotice() {
        return failMessage(RES_RESULT_ERROR_NEED_UPGRADE);
    }

    /**
     * 扩展tab的数字红点
     * 1.5.3之后未登录用户也能看资讯。所以不做校验了。
     */
    @RequestMapping(value = "ext_tab_latest.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getExtTabLatestInfo() {
        Long time = getRequestLong(REQ_PARENT_LATEST_TIME);
        Long parentId = getCurrentParentId();
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parentId);
        if (VersionUtil.compareVersion(ver, "1.5.3") < 0) {
            //低于1.5.3版本仍然要判断登录状态和有无孩子。
            //高于1.5.3这俩都不限制了
            if (parentId == null) {
                return failMessage(RES_RESULT_LOAD_USER_ERROR).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
            }
            if (CollectionUtils.isEmpty(studentParentRefs)) {
                return failMessage(RES_RESULT_USER_UNBIND_STUDENT);
            }
        }
        List<Map<String, Object>> redPointOrCountMap = new ArrayList<>();
        // 用户系统消息不包括点赞提醒、头条提醒
        List<AppMessage.Location> locationList = messageLoaderClient.getMessageLoader().loadAppMessageLocations(parentId);
        if (CollectionUtils.isNotEmpty(studentParentRefs) && VersionUtil.compareVersion(ver, "1.8.2") < 0) {
            Set<String> tagList = getUserMessageTagList(parentId);
            List<AppGlobalMessage> appGlobalMessageList = appMessageLoaderClient.appGlobalMessages(AppMessageSource.PARENT.name(), tagList);
            long dynamicMessageCount = locationList.stream()
                    .filter(p -> time == 0 || p.getCreateTime() > time)
                    .filter(t -> ParentMessageType.REMINDER.getType().equals(t.getMessageType()))
                    .count();
            long globalMessageCount = appGlobalMessageList.stream()
                    .filter(p -> time == 0 || p.getCreateTime() > time)
                    .filter(t -> ParentMessageType.REMINDER.getType().equals(t.getMessageType()))
                    .count();
            long messageCount = dynamicMessageCount + globalMessageCount;
            Map<String, Object> sysMessageShowMap = new HashMap<>();
            sysMessageShowMap.put(RES_RESULT_JXT_EXT_TAB_MESSAGE_COUNT_SHOW, ParentAppJxtExtTabTypeToNative.USER_MESSAGE.getShowCount());
            sysMessageShowMap.put(RES_RESULT_JXT_EXT_TAB_MESSAGE_COUNT, messageCount);
            sysMessageShowMap.put(RES_RESULT_JXT_EXT_TAB_MESSAGE_COUNT_TYPE, SafeConverter.toLong(ParentAppJxtExtTabTypeToNative.USER_MESSAGE.getType()));
            redPointOrCountMap.add(sysMessageShowMap);
        }
        // 公众号红点 上线状态才去查询
        if (ParentAppJxtExtTabTypeToNative.OFFICIAL_ACCOUNT.getOnline()) {
            List<OfficialAccounts> accountsList = officialAccountsServiceClient.loadUserOfficialAccounts(parentId);
            if (CollectionUtils.isNotEmpty(accountsList)) {
                for (OfficialAccounts accounts : accountsList) {
                    // 查询未读文章数量
                    // 默认取7天以内的文章
                    // 这里修改成和获取文章的逻辑一致，起始时间取最近一次关注时间
                    Date defaultStartDate = DateUtils.calculateDateDay(new Date(), -7);
                    // 如果起始时间是空，查询最近一次关注的修改时间，从那个时间点开始load，超过七天按七天取
                    Date startDate;
                    // 获得最近一次关注的时间
                    Date latestFollowTime = officialAccountsServiceClient.getFollowAccountDate(parentId, accounts.getId());
                    if (time != 0) {
                        startDate = new Date(time);
                        // 如果传入的起始时间是在最近一次关注之前，舍掉关注前的东西
                        // 这个是为了解决关注取关清缓存后，列表与进去后数据不一致的情况
                        if (latestFollowTime != null && startDate.before(latestFollowTime)) {
                            startDate = latestFollowTime;
                        }
                    } else {
                        startDate = latestFollowTime;
                    }

                    // 如果获取到的时间大于7天， 则还按照7天的时间去过滤
                    if (startDate == null || defaultStartDate.after(startDate)) {
                        startDate = defaultStartDate;
                    }

                    int articleCount = officialAccountsServiceClient.loadArticleRedCount(accounts.getId(), startDate);
                    // 加上公众号新消息的数量  1.7.2及以上的版本支持
                    long messageCount = 0;
                    if (VersionUtil.compareVersion(ver, "1.7.2") >= 0) {
                        final Date finalStartDate = startDate;
                        Set<String> officialUserMessageIds = locationList.stream()
                                .filter(p -> ParentMessageType.OFFICIAL_MSG.getType().equals(p.getMessageType()))
                                .filter(p -> new Date(p.getCreateTime()).after(finalStartDate))
                                .map(AppMessage.Location::getId)
                                .collect(Collectors.toSet());
                        Map<String, AppMessage> appMessageMap = messageLoaderClient.getMessageLoader().loadAppMessageByIds(officialUserMessageIds);
                        messageCount = appMessageMap.values().stream()
                                .filter(p -> ParentMessageType.OFFICIAL_MSG.getType().equals(p.getMessageType()))
                                .filter(p -> new Date(p.getCreateTime()).after(finalStartDate))
                                .filter(p -> p.getExtInfo() != null && Objects.equals(SafeConverter.toString(p.getExtInfo().get("accountsKey")), accounts.getAccountsKey()))
                                .count();

                        // load全局的消息，算进消息的数量
                        List<AppGlobalMessage> appGlobalMessages;
                        appGlobalMessages = appMessageLoaderClient.findByMessageSource(AppMessageSource.PARENT.name());

                        messageCount += appGlobalMessages
                                .stream()
                                .filter(m -> Objects.equals(m.getMessageType(), ParentMessageType.OFFICIAL_MSG.type))
                                .filter(p -> new Date(p.getCreateTime()).after(finalStartDate))
                                // 由于有些模板消息设置成了未来时间，需要这里过滤下
                                .filter(p -> new Date(p.getCreateTime()).before(new Date()))
                                .filter(m -> MapUtils.isNotEmpty(m.getExtInfo()) &&
                                        Objects.equals(SafeConverter.toString(m.getExtInfo().get("accountsKey")),
                                                SafeConverter.toString(accounts.getAccountsKey())))
                                .count();
                    }

                    Map<String, Object> newsShowMap = new HashMap<>();
                    newsShowMap.put(RES_RESULT_JXT_EXT_TAB_MESSAGE_COUNT_SHOW, ParentAppJxtExtTabTypeToNative.OFFICIAL_ACCOUNT.getShowCount());
                    newsShowMap.put(RES_RESULT_JXT_EXT_TAB_MESSAGE_COUNT, articleCount + messageCount);
                    newsShowMap.put(RES_RESULT_JXT_EXT_TAB_MESSAGE_COUNT_TYPE, accounts.getId());
                    redPointOrCountMap.add(newsShowMap);
                }
            }
        }
        //资讯的tab
        JxtExtTab jxtExtTab = jxtLoaderClient.getAllOnlineJxtExtTabList().stream().filter(p -> p.getTabType().equals(10001L)).findFirst().orElse(null);
        //资讯推送
        Set<Long> studentIds = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());
        Collection<StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(studentIds).values();
        if (jxtExtTab != null) {
            //命中了这个tab的灰度再返回
            List<JxtNewsPushRecord> pushRecordList = jxtNewsLoaderClient.getAllOnlineJxtNewsPushRecord();
            long jxtNewsPushRecordCount = 0;
            if (CollectionUtils.isEmpty(studentDetails)) {
                jxtNewsPushRecordCount = pushRecordList.stream()
                        //需要返回的是用户上次请求的time和当前current之间生效的推送
                        .filter(p -> time == 0 || p.getStartTime() == null || (p.getStartTime().after(new Date(time)) && p.getStartTime().before(new Date())))
                        .filter(p -> p.generatePushType().equals(JxtNewsPushType.ALL_USER.getType())
                                || (p.generatePushType().equals(JxtNewsPushType.SIGNAL_USER.getType()) && parentId != null && parentId.equals(p.getAvailableUserId())))
                        .count();
            } else if (isHitGray(studentDetails, jxtExtTab)) {
                Set<Integer> parentRegionCodes = new HashSet<>();
                for (StudentDetail studentDetail : studentDetails) {
                    parentRegionCodes.add(studentDetail.getStudentSchoolRegionCode());
                    parentRegionCodes.add(studentDetail.getCityCode());
                    parentRegionCodes.add(studentDetail.getRootRegionCode());
                }
                jxtNewsPushRecordCount = new ArrayList<>(jxtNewsLoaderClient.getAllOnlineJxtNewsPushRecord()).stream()
                        //需要返回的是用户上次请求的time和当前current之间生效的推送
                        .filter(p -> time == 0 || p.getStartTime() == null || (p.getStartTime().after(new Date(time)) && p.getStartTime().before(new Date())))
                        //三种推送方式
                        //全部用户
                        //单个用户
                        //指定区域
                        .filter(p -> p.generatePushType().equals(JxtNewsPushType.ALL_USER.getType())
                                || (p.generatePushType().equals(JxtNewsPushType.SIGNAL_USER.getType()) && parentId != null && parentId.equals(p.getAvailableUserId()))
                                || (p.generatePushType().equals(JxtNewsPushType.REGION.getType()) && parentRegionCodes.stream().anyMatch(e -> p.getRegionCodeList().contains(e))))
                        .count();

            }
            Map<String, Object> newsShowMap = new HashMap<>();
            newsShowMap.put(RES_RESULT_JXT_EXT_TAB_MESSAGE_COUNT_SHOW, jxtExtTab.getShowMessageCount());
            newsShowMap.put(RES_RESULT_JXT_EXT_TAB_MESSAGE_COUNT, jxtNewsPushRecordCount);
            newsShowMap.put(RES_RESULT_JXT_EXT_TAB_MESSAGE_COUNT_TYPE, jxtExtTab.getTabType());
            redPointOrCountMap.add(newsShowMap);
        }

        return successMessage().add(RES_RESULT_EXT_TAB_LATEST_NEW_COUNT, redPointOrCountMap).add(RES_RESULT_JXT_EXT_TAB_MESSAGE_COUNT_TIME, new Date().getTime());
    }


    /**
     * 查询聊天群成员列表
     *
     * @since v1.5.2
     */
    @RequestMapping(value = "/chatgroup/memberlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage chatGroupMemberList() {
        return failMessage(RES_RESULT_ERROR_NEED_UPGRADE);
    }

    /**
     * 检查用户是否已被聊天群屏蔽
     *
     * @since v1.5.2
     */
    @RequestMapping(value = "/chatgroup/check/blocked.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkBlocked() {
        return failMessage(RES_RESULT_ERROR_NEED_UPGRADE);
    }

    /**
     * 用户加群结果通知
     *
     * @since V1.5.2
     */
    @RequestMapping(value = "/chatgroup/notify/join.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage joinNotify() {
        return failMessage(RES_RESULT_ERROR_NEED_UPGRADE);
    }

    /**
     * 用户退群结果通知
     *
     * @since v1.5.2
     */
    @RequestMapping(value = "/chatgroup/notify/quit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage quitNotify() {
        return failMessage(RES_RESULT_ERROR_NEED_UPGRADE);
    }

    /**
     * 用户通用群列表查询
     *
     * @since v1.5.2
     */
    @RequestMapping(value = "/chatgroup/list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage chatGroupList() {
        return failMessage(RES_RESULT_ERROR_NEED_UPGRADE);
    }


    /**
     * 用户分享资讯取得可分享的群列表
     * 包括班级群和通用群
     * 班级群要去掉毕业班和禁言的群,同时要注意取出来的群组可能是脏数据。
     * 通用群没啥好说的,跟chatGroupList取得差不多。
     *
     * @since v1.6.0
     */
    @RequestMapping(value = "/share/chatgroup/list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage chatGroupList4Share() {
        return failMessage(RES_RESULT_ERROR_NEED_UPGRADE);
    }

    /**
     * 查询聊天群信息
     *
     * @since v1.5.2
     */
    @RequestMapping(value = "/chatgroup/info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage chatGroupInfo() {
        return failMessage(RES_RESULT_ERROR_NEED_UPGRADE);
    }

    //广告详情
    private List<Map<String, Object>> generateAdMap(Collection<AdMapper> adMapperList) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (CollectionUtils.isEmpty(adMapperList)) {
            return mapList;
        }
        adMapperList.forEach(adMapper -> {
            Map<String, Object> map = new HashMap<>();
            map.put(RES_RESULT_AD_ID, adMapper.getId());
            //链接是绝对地址
            map.put(RES_RESULT_AD_URL, adMapper.getResourceUrl());
            //图片是相对地址
            map.put(RES_RESULT_AD_IMG, combineMessageUrl(adMapper.getImg()));
            mapList.add(map);
        });
        return mapList;
    }


    private List<Map<String, Object>> generateExtTabList(Long parentId, List<ParentAppJxtExtTabTypeToNative> extTabTypeToNatives,
                                                         List<NewAdMapper> mapperList,
                                                         List<JxtExtTab> jxtExtTabList,
                                                         List<OfficialAccounts> accountsList) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        //首页扩展项
        if (CollectionUtils.isNotEmpty(jxtExtTabList)) {
            Collections.sort(jxtExtTabList, (o1, o2) -> o2.getRank() - o1.getRank());
            jxtExtTabList.forEach(type -> {
                Map<String, Object> map = new HashMap<>();
                map.put(RES_RESULT_JXT_EXT_TAB_TYPE, 0);
                map.put(RES_RESULT_JXT_EXT_TAB_NAME, type.getName());
                map.put(RES_RESULT_JXT_EXT_TAB_EXT_INFO, type.getDesc());
                map.put(RES_RESULT_JXT_EXT_TAB_ICON, combineCdbUrl(type.getImg()));
                if (JxtExtTab.relativeLinkType.equals(type.getLinkType())) {
                    map.put(RES_RESULT_JXT_EXT_TAB_URL, ProductConfig.getMainSiteBaseUrl() + type.getLink());
                } else {
                    map.put(RES_RESULT_JXT_EXT_TAB_URL, type.getLink());
                }
                map.put(RES_RESULT_JXT_EXT_TAB_MESSAGE_COUNT_TYPE, type.getTabType());
                mapList.add(map);
            });
        }
        //原生tab+公众号
        if (CollectionUtils.isNotEmpty(extTabTypeToNatives)) {
            Collections.sort(extTabTypeToNatives, (o1, o2) -> o1.getRank() - o2.getRank());
            extTabTypeToNatives.forEach(type -> {
                // 公众号的单独处理
                if (type == ParentAppJxtExtTabTypeToNative.OFFICIAL_ACCOUNT) {
                    if (CollectionUtils.isNotEmpty(accountsList)) {
                        // 处理公众号
                        for (OfficialAccounts account : accountsList) {
                            Map<String, Object> map = new HashMap<>();
                            map.put(RES_RESULT_JXT_EXT_TAB_TYPE, type.getType());
                            map.put(RES_RESULT_JXT_EXT_TAB_NAME, account.getName());
                            map.put(RES_RESULT_JXT_EXT_TAB_EXT_INFO, account.getTitle());
                            map.put(RES_RESULT_JXT_EXT_TAB_ICON, account.getImgUrl());
                            map.put(RES_RESULT_JXT_EXT_TAB_MESSAGE_COUNT_TYPE, account.getId());
                            map.put(RES_RESULT_JXT_EXT_TAB_OAID, account.getId());
                            mapList.add(map);
                        }
                    }
                } else {
                    Map<String, Object> map = new HashMap<>();
                    map.put(RES_RESULT_JXT_EXT_TAB_TYPE, type.getType());
                    map.put(RES_RESULT_JXT_EXT_TAB_NAME, type.getTabName());
                    map.put(RES_RESULT_JXT_EXT_TAB_EXT_INFO, type.getTabExtInfo());
                    map.put(RES_RESULT_JXT_EXT_TAB_ICON, getCdnBaseUrlStaticSharedWithSep() + type.getTabIcon());
                    map.put(RES_RESULT_JXT_EXT_TAB_MESSAGE_COUNT_TYPE, SafeConverter.toLong(type.getType()));
                    mapList.add(map);
                }
            });
        }
        //导流广告位
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        String sys = getRequestString(REQ_SYS);
        if (CollectionUtils.isNotEmpty(mapperList)) {
            for (int index = 0; index < mapperList.size(); index++) {
                NewAdMapper mapper = mapperList.get(index);
                Map<String, Object> map = new HashMap<>();
                map.put(RES_RESULT_JXT_EXT_TAB_TYPE, 0);
                map.put(RES_RESULT_JXT_EXT_TAB_NAME, mapper.getContent());
                map.put(RES_RESULT_JXT_EXT_TAB_EXT_INFO, mapper.getBtnContent());
                map.put(RES_RESULT_JXT_EXT_TAB_ICON, combineCdbUrl(mapper.getImg()));
//                String link = "/be/london.vpage?aid=" + mapper.getId() + "&index=" + index + "&v=" + ver + "&s=" + sys;
                String link = AdvertiseRedirectUtils.redirectUrl(mapper.getId(), index, ver, sys, "", 0L);
                map.put(RES_RESULT_JXT_EXT_TAB_URL, ProductConfig.getMainSiteBaseUrl() + link);
                mapList.add(map);
                //曝光打点
                if (Boolean.FALSE.equals(mapper.getLogCollected())) {
                    continue;
                }
                LogCollector.info("sys_new_ad_show_logs",
                        MiscUtils.map(
                                "user_id", parentId,
                                "env", RuntimeMode.getCurrentStage(),
                                "version", ver,
                                "aid", mapper.getId(),
                                "acode", SafeConverter.toString(mapper.getCode()),
                                "index", index,
                                "slotId", "220701",
                                "client_ip", getWebRequestContext().getRealRemoteAddress(),
                                "time", DateUtils.dateToString(new Date()),
                                "agent", getRequest().getHeader("User-Agent"),
                                "system", sys
                        ));
            }
        }
        return mapList;
    }

    private String combineMessageUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return StringUtils.EMPTY;
        }
        return getCdnBaseUrlStaticSharedWithSep() + "gridfs/" + url;
    }

    private boolean isHitGray(Collection<StudentDetail> studentDetails, JxtExtTab jxtExtTab) {
        //staging以下直接返回true
        if (RuntimeMode.lt(Mode.STAGING)) {
            return true;
        }
        if (CollectionUtils.isEmpty(studentDetails) || jxtExtTab == null) {
            return false;
        }
        if (StringUtils.isBlank(jxtExtTab.getMainFunctionName()) || StringUtils.isBlank(jxtExtTab.getSubFunctionName())) {
            return true;
        }
        boolean hit = false;
        for (StudentDetail studentDetail : studentDetails) {
            if (getGrayFunctionManagerClient().getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, jxtExtTab.getMainFunctionName(), jxtExtTab.getSubFunctionName(), true)) {
                hit = true;
            }
            if (hit) {
                break;
            }
        }
        return hit;
    }


    @RequestMapping(value = "easemob/bottom/menu.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getMenu() {
        return failMessage(RES_RESULT_ERROR_NEED_UPGRADE);
    }

}
