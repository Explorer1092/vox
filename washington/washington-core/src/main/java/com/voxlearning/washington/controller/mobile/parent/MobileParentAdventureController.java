package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.content.api.entity.Lesson;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.content.api.entity.Unit;
import com.voxlearning.utopia.service.nekketsu.consumer.AdventureLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by zhangpeng on 2016/5/3.
 * 沃克大冒险相关url
 */
@Controller
@RequestMapping(value = "/parentMobile/adventure")
public class MobileParentAdventureController extends AbstractMobileParentController {

    @Inject
    protected AdventureLoaderClient adventureLoaderClient;

    @RequestMapping(value = "/testOkJson.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage testOk() {
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "/getLearningWordsReport.vpage", method = RequestMethod.GET)
    public String getLearningWordsReport(Model model) {
        Long bookId = getRequestLong("bookId");
        Long userId = getRequestLong("userId");
        Integer stageOrderId = getRequestInt("stageOrder");
        MapMessage message = adventureLoaderClient.getUserStageByBookIdAndStageOrderId(userId, bookId, stageOrderId);
        if (message.isSuccess() == true) {
            model.addAttribute("wordNum", message.get("wordNum"));
            model.addAttribute("stuName", message.get("stuName"));
            model.addAttribute("words", message.get("words"));
            model.addAttribute("surplusStageNum", message.get("surplusStageNum"));
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(ProductConfig.getMainSiteBaseUrl()).append("/parentMobile/ucenter/shoppinginfo.vpage");
            urlBuilder.append("?sid=").append(userId);
            urlBuilder.append("&productType=").append(OrderProductServiceType.Walker);
            urlBuilder.append("&orderReferer=").append("walkerTrialReport");
            model.addAttribute("buyUrl", urlBuilder.toString());
        }
        return "parentmobile/walkerreport/walkerReport";
    }

    @RequestMapping(value = "/getLearningWordsReportJson.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage gettLearningWordsReportWithJSon() {

        Long bookId = getRequestLong("bookId");
        Long userId = getRequestLong("userId");
        Integer stageOrderId = getRequestInt("stageOrder");
        MapMessage message = adventureLoaderClient.getUserStageByBookIdAndStageOrderId(userId, bookId, stageOrderId);
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(ProductConfig.getMainSiteBaseUrl()).append("/parentMobile/ucenter/shoppinginfo.vpage");
        urlBuilder.append("?sid=").append(userId);
        urlBuilder.append("&productType=").append(OrderProductServiceType.Walker);
        urlBuilder.append("&orderReferer=").append("walkerTrialReport");
        message.add("buyUrl", urlBuilder.toString());
//        List<String> wordList = (List<String>) message.get("words");
//        List<String> wordRelation = new LinkedList<>();
//        List<Sentence> psrSentence = this.loadSentenceFromWordListAndBook(wordList, bookId, wordRelation);
//        Map<String, String> wordsMap = new HashMap<String, String>();
//        for (Sentence sentence : psrSentence) {
////            wordsMap.put(psrSentence.)
//        }
        return message;
    }

}
