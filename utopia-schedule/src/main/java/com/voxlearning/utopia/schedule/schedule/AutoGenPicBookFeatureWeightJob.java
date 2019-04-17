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

package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.afenti.api.entity.PicBookStat;
import com.voxlearning.utopia.service.afenti.client.UserPicBookServiceClient;
import com.voxlearning.utopia.service.question.consumer.PictureBookLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自动生成绘本推荐Cache的Job，按阅读等级来划分
 * Created by haitian.gan on 2018/03/20.
 */
@Named
@ScheduledJobDefinition(
        jobName = "自动生成绘本推荐Cach",
        jobDescription = "每天00:01执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 1 0 * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoGenPicBookFeatureWeightJob extends ScheduledJobWithJournalSupport {

    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @Inject private UserPicBookServiceClient usrPicBookSrvCli;
    @Inject private PictureBookLoaderClient picBookLoaderCli;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;

    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        Map<String,PicBookStat> bookStatMap = usrPicBookSrvCli.loadPicBookStatMap();
        Date now = DayRange.current().getEndDate();

        Map<String,Object> seriesWeightMap = MapUtils.m(
                "Farfaria",3,
                "BEC",1.5,
                "China Kids English",1,
                "e-future Classic Readers",2,
                "Moon Kkang",2,
                "Cambridge Reading Adventure",2.5,
                "Longman eReading",2.5,
                "Cultural Stories in English",1);

        Map<String,String> seriesMap = picBookLoaderCli.loadAllPictureBookSeries()
                .stream()
                .collect(Collectors.toMap(pbs -> pbs.getId(),pbs -> pbs.getName()));

        usrPicBookSrvCli.loadSelfPicBooks(pictureBookPlusServiceClient).forEach(book -> {
            String bookId = book.getId();
            double weight = 0;

            PicBookStat bookStat = bookStatMap.get(bookId);
            boolean hadBeenSold = Optional.ofNullable(bookStat)
                    .map(PicBookStat::getLastBuyTime)
                    .map(buyTime -> DateUtils.truncate(buyTime,Calendar.HOUR_OF_DAY))
                    .map(pbs -> DateUtils.dayDiff(now,pbs) <= 3)
                    .orElse(false);

            if(hadBeenSold) weight ++;

            Date createTime = DateUtils.truncate(book.getCreatedAt(),Calendar.HOUR_OF_DAY);
            if(DateUtils.dayDiff(now,createTime) <= 30){
                weight += 3;
            }

            Double seriesWeight = Optional.ofNullable(seriesMap.get(book.getSeriesId()))
                    .map(n -> SafeConverter.toDouble(seriesWeightMap.get(n)))
                    .orElse(0d);

            weight += seriesWeight;

            if(bookStat == null){
                bookStat = new PicBookStat();
                bookStat.setBookId(book.getId());
                bookStat.setSales(0);
            }
            // 如果值是一样的，则不更新直接返回
            else if(Objects.equals(bookStat.getWeight(),weight)){
                return;
            }

            bookStat.setWeight(weight);
            MapMessage resultMsg = usrPicBookSrvCli.savePicBookStat(bookStat);
            if(!resultMsg.isSuccess()){
                logger.error("Auto generate picbook cache error!bookId:{},detail:{}",book.getId(),resultMsg.getInfo());
            }

        });
    }

}
