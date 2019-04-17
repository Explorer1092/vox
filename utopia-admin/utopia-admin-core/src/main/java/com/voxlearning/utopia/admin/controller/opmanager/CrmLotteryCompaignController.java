package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.concurrent.LazyInitializationSupplier;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.utopia.admin.data.LotteryCampsignAwardMapper;
import com.voxlearning.utopia.admin.data.LotteryCampsignMapper;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.service.config.api.LotteryCampaignConfigLoader;
import com.voxlearning.utopia.service.config.api.LotteryCampaignConfigService;
import com.voxlearning.utopia.service.config.api.entity.LotteryCampaign;
import com.voxlearning.utopia.service.config.api.entity.LotteryCampaignAward;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.entity.RewardImage;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author songtao
 * @since 2018/3/21
 */
@Controller
@RequestMapping("/opmanager/lottery/compaign")
public class CrmLotteryCompaignController extends OpManagerAbstractController {

    @ImportService(interfaceClass = LotteryCampaignConfigService.class)
    private LotteryCampaignConfigService lotteryCampaignConfigService;
    @ImportService(interfaceClass = LotteryCampaignConfigLoader.class)
    private LotteryCampaignConfigLoader lotteryCampaignConfigLoader;
    @ImportService(interfaceClass = CRMRewardService.class)
    private CRMRewardService crmRewardService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String lotteryIndex() {
        return "opmanager/lottery/index";
    }

    @RequestMapping(value = "config.vpage", method = RequestMethod.GET)
    public String lotterySetting() {
        return "opmanager/lottery/config";
    }

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String newLotteryIndex(Model model) {
        List<LotteryCampaign> lotteryCampaigns = lotteryCampaignConfigLoader.loadAllLotteryCampaign();
        model.addAttribute("lotteryCampaigns", lotteryCampaigns);
        return "opmanager/lotteryCampaign/list";
    }

    @RequestMapping(value = "upsert.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upsert(@RequestBody final LotteryCampsignMapper mapper) {
        LotteryCampaign lotteryCampaign = new LotteryCampaign();
        BeanUtils.copyProperties(mapper, lotteryCampaign);
        lotteryCampaignConfigService.upsertLotteryCampaign(lotteryCampaign);
        return MapMessage.successMessage();
    }

    /**
     * 上架下架
     * @return
     */
    @RequestMapping(value = "updownlined.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upDownLined() {
        long id = getRequestLong("id");
        if (id <= 0) {
            return MapMessage.errorMessage();
        }
        boolean onLined = getRequestBool("onLined");

        LotteryCampaign lotteryCampaign = lotteryCampaignConfigLoader.loadLotteryCampaignById(id);

        if (lotteryCampaign == null) {
            return MapMessage.errorMessage();
        }
        lotteryCampaign.setOnlined(onLined);

        lotteryCampaign = lotteryCampaignConfigService.upsertLotteryCampaign(lotteryCampaign);
        if (lotteryCampaign != null) {
            return MapMessage.successMessage().setInfo("操作成功");
        } else {
            return MapMessage.errorMessage().setInfo("操作失败");
        }
    }

    @RequestMapping(value = "awardList.vpage", method = RequestMethod.GET)
    public String detail(Model model) {
        Long id = requestLong("lotteryCampaignId");
        List<LotteryCampaignAward> lotteryCampaignAwards = lotteryCampaignConfigLoader.loadAweadByCampaignIdIncludDisabled(id);
        lotteryCampaignAwards.sort(new Comparator<LotteryCampaignAward>() {
            @Override
            public int compare(LotteryCampaignAward o1, LotteryCampaignAward o2) {
                return o1.getDisabled().compareTo(o2.getDisabled());
            }
        }.thenComparing(new Comparator<LotteryCampaignAward>() {
            @Override
            public int compare(LotteryCampaignAward o1, LotteryCampaignAward o2) {
                return o2.getDisplayOrder().compareTo(o1.getDisplayOrder());
            }
        }));
        model.addAttribute("lotteryCampaignAwards", lotteryCampaignAwards);
        return "opmanager/lotteryCampaign/awardList";
    }

