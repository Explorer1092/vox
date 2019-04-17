package com.voxlearning.washington.controller.ai;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestUtils;
import com.voxlearning.utopia.core.helper.ShortUrlGenerator;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.ai.api.AiChipsEnglishConfigService;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishClazzService;
import com.voxlearning.utopia.service.ai.client.AiChipsEnglishConfigServiceClient;
import com.voxlearning.utopia.service.ai.client.AiLoaderClient;
import com.voxlearning.utopia.service.ai.client.AiServiceClient;
import com.voxlearning.washington.support.AbstractController;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.function.Consumer;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * Created by Summer on 2018/3/27
 */
public abstract class AbstractAiController extends AbstractController {


    @Inject
    protected AiLoaderClient aiLoaderClient;
    @Inject
    protected AiServiceClient aiServiceClient;
    @ImportService(interfaceClass = ChipsEnglishClazzService.class)
    protected ChipsEnglishClazzService chipsEnglishClazzService;

    @Inject
    protected AiChipsEnglishConfigServiceClient chipsEnglishConfigServiceClient;

    protected final static String APP_VERSION = "chips_app_upgrade_version_";

    private static final int BLACK = -16777216;

    private static final int WHITE = -1;


    protected MapMessage successMessage(String msg) {
        return responseMessage(true, RES_RESULT_SUCCESS, msg);
    }

    protected MapMessage successMessage() {
        return successMessage("");
    }


    protected MapMessage failMessage(String msg) {
        return failMessage(RES_RESULT_BAD_REQUEST_CODE, msg);
    }

    protected MapMessage failMessage(String code, String msg) {
        return responseMessage(false, code, msg);
    }


    private MapMessage responseMessage(boolean success, String code, String info) {
        MapMessage message = new MapMessage();
        message.setSuccess(success);
        message.setInfo(info);
        message.add(RES_RESULT, code);
        message.add(RES_MESSAGE, info);
        return message;
    }


    protected MapMessage wrapper(Consumer<MapMessage> wrapper) {

        MapMessage mm = successMessage();
        mm.setSuccess(true);
        try {
            wrapper.accept(mm);

            if (!mm.isSuccess()) {
                mm.putIfAbsent(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                mm.putIfAbsent(RES_MESSAGE, mm.getInfo());
            } else {
                mm.putIfAbsent(RES_RESULT, RES_RESULT_SUCCESS);
            }
        } catch (CannotAcquireLockException e) {
            mm = failMessage("正在处理中");
        } catch (DuplicatedOperationException e) {
            mm = failMessage("您点击太快了，请重试");
        } catch (Exception e) {
            if (RuntimeMode.current().lt(Mode.STAGING)) {
                mm = failMessage(e.getMessage());
            } else {
                mm = failMessage("服务器繁忙，请稍后再试");
            }
            logger.error(e.getMessage());
        }
        return mm;
    }

    protected void urlToQRCode(String url, OutputStream out) {
        urlToQRCode(url, 300, 300, null, 0, out);
    }


    protected void urlToQRCode(String url, int imgWidth, int imgHeight, String icon, int color, OutputStream out) {
        if (StringUtils.isBlank(url)) {
            return;
        }
        try {
            //解码
            url = url.contains("http://") || url.contains("https://") ? url : URLDecoder.decode(url, "UTF-8");
            //生成短连接
            String shortUrl = ShortUrlGenerator.generateShortUrl(url, true).orElse("");
            if (StringUtils.isNotBlank(shortUrl)) {
                url = ShortUrlGenerator.getShortUrlSiteUrl() + "/" + shortUrl;
            }

            HashMap<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            BitMatrix byteMatrix = new MultiFormatWriter().encode(
                    new String(url.getBytes("UTF-8"), "ISO-8859-1"),
                    BarcodeFormat.QR_CODE,
                    imgWidth,
                    imgHeight,
                    hints);
            color = color != 0 ? color : BLACK;
            BufferedImage image = toBufferedImage(byteMatrix, color);
            if (StringUtils.isNotBlank(icon)) {
                BufferedImage logo = ImageIO.read(new URL(icon));
                Graphics2D graphics = image.createGraphics();
                graphics.drawImage(logo, imgWidth * 2 / 5, imgHeight * 2 / 5, imgWidth * 2 / 10, imgHeight * 2 / 10, null);
                graphics.dispose();
                logo.flush();
            }
            ImageIO.write(image, "png", out);
        } catch (Exception e) {
            logger.error("url to QRCode error. url:{}", url, e);
        }
    }

    private BufferedImage toBufferedImage(BitMatrix matrix, int color) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? color : WHITE);
            }
        }
        return image;
    }

    protected String getWechatSiteUrl() {
        String url = ProductConfig.get("wechat.base_site_url");

        String scheme = HttpRequestUtils.getRealRequestSchema(HttpRequestContextUtils.getRequestContextRequest());
        if (StringUtils.equals(scheme, "http")) {
            scheme = HttpRequestUtils.getRealRequestSchema2(HttpRequestContextUtils.getRequestContextRequest());
        }

        if (StringUtils.isBlank(scheme)) scheme = "https";

        if (url.startsWith(scheme + "://")) return url;

        if (url.startsWith("http://")) return scheme + url.substring(4);
        if (url.startsWith("https://")) return scheme + url.substring(5);

        return scheme + "://" + url;
    }
}
