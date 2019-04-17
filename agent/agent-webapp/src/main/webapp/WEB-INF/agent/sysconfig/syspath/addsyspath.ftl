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
                        <label class="control-label" for="focusedInput">权限应用名称</label>
                        <div class="controls">
                            <input id="appName" class="input-xlarge focused" type="text" value="<#if agentSysPath??>${agentSysPath.appName!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">权限路径名称</label>
                        <div class="controls">
                            <input id="pathName" class="input-xlarge focused" type="text" value="<#if agentSysPath??>${agentSysPath.pathName!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">权限描述</label>
                        <div class="controls">
                            <textarea id="desc" style="width: 270px;resize:none" rows="5"><#if agentSysPath??>${agentSysPath.description!}</#if></textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="selectError1">角色列表</label>
                        <div class="controls">
                            <#list allAgentRoleMap?keys as item>
                                <input type="checkbox" name="roles" value="${item!}"
                                    <#if agentSysPath?? && agentSysPath.authRoleList??>
                                        <#list agentSysPath.authRoleList as role>
                                            <#if item == role.roleId?string>
                                                checked
                                            </#if>
                                        </#list>
                                    </#if>
                                />
                                ${allAgentRoleMap[item?string].roleName!}<br>
                            </#list>
                        </div>
                    </div>
                    <div class="form-actions">
                        <button id="addPathRoleBtn" type="button" class="btn btn-primary">保存</button>
                        <a class="btn" href="index.vpage"> 取消 </a>
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
            var appName = $('#appName').val();
            var pathName = $('#pathName').val();
            var desc = $('#desc').val();
            var roles = new Array();
            $("input[name='roles']").each(function(){
                if ("checked" == $(this).attr("checked")) {
                    roles.push($(this).attr('value'));
                }
            });
            if(!checkAddPathRole(appName,pathName,roles)){
                return false;
            }
            $.post('addsyspath.vpage',{
                appName:appName,
                pathName:pathName,
                desc: desc,
                roles:roles,
                pathId:$('#pathId').val()
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    $(window.location).attr('href', 'index.vpage');
                }
            });
        });

    });
    function checkAddPathRole(appName,pathName,roles){
        if(appName.trim() == ''){
            alert("请输入应用名称!");
            return false;
        }
        if(pathName.trim() == ''){
            alert("请输入路径名称!");
            return false;
        }
        if(roles.length == 0){
            alert("请选择角色!");
            return false;
        }
        return true;
    }
</script>

</@layout_default.page>