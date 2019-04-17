package com.voxlearning.utopia.service.mizar.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.entity.order.PicOrderInfo;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiang wei on 2017/3/9.
 */
@ServiceVersion(version = "1.3.STABLE")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface PicOrderInfoService extends IPingable {


    List<PicOrderInfo> getOrderCount(Date startDate, Date endDate);


    Page<PicOrderInfo> getCurrentDayOrderDetailByPage(Date currentDate, Pageable pageable,String regex);


    List<PicOrderInfo> downloadCurrentDayOrderDetail(Date currentDate);


    void insertPicOrderInfo(PicOrderInfo picOrderInfo);


}
