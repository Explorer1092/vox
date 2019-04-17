package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class EkToNewEidContent implements Serializable {

    private static final long serialVersionUID = 0L;

    /** 知识点 */
    private String ek;
    /** 该知识点对应的新题 列表 */
    private List<String> eidList;

    public boolean isEidListNull() {
        return (eidList == null);
    }
}
