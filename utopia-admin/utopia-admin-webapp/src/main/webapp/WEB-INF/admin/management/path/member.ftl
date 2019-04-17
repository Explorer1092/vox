<#include "../index.ftl" />
{% block jq %}
var arrayObj = new Array();

$('html').ajaxStart(function() {
$(':input').attr("disabled","disabled");
$('.submitok').text('处理中...');
}).ajaxStop(function(){
$('.submitok').text('保存');
});

function arrayget() {
if (arrayObj.length > 0) {
var get_url = arrayObj.shift();
$.get(get_url).complete(function(){ arrayget();});
}else{
//sleep(3000);
location.href = "/save_ok/";
}
}

$('.submitok').click( function () {
arrayget();
});

$('.role').click( function () {
var status = $(this).is(':checked');
if (status) {
arrayObj.push('/jman/path/role/member/do/?do=add&role={{ info.dn }}&group=' + $(this).val());
}else{
arrayObj.push('/jman/path/role/member/do/?do=del&role={{ info.dn }}&group=' + $(this).val());
}
});

$('#all_add').click( function () {
var status = $(this).is(':checked');
var dolist =  $('.role:not(:checked)');
if (status) {
$('#all_del').removeAttr('checked');
dolist.attr('checked','').click();
dolist.attr('checked','');
}
});

$('#all_del').click( function () {
var status = $(this).is(':checked');
var dolist = $('.role:checked')
if (status) {
$('#all_add').removeAttr('checked');
dolist.removeAttr('checked').click();
dolist.removeAttr('checked');
}
});
{% endblock %}

<script type="text/javascript">
    function de_confirm(){
        if ( ! confirm("确认删除吗?")){
            return false;
        }
    }
</script>

<div class="container">
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <legend>${pageMessage!}：</legend>
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>角色标识</td>
                        <td>角色组名称</td>
                        <td>所属路径</td>
                        <td>所属业务系统</td>
                        <td>操作</td>
                    </tr>
                    <tr>
                        <td>{{ info.cn.0 }}</td>
                        <td>{{ info.description.0 }}</td>
                        <td>{{ info.path }}</td>
                        <td>{{ info.app }}</td>
                        <td>
                            {% if info.app in admin_apps_w %}<a href="role_edit.vpage?dn={{ info.dn }}" class="btn btn-mini">修改</a> {% endif %}
                            {% if info.app in admin_apps_x %}<a href="del/?dn={{ info.dn }}" class="btn btn-mini btn-danger" onclick="javascript:return de_confirm();">删除</a>{% endif %}
                        </td>
                    </tr>
                </table>

                <form class="form-horizontal">
                    <input type="text" class="input-small" placeholder="Group" name="s" value="{{ request.GET.s|default_if_none:"auth" }}">
                    <input type="hidden" name="dn" value="{{ request.GET.dn }}">
                    <button type="submit" class="btn">查找</button>
                </form>
                <button type="button" class="btn btn-info pull-right submitok" style="margin-bottom: 21px">保存</button>
                <table class="table table-striped table-bordered">
                    <tr>
                        <td></td>
                        <td>所属业务系统</td>
                        <td>权限组ID</td>
                        <td>权限组名称</td>
                        <td class="form-inline">
                                <span class="pull-right">
                                    <label class="checkbox"><input type="checkbox" id="all_add"> 全选</label>
                                    <label class="checkbox"><input type="checkbox" id="all_del"> 取消全部</label>
                                </span>
                        </td>
                    </tr>
                    {% for one in GroupList %}
                    <tr>
                        <td>{{ forloop.counter }}</td>
                        <td>{{ info.app }}</td>
                        <td>{{ one.entry.cn.0 }}</td>
                        <td>{{ one.entry.description.0 }}</td>
                        <td class="form-inline">
                            <label class="checkbox">
                                <input type="checkbox" value="{{ one.dn }}" class="role" {% if one.entry.check %}checked{% endif %}>
                            </label>
                        </td>
                    </tr>
                    {% endfor %}
                </table>
                <button type="button" class="btn btn-info pull-right submitok">保存</button> <br>
            </div>
        </div>
    </div>
</div>