    @RequestMapping(value = "addLotteryCampaignAwead.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addLotteryCampaignAwead(@RequestBody final LotteryCampsignAwardMapper mapper) {
        LotteryCampaignAward lotteryCampaignAward = new LotteryCampaignAward();
        BeanUtils.copyProperties(mapper, lotteryCampaignAward);

        LazyInitializationSupplier<Map<Long, List<RewardImage>>> rewardImageSupplier = new LazyInitializationSupplier<>(new Supplier<Map<Long, List<RewardImage>>>() {
            @Override
            public Map<Long, List<RewardImage>> get() {
                return crmRewardService.$loadRewardImages().stream().collect(Collectors.groupingBy(RewardImage::getProductId));
            }
        });
        AtomicReference<String> rewardImageReference = new AtomicReference<>("");

        List<Map<String, Object>> awardListDto = mapper.getAwardList();
        if (CollectionUtils.isNotEmpty(awardListDto)) {
            List<LotteryCampaignAward.Award> awardList = awardListDto.stream().map(map -> {
                LotteryCampaignAward.Award award = new LotteryCampaignAward.Award();
                String ext = map.get("ext").toString();
                award.setExt(ext);
                award.setName(map.get("name").toString());
                award.setNum(NumberUtils.toInt(map.get("num").toString()));
                award.setImgUrl(map.get("imgUrl").toString());
                String type = map.get("type").toString();
                LotteryCampaignAward.AwardType awardType = LotteryCampaignAward.AwardType.valueOf(type);
                award.setType(awardType);
                // 如果只有一个实物商品
                if (awardType == LotteryCampaignAward.AwardType.GOODS && awardListDto.size() == 1) {
                    List<RewardImage> rewardImages = rewardImageSupplier.initializeIfNecessary().get(SafeConverter.toLong(ext));
                    if (CollectionUtils.isNotEmpty(rewardImages)) {
                        String location = rewardImages.get(0).getLocation();
                        award.setImgUrl(location);
                        rewardImageReference.set(location);
                    }
                }
                return award;
            }).collect(Collectors.toList());
            lotteryCampaignAward.setAwardList(awardList);
        }
        lotteryCampaignAward.setDisabled(true);
        lotteryCampaignAward.setAlreadyIssuedNum(0L);

        String rewardImage = rewardImageReference.get();
        if (StringUtils.isBlank(lotteryCampaignAward.getImgUrl())
                && StringUtils.isNotBlank(rewardImage)) {
            lotteryCampaignAward.setImgUrl(rewardImage);
        }

        lotteryCampaignAward = lotteryCampaignConfigService.upsertLotteryCampaignAwead(lotteryCampaignAward);
        if (lotteryCampaignAward != null) {
            return MapMessage.successMessage().add("id", lotteryCampaignAward.getId()).setInfo("操作成功");
        } else {
            return MapMessage.errorMessage().setInfo("操作失败");
        }
    }

    @RequestMapping(value = "uploadLotteryCampaignAwardImg.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SneakyThrows
    public MapMessage uploadLotteryCampaignAwardImg(MultipartFile file) {
        String id = getRequestString("id");
        LotteryCampaignAward lotteryCampaignAward = lotteryCampaignConfigLoader.loadLotteryCampaignAweadById(id);
        if (lotteryCampaignAward == null) {
            return MapMessage.errorMessage("奖项不存在!");
        }
        String filename = AdminOssManageUtils.upload(file, "lotteryCampaign");
        if (StringUtils.isBlank(filename)) {
            return MapMessage.errorMessage("图片上传失败");
        }

        lotteryCampaignAward.setImgUrl(filename);
        lotteryCampaignAward = lotteryCampaignConfigService.upsertLotteryCampaignAwead(lotteryCampaignAward);
        if (lotteryCampaignAward != null) {
            return MapMessage.successMessage().add("filename", filename).setInfo("操作成功");
        } else {
            return MapMessage.errorMessage().setInfo("操作失败");
        }
    }

    @RequestMapping(value = "uploadAwardImg.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SneakyThrows
    public MapMessage uploadAwardImg(MultipartFile file) {
        String filename = AdminOssManageUtils.upload(file, "lotteryCampaign");
        if (StringUtils.isBlank(filename)) {
            return MapMessage.errorMessage("图片上传失败");
        }

        return MapMessage.successMessage().add("filename", filename).setInfo("操作成功");
    }

    @RequestMapping(value = "addAward.vpage", method = RequestMethod.GET)
    public String addAward() {
        return "opmanager/lotteryCampaign/addAward";
    }

    @RequestMapping(value = "editAward.vpage", method = RequestMethod.GET)
    public String editAward(Model model) {
        String awardId = getRequestString("awardId");
        LotteryCampaignAward award = lotteryCampaignConfigLoader.loadLotteryCampaignAweadById(awardId);
        model.addAttribute("award", award);
        return "opmanager/lotteryCampaign/editAward";
    }

    @RequestMapping(value = "editAward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editAwardPost(@RequestBody LotteryCampaignAward award) {
        LotteryCampaignAward campaignAward = lotteryCampaignConfigLoader.loadLotteryCampaignAweadById(award.getId());

        campaignAward.setName(award.getName());
        campaignAward.setDescribeContent(award.getDescribeContent());
        campaignAward.setBigAward(award.getBigAward());
        campaignAward.setMinAward(award.getMinAward());
        campaignAward.setAwardRate(award.getAwardRate());
        campaignAward.setDisplayOrder(award.getDisplayOrder());
        campaignAward.setTotalAwardNum(award.getTotalAwardNum());

        campaignAward = lotteryCampaignConfigService.upsertLotteryCampaignAwead(campaignAward);
        if (campaignAward == null) {
            return MapMessage.errorMessage().setInfo("编辑失败");
        } else {
            return MapMessage.successMessage();
        }
    }

    @RequestMapping(value = "delAward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delAward() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage();
        }
        LotteryCampaignAward lotteryCampaignAward = lotteryCampaignConfigLoader.loadLotteryCampaignAweadById(id);
        lotteryCampaignAward.setDisabled(!lotteryCampaignAward.getDisabled());

        // 如果是禁用,需要重置排序号
        if (lotteryCampaignAward.getDisabled()) {
            lotteryCampaignAward.setDisplayOrder(0);
        }
        lotteryCampaignAward = lotteryCampaignConfigService.upsertLotteryCampaignAwead(lotteryCampaignAward);
        if (lotteryCampaignAward != null) {
            return MapMessage.successMessage().setInfo("操作成功");
        } else {
            return MapMessage.errorMessage().setInfo("操作失败");
        }
    }
}
