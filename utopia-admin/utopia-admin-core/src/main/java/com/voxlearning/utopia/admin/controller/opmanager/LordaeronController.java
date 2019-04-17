package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.order.api.constants.OrderProductSalesType;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.parent.api.ProductViewInfoCRMLoader;
import com.voxlearning.utopia.service.parent.api.ProductViewInfoCRMService;
import com.voxlearning.utopia.service.parent.api.entity.ProductViewInfo;
import com.voxlearning.utopia.service.parent.constant.ProductRecommendStrategy;
import com.voxlearning.utopia.service.parent.constant.ProductShowStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品详情页
 *
 * @author qiuxu
 * @since 2018-11-22
 */
@Controller
@Slf4j
@RequestMapping(value = "/opmanager/lordaeron")
public class LordaeronController extends OpManagerAbstractController {

    private final static Map<String, String> grades = new HashMap<>();

    static {
        grades.put("1", "一");
        grades.put("2", "二");
        grades.put("3", "三");
        grades.put("4", "四");
        grades.put("5", "五");
        grades.put("6", "六");
    }

    @ImportService(interfaceClass = ProductViewInfoCRMService.class)
    private ProductViewInfoCRMService service;

    @ImportService(interfaceClass = ProductViewInfoCRMLoader.class)
    private ProductViewInfoCRMLoader loader;

    @RequestMapping(value = "/list.vpage", method = RequestMethod.GET)
    public String list(Model model) {
        String productType = requestString("productType");
        String productId = requestString("productId");

        int pageIndex = getRequestInt("page", 1);

        PageRequest pageRequest = new PageRequest(pageIndex - 1, 20);

        PageImpl<ProductViewInfo> paging = loader.paging(productType, productId, pageRequest);
        List<ProductViewInfo> content = paging.getContent();

        List<String> productIds = content.stream().map(ProductViewInfo::getProductId).collect(Collectors.toList());
        Map<String, OrderProduct> productMap = userOrderLoaderClient.loadOrderProducts(productIds);

        List<Map<String, Object>> maps = new LinkedList<>();
        for (ProductViewInfo info : content) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("url", convertUrl(info.getProductType(), info.getProductId()));
            map.put("id", info.getId());
            map.put("productType", info.getProductType());
            map.put("productId", info.getProductId());
            map.put("updateTime", DateUtils.dateToString(info.getUpdateTime(), "yyyy-MM-dd"));
            map.put("subhead", info.getSubhead());
            map.put("button", info.getShowStatus() == ProductShowStatus.ONLINE.value ? "下线" : "上线");
            map.put("status", info.getShowStatus() == ProductShowStatus.ONLINE.value ? ProductShowStatus.OFFLINE.value : ProductShowStatus.ONLINE.value);
            map.put("productName", info.getProductName());
            maps.add(map);
        }

        model.addAttribute("maps", maps);


        model.addAttribute("pageIndex", pageIndex);
        int start = ((pageIndex - 1) / 10) * 10 + 1;
        model.addAttribute("start", start);
        int end = ((pageIndex - 1) / 10 + 1) * 10;
        int pageCount = paging.getTotalPages();
        if (end > pageCount) {
            end = pageCount;
        }
        model.addAttribute("end", end);
        model.addAttribute("pageCount", pageCount);
        String query = "?";
        if (StringUtils.isNotBlank(productType)) {
            query += "productType=" + productType + "&";
        }
        if (StringUtils.isNotBlank(productId)) {
            query += "productId=" + productId + "&";
        }
        query += "page=";
        model.addAttribute("query", query);

        List<OrderProductServiceType> types = Arrays.asList(OrderProductServiceType.values());
        model.addAttribute(
                "productTypes",
                types.stream().filter(t -> !t.isOrderClosed()).collect(Collectors.toList()));

