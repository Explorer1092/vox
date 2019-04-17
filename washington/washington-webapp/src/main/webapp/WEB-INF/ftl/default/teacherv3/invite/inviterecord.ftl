<div class="w-base">
    <div class="w-base-title">
        <h3>邀请状态查询（三个月内）</h3>
    </div>
    <!--template container-->
    <div class="w-base-container">
        <!--//start-->
    <#if inviteRecordList?size gt 0>
        <div class="w-table w-table-border-bot" style="position: relative;">
            <table class="table_vox table_vox_bordered " id="isTab">
                <thead>
                <tr>
                    <th style="width: 77px;">姓名</th>
                    <th style="width: 77px;">邀请时间</th>
                    <th style="width: 77px;">被邀请人学科</th>
                    <th >登录</th>
                    <th >布置作业</th>
                    <th>认证</th>
                    <th style="width: 110px;">奖励状态</th>
                    <th style="width: 160px;">建议</th>
                </tr>
                </thead>
                <tbody>
                    <#list inviteRecordList as inviteRecord>
                    <tr>
                        <th>${inviteRecord.inviteeName!}<br/>(${inviteRecord.inviteeId!})</th>
                        <th>${inviteRecord.inviteeTime!}</th>
                        <th>${inviteRecord.inviteeSubject!}</th>
                        <th>
                            <div class="errorSuccess">
                                <#if (inviteRecord.loginFlag?? && inviteRecord.loginFlag) || (inviteRecord.arrangeFlag?? && inviteRecord.arrangeFlag)>
                                    <i class='icon_general icon_general_70'></i>
                                <#else>
                                    <i class='icon_general icon_general_95'></i>
                                </#if>
                            </div>
                        </th>
                        <th>
                            <div class="errorSuccess">
                                <#if (inviteRecord.arrangeFlag?? && inviteRecord.arrangeFlag) || (inviteRecord.authenticated?? && inviteRecord.authenticated)>
                                    <i class='icon_general icon_general_70'></i>
                                <#else>
                                    <i class='icon_general icon_general_95'></i>
                                </#if>
                            </div>
                        </th>
                        <th>
                            <div class="errorSuccess">
                                <#if inviteRecord.authenticated?? && inviteRecord.authenticated>
                                    <i class='icon_general icon_general_70'></i>
                                <#else>
                                    <i class='icon_general icon_general_95'></i>
                                </#if>
                            </div>
                        </th>
                        <th>
                            ${inviteRecord.reward!'---'}<#if inviteRecord.authenticated?? && !inviteRecord.authenticated><br/>(未完成)</#if>
                        </th>
                        <th>
                            <#if (inviteRecord.loginFlag?? && !inviteRecord.loginFlag) && (inviteRecord.authenticated?? && !inviteRecord.authenticated)>
                                陪${inviteRecord.inviteeName!}老师登录一起作业网
                            <#elseif (inviteRecord.arrangeFlag?? && !inviteRecord.arrangeFlag) && (inviteRecord.authenticated?? && !inviteRecord.authenticated)>
                                邀请${inviteRecord.inviteeName!}老师布置作业<br/>获得奖励
                            <#elseif inviteRecord.authenticated?? && !inviteRecord.authenticated>
                                提醒${inviteRecord.inviteeName!}老师<br/>登录一起作业网完成认证
                            <#else>
                                完成认证
                            </#if>
                        </th>
                    </tr>
                    </#list>
                </tbody>
            </table>
        </div>
        <div class="page message_page_list"></div>
        <script type="text/javascript">
            $(function(){
                var $this	= $("#isTab");
                var $tr		= $this.find("tbody tr");
                var groud	= 5;
                var trLen	= $tr.length;

                //$this.find("tbody tr:even").addClass("even");

                $tr.each(function(index){
                    var x = index + 1;

                    if(index >= groud){
                        $tr.eq(index).hide();
                    }

                    if(index < (trLen/groud)){
                        $(".page").append("<a href='javascript:void(0);' pv=" + x + "><span>" + x + "</span></a>");
                    }
                });

                $(".page a:first").addClass("this");

                $(".page a").on('click', function(){
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

                $this.find(".remindCertification").on("click", function(){
                    var $that = $(this);

                    $.post("notify.vpage", {inviteeId : $that.attr("data-id") }, function(data){
                        if(data.success){
                            $that.addClass("btn_disable").find("strong").text("已提醒");
                        }else{
                            $17.alert(data.info);
                        }
                        $17.tongji("老师邀请信-提醒认证");
                    });
                });
            });
        </script>
    <#else>
        <div  style="padding: 20px; text-align: center;">
            <span class="text_big text_gray_6">最近三个月内，您还未邀请老师注册</span>
            <a href="/teacher/invite/index.vpage" class="w-btn w-btn-green w-btn-small"><strong>立即邀请</strong></a>
        </div>
    </#if>
    <!--end//-->
    </div>
</div>
