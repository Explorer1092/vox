package com.voxlearning.utopia.service.afenti.api.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Summer on 2017/8/18.
 */
@Data
public class SectionInfo implements Serializable {

    private static final long serialVersionUID = -7128493002359439613L;

    public Integer sectionRankNo;           // 关卡序号
    public String sectionName;          // 关卡名称
    public List<String> studyTargets;   // 学习目标列表
    public List<String> kps;            // 知识点名称列表

}
