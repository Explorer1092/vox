package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.buffer.ManagedNearBuffer;
import com.voxlearning.alps.api.buffer.NearBufferBuilder;
import com.voxlearning.utopia.service.business.api.NewTeacherResourceService;
import com.voxlearning.utopia.service.business.api.mapper.NewTeacherResourceWrapper;
import com.voxlearning.utopia.service.business.buffer.NewTeacherResourceWrapperBuffer;
import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class NewTeacherResourceServiceClient implements InitializingBean {

    @Getter
    @ImportService(interfaceClass = NewTeacherResourceService.class)
    private NewTeacherResourceService remoteReference;

    @Getter
    private ManagedNearBuffer<List<NewTeacherResourceWrapper>, NewTeacherResourceWrapperBuffer> teacherResourceBuffer;

    @Override
    public void afterPropertiesSet() throws Exception {
        NearBufferBuilder<List<NewTeacherResourceWrapper>, NewTeacherResourceWrapperBuffer> builder = NearBufferBuilder.newBuilder();
        builder.nearBufferClass(NewTeacherResourceWrapperBuffer.class);
        builder.reloadNearBuffer(1, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> remoteReference.loadNewTeacherResourceWrapperBufferData(-1L));
        builder.reloadNearBuffer((version, attributes) -> remoteReference.loadNewTeacherResourceWrapperBufferData(version));
        teacherResourceBuffer = builder.build();
    }

}
