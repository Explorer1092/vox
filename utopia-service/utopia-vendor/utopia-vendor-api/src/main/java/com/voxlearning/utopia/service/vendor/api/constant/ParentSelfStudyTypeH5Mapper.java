package com.voxlearning.utopia.service.vendor.api.constant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jiangpeng
 * @since 2017-06-06 下午4:11
 **/
@Data
public class ParentSelfStudyTypeH5Mapper implements Serializable {

    private static final long serialVersionUID = -2488478375224524884L;

    @JsonProperty("icon_url")
    private String iconUrl;  //应用icon 没加域名啊，自己加去。

    @JsonProperty("main_title")
    private String mainTitle;    //应用名称

    @JsonProperty("sub_title")
    private String subTitle; //副标题啊  世伟自己写吧

    @JsonProperty("jump_type")
    private JumpType jumpType;

    @JsonProperty("jump_key")
    private String jumpKey;


    public enum JumpType{
        H5,
        NATIVE
    }

    public static ParentSelfStudyTypeH5Mapper enterPurchasePage(String mainsiteUrl ,FairylandProduct fairylandProduct, Long sid, String rel, Boolean recIcon){
        ParentSelfStudyTypeH5Mapper mapper =
                ParentSelfStudyTypeH5Mapper.newInstance(recIcon ? fairylandProduct.getProductRectIcon() : fairylandProduct.getProductIcon(), fairylandProduct.getProductName());
        String url = mainsiteUrl + "/parentMobile/ucenter/shoppinginfo.vpage?sid=" + sid + "&productType=" + fairylandProduct.getAppKey() + "&rel=" + rel;
        mapper.setJumpType(JumpType.H5);
        mapper.setJumpKey(url);
        return mapper;
    }


    public static ParentSelfStudyTypeH5Mapper enterApp(FairylandProduct fairylandProduct, VendorApps vendorApps, Mode runtimeMode, Boolean recIcon) {
        Map<String, Object> map = new LinkedHashMap<>();
        ParentSelfStudyTypeH5Mapper mapper =
                ParentSelfStudyTypeH5Mapper.newInstance(recIcon ? fairylandProduct.getProductRectIcon() : fairylandProduct.getProductIcon(), fairylandProduct.getProductName());

        if (fairylandProduct.getAppKey().equals(SelfStudyType.PICLISTEN_ENGLISH.getOrderProductServiceType())) {
            map.put("appKey", SelfStudyType.PICLISTEN_ENGLISH.name());
        } else {
            String url = fairylandProduct.fetchRedirectUrl(runtimeMode);
            map.put("appKey", fairylandProduct.getAppKey());
            map.put("launchUrl", url);
            map.put("orientation", vendorApps.getOrientation());
            map.put("browser", vendorApps.getBrowser());
        }
        mapper.setJumpType(JumpType.NATIVE);
        mapper.setJumpKey(JsonStringSerializer.getInstance().serialize(map));
        return mapper;
    }

    public static ParentSelfStudyTypeH5Mapper newInstance(String iconUrl, String name){
        ParentSelfStudyTypeH5Mapper mapper = new ParentSelfStudyTypeH5Mapper();
        mapper.iconUrl = iconUrl;
        mapper.mainTitle = name;
        return mapper;
    }


}
