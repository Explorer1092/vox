/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.wechat.impl.listener.handler;

import com.voxlearning.alps.annotation.common.Spring;
import com.voxlearning.alps.core.concurrent.ThreadUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.DateUtils;
import com.voxlearning.utopia.service.wechat.api.entities.WechatNotice;
import com.voxlearning.utopia.service.wechat.api.entities.WechatNoticeHistory;
import com.voxlearning.utopia.service.wechat.impl.service.wechat.WechatNoticeHistoryService;
import com.voxlearning.utopia.service.wechat.impl.service.wechat.WechatNoticeService;
import org.springframework.beans.BeanUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Spring
@Named
public class Handler_DeleteWechatNotice extends SpringContainerSupport {

    private static final int BATCH_COUNT = 10000;

    @Inject private WechatNoticeService wechatNoticeService;
    @Inject private WechatNoticeHistoryService wechatNoticeHistoryService;

    public void execute() {
        Date today = DateUtils.truncate(new Date(), Calendar.DATE);
        int backup = 0;
        int deleted = 0;
        logger.info("迁移 VOX_WECHAT_NOTICE 开始");
        while (true) {
            List<WechatNotice> notices = wechatNoticeService.listExpired(today, BATCH_COUNT);
            if (notices == null || notices.isEmpty()) {
                logger.info("VOX_WECHAT_NOTICE 无更多待迁移的数据");
                break;
            }
            List<WechatNoticeHistory> histories = new ArrayList<>(notices.size());
            List<Long> ids = new ArrayList<>(notices.size());
            for (WechatNotice e : notices) {
                WechatNoticeHistory history = new WechatNoticeHistory();
                BeanUtils.copyProperties(e, history);
                history.setId(null);
                histories.add(history);
                ids.add(e.getId());
            }

            Collection<Long> pks = wechatNoticeHistoryService.batchAdd(histories);
            backup += pks == null ? 0 : pks.size();
            logger.info("已经迁移至 VOX_WECHAT_NOTICE_HISTORY 共 '{}' 条", backup);

            deleted += wechatNoticeService.removeByIds(ids);
            logger.info("已经清除 VOX_WECHAT_NOTICE 共 '{}' 条", deleted);

            ThreadUtils.sleepCurrentThread(500);
        }
        logger.info("迁移 VOX_WECHAT_NOTICE 结束");

        Date lastWeek = DateUtils.addWeeks(today, -1);
        int deletedHistory = 0;
        logger.info("清理 VOX_WECHAT_NOTICE_HISTORY 开始");
        while (true) {
            int delCnt = wechatNoticeHistoryService.removeByCreateDatetime(lastWeek, BATCH_COUNT);
            if (delCnt < 1) {
                logger.info("VOX_WECHAT_NOTICE_HISTORY 无更多待清除的数据");
                break;
            }
            deletedHistory += delCnt;
            logger.info("已经清除 VOX_WECHAT_NOTICE_HISTORY 共 '{}' 条", deletedHistory);
            ThreadUtils.sleepCurrentThread(500);
        }
        logger.info("清理 VOX_WECHAT_NOTICE_HISTORY 结束");
    }
}
