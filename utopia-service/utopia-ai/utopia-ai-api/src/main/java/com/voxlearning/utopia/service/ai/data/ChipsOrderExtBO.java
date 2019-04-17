package com.voxlearning.utopia.service.ai.data;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ChipsOrderExtBO implements Serializable {
    private static final long serialVersionUID = 0L;
    private String orderId;
    private Long userId;
    private String groupCode;
    private Boolean repeatSponsor;
    private Boolean groupSuccess;
    private Date createDate;
}
