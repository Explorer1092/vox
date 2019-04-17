package com.voxlearning.utopia.service.psr.impl.dao;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.psr.entity.AboveLevelBookUnitEidsNewMath;
import com.voxlearning.utopia.service.psr.impl.persistence.AboveLevelBookUnitEidsNewMathPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/22.
 */
@Named
public class PsrAboveLevelBookEidsNewMathDao extends SpringContainerSupport {
    @Inject private AboveLevelBookUnitEidsNewMathPersistence aboveLevelBookUnitEidsNewMathPersistence;

    public List<String> findBookEidsByBookId(String bookId) {
        return getEidsByBookUnitEids(findUnitsEidsByBookId(bookId));
    }

    public AboveLevelBookUnitEidsNewMath findUnitsEidsByBookId(String bookId) {
        if (bookId == null) {
            AboveLevelBookUnitEidsNewMath inst = new AboveLevelBookUnitEidsNewMath();
            inst.setUnits(new HashMap<>());
            return inst;
        }
        return aboveLevelBookUnitEidsNewMathPersistence.findByBookId(bookId);
    }

    public List<String> getEidsByBookUnitEids(AboveLevelBookUnitEidsNewMath bookUnitEids) {
        List<String> retList = new ArrayList<>();
        if (bookUnitEids == null || bookUnitEids.getUnits() == null)
            return retList;

        for (Map.Entry<String, List<String>> entry : bookUnitEids.getUnits().entrySet()) {
            retList.addAll(entry.getValue());
        }

        return retList;
    }

}

