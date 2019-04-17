/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = RewardOrder.class)
public class TestCrmRewardOrderDao {

    @Inject private CrmRewardOrderDao crmRewardOrderDao;

    @Test
    public void testFind() throws Exception {
        List<RewardOrder> documents = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            RewardOrder document = new RewardOrder();
            document.setProductId(1L);
            document.setSkuId(0L);
            document.setSkuName("");
            document.setQuantity(0);
            document.setPrice(0D);
            document.setTotalPrice(0D);
            document.setUnit("");
            document.setCode("");
            document.setBuyerId(0L);
            document.setBuyerType(0);
            document.setBuyerName("");
            document.setStatus("");
            document.setSaleGroup("");
            documents.add(document);
        }
        crmRewardOrderDao.inserts(documents);
        Criteria criteria = Criteria.where("PRODUCT_ID").is(1L);
        Pageable pageable = new PageRequest(0, 4, Sort.Direction.ASC, "ID");
        Page<RewardOrder> page = crmRewardOrderDao.find(pageable, criteria);
        assertTrue(page.isFirst());
        assertFalse(page.isLast());
        assertFalse(page.hasPrevious());
        assertTrue(page.hasNext());
        assertEquals(3, page.getTotalPages());
        assertEquals(10, page.getTotalElements());
        assertEquals(4, page.getNumberOfElements());

        pageable = new PageRequest(1, 4, Sort.Direction.ASC, "ID");
        page = crmRewardOrderDao.find(pageable, criteria);
        assertFalse(page.isFirst());
        assertFalse(page.isLast());
        assertTrue(page.hasPrevious());
        assertTrue(page.hasNext());
        assertEquals(3, page.getTotalPages());
        assertEquals(10, page.getTotalElements());
        assertEquals(4, page.getNumberOfElements());

        pageable = new PageRequest(2, 4, Sort.Direction.ASC, "ID");
        page = crmRewardOrderDao.find(pageable, criteria);
        assertFalse(page.isFirst());
        assertTrue(page.isLast());
        assertTrue(page.hasPrevious());
        assertFalse(page.hasNext());
        assertEquals(3, page.getTotalPages());
        assertEquals(10, page.getTotalElements());
        assertEquals(2, page.getNumberOfElements());
    }
}