        return "opmanager/lordaeron/list";
    }

    private String convertUrl(String productType, String productId) {
        String prefix = RuntimeMode.isProduction() ?
                "https://www.17zuoye.com"
                : RuntimeMode.isStaging()
                ? "https://www.staging.17zuoye.net"
                : "https://www.test.17zuoye.net";

        prefix += "/karp/commodity_details/index/list?productType=" + productType;

        if (StringUtils.isNotBlank(productId)) {
            prefix += "&productId=" + productId;
        }

        return prefix;
    }

    @RequestMapping(value = "/detail.vpage", method = RequestMethod.GET)
    public String detail(Model model) {

        List<OrderProductServiceType> types = Arrays.asList(OrderProductServiceType.values());
        model.addAttribute(
                "productTypes",
                types.stream().filter(t -> !t.isOrderClosed()).collect(Collectors.toList()));

        Map<String, Object> product = new LinkedHashMap<>();

        model.addAttribute("product", product);
        model.addAttribute("grades", grades);

        Map<String, Object> userTypes = new LinkedHashMap<>();
        userTypes.put(UserType.STUDENT.name(), "孩子");
        userTypes.put(UserType.PARENT.name(), "家长");
        model.addAttribute("userTypes", userTypes);

        Map<String, Object> saleBases = new LinkedHashMap<>();
        saleBases.put(OrderProductSalesType.TIME_BASED.name(), "时间");
        saleBases.put(OrderProductSalesType.ITEM_BASED.name(), "项目");


        responseProductViewInfo(model);

        return "opmanager/lordaeron/detail";
    }

    @RequestMapping(value = "/save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage save() {

        ProductViewInfo info = requestProductViewInfo();
        if (StringUtils.isBlank(info.getId())) {
            service.create(info);
        } else {
            service.update(info);
        }

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "/changeStatus.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeStatus() {
        String id = getRequestString("id");
        if (StringUtils.isEmpty(id)) {
            return MapMessage.errorMessage("参数错误");
        }

        int status = getRequestInt("status");
        return service.changeShowStatus(id, status);
    }

    private void responseProductViewInfo(Model model) {
        String id = requestString("id");
        if (StringUtils.isBlank(id)) {
            return;
        }

        ProductViewInfo info = loader.load(id);
        if (info == null) {
            return;
        }

        model.addAttribute("id", id)
                .addAttribute("subhead", info.getSubhead())
                .addAttribute("subject", info.getSubject())
                .addAttribute("endClazzLevel", info.getEndClazzLevel())
                .addAttribute("startClazzLevel", info.getStartClazzLevel())
                .addAttribute("headImage", info.getHeadImage())
                .addAttribute("iconUrl", info.getIconUrl())
                .addAttribute("intro", info.getIntro())
                .addAttribute("recommendProducts", loadItems(info.getRecommendProducts()))
                .addAttribute("subProducts", loadItems(info.getSubProducts()))
                .addAttribute("viewType", info.getViewType())
                .addAttribute("productId", info.getProductId())
                .addAttribute("productType", info.getProductType())
                .addAttribute("salesType", info.getSalesType() == null ? OrderProductSalesType.TIME_BASED.name() : info.getSalesType().name())
                .addAttribute("productName", info.getProductName())
                .addAttribute("userType", info.getUserType() == null ? UserType.STUDENT.name() : info.getUserType().name());
    }

    private List<Map<String, Object>> loadItems(List<ProductViewInfo.ProductItem> items) {
        List<Map<String, Object>> mapList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(items)) {
            List<String> ids = items.stream().filter(x -> StringUtils.isNotBlank(x.getProductId())).map(ProductViewInfo.ProductItem::getProductId).collect(Collectors.toList());
            Map<String, OrderProduct> map = userOrderLoaderClient.loadOrderProducts(ids);
            for (ProductViewInfo.ProductItem item : items) {
                Map<String, Object> itemMap = new LinkedHashMap<>();
                itemMap.put("productId", item.getProductId());
                itemMap.put("productType", item.getProductType());
                itemMap.put("itemId", item.getProductType() + "@" + item.getProductId());
                OrderProduct product = map.get(item.getProductId());
                itemMap.put("productName", product == null ? "默认全部" : product.getName());
                mapList.add(itemMap);
            }
        }
        return mapList;
    }

    private ProductViewInfo requestProductViewInfo() {
        ProductViewInfo productViewInfo = new ProductViewInfo();

        productViewInfo.setId(requestString("id"));
        productViewInfo.setSubject(requestString("subject"));
        productViewInfo.setIconUrl(requestString("iconUrl"));
        productViewInfo.setStartClazzLevel(getRequestInt("startClazzLevel", 1));
        productViewInfo.setEndClazzLevel(getRequestInt("endClazzLevel", 6));
        productViewInfo.setHeadImage(requestString("headImage"));
        productViewInfo.setIntro(requestString("intro"));
        productViewInfo.setRecommendStrategy(ProductRecommendStrategy.MANUAL.value);
        productViewInfo.setSubhead(requestString("subhead"));
        productViewInfo.setViewType(requestInteger("viewType"));
        productViewInfo.setProductId(requestString("productId"));
        productViewInfo.setProductType(requestString("productType"));
        productViewInfo.setProductName(requestString("productName"));
        productViewInfo.setSalesType(OrderProductSalesType.of(requestString("salesType")));
        productViewInfo.setUserType(UserType.valueOf(requestString("userType")));
        productViewInfo.setRecommendProducts(requestProductViewBase("recommendProductIds"));
        productViewInfo.setSubProducts(requestProductViewBase("packageProductIds"));

        return productViewInfo;
    }

    private List<ProductViewInfo.ProductItem> requestProductViewBase(String parameterName) {

        String idStr = requestString(parameterName);
        if (StringUtils.isBlank(idStr)) {
            return Collections.emptyList();
        }

        String[] ids = StringUtils.split(idStr, ",");

        List<ProductViewInfo.ProductItem> items = new LinkedList<>();

        for (String id : ids) {
            if (StringUtils.isBlank(id) || id.length() == 1 || !id.contains("@") || id.startsWith("@")) {
                continue;
            }
            ProductViewInfo.ProductItem item = new ProductViewInfo.ProductItem();

            item.setProductType(id.substring(0, id.indexOf("@")));
            if (!id.endsWith("@")) {
                item.setProductId(id.substring(id.indexOf("@") + 1));
            }
            items.add(item);
        }

        return items;
    }


    @RequestMapping(value = "/productlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getProductList() {
        String productType = getRequestString("productType");
        MapMessage message = MapMessage.successMessage();
        List<OrderProduct> productList =
                userOrderLoaderClient.loadAllOrderProductIncludeOfflineForCrm();
        if (StringUtils.isNotBlank(productType)) {
            productList = productList
                    .stream()
                    .filter(x -> StringUtils.equals(x.getProductType(), productType))
                    .collect(Collectors.toList());
        }
        OrderProduct product = new OrderProduct();
        product.setName("默认全部");
        product.setId("");
        productList.add(0, product);
        message.put("products", productList);
        return message;
    }

    @RequestMapping(value = "/ispackageproduct.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage isPackageProduct() {
        MapMessage message = MapMessage.successMessage();
        String productId = getRequestString("productId");
        if (StringUtils.isBlank(productId)) {
            return message.add("exists", false);
        }
        Map<String, List<OrderProductItem>> map
                = userOrderLoaderClient.loadProductItemsByProductIds(Collections.singleton(productId));
        boolean exists = MapUtils.isNotEmpty(map)
                && CollectionUtils.isNotEmpty(map.get(productId))
                && map.get(productId).size() > 1;

        return message.add("exists", exists);
    }


    // region 辅助方法
    @RequestMapping(value = "/upload.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadBackground(MultipartFile inputFile) {
        try {
            String path = AdminOssManageUtils.upload(inputFile, "lordaeron");
            return MapMessage.successMessage().add("path", path);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(
            value = "/ueditorcontroller.vpage",
            method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ueditorController() {
        String action = getRequestString("action");
        MapMessage mapMessage = new MapMessage();

        switch (action) {
            case "config":
                mapMessage.set("imageActionName", "uploadimage");
                mapMessage.set("imageFieldName", "upfile");
                mapMessage.set("imageInsertAlign", "none");
                mapMessage.set("imageMaxSize", 2048000);
                mapMessage.set("imageUrlPrefix", "");
                mapMessage.set("imageAllowFiles", new String[]{".png", ".jpg", ".jpeg", ".gif"});
                mapMessage.set("videoActionName", "uploadvideo");
                mapMessage.set("videoFieldName", "upfile");
                mapMessage.set("videoUrlPrefix", "");
                mapMessage.set("videoMaxSize", 20971520);
                mapMessage.set("videoAllowFiles",
                        new String[]{
                                ".flv", ".swf", ".mkv", ".avi", ".rm", ".rmvb", ".mpeg", ".mpg", ".ogg", ".ogv",
                                ".mov", ".wmv", ".mp4", ".webm", ".mp3", ".wav", ".mid"
                        });
                mapMessage.setSuccess(true);
                break;
            case "uploadimage":
            case "uploadvideo":
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
                MultipartFile imgFile = multipartRequest.getFile("upfile");

                if (imgFile.isEmpty()) {
                    mapMessage.setSuccess(false);
                    mapMessage.setInfo("没有文件上传");
                } else {
                    try {
                        String filename = imgFile.getOriginalFilename();
                        String path = AdminOssManageUtils.upload(imgFile, "lordaeron/editor");
                        mapMessage.add("url", path)
                                .add("title", filename)
                                .add("state", "SUCCESS")
                                .add("original", filename);
                        mapMessage.setSuccess(true);
                    } catch (Exception e) {
                        mapMessage.setSuccess(false);
                        mapMessage.setInfo("文件上传异常");
                    }
                }
                break;
        }

        return mapMessage;
    }
    // endregion
}
