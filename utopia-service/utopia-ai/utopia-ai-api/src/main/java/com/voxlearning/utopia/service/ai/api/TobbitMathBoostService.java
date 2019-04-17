package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.entity.TobbitMathOralBook;

import java.util.List;
import java.util.concurrent.TimeUnit;


@ServiceVersion(version = "20190218")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface TobbitMathBoostService extends IPingable {


    boolean isOnline();

    MapMessage status(String openId, Long uid,String bid);

    MapMessage appendBoost(String bid, String openId, String name, String avatar);

    MapMessage newBoost(String openId, Long uid, String bookId, String name, String tel, String city, String addr);

    MapMessage oralBookList();

    MapMessage scrollingList();


    void addOralBooksDoNotCallIfYouConfused(List<TobbitMathOralBook> books);
    void cleanOralBooksDoNotCallIfYouConfused();
    List<TobbitMathOralBook> loadOralBooksDoNotCallIfYouConfused();
}
