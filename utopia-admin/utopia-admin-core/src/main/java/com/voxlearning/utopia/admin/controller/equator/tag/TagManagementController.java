package com.voxlearning.utopia.admin.controller.equator.tag;

import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import com.voxlearning.utopia.admin.controller.fairyland.AbstractFairylandController;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.tag.api.constant.SelfStudyTagMatchStrategy;
import com.voxlearning.utopia.service.tag.api.data.TagMatchResult;
import com.voxlearning.utopia.service.tag.api.entity.TargetTagConfig;
import com.voxlearning.utopia.service.tag.client.TagAnythingLoaderClient;
import com.voxlearning.utopia.service.tag.client.TagAnythingUpdateClient;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标签管理
 *
 * @author lei.liu
 * @since 2018/06/19
 */
@Controller
@RequestMapping(value = "/equator/newwonderland/tag")
public class TagManagementController extends AbstractEquatorController {

    @Inject private TagAnythingLoaderClient tagAnythingLoaderClient;
    @Inject private TagAnythingUpdateClient tagAnythingUpdateClient;
    @Inject private UserServiceClient userServiceClient;

    private static final Map<String, String> PRODUCT_NAME;
    private static final Map<String, String> STRATEGY_NAME;
    private static Map<String, String> PRODUCT_NAME_MAP = new HashMap<>();
    private static Map<String, String> STRATEGY_NAME_MAP = new HashMap<>();

    static {
        PRODUCT_NAME_MAP.put("AfentiChinese", "阿分题-语文");
        PRODUCT_NAME_MAP.put("AfentiMath", "阿分题-数学");
        PRODUCT_NAME_MAP.put("AfentiExam", "阿分题-英语");
        PRODUCT_NAME_MAP.put("MathGarden", "速算100分");
        PRODUCT_NAME_MAP.put("UsaAdventure", "走遍美国学英语");
        PRODUCT_NAME_MAP.put("ChineseSynPractice", "语文同步练");
        PRODUCT_NAME_MAP.put("EncyclopediaChallenge", "百科大挑战");
        PRODUCT_NAME_MAP.put("GreatAdventure", "酷跑学单词");
        PRODUCT_NAME_MAP.put("Arithmetic", "速算脑力王");
        PRODUCT_NAME_MAP.put("ChineseHero", "字词英雄");
        PRODUCT_NAME_MAP.put("AnimalLand", "动物大冒险");
        PRODUCT_NAME_MAP.put("DinosaurLand", "恐龙时代");
        PRODUCT_NAME_MAP.put("ScienceLand", "魔力科技");
        PRODUCT_NAME_MAP.put("WrongTopic", "错题本");
        PRODUCT_NAME_MAP.put("ChineseStoryBook", "中文绘本");
        PRODUCT_NAME_MAP.put("EnglishStoryBook", "英文绘本");
        PRODUCT_NAME_MAP.put("FunOlympicMath", "思维大挑战");
        PRODUCT_NAME_MAP.put("PicListen", "付费点读机");
        PRODUCT_NAME_MAP.put("AfentiExamImproved", "阿分题英语提高版");
        PRODUCT_NAME_MAP.put("AfentiMathImproved", "阿分题数学提高版");
        PRODUCT_NAME_MAP.put("AfentiChineseImproved", "阿分题语文提高版");
        PRODUCT_NAME_MAP.put("ChinesePilot", "字词100分");
        PRODUCT_NAME_MAP.put("WordBuilder", "单词100分");
        PRODUCT_NAME_MAP.put("ListenWorld", "配音100分");
        PRODUCT_NAME_MAP.put("ELevelReading", "小U绘本");

        STRATEGY_NAME_MAP.put("NOT", "非");
        STRATEGY_NAME_MAP.put("AND", "是");
        STRATEGY_NAME_MAP.put("OR", "或");

        PRODUCT_NAME = Collections.unmodifiableMap(PRODUCT_NAME_MAP);
        STRATEGY_NAME = Collections.unmodifiableMap(STRATEGY_NAME_MAP);
    }

