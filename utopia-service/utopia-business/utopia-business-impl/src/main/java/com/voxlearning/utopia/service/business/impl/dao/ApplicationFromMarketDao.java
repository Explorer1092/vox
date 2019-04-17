/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.dao;

import com.mongodb.ReadPreference;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.api.constant.MarketApplicationStatus;
import com.voxlearning.utopia.service.business.api.entity.ApplicationFromMarket;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonString;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;

/**
 * Created by dell on 2015/7/13.
 */
@Named
public class ApplicationFromMarketDao extends StaticMongoDao<ApplicationFromMarket, String> {
    @Override
    protected void calculateCacheDimensions(ApplicationFromMarket source, Collection<String> dimensions) {
    }

    public Page<ApplicationFromMarket> pageApplicationByCreatorAndStatus(String accountName,
                                                                         String status, Pageable pageable) {
        BsonDocument filter = new BsonDocument();
        if (StringUtils.isNotBlank(accountName)) {
            filter.put("accountName", new BsonString(accountName));
        }
        if (StringUtils.isNotBlank(status)) {
            filter.put("status", new BsonString(status));
        }
        filter.put("enabled", new BsonBoolean(true));

        pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), Sort.Direction.DESC, "ct");
        return __pageFind_OTF(filter, pageable, ReadPreference.primary());
    }

    public ApplicationFromMarket updateApplicationFromMarketTime(String id, Date beginTime, Date endTime, String account) {
        if (StringUtils.isBlank(id) || beginTime == null || endTime == null) return null;
        ApplicationFromMarket inst = new ApplicationFromMarket();
        inst.setBeginDateTime(beginTime);
        inst.setEndDateTime(endTime);
        inst.setUpdator(account);
        return update(id, inst);
    }

    public boolean remove(String id) {
        if (StringUtils.isBlank(id)) {
            return false;
        }
        ApplicationFromMarket inst = new ApplicationFromMarket();
        inst.setEnabled(Boolean.FALSE);
        return update(id, inst) != null;
    }

    public boolean updateStatus(String id, MarketApplicationStatus status, String operator) {
        if (StringUtils.isBlank(id) || status == null) return false;
        ApplicationFromMarket inst = new ApplicationFromMarket();
        inst.setStatus(status);
        inst.setUpdator(operator);
        return update(id, inst) != null;
    }

    public boolean updateStatusAndPaperId(String id, MarketApplicationStatus status, String pid, String operator) {
        if (StringUtils.isBlank(id) || status == null || StringUtils.isBlank(pid)) {
            return false;
        }
        ApplicationFromMarket inst = new ApplicationFromMarket();
        inst.setStatus(status);
        inst.setPid(pid);
        inst.setUpdator(operator);
        return update(id, inst) != null;
    }
}
