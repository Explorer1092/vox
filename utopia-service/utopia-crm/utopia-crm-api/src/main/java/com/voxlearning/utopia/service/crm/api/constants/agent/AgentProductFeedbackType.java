package com.voxlearning.utopia.service.crm.api.constants.agent;

import com.voxlearning.alps.core.util.CollectionUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 产品反馈类型
 *
 * @author song.wang
 * @date 2017/2/21
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentProductFeedbackType {

    HOMEWORK_PRODUCT(1, "小学作业产品"),
    PLATFORM_PRODUCT(2, "平台类"),
    MIDDLE_MATH(3, "中学作业产品"),
    SELF_STUDY(4, "自学与增值"),
    OPERATION(5, "运营活动"),
    BOOK_CONTENT_ADJUSTMENT(6, "教材内容调整"),
    ADD_BOOK(7, "新增教材教辅"),
    LIVE_COMMERCIALIZE(10, "直播商业化"),
    PHEGDA_MEGREZ_TOOL(9, "天玑天权工具"),
    AGENT_MATERIALS(11, "市场物料"),
    MATERIAL_COLLECTION(12, "用户口碑素材收集"),
    OTHERS(8, "其他");

    @Getter
    private final int type;
    @Getter
    private final String desc;

    private final static Map<Integer, AgentProductFeedbackType> feedbackTypeMap = new HashMap<>();
    private final static Map<Integer, List<AgentProductFeedbackType>> feedbackByClazzMap = new HashMap<>();

    static {
        for (AgentProductFeedbackType type : AgentProductFeedbackType.values()) {
            feedbackTypeMap.put(type.getType(), type);
        }
        List<AgentProductFeedbackType> type1List = new ArrayList<>();
        type1List.add(HOMEWORK_PRODUCT);
        type1List.add(PLATFORM_PRODUCT);
        type1List.add(MIDDLE_MATH);
        type1List.add(SELF_STUDY);
        type1List.add(OPERATION);
        type1List.add(PHEGDA_MEGREZ_TOOL);
        type1List.add(OTHERS);
        type1List.add(LIVE_COMMERCIALIZE);
        type1List.add(AGENT_MATERIALS);
        type1List.add(MATERIAL_COLLECTION);
        List<AgentProductFeedbackType> type2List = new ArrayList<>();
        type2List.add(BOOK_CONTENT_ADJUSTMENT);
        List<AgentProductFeedbackType> type3List = new ArrayList<>();
        type3List.add(ADD_BOOK);
        feedbackByClazzMap.put(1, type1List);
        feedbackByClazzMap.put(2, type2List);
        feedbackByClazzMap.put(3, type3List);
    }

    public static AgentProductFeedbackType of(Integer type) {
        return feedbackTypeMap.get(type);
    }

    public static List<AgentProductFeedbackType> ofClazz(Integer clazz) {
        return feedbackByClazzMap.get(clazz);
    }

    public static Boolean isClazz(Integer clazz, AgentProductFeedbackType type) {
        if (type == null || CollectionUtils.isEmpty(ofClazz(clazz))) {
            return Boolean.FALSE;
        }
        return ofClazz(clazz).contains(type);
    }
}
