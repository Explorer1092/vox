<#-- @ftlvariable name="feedbackType" type="java.lang.String" -->
<#-- @ftlvariable name="feedbackTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="startDate" type="java.lang.String" -->
<#-- @ftlvariable name="content" type="java.lang.String" -->
<#-- @ftlvariable name="unmask" type="java.lang.String" -->
<#-- @ftlvariable name="feedbackQuickReplyList" type="java.util.List" -->
<#-- @ftlvariable name="feedbackStateMap" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="userId" type="java.lang.Long" -->
<#-- @ftlvariable name="feedbackInfoList" type="java.util.List<java.util.Map>" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div class="span9">
    <div>
        <fieldset>
            <legend>
                <a href="/crm/feedback/feedbackindex.vpage">用户反馈查询</a>&nbsp;
                用户反馈Tag管理&nbsp;
                <a href="/crm/feedback/watcher/index.vpage">用户反馈跟踪者管理</a>
            </legend>
        </fieldset>
    </div>
    <div>
        <form method="post" action="?" class="form-horizontal">
            <ul class="inline">
                <li>
                    <label for="watcher">
                        跟踪者
                        <select name="watcher" id="watcher_filter">
                            <option value='-1' selected="selected">全部</option>
                            <#if watchersWithTag?has_content>
                                <#list watchersWithTag as watcher>
                                    <option value="${watcher.WATCHER_NAME!}">${watcher.WATCHER_NAME!}</option>
                                </#list>
                            </#if>
                        </select>
                    </label>
                </li>
                <li>
                    <button type="submit" class="btn btn-success">查询</button>
                </li>
            </ul>
        </form>
    </div>
    <div>
        <div>
            <ul class="inline">
                <li>
                    <button class="btn btn-primary" id="add_tag_button">添加tag</button>
                </li>
            </ul>
        </div>
        <fieldset><legend>查询结果</legend></fieldset>
        <br/>
        <table id="tagList" class="table table-striped table-bordered" style="font-size: 14px;">
            <thead>
            <tr>
                <th>Tag名称</th>
                <th>Tag跟踪者</th>
                <th>创建时间</th>
                <th>操作</th>
            </tr>
            </thead>
            <#list tags as tag>
                <tr id="tag_info_${tag.id!}">
                    <td id="tag_name_${tag.id!}" style="width: 90px;max-width:90px;">
                        ${tag.name!}
                    </td>
                    <td id="tag_watcherName_${tag.id!}" nowrap style="width: 90px;max-width:90px;">
                        ${tag.watcherName!}
                    </td>
                    <td nowrap style="width: 90px;max-width:90px;">
                        ${tag.createDatetime!}
                    </td>
                    <td nowrap style="width: 90px;max-width:90px;">
                        <a id="del_tag_${tag.id!}" href="javascript:void(0);">删除</a>&nbsp;<a href="javascript:void(0);" onclick=loadEditDialog(${tag.id!});>编辑</a>
                    </td>
                </tr>
            </#list>
        </table>
    </div>
</div>
<div id="add_tag_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>添加tag</h3>
    </div>
    <div class="modal-body">
        <div class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dl>
                        <dt>名称：</dt>
                        <dd>
                            <label><input type="text" id="tag_name" style="border: solid 1px #ccc; border-radius:5px; width:200px; padding: 7px 6px; font:14px/120% '微软雅黑', 'Microsoft YaHei', Arial, '黑体'; color:#666; vertical-align:middle; outline: none; box-shadow:1px 1px 2px #eee inset;"/></label>
                        </dd>
                    </dl>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dl>
                        <dt>请选择观察者</dt>
                        <dd>
                            <label>
                                <select id="tag_select_watcher">
                                    <#if watchers?has_content>
                                        <#list watchers as watcher>
                                            <option value="${watcher.watcherName!}">${watcher.watcherName!}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </label>
                        </dd>
                    </dl>
                </li>
            </ul>
        </div>
    </div>
    <div class="modal-footer">
        <button id="add_tag_dialog_btn_ok" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="edit_tag_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>编辑tag</h3>
    </div>
    <div class="modal-body">
        <div class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dl>
                        <dt>名称：</dt>
                        <dd>
                            <label><input type="text" id="edit_tag_name" value="" style="border: solid 1px #ccc; border-radius:5px; width:200px; padding: 7px 6px; font:14px/120% '微软雅黑', 'Microsoft YaHei', Arial, '黑体'; color:#666; vertical-align:middle; outline: none; box-shadow:1px 1px 2px #eee inset;"/></label>
                        </dd>
                    </dl>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dl>
                        <dt>请选择观察者</dt>
                        <dd>
                            <label>
                                <select id="tag_edit_watcher">
                                    <#if watchers?has_content>
                                        <#list watchers as watcher>
                                            <option value="${watcher.watcherName!}">${watcher.watcherName!}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </label>
                        </dd>
                    </dl>
                </li>
            </ul>
        </div>
    </div>
    <div class="modal-footer">
        <button id="edit_tag_dialog_btn_ok" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
    <input type="hidden" id="curid" value=""/>
