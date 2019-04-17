package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.data.ChipsOrderExtBO;
import com.voxlearning.utopia.service.ai.entity.ChipsGroupShopping;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190216")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsOrderLoader extends IPingable {
    ChipsOrderExtBO loadOrderExtInfo(String orderId);

    /**
     * 查询
     * @return
     */
    MapMessage loadGroupShoppingList();


    MapMessage loadGroupSponsorInfo(String groupCode);

    List<ChipsGroupShopping> loadGroupShoppingListForCrm();
}
