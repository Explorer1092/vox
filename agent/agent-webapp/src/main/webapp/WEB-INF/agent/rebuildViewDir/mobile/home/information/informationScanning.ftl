<#import "../../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="业绩" pageJs="groupSchoolList">
    <@sugar.capsule css=['home']/>
<style type="text/css">
    .schoolBox{background-color: #fff;padding: 0.05rem;font-size: .65rem;}
    .schoolBox .mTable table{width:100%;text-align:center;color:#636880}
    .schoolBox .mTable table tr td{padding:.4rem 0;vertical-align:middle;width:20%;cursor: pointer;}
</style>
<div class="primary-box">
    <div class="schoolBox"></div>
</div>
<script src="/public/rebuildRes/js/mobile/home/sortTable.js"></script>
<script type="text/html" id="groupList">
    <%if(res){%>
        <%var dataMap = res.dataMap%>
        <div class="mTable">
            <%for (var key in dataMap){%>
            <%var resKey = dataMap[key]%>
            <%if (key == 1){%>
            <table class="table_<%=key%>" cellpadding="0" cellspacing="0">
                <thead>
                    <tr>
                        <td class="sortable">部门/姓名</td>
                        <td class="sortable">本月普扫<br>(≥1)</td>
                        <td class="sortable">昨日普扫<br>(≥1)</td>
                        <td class="sortable">本月普扫<br>(≥3)</td>
                        <td class="sortable">昨日普扫<br>(≥3)</td>
                    </tr>
                </thead>
                <tbody>
                <%for(var j=0;j< resKey.length;j++){%>
                <%var resList = resKey[j]%>
                <tr class="js-item" data-id="<%=resList.id%>" data-type="<%=resList.idType%>">
                    <td class="1"><%=resList.name%></td>
                    <td class="2"><%=resList.tmFinTpGte1StuCount%></td>
                    <td class="3"><%=resList.pdFinTpGte1StuCount%></td>
                    <td class="4"><%=resList.tmFinTpGte3StuCount%></td>
                    <td class="5"><%=resList.pdFinTpGte3StuCount%></td>
                </tr>
                <%}%>
                </tbody>
            </table>
            <%}%>
            <%}%>
        </div>
    <%}%>
</script>
<script>
    var groupLevel = "初高中扫描";
    var idType = "${idType!""}";
    var id = ${id!0};
    var schoolLevel = "${schoolLevel!0}" ;
    var mode = "${mode!0}" ;
    var roleType = "${groupRole!""}";


    $(document).on("click",".js-item",function(){
        if($(this).data("type") == "GROUP"){
            openSecond("/mobile/performance/performance_list_page.vpage?schoolLevel="+schoolLevel+"&id="+$(this).data("id")+"&idType="+$(this).data("type")+"&mode=" + mode) ;
        }else{
            openSecond("/mobile/performance/school_performance.vpage?schoolLevel="+schoolLevel+"&id="+$(this).data("id")+"&idType="+$(this).data("type")+"&mode=" + mode);
        }
    });
</script>
</@layout.page>