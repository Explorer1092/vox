package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xinxin
 * @since 10/8/2016
 */
@Getter
@Setter
public class StudentLikeLogPerDayMapper implements Serializable {
    private static final long serialVersionUID = -7964408254878235363L;

    private String date;
    private Integer count;
}
