package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.business.api.TeachingResourceService;
import com.voxlearning.utopia.service.business.api.entity.TeacherResourceTask;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by haitian.gan on 2017/8/3.
 */
public class TeachingResourceServiceClient implements TeachingResourceService {
    private static  final Logger logger = LoggerFactory.getLogger(TeachingResourceServiceClient.class);

    @ImportService(interfaceClass = TeachingResourceService.class)
    private TeachingResourceService remoteReference;

    @Override
    public MapMessage upsertTeachingResource(TeachingResource resource) {
        return remoteReference.upsertTeachingResource(resource);
    }

    @Override
    public MapMessage receiveTask(Long userId, String resourceId, String type) {
        try{
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("TeachingResourceService:receiveTask")
                    .keys(userId,resourceId)
                    .callback(() -> remoteReference.receiveTask(userId, resourceId, type))
                    .build()
                    .execute();
        }catch(CannotAcquireLockException e){
            logger.error("Failed to receive task (user={},resource={}): DUPLICATED OPERATION", userId,resourceId);
            return MapMessage.errorMessage();
        }catch (Throwable t){
            logger.error("Failed to receive task (user={},resource={})", userId,resourceId,t);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage checkTask(TeacherResourceTask task) {
        return remoteReference.checkTask(task);
    }

    @Override
    public MapMessage finishTask(String taskId) {
        return remoteReference.finishTask(taskId);
    }

    @Override
    public MapMessage finishUserTask(Long userId, String taskType) {
        return remoteReference.finishUserTask(userId,taskType);
    }

    @Override
    public MapMessage supplyUserTask(Long userId) {
        return remoteReference.supplyUserTask(userId);
    }

    @Override
    public MapMessage addReadCount(String id) {
        return remoteReference.addReadCount(id);
    }

    @Override
    public MapMessage addCollectCount(String id) {
        return remoteReference.addCollectCount(id);
    }

    @Override
    public MapMessage addCollect(Long userId, String categorie, String resourceId) {
        return remoteReference.addCollect(userId, categorie, resourceId);
    }

    @Override
    public MapMessage disableCollect(Long userId, String recourceId, String categorie, String collectId) {
        return remoteReference.disableCollect(userId, recourceId, categorie, collectId);
    }

    @Override
    public MapMessage addHotSearch(String word) {
        return remoteReference.addHotSearch(word);
    }

    @Override
    public MapMessage moveDataForBackDoor() {
        return remoteReference.moveDataForBackDoor();
    }

    @Override
    public MapMessage fixExpiryData() {
        return remoteReference.fixExpiryData();
    }


}
