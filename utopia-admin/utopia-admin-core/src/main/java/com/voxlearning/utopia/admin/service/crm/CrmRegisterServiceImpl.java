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

package com.voxlearning.utopia.admin.service.crm;

import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.service.feedback.api.entities.RegisterFeedback;
import com.voxlearning.utopia.service.feedback.client.FeedbackLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Created by jishu on 14-3-3.
 */
@Named
public class CrmRegisterServiceImpl extends AbstractAdminService {

    @Inject private FeedbackLoaderClient feedbackLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;

    /**
     * 默认查询当天未处理的反馈
     */
    public List<Map<String, Object>> findRegisterFeedback(int state, Date start, Date end) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<RegisterFeedback> list = feedbackLoaderClient.getFeedbackLoader().loadRegisterFeedbacks(state, start, end);

        for (RegisterFeedback rfb : list) {
            Map<String, Object> map = new HashMap<>();

            if (rfb.getSensitiveMobile() != null) {
                String mobile = sensitiveUserDataServiceClient.loadRegisterFeedbackMobile(rfb.getId(), "CrmRegisterServiceImpl.findRegisterFeedback");
                rfb.setSensitiveMobile(mobile);
            }

            map.put("feedback", rfb);
            // 这个功能是用户手机注册时发送验证码没有收到，会给客服发个请求，客服告知验证码。
            // 现在通过手机查找用户可能找出多个，如果UserAuthentication是今天以前创建的，那肯定和当前要注册这个用户无关
            // 所以过滤掉今天之前的记录后，如果为空，返回null，如果不为空则去第一个。
            // 但是这样也有一种情况是错误的，就是另外一个角色验证手机也是发生在今天，这样客服就会误以为当前用户已经注册成功，其实他没有成功。。。
            List<UserAuthentication> authentications = userLoaderClient.loadMobileAuthentications(rfb.getSensitiveMobile());
//            authentications = MiscUtils.filterCollection(authentications, new Filter<UserAuthentication>() {
//                @Override
//                public boolean match(UserAuthentication source) {
//                    return source.getCreateDatetime().after(DayRange.current().getStartDate());
//                }
//            });
            map.put("userAuth", authentications.isEmpty() ? null : authentications.get(0));
            result.add(map);
        }

        return result;
    }
}
