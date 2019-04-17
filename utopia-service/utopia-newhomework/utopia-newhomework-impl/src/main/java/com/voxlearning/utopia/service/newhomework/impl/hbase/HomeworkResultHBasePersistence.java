package com.voxlearning.utopia.service.newhomework.impl.hbase;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.hbase.persistence.StaticHBasePersistence;
import com.voxlearning.utopia.service.newhomework.api.hbase.HomeworkResultHBase;
import com.voxlearning.utopia.service.newhomework.impl.support.HomeworkHBaseHelper;

import javax.inject.Named;
import java.util.*;

/**
 * @author xuesong.zhang
 * @since 2017/8/10
 */
@Named
public class HomeworkResultHBasePersistence extends StaticHBasePersistence<HomeworkResultHBase, String> {

    public HomeworkResultHBase load(String id) {
        HomeworkResultHBase resultHBase = super.load(id);
        return HomeworkHBaseHelper.transformJsonData(resultHBase);
    }

    public Map<String, HomeworkResultHBase> loads(Collection<String> strings) {
        if (CollectionUtils.isEmpty(strings)) {
            return Collections.emptyMap();
        }
        Map<String, HomeworkResultHBase> resultHBaseMap = super.loads(strings);
        Map<String, HomeworkResultHBase> resultMap = new HashMap<>();
        for (String id : strings) {
            HomeworkResultHBase resultHBase = resultHBaseMap.getOrDefault(id, null);
            resultHBase = HomeworkHBaseHelper.transformJsonData(resultHBase);
            if (resultHBase != null) {
                resultMap.put(id, resultHBase);
            }
        }
        return resultMap;
    }

    @Override
    public boolean exists(String s) {
        return false;
    }

    @Override
    public HomeworkResultHBase insertIfAbsent(String s, HomeworkResultHBase document) {
        return null;
    }

    @Override
    public HomeworkResultHBase upsert(HomeworkResultHBase document) {
        return null;
    }

    @Override
    public HomeworkResultHBase replace(HomeworkResultHBase document) {
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
    public List<HomeworkResultHBase> query(Query query) {
        return null;
    }
}
