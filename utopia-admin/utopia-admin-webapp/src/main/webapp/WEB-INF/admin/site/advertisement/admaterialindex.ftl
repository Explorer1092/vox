<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='广告位设置' page_num=4>
<div id="main_container" class="span9">
    <legend>广告位设置:${ad.name!}</legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <div class="form-horizontal">
                    <#if positionTypes??>
                        <#list positionTypes as type>
                            <div class="control-group">
                                <label class="col-sm-2 control-label">${type.value!}</label>
                                <form id="form_${type.value!}" name="upload_img_form" action="saveposition.vpage" method="post" enctype="multipart/form-data">
                                    <div class="controls">
                                        <input type="file" name="file" class="" value="上传图片"/>
                                        是否置顶:<input type="checkbox" name="isTop" value="0">
                                        是否默认:<input type="checkbox" name="isDefault" value="0">
                                        <input type="hidden" name="position" value="${type.key!}" id="position_${type.value!}">
                                        <input type="hidden" name="adId" value="${ad.id!}" id="adId_${type.value!}">
                                        &nbsp;&nbsp;&nbsp;&nbsp;
                                        <button type="submit" class="btn btn-primary" id="save_img_btn">保存</button>
                                    </div>
                                </form>
                            </div>
                        </#list>
                    </#if>
                </div>
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>广告位</td>
                        <td>图片</td>
                        <td>是否置顶</td>
                        <td>是否默认</td>
                        <td>操作</td>
                    </tr>
                    <#if advertisementPositions?? >
                        <#list advertisementPositions as position >
                            <tr>
                                <td>${positionMap[position.position?string].getName()!}</td>
                                <td><img src="${prePath!}${position.imgUrl!}" width="300px" height="300px"/></td>
                                <td>
                                    <span id="is_top_${position.id}">${position.isTop?string("置顶","不置顶")!}</span>
                                    <#if position.isTop><a id="set_top_${position.id}" href="javascript:void(0);" status="0">不置顶</a>
                                    <#else><a id="set_top_${position.id}" href="javascript:void(0);" status="1">置顶</#if></a>
                                </td>
                                <td>
                                    <span id="is_default_${position.id}">${position.isDefault?string("默认广告","非默认广告")!}</span>
                                    <#if position.isDefault><a id="set_default_${position.id}" href="javascript:void(0);" status="0">设置非默认</a>
                                    <#else><a id="set_default_${position.id}" href="javascript:void(0);" status="1">设置默认</#if></a>
                                </td>
                                <td>
                                    <a id="del_ad_position_${position.id!}" href="javascript:void(0)">删除</a>
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
    $(function() {
        //上传图片
        $("input[type='checkbox']").each(function(){
            $(this).on("click", function(){
                if($(this).prop("checked")){
                    $(this).val(1);
                }else{
                    $(this).val(0);
                }
            });
        });

        //删除图片
        $('[id^="del_ad_position_"]').on('click', function(){
            if(!confirm("确定要删除吗？")){
                return false;
            }
            var id = $(this).attr("id").substr("del_ad_material_".length);
            $.post('deladposition.vpage',{id : id}, function(data){
                if(data.success){
                    window.location.reload();
                }else{
                    alert(data.info);
                }
            });
        });

        $('[id^="set_top_"]').on('click', function(){

            var status = $(this).attr("status");
            var op;
            if(status == '1'){
                op = "置顶";
            }else{
                op = "不置顶";
            }
            if(!confirm("确定要"+op+"？")){
                return false;
            }
            var id = parseInt($(this).attr("id").substr("set_top_".length));
            $.post("settop.vpage",{
                id:id,
                isTop:parseInt(status)
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    status = (status == '0')?1:0;
                    $("#set_top_"+id).text((op == '置顶')?"不置顶":"置顶");
                    $("#is_top_"+id).text((op == '置顶')?"置顶":"不置顶");
                    $("#set_top_"+id).attr("status",status);
                }
            });
        });

        $('[id^="set_default_"]').on('click', function(){

            var status = $(this).attr("status");
            var op;
            if(status == '1'){
                op = "设置默认";
            }else{
                op = "设置非默认";
            }
            if(!confirm("确定要"+op+"？")){
                return false;
            }
            var id = parseInt($(this).attr("id").substr("set_default_".length));
            $.post("setdefault.vpage",{
                id:id,
                isDefault:parseInt(status)
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    status = (status == '0')?1:0;
                    $("#set_default_"+id).text((op == '设置默认')?"设置非默认":"设置默认");
                    $("#is_default_"+id).text((op == '设置默认')?"设置默认":"设置非默认");
                    $("#set_default_"+id).attr("status",status);
                }
            });
        });
    });

</script>
</@layout_default.page>