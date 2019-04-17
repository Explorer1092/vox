package com.voxlearning.utopia.service.business.impl.processor.fairyland;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.business.impl.processor.AbstractExecuteTask;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductStatus;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.client.FairylandProductServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform.STUDENT_APP;

/**
 * 获取学生移动端上线状态的应用
 *
 * @author Ruib
 * @since 2019/1/2
 */
@Named
public class FSMA_FairylandProduct extends AbstractExecuteTask<FetchStudentAppContext> {

    @Inject private FairylandProductServiceClient client;

    @Override
    public void execute(FetchStudentAppContext context) {

        List<FairylandProduct> products = client.getFairylandProductBuffer()
                .loadFairylandProducts(STUDENT_APP, null)
                .stream()
                .filter(p -> StringUtils.equals(p.getStatus(), FairylandProductStatus.ONLINE.name()))
                .collect(Collectors.toList());

        context.setFps(products);
        context.setFpm(products.stream().collect(Collectors.toMap(FairylandProduct::getAppKey, Function.identity(),
                (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                },
                LinkedHashMap::new)));
    }
}
