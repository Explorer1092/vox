package com.voxlearning.utopia.service.business.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class ChipsRedPack implements Serializable {
    private static final long serialVersionUID = -1723307905580907849L;
    private Long userId;
    private Integer amount;//单位分
    private String productId;
    private String productName;
}
