<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="拜访统计" pageJs="sortTable">
    <@sugar.capsule css=['home']/>
<div id="intoSchoolContent"></div>
<script type="text/html" id="intoSchoolTemplete">
    <div class="c-opts gap-line" style="display: -webkit-box;display: -moz-box;">
        <span class="js-time <%if(range != 'month'){%> active<%}%>" style="display: block;-webkit-box-flex: 1;" data-info="day">当天</span>
        <span class="js-time <%if(range == 'month'){%> active<%}%>" style="display: block;-webkit-box-flex: 1;" data-info="month">本月</span>
    </div>
    <%if(groupViews && range == "day"){%>
    <div class="c-opts gap-line" style="display: -webkit-box;display: -moz-box;">
        <%if(groupViews.Region){%>
        <span class="js-tab" data-info="show_Region" data-index="0" style="display: block;-webkit-box-flex: 1;" >大区</span>
        <%}%>
        <%if(groupViews.City){%>
        <span class="js-tab" data-info="show_City" data-index="1" style="display: block;-webkit-box-flex: 1;" >分区</span>
        <%}%>
        <span class="js-tab" data-info="show_user" data-index="2" style="display: block;-webkit-box-flex: 1;" >专员</span>
    </div>
    <%}%>
    <%if(monthGroupViews && range == "month"){%>
    <div class="c-opts gap-line" style="display: -webkit-box;display: -moz-box;">
        <%if(monthGroupViews.Region){%>
        <span class="js-tab" data-info="show_Region" data-index="0" style="display: block;-webkit-box-flex: 1;">大区</span>
        <%}%>
        <%if(monthGroupViews.City){%>
        <span class="js-tab" data-info="show_City" data-index="1" style="display: block;-webkit-box-flex: 1;">分区</span>
        <%}%>
        <span class="js-tab" data-info="show_user" data-index="2" style="display: block;-webkit-box-flex: 1;">专员</span>
    </div>
    <%}%>
    <div id="my_table">
        <%if(groupViews){%>
        <%for(var key in groupViews){%>
        <% var data = groupViews[key];%>
        <div class="show_<%=key%>_day view-box schoolRecord-box" style="display:none;">
            <div class="srd-module">
                <div class="mTable bgTable" style="display: block">
                    <table class="table_<%=key%>" cellpadding="0" cellspacing="0">
                        <thead>
                        <tr>
                            <td>部门</td>
                            <td class="sortable">人均进校（次）</td>
                            <td class="sortable">人均单校拜访老师数</td>
                        </tr>
                        </thead>
                        <tbody>
                        <%for(var i = 0;i < data.length;i++){%>
                        <% var userRole = data[i];%>
                        <tr class="js-item" data-url="<%=userRole.nextUrl%>&range=day">
                            <td><%=userRole.agentName%></td>
                            <td><%=userRole.intoSchoolCountAvg%></td>
                            <td><%=userRole.visitTeacherAvg%></td>
                        </tr>
                        <%}%>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        <%}%>
        <%}%>
        <%if(monthGroupViews){%>
        <%for(var key in monthGroupViews){%>
        <% var data = monthGroupViews[key];%>
        <div class="show_<%=key%>_month view-box schoolRecord-box" style="display:none;">
            <div class="srd-module">
                <div class="mTable bgTable" style="display: block">
                    <table class="table_<%=key%>" cellpadding="0" cellspacing="0">
                        <thead>
                        <tr>
                            <td>部门</td>
                            <td class="sortable">人均进校（次）</td>
                            <td class="sortable">人均单校拜访老师</td>
                            <td class="sortable">拜访老师布置作业率</td>
                        </tr>
                        </thead>
                        <tbody>

                        <%for(var i = 0;i < data.length;i++){%>
                        <% var userRole = data[i];%>
                        <tr class="js-item" data-url="<%=userRole.nextUrl%>&range=month">
                            <td><%=userRole.agentName%></td>
                            <td><%=userRole.intoSchoolCountAvg%></td>
                            <td><%=userRole.visitTeacherAvg%></td>

                            <td><%=userRole.visitTeacherHwPro%>%</td>
                        </tr>
                        <%}%>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        <%}%>
        <%}%>
        <%if(bdViews){%>
        <div class="show_user_day view-box schoolRecord-box" style="display:none;">
            <div class="srd-module">
                <div class="mTable bgTable" style="display: block">
                    <table cellpadding="0" cellspacing="0">
                        <thead>
                        <tr>
                            <td>姓名</td>
                            <td class="sortable">进校（次）</td>
                            <td class="sortable">校均拜访老师</td>
                        </tr>
                        </thead>
                        <tbody>

                        <%for(var key in bdViews){%>
                        <% var userRole = bdViews[key];%>
                        <tr class="js-item" data-url="<%=userRole.nextUrl%>&range=day" <%if (userRole.intoSchoolCount < 2 || userRole.visitTeacherAvg < 2){%> style="background: yellow" <%}%>>
                        <td><%=userRole.agentName%></td>
                        <td <%if (userRole.intoSchoolCount < 2){%>style="color: red" <%}%>><%=userRole.intoSchoolCount%></td>
                        <td <%if (userRole.visitTeacherAvg < 2){%>style="color: red" <%}%>><%=userRole.visitTeacherAvg%></td>
                        </tr>
                        <%}%>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        <%}%>
        <%if(monthBdViews){%>
        <div class="show_user_month view-box schoolRecord-box" style="display:none;">
            <div class="srd-module">
                <div class="mTable bgTable" style="display: block">
                    <table cellpadding="0" cellspacing="0">
                        <thead>
                        <tr>
                            <td>姓名</td>
                            <td class="sortable">进校（次）</td>
                            <td class="sortable">已访校/学校总数</td>
                            <td class="sortable">校均拜访老师</td>
                            <td class="sortable">拜访老师布置作业率</td>
                        </tr>
                        </thead>
                        <tbody>
                        <%for(var key in monthBdViews){%>
                        <% var userRole = monthBdViews[key];%>
                        <tr class="js-item" data-url="<%=userRole.nextUrl%>&range=month" <%if (userRole.visitTeacherAvg < 2 ){%> style="background: yellow" <%}%>>
                        <td><%=userRole.agentName%></td>
                        <td><%=userRole.intoSchoolCount%></td>
                        <td><%=userRole.visitedSchoolCount%>/<%=userRole.schoolTotal%></td>
                        <td <%if (userRole.visitTeacherAvg < 2){%>style="color: red" <%}%>><%=userRole.visitTeacherAvg%></td>
                        <td><%=userRole.visitTeacherHwPro%>%</td>
                        </tr>
                        <%}%>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        <%}%>
    </div>
