package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.entity.crm.CrmInviteClue;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/4/12
 */
@Named
public class CrmInviteClueDao extends StaticMongoDao<CrmInviteClue, String> {

    @Override
    protected void calculateCacheDimensions(CrmInviteClue source, Collection<String> dimensions) {

    }

    public List<CrmInviteClue> findByTimeZone(Date startDay, Date endDay){
        Filter filter = filterBuilder.where("createTime").gte(startDay).lte(endDay);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        return find(filter, sort);
    }

    public List<CrmInviteClue> findBySchoolIds(int startDay, int endDay, Collection<Long> schools){
        Filter filter = filterBuilder.where("createTime").gte(startDay).lte(endDay);
        filterIn(filter, "inviterSchoolId", schools);
        return find(filter);
    }

    public List<CrmInviteClue> findByRegionList(int startDay, int endDay, Collection<Integer> regionList){
        Filter filter = filterBuilder.where("createTime").gte(startDay).lte(endDay);
        filterIn(filter, "inviterProvinceCode", regionList);
        filterIn(filter, "inviterCityCode", regionList);
        filterIn(filter, "inviterCountyCode", regionList);
        return find(filter);
    }

    private void filterIs(Filter filter, String key, Object value) {
        if (value != null) {
            filter.and(key).is(value);
        }
    }

    private void filterIn(Filter filter, String key, Collection<?> values) {
        if (values != null) {
            filter.and(key).in(values);
        }
    }

    private List<CrmInviteClue> find(Filter filter) {
        return __find_OTF(Find.find(filter));
    }

    private List<CrmInviteClue> find(Filter filter, Sort sort) {
        return __find_OTF(Find.find(filter).with(sort));
    }
}
