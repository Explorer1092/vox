package com.voxlearning.washington.mapper;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class LikeExchangeMapper implements Serializable {

    private static final long serialVersionUID = -4627767456723675663L;


    private String clazzName;

    private Long num;

    private Date create;

}
