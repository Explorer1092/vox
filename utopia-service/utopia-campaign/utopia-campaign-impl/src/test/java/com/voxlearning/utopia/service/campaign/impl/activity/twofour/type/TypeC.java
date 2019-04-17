package com.voxlearning.utopia.service.campaign.impl.activity.twofour.type;


import com.voxlearning.utopia.service.campaign.impl.activity.twofour.base.AbstractBaseType;
import com.voxlearning.utopia.service.campaign.impl.activity.twofour.base.TypeInterface;

import java.util.ArrayList;
import java.util.List;

public class TypeC extends AbstractBaseType implements TypeInterface {

    public TypeC(String fileNamePrefix) {
        super(fileNamePrefix);
    }

    @Override
    public void genScheme(int a, int b, int c, int d) {
        List<String> list = new ArrayList<>();

        list.add("(" + a + "+" + b + ")*" + c + "+" + d);
        list.add("(" + a + "+" + b + ")*" + c + "-" + d);
        list.add("(" + a + "+" + b + ")/" + c + "+" + d);
        list.add("(" + a + "+" + b + ")/" + c + "-" + d);

        list.add("(" + a + "-" + b + ")*" + c + "+" + d);
        list.add("(" + a + "-" + b + ")*" + c + "-" + d);
        list.add("(" + a + "-" + b + ")/" + c + "+" + d);
        list.add("(" + a + "-" + b + ")/" + c + "-" + d);

        list.add("(" + a + "*" + b + ")*" + c + "+" + d);
        list.add("(" + a + "*" + b + ")*" + c + "-" + d);
        list.add("(" + a + "*" + b + ")/" + c + "+" + d);
        list.add("(" + a + "*" + b + ")/" + c + "-" + d);

        list.add("(" + a + "/" + b + ")*" + c + "+" + d);
        list.add("(" + a + "/" + b + ")*" + c + "-" + d);
        list.add("(" + a + "/" + b + ")/" + c + "+" + d);
        list.add("(" + a + "/" + b + ")/" + c + "-" + d);

        for (String expression : list) {
            calc(a, b, c, d, expression);
        }
    }

}
