package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class EkToEidContent implements Serializable {

    private static final long serialVersionUID = 0L;

    /** 知识点 */
    private String ek;
    private List<EidItem> eidList;

    public boolean isEidListNull() {
        return (eidList == null);
    }
}
