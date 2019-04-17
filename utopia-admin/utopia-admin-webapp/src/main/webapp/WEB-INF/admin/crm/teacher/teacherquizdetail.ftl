<#-- @ftlvariable name="day" type="java.lang.Integer" -->
<#-- @ftlvariable name="userId" type="java.lang.Long" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div>
        <form id="frm" class="form-horizontal" method="post" action="${requestContext.webAppContextPath}/crm/teacher/teacherquizdetail.vpage">
            <input type="hidden" id="pageNumber" name="pageNumber" value="1">
            <input type="hidden" id="userId" name="userId" value="${userId!''}">
        </form>
        <ul class="pager">
            <#if (quizs.hasPrevious())>
                <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
            <#else>
                <li class="disabled"><a href="#">上一页</a></li>
            </#if>
            <#if (quizs.hasNext())>
                <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
            <#else>
                <li class="disabled"><a href="#">下一页</a></li>
            </#if>
            <li>当前第 ${pageNumber!} 页 |</li>
            <li>共 ${quizs.totalPages!} 页</li>
        </ul>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 测验ID</th>
                <th> 教师ID</th>
                <th> 班/组ID</th>
                <th> 检查时间</th>
                <th> 班级名称</th>
                <th> 学生总人数</th>
                <th> 完成人数</th>
                <th> 是否作弊</th>
                <th> 消耗学豆</th>
            </tr>
            <#if quizs?has_content>
                <#list quizs.content as quiz>
                    <tr>
                        <td>${quiz.quizId!''}</td>
                        <td>
                            <a href="../user/userhomepage.vpage?userId=${quiz.teacherId!""}"> ${quiz.teacherId!""}</a>
                        </td>
                        <td>${quiz.clazzId!""}/${quiz.groupId!""}</td>
                        <td>${quiz.checkDate!""}</td>
                        <td>${quiz.clazzName!""}(${quiz.clazzId!""})</td>
                        <td>${quiz.studentCount!""}</td>
                        <td>${quiz.completeCount!""}</td>
                        <#if quiz.cheat>
                            <td>作弊
                                <#if quiz.possibleCheat??>
                                    <i class=" icon-exclamation-sign" title="${quiz.possibleCheat.reason!''}"></i>
                                    <#if quiz.possibleCheat.isAddIntegral?has_content && quiz.possibleCheat.isAddIntegral>
                                        已补加
                                    <#else>
                                        <a class="add_Integral" href="javascript:void(0)"
                                           data-homework_id="${quiz.possibleCheat.id!}">补加</a>
                                    </#if>
                                </#if>
                            </td>
                        <#else>
                            <td>未作弊</td>
                        </#if>

                        <td>
                            <#if !quiz.isOralQuiz>
                                <a name="usedPrize" role="button"
                                   data-homework_id="${quiz.quizId!}" data-homework_subject="${quiz.homeworkSubject!}"
                                   class="btn btn-primary">查看</a>
                            </#if>
                        </td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>

<script>
    function pagePost(pageNumber) {
        $("#pageNumber").val(pageNumber);
        $("#frm").submit();
    }

    $(function () {
        $(".add_Integral").on("click", function () {
            if (confirm("确定为该测验补加金银币吗？")) {
                var $this = $(this);
                var postData = {
                    cheateId: $this.data('homework_id')
                };
                $.ajax({
                    type: 'post',
                    url: 'addIntegral.vpage',
                    data: postData,
                    success: function (data) {
                        if (data.success) {
                            window.location.href = "teacherquizdetail.vpage?userId=${userId!''}";
                        } else {
                            alert("补加失败，请联系管理员")
                        }
                    }
                });
            }
        });

        $("a[name='usedPrize']").on("click", function () {
            var homeworkId = $(this).attr("data-homework_id");
            var homeworkType = $(this).attr("data-homework_subject");
            var item = $(this);
            $.ajax({
                type: "get",
                url: "getusedhomeworkprize.vpage",
                data: {
                    homeworkId: homeworkId,
                    homeworkType: homeworkType
                },
                success: function (data) {
                    if (data.success) {
                        item.before(data.usedprize);
                    } else {
                        item.before(0);
                    }
                    item.remove();
                }
            });
        });
    });
</script>
</@layout_default.page>