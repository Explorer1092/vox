package com.voxlearning.utopia.service.parent.homework.impl.template.questionPackage.intelligentTeaching;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserProgress;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 加载单元
 *
 * @author Wenlong Meng
 * @since Feb 20, 2019
 */
@Named("IntelliagentTeaching.LoadUnitSubProcessor")
public class LoadUnitProcessor implements HomeworkProcessor {
    //local variables
    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    UtopiaCache utopiaCache = CacheSystem.CBS.getCache("flushable");

    /**
     * exec
     *
     * @param hc args
     */
    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();
        // 获取单元列表
        List<NewBookCatalog> units = newContentLoaderClient.loadChildren(Collections.singleton(param.getBookId()), BookCatalogType.UNIT).
                getOrDefault(param.getBookId(), Collections.emptyList()).stream().
                sorted(new NewBookCatalog.RankComparator()).collect(Collectors.toList());
        hc.setUnits(units);
        defaultUnit(hc);
    }

    /**
     * 默认单元
     *
     * @param hc
     */
    private void defaultUnit(HomeworkContext hc){
        HomeworkParam param = hc.getHomeworkParam();
        Long studentId = param.getStudentId();
        String bizType = param.getBizType();
        String bookId = param.getBookId();
        String unitId = ObjectUtils.get(()->param.getUnitIds().get(0));
        if(ObjectUtils.anyBlank(unitId)){
            //上次选择单元
            Map<String, String> select = utopiaCache.load(HomeworkUtil.generatorID(studentId, bizType, "selectUnit"));
            unitId = ObjectUtils.get(()->select.get("unitId"));
        }
        if(ObjectUtils.anyBlank(unitId)){
            //上次学习单元
            HomeworkUserProgress hup = hc.getProgress();
            unitId = ObjectUtils.get(()->hup.getUserProgresses().stream().filter(u->u.getBookId().equals(bookId)).findFirst().get().getUnitId());
        }

        NewBookCatalog selectUnit = null;
        if(!ObjectUtils.anyBlank(unitId)){
            for(NewBookCatalog unit : hc.getUnits()){
                if(StringUtils.equals(unit.getId(), unitId)){
                    selectUnit = unit;
                    break;
                }
            }
        }
        if(selectUnit == null){
            selectUnit = hc.getUnits().get(0);//第一单元
        }

        hc.setUnitId(selectUnit.getId());
        // 选择的单元
        hc.getData().put("selectedUnit", MapUtils.m("id", selectUnit.getId(), "name",  selectUnit.getName()));
    }
}
