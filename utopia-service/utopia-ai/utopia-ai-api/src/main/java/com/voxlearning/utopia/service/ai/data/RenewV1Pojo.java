package com.voxlearning.utopia.service.ai.data;

import com.voxlearning.utopia.service.ai.entity.ActiveServiceItem;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author guangqing
 * @since 2019/2/22
 */
@Getter
@Setter
public class RenewV1Pojo implements Serializable {

    private static final long serialVersionUID = -2039357888465272019L;
    private List<ActiveServiceItem> topItemList;
    private List<WeekPoint> weekPointList;
    private List<ActiveServiceItem> bottomItemList;

    @Getter
    @Setter
    public static class WeekPoint implements Serializable{

        private static final long serialVersionUID = -4878040102034914919L;
        //        private UGCWeekPointsEnum weekPoint;//薄弱点
        private String weekPointName;//薄弱点 定级报告名称
        private String weekPointDesc;//薄弱点中文描述
        private int weekPointLevel;//薄弱点优先级
        private List<ActiveServiceItem> wpItemList;
    }

    public static List<ActiveServiceItem> buildRenewV1WeekPoint() {
        List<ActiveServiceItem> list = new ArrayList<>();
        ActiveServiceItem k1 = new ActiveServiceItem();
        k1.setName("薄弱点解读");
        k1.setValue("");
        k1.setType(ActiveServiceItem.Type.text);
        list.add(k1);

        ActiveServiceItem k2 = new ActiveServiceItem();
        k2.setName("薄弱点解读音频");
        k2.setValue("");
        k2.setType(ActiveServiceItem.Type.audio);
        list.add(k2);

        ActiveServiceItem k3 = new ActiveServiceItem();
        k3.setName("薄弱点提升");
        k3.setValue("");
        k3.setType(ActiveServiceItem.Type.text);
        list.add(k3);

        ActiveServiceItem k4 = new ActiveServiceItem();
        k4.setName("薄弱点提升音频");
        k4.setValue("");
        k4.setType(ActiveServiceItem.Type.audio);
        list.add(k4);
        ActiveServiceItem k5 = new ActiveServiceItem();
        k5.setName("推课");
        k5.setValue("");
        k5.setType(ActiveServiceItem.Type.text);
        list.add(k5);

        ActiveServiceItem k6 = new ActiveServiceItem();
        k6.setName("推课音频");
        k6.setValue("");
        k6.setType(ActiveServiceItem.Type.audio);
        list.add(k6);

        return list;
    }

    public static List<ActiveServiceItem> buildRenewV1Bottom() {
        List<ActiveServiceItem> list = new ArrayList<>();
        ActiveServiceItem k1 = new ActiveServiceItem();
        k1.setName("后续系统课程介绍");
        k1.setValue("");
        k1.setType(ActiveServiceItem.Type.text);
        list.add(k1);

        ActiveServiceItem k2 = new ActiveServiceItem();
        k2.setName("后续系统课程音频");
        k2.setValue("");
        k2.setType(ActiveServiceItem.Type.audio);
        list.add(k2);

        return list;
    }

    public static List<ActiveServiceItem> buildRenewV1Top() {
        List<ActiveServiceItem> list = new ArrayList<>();
        ActiveServiceItem k1 = new ActiveServiceItem();
        k1.setName("定级报告介绍");
        k1.setValue("");
        k1.setType(ActiveServiceItem.Type.text);
        list.add(k1);

        ActiveServiceItem k2 = new ActiveServiceItem();
        k2.setName("定级报告音频");
        k2.setValue("");
        k2.setType(ActiveServiceItem.Type.audio);
        list.add(k2);

        return list;
    }

}
