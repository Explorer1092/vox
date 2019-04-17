package com.voxlearning.utopia.service.reward.mapper;

import com.voxlearning.utopia.service.reward.constant.RewardDisplayTerminal;

import java.io.Serializable;
import java.util.List;

/**
 * Load reward product context.
 *
 * @author Xiaohai Zhang
 * @serial
 * @since Dec 4, 2014
 */
public class LoadRewardProductContext implements Serializable {
    private static final long serialVersionUID = 1781044376044949007L;

    public Long categoryId;
    public List<Long> categoryIds;
    public List<Long> tags;             // 批量查 tags, 取所有tag的并集
    public Long oneLevelTagId;          // in case of loadPage=tag
    public Long twoLevelTagId;
    public String orderBy;
    public String upDown;
    public int pageNumber;              // starts with 0
    public int pageSize;
    public boolean canExchangeFlag;
    public String loadPage;             // 'all' or 'tag'
    public boolean teacherLevelFlag;
    public boolean ambassadorLevelFlag;
    public Integer ambassadorLevel;     //大使级别
    public boolean nextLevelFlag;       // 下一等级可兑换   老师
    public RewardDisplayTerminal terminal; // 展示端
    public String productType;
    public boolean showAffordable;// 负担得起的选项
    public Boolean twoLevelTagOnly;//当一级标签为0或者空的时候，仍然取二级标签

}
