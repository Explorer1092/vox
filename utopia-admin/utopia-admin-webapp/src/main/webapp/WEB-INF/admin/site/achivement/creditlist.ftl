<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="Advertisement Management" page_num=4>
<link  href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<div id="main_container" class="span9">
    <legend>
        <a href="index.vpage" style="color: #0C0C0C">学分体系管理</a>&nbsp;&nbsp;&nbsp;&nbsp;
        <a href="search_credit.vpage">学分详情查询</a>&nbsp;&nbsp;&nbsp;&nbsp;
        <a href="credit_logs.vpage" style="color: #0C0C0C">学分记录查询</a>&nbsp;&nbsp;&nbsp;&nbsp;
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="frm" id="checkStatus" class="form-horizontal" method="post" action="search_credit.vpage" >
                    <input type="hidden" id="pageNumber" name="pageNumber" value="${pageNumber!''}"/>
                    学生ID：
                    <input id="sid" name="sid" value="${sid!''}" autocomplete="true"/>
                    &nbsp;&nbsp;
                    <input type="checkbox" class="approved" id="status" name="status" value="0" <#if status==0>checked</#if>>
                    个人学分
                    &nbsp;&nbsp;
                    <input type="checkbox" class="approved" id="status" name="status" value="1" <#if status==1>checked</#if>>
                    班级学分
                    &nbsp;&nbsp;&nbsp;&nbsp;
                    <button type="submit" class="btn btn-primary">查询</button>
                    <a style="float: right;" class="btn btn-warning" id="exportCredit" name="export_credit" download>导出学分</a>
                </form>
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>学校ID</td>
                        <td>学校名称</td>
                        <td>班级ID</td>
                        <td>班级名称</td>
                        <td>学生ID</td>
                        <td>学生姓名</td>
                        <td>本周学分</td>
                        <td>本期总学分</td>
                        <td>本周英语学分</td>
                        <td>本周数学学分</td>
                        <td>本周语文学分</td>
                        <td style="width: 90px;">操作</td>
                    </tr>
                    <#if creditList?? >
                        <#list creditList as al >
                            <tr>
                                <td>${al.scid!}</td>
                                <td>${al.schoolName!}</td>
                                <td>${al.cid!}</td>
                                <td>${al.clazzName!}</td>
                                <td>${al.sid!}</td>
                                <td>${al.userName!}</td>
                                <td>${al.proCredit!}</td>
                                <td>${al.totalCredit!}</td>
                                <td>${al.proEngCredit!}</td>
                                <td>${al.proMathCredit!}</td>
                                <td>${al.proChineseCredit!}</td>
                                <td>
                                    <a class="btn btn-success" href="credit_logs.vpage?sid=${al.sid!}">学分详情</a>
                                </td>
                            </tr>
                        </#list>
                    </#if>
                </table>
                <ul class="pager">
                    <#if (hasPrevious!)>
                        <li><a href="javascript::void()" onclick="pagePost(${pageNumber-1}, ${sid!''})" title="Pre">上一页</a></li>
                    <#else>
                        <li class="disabled"><a href="javascript::void()">上一页</a></li>
                    </#if>
                    <#if (hasNext!)>
                        <li><a href="javascript::void()" onclick="pagePost(${pageNumber+1}, ${sid!''})" title="Next">下一页</a></li>
                    <#else>
                        <li class="disabled"><a href="javascript::void()">下一页</a></li>
                    </#if>
                    <li>当前第 ${pageNumber!} 页 |</li>
                    <li>共 ${totalPages!} 页</li>
                </ul>
            </div>
        </div>
    </div>
</div>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-prompts-alert.js"></script>
<script type="text/javascript">
    function pagePost(pageNumber, sid){
        $("#sid").val(sid);
        $("#pageNumber").val(pageNumber);
        $("#frm").submit();
    }

    $("input:checkbox").on('click', function () {
        var $box = $(this);
        if ($box.is(":checked")) {
            var group = "input:checkbox[name='" + $box.attr("name") + "']";
            $(group).prop("checked", false);
            $box.prop("checked", true);
        } else {
            $box.prop("checked", false);
        }
    })

    $("a[name='export_credit']").on("click", function () {
        var sid = $("#sid").val();
        if (null == sid || '' == sid) {
            alert("请输入学生ID!")
            return false;
        }
        location.href = '/site/achivement/export/credit_excel.vpage?sid=' + sid;
    });
</script>
</@layout_default.page>