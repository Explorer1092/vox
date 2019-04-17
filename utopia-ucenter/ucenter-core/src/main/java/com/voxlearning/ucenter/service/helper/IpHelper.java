package com.voxlearning.ucenter.service.helper;

import com.nature.ipdb.IpLocation;
import com.nature.ipdb.IpSearcher;
import com.voxlearning.alps.core.util.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import sun.net.util.IPAddressUtil;

import javax.inject.Named;
import java.util.Objects;

/**
 *
 * ip库的工具类
 *
 * Created by zhouwei on 2018/8/13
 **/
@Named
public class IpHelper implements InitializingBean{

    @Override
    public void afterPropertiesSet() throws Exception{
        IpSearcher.preload();//预加载IP库，大约需要耗时4秒
    }

    /**
     * 是否是海外IP
     * @param ip
     * @return
     */
    public boolean isOverseas(String ip) {
        IpLocation ipLocation = IpSearcher.search(ip);
        if (null != ipLocation && StringUtils.isNoneBlank(ipLocation.getCountry()) && !ipLocation.getCountry().contains("中国")
                && !ipLocation.getCountry().contains("保留")) {
            return true;
        }
        return false;
    }

    public static boolean internalIp(String ip) {
        byte[] addr = IPAddressUtil.textToNumericFormatV4(ip);
        if (Objects.equals(ip, "127.0.0.1") || Objects.equals(ip, "0.0.0.0")) {
            return true;
        }
        final byte b0 = addr[0];
        final byte b1 = addr[1];
        //10.x.x.x/8
        final byte SECTION_1 = 0x0A;
        //172.16.x.x/12
        final byte SECTION_2 = (byte) 0xAC;
        final byte SECTION_3 = (byte) 0x10;
        final byte SECTION_4 = (byte) 0x1F;
        //192.168.x.x/16
        final byte SECTION_5 = (byte) 0xC0;
        final byte SECTION_6 = (byte) 0xA8;
        switch (b0) {
            case SECTION_1:
                return true;
            case SECTION_2:
                if (b1 >= SECTION_3 && b1 <= SECTION_4) {
                    return true;
                }
            case SECTION_5:
                switch (b1) {
                    case SECTION_6:
                        return true;
                }
            default:
                return false;

        }
    }

}
