/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.vendor.impl.persistence;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mysql.persistence.NoCacheStaticMySQLPersistence;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Products information Persistence Class.
 *
 * @author peng.zhang.a
 * @since 2016/6/23
 */
@Named("com.voxlearning.utopia.service.vendor.impl.persistence.FairylandProductPersistence")
public class FairylandProductPersistence extends NoCacheStaticMySQLPersistence<FairylandProduct, Long> {

    @Inject private FairylandProductVersion fairylandProductVersion;

    @Override
    public void insert(FairylandProduct document) {
        $insert(document);
        fairylandProductVersion.increment();
    }

    @Override
    public FairylandProduct upsert(FairylandProduct document) {
        FairylandProduct modified = $upsert(document);
        if (modified != null) {
            fairylandProductVersion.increment();
        }
        return modified;
    }

    @Override
    public FairylandProduct replace(FairylandProduct document) {
        FairylandProduct modified = $replace(document);
        if (modified != null) {
            fairylandProductVersion.increment();
        }
        return modified;
    }

    public int disable(final Long id) {
        long rows = $update(Update.update("DISABLED", true), Criteria.where("ID").is(id));
        if (rows > 0) {
            fairylandProductVersion.increment();
        }
        return (int) rows;
    }
}
