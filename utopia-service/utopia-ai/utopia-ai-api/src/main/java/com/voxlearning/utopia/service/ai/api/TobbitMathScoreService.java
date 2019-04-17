package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.constant.TobbitScoreType;
import com.voxlearning.utopia.service.ai.entity.TobbitMathCourse;

import java.util.List;
import java.util.concurrent.TimeUnit;


@ServiceVersion(version = "20190218")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface TobbitMathScoreService extends IPingable {

    // new
    MapMessage history(Long uid);

    long total(Long uid);

    boolean addScore(Long uid, TobbitScoreType type);

    boolean addScore(Long uid, String openId, TobbitScoreType type);

    MapMessage redeemCourse(Long uid, String cid);

    MapMessage invite(String openId, String inviter);

    MapMessage course(Long uid);

    void addCourseDoNotCallIfYouConfused(List<TobbitMathCourse> courses);

    void cleanCourseDoNotCallIfYouConfused();
}
