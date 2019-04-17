<#--<div style="margin-bottom: 10px;" id="inviteTeacherListBanner">
    <a href="javascript:void(0);"><img src="<@app.link href="public/skin/teacherv3/images/lv/to_banner_v1.jpg"/>"></a>
</div>-->
<#if (currentTeacherDetail.isPrimarySchool())!false>
    <div class="banner" style="margin-bottom:15px;">
        <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/reward/index.vpage"><img src="<@app.link href="public/skin/teacherv3/images/auth/header-banner.jpg"/>" /></a>
    </div>
<#else>
    <div class="banner" style="margin-bottom:15px;">
        <img src="<@app.link href="public/skin/teacherv3/images/auth/banner-02.jpg"/>" />
    </div>
</#if>
<#--获取方案代号(上月过期，本月避免过去，本月过期)-->
<#function getPlanCode lastMonthOverDue thisMonthAvoidOverDue thisMonthOverDue>
    <#local planCode = "">
    <#if (!lastMonthOverDue && thisMonthAvoidOverDue) || (!lastMonthOverDue && !thisMonthAvoidOverDue && !thisMonthOverDue) || (lastMonthOverDue && thisMonthAvoidOverDue) >
        <#local planCode = "planOne">
    <#elseif !lastMonthOverDue && !thisMonthAvoidOverDue && thisMonthOverDue>
        <#local planCode = "planTwo">
    <#elseif lastMonthOverDue && !thisMonthAvoidOverDue && thisMonthOverDue>
        <#local planCode = "planFour">
    <#elseif lastMonthOverDue && !thisMonthAvoidOverDue && !thisMonthOverDue>
        <#local planCode = "planThree">
    </#if>
    <#return planCode>
</#function>

