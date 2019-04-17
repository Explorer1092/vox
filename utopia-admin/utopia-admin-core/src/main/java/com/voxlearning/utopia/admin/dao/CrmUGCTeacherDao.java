package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.entity.crm.ugc.CrmUGCTeacher;
import org.bson.BsonDocument;
import org.bson.BsonString;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author Zhuan liu
 * @since 2016/1/28.
 */

@Named
public class CrmUGCTeacherDao extends StaticMongoDao<CrmUGCTeacher,String> {
    @Override
    protected void calculateCacheDimensions(CrmUGCTeacher source, Collection<String> dimensions) {

    }

    public Page<CrmUGCTeacher> findCrmUgcTeacherIs(Integer triggerType, String subjectTrigger,Pageable pageable) {
        Filter filter = filterBuilder.where("triggerType").is(triggerType);
        if(subjectTrigger != null){
            filter.and("subject").is(subjectTrigger);
        }
        return __pageFind_OTF(filter.toBsonDocument(), pageable);
    }

    public Page<CrmUGCTeacher> findCrmUgcTeacherIn(List<Integer> triggerTypes, String subjectTrigger,Pageable page) {
        Filter filter = filterBuilder.where("triggerType").in(triggerTypes);
        if(subjectTrigger != null){
            filter.and("subject").is(subjectTrigger);
        }
        return __pageFind_OTF(filter.toBsonDocument(), page);

    }

    public Page<CrmUGCTeacher> findCrmUgcTeacherAnswerChange(Integer triggerType, String subjectTrigger,Pageable pageable) {
        Filter filter = filterBuilder.where("triggerType").is(triggerType);
        BsonDocument document = filter.toBsonDocument();
        document.put("$where", new BsonString("this.ugcTeacherName != this.historyUgcTeacherName"));
        if(subjectTrigger != null){
            filter.and("subject").is(subjectTrigger);
        }
        return __pageFind_OTF(document, pageable);

    }

    public CrmUGCTeacher findUgcTeacherBySubject(Long schoolId,Long clazzId,String subject){
        Filter filter = filterBuilder.where("schoolId").is(schoolId)
                .and("clazzId").is(clazzId).and("subject").is(subject);
        return __find_OTF(Find.find(filter)).stream().findFirst().orElse(null);
    }

    public long getCrmUgcTeacherCount() {
        return __count_OTF();
    }

    public List<CrmUGCTeacher> allCrmUgcTeacherData(int limit, int skip) {
        Filter filter = filterBuilder.build();
        return __find_OTF(filter.toBsonDocument(), limit, skip, null, null);
    }

}
