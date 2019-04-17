<#if hadVhHomework!false>
    <#include "../../constants.ftl">
    <#function winterVacationStaticBasePath name>
        <#return buildStaticFilePath("/activity/holiday/" + name, "img")>
    </#function>
	<style>
        .parentApp-homeWinterHomework { height: 162px; background: url(${winterVacationStaticBasePath('parentApp-homeWinterHomework-bg.png')}) no-repeat; background-size: 100% 100%; text-align: center; }
		.parentApp-homeWinterHomework a { display: block; height: 100%; }
		.parentApp-homeWinterHomework span { display: inline-block; vertical-align: top; margin: 44px auto 0; padding: 0 46px 0 24px; color: #fff; font-size: 28px; line-height: 60px; background: url(${winterVacationStaticBasePath('parentApp-homeMsg-new-ico.png')}) no-repeat 92% 50%; border: 2px #fff solid; border-radius: 2px; }
		.parentApp-homeSummerHomework { background-image: url(${winterVacationStaticBasePath('parentApp-homeSummerHomework-bg.png')}); background-size: cover; }
	</style>
    <div class="parentApp-homeWinterHomework parentApp-homeSummerHomework">
        <a href="/parentMobile/homework/vhindex.vpage?no_select_kids=1&sid=${sid}" class="doTrack do_not_add_client_params" ${buildTrackData("HTML|vacationhw_click")}><span>进入假期作业</span></a>
    </div>
</#if>
