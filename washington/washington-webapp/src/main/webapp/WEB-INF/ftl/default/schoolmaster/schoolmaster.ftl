<#include "information.ftl">
<#macro page menuIndex=0,menuType="normal">
<!DOCTYPE html>
<html>
<head>
    <#include "../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
    <@sugar.capsule js=["jquery", "core", "alert", "template", "datepicker"] css=["plugin.alert", "plugin.so", "plugin.datepicker", "rstaff.main"] />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
    <#include "../block/detectzoom.ftl">
<div id="back_top_but" style="display: none;" class="backTop"><a href="javascript:void (0)"></a></div>
<!--header-->
<div class="header">
    <div class="head_inline">
        <p><a class="logo " title="一起作业" href="javascript:void(0);"></a></p>
        <div class="nav">
            <ul>
                <li>
                    <a href="/rstaff/index.vpage">&nbsp;首 页</a>
                </li>
            </ul>
        </div>
        <div class="aside">
        <#--个人信息-->
                <@schoolmasterInformation/>
        </div>
    </div>
</div>
<!--section-->
    <#switch menuType>
        <#case "normal">
        <div class="section">
            <div class="nav r-nav">
                <ul class="sn-list">
                    <li class="si">
                        <a class="si-f" href="javascript:void(0);">大数据报告</a>
                        <ul>
                            <li class="<#if menuIndex == 10>current</#if>">
                                <a href="/schoolmaster/report/schoolsitutation.vpage">
                                    学校概况
                                </a>
                            </li>
                            <li class="<#if menuIndex == 11>current</#if>">
                                <a href="/schoolmaster/report/classstudysitutation.vpage">
                                    班级学情分析
                                </a>
                            </li>
                            <li class="<#if menuIndex == 12>current</#if>">
                                <a href="/schoolmaster/report/knowledgeabilityanalysis.vpage">
                                    知识能力分析
                                </a>
                            </li>
                        </ul>
                    </li>
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
    });
</script>
    <@sugar.site_traffic_analyzer_end />
</body>
</html>
</#macro>