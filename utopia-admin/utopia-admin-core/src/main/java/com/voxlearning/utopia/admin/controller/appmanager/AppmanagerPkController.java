/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller.appmanager;

import com.voxlearning.utopia.admin.controller.crm.CrmAbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Longlong Yu
 * @since 下午8:39,13-6-25.
 */
@Controller
@RequestMapping("/appmanager/pk")
public class AppmanagerPkController extends CrmAbstractController {

//    @Inject private PkConfigManagerClient pkConfigManagerClient;
//    @Inject private PkServiceClient pkService;
//    @Inject private PkLoaderClient pkLoader;
//
//    /**
//     * ******************PK工具******************************
//     */
//    @RequestMapping(value = "importtools.vpage", method = RequestMethod.GET)
//    public String importTtools() {
//        return "crm/pk/importtools";
//    }
//
//    @RequestMapping(value = "/importskills.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage importSkills(MultipartFile file) {
//        MapMessage mapMessage = MapMessage.successMessage();
//
//        Workbook workbook = null;
//        try {
//            if (file.isEmpty()) {
//                mapMessage.setSuccess(false);
//                mapMessage.setErrorCode("技能数据不能为空！！");
//                return mapMessage;
//            }
//            workbook = Workbook.getWorkbook(file.getInputStream());// 得到Excel文件
//            Sheet sheet = workbook.getSheet(0);// Excel中的工作表 下标从0开始
//            int row = sheet.getRows(); // 工作表共有的行
//            Cell[] cells = null;
//            SkillConfig skill;
//            SkillResult skillResult;
//            List<SkillConfig> skillList = new LinkedList<>();
//            for (int i = 0; i < row; i++) {
//                cells = sheet.getRow(i);
//                skill = new SkillConfig();
//                skillResult = new SkillResult();
//
//                skill.setId(cells[0].getContents());
//                skill.setSwf(cells[1].getContents() + ".swf");
//                skill.setName(cells[2].getContents());
//                skill.setDesc(cells[3].getContents());
//                skill.setMaxLevel(Integer.parseInt(cells[4].getContents()));
//                skill.setType(SkillType.valueOf(cells[5].getContents()));
//                skill.setRequiredRoleLevel(Integer.valueOf(cells[6].getContents()));
//                skill.setAttackRange(AttackRange.valueOf(cells[7].getContents()));
//                skill.setSeries(SkillSeries.valueOf(cells[8].getContents()));
//                if (!"all".equals(cells[9].getContents())) {
//                    skill.setCareer(Career.valueOf(cells[9].getContents()));
//                } else {
//                    skill.setCareer(null);
//                }
//                if (!"all".equals(cells[10].getContents())) {
//                    skill.setGender(Gender.valueOf(cells[10].getContents()));
//                } else {
//                    skill.setGender(null);
//                }
//                skill.setOpenStar(Integer.valueOf(cells[11].getContents()));
//                skill.setUpgradeStar(Integer.valueOf(cells[12].getContents()));
//
//                if (StringUtils.isNotEmpty(cells[13].getContents())) {
//                    skillResult.setDamageType(SkillSeries.valueOf(cells[13].getContents()));
//                } else {
//                    skillResult.setDamageType(null);
//                }
//                skillResult.setBase(Integer.valueOf(cells[14].getContents()));
//                skillResult.setLevelTimes(Double.valueOf(cells[15].getContents()));
//                skillResult.setCounterattackRatio(Double.valueOf(cells[16].getContents()));
//                skillResult.setGeneralAttackIncrRatio(Double.valueOf(cells[17].getContents()));
//                skillResult.setCritAttackIncrRatio(Double.valueOf(cells[18].getContents()));
//                skillResult.setDefenseIncrRatio(Double.valueOf(cells[19].getContents()));
//                skillResult.setDodgeIncrRatio(Double.valueOf(cells[20].getContents()));
//                skillResult.setHitIncrRatio(Double.valueOf(cells[21].getContents()));
//                skillResult.setAdditionalHurtRatio(Double.valueOf(cells[22].getContents()));
//                skillResult.setHitDecrRatio(Double.valueOf(cells[23].getContents()));
//                skillResult.setRecoverHealthRatio(Double.valueOf(cells[24].getContents()));
//                if (26 == cells.length) {
//                    skill.setBuffSwf(cells[25].getContents() + ".swf");
//                }
//                if (27 == cells.length) {
//                    if (StringUtils.isNotEmpty(cells[25].getContents())) {
//                        skill.setBuffSwf(cells[25].getContents() + ".swf");
//                    }
//                    skill.setDebuffSwf(cells[26].getContents() + ".swf");
//                }
//                skill.setSkillResult(skillResult);
//
//                skillList.add(skill);
//            }
//
//            if (skillList.isEmpty()) {
//                return MapMessage.errorMessage("没有数据");
//            }
//
//            // ================================================================
//            // 使用了本地缓存，必须重启相关的服务才会生效
//            // ================================================================
//            pkConfigManagerClient.importSkillConfigs(skillList);
//
//            return MapMessage.successMessage("技能信息导入成功，请重启相关服务");
//        } catch (Exception e) {
//            e.printStackTrace();
//            mapMessage.setSuccess(false);
//            mapMessage.setErrorCode("技能信息导入失败！" + e.getMessage());
//            return mapMessage;
//        } finally {
//            if (workbook != null) {
//                workbook.close();
//            }
//        }
//    }
//
//    @RequestMapping(value = "/importequipments.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage importEquipments(MultipartFile file) {
//        MapMessage mapMessage = MapMessage.successMessage();
//
//        Workbook workbook = null;
//        try {
//            if (file.isEmpty()) {
//                mapMessage.setSuccess(false);
//                mapMessage.setErrorCode("装备数据不能为空！！");
//                return mapMessage;
//            }
//
//            workbook = Workbook.getWorkbook(file.getInputStream());// 得到Excel文件
//            Sheet sheet = workbook.getSheet(0);// Excel中的工作表 下标从0开始
//            int row = sheet.getRows(); // 工作表共有的行
//
//            SkillSeries[] skillSerieses = SkillSeries.values();
//            Cell[] cells = null;
//            EquipmentConfig equipment;
//            EquipmentConfig.AttributeGroup attributeGroup;
//            EquipmentAttribute attribute;
//            List<EquipmentConfig> equipmentConfigList = new LinkedList<>();
//            EquipmentAttributeType baseAttributeType;
//            EquipmentAttributeType secondaryAttributeType;
//            for (int i = 0; i < row; i++) {
//                cells = sheet.getRow(i);
//                equipment = new EquipmentConfig();
//
//                equipment.setId(cells[22].getContents());
//                equipment.setSwf(cells[0].getContents() + ".swf");
//                equipment.setImg(cells[0].getContents() + ".png");
//                equipment.setName(cells[1].getContents());
//                equipment.setDesc(cells[2].getContents());
//                equipment.setBasePrice(Integer.valueOf(cells[15].getContents()));
//                equipment.setType(EquipmentType.valueOf(cells[16].getContents()));
//                if (!"all".equals(cells[17].getContents())) {
//                    equipment.setCareer(Career.valueOf(cells[17].getContents()));
//                }
//                equipment.setRequiredLevel(Integer.valueOf(cells[18].getContents()));
//                equipment.setRandom(Integer.valueOf(cells[19].getContents()) == 1);
//                if (equipment.isRandomTrue()) {
//                    equipment.setResistanceType(skillSerieses[RandomUtils.nextInt(0, skillSerieses.length - 1)]);
//                }
//                equipment.setMinQuality(Integer.valueOf(cells[20].getContents()));
//                equipment.setMaxQuality(Integer.valueOf(cells[21].getContents()));
//                equipment.setCategory(SourceCategory.valueOf(cells[23].getContents()));
//                equipment.setRelation(Integer.valueOf(cells[24].getContents()));
//
//                baseAttributeType = EquipmentAttributeType.valueOf(cells[3].getContents());
//                secondaryAttributeType = EquipmentAttributeType.valueOf(cells[4].getContents());
//
//                //白
//                attributeGroup = new EquipmentConfig.AttributeGroup();
//                attribute = new EquipmentAttribute();
//                attribute.setType(baseAttributeType);//基础属性
//                attribute.setValue(Double.valueOf(cells[5].getContents()));
//                attributeGroup.setBaseAttribute(attribute);
//                attribute = new EquipmentAttribute();
//                attribute.setType(secondaryAttributeType);//次级属性
//                attribute.setValue(Double.valueOf(cells[6].getContents()));
//                attributeGroup.setSecondaryAttribute(attribute);
//                equipment.setWhite(attributeGroup);
//
//                //绿
//                attributeGroup = new EquipmentConfig.AttributeGroup();
//                attribute = new EquipmentAttribute();
//                attribute.setType(baseAttributeType);//基础属性
//                attribute.setValue(Double.valueOf(cells[7].getContents()));
//                attributeGroup.setBaseAttribute(attribute);
//                attribute = new EquipmentAttribute();
//                attribute.setType(secondaryAttributeType);//次级属性
//                attribute.setValue(Double.valueOf(cells[8].getContents()));
//                attributeGroup.setSecondaryAttribute(attribute);
//                equipment.setGreen(attributeGroup);
//
//                //蓝
//                attributeGroup = new EquipmentConfig.AttributeGroup();
//                attribute = new EquipmentAttribute();
//                attribute.setType(baseAttributeType);//基础属性
//                attribute.setValue(Double.valueOf(cells[9].getContents()));
//                attributeGroup.setBaseAttribute(attribute);
//                attribute = new EquipmentAttribute();
//                attribute.setType(secondaryAttributeType);//次级属性
//                attribute.setValue(Double.valueOf(cells[10].getContents()));
//                attributeGroup.setSecondaryAttribute(attribute);
//                equipment.setBlue(attributeGroup);
//
//                //紫
//                attributeGroup = new EquipmentConfig.AttributeGroup();
//                attribute = new EquipmentAttribute();
//                attribute.setType(baseAttributeType);//基础属性
//                attribute.setValue(Double.valueOf(cells[11].getContents()));
//                attributeGroup.setBaseAttribute(attribute);
//                attribute = new EquipmentAttribute();
//                attribute.setType(secondaryAttributeType);//次级属性
//                attribute.setValue(Double.valueOf(cells[12].getContents()));
//                attributeGroup.setSecondaryAttribute(attribute);
//                equipment.setViolet(attributeGroup);
//
//                //金
//                attributeGroup = new EquipmentConfig.AttributeGroup();
//                attribute = new EquipmentAttribute();
//                attribute.setType(baseAttributeType);//基础属性
//                attribute.setValue(Double.valueOf(cells[13].getContents()));
//                attributeGroup.setBaseAttribute(attribute);
//                attribute = new EquipmentAttribute();
//                attribute.setType(secondaryAttributeType);//次级属性
//                attribute.setValue(Double.valueOf(cells[14].getContents()));
//                attributeGroup.setSecondaryAttribute(attribute);
//                equipment.setGold(attributeGroup);
//
//                equipmentConfigList.add(equipment);
//            }
//
//            if (equipmentConfigList.isEmpty()) {
//                return MapMessage.errorMessage("没有数据");
//            }
//
//            // ================================================================
//            // 使用了本地缓存，必须重启相关的服务才会生效
//            // ================================================================
//            pkConfigManagerClient.importEquipmentConfigs(equipmentConfigList);
//
//            return MapMessage.successMessage("装备信息导入成功，请重启相关服务");
//        } catch (Exception e) {
//            e.printStackTrace();
//            mapMessage.setSuccess(false);
//            mapMessage.setErrorCode("装备信息导入失败！" + e.getMessage());
//            return mapMessage;
//        } finally {
//            if (workbook != null) {
//                workbook.close();
//            }
//        }
//    }
//
//    @RequestMapping(value = "/importcareerlevels.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage importCareerLevels(MultipartFile file) {
//        MapMessage mapMessage = MapMessage.successMessage();
//
//        Workbook workbook = null;
//        try {
//            if (file.isEmpty()) {
//                mapMessage.setSuccess(false);
//                mapMessage.setErrorCode("战斗属性数据不能为空！！");
//                return mapMessage;
//            }
//            workbook = Workbook.getWorkbook(file.getInputStream());// 得到Excel文件
//            Sheet sheet = workbook.getSheet(0);// Excel中的工作表 下标从0开始
//            int row = sheet.getRows(); // 工作表共有的行
//            Cell[] cells = null;
//            CareerLevelConfig init;
//            List<CareerLevelConfig> careerLevelConfigs = new ArrayList<>();
//            for (int i = 0; i < row; i++) {
//                cells = sheet.getRow(i);
//                init = new CareerLevelConfig();
//
//                init.setLevel(Integer.parseInt(cells[0].getContents()));
//                init.setHealth(Integer.parseInt(cells[1].getContents()));
//                init.setAttack(Integer.parseInt(cells[2].getContents()));
//                init.setDefense(Integer.parseInt(cells[3].getContents()));
//                init.setEvasion(((NumberCell) cells[4]).getValue());
//                init.setCrit(((NumberCell) cells[5]).getValue());
//                init.setCareer(Career.valueOf(cells[6].getContents()));
//
//                careerLevelConfigs.add(init);
//            }
//
//            if (careerLevelConfigs.isEmpty()) {
//                return MapMessage.errorMessage("没有数据");
//            }
//
//            // ================================================================
//            // 使用了本地缓存，必须重启相关的服务才会生效
//            // ================================================================
//            pkConfigManagerClient.importCareerLevelConfigs(careerLevelConfigs);
//
//            return MapMessage.successMessage("战斗属性导入成功，请重启相关的服务");
//        } catch (Exception e) {
//            e.printStackTrace();
//            mapMessage.setSuccess(false);
//            mapMessage.setErrorCode("战斗属性导入失败！" + e.getMessage());
//            return mapMessage;
//        } finally {
//            if (workbook != null) {
//                workbook.close();
//            }
//        }
//    }
//
//    @RequestMapping(value = "/sendEquip.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage sendEquip(MultipartFile file) {
//        MapMessage mapMessage = MapMessage.successMessage();
//        Workbook workbook = null;
//        if (file.isEmpty()) {
//            mapMessage.setSuccess(false);
//            mapMessage.setErrorCode("文件呢？");
//            return mapMessage;
//        }
//        Map<Long, List<Map<String, String>>> dataLoadedFromExcel = new HashMap<>();
//        try {//读取Excel。此过程中发生任何异常，均停止操作
//            workbook = Workbook.getWorkbook(file.getInputStream());// 得到Excel文件
//            int sheetCount = workbook.getNumberOfSheets();
//            for (int i = 0; i < sheetCount; i++) {
//                Sheet sheet = workbook.getSheet(i);
//                int row = sheet.getRows();
//                for (int j = 0; j < row; j++) {
//                    Cell[] cells = sheet.getRow(j);
//                    long userId = NumberUtils.toLong(cells[0].getContents(), 0L);
//                    if (userId <= 0L) {
//                        throw new RuntimeException();
//                    }
//                    List<Map> prizeList = JsonUtils.fromJsonToList(cells[1].getContents(), Map.class);
//                    List<Map<String, String>> listToAdd = new ArrayList<>();
//                    for (Map mp : prizeList) {
//                        Map<String, String> adMp = new HashMap<>();
//                        adMp.put("id", mp.get("id").toString());
//                        adMp.put("color", mp.get("color").toString());
//                        adMp.put("count", mp.get("count").toString());
//                        listToAdd.add(adMp);
//                    }
//                    List<Map<String, String>> fromLoaded = dataLoadedFromExcel.get(userId);
//                    if (null != fromLoaded) {
//                        fromLoaded.addAll(listToAdd);
//                    } else {
//                        dataLoadedFromExcel.put(userId, listToAdd);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            mapMessage.setSuccess(false);
//            mapMessage.setErrorCode("读取文件失败，请检查文件格式是否正确？");
//            return mapMessage;
//        }
//
//        int success = 0;
//        int fail = 0;
//        Map<String, Integer> failReasonCounter = new HashMap<>();
//        for (Map.Entry<Long, List<Map<String, String>>> entry : dataLoadedFromExcel.entrySet()) {
//            try {
//                MapMessage addEquipResult = pkService.addMultiEquipmentWithColorAndCareerOption(entry.getKey(), entry.getValue(), true);
//                if (addEquipResult.isSuccess()) {
//                    success++;
//                } else {
//                    String failReason = StringUtils.trimToEmpty(addEquipResult.getInfo());
//                    Integer reasonCounter = failReasonCounter.get(failReason);
//                    if (null == reasonCounter) {
//                        reasonCounter = 0;
//                        failReasonCounter.put(failReason, reasonCounter);
//                    }
//                    failReasonCounter.put(failReason, failReasonCounter.get(failReason) + 1);
//                    fail++;
//                }
//            } catch (Exception e) {
//                fail++;
//            }
//        }
//        mapMessage.setSuccess(true).add("总人数", dataLoadedFromExcel.size()).add("成功人数", success).add("失败人数", fail);
//        if (!failReasonCounter.isEmpty()) {
//            mapMessage.add("其中", failReasonCounter);
//        }
//        return mapMessage;
//    }
//
//    @RequestMapping(value = "/importPetInitial.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage importPetInitial(MultipartFile file) {
//        MapMessage mapMessage = MapMessage.successMessage();
//        if (file.isEmpty()) {
//            mapMessage.setSuccess(false);
//            mapMessage.setErrorCode("文件呢？");
//            return mapMessage;
//        }
//        try {//读取Excel。此过程中发生任何异常，均停止操作
//            Workbook workbook = Workbook.getWorkbook(file.getInputStream());// 得到Excel文件
//            Sheet sheet = workbook.getSheet(0);
//            int row = sheet.getRows();
//            List<PetConfig> saveList = new ArrayList<>();
//            for (int i = 0; i < row; i++) {
//                Cell[] cells = sheet.getRow(i);
//                PetConfig initial = new PetConfig();
//                initial.setPgId(cells[0].getContents());
//                initial.setEggId(Integer.parseInt(cells[2].getContents()));
//                String[] npcidstrlst = cells[1].getContents().split(",");
//                List<Integer> npcList = new LinkedList<>();
//                for (String st : npcidstrlst) {
//                    npcList.add(Integer.parseInt(st));
//                }
//                initial.setNpcList(npcList);
//                String[] sklst = cells[3].getContents().split(",");
//                initial.setSkillList(new ArrayList<>(Arrays.asList(sklst)));
//                saveList.add(initial);
//            }
//
//            if (saveList.isEmpty()) {
//                return MapMessage.errorMessage("没有数据");
//            }
//
//            // ================================================================
//            // 由于使用了相关缓存，必须重启相关服务才能生效
//            // ================================================================
//            pkConfigManagerClient.importPetConfigs(saveList);
//
//            return MapMessage.successMessage("宠物配置数据导入，请重启相关服务");
//        } catch (Exception e) {
//            mapMessage.setSuccess(false);
//            mapMessage.setErrorCode("读取文件失败，请检查文件格式是否正确？");
//            return mapMessage;
//        }
//    }
//
//    @RequestMapping(value = "/importPetSkill.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage importPetSkill(MultipartFile file) {
//        MapMessage mapMessage = MapMessage.successMessage();
//        if (file.isEmpty()) {
//            mapMessage.setSuccess(false);
//            mapMessage.setErrorCode("文件呢？");
//            return mapMessage;
//        }
//        try {//读取Excel。此过程中发生任何异常，均停止操作
//            Workbook workbook = Workbook.getWorkbook(file.getInputStream());// 得到Excel文件
//            Sheet sheet = workbook.getSheet(0);
//            int row = sheet.getRows();
//            List<PetSkillConfig> saveList = new ArrayList<>();
//            for (int i = 0; i < row; i++) {
//                Cell[] cells = sheet.getRow(i);
//                PetSkillConfig initial = new PetSkillConfig();
//                initial.setSkId(cells[0].getContents());
//                initial.setSkName(cells[1].getContents());
//                initial.setSkType(cells[2].getContents());
//                initial.setMaxLevel(Integer.parseInt(cells[3].getContents()));
//                initial.setDegree(Integer.parseInt(cells[4].getContents()));
//                initial.setOpenLevel(Integer.parseInt(cells[5].getContents()));
//                initial.setSkDesc(cells[6].getContents());
//                initial.setTriggerTiming(PetSkillTriggerTiming.valueOf(cells[7].getContents()));
//                initial.setPetSkillTriggerCondition(PetSkillTriggerCondition.valueOf(cells[8].getContents()));
//                initial.setEffectType(PkSkillEffectType.valueOf(cells[9].getContents()));
//                initial.setValueChangeOn(SkillBuffShowObject.valueOf(cells[10].getContents()));
//                initial.setSkillBuffShowObject(SkillBuffShowObject.valueOf(cells[11].getContents()));
//                initial.setEffectRoundDuration(Integer.parseInt(cells[12].getContents()));
//                initial.setEffectLevelCoe(Double.parseDouble(cells[13].getContents()));
//                initial.setEffectTail(Integer.parseInt(cells[14].getContents()));
//                initial.setTriggerProbLevelCoe(Double.parseDouble(cells[15].getContents()));
//                initial.setTriggerProbTail(Double.parseDouble(cells[16].getContents()));
//                initial.setEffectLimit(Double.parseDouble(cells[17].getContents()));
//                initial.setMaxTriggerCount(Integer.parseInt(cells[18].getContents()));
//                String swf = StringUtils.trimToNull(cells[19].getContents());
//                initial.setSwf(null != swf ? (swf + ".swf") : null);
//                String buffSwf = StringUtils.trimToNull(cells[20].getContents());
//                initial.setBuffSwf(null != buffSwf ? (buffSwf + ".swf") : null);
//                saveList.add(initial);
//            }
//            if (saveList.isEmpty()) {
//                return MapMessage.errorMessage("没有数据");
//            }
//
//            // ================================================================
//            // 由于使用了本地缓存，必须重启相关服务才能生效
//            // ================================================================
//            pkConfigManagerClient.importPetSkillConfigs(saveList);
//
//            return MapMessage.successMessage("宠物技能导入成功，请重启相关服务");
//        } catch (Exception e) {
//            e.printStackTrace();
//            mapMessage.setSuccess(false);
//            mapMessage.setErrorCode("读取文件失败，请检查文件格式是否正确？");
//            return mapMessage;
//        }
//    }
//
//    @RequestMapping(value = "/importLevelConf.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage importLevelConf(MultipartFile file) {
//        MapMessage mapMessage = MapMessage.successMessage();
//        if (file.isEmpty()) {
//            mapMessage.setSuccess(false);
//            mapMessage.setErrorCode("文件呢？");
//            return mapMessage;
//        }
//        try {//读取Excel。此过程中发生任何异常，均停止操作
//            Workbook workbook = Workbook.getWorkbook(file.getInputStream());// 得到Excel文件
//            Sheet sheet = workbook.getSheet(0);
//            int row = sheet.getRows();
//            List<ExperienceConfig> saveList = new ArrayList<>();
//            for (int i = 0; i < row; i++) {
//                Cell[] cells = sheet.getRow(i);
//                ExperienceConfig initial = new ExperienceConfig(null,
//                        Integer.parseInt(cells[0].getContents()),
//                        Integer.parseInt(cells[1].getContents()),
//                        NumberUtils.toInt(cells[2].getContents()),
//                        Integer.parseInt(cells[3].getContents()),
//                        NumberUtils.toInt(cells[4].getContents()),
//                        Integer.parseInt(cells[5].getContents()),
//                        Integer.parseInt(cells[6].getContents()),
//                        Integer.parseInt(cells[7].getContents()),
//                        Integer.parseInt(cells[8].getContents()),
//                        Integer.parseInt(cells[9].getContents()),
//                        Integer.parseInt(cells[10].getContents()),
//                        Integer.parseInt(cells[11].getContents()));
//                saveList.add(initial);
//            }
//            if (saveList.isEmpty()) {
//                return MapMessage.errorMessage("没有数据");
//            }
//
//            // ================================================================
//            // 由于使用了本地缓存，必须重启相关服务才能生效
//            // ================================================================
//            pkConfigManagerClient.importExperienceConfigs(saveList);
//
//            return MapMessage.successMessage("经验配置导入成功，请重启相关服务");
//        } catch (Exception e) {
//            mapMessage.setSuccess(false);
//            mapMessage.setErrorCode("读取文件失败，请检查文件格式是否正确？");
//            return mapMessage;
//        }
//    }
//
//    @RequestMapping(value = "/importactivityprize.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage importActivityPrize() {
//        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
//        MultipartFile file = multipartRequest.getFile("file");
//
//        MapMessage mapMessage = MapMessage.successMessage();
//        if (file.isEmpty()) {
//            mapMessage.setSuccess(false);
//            mapMessage.setErrorCode("文件呢？");
//            return mapMessage;
//        }
//        try {//读取Excel。此过程中发生任何异常，均停止操作
//            Workbook workbook = Workbook.getWorkbook(file.getInputStream());// 得到Excel文件
//            Sheet sheet = workbook.getSheet(0);
//            int row = sheet.getRows();
//            List<PrizeConfig> saveList = new ArrayList<>();
//            for (int i = 0; i < row; i++) {
//                Cell[] cells = sheet.getRow(i);
//                PrizeConfig prizeConfig = new PrizeConfig();
//                prizeConfig.setActId(cells[0].getContents());
//                prizeConfig.setPrizeType(PkPrizeType.valueOf(cells[1].getContents()));
//                prizeConfig.setContentDesc(cells[2].getContents());
//                prizeConfig.setSource(cells[3].getContents());
//                String prizeStr = StringUtils.trimToEmpty(cells[4].getContents());
//                if (StringUtils.isNotEmpty(prizeStr)) {
//                    prizeConfig.setIdList(new ArrayList<>(Arrays.asList(StringUtils.split(prizeStr, ","))));
//                }
//                prizeConfig.setCount(Integer.parseInt(cells[5].getContents()));
//                saveList.add(prizeConfig);
//            }
//            if (saveList.isEmpty()) {
//                return MapMessage.errorMessage("没有数据");
//            }
//
//            // ================================================================
//            // 由于使用了本地缓存，需要重启相关服务才能生效
//            // ================================================================
//            pkConfigManagerClient.importPrizeConfigs(saveList);
//
//            return MapMessage.successMessage("导入成功，请重启相关服务");
//        } catch (Exception e) {
//            mapMessage.setSuccess(false);
//            mapMessage.setErrorCode("读取文件失败，请检查文件格式是否正确？");
//            return mapMessage;
//        }
//    }
//
//    @RequestMapping(value = "/importnpcroles.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage importNpcRoles(MultipartFile file) {
//        MapMessage mapMessage = MapMessage.successMessage();
//
//        Workbook workbook = null;
//        try {
//            if (file.isEmpty()) {
//                mapMessage.setSuccess(false);
//                mapMessage.setErrorCode("陪练员数据不能为空！！");
//                return mapMessage;
//            }
//            workbook = Workbook.getWorkbook(file.getInputStream());// 得到Excel文件
//            Sheet sheet = workbook.getSheet(0);// Excel中的工作表 下标从0开始
//            int row = sheet.getRows(); // 工作表共有的行
//            Cell[] cells = null;
//            NpcRoleConfig init;
//            List<NpcRoleConfig> npcRoleConfigs = new ArrayList<>();
//            for (int i = 0; i < row; i++) {
//                cells = sheet.getRow(i);
//                init = new NpcRoleConfig();
//
//                init.setId(Long.parseLong(cells[0].getContents()));
//                init.setLevel(Integer.parseInt(cells[1].getContents()));
//                init.setName(cells[2].getContents());
//                init.setHealth(Integer.parseInt(cells[3].getContents()));
//                init.setAttack(Integer.parseInt(cells[4].getContents()));
//                init.setDefense(Integer.parseInt(cells[5].getContents()));
//                init.setEvasion(Double.valueOf(cells[6].getContents()));
//                init.setCrit(Double.valueOf(cells[7].getContents()));
//                init.setGender(Gender.valueOf(cells[8].getContents()));
//                init.setCareer(Career.valueOf(cells[9].getContents()));
//
//                npcRoleConfigs.add(init);
//            }
//
//            if (npcRoleConfigs.isEmpty()) {
//                return MapMessage.errorMessage("没有数据");
//            }
//
//            // ================================================================
//            // 由于使用了本地缓存，需要重启相关服务才能生效
//            // ================================================================
//            pkConfigManagerClient.importNpcRoleConfigs(npcRoleConfigs);
//
//            return MapMessage.successMessage("陪练员导入成功，请重启相关服务");
//        } catch (Exception e) {
//            e.printStackTrace();
//            mapMessage.setSuccess(false);
//            mapMessage.setErrorCode("陪练员导入失败！" + e.getMessage());
//            return mapMessage;
//        } finally {
//            if (workbook != null) {
//                workbook.close();
//            }
//        }
//    }
//
//    @RequestMapping(value = "/importexperiencelevels.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage importExperienceLevels(MultipartFile file) {
//        MapMessage mapMessage = MapMessage.successMessage();
//
//        Workbook workbook = null;
//        try {
//            if (file.isEmpty()) {
//                mapMessage.setSuccess(false);
//                mapMessage.setErrorCode("经验级别数据不能为空！！");
//                return mapMessage;
//            }
//            workbook = Workbook.getWorkbook(file.getInputStream());// 得到Excel文件
//            Sheet sheet = workbook.getSheet(0);// Excel中的工作表 下标从0开始
//            int row = sheet.getRows(); // 工作表共有的行
//            Cell[] cells = null;
//            ExperienceLevelConfig init;
//            List<ExperienceLevelConfig> experienceLevelConfigList = new ArrayList<>();
//            for (int i = 0; i < row; i++) {
//                cells = sheet.getRow(i);
//                init = new ExperienceLevelConfig();
//
//                init.setId(Long.parseLong(cells[0].getContents()));
//                init.setLevel(Integer.parseInt(cells[0].getContents()));
//                init.setCount(Integer.parseInt(cells[1].getContents()));
//                init.setExperience(Integer.parseInt(cells[2].getContents()));
//
//                experienceLevelConfigList.add(init);
//            }
//
//            if (experienceLevelConfigList.isEmpty()) {
//                return MapMessage.errorMessage("没有数据");
//            }
//
//            // ================================================================
//            // 由于使用了本地缓存，必须重启相关服务才能生效
//            // ================================================================
//            pkConfigManagerClient.importExperienceLevelConfigs(experienceLevelConfigList);
//
//            return MapMessage.successMessage("经验级别导入成功，请重启相关服务");
//        } catch (Exception e) {
//            e.printStackTrace();
//            mapMessage.setSuccess(false);
//            mapMessage.setErrorCode("经验级别导入失败！" + e.getMessage());
//            return mapMessage;
//        } finally {
//            if (workbook != null) {
//                workbook.close();
//            }
//        }
//    }
//
//    @RequestMapping(value = "/importhonours.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage importHonours(MultipartFile file) {
//        MapMessage mapMessage = MapMessage.successMessage();
//
//        Workbook workbook = null;
//        try {
//            if (file.isEmpty()) {
//                mapMessage.setSuccess(false);
//                mapMessage.setErrorCode("荣誉数据不能为空！！");
//                return mapMessage;
//            }
//            workbook = Workbook.getWorkbook(file.getInputStream());// 得到Excel文件
//            Sheet sheet = workbook.getSheet(0);// Excel中的工作表 下标从0开始
//            int row = sheet.getRows(); // 工作表共有的行
//            Cell[] cells = null;
//            HonourConfig init;
//            List<HonourConfig> honourConfigList = new ArrayList<>();
//            for (int i = 0; i < row; i++) {
//                cells = sheet.getRow(i);
//                init = new HonourConfig();
//
//                init.setId(Long.parseLong(cells[0].getContents()));
//                init.setHonourLevel(Integer.parseInt(cells[0].getContents()));
//                init.setTitleWithoutLevel(cells[1].getContents());
//                init.setTitle(cells[2].getContents());
//                init.setVictoryCount(Integer.parseInt(cells[3].getContents()));
//                init.setHonour(Integer.parseInt(cells[4].getContents()));
//
//                honourConfigList.add(init);
//            }
//
//            if (honourConfigList.isEmpty()) {
//                return MapMessage.errorMessage("没有数据");
//            }
//
//            // ================================================================
//            // 由于使用了本地缓存，必须重启相关服务才能生效
//            // ================================================================
//            pkConfigManagerClient.importHonourConfigs(honourConfigList);
//
//            return MapMessage.successMessage("荣誉数据导入成功，请重启相关服务");
//        } catch (Exception e) {
//            e.printStackTrace();
//            mapMessage.setSuccess(false);
//            mapMessage.setErrorCode("荣誉数据导入失败！" + e.getMessage());
//            return mapMessage;
//        } finally {
//            if (workbook != null) {
//                workbook.close();
//            }
//        }
//    }
//
//    @RequestMapping(value = "/importproducts.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage importProducts(MultipartFile file) {
//        MapMessage mapMessage = MapMessage.successMessage();
//
//        Workbook workbook = null;
//        try {
//            if (file.isEmpty()) {
//                mapMessage.setSuccess(false);
//                mapMessage.setErrorCode("商品数据不能为空！！");
//                return mapMessage;
//            }
//            workbook = Workbook.getWorkbook(file.getInputStream());// 得到Excel文件
//            Sheet sheet = workbook.getSheet(0);// Excel中的工作表 下标从0开始
//            int row = sheet.getRows(); // 工作表共有的行
//            Cell[] cells = null;
//            ProductConfig init;
//            List<ProductConfig> productConfigList = new ArrayList<>();
//            for (int i = 0; i < row; i++) {
//                cells = sheet.getRow(i);
//                init = new ProductConfig();
//
//                init.setId(cells[0].getContents());
//                init.setName(cells[1].getContents());
//                init.setImg(cells[2].getContents());
//                init.setSwf(cells[3].getContents());
//                init.setWarriorSuitable(Boolean.parseBoolean(cells[4].getContents()));
//                init.setSageSuitable(Boolean.parseBoolean(cells[5].getContents()));
//                init.setBardSuitable(Boolean.parseBoolean(cells[6].getContents()));
//                init.setMaleSuitable(Boolean.parseBoolean(cells[7].getContents()));
//                init.setFemaleSuitable(Boolean.parseBoolean(cells[8].getContents()));
//                init.setType(PkProductType.valueOf(cells[9].getContents()));
//                init.setState(ProductState.valueOf(cells[10].getContents()));
//                init.setPart(ProductPart.valueOf(cells[11].getContents()));
//                init.setIndate(Long.parseLong(cells[12].getContents()));
//                init.setCategory(SourceCategory.valueOf(cells[13].getContents()));
//                init.setPrice(Integer.parseInt(cells[14].getContents()));
//                init.setOtherPrice(Integer.parseInt(cells[15].getContents()));
//                init.setRank(Integer.parseInt(cells[16].getContents()));
//                init.setEndDate(DateUtils.stringToDate(cells[17].getContents(), "yyyy-MM-dd"));
//                init.setDiscount(Double.valueOf(cells[18].getContents()));
//                init.setStartDate(DateUtils.stringToDate(cells[19].getContents(), "yyyy-MM-dd"));
//                productConfigList.add(init);
//            }
//            if (productConfigList.isEmpty()) {
//                return MapMessage.errorMessage("没有数据");
//            }
//
//            // ================================================================
//            // 由于使用了本地缓存，必须重启相关服务才能生效
//            // ================================================================
//            pkConfigManagerClient.importProductConfigs(productConfigList);
//
//            return MapMessage.successMessage("商品数据导入成功，请重启相关服务");
//        } catch (Exception e) {
//            e.printStackTrace();
//            mapMessage.setSuccess(false);
//            mapMessage.setErrorCode("商品数据导入失败！" + e.getMessage());
//            return mapMessage;
//        } finally {
//            if (workbook != null) {
//                workbook.close();
//            }
//        }
//    }
//
//    @RequestMapping(value = "/batchprize.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage batchPrize(MultipartFile file) {
//        MapMessage mapMessage = MapMessage.successMessage();
//        Workbook workbook = null;
//        long userCursor = 0L;
//        int sucRow = 0;
//        int errorUser = 0;
//        try {
//            if (file.isEmpty()) {
//                mapMessage.setSuccess(false);
//                return mapMessage;
//            }
//            workbook = Workbook.getWorkbook(file.getInputStream());// 得到Excel文件
//            Sheet sheet = workbook.getSheet(0);// Excel中的工作表 下标从0开始
//            int row = sheet.getRows(); // 工作表共有的行
//            Cell[] cells = null;
//
//            for (int i = 0; i < row; i++) {
//                cells = sheet.getRow(i);
//                if (StringUtils.isEmpty(StringUtils.trim(cells[0].getContents()))) {
//                    return MapMessage.errorMessage().setInfo("最后成功userid" + userCursor);
//                }
//                userCursor = Long.parseLong(cells[0].getContents());
//                if (null == studentLoaderClient.loadStudent(userCursor)) {
//                    errorUser++;
//                    continue;
//                }
//                MapMessage rtn = pkServiceClient.grantPkPrize(userCursor, cells[1].getContents());
//                if (!rtn.isSuccess()) {
//                    return MapMessage.errorMessage().setInfo("失败userid:" + userCursor + ",成功" + sucRow + ",错误学号" + errorUser);
//                }
//                sucRow++;
//            }
//            return mapMessage.setInfo("成功" + sucRow + ",错误学号" + errorUser);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return MapMessage.errorMessage().setInfo("失败userid:" + userCursor + ",成功" + sucRow + ",错误学号" + errorUser);
//        } finally {
//            if (workbook != null) {
//                workbook.close();
//            }
//        }
//    }
//
//    @RequestMapping(value = "/importSuperScholarRewardInitial.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage importSuperScholarRewardInitial(MultipartFile file) {
//        MapMessage mapMessage = MapMessage.successMessage();
//
//        Workbook workbook = null;
//        try {
//            if (file.isEmpty()) {
//                mapMessage.setSuccess(false);
//                mapMessage.setErrorCode("数据不能为空！！");
//                return mapMessage;
//            }
//            workbook = Workbook.getWorkbook(file.getInputStream());// 得到Excel文件
//            Sheet sheet = workbook.getSheet(0);// Excel中的工作表 下标从0开始
//            int row = sheet.getRows(); // 工作表共有的行
//            Cell[] cells = null;
//            SuperScholarRewardConfig init;
//            List<SuperScholarRewardConfig> initialList = new ArrayList<>();
//            for (int i = 0; i < row; i++) {
//                cells = sheet.getRow(i);
//                init = new SuperScholarRewardConfig();
//
//                init.setId(Long.parseLong(cells[0].getContents()));
//                init.setCycle(Integer.valueOf(cells[1].getContents()));
//                init.setM1Id(cells[2].getContents());
//                init.setM1Name(cells[3].getContents());
//                init.setM2Id(cells[4].getContents());
//                init.setM2Name(cells[5].getContents());
//                init.setM3Id(cells[6].getContents());
//                init.setM3Name(cells[7].getContents());
//                init.setM4Id(cells[8].getContents());
//                init.setM4Name(cells[9].getContents());
//                init.setM5Id(cells[10].getContents());
//                init.setM5Name(cells[11].getContents());
//
//                init.setF1Id(cells[12].getContents());
//                init.setF1Name(cells[13].getContents());
//                init.setF2Id(cells[14].getContents());
//                init.setF2Name(cells[15].getContents());
//                init.setF3Id(cells[16].getContents());
//                init.setF3Name(cells[17].getContents());
//                init.setF4Id(cells[18].getContents());
//                init.setF4Name(cells[19].getContents());
//                init.setF5Id(cells[20].getContents());
//                init.setF5Name(cells[21].getContents());
//
//                initialList.add(init);
//            }
//            if (initialList.isEmpty()) {
//                return MapMessage.errorMessage("没有数据");
//            }
//
//            // ================================================================
//            // 由于使用了本地缓存，必须重启相关服务才能生效
//            // ================================================================
//            pkConfigManagerClient.importSuperScholarRewardConfigs(initialList);
//
//            return MapMessage.successMessage("数据导入成功，请重启相关服务");
//        } catch (Exception e) {
//            e.printStackTrace();
//            mapMessage.setSuccess(false);
//            mapMessage.setErrorCode("数据导入失败！" + e.getMessage());
//            return mapMessage;
//        } finally {
//            if (workbook != null) {
//                workbook.close();
//            }
//        }
//    }
//
//    @RequestMapping(value = "/initbag.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage initBag() {
//        return MapMessage.errorMessage("不支持的操作");
//    }
//
//    @RequestMapping(value = "/initrolecareerlevel.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage initRoleCareerLevel() {
//        return MapMessage.errorMessage("不支持的操作");
//    }
//
//    /**
//     * *******************************PK Management start*************************************
//     */
//    @RequestMapping(value = "management.vpage", method = RequestMethod.GET)
//    public String management() {
//        return "crm/pk/management";
//    }
//
//    @RequestMapping(value = "getRoleCache.vpage", method = {RequestMethod.POST, RequestMethod.GET})
//    @ResponseBody
//    public MapMessage getRoleCache(Long roleId) {
//        if (null == roleId) {
//            return MapMessage.errorMessage().setInfo("请输入参数。");
//        }
//        return getPkRoleCache(roleId);
//    }
//
//    @RequestMapping(value = "refreshRoleCache.vpage", method = {RequestMethod.POST, RequestMethod.GET})
//    @ResponseBody
//    public MapMessage refreshRoleCache(Long roleId) {
//        if (null == roleId) {
//            return MapMessage.errorMessage().setInfo("请输入参数。");
//        }
//        refreshPkRoleCache(roleId);
//        Set<Long> ids = new HashSet<>();
//        ids.add(roleId);
//        User user = userLoaderClient.loadUser(roleId);
//        if (user != null) {
//            pkLoader.getBag(user.getId(), user.getUserType());
//        }
//        pkLoader.getRoleInfos(ids);
//
//        return getPkRoleCache(roleId);
//    }
//
//    @RequestMapping(value = "initBagForRole.vpage", method = {RequestMethod.POST, RequestMethod.GET})
//    @ResponseBody
//    public MapMessage initBagForRole(Long roleId) {
//        if (null == roleId) {
//            return MapMessage.errorMessage().setInfo("请输入参数。");
//        }
//        return pkService.initBagForRole(roleId);
//    }
//
//    @RequestMapping(value = "initSkillForRole.vpage", method = {RequestMethod.POST, RequestMethod.GET})
//    @ResponseBody
//    public MapMessage initSkillForRole(Long roleId) {
//        if (null == roleId) {
//            return MapMessage.errorMessage().setInfo("请输入参数。");
//        }
//        return pkService.initSkillForRole(roleId);
//    }
//
//    @RequestMapping(value = "initEquipmentForRole.vpage", method = {RequestMethod.POST, RequestMethod.GET})
//    @ResponseBody
//    public MapMessage initEquipmentForRole(Long roleId) {
//        if (null == roleId) {
//            return MapMessage.errorMessage().setInfo("请输入参数。");
//        }
//        return pkService.initEquipmentForRole(roleId);
//    }
//
//    @RequestMapping(value = "addSkillForRole.vpage", method = {RequestMethod.POST, RequestMethod.GET})
//    @ResponseBody
//    public MapMessage addSkillForRole(Long roleId, String skillId) {
//        if (null == roleId || StringUtils.isEmpty(skillId)) {
//            return MapMessage.errorMessage().setInfo("请输入参数。");
//        }
//        return pkService.addSkillForRole(roleId, skillId);
//    }
//
//    @RequestMapping(value = "equipedSkillForRole.vpage", method = {RequestMethod.POST, RequestMethod.GET})
//    @ResponseBody
//    public MapMessage equipedSkillForRole(Long roleId, String skillId) {
//        if (null == roleId || StringUtils.isEmpty(skillId)) {
//            return MapMessage.errorMessage().setInfo("请输入参数。");
//        }
//        return pkService.equipedSkillForRole(roleId, skillId);
//    }
//
//    @RequestMapping(value = "unequipedSkillForRole.vpage", method = {RequestMethod.POST, RequestMethod.GET})
//    @ResponseBody
//    public MapMessage unequipedSkillForRole(Long roleId, String skillId) {
//        if (null == roleId || StringUtils.isEmpty(skillId)) {
//            return MapMessage.errorMessage().setInfo("请输入参数。");
//        }
//        return pkService.unequipedSkillForRole(roleId, skillId);
//    }
//
//    @RequestMapping(value = "deleteSkillForRole.vpage", method = {RequestMethod.POST, RequestMethod.GET})
//    @ResponseBody
//    public MapMessage deleteSkillForRole(Long roleId, String skillId) {
//        if (null == roleId || StringUtils.isEmpty(skillId)) {
//            return MapMessage.errorMessage().setInfo("请输入参数。");
//        }
//        return pkService.deleteSkillForRole(roleId, skillId);
//    }
//
//
//    @RequestMapping(value = "addEquipmentForRole.vpage", method = {RequestMethod.POST, RequestMethod.GET})
//    @ResponseBody
//    public MapMessage addEquipmentForRole(Long roleId, String equipmentOriginalId) {
//        if (null == roleId || StringUtils.isEmpty(equipmentOriginalId)) {
//            return MapMessage.errorMessage().setInfo("请输入参数。");
//        }
//        return pkService.addEquipmentForRole(roleId, equipmentOriginalId);
//    }
//
//    @RequestMapping(value = "equipedEquipmentForRole.vpage", method = {RequestMethod.POST, RequestMethod.GET})
//    @ResponseBody
//    public MapMessage equipedEquipmentForRole(Long roleId, String equipmentOriginalId, String equipmentId) {
//        if (null == roleId || StringUtils.isEmpty(equipmentOriginalId) || StringUtils.isEmpty(equipmentId)) {
//            return MapMessage.errorMessage().setInfo("请输入参数。");
//        }
//        return pkService.equipedEquipmentForRole(roleId, equipmentOriginalId, equipmentId);
//    }
//
//    @RequestMapping(value = "unequipedEquipmentForRole.vpage", method = {RequestMethod.POST, RequestMethod.GET})
//    @ResponseBody
//    public MapMessage unequipedEquipmentForRole(Long roleId, String equipmentOriginalId, String equipmentId) {
//        if (null == roleId || StringUtils.isEmpty(equipmentOriginalId) || StringUtils.isEmpty(equipmentId)) {
//            return MapMessage.errorMessage().setInfo("请输入参数。");
//        }
//        return pkService.unequipedEquipmentForRole(roleId, equipmentOriginalId, equipmentId);
//    }
//
//    @RequestMapping(value = "deleteEquipmentForRole.vpage", method = {RequestMethod.POST, RequestMethod.GET})
//    @ResponseBody
//    public MapMessage deleteEquipmentForRole(Long roleId, String equipmentOriginalId, String equipmentId) {
//        if (null == roleId || StringUtils.isEmpty(equipmentOriginalId) || StringUtils.isEmpty(equipmentId)) {
//            return MapMessage.errorMessage().setInfo("请输入参数。");
//        }
//        return pkService.deleteEquipmentForRole(roleId, equipmentOriginalId, equipmentId);
//    }
//
//    /**
//     * *******************************PK Management end*************************************
//     */
//
//    private void refreshPkRoleCache(Long roleId) {
//        adminCacheSystem.CBS.flushable.delete(Bag.ck_id(roleId));
//        adminCacheSystem.CBS.flushable.delete(Role.cacheKeyFromId(roleId));
//    }
//
//    private MapMessage getPkRoleCache(Long roleId) {
//        Bag bag = adminCacheSystem.CBS.flushable.load(Bag.ck_id(roleId));
//        Role role = adminCacheSystem.CBS.flushable.load(Role.cacheKeyFromId(roleId));
//        return MapMessage.successMessage().add("role", role).add("bag", bag);
//    }
}