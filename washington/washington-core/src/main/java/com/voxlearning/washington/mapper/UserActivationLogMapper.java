package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xinxin
 * @since 12/19/17
 */
@Getter
@Setter
public class UserActivationLogMapper implements Serializable {
    private static final long serialVersionUID = 4789956724573163509L;

    private String time;
    private String title;
    private Long value;
    private Date createDatetime;
}
