package com.voxlearning.washington.controller.parent.homework;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkAssignLoader;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 获取题
 * @author chongfeng.qi
 * @data 20190118
 */
@Controller
@RequestMapping("/parent/question")
public class HomeworkQuestionController extends AbstractController {

    @ImportService(interfaceClass = HomeworkAssignLoader.class)
    private HomeworkAssignLoader homeworkAssignLoader;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    /**
     * 根据题包获取具体题的内容
     * @return
     */
    @RequestMapping(value = "/content/get.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getQuestionContentByBoxId() {
        // 逗号分隔
        String boxIds = getRequestString("boxIds");
        String bizType = getRequestString("bizType");
        String unitId = getRequestString("unitId");
        if (StringUtils.isAnyBlank(boxIds, bizType, unitId)) {
            return MapMessage.errorMessage("参数错误");
        }
        List<String> contents = homeworkAssignLoader.loadQuestionDocIdByBoxId(Arrays.asList(boxIds.split(",")), bizType);
        if (CollectionUtils.isEmpty(contents)) {
            return MapMessage.errorMessage("题包失效，请重新进入页面");
        }
        List<List<String>> resultList = new ArrayList<>();
        // 每30个分一组
        int index = 0;
        while (contents.size() > index) {
            resultList.add(contents.subList(index, Math.min(index += 30, contents.size())));
        }
        NewBookCatalog unit = newContentLoaderClient.loadBookCatalogByCatalogId(unitId);
        if (unit == null) {
            return MapMessage.errorMessage("单元异常");
        }
        return MapMessage.successMessage().add("result", resultList).add("unit", MapUtils.map("id", unit.getId(), "value", unit.getName()));
    }

}
