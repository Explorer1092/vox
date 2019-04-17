package com.voxlearning.utopia.admin.controller.chips;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishClazzService;
import com.voxlearning.utopia.service.ai.entity.ChipsClazzCompare;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClass;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * @author guangqing
 * @since 2019/2/13
 */
@Controller
@RequestMapping("/chips/clazz/monitor")
public class ClazzMonitorController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = ChipsEnglishClazzService.class)
    private ChipsEnglishClazzService chipsEnglishClazzService;

    @RequestMapping(value = "compareClazzIndex.vpage", method = RequestMethod.GET)
    public String compareClazzIndex(Model model) {
        return "chips/clazz/compareClazzIndex";
    }

    @RequestMapping(value = "compareClazzList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage compareClazzList() {
        int pageNum = getRequestInt("pageNum");
       return chipsEnglishClazzService.loadAllChipsClazzCompare(pageNum);
    }

    @RequestMapping(value = "compareClazzEdit.vpage", method = RequestMethod.GET)
    public String compareClazzEdit(Model model) {
        model.addAttribute("id", getRequestString("id"));
        return "chips/clazz/compareClazzEdit";
    }

    @RequestMapping(value = "compareClazzQuery.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadCompareClazz() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage().add("info","no id");
        }
        return chipsEnglishClazzService.loadChipsClazzCompareById(id);
    }

    @RequestMapping(value = "compareClazzEditSave.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage compareClazzEditSave() {
        String title = getRequestString("title");
        String basicClazzId = getRequestString("basicClazzId");
        String basicClazzName = getRequestString("basicClazzName");
        String compareClazzIds = getRequestString("compareClazzIds");
        String compareClazzNames = getRequestString("compareClazzNames");
        String id = getRequestString("id");
        ChipsClazzCompare clazzCompare = new ChipsClazzCompare();
        if (StringUtils.isNotBlank(id)) {
            clazzCompare.setId(id);
        }
        clazzCompare.setTitle(title);
        clazzCompare.setBasicClazzId(basicClazzId);
        clazzCompare.setBasicClazzName(basicClazzName);
        clazzCompare.setCompareClazzIds(compareClazzIds);
        clazzCompare.setCompareClazzNames(compareClazzNames);
        return chipsEnglishClazzService.saveChipsClazzCompare(clazzCompare);
    }
    @RequestMapping(value = "loadClazz.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadClazzByProduct() {
        String productId = getRequestString("productId");
        List<ChipsEnglishClass> clazzList = chipsEnglishClazzService.selectChipsEnglishClassByProductId(productId);
        MapMessage message = MapMessage.successMessage();
        message.add("clazzList", clazzList);
        return message;
    }

    @RequestMapping(value = "compareClazzDetailIndex.vpage", method = RequestMethod.GET)
    public String compareClazzDetailIndex(Model model) {
        String id = getRequestString("id");
        model.addAttribute("id", id);
//        MapMessage message = chipsEnglishClazzService.loadChipsClazzCompareById(id);
//        Object clazzCompareObj = message.get("clazzCompare");
//        String title = "";
//        List<Integer> list = new ArrayList<>();
//        if(clazzCompareObj != null){
//            ChipsClazzCompare clazzCompare = (ChipsClazzCompare) clazzCompareObj;
//            title = clazzCompare.getTitle();
//            clazzCompare.getBasicClazzId();
//            if (StringUtils.isNotBlank(clazzCompare.getBasicClazzId())) {
//                int count = chipsEnglishClazzService.dayIndexCount(Long.parseLong(clazzCompare.getBasicClazzId()));
//                for(int i = 0 ; i < count ; i++ ) {
//                    list.add(i + 1);
//                }
//            }
//        }
//        model.addAttribute("dayIndexList", list);
//        model.addAttribute("title", title);
        return "chips/clazz/compareClazzDetailIndex";
    }

    @RequestMapping(value = "compareClazzDetailQuery.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage compareClazzDetailQuery() {
        String id = getRequestString("id");
        String type = getRequestString("dataType");
        int dayIndex = getRequestInt("dayIndex");
        return chipsEnglishClazzService.buildClazzCompareData(id, type, dayIndex);
    }
    @RequestMapping(value = "compareClazzDetailHead.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage compareClazzDetailHead() {
        MapMessage mapMessage = MapMessage.successMessage();
        String id = getRequestString("id");
        mapMessage.add("id", id);
        MapMessage message = chipsEnglishClazzService.loadChipsClazzCompareById(id);
        Object clazzCompareObj = message.get("clazzCompare");
        String title = "";
        List<Integer> list = new ArrayList<>();
        String basicClazzId = "";
        if(clazzCompareObj != null){
            ChipsClazzCompare clazzCompare = (ChipsClazzCompare) clazzCompareObj;
            title = clazzCompare.getTitle();
            basicClazzId = clazzCompare.getBasicClazzId();
            if (StringUtils.isNotBlank(clazzCompare.getBasicClazzId())) {
                int count = chipsEnglishClazzService.dayIndexCount(Long.parseLong(clazzCompare.getBasicClazzId()));
                for(int i = 0 ; i < count ; i++ ) {
                    list.add(i + 1);
                }
            }
        }
        mapMessage.add("dayIndexList", list);
        mapMessage.add("title", title);
        mapMessage.add("basicClazzId", basicClazzId);
        return mapMessage;
    }
    @RequestMapping(value = "compareClazzListDel.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage compareClazzListDel() {
        String id = getRequestString("id");
        return chipsEnglishClazzService.removeClazzCompareData(id);
    }

}
