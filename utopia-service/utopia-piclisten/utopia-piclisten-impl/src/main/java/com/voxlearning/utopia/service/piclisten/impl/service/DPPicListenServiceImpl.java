package com.voxlearning.utopia.service.piclisten.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.piclisten.api.DPPicListenService;
import com.voxlearning.utopia.service.piclisten.impl.loader.TextBookManagementLoaderImpl;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenBookPayInfo;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangpeng
 * @since 2017-12-27 下午5:50
 **/
@Slf4j
@Named
@Service(interfaceClass = DPPicListenService.class)
@ExposeService(interfaceClass = DPPicListenService.class)
public class DPPicListenServiceImpl implements DPPicListenService {

    @Inject
    private TextBookManagementLoaderImpl textBookManagementLoader;

    @Inject
    private PicListenCommonServiceImpl picListenCommonService;



    @Override
    public Map<String, Object> loadPicListenBookInfo(String bookId, Long studentId, String sys) {
        boolean studentAuth = picListenCommonService.isStudentAuth(studentId);
        Boolean online = textBookManagementLoader.picListenShow(bookId, sys, studentAuth);
        if (!online)
            return null;
        Map<String, Object> map = new HashMap<>();
        Boolean needPay = textBookManagementLoader.picListenBookNeedPay(bookId);
        if (!needPay){
            map.put("product_status", "free");
            return map;
        }
        Map<String, PicListenBookPayInfo> infoMap = picListenCommonService.studentPicListenBuyInfoMap(studentId, true);
        PicListenBookPayInfo picListenBookPayInfo = infoMap.get(bookId);
        if (picListenBookPayInfo == null){
            map.put("product_status", "unpaid");
            return map;
        }
        PicListenBookPayInfo.ActiveStatus activeStatus = picListenBookPayInfo.getActiveStatus();
        map.put("product_status", activeStatus.name());
        map.put("expire_time", picListenBookPayInfo.getDayRange().getEndDate().getTime());
        return map;
    }
}
