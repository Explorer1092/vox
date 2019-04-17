package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class MathEkEkContent implements Serializable {

    private static final long serialVersionUID = -4171479558711291239L;

    private String ek;
    private String ekBase;

    public MathEkEkContent() {
        ek = "";
        ekBase = "";
    }
}
