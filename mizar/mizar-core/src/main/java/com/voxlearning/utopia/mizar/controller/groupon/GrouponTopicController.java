package com.voxlearning.utopia.mizar.controller.groupon;


import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.mizar.utils.MizarOssManageUtils;
import com.voxlearning.utopia.service.mizar.api.constants.SpecialTopicType;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GrouponGoods;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.SpecialTopic;
import com.voxlearning.utopia.service.mizar.api.utils.ImageUtil;
import com.voxlearning.utopia.service.mizar.consumer.loader.GrouponGoodsLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.loader.SpecialTopicLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.SpecialTopicServiceClient;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 运营使用，专题 Controller
 * Created by yuechen.wang on 16/10/18.
 */
@Controller
@RequestMapping(value = "/groupon/topic")
public class GrouponTopicController extends AbstractMizarController {

    @Inject
    private SpecialTopicLoaderClient specialTopicLoaderClient;
    @Inject
    private GrouponGoodsLoaderClient grouponGoodsLoaderClient;
    @Inject
    private SpecialTopicServiceClient specialTopicServiceClient;

    // 基本信息首页列表
    @RequestMapping(value = "index.vpage")
    public String index(Model model) {
        int pageNum = getRequestInt("page", 1);
        Date now = new Date();
        String token = getRequestString("token");
        String position = getRequestString("pos");
        Date startDate = getRequestDate("start");
        Date endDate = getRequestDate("end");
        Date endStart = getRequestDate("endStart");
        Date endEnd = getRequestDate("endEnd");

        int totalPage = 1;
        SpecialTopic topic = null;
        List<SpecialTopic> topicList = new ArrayList<>();
        // 默认先按照ID去取
        if (StringUtils.isNotBlank(token)) {
            topic = specialTopicLoaderClient.loadById(token);
        }
        // 取不到再根据条件去取
        if (topic != null) {
            topicList.add(topic);
        } else {
            pageNum = pageNum < 1 ? 1 : pageNum;
            Pageable page = new PageRequest(pageNum - 1, 10);
            List<SpecialTopic> specialTopicList = specialTopicLoaderClient.loadAllTopics();
            if (StringUtils.isNotBlank(token)) {
                specialTopicList = specialTopicList.stream().filter(o -> StringUtils.contains(o.getName(), token)).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(position)) {
                specialTopicList = specialTopicList.stream().filter(o -> StringUtils.contains(o.getPosition(), position)).collect(Collectors.toList());
            }
            if (Objects.nonNull(startDate)) {
                specialTopicList = specialTopicList.stream().filter(o -> Objects.nonNull(o.getStartTime()) && o.getStartTime().after(startDate)).collect(Collectors.toList());
            }
            if (Objects.nonNull(endDate)) {
                specialTopicList = specialTopicList.stream().filter(o -> Objects.nonNull(o.getStartTime()) && o.getStartTime().before(endDate)).collect(Collectors.toList());
            }
            if (Objects.nonNull(endStart)) {
                specialTopicList = specialTopicList.stream().filter(o -> Objects.nonNull(o.getEndTime()) && o.getEndTime().after(endStart)).collect(Collectors.toList());
            }
            if (Objects.nonNull(endEnd)) {
                specialTopicList = specialTopicList.stream().filter(o -> Objects.nonNull(o.getEndTime()) && o.getEndTime().before(endEnd)).collect(Collectors.toList());
            }
            specialTopicList = specialTopicList.stream().sorted(new Comparator<SpecialTopic>() {
                public int compare(SpecialTopic o1, SpecialTopic o2) {
                    String position = SafeConverter.toString(o1.getPosition(), "");
                    String positionOther = SafeConverter.toString(o2.getPosition(), "");
                    if (StringUtils.equalsIgnoreCase(o1.getPosition(), o2.getPosition())) {
                        Integer orderIndex = SafeConverter.toInt(o1.getOrderIndex());
                        Integer orderIndexOther = SafeConverter.toInt(o2.getOrderIndex());
                        return Integer.compare(orderIndexOther, orderIndex);
                    }
                    return position.compareTo(positionOther);
                }
            }).collect(Collectors.toList());

            Page<SpecialTopic> topicPage = PageableUtils.listToPage(specialTopicList, page);
            totalPage = topicPage.getTotalPages();
            topicList = topicPage.getContent();
        }
        model.addAttribute("token", token);
        model.addAttribute("pos", position);
        model.addAttribute("start", getRequestString("start"));
        model.addAttribute("end", getRequestString("end"));
        model.addAttribute("endStart", getRequestString("endStart"));
        model.addAttribute("endEnd", getRequestString("endEnd"));
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("pageNum", pageNum);
        model.addAttribute("topicList", topicList);
        return "groupon/topic/topiclist";
    }