    /**
     * 获取所有的标签信息
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String selfStudyTagList(Model model) {

        List<TargetTagConfig> targetTagConfigList = tagAnythingLoaderClient.loadAllTargetTagConfigFromDatabase().getUninterruptibly();

        // 无奈之举：FreeMaker不能遍历key为非String类型的Map
        Map<String, Map<String, List<String>>> targetIdProductStrategyProduct = new HashMap<>();

        targetTagConfigList.forEach(targetTagConfig -> {
            if (targetTagConfig.getProductStrategy() != null && !targetTagConfig.getProductStrategy().isEmpty()) {
                Map<SelfStudyTagMatchStrategy, List<String>> old = targetTagConfig.getProductStrategy();
                Map<String, List<String>> productStrategyProduct = new HashMap<>();
                old.forEach((k, v) -> {
                    productStrategyProduct.put(k.name(), v);
                });
                targetIdProductStrategyProduct.put(targetTagConfig.getId(), productStrategyProduct);
            }
        });

        model.addAttribute("targetTagConfigList", targetTagConfigList);
        model.addAttribute("targetIdProductStrategyProduct", targetIdProductStrategyProduct);
        model.addAttribute("PRODUCT_NAME", PRODUCT_NAME);
        model.addAttribute("STRATEGY_NAME", STRATEGY_NAME);

        return "/equator/tag/list";

    }

    /**
     * 获取用户的标签信息
     */
    @RequestMapping(value = "user.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String userTargets(Model model) {
        List<TargetTagConfig> targetTagConfigList = tagAnythingLoaderClient.loadAllTargetTagConfigFromDatabase().getUninterruptibly();
        model.addAttribute("targetTagConfigList", targetTagConfigList);

        long studentId = getRequestLong("studentId");
        if (studentId > 0L) {
            model.addAttribute("studentId", studentId);
            StudentDetail student = studentLoaderClient.loadStudentDetail(studentId);
            if (student == null || student.getClazz() == null) {
                return "/equator/tag/user";
            }

            List<String> targetIds = targetTagConfigList.stream().map(TargetTagConfig::getId).collect(Collectors.toList());
            Map<String, TagMatchResult> matchResult = tagAnythingLoaderClient.loadTagMatchResultsByTagIds(studentId, targetIds).getUninterruptibly();

            List<String> matchedTagIds = new ArrayList<>();
            matchResult.forEach((tagId, match) -> {
                if (match.getWeight() > -1) matchedTagIds.add(tagId);
            });

            model.addAttribute("studentName", student.fetchRealname());
            model.addAttribute("matchedTags", matchedTagIds);
        }

        return "/equator/tag/user";
    }

    /**
     * 给用户设置一个标签
     */
    @RequestMapping(value = "mocktag.vpage", method = RequestMethod.GET)
    public String mockTag(Model model) {
        long studentId = getRequestLong("studentId");
        List<TargetTagConfig> targetTagConfigList = tagAnythingLoaderClient.loadAllTargetTagConfigFromDatabase().getUninterruptibly();

        model.addAttribute("studentId", studentId);
        model.addAttribute("targetTagConfigList", targetTagConfigList);

        return "/equator/tag/mocktag";
    }

    /**
     * 保存用户的标签
     */
    @RequestMapping(value = "domocktag.vpage", method = RequestMethod.POST)
    public String doMockTag() {
        long studentId = getRequestLong("studentId");
        String targetName = getRequestString("targetName");

        tagAnythingUpdateClient.mockUser(studentId, targetName);

        // 记录CRM中tag变更日志
        String operator = getCurrentAdminUser().getAdminUserName();
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(studentId);
        userServiceRecord.setOperatorId(operator);
        userServiceRecord.setOperationType("标签变更");
        userServiceRecord.setOperationContent("标签变更");
        userServiceRecord.setComments(operator + "在" + DateFormatUtils.format(new Date()) + "变更" + studentId + "的标签为" + targetName);
        userServiceClient.saveUserServiceRecord(userServiceRecord);

        return "redirect:/equator/newwonderland/tag/user.vpage?studentId=" + studentId;
    }

}
