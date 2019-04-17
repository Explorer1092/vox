package com.voxlearning.utopia.service.reward.impl.internal;

import com.voxlearning.utopia.service.reward.entity.newversion.PowerPrizeRecord;
import com.voxlearning.utopia.service.reward.impl.dao.PowerPrizeRecordDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public class InternalBackPackService {
    @Inject
    private PowerPrizeRecordDao powerPrizeRecordDao;

    public List<PowerPrizeRecord> loadRecordByUserId(long userrId) {
        return powerPrizeRecordDao.loadByUserId(userrId);
    }

}
