<#import '../apps/list.ftl' as apps>

<#assign _layout = ""/>
<#assign isThirdApp = (['Stem101','TravelAmerica','PetsWar','iandyou100','DrawTogether','WalkerElf','WukongShizi','WukongPinyin']?seq_contains(curapp.appKey))!false/>
<#if isThirdApp>
    <#--以上app 均使用空模板-->
    <#assign _layout = "layoutblank.ftl"/>
<#else>
    <#assign _layout = "layout.ftl"/>
</#if>

<#import '../layout/${_layout}' as temp>
<@temp.page pageName='${curapp.appName!}'>
    <#assign PageBalckList = false productCartType=""/>
    <#if (currentStudentDetail.inPaymentBlackListRegion)!false>
        <#assign PageBalckList = true/>
    </#if>

<#switch curapp.appKey>
    <#case "KaplanPicaro">
        <#assign cartAppLink = "picaro" showPay = false gameWidth=960 gameHeight=540/>
        <#break />
    <#case "TravelAmerica">
        <#assign cartAppLink = "travel" showPay = true productCartType="?type=1"/>
        <#break />
    <#case "iandyou100">
        <#assign cartAppLink = "iandyou" showPay = false />
        <#break />
    <#case "SanguoDmz">
        <#assign cartAppLink = "sanguodmz" showPay = true productCartType="?type=0"/>
        <#break />
    <#case "PetsWar">
        <#assign cartAppLink = "petswar" showPay = true productCartType="?type=0"/>
        <#break />
    <#case "Stem101">
        <#assign cartAppLink = "stem" showPay = true gameHeight = 720/>
        <#break />
    <#case "WalkerElf">
        <#assign cartAppLink = "walkerelf" showPay = true/>
        <#break />
    <#case "Walker">
        <#assign cartAppLink = "walker" showPay = true productCartType="?type=1"/>
        <#break />
    <#case "AfentiExam">
        <#assign afentiExamLimitClazzFlag = (currentStudentDetail.getClazzLevelAsInteger() gt 2 && currentStudentDetail.getClazzLevelAsInteger() lte 6)!false/>
        <#assign cartAppLink = "exam" showPay = afentiExamLimitClazzFlag  gameWidth=900 gameHeight=600/>
        <#break />
    <#case "WukongPinyin">
        <#assign cartAppLink = (curapp.appKey)?lower_case showPay = ((curapp.appStatus == 0)!false) gameWidth=960 gameHeight=600/>
        <#break />
    <#case "WukongShizi">
        <#assign cartAppLink = (curapp.appKey)?lower_case showPay = true gameWidth=960 gameHeight=600/>
        <#break />
    <#default>
        <#assign cartAppLink = curapp.appKey  showPay = false/>
</#switch>

<div class="t-app-container">
    <div class="t-app-inner">
        <!--tai-head-->
        <div class="ta-head" style="<#if gameWidth?has_content>float: none; margin: 0 auto;</#if>">
            <h1>${curapp.appName!}</h1>
            <#if !PageBalckList && showPay><#--按以上条件显示开通按钮-->
                <div class="btn-area">
                    <a href="/apps/afenti/order/${cartAppLink}-cart.vpage${productCartType}" target="_blank" class="w-btn w-btn-green"><#if curapp.appStatus == 0>开通<#else>续费</#if></a>
                </div>
            </#if>
            <div class="ctn-area">
                <div class="ta-info">
                    <#--start-->
                    <#--//微信绑定提示-->
                    <div class="v-textScrollInfo">
                        <#if curapp.appKey == "TravelAmerica">
                        ${pageBlockContentGenerator.getPageBlockContentHtml('StudentIndex', 'TravelAmericaBanner')}
                        </#if>
                        <#if curapp.appKey == "iandyou100">
                        ${pageBlockContentGenerator.getPageBlockContentHtml('StudentIndex', 'IandyouAmericaBanner')}
                        </#if>
                        <#if curapp.appKey == "SanguoDmz">
                        ${pageBlockContentGenerator.getPageBlockContentHtml('StudentIndex', 'SanguoDmzBanner')}
                        </#if>
                        <#if curapp.appKey == "PetsWar">
                        ${pageBlockContentGenerator.getPageBlockContentHtml('StudentIndex', 'PetsWarBanner')}
                        </#if>
                        <#if curapp.appKey == "AfentiExam">
                        ${pageBlockContentGenerator.getPageBlockContentHtml('StudentIndex', 'AfentiBanner')}
                        </#if>
                        <#if curapp.appKey == "Stem101">
                        ${pageBlockContentGenerator.getPageBlockContentHtml('StudentIndex', 'Stem101Banner')}
                        </#if>
                        <#if curapp.appKey == "Walker">
                            ${pageBlockContentGenerator.getPageBlockContentHtml('StudentIndex', 'nekketsuBanner')}
                        </#if>
                        <#if curapp.appKey == "KaplanPicaro">
                            <ul>
                                <li>Picaro将7月8日下线，谢谢你的使用！</li>
                            </ul>
                        </#if>
                    </div>
                    <#--end-->
                </div>
            </div>
        </div>

        <!--ta-content-->
        <div class="ta-content">
            <dl class="ta-game-box">
                <#if !(gameWidth?has_content) && !isThirdApp>
                    <@apps.appsList appName='${curapp.appName!}'/>
                </#if>
                <dd style="width: ${(gameWidth + 'px')!''};height: ${(gameHeight + 'px')!''}; <#if gameWidth?has_content>float: none; margin: 0 auto;</#if> <#if isThirdApp && !gameWidth?has_content>margin-left:100px</#if> ">
                    <div class="game-container-box">
                        <iframe class="vox17zuoyeIframe" src="${curapp.appUrl!}?session_key=${sessionKey}&sig=${sig!}" name="apps_game_homework" scrolling="auto" frameborder="0" width="${(gameWidth)!'900'}" height="${gameHeight!'610'}"></iframe>
                    </div>
                </dd>
            </dl>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $(".v-textScrollInfo").textScroll({
            line: 1,
            speed: 1000,
            timer : 3000
        });

        $17.voxPageTimeLogs({
            isOpen : <#if ftlmacro.devTestSwitch> false <#else> true </#if> , //开发和测试环境关闭voxPageTimeLogs
            rt : 30*1000,
            module : "${(curapp.appKey)!''}"
        });

        //反馈
        feedBackInner.homeworkType = "${curapp.appKey!''}";

        <#if [0, 2, 4, 6]?seq_contains( (currentStudentDetail.studentSchoolRegionCode%7)!1 ) && ((!PageBalckList)!false)>
            $17.voxLog({
                module : "huodongyure",
                op : "page_${(curapp.appKey)!''}_B"
            }, "student");
        <#else>
            $17.voxLog({
                module : "huodongyure",
                op : "page_${(curapp.appKey)!''}_A"
            }, "student");
        </#if>

        <#--下线列表-->
        <#assign offlineList = {
        'SanguoDmz' : 'sanguodmz',
        'TravelAmerica' : 'travel',
        'PetsWar' : 'petswar'
        }/>
        <#if (['SanguoDmz', 'TravelAmerica', 'PetsWar']?seq_contains(curapp.appKey))!false>
            $.prompt(template("T:${offlineList[(curapp.appKey)!'travelInfo']!}Info", {}), {
                buttons : { },
                title   : "公告："
            });
        </#if>
    });
