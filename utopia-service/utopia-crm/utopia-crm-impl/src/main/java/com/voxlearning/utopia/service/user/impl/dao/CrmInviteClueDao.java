package com.voxlearning.utopia.service.user.impl.dao;

import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.entity.crm.CrmInviteClue;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/4/12
 */
@Named("user.CrmInviteClueDao")
public class CrmInviteClueDao extends AsyncStaticMongoPersistence<CrmInviteClue, String> {

    @Override
    protected void calculateCacheDimensions(CrmInviteClue source, Collection<String> dimensions) {
    }
}
