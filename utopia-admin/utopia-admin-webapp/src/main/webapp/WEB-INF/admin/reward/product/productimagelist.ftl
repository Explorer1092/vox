<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='CRM' page_num=12>
    <div id="main_container" class="span9">
        <legend>上传图片列表 <a id="uploadPhotoButton" href="javascript:void (0);" role="button" class="btn btn-success">上传照片</a></legend>
        <div class="row-fluid">
            <table class="table table-striped table-bordered">
                <tr>
                    <td>产品Id</td>
                    <td>图片名称</td>
                    <td>图片</td>
                    <td>属性</td>
                    <td>属性值</td>
                    <td>操作</td>
                </tr>
                <#if images?? >
                    <#list images as i>
                        <tr>
                            <td>${i.productId!''}</td>
                            <td>${i.location!''}</td>
                            <td>
                                <#if i.location ?? && i.location?substring(0, 4) == 'http'>
                                    <img src="${i.location}" width="120" style="height: 120px"/>
                                <#else>
                                    <img src="${prePath}/gridfs/${i.location!}" width="120" style="height: 120px"/>
                                </#if>
                            </td>
                            <td>
                                <#if i.relateAttr??>
                                <#switch i.relateAttr>
                                    <#case "Sex">
                                        性别
                                        <#break >
                                    <#default>
                                        无
                                </#switch>
                                </#if>
                            </td>
                            <td>
                            <#if i.relateAttr?? && i.relateAttr == "Sex">
                                <#switch i.relateValue>
                                    <#case "F">
                                        女
                                        <#break >
                                    <#case "M">
                                        男
                                        <#break >
                                    <#default>
                                </#switch>
                            </#if>
                            </td>
                            <td><a href="javascript:void (0);" data-img_id="${i.id!''}" class="btn btn-danger delete_photo_but">删除</a></td>
                        </tr>
                    </#list>
                </#if>
            </table>
        </div>
    </div>
    <#include "uploadphoto.ftl" />

    <script type="text/javascript">
        $(function() {
            //上传图片
            $('#uploadPhotoButton').click(function(){
                $('#uploadphotoBox').modal('show');
            });

            //删除图片
            $(".delete_photo_but").on('click', function(){
                var $this = $(this);
                var imgId = $this.data('img_id');
                $.post('deleteproductimage.vpage',{imageId : imgId}, function(data){
                    if(data.success){
                        $this.closest('tr').remove();
                    }else{
                        alert(data.info);
                    }
                });
            });
        });
    </script>
</@layout_default.page>