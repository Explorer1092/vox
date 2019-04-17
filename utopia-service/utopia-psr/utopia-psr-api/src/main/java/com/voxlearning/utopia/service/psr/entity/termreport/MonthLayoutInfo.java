package com.voxlearning.utopia.service.psr.entity.termreport;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by 17ZY-HPYKFD2 on 2016/10/31.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class MonthLayoutInfo implements Serializable {
    private static final long serialVersionUID = 3552592815349294543L;
    private String month;
    private Integer layout_count; //老师布置了多少次
}
