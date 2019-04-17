<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="第三方应用管理" page_num=10>
    <form id="importSkillForm" method="post" action="/appmanager/pk/importskills.vpage" enctype="multipart/form-data">
        <legend>技能导入</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入技能表</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="importEquipmentForm" method="post" action="/appmanager/pk/importequipments.vpage" enctype="multipart/form-data">
        <legend>装备导入</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入装备表</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="importCareerLevels" method="post" action="/appmanager/pk/importcareerlevels.vpage" enctype="multipart/form-data">
        <legend>战斗属性导入</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入战斗属性</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="importNpcRoles" method="post" action="/appmanager/pk/importnpcroles.vpage" enctype="multipart/form-data">
        <legend>NPC导入</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入NPC</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="importExperienceLevels" method="post" action="/appmanager/pk/importexperiencelevels.vpage" enctype="multipart/form-data">
        <legend>经验级别导入</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入经验级别</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="importHonours" method="post" action="/appmanager/pk/importhonours.vpage" enctype="multipart/form-data">
        <legend>荣誉信息导入</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入荣誉信息</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="importProducts" method="post" action="/appmanager/pk/importproducts.vpage" enctype="multipart/form-data">
        <legend>商品信息导入</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入商品信息</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="initBag" method="post" action="/appmanager/pk/importSuperScholarRewardInitial.vpage" enctype="multipart/form-data">
        <legend>导入我要当学霸礼物</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入我要当学霸礼物</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="initBag" method="post" action="/appmanager/pk/sendEquip.vpage" enctype="multipart/form-data">
        <legend>批量发送PK武装</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入批量发送PK武装excel</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="initBag" method="post" action="/appmanager/pk/importPetInitial.vpage" enctype="multipart/form-data">
        <legend>导入PK宠物基本信息</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入PK宠物基本信息excel</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="initBag" method="post" action="/appmanager/pk/importPetSkill.vpage" enctype="multipart/form-data">
        <legend>导入PK宠物技能信息</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入PK宠物技能信息excel</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="initBag" method="post" action="/appmanager/pk/importLevelConf.vpage" enctype="multipart/form-data">
        <legend>导入PK等级经验信息</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入PK等级经验信息excel</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="initBag" method="post" action="/appmanager/pk/importactivityprize.vpage" enctype="multipart/form-data">
        <legend>导入PK活动奖品配置</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入PK活动奖品配置excel</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="initBag" method="post" action="/appmanager/pk/batchprize.vpage" enctype="multipart/form-data">
        <legend>批量发奖</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入批量发奖excel</button>
                </td>
            </tr>
        </table>
    </form>

</@layout_default.page>