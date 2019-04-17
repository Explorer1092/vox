package com.voxlearning.utopia.mapper.rstaff;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.data.SchoolYear;
import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Changyuan on 2015/1/7.
 */
@Data
public class ResearchStaffBookPaperMapper implements Serializable, Comparable<ResearchStaffBookPaperMapper> {

    private static final long serialVersionUID = -8081126326495613828L;

    private Long bookId;

    private String bookName;

    private Set<String> examPaperIds = new HashSet<>();

    private boolean isExamPaperDataFilled;

    private Integer termType;

    private int clazzLevel;

    public String examPaperIdsStr(){
        return StringUtils.join(examPaperIds, ",");
    }


    @Override
    public int compareTo(ResearchStaffBookPaperMapper o) {
        int curTermType = SchoolYear.newInstance().currentTerm().getKey();
        int ret = Math.abs(termType - curTermType) - Math.abs(o.termType - curTermType);
        if (ret == 0) {
            ret = this.clazzLevel - o.getClazzLevel();
            if (ret == 0)
                ret = bookId.compareTo(o.getBookId());
        }
        return ret;
    }
}
