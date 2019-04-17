package util;

import java.util.Arrays;

public class DataNode {
    double[] datas;

    public double[] getDatas() {
        return datas;
    }

    public void setDatas(double[] datas) {
        this.datas = datas;
    }

    @Override
    public String toString() {
        return "DataNode [datas=" + Arrays.toString(datas) + "]";
    }

    public DataNode(double[] datas) {
        super();
        this.datas = datas;
    }
}
