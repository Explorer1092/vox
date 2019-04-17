package com.voxlearning.utopia.service.vendor.impl.cache;

import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.vendor.api.mapper.XiaoUOrderInfo;
import com.voxlearning.utopia.service.vendor.impl.service.XiaoUOrderInfoServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/3/9
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestXiaoUOrderInfoServiceImpl {
    @Inject
    private XiaoUOrderInfoServiceImpl xiaoUOrderInfoService;


    @Test
    public void testAdd() {
        List<XiaoUOrderInfo> xiaoUOrderInfoList = xiaoUOrderInfoService.getXiaoUOrderInfoList();
        xiaoUOrderInfoService.addXiaoUOrderInfo(111111L, "aaaaaaaaaaaa");
        xiaoUOrderInfoService.addXiaoUOrderInfo(222222L, "aaaaaaaaaaaa");
        xiaoUOrderInfoService.addXiaoUOrderInfo(33333L, "aaaaaaaaaaaa");
        List<XiaoUOrderInfo> xiaoUOrderInfoListAfter = xiaoUOrderInfoService.getXiaoUOrderInfoList();
        System.out.println(xiaoUOrderInfoListAfter);
        Assert.assertEquals(3, xiaoUOrderInfoListAfter.size());
    }

}
