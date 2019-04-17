package com.voxlearning.utopia.service.afenti.api.data;

import com.voxlearning.utopia.data.RankType;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author vincent
 * @since 14-7-2
 */
public class UnitRank implements Serializable {
    private static final long serialVersionUID = 995963736289641030L;

    public int rank;                                                // 关卡排序
    public int star;                                                // 获得的星星数量
    public RankType rankType;                                       // 基础关卡、总结关卡
    public boolean isLocked;                                        // 是否锁定
    public List<Map<String, Object>> footprints;                    // 同学足迹
    public boolean pushed;                                          // 是否已经推送过题目

    // to be deleted
    public Map<String, Map<String, Integer>> knowledgeMap;          // 知识点掌握情况
    public int baseStar;                                            // 获得的星星数量
    public String baseExamUrl;
    public String examHistoryUrl; // no use
    public String baseKnowledgeMapUrl;
    public int attachStar; // no use
}
