package com.voxlearning.washington.controller.open.v2.teacher;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.parent.api.DPGroupMessageConfirmInfoLoader;
import com.voxlearning.utopia.service.parent.api.DPScoreCircleLoader;
import com.voxlearning.utopia.service.parent.api.entity.GroupMessageConfirmInfo;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.ScoreCircleGroupContext;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.ScoreCircleGroupContextStoreInfo;
import com.voxlearning.utopia.service.parent.api.mapper.scorecircle.ScoreCircleResponse;
import com.voxlearning.utopia.service.parent.constant.GroupCircleExtType;
import com.voxlearning.utopia.service.parent.constant.GroupCircleType;
import com.voxlearning.utopia.service.parent.constant.ParentGroupMessageStatus;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwei.liao
 * @since 2018-3-30
 */
@Controller
@RequestMapping(value = "/v2/teacher/group_message/")
public class TeacherGroupMessageApiController extends AbstractTeacherApiController {


    @ImportService(interfaceClass = DPScoreCircleLoader.class)
    private DPScoreCircleLoader dpScoreCircleLoader;
    @ImportService(interfaceClass = DPGroupMessageConfirmInfoLoader.class)
    private DPGroupMessageConfirmInfoLoader dpGroupMessageConfirmInfoLoader;

    @RequestMapping(value = "list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getGroupMessageList() {
        try {
            validateRequired(REQ_GROUP_ID, "班组ID");
            validateRequest(REQ_GROUP_ID, REQ_CREATE_TIME);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        Long groupId = getRequestLong(REQ_GROUP_ID);
        Long createTime = getRequestLong(REQ_CREATE_TIME);
        //与上一次最后一条数据时间相等的数据排开。所以要-1
        Date createDate = createTime == 0 ? new Date() : new Date(createTime - 1);
        Long teacherId = getApiRequestUser().getId();
        Set<ScoreCircleGroupContext> circleContextSet = getTeacherGroupCircleContext(teacherId, groupId, createDate);
        if (CollectionUtils.isEmpty(circleContextSet)) {
            return successMessage().add(RES_CARD_LIST, Collections.EMPTY_LIST);
        }
        //排序并且取前10条数据
        //与最后一条数据时间相等的数据必须同时返回
        List<ScoreCircleGroupContext> returnContextList = new ArrayList<>();
        circleContextSet.stream()
                .sorted((o1, o2) -> o2.getCreateDate().compareTo(o1.getCreateDate()))
                .forEach(p -> {
                    if (returnContextList.size() < 10) {
                        returnContextList.add(p);
                    } else if (returnContextList.get(returnContextList.size() - 1).getCreateDate().equals(p.getCreateDate())) {
                        returnContextList.add(p);
                    }
                });
        //读取存储中的信息
        Set<String> ids = new HashSet<>();
        returnContextList.forEach(context -> ids.add(context.generateId()));
        Map<String, ScoreCircleGroupContextStoreInfo> storeInfoMap = dpScoreCircleLoader.loads(ids);
        //家长确认信息
        Set<String> confirmIds = new HashSet<>();
        storeInfoMap.values().forEach(p -> confirmIds.add(GroupMessageConfirmInfo.generateId(p.getGroupCircleType(), p.getTypeId())));
        Map<String, GroupMessageConfirmInfo> confirmInfoMap = dpGroupMessageConfirmInfoLoader.loadByIds(confirmIds);
        List<Map<String, Object>> mapList = new ArrayList<>();
        //上一条卡片的日期
        DayRange lastItemDay = null;
        if (createTime != 0) {
            lastItemDay = DayRange.newInstance(createTime);
        }
        for (ScoreCircleGroupContext context : returnContextList) {
            ScoreCircleGroupContextStoreInfo storeInfo = storeInfoMap.get(context.generateId());
            if (storeInfo == null || storeInfo.getGroupCircleType() == null) {
                continue;
            }
            ParentGroupMessageStatus status = ParentGroupMessageStatus.parse(storeInfo.getGroupCircleType().getConfirmStatus());
            if (status == ParentGroupMessageStatus.UNKNOWN) {
                continue;
            }

            Map<String, Object> map = new HashMap<>();
            map.put(RES_CARD_TYPE_ID, storeInfo.getTypeId());
            map.put(RES_CARD_CREATE_DATE, context.getCreateDate().getTime());
            //日期显示
            if (lastItemDay == null || !lastItemDay.contains(context.getCreateDate())) {
                map.put(RES_CARD_SHOW_DATE, DateUtils.dateToString(context.getCreateDate(), "MM月dd日"));
            }
            //左上角标题
            map.put(RES_CARD_TITLE, storeInfo.getGroupCircleType().getLeftTopTag());
            //家长处理状态
            GroupMessageConfirmInfo confirmInfo = confirmInfoMap.get(GroupMessageConfirmInfo.generateId(storeInfo.getGroupCircleType(), storeInfo.getTypeId()));
            if (confirmInfo != null && CollectionUtils.isNotEmpty(confirmInfo.getConfirmUserIds())) {
                map.put(RES_CARD_TOP_TAG, confirmInfo.getConfirmUserIds().size() + "位家长" + status.getTag());
            }
            //正文
            String content = storeInfo.getContent();
            if (storeInfo.getGroupCircleType() == GroupCircleType.HOMEWORK_NEW) {
                NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(storeInfo.getTypeId());
                if (newHomework != null && newHomework.getNewHomeworkType() == NewHomeworkType.OCR) {
                    Map<ObjectiveConfigType, NewHomeworkPracticeContent> newHomeworkPracticeContents = newHomework.findPracticeContents();
                    if (newHomeworkPracticeContents.containsKey(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC)) {
                        NewHomeworkPracticeContent newHomeworkPracticeContent = newHomeworkPracticeContents.get(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC);
                        String workBookName = newHomeworkPracticeContent.getWorkBookName();
                        String homeworkDetail = newHomeworkPracticeContent.getHomeworkDetail();
                        String[] bookNames = StringUtils.split(workBookName, NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_SEPARATOR);
                        String[] homeworkDetails = StringUtils.split(homeworkDetail, NewHomeworkConstants.OCR_MENTAL_ARITHMETIC_SEPARATOR);
                        List<String> bookNameList = Arrays.asList(bookNames);
                        List<String> homeworkDetailList = Arrays.asList(homeworkDetails);
                        int length = Integer.max(bookNameList.size(), homeworkDetailList.size());
                        List<String> homeworkContent = new ArrayList<>();
                        for (int i = 0; i < length; i++) {
                            homeworkContent.add(bookNameList.get(i) + "：" + homeworkDetailList.get(i));
                        }
                        content = StringUtils.join(homeworkContent, ";");
                    }
                }
            }
            map.put(RES_CARD_CONTENT, content);
            //扩展信息
            List<Map<String, Object>> extResultList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(storeInfo.getExtInfoList())) {
                storeInfo.getExtInfoList().forEach(extStoreInfo -> {
                    Map<String, Object> extMap = new HashMap<>();
                    GroupCircleExtType extType = extStoreInfo.getExtType();
                    if (extType != null) {
                        extMap.put(RES_CARD_EXT_COLOR, extStoreInfo.getExtType().getContentColor());
                        extMap.put(RES_CARD_EXT_ICON, getCdnBaseUrlStaticSharedWithSep() + extStoreInfo.getExtType().getIcon());
                    }
                    extMap.put(RES_CARD_EXT_TITLE, extStoreInfo.getContent());
                    extResultList.add(extMap);
                });
            }
            map.put(RES_CARD_EXT_INFO, extResultList);
            mapList.add(map);
            lastItemDay = DayRange.newInstance(storeInfo.getCreateDate().getTime());
        }
        return successMessage().add(RES_CARD_LIST, mapList);
    }

