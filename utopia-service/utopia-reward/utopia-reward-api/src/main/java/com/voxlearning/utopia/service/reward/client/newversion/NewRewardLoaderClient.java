package com.voxlearning.utopia.service.reward.client.newversion;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.utopia.api.constant.AmbassadorLevel;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.reward.api.RewardLoader;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.entity.ProductFilterEntity;
import com.voxlearning.utopia.service.reward.api.newversion.NewRewardLoader;
import com.voxlearning.utopia.service.reward.base.support.RewardProductDetailGenerator;
import com.voxlearning.utopia.service.reward.constant.*;
import com.voxlearning.utopia.service.reward.consumer.RewardLoaderClient;
import com.voxlearning.utopia.service.reward.consumer.newversion.RewardCenterClient;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.entity.newversion.*;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetailPagination;
import com.voxlearning.utopia.service.reward.mapper.product.CategoryMapper;
import com.voxlearning.utopia.service.reward.mapper.product.MobileHomePageCategoryMapper;
import com.voxlearning.utopia.service.reward.mapper.product.crm.GetProductDetailMapper;
import com.voxlearning.utopia.service.reward.util.RewardProductDetailUtils;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class NewRewardLoaderClient {

    private static final Logger log = LoggerFactory.getLogger(NewRewardLoaderClient.class);

    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject
    private NewRewardBufferLoaderClient newRewardBufferLoaderClient;
    @Inject
    private RewardLoaderClient rewardLoaderClient;
    @Inject
    RewardCenterClient rewardCenterClient;
    @Inject private
    RewardLoader rewardLoader;
    @ImportService(interfaceClass = NewRewardLoader.class)
    private NewRewardLoader remoteReference;

    @Inject private ParentLoaderClient parentLoaderClient;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject private UserLoaderClient userLoaderClient;

    public List<ProductFilterEntity> buildFilterItem(Integer userVisibleType, Long categoryId, Integer categoryType, Long oneLevelFilterId, Long twoLevelFilterId) {
        List<ProductFilterEntity> filter;
        if (Objects.equals(MapperCategoryType.SET.intType(), categoryType)) {
            filter = buildSetFilterItem(categoryId);
            checkSetFilterItem(filter, oneLevelFilterId);
        } else {
            filter = biuldCategoryFilterItem(userVisibleType, categoryId);
            checkCategoryFilterItem(filter, oneLevelFilterId, twoLevelFilterId);
        }
        return filter;
    }

    //选中分类类型为分类集合的过滤条件（无一级过滤条件）
    private void checkSetFilterItem(List<ProductFilterEntity> filter, Long twoLevelFilterId) {
        if (CollectionUtils.isEmpty(filter)) {
            return;
        }

        //选择了二级过滤条件
        if (Objects.equals(twoLevelFilterId, 0L)) {
            filter.add(0, ProductFilterEntity.buildDefaultFilterItem(true));
            //选择了二级过滤条件
        } else {
            filter.stream()
                    .forEach(item -> {
                        if (Objects.equals(twoLevelFilterId, item.getId())) {
                            item.setStatus(true);
                        }
                    });
            filter.add(0, ProductFilterEntity.buildDefaultFilterItem(false));
        }
    }

    //选中分类类型为一级分类的过滤条件
    private void checkCategoryFilterItem(List<ProductFilterEntity> filter, Long oneLevelFilterId, Long twoLevelFilterId) {
        if (CollectionUtils.isEmpty(filter)) {
            return;
        }

        //过滤条件都不选择
        if (Objects.equals(oneLevelFilterId, 0L)) {
            filter.add(0, ProductFilterEntity.buildDefaultFilterItem(true));
            return;
        } else {//选择了一级过滤条件
            filter.stream()
                    .forEach(item -> {
                        if (Objects.equals(oneLevelFilterId, item.getId())) {
                            item.setStatus(true);
                            if (CollectionUtils.isNotEmpty(item.getChildren())) {
                                if (!Objects.equals(twoLevelFilterId, 0L)) {//选择了二级过滤条件
                                    item.getChildren().stream().forEach(twoLevelItem -> {
                                        if (Objects.equals(twoLevelFilterId, twoLevelItem.getId())) {
                                            twoLevelItem.setStatus(true);
                                        }
                                    });
                                    item.getChildren().add(0, ProductFilterEntity.buildDefaultFilterItem(false));
                                } else {//没有选择二级过滤条件
                                    item.getChildren().add(0, ProductFilterEntity.buildDefaultFilterItem(true));
                                }
                            }
                        }
                    });
            filter.add(0, ProductFilterEntity.buildDefaultFilterItem(false));
            //选择一级过滤不选择条件
            if (Objects.equals(twoLevelFilterId, 0L)) {

            } else {//一二级过滤条件都选择

            }
        }
    }

    //构建分类类型为分类集合的过滤器
    private List<ProductFilterEntity> buildSetFilterItem(Long setId) {
        List<ProductFilterEntity> result = new ArrayList<>();
        result.addAll(biuldTagFilterItem(setId, ProductTag.ParentType.SET.getType()));
        return result;
    }

    //构建分类类型为一级分类的过滤器
    private List<ProductFilterEntity> biuldCategoryFilterItem(Integer userVisibleType, Long oneLevelCategoryId) {
        List<ProductFilterEntity> result = new ArrayList<>();

        List<ProductCategory> twoLevelCategoryList = newRewardBufferLoaderClient.getProductCategoryBuffer().loadByParentId(oneLevelCategoryId);
        if (CollectionUtils.isNotEmpty(twoLevelCategoryList)) {
            result = twoLevelCategoryList.stream()
                    .filter(category -> RewardUserVisibilityType.isVisible(category.getVisible(), userVisibleType))
                    .sorted(Comparator.comparing(ProductCategory::getDisplayOrder).reversed())
                    .map(category -> {
                        ProductFilterEntity oneLevelFilter = new ProductFilterEntity();
                        oneLevelFilter.setId(category.getId());
                        oneLevelFilter.setName(category.getName());
                        List<ProductFilterEntity> twoLevelFilter = biuldTagFilterItem(category.getId(), ProductTag.ParentType.CATEGPRY.getType());
                        oneLevelFilter.setChildren(twoLevelFilter);
                        return oneLevelFilter;
                    }).collect(toList());
        }
        return result;
    }

    private List<ProductFilterEntity> biuldTagFilterItem(Long parentId, Integer parentType) {
        List<ProductFilterEntity> result = Collections.EMPTY_LIST;
        List<ProductTag> tagList = loadProductTagByParent(parentId, parentType);
        if (CollectionUtils.isNotEmpty(tagList)) {
            result = tagList.stream()
                    .sorted(Comparator.comparing(ProductTag::getDisplayOrder).reversed())
                    .map(tag -> {
                        ProductFilterEntity twoLevelFilterItem = new ProductFilterEntity();
                        twoLevelFilterItem.setId(tag.getId());
                        twoLevelFilterItem.setName(tag.getName());
                        twoLevelFilterItem.setChildren(new ArrayList<>());
                        return twoLevelFilterItem;
                    }).collect(toList());
        }
        return result;
    }

    public List<MobileHomePageCategoryMapper> getMobileHomePageCategoryMapper(User user) {
        Integer userVisibleType = RewardUserVisibilityType.getVisibleType(user);

        List<CategoryMapper> categoryMappers = getCategoryMapper(user, userVisibleType, RewardDisplayTerminal.Mobile.name());

        Map<Long, RewardProduct> productMap = rewardLoaderClient.loadRewardProductMap();
        Set<Long> hasInventoryProductIds = new HashSet<>(rewardLoaderClient.getHasInventoryProducts());
        Map<Long, List<RewardProductTarget>> productTargetsMap = newRewardBufferLoaderClient.getProductTargetBuffer().loadAllProductTargets();

        List<MobileHomePageCategoryMapper> mobileCategoryMappers = categoryMappers.stream()
                .map(category -> {

                    Set<Long> productIds;
                    if (Objects.equals(category.getType(), MapperCategoryType.CATEGORY.intType())) {
                        productIds = rewardLoaderClient.loadProductIdByOneLevelCategoryId(category.getId());
                    } else {
                        productIds = newRewardBufferLoaderClient.loadProductIdBySetId(category.getId());
                    }

                    Map<Long, List<RewardImage>> rewardImages = rewardLoader.loadProductRewardImages(productIds);

                    List<ProductCategory> oneLevelCategory = newRewardBufferLoaderClient.getProductCategoryBuffer().loadProductCategoryByLevel(1);
                    Map<Long, Integer> categoryIdTypeMap = oneLevelCategory.stream().collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getOneLevelCategoryType));

                    //已拥有订单
                    Map<Long, RewardOrder> userAllOrderMap = rewardLoaderClient.getRewardOrderLoader().loadUserRewardOrders(user.getId())
                            .stream().collect(Collectors.toMap(RewardOrder::getProductId, Function.identity(), (o1, o2) -> o1.getId()>o2.getId() ? o1:o2));

                    Map<Long, RewardCouponDetail> userAllCouponMap = rewardLoaderClient.getRewardCouponDetailLoader().loadUserRewardCouponDetails(user.getId())
                            .stream().collect(Collectors.toMap(RewardCouponDetail::getProductId, Function.identity(), (o1, o2) -> o1.getId()>o2.getId() ? o1:o2));

                    List<RewardProductDetail> productList = productIds
                            .stream()
                            .map(id -> productMap.get(id))
                            .filter(product -> showFilter(user, RewardDisplayTerminal.Mobile.name(), hasInventoryProductIds, productTargetsMap, product))
                            .map(product -> {
                                RewardImage selectedImage = rewardLoader.pickDisplayImage(user, rewardImages.get(product.getId()));
                                String tagName = getTagName(user, product.getId());
                                return generateRewardProductDetail(product, tagName, user, rewardImages, selectedImage, categoryIdTypeMap, userAllOrderMap, userAllCouponMap);
                            })
                            .collect(toList());

                    productList = RewardProductDetailUtils.orderProducts(user, productList, "", "");

                    MobileHomePageCategoryMapper mobileCategoryMapper = new MobileHomePageCategoryMapper();
                    mobileCategoryMapper.setDisplayOrder(category.getDisplayOrder());
                    mobileCategoryMapper.setId(category.getId());
                    mobileCategoryMapper.setName(category.getName());
                    mobileCategoryMapper.setType(category.getType());
                    mobileCategoryMapper.setOneLevelCategoryType(Objects.equals(category.getType(), MapperCategoryType.SET.intType()) ? category.getId().intValue():newRewardBufferLoaderClient.getProductCategoryBuffer().getOneLevelCategoryType(category.getId()));
                    mobileCategoryMapper.setProductList(productList.size()>3 ? productList.subList(0, 3):productList);
                    if (user.isStudent()) {
                        StudentDetail studentDetail = (StudentDetail) user;
                        if (studentDetail.isJuniorStudent()) {
                            mobileCategoryMapper.setProductList(productList.size()>9 ? productList.subList(0, 9):productList);
                        } else {
                            mobileCategoryMapper.setProductList(productList.size()>5 ? productList.subList(0, 5):productList);
                        }
                    }
                    return mobileCategoryMapper;
                })
                .filter(i -> CollectionUtils.isNotEmpty(i.getProductList())).collect(toList());
        return mobileCategoryMappers;
    }

    public RewardProductDetailPagination loadOnePageProductList(
            User user,
            String displayTerminal,
            Integer categoryType,
            Long categoryId,
            Long oneLevelFilterId,
            Long twoLevelFilterId,
            Boolean showAffordable,
            Integer pageNum,
            Integer pageSize,
            String orderBy,
            String upDown,
            Double couponDiecount) {
        return loadOnePageProductList(
                user,
                displayTerminal,
                categoryType,
                categoryId,
                oneLevelFilterId,
                twoLevelFilterId,
                showAffordable,
                pageNum,
                pageSize,
                orderBy,
                upDown,
                couponDiecount,
                procutId -> true);
    }

    public RewardProductDetailPagination loadOnePageProductList(
            User user,
            String displayTerminal,
            Integer categoryType,
            Long categoryId,
            Long oneLevelFilterId,
            Long twoLevelFilterId,
            Boolean showAffordable,
            Integer pageNum,
            Integer pageSize,
            String orderBy,
            String upDown,
            Double couponDiecount,
            Predicate<RewardProduct> productIdFilter) {

        List<RewardProductDetail> productList = loadProductList(user, displayTerminal, categoryType, categoryId, oneLevelFilterId, twoLevelFilterId, showAffordable, couponDiecount, productIdFilter);
        long total = productList.size();
        int start = pageNum * pageSize;
        int end = Math.min((int) total, (pageNum + 1) * pageSize);
        if (start > total) {
            return new RewardProductDetailPagination(Collections.<RewardProductDetail>emptyList());
        }
        productList = RewardProductDetailUtils.orderProducts(user, productList, orderBy, upDown);
        productList = new LinkedList<>(productList.subList(start, end));
        return new RewardProductDetailPagination(
                productList,
                new PageRequest(pageNum, pageSize),
                total);
    }

    public Set<Long> loadProductIdSet(Integer categoryType,
                                      Long categoryId,
                                      Long oneLevelFilterId,
                                      Long twoLevelFilterId) {
        Set<Long> productIdSet;
        if (Objects.equals(categoryType, MapperCategoryType.CATEGORY.intType())) {
            if (!Objects.equals(twoLevelFilterId, 0L)) {
                productIdSet = newRewardBufferLoaderClient.loadProductIdByTagId(twoLevelFilterId);
            } else if (!Objects.equals(oneLevelFilterId, 0L)) {
                productIdSet = newRewardBufferLoaderClient.loadProductIdByCategoryId(oneLevelFilterId);
            } else {
                productIdSet = rewardLoaderClient.loadProductIdByOneLevelCategoryId(categoryId);
            }
        } else {
            if (!Objects.equals(oneLevelFilterId, 0L)) {
                productIdSet = newRewardBufferLoaderClient.loadProductIdByTagId(oneLevelFilterId);
            } else {
                productIdSet = newRewardBufferLoaderClient.loadProductIdBySetId(categoryId);
            }
        }
        return productIdSet;
    }

    public List<RewardProductDetail> loadProductList(
            User user,
            String displayTerminal,
            Integer categoryType,
            Long categoryId,
            Long oneLevelFilterId,
            Long twoLevelFilterId,
            Boolean showAffordable,
            Double couponDiecount,
            Predicate<RewardProduct> productIdFilter) {
        //得到所选择分类和标签相应的prodctid列表
        Set<Long> productIdSet = loadProductIdSet(categoryType, categoryId, oneLevelFilterId, twoLevelFilterId);

        Map<Long, List<RewardImage>> rewardImages = rewardLoader.loadProductRewardImages(productIdSet);

        Map<Long, List<RewardProductTarget>> productTargetsMap = newRewardBufferLoaderClient.getProductTargetBuffer().loadAllProductTargets();
        Set<Long> hasInventoryProductIds = new HashSet<>(rewardLoaderClient.getHasInventoryProducts());
        List<ProductCategory> oneLevelCategory = newRewardBufferLoaderClient.getProductCategoryBuffer().loadProductCategoryByLevel(1);
        Map<Long, Integer> categoryIdTypeMap = oneLevelCategory.stream().collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getOneLevelCategoryType));
        Map<Long, RewardProduct> productMap = rewardLoaderClient.loadRewardProductMap();
        Long integral = rewardCenterClient.getIntegral(user);

        //已拥有订单
        Map<Long, RewardOrder> userAllOrderMap = rewardLoaderClient.getRewardOrderLoader().loadUserRewardOrders(user.getId())
                .stream().collect(Collectors.toMap(RewardOrder::getProductId, Function.identity(), (o1, o2) -> o1.getId()>o2.getId() ? o1:o2));

        Map<Long, RewardCouponDetail> userAllCouponMap = rewardLoaderClient.getRewardCouponDetailLoader().loadUserRewardCouponDetails(user.getId())
                .stream().collect(Collectors.toMap(RewardCouponDetail::getProductId, Function.identity(), (o1, o2) -> o1.getId()>o2.getId() ? o1:o2));

        List<RewardProductDetail> productList = productIdSet
                .stream()
                .map(id -> productMap.get(id))
                .filter(productIdFilter)
                .filter(product -> showFilter(user, displayTerminal, hasInventoryProductIds, productTargetsMap, product))
                .filter(product -> showAffordableFilter(showAffordable, integral, couponDiecount, product, user))
                .map(product -> {
                    RewardImage selectedImage = rewardLoader.pickDisplayImage(user, rewardImages.get(product.getId()));
                    String tagName = getTagName(user, product.getId());
                    return generateRewardProductDetail(product, tagName, user, rewardImages, selectedImage, categoryIdTypeMap, userAllOrderMap, userAllCouponMap);
                })
                .collect(toList());
        return productList;
    }

    /**
     * 额外处理头饰，细节逻辑
     * @param relateProductIdSet
     * @param privilegeMap
     * @param product
     */
    public void proHeadWearExtra(Set<Long> relateProductIdSet, Map<String, Privilege> privilegeMap, RewardProductDetail product) {
        if (!isHeadWear(product.getOneLevelCategoryId())) {
            return;
        }
        //是否已拥有
        if (CollectionUtils.isNotEmpty(relateProductIdSet)) {
            boolean isHave = CollectionUtils.isNotEmpty(relateProductIdSet) && relateProductIdSet.contains(product.getId());
            product.setHave(isHave);
        }

        proTobyAndHeadWearPrice(product);

        // 来源
        Privilege privilege = privilegeMap.get(product.getRelateVirtualItemId());
        if (privilege != null) {
            product.setOrigin(privilege.getOrigin());
            product.setHeadWearImg(privilege.getImg());
        }
    }

    /**
     * 额外处理微课逻辑
     * @param userAllOrderMap
     * @param product
     */
    public void proMiniCourseExtra(Map<Long, RewardOrder> userAllOrderMap, RewardProductDetail product) {
        //是否已拥有
        proIsHasExtra(userAllOrderMap, product);
    }

    /**
     * 额外处理教学资源逻辑
     * @param userAllOrderMap
     * @param product
     */
    public void proTeachingResourceExtra(Map<Long, RewardOrder> userAllOrderMap, Map<Long, RewardCouponDetail>  userAllCouponMap, RewardProductDetail product) {
        //是否已拥有
        proIsHasExtra(userAllOrderMap, product);
        //额外处理，之前教学资源仅仅入优惠券兑换记录表，所以部分教学资源，需要在此判断是否已拥有
        if (MapUtils.isNotEmpty(userAllCouponMap) && userAllCouponMap.containsKey(product.getId())) {
            product.setHave(true);
        }
    }

    /**
     * 额外处理，是否已拥有逻辑
     * @param userAllOrderMap
     * @param product
     */
    public void proIsHasExtra(Map<Long, RewardOrder> userAllOrderMap, RewardProductDetail product) {
        //是否已拥有
        if (MapUtils.isNotEmpty(userAllOrderMap) && userAllOrderMap.containsKey(product.getId())) {
            product.setHave(true);
        }
    }

    /**
     * 额外处理托比，细节逻辑
     * @param product
     */
    public void proTobyExtra(RewardProductDetail product) {
        proTobyAndHeadWearPrice(product);
    }

    /**
     * 处理头饰和托比价格显示
     * @param product
     */
    public void proTobyAndHeadWearPrice(RewardProductDetail product) {
        BigDecimal price = new BigDecimal(product.getPrice()).multiply(new BigDecimal(2)).multiply(new BigDecimal(0.9));
        int totalPrice = price.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();

        RewardProductDetail.PriceDay priceDay1 = new RewardProductDetail.PriceDay();
        priceDay1.setDay(product.getExpiryDate());
        priceDay1.setPrice(product.getPrice());
        priceDay1.setQuantity(1);

        RewardProductDetail.PriceDay priceDay2 = new RewardProductDetail.PriceDay();
        priceDay2.setDay(product.getExpiryDate() * 2);
        priceDay2.setPrice(SafeConverter.toDouble(totalPrice));
        priceDay2.setQuantity(2);
        product.setPriceDay(Arrays.asList(priceDay1, priceDay2));
    }

    public String getTagName(User user, Long productId) {
        Set<Long> tagIdSet =  newRewardBufferLoaderClient.loadTagIdByProductId(productId);
        String tagName = StringUtils.EMPTY;
        if (CollectionUtils.isNotEmpty(tagIdSet)) {
            if (user.isStudent()) {//如果是学生并且有公益标签返回公益标签
                StudentDetail studentDetail = (StudentDetail) user;
                if (studentDetail.isPrimaryStudent() && tagIdSet.contains(TagIdLogicRelation.PUBLIC_GOOD.getNumber())) {
                    return TagIdLogicRelation.PUBLIC_GOOD.getName();
                }
            } else if (user.isTeacher()) {//如果是小学老师并且有特权标签，返回特权标签
                TeacherDetail teacherDetail = (TeacherDetail)user;
                if (teacherDetail.isJuniorTeacher()) {
                    tagName = StringUtils.EMPTY;
                } else {
                    if (tagIdSet.contains(TagIdLogicRelation.TEACHER_PRIVILEGE_SENIOR.getNumber())) {
                        return TagIdLogicRelation.TEACHER_PRIVILEGE_SENIOR.getName();
                    } else if (tagIdSet.contains(TagIdLogicRelation.TEACHER_PRIVILEGE_SUPER.getNumber())) {
                        return TagIdLogicRelation.TEACHER_PRIVILEGE_SUPER.getName();
                    }
                }
            }
        }
        return tagName;
    }

    public List<CategoryMapper> getCategoryMapper(User user, Integer userVisibleType, String displayTerminal) {
        List<CategoryMapper> mappers = new ArrayList<>();

        //显示在首页的一级分类
        List<ProductCategory> productCategoryList = newRewardBufferLoaderClient.getProductCategoryBuffer().loadProductCategoryByLevel(ProductCategory.Level.ONE_LEVEL.getLevel())
                .stream()
                .filter(ProductCategory::getDisplay)
                .filter(category ->isShowOneLevelCategoryType(user, MapperCategoryType.CATEGORY.intType(), category.getOneLevelCategoryType(), displayTerminal))
                .filter(category -> RewardUserVisibilityType.isVisible(userVisibleType, category.getVisible()))
                .filter(category -> !Objects.equals(OneLevelCategoryType.JPZX_SHIWU.intType(), category.getOneLevelCategoryType()) || !isShiWuOffLineGrayCityWithSchoolLevel(user))
                .collect(toList());

        //显示在首页的分类集合
        List<ProductSet> productSetList = loadAllProductSet()
                .stream()
                .filter(ProductSet::getDisplay)
                .filter(set ->isShowOneLevelCategoryType(user, MapperCategoryType.SET.intType(), SafeConverter.toInt(set.getId()), displayTerminal))
                .filter(set -> RewardUserVisibilityType.isVisible(userVisibleType, set.getVisible()))
                .collect(toList());

        if (CollectionUtils.isNotEmpty(productCategoryList)) {
            mappers = productCategoryList.stream().map(category -> {
                CategoryMapper mapper = new CategoryMapper();
                mapper.setId(category.getId());
                mapper.setType(MapperCategoryType.CATEGORY.intType());
                mapper.setName(category.getName());
                mapper.setDisplayOrder(category.getDisplayOrder());
                return mapper;
            }).collect(toList());
        }

        if (CollectionUtils.isNotEmpty(productSetList)) {
            mappers.addAll(productSetList.stream().map(set -> {
                CategoryMapper mapper = new CategoryMapper();
                mapper.setId(set.getId());
                mapper.setName(set.getName());
                mapper.setType(MapperCategoryType.SET.intType());
                mapper.setDisplayOrder(set.getDisplayOrder());
                return mapper;
            }).collect(toList()));
        }
        //排序
        return mappers.stream().sorted(Comparator.comparing(CategoryMapper::getDisplayOrder).reversed()).collect(toList());
    }

    /**
     * pc端的一级分类和分类集合是否展示（人为限制）
     * @param user
     * @param type
     * @param value
     * @param displayTerminal
     * @return
     */
    public Boolean isShowOneLevelCategoryType(User user, Integer type, Integer value, String displayTerminal) {
        //app端不做限制
        if (Objects.equals(displayTerminal, RewardDisplayTerminal.Mobile.name())) {
            return true;
        }

        if (user.isStudent()) {
            if (Objects.equals(MapperCategoryType.CATEGORY.intType(), type)) {
                if (Objects.equals(OneLevelCategoryType.JPZX_SHIWU.intType(), value)) {
                    return true;
                }
            } else {
                if (Objects.equals(value, SetIdLogicRelation.TEACHER_PUBLIC_GOOD.getNumber())
                        || Objects.equals(value, SetIdLogicRelation.TEACHER_YIQI.getNumber())
                        || Objects.equals(value, SetIdLogicRelation.TEACHER_HOT.getNumber())) {
                    return true;
                }
            }
        }else if (user.isTeacher()) {
            if (Objects.equals(MapperCategoryType.CATEGORY.intType(), type)) {
                if (Objects.equals(OneLevelCategoryType.JPZX_SHIWU.intType(), value)
                        || Objects.equals(OneLevelCategoryType.JPZX_COUPON.intType(), value)
                        || Objects.equals(OneLevelCategoryType.JPZX_TEACHING_RESOURCES.intType(), value)) {
                    return true;
                }
            } else {
                if (Objects.equals(value, SetIdLogicRelation.TEACHER_PRIVILEGE.getNumber())) {
                    return true;
                }
            }
        }
        return false;
    }

    public RewardProductDetail generateRewardProductDetail(User user, Long productId) {
        RewardProduct product = rewardLoaderClient.loadRewardProductMap().get(productId);
        if (Objects.isNull(product)) {
            return null;
        }

        Map<Long, List<RewardImage>> rewardImages = rewardLoader.loadProductRewardImages(Collections.singletonList(productId));

        RewardImage selectedImage = rewardLoader.pickDisplayImage(user, rewardImages.get(product.getId()));
        String tagName = getTagName(user, product.getId());
        List<ProductCategory> oneLevelCategory = newRewardBufferLoaderClient.getProductCategoryBuffer().loadProductCategoryByLevel(1);
        Map<Long, Integer> categoryIdTypeMap = oneLevelCategory.stream().collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getOneLevelCategoryType));
        //已拥有订单
        Map<Long, RewardOrder> userAllOrderMap = rewardLoaderClient.getRewardOrderLoader().loadUserRewardOrders(user.getId())
                .stream().collect(Collectors.toMap(RewardOrder::getProductId, Function.identity(), (o1, o2) -> o1.getId()>o2.getId() ? o1:o2));

        Map<Long, RewardCouponDetail> userAllCouponMap = rewardLoaderClient.getRewardCouponDetailLoader().loadUserRewardCouponDetails(user.getId())
                .stream().collect(Collectors.toMap(RewardCouponDetail::getProductId, Function.identity(), (o1, o2) -> o1.getId()>o2.getId() ? o1:o2));

        return generateRewardProductDetail(product, tagName, user, rewardImages, selectedImage, categoryIdTypeMap, userAllOrderMap, userAllCouponMap);
    }

    public RewardProductDetail generateRewardProductDetail(RewardProduct product,
                                                           String showTagName,
                                                           User user,
                                                           Map<Long, List<RewardImage>> rewardImages,
                                                           RewardImage selectedImage,
                                                           Map<Long, Integer>categoryIdTypeMap,
                                                           Map<Long, RewardOrder> userAllOrderMap,
                                                           Map<Long, RewardCouponDetail> userAllCouponMap) {
        if (user.isTeacher()) {
            return generateTeacherRewardProductDetail(product, showTagName, (TeacherDetail) user, rewardImages, categoryIdTypeMap, userAllOrderMap, userAllCouponMap);
        } else if (user.isStudent()) {
            return generateStudentRewardProductDetails(product, showTagName, rewardImages, selectedImage, categoryIdTypeMap, userAllOrderMap, userAllCouponMap);
        }
        return null;
    }

    public RewardProductDetail generateStudentRewardProductDetails(RewardProduct product,
                                                                   String showTagName,
                                                                   Map<Long, List<RewardImage>> rewardImages,
                                                                   RewardImage selectedImage,
                                                                   Map<Long, Integer> categoryIdTypeMap,
                                                                   Map<Long, RewardOrder> userAllOrderMap,
                                                                   Map<Long, RewardCouponDetail> userAllCouponMap) {
        RewardProductDetail detail = new RewardProductDetail();
        detail.setExtenstionAttributes(new LinkedHashMap<>());
        detail.getExtenstionAttributes().put("product", product);

        detail.setRepeatExchanged(product.getRepeatExchanged());
        detail.setId(product.getId());
        detail.setProductName(product.getProductName());
        detail.setProductType(product.getProductType());
        detail.setDescription(product.getDescription());
        detail.setSoldQuantity(product.getSoldQuantity() == null ? 0 : product.getSoldQuantity());
        detail.setSaleGroup(product.getSaleGroup());
        detail.setWishQuantity(product.getWishQuantity() == null ? 0 : product.getWishQuantity());
        detail.setTags(showTagName);
        detail.setCreateDatetime(product.getCreateDatetime());
        String url;
        if (product.getCouponResource() == RewardCouponResource.DUIBA && StringUtils.isNotBlank(product.getUsedUrl())) {
            url = RewardProductDetailGenerator.COUPON_REDRICT +  detail.getId();
        } else {
            url = product.getUsedUrl();
        }
        detail.setUsedUrl(url);
        detail.setRebated(product.getRebated());
        detail.setTeacherLevel(product.getTeacherLevel());
        detail.setAmbassadorLevel(product.getAmbassadorLevel());
        detail.setStudentOrderValue(product.getStudentOrderValue());
        detail.setTeacherOrderValue(product.getTeacherOrderValue());
        detail.setExpiryDate(product.getExpiryDate());
        detail.setDisplayTerminal(product.getDisplayTerminal());
        detail.setRelateVirtualItemId(product.getRelateVirtualItemId());
        detail.setRelateVirtualItemContent(product.getRelateVirtualItemContent());
        detail.setOriginPrice(product.getPriceS());
        detail.setCouponResource(product.getCouponResource());
        detail.setSpendType(product.getSpendType());
        detail.setPrice(product.getPriceOldS());
        Double vipPrice = new BigDecimal(product.getPriceOldS() * RewardConstants.DISCOUNT_VIP).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
        detail.setVipPrice(vipPrice);
        detail.setDiscountPrice(product.getPriceOldS());
        detail.setUnit(RewardProductPriceUnit.学豆.name());
        detail.setSpendType(product.getSpendType());
        detail.setOneLevelCategoryId(product.getOneLevelCategoryId());
        detail.setTwoLevelCategoryId(product.getTwoLevelCategoryId());
        detail.setIsNewProduct(product.getIsNewProduct());
        detail.setOneLevelCategoryType(categoryIdTypeMap.get(product.getOneLevelCategoryId()));

        List<RewardImage> images = rewardImages.get(product.getId());
        if (CollectionUtils.isEmpty(images)) {
            RewardImage image = new RewardImage();
            image.setLocation(RewardConstants.DEFAULT_PRODUCT_IMAGE_URL);
            images = Collections.singletonList(image);
        }

        // 这里要根据图片的属性进行选择
        if (selectedImage == null) {
            selectedImage = images.stream().findFirst().orElse(null);
        }

        detail.setImage(selectedImage.getLocation());
        detail.setImages(images);

        detail.setSkus(new ArrayList<>());
        // 记录上线状态
        detail.setOnline(product.getOnlined());
        detail.setMinBuyNums(product.getMinBuyNums());
        if (this.isTobyWear(product.getOneLevelCategoryId())) {
            this.proTobyExtra(detail);
        } else if (this.isMiniCourse(product.getOneLevelCategoryId())) {
            this.proMiniCourseExtra(userAllOrderMap, detail);
        } else if (this.isTeachingResources(product.getOneLevelCategoryId())) {
            this.proTeachingResourceExtra(userAllOrderMap, userAllCouponMap, detail);
        }
        return detail;
    }

    private RewardProductDetail generateTeacherRewardProductDetail(RewardProduct product,
                                                                   String showTagName,
                                                                   TeacherDetail teacher,
                                                                   Map<Long, List<RewardImage>> rewardImages,
                                                                   Map<Long, Integer>categoryIdTypeMap,
                                                                   Map<Long, RewardOrder> userAllOrderMap,
                                                                   Map<Long, RewardCouponDetail> userAllCouponMap) {
        RewardProductDetail detail = new RewardProductDetail();
        detail.setRepeatExchanged(product.getRepeatExchanged());
        detail.setExtenstionAttributes(new LinkedHashMap<>());
        detail.getExtenstionAttributes().put("product", product);
        detail.setId(product.getId());
        detail.setProductName(product.getProductName());
        detail.setProductType(product.getProductType());
        detail.setDescription(product.getDescription());
        detail.setSoldQuantity(product.getSoldQuantity());
        detail.setWishQuantity(product.getWishQuantity());
        detail.setSaleGroup(product.getSaleGroup());
        detail.setTags(showTagName);
        detail.setCreateDatetime(product.getCreateDatetime());
        detail.setUsedUrl(product.getCouponResource() == RewardCouponResource.DUIBA && StringUtils.isNotBlank(product.getUsedUrl()) ? (RewardProductDetailGenerator.COUPON_REDRICT +  detail.getId()): product.getUsedUrl());
        detail.setRebated(product.getRebated());
        detail.setTeacherLevel(product.getTeacherLevel());
        detail.setAmbassadorLevel(product.getAmbassadorLevel());
        detail.setStudentOrderValue(product.getStudentOrderValue());
        detail.setTeacherOrderValue(product.getTeacherOrderValue());
        detail.setDisplayTerminal(product.getDisplayTerminal());
        detail.setRelateVirtualItemId(product.getRelateVirtualItemId());
        detail.setRelateVirtualItemContent(product.getRelateVirtualItemContent());
        detail.setMinBuyNums(product.getMinBuyNums());
        detail.setCouponResource(product.getCouponResource());
        detail.setSpendType(product.getSpendType());
        detail.setOneLevelCategoryId(product.getOneLevelCategoryId());
        detail.setTwoLevelCategoryId(product.getTwoLevelCategoryId());
        detail.setIsNewProduct(product.getIsNewProduct());
        detail.setOneLevelCategoryType(categoryIdTypeMap.get(product.getOneLevelCategoryId()));

        if (product.getAmbassadorLevel() > 0) {
            detail.setAmbassadorLevelName(AmbassadorLevel.of(product.getAmbassadorLevel()).getDescription());
        }
        // 小学老师需要区分， 现在对于奖品来说，后台配置的价格全部按照积分配置的。 小学老师要除以10
        double price = product.getPriceOldS();
        double originPrice = product.getPriceS();
        RewardProductPriceUnit unit = RewardProductPriceUnit.中学积分;
        if (teacher.isPrimarySchool()) {
            // 小学老师
            price = new BigDecimal(price).divide(new BigDecimal(10), 0, BigDecimal.ROUND_HALF_UP).doubleValue();
            // 原始积分也要换算单位
            originPrice = new BigDecimal(originPrice).divide(new BigDecimal(10),0,BigDecimal.ROUND_HALF_UP).doubleValue();
            unit = RewardProductPriceUnit.园丁豆;
        }

        detail.setPrice(price);
        detail.setVipPrice(price);
        detail.setDiscount(1.0);
        detail.setDiscountPrice(price);
        detail.setOriginPrice(originPrice);
        detail.setUnit(unit.name());
        if (MapUtils.isNotEmpty(rewardImages)) {
            List<RewardImage> images = rewardImages.get(product.getId());
            if (CollectionUtils.isEmpty(images)) {
                RewardImage image = new RewardImage();
                image.setLocation(RewardConstants.DEFAULT_PRODUCT_IMAGE_URL);
                images = Collections.singletonList(image);
            }
            detail.setImage(images.iterator().next().getLocation());
            detail.setImages(images);
        }

        detail.setSkus(new ArrayList<>());
        detail.setOnline(product.getOnlined());
        if (this.isTobyWear(product.getOneLevelCategoryId())) {
            this.proTobyExtra(detail);
        } else if (this.isMiniCourse(product.getOneLevelCategoryId())) {
            this.proMiniCourseExtra(userAllOrderMap, detail);
        } else if (this.isTeachingResources(product.getOneLevelCategoryId())) {
            this.proTeachingResourceExtra(userAllOrderMap, userAllCouponMap, detail);
        }
        return detail;
    }

    public Boolean showAffordableFilter(Boolean affordable, Long integral, Double discount, RewardProduct product, User user) {
        if (!affordable) {
            return true;
        }

        Double price = product.getPriceOldS();
        if (user.isTeacher()) {
            TeacherDetail teacher = (TeacherDetail) user;

            // 小学老师
            if (teacher.isPrimarySchool()) {
                price = new BigDecimal(price).multiply(new BigDecimal(discount)).divide(new BigDecimal(10), 0, BigDecimal.ROUND_HALF_UP).doubleValue();
                if (!checkCanExchangePrivilegeProduct(product.getId(), teacher)) {
                    return false;
                }
            }
        }

        if (integral < price) {
            return false;
        }
        return true;
    }

    /**
     * 检查商品是否是特权商品，老是特权级别是否能够兑换该商品
     * @param productId
     * @param teacher
     * @return
     */
    public Boolean checkCanExchangePrivilegeProduct(Long productId, TeacherDetail teacher) {
        if (teacher.isPrimarySchool()) {//老师等级过滤

            Set<Long> tagIds = newRewardBufferLoaderClient.loadTagIdByProductId(productId);
            if (CollectionUtils.isEmpty(tagIds)) {
                return true;
            }

            if (!tagIds.contains(TagIdLogicRelation.TEACHER_PRIVILEGE_SENIOR.getNumber()) && !tagIds.contains(TagIdLogicRelation.TEACHER_PRIVILEGE_SUPER.getNumber())) {
                return true;
            } else {
                TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacher.getId());
                if (extAttribute == null || extAttribute.getNewLevel() == null) {
                    return false;
                }
                if (tagIds.contains(TagIdLogicRelation.TEACHER_PRIVILEGE_SUPER.getNumber())) {
                    return extAttribute.getNewLevel() >= TeacherExtAttribute.NewLevel.SUPER.getLevel();
                } else {
                    return extAttribute.getNewLevel() >= TeacherExtAttribute.NewLevel.SENIOR.getLevel();
                }
            }
        }
        return true;
    }

    public Boolean isShiWuOffLineGrayCityWithSchoolLevel(User user) {
        if (user.isStudent()) {
            return grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable((StudentDetail) user, "Reward","OfflineShiWu", true);
        } else if (user.isTeacher()) {
            return grayFunctionManagerClient.getTeacherGrayFunctionManager()
                    .isWebGrayFunctionAvailable((TeacherDetail) user, "Reward","OfflineShiWu", true);
        }
        return false;
    }

    public Boolean showFilter(User user, String displayTerminal, Set<Long> hasInventoryProductIds, Map<Long, List<RewardProductTarget>> productTargetsMap, RewardProduct product) {
        if (isSHIWU(product.getOneLevelCategoryId()) && !hasInventoryProductIds.contains(product.getId())) {
            return false;
        }

        //非上架商品
        if (!Boolean.TRUE.equals(product.getOnlined())) {
            return false;
        }
        //非端展示商品
        if (Objects.nonNull(product.getDisplayTerminal()) && !product.getDisplayTerminal().contains(displayTerminal)) {
            return false;
        }

        if (user.isStudent()) {
            return showStudentFilter((StudentDetail)user, productTargetsMap, product);
        } else if (user.isTeacher()) {
            return showTeacherFilter((TeacherDetail) user, productTargetsMap, product);
        }

        return false;
    }

    private Boolean showStudentFilter(StudentDetail studentDetail, Map<Long, List<RewardProductTarget>> productTargetsMap, RewardProduct product) {

        //学生可见性
        if (!product.getStudentVisible()) {
            return false;
        }

        if (studentDetail.isPrimaryStudent() && !product.getPrimarySchoolVisible()) {
            return false;
        }

        if (studentDetail.isJuniorStudent() && !product.getJuniorSchoolVisible()) {
            return false;
        }

        //毕业班不可见实物商品
        Clazz clazz = studentDetail.getClazz();
        if (this.isSHIWU(product.getOneLevelCategoryId()) && Objects.nonNull(clazz) && clazz.isTerminalClazz()) {
            return false;
        }

        //年级可见性
        Integer grade = studentDetail.getClazzLevelAsInteger();
        if (StringUtils.isNotEmpty(product.getGradeVisible())) {
            boolean match = Arrays.stream(product.getGradeVisible().split(","))
                    .filter(StringUtils::isNumeric)
                    .map(Integer::parseInt)
                    .anyMatch(g -> Objects.equals(grade,g));

            if(!match) {
                return false;
            }
        }

        if (isSHIWU(product.getOneLevelCategoryId())) {
            //灰度地区不可见
            if (isShiWuOffLineGrayCityWithSchoolLevel(studentDetail)) {
                return false;
            }
        }

        List<Integer> regionCodeList = new ArrayList<>();
        regionCodeList.add(studentDetail.getCityCode());
        regionCodeList.add(studentDetail.getRootRegionCode());
        regionCodeList.add(studentDetail.getStudentSchoolRegionCode());
        return showTargetsFilter(product.getId(), regionCodeList, productTargetsMap);
    }

    private Boolean showTeacherFilter(TeacherDetail teacherDetail, Map<Long, List<RewardProductTarget>> productTargetsMap, RewardProduct product) {
        if (Objects.isNull(product)) {
            return false;
        }

        //小学不可见
        if (teacherDetail.isPrimarySchool() && !product.getPrimarySchoolVisible()) {
            return false;
        }

        //中学不可见
        if ((teacherDetail.isJuniorTeacher() || teacherDetail.isSeniorTeacher()) && !product.getJuniorSchoolVisible()) {
            return false;
        }

        if (!product.getTeacherVisible()) {
            return false;
        }

        List<Integer> regionCodeList = new ArrayList<>();
        regionCodeList.add(teacherDetail.getCityCode());
        regionCodeList.add(teacherDetail.getRootRegionCode());
        regionCodeList.add(teacherDetail.getRegionCode());
        return showTargetsFilter(product.getId(), regionCodeList, productTargetsMap);
    }

    private Boolean showTargetsFilter(Long productId, List<Integer> regionCodeList, Map<Long, List<RewardProductTarget>> productTargetsMap) {
        List<RewardProductTarget> productTargets = productTargetsMap.get(productId);
        // 投放区域
        if (CollectionUtils.isNotEmpty(productTargets)) {
            // 查看是不是全部投放
            if (productTargets.stream()
                    .filter(pt -> pt.getTargetType() == RewardProductTargetType.TARGET_TYPE_ALL.getType())
                    .anyMatch(pt -> SafeConverter.toBoolean(pt.getTargetStr()))) {
                return true;
            }

            if (!productTargets.stream()
                    .map(pt -> SafeConverter.toInt(pt.getTargetStr()))
                    .anyMatch(regionCodeList::contains)) {
                return false;
            }
        }
        return true;
    }

    public List<ProductCategory> loadProductCategoryByParentId(Long parentId) {
        List<ProductCategory> productCategoryList = remoteReference.loadProductCategoryByParentId(parentId);
        if (CollectionUtils.isEmpty(productCategoryList)) {
            return Collections.emptyList();
        }
        return productCategoryList;
    }

    public List<GetProductDetailMapper.CategoryMapper> loadProductCategoryMapperByParentId(Long parentId) {
        List<ProductCategory> productCategoryList = remoteReference.loadProductCategoryByParentId(parentId);
        if (CollectionUtils.isEmpty(productCategoryList)) {
            return Collections.emptyList();
        }
        List<GetProductDetailMapper.CategoryMapper> result = productCategoryList.stream().map(category -> {
            GetProductDetailMapper.CategoryMapper mapper = new GetProductDetailMapper().new CategoryMapper();
            mapper.setId(category.getId());
            mapper.setName(category.getName());
            mapper.setOneLevelCategoryType(category.getOneLevelCategoryType());
            return mapper;
        }).collect(Collectors.toList());
        return result;
    }

    public List<Long> loadProductIdListByCategoryParentId(Long parentId) {
        List<ProductCategory> productCategoryList = remoteReference.loadProductCategoryByParentId(parentId);
        if (CollectionUtils.isEmpty(productCategoryList)) {
            return Collections.emptyList();
        }
        List<Long> result = new ArrayList<>();
        for (ProductCategory category : productCategoryList) {
            List<Long> subList = loadProductIdListByCategoryId(category.getId());
            if (CollectionUtils.isNotEmpty(subList)) {
                result.addAll(subList);
            }
        }
        return result;
    }

    public List<ProductCategoryRef> loadAllProductCategoryRef(){
        return remoteReference.loadAllProductCategoryRef();
    }

    public ProductCategory loadProductCategoryById(Long id) {
        return remoteReference.loadProductCategoryById(id);
    }

    public List<ProductTag> loadProductTagByParent(Long parentId, Integer parentType) {
        return newRewardBufferLoaderClient.getProductTagBuffer().loadProductTagByParent(parentId, parentType);
    }

    public ProductTag loadProductTagById(Long id) {
        return remoteReference.loadProductTagById(id);
    }

    public List<Long> loadProductIdListByCategoryId(Long categoryId) {
        return remoteReference.loadProductIdListByCategoryId(categoryId);
    }

    public List<Long> loadProductIdListBySetId(Long setId) {
        return remoteReference.loadProductIdListBySetId(setId);
    }

    public ProductCategoryRef loadProductCategoryRefByProductId(Long productId) {
        return remoteReference.loadProductCategoryRefByProductId(productId);
    }

    public Map<Long, List<ProductTagRef>> loadProductTagRefByTagIdList(Collection<Long> tagIdList) {
        return remoteReference.loadProductTagRefByTagIdList(tagIdList);
    }

    public List<GetProductDetailMapper.TagMapper> loadProductTagListByProductId(Long productId) {
        List<ProductTagRef> productTagRefList = remoteReference.loadProductTagRefByProductId(productId);
        if (CollectionUtils.isEmpty(productTagRefList)) {
            return Collections.emptyList();
        }
        Map<Long, ProductTag> map =remoteReference.loadProductTagByIds(productTagRefList.stream().map(ProductTagRef::getTagId).collect(Collectors.toList()));
        return map.values()
                .stream()
                .map(tag -> {
                    GetProductDetailMapper.TagMapper mapper = new GetProductDetailMapper().new TagMapper();
                    mapper.setId(tag.getId());
                    mapper.setName(tag.getName());
                    return mapper;})
                .collect(Collectors.toList());
    }

    public List<Long> loadTagIdListByProductId(Long productId) {
        List<ProductTagRef> productTagRefList = remoteReference.loadProductTagRefByProductId(productId);
        if (CollectionUtils.isEmpty(productTagRefList)) {
            return Collections.emptyList();
        }
        return productTagRefList.stream().filter(ref -> !ref.getDisabled()).map(ProductTagRef::getTagId).collect(Collectors.toList());
    }

    public List<ProductSet> loadAllProductSet() {
        return remoteReference.loadAllProductSet();
    }

    public List<ProductCategory> loadProductCategoryByLevel(Integer level) {
        return remoteReference.loadProductCategoryByLevel(level);
    }

    public List<ProductCategory> loadAllProductCategory() {
        return remoteReference.loadAllProductCategory();
    }

    public ProductSet loadProductSetById(Long id) {
        return remoteReference.loadProductSetById(id);
    }

    public Map<Long, ProductSet> loadProductSetByProductId(Long productId) {
        return remoteReference.loadProductSetByProductId(productId);
    }

    public List<ProductSetRef> loadAllProductSetRef() {
        return remoteReference.loadAllProductSetRef();
    }

    public List<RewardSku> loadAllRewardSku() {
        return remoteReference.loadAllRewardSku();
    }

    public Map<Long, Long> loadCouponStock() {
        return remoteReference.loadCouponStock();
    }

    public Boolean isSHIWU(Long oneLevelCategoryId) {
        return newRewardBufferLoaderClient.getProductCategoryBuffer().isSHIWU(oneLevelCategoryId);
    }

    public Boolean isFlowPacket(Long oneLevelCategoryId) {
        return newRewardBufferLoaderClient.getProductCategoryBuffer().isFlowPacket(oneLevelCategoryId);
    }

    public Boolean isTobyWear(Long oneLevelCategoryId) {
        return newRewardBufferLoaderClient.getProductCategoryBuffer().isTobyWear(oneLevelCategoryId);
    }

    public Boolean isHeadWear(Long oneLevelCategoryId) {
        return newRewardBufferLoaderClient.getProductCategoryBuffer().isHeadWear(oneLevelCategoryId);
    }

    public Boolean isMiniCourse(Long oneLevelCategoryId) {
        return newRewardBufferLoaderClient.getProductCategoryBuffer().isMiniCourse(oneLevelCategoryId);
    }

    public Boolean isCoupon(Long oneLevelCategoryId) {
        return newRewardBufferLoaderClient.getProductCategoryBuffer().isCoupon(oneLevelCategoryId);
    }

    public Boolean isTeachingResources(Long oneLevelCategoryId) {
        return newRewardBufferLoaderClient.getProductCategoryBuffer().isTeachingResources(oneLevelCategoryId);
    }

    public Integer getOneLevelCategoryType(Long oneLevelCategoryId) {
        return newRewardBufferLoaderClient.getProductCategoryBuffer().getOneLevelCategoryType(oneLevelCategoryId);
    }

    /**
     * 兼容老的商品分类方式
     * @param order
     * @return
     */
    public Integer fetchOnelevelCategoryTypeByOrder(RewardOrder order) {
        OneLevelCategoryType type = (OneLevelCategoryType.of(NumberUtils.toInt(order.getProductType())));
        if (Objects.equals(type, OneLevelCategoryType.JPZX_UNKNOWN)) {
            if (Objects.equals(order.getProductType(), RewardProductType.JPZX_SHIWU.name())) {
                return OneLevelCategoryType.JPZX_SHIWU.intType();
            } else if (Objects.equals(order.getProductType(), RewardProductType.JPZX_TIYAN.name())) {
                if (Objects.equals(order.getProductCategory(), RewardCategory.SubCategory.COUPON.name())) {
                    return OneLevelCategoryType.JPZX_COUPON.intType();
                } else if (Objects.equals(order.getProductCategory(), RewardCategory.SubCategory.HEAD_WEAR.name())) {
                    return OneLevelCategoryType.JPZX_HEADWEAR.intType();
                } else if (Objects.equals(order.getProductCategory(), RewardCategory.SubCategory.MINI_COURSE.name())) {
                    return OneLevelCategoryType.JPZX_MINI_COURSE.intType();
                } else if (Objects.equals(order.getProductCategory(), RewardCategory.SubCategory.FLOW_PACKET.name())) {
                    return OneLevelCategoryType.JPZX_FLOW_PACKET.intType();
                } else if (Objects.equals(order.getProductCategory(), RewardCategory.SubCategory.TOBY_WEAR.name())) {
                    return OneLevelCategoryType.JPZX_TOBY.intType();
                } else if (Objects.equals(order.getProductCategory(), RewardCategory.SubCategory.COURSE_WARE.name())) {
                    return OneLevelCategoryType.JPZX_TEACHING_RESOURCES.intType();
                }
            }
        } else {
            return type.intType();
        }
        return OneLevelCategoryType.JPZX_UNKNOWN.intType();
    }

    /**
     * 兼容老的商品分类方式
     * @param product
     * @return
     */
    public Integer fetchOnelevelCategoryTypeIncludeOldCategory(RewardProductDetail product) {
        Integer oneLevelCategoryType =  getOneLevelCategoryType(product.getOneLevelCategoryId());
        if (Objects.nonNull(oneLevelCategoryType) && !Objects.equals(oneLevelCategoryType, 0)) {
            return oneLevelCategoryType;
        }
        List<String> categoryCodeList = rewardLoaderClient.findCategoryCode(product.getId());
        String productType = product.getProductType();
        String categoryCode = StringUtils.EMPTY;
        if (CollectionUtils.isNotEmpty(categoryCodeList)) {
            categoryCode = categoryCodeList.get(0).trim();
        }

        OneLevelCategoryType type = (OneLevelCategoryType.of(NumberUtils.toInt(productType)));
        if (Objects.equals(type, OneLevelCategoryType.JPZX_UNKNOWN)) {
            if (Objects.equals(productType, RewardProductType.JPZX_SHIWU.name())) {
                return OneLevelCategoryType.JPZX_SHIWU.intType();
            } else if (Objects.equals(productType, RewardProductType.JPZX_TIYAN.name())) {
                if (Objects.equals(categoryCode, RewardCategory.SubCategory.COUPON.name())) {
                    return OneLevelCategoryType.JPZX_COUPON.intType();
                } else if (Objects.equals(categoryCode, RewardCategory.SubCategory.HEAD_WEAR.name())) {
                    return OneLevelCategoryType.JPZX_HEADWEAR.intType();
                } else if (Objects.equals(categoryCode, RewardCategory.SubCategory.MINI_COURSE.name())) {
                    return OneLevelCategoryType.JPZX_MINI_COURSE.intType();
                } else if (Objects.equals(categoryCode, RewardCategory.SubCategory.FLOW_PACKET.name())) {
                    return OneLevelCategoryType.JPZX_FLOW_PACKET.intType();
                } else if (Objects.equals(categoryCode, RewardCategory.SubCategory.TOBY_WEAR.name())) {
                    return OneLevelCategoryType.JPZX_TOBY.intType();
                } else if (Objects.equals(categoryCode, RewardCategory.SubCategory.COURSE_WARE.name())) {
                    return OneLevelCategoryType.JPZX_TEACHING_RESOURCES.intType();
                }
            }
        } else {
            return type.intType();
        }
        return OneLevelCategoryType.JPZX_UNKNOWN.intType();
    }

    /**
     * 获得用户在奖品中心兑换时需要的电话信息列表
     *
     * @param user
     * @return
     */
    public List<Map<String, Object>> loadMobileInfo(User user) {
        if (user == null)
            return Collections.emptyList();

        List<Map<String, Object>> mobileList = new ArrayList<>();
        //UserAuthentication userAct = userLoaderClient.loadUserAuthentication();
        if (UserType.STUDENT == user.fetchUserType()) {
            StudentParent studentParent = parentLoaderClient.loadStudentKeyParent(user.getId());
            if (studentParent != null) {
                //UserAuthentication parentAct = userLoaderClient.loadUserAuthentication(studentParent.getParentUser().getId());
                String am = sensitiveUserDataServiceClient.showUserMobile(studentParent.getParentUser().getId(), "/reward/product/experience/detail", SafeConverter.toString(studentParent.getParentUser().getId()));
                if (am != null) {
                    Map<String, Object> parentMap = new HashMap<>();
                    parentMap.put("callName", studentParent.getCallName());
                    parentMap.put("mobile", am);
                    mobileList.add(parentMap);
                }
            }
        }

        String am = sensitiveUserDataServiceClient.showUserMobile(user.getId(), "/reward/product/experience/detail", SafeConverter.toString(user.getId()));
        if (am != null) {
            Map<String, Object> studentMap = new HashMap<>();
            studentMap.put("callName", "我");
            studentMap.put("mobile", am);
            mobileList.add(studentMap);
        }

        if (UserType.RESEARCH_STAFF == user.fetchUserType()) {
            UserShippingAddress address = userLoaderClient.loadUserShippingAddress(user.getId());
            if (address != null && StringUtils.isNotBlank(address.getSensitivePhone())) {
                Map<String, Object> rstaffMap = new HashMap<>();
                rstaffMap.put("callName", "我");
                rstaffMap.put("mobile", address.getSensitivePhone());
                mobileList.add(rstaffMap);
            }
        }

        return mobileList;
    }
}
