package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.service.ai.api.ChipsActivityLoader;
import com.voxlearning.utopia.service.ai.cache.manager.UserPageVisitCacheManager;
import com.voxlearning.utopia.service.ai.data.ChipsMiniProgramQRBO;
import com.voxlearning.utopia.service.ai.entity.ChipsMiniProgramQR;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsMiniProgramQRDao;
import com.voxlearning.utopia.service.ai.impl.support.ConstantSupport;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Named
@ExposeService(interfaceClass = ChipsActivityLoader.class)
public class ChipsActivityLoaderImpl implements ChipsActivityLoader {
    @Inject
    private UserPageVisitCacheManager userPageVisitCacheManager;

    @Inject
    private ChipsMiniProgramQRDao chipsMiniProgramQRDao;

    private static int PAGE_SIZE = 15;

    @Override
    public MapMessage loadLeadPageUser() {
        Set<Long> userIdSet = userPageVisitCacheManager.getRecordIds(ConstantSupport.WECHAT_BUSINESS_LEAD_AD_PAGE_KEY);
        return MapMessage.successMessage().set("users", userIdSet);
    }

    @Override
    public MapMessage loadChipsMiniProgramPageable(int page) {
        if (page <= 0) {
            return MapMessage.successMessage().set("totalPage", 0).set("result", Collections.emptyList()).set("current", 0);
        }

        List<ChipsMiniProgramQRBO> result = new ArrayList<>();
        Page<ChipsMiniProgramQR> seachResult = chipsMiniProgramQRDao.loadExculedDisabledPageable(page, PAGE_SIZE);

        if (CollectionUtils.isNotEmpty(seachResult.getContent())) {
            for (ChipsMiniProgramQR qr : seachResult) {
                ChipsMiniProgramQRBO bo = new ChipsMiniProgramQRBO();
                bo.setContent(qr.getContent());
                bo.setCreateDate(qr.getCreateDate());
                bo.setId(qr.getId());
                bo.setImage(qr.getImage());
                result.add(bo);
            }
        }

        return MapMessage.successMessage().set("result", result).set("totalPage", seachResult.getTotalPages()).set("current", page < seachResult.getTotalPages() ? page : seachResult.getTotalPages());
    }
}
