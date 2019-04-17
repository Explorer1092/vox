<#include "../index.ftl" />

<script type="text/javascript">
$("select").change(function () {
    var str = $("select option:selected").val();
    $('#department_name').val(str);
    $('form').submit();
});
</script>

<div class="container">
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <legend>${pageMessage!''}列表： {% if admin_depart_w %}<a class="btn btn-primary pull-right" href="new.vpage">添加新${pageMessage!''}</a>{% endif %}</legend>

                <form class="form-horizontal">
                    <select>
                        <option value="">All</option>
                        <#list departmentList as departmentInfo>
                        <option value="${departmentInfo.name!}" <#if departmentInfo.name == departmentName > selected="selected" </#if>>${departmentInfo.description!''}</option>
                        </#list>
                    </select>
                    <input type="hidden" class="input-small" placeholder="Depart" name="department_name" value="{{ request.GET.d }}" id="d">
                    <input type="text" class="input-small" placeholder="Group" name="s" value="{{ request.GET.s }}">
                    <button type="submit" class="btn">查找</button>
                </form>
                <table class="table table-striped table-bordered">
                    <tr>
                        <td width=20%>所属业务系统</td>
                        <td width=30%>权限组ID</td>
                        <td>权限组名称</td>
                        <td>成员</td>
                    </tr>
                    {% for group in groups %}
                    <tr>
                        <td>{{ group.app }}</td>
                        <td>{{ group.name }}</td>
                        <td>{{ group.description.0 }}</td>
                        <td>{{ group.memberUid }}</td>
                    </tr>
                    {% endfor %}
                </table>
            </div>
        </div>
    </div>
</div>
