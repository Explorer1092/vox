package com.voxlearning.utopia.agent.controller.mobile.partner;


import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.partner.AgentPartnerService;
import com.voxlearning.utopia.agent.service.partner.domain.PartnerDomain;
import com.voxlearning.utopia.agent.service.partner.model.Partner;
import com.voxlearning.utopia.agent.support.AgentRequestSupport;
import com.voxlearning.utopia.agent.view.partner.input.UpsertPartnerParams;
import com.voxlearning.utopia.agent.view.partner.output.PartnerListVo;
import com.voxlearning.utopia.core.helper.AmapMapApi;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * 机构管理
 * @author deliang.che
 * @since 2018/12/10
 */
@Controller
@RequestMapping(value = "/mobile/partner")
public class PartnerMobileController extends AbstractAgentController {
    @Inject
    private AgentRequestSupport agentRequestSupport;
    @Inject
    private AgentPartnerService agentPartnerService;
    @Inject
    private PartnerDomain partnerDomain;

    /**
     * 添加机构
     * @return
     */
    @RequestMapping(value = "upsert_partner.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upsertPartner(HttpServletRequest request){
        Long id = getRequestLong("id");
        String name = getRequestString("name");                 //名称
        Integer regionCode = getRequestInt("regionCode");     //区域编码
        Double longitude = getRequestDouble("longitude");   //经度
        Double latitude = getRequestDouble("latitude");     //纬度
        String type = getRequestString("type");                 //类别

        String homePhotoUrl = getRequestString("homePhotoUrl"); //照片
        List<String> otherPhotoUrls = JsonUtils.fromJsonToList(getRequestString("otherPhotoUrls"), String.class);
        //坐标转化
        String coordinateType;
        if (agentRequestSupport.isIOSRequest(request)) {
            coordinateType = "wgs84ll";
        } else {
            coordinateType = "autonavi";
        }
        MapMessage mapAddress = AmapMapApi.getAddress(ConversionUtils.toString(latitude), ConversionUtils.toString(longitude), coordinateType);
        if (!mapAddress.isSuccess()) {
            return mapAddress;
        }
        String longitudeFinal = (String) mapAddress.get("longitude");
        String latitudeFinal = (String) mapAddress.get("latitude");
        String addressFinal = ConversionUtils.toString(mapAddress.get("address"));

        UpsertPartnerParams params = UpsertPartnerParams.builder()
                .id(id)
                .name(name)
                .regionCode(regionCode)
                .longitude(longitude)
                .latitude(latitude)
                .type(type)
                .homePhotoUrl(homePhotoUrl)
                .otherPhotoUrls(otherPhotoUrls)
                .build();
        MapMessage checkMsg = agentPartnerService.upsertCheck(getCurrentUserId(), params);
        if(!checkMsg.isSuccess()){
            return checkMsg;
        }

        Partner partner = partnerDomain.build(longitudeFinal, latitudeFinal, addressFinal, params, getCurrentUser());
        return agentPartnerService.upsert(partner);
    }

    /**
     * 合作伙伴列表
     * @return
     */
    @RequestMapping(value = "partner_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchPartners(){
        Integer cityCode = getRequestInt("cityCode");
        if (cityCode == 0){
            return MapMessage.errorMessage("请选择城市！");
        }
        String name = getRequestString("name");
        List<Partner> partners = agentPartnerService.searchPartners(cityCode, name, getCurrentUserId());
        List<PartnerListVo> vos = Optional.ofNullable(partners).orElse(new ArrayList<>())
                .stream()
                .sorted(Comparator.comparing(Partner::getCreateTime).reversed())
                .map(partner -> PartnerListVo.Builder.build(partner))
                .collect(Collectors.toList());
        return MapMessage.successMessage().add("dataList", vos);
    }

    /**
     * 机构基础详情
     * @return
     */
    @RequestMapping(value = "partner_base.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage partnerBase(){
        Long id = getRequestLong("id", 0L);
        if (id == 0L){
            return MapMessage.errorMessage("机构ID不正确！");
        }
        return agentPartnerService.queryBaseById(id);
    }

    /**
     * 机构详情
     * @return
     */
    @RequestMapping(value = "partner_detail.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage partnerDetail(){
        Long id = getRequestLong("id", 0L);
        if (id == 0L){
            return MapMessage.errorMessage("机构ID不正确！");
        }
        return agentPartnerService.queryById(id);
    }

    /**
     * 联系人列表
     * @return
     */
    @RequestMapping(value = "linkman_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage linkManList(){
        Long partnerId = getRequestLong("partnerId", 0L);
        return agentPartnerService.queryLinkMan(partnerId);
    }

    /**
     * 新增或者更改联系人
     * @return
     */
    @RequestMapping(value = "upsert_linkman.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upsertLinkman(){
        Long linkManId = getRequestLong("linkManId", 0L);
        Long partnerId = getRequestLong("partnerId", 0L);
        return agentPartnerService.insertPaertnerLinkMan(linkManId, partnerId);
    }

    /**
     * 部门负责区域列表
     * @return
     */
    @RequestMapping(value = "group_manage_region_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage groupManageRegionList(){
        Integer regionRank = getRequestInt("regionRank");
        if (regionRank == 0){
            return MapMessage.errorMessage("请先选择区域级别！");
        }
        Long userId = getRequestLong("userId");
        if (userId == 0L){
            userId = getCurrentUserId();
        }
        //编辑回显
        int provinceCode = getRequestInt("provinceCode");
        int cityCode = getRequestInt("cityCode");
        int countyCode = getRequestInt("countyCode");

        List<Integer> isSelectedIds = new ArrayList<>();
        if (provinceCode >= 0) {
            isSelectedIds.add(provinceCode);
        }
        if (cityCode >= 0) {
            isSelectedIds.add(cityCode);
        }
        if (countyCode >= 0) {
            isSelectedIds.add(countyCode);
        }
        return agentPartnerService.groupManageRegionList(userId,regionRank,isSelectedIds);
    }
}