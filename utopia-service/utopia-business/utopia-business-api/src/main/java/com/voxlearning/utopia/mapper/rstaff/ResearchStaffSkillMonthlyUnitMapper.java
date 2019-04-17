package com.voxlearning.utopia.mapper.rstaff;

import lombok.Data;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Changyuan on 2015/1/16.
 */
@Data
public class ResearchStaffSkillMonthlyUnitMapper implements Serializable {

    private static final long serialVersionUID = 422296060203393663L;
    private List<String> names = new ArrayList<>();

    private Map<String, List<Double>> monthlyRate = new LinkedHashMap<>();

    private Map<String, List<Integer>> monthlySum = new LinkedHashMap<>();
}
