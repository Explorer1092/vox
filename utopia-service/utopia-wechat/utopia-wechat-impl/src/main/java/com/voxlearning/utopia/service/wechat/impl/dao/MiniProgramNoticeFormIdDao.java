package com.voxlearning.utopia.service.wechat.impl.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramType;
import com.voxlearning.utopia.service.wechat.api.entities.MiniProgramNoticeFormId;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;


@Named
public class MiniProgramNoticeFormIdDao extends AlpsStaticMongoDao<MiniProgramNoticeFormId, String> {


    @Override
    protected void calculateCacheDimensions(MiniProgramNoticeFormId document, Collection<String> dimensions) {
        dimensions.add(MiniProgramNoticeFormId.ck_id(document.getId()));
        dimensions.add(MiniProgramNoticeFormId.ck_openId(document.getOpenId()));
        dimensions.add(MiniProgramNoticeFormId.ck_type(document.getType()));
    }

    public List<MiniProgramNoticeFormId> loadByOpenId(String openId, MiniProgramType type) {
        Criteria criteria = Criteria.where("openId").is(openId).and("type").is(type);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = Query.query(criteria).with(sort).limit(1);
        return query(query);
    }

}
