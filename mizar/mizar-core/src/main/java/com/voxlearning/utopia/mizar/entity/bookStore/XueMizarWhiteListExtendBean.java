package com.voxlearning.utopia.mizar.entity.bookStore;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class XueMizarWhiteListExtendBean implements Serializable {

    private static final long serialVersionUID = -6859218297977602327L;
    private Long id;
    private Integer type;
    private String content;
    private String remark;
    private Integer bookStoreNum;
    private Date operationTime;
    private String operationUserName;
    private Date createDateTime;
    private Date updateDateTime;
    private String createMizarUserId;
    private String updateMizarUserId;
    private Boolean disable;

}
