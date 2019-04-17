package com.voxlearning.utopia.service.mizar.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mizar User Role Type
 * Created by alex on 2016/8/18.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MizarUserRoleType {

    MizarAdmin(1, "管理员", "管理员"),
    Operator(10, "运营人员", "运营人员"),
    ShopOwner(21, "机构业主", "机构业主"),
    MicroTeacher(22, "微课堂教师", "微课堂教师"),
    TangramJury(25, "七巧板活动评审", "七巧板活动评审"),
    BusinessDevelopment(30, "市场人员", "市场人员"),
    BusinessDevelopmentManager(31,"市场管理人员","市场管理人员"),
    OfficialAccounts(40, "公众号", "公众号"),
    INFANT_SCHOOL(60, "学前学校", "学前学校"),
    INFANT_OP(61, "学前运营", "学前运营"),
    JX_TEACHER_RESEARCH(70, "江西教研员", "江西教研员"),
    CREATE_CURRENT_DEPARTMENT_USER(80, "创建当前部门市场和运营人员角色", "创建当前部门市场和运营人员角色"),
    ;

    @Getter private final Integer id;
    @Getter private final String roleName;
    @Getter private final String desc;

    private final static Map<String, MizarUserRoleType> ROLE_TYPE_MAP = new LinkedHashMap<>();
    private final static List<Integer> ROLE_ID_LIST = new ArrayList<>();

    static {
        for (MizarUserRoleType roleType : MizarUserRoleType.values()) {
            ROLE_ID_LIST.add(roleType.getId());
            ROLE_TYPE_MAP.put(String.valueOf(roleType.getId()), roleType);
        }
    }

    public static Map<String, MizarUserRoleType> getAllMizarUserRoles() {
        return ROLE_TYPE_MAP;
    }

    public static List<Integer> getAllMizarUserRoleIds() {
        return ROLE_ID_LIST;
    }

    public static MizarUserRoleType of(Integer type) {
        if (type == null) {
            return null;
        }
        return ROLE_TYPE_MAP.get(String.valueOf(type));
    }

}
