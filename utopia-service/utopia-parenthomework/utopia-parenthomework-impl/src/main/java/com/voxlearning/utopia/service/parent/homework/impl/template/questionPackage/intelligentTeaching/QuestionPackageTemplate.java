package com.voxlearning.utopia.service.parent.homework.impl.template.questionPackage.intelligentTeaching;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.Processors;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SupportType;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.AbstractProcessorTemplate;
import com.voxlearning.utopia.service.parent.homework.impl.template.TemplateProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.template.questionPackage.HomeworkAppEventMQProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 题包模板
 *
 * @author Wenlong Meng
 * @since Feb 20, 2019
 */
@Named("IntelligentTeaching.QuestionPackageTemplate")
@Processors({
        InitProcessor.class,
        LoadUnitProcessor.class,
        QuestionPackageProcessor.class,
        HomeworkAppEventMQProcessor.class
})
@SupportType(bizType = "INTELLIGENT_TEACHING", op="questionPackage")
public class QuestionPackageTemplate extends AbstractProcessorTemplate implements TemplateProcessor {

    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    /**
     * 获取题包
     *
     * @param hc
     * @return
     */
    public void process(HomeworkContext hc) {
        try{
            // 流程处理
            processor.accept(hc);
            // 封装数据
            MapMessage mapMessage = hc.getMapMessage();
            if (mapMessage == null) {
                mapMessage = MapMessage.successMessage();
            }
            Map<String, Object> data = hc.getData();
            if (data == null) {
                data = new HashMap<>();
            }
            data.put("unitList", unitList(hc));
            NewBookProfile newBookProfile = newContentLoaderClient.loadBook(hc.getHomeworkParam().getBookId());
            data.put("selectedBook", newBookProfile != null ? MapUtils.map("id", newBookProfile.getId(), "name", newBookProfile.getName()) : null);
            hc.setMapMessage(mapMessage.add("data", data));
        }catch (Exception e){
            logger.error("loadQuestionPackage:{}", JsonUtils.toJson(hc.getHomeworkParam()), e);
            hc.setMapMessage(MapMessage.errorMessage("操作失败"));
        }

    }

    /**
     * 单元列表
     *
     * @param hc
     * @return
     */
    private List<Map<String, Object>> unitList(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();
        List<Map<String, Object>> unitList = new ArrayList<>();
        List<NewBookCatalog> units = hc.getUnits();
        if (CollectionUtils.isEmpty(units)) {
            units = newContentLoaderClient.loadChildren(Collections.singleton(param.getBookId()), BookCatalogType.UNIT).
                    getOrDefault(param.getBookId(), Collections.emptyList()).stream().
                    sorted(new NewBookCatalog.RankComparator()).collect(Collectors.toList());
        }
        List<NewBookCatalog> moduleUnits = null;
        if (param.getSubject().equals(Subject.ENGLISH.name())) {
            moduleUnits = newContentLoaderClient.loadChildren(Collections.singleton(param.getBookId()), BookCatalogType.MODULE).
                    getOrDefault(param.getBookId(), Collections.emptyList()).stream().
                    sorted(new NewBookCatalog.RankComparator()).collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(moduleUnits)) {
            Map<String, List<NewBookCatalog>> unitMap = units.stream().collect(Collectors.groupingBy(NewBookCatalog::getParentId));
            unitList.addAll(moduleUnits.stream().map(u -> MapUtils.m("id", u.getId(),
                    "name", u.getAlias(),
                    "subUnitList", unitMap.get(u.getId()).stream().map(m -> MapUtils.map("id", m.getId(), "name", HomeworkUtil.unitName(m))).collect(Collectors.toList()))
            ).collect(Collectors.toList()));
        } else {
            unitList.addAll(units.stream().map(u -> MapUtils.m("id", u.getId(), "name", HomeworkUtil.unitName(u))).collect(Collectors.toList()));
        }
        return unitList;
    }

}
