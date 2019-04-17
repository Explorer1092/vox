package com.voxlearning.utopia.service.reward.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 公益活动捐赠记录Mapper
 */
@Getter
@Setter
public class PGRecordMapper implements Serializable {

    private static final long serialVersionUID = 3151106552699629700L;

    private String comment;
    private Long price;
    private Date t;
    private String time;
    private String type;

}