</div>
<script type="text/javascript">
Date.prototype.format = function(format){
    var o = {
        "M+" : this.getMonth()+1, //month
        "d+" : this.getDate(), //day
        "h+" : this.getHours(), //hour
        "m+" : this.getMinutes(), //minute
        "s+" : this.getSeconds(), //second
        "q+" : Math.floor((this.getMonth()+3)/3), //quarter
        "S" : this.getMilliseconds() //millisecond
    }

    if(/(y+)/.test(format)) {
        format = format.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
    }

    for(var k in o) {
        if(new RegExp("("+ k +")").test(format)) {
            format = format.replace(RegExp.$1, RegExp.$1.length==1 ? o[k] : ("00"+ o[k]).substr((""+ o[k]).length));
        }
    }
    return format;
}
function loadEditDialog(id){
    $('#edit_tag_dialog').modal('show');
    $('#edit_tag_name').val($('#tag_name_'+id).html().trim());
    $('#curid').val(id);
    $("#tag_edit_watcher").attr("value",$('#tag_watcherName_'+id).html().trim());
}
$(function(){
    $('[id^="del_tag_"]').on('click', function(){
        var id = $(this).attr('id').substring("del_tag_".length);
        $.post('deltag.vpage',{
            id:id
        },function(data){
            if(!data.success){
                alert(data.info);
            }else{
                $('#tag_info_'+id).remove();
            }
        });
    });

    $('#add_tag_button').on('click',function(){
        $('#add_tag_dialog').modal('show');
    });

    $('#add_tag_dialog_btn_ok').on('click',function(){

        var tagName = $('#tag_name').val();
        if(tagName == ''){
            alert("请输入tag名称");
            return false;
        }
        var watcher = $('#tag_select_watcher').find('option:selected').val();
        $.post('addtag.vpage',{
            name:tagName,
            watcher:watcher
        },function(data){
            if(!data.success){
                alert(data.info);
            }else{
                var str = "<tr id=\"tag_info_"+data.value.id+"\"><td id=\"tag_name_"+data.value.id+"\" style=\"width: 90px;max-width:90px;\">";
                str += data.value.name;
                str += "</td>";
                str += "<td id=\"tag_watcherName_"+data.value.id+"\" style=\"width: 90px;max-width:90px;\">";
                str += data.value.watcherName;
                str += "</td>";
                str += "<td nowrap style=\"width: 90px;max-width:90px;\">";
                str += new Date(data.value.createDatetime).format("yyyy-MM-dd hh:mm:ss");
                str += "</td>";
                str += "<td nowrap style=\"width: 90px;max-width:90px;\">";
                str += "<a id=\"del_tag_" + data.value.id + "\" href=\"javascript:void(0);\">删除</a>&nbsp;";
                str += "<a href=\"javascript:void(0);\" onclick=loadEditDialog("+data.value.id+");>编辑</a>";
                str += "</td></tr>";
                $('#tagList').append(str);
                $("#del_tag_"+data.value.id).on('click',function(){
                    var id = data.value.id;
                    $.post('deltag.vpage',{
                        id:id
                    },function(data){
                        if(!data.success){
                            alert(data.info);
                        }else{
                            $('#tag_info_'+id).remove();
                        }
                    });
                });
            }
            $('#add_tag_dialog').modal('hide');
        });
    });

    function editTag(){
        var id = $('#curid').val();
        var name = $('#edit_tag_name').val();
        var watcher = $('#tag_edit_watcher').find('option:selected').val();
        if(name == ''){
            alert("请输入tag名称");
            return false;
        }
        $.post('edittag.vpage',{
            id:id,
            name:name,
            watcher:watcher
        },function(data){
            if(!data.success){
                alert(data.info);
            }else{
                $('#tag_watcherName_'+id).html(watcher);
                $('#tag_name_'+id).html(name);
            }
            $('#edit_tag_dialog').modal('hide');
        });
    }

    $('#edit_tag_dialog_btn_ok').on('click',editTag);

});
var sltData = {
    no_results_text: "没有符合您要找的Tag！",
    disable_search_threshold: 10,
    width: "300px"
};

</script>
</@layout_default.page>