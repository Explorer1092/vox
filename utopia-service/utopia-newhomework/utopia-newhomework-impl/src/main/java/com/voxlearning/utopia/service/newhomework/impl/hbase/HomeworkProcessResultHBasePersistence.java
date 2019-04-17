package com.voxlearning.utopia.service.newhomework.impl.hbase;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.hbase.persistence.StaticHBasePersistence;
import com.voxlearning.utopia.service.newhomework.api.hbase.HomeworkProcessResultHBase;
import com.voxlearning.utopia.service.newhomework.impl.support.HomeworkHBaseHelper;

import javax.inject.Named;
import java.util.*;

/**
 * @author xuesong.zhang
 * @since 2017/8/14
 */
@Named
public class HomeworkProcessResultHBasePersistence extends StaticHBasePersistence<HomeworkProcessResultHBase, String> {

    public HomeworkProcessResultHBase load(String id) {
        HomeworkProcessResultHBase processResultHBase = super.load(id);
        return HomeworkHBaseHelper.transformJsonData(processResultHBase);
    }

    public Map<String, HomeworkProcessResultHBase> loads(Collection<String> strings) {
        if (CollectionUtils.isEmpty(strings)) {
            return Collections.emptyMap();
        }
        Map<String, HomeworkProcessResultHBase> processResultHBaseMap = super.loads(strings);
        Map<String, HomeworkProcessResultHBase> resultMap = new HashMap<>();
        for (String id : strings) {
            HomeworkProcessResultHBase processResultHBase = processResultHBaseMap.getOrDefault(id, null);
            processResultHBase = HomeworkHBaseHelper.transformJsonData(processResultHBase);
            if (processResultHBase != null) {
                resultMap.put(id, processResultHBase);
            }
        }
        return resultMap;
    }

    @Override
    public boolean exists(String s) {
        return false;
    }

    @Override
    public HomeworkProcessResultHBase insertIfAbsent(String s, HomeworkProcessResultHBase document) {
        return null;
    }

    @Override
    public HomeworkProcessResultHBase upsert(HomeworkProcessResultHBase document) {
        return null;
    }

    @Override
    public HomeworkProcessResultHBase replace(HomeworkProcessResultHBase document) {
        return null;
    }

    @Override
    public boolean remove(String s) {
        return false;
    }

    @Override
    public long removes(Collection<String> strings) {
        return 0;
    }

    @Override
    public long count(Query query) {
        return 0;
    }

    @Override
    public List<HomeworkProcessResultHBase> query(Query query) {
        return null;
    }
}
