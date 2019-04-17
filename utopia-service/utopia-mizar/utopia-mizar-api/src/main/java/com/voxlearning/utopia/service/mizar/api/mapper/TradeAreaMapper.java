package com.voxlearning.utopia.service.mizar.api.mapper;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by Summer Yang on 2016/8/26.
 */
@Data
public class TradeAreaMapper implements Serializable {

    private static final long serialVersionUID = 103337861688738623L;
    private String regionName;
    private Integer regionCode;
    private List<Map<String, Object>> tradeAreaList;  // regionCode,  tradeArea
}
