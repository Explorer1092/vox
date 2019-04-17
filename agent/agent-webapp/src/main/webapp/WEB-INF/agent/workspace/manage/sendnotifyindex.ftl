<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='下发通知' page_num=1>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>

<div class="row-fluid sortable ui-sortable">
    <#if error??>
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong>出错啦！ ${error!}</strong>
        </div>
    </#if>
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 下发通知</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <form id="notify_form" class="form-horizontal" method="POST" action="send.vpage" enctype="multipart/form-data">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">通知题目</label>
                        <div class="controls">
                            <textarea id="notify_title" class="input-xlarge focused" rows="2" name="title"></textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">通知内容</label>
                        <div class="controls">
                            <textarea id="notify_content" class="input-xlarge focused" rows="5" name="content"></textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">通知类型</label>
                        <div class="controls">
                            <select id="notify_type" class="input-xlarge focused" name="type">
                                <option value=>--请选择--</option>
                                <option value=99>群发通知</option>
                                <option value=14>重要通知 (可于天玑中查看) </option>
                            </select>
                        </div>
                    </div>
                    <div id ="file_1" class="control-group">
                        <label class="control-label" for="focusedInput">附件1</label>
                        <div class="controls">
                            <input type="file" name="file1">
                        </div>
                    </div>
                    <div id ="file_2" class="control-group">
                        <label class="control-label" for="focusedInput">附件2</label>
                        <div class="controls">
                            <input type="file" name="file2">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">适用角色:(必填)</label>
                        <div class="controls">
                            <#list applyRole  as data>
                                <input type="checkbox" id="d-p-r-${data.id!0}" name="role_${data.id!0}"
                                       class="apply-role" value="${data.id!0}"/>${data.roleName!""}&nbsp;&nbsp;
                            </#list>
                        </div>
                    </div>
                    <div class="control-group" id="group_region_tree">
                        <label class="control-label">通知部门</label>
                        <div id="groupTree" name="groupTree" class="controls" style="width: 280px;height: 300px">
                        </div>
                        <input type="hidden" name="groups" value="" id="groupIds">
                    </div>

                    <div class="form-actions">
                        <button id="send_notify_btn" type="button" class="btn btn-primary">保存</button>
                        <a class="btn" href="index.vpage"> 取消 </a>
                    </div>
                </fieldset>
            </form>
        </div>
    </div><!--/span-->
</div>
<input type="hidden" id="userId" value="${userId!}">

<script type="text/javascript">
$(function(){
    $("#groupTree").fancytree({
        extensions: ["filter"],
        source: {
            url: "/user/orgconfig/getNewDepartmentTree.vpage",
            cache:true
        },
        checkbox: true,
        selectMode: 3,

        init: function(event, data, flag) {
            var tree = $("#groupTree").fancytree("getTree");
            tree.visit(function(node){
                $("input[name='group']").each(function(){
                    if (node.key == $(this).attr("value")) {
                        node.setSelected(true);
                        node.setActive();
                    }
                });
            });
        }
    });

    $('#send_notify_btn').live('click',function(){
        var content = $('#notify_content').val().trim();
        var title = $('#notify_title').val().trim();
        var type = $('#notify_type').val().trim();
        var groups = [];

        var groupTree = $("#groupTree").fancytree("getTree");
        var groupNodes = groupTree.getSelectedNodes();

        $.map(groupNodes, function(node){
            if (node.data.type == 'group') {
                groups.push(node.key);
            }
        });

        var applyRole = "";
        $(".apply-role").each(function () {
            if ($("#" + this.id).attr("checked")) {
                applyRole += this.value + ","
            }
        });
        if (blankStringOrZero(applyRole)) {
            alert("请选择适应的角色");
            return false;
        }

        if(title == "") {
            alert("请输入通知题目！");
            return false;
        }

        if(content == "") {
            alert("请输入通知内容！");
            return false;
        }
        if (type  != 14 && type != 99) {
            alert("请选择通知类型！");
            return false;
        }
        if(groups.length == 0) {
            alert("请选择通知部门!");
            return false;
        }
        $('#groupIds').val(groups);
        $('#notify_form').submit();
    });

});

</script>

</@layout_default.page>