<#include "../index.ftl" />

<div class="container">
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <legend>
                ${pageMessage!}列表：
                </legend>

                <form class="form-horizontal">
                    <select name='appName' id="search_appName">
                        <option value="">All</option>
                    <#list selectAppList as applistItem>
                        <option value="${applistItem.appName!}" <#if applistItem.appName == appName>
                                selected="selected" </#if>>${applistItem.appDescription!}</option>
                    </#list>
                    </select>
                    <input type="text" id="search_pathName" class="input-large" placeholder="Path" name="pathName"
                           value="${pathName!}">
                    <button type="button" class="btn" id="search_button">查找</button>
                </form>

                <table class="table table-striped table-bordered">
                    <tr>
                        <td style="width: 4%"></td>
                        <td style="width: 15%">所属业务系统</td>
                        <td style="width:35%;">路径</td>
                        <td style="width: 15%">名称</td>
                        <td style="width: 10%">角色</td>
                        <td style="width: 10%"></td>
                    </tr>
                <#if pathList??>
                    <#list pathList as pathItem>
                        <tr>
                            <td>${pathItem_index+1}</td>
                            <td>${appNames[pathItem.appName]!pathItem.appName}</td>
                            <td>${pathItem.pathName!}</td>
                            <td><input type="text" id="pathDescription_${pathItem_index+1}"></td>
                            <td>
                                <#if pathItem.adminRoles??>
                                    <#assign  keys=pathItem.adminRoles?keys/>
                                    <#list keys as key>
                                        <input disabled type="checkbox" value="${key}" class="role"
                                               group="role_${pathItem_index+1}"
                                               <#if pathItem.adminRoles[key]>checked</#if>>

                                    ${key}
                                    </#list>
                                </#if>
                            </td>
                            <td>
                                <a class="btn btn-mini sure-add" appName="${pathItem.appName}"
                                   pathName="${pathItem.pathName!}"
                                   pathDescriptionId="pathDescription_${pathItem_index+1}"
                                   pathRoleGroup="role_${pathItem_index+1}">确认添加</a>
                            </td>
                        </tr>
                    </#list>
                </#if>
                </table>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(".sure-add").click(function () {
        var pathName = $(this).attr("pathName");
        var appName = $(this).attr("appName");
        var pathDescriptionId = $(this).attr("pathDescriptionId");
        var pathDescription = $("#" + pathDescriptionId).val();
        var pathRoleGroup = $(this).attr("pathRoleGroup");
        if (pathDescription.trim().length == 0) {
            alert("请填写名称");
            return;
        }
        $.post('path_role_add.vpage'
                , {'appName': appName, 'pathName': pathName, 'pathDescription': pathDescription}
                , function (data) {
                    if (data.success) {
                        $('input[group="' + pathRoleGroup + '"]:checked').each(function () {
                            $.post('path_role_edit.vpage'
                                    , {'do': 'add', 'pathId': data.id, 'roleName': $(this).val()+"Accessor"}
                                    , function (data) {
                                        // ..
                                    });
                        });
                        setTimeout(function () {
                            alert("操作成功");
                            location.reload();
                        },3000);

                    } else {
                        alert(data.info)
                    }

                });


    });
    function setUrlParam(para_name, para_value, url) {
        var strNewUrl = new String();
        var strUrl = url;
        if (strUrl.indexOf("?") != -1) {
            strUrl = strUrl.substr(strUrl.indexOf("?") + 1);
            var aParam = strUrl.split("&");
            var isIn = false;
            for (var i = 0; i < aParam.length; i++) {
                if (aParam[i].substr(0, aParam[i].indexOf("=")).toLowerCase() == para_name.toLowerCase()) {
                    aParam[i] = aParam[i].substr(0, aParam[i].indexOf("=")) + "=" + para_value;
                    isIn = true;
                    break;
                }
            }
            if (!isIn) {
                aParam.push(para_name + "=" + para_value);
            }
            strNewUrl = url.substr(0, url.indexOf("?") + 1) + aParam.join("&");
            return strNewUrl;
        } else {
            strUrl += "?" + para_name + "=" + para_value;
            return strUrl;
        }
    }
    function doSearch() {
        var url = location.href;
        url = setUrlParam("appName", $("#search_appName").val(), url);
        url = setUrlParam("pathName", $("#search_pathName").val(), url);
        location.href = url;
    }
    $("select").change(function () {
        doSearch();
    });
    $("#search_button").click(function () {
        doSearch();
    });

</script>
