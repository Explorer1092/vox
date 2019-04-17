package com.voxlearning.utopia.service.afenti.api.data;

import com.voxlearning.utopia.service.afenti.api.constant.UnitRankType;

import java.io.Serializable;

/**
 * @author vincent
 * @since 14-7-2
 */
public class BookUnit implements Serializable {
    private static final long serialVersionUID = 5220656640333600509L;

    public String bookId;                     // 教材ID
    public String unitId;                     // 单元ID
    public int unitRank;                     // 单元排序
    public int acquiredStarCount;            // 已经获得的星星数量
    public int totalStarCount;               // 一共可以获得多少颗星
    public int acquiredStarRankCount;        // 已经获得星星的小关卡数量
    public int totalRankCount;               // 小关卡数量
    public int footprintCount;               // 最后足迹在该单元的同校学生数量
    public UnitRankType unitRankType;        // 单元关卡类型普通关卡和终极关卡
    public boolean locked;                   // 是否锁定
    public String openDate;                  // 文案

    // to be deleted
    public int unitStar;                     // 单元总的星级评定，根据单元所有关卡获星总数来计算
    public int unitRankStarCount;           // 单元所有关卡获星总数
    public int errorBookKeyCount;           // 单元所有关卡获得钥匙总数
    public int smallRankCount;              // 小关卡数
    public int integral;                     // 单元所有关卡获得积分和连对积分总数
    public boolean isLocked;                // 是否锁定
    public String rankUrl;                  // 根据单元获取关卡数据接口url
    public String unitName;                  // 单元名称
}