    private Set<ScoreCircleGroupContext> getTeacherGroupCircleContext(Long teacherId, Long groupId, Date createDate) {
        Set<ScoreCircleGroupContext> contextSet = new HashSet<>();
        //共享组
        Set<Long> sharedGroupIds = deprecatedGroupLoaderClient.loadSharedGroupIds(groupId);
        if (CollectionUtils.isEmpty(sharedGroupIds)) {
            sharedGroupIds = new HashSet<>();
        }
        //本身这个组
        sharedGroupIds.add(groupId);
        //参数
        ScoreCircleGroupContext context = new ScoreCircleGroupContext();
        context.setUserId(teacherId);
        //时间处理
        Date current = new Date();
        //最多取两个月内的数据
        Date oldestDate = DateUtils.addDays(current, -60);
        //本月
        MonthRange monthRange = MonthRange.newInstance(createDate.getTime());
        do {
            context.setCreateDate(createDate);
            for (Long id : sharedGroupIds) {
                context.setGroupId(id);
                ScoreCircleResponse circleResponse = dpScoreCircleLoader.loadGroupCircleByScoreDesc(context, 10);
                if (circleResponse.isSuccess()) {
                    circleResponse.getContextList()
                            .stream()
                            .filter(e -> e instanceof ScoreCircleGroupContext)
                            .map(e -> (ScoreCircleGroupContext) e)
                            .filter(p -> !p.getCreateDate().before(oldestDate))
                            .forEach(contextSet::add);
                }
                //忽略学科的消息中不要通用消息
                ScoreCircleResponse ignoreSubjectCircleResponse = dpScoreCircleLoader.loadGroupIgnoreSubjectCircleByScoreDesc(context, 10);
                if (ignoreSubjectCircleResponse.isSuccess()) {
                    ignoreSubjectCircleResponse.getContextList()
                            .stream()
                            .filter(e -> e instanceof ScoreCircleGroupContext)
                            .map(e -> (ScoreCircleGroupContext) e)
                            .filter(p -> p.getGroupCircleType() != GroupCircleType.COMMON)
                            .filter(p -> !p.getCreateDate().before(oldestDate))
                            .forEach(contextSet::add);
                }
            }
            //获取记录条数>=10跳出循环
            if (contextSet.size() >= 10) {
                break;
            }
            //重新计算时间
            monthRange = monthRange.previous();
            createDate = monthRange.getEndDate();
        } while (!oldestDate.after(createDate));
        return contextSet;
    }
}
