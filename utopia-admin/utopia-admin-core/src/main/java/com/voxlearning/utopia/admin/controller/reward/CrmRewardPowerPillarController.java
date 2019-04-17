package com.voxlearning.utopia.admin.controller.reward;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.admin.mapper.reward.powerpillar.GetPrizeListMapper;
import com.voxlearning.utopia.admin.mapper.reward.powerpillar.UpdatePrizeMapper;
import com.voxlearning.utopia.service.reward.constant.RewardConstants;
import com.voxlearning.utopia.service.reward.consumer.RewardLoaderClient;
import com.voxlearning.utopia.service.reward.consumer.newversion.RewardCenterClient;
import com.voxlearning.utopia.service.reward.entity.RewardImage;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import com.voxlearning.utopia.service.reward.entity.newversion.PowerPrize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reward/powerpillar")
@Slf4j
public class CrmRewardPowerPillarController extends RewardAbstractController {

    @Inject
    private RewardCenterClient rewardCenterClient;
    @Inject
    private RewardLoaderClient rewardLoaderClient;

    @RequestMapping(value = "prizeList.vpage",method = RequestMethod.GET)
    public String getPrizeList(Model model){
        int page = getRequestInt("page",1);
        if(page <= 0)
            page = 1;

        Pageable pageable = new PageRequest(page - 1,30);

        List<PowerPrize> powerPrizeList = rewardCenterClient.loadAllPowerPrize();
        List<GetPrizeListMapper> mappers = powerPrizeList
                .stream()
                .map(t -> {
                    GetPrizeListMapper mapper = new GetPrizeListMapper();
                    mapper.setFragmentNum(t.getPrize().intValue());
                    mapper.setId(t.getId());
                    mapper.setIsReserve(t.getIsReserve());
                    mapper.setLevel(t.getLevel());
                    mapper.setName(t.getName());
                    mapper.setPrizeType(t.getType());
                    mapper.setProductId(t.getPrize());
                    mapper.setStock(t.getStock());
                    mapper.setInitStock(t.getInitStock());
                    return mapper;
                })
                .collect(Collectors.toList());
        Page<GetPrizeListMapper> powerPrizePage = PageableUtils.listToPage(mappers, pageable);
        model.addAttribute("powerPrizeListJSON", JSON.toJSONString(powerPrizePage.getContent()));
        model.addAttribute("currentPage", powerPrizePage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", powerPrizePage.getTotalPages());
        model.addAttribute("hasPrev", powerPrizePage.hasPrevious());
        model.addAttribute("hasNext", powerPrizePage.hasNext());

        return "reward/powerpillar/prizelist";
    }

    @RequestMapping(value = "prize.vpage",method = RequestMethod.GET)
    @ResponseBody
    public Model getPrize(Model model){
        long id = getRequestLong("id",0);


        PowerPrize powerPrize = rewardCenterClient.loadPowerById(id);
        model.addAttribute("powerPrize", powerPrize);

        return model;
    }

    @RequestMapping(value = "savePrize.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updatePrize(@RequestBody UpdatePrizeMapper mapper){
        Map<Long, List<RewardImage>> rewardImageMap = rewardLoaderClient.loadProductRewardImages(Collections.singletonList(mapper.getProductId()));
        if (rewardImageMap == null || !rewardImageMap.containsKey(mapper.getProductId())) {
            return MapMessage.errorMessage("商品id不存在");
        }
        List<RewardImage> images = rewardImageMap.get(mapper.getProductId());
        if (CollectionUtils.isEmpty(images)) {
            RewardImage image = new RewardImage();
            image.setLocation(RewardConstants.DEFAULT_PRODUCT_IMAGE_URL);
            images = Collections.singletonList(image);
        }

        // 这里要根据图片的属性进行选择
        PowerPrize powerPrize = new PowerPrize();
        if (images != null) {
            RewardImage selectedImage = images.stream().findFirst().orElse(null);
            powerPrize.setPicterUrl(selectedImage.getLocation());
        }
        powerPrize.setId(mapper.getId());
        powerPrize.setStock(mapper.getStock());
        powerPrize.setIsReserve(mapper.isReserve());
        powerPrize.setInitStock(mapper.getInitStock());
        powerPrize.setLevel(mapper.getLevel());
        powerPrize.setName(mapper.getName());
        powerPrize.setPrize(mapper.getProductId());
        powerPrize.setType(PowerPrize.PrizeType.REAL_GOODS.intValue());
        rewardCenterClient.updatePowerPrize(powerPrize);

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "deletePrize.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deletePrize(@RequestBody Long id){
        rewardCenterClient.deletePowerPrize(id);
        return MapMessage.successMessage();
    }
}