</script>

<script type="text/html" id="T:sanguodmzInfo">
    <div class="lya-tip-2">
        <p>由于一起作业平台整体的产品线升级，我们对学习产品的教育价值有了更高的标准和期望。所以，怀着万分不舍，《${(curapp.appName)!'--'}》即将准备跟大家告别了。</p>
        <p>1、即日起，本产品的开通和续费功能将永久性地关闭。</p>
        <p>2、本产品将于2016年8月24日关闭试用。</p>
        <p>3、在包月有效期限内，您仍可继续登录和使用，直至包月到期或产品完全下线。</p>
        <#if (curapp.appKey == "SanguoDmz")!false>
            <p>4、北京时间2017年5月16日0点整，《进击的三国》将正式从一起作业平台下线。</p>
        </#if>
        <p>感谢大家一直以来对《${(curapp.appName)!'--'}》的喜爱，更多、更优质的学习产品即将陆续上线一起作业平台，敬请关注！</p>
        <div style="text-align: center; padding: 20px 0 0;">
            <a href="javascript:;" onclick="$.prompt.close();" class="w-btn-dic w-btn-gray-new">知道了</a>
            <a href="/apps/afenti/order/${offlineList[(curapp.appKey)!'A17ZYSPG']!}-cart.vpage" onclick="$.prompt.close();" class="w-btn-dic w-btn-green-new">了解详情</a>
        </div>
    </div>
</script>

<script type="text/html" id="T:travelInfo">
    <div class="lya-tip-2">
        <p>由于一起作业平台整体的产品线升级，我们对学习产品的教育价值有了更高的标准和期望。所以，怀着万分不舍，《走遍美国》即将准备跟大家告别了。</p>
        <p>1、即日起，本产品的开通和续费功能将永久性地关闭。</p>
        <p>2、本产品将于2016年12月11日关闭试用。</p>
        <p>3、在包月有效期限内，您仍可继续登录和使用，直至包月到期或产品完全下线。</p>
        <p>4、北京时间2017年2月17日0点整，《走遍美国》将正式从一起作业平台下线。</p>
        <p>感谢大家一直以来对《走遍美国》的喜爱，更多、更优质的学习产品即将陆续上线一起作业平台，敬请关注！</p>
        <div style="text-align: center; padding: 20px 0 0;">
            <a href="javascript:;" onclick="$.prompt.close();" class="w-btn-dic w-btn-gray-new">知道了</a>
            <a href="/apps/afenti/order/${offlineList[(curapp.appKey)!'']!}-cart.vpage" onclick="$.prompt.close();" class="w-btn-dic w-btn-green-new">了解详情</a>
        </div>
    </div>
</script>

<script type="text/html" id="T:petswarInfo">
    <div class="lya-tip-2">
        <p>由于一起作业平台整体的产品线升级，我们对学习产品的教育价值有了更高的标准和期望。所以，怀着万分不舍，《宠物大乱斗》即将准备跟大家告别了。</p>
        <p>1、即日起，本产品的开通和续费功能将永久性地关闭。</p>
        <p>2、本产品将于2017年2月1日关闭试用。</p>
        <p>3、在包月有效期限内，您仍可继续登录和使用，直至包月到期或产品完全下线。</p>
        <p>4、北京时间2017年4月1日0点整，《宠物大乱斗》将正式从一起作业平台下线。</p>
        <p>感谢大家一直以来对《宠物大乱斗》的喜爱，更多、更优质的学习产品即将陆续上线一起作业平台，敬请关注！</p>
        <div style="text-align: center; padding: 20px 0 0;">
            <a href="javascript:;" onclick="$.prompt.close();" class="w-btn-dic w-btn-gray-new">知道了</a>
            <a href="/apps/afenti/order/${offlineList[(curapp.appKey)!'']!}-cart.vpage" onclick="$.prompt.close();" class="w-btn-dic w-btn-green-new">了解详情</a>
        </div>
    </div>
</script>
</@temp.page>