<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title="第三方应用开发商管理" page_num=10>
<link  href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>

<div id="main_container" class="span9">
    <legend>
        <input id="btn_add_vendor" type="button" class="btn" value="添加应用开发商" />
    </legend>

    <form action="vendorindex.vpage" method="post" id="vendor_list_form">
    <ul class="inline">
        <table class="table table-bordered" >
            <tr>
                <th>ID</th>
                <th>公司名</th>
                <th>联系人姓名</th>
                <th>联系人手机</th>
                <th>联系人邮箱</th>
                <th>操作</th>
            </tr>
            <tbody id="tbody">
                <#if appVendorList ?? >
                    <#list appVendorList as appVendor >
                    <tr>
                        <td>${appVendor.id!}</td>
                        <td>${appVendor.cname!}</td>
                        <td>${appVendor.contact1Name!}</td>
                        <td>${appVendor.contact1Mob!}</td>
                        <td>${appVendor.contact1Email!}</td>
                        <td><a href="addvendor.vpage?vendorId=${appVendor.id}">编辑</a>&nbsp;&nbsp;<a href="#" id="del_app_vendor_${appVendor.id}">删除</a></td>
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

    $('#btn_add_vendor').on('click',function(){
        $("#vendor_list_form").attr("action", "addvendor.vpage");
        $("#vendor_list_form").submit();
    });

    $("a[id^='del_app_vendor_']").on('click',function(){
        delAppVendor($(this).attr("id"));
    });
});

function delAppVendor(cmdStr){
    if (!confirm("确实要删除该开发商吗？")) {
        return false;
    }

    vendorId = parseInt(cmdStr.substr("del_app_vendor_".length));

    $.post('delvendor.vpage',{
        vendorId:vendorId
    },function(data){
        if(!data.success){
            alert(data.info);
        } else {
            $("#vendor_list_form").attr("action", "vendorindex.vpage");
            $("#vendor_list_form").submit();
        }
    });
}

</script>

</@layout_default.page>