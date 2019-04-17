package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * dataTable的内容格式
 * Created by yaguang.wang on 2016/10/14.
 */
@Getter
@Setter
@NoArgsConstructor
public class DataTableInfo implements Serializable {
    private static final long serialVersionUID = 7957391996235373397L;
    private Boolean success;
    private String info;
    private Integer draw;
    private Integer rowCount;
    private Integer recordsTotal;
    private Integer recordsFiltered;
    private List<List<Object>> data;
}
