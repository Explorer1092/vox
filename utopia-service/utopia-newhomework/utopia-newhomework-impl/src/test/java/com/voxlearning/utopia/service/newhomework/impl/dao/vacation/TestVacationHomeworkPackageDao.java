package com.voxlearning.utopia.service.newhomework.impl.dao.vacation;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.context.MDP;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author xuesong.zhang
 * @since 2016/11/28
 */
@DropMongoDatabase
public class TestVacationHomeworkPackageDao extends NewHomeworkUnitTestSupport {

    @Test
    public void testInsert() throws Exception {
        Date createDate = new Date();
        Subject subject = Subject.MATH;

        VacationHomeworkPackage homeworkPackage = new VacationHomeworkPackage();
        homeworkPackage.setSubject(subject);
        homeworkPackage.setCreateAt(createDate);

        vacationHomeworkPackageDao.insert(homeworkPackage);
        String id = homeworkPackage.getId();
        homeworkPackage = vacationHomeworkPackageDao.load(id);

        assertEquals(subject, homeworkPackage.getSubject());
        vacationHomeworkPackageDao.load(id);
    }

    @Test
    @MockBinder(
            type = VacationHomeworkPackage.class,
            jsons = "{'_id':$objectId}",
            persistence = VacationHomeworkPackageDao.class
    )
    public void updateDisabledTrue() throws Exception {
        String id = MDP.findOne(VacationHomeworkPackage.class).getId();
        assertTrue(vacationHomeworkPackageDao.updateDisabledTrue(id));
        VacationHomeworkPackage homeworkPackage = vacationHomeworkPackageDao.load(id);
        assertTrue(homeworkPackage.isDisabledTrue());

        String oid = RandomUtils.nextObjectId();
        boolean b = vacationHomeworkPackageDao.updateDisabledTrue(oid);
        assertFalse(vacationHomeworkPackageDao.updateDisabledTrue(oid));
    }

    @Test
    public void testInsertAndDelete() throws Exception {
        VacationHomeworkPackage vacationHomeworkPackage = new VacationHomeworkPackage();
        vacationHomeworkPackage.setId("1");
        vacationHomeworkPackage.setClazzGroupId(11L);
        vacationHomeworkPackage.setTeacherId(111L);
        vacationHomeworkPackage.setDisabled(false);
        vacationHomeworkPackageDao.insert(vacationHomeworkPackage);

        VacationHomeworkPackage vacationHomeworkPackage1 = vacationHomeworkPackageDao.load("1");
        assertNotNull(vacationHomeworkPackage1);
        Map<Long, List<VacationHomeworkPackage.Location>> vacationHomeworkPackageMap1 = vacationHomeworkPackageDao.loadVacationHomeworkPackageByClazzGroupIds(Arrays.asList(11L, 22L));
        assertTrue(CollectionUtils.isNotEmpty(vacationHomeworkPackageMap1.get(11L)));
        assertFalse(CollectionUtils.isNotEmpty(vacationHomeworkPackageMap1.get(22L)));
        vacationHomeworkPackageMap1 = vacationHomeworkPackageDao.loadVacationHomeworkPackageByClazzGroupIds(Arrays.asList(11L, 22L));
        assertTrue(CollectionUtils.isNotEmpty(vacationHomeworkPackageMap1.get(11L)));
        assertFalse(CollectionUtils.isNotEmpty(vacationHomeworkPackageMap1.get(22L)));
    }
}
