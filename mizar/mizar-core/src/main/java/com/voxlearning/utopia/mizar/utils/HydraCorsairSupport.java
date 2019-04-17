package com.voxlearning.utopia.mizar.utils;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.remote.hydra.client.generic.json.HydraJsonClient;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.mizar.entity.bookStore.*;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
@Named
@Slf4j
public class HydraCorsairSupport {

   private static final String GROUP = "alps-hydra-" + (RuntimeMode.current().le(Mode.TEST) ? Mode.TEST.getStageMode() : RuntimeMode.getCurrentStage());
    // private static final String GROUP = "alps-hydra-yong.he";
    private static final String VERSION = "1.0";
    private static final String BOOK_STORE_LOADER_INTERFACE = "com.voxlearning.xue.service.marketing.api.XueMizarBookStoreLoader";
    private static final String BOOK_STORE_SERVICE_INTERFACE = "com.voxlearning.xue.service.marketing.api.XueMizarBookStoreService";
    private static final String WHITE_LIST_LOADER_INTERFACE = "com.voxlearning.xue.service.marketing.api.XueMizarWhiteListLoader";
    private static final String WHITE_LIST_SERVICE_INTERFACE = "com.voxlearning.xue.service.marketing.api.XueMizarWhiteListService";

    public XueMizarBookStoreBean loadPageBookStore(String bookStoreName, Long bookStoreId, List<String> mizarUserIds, Integer page, Integer pageSize,Integer userRole, String contactName, String mizarUserId) {
        XueMizarBookStoreBean bookStoreInfo = null;
        try {
            bookStoreInfo = HydraJsonClient.builder()
                    .serviceInterface(BOOK_STORE_LOADER_INTERFACE)
                    .serviceGroup(GROUP)
                    .serviceVersion(VERSION)
                    .methodName("loadPageBookStore")
                    .parameterTypes(String.class.getName(), Long.class.getName(), List.class.getName(), Integer.class.getName(),Integer.class.getName(), Integer.class.getName(), String.class.getName(), String.class.getName())
                    .arguments(bookStoreName, bookStoreId, mizarUserIds, page,pageSize, userRole, contactName, mizarUserId)
                    .build()
                    .invoke()
                    .mapTo(XueMizarBookStoreBean.class);
        } catch (Exception e) {
            log.error("获取门店列表信息失败", e);
        }
        return bookStoreInfo;
    }

    public Boolean addOrEditBookStore(BookStoreBean searchBean) {
        try {
            Boolean addOrEditResult = HydraJsonClient.builder()
                    .serviceInterface(BOOK_STORE_SERVICE_INTERFACE)
                    .serviceGroup(GROUP)
                    .serviceVersion(VERSION)
                    .methodName("addOrEditBookStore")
                    .parameterTypes(String.class.getName())
                    .arguments(JsonUtils.toJson(searchBean))
                    .build()
                    .invoke()
                    .mapTo(Boolean.class);
            return addOrEditResult;
        } catch (Exception e) {
            log.error("增加或修改门店列表信息失败", e);
        }
        return false;
    }

    public BookStoreBean loadBookStoreById(Long id) {
        BookStoreBean bookStoreBean = new BookStoreBean();
        try {
            bookStoreBean = HydraJsonClient.builder()
                    .serviceInterface(BOOK_STORE_LOADER_INTERFACE)
                    .serviceGroup(GROUP)
                    .serviceVersion(VERSION)
                    .methodName("loadMizarBookStoreInfoById")
                    .parameterTypes(Long.class.getName())
                    .arguments(id)
                    .build()
                    .invoke()
                    .mapTo(BookStoreBean.class);
        } catch (Exception e) {
            log.error("获取门店详细信息失败", e);
        }
        return bookStoreBean;
    }

    public String checkRepeatBookStoreIds(List<Long> ids,Long id) {
        String checkResult = "";
        try {
            checkResult = HydraJsonClient.builder()
                    .serviceInterface(WHITE_LIST_LOADER_INTERFACE)
                    .serviceGroup(GROUP)
                    .serviceVersion(VERSION)
                    .methodName("checkRepeatBookStoreIds")
                    .parameterTypes(List.class.getName(),Long.class.getName())
                    .arguments(ids,id)
                    .build()
                    .invoke()
                    .mapTo(String.class);
        } catch (Exception e) {
            log.error("checkRepeatBookStoreIds", e);
        }
        return checkResult;
    }

