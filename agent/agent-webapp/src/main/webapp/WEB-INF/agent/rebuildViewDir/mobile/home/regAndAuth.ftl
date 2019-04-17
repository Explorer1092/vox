<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>

<@layout.page title="注册认证" pageJs="">
    <@sugar.capsule css=['home']/>

<div class="primary-box">
    <div class="res-top fixed-head">
        <a href="javascript:window.history.back();"><div class="return"><i class="return-icon"></i>返回</div></a>
        <span class="return-line"></span>
        <span class="res-title">注册认证 <#if schoolPhase??> (<#if schoolPhase == 2>中<#elseif schoolPhase == 1>小</#if>学)</#if></span>
    </div>
    <div class="c-opts gap-line">
        <span><a href="/mobile/performance/user_performance.vpage?type=1&id=${id!0}&idType=${idType!}">小学英语</a></span>
        <span><a href="/mobile/performance/user_performance.vpage?type=2&id=${id!0}&idType=${idType!}">小学数学</a></span>
        <span><a href="/mobile/performance/user_performance.vpage?type=3&id=${id!0}&idType=${idType!}">初中英语</a></span>
        <span class="js-filterBtn select-icon the">注册认证</span>
    </div>
    <div class="filter_pane single" style="display: <#if schoolPhase??>none<#else>block</#if>;" >
        <p class="js-sort <#if schoolPhase?? && schoolPhase == 2>selected</#if>" data-key="2">中学</p>
        <p class="js-sort <#if schoolPhase?? && schoolPhase == 1>selected</#if>" data-key="1">小学</p>
    </div>
    <div class="mask"  style="display: <#if schoolPhase??>none<#else>block</#if>;" ></div>

    <div class="regCer-list" id="headerCon">
        <#if success!false>
            <ul>
                <li>
                    <div class="regCer-num">${sunThisMonthRegNum!0}</div>
                    <div class="regCer-info">本月注册</div>
                </li>
                <li>
                    <div class="regCer-num">${sunThisMonthAuthNum!0}</div>
                    <div class="regCer-info">本月认证</div>
                </li>
                <li>
                    <div class="regCer-num reg-numRed">${sunYesterdayRegNum!0}</div>
                    <div class="regCer-info">昨日注册</div>
                </li>
                <li>
                    <div class="regCer-num reg-numRed">${sunYesterdayAuthNum!0}</div>
                    <div class="regCer-info">昨日认证</div>
                </li>
            </ul>
        </#if>
    </div>
    <div class="pr-side">
        <table class="sideTable">
            <thead>
            <tr>
                <td>姓名</td>
                <td>本月<br>注册</td>
                <td>本月<br>认证</td>
                <td>昨日<br>注册</td>
                <td>昨日<br>认证</td>
            </tr>
            </thead>
            <tbody id="authBody">
                <#if regAndAuthData?? && regAndAuthData?size gt 0>
                    <#list regAndAuthData as ra>
                        <tr data-uid="${ra.targetId!0}" data-idtype="${ra.type!''}" class="<#if ra.isGroupManager!false>js-Item<#else>js-agentItem</#if>">
                            <td><span class="pr-name">${ra.targetName!""}</span></td>
                            <td>${ra.thisMonthRegNum!0}</td>
                            <td>${ra.thisMonthAuthNum!0}</td>
                            <td>${ra.yesterdayRegNum!0}</td>
                            <td>${ra.yesterdayAuthNum!0}</td>
                            <td><span class="arrow"></span></td>
                        </tr>
                    </#list>
                </#if>
            </tbody>
        </table>
    </div>
</div>
<script>
    var id = '${id!0}';
    var idType = '${idType!}';
    var schoolPhase = '${schoolPhase!}';

    var AT = new agentTool();
    $(document).on("ready",function(){

        <#if !success!false> <#--忽略首次未选学校阶段-->
            <#if !ignore!false>
                AT.alert("${info!''}");
            </#if>
        </#if>

        var $pane = $(".filter_pane"),
            $mask = $(".mask");

        var getAuthData = function(id, idType, schoolPhase){
            location.href = "/mobile/performance/regAndAuth.vpage?id="+id+"&idType="+idType + "&schoolPhase="+schoolPhase;
        };
        var getReplace = function(id, idType, schoolPhase){
            var Url = "/mobile/performance/regAndAuth.vpage?id="+id+"&idType="+idType + "&schoolPhase="+schoolPhase;
            location.replace(Url);
        };
        $(document).on("click",".js-filterBtn",function(){
            if($pane.is(':visible')){
                $pane.hide();
                $mask.hide();
            }else{
                $pane.show();
                $mask.show();
            }
        });

        $(document).on("click",".js-sort",function(){
            var key = $(this).data("key");
            $(this).addClass("selected").siblings("p").removeClass("selected");
            $pane.hide();
            $mask.hide();
            location.href = "/mobile/performance/regAndAuth.vpage?id=${id!0}&idType=${idType!}&schoolPhase="+key;

        });

        $(document).on("click",".js-Item",function(){
            var id = $(this).data("uid");
            var idType = $(this).data("idtype");

            //ios10 history back 问题暂时绕开
            if(navigator.userAgent.indexOf('iPhone OS 10') != -1){
                getReplace(id, idType, schoolPhase);
            }else{
                getAuthData(id, idType, schoolPhase);
            }
        });

        $(document).on("click",".js-agentItem",function(){
            var uid = $(this).data("uid");
            var idType = $(this).data("idtype"),
                url = "/mobile/performance/bd_performance_report.vpage?type=1&id="+uid+'&idType='+idType;

            //ios10 history back 问题暂时绕开
            if(navigator.userAgent.indexOf('iPhone OS 10') != -1){
                location.replace(url);
            }else{
                location.href = url;
            }
        });

    });
</script>
</@layout.page>