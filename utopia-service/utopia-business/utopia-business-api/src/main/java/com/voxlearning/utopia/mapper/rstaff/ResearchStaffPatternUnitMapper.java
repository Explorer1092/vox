package com.voxlearning.utopia.mapper.rstaff;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Changyuan on 2015/1/13.
 * 教研员题型页面中单行mapper
 */
@Data
public class ResearchStaffPatternUnitMapper implements Serializable{
    private static final long serialVersionUID = -6169234018468204964L;

    String name;

    private Map<String, ResearchStaffPatternDetailMapper> patternMap;
}
