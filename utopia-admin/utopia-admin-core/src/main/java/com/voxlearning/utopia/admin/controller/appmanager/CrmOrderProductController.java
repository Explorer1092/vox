package com.voxlearning.utopia.admin.controller.appmanager;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.admin.controller.crm.CrmAbstractController;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.order.api.constants.OrderActiveType;
import com.voxlearning.utopia.service.order.api.constants.OrderProductAmortizeType;
import com.voxlearning.utopia.service.order.api.constants.OrderProductSalesType;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItemRef;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.helper.Validate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

/**
 * Created by Summer on 2016/12/13.
 */
@Controller
@RequestMapping("/appmanager/product")
public class CrmOrderProductController extends CrmAbstractController {
    @Inject private UserOrderLoaderClient userOrderLoaderClient;
    @Inject private UserOrderServiceClient userOrderServiceClient;

    // 列表页
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        // 获取全部商品
        int page = getRequestInt("page", 1);
        if (page <= 0) page = 1;
        Pageable pageable = new PageRequest(page - 1, 10);

        String name = getRequestString("name");
        String productType = getRequestString("productType");
        List<OrderProduct> productList = userOrderLoaderClient.loadAllOrderProductIncludeOfflineForCrm();
        if (StringUtils.isNotBlank(name)) {
            productList = productList.stream().filter(p -> p.getName().contains(name))
                    .collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(productType)) {
            productList = productList.stream().filter(p -> Objects.equals(p.getProductType(), productType))
                    .collect(Collectors.toList());
        }
        Page<OrderProduct> productPage = PageableUtils.listToPage(productList, pageable);
        model.addAttribute("productPage", productPage);
        model.addAttribute("currentPage", productPage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", productPage.getTotalPages());
        model.addAttribute("hasPrev", productPage.hasPrevious());
        model.addAttribute("hasNext", productPage.hasNext());
        model.addAttribute("name", name);
        model.addAttribute("productType", productType);
        List<OrderProductServiceType> types = Arrays.asList(OrderProductServiceType.values());
        model.addAttribute("productTypes", types.stream().filter(t -> !t.isOrderClosed()).collect(Collectors.toList()));
        return "apps/product/index";
    }

    // 添加编辑跳转
    @RequestMapping(value = "productdetail.vpage", method = RequestMethod.GET)
    public String productDetail(Model model) {
        String id = getRequestString("id");
        if (StringUtils.isNotBlank(id)) {
            OrderProduct product = userOrderLoaderClient.loadOrderProductById(id);
            if (product != null) {
                model.addAttribute("product", product);
                // 获取已经关联的item
                List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductId(id);
                if (CollectionUtils.isNotEmpty(itemList)) {
                    model.addAttribute("itemList", itemList);
                }
            }
        }
        model.addAttribute("id", id);
        List<OrderProductServiceType> types = Arrays.asList(OrderProductServiceType.values());
        model.addAttribute("productTypes", types.stream().filter(t -> !t.isOrderClosed()).collect(Collectors.toList()));
        model.addAttribute("saleTypes", OrderProductSalesType.values());
        return "apps/product/productdetail";
    }

    // 添加编辑 post
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveProduct() {
        // 获取参数
        String id = getRequestString("id");
        String name = getRequestString("name");
        String desc = getRequestString("desc");
        String productType = getRequestString("productType");
        Double price = getRequestDouble("price", 0);
        Double originalPrice = getRequestDouble("originalPrice", 0);
        String attributes = getRequestString("attributes");
        String status = getRequestString("status");
        String category = getRequestString("category");
        String salesType = getRequestString("salesType");
        Boolean actualGoods = getRequestBool("actualGoods");
        if (StringUtils.isBlank(name)) {
            return MapMessage.errorMessage("请输入商品名称");
        }
        try {
            OrderProduct product = null;
            if (StringUtils.isNotBlank(id)) {
                product = userOrderLoaderClient.loadOrderProductById(id);
            }
            if (product == null) {
                product = new OrderProduct();
            }
            product.setName(name);
            product.setDesc(desc);
            product.setPrice(new BigDecimal(price));
            product.setOriginalPrice(new BigDecimal(originalPrice));
            product.setAttributes(attributes);
            product.setProductType(productType);
            product.setStatus(status);
            product.setCategory(category);
            product.setSalesType(OrderProductSalesType.of(salesType));
            product.setActualGoods(actualGoods);
            return userOrderServiceClient.saveOrderProduct(product);
        } catch (Exception ex) {
            logger.error("Save order product failed.", ex);
            return MapMessage.errorMessage("保存失败：" + ex.getMessage());
        }
    }

