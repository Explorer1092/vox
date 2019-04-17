package com.voxlearning.utopia.service.ai.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class ScoreSimpleInfo implements Serializable {
    private String bookId;
    private String bookName;
    private long finishedNum;
    private int totalNum;
    private int recentlyScore = -1;
    private String recentlyUnitId;
}
