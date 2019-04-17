package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.PublicGoodNationRank;

import javax.inject.Named;
import java.util.Collection;

@Named
public class PublicGoodNationRankDao extends AlpsStaticJdbcDao<PublicGoodNationRank,Long>{

    @Override
    protected void calculateCacheDimensions(PublicGoodNationRank document, Collection<String> dimensions) {

    }


}
