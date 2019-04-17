/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.nekketsu.parkour.net.types.response;

import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.utopia.entity.flashmsg.AbstractMessageBean;
import com.voxlearning.utopia.service.nekketsu.parkour.net.types.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;


/**
 * 关卡信息。包括关卡单词，我当前获得的拼图，我的本关最好成绩，本关星星数，本关排行榜以及随机挑战候选人和同班挑战候选人
 */
public class GetStageInfoResponse extends AbstractMessageBean implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * 返回结果时表示是否操作成功
     */
    public boolean success;
    /**
     * 关卡id
     */
    public int stageId;
    /**
     * 随机挑战候选人
     */
    public Collection<ParkourAi> randomCandidate = new ArrayList<>();
    /**
     * 同班同学挑战候选人
     */
    public Collection<ParkourAi> classmateCandidate = new ArrayList<>();
    /**
     * 本关ai
     */
    public ParkourAi stageAi;
    /**
     * 全市排名前10
     */
    public StageRankStruct cityRank;
    /**
     * 全省排名前10
     */
    public StageRankStruct provinceRank;
    /**
     * 全国排名前10
     */
    public StageRankStruct nationalRank;
    /**  */
    public String topic = "";
    /**
     * 关卡全部词汇
     */
    public Collection<StageWord> wordList = new ArrayList<>();
    /**
     * 我的最快通关时间，毫秒数
     */
    public int personalBest;
    /**
     * 获得星星数
     */
    public int starBest;
    /**
     * 本关已获得的拼图
     */
    public Collection<ParkourPuzzle> achievedPuzzle = new ArrayList<>();
    /**
     * 障碍数量
     */
    public int barricadeCount;
    /**
     * 散落在赛道上的金币数
     */
    public int pickCountCount;
    /**
     * 最大答错题数。如果答错题目超过这个数量，本关直接失败
     */
    public int failErrorCount;
    /**
     * 赛道长度
     */
    public int distance;
    /**
     * 长度为3的数组，对应1星2星3星的经验奖励数
     */
    public Collection<Integer> exp = new ArrayList<>();
    /**
     * 初次获得3星时奖励的金币
     */
    public int stageCoinBonus;
    /**
     * 用户在未付费时获得的单词，用户绘制拼图时使用
     */
    public Collection<WordToExchange> wordToExchange = new ArrayList<>();


    /**
     * 将当前对象转换成返回需要的字符串
     *
     * @return 返回字符串，通常是JSON
     */
    public String toResponse() {
        return JsonStringSerializer.getInstance().serialize(this);
    }
}