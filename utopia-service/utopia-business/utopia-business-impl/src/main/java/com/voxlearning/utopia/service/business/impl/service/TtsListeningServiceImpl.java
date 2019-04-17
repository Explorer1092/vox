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

package com.voxlearning.utopia.service.business.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.business.api.TtsListeningService;
import com.voxlearning.utopia.business.api.entity.TtsListeningPaper;
import com.voxlearning.utopia.service.business.impl.dao.TtsListeningPaperDao;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@Service(interfaceClass = TtsListeningService.class)
@ExposeService(interfaceClass = TtsListeningService.class)
public class TtsListeningServiceImpl implements TtsListeningService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    TtsListeningPaperDao ttsListeningPaperDao;

    @Override
    public Page<TtsListeningPaper> getListeningPaperPageByUserId(Long userId, Pageable pageable, String title) {
        return ttsListeningPaperDao.getListeningPaperPageByUserId(userId, pageable, title);
    }

    @Override
    public Page<TtsListeningPaper> getListenPaperPageByUidAndBid(Long userId, Long bookId, Pageable pageable) {
        return ttsListeningPaperDao.getListenPaperPageByUidAndBid(userId, bookId, pageable, null);
    }

    @Override
    public Page<TtsListeningPaper> getSharedListeningPaperPage(Pageable pageable, Long bookId, Integer classLevel, Ktwelve ktwelve) {
        return ttsListeningPaperDao.getSharedListeningPaperPage(pageable, bookId, classLevel, ktwelve);
    }

    @Override
    public TtsListeningPaper getListeningPaperById(String id) {
        return ttsListeningPaperDao.load(id);
    }

    @Override
    public String saveListeningPaper(TtsListeningPaper paper) {
        if (paper == null) {
            return null;
        }
        ttsListeningPaperDao.save(paper);
        if (paper.getId() != null)
            return paper.getId();
        return null;
    }

    @Override
    public boolean deleteListeningPaper(String id, Long userId) {
        TtsListeningPaper paper = ttsListeningPaperDao.load(id);
        if (paper != null && paper.getAuthor().equals(userId)) {
            ttsListeningPaperDao.delete(id);
            return true;
        }
        return false;
    }

}
