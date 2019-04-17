<#import "../researchstaffv3.ftl" as com>
<@com.page menuIndex=22 menuType="normal">
<ul class="breadcrumb_vox">
    <li><a href="/rstaff/oral/index.vpage">组卷统考</a> <span class="divider">/</span></li>
    <li class="active">班级统考</li>
    <li style="float: right"><a href="/rstaff/oral/schoolStatistic.vpage?county_id=${county_id}&paper_id=${paper_id}&exam_id=${exam_id}">返回</a></li>
</ul>
<div class="r-mapResearch-box">
    <div style="text-align: left">
        试卷类型：<select id="selectPaperId" name="selectPaper" class="selectPaper">
        <option value="">全部</option>
        <#if papers?? && papers?has_content>
            <#list papers as p>
                <option value="${p.paperId}">${p.paperName}</option>
            </#list>
        </#if>
    </select>
        <a id="btn-query" href="javascript:void(0);" class="btn_vox btn_vox_small">
            查询
        </a>
    </div>
    <div class="r-table">
        <table id="oralTable">
        </table>
        <div class="system_message_page_list message_page_list" style="float:right; padding:0 0 10px;"></div>
    </div>
</div>

<script id="t:班级统计数据" type="text/html">
    <tbody>
    <%
    if(paperMapper != null && paperMapper.length > 0){
    %>
    <tr>
        <td>班级名称</td>
        <td>实考人数</td>
        <td>平均分数</td>
        <td>平均答题时长</td>
        <td>最高分</td>
        <td>最低分</td>
        <% if(paperMapper != null && paperMapper.length > 0){
        for(var i=1;i<=(paperMapper[0].ranks!=undefined?paperMapper[0].ranks.length:0);i++){
        %>
        <td><%=paperMapper[0].ranks[i-1].rank_name%></td>
        <%}}%>

        <% if(paperMapper != null && paperMapper.length > 0){
        for(var i=1;i<=(paperMapper[0].parts!=undefined?paperMapper[0].parts.length:0);i++){
        %>
        <td><%=paperMapper[0].parts[i-1].part_name%>平均分</td>
        <%}}%>
    </tr>
    <%}%>
    <%
    if(paperMapper != null && paperMapper.length > 0){
    for(var i = 0; i < paperMapper.length; i++){
    %>
    <tr>
        <td><%=paperMapper[i].clazzName%></td>
        <td><%=paperMapper[i].realStuNum%></td>
        <td><%=paperMapper[i].avgScore%></td>
        <td><%=paperMapper[i].avgDuration%></td>
        <td><%=paperMapper[i].maxScore%></td>
        <td><%=paperMapper[i].minScore%></td>
        <%
        for(var j=1;j<=(paperMapper[0].ranks!=undefined?paperMapper[0].ranks.length:0);j++){
        %>
        <td><%=paperMapper[i].ranks[j-1].ranks_rat%></td>
        <%}%>

        <%for(var j=1;j<=(paperMapper[0].parts!=undefined?paperMapper[0].parts.length:0);j++){%>
        <td><%=paperMapper[i].parts[j-1].part_avg_score%></td>
        <%}%>
    </tr>
    <%}}else{%>
    <tr>
        <td class="text_blue" colspan="9">暂无相关数据</td>
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
        getTotalPages: function () {
            return Math.ceil(paperOperate.dataList.length / paperOperate.size);
        },
        pagePaper: function (page) {
            paperOperate.page = page;
            paperOperate.$targetTable.removeClass("pageLoding").empty().html(template("t:班级统计数据", {paperMapper: paperOperate.getContents()}));
            var totalPages = paperOperate.getTotalPages();
            if (totalPages > 1) {
                $("div.system_message_page_list").show().page({
                    total: totalPages, current: page, jumpCallBack: paperOperate.pagePaper
                });
            } else {
                $("div.system_message_page_list").hide();
            }
        },
        loadPaper: function (page) {
            page = page || 1;
            if (paperOperate.$targetTable.isFreezing()) {
                return false;
            }
            paperOperate.$targetTable.freezing();
            var postData = {
                exam_id: "${exam_id}",
                school_id: "${school_id}",
                paper_id: $("#selectPaperId").val()
            };

            App.postJSON("/rstaff/oral/oralClassStatisticData.vpage", postData, function (data) {
                paperOperate.$targetTable.thaw();
                for (var i = 0; i < (data.refList != undefined ? data.refList.length : 0); i++) {
                    if (data.refList[i].class_name == "NULL") {
                        data.refList[i].class_name = ""
                    }
                }
                var dataList = data.refList || [];
                paperOperate.dataList = []; //清空数据
                if($("#selectPaperId").val()){
                    dataList.forEach(function (item) {
                        item.actStuNum = "-";
                    })
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

    $("#btn-query").on("click",function(){
        paperOperate.init();
    });

</script>
</@com.page>