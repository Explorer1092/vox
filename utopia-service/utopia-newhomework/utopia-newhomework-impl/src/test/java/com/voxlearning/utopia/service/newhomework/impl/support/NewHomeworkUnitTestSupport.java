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

package com.voxlearning.utopia.service.newhomework.impl.support;

import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.newhomework.impl.dao.JournalNewHomeworkProcessResultDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.basicreview.BasicReviewHomeworkPackageDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.poetry.AncientPoetryRegisterDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.shard.ShardHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkProcessResultDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkResultAnswerDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend.ReadingDubbingRecommendDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.*;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
abstract public class NewHomeworkUnitTestSupport extends NewHomeworkSpringBean {
    @Inject protected JournalNewHomeworkProcessResultDao journalNewHomeworkProcessResultDao;
    @Inject protected NewAccomplishmentLoaderImpl newAccomplishmentLoader;
    @Inject protected NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject protected NewHomeworkPartLoaderImpl newHomeworkPartLoader;
    @Inject protected OfflineHomeworkLoaderImpl offlineHomeworkLoader;
    @Inject protected SubHomeworkDao subHomeworkDao;
    @Inject protected SubHomeworkResultDao subHomeworkResultDao;
    @Inject protected SubHomeworkResultAnswerDao subHomeworkResultAnswerDao;
    @Inject protected SubHomeworkProcessResultDao subHomeworkProcessResultDao;
    @Inject protected VacationHomeworkCacheLoaderImpl vacationHomeworkCacheLoader;
    @Inject protected VoiceRecommendLoaderImpl voiceRecommendLoader;
    @Inject protected BasicReviewHomeworkPackageDao basicReviewHomeworkPackageDao;
    @Inject protected ReadingDubbingRecommendDao readingDubbingRecommendDao;
    @Inject protected ShardHomeworkDao shardHomeworkDao;
    @Inject protected AncientPoetryRegisterDao ancientPoetryRegisterDao;
}
