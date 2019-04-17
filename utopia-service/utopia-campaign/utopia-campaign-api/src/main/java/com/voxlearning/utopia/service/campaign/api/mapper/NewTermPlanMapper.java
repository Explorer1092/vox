package com.voxlearning.utopia.service.campaign.api.mapper;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = {"clazzId"})
public class NewTermPlanMapper implements java.io.Serializable {

    private static final long serialVersionUID = -6729495077480613896L;

    private Long clazzId;
    private String clazzName;
    private Integer size;
    private Integer assignSize;

}
