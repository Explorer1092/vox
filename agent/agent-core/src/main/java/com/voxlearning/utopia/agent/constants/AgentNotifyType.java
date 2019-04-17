/*
 *
 *  * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *  *
 *  *  Copyright 2006-2014 Vox Learning Technology, Inc. All Rights Reserved.
 *  *
 *  *  NOTICE: All information contained herein is, and remains the property of
 *  *  Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 *  *  and technical concepts contained herein are proprietary to Vox Learning
 *  *  Technology, Inc. and its suppliers and may be covered by patents, patents
 *  *  in process, and are protected by trade secret or copyright law. Dissemination
 *  *  of this information or reproduction of this material is strictly forbidden
 *  *  unless prior written permission is obtained from Vox Learning Technology, Inc.
 *
 */

package com.voxlearning.utopia.agent.constants;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 系统通知类型
 * Created by Shuai.Huan on 2014/7/21.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentNotifyType {

    ADD_NEW_GROUP(0, "创建新群组", "expire"),
    ADD_NEW_USER(1, "创建新用户", "expire"),
    SALARY_CALCULATE(2, "工资结算完毕", "expire"),
    SALARY_CONFIRM(3, "工资确认完毕", "expire"),
    DEPOSIT_CONFIRM(4, "保证金已收到", "expire"),
    ORDER_NOTICE(5, "商品购买提醒", "system"),
    ORDER_DELIVERY_NOTICE(6, "商品发货提醒", "system"),
    ORDER_REJECT_NOTICE(7, "订单拒绝", "expire"),
    TASK_DISPATCH_NOTICE(8, "任务派发通知", "expire"),
    REFUND_NOTICE(9, "退款通知", "expire"),
    BATCH_SEND_FROM_USER(99, "群发通知", "expire"),//目前唯一一类用户发的通知

    // 增加消息中心里的几个通知的类型 By Wyc 2016-08-01
    WEEKLY_REPORT(10, "周报通知", "expire"),
    MONTHLY_REPORT(11, "月报通知", "expire"),
    VISIT_SUGGEST(12, "陪访建议", "expire"),
    VISIT_REMIND(13, "陪访提醒", "expire"),
    IMPORTANT_NOTICE(14, "重要通知", "expire"),
    PLATFORM_UPDATE(15, "平台更新提醒", "expire"),
    @Deprecated
    ALTERATION_REMIND(16, "老师换班申请", "alteration_clazz"),
    ALTERATION_REMIND_NEW(25, "老师换班申请", "alteration_clazz_new"),
    // add by wangsong 20170524
    UNIFIED_EXAM_APPLY(17, "统考申请通知", "system"),
    MODIFY_DICT_SCHOOL_APPLY(18, "字典表调整通知", "system"),
    REVIEW_SCHOOL_CLUE(19, "学校鉴定通知", "system"),
    REVIEW_TEACHER_FAKE(20, "老师判假申请通知", "system"),
    MAIN_SUB_ACCOUNT_APPLY(21, "老师包班申请通知", "expire"),
    DATA_REPORT_APPLY(22, "大数据申请报告", "system"),
    NEW_REGISTER_TEACHER(23, "新注册老师", "new_teacher"),
    PENDING_AUDIT(24, "待我审批", "system"),
    PRODUCT_FEEDBACK_NOTICE(26, "产品反馈提醒", "system"),
    INTO_SCHOOL_WARNING(31, "学校连续拜访", "warning"),
    INTO_SCHOOL_REACH_WARNING(32, "进校未达标", "warning"),
    SCHOOL_INFO_MODIFY(33, "学校信息修改", "system"),
    VISIT_FEEDBACK(34,"陪访反馈", "system"),
    GROUP_MESSAGE(35,"消息中心通知", "system"),
    MOCK_EXAM_MESSAGE(36,"测评系统通知","system"),
    GR_RESOURCE_APPLY(37,"上层资源申请","system")
    ;

    @Getter private final int type;
    @Getter private final String desc;
    @Getter private final String category;  // expire：旧的消息类型  system:系统消息  new_teacher：新注册老师

    private final static Map<Integer, AgentNotifyType> ID_TYPE_MAP = Arrays.asList(values()).stream().collect(Collectors.toMap(AgentNotifyType::getType, Function.identity()));
    private final static Map<String, List<AgentNotifyType>> categoryMap = Arrays.asList(values()).stream().collect(Collectors.groupingBy(AgentNotifyType::getCategory, Collectors.toList()));

    public static AgentNotifyType fetchByType(Integer type){
        return fetchByType(type, null);
    }

    public static AgentNotifyType fetchByType(Integer type, AgentNotifyType defaultLevel){
        if(type == null){
            return defaultLevel;
        }
        return ID_TYPE_MAP.getOrDefault(type, defaultLevel);
    }

    public static List<AgentNotifyType> fetchByCategory(String category){
        if(StringUtils.isBlank(category) || !categoryMap.containsKey(category)){
            return Collections.emptyList();
        }
        return categoryMap.get(category);
    }

}
