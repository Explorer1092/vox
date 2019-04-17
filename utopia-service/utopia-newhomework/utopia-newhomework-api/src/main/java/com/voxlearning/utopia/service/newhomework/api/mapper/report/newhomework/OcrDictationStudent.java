package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Comparator;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/1/25
 * \* Time: 6:49 PM
 * \* Description:
 * \
 */
@Getter
@Setter
public class OcrDictationStudent  implements Serializable{
    private static final long serialVersionUID = -7535944971785635626L;
    private Long userId;
    private String userName;
    private int score;
    private int identifyCount;
    private int errorCount;
    private boolean manualCorrect;
    private boolean finished;
    private String resultUrl;

    public static Comparator<OcrDictationStudent> comparator = (Comparator<OcrDictationStudent>) (o1, o2) -> {
        int compare = Boolean.compare(o2.isFinished(), o1.isFinished());
        if (compare != 0) {
            return compare;
        }
        return Integer.compare(o2.getScore(), o1.getScore());
    };
}
