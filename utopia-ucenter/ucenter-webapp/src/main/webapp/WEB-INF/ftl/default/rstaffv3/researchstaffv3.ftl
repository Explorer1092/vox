<#include "information.ftl">
<#macro page menuIndex=0,menuType="normal">
<!DOCTYPE html>
<html>
<head>
    <#include "../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
    <@sugar.capsule js=["jquery", "core", "alert", "template"] css=["plugin.alert", "rstaff.main"] />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
<div id="back_top_but" style="display: none;" class="backTop"><a href="javascript:void (0)"></a></div>
<!--header-->
<div class="header">
    <div class="head_inline">
        <p><a class="logo " title="一起作业" href="javascript:void(0);"></a></p>
        <div class="nav">
            <ul>
                <li>
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/rstaff/index.vpage">&nbsp;首 页</a>
                </li>
                <li>
                <#--只有承德市教研员/潍坊寿光市教研员可点击-->
                    <#if currentUser.region.code == 130800 >
                        <a href="${ProductConfig.getMainSiteBaseUrl()}/ucenter/partner.vpage?url=${ProductConfig.getBbsSiteBaseUrl()}%2fopen.php%3fmod%3dregister&urlInput=${ProductConfig.getBbsSiteBaseUrl()}%2fforum.php%3Fmod%3Dforumdisplay%26fid%3D60"
                           target="_blank">教学资源</a>
                    </#if>
                    <#if currentUser.region.cityCode == 370700 >
                        <a href="${ProductConfig.getMainSiteBaseUrl()}/ucenter/partner.vpage?url=${ProductConfig.getBbsSiteBaseUrl()}%2fopen.php%3fmod%3dregister&urlInput=${ProductConfig.getBbsSiteBaseUrl()}%2fforum.php%3Fmod%3Dforumdisplay%26fid%3D68"
                           target="_blank">教学资源</a>
                    </#if>
                </li>
            </ul>
        </div>
        <div class="aside">
        <#--个人信息-->
                <@rstaffInformation/>
        </div>
    </div>
</div>
<!--section-->
    <#switch menuType>
        <#case "normal">
        <div class="section">
            <div class="nav r-nav">
                <ul class="sn-list">
                    <#if ktwelve?has_content && ktwelve!="JUNIOR_SCHOOL">
                        <li class="si">
                            <a class="si-f" href="javascript:void(0);">大数据报告</a>
                            <ul>
                                <#if currentUser.subject == "ENGLISH" && (currentUser.isResearchStaffForProvince() || currentUser.isResearchStaffForCounty() || currentUser.isResearchStaffForCity() || currentUser.isResearchStaffForStreet()) >
                                    <li class="<#if menuIndex == 10>current</#if>">
                                        <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/rstaff/report/knowledgedata.vpage">
                                            知识数据
                                        </a>
                                    </li>
                                </#if>
                                <li class="<#if menuIndex == 11>current</#if>">
                                    <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/rstaff/report/behaviordata.vpage">
                                        行为数据
                                    </a>
                                </li>
                                <#if currentUser.subject == "ENGLISH">
                                    <li class="<#if menuIndex == 12>current</#if>">
                                        <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/rstaff/report/integralstat/summary.vpage">
                                            积分统计
                                        </a>
                                    </li>
                                </#if>
                            </ul>
                        </li>
                    </#if>
                    <#if (currentUser.isResearchStaffForCounty() || currentUser.isResearchStaffForCity())>
                        <li class="si">
                            <a class="si-f" href="javascript:void(0);">组卷统考</a>
                            <ul>
                                <#--<#if ktwelve?has_content && ktwelve!="JUNIOR_SCHOOL">-->
                                    <#--<li class="<#if menuIndex == 20>current</#if>">-->
                                        <#--<a href="${(ProductConfig.getMainSiteBaseUrl())!''}/rstaff/testpaper/paperreport/list.vpage">-->
                                            <#--试卷及报告-->
                                        <#--</a>-->
                                    <#--</li>-->
                                    <#--<li class="<#if menuIndex == 21>current</#if>">-->
                                        <#--<a href="${(ProductConfig.getMainSiteBaseUrl())!''}/rstaff/testpaper/index.vpage">-->
                                            <#--组卷-->
                                        <#--</a>-->
                                    <#--</li>-->
                                <#--</#if>-->
                                <#if ((currentUser.isResearchStaffForCounty() || currentUser.isResearchStaffForCity()) ) >
                                    <li class="<#if menuIndex == 22>current</#if>">
                                        <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/rstaff/oral/index.vpage">
                                            统考
                                        </a>
                                    </li>
                                </#if>
                            </ul>
                        </li>
                    </#if>
                    <#if currentUser.subject == "ENGLISH"&& ktwelve?has_content && ktwelve!="JUNIOR_SCHOOL">
                        <li class="si">
                            <a class="si-f" href="${(ProductConfig.getMainSiteBaseUrl())!''}/tts/listening.vpage">
                                听力卷TTS
                            </a>
                        </li>
                    </#if>
                </ul>
            </div>
            <div class="aside">
                <#nested>
            </div>
            <div class="clear"></div>
        </div>
            <#break />
        <#case "examindex">
            <#nested>
            <#break />
        <#default>
        <div class="section"><#nested></div>
            <#break />
    </#switch>


<!--footer-->
<div class="footer">
    <div class="copyright">
        ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $(window).scroll(function () {
            if ($(this).scrollTop() != 0) {
                $('#back_top_but').fadeIn();
            } else {
                $('#back_top_but').fadeOut();
            }
        });

        $('#back_top_but').click(function () {
            $17.backToTop();
        });

        $("table.table_vox_striped tbody tr:odd").addClass("odd");

        $("table.table_vox_striped tbody tr").hover(function () {
            $(this).addClass("active");
        }, function () {
            $(this).removeClass("active");
        });

        $("#groupVolumeVoxLog").on("click", function () {
            $17.voxLog({
                module: "rstaff_paper",
                op: "rstaff_zujuan"
            });
        });
    });
</script>
</body>
</html>
</#macro>