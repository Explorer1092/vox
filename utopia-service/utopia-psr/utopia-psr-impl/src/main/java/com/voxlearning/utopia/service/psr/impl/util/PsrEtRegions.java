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

package com.voxlearning.utopia.service.psr.impl.util;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimer;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimerTask;
import com.voxlearning.utopia.service.psr.entity.EtGradeRegionContent;
import com.voxlearning.utopia.service.psr.entity.EtRegionContent;
import com.voxlearning.utopia.service.psr.entity.EtRegionItem;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Created by ChaoLi Lee on 14-7-10.
 * Et 题型数量 在 100 个左右, 常住 内存,减少数据库访问
 * 必须初始化 ekCouchbaseDao
 */
@Slf4j
@Named
public class PsrEtRegions implements InitializingBean {
    /*
     * key : et
     * value : etInfo-list
     */
    private Map<String, EtGradeRegionContent> etGradeRegionContentMap = new ConcurrentHashMap<>();
    @Inject private EkCouchbaseDao ekCouchbaseDao;

    /*
     * 必须初始化 ekCouchbaseDao
     * 如果该题型在城市region下面查不到数据,就取该题型在全国下的 热度
     */
    public EtRegionItem getEtRegionItemByEt(String et, Integer grade, Integer region) {
        if (StringUtils.isEmpty(et) || grade == null || region == null)
            return null;

        et = et.trim();

        // 表示 的是 全国 地区编号,fixme 暂不使用,20150820
        int countryRegion = 0;

        if (etGradeRegionContentMap.containsKey(et)) {
            EtRegionItem etRegionItem = etGradeRegionContentMap.get(et).getEtRegionItemByGradeAndRegion(grade, region);
            if (etRegionItem != null)
                return etRegionItem;
        }

        // 从couchbase数据库中取
        if (ekCouchbaseDao == null) {
            log.error("PsrEtRegions ekCouchbaseDao is null");
            return null;
        }

        EtRegionContent etRegionContent = ekCouchbaseDao.getEtRegionContentFromCouchbase(et, grade);

        if (etRegionContent == null) {
            // 暂时忽略日志,小学英语应用的知识点 在 应试中 可能不存在,所以这个日志可能比较多,所以暂时忽略.
            // log.warn("PsrEkRegions ekRegionContent is null, ek:" + ek);
            return null;
        }

        EtGradeRegionContent etGradeRegionContent = null;
        if (etGradeRegionContentMap.containsKey(et))
            etGradeRegionContent = etGradeRegionContentMap.get(et);
        else
            etGradeRegionContent = new EtGradeRegionContent();

        etGradeRegionContent.setEtRegionContent(grade, etRegionContent);

        /*
         * 不能无限制的 存入map
         */
        if (etGradeRegionContentMap != null && etGradeRegionContentMap.size() < 5000)
            etGradeRegionContentMap.put(et, etGradeRegionContent);
        else
            log.error("PsrEtRegions etRegionContentMap buffer full : " + etGradeRegionContentMap.size());

        EtRegionItem etRegionItem = etGradeRegionContent.getEtRegionItemByGradeAndRegion(grade, region);
        if (etRegionItem != null)
            return etRegionItem;

        log.warn("PsrEtRegions etRegionContent not found region:0 when find region:" + Integer.valueOf(region).toString());
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ExceptionSafeTimerTask task = new ExceptionSafeTimerTask("PsrEtRegions-Loader") {
            @Override
            public void runSafe() {
                etGradeRegionContentMap.clear();
                log.info("PsrEtRegions etRegionContentMap.clear on the timer");
            }
        };
        ExceptionSafeTimer.getCommonInstance().schedule(task, 60*60*1000, 60*60*1000);
    }
}
