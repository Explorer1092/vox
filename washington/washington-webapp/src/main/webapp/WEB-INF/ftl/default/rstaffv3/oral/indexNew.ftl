<#import "../researchstaffv3.ftl" as com>
<@com.page menuIndex=22 menuType="normal">
<ul class="breadcrumb_vox">
    <li><a href="javascript:void(0);">组卷统考</a> <span class="divider">/</span></li>
    <li class="active">统考</li>
</ul>
<div class="r-mapResearch-box">
    <div class="r-table">
        <table id="oralTable">
        </table>
        <div class="system_message_page_list message_page_list" style="float:right; padding:0 0 10px;"></div>
    </div>
</div>
<script id="t:试卷列表" type="text/html">
    <tbody>
    <tr>
        <td>测试名称</td>
        <td>开始时间</td>
        <td>结束时间</td>
        <td>操作</td>
    </tr>
    <%
    if(paperMapper != null && paperMapper.length > 0){
       for(var i = 0; i < paperMapper.length; i++){
    %>
    <tr>
        <td><%=paperMapper[i].name%></td>
        <td><%=paperMapper[i].examStartAt%></td>
        <td><%=paperMapper[i].examStopAt%></td>
        <td class="text_blue paperName">
            <%
            if(paperMapper[i].resultIssueAt < date){
            %>
            <a style="text-decoration: underline;"
               href="/rstaff/oral/regionStatistic.vpage?exam_id=<%=paperMapper[i].id%>">查看统计
            </a>&nbsp;
            <a style="text-decoration: underline;"
               href="/rstaff/oral/loadStudentAchievement.vpage?exam_id=<%=paperMapper[i].id%>&type=system">下载系统成绩
            </a>&nbsp;
            <a style="text-decoration: underline;"
               href="/rstaff/oral/loadStudentAchievement.vpage?exam_id=<%=paperMapper[i].id%>&type=teacher">下载批改成绩
            </a>
            <%
            } else {
            %>
            成绩于 <%=paperMapper[i].resultIssueAt%> 后发布，敬请等待
            <%}%>
        </td>
    </tr>
    <%}}else{%>
    <tr>
        <td class="text_blue" colspan="4">暂无相关数据</td>
    </tr>
    <%}%>
    </tbody>
</script>


<script type="text/javascript">
    var paperOperate = {
        $targetTable: $("#oralTable"),
        dataList: [],
        page: 1,
        size: 10,
        tool: function (now) {
            var year = now.getFullYear();
            var month = (now.getMonth() + 1).toString();
            var day = (now.getDate()).toString();
            if (month.length == 1) {
                month = "0" + month;
            }

            if (day.length == 1) {
                day = "0" + day;
            }
            var hour = now.getHours() > 10 ? now.getHours() : "0" + now.getHours();
            var minutes = now.getMinutes() > 10 ? now.getMinutes() : "0" + now.getMinutes();
            return year + "-" + month + "-" + day + " " + hour + ":" + minutes;
        },

        //获取内容
        getContents: function () {
            if (paperOperate.dataList.length < 1) return [];
            var fromIndex = (paperOperate.page - 1) * paperOperate.size;
            var toIndex = paperOperate.page * paperOperate.size;
            if (toIndex > paperOperate.dataList.length) {
                toIndex = paperOperate.dataList.length;
            }
            if (fromIndex > toIndex) {
                fromIndex = toIndex;
            }
            return paperOperate.dataList.slice(fromIndex, toIndex);
        },
        //获取全部页数
        getTotalPages: function () {
            return Math.ceil(paperOperate.dataList.length / paperOperate.size);
        },
        //补充页面的信息
        pagePaper: function (page) {
            paperOperate.page = page;
            var date = paperOperate.tool(new Date());
            paperOperate.$targetTable.removeClass("pageLoding").empty().html(template("t:试卷列表", {paperMapper: paperOperate.getContents(),date:date}));
            var totalPages = paperOperate.getTotalPages();
            if (totalPages > 1) {
                $("div.system_message_page_list").show().page({
                    total: totalPages, current: page, jumpCallBack: paperOperate.pagePaper
                });
            } else {
                $("div.system_message_page_list").hide();
            }
        },
        //加载页数
        loadPaper: function (page) {
            page = page || 1;
            if (paperOperate.$targetTable.isFreezing()) {
                return false;
            }
            paperOperate.$targetTable.freezing();

            App.postJSON("/rstaff/oral/oralpaperNewData.vpage", {}, function (data) {
                paperOperate.$targetTable.thaw();//thaw()是什么函数
                var dataList = data.refList || [];
                paperOperate.dataList = []; //清空数据
                for (var i = 0; i < dataList.length; i++) {
                    dataList[i]["examStartAt"] = paperOperate.tool(new Date(Number(dataList[i]["examStartAt"])));
                    dataList[i]["examStopAt"] = paperOperate.tool(new Date(Number(dataList[i]["examStopAt"])));
                    dataList[i]["resultIssueAt"] = paperOperate.tool(new Date(Number(dataList[i]["resultIssueAt"])));
                }
                paperOperate.dataList = $.merge(paperOperate.dataList, dataList);
                paperOperate.pagePaper(page);
            });
        },
        init: function () {
            paperOperate.$targetTable.addClass("pageLoding");
            paperOperate.loadPaper(1);
        }
    };
    $(function () {
        paperOperate.init();
    });

</script>




</@com.page>