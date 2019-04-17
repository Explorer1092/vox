<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<style>
    span { font: "arial"; }
</style>
<div id="main_container" class="span9">
    <div>
        <form id="s_form" action="${requestContext.webAppContextPath}/site/cheat/index.vpage" method="post" class="form-horizontal">
            <input type="hidden" id="pageNumber" name="pageNumber" value="${pageNumber!''}"/>
            <fieldset>
                <legend>作弊查询</legend>
            </fieldset>
            <ul class="inline">
                <li>
                    开始时间： <input id="beginDate" name="startDate" value="${startDate!''}" class="input-medium" type="text" placeholder="例：2014-07-07">

                </li>
                <li>
                    结束时间： <input id="endDate" name="endDate" value="${endDate!''}" class="input-medium" type="text" placeholder="例：2014-07-08">
                </li>
                <li>
                    <button id="selectTable" type="submit" class="btn btn-primary">查 询</button>
                </li>
            </ul>
        </form>
    </div>
    <ul class="pager">
        <#if (commentPage.hasPrevious())>
            <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
        <#else>
            <li class="disabled"><a href="#">上一页</a></li>
        </#if>
        <#if (commentPage.hasNext())>
            <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
        <#else>
            <li class="disabled"><a href="#">下一页</a></li>
        </#if>
        <li>当前第 ${pageNumber!} 页 |</li>
        <li>共 ${commentPage.totalPages!} 页</li>
    </ul>
    <div id="data_table_journal">
        <table class="table table-striped table-bordered so_checkboxs" so_checkboxs_values="">
            <tr>
                <td>学校</td>
                <td>老师姓名</td>
                <td>班级</td>
                <td>作业ID</td>
                <td>作业类型</td>
                <td>作弊原因</td>
                <td>园丁豆已经冻结</td>
                <td>日期</td>
            </tr>
            <#if commentPage.content?? >
                <#list commentPage.content as comment >
                    <tr>
                        <td>${comment.schoolName!}</td>
                        <td><a href="/crm/teacher/teacherhomepage.vpage?teacherId=${comment.teacherId!""}">${comment.teacherName!}</a></td>
                        <td>${comment.clazzId!}</td>
                        <td>
                            <#if comment.homeworkSubject?? && (comment.homeworkSubject=='QUIZ_ENGLISH' || comment.homeworkSubject=='QUIZ_MATH')>
                                ${comment.homeworkId!""}
                            <#else>
                                <a href="/crm/homework/homeworkhomepage.vpage?homeworkId=${comment.homeworkId!}&homeworkSubject=${comment.homeworkSubject!}">${comment.homeworkId!""}</a>
                            </#if>
                        </td>
                        <td>${comment.homeworkSubject!}</td>
                        <td>${comment.reason!}</td>
                        <td>${(comment.freeze)?string("是", "否")}</td>
                        <td>${comment.date!}</td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>

<script>
    function pagePost(pageNumber){
        $("#pageNumber").val(pageNumber);
        $("#s_form").submit();
    }

    $(function() {
        $("#beginDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        $("#endDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });
    });

</script>
</@layout_default.page>