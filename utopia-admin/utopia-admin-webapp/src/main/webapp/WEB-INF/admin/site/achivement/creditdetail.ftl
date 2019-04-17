<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="Advertisement Management" page_num=4>
<link  href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<div id="main_container" class="span9">
    <legend>
        <a href="index.vpage" style="color: #0C0C0C">学分体系管理</a>&nbsp;&nbsp;&nbsp;&nbsp;
        <a href="search_credit.vpage" style="color: #0C0C0C">学分详情查询</a>&nbsp;&nbsp;&nbsp;&nbsp;
        <a href="credit_logs.vpage">学分记录查询</a>&nbsp;&nbsp;&nbsp;&nbsp;
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="frm" class="form-horizontal" method="post" action="credit_logs.vpage" onsubmit="return verify()">
                    <input type="hidden" id="pageNumber" name="pageNumber" value="${pageNumber!''}"/>
                    学生ID：
                    <input type="text" id="sid" name="sid" value="${sid!''}"/>
                    &nbsp;&nbsp;
                    创建时间：
                    <input id="startDate" type="text" class="input-small" placeholder="开始时间" name="startDate" value="${startDate!}">~
                    <input id="endDate" type="text" class="input-small" placeholder="结束时间" name="endDate" value="${endDate!}">
                    &nbsp;&nbsp;&nbsp;&nbsp;
                    <button type="submit" class="btn btn-primary">查询</button>
                    <a style="float: right;" class="btn btn-warning" id="exportCredit" name="export_credit" download>导出历史记录</a>
                </form>
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>学校ID</td>
                        <td>学校名称</td>
                        <td>班级ID</td>
                        <td>班级名称</td>
                        <td>学生ID</td>
                        <td>学生姓名</td>
                        <td>学分来源</td>
                        <td>获得学分</td>
                        <td>获取时间</td>
                    </tr>
                    <#if achivementlogs?? >
                        <#list achivementlogs as al >
                            <tr>
                                <td>${al.scid!'-'}</td>
                                <td>${al.schoolName!'-'}</td>
                                <td>${al.cid!'-'}</td>
                                <td>${al.clazzName!'-'}</td>
                                <td>${al.sid!'-'}</td>
                                <td>${al.userName!'-'}</td>
                                <td>${al.creditSource!'-'}</td>
                                <td>${al.credit!'-'}</td>
                                <td>${al.createTime!?string('yyyy-MM-dd HH:mm:ss')!}</td>
                            </tr>
                        </#list>
                    </#if>
                </table>
                <ul class="pager">
                    <#if (hasPrevious!)>
                        <li><a href="javascript::void()" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
                    <#else>
                        <li class="disabled"><a href="javascript::void()">上一页</a></li>
                    </#if>
                    <#if (hasNext!)>
                        <li><a href="javascript::void()" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
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
<script type="text/javascript">

    $('#startDate').val('${startDate!''}');
    $('#endDate').val('${endDate!''}');

    function pagePost(pageNumber){
        $("#pageNumber").val(pageNumber);
        $("#frm").submit();
    }

    function verify() {
        var sid = $("#sid").val();
        if (null == sid || '' == sid) {
            alert("请输入学生ID！")
            return false;
        }
        var startDate = $("#startDate").val();
        if (null == startDate || '' == startDate) {
            alert("请选择开始日期！")
            return false;
        }
        var endDate = $("#endDate").val();
        if (null == endDate || '' == endDate) {
            alert("请选择结束日期！")
            return false;
        }
        var startTime = new Date(startDate.replace(/\-/g, "\/"));
        var endTime = new Date(endDate.replace(/\-/g, "\/"));
        if (startTime > endTime) {
            alert("开始日期不允许大于结束日期！");
            return false;
        }
        var days = parseInt(diffTime(startTime, endTime));
        if (days > 185) {
            alert("开始结束日期大于一个学期！");
            return false;
        }
    }

    $("a[name='export_credit']").on("click", function () {
        var sid = $("#sid").val();
        if (null == sid || '' == sid) {
            alert("请输入学生ID！")
            return false;
        }
        var startDate = $("#startDate").val();
        if (null == startDate || '' == startDate) {
            alert("请选择开始日期！")
            return false;
        }
        var endDate = $("#endDate").val();
        if (null == endDate || '' == endDate) {
            alert("请选择结束日期！")
            return false;
        }
        var startTime = new Date(startDate.replace(/\-/g, "\/"));
        var endTime = new Date(endDate.replace(/\-/g, "\/"));
        if (startTime > endTime) {
            alert("开始日期不允许大于结束日期！");
            return false;
        }
        var days = parseInt(diffTime(startTime, endTime));
        if (days > 185) {
            alert("开始结束日期大于一个学期！");
            return false;
        }
        location.href = '/site/achivement/export/credit_log.vpage?sid=' + sid + "&startDate=" + startDate + "&endDate=" + endDate;
    });

    function diffTime(startDate, endDate) {
        //时间差的毫秒数
        var diff = endDate.getTime() - startDate.getTime();
        //计算出相差天数
        var days = Math.floor(diff/(24*3600*1000));
        return days;
    }

    $(function(){

        $("#startDate").datepicker({
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
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-prompts-alert.js"></script>
</@layout_default.page>