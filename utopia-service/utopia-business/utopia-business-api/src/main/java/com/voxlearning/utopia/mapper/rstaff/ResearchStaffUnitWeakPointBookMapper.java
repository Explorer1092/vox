package com.voxlearning.utopia.mapper.rstaff;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Changyuan on 2015/1/22.
 */
@Data
public class ResearchStaffUnitWeakPointBookMapper implements Serializable {

    private static final long serialVersionUID = -2254845748524889291L;

    private String press;

    // 2015.1.26 changyuan.liu 合并各单元薄弱知识点
    // private List<String> weakPointTags = new ArrayList<>();
    private String weakPoints;
}
