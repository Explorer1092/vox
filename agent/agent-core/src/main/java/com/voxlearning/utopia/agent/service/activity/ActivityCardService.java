package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.dao.mongo.activity.*;
import com.voxlearning.utopia.agent.persist.entity.activity.*;
import com.voxlearning.utopia.agent.persist.entity.platform.AgentPlatformUserInfo;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.platform.AgentPlatformUserInfoService;
import com.voxlearning.utopia.agent.view.activity.ActivityCardRecordView;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class ActivityCardService {

    @Inject
    private AgentActivityDao agentActivityDao;
    @Inject
    private ActivityCardDao cardDao;
    @Inject
    private ActivityCardRedeemCodeDao cardRedeemCodeDao;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private ActivityCardStatisticsService cardStatisticsService;
    @Inject
    private ActivityCardCourseService cardCourseService;
    @Inject
    private CrmSummaryLoaderClient summaryLoaderClient;
    @Inject
    private AgentPlatformUserInfoService platformUserInfoService;
    @Inject
    private AgentPlatformUserInfoDao platformUserInfoDao;
    @Inject
    private ActivityCardCourseTempDao cardCourseTempDao;

    public MapMessage addCardRecord(String activityId, String cardNo, Long cardUserId, Date cardTime, Long userId){

        AgentActivity activity = agentActivityDao.load(activityId);
        if(activity == null || SafeConverter.toBoolean(activity.getDisabled())){
            return MapMessage.errorMessage("不存在该活动");
        }

        ActivityCard dbCard = cardDao.loadByCn(cardNo);
        if(dbCard != null){
            return MapMessage.errorMessage("该序列号已经被" + (StringUtils.isNotBlank(dbCard.getUserName())? dbCard.getUserName() : "") + "人员添加，请勿重复添加");
        }

        ActivityCardRedeemCode cardRedeemCode = cardRedeemCodeDao.loadByCn(cardNo);
        if(cardRedeemCode == null){
            return MapMessage.errorMessage("该卡号非法");
        }

        User platformUser = userLoaderClient.loadUser(cardUserId);
        if(platformUser == null){
            return MapMessage.errorMessage("不存在该用户");
        }

        ActivityCard card = new ActivityCard();
        card.setActivityId(activityId);
        card.setCardNo(cardNo);
        card.setCardUserId(cardUserId);
        card.setCardTime(cardTime == null ? new Date() : cardTime);
        card.setCardUserRegTime(platformUser.getCreateTime());
        card.setIsUsed(false);
        card.setUserId(userId);
        AgentUser agentUser = baseOrgService.getUser(userId);
        if(agentUser != null){
            card.setUserName(agentUser.getRealName());
        }
        cardDao.insert(card);

        AlpsThreadPool.getInstance().submit(() -> cardStatisticsService.cardStatistics(card));

        AlpsThreadPool.getInstance().submit(() -> cardAlreadyUsed(cardNo));

        return MapMessage.successMessage();
    }

    private void cardAlreadyUsed(String cardNo){
        ActivityCardCourseTemp cardCourseTemp = cardCourseTempDao.loadByCn(cardNo);
        if(cardCourseTemp == null){
            return;
        }
        cardUsedByCardNo(cardNo, cardCourseTemp.getStudentId(), cardCourseTemp.getCourseIds());
        cardCourseTempDao.remove(cardCourseTemp.getId());
    }

    public void cardUsedByRedeemCode(String cardRedeemCode, Long studentId, List<String> courseIds){
        ActivityCardRedeemCode dbData = cardRedeemCodeDao.loadByRd(cardRedeemCode);
        if(dbData == null || StringUtils.isBlank(dbData.getCardNo())){
            return;
        }
        cardUsedByCardNo(dbData.getCardNo(), studentId, courseIds);
    }

    public void cardUsedByCardNo(String cardNo, Long studentId, List<String> courseIds){


        ActivityCard card = cardDao.loadByCn(cardNo);
        if(card == null){
            ActivityCardCourseTemp cardCourseTemp = cardCourseTempDao.loadByCn(cardNo);
            if(cardCourseTemp == null){
                cardCourseTemp = new ActivityCardCourseTemp();
                cardCourseTemp.setCardNo(cardNo);
                cardCourseTemp.setStudentId(studentId);
                cardCourseTemp.setCourseIds(courseIds);
                cardCourseTempDao.insert(cardCourseTemp);
            }
            return;
        }
        card.setIsUsed(true);
        cardDao.upsert(card);

        // 更新礼品卡的使用数据
        AlpsThreadPool.getInstance().submit(() -> cardStatisticsService.cardUsedStatistics(card));

        // 保存激活的课程信息
        AlpsThreadPool.getInstance().submit(() -> cardCourseService.handleListenerData(card.getCardNo(), studentId, courseIds));

    }

    public List<ActivityCardRecordView> getRecordList(String activityId, Long userId){

        List<ActivityCardRecordView> resultList = new ArrayList<>();
        AgentActivity activity = agentActivityDao.load(activityId);
        if(activity == null){
            return resultList;
        }
        List<ActivityCard> cardList = cardDao.loadByAidAndUid(activityId, userId);
        if(CollectionUtils.isEmpty(cardList)){
            return resultList;
        }

        Set<Long> teacherIds = cardList.stream().map(ActivityCard::getCardUserId).collect(Collectors.toSet());
        Map<Long, CrmTeacherSummary> teacherSummaryMap = summaryLoaderClient.loadTeacherSummary(teacherIds);

        cardList.forEach(p -> {
            ActivityCardRecordView view = new ActivityCardRecordView();
            view.setTeacherId(p.getCardUserId());
            CrmTeacherSummary teacherSummary = teacherSummaryMap.get(p.getCardUserId());
            if(teacherSummary != null){
                view.setTeacherName(teacherSummary.getRealName());
                view.setSchoolId(teacherSummary.getSchoolId());
                view.setSchoolName(teacherSummary.getSchoolName());
                Subject subject = Subject.safeParse(teacherSummary.getSubject());
                view.setSubject(subject == null ? "" : subject.getValue());
                view.setAuthFlag(AuthenticationState.valueOf(teacherSummary.getAuthStatus()) == AuthenticationState.SUCCESS);
            }
            view.setCardNo(p.getCardNo());

            view.setIsUsed(SafeConverter.toBoolean(p.getIsUsed()));
            view.setBusinessTime(p.getCardTime());

            view.setIsParent(platformUserInfoService.isParent(p.getCardUserId()));

            resultList.add(view);
        });

        return resultList;
    }

    private List<ActivityCard> loadByAidAndUidsAndTime(String activityId, Collection<Long> userIds, Date startDate, Date endDate){

        List<ActivityCard> cardList = cardDao.loadByAidAndUids(activityId, userIds);
        if(CollectionUtils.isEmpty(cardList)){
            return new ArrayList<>();
        }
        return cardList.stream().filter(p -> {
            if(startDate != null){
                if(endDate != null){
                    return p.getCardTime().after(startDate) && p.getCardTime().before(endDate);
                }else {
                    return p.getCardTime().after(startDate);
                }
            }else {
                if(endDate != null){
                    return p.getCardTime().before(endDate);
                }else {
                    return true;
                }
            }
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getParentCountData(String activityId, Long userId){
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("newParentCount", 0);
        dataMap.put("parentCount", 0);

        AgentActivity activity = agentActivityDao.load(activityId);
        if(activity == null || SafeConverter.toBoolean(activity.getDisabled())){
            return dataMap;
        }

        Set<Long> userIds = new HashSet<>();
        userIds.add(userId);
        List<Long> managedGroupIds = baseOrgService.getManagedGroupIdListByUserId(userId);
        if(CollectionUtils.isNotEmpty(managedGroupIds)){
            List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(managedGroupIds.get(0));
            userIds.addAll(groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList()));
        }

        Date endDate = new Date();
        if(activity.getEndDate() != null && activity.getEndDate().before(endDate)){
            endDate = activity.getEndDate();
        }
        Date startDate = null;
        if(activity.getStartDate() != null){
            startDate = activity.getStartDate();
        }

        List<ActivityCard> cardList = loadByAidAndUidsAndTime(activityId, userIds, startDate, endDate);
        if(CollectionUtils.isEmpty(cardList)){
            return dataMap;
        }

        Set<Long> platformUserIds = cardList.stream().map(ActivityCard::getCardUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        int parentCount = 0;
        int newParentCount = 0;
        for(Long p : platformUserIds){
            if(platformUserInfoService.isParent(p)){
                parentCount ++;
                if(startDate == null){
                    newParentCount ++;
                }else {
                    AgentPlatformUserInfo userInfo = platformUserInfoDao.load(p);
                    if (userInfo != null && userInfo.getBeParentTime() != null && userInfo.getBeParentTime().after(startDate)) {
                        newParentCount ++;
                    }
                }
            }
        }

        dataMap.put("newParentCount", newParentCount);
        dataMap.put("parentCount", parentCount);
        return dataMap;
    }

}
