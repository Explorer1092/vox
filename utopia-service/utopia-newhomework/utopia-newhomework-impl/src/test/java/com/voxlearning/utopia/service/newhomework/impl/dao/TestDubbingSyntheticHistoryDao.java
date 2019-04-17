package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.utopia.service.newhomework.api.entity.DubbingSyntheticHistory;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;

@DropMongoDatabase
public class TestDubbingSyntheticHistoryDao extends NewHomeworkUnitTestSupport {

    @Inject private DubbingSyntheticHistoryDao dubbingSyntheticHistoryDao;
    @Test
    public void testInsert(){
        DubbingSyntheticHistory dsh = new DubbingSyntheticHistory();
        String id = "201803_5a71c1c97774870283b6a3ea_1__333879088__D_10300002393007-2";
        dsh.setId(id);
        dsh.setSyntheticSuccess(false);
        dubbingSyntheticHistoryDao.insert(dsh);
        DubbingSyntheticHistory aa = dubbingSyntheticHistoryDao.load(id);
        dubbingSyntheticHistoryDao.updateSyntheticState(id, true);
        DubbingSyntheticHistory aaa = dubbingSyntheticHistoryDao.load(id);
        Assert.assertEquals(id, aa.getId());
        Assert.assertEquals(false, aa.getSyntheticSuccess());
        Assert.assertEquals(true, aaa.getSyntheticSuccess());
    }
}
