package com.voxlearning.utopia.mapper.rstaff;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Changyuan on 2015/1/15.
 */
@Data
public class ResearchStaffUnitWeakPointUnitMapper implements Serializable {

    private static final long serialVersionUID = -5197752587406457244L;

    private int clazzLevel;

    private List<ResearchStaffUnitWeakPointBookMapper> bookList = new ArrayList<>();


}
