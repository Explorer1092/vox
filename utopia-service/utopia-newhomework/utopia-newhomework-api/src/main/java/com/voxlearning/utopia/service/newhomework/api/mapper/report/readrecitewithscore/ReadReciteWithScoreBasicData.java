package com.voxlearning.utopia.service.newhomework.api.mapper.report.readrecitewithscore;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;


@Getter
@Setter
public class ReadReciteWithScoreBasicData implements Serializable {
    private static final long serialVersionUID = -6555125527768846529L;
    private String lessonName;
    private String questionBoxId;
    private boolean standard;
    private List<String> voices = new LinkedList<>();
    private List<ParagraphDetailed> paragraphDetails = new LinkedList<>();
}
