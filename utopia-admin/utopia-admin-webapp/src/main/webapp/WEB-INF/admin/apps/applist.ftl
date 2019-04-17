<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title="第三方应用管理" page_num=10>
<link  href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>

<div id="main_container" class="span9">
    <form action="appindex.vpage" method="post" id="app_form">
        <ul class="inline">
            <li>
                <select id="searchVendorId" name="searchVendorId" style="width:250px">
                    <option value="0">--选择开发商--</option>
                    <#list appVendorList as appVendorItem>
                        <option value="${appVendorItem.id}">${appVendorItem.cname}</option>
                    </#list>
                </select>
            </li>
            <li>
                <input type="text" id="searchKeyWords" name="searchKeyWords" class="form-control" style="width: 250px"/>
            </li>
            <li>
                <input id="btn_app_search" type="submit" class="btn btn-primary" value="查询" />
            </li>
            <li>
                <input id="btn_add_app" type="button" class="btn" value="新增" />
            </li>
        </ul>
        <ul class="inline">
            <table class="table table-bordered" >
                <tr>
                    <th>开发商</th>
                    <th>应用名(中文)</th>
                    <th>应用名(英文)</th>
                    <th>APP KEY</th>
                    <th>应用状态</th>
                    <th>运行环境</th>
                    <th>操作</th>
                </tr>
                <tbody id="tbody">
                    <#if vendorAppsList ?? >
                        <#list vendorAppsList as vendorApps >
                        <tr>
                            <td>${vendorApps.vendorId!}</td>
                            <td>${vendorApps.cname!}</td>
                            <td>${vendorApps.ename!}</td>
                            <td>${vendorApps.appKey!}</td>
                            <td>${vendorApps.status!}</td>
                            <td>${vendorApps.runtimeMode!}</td>
                            <td>
                                <a href="addvendorapp.vpage?vendorAppId=${vendorApps.id}">编辑</a>&nbsp;&nbsp;
                                <a href="#" id="del_vendor_app_${vendorApps.id}">删除</a>&nbsp;&nbsp;
                                <a href="editappresg.vpage?appId=${vendorApps.id!}" id="edit_vendor_resg_${vendorApps.id}">受限访问资源</a>&nbsp;&nbsp;
                            </td>
                        </tr>
                        </#list>
                    </#if>
                </tbody>
            </table>
        </ul>
    </form>
</div>

<script type="text/javascript">
$(function(){

    $('#btn_add_app').on('click',function(){
        $("#app_form").attr("action", "addvendorapp.vpage");
        $("#app_form").submit();
    });

    $("a[id^='del_vendor_app_']").on('click',function(){
        delVendorApp($(this).attr("id"));
    });

    $("a[id^='edit_vendor_resg_']").on('click',function(){
        editVendorAppsResg($(this).attr("id"));
    });

});

function delVendorApp(cmdStr){
    if (!confirm("确实要删除该应用吗？")) {
        return false;
    }

    vendorAppId = parseInt(cmdStr.substr("del_vendor_app_".length));

    $.post('delvendorapp.vpage',{
        vendorAppId:vendorAppId
    },function(data){
        if(!data.success){
            alert(data.info);
        } else {
            $("#app_form").attr("action", "appindex.vpage");
            $("#app_form").submit();
        }
    });
}

</script>
</@layout_default.page>