</script>
<div class="loading_mark" style="display:none;">
    <div class="loading">

    </div>
</div>
<script src="/public/rebuildRes/js/common/common.js"></script>
<script>

    var range = "${range}";
    var groupId = "${groupId}";
    var roleType = "${roleType}";
    var hadLoadCache = {};

    function loadData(range,groupId,roleType) {

        var cache = hadLoadCache[range];
        if(cache){
            dealData(cache,roleType);
            return;
        }
        $('.loading_mark').show();
        $.get('into_school_statistics_data.vpage', {range:range,groupId:groupId}, function (data) {
            data.range = range;
            hadLoadCache[range] = data;
            dealData(data,roleType);
            $('.loading_mark').hide();
        });
    }
    function dealData(data,roleType) {
        $("#intoSchoolContent").html(template("intoSchoolTemplete",data));
        setTimeout(function () {
            if($('.js-tab').length > 0 ){
                if(-1 == roleType){
                    $('.js-tab').eq(0).click();
                }else {
                    $('.js-tab').each(function () {
                        if($(this).data("index") == roleType){
                            $(this).click();
                        }
                    });
                }
            }else{
                $(".show_user"+"_"+$(".js-time.active").data("info")).show().siblings().hide();
            }
        },0);
    }
    var locationUrl = "/mobile/into_school/" ;
    $(document).ready(function () {
        loadData(range,groupId,roleType);
        $(document).on("click",".js-item",function () {
            openSecond(locationUrl + $(this).data("url"));
        });
        $(document).on("click",".js-tab",function () {
            $(this).addClass("active").siblings().removeClass("active");
            $("." + $(this).data("info")+"_"+$(".js-time.active").data("info")).show().siblings().hide();
        });
        $(document).on("click",".js-time",function () {
            if(!$(this).hasClass("active")){
                loadData($(this).data("info"),groupId,$(".js-tab.active").data("index"));
                $(this).addClass("active").siblings().removeClass("active");
            }
        });
        $(document).on("click",".sortable",function () {
            var colIndex = $(this).index();
            var table = $(this).closest("table");
            $(this).addClass("active").siblings().removeClass("active");
            sortTable(table, colIndex);
        });
    })
</script>
</@layout.page>
