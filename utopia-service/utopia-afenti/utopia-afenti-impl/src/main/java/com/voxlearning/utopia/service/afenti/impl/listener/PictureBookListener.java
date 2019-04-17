package com.voxlearning.utopia.service.afenti.impl.listener;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.entity.UserPicBook;
import com.voxlearning.utopia.service.afenti.api.entity.UserPicBookResult;
import com.voxlearning.utopia.service.afenti.api.mapper.UserPicBookProgress;
import com.voxlearning.utopia.service.afenti.cache.AfentiCache;
import com.voxlearning.utopia.service.afenti.cache.UserPicBookCache;
import com.voxlearning.utopia.service.afenti.impl.service.UserPicBookServiceImpl;
import com.voxlearning.utopia.service.question.api.constant.ApplyToType;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;
import com.voxlearning.utopia.service.question.consumer.PictureBookLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * 绘本上报数据的处理
 *
 * @author haitian.gan
 */
@Named
@PubsubSubscriber(destinations = {
        @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.afenti.picbook.topic"),
        @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.afenti.picbook.topic")
})
public class PictureBookListener extends SpringContainerSupport implements MessageListener {

    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @Inject private UserPicBookServiceImpl userPicBookSrv;
    @Inject private PictureBookLoaderClient picBookLoader;
    private UserPicBookCache cache;

    @Override
    public void afterPropertiesSet() {
        cache = new UserPicBookCache(AfentiCache.getPersistent());
    }

    private void processPicBookResult(UserPicBookResult result) {
        Long userId = result.getUserId();
        String bookId = result.getBookId();
        int module = SafeConverter.toInt(result.getModule());

        if (userId == null || StringUtils.isEmpty(bookId) || module <= 0) {
            return;
        }

        // 怕扛不住
        if (module == 3 || module == 4) {
            // 记录下
            userPicBookSrv.saveUserPicBookHistory(result);
        }

        UserPicBook userPicBook = userPicBookSrv.loadUserPicBook(userId, bookId);
        PictureBookPlus picBook = pictureBookPlusServiceClient.loadById(bookId);
        if (picBook == null)
            return;

        // 当前模块是否完成的标志
        boolean finish = SafeConverter.toBoolean(result.getFinish());

        // 用户的进度
        UserPicBookProgress progress = cache.loadProgress(userId, Collections.singletonList(bookId)).get(bookId);
        // 免费绘本在第1模块如果翻了两页，则放进我的绘本
        if (module == 1) {
            Set<String> readPages = Optional.ofNullable(progress.getReadPages()).orElse(new HashSet<>());
            // 免费的读到第2页，则自动添加到我的绘本
            boolean isFree = Optional.ofNullable(picBook.getFreeMap())
                    .map(m -> m.getOrDefault(ApplyToType.SELF, 0) == 1)
                    .orElse(false);

            // 翻页的1页，相当产品说的浏览了2页的意思
            // 前端偷摸改回
            if (readPages.size() > 1 && isFree && userPicBook == null) {
                userPicBookSrv.createUserPicBook(userId, bookId, OrderProductServiceType.ELevelReading.name());
            }
        }

        // 模块完成后触发重新算分，如果发现大于原来的分数则更新
        if (finish && userPicBook != null) {
            int allModuleNum = CollectionUtils.isEmpty(picBook.getPracticeQuestions()) ? 3 : 4;
            int totalScore = progress.calTotalScore(allModuleNum);
            int orgScore = SafeConverter.toInt(userPicBook.getScore());

            if (totalScore > orgScore) {
                userPicBook.setScore(totalScore);
                userPicBookSrv.updateUserPicBook(userPicBook);
            }
        }
    }

    @Override
    public void onMessage(Message message) {
        Object body = message.decodeBody();

        Map<String, Object> msgMap;
        if (body instanceof String) {
            msgMap = JsonUtils.fromJson(SafeConverter.toString(body));
        } else if (body instanceof Map) {
            msgMap = (Map) body;
        } else
            return;

        String msgType = SafeConverter.toString(msgMap.get("messageType"));
        switch (msgType) {
            case "reading":

                List<UserPicBookResult> records = (List<UserPicBookResult>) msgMap.get("records");
                if (records == null)
                    return;

                int module = records.get(0).getModule();
                // 第3模块需要记录每个题的情况，供前端展示进度
                // 1模块只用一个标记即可
                // 4模块要算每道题的分，所以也得逐条处理
                if (module == 3 || module == 4) {
                    records.forEach(this::processPicBookResult);
                } else {
                    // 最后一条记录，但是要汇总所有记录的duration
                    int sumDuration = records.stream()
                            .mapToInt(r -> SafeConverter.toInt(r.getDuration()))
                            .sum();

                    UserPicBookResult lastResult = records.get(records.size() - 1);
                    lastResult.setDuration(sumDuration);

                    processPicBookResult(lastResult);
                }

                break;
        }
    }
}
