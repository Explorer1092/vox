<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='添加系统权限' page_num=6>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 添加/编辑系统权限</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <form class="form-horizontal" method="POST">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">模块</label>
                        <div class="controls">
                            <input class="input-xlarge focused" type="text" value="<#if operation??>${operation.module!}</#if>" readonly>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">子模块</label>
                        <div class="controls">
                            <input class="input-xlarge focused" type="text" value="<#if operation??>${operation.subModule!}</#if>" readonly>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">功能</label>
                        <div class="controls">
                            <input class="input-xlarge focused" type="text" value="<#if operation??>${operation.operationDesc!}</#if>" readonly>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">地址</label>
                        <div class="controls">
                            <input id="pathName" class="input-xlarge focused" type="text" value="<#if operation??>${operation.path!}</#if>" readonly>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="selectError1">角色列表</label>
                        <div class="controls">
                            <#list roleList as item>
                                <input type="checkbox" name="roles" value="${item.id!}"
                                    <#if operation?? && operation.roleTypeList??>
                                        <#list operation.roleTypeList as role>
                                            <#if item.id == role.id>
                                       checked
                                            </#if>
                                        </#list>
                                    </#if>
                                />
                            ${item.roleName!}<br>
                            </#list>
                        </div>
                    </div>
                    <div class="form-actions">
                        <button id="addPathRoleBtn" type="button" class="btn btn-primary">保存</button>
                        <a class="btn" href="module_operation_roles.vpage"> 取消 </a>
                    </div>
                </fieldset>
            </form>
        </div>
    </div><!--/span-->
</div>
<input type="hidden" id="pathId" value="${pathId!}">
<script type="text/javascript">
    $(function(){
        $('#addPathRoleBtn').live('click',function(){
            var pathName = $('#pathName').val();
            var roleIds = new Array();
            $("input[name='roles']").each(function(){
                if ("checked" == $(this).attr("checked")) {
                    roleIds.push($(this).attr('value'));
                }
            });
            $.post('save_path_to_roles.vpage',{
                path:pathName,
                roleIds:roleIds.toString()
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    $(window.location).attr('href', 'module_operation_roles.vpage');
                }
            });
        });
    });
</script>

</@layout_default.page>