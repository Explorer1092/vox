<#include "infomation.ftl" />
<#macro page t=0,s=0,columnType="normal">
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
        <div class="header">
            <div class="head_inline">
                <p><a href="javascript:void(0);" title="一起作业" class="logo <#if columnType == "exmaindex">logo_back</#if>"></a></p>
                <div class="nav">
                    <ul>
                        <#if currentUser.isResearchStaffForCounty() || currentUser.isResearchStaffForCity() >
                            <li <#if t==0>class="active"</#if>>
                                <a href="/rstaff/index.vpage"><i class='icon_rstaff icon_rstaff_1'></i>&nbsp;金 币</a>
                            </li>
                            <li <#if t==1>class="active"</#if>>
                                <a href="/rstaff/testpaper/index.vpage" id="groupVolumeVoxLog"><i class='icon_rstaff icon_rstaff_2'></i>&nbsp;组 卷</a>
                            </li>
                            <li <#if t==2>class="active"</#if>>
                                <a href="/rstaff/report/index.vpage"><i class='icon_rstaff icon_rstaff_3'></i>&nbsp;报 告</a>
                            </li>
                            <li>
                                <a href="/tts/listening.vpage"><i class='icon_rstaff icon_rstaff_2'></i>&nbsp;听力材料</a>
                            </li>
                        <#else>
                            <li <#if t==0>class="active"</#if>>
                                <a href="/rstaff/summary.vpage"><i class='icon_rstaff icon_rstaff_1'></i>&nbsp;统 计</a>
                            </li>
                            <li>
                                <a href="/tts/listening.vpage"><i class='icon_rstaff icon_rstaff_2'></i>&nbsp;听力材料</a>
                            </li>
                        </#if>
                    </ul>
                </div>
                <div class="aside">
                    <@rstaffInfomation/>
                </div>
            </div>
        </div>
        <#---------//middle start--------->
        <#switch columnType>
            <#case "normal">
                <div class="section">
                    <div class="nav">
                        <ul id="left_menu_list">
                            <#if currentUser.isResearchStaffForCounty()>
                                <#if t==0>
                                    <li <#if s==0>class="active"</#if>><a href="/rstaff/index.vpage"><i></i>试卷统计</a></li>
                                    <li <#if s==1>class="active"</#if>><a href="/rstaff/testpaper.vpage"><i></i>试卷使用情况</a></li>
                                    <li <#if s==2>class="active"</#if>><a href="/rstaff/goldrecord.vpage"><i></i>园丁豆记录</a></li>
                                    <li <#if s==3>class="active"</#if>><a href="/rstaff/summary.vpage"><i></i>用户汇总</a></li>
                                    <li <#if s==7>class="active"</#if>><a href="/rstaff/book/booklist.vpage"><i></i>组卷统考</a></li>
                                </#if>
                                <#if t==1>
                                    <li <#if s==4>class="active"</#if>><a href="/rstaff/testpaper/index.vpage"><i></i>组卷</a></li>
                                    <li <#if s==5>class="active"</#if>><a href="/rstaff/testpaper/list.vpage"><i></i>试卷列表</a></li>
                                </#if>
                                <#if t==2>
                                    <li <#if s==6>class="active"</#if>><a href="/rstaff/report/index.vpage"><i></i>试卷全区成绩对比</a></li>
                                    <#--<li <#if s==7>class="active"</#if>><a href="/rstaff/report/list.vpage"><i></i>区热点题型统计</a></li>-->
                                </#if>
                            <#elseif currentUser.isResearchStaffForCity()>
                                <!-- Feature #7350, 对市教研员开放组卷功能，屏蔽邀请、园丁豆记录、奖品中心功能, by Changyuan, 2015-1-7 -->
                                <#if t==0>
                                    <li <#if s==0>class="active"</#if>><a href="/rstaff/index.vpage"><i></i>试卷统计</a></li>
                                    <li <#if s==1>class="active"</#if>><a href="/rstaff/testpaper.vpage"><i></i>试卷使用情况</a></li>
                                    <li <#if s==3>class="active"</#if>><a href="/rstaff/summary.vpage"><i></i>用户汇总</a></li>
                                    <li <#if s==7>class="active"</#if>><a href="/rstaff/book/booklist.vpage"><i></i>组卷统考</a></li>
                                </#if>
                                <#if t==1>
                                    <li <#if s==4>class="active"</#if>><a href="/rstaff/testpaper/index.vpage"><i></i>组卷</a></li>
                                    <li <#if s==5>class="active"</#if>><a href="/rstaff/testpaper/list.vpage"><i></i>试卷列表</a></li>
                                </#if>
                                <#if t==2>
                                    <li <#if s==6>class="active"</#if>><a href="/rstaff/report/index.vpage"><i></i>试卷全市成绩对比</a></li>
                                <#--<li <#if s==7>class="active"</#if>><a href="/rstaff/report/list.vpage"><i></i>区热点题型统计</a></li>-->
                                </#if>
                            <#else>
                                <#if t==0>
                                    <li class="active"><a href="/rstaff/summary.vpage"><i></i>用户汇总</a></li>
                                </#if>
                            </#if>

                            <#if t=3>
                               <li><br/><br/><br/></li>
                            </#if>
                        </ul>
                        <div class="clear"></div>
                        <!--切换广告-->
                        <div class="switchBox belist">
                            <ul>
                                ${pageBlockContentGenerator.getPageBlockContentHtml('RstaffIndex', 'RightAdBoxItemsP1')}
                            </ul>
                            <div class="tab"></div>
                        </div>
                        <@ftlmacro.allswitchbox />
                    </div>
                    <div class="aside">
                        <#if t==0>
                            <div class="row_vox_right">
                            <#if currentUser.isResearchStaffForCounty()>
                                <a href="/reward/index.vpage" target="_blank">
                                    <i class='icon_rstaff icon_rstaff_7'></i> <strong class="text_orange">奖品中心</strong>
                                </a>
                            </#if>
                            </div>
                        </#if>
                    <#nested>
                    </div>
                    <div class="clear"></div>
                </div>
                <#break />
            <#case "exmaindex">
                <#nested>
                <#break />
            <#default>
                <div class="section"><#nested></div>
                <#break />
        </#switch>
        <#---------middle end//--------->
        <div class="footer">
            <div class="copyright">
                ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
            </div>
        </div>

        <script>
            $(function(){
                $(window).scroll(function(){
                    if($(this).scrollTop() != 0){
                        $('#back_top_but').fadeIn();
                    }else{
                        $('#back_top_but').fadeOut();
                    }
                });

                $('#back_top_but').click(function(){
                    $17.backToTop();
                });

                $("#left_menu_list").find("li").click(function(){
                    $(this).radioClass("active");
                });

                $("table.table_vox_striped tbody tr:odd").addClass("odd");

                $("table.table_vox_striped tbody tr").hover(function(){
                    $(this).addClass("active");
                },function(){
                    $(this).removeClass("active");
                });

                $("#groupVolumeVoxLog").on("click", function(){
                    $17.voxLog({
                        module: "rstaff_paper",
                        op:"rstaff_zujuan"
                    });
                });
            });
        </script>
        <@sugar.site_traffic_analyzer_end />
    </body>
</html>
</#macro>


