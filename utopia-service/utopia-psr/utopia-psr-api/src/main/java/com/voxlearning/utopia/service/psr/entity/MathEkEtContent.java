package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class MathEkEtContent implements Serializable {

    private static final long serialVersionUID = -2172275133709475751L;
    private String ek;
    private Map<Integer, MathEtTime> etMap;

    public MathEkEtContent() {
        etMap = new HashMap<>();
    }
}

