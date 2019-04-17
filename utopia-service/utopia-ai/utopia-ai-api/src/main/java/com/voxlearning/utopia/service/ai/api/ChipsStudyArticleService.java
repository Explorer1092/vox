package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.entity.ChipsStudyArticle;

import java.util.concurrent.TimeUnit;

/**
 * @author guangqing
 * @since 2019/3/19
 */
@ServiceVersion(version = "20190319")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsStudyArticleService extends IPingable {

    MapMessage loadArticleListForCrm();

    //如果id不为空，则更新 否则新增
    MapMessage upsertStudyArticle(ChipsStudyArticle article);

    MapMessage loadArticleForCrm(String articleId);

}
