package com.voxlearning.utopia.service.mizar.impl.dao.order;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.mizar.api.entity.order.PicOrderInfo;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by jiang wei on 2017/3/10.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestPicOrderInfoPersistence {

    @Inject
    private PicOrderInfoPersistence picOrderInfoPersistence;


    @Test
    public void upsertData() {
        PicOrderInfo picOrderInfo = new PicOrderInfo();
        picOrderInfo.setOrderId("aaaa");
        picOrderInfo.setUserId(1111L);
        picOrderInfo.setPayAmount(new BigDecimal("11.11"));
        picOrderInfo.setPaymentStatus(PaymentStatus.Paid.name());
        picOrderInfo.setServiceStartTime(new Date());
        picOrderInfo.setServiceEndTime(new Date());
        picOrderInfo.setProductName("abc");
        picOrderInfo.setOrderCreateTime(new Date());
        picOrderInfoPersistence.insertOrderInfo(picOrderInfo);
    }


    @Test
    public void testFindByPage() {
        Pageable pageable = new PageRequest(0, 10);
        Page<PicOrderInfo> currentDayOrderDetailByPage = picOrderInfoPersistence.getCurrentDayOrderDetailByPage(DateUtils.stringToDate("2017-03-10", DateUtils.FORMAT_SQL_DATE), pageable, "");
        Assert.assertNotNull(currentDayOrderDetailByPage);
        System.out.println(currentDayOrderDetailByPage.getContent().get(0).getUserId());
    }
}
