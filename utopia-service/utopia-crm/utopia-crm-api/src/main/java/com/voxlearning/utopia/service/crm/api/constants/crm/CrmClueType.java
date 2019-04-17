package com.voxlearning.utopia.service.crm.api.constants.crm;

/**
 * CrmClueType
 *
 * @author song.wang
 * @date 2016/8/6
 */
public enum CrmClueType {

    核实老师认证;

    public static CrmClueType nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }

}
