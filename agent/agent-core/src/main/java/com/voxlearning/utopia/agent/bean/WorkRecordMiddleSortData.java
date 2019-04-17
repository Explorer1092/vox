package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Created by dell on 2017/3/2.
 */
@Getter
@Setter
public class WorkRecordMiddleSortData implements Serializable {
    private Collection<WorkRecordListData> workRecordListData;
    //排序日期
    private String sortDate;
    //工作量
    private double workload;
}
