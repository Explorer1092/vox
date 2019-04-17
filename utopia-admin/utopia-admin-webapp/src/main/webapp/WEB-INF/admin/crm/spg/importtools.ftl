<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="第三方应用管理" page_num=10>
    <form id="importSkillForm" method="post" action="/appmanager/spg/importegg.vpage" enctype="multipart/form-data">
        <legend>导入宠物蛋</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入宠物蛋</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="importpetform" method="post" action="/appmanager/spg/importpetskill.vpage" enctype="multipart/form-data">
        <legend>导入宠物技能</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入宠物技能</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="importPetDef" method="post" action="/appmanager/spg/importpetdef.vpage" enctype="multipart/form-data">
        <legend>导入宠物定义</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入宠物定义</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="importEvolveFormula" method="post" action="/appmanager/spg/importevolveformula.vpage" enctype="multipart/form-data">
        <legend>进化路线导入</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">进化路线导入</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="importEnemy" method="post" action="/appmanager/spg/importenemy.vpage" enctype="multipart/form-data">
        <legend>导入关卡敌人</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入关卡敌人</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="importFight" method="post" action="/appmanager/spg/importfight.vpage" enctype="multipart/form-data">
        <legend>导入关卡定义信息</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入关卡定义信息</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="importInstancePlay" method="post" action="/appmanager/spg/importinstanceplay.vpage" enctype="multipart/form-data">
        <legend>导入剧情副本</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入剧情副本</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="importExpLevelConf" method="post" action="/appmanager/spg/importexplevelconf.vpage" enctype="multipart/form-data">
        <legend>导入经验等级配置</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">导入经验等级配置</button>
                </td>
            </tr>
        </table>
    </form>


</@layout_default.page>