package com.voxlearning.utopia.service.ai.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class TargetProductADConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private String beImage;
    private String adProductId;
    private Long clazzId;
    private String type;
    private String path;
    private Date endDate;
    private Date beginDate;
}
