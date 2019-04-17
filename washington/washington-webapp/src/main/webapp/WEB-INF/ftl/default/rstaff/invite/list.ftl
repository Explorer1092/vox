<#import "invitemodule.ftl" as com>
<@com.page tagindex = 1>
    <div class="inviteBox">
        <div class="spacing_vox_bot">
            <#--<a id="filterSuccessRow" style="display: none" href="javascript:void(0);" class="row_vox_right text_gray"><span class="checkboxs"></span> <strong>只显示未成功</strong></a>-->
            <#--<a href="javascript:void(0);" class="btn_vox btn_vox_primary batchSendTeacherAccountSms" style="display: none;"><i class='icon_rstaff icon_rstaff_22'></i> 批量发送老师账号和密码</a>-->
            <a href="javascript:void(0);" class="btn_vox btn_vox_primary edge_vox_bot row_vox_right RemindButton" data-val="all">提醒全部老师</a>
        </div>
        <div class="clear"></div>
        <div id="tablelist">
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
            </tr>
            <%}%>
            <%}else{%>
            <tr>
                <th style="padding:50px 0" colspan="6">没有相关邀请记录</th>
            </tr>
            <%}%>
            </tbody>
        </table>
        <div class="common_pagination message_page_list" style="float: right;"></div>
    </script>

    <script type="text/javascript">
        $(function(){
           //显示未成功按钮
           /*$("#filterSuccessRow").on("click",function(){
              var spanObj = $(this).children("span[class*='checkboxs']").first();
               var filterSuccessRow = false;
              if(spanObj.hasClass("checkboxs_active")){
                  spanObj.removeClass("checkboxs_active");
              }else{
                  spanObj.addClass("checkboxs_active");
                  filterSuccessRow = true;
              }

              getInviteHistoryList(filterSuccessRow,1);

           });*/

           getInviteHistoryList(false,1);
        });


        function getInviteHistoryList(filterSuccessRow,pageNumber){
           $.getJSON("/rstaff/invite/inviteHistory.vpage",{filterSuccessInvite :filterSuccessRow, currentPage : pageNumber},function(data){
               if(data.success){
                   $("#tablelist").html(template("t:邀请记录", {
                       content: data.page
                   }));
                   pageListBox();

                   //点击提醒使用-提醒认证-全部提醒
                   $(".RemindButton").on("click", function(){
                       var arrangeAuthentication = $(".RemindButton[data-val=arrange], .RemindButton[data-val=authentication]")
                       var $this = $(this);
                       var dataVal = {};

                       if(!arrangeAuthentication.hasClass("RemindButton")){
                           $17.alert("已经提醒过全部老师");
                           return false;
                       }

                       if($this.attr("data-val") == "all"){
                           dataVal = {
                               teacherId :  $this.attr("data-val")
                           };
                       }else{
                           dataVal = {
                               teacherId :  $this.attr("data-id"),
                               notifyFlag :  $this.attr("data-val")
                           };
                       }

                       $.post("/rstaff/invite/notify.vpage", dataVal, function(data){
                           if(data.success){
                               if($this.attr("data-val") != "all"){
                                   $this.addClass("btn_disable").find("strong").text("已提醒").end().removeClass("RemindButton");
                               }else{
                                   arrangeAuthentication.addClass("btn_disable").find("strong").text("已提醒").end().removeClass("RemindButton");
                               }
                           }else{
                               $17.alert(data.info);
                           }
                       });
                   });
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


