package com.voxlearning.utopia.service.ai.impl.service.processor.v2.dailyclass;

import com.alibaba.fastjson.JSONArray;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.ai.data.TargetProductADConfig;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClass;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsContentDailyClassContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Named
public class CCDCR_BottomBe_1 extends AbstractAiSupport implements IAITask<ChipsContentDailyClassContext> {

    @Override
    public void execute(ChipsContentDailyClassContext context) {
        boolean black = chipsUserService.isInADBlackList(context.getUser().getId());
        if (black) {
            context.terminateTask();
            return;
        }

        ChipsEnglishClass chipsEnglishClass = chipsUserService.loadClazzIdByUserAndProduct(context.getUser().getId(), context.getBookRef().getProductId());
        TargetProductADConfig conf= Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName("target_product_ad_cfg"))
                .filter(e -> StringUtils.isNotBlank(e.getValue()))
                .map(e -> JSONArray.parseArray(e.getValue(), TargetProductADConfig.class))
                .map(e -> e.stream().filter(con -> StringUtils.isNoneBlank(con.getAdProductId(), con.getBeImage(), con.getPath(), con.getType()))
                        .filter(con -> context.getBookRef().getProductId().equals(con.getAdProductId()))
                        .filter(con -> con.getClazzId() == null || chipsEnglishClass == null || con.getClazzId().equals(chipsEnglishClass.getId()))
                        .findFirst().orElse(null))
                .orElse(null);

        if (conf == null || conf.getBeginDate() == null || conf.getEndDate() == null) {
            return;
        }

        Date now = new Date();
        if (now.before(conf.getBeginDate()) || now.after(conf.getEndDate())) {
            return;
        }

        StringBuffer linkStr = new StringBuffer();
        linkStr.append(ProductConfig.getMainSiteBaseUrl())
                .append(conf.getPath())
                .append("?endDate=")
                .append(DateUtils.dateToString(conf.getEndDate(), "yyyy年MM月dd日"))
                .append("&type=")
                .append(conf.getType());
        Map<String, Object> buttomBe = new HashMap<>();
        buttomBe.put("linkUrl", linkStr.toString());
        buttomBe.put("image", conf.getBeImage());
        context.getExtMap().put("bottomBe", buttomBe);
        context.terminateTask();
    }
}