<div class="w-base">
    <div class="w-base-title">
        <h3 style="font-weight: normal;">我的<@ftlmacro.garyBeansText/> <span style="color:#ff633e;">${currentTeacherDetail.userIntegral.usable}</span></h3>
        <div class="w-base-ext">
            <span class="expired">
                (${date!'---'}将过期<@ftlmacro.garyBeansText/> <span style="color:#ff633e;font-size:16px;"><#if (count!0)?number lt 4>${expired!0}<#else>0</#if></span>个)
                <a href="javascript:void(0)" class="w-tips-main">
                    <i class="icon" style="margin-top: -4px;"></i>
                    <span class="tips-box" style="width: 200px;"><@ftlmacro.garyBeansText/>有效期为12个月，每月最后一天对12个月之前获得且未使用的<@ftlmacro.garyBeansText/>做过期清理。<br/>（例如：2015年12月31日之前获得的<@ftlmacro.garyBeansText/>，如果截止2016年12月31日还未消耗完毕，会在2016年12月31日过期）<i></i></span>
                </a>
            </span>
        </div>
        <div class="w-base-right">
            <div class="rewardStudent">
                <a href="/teacher/smartclazz/list.vpage" class="w-blue"><span class="w-icon-md">去奖励学生</span> <i class="goReward"></i></a>
            </div>
        </div>
    </div>
    <div class="w-base-switch w-base-two-switch w-base-three-switch" style="margin: 0;">
        <div class="w-clear"></div>
        <ul id="recordingListBox">
            <li class="ml" data-ge0='true'>
                <a href="javascript:void(0);" style="padding: 16px 0 13px;font-size:16px;color:#4e5656;">
                    获取记录
                </a>
            </li>
            <li class="ml" data-ge0='false'>
                <a href="javascript:void(0);" style="padding: 16px 0 13px;font-size:16px;color:#4e5656;">
                    消耗记录
                </a>
            </li>
            <li style="width:39%;text-align: left;border-right:none;">
                <a href="javascript:void(0);" style="font-size:12px;color:#7f96a3;cursor: default;background-color: #fff;">
                    <@ftlmacro.garyBeansText/>记录仅显示最近三个月的明细
                </a>
            </li>
        </ul>
    </div>
    <div id="mygoldhistory" class="w-table">

    </div>

</div>
    <#if (show!false) && ((currentTeacherDetail.isPrimarySchool())!false)>
        <#assign lastMonthAod = last!false>
        <#if count gte 4>
            <#assign thisMonthAod = true>
        <#else>
            <#assign thisMonthAod = false>
        </#if>
        <#if expired gt 0>
            <#assign thisMonthOd = true>
        <#else>
            <#assign thisMonthOd = false>
        </#if>
        <#assign panCode = getPlanCode(lastMonthAod,thisMonthAod,thisMonthOd)>
    <#else>
        <#assign panCode = "planOne">
    </#if>
    <#--5月一号后新规-->
    <#if (ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv() || ProductDevelopment.isStagingEnv()) || (.now gt "2016-05-01 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss"))>
        <#switch panCode>
            <#case "planOne">

                <#break>
            <#case "planTwo">
            <div class="w-base">
                <div class="w-base-title">
                    <h3 style="font-weight: normal;">本月福利</h3>
                </div>
                <div class="w-base-container">
                    <div class="welfare-title">老师您好，本月您将有部分<@ftlmacro.garyBeansText/>过期，不过别担心！您只需在本月布置 <span>4</span> 次作业（每班每次至少 <span>8</span> 名学生完成，不包括补做，可算作一次。），即可免于过期。</div>
                    <div class="welfare-info">当前作业完成次数：
                        <span class="welfareNumber">${count!0}</span> 次
                        <span class="w-red"><i class="w-icon-public w-icon-error"></i>未达成</span>
                    </div>
                </div>
            </div>
                <#break>
            <#case "planThree">
            <div class="w-base">
                <div class="w-base-title">
                    <h3 style="font-weight: normal;">本月福利</h3>
                </div>
                <div class="w-base-container">
                    <div class="welfare-title">老师您好，上个月过期的<@ftlmacro.garyBeansText/>也能挽回啦！您只需在本月布置 <span>4</span> 次作业（每班每次至少 <span>8</span> 名学生完成，不包括补做，可算作一次。），就能重新将其收入账户啦！赶紧行动吧！</div>
                    <div class="welfare-info">当前作业完成次数：
                        <span class="welfareNumber">${count!0}</span> 次
                        <span class="w-red"><i class="w-icon-public w-icon-error"></i>未达成</span>
                    </div>
                </div>
            </div>
                <#break>
            <#case "planFour">
            <div class="w-base">
                <div class="w-base-title">
                    <h3 style="font-weight: normal;">本月福利</h3>
                </div>
                <div class="w-base-container">
                    <div class="welfare-title">连续两个月都有<@ftlmacro.garyBeansText/>过期？福利来了！您只需在本月布置 <span>4</span> 次作业（每班每次至少 <span>8</span> 名学生完成，不包括补做，可算作一次。），上月和本月要过期的<@ftlmacro.garyBeansText/>就能全部挽回啦！赶紧行动吧！</div>
                    <div class="welfare-info">当前作业完成次数：
                        <span class="welfareNumber">${count!0}</span> 次
                        <span class="w-red"><i class="w-icon-public w-icon-error"></i>未达成</span>
                    </div>
                </div>
            </div>
                <#break>
        </#switch>
    </#if>
<#--小学显示-->
<#if (currentTeacherDetail.isPrimarySchool())!false>
    <div style="background: url(<@app.link href="public/skin/teacherv3/images/auth/content-int.jpg"/>) no-repeat; height: 318px; width: 780px; margin-bottom: 15px;">
    <#--images-->
    </div>
<#else>
<#--中学显示-->
    <div class="w-base">
        <div class="w-base-title">
            <h3 style="color:#179cfb;font-size: 24px;text-align: center;float:none;font-weight: normal;"><@ftlmacro.garyBeansText/>有什么用？</h3>
        </div>
        <div class="t-commonProblem-box">
            <ul style="margin: 30px 0 30px 30px;">
                <li style="width:50%;">
                    <div class="use-1"></div>
                </li>
                <li>
                    <div class="use-2"></div>
                </li>

            </ul>
        </div>
    </div>
</#if>

<!--兑换规则-->
<div class="w-base">
    <div class="w-base-title">
        <h3 style="color:#179cfb;font-size: 24px;text-align: center;float:none;font-weight: normal;"><@ftlmacro.garyBeansText/>使用常见问题</h3>
    </div>
    <div class="t-commonProblem-box">
        <ul>
            <#if (currentTeacherDetail.isPrimarySchool())!false>
            <#--小学显示-->
                <li>
                    <div class="image image-1"></div>
                    <div class="btn">
                        <a href="http://help.17zuoye.com/?p=1155" class="w-btn w-btn-mini" target="_blank" style="font-size: 16px;">如何获得</a>
                    </div>
                </li>
                <li>
                    <div class="image image-2"></div>
                    <div class="btn">
                        <a href="http://help.17zuoye.com/?p=1157" class="w-btn w-btn-mini" target="_blank" style="font-size: 16px;">使用说明</a>
                    </div>
                </li>
                <li>
                    <div class="image image-3"></div>
                    <div class="btn">
                        <a href="http://help.17zuoye.com/?p=1159" class="w-btn w-btn-mini" target="_blank" style="font-size: 16px;">温馨提示</a>
                    </div>
                </li>
            <#else>
                <#--中学显示-->
                <li>
                    <div class="image image-1"></div>
                    <div class="btn">
                        <a href="http://help.17zuoye.com/?p=1252" class="w-btn w-btn-mini" target="_blank" style="font-size: 16px;">如何获得</a>
                    </div>
                </li>
                <li>
                    <div class="image image-2"></div>
                    <div class="btn">
                        <a href="http://help.17zuoye.com/?p=1254" class="w-btn w-btn-mini" target="_blank" style="font-size: 16px;">使用说明</a>
                    </div>
                </li>
                <li>
                    <div class="image image-3"></div>
                    <div class="btn">
                        <a href="http://help.17zuoye.com/?p=1256" class="w-btn w-btn-mini" target="_blank" style="font-size: 16px;">温馨提示</a>
                    </div>
                </li>
            </#if>
        </ul>
    </div>
</div>
<script type="text/javascript">
    if(location.pathname == "/teacher/center/mygold.vpage"){
        location.href = "/teacher/center/index.vpage#/teacher/center/mygold.vpage";
    }

    var ge0 = true;
    $(function(){
        LeftMenu.changeMenu();
        LeftMenu.focus("wallet");

        if($17.getQuery("type") == "lv"){
            $("html, body").animate({scrollTop : $("#activeLevel").offset().top + 150 }, 500);
        }

        $("#inviteTeacherListBanner").on("click", function(){
            $("html, body").animate({scrollTop : $("#inviteTeacherList").offset().top });
        });

        if($17.getQuery("type") == "rule"){
            setTimeout(function(){
                $("html, body").animate({scrollTop : $("#inviteTeacherList").offset().top });
            }, 200);
        }

        //记录切换
        $('#recordingListBox li.ml').on('click',function(){
            var $this = $(this);
            $this.addClass('active').siblings().removeClass('active');
            ge0 = $this.data('ge0');
            loadSystemMessage(1);
        }).eq(0).click();
    });

    function loadSystemMessage(pageIndex) {
        var $mygoldhistory = $('#mygoldhistory');
        $mygoldhistory.html('<div class="w-ag-center" style="padding: 50px 0;"><img src="<@app.link href="public/skin/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>');

        var param = {
            pageNumber : pageIndex,
            ge0 : ge0
        };

        $.get("/teacher/center/mygoldchip.vpage?" + $.param(param), function (data) {
            if (data) {
                $mygoldhistory.html(data);
            }
            
            var $integralNullBox = $('#integral_null_box');
            if(ge0){
                $integralNullBox.text('您还没有获得任何<@ftlmacro.garyBeansText/>。');
            }else{
                $integralNullBox.text('您还没有任何消耗记录。');
            }
        });
    }
</script>