    public String checkInvalidBookStoreIds(List<Long> ids) {
        String checkResult = "";
        try {
            checkResult = HydraJsonClient.builder()
                    .serviceInterface(BOOK_STORE_LOADER_INTERFACE)
                    .serviceGroup(GROUP)
                    .serviceVersion(VERSION)
                    .methodName("checkInvalidBookStoreIds")
                    .parameterTypes(List.class.getName())
                    .arguments(ids)
                    .build()
                    .invoke()
                    .mapTo(String.class);
        } catch (Exception e) {
            log.error("checkInvalidBookStoreIds", e);
        }
        return checkResult;
    }

    public List<BookStoreBean> findBookStoreListByMizarUserId(String mizarUserId) {
        List<BookStoreBean> bookStoreBeanList = new ArrayList<>();
        try {
            bookStoreBeanList = HydraJsonClient.builder()
                    .serviceInterface(BOOK_STORE_LOADER_INTERFACE)
                    .serviceGroup(GROUP)
                    .serviceVersion(VERSION)
                    .methodName("findBookStoreListByMizarUserId")
                    .parameterTypes(String.class.getName())
                    .arguments(mizarUserId)
                    .build()
                    .invoke()
                    .mapTo(TypeFactory.defaultInstance().constructParametricType(List.class,BookStoreBean.class));
        } catch (Exception e) {
            log.error("获取门店信息失败", e);
        }
        return bookStoreBeanList;
    }

    public List<BookStoreBean> findBookStoreExtendBeanListByMizarUserId(String mizarUserId) {
        List<BookStoreBean> bookStoreBeanList = new ArrayList<>();
        try {
            bookStoreBeanList = HydraJsonClient.builder()
                    .serviceInterface(BOOK_STORE_LOADER_INTERFACE)
                    .serviceGroup(GROUP)
                    .serviceVersion(VERSION)
                    .methodName("findBookStoreExtendBeanListByMizarUserId")
                    .parameterTypes(String.class.getName())
                    .arguments(mizarUserId)
                    .build()
                    .invoke()
                    .mapTo(TypeFactory.defaultInstance().constructParametricType(List.class,BookStoreBean.class));
        } catch (Exception e) {
            log.error("获取门店信息失败", e);
        }
        return bookStoreBeanList;
    }

    public Integer findCheckOrderNum() {
       Integer checkOrderNum = 0;
        try {
            checkOrderNum = HydraJsonClient.builder()
                    .serviceInterface(BOOK_STORE_LOADER_INTERFACE)
                    .serviceGroup(GROUP)
                    .serviceVersion(VERSION)
                    .methodName("findCheckOrderNum")
                    .parameterTypes()
                    .arguments()
                    .build()
                    .invoke()
                    .mapTo(Integer.class);
        } catch (Exception e) {
            log.error("获取门店信息失败", e);
        }
        return checkOrderNum;
    }

    public Integer findCheckOrderNumByBookStoreIds(List<Long> bookStoreIds) {
        Integer checkOrderNum = 0;
        try {
            checkOrderNum = HydraJsonClient.builder()
                    .serviceInterface(BOOK_STORE_LOADER_INTERFACE)
                    .serviceGroup(GROUP)
                    .serviceVersion(VERSION)
                    .methodName("findCheckOrderNumByBookStoreIds")
                    .parameterTypes(List.class.getName())
                    .arguments(bookStoreIds)
                    .build()
                    .invoke()
                    .mapTo(Integer.class);
        } catch (Exception e) {
            log.error("获取门店信息失败", e);
        }
        return checkOrderNum;
    }

    public XueMizarBookStoreOrderRankBean loadOperationInfoByMizarUserIds(List<String> mizarUserIds, Integer userRole) {
        XueMizarBookStoreOrderRankBean bean = new XueMizarBookStoreOrderRankBean();
        try {
            bean = HydraJsonClient.builder()
                    .serviceInterface(BOOK_STORE_LOADER_INTERFACE)
                    .serviceGroup(GROUP)
                    .serviceVersion(VERSION)
                    .methodName("loadOperationInfoByMizarUserIds")
                    .parameterTypes(List.class.getName(), Integer.class.getName())
                    .arguments(mizarUserIds, userRole)
                    .build()
                    .invoke()
                    .mapTo(XueMizarBookStoreOrderRankBean.class);
        } catch (Exception e) {
            log.error("获取门店列表信息失败", e);
        }
        return bean;
    }

    public Boolean checkStoreName(String bookStoreName,Long id,String mizarUserId) {
        try {
            Boolean checkStoreName = HydraJsonClient.builder()
                    .serviceInterface(BOOK_STORE_LOADER_INTERFACE)
                    .serviceGroup(GROUP)
                    .serviceVersion(VERSION)
                    .methodName("checkBookStoreName")
                    .parameterTypes(String.class.getName(),Long.class.getName(),String.class.getName())
                    .arguments(bookStoreName,id,mizarUserId)
                    .build()
                    .invoke()
                    .mapTo(Boolean.class);
            return checkStoreName;
        } catch (Exception e) {
            log.error("获取门店详细信息失败", e);
        }
        return false;
    }

