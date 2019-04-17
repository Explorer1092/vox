<#import "../layout.ftl" as activity>
<@activity.page title="热门活动" pageJs="activityList">
    <style>
        /*t-hot-active-meg-box*/
        .t-hot-active-meg-box{ margin: 20px 30px 0 30px;}
        .t-hot-active-meg-box a.hot-content{ display: block; border: 1px solid #d3d8df; border-radius: 6px; background-color: #fff;}
        .t-hot-active-meg-box a.hot-content .th-img{ width: 100%; height: 240px;}
        .t-hot-active-meg-box a.hot-content .th-img img{ width: 100%; height: 240px;}
        .t-hot-active-meg-box a.hot-content .tn-info{ padding: 10px 30px; position: relative;}
        .t-hot-active-meg-box a.hot-content .tn-info p{ font-size: 22px; color: #4e5656; padding: 5px 0; width: 72%; text-overflow: ellipsis; overflow: hidden; white-space: nowrap;}
        .t-hot-active-meg-box a.hot-content .tn-info .time{ color: #afb5b7; font-size: 18px;}
        .t-hot-active-meg-box a.hot-content .tn-info .n-btn{ position: absolute; right: 30px; top: 22px;}
    </style>
    <div id="activityListBox" style="padding-bottom: 20px;">
        <#--索尼比赛成绩公布 2016.04.07——2016.04.21-->
        <#if false && (.now lt "2016-04-22 00:00:00"?datetime('yyyy-MM-dd HH:mm:ss'))>
            <div class="t-hot-active-meg-box">
                <a class="hot-content" href="http://www.17zuoye.com/redirector/apps/go.vpage?app_key=GlobalMath">
                    <div class="th-img"><img src="/public/images/parent/activity/globalmath/sonyGmcResultBanner.jpg" alt="世界趣味数学挑战赛"/></div>
                    <div class="tn-info">
                        <p>世界趣味数学挑战赛成绩公布啦</p>
                        <span class="n-btn">查看详情</span>
                    </div>
                </a>
            </div>
        </#if>

        <#--公开课-->
        <#if openClassId?has_content>
            <div class="t-hot-active-meg-box">
                <a class="hot-content js-openclassBannerBtn" data-href="${openClassId!0}" href="javascript:void(0);">
                    <div class="th-img"><img src="/public/images/parent/openclass/banner_v2.png?v=1.0.1" alt="公开课！！！"/></div>
                    <div class="tn-info">
                        <p>闲暇无事，就来一起公开课吧~</p>
                        <p class="time">活动时间/地点，请点击“查看详情”</p>
                        <span class="n-btn">查看详情</span>
                    </div>
                </a>
            </div>
        </#if>

   		<#--托管班 Managed class-->
        <#if trusteeId?has_content>
            <div class="t-hot-active-meg-box">
                <a class="hot-content js-trusteeBannerBtn" data-href="${trusteeId!0}" href="javascript:void(0);">
                    <div class="th-img"><img src="/public/images/parent/trustee/banner.png" alt="托管班！！！"/></div>
                    <div class="tn-info">
                        <p>寒假来啦，一起养个好习惯吧~</p>
                        <p class="time">活动时间/地点，请点击“查看详情”</p>
                        <span class="n-btn">查看详情</span>
                    </div>
                </a>
            </div>
        </#if>

    </div>
</@activity.page>