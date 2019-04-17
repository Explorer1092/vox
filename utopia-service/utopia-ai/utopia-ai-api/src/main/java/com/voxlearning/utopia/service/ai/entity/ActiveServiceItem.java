package com.voxlearning.utopia.service.ai.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author guangqing
 * @since 2019/1/4
 */
@Getter
@Setter
public class ActiveServiceItem implements Serializable {

    private static final long serialVersionUID = 7845541309784727726L;
    private String key;
    private String name;
    private String value;
    private Type type;

    public static List<ActiveServiceItem> buildRenewV1() {
        List<ActiveServiceItem> list = new ArrayList<>();
        ActiveServiceItem k1 = new ActiveServiceItem();
        k1.setName("文本1");
        k1.setValue("");
        k1.setType(Type.text);
        list.add(k1);

        ActiveServiceItem k2 = new ActiveServiceItem();
        k2.setName("音频1");
        k2.setValue("");
        k2.setType(Type.audio);
        list.add(k2);

        ActiveServiceItem k3 = new ActiveServiceItem();
        k3.setName("文本2");
        k3.setValue("");
        k3.setType(Type.text);
        list.add(k3);

        ActiveServiceItem k4 = new ActiveServiceItem();
        k4.setName("音频2");
        k4.setValue("");
        k4.setType(Type.audio);
        list.add(k4);
        ActiveServiceItem k5 = new ActiveServiceItem();
        k5.setName("文本3");
        k5.setValue("");
        k5.setType(Type.text);
        list.add(k5);

        ActiveServiceItem k6 = new ActiveServiceItem();
        k6.setName("音频3");
        k6.setValue("");
        k6.setType(Type.audio);
        list.add(k6);

        return list;
    }

    public static List<ActiveServiceItem> buildRenewV2() {
        List<ActiveServiceItem> list = new ArrayList<>();
        ActiveServiceItem k1 = new ActiveServiceItem();
        k1.setName("优惠券介绍");
        k1.setValue("");
        k1.setType(Type.text);
        list.add(k1);

        ActiveServiceItem k2 = new ActiveServiceItem();
        k2.setName("优惠券图片");
        k2.setValue("");
        k2.setType(Type.image);
        list.add(k2);

        ActiveServiceItem k3 = new ActiveServiceItem();
        k3.setName("大礼包介绍");
        k3.setValue("");
        k3.setType(Type.text);
        list.add(k3);

        ActiveServiceItem k4 = new ActiveServiceItem();
        k4.setName("大礼包图片");
        k4.setValue("");
        k4.setType(Type.image);
        list.add(k4);
        ActiveServiceItem k5 = new ActiveServiceItem();
        k5.setName("拼团介绍");
        k5.setValue("");
        k5.setType(Type.text);
        list.add(k5);

        ActiveServiceItem k6 = new ActiveServiceItem();
        k6.setName("拼团图片");
        k6.setValue("");
        k6.setType(Type.image);
        list.add(k6);
        ActiveServiceItem k7 = new ActiveServiceItem();
        k7.setName("紧迫性介绍");
        k7.setValue("");
        k7.setType(Type.text);
        list.add(k7);

        ActiveServiceItem k8 = new ActiveServiceItem();
        k8.setName("购买二维码");
        k8.setValue("");
        k8.setType(Type.image);
        list.add(k8);
        return list;
    }

    public enum Type {
        text,
        audio,
        image
    }

    @Override
    public String toString() {
        return "ActiveServiceKnowledge{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", type=" + type +
                '}';
    }
}


