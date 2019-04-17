package com.voxlearning.utopia.admin.controller.equator.pet;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.equator.service.configuration.api.entity.pet.PetConfig;
import com.voxlearning.equator.service.configuration.client.ResourceConfigServiceClient;
import com.voxlearning.equator.service.pet.api.entity.StudentPet;
import com.voxlearning.equator.service.pet.client.PetLoaderClient;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author xiaoying.han
 * @CreateDate 2018/12/17
 */
@Controller
@RequestMapping(value = "equator/newwonderland/pet")
public class StudentPetManagerController extends AbstractEquatorController {

    @Inject
    private PetLoaderClient petLoaderClient;
    @Inject
    private ResourceConfigServiceClient resourceConfigServiceClient;

    @RequestMapping(value = "list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String fetchUserPetList(Model model) {
        String responseString = "equator/pet/index";
        Long studentId = getRequestLong("studentId");
        model.addAttribute("studentId", studentId == 0 ? "" : studentId);
        if (studentId == 0) {
            return responseString;
        }
        List<StudentPet> petList = petLoaderClient.getPetLoader().loadUserPets(studentId);
        if (petList == null || petList.isEmpty()) {
            return responseString;
        }
        //加载petConfig
        Map<String, PetConfig> petConfigMap = getPetConfigMap();
        List<Map<String, Object>> studentPetInfoList = petList.stream().map(t -> {
            Map<String, Object> currentObject = JsonUtils.safeConvertObjectToMap(t);
            String petId = t.fetchPetId();
            PetConfig petConfig = petConfigMap.get(petId);
            currentObject.put("petType", petConfig.getPetType());
            currentObject.put("totalStage", petConfig.getTotalStage());
            return currentObject;
        }).collect(Collectors.toList());
        model.addAttribute("studentPet", studentPetInfoList);
        model.addAttribute("petConfigList", petConfigMap.values());
        return responseString;
    }

    private Map<String, PetConfig> getPetConfigMap() {
        List<PetConfig> petConfigList = resourceConfigServiceClient.getBuffer(PetConfig.class).loadDataList();
        return petConfigList.stream().collect(Collectors.toMap(PetConfig::getId, pet -> pet));
    }
}
