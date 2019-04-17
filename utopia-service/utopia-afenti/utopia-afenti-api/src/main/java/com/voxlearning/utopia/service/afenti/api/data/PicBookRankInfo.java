package com.voxlearning.utopia.service.afenti.api.data;

import com.voxlearning.utopia.service.afenti.api.constant.PicBookRankCategory;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Summer on 2018/4/8
 */
@Getter
@Setter
public class PicBookRankInfo implements Serializable {

    private static final long serialVersionUID = -4482485992651042883L;

    private Long studentId;
    private String studentName;
    private String img;
    private String className;
    private String schoolName;
    private Integer readCount;
    private Integer wordCount;
    private Integer rank;

    static public PicBookRankInfo newInstanceForRank(Long studentId, int count, int rank, PicBookRankCategory rankType) {
        PicBookRankInfo rankInfo = new PicBookRankInfo();
        rankInfo.setStudentId(studentId);
        if (rankType == PicBookRankCategory.READ) {
            rankInfo.setReadCount(count);
        } else if (rankType == PicBookRankCategory.WORD) {
            rankInfo.setWordCount(count);
        }
        rankInfo.setRank(rank);
        return rankInfo;
    }
}
