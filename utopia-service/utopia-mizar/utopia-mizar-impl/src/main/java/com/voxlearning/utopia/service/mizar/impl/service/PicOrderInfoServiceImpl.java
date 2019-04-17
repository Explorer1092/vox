package com.voxlearning.utopia.service.mizar.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.mizar.api.entity.order.PicOrderInfo;
import com.voxlearning.utopia.service.mizar.api.service.PicOrderInfoService;
import com.voxlearning.utopia.service.mizar.impl.dao.order.PicOrderInfoPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;

/**
 * Created by jiang wei on 2017/3/9.
 */
@Named
@Service(interfaceClass = PicOrderInfoService.class)
@ExposeService(interfaceClass = PicOrderInfoService.class)
public class PicOrderInfoServiceImpl implements PicOrderInfoService {

    @Inject
    private PicOrderInfoPersistence picOrderInfoPersistence;


    @Override
    public List<PicOrderInfo> getOrderCount(Date startDate, Date endDate) {
        return picOrderInfoPersistence.getOrderCount(startDate, endDate);
    }

    @Override
    public Page<PicOrderInfo> getCurrentDayOrderDetailByPage(Date currentDate, Pageable pageable, String regex) {
        return picOrderInfoPersistence.getCurrentDayOrderDetailByPage(currentDate, pageable, regex);
    }

    @Override
    public List<PicOrderInfo> downloadCurrentDayOrderDetail(Date currentDate) {
        return picOrderInfoPersistence.downloadCurrentDayOrderDetail(currentDate);
    }

    @Override
    public void insertPicOrderInfo(PicOrderInfo picOrderInfo) {
        picOrderInfoPersistence.insertOrderInfo(picOrderInfo);
    }
}
