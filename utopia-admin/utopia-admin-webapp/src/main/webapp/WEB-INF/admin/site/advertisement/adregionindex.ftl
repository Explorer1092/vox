<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="Advertisement Management" page_num=4>
<div id="main_container" class="span9">
    <legend>
        城市校区设置:<#if ad??>${ad.name!}</#if>
        <a id="add_adregion" href="addadregion.vpage?adId=<#if ad??>${ad.id!}</#if>" type="button" class="btn btn-info" style="float: right">增加</a>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>省份</td>
                        <td>市</td>
                        <td>区县</td>
                        <td>联系电话</td>
                        <td>广告单价</td>
                        <td>分区名称</td>
                        <td>操作</td>
                    </tr>
                    <#if adRegionList?? >
                        <#list adRegionList as adRegion >
                            <tr>
                                <td>${adRegion.province!}</td>
                                <td>${adRegion.city!}</td>
                                <td>${adRegion.county!}</td>
                                <td>${adRegion.contactPhone!}</td>
                                <td>${adRegion.price!}</td>
                                <td>${adRegion.name!}</td>
                                <td>
                                    <a href="addadregion.vpage?adId=<#if ad??>${ad.id!}</#if>&id=${adRegion.id!}">编辑</a>
                                    <a id="del_ad_region_${adRegion.id!}" href="javascript:void(0)">删除</a>
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

    $(function(){

        $('[id^="online_"]').on('click', function(){

            var status = $(this).attr("status");
            var op;
            if(status == '1'){
                op = "上线";
            }else{
                op = "下线";
            }
            if(!confirm("确定要"+op+"？")){
                return false;
            }
            var id = parseInt($(this).attr("id").substr("online_".length));
            $.post("setonline.vpage",{
                id:id,
                status:parseInt(status)
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    status = (status == '2')?1:2;
                    $("#online_"+id).text((op == '上线')?"下线":"上线");
                    $("#status_"+id).text((op == '上线')?"上线":"下线");
                    $("#online_"+id).attr("status",status);
                }
            });
        });

        $('[id^="del_ad_region_"]').on('click', function(){
            if(!confirm("确定要删除吗？")){
                return false;
            }
            var id = $(this).attr("id").substring("del_ad_region_".length);
            $.post('deladregion.vpage',{
                id:id
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.reload();
                }
            });
        });
    });
</script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-prompts-alert.js"></script>
</@layout_default.page>