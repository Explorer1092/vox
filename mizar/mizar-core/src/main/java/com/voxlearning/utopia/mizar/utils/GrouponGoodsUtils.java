package com.voxlearning.utopia.mizar.utils;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.mizar.api.constants.GrouponGoodsSourceType;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GrouponGoods;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Created by xiang.lv on 2016/10/10.
 *
 * @author xiang.lv
 * @date 2016/10/10   14:08
 * 抓取第三方数据
 */
@Slf4j
public class GrouponGoodsUtils {
    /**
     * @param url 当当图书url
     * @return
     */
    public static GrouponGoods getDDBookByUrl(final String url) {
        final WebClient webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(false); // 启用JS解释器，默认为true
        webClient.getOptions().setCssEnabled(false); // 禁用css支持
        webClient.getOptions().setThrowExceptionOnScriptError(false); // js运行错误时，是否抛出异常
        webClient.getOptions().setTimeout(100000); // 设置连接超时时间 ，这里是10S。如果为0，则无限期等待
        webClient.getOptions().setDoNotTrackEnabled(false);
        GrouponGoods grouponGoods = new GrouponGoods();
        try {
            final HtmlPage page = webClient.getPage(url);
            final HtmlDivision div = page.getFirstByXPath("//div[@class='name_info']");
            DomNode shortTitleNode = div.getFirstByXPath("//h1");
            String shortTitle = shortTitleNode.getAttributes().getNamedItem("title").getNodeValue();
            DomNode titleNode = div.getFirstByXPath("//h2//span");//长标题
            String title = titleNode.getAttributes().getNamedItem("title").getNodeValue();
            HtmlDivision originPriceNode = page.getFirstByXPath("//div[@class='price_pc']//div[@class='price_m']");//原价
            HtmlDivision priceNode = page.getFirstByXPath("//div[@class='price_pc']//div[@class='price_d']");//现价
            HtmlParagraph p = priceNode.getFirstByXPath("//p[@id='price_sale']");
            HtmlImage bigImgNode = page.getFirstByXPath("//div[@class='big_pic']//img");//封面图(大图)
            String priceNodeReadyState = priceNode.getReadyState();
            if (Objects.isNull(p)) {
                p = priceNode.getFirstByXPath("//p[@id='dd-price']");
            }
            String price = p.asText();
            String originPrice = originPriceNode.asText();
            String img = bigImgNode.getAttributes().getNamedItem("src").getNodeValue();
            grouponGoods.setOriginUrl(url);
            grouponGoods.setUrl(url + "?unionid=P-330667m");
            grouponGoods.setOriginalPrice(SafeConverter.toDouble(originPrice.replace("¥", "").trim()));
            grouponGoods.setPrice(SafeConverter.toDouble(price.replace("¥", "").trim()));
            grouponGoods.setShortTitle(shortTitle);
            grouponGoods.setTitle(title);
            grouponGoods.setGoodsSource(GrouponGoodsSourceType.DANG_DNAG.getCode());
            String imgUrl = MizarOssManageUtils.upload(img);
            if (!MizarOssManageUtils.invalidFile.equals(imgUrl)) {
                grouponGoods.setImage(imgUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("抓取数据出错,url=" + url, e);
        } finally {
            webClient.close();
        }
        return grouponGoods;
    }

    public static void main(String[] args) {
        //  String url="http://product.dangdang.com/24002979.html#ddclick_reco_reco_alsoview";
        String[] urls = new String[]{"http://product.dangdang.com/23706293.html#ddclick_reco_reco_buytogether", "http://product.dangdang.com/23506329.html#ddclick_reco_reco_buytogether", "http://product.dangdang.com/21078682.html", "http://product.dangdang.com/24002979.html#ddclick_reco_reco_alsoview"};
        for (String url : urls) {
            long s = System.currentTimeMillis();
            GrouponGoods grouponGoods = getDDBookByUrl(url);
            long e = System.currentTimeMillis();
            System.out.println("use time = " + (e - s) + " grouponGoods=" + grouponGoods);
        }
    }
}
