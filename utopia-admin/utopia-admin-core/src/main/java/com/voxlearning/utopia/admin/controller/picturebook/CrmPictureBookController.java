package com.voxlearning.utopia.admin.controller.picturebook;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.galaxy.service.picturebook.api.entity.PictureBookCard;
import com.voxlearning.galaxy.service.picturebook.api.entity.PictureBookCardSubject;
import com.voxlearning.galaxy.service.picturebook.api.mapper.CrmPictureBookColorCardMapper;
import com.voxlearning.galaxy.service.picturebook.api.mapper.CrmPictureBookSubjectMapper;
import com.voxlearning.galaxy.service.picturebook.api.service.CrmPictureBookCardService;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.service.parent.api.PictureBookConfigService;
import com.voxlearning.utopia.service.parent.api.entity.picturebook.PictureBookConfigInfo;
import com.voxlearning.utopia.service.parent.api.entity.picturebook.PictureBookConfigList;
import com.voxlearning.utopia.service.parent.api.mapper.picturebook.PictureBookConfigListVO;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther: zhiqian.ren Sirius
 * @Date: 2018/11/22
 * @Description: 绘本运维相关
 */
@Controller
@RequestMapping("opmanager/pictureBook")
public class CrmPictureBookController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = PictureBookConfigService.class)
    private PictureBookConfigService pictureBookConfigService;

    @StorageClientLocation(storage = "17-pmc")
    private StorageClient storageClient;

    @Inject
    private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @ImportService(interfaceClass = CrmPictureBookCardService.class)
    private CrmPictureBookCardService cardService;

    @RequestMapping(value = "/recommend/list.vpage", method = RequestMethod.GET)
    public String recommendList(Model model) {
        Integer showType = getRequestInt("query_type");
        String title = getRequestString("query_title");
        Integer pageNum = getRequestInt("page", 1);
        PageRequest page = new PageRequest(pageNum - 1, 10);
        Page<PictureBookConfigListVO> configListVOS = pictureBookConfigService.loadPictureBookConfigList(showType, title, page);
        model.addAttribute("content", configListVOS.getContent().stream()
                .sorted(Comparator.comparing(PictureBookConfigListVO::getUpdateDate).reversed()).collect(Collectors.toList()));
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", configListVOS.getTotalPages());
        model.addAttribute("hasPrev", configListVOS.hasPrevious());
        model.addAttribute("hasNext", configListVOS.hasNext());
        model.addAttribute("showType", showType);
        model.addAttribute("title", title);
        return "/opmanager/picturebook/pictureBookConfigListPage";
    }

    @RequestMapping(value = "/recommend/save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage recommendSave() {

        Integer showType = getRequestInt("showType");
        String title = getRequestString("from_title");
        String imageUrl = getRequestString("imageUrl");
        String instruction = getRequestString("instruction");
        String id = getRequestString("id");
        String adUrl = getRequestString("adUrl");

        PictureBookConfigList config = new PictureBookConfigList();
        if (StringUtils.isNotBlank(id)) {
            config.setId(id);
        }
        config.setAdImgUrl(adUrl);
        config.setShowType(showType);
        config.setEnabled(false);
        config.setIntroduction(instruction);
        config.setTitle(title);
        config.setMainImgUrl(imageUrl);
        config.setCreator(getCurrentAdminUser().getAdminUserName());
        int singleItemBox = getRequestInt("singleItemBox");
        List<PictureBookConfigInfo> infoList = new ArrayList<>();
        for (int i = 1; i <= singleItemBox; i++) {
            PictureBookConfigInfo info = new PictureBookConfigInfo();
            String infoId = getRequestString("info_id_" + i);
            if (StringUtils.isNotBlank(infoId)) {
                info.setId(infoId);
            }
            String clId = getRequestString("cl_id_" + i);
            if (StringUtils.isNotBlank(clId)) {
                info.setConfigListId(clId);
            }
            String pbId = getRequestString("pb_id_" + i);
            if (StringUtils.isNotBlank(pbId)) {
                //校验绘本是否online
                PictureBookPlus bookPlus = pictureBookPlusServiceClient.loadById(pbId);
                if (bookPlus == null) {
                    return MapMessage.errorMessage("当前绘本合集包含已删除绘本:" + pbId);
                }
                info.setPictureBookId(pbId);
            }
            info.setConfigWords(getRequestString("pb_ci_" + i));
            info.setRecommendWords(getRequestString("pb_rewords1_" + i));
            info.setRecommendWordsSecond(getRequestString("pb_rewords2_" + i));
            infoList.add(info);
        }
        return pictureBookConfigService.savePictureBookConfigList(config, infoList);
    }

    @RequestMapping(value = "/recommend/modify.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage recommendModify() {
        String id = getRequestString("id");
        PictureBookConfigListVO configListVO = pictureBookConfigService.loadPictureBookConfig(id);
        if (Objects.isNull(configListVO)) {
            return MapMessage.errorMessage();
        }
        MapMessage message = MapMessage.successMessage();
        message.add("config", configListVO);
        message.add("singleItemBox", CollectionUtils.isEmpty(configListVO.getInfoVOS()) ? 0 : configListVO.getInfoVOS().size());
        return message;
    }


    @RequestMapping(value = "/recommend/enable.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage recommendEnable() {
        String id = getRequestString("id");
        Integer enable = getRequestInt("enable");
        return pictureBookConfigService.enablePictureBookConfig(id, enable);
    }

    /**
     * 上传图片
     */
    @ResponseBody
    @RequestMapping(value = "/recommend/upload.vpage", method = RequestMethod.POST)
    public MapMessage upload(MultipartFile inputFile) {
        if (inputFile == null) {
            return MapMessage.errorMessage("没有可上传的文件");
        }
        String suffix = StringUtils.substringAfterLast(inputFile.getOriginalFilename(), ".");
        if (StringUtils.isBlank(suffix)) {
            suffix = "jpg";
        }
        StorageMetadata storageMetadata = new StorageMetadata();
        storageMetadata.setContentLength(inputFile.getSize());
        String env = "crmPictureBookConfig/";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = "crmPictureBookConfig/test/";
        }
        try {
            String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
            String fileName = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + suffix;
            String realName = storageClient.upload(inputFile.getInputStream(), fileName, path, storageMetadata);
            String fileUrl = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + realName;
            return MapMessage.successMessage().add("imgName", realName).add("imgUrl", fileUrl);
        } catch (Exception ex) {
            logger.error("上传图片异常： " + ex.getMessage(), ex);
            return MapMessage.errorMessage("上传图片异常： " + ex.getMessage());
        }

    }

    @RequestMapping(value = "/card/list.vpage", method = RequestMethod.GET)
    public String cardList(Model model) {
        Integer isOnLine = getRequestInt("isOnLine");
        String title = getRequestString("query_title");
        Integer pageNum = getRequestInt("page", 1);
        PageRequest page = new PageRequest(pageNum - 1, 10);
        Page<CrmPictureBookSubjectMapper> subjectMappers = cardService.loadCrmPictureBookSubjectMapperListByPage(page, title,isOnLine);
        model.addAttribute("content", subjectMappers.getContent().stream()
                .sorted(Comparator.comparing(CrmPictureBookSubjectMapper::getUpdateTime).reversed()).collect(Collectors.toList()));
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", subjectMappers.getTotalPages());
        model.addAttribute("title", title);
        model.addAttribute("isOnLine", isOnLine);
        model.addAttribute("hasPrev", subjectMappers.hasPrevious());
        model.addAttribute("hasNext", subjectMappers.hasNext());
        return "/opmanager/picturebook/card/list";
    }

    @RequestMapping(value = "/card/subject/save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage subjectSave() {
        String name = getRequestString("subject_name");
        PictureBookCardSubject subject = new PictureBookCardSubject();
        subject.setName(name);
        subject.setIsOnLine(2);
        subject.setCreateUser(getCurrentAdminUser().getAdminUserName());
        PictureBookCardSubject cardSubject = cardService.savePictureBooKCardSubject(subject);
        if (cardSubject == null) {
            return MapMessage.errorMessage("操作失败，请重试");
        }
        return MapMessage.successMessage().add("subjectId",cardSubject.getId())
                .add("subjectName",cardSubject.getName());
    }

    @RequestMapping(value = "/card/save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage save() {
        Integer sort = getRequestInt("sort");
        String id = getRequestString("card_id");
        String name = getRequestString("name");
        String des = getRequestString("des");
        Integer num = getRequestInt("num");
        String imgUrl = getRequestString("imgUrl");
        Integer type = getRequestInt("type");
        String subjectId = getRequestString("subjectId");
        PictureBookCard card = new PictureBookCard();
        if(StringUtils.isNotBlank(id)){
            card.setId(id);
        }
        card.setCardType(type);
        card.setCreateUser(getCurrentAdminUser().getAdminUserName());
        card.setDescription(des);
        card.setSort(sort);
        MapMessage message = MapMessage.successMessage();
        if(Objects.equals(type,1)){//常规卡
            card.setFragmentNum(num);
            message.add("subjectId",subjectId);
        }
        if(Objects.equals(type,2)){//彩蛋卡
            String startDateStr = getRequestString("start");
            card.setStartDate(DateUtils.stringToDate(startDateStr));
            card.setFragmentNum(1);
            Integer continueWeekNum = getRequestInt("weekNum");
            card.setContinuedWeekNum(continueWeekNum);
        }
        card.setImgUrl(imgUrl);
        card.setIsOnLine(2);
        card.setName(name);
        card.setSubjectId(subjectId);
        PictureBookCard savePictureBooKCard = cardService.savePictureBooKCard(card);
        if (savePictureBooKCard == null) {
            return MapMessage.errorMessage("操作失败，请重试");
        }
        message.add("cardId",savePictureBooKCard.getId());
        return MapMessage.successMessage().add("subjectId",subjectId);
    }

    @RequestMapping(value = "/card/enabled.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage enable() {
        String subjectId = getRequestString("subjectId");
        String enabled = getRequestString("enabled");
        if(StringUtils.isBlank(subjectId)){
            MapMessage.errorMessage("参数异常");
        }
        return cardService.enabledCommonCardBySubject(subjectId,enabled);
    }

    @RequestMapping(value = "/card/add.vpage", method = RequestMethod.GET)
    public String add(Model model) {
        String subjectId = getRequestString("subjectId");
        String subjectName = getRequestString("subjectName");
        if(StringUtils.isBlank(subjectId)){
            model.addAttribute("success",false);
            model.addAttribute("error","参数异常");
            return "/opmanager/picturebook/card/add";
        }
        model.addAttribute("success",true);
        model.addAttribute("subjectId",subjectId);
        model.addAttribute("subjectName",subjectName);
        model.addAttribute("isOnline",0);
        return "/opmanager/picturebook/card/add";
    }

    @RequestMapping(value = "/card/detail.vpage", method = RequestMethod.GET)
    public String detail(Model model) {
        String subjectId = getRequestString("subjectId");
        Integer isOnline = getRequestInt("isOnline");
        if(StringUtils.isBlank(subjectId)){
            model.addAttribute("success",false);
            model.addAttribute("error","参数异常");
            return "/opmanager/picturebook/card/add";
        }
        model.addAttribute("success",true);
        model.addAttribute("isOnline",isOnline);
        model.addAllAttributes(cardService.loadCommonCardBySubject(subjectId));
        return "/opmanager/picturebook/card/add";
    }

    @RequestMapping(value = "/card/edit.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage edit() {
        String id = getRequestString("card_id");
        if(StringUtils.isBlank(id)){
           return MapMessage.errorMessage("参数异常");
        }
        return MapMessage.successMessage().add("card",cardService.loadCommonCardById(id));
    }

    @RequestMapping(value = "/card/color/list.vpage", method = RequestMethod.GET)
    public String colorList(Model model) {
        Integer isOnLine = getRequestInt("isOnLine");
        String title = getRequestString("title");
        Integer pageNum = getRequestInt("page", 1);
        PageRequest page = new PageRequest(pageNum - 1, 10);
        Page<CrmPictureBookColorCardMapper> cardMappers = cardService.loadCrmPictureBookColorCardListByPage(page, title,isOnLine);
        model.addAttribute("content", cardMappers.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", cardMappers.getTotalPages());
        model.addAttribute("title", title);
        model.addAttribute("isOnLine", isOnLine);
        model.addAttribute("hasPrev", cardMappers.hasPrevious());
        model.addAttribute("hasNext", cardMappers.hasNext());
        return "/opmanager/picturebook/card/colorList";
    }

    @RequestMapping(value = "/card/color/edit.vpage", method = RequestMethod.GET)
    public String editColor(Model model) {
        String id = getRequestString("card_id");
        if(StringUtils.isBlank(id)){
            model.addAttribute("success",false);
            model.addAttribute("error","参数异常");
            model.addAttribute("isOnLine",0);
            return "/opmanager/picturebook/card/addColor";
        }
        model.addAttribute("success",true);
        model.addAttribute("id",id);
        CrmPictureBookColorCardMapper card = cardService.loadColorCardById(id);
        if(card!=null){
            model.addAttribute("name",card.getName());
            model.addAttribute("des",card.getDescription());
            model.addAttribute("imgUrl",card.getImgUrl());
            model.addAttribute("startTime",DateUtils.dateToString(card.getStartDate()));
            model.addAttribute("weekNum",card.getContinuedWeekNum());
            model.addAttribute("id",card.getId());
            model.addAttribute("type",card.getCardType());
            model.addAttribute("isOnLine",card.getIsOnLine());
        }
        return "/opmanager/picturebook/card/addColor";
    }

    @RequestMapping(value = "/card/color/enabled.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage colorEnabled() {
        String id = getRequestString("card_id");
        String enabled = getRequestString("enabled");
        if(StringUtils.isBlank(id)){
            return MapMessage.errorMessage("参数异常");
        }
        return MapMessage.successMessage().add("card",cardService.enabledColorCardById(id,enabled));
    }

}
