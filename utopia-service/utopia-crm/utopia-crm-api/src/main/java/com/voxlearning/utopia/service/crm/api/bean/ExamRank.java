package com.voxlearning.utopia.service.crm.api.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by yaguang.wang
 * on 2017/10/26.
 */
@Getter
@Setter
@NoArgsConstructor
public class ExamRank {
    /**
     *  等级名称
     */
    private String rankName;
    /**
     *  最低线
     */
    private Integer bottom;
    /**
     *  最高线
     */
    private Integer top;
}