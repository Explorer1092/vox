<#-- @ftlvariable name="productTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<style>
    span {
        font: "arial";
    }
</style>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend>校园大使申请审核（两周内数据）</legend>
        </fieldset>
    </div>
    <div id="data_table">
        <table class="table table-hover table-striped table-bordered">
            <tr>
                <th>老师ID</th>
                <th>老师姓名</th>
                <th>认证时间</th>
                <th>等级</th>
                <th>班级数量</th>
                <th>学生数量</th>
                <th>学校ID</th>
                <th>作业历史</th>
                <th>操作</th>
            </tr>
            <#if dataList??>
                <#list dataList as data>
                    <tr>
                        <th><a href="/crm/user/userhomepage.vpage?userId=${data.userId!""}">${data.userId!}</a></th>
                        <td>${data.userName!}</td>
                        <td>${data.verifyTime!}</td>
                        <td>${data.userLevel!}</td>
                        <td>${data.clazzCount!}</td>
                        <td>${data.studentCount!}</td>
                        <td><a href="/crm//school/schoolhomepage.vpage?schoolId=${data.schoolId!}">${data.schoolId!}</a>
                        </td>
                        <td><a href="/crm/teacher/teacherhomeworkdetail.vpage?userId=${data.userId!''}&day=30">查看作业历史</a>
                        </td>
                        <td>
                            <a name="setAmbassador" data-id="${data.userId!}" role="button" class="btn btn-success">任命</a>
                        </td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>

<script>
    $(function () {
        $("a[name='setAmbassador']").on('click', function () {
            var teacherId = $(this).attr("data-id");
            if(confirm("确定将这个老师设置为校园大使吗？")){
                $.post("setambassador.vpage", {teacherId: teacherId}, function (data) {
                    if (data.success) {
                        alert(data.info);
                        setTimeout(function () {
                            location.reload();
                        }, 200);
                    } else {
                        alert(data.info);
                    }
                });
            }
        });
    });

</script>
</@layout_default.page>