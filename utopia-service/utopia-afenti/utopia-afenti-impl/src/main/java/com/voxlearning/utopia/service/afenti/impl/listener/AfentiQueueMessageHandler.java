package com.voxlearning.utopia.service.afenti.impl.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * For handling received message from afenti queue.
 *
 * @author Ruib
 * @since 2016/7/25
 */
public interface AfentiQueueMessageHandler {
    void handle(ObjectMapper mapper, JsonNode root) throws Exception;
}
