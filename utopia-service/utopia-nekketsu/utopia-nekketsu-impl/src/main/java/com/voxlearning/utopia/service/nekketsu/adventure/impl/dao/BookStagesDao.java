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

package com.voxlearning.utopia.service.nekketsu.adventure.impl.dao;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.BookStages;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.Stage;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 人、教材、关卡DAO
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/19 16:51
 */
@Named
@UtopiaCacheSupport(BookStages.class)
public class BookStagesDao extends StaticMongoDao<BookStages, String> {

    @Override
    protected void calculateCacheDimensions(BookStages source, Collection<String> dimensions) {
        dimensions.add(BookStages.ck_id(source.getId()));
    }

    public void addStage(Long userId, Long bookId, final Stage stage) {
        Update update = updateBuilder.build();
        update = update.set("stages." + stage.getOrder(), stage);
        update(BookStages.generateId(userId, bookId), update);
    }

    public void increaseCurrentStage(Long userId, Long bookId) {
        Update update = updateBuilder.build();
        update = update.inc("currentStage", 1);
        update(BookStages.generateId(userId, bookId), update);
    }

    public BookStages updateStageApp(Long userId, Long bookId, final Integer stageOrder, final Integer appOrder,
                                     final Integer realObtainDiamond, final boolean obtainReward, final Integer nextOpenAppOrder) {
        Update update = updateBuilder.build();
        if (null != realObtainDiamond && realObtainDiamond > 0) {
            update = update.inc("stages." + stageOrder + ".obtainDiamond", realObtainDiamond);
            update = update.inc("stages." + stageOrder + ".apps." + appOrder + ".diamond", realObtainDiamond);
        }
        update = update.set("stages." + stageOrder + ".apps." + appOrder + ".obtainReward", obtainReward);
        if (null != nextOpenAppOrder && nextOpenAppOrder > 0) {
            update = update.set("stages." + stageOrder + ".apps." + nextOpenAppOrder + ".open", true);
        }
        return update(BookStages.generateId(userId, bookId), update);
    }


    public BookStages updateStage(Long userId, Long bookId, Integer stageOrder, Boolean receiveFreeBeans, Date receiveBeansTime, Boolean isReceived) {
        Update update = updateBuilder.build();
        update = update.set("stages." + stageOrder + ".receiveFreeBeans", receiveFreeBeans);
        update = update.set("stages." + stageOrder + ".receiveBeansTime", receiveBeansTime);
        update = update.set("stages." + stageOrder + ".isReceived", isReceived);

        return update(BookStages.generateId(userId, bookId), update);
    }

    public void decorativeCrown(Long userId, Long bookId, final Integer stageOrder) {
        Update update = updateBuilder.build();
        update = update.inc("currentCrown", 1);
        update = update.set("stages." + stageOrder + ".decorativeCrown", true);
        update(BookStages.generateId(userId, bookId), update);
    }

    public void increaseOpenedStage(String id) {
        Update update = updateBuilder.build();
        update = update.inc("openedStage", 15);
        update(id, update);
    }

    public void addStageWords(Long userId, Long bookId, final Integer stageOrder, final List<String> words) {
        Update update = updateBuilder.build();
        update = update.set("stages." + stageOrder + ".words", words);
        update(BookStages.generateId(userId, bookId), update);
    }
}
