package com.voxlearning.utopia.service.newhomework.api.mapper.report.pc;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class KnowledgePointDetail implements Serializable {
    private static final long serialVersionUID = 6494070071802284231L;
    private String knowledgePointId;
    private String name;
    private int totalNum;
    private int rightNum;
    private int percentage;
    private int questionNum = 1;
}