    // 获取可选的ITEM
    @RequestMapping(value = "loaditems.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadItems() {
        // 获取参数
        String productType = getRequestString("productType");
        if (StringUtils.isBlank(productType)) {
            return MapMessage.errorMessage("参数错误");
        }
        List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductType(OrderProductServiceType.valueOf(productType));
        return MapMessage.successMessage().add("itemList", itemList);
    }

    // 保存itemref
    @RequestMapping(value = "saveitemrefs.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveItemRefs() {
        // 获取参数
        String productId = getRequestString("productId");
        String itemIds = getRequestString("itemIds");
        if (StringUtils.isBlank(itemIds) && StringUtils.isBlank(productId)) {
            return MapMessage.successMessage();
        }
        OrderProduct product = userOrderLoaderClient.loadOrderProductById(productId);
        if (product == null) {
            return MapMessage.errorMessage("产品不存在");
        }
        String[] itemIdArray = StringUtils.split(itemIds, ",");
//        if (itemIdArray.length > 1) {
//            // 多个Item 有效期必须一样  否则报错
//            Map<String, OrderProductItem> itemMap = userOrderLoaderClient.loadOrderProductItems(Arrays.asList(itemIdArray));
//            OrderProductItem firstItem = itemMap.values().stream().findFirst().orElse(null);
//            OrderProductItem diffItem = itemMap.values().stream().filter(i -> i.getPeriod() == null || !Objects.equals(i.getPeriod(), firstItem.getPeriod()))
//                    .findAny().orElse(null);
//            if (diffItem != null) {
//                return MapMessage.errorMessage("多个子产品有效期必须相同");
//            }
//        }
        // 先删除 再保存
        userOrderServiceClient.deleteOrderProductItemRefByProductId(productId);
        for (String itemId : itemIdArray) {
            OrderProductItemRef ref = new OrderProductItemRef();
            ref.setProductId(productId);
            ref.setProductItemId(itemId);
            userOrderServiceClient.saveOrderProductItemRef(ref);
        }
        return MapMessage.successMessage();
    }


    // 删除商品
    @RequestMapping(value = "deleteproduct.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteProduct() {
        String id = getRequestString("id");
        try {
            OrderProduct product = userOrderLoaderClient.loadOrderProductById(id);
            if (product == null) {
                return MapMessage.errorMessage("无效的信息");
            }
            return userOrderServiceClient.deleteOrderProduct(id);
        } catch (Exception ex) {
            logger.error("delete order product failed, id={}", id, ex);
            return MapMessage.errorMessage("操作失败");
        }
    }

