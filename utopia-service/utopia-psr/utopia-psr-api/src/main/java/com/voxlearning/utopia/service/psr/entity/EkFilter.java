package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.util.*;

@Data
public class EkFilter {

    private List<String> eksPointRefs;
    //private Map<String, List<Map.Entry<Double, String>>> ekTypeMap;
    private EksTypeListContent eksTypeListContent;

}

