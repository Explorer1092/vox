<#import "../../layoutTemplate/mobileLayoutBase.ftl" as layout>
<#if performanceOverview??>
    <#assign title = performanceOverview["name"]!"">
</#if>
<@layout.page title="业绩(${title})" pageJs="informationNew">
    <@sugar.capsule css=['home']/>
<div class="primary-box">
    <a href="javascript:void(0);" class="inner-right arrow_btn js-showHand" style="display:none;"><#if level?? ><#if level == 1>小学<#elseif level == 2>初高中线上<#elseif level ==3>初高中扫描</#if></#if></a>
    <#--<div class="res-top fixed-head">-->
        <#--<a href="javascript:window.history.back();"><div class="return"><i class="return-icon"></i>返回</div></a>-->
        <#--<span class="return-line"></span>-->
        <#--<span class="res-title js-pageTitle">业绩<#if performanceOverview??>（${performanceOverview["name"]!""}）</#if></span>-->
        <#--<a href="javascript:void(0);" class="inner-right arrow_btn js-showHand" style="color:#636880"><#if level?? ><#if level == 1>小学<#elseif level == 2>初高中线上<#elseif level ==3>初高中扫描</#if></#if></a>-->
    <#--</div>-->
    <div class="feedbackList-pop show_now" style="display:none;z-index:100;position:fixed;background-color:rgba(125,125,125,.5);width:100%;height:100%">
        <ul style="background:#fff;margin-top:-0.1rem">
            <li style="padding:.2rem 0"><span class="tab_rowInfo <#if level?? && level == 1>active</#if>" style="display:inline-block;text-align:center;height:2.5rem;line-height:2.5rem;width:100%;font-size: .75rem;border-bottom: .05rem solid #cdd3d3" data-index="1" data-info="1" data-idtype="${idType!""}" data-id="${id!0}">小学</span></li>
            <li style="padding:.2rem 0"><span class="tab_rowInfo <#if level?? && level == 2>active</#if>" style="height:2.5rem;line-height:2.5rem;border-bottom: .05rem solid #cdd3d3;text-align:center;display:inline-block;width:100%;font-size: .75rem;" data-index="2" data-info="2" data-idtype="${idType!""}" data-id="${id!0}">初高中线上</span></li>
            <li style="padding:.2rem 0"><span class="tab_rowInfo <#if level?? && level == 3>active</#if>" style="height:2.5rem;line-height:2.5rem;border-bottom: .05rem solid #cdd3d3;text-align:center;display:inline-block;width:100%;font-size: .75rem;" data-index="2" <#--初高中统一传2--> data-info="3" data-idtype="${idType!""}" data-id="${id!0}">初高中扫描</span></li>
        </ul>
    </div>
    <div>
        <div class="show_gailan view-box">
            <#if performanceOverview??>
                <#if level?? && level == 3>
                    <div class="statisticsDetail">
                        <div class="hd">
                            <span>本月扫描情况</span>
                        </div>
                        <ul class="mn column-2">
                            <li>
                                <div class="leftTxt">低标=1</div>
                                <div class="rightTxt">${performanceOverview["lowAnshEq1StuCount"]!0}</div>
                            </li>
                            <li>
                                <div class="leftTxt">低标≥2</div>
                                <div class="rightTxt">${performanceOverview["lowAnshGte2StuCount"]!0}</div>
                            </li>
                            <li>
                                <div class="leftTxt">高标=1</div>
                                <div class="rightTxt">${performanceOverview["highAnshEq1StuCount"]!0}</div>
                            </li>
                            <li>
                                <div class="leftTxt">高标≥2</div>
                                <div class="rightTxt">${performanceOverview["highAnshGte2StuCount"]!0}</div>
                            </li>
                        </ul>
                    </div>
                    <div class="schoolRecord-box" style="padding:.5rem 0;">
                        <div class="srd-module">
                            <div class="mHead">高标≥2明细：</div>
                            <div class="mTable">
                                <table cellpadding="0" cellspacing="0">
                                    <thead>
                                    <tr>
                                        <td></td>
                                        <td>全部扫描</td>
                                        <td>新增</td>
                                        <td>回流</td>
                                        <td>扫描日浮</td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                        <#if performanceOverview??>
                                            <#if performanceOverview["completeDataList"]??>
                                                <#list performanceOverview["completeDataList"] as list>
                                                <tr <#if list.name?? && list.name == "数学">style="color:#ff7d5a" </#if>>
                                                    <td>${list.name!""}</td>
                                                    <td>${list.anshGte2StuCount!0}</td>
                                                    <td>${list.anshGte2IncStuCount!0}</td>
                                                    <td>${list.anshGte2BfStuCount!0}</td>
                                                    <td>${list.anshGte2StuCountDf!0}</td>
                                                </tr>
                                                </#list>
                                            </#if>
                                        </#if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </#if>
                <#if level?? && level == 2>
                    <div class="schoolRecord-box" style="padding:.5rem 0;">
                        <div class="subTitle">英语月活情况</div>
                        <#if performanceOverview["completeDataList"]?? && performanceOverview["completeDataList"]?size gt 0>
                            <#list performanceOverview["completeDataList"] as view>
                                <ul class="colList-1">
                                    <li style="width:25%;">${view.mauc!0}<div>全部月活</div></li>
                                    <li style="width:25%;">${view.incMauc!0}<div>新增</div></li>
                                    <li style="width:25%;">${view.bfMauc!0}<div>回流</div></li>
                                    <li style="width:25%;">${view.maucDf!0}<div>月活日浮</div></li>
                                </ul>
                            </#list>
                        </#if>
                    </div>
                </#if>
                <#if level?? && level == 1 >
                    <div class="schoolRecord-box" style="padding:.5rem 0;">
                        <div class="srd-module">
                            <div class="mHead">目标达成情况</div>
                            <div class="mTable">
                                <table cellpadding="0" cellspacing="0">
                                    <thead>
                                    <tr>
                                        <td></td>
                                        <td>目标</td>
                                        <td>完成数</td>
                                        <td>完成率</td>
                                        <td>日浮</td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                        <#if performanceOverview??>
                                            <#if performanceOverview["completeDataList"]??>
                                                <#list performanceOverview["completeDataList"] as list>
                                                <tr <#if list.name?? && list.name == "月活">style="color:#ff7d5a" </#if>>
                                                    <td>${list.name!""}</td>
                                                    <td>${list.maucBudget!0}</td>
                                                    <td>${list.mauc!0}</td>
                                                    <td data-info="${(list.maucCompleteRate!0) * 100}%">
                                                        <#if list.maucCompleteRate?? && list.maucCompleteRate gt 2 >
                                                            >200%
                                                        <#else>
                                                        ${(list.maucCompleteRate!0) * 100}%
                                                        </#if>
                                                    </td>
                                                    <td>${list.maucDf!0}</td>
                                                </tr>
                                                </#list>
                                            </#if>
                                        </#if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </#if>
                <#if level?? && level == 1 >
                    <div class="schoolRecord-box" style="padding:.5rem 0;">
                        <div class="subTitle">月环比 <span class="js-moreBtn" style="float:right;color:#ff7d5a;margin-right:.5rem;cursor:pointer;z-index: 123;">更多></span></div>
                        <#if performanceOverview["lmRateDataList"]?? && performanceOverview["lmRateDataList"]?size gt 0>
                            <#list performanceOverview["lmRateDataList"] as view>
                                <#if view.name?? && view.name = "所有学校">
                                    <ul class="colList-1">
                                        <li style="width:25%;">${(view.maucLmRate!0)*100}%<div>月活</div></li>
                                        <li style="width:25%;">${(view.incMaucLmRate!0)*100}%<div>新增</div></li>
                                        <li style="width:25%;">${(view.ltMaucLmRate!0)*100}%<div>长回</div></li>
                                        <li style="width:25%;">${(view.stMaucLmRate!0)*100}%<div>短回</div></li>
                                    </ul>
                                </#if>
                            </#list>
                        </#if>
                    </div>
                </#if>
                <#if level?? && (level == 1 || level == 2)>
                    <div class="schoolRecord-box" style="padding:.5rem 0;">
                        <div class="subTitle">1套2套（所有学生）</div>
                        <ul class="colList-2">
                            <li>
                                <div>1套：${performanceOverview["finHwEq1StuCount"]!0}</div>
                                <div>2套：${performanceOverview["finHwEq2StuCount"]!0}</div>
                            </li>
                        </ul>
                    </div>
                </#if>
                <#if level?? && (level == 1 || level == 2)>
                    <div class="schoolRecord-box" style="padding:.5rem 0;">
                        <div class="subTitle">注册认证</div>
                        <ul class="colList-2">
                            <li>
                                <div>注册</div>
                                <div>本月 ${performanceOverview["monthRegStuCount"]!0}</div>
                                <div>昨日 <#if performanceOverview["regStuCountDf"]??> <#if performanceOverview["regStuCountDf"] gt 0>+<#elseif performanceOverview["regStuCountDf"] lt 0>-</#if></#if>${performanceOverview["regStuCountDf"]!0}</div>
                            </li>
                            <li>
                                <div>认证</div>
                                <div>本月 ${performanceOverview["monthAuthStuCount"]!0}</div>
                                <div>昨日 <#if performanceOverview["authStuCountDf"]??> <#if performanceOverview["authStuCountDf"] gt 0>+<#elseif performanceOverview["authStuCountDf"] lt 0>-</#if></#if>${performanceOverview["authStuCountDf"]!0}</div>
                            </li>
                        </ul>
                    </div>
                </#if>
                <#if level?? && level ==3>
                    <div class="schoolRecord-box" style="padding:.5rem 0;">
                        <div class="subTitle">区域概况</div>
                        <ul class="colList-2">
                            <li>
                                <div>规模：${performanceOverview["stuScale"]!0}</div>
                                <div>考号：${performanceOverview["stuKlxTnCount"]!0}</div>
                            </li>
                        </ul>
                    </div>
                <#else>
                    <div class="schoolRecord-box" style="padding:.5rem 0;">
                        <div class="subTitle">区域概况</div>
                        <ul class="colList-2">
                            <li>
                                <div>规模：${performanceOverview["stuScale"]!0}</div>
                                <div>注册：${performanceOverview["regStuCount"]!0}</div>
                                <div>认证：${performanceOverview["authStuCount"]!0}</div>
                            </li>
                        </ul>
                    </div>
                </#if>
                <#if idType?? && (idType == "USER" || idType == "OTHER_SCHOOL")>
                <div class="view_nav">
                    <ul>
                        <li class="js-showSchoolInfo" style="background: #fff;">查看学校明细</li>
                    </ul>
                </div>
            </#if>
            </#if>
            </input>
        </div>
    </div>
    <div class="popUp-box showMore" style="display:none;">
        <div class="inner">
            <div class="content">
                <table>
                    <thead>
                    <tr>
                        <td></td>
                        <td>月活</td>
                        <td>新增</td>
                        <td>长回</td>
                        <td>短回</td>
                    </tr>
                    </thead>
                    <tbody>
                        <#if performanceOverview["lmRateDataList"]?? && performanceOverview["lmRateDataList"]?size gt 0>
                            <#list performanceOverview["lmRateDataList"] as view>
                            <tr>
                                <td>${view.name!""}</td>
                                <td>${(view.maucLmRate!0)*100}%</td>
                                <td>${(view.incMaucLmRate!0)*100}%</td>
                                <td>${(view.ltMaucLmRate!0)*100}%</td>
                                <td>${(view.stMaucLmRate!0)*100}%</td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
            <div class="btn">
                <a href="javascript:void(0);" class="orange_btn closeBtn">我知道了</a>
            </div>
        </div>
    </div>
    <script src="/public/rebuildRes/js/mobile/home/sortTable.js"></script>

    <script>
         var groupLevel = "<#if level?? ><#if level == 1>小学<#elseif level == 2>初高中线上<#elseif level ==3>初高中扫描</#if></#if>";
        $(document).on("click",".closeBtn",function(){
            $('.showMore').hide();
        });
        $(document).on("click",".js-showSchoolInfo",function(){
            var level = $('.tab_rowInfo.active').data("index");
            window.location.href = "school_performance.vpage?schoolLevel="+level+"&idType=${idType!""}&id=${id!0}" ;
        });
        $(document).on("click",".tab_rowInfo",function(){
            var data = $(this).data();
            var idType = $(this).data().idtype;
            if(!$(this).hasClass("active")){
                window.location.href = "performance_detail.vpage?id=" + data.id +"&idType="+ data.idtype +"&level="+$(this).data().info;
            }else{
                $(".show_now").hide();
            }
        });
        var roleType = "${idType!""}";
        var userId = ${requestContext.getCurrentUser().getUserId()!0};
    </script>
</@layout.page>