    // item列表页
    @RequestMapping(value = "itemlist.vpage", method = RequestMethod.GET)
    public String itemList(Model model) {
        // 获取全部商品
        int page = getRequestInt("page", 1);
        if (page <= 0) page = 1;
        Pageable pageable = new PageRequest(page - 1, 10);

        String name = getRequestString("name");
        String productType = getRequestString("productType");
        List<OrderProductItem> itemList = userOrderLoaderClient.loadAllOrderProductItems();
        if (StringUtils.isNotBlank(name)) {
            itemList = itemList.stream().filter(p -> p.getName().contains(name))
                    .collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(productType)) {
            itemList = itemList.stream().filter(p -> Objects.equals(p.getProductType(), productType))
                    .collect(Collectors.toList());
        }
        Page<OrderProductItem> itemPage = PageableUtils.listToPage(itemList, pageable);
        model.addAttribute("itemPage", itemPage);
        model.addAttribute("currentPage", itemPage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", itemPage.getTotalPages());
        model.addAttribute("hasPrev", itemPage.hasPrevious());
        model.addAttribute("hasNext", itemPage.hasNext());
        model.addAttribute("name", name);
        model.addAttribute("productType", productType);
        List<OrderProductServiceType> types = Arrays.asList(OrderProductServiceType.values());
        model.addAttribute("productTypes", types.stream().filter(t -> !t.isOrderClosed()).collect(Collectors.toList()));
        return "apps/product/itemlist";
    }

    // 添加编辑跳转
    @RequestMapping(value = "itemdetail.vpage", method = RequestMethod.GET)
    public String itemDetail(Model model) {
        String id = getRequestString("id");
        if (StringUtils.isNotBlank(id)) {
            OrderProductItem item = userOrderLoaderClient.loadOrderProductItemById(id);
            if (item != null) {
                model.addAttribute("item", item);
            }
        }
        model.addAttribute("id", id);
        List<OrderProductServiceType> types = Arrays.asList(OrderProductServiceType.values());
        model.addAttribute("productTypes", types.stream().filter(t -> !t.isOrderClosed()).collect(Collectors.toList()));
        model.addAttribute("saleTypes", OrderProductSalesType.values());
        model.addAttribute("activeTypes", OrderActiveType.values());
        model.addAttribute("amortizeTypes", OrderProductAmortizeType.values());
        return "apps/product/itemdetail";
    }

    // 添加编辑 post
    @RequestMapping(value = "saveitem.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveProductItem() {
        // 获取参数
        String id = getRequestString("id");
        String name = getRequestString("name");
        String desc = getRequestString("desc");
        String productType = getRequestString("productType");
        String salesType = getRequestString("salesType");
        Integer period = getRequestInt("period");
        Double originalPrice = getRequestDouble("originalPrice", 0);
        Boolean repurchaseAllowed = getRequestBool("repurchaseAllowed");
        String activeType = getRequestString("activeType");
        String amortizeType = getRequestString("amortizeType");
        String appItemId = getRequestString("appItemId");
        appItemId = appItemId.replaceAll(" ","");
        if (StringUtils.isBlank(name)) {
            return MapMessage.errorMessage("请输入商品名称");
        }
        try {
            OrderProductItem item = null;
            if (StringUtils.isNotBlank(id)) {
                item = userOrderLoaderClient.loadOrderProductItemById(id);
            }
            if (item == null) {
                item = new OrderProductItem();
            }
            item.setName(name);
            item.setDesc(desc);
            item.setProductType(productType);
            item.setSalesType(OrderProductSalesType.of(salesType));
            item.setPeriod(period);
            item.setOriginalPrice(new BigDecimal(originalPrice));
            item.setRepurchaseAllowed(repurchaseAllowed);
            item.setActiveType(OrderActiveType.valueOf(activeType));
            item.setAmortizeType(OrderProductAmortizeType.valueOf(amortizeType));
            item.setAppItemId(appItemId);
            return userOrderServiceClient.saveOrderProductItem(item);
        } catch (Exception ex) {
            logger.error("Save order product item failed.", ex);
            return MapMessage.errorMessage("保存失败：" + ex.getMessage());
        }
    }

    // 删除商品item
    @RequestMapping(value = "deleteproductitem.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteProductItem() {
        String id = getRequestString("id");
        try {
            OrderProductItem item = userOrderLoaderClient.loadOrderProductItemById(id);
            if (item == null) {
                return MapMessage.errorMessage("无效的信息");
            }
            userOrderServiceClient.deleteOrderProductItem(id);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("delete order product item failed, id={}", id, ex);
            return MapMessage.errorMessage("操作失败");
        }
    }

    /**
     * 批量导入商品，上传excel，然后解析。
     * @return
     */
    @RequestMapping(value = "batch_add_product.vpage")
    @ResponseBody
    public MapMessage batchAddProduct(){
        try {
            MultipartRequest multiRequest = (MultipartRequest) getRequest();
            MultipartFile file = multiRequest.getFile("upFile");
            Validate.notNull(file,"上传文件为空!");

            XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
            XSSFSheet sheet = workbook.getSheetAt(0);

            Map<String, List<OrderProduct>> productMap = userOrderLoaderClient.loadAllOrderProductIncludeOffline()
                    .stream()
                    .collect(Collectors.groupingBy(p -> p.getName()));

            MapMessage resultMsg;
            XSSFRow row;
            String name;
            for(int rowIndex = 1;(row = sheet.getRow(rowIndex)) != null ; rowIndex ++){
                name = Optional.ofNullable(row.getCell(0))
                        .map(XSSFCell::getStringCellValue)
                        .orElse(null);
                if(StringUtils.isEmpty(name))
                    break;

                OrderProductServiceType productType = OrderProductServiceType.safeParse(row.getCell(2).getStringCellValue());
                OrderProductSalesType salesType = OrderProductSalesType.of(row.getCell(3).getStringCellValue());

                Validate.isTrue(productType != OrderProductServiceType.Unknown,"");

                OrderProduct newProduct = new OrderProduct();
                newProduct.setName(name);

                // 如果商品已经存在了。则覆盖掉信息
                OrderProduct existProduct = productMap.getOrDefault(newProduct.getName(), emptyList())
                        .stream()
                        .findFirst()
                        .orElse(null);
                if(existProduct != null){
                    newProduct.setId(existProduct.getId());
                }

                newProduct.setDesc(row.getCell(1).getStringCellValue());
                newProduct.setProductType(productType == null ? null : productType.name());
                newProduct.setSalesType(salesType);
                newProduct.setPrice(new BigDecimal(row.getCell(4).getNumericCellValue()));
                newProduct.setOriginalPrice(new BigDecimal(row.getCell(5).getNumericCellValue()));
                newProduct.setCategory(row.getCell(6).getStringCellValue());
                newProduct.setAttributes(row.getCell(7).getStringCellValue());
                newProduct.setStatus(row.getCell(8).getStringCellValue());

                resultMsg = userOrderServiceClient.saveOrderProduct(newProduct);
                String pId = MapUtils.getString(resultMsg,"id");
                Validate.isTrue(resultMsg.isSuccess(),StringUtils.formatMessage("第{}行导入失败,创建商品失败,detail:",rowIndex,resultMsg.getInfo()));

                OrderProductItem item = new OrderProductItem();
                if(existProduct != null){
                    // 如果子商品已经存在了，则覆盖掉信息
                    OrderProductItem existItem = userOrderLoaderClient.loadProductItemsByProductId(existProduct.getId())
                            .stream()
                            .findFirst()
                            .orElse(null);
                    if(existItem != null){
                        item.setId(existItem.getId());
                    }
                }

                item.setName(row.getCell(9).getStringCellValue());
                item.setDesc(row.getCell(10).getStringCellValue());
                item.setProductType(productType == null ? null : productType.name());
                item.setSalesType(salesType);
                item.setActiveType(OrderActiveType.valueOf(row.getCell(13).getStringCellValue()));
                item.setAmortizeType(OrderProductAmortizeType.of(row.getCell(14).getStringCellValue()));
                item.setPeriod((int)row.getCell(15).getNumericCellValue());
                item.setOriginalPrice(BigDecimal.valueOf(row.getCell(16).getNumericCellValue()));
                item.setRepurchaseAllowed(SafeConverter.toBoolean(row.getCell(17).getRawValue()));
                item.setAppItemId(row.getCell(18).getStringCellValue());

                resultMsg = userOrderServiceClient.saveOrderProductItem(item);

                Validate.isTrue(resultMsg.isSuccess(),StringUtils.formatMessage("第{}行导入失败,创建子商品失败,detail:",rowIndex,resultMsg.getInfo()));
                String itemId = MapUtils.getString(resultMsg,"id");

                OrderProductItemRef ref = new OrderProductItemRef();
                ref.setProductId(pId);
                ref.setProductItemId(itemId);

                // 对于已经存在的，不用保存关联关系
                if(existProduct == null)
                    userOrderServiceClient.saveOrderProductItemRef(ref);
            }
            return MapMessage.successMessage();
        }catch (Exception e){
            logger.error("Batch add product errlor!",e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }
}
