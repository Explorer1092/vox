package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.piclisten.api.ParentSelfStudyService;
import com.voxlearning.utopia.service.piclisten.api.PicListenCommonService;
import com.voxlearning.utopia.service.question.consumer.PicListenLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.service.parent.ParentSelfStudyPublicHelper;

import javax.inject.Inject;
import java.util.*;

/**
 * @author jiangpeng
 * @since 2017-03-17 下午3:47
 **/
public class AbstractMobileParentSelfStudyController extends AbstractMobileParentController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    protected PicListenLoaderClient picListenLoaderClient;

    @ImportService(interfaceClass = ParentSelfStudyService.class)
    protected ParentSelfStudyService parentSelfStudyService;

    @Inject
    protected ParentSelfStudyPublicHelper parentSelfStudyPublicHelper;

    @ImportService(interfaceClass = PicListenCommonService.class)
    protected PicListenCommonService picListenCommonService;


    @Override
    protected User currentParent() {
        User parent = null;
        if (RuntimeMode.isDevelopment())
            parent = raikouSystem.loadUser(getRequestLong("pid"));

        if (parent == null)
            parent = super.currentParent();

        if (parent == null || !parent.isParent())
            return null;
        else
            return parent;
    }

    protected Map<String, OrderProduct> getPackageProductByEnglishBookIds(Collection<String> bookIds){
        if (CollectionUtils.isEmpty(bookIds))
            return Collections.emptyMap();

        Map<String, List<OrderProduct>> itemId2ProductListMap = userOrderLoaderClient.loadOrderProductByAppItemIds(bookIds);
        Map<String, OrderProduct> item2ProductMap = new HashMap<>();
        itemId2ProductListMap.entrySet().forEach(t -> {
            List<OrderProduct> orderProductList = t.getValue();
            String bookId = t.getKey();
            OrderProduct packageProduct = orderProductList.stream().filter(this::isPackage).findFirst().orElse(null);
            if (packageProduct == null)
                return;

            item2ProductMap.put(bookId, packageProduct);
        });
        return item2ProductMap;
    }

    protected Boolean isPackage(OrderProduct orderProduct){
        return orderProduct != null && orderProduct.fetchAttribute("piclisten_package_id")!=null;
    }
}