    @RequestMapping(value = "view.vpage")
    public String view(Model model) {
        String tid = getRequestString("tid");
        SpecialTopic topic = specialTopicLoaderClient.loadById(tid);
        if (topic != null) {
            model.addAttribute("topic", topic);
            List<String> grouponGoodsIdList = topic.getGrouponGoodsIdList();
            if (CollectionUtils.isNotEmpty(grouponGoodsIdList)) {
                List<Map<String, String>> goodsList = grouponGoodsLoaderClient.loadGroupGoods(grouponGoodsIdList)
                        .stream()
                        .map(this::simpleGropuonGoods)
                        .collect(Collectors.toList());
                model.addAttribute("goodsList", goodsList);
            }
        }
        model.addAttribute("tid", tid);
        return "groupon/topic/view";
    }

    // 专题编辑详情页
    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    public String detail(Model model) {
        String tid = getRequestString("tid");
        SpecialTopic topic = specialTopicLoaderClient.loadById(tid);
        if (topic != null) {
            model.addAttribute("topic", topic);
            List<String> grouponGoodsIdList = topic.getGrouponGoodsIdList();
            if (CollectionUtils.isNotEmpty(grouponGoodsIdList)) {
                List<Map<String, String>> goodsList = grouponGoodsLoaderClient.loadGroupGoods(grouponGoodsIdList)
                        .stream()
                        .map(this::simpleGropuonGoods)
                        .collect(Collectors.toList());
                model.addAttribute("goodsList", goodsList);
            }
        } else {
            model.addAttribute("topic", new SpecialTopic());
        }
        model.addAttribute("editable", topic == null || !"ONLINE".equals(topic.getStatus()));
        model.addAttribute("tid", tid);
        return "groupon/topic/topicdetail";
    }

