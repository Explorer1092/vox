package com.voxlearning.utopia.service.guest.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.utopia.service.guest.impl.support.GuestUnitTestSupport;
import com.voxlearning.utopia.service.user.api.entities.third.ThirdPartyGroupTeacherRef;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author xuesong.zhang
 * @since 2016/9/22
 */
public class TestThirdPartyGroupTeacherRefPersistence extends GuestUnitTestSupport {
    @Inject private ThirdPartyGroupTeacherRefPersistence thirdPartyGroupTeacherRefPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = ThirdPartyGroupTeacherRef.class)
    public void testFindByTeacherId() throws Exception {
        Long teacherId = 1L;
        List<ThirdPartyGroupTeacherRef> refs = new ArrayList<>();
        refs.add(ThirdPartyGroupTeacherRef.newInstance(1L, teacherId));
        refs.add(ThirdPartyGroupTeacherRef.newInstance(2L, teacherId));
        refs.add(ThirdPartyGroupTeacherRef.newInstance(3L, teacherId));
        thirdPartyGroupTeacherRefPersistence.persist(refs);
        refs = thirdPartyGroupTeacherRefPersistence.findByTeacherId(teacherId);
        assertEquals(3, refs.size());

        // disable one
        refs.get(0).setDisabled(true);
        thirdPartyGroupTeacherRefPersistence.update(refs.get(0).getId(), refs.get(0));
        refs = thirdPartyGroupTeacherRefPersistence.findByTeacherId(teacherId);
        assertEquals(2, refs.size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = ThirdPartyGroupTeacherRef.class)
    @MockBinder(
            type = ThirdPartyGroupTeacherRef.class,
            jsons = {
                    "{'groupId':1, 'teacherId':1}",
                    "{'groupId':2, 'teacherId':2}",
                    "{'groupId':3, 'teacherId':3}"
            },
            persistence = ThirdPartyGroupTeacherRefPersistence.class
    )
    public void testFindByTeacherIds() throws Exception {
        List<Long> teacherIds = Arrays.asList(1L, 2L, 3L);
        Map<Long, List<ThirdPartyGroupTeacherRef>> refs = thirdPartyGroupTeacherRefPersistence.findByTeacherIds(teacherIds);
        assertEquals(3, refs.size());

        // from cache
        refs = thirdPartyGroupTeacherRefPersistence.findByTeacherIds(teacherIds);
        assertEquals(3, refs.size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = ThirdPartyGroupTeacherRef.class)
    public void testFindByTeacherIdIncludeDisabled() throws Exception {
        Long teacherId = 1L;
        List<ThirdPartyGroupTeacherRef> refs = new ArrayList<>();
        refs.add(ThirdPartyGroupTeacherRef.newInstance(1L, teacherId));
        refs.add(ThirdPartyGroupTeacherRef.newInstance(2L, teacherId));
        refs.add(ThirdPartyGroupTeacherRef.newInstance(3L, teacherId));
        thirdPartyGroupTeacherRefPersistence.persist(refs);
        refs = thirdPartyGroupTeacherRefPersistence.findByTeacherIdIncludeDisabled(teacherId);
        assertEquals(3, refs.size());

        // disable one
        refs.get(0).setDisabled(true);
        thirdPartyGroupTeacherRefPersistence.update(refs.get(0).getId(), refs.get(0));
        refs = thirdPartyGroupTeacherRefPersistence.findByTeacherIdIncludeDisabled(teacherId);
        assertEquals(3, refs.size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = ThirdPartyGroupTeacherRef.class)
    public void testFindByGroupId() throws Exception {
        Long groupId = 1L;
        List<ThirdPartyGroupTeacherRef> refs = new ArrayList<>();
        refs.add(ThirdPartyGroupTeacherRef.newInstance(groupId, 1L));
        refs.add(ThirdPartyGroupTeacherRef.newInstance(groupId, 2L));
        thirdPartyGroupTeacherRefPersistence.persist(refs);
        // from db
        assertEquals(2, thirdPartyGroupTeacherRefPersistence.findByGroupId(groupId).size());
        // from cache
        assertEquals(2, thirdPartyGroupTeacherRefPersistence.findByGroupId(groupId).size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = ThirdPartyGroupTeacherRef.class)
    public void testFindByGroupIds() throws Exception {
        List<ThirdPartyGroupTeacherRef> refs = new ArrayList<>();
        refs.add(ThirdPartyGroupTeacherRef.newInstance(1L, 11L));
        refs.add(ThirdPartyGroupTeacherRef.newInstance(1L, 12L));
        refs.add(ThirdPartyGroupTeacherRef.newInstance(2L, 13L));
        refs.add(ThirdPartyGroupTeacherRef.newInstance(2L, 14L));
        refs.add(ThirdPartyGroupTeacherRef.newInstance(2L, 15L));
        thirdPartyGroupTeacherRefPersistence.persist(refs);
        // from db
        Map<Long, List<ThirdPartyGroupTeacherRef>> ret = thirdPartyGroupTeacherRefPersistence.findByGroupIds(Arrays.asList(1L, 2L));
        assertEquals(2, ret.size());
        assertEquals(3, ret.get(2L).size());
        // from cache
        ret = thirdPartyGroupTeacherRefPersistence.findByGroupIds(Arrays.asList(1L, 2L));
        assertEquals(2, ret.size());
        assertEquals(3, ret.get(2L).size());
    }

}
