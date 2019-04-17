<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='页面元素配置' page_num=6>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><#if role?has_content>${role.roleName!''}</#if>页面元素设置</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a type="button" class="submit_btn btn btn-success" href="javascript:;">保存</a>
            </div>
        </div>
        <div class="box-content">
            <div class="dataTables_wrapper">
                <table class="table table-striped table-bordered bootstrap-datatable ">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 60px;">序号</th>
                        <th class="sorting" style="width: 60px;">元素编码</th>
                        <th class="sorting" style="width: 60px;">模块</th>
                        <th class="sorting" style="width: 60px;">子模块</th>
                        <th class="sorting" style="width: 60px;">页面名称</th>
                        <th class="sorting" style="width: 140px;">具体元素</th>
                        <th class="sorting" style="width: 140px;">备注</th>
                        <th class="sorting" style="width: 140px;">有无权限</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list pageElementList as list>
                        <tr>
                            <td>${list_index!0}</td>
                            <td>${list.elementCode!''}</td>
                            <td>${list.module!''}</td>
                            <td>${list.subModule!''}</td>
                            <td>${list.pageName!''}</td>
                            <td>${list.elementName!''}</td>
                            <td>${list.comment!''}</td>
                            <td>
                                <input class="postJsonIpt" type="checkbox" value="${list.id!0}" <#if list.selected?? && list.selected>checked</#if>>
                            </td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<script>
    $(document).on('click','.submit_btn',function () {
        var pageElementIds = [],
                postJson = {};
        for(var i = 0 ; i<$('.postJsonIpt').length ; i++){
            if($('.postJsonIpt').eq(i).is(':checked')){
                pageElementIds.push($('.postJsonIpt').eq(i).val())
            }
        }
        postJson.roleId = '${role.id!0}';
        postJson.pageElementIds = pageElementIds.toString();
        console.log(postJson);
        $.post('save_role_elements.vpage',postJson,function (res) {
            if(res.success){
                alert('设置成功');
            }else{
                alert(res.info);
            }
        })
    });
</script>
</@layout_default.page>