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

package com.voxlearning.utopia.service.business.impl.service.teacher.internal.card;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.business.api.constant.TeacherCardType;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.mapper.TeacherCardMapper;
import com.voxlearning.utopia.service.advertisement.client.UserAdvertisementServiceClient;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;

/**
 * Created by Summer on 2017/4/20.
 */
@Named
public class LoadTeacherActivityCard extends AbstractTeacherCardDataLoader {

    @Inject private UserAdvertisementServiceClient userAdvertisementServiceClient;

    @Override
    protected TeacherCardDataContext doProcess(TeacherCardDataContext context) {
        // 默认是PC
        String slotId = "110105";
        if (StringUtils.isNotBlank(context.getSys())) {
            slotId = "120103";
        }
        List<NewAdMapper> actList = userAdvertisementServiceClient.getUserAdvertisementService()
                .loadNewAdvertisementData(context.getTeacher().getId(), slotId, context.getSys(), context.getVer());
        if (CollectionUtils.isNotEmpty(actList)) {
            Integer index = 0;
            for (NewAdMapper adMapper : actList) {
                TeacherCardMapper mapper = new TeacherCardMapper();
                if (StringUtils.isNotBlank(adMapper.getImg())) {
                    mapper.setImgUrl("gridfs/" + adMapper.getImg());
                }
                mapper.setDetailUrl(AdvertiseRedirectUtils.redirectUrl(adMapper.getId(), index, context.getVer(), context.getSys(), "", null));
                mapper.setCardType(TeacherCardType.ACTIVITY);
                mapper.setCardName(adMapper.getName());
                mapper.setCardDescription(adMapper.getDescription());
                mapper.setProgress(adMapper.getDescription());
                mapper.setTeacherId(context.getTeacher().getId());
                mapper.setSubject(context.getTeacher().getSubject());
                mapper.setBtnContent(adMapper.getBtnContent());
                context.taskCards.add(mapper);

                if (Boolean.TRUE.equals(adMapper.getLogCollected())) {
                    //曝光打点
                    LogCollector.info("sys_new_ad_show_logs",
                            MapUtils.map(
                                    "user_id", context.getTeacher().getId(),
                                    "env", RuntimeMode.getCurrentStage(),
                                    "version", context.getVer(),
                                    "aid", adMapper.getId(),
                                    "acode", SafeConverter.toString(adMapper.getCode()),
                                    "index", index,
                                    "slotId", slotId,
                                    "client_ip", "",
                                    "time", DateUtils.dateToString(new Date()),
                                    "agent", "",
                                    "system", context.getSys()
                            ));
                }
                index++;
            }
        }
        return context;
    }

}
