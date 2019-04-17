/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.annotation.MockBinders;
import com.voxlearning.alps.test.context.MDP;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkBookDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@DropMongoDatabase
public class TestNewHomeworkLoaderImpl extends NewHomeworkUnitTestSupport {

    @Test
    @MockBinders({
            @MockBinder(
                    type = SubHomework.class,
                    jsons = {
                            "{'id':'201701_587f3d192fad40267898fc73_1'}",
                            "{'id':'201701_587f3d192fad40267898fc74_1'}",
                    },
                    persistence = SubHomeworkDao.class)

    })
    public void testLoadNewHomeworksIncludeDisabled() throws Exception {
        Set<String> ids = MDP.toMap(SubHomework.class, SubHomework::getId).keySet();
        Map<String, NewHomework> map = newHomeworkLoader.loads(ids);
        assertEquals(ids.size(), map.size());
        for (String id : ids) {
            assertNotNull(map.get(id));
            assertNotNull(newHomeworkLoader.loadNewHomework(id));
        }
        map = newHomeworkLoader.loads(ids);
        assertEquals(ids.size(), map.size());
        for (String id : ids) {
            assertNotNull(map.get(id));
            assertNotNull(newHomeworkLoader.load(id));
        }
    }

    @Test
    @MockBinders({
            @MockBinder(
                    type = SubHomeworkBook.class,
                    jsons = {
                            "{'id':'201701_587f3d192fad40267898fc73_1'}",
                            "{'id':'201701_587f3d192fad40267898fc74_1'}",
                    },
                    persistence = SubHomeworkBookDao.class)

    })
    public void testLoadNewHomeworkBook() throws Exception {
        Set<String> ids = MDP.toMap(SubHomeworkBook.class, SubHomeworkBook::getId).keySet();
        Map<String, NewHomeworkBook> map = newHomeworkLoader.loadNewHomeworkBooks(ids);
        assertEquals(ids.size(), map.size());
        for (String id : ids) {
            assertNotNull(map.get(id));
            assertNotNull(newHomeworkLoader.loadNewHomeworkBook(id));
        }
        map = newHomeworkLoader.loadNewHomeworkBooks(ids);
        assertEquals(ids.size(), map.size());
        for (String id : ids) {
            assertNotNull(map.get(id));
            assertNotNull(newHomeworkLoader.loadNewHomeworkBook(id));
        }
    }

    @Test
    @MockBinders({
            @MockBinder(
                    type = SubHomework.class,
                    jsons = {
                            "{'subject':'CHINESE','clazzGroupId':1,'disabled':false}",
                            "{'subject':'CHINESE','clazzGroupId':1,'disabled':false}",
                            "{'subject':'CHINESE','clazzGroupId':1,'disabled':false}",
                            "{'subject':'CHINESE','clazzGroupId':2,'disabled':false}",
                            "{'subject':'CHINESE','clazzGroupId':2,'disabled':false}",
                            "{'subject':'CHINESE','clazzGroupId':2,'disabled':false}",
                            "{'subject':'CHINESE','clazzGroupId':3,'disabled':false}",
                            "{'subject':'CHINESE','clazzGroupId':3,'disabled':false}",
                            "{'subject':'CHINESE','clazzGroupId':3,'disabled':false}",
                    },
                    persistence = SubHomeworkDao.class)

    })
    public void testLoadNewHomeworksByClazzGroupIds() throws Exception {
        Set<Long> groupIds = MDP.toMap(SubHomeworkBook.class, SubHomeworkBook::getClazzGroupId).keySet();
        Map<Long, List<NewHomework.Location>> map = newHomeworkLoader.loadNewHomeworksByClazzGroupIds(groupIds, Subject.CHINESE);
        assertEquals(groupIds.size(), map.size());
        for (Long groupId : groupIds) {
            assertEquals(6, map.get(groupId).size());
        }
        map = newHomeworkLoader.loadNewHomeworksByClazzGroupIds(groupIds, Subject.CHINESE);
        assertEquals(groupIds.size(), map.size());
        for (Long groupId : groupIds) {
            assertEquals(6, map.get(groupId).size());
        }
    }
}
