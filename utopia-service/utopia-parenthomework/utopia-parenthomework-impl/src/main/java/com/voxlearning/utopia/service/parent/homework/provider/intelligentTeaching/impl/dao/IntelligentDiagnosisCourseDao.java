package com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.impl.dao;

import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.impl.entity.IntelligentDiagnosisCourse;

import javax.inject.Named;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 讲练测课程dao
 *
 * @author Wenlong Meng
 * @since Feb 23, 2019
 */
@Named
public class IntelligentDiagnosisCourseDao extends AlpsStaticMongoDao<IntelligentDiagnosisCourse, String> {

    //Local variables

    @Override
    protected void calculateCacheDimensions(IntelligentDiagnosisCourse course, Collection<String> collection) {
    }

    /**
     * 根据讲练测课程id获取课程信息
     *
     * @param ids 讲练测课程id
     * @return
     */
    public Map<String, IntelligentDiagnosisCourse> loadCourses(Set<String> ids) {
        return loads(ids).values().stream().filter(c-> ObjectUtils.get(()->c.getOnStatus() == 1, false)).collect(Collectors.toMap(IntelligentDiagnosisCourse::getId, c->c));
    }


}
