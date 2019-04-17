package com.voxlearning.utopia.service.vendor.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.dao.mongo.support.MongoExceptionUtils;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.vendor.api.entity.StudyToolShelf;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2017-10-13 下午1:09
 **/
@Named
@CacheBean(type = StudyToolShelf.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class StudyToolShelfDao extends AlpsStaticMongoDao<StudyToolShelf, Long> {
    @Override
    protected void calculateCacheDimensions(StudyToolShelf document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    public void updateList(Long studentId, List<SelfStudyType> selfStudyTypeList){
        if (CollectionUtils.isEmpty(selfStudyTypeList))
            return;
        List<String> typeList = selfStudyTypeList.stream().filter(Objects::nonNull).map(SelfStudyType::name).collect(Collectors.toList());
        try {
            innerUpdateList(studentId, typeList);
        }catch (Exception e){
            if (MongoExceptionUtils.isDuplicateKeyError(e))
                innerUpdateList(studentId, typeList);
            else
                throw e;
        }
    }


    private void innerUpdateList(Long studentId, List<String> typeList){
        StudyToolShelf studyToolShelf = new StudyToolShelf();
        studyToolShelf.setStudentId(studentId);
        studyToolShelf.setSelfStudyTypeList(typeList);
        upsert(studyToolShelf);
    }
}