    @RequestMapping(value = "savetopic.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTopic() {
        String tid = getRequestString("topicId");
        SpecialTopic topic = requestEntity(SpecialTopic.class);
        if (topic == null) {
            return MapMessage.errorMessage("参数异常");
        }
        try {
            SpecialTopic origin = specialTopicLoaderClient.loadById(tid);
            if (origin != null) {
                topic.setId(origin.getId());
            }
            topic.setStatus("OFFLINE");
            if (StringUtils.isBlank(topic.getCoverImg())) {
                return MapMessage.errorMessage("请上传专题封面图图");
            }
            if (StringUtils.equalsIgnoreCase(SpecialTopicType.TO_DETAIL.name(), topic.getType())) {
                // 如果是商品专题,过滤掉无用的商品ID
                if (StringUtils.isBlank(topic.getDetailImg())) {
                    return MapMessage.errorMessage("请上传专题头图");
                }

                List<String> grouponGoods = topic.getGrouponGoodsIdList();
                if (CollectionUtils.isNotEmpty(grouponGoods)) {
                    grouponGoods = grouponGoodsLoaderClient.loadGroupGoods(grouponGoods).stream()
                            .map(GrouponGoods::getId).distinct().collect(Collectors.toList());
                }
                if (CollectionUtils.isEmpty(grouponGoods)) {
                    return MapMessage.errorMessage("请填写有效的专题商品");
                }
                topic.setGrouponGoodsIdList(new ArrayList<>(grouponGoods));
            } else {
                String url = topic.getUrl();
                if (StringUtils.isBlank(url) || !(StringUtils.startsWith(url, "http://") || StringUtils.startsWith(url, "https://"))) {
                    return MapMessage.errorMessage("请填写有效的跳转链接");
                }
            }

            SpecialTopic upsert = specialTopicServiceClient.save(topic);
            if (upsert == null) {
                return MapMessage.errorMessage("保存失败");
            }
            return MapMessage.successMessage().add("id", upsert.getId());
        } catch (Exception ex) {
            logger.error("Save Mizar Special Topic failed.", ex);
            return MapMessage.errorMessage("新增专题失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "changestatus.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeTopicStatus() {
        String topicId = getRequestString("tid");
        String status = getRequestString("status");
        try {
            if (!"ONLINE".equals(status) && !"OFFLINE".equals(status)) {
                return MapMessage.errorMessage("状态异常");
            }
            SpecialTopic topic = specialTopicLoaderClient.loadById(topicId);
            if (topic == null) {
                return MapMessage.errorMessage("无效的专题");
            }
            topic.setStatus(status);
            specialTopicServiceClient.save(topic);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed change topic status, tid={}, status={}", topicId, status, ex);
            return MapMessage.errorMessage("状态变更失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "searchgoods.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage searchGoods() {
        String goodsId = getRequestString("goods");
        if (StringUtils.isBlank(goodsId)) {
            return MapMessage.successMessage().add("goodsList", Collections.emptyList());
        }
        try {
            Set<String> idSet = Stream.of(goodsId.split(",")).filter(ObjectId::isValid).collect(Collectors.toSet());
            List<Map<String, String>> goodsList = grouponGoodsLoaderClient.loadGroupGoods(idSet)
                    .stream()
                    .map(this::simpleGropuonGoods)
                    .collect(Collectors.toList());
            return MapMessage.successMessage().add("goodsList", goodsList);
        } catch (Exception ex) {
            logger.error("Failed search topic's goods, gid={}", goodsId, ex);
            return MapMessage.errorMessage("查询失败：" + ex.getMessage());
        }
    }

    private Map<String, String> simpleGropuonGoods(GrouponGoods goods) {
        if (goods == null) return Collections.emptyMap();
        Map<String, String> info = new HashMap<>();
        info.put("id", goods.getId());
        info.put("title", goods.getShortTitle());
        return info;
    }

    @RequestMapping(value = "uploadcheckphoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    //只对封面图验证
    public MapMessage uploadAndCheckPhoto() {
        String position = requestString("position");
        if (StringUtils.isBlank(position)) {
            return MapMessage.errorMessage("请选择专题位置");
        }
        Map<String, String> map = new HashMap<>();
        map.put("BANNER", "750*300");
        map.put("LEFT", "340*330");
        map.put("RIGHT_TOP", "380*160");
        map.put("RIGHT_BOTTOM", "380*160");
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
        MultipartFile inputFile = multipartRequest.getFile("file");
        if (inputFile == null || inputFile.isEmpty()) {
            return MapMessage.errorMessage("上传文件为空");
        }
        String sizeValue = map.get(position);
        if (StringUtils.isNotBlank(sizeValue)) {
            String[] sizeArray = sizeValue.split("\\*");
            int width = Integer.parseInt(sizeArray[0]);
            int height = Integer.parseInt(sizeArray[1]);
            boolean check = ImageUtil.checkImageSize(width, height, inputFile);
            if (!check) {
                return MapMessage.errorMessage("封面图大小不符合尺寸" + sizeValue);
            }
        }
        if (inputFile.getSize() > MAXIMUM_UPLOAD_PHOTO_SIZE) {
            return MapMessage.errorMessage("上传文件超出{}KB", MAXIMUM_UPLOAD_PHOTO_SIZE / 1024);
        }
        String fileName =  MizarOssManageUtils.upload(inputFile);
        if (StringUtils.isBlank(fileName)) {
            return MapMessage.errorMessage("图片上传失败");
        }
        if (MizarOssManageUtils.invalidFile.equals(fileName)) {
            return MapMessage.errorMessage("无效的文件类型！");
        }
        return MapMessage.successMessage().add("imgUrl", fileName);
    }
}
