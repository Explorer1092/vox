package com.voxlearning.utopia.service.business.impl.dao.summerreporter;

import com.mongodb.ReadPreference;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.business.api.entity.summerreport.StudentCityInterviewCount;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by jiangpeng on 16/6/13.
 */
@Named
@UtopiaCacheSupport(StudentCityInterviewCount.class)
public class StudentCityInterviewCountDao extends StaticMongoDao<StudentCityInterviewCount, String> {
    @Override
    protected void calculateCacheDimensions(StudentCityInterviewCount source, Collection<String> dimensions) {
        dimensions.add(StudentCityInterviewCount.ck_id(source.getId()));
        dimensions.add(StudentCityInterviewCount.ck_cityId(source.getCityId()));
    }



    /**
     * 只有上传新采访记录才会count+1
     */
    public void updateCountAddOne(Long studentId,Integer cityId,String studentSchoolName) {

        Update update = updateBuilder.build()
                .setOnInsert("student_id", studentId)
                .setOnInsert("city_id", cityId)
                .setOnInsert("school_name", studentSchoolName)
                .inc("count",1);

        upsert(studentId.toString(),update);

//        List<StudentCityInterviewCount> oldList = findByStudentIds(Collections.singleton(studentId)).get(studentId);
//        if(CollectionUtils.isNotEmpty(oldList)) {
//            StudentCityInterviewCount old = oldList.get(0);
//            StudentCityInterviewCount inst = new StudentCityInterviewCount();
//            inst.setCount(old.getCount() + 1);
//            update(old.getId(), inst);
//        }else {
//            StudentCityInterviewCount instance = StudentCityInterviewCount.instance(studentId,cityId,studentSchoolName);
//            this.insert(instance);
//        }

    }

    // ========================================================================
    // Private methods
    // ========================================================================


    public List<StudentCityInterviewCount> __findByCityIds(Integer cityId) {
        Filter filter = filterBuilder.where("city_id").is(cityId);
        Sort countSort =  new Sort(Sort.Direction.DESC,"count");
        Sort timeSort = new Sort(Sort.Direction.ASC,"ut");
        Find find = Find.find(filter).with(countSort).with(timeSort);

        return __find_OTF(find, ReadPreference.primary());
    }
}
