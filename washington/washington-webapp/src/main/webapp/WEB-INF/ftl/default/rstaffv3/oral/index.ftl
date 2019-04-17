<#import "../researchstaffv3.ftl" as com>
<@com.page menuIndex=22 menuType="normal">
<ul class="breadcrumb_vox">
    <li><a href="javascript:void(0);">组卷统考</a> <span class="divider">/</span></li>
    <li class="active">口语测试</li>
</ul>
<div class="r-mapResearch-box">
    <div class="r-table">
        <table id="oralTable">
        </table>
        <div class="system_message_page_list message_page_list" style="float:right; padding:0 0 10px;"></div>
    </div>
</div>
<script id="t:口语试题列表" type="text/html">
    <tbody>
    <tr>
        <td>名称</td>
        <td>题量</td>
        <td>创建时间</td>
        <td>有效开始时间</td>
        <td>有效结束时间</td>
        <td>操作</td>
    </tr>
    <%
    if(paperMapper != null && paperMapper.length > 0){
    for(var i = 0; i < paperMapper.length; i++){
    %>
    <tr>
        <td class="text_blue paperName"><a style="text-decoration: underline;" href="/rstaff/oral/oralpreview.vpage?paperId=<%=paperMapper[i].paperId%>" target="_blank"><%=paperMapper[i].title%></a></td>
        <td><%=paperMapper[i].questionNum%></td>
        <td><%=paperMapper[i].createDatetimeStr%></td>
        <td><%=paperMapper[i].beginDateTimeStr%></td>
        <td><%=paperMapper[i].endDateTimeStr%></td>
        <td>
            <%if(paperMapper[i].reportBtn){%>
                <input type="button" data-id="<%=paperMapper[i].id%>" class="view_oral_report" value="查看报告">
            <%}%>
        </td>
    </tr>
    <%}}else{%>
    <tr>
        <td class="text_blue" colspan="6">暂无相关数据</td>
    </tr>
    <%}%>
    </tbody>
</script>



<script type="text/javascript">
    var paperOperate = {
        $targetTable : $("#oralTable"),
        dataList     : [],
        page         : 1,
        size         : 10,
        getContents  : function(){
            if(paperOperate.dataList.length < 1) return [];
            var fromIndex = (paperOperate.page - 1) * paperOperate.size;
            var toIndex = paperOperate.page * paperOperate.size;
            if(toIndex > paperOperate.dataList.length){
                toIndex = paperOperate.dataList.length;
            }
            if(fromIndex > toIndex){
                fromIndex = toIndex;
            }
            return paperOperate.dataList.slice(fromIndex,toIndex);
        },
        getTotalPages : function(){
            return Math.ceil(paperOperate.dataList.length/paperOperate.size);
        },
        pagePaper    : function(page){
            paperOperate.page = page;
            paperOperate.$targetTable.removeClass("pageLoding").empty().html(template("t:口语试题列表",{paperMapper : paperOperate.getContents()}));
            var totalPages = paperOperate.getTotalPages();
            if(totalPages > 1){
                $("div.system_message_page_list").show().page({
                    total: totalPages, current: page, jumpCallBack: paperOperate.pagePaper
                });
            }else{
                $("div.system_message_page_list").hide();
            }
        },
        loadPaper    : function(page){
            page = page || 1;
            if(paperOperate.$targetTable.isFreezing()){
                return false;
            }
            paperOperate.$targetTable.freezing();

            App.postJSON("/rstaff/oral/oralpaperlist.vpage",{},function(data){
                paperOperate.$targetTable.thaw();
                var dataList = data.refList || [];
                paperOperate.dataList = []; //清空数据
                paperOperate.dataList = $.merge(paperOperate.dataList,dataList);
                paperOperate.pagePaper(page);
            });
        },
        init : function() {
            paperOperate.$targetTable.addClass("pageLoding");
            paperOperate.loadPaper(1);
        }
    };
    var a="${a}";
    $(function(){
        paperOperate.init();

        // 查看本学期报告功能
        $(document).on("click", "input.view_oral_report", function () {
            window.location = "oralreport.vpage?id=" + $(this).attr("data-id");
        })
    });

</script>




</@com.page>