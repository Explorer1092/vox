package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class EksTypeListContent implements Serializable {

    private static final long serialVersionUID = 1711687667848115216L;

    /** 未掌握EK列表 */
    private List<UserEkContent> ekLevelZeroList;
    /** 基本掌握EK列表 */
    private List<UserEkContent> ekLevelOneList;
    /** 掌握EK列表 */
    private List<UserEkContent> ekLevelTwoList;
    /** 超出课本EK列表 */
    private List<UserEkContent> ekAboveLevelList;
    /** 降权EK列表，当天作对的知识点 */
    private List<UserEkContent> ekDownWeightList;

    /** 未掌握EK列表所有EK权重之和 */
    private double ekLevelZeroWeightSum;
    /** 基本掌握EK列表所有EK权重之和 */
    private double ekLevelOneWeightSum;
    /** 掌握EK列表所有EK权重之和 */
    private double ekLevelTwoWeightSum;
    /** 超出课本EK列表所有EK权重之和 */
    private double ekAboveLevelWeightSum;
    /** 除权EK列表所有EK权重之和 */
    private double ekDownWeightSum;

    public EksTypeListContent() {
        ekLevelZeroList = new ArrayList<>();
        ekLevelOneList = new ArrayList<>();
        ekLevelTwoList = new ArrayList<>();
        ekAboveLevelList = new ArrayList<>();
        ekDownWeightList = new ArrayList<>();

        ekLevelZeroWeightSum = 0.0;
        ekLevelOneWeightSum = 0.0;
        ekLevelTwoWeightSum = 0.0;
        ekAboveLevelWeightSum = 0.0;
        ekDownWeightSum = 0.0;
    }

    public List<String> getEks(List<UserEkContent> ekContentList) {

        if (ekContentList == null) return null;
        List<String> list = new ArrayList<>();

        for (UserEkContent userEkContent : ekContentList) {
            list.add(userEkContent.getEk());
        }

        return list;
    }

    public Integer getValidEkNum() {
        return ekLevelZeroList.size() + ekLevelOneList.size() + ekLevelTwoList.size() + ekAboveLevelList.size() + ekDownWeightList.size();
    }

    public boolean isAllListEmpty() {
        return (ekLevelZeroList.isEmpty() && ekLevelOneList.isEmpty()
                && ekLevelTwoList.isEmpty() && ekAboveLevelList.isEmpty() && ekDownWeightList.isEmpty());
    }

    public boolean isEkLevelZeroListNull() {
        return (ekLevelZeroList == null);
    }

    public boolean isEkLevelOneListNull() {
        return (ekLevelOneList == null);
    }

    public boolean isEkLevelTwoListNull() {
        return (ekLevelTwoList == null);
    }

    public boolean isAboveLevelListNull() {
        return (ekAboveLevelList == null);
    }

    public boolean isDownWeightListNull() {
        return (ekDownWeightList == null);
    }
}
