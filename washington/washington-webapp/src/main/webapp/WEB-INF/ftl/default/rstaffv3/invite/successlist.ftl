<#import "invitemodule.ftl" as com>
<@com.page tagindex = 2>
<div class="inviteBox" xmlns="http://www.w3.org/1999/html">
    <div class="spacing_vox_bot">
        <input id="startDate" style="width: 110px;" type="text" placeholder="起始日期" class="int_vox" readonly="readonly"/>
        <label for="startDate"><i class='icon_rstaff icon_rstaff_6'></i></label>
        <input id="endDate" style="width: 110px;" type="text" placeholder="结束日期" class="int_vox" readonly="readonly"/>
        <label for ="endDate"><i class='icon_rstaff icon_rstaff_6'></i></label></i>
        <a id="query_but" href="javascript:void(0);" class="btn_vox btn_vox_warning">
            <i class='icon_rstaff icon_rstaff_20'></i> 查询
        </a>
    </div>

    <div class="clear"></div>
    <div id="tablelist">
    </div>
    <div class="spacing_vox_bot">
        <label id="totalLabel" class="row_vox_right"></label>
    </div>
</div>

<script id="t:邀请记录" type="text/html">
    <table class="table_vox table_vox_bordered table_vox_blue">
        <thead>
        <tr>
            <td>老师姓名</td>
            <td>老师手机</td>
            <td>邀请时间</td>
            <td>邀请方式</td>
            <td>是否登录</td>
            <td>布置作业</td>
            <td>是否认证</td>
            <td>奖励状态</td>
            <td style="width:160px;">建议</td>
            <td>新增认证学生</td>
        </tr>
        </thead>
        <tbody>
        <%if(content.length > 0){%>
        <%for(var i = 0; i < content.length; i++){%>
        <tr <%if(i%2!=0){%>class='odd'<%}%>>
        <th>
            <%=content[i].teacherName%>
            <p>ID：<%=content[i].teacherId%></p>
        </th>
        <td><%=content[i].teacherMobile%></td>
        <td><%=content[i].inviteDate%></td>
        <td><%=content[i].inviteType%></td>
        <th>
            <div class="errorSuccess">
                <%if(content[i].loginFlag){%>
                <i class='icon_rstaff icon_rstaff_30' description="right icon"></i>
                <%}else{%>
                <i class='icon_rstaff icon_rstaff_29' description="error icon"></i>
                <%}%>
            </div>
        </th>
        <th>
            <div class="errorSuccess">
                <%if(content[i].arrangeFlag){%>
                <i class='icon_rstaff icon_rstaff_30' description="right icon"></i>
                <%}else{%>
                <i class='icon_rstaff icon_rstaff_29' description="error icon"></i>
                <%}%>
        </th>
        <th>
            <div class="errorSuccess">
                <%if(content[i].teacherAuthState){%>
                <i class='icon_rstaff icon_rstaff_30' description="right icon"></i>
                <%}else{%>
                <i class='icon_rstaff icon_rstaff_29' description="error icon"></i>
                <%}%>
            </div>
        </th>
        <td><%=content[i].reward%>
            <%if(!content[i].teacherAuthState){%>
            （未获得）
            <%}%>
        </td>
        <th>
            <%if(content[i].inviteType == "短信"){%>
            <%if(!content[i].loginFlag){%>
            邀请 <%=content[i].teacherName%> 老师登录一起作业网
            <%}else if(!content[i].arrangeFlag){%>
            <a href="javascript:void(0);" class="btn_vox btn_vox_small <%if(content[i].arrangeNotified){%>btn_disable<%}else{%>RemindButton<%}%>" data-id="<%=content[i].teacherId%>" data-val="arrange"><strong><%if(content[i].arrangeNotified){%>已提醒<%}else{%>提醒使用<%}%></strong></a>
            <%}else if(!content[i].teacherAuthState){%>
            <a href="javascript:void(0);" class="btn_vox btn_vox_small <%if(content[i].authenticationNotified){%>btn_disable<%}else{%>RemindButton<%}%>" data-id="<%=content[i].teacherId%>" data-val="authentication"><strong><%if(content[i].authenticationNotified){%>已提醒<%}else{%>提醒认证<%}%></strong></a>
            <%}else{%>
            --
            <%}%>
            <%}else{%>
            <%if(!content[i].loginFlag){%>
            邀请 <%=content[i].teacherName%> 老师登录一起作业网
            <%}else if(!content[i].arrangeFlag){%>
            邀请 <%=content[i].teacherName%> 老师体验互联网作业
            <%}else if(!content[i].teacherAuthState){%>
            提醒 <%=content[i].teacherName%> 老师登录一起作业网完成认证
            <%}else{%>
            --
            <%}%>
            <%}%>
        </th>
        <td>
            <%if(content[i].studentCount==null){%>
                0
            <%}else{%>
                <%=content[i].studentCount%>
            <%}%>
        </td>
        </tr>
        <%}%>
        <%}else{%>
        <tr>
            <th style="padding:50px 0" colspan="6">没有相关邀请记录</th>
        </tr>
        <%}%>
        </tbody>
    </table>
    <div class="spacing_vox_bot">
        <div id="totalLabel" style="text-align: right; clear: both; padding: 5px;"></div>
    </div>
    <div class="common_pagination message_page_list" style="float: right;"></div>
