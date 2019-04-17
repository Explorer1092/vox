<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title="沃克大冒险奇幻探险配置" page_num=10>
<span><h1>邹鹏，你可以去厕所了。出来记得洗手。。。@_@</h1></span>
    <form action="save.vpage" method="post">
        <table>
            <tr>
                <td>小游戏ID</td>
                <td><input type="text" name="id"/></td>

                <td>奇幻探险游戏分类</td>
                <td>
                    <select name="type">
                        <option value="BASE">基础应用</option>
                        <option value="SPOKEN">口语应用</option>
                    </select>
                </td>

                <td>奇幻探险游戏类型</td>
                <td>
                    <select name="category">
                        <option value="RECOGNITION">单词辨识</option>
                        <option value="LISTENING">听音选词</option>
                        <option value="FIGURE">看图识词</option>
                        <option value="SPELLING">单词拼写</option>
                        <option value="REPEAT">单词跟读</option>
                    </select>
                </td>
                <td align="right">
                    <input type="submit" class="btn" value="添加"/>
                </td>
            </tr>
            </tr>
        </table>
    </form>

    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th style="width: 20px;">ID</th>
            <th style="width: 100px;">CategoryName</th>
            <th style="width: 100px;">FileName</th>
            <th style="width: 100px;">PracticeName</th>
            <th style="width: 30px;">Size</th>
            <th style="width: 110px;">Type（奇幻探险）</th>
            <th style="width: 130px;">Category（奇幻探险）</th>
            <th style="width: 50px;">状态</th>
            <th style="width: 30px;"></th>
            <th style="width: 50px;"></th>
        </tr>
        <#if systemApps??>
            <#list systemApps as app>
                <tr>
                    <td>${app.id}</td>
                    <td>${app.categoryName}</td>
                    <td>${app.fileName}</td>
                    <td>${app.practiceName}</td>
                    <td>${app.size}</td>
                    <td><@formatType app.type/></td>
                    <td><@formatCategory app.category/></td>
                    <td><@formatValid app.valid/></td>
                    <td><a style="cursor: Pointer" onclick="deleteSystemApp(${app.id})">删除</a></td>
                    <td><a style="cursor: Pointer" onclick="changeSystemAppValid(${app.id})">更改状态</a></td>
                </tr>
            </#list>
        </#if>
    </table>

    <#macro formatType type>
        <#if type??>
            <#if (type == "BASE")>
                基础应用
            <#elseif (type == "SPOKEN")>
                口语应用
            </#if>
        </#if>
    </#macro>

    <#macro formatValid valid>
        <#if valid??>
            <#if (valid)>
            有效
            <#else>
            无效
            </#if>
        </#if>
    </#macro>

    <#macro formatCategory category>
        <#if category??>
            <#if (category == "RECOGNITION")>
                单词辨识
            <#elseif (category == "LISTENING")>
                听音选词
            <#elseif (category == "FIGURE")>
                看图识词
            <#elseif (category == "SPELLING")>
                单词拼写
            <#elseif (category == "REPEAT")>
                单词跟读
            </#if>
        </#if>
    </#macro>
<script>
    function deleteSystemApp(id) {
        if (confirm('将删除小游戏信息，确认继续？')) {
            var postData = {
                id: id
            };
            $.post('delete.vpage', postData, function (data) {
                setTimeout(reload, 1000);
            });
        }
    }

    function changeSystemAppValid(id) {
        if (confirm('将改变小游戏状态，确认继续？')) {
            var postData = {
                id: id
            };
            $.post('changeSystemAppValid.vpage', postData, function (data) {
                setTimeout(reload, 1000);
            });
        }
    }

    function reload() {
        window.location = '/crm/nekketsu/adventure/index.vpage';
    }

</script>
</@layout_default.page>