    public Boolean changeBookStoresMizarId(String fromMizarId, String toMizarId) {
        try {
            Boolean result = HydraJsonClient.builder()
                    .serviceInterface(BOOK_STORE_SERVICE_INTERFACE)
                    .serviceGroup(GROUP)
                    .serviceVersion(VERSION)
                    .methodName("changeBookStoresMizarId")
                    .parameterTypes(String.class.getName(),String.class.getName())
                    .arguments(fromMizarId,toMizarId)
                    .build()
                    .invoke()
                    .mapTo(Boolean.class);
            return result;
        } catch (Exception e) {
            log.error("修改用户手机号失败", e);
        }
        return false;
    }

    public XueMizarBookStoreOrderBean loadOrderInfoByBookStoreId(Long bookStoreId, Integer page, Integer pageSize) {
        XueMizarBookStoreOrderBean orderBean = null;
        try {
            orderBean = HydraJsonClient.builder()
                    .serviceInterface(BOOK_STORE_LOADER_INTERFACE)
                    .serviceGroup(GROUP)
                    .serviceVersion(VERSION)
                    .methodName("loadPageOrderInfo")
                    .parameterTypes(Long.class.getName(), Integer.class.getName(), Integer.class.getName())
                    .arguments(bookStoreId, page, pageSize)
                    .build()
                    .invoke()
                    .mapTo(XueMizarBookStoreOrderBean.class);
        } catch (Exception e) {
            log.error("获取门店订单信息失败", e);
        }
        return orderBean;
    }

    public Boolean checkViewBookStoreByRole(Long bookStoreId,Integer userRole,String currentUserId,List<String> allBDUserIds) {
        try {
            Boolean result = HydraJsonClient.builder()
                    .serviceInterface(BOOK_STORE_LOADER_INTERFACE)
                    .serviceGroup(GROUP)
                    .serviceVersion(VERSION)
                    .methodName("checkViewBookStoreByRole")
                    .parameterTypes(Long.class.getName(),Integer.class.getName(),String.class.getName(),List.class.getName())
                    .arguments(bookStoreId,userRole,currentUserId,allBDUserIds)
                    .build()
                    .invoke()
                    .mapTo(Boolean.class);
            return result;
        } catch (Exception e) {
            log.error("校验查看订单详情失败", e);
        }
        return false;
    }

    public XueMizarWhiteListBean loadPageMizarWhiteList(int type, int page, int pageSize) {
        XueMizarWhiteListBean whiteListBean = null;
        try {
            whiteListBean = HydraJsonClient.builder()
                    .serviceInterface(WHITE_LIST_LOADER_INTERFACE)
                    .serviceGroup(GROUP)
                    .serviceVersion(VERSION)
                    .methodName("loadPageWhiteList")
                    .parameterTypes(Integer.class.getName(), Integer.class.getName(), Integer.class.getName())
                    .arguments(type, page, pageSize)
                    .build()
                    .invoke()
                    .mapTo(XueMizarWhiteListBean.class);
        } catch (Exception e) {
            log.error("获取白名单列表信息失败", e);
        }
        return whiteListBean;
    }

    public Boolean addOrEditWhiteList(XueMizarWhiteListExtendBean whiteListExtendBean) {
        try {
            Boolean addOrEditResult = HydraJsonClient.builder()
                    .serviceInterface(WHITE_LIST_SERVICE_INTERFACE)
                    .serviceGroup(GROUP)
                    .serviceVersion(VERSION)
                    .methodName("addOrEditWhiteList")
                    .parameterTypes(String.class.getName())
                    .arguments(JsonUtils.toJson(whiteListExtendBean))
                    .build()
                    .invoke()
                    .mapTo(Boolean.class);
            return addOrEditResult;
        } catch (Exception e) {
            log.error("增加或修改白名单信息失败", e);
        }
        return false;
    }

    public XueMizarWhiteListExtendBean loadWhiteListById(Long id) {
        XueMizarWhiteListExtendBean whiteListExtendBean = new XueMizarWhiteListExtendBean();
        try {
            whiteListExtendBean = HydraJsonClient.builder()
                    .serviceInterface(WHITE_LIST_LOADER_INTERFACE)
                    .serviceGroup(GROUP)
                    .serviceVersion(VERSION)
                    .methodName("loadWhiteListById")
                    .parameterTypes(Long.class.getName())
                    .arguments(id)
                    .build()
                    .invoke()
                    .mapTo(XueMizarWhiteListExtendBean.class);
        } catch (Exception e) {
            log.error("获取白名单详细信息失败", e);
        }
        return whiteListExtendBean;
    }

}
