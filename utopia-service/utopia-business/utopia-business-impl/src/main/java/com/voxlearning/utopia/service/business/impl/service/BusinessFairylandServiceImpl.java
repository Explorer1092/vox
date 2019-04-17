package com.voxlearning.utopia.service.business.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.api.constant.AppSystemType;
import com.voxlearning.utopia.business.api.BusinessFairylandService;
import com.voxlearning.utopia.service.business.impl.processor.fairyland.FetchStudentAppContext;
import com.voxlearning.utopia.service.business.impl.processor.fairyland.FetchStudentMobileAppProcessor;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * @author Ruib
 * @since 2019/1/2
 */
@Named
@ExposeService(interfaceClass = BusinessFairylandService.class)
public class BusinessFairylandServiceImpl extends BusinessServiceSpringBean implements BusinessFairylandService {

    @Inject private FetchStudentMobileAppProcessor processor;

    @Override
    public List<Map<String, Object>> fetchStudentMobileAvailableApps(Long studentId, String version, String ast) {
        FetchStudentAppContext ctx = new FetchStudentAppContext();
        ctx.setStudentId(studentId);
        ctx.setVersion(version);
        ctx.setAst(AppSystemType.valueOf(ast));
        processor.process(ctx);
        return null;
    }
}
