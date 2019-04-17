package com.voxlearning.utopia.service.newhomework.impl.hbase;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.hbase.persistence.StaticHBasePersistence;
import com.voxlearning.utopia.service.newhomework.api.hbase.HomeworkResultAnswerHBase;
import com.voxlearning.utopia.service.newhomework.impl.support.HomeworkHBaseHelper;

import javax.inject.Named;
import java.util.*;

/**
 * @author xuesong.zhang
 * @since 2017/8/19
 */
@Named
public class HomeworkResultAnswerHBasePersistence extends StaticHBasePersistence<HomeworkResultAnswerHBase, String> {

    public HomeworkResultAnswerHBase load(String id) {
        HomeworkResultAnswerHBase resultAnswerHBase = super.load(id);
        return HomeworkHBaseHelper.transformJsonData(resultAnswerHBase);
    }

    public Map<String, HomeworkResultAnswerHBase> loads(Collection<String> strings) {
        if (CollectionUtils.isEmpty(strings)) {
            return Collections.emptyMap();
        }
        Map<String, HomeworkResultAnswerHBase> resultHBaseMap = super.loads(strings);
        Map<String, HomeworkResultAnswerHBase> resultMap = new HashMap<>();
        for (String id : strings) {
            HomeworkResultAnswerHBase resultAnswerHBase = resultHBaseMap.getOrDefault(id, null);
            resultAnswerHBase = HomeworkHBaseHelper.transformJsonData(resultAnswerHBase);
            if (resultAnswerHBase != null) {
                resultMap.put(id, resultAnswerHBase);
            }
        }
        return resultMap;
    }

    @Override
    public boolean exists(String s) {
        return false;
    }

    @Override
    public HomeworkResultAnswerHBase insertIfAbsent(String s, HomeworkResultAnswerHBase document) {
        return null;
    }

    @Override
    public HomeworkResultAnswerHBase upsert(HomeworkResultAnswerHBase document) {
        return null;
    }

    @Override
    public HomeworkResultAnswerHBase replace(HomeworkResultAnswerHBase document) {
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
    public List<HomeworkResultAnswerHBase> query(Query query) {
        return null;
    }
}
