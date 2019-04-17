/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.psr.impl.dao;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.psr.entity.AboveLevelBookUnitEids;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
public class PsrAboveLevelBookEidsDao extends SpringContainerSupport {
    @Inject private AboveLevelBookUnitEidsDao aboveLevelBookUnitEidsDao;

    public List<String> findBookEidsByBookId(Long bookId) {
        return getEidsByBookUnitEids(findUnitsEidsByBookId(bookId));
    }

    public AboveLevelBookUnitEids findUnitsEidsByBookId(Long bookId) {
        if (bookId == null) {
            AboveLevelBookUnitEids inst = new AboveLevelBookUnitEids();
            inst.setUnits(new HashMap<>());
            return inst;
        }

        return aboveLevelBookUnitEidsDao.findByBookId(bookId);
    }

    public List<String> getEidsByBookUnitEids(AboveLevelBookUnitEids bookUnitEids) {
        List<String> retList = new ArrayList<>();
        if (bookUnitEids == null || bookUnitEids.getUnits() == null)
            return retList;

        for (Map.Entry<Long, List<String>> entry : bookUnitEids.getUnits().entrySet()) {
            retList.addAll(entry.getValue());
        }

        return retList;
    }
}
