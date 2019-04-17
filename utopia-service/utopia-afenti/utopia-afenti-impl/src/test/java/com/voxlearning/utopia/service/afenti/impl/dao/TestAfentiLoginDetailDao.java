package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLoginDetail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Ruib
 * @since 2016/8/25
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestAfentiLoginDetailDao {
    @Inject private AfentiLoginDetailDao dao;

    @Test
    @DropMongoDatabase
    public void testLogin() throws Exception {
        AfentiLoginDetail detail = dao.login(30017L, Subject.ENGLISH, "2016-08-25");
        assertNotNull(detail);
        assertEquals(1, detail.getDetails().size());

        dao.login(30017L, Subject.ENGLISH, "2016-08-26");
        detail = dao.login(30017L, Subject.ENGLISH, "2016-08-26");
        assertNotNull(detail);
        assertEquals(2, detail.getDetails().size());

        detail = dao.login(30017L, Subject.ENGLISH, "2016-08-27", "2016-08-28");
        assertNotNull(detail);
        assertEquals(4, detail.getDetails().size());

        String[] dates = new String[2];
        dates[0] = "2016-08-29";
        dates[1] = "2016-08-30";
        detail = dao.login(30017L, Subject.ENGLISH, dates);
        assertNotNull(detail);
        assertEquals(6, detail.getDetails().size());

        Set<String> set = new HashSet<>();
        set.add("2016-08-31");
        set.add("2016-09-01");
        detail = dao.login(30017L, Subject.ENGLISH, set.toArray(new String[set.size()]));
        assertNotNull(detail);
        assertEquals(8, detail.getDetails().size());
    }
}
