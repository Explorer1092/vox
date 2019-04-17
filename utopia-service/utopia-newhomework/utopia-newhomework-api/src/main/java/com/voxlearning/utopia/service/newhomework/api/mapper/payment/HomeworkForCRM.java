package com.voxlearning.utopia.service.newhomework.api.mapper.payment;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xuesong.zhang
 * @since 2017/4/26
 */
@Setter
@Getter
public class HomeworkForCRM implements Serializable {

    private static final long serialVersionUID = 3573802409118190917L;

    private String id;
    private String subject;
    private String week;
    private String clazzLevel;
    private String questionIds;
    private String betweenTime;
    private String createAt;
}
