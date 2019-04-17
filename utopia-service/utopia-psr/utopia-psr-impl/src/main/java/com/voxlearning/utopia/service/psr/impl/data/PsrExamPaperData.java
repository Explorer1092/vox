/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.psr.impl.data;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimer;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimerTask;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.question.api.entity.NewPaper;
import com.voxlearning.utopia.service.question.consumer.PaperLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * lichaoli, 2016-10-26
 */

@Slf4j
@Named
public class PsrExamPaperData implements InitializingBean {
    @Inject private PaperLoaderClient paperLoaderClient;

    protected Map<String/*bookId*/, Map<String/*qid*/, Boolean/*status*/>> paperBookQids = new ConcurrentHashMap<>();

    public Map<String, Boolean> getPaperQidsByBookId(PsrExamContext psrExamContext) {
        if (psrExamContext == null || StringUtils.isBlank(psrExamContext.getBookId()))
            return Collections.emptyMap();
        if (paperBookQids.containsKey(psrExamContext.getBookId()))
            return paperBookQids.get(psrExamContext.getBookId());

        List<NewPaper> papers = paperLoaderClient.loadPaperAsListByNewBookIds(Collections.singleton(psrExamContext.getBookId()), psrExamContext.getSubject().getId());
        if (CollectionUtils.isEmpty(papers))
            return Collections.emptyMap();

        Map<String, Boolean> retMap = new HashMap<>();

        // 过滤试卷, 只选取1 2 7
        // (1, '单元同步习题'), int
        // (2, '阶段测试'),
        // (7, '专项训练'),
        papers.stream().filter(p -> {
                    return (p.getPaperTypes().contains(1) || p.getPaperTypes().contains(2) || p.getPaperTypes().contains(7));
            }).forEach(x -> {
                x.loadQuestionIds().stream().forEach(m -> {
                    if(!retMap.containsKey(m)) retMap.put(m, true);
                });
            });

        // 缓存不易过多,目前paper有9W
        if (paperBookQids.size() <= 1000 && MapUtils.isNotEmpty(retMap))
            paperBookQids.put(psrExamContext.getBookId(), retMap);

        return retMap;
    }

    // 不适合移动端及符合的题型 或者 找不到旧知识点的题 设置成不可用
    public void rmInvalidQuestion(String bookId, String qid) {
        if (StringUtils.isBlank(qid) || StringUtils.isBlank(bookId))
            return;

        if (!paperBookQids.containsKey(bookId))
            return;

        if (paperBookQids.get(bookId).containsKey(qid))
            paperBookQids.get(bookId).put(qid, false);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ExceptionSafeTimerTask task = new ExceptionSafeTimerTask("PsrExamPaperData-Loader") {
            @Override
            public void runSafe() {
                paperBookQids.clear();
                log.info("PsrExamPaperData map clear on the timer");
            }
        };
        ExceptionSafeTimer.getCommonInstance().schedule(task, 60 * 60 * 1000, 60 * 60 * 1000);
    }

}

