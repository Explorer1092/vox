<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='活动权限配置' page_num=page_num>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i>活动权限配置</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content form-horizontal">
            <div class="control-group">
                <div class="controls" style="color:red">
                    如果只勾选部门（或角色）， 则部门（或角色）和用户ID取并集所包含的用户具有相应权限
                </div>
            </div>
            <div class="control-group">
                <div class="controls" style="color:red">
                    勾选的部门和角色取交集（或并集）后，在和用户ID取并集所包含的用户具有相应权限
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">部门和角色</label>
                <div class="controls">
                    <label class="control-label" style="text-align: left;">
                        <input type="radio" name="rule" value="0" <#if rule == 0 >checked</#if>/>取交集
                    </label>
                    <label class="control-label" style="text-align: left;">
                        <input type="radio" name="rule" value="1" <#if rule == 1>checked</#if>/>取并集
                    </label>
                </div>
            </div>
            <div class="control-group group_item" style="display: block">
                <label class="control-label">可见部门</label>
                <div class="controls">
                    <div id="useUpdateDep_con_dialog" class="span4"></div>
                </div>
            </div>
            <div class="control-group group_item" style="display: block;width: 80%;">
                <label class="control-label">可见角色</label>
                <div class="controls">
                    <input type="checkbox" name="all" class="all_select" <#if selectedRoleIds?? && selectedRoleIds?size == allRoles?size> checked</#if> >全选
                    <#if allRoles?? && allRoles?size gt 0>
                    <#list allRoles as list>
                        <div>
                        <input type="checkbox"  name="roleType" value="${list.id!0}" <#if selectedRoleIds?? && selectedRoleIds?size gt 0><#list selectedRoleIds as role><#if list.id == role>checked</#if></#list></#if>> ${list.roleName}
                        </div>
                    </#list>
                    </#if>
                </div>
            </div>
            <div class="control-group user_item" style="display: block">
                <label class="control-label">用户ID：</label>
                <div class="controls">
                    <textarea class="input-xlarge"
                              id="userIds" rows="5"
                              style="width: 80%;"
                              placeholder="请输入用户ID，多个用户以“,”隔开"><#if userIds?? && userIds?size gt 0><#list userIds as list>${list!''},</#list></#if></textarea>
                </div>
            </div>
            <div class="form-actions">
                <button type="button" class="btn btn-primary submitBtn" data-info="0">取消</button>
                <button type="button" class="btn btn-primary submitBtn" data-info="1">保存</button>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
// $(function () {
    var recordId = getQuery('recordId');
    var recordType = getQuery('recordType');
    //获取可见部门
    var groupIds = "";
    var groupIdList = [];
    <#if groupIds?? && groupIds?size gt 0>
        <#list groupIds as list>
            groupIdList.push(${list!0});
        </#list>
    </#if>
    groupIds = groupIdList.toString();
    $("#useUpdateDep_con_dialog").fancytree({
        source: {
            url: "/user/orgconfig/getNewDepartmentTree.vpage?groupIds="+groupIds,
            cache:true
        },
        checkbox: true,
        autoCollapse:true,
        selectMode: 3,
        init:function (event, data) {// [356, 183, 346]
            $("#useUpdateDep_con_dialog").fancytree("getTree").visit(function (node) {
                if(groupIds.indexOf(node.key) > -1){
                    node.setSelected(true);
                }
            });
        }
    });

//    $('input[name="type"]').on('click',function () {
//       var val = $(this).val();
//       if(val == 1){
//           $('.group_item').show();
//           $('.user_item').show();
//       }else{
//           $('.group_item').show();
//           $('.user_item').show();
//       }
//    });

    $('.all_select').on('click',function () {
       if($(this).prop('checked')){
           $('input[name="roleType"]').parent('span').addClass('checked');
           $('input[name="roleType"]').attr('checked',true);
       } else{
           $('input[name="roleType"]').parent('span').removeClass('checked');
           $('input[name="roleType"]').attr('checked',false);
       }
    });

    $('.submitBtn').on('click',function () {
        var info = $(this).data('info');
        if(info == 0){
            window.history.back();
        }else{
            var post_data = {
                recordId: recordId || '1212144',
                recordType: recordType || '123'
            };

            //获取部门列表
            var tree = $("#useUpdateDep_con_dialog").fancytree("getTree");
            var groupIds = [];
            if($("#useUpdateDep_con_dialog").fancytree("getTree").getSelectedNodes()){
                var node = $("#useUpdateDep_con_dialog").fancytree("getTree").getSelectedNodes();
                for(var i = 0; i< node.length; i++){
                    groupIds.push(node[i].key);
                }
            }
            // 获取角色列表
            var roleTypes = $('input[name="roleType"]');
            var roleIds = [];
            roleTypes.each(function () {
                if($(this).prop('checked')){
                    roleIds.push($(this).val());
                }
            });
            post_data.groupIds = groupIds.toString();
            post_data.roleIds = roleIds.toString();
            post_data.userIds = $('#userIds').val().trim();
            post_data.rule = $('input[name="rule"]:checked').val();
//            if($('input[name="type"]:checked').val() == 1){
//                post_data.groupIds = groupIds.toString();
//                post_data.roleIds = roleIds.toString();
//            }else{
//                post_data.userIds = $('#userIds').val().trim();
//            }
            $.get('/authority/record/save_authority.vpage',post_data).done(function (res) {
                if(res.success){
                    layer.alert('保存成功',function () {
                        window.history.back();
                    });
                }else{
                    layer.alert(res.info);
                }
            })
        }

    });
// })
</script>
</@layout_default.page>
