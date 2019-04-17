package com.voxlearning.utopia.service.afenti.impl.listener.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxlearning.utopia.service.afenti.api.annotations.AfentiQueueMessageTypeIdentification;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiPromptType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiQueueMessageType;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import com.voxlearning.utopia.service.afenti.impl.dao.WrongQuestionLibraryDao;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import org.springframework.context.annotation.Lazy;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/7/25
 */
@Named
@Lazy(false)
@AfentiQueueMessageTypeIdentification(AfentiQueueMessageType.WRONG_QUESTION_LIBRARY)
public class WrongQuestionLibraryHandler extends AbstractAfentiQueueMessageHandler {

    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;
    @Inject WrongQuestionLibraryDao wrongQuestionLibraryDao;

    @Override
    public void handle(ObjectMapper mapper, JsonNode root) throws Exception {
        WrongQuestionLibrary wql;
        try {
            wql = mapper.readValue(root.get("WQL").traverse(), WrongQuestionLibrary.class);
        } catch (Exception ex) {
            logger.error("Failed to map WrongQuestionLibrary object from json text", ex);
            wql = null;
        }

        if (wql == null || wql.getId() == null) return;

        try {
            wrongQuestionLibraryDao.insert(wql);
            // 插入成功后更新错题精灵小红点
            asyncAfentiCacheService.AfentiPromptCacheManager_record(wql.getUserId(), wql.getSubject(), AfentiPromptType.elf)
                    .awaitUninterruptibly();
        } catch (Exception ignored) {
        }
    }
}
