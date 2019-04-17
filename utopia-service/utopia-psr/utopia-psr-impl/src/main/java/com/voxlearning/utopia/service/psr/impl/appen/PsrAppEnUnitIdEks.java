package com.voxlearning.utopia.service.psr.impl.appen;

import com.voxlearning.utopia.service.psr.entity.UserAppEnContent;
import com.voxlearning.utopia.service.psr.entity.UserAppEnEkItem;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class PsrAppEnUnitIdEks {
    @Getter private Map<Long, List<String>> unitIdEks; // 本课本的 单元id 对应 ek信息
    @Getter private List<Map.Entry<Long, List<String>>> unitListSort; // 按照单元id排序
    @Getter private List<Long> rightUnitIds; // 符合条件的单元id列表,预习单元、当前单元及之前单元id列表
    @Getter private Long curUnitId;
    @Getter private Long previewUnitId;
    @Getter private Map<String, UserAppEnEkItem> rightEksMap;
    @Getter @Setter private List<String> todayCorrectEks;  // 当天做过的知识点,并且有状态升级不在推荐的

    public PsrAppEnUnitIdEks() {
        //unitIdEks = new HashMap<>();
        unitIdEks = new LinkedHashMap<>();
        rightUnitIds = new ArrayList<>();
        //rightEksMap = new HashMap<>();
        rightEksMap = new LinkedHashMap<>();
        todayCorrectEks = new ArrayList<>();

        previewUnitId = -1L;
        curUnitId = -1L;

    }

    /*
     * 当符合条件的知识点不够时，则扩充知识点,向 预习单元以后的单元知识点扩充
     */
    public Map<String, UserAppEnEkItem> getMoreUnitIdEks(int eCount) {

        for(Map.Entry<Long, List<String>> entry : unitListSort) {
            if (rightUnitIds.contains(entry.getKey()))
                continue;

            for (String ek : entry.getValue()) {
                if (rightEksMap.containsKey(ek) || todayCorrectEks.contains(ek))
                    continue;
                UserAppEnEkItem item = new UserAppEnEkItem();
                item.setEk(ek);

                rightEksMap.put(ek, item);

                if (rightEksMap.size() >= eCount)
                    return rightEksMap;
            }
        }
        return rightEksMap;
    }

    /*
     * 获取当前单元[及当前考察单元] 及 以前单元
     * 当前单元, 若输入单元id,则为当前单元, 没有输入单元id 则寻找当前单元, 1> 无用户信息 默认第一单元为当前单元, 2> 根据用户信息选取当前单元
     * 预习单元, 当前单元的下一单元, 若下一个单元没有知识点 在找下一个单元,直到最后一个单元
     * 新教材结构 unitid 需要转换为GroupId 在调用本接口
     * 微调当前单元 - add - Chaoli.Li - 2015-03-31
     * fix 某个用户当天在该教材的当前单元下做的题量超多,导致当前单元的知识点都被过滤了
     * 此种情况下 按离线数据推算出的当前单元与实际不符合
     * 所以 把当前单元往后面推一个单元变成练习单元,保证有新的知识点进入候选队列,如果是最后一个单元就完蛋了 换教材或者外面补题吧
     */
    public Map<String, UserAppEnEkItem> getUnitIdEks(Long unitId, UserAppEnContent userAppEnContent, Map<Long, List<String>> unitSentences) {
        // 获取单元Id列表,已经排序
        unitIdEks = unitSentences;
        if (unitIdEks == null || unitIdEks.size() <= 0)
            return null;

        // 按单元排序后的list: 需要确认entrySet()方法返回的元素顺序保持不变lianhua.li
        unitListSort = new LinkedList<>(unitIdEks.entrySet());

        boolean isLearned = false;

        // 如果没有指定当前单元Id(-1 为没指定) 则开始寻找 用户信息里面的最大单元 为当前单元
        if (unitId < 0L) {
            if (userAppEnContent.isEkMapNull() || userAppEnContent.getEkMap().size() <= 0) {
                unitId = unitListSort.get(0).getKey();
                isLearned = false;
            } else {
                // 遍历该book下的每个单元寻找 [D-S]/[E-S] >= 60% 的最大单元 为当前单元
                List<Long> maxUnitListSort = new LinkedList<>();

                Integer statusECount = 0;
                Integer statusDCount = 0;
                for (Map.Entry<Long,List<String>> entry : unitIdEks.entrySet()) {
                    statusECount = 0;
                    statusDCount = 0;

                    for (String uEk : entry.getValue()) {
                        statusECount++;
                        if (userAppEnContent.getEkMap().containsKey(uEk)
                             && userAppEnContent.getEkMap().get(uEk).getStatus() != 'E')
                            statusDCount++;
                        else if (todayCorrectEks.contains(uEk))  // 练习单元,当天做题量较多,练习单元可能为当前单元,也可能为当前单元之后的某个单元
                            statusDCount++;
                    }

                    // todo 0.6 可配置
                    if (statusECount > 0 && ((statusDCount + 0.0) / (statusECount + 0.0)) >= 0.6)
                        maxUnitListSort.add(entry.getKey());
                }

                if (maxUnitListSort.size() > 0)
                    unitId = maxUnitListSort.get(maxUnitListSort.size() - 1);
                else
                    unitId = unitListSort.get(0).getKey();  // 默认第一单元为当前单元

                isLearned = true;
            }
        }

        boolean isPreviewUnitId = false;

        // 寻找当前单元id之前的单元, 已经排序-升序
        for (Map.Entry<Long, List<String>> entry : unitListSort) {
            if (entry.getKey().equals(unitId)) {
                isPreviewUnitId = true;
                continue;
            }
            if (isPreviewUnitId) {
                // 没有指定的unitd 且没有练习数据,所以当前单元就是新单元,不设置预习单元
                if (!isLearned)
                    break;
                previewUnitId = entry.getKey();
                if (unitIdEks.containsKey(previewUnitId)) {
                    List<String> eks = unitIdEks.get(previewUnitId);
                    // 有新知识点 则为复习单元
                    if (eks != null && eks.size() > 0)
                        break;
                }
                // 本单元没有新知识点 则为复习单元，则继续寻找下一个单元
                continue;
            }

            // 添加当前单元之前单元
            rightUnitIds.add(entry.getKey());
        }

        // 添加当前单元
        rightUnitIds.add(unitId);
        curUnitId = unitId;
        // 添加预习单元
        if (previewUnitId > -1L)
            rightUnitIds.add(previewUnitId);

        for (Long unit : rightUnitIds) {
            if (unitIdEks.containsKey(unit)) {
                for (String ekTmp : unitIdEks.get(unit)) {
                    UserAppEnEkItem item = new UserAppEnEkItem();
                    item.setEk(ekTmp);

                    rightEksMap.put(ekTmp, item);
                }
            }
        }

        // 合并learningProfile 知识点
        if (!userAppEnContent.isEkMapNull() && userAppEnContent.getEkMap().size() > 0) {
            for (Map.Entry<String, UserAppEnEkItem> entry : userAppEnContent.getEkMap().entrySet()) {
                if (rightEksMap.containsKey(entry.getKey()))
                    rightEksMap.put(entry.getKey(), userAppEnContent.getEkMap().get(entry.getKey()));
            }
        }

        return rightEksMap;
    }
}

