package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.entity.activity.ActivityMothersDayCard;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = ActivityMothersDayCard.class)
public class TestActivityMothersDayCardPersistence {

    @Inject private ActivityMothersDayCardPersistence activityMothersDayCardPersistence;

    @Test
    public void testUpdateSended() throws Exception {
        long id = RandomUtils.nextLong();
        ActivityMothersDayCard card = new ActivityMothersDayCard();
        card.setStudentId(id);
        activityMothersDayCardPersistence.insert(card);
        card = activityMothersDayCardPersistence.load(id);
        assertFalse(card.getSended());
        assertTrue(activityMothersDayCardPersistence.updateSended(id));
        card = activityMothersDayCardPersistence.load(id);
        assertTrue(card.getSended());
    }

    @Test
    public void testUpdateShared() throws Exception {
        long id = RandomUtils.nextLong();
        ActivityMothersDayCard card = new ActivityMothersDayCard();
        card.setStudentId(id);
        activityMothersDayCardPersistence.insert(card);
        card = activityMothersDayCardPersistence.load(id);
        assertFalse(card.getShared());
        assertTrue(activityMothersDayCardPersistence.updateShared(id));
        card = activityMothersDayCardPersistence.load(id);
        assertTrue(card.getShared());
    }
}
