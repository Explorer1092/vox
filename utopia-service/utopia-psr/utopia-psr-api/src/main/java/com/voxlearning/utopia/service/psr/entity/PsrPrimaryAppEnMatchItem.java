package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.*;

@Data
public class PsrPrimaryAppEnMatchItem implements Serializable {
    private static final long serialVersionUID = 3960030904289223687L;

    public PsrPrimaryAppEnMatchItem() {
        algov = "appenmatch";
        matchEids = new ArrayList<>();
        matchEidsMap = new LinkedHashMap<>();
    }

    public void setMatchItem(PsrPrimaryAppEnItem item) {
        this.eid    = item.getEid();
        this.eType  = item.getEType();
        this.status = item.getStatus();
        this.weight = item.getWeight();
        this.algov  = item.getAlgov();
    }

    // 题目ID
    private String eid;
    // 推题类型
    private String eType;
    // 该EID对应的状态：E D C B A S五种
    private Character status;
    // 计算得出的权重
    private double weight;
    // alogv 算法类型
    private String algov;
    // 配错项列表 - eks, fixme 请使用 matchEidsMap 代替
    private List<String> matchEids;

    // 记录配错项对应的配错算法, fixme matchEidsMap 和 matchEids 重复, 优化时记得删除matchEids
    private Map<String/*eid*/,String/*algo*/> matchEidsMap;
}
