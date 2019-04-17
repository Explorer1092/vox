package com.voxlearning.utopia.service.afenti.api.data;

import com.voxlearning.utopia.data.RankType;

import java.io.Serializable;

/**
 * @author songtao
 * @since 2017/11/28
 */
public class ReviewRank implements Serializable {
    private static final long serialVersionUID = 5717051163898726711L;
    public String unitId;
    public int rank;                                                // 关卡排序
    public int star;                                                // 获得的星星数量
    public RankType rankType;                                       // 基础关卡、总结关卡
    public boolean locked;                                        // 是否锁定
    public boolean pushed;                                          // 是否已经推送过题目
    public boolean rewarded;                                          // 是否已经推送过题目
    public String rankUser;                                         // 同学足迹
    public int rankUserNum;                                           //关卡用户数
}
