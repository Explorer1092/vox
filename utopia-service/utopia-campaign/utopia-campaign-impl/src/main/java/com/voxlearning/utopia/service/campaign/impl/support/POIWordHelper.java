package com.voxlearning.utopia.service.campaign.impl.support;

import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageSystem;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * word 转 PDF
 *
 * @Author: peng.zhang
 */
@Named
@Slf4j
public class POIWordHelper {

    @StorageClientLocation(system = StorageSystem.OSS, storage = "17-pmc")
    private StorageClient storageClient;

    /**
     * word 文件转 PDF (.doc 类型)
     * @param wordFile word 文件
     * @param imageSuffix 图片后缀
     * @return 转换完的图片地址
     */
    public List<String> convertDoc2Pdf(String wordFile, String imageSuffix){
        List<String> images = new ArrayList<>();
        return images;
    }

    /**
     * word 文件转 PDF (.docx 类型)
     * @param wordFile word 文件
     * @param imageSuffix 图片后缀
     * @return 转换完的图片地址
     */
    public List<String> convertDocx2Pdf(String wordFile, String imageSuffix){
        List<String> images = new ArrayList<>();
        return images;
    }

}
