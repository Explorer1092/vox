package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.event.AlpsEventContext;
import com.voxlearning.alps.api.event.EventBus;
import com.voxlearning.alps.api.event.dsl.CallbackEvent;
import com.voxlearning.alps.api.event.dsl.MinuteTimerEventListener;
import com.voxlearning.alps.api.event.dsl.TimerEvent;
import com.voxlearning.alps.core.concurrent.LazyInitializationSupplier;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.utopia.mapper.TeachingResourceStatistics;
import com.voxlearning.utopia.service.business.api.TeachingResourceLoader;
import com.voxlearning.utopia.service.business.api.entity.TeacherResourceTask;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import com.voxlearning.utopia.service.business.api.entity.TeachingResourceCollect;
import com.voxlearning.utopia.service.business.api.mapper.TeachingResourceRaw;
import com.voxlearning.utopia.service.business.buffer.TeachingResourceBuffer;
import com.voxlearning.utopia.service.business.buffer.internal.JVMTeachingResourceBuffer;
import com.voxlearning.utopia.service.business.buffer.mapper.TeachingResourceList;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by haitian.gan on 2017/8/1.
 */
public class TeachingResourceLoaderClient implements TeachingResourceLoader,TeachingResourceBuffer.Aware,InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(TeachingResourceLoaderClient.class);

    @ImportService(interfaceClass = TeachingResourceLoader.class)
    private TeachingResourceLoader remoteReference;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (RuntimeMode.isProduction()) {
            EventBus.publish(new CallbackEvent(this::getTeachingResourceBuffer));
        }
    }

    @Override
    public TeachingResourceStatistics loadResourceStatistics(String id) {
        return remoteReference.loadResourceStatistics(id);
    }

    /**
     * 除 crm 外,请调用 getTeachingResourceBuffer().loadTeachingResourceById(id);
     * @param id
     * @return
     */
    @Override
    public TeachingResource loadResource(String id) {
        return remoteReference.loadResource(id);
    }

    @Override
    @Deprecated
    public List<TeachingResource> loadAllResources() {
        return remoteReference.loadAllResources();
    }

    /**
     * 除 crm 外,请调用 getTeachingResourceBuffer().loadTeachingResourceRaw();
     * @return
     */
    @Override
    @Deprecated
    public List<TeachingResourceRaw> loadAllResourcesRaw() {
        return remoteReference.loadAllResourcesRaw();
    }

    @Override
    public MapMessage loadHomePageChoicestResources(Long teacherId) {
        return remoteReference.loadHomePageChoicestResources(teacherId);
    }

    @Override
    public List<TeacherResourceTask> loadTeacherTasks(Long userId) {
        return remoteReference.loadTeacherTasks(userId);
    }

    @Override
    public List<TeacherResourceTask> loadTasksByStatus(String status) {
        return remoteReference.loadTasksByStatus(status);
    }

    @Override
    public List<String> testForRedis(Long teacherId) {
        return remoteReference.testForRedis(teacherId);
    }

    @Override
    public List<Map<String, Object>> loadTaskHomeworkDetail(Long teacherId) {
        return remoteReference.loadTaskHomeworkDetail(teacherId);
    }

    public Map<String, Object> loadTaskProgress(Long teacherId, String resourceId){
        return remoteReference.loadTaskProgress(teacherId,resourceId);
    }

    @Override
    public List<TeachingResourceCollect> loadCollectByUserId(Long userId) {
        return remoteReference.loadCollectByUserId(userId);
    }

    @Override
    public List<String> getHotWord() {
        return remoteReference.getHotWord();
    }

    @Override
    public List<TeachingResourceRaw> getYQJTRaws() {
        return remoteReference.getYQJTRaws();
    }

    /**
     * 根据状态筛选老师领取到的任务
     * @param userId
     * @param status
     * @return
     */
    public List<TeacherResourceTask> loadTasksByStatus(Long userId,String status){
        return loadTeacherTasks(userId)
                .stream()
                .filter(t -> StringUtils.isEmpty(status) || Objects.equals(t.getStatus(),status))
                .collect(Collectors.toList());
    }

    /**
     * 除 crm 外请调用 getTeachingResourceBuffer().loadResourceRawMap();
     *
     * @return
     */
    @Deprecated
    public Map<String,TeachingResourceRaw> loadResourceMap(){
        return loadAllResourcesRaw().stream().collect(Collectors.toMap(TeachingResourceRaw::getId, r -> r));
    }

    private class ReloadTeachingResourceBuffer extends MinuteTimerEventListener {
        @Override
        protected void processEvent(TimerEvent event, AlpsEventContext context) {
            if (RuntimeModeLoader.getInstance().isUnitTest()) {
                return;
            }
            TeachingResourceBuffer buffer = getTeachingResourceBuffer();
            long version = buffer.getVersion();
            TeachingResourceList data = remoteReference.loadTeachingResourceList(version);
            if (data != null) {
                buffer.attach(data);
                logger.debug("[ReloadTeachingResourceBuffer] reloaded: [{}] -> [{}]", version, data.getVersion());
            }
        }
    }

    private final ReloadTeachingResourceBuffer reloadTeachingResourceBuffer = new ReloadTeachingResourceBuffer();

    private final LazyInitializationSupplier<JVMTeachingResourceBuffer> reloadTeachingResourceBufferSupplier = new LazyInitializationSupplier<>(() -> {
        TeachingResourceList data = remoteReference.loadTeachingResourceList(-1);
        assert data != null;
        JVMTeachingResourceBuffer buffer = new JVMTeachingResourceBuffer();
        buffer.attach(data);
        EventBus.subscribe(reloadTeachingResourceBuffer);
        logger.debug("[JVMTeachingResourceBuffer] initialized: [{}]", data.getVersion());
        return buffer;
    });

    @Override
    public TeachingResourceBuffer getTeachingResourceBuffer() {
        return reloadTeachingResourceBufferSupplier.initializeIfNecessary();
    }

    @Override
    public void resetTeachingResourceBuffer() {
        reloadTeachingResourceBufferSupplier.reset();
    }

    @Override
    public void reloadTeachingResourceList() {
        remoteReference.reloadTeachingResourceList();
    }

    @Override
    public TeachingResourceList loadTeachingResourceList(long version) {
        return remoteReference.loadTeachingResourceList(version);
    }
}
