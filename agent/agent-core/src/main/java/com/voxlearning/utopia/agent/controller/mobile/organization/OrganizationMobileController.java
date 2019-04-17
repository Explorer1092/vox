package com.voxlearning.utopia.agent.controller.mobile.organization;


import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.organization.AgentOrganizationService;
import com.voxlearning.utopia.agent.service.organization.AgentOuterResourceService;
import com.voxlearning.utopia.agent.support.AgentRequestSupport;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrganizationType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 机构管理
 * @author deliang.che
 * @since 2018/12/10
 */
@Controller
@RequestMapping(value = "/mobile/organization")
public class OrganizationMobileController extends AbstractAgentController {
    @Inject
    private AgentOrganizationService agentOrganizationService;
    @Inject
    private AgentRequestSupport agentRequestSupport;
    @Inject
    private AgentOuterResourceService agentOuterResourceService;
    /**
     * 部门负责区域列表
     * @return
     */
    @RequestMapping(value = "group_manage_region_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage groupManageRegionList(){
        Long userId = getRequestLong("userId");
        if (userId == 0L){
            userId = getCurrentUserId();
        }
        Integer regionRank = getRequestInt("regionRank");
        if (regionRank == 0){
            return MapMessage.errorMessage("请先选择区域级别！");
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
        return agentOrganizationService.groupManageRegionList(userId,regionRank,isSelectedIds);
    }

    /**
     * 添加机构
     * @return
     */
    @RequestMapping(value = "add_organization.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addOrganization(HttpServletRequest request){
        String name = getRequestString("name");                 //名称
        String type = getRequestString("type");                 //类别
        Integer regionRank = getRequestInt("regionRank");     //区域级别

        Integer regionCode = getRequestInt("regionCode");     //区域编码

        Integer provinceCode = getRequestInt("provinceCode"); //省级编码
        Integer cityCode = getRequestInt("cityCode");         //市级编码
        Integer countyCode = getRequestInt("countyCode");     //区级编码

        Double longitude = getRequestDouble("longitude");   //经度
        Double latitude = getRequestDouble("latitude");     //纬度
//        String address = getRequestString("address");   //地址
        String photoUrl = getRequestString("photoUrl"); //照片
        String webAddress = getRequestString("webAddress"); //官网地址

        if (StringUtils.isBlank(name)){
            return MapMessage.errorMessage("名称不得为空！");
        }
        MapMessage nameMsg = agentOrganizationService.checkNameRepeat(null,name);
        if(!nameMsg.isSuccess()){
            return nameMsg;
        }
        if(StringUtils.isBlank(type)){
            return MapMessage.errorMessage("类别不得为空！");
        }
        if(AgentOrganizationType.nameOf(type) == null){
            return MapMessage.errorMessage("类别不正确！");
        }
        if (regionRank == 0){
            return MapMessage.errorMessage("级别不得为空！");
        }
        if ((regionRank == 2 || regionRank == 3 || regionRank == 4) && regionCode == 0){
            return MapMessage.errorMessage("区域编码不得为空！");
        }
        if (longitude == 0 || latitude == 0){
            return MapMessage.errorMessage("经纬度不得为空！");
        }
        //坐标转化
        String coordinateType;
        if (agentRequestSupport.isIOSRequest(request)) {
            coordinateType = "wgs84ll";
        } else {
            coordinateType = "autonavi";
        }

        return agentOrganizationService.saveOrganization(null,name,regionRank,regionCode,provinceCode,cityCode,countyCode,longitude,latitude,"",coordinateType,photoUrl,true,type,webAddress);
    }

    /**
     * 组织列表
     * @return
     */
    @RequestMapping(value = "organization_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage organizationList(HttpServletRequest request){
        Integer regionCode = getRequestInt("regionCode");
        if (regionCode == 0){
            return MapMessage.errorMessage("请选择城市！");
        }
        String name = getRequestString("name");
        Double longitude = getRequestDouble("longitude");   //经度
        Double latitude = getRequestDouble("latitude");     //纬度
        if (longitude == 0 || latitude == 0){
            return MapMessage.errorMessage("经纬度不得为空！");
        }
        //坐标转化
        String coordinateType;
        if (agentRequestSupport.isIOSRequest(request)) {
            coordinateType = "wgs84ll";
        } else {
            coordinateType = "autonavi";
        }
        return MapMessage.successMessage().add("dataList",agentOrganizationService.organizationList(regionCode,name,longitude,latitude,coordinateType));
    }

    /**
     * 机构详情
     * @return
     */
    @RequestMapping(value = "organization_detail.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage organizationDetail(){
        Long id = getRequestLong("id");
        if (id == 0L){
            return MapMessage.errorMessage("机构ID不正确！");
        }
        return agentOrganizationService.organizationDetail(id);
    }

    /**
     * 机构资源列表
     * @return
     */
    @RequestMapping(value = "organization_resource_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage organizationResourceList(){
        Long id = getRequestLong("id");                     //机构ID
        int sortType = getRequestInt("sortType", 1);     //排序类型，1：按姓名排序  2：按最近拜访时间
        if (id == 0L){
            return MapMessage.errorMessage("机构ID不正确！");
        }
        return MapMessage.successMessage().add("dataList",agentOrganizationService.organizationResourceList(id,sortType));
    }

    /**
     * 编辑机构
     * @param request
     * @return
     */
    @RequestMapping(value = "edit_organization.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editOrganization(HttpServletRequest request){
        Long id = getRequestLong("id");                     //机构ID
        String name = getRequestString("name");                 //名称
        String type = getRequestString("type");                 //类别
        Integer regionRank = getRequestInt("regionRank");     //区域级别

        Integer regionCode = getRequestInt("regionCode");     //区域编码

        Integer provinceCode = getRequestInt("provinceCode"); //省级编码
        Integer cityCode = getRequestInt("cityCode");         //市级编码
        Integer countyCode = getRequestInt("countyCode");     //区级编码

        Double longitude = getRequestDouble("longitude");   //经度
        Double latitude = getRequestDouble("latitude");     //纬度
        String address = getRequestString("address");   //地址
        String photoUrl = getRequestString("photoUrl"); //照片
        Boolean needTransform = getRequestBool("needTransform");//坐标是否需要转换
        String webAddress = getRequestString("webAddress"); //官网地址

        if (StringUtils.isBlank(name)){
            return MapMessage.errorMessage("名称不得为空！");
        }
        MapMessage nameMsg = agentOrganizationService.checkNameRepeat(id,name);
        if(!nameMsg.isSuccess()){
            return nameMsg;
        }
        if(StringUtils.isBlank(type)){
            return MapMessage.errorMessage("类别不得为空！");
        }
        if(AgentOrganizationType.nameOf(type) == null){
            return MapMessage.errorMessage("类别不正确！");
        }
        if (regionRank == 0){
            return MapMessage.errorMessage("级别不得为空！");
        }
        if ((regionRank == 2 || regionRank == 3 || regionRank == 4) && regionCode == 0){
            return MapMessage.errorMessage("区域编码不得为空！");
        }
//        if (provinceCode == 0 || cityCode == 0 || countyCode == 0){
//            return MapMessage.errorMessage("省市区编码不得为空！");
//        }
        if (longitude == 0 || latitude == 0){
            return MapMessage.errorMessage("经纬度不得为空！");
        }
//        if (StringUtils.isBlank(address)){
//            return MapMessage.errorMessage("办公地址不得为空！");
//        }
        if(StringUtils.isBlank(webAddress)){
            return MapMessage.errorMessage("官网地址不得为空！");
        }

        //坐标转化
        String coordinateType;
        if (agentRequestSupport.isIOSRequest(request)) {
            coordinateType = "wgs84ll";
        } else {
            coordinateType = "autonavi";
        }

        return agentOrganizationService.saveOrganization(id,name,regionRank,regionCode,provinceCode,cityCode,countyCode,longitude,latitude,address,coordinateType,photoUrl,needTransform,type,webAddress);
    }

    /**
     * 机构推荐资源列表(全国级别没有推荐)
     * @return
     */
    @RequestMapping(value = "recommend_resource_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage recommendResourceList(){
        Long id = getRequestLong("id");
        return agentOrganizationService.recommendResourceId(id);
    }

    /**
     * 资源关联机构
     * @return
     */
    @RequestMapping(value = "link_resource_organization.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage linkResourceOrganization(){
        Long resourceId = getRequestLong("resourceId");
        Long orgId = getRequestLong("orgId");
        return agentOuterResourceService.linkResourceAndOrganization(resourceId,orgId);
    }

    @RequestMapping(value = "search_organization.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchOrganization(HttpServletRequest request){
        String name = getRequestString("name");
        Integer pageSize = getRequestInt("pageSize");
        Integer pageNo = getRequestInt("pageNo");
        Double longitude = getRequestDouble("longitude");   //经度
        Double latitude = getRequestDouble("latitude");     //纬度
//        if (longitude == 0 || latitude == 0){
//            return MapMessage.errorMessage("经纬度不得为空！");
//        }
        //坐标转化
        String coordinateType;
        if (agentRequestSupport.isIOSRequest(request)) {
            coordinateType = "wgs84ll";
        } else {
            coordinateType = "autonavi";
        }

        if(pageSize <= 0){
            pageSize = 50;
        }
        Pageable pageable = new PageRequest(pageNo,pageSize);
        List<Map<String,Object>> result = agentOrganizationService.searchOrganization(name,longitude,latitude,coordinateType);
        return agentOuterResourceService.pageResource(result,pageable);
    }
}