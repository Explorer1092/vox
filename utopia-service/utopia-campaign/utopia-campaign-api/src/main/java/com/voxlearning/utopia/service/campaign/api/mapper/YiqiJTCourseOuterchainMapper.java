package com.voxlearning.utopia.service.campaign.api.mapper;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class YiqiJTCourseOuterchainMapper implements java.io.Serializable{
    private static final long serialVersionUID = -1265676885288478120L;

    private Long id;
    private Long courseId;
    private String outerchain;
    private String outerchainUrl;
}
