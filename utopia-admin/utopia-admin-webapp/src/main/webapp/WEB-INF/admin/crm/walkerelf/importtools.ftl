<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="沃克拯救精灵王数据导入" page_num=10>
    <form id="importBookForm" method="post" action="/appmanager/walkerelf/importbook.vpage" enctype="multipart/form-data">
        <legend>读本导入</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">读本导入表</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="importPlantForm" method="post" action="/appmanager/walkerelf/importplant.vpage" enctype="multipart/form-data">
        <legend>植物导入</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">植物导入表</button>
                </td>
            </tr>
        </table>
    </form>

    <form id="importLevels" method="post" action="/appmanager/walkerelf/importlevel.vpage" enctype="multipart/form-data">
        <legend>关卡导入</legend>
        <table>
            <tr>
                <td colspan="4">
                    <input type="file" name="file"/>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button class="btn btn-primary">关卡导入属性</button>
                </td>
            </tr>
        </table>
    </form>



</@layout_default.page>