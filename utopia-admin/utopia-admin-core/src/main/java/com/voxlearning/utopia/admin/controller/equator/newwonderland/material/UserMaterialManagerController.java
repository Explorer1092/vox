package com.voxlearning.utopia.admin.controller.equator.newwonderland.material;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.equator.common.api.enums.material.MaterialType;
import com.voxlearning.equator.common.api.vo.MaterialQuantityInfoVo;
import com.voxlearning.equator.service.configuration.api.entity.material.Material;
import com.voxlearning.equator.service.configuration.client.ResourceConfigServiceClient;
import com.voxlearning.equator.service.material.api.data.MaterialModificationContext;
import com.voxlearning.equator.service.material.client.MaterialServiceClient;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import com.voxlearning.utopia.admin.data.MaterialInfoData;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 新成长世界 伙伴
 *
 * @author lei.liu
 * @version 18-7-23
 */
@Controller
@RequestMapping(value = "equator/newwonderland/material")
public class UserMaterialManagerController extends AbstractEquatorController {

    @Inject private MaterialServiceClient materialServiceClient;
    @Inject private ResourceConfigServiceClient resourceConfigServiceClient;

    @RequestMapping(value = "list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String getUserMaterial(Model model) {

        String responseString = "equator/material/index";

        Long studentId = getRequestLong("studentId");
        String materialType = getRequestString("materialType");

        model.addAttribute("studentId", studentId == 0 ? "" : studentId);
        model.addAttribute("materialType", materialType);
        model.addAttribute("materialTypes", Arrays.stream(MaterialType.values()).filter(MaterialType::getShow)
                .collect(Collectors.toList())
        );

        if (studentId == 0) {
            return responseString;
        }

        StudentDetail student = studentLoaderClient.loadStudentDetail(studentId);
        if (student == null) {
            getAlertMessageManager().addMessageError(studentId + "不是学生角色");
            return responseString;
        }
        model.addAttribute("studentName", student.fetchRealname());

        // 道具列表
        List<MaterialQuantityInfoVo> userMaterialList;
        // materialType <-> List<MaterialInfoData>
        Map<String, List<MaterialInfoData>> materialDB;

        List<Material> materialConfigList = resourceConfigServiceClient.getMaterialsFromBuffer();

        // 没有指定道具类型，搜索全部
        if (StringUtils.isEmpty(materialType)) {
            userMaterialList = materialServiceClient.getMaterialService().fetchByUserId(studentId);

            materialDB = materialConfigList.stream()
                    // // 伙伴卡不算
                    // .filter(material -> !material.getMaterialType().equals(MaterialType.PartnerDayCard.name()))
                    // // 装备卡不算
                    // .filter(material -> !material.getMaterialType().equals(MaterialType.PartnerEquipmentCard.name()))
                    .filter(material -> MaterialType.safeParse(material.getMaterialType()).getShow())
                    .map(material -> new MaterialInfoData(material, 0))
                    .collect(Collectors.groupingBy(MaterialInfoData::getMaterialType));

        } else {
            if (MaterialType.safeParse(materialType) == MaterialType.UNKNOWN) {
                model.addAttribute("error", "不存在的道具类型，请联系管理员。");
                return responseString;
            }
            userMaterialList = materialServiceClient.getMaterialService().fetchByUserIdAndMaterialTypes(studentId, Collections.singleton(materialType));

            materialDB = materialConfigList.stream()
                    .filter(material -> material.getMaterialType().equals(materialType))
                    .map(material -> new MaterialInfoData(material, 0))
                    .collect(Collectors.groupingBy(MaterialInfoData::getMaterialType));
        }


        Map<String, Material> materialConfigInfoMap = materialConfigList.stream().collect(Collectors.toMap(Material::getId, m -> m));
        userMaterialList.forEach(materialQuantityInfoVo -> {
            Material material = materialConfigInfoMap.get(materialQuantityInfoVo.getMaterialId());
            if (material == null) {
                logger.error("unrecognized material id {}, plz check.", materialQuantityInfoVo.getMaterialId());
                return;
            }

            MaterialInfoData myMaterialInfoData = materialDB.get(material.getMaterialType()).stream()
                    .filter(materialInfoData -> material.getId().equals(materialInfoData.getId()))
                    .findFirst()
                    .orElse(null);
            if (myMaterialInfoData == null) {
                return;
            }

            myMaterialInfoData.setQuality(materialQuantityInfoVo.getQuantity());

        });

        Map<String, List<MaterialInfoData>> materialDescDB = new HashMap<>();
        materialDB.forEach((k, v) -> materialDescDB.put(MaterialType.safeParse(k).getDesc(), v));
        model.addAttribute("materialDB", materialDescDB);

        return responseString;
    }

    /**
     * 给用户发放道具
     */
    @RequestMapping(value = "grant.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage grantMaterial() {
        Long studentId = getRequestLong("studentId");
        String materialId = getRequestString("materialId");
        Integer materialQuality = getRequestInt("materialQuality");
        try {
            MaterialModificationContext materialModificationContext = MaterialModificationContext.create(getCurrentAdminUser().getAdminUserName())
                    .attachUserId(studentId)
                    .attachMaterialId(materialId)
                    .attachQuantity(materialQuality);
            return materialServiceClient.getMaterialService().modify(materialModificationContext);
        } catch (Exception e) {
            // really don't know what to do
            e.printStackTrace();
            return MapMessage.errorMessage("授予道具失败，请联系管理员。");
        }
    }
}
