package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
@Data
public class ExamEnAfentiQuizEidItem implements Serializable {
    // fixme serialVer
    private static final long serialVersionUID = 1392253493362382323L;

    private Integer pos;
    private String eid;
    private List<String> eks;
    private List<String> ekIds;
    private String et;

    public String getEk() {
        if (ekIds != null && ekIds.size() > 0)
            return ekIds.get(0);
        if (eks != null && eks.size() > 0)
            return eks.get(0);

        return null;
    }
}