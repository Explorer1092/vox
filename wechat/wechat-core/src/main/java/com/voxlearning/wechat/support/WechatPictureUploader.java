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

package com.voxlearning.wechat.support;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucket;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucketNamespace;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageSystem;
import com.voxlearning.alps.storage.gridfs.factory.GridFSBucketFactory;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import javax.imageio.ImageIO;
import javax.inject.Named;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Summer Yang on 2015/11/6.
 */
@Named
@Slf4j
public class WechatPictureUploader extends SpringContainerSupport {
    private static final int BLACK = -16777216;
    private static final int WHITE = -1;

    @StorageClientLocation(system = StorageSystem.OSS, storage = "homework")
    private StorageClient storageClient;

    private static String HOST = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_homework_image_host"));
    /**
     * 根据地址获得数据的字节流
     *
     * @param strUrl 网络连接地址
     * @return
     */
    public static byte[] getImageFromNetByUrl(String strUrl) {
        try {
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5 * 1000);
            InputStream inStream = conn.getInputStream();//通过输入流获取图片数据
            byte[] btImg = readInputStream(inStream);//得到图片的二进制数据
            return btImg;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 从输入流中获取数据
     *
     * @param inStream 输入流
     * @return
     * @throws Exception
     */
    public static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }

    public String uploadMediaToGFS(Long missionId, byte[] mediaArray) throws IOException {
        if (ArrayUtils.isEmpty(mediaArray)) return null;
        String gfsId = RandomUtils.nextObjectId();
        String filename = "sprmp-" + DateUtils.dateToString(new Date(), "yyyyMMdd") + "-" + missionId + "-" + gfsId + ".jpg";

        try {
            @Cleanup ByteArrayInputStream inStream = new ByteArrayInputStream(mediaArray);
            // FIXME: =========================================================
            // FIXME: Use StorageClient instead
            // FIXME: =========================================================
            GridFSBucketNamespace namespace = new GridFSBucketNamespace("GFSDatabase");
            GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);
            bucket.uploadFromStream(new ObjectId(gfsId), filename, "image/jpeg", inStream);
            return filename;
        } catch (Exception ex) {
            logger.warn("Upload spr mission picture: failed writing into mongo gfs", ex.getMessage());
            return null;
        }
    }

    public void delete(String filename) {
        // FIXME: =========================================================
        // FIXME: Use StorageClient instead
        // FIXME: =========================================================
        GridFSBucketNamespace namespace = new GridFSBucketNamespace("GFSDatabase");
        GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);
        bucket.safeDeleteByFilename(filename);
    }

    public byte[] downLoadMediaFromWechat(String accessToken, String mediaId) throws IOException {
        String url = "http://file.api.weixin.qq.com/cgi-bin/media/get?access_token=" + accessToken + "&media_id=" + mediaId;
        return getImageFromNetByUrl(url);
    }

    public String generateAndUploadQRCodeImage(String url, String filePath) {
        if (StringUtils.isAnyBlank(url, filePath)) {
            return null;
        }
        try(ByteArrayOutputStream imageOut = new ByteArrayOutputStream()) {
            // 二维码大小,默认300*300
            int imgWidth = 300;
            int imgHeight = 300;
            BitMatrix byteMatrix;
            HashMap<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            byteMatrix = new MultiFormatWriter().encode(
                    new String(url.getBytes("UTF-8"), "ISO-8859-1"),
                    BarcodeFormat.QR_CODE,
                    imgWidth,
                    imgHeight,
                    hints);
            BufferedImage image = toBufferedImage(byteMatrix);
            ImageIO.write(image, "png", imageOut);
            String imageName = "QRCode_" + RandomUtils.nextObjectId() + ".png";
            storageClient.upload(new ByteArrayInputStream(imageOut.toByteArray()), imageName, filePath);

            return HOST + "/" + filePath + "/" + imageName;
        } catch (Exception e) {
            logger.error("generateAndUploadQRCodeImage error. url:{}, filePath:{}", url, filePath, e);
            return null;
        }
    }

    public BufferedImage toBufferedImage(BitMatrix matrix) {
       return toBufferedImage(matrix, BLACK);
    }

    public BufferedImage toBufferedImage(BitMatrix matrix, int color) {
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
}
