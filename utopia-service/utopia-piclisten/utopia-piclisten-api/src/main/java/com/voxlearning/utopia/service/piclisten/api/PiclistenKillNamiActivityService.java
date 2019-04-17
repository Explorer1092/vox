package com.voxlearning.utopia.service.piclisten.api;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.piclisten.api.mapper.PiclistenKillNamiActivityContext;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author jiangpeng
 * @since 2018-08-23 上午10:54
 **/
@ServiceRetries
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceVersion(version = "2018.09.17")
public interface PiclistenKillNamiActivityService extends IPingable {

    MapMessage recBooks(PiclistenKillNamiActivityContext context);

    MapMessage recBooks(PiclistenKillNamiActivityContext context, String productId);


    MapMessage changeSelectBooks(PiclistenKillNamiActivityContext context, List<String> bookIds);

    MapMessage createAssist(PiclistenKillNamiActivityContext context, String productId);

    MapMessage doAssist(String assistId, String openId);

    MapMessage assistDetail(String assistId, String openId);

    MapMessage assistMembers(PiclistenKillNamiActivityContext context, String assistId);

    MapMessage publisherList(Subject subject);

    MapMessage bookList(Subject subject, String publisherName, Integer clazzLevel, String sys);

    MapMessage purchaseInfo(PiclistenKillNamiActivityContext context, String orderId);

    Boolean addShareRecord(Long parentId, String productId);

    Long loadShareRecord(Long parentId, String productId);

    MapMessage sendCouponForShare(String productId, Long parentId);

    MapMessage newBookList(String publisherName,
                           Integer clazzLevel,
                           String sys,
                           PiclistenKillNamiActivityContext piclistenKillNamiActivityContext,
                           Boolean isPackage,
                           Boolean hasClazzLevel,
                           Boolean isSameSubject);


    Long loadNewRecord(Long parentId);

    Boolean addNewRecord(Long parentId);

    MapMessage sendNewCoupon(Long parentId);

}
