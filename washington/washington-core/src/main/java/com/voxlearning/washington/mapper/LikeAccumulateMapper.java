package com.voxlearning.washington.mapper;

import com.voxlearning.utopia.api.constant.FlowerSourceType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class LikeAccumulateMapper implements Serializable {

    private static final long serialVersionUID = 7949786688341167191L;

    private String clazzName;

    private Long num;

    private Date create;

    private String flowerSourceType;
}
