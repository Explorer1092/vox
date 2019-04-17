package com.voxlearning.utopia.service.newhomework.impl.dao.poetry;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryRegister;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/3/18
 */
@DropMongoDatabase
public class TestAncientPoetryRegisterDao extends NewHomeworkUnitTestSupport {

    @Test
    public void testLoadByClazzGroupIds(){
        Map<Long, List<AncientPoetryRegister>> map = ancientPoetryRegisterDao.loadByClazzGroupIds(Collections.singleton(123L));
        assertEquals(1, map.size());
    }
}