</script>

<script type="text/javascript">
    $(function(){
        /** 开始时间 */
        $("#startDate").datepicker({
            dateFormat: 'yy-mm-dd',
            defaultDate: "",
            numberOfMonths: 1,
            maxDate: "-1D",
            onSelect: function (selectedDate) {
                $('#endDate').datepicker('option', 'minDate', selectedDate);
                $("#startDate").removeClass("alert_vox_error");
            }
        });

        /** 结束时间 */
        $('#endDate').datepicker({
            dateFormat: 'yy-mm-dd',
            defaultDate: "",
            numberOfMonths: 1,
            maxDate: "-1D",
            onSelect:function(){
                $("#endDate").removeClass("alert_vox_error");
            }
        });

        /** 查询 */
        $("#query_but").on("click",function(){
            var _this = $(this);
            var startDate = $("#startDate").val();
            var endDate = $("#endDate").val();
            if($17.isBlank(startDate)){
                $("#startDate").addClass("alert_vox_error");
                return false;
            }
            if($17.isBlank(endDate)){
                $("#endDate").addClass("alert_vox_error");
                return false;
            }

            getInviteSuccessfulHistoryList(false,1,startDate,endDate);

        });

        getInviteSuccessfulHistoryList(false,1,null,null);
    });


    function getInviteSuccessfulHistoryList(filterSuccessRow,pageNumber,startDate,endDate){
        var map = {
            filterSuccessInvite :filterSuccessRow,
            currentPage: pageNumber
        };
        if (startDate != null) {
            map["startDate"] = startDate;
        }
        if (endDate != null) {
            map["endDate"] = endDate;
        }
        $.getJSON("/rstaff/invite/inviteSuccessfulHistory.vpage",map,function(data){
            if(data.success){
                $("#tablelist").html(template("t:邀请记录", {
                    content: data.page
                }));
                $("#totalLabel").html("合计：" + data.total);
                pageListBox();
            }
        });
    }

    function pageListBox(){
        var $this	= $("#tablelist");
        var $tr		= $this.find("tbody tr");
        var groud	= 10;
        var trLen	= $tr.length;

        if((trLen/groud) > 1){
            $tr.each(function(index){
                var x = index + 1;

                if(index >= groud){
                    $tr.eq(index).hide();
                }

                if(index < (trLen/groud)){
                    $(".common_pagination").append("<a href='javascript:void(0);' pv=" + x + "><span>" + x + "</span></a>");
                }
            });

            $(".common_pagination a:first").addClass("this");

            $(".common_pagination a").on('click', function(){
                var x = $(this).attr("pv");
                var y = x * groud;

                $(this).radioClass("this");

                for(var i = 0; i < trLen; i++){
                    $tr.eq(i).show();
                    if(i >= y || i < (y-groud)){
                        $tr.eq(i).hide();
                    }
                }
            });
        }
    }
</script>
</@com.page>