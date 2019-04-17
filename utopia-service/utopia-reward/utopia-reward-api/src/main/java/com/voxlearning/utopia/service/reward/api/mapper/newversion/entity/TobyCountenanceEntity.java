package com.voxlearning.utopia.service.reward.api.mapper.newversion.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class TobyCountenanceEntity implements Serializable {
    private Long id;
    private String url;
    private String name;
    private String tag;
    private Integer publicGoodsTimes;
    private Integer status;
    List<TobyDressCVOption> cvOption;
    private Integer spendType;
    private String remark;
    private String type = "countenance";
    private Boolean cvAuthority;
    private String unallowCVTip;
}
