package com.voxlearning.utopia.service.piclisten.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.concurrent.LazyInitializationSupplier;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.utopia.service.piclisten.api.TextBookManagementLoader;
import com.voxlearning.utopia.service.piclisten.impl.service.CRMTextBookManagementServiceImpl;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.mapper.TextBookMapper;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedTextBookManagementList;
import com.voxlearning.utopia.service.piclisten.buffer.TextBookManagementBuffer;
import com.voxlearning.utopia.service.piclisten.buffer.internal.JVMTextBookManagementBuffer;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by jiang wei on 2017/4/5.
 */
@Named
@Service(interfaceClass = TextBookManagementLoader.class)
@ExposeService(interfaceClass = TextBookManagementLoader.class)
public class TextBookManagementLoaderImpl extends SpringContainerSupport implements TextBookManagementLoader, TextBookManagementBuffer.Aware {

    @Inject
    private CRMTextBookManagementServiceImpl crmTextBookManagementService;

    @Override
    protected void afterPropertiesSetCallback() throws Exception {
        super.afterPropertiesSetCallback();
        VersionedTextBookManagementList data = getTextBookManagementBuffer().dump();
        logger.info("[TextBookManagementBuffer] initialized: [{}]", data.getVersion());
    }

    private final LazyInitializationSupplier<TextBookManagementBuffer> textBookManagementBufferSupplier = new LazyInitializationSupplier<>(() -> {
        VersionedTextBookManagementList data = crmTextBookManagementService.loadVersionedTextBookManagementList();
        JVMTextBookManagementBuffer buffer = new JVMTextBookManagementBuffer();
        buffer.attach(data);
        return buffer;
    });

    @Override
    public TextBookManagementBuffer getTextBookManagementBuffer() {
        return textBookManagementBufferSupplier.initializeIfNecessary();
    }

    @Override
    public void resetTextBookManagementBuffer() {
        textBookManagementBufferSupplier.reset();
    }

    @Override
    public Map<String, TextBookManagement> getTextBookByIds(Collection<String> ids) {
        return getTextBookManagementBuffer().loadByIds(ids);
    }


    @Override
    public List<TextBookMapper> getPublisherList() {
        return getTextBookManagementBuffer().getTextBookMapperList();
    }

    @Override
    public Map<Integer, List<TextBookManagement>> getClazzLevelMap() {
        return getTextBookManagementBuffer().getClazzLevelMap();
    }

    @Override
    public List<TextBookManagement> getTextBookManagementList() {
        return getTextBookManagementBuffer().getTextBookManagementList();
    }

    // ========================================================================
    // Buffer supported methods
    // ========================================================================

    @Override
    public VersionedTextBookManagementList loadVersionedTextBookManagementList(long version) {
        FlightRecorder.closeLog();
        VersionedTextBookManagementList data = getTextBookManagementBuffer().dump();
        return (version == 0 || version < data.getVersion()) ? data : null;
    }
}
