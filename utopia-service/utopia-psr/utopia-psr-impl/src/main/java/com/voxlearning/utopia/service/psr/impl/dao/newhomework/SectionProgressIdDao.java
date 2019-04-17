package com.voxlearning.utopia.service.psr.impl.dao.newhomework;

import com.google.common.collect.Lists;
import com.mongodb.ReadPreference;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.utopia.service.psr.entity.newhomework.SectionProgressId;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by huicheng on 2016/8/4.
 */


@Named
@UtopiaCacheSupport(SectionProgressId.class)
public class SectionProgressIdDao extends StaticMongoDao<SectionProgressId, String> {

    @Override
    protected void calculateCacheDimensions(SectionProgressId source, Collection<String> dimensions) {

    }

    public SectionProgressId getProgressIdBySection(String sectionid){
        Filter sectionFilter = filterBuilder.where("section_id").is(sectionid).and("deleted_at").exists(false);
        Find find = Find.find(sectionFilter).limit(1);

        List<SectionProgressId> sectionProgressIds = Lists.newArrayList();
        sectionProgressIds.addAll(__find_OTF(find, ReadPreference.primary()));

        if (sectionProgressIds.size() > 0) {
            return sectionProgressIds.get(0);
        } else {
            return null;
        }
    }

    public Map<String,SectionProgressId> getProgressIdsBySections(List<String> sectionIds) {
        if (CollectionUtils.isEmpty(sectionIds)) {
            return Collections.emptyMap();
        }

        Filter filter = filterBuilder.build();
        filter = filter
                .and("section_id").in(sectionIds)
                .and("deleted_at").exists(false);

        return __find_OTF(Find.find(filter), ReadPreference.primary()).stream().collect(Collectors.toMap(SectionProgressId::getSectionId, Function.identity()));
    }

    public List<SectionProgressId> getSectionProgressIdsByUnit(String unitId) {
        if (StringUtils.isEmpty(unitId)) {
            return Collections.emptyList();
        }
        Filter filter = filterBuilder.build();
        filter = filter
                .and("unit_id").is(unitId)
                .and("deleted_at").exists(false);

        return __find_OTF(Find.find(filter), ReadPreference.primary()).stream().collect(Collectors.toList());

    }
}