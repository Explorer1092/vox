<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='功能操作配置' page_num=6>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i>添加/编辑功能操作</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <form class="form-horizontal" method="POST">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">操作权限编码</label>
                        <div class="controls">
                            <input name="operationCode" data-info="请填写 元素编码" class="operationCode input-xlarge focused postJsonIpt" type="text" <#if operation?has_content>readonly value="${operation.operationCode!}"<#elseif codeEditable?has_content && !codeEditable>readonly</#if>>
                            <#if !operation?has_content><a type="button" class="submit_btn btn btn-success" href="javascript:;">自动生成操作权限编码</a></#if>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">模块</label>
                        <div class="controls">
                            <input name="module" data-info="请填写 页面名称" class="input-xlarge focused postJsonIpt" type="text" value="<#if operation?has_content>${operation.module!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">子模块</label>
                        <div class="controls">
                            <input name="subModule" data-info="请填写 页面名称" class="input-xlarge focused postJsonIpt" type="text" value="<#if operation?has_content>${operation.subModule!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">功能</label>
                        <div class="controls">
                            <input name="operationName" data-info="请填写 页面名称" class="input-xlarge focused postJsonIpt" type="text" value="<#if operation?has_content>${operation.operationName!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">备注</label>
                        <div class="controls">
                            <textarea name="comment" data-info="请填写 描述" maxlength="120" rows="4"  class="input-xlarge focused js-remarks"><#if operation?has_content>${operation.comment!}</#if></textarea>
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
                        <a class="btn" href="operations_roles_list.vpage"> 取消 </a>
                    </div>
                </fieldset>
            </form>
        </div>
    </div><!--/span-->
</div>
<input type="hidden" id="pathId" value="${pathId!}">
<script type="text/javascript">
    $(function(){
        $(document).on("click",'.submit_btn',function () {
            $.post('generate_uuid.vpage',function (res) {
                if(res.success){
                    $('.operationCode').val(res.code);
//                    $('.elementCode').attr("readOnly",true);
                }
            })
        });
        $('#addPathRoleBtn').live('click',function(){
            var postJson = {};
            <#if operation?has_content>
                postJson.operationId = "${operation.id!''}";
            </#if>
            var checkFlag = true;
            $('.postJsonIpt').each(function () {
                if($(this).val() == ''){
                    alert($(this).data('info'));
                    checkFlag = false;
                    return false;
                }else{
                    postJson[$(this).attr('name')] = $(this).val();
                }
            });
            if(checkFlag) {
                var roleIds = new Array();
                $("input[name='roles']").each(function () {
                    if ("checked" == $(this).attr("checked")) {
                        roleIds.push($(this).attr('value'));
                    }
                });
                postJson.roleIds = roleIds.toString();
                postJson[$('.js-remarks').attr('name')] = $('.js-remarks').val();

                $.post('save_operation_roles.vpage', postJson, function (data) {
                    if (!data.success) {
                        alert(data.info);
                    } else {
                        $(window.location).attr('href', 'operations_roles_list.vpage');
                    }
                });
            }
        });
    });
</script>

</@layout_default.page>