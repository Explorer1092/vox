<#import "../../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="业绩" pageJs="informationNew">
    <@sugar.capsule css=['home']/>
<#if (schoolLevel!0) == 1 && (mode!0) == 1>
    <#assign level = 1>
<#elseif (schoolLevel!0) == 24 && (mode!0) == 1>
    <#assign level = 2>
<#elseif (schoolLevel!0) == 24 && (mode!0) == 2>
    <#assign level = 3>
</#if>
<div class="primary-box">
    <a href="javascript:void(0);" class="inner-right arrow_btn js-showHand" style="display:none;"><#if level?? ><#if level == 1>小学<#elseif level == 2>初高中线上<#elseif level ==3>初高中扫描</#if></#if></a>
    <div class="feedbackList-pop show_now" style="display: none; z-index:100;position:fixed;top:0;background-color:rgba(125,125,125,.5);width:100%;height:100%">
        <ul style="background:#fff;margin-top:-0.1rem">
            <li style="padding:.2rem 0"><span class="tab_row <#if level?? && level == 2>active</#if>" style="height:2.5rem;line-height:2.5rem;border-bottom: .05rem solid #cdd3d3;text-align:center;display:inline-block;width:100%;font-size: .75rem;" data-index="24" data-idtype="${idType!""}" data-id="${id!0}" data-mode="1">初高中线上</span></li>
            <li style="padding:.2rem 0"><span class="tab_row <#if level?? && level == 3>active</#if>" style="height:2.5rem;line-height:2.5rem;border-bottom: .05rem solid #cdd3d3;text-align:center;display:inline-block;width:100%;font-size: .75rem;" data-index="24" data-idtype="${idType!""}" data-id="${id!0}" data-mode="2">初高中扫描</span></li>
        </ul>
    </div>
    <div>
        <div class="show_gailan view-box">
            <#if performanceOverview??>
                <#if level?? && level == 3>
                    <div class="schoolRecord-box" style="padding:.5rem 0;">
                        <div class="srd-module">
                            <div class="mHead">普通扫描情况(名校+重点校)</div>
                            <div class="mTable js-item">
                                <table cellpadding="0" cellspacing="0">
                                    <thead>
                                    <tr style="border: 0;">
                                        <td></td>
                                        <td>本月</td>
                                        <td>昨日</td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                        <tr>
                                            <td>普通扫描（≥1）</td>
                                            <td>${performanceOverview["tmFinTpGte1StuCount"]!0}</td>
                                            <td>${performanceOverview["pdFinTpGte1StuCount"]!0}</td>
                                        </tr>
                                        <tr>
                                            <td>普通扫描（≥3）</td>
                                            <td>${performanceOverview["tmFinTpGte3StuCount"]!0}</td>
                                            <td>${performanceOverview["pdFinTpGte3StuCount"]!0}</td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                    <div class="schoolRecord-box" style="padding:.5rem 0;">
                        <div class="srd-module">
                            <div class="mHead">大考扫描情况</div>
                            <ul class="course clearfix">
                                <li class="js-item">语<span>${performanceOverview["tmFinChnBgExamStuCount"]!0}</span></li>
                                <li class="js-item">政<span>${performanceOverview["tmFinPolBgExamStuCount"]!0}</span></li>
                                <li class="js-item">物<span>${performanceOverview["tmFinPhyBgExamStuCount"]!0}</span></li>
                                <li class="js-item">数<span>${performanceOverview["tmFinMathBgExamStuCount"]!0}</span></li>
                                <li class="js-item">史<span>${performanceOverview["tmFinHistBgExamStuCount"]!0}</span></li>
                                <li class="js-item">化<span>${performanceOverview["tmFinCheBgExamStuCount"]!0}</span></li>
                                <li class="js-item">英<span>${performanceOverview["tmFinEngBgExamStuCount"]!0}</span></li>
                                <li class="js-item">地<span>${performanceOverview["tmFinGeogBgExamStuCount"]!0}</span></li>
                                <li class="js-item">生<span>${performanceOverview["tmFinBiolBgExamStuCount"]!0}</span></li>
                            </ul>
                        </div>
                    </div>
                    <div class="schoolRecord-box" style="padding:.5rem 0;">
                            <div class="srd-module">
                                <div class="mHead">区域概况</div>
                                <ul class="area clearfix">
                                    <li class="js-item">规模<span>${performanceOverview["stuScale"]!0}</span></li>
                                    <li class="js-item">考号<span>${performanceOverview["klxTnCount"]!0}</span></li>
                                </ul>
                            </div>
                        </div>
                </#if>
                <#if level?? && level == 1 >
                    <div class="schoolRecord-box" style="padding:.5rem 0;">
                        <div class="srd-module">
                            <div class="mHead">整体月活</div>
                        </div>
                        <ul class="res-list list-dif clearfix" style="clear:both;">
                            <li class="js-item" style="width:50%">
                                <div class="volume">小英月活</div>
                                <div class="sub" style="color:#000">${performanceOverview["engMauc"]!0}</div>
                                <div class="volume" style="color:#ff7d5a">昨日+${performanceOverview["engMaucDf"]!0}</div>
                            </li>
                            <li class="js-item" style="width:50%">
                                <div class="volume">小数月活</div>
                                <div class="sub" style="color:#000">${performanceOverview["mathMauc"]!0}</div>
                                <div class="volume" style="color:#ff7d5a">昨日+${performanceOverview["mathMaucDf"]!0}</div>
                            </li>
                        </ul>
                    </div>
                </#if>
                <#if level?? && (level == 1 || level = 2)>
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
                                            <#if performanceOverview["dataList"]??>
                                                <#list performanceOverview["dataList"] as list>
                                                <tr>
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
                        <div class="srd-module">
                            <div class="mHead">留存情况</div>
                        </div>
                        <ul class="res-list list-dif clearfix" style="clear:both;">
                            <li class="js-item" style="width:50%">
                                <div class="volume">小英次月留存</div>
                                <div class="volume" style="color:#000">${(performanceOverview["engMrtRate"]!0) * 100}%<#--(上月${(performanceOverview["lmEngMrtRate"]!0) * 100}%)--></div>
                            </li>
                            <li class="js-item" style="width:50%">
                                <div class="volume">小数次月留存</div>
                                <div class="volume" style="color:#000">${(performanceOverview["mathMrtRate"]!0) * 100}%<#--(上月${(performanceOverview["lmMathMrtRate"]!0) * 100}%)--></div>
                            </li>
                        </ul>
                    </div>
                    <#if requestContext.getCurrentUser().isCountryManager()>
                        <div class="schoolRecord-box" style="padding:.5rem 0;">
                            <div class="srd-module">
                                <div class="mHead">单科月活转化情况</div>
                            </div>
                            <ul class="res-list list-dif clearfix" style="clear:both;">
                                <li class="js-item" style="width:33%">
                                    <div class="volume">登录月活</div>
                                    <div class="volume" style="color:#000">${performanceOverview["tmLoginStuCount"]!0}</div>
                                </li>
                                <li class="js-item" style="width:33%">
                                    <div class="volume">认证1套月活</div>
                                    <div class="volume" style="color:#000">${performanceOverview["finHwGte1AuStuCount"]!0}</div>
                                </li>
                                <li class="js-item" style="width:33%">
                                    <div class="volume">认证3套月活</div>
                                    <div class="volume" style="color:#000">${performanceOverview["finHwGte3AuStuCount"]!0}</div>
                                </li>
                            </ul>
                        </div>
                    <#else>
                        <div class="schoolRecord-box" style="padding:.5rem 0;">
                            <div class="subTitle">待转化数据</div>
                            <ul class="res-list list-dif clearfix" style="clear:both;">
                                <li class="js-item" style="width:50%">
                                    <div class="volume">当月完成1套作业学生</div>
                                    <div class="volume"> <span style="width:25%;display:inline-block;text-align:center">小英</span> <span style="width:25%;display:inline-block;text-align:center">小数</span></div>
                                    <div class="volume">
                                        <span style="width:25%;display:inline-block;text-align:center">${performanceOverview["finEngHwEq1StuCount"]!0}</span>
                                        <span style="width:25%;display:inline-block;text-align:center">${performanceOverview["finMathHwEq1StuCount"]!0}</span>
                                    </div>
                                </li>
                                <li class="js-item" style="width:50%">
                                    <div class="volume">当月完成2套作业学生</div>
                                    <div class="volume"> <span style="width:25%;display:inline-block;text-align:center">小英</span> <span style="width:25%;display:inline-block;text-align:center">小数</span></div>
                                    <div class="volume">
                                        <span style="width:25%;display:inline-block;text-align:center">${performanceOverview["finEngHwEq2StuCount"]!0}</span>
                                        <span style="width:25%;display:inline-block;text-align:center">${performanceOverview["finMathHwEq2StuCount"]!0}</span>
                                    </div>
                                </li>
                            </ul>
                        </div>
                    </#if>
                </#if>
                <#if level?? && level == 2>
                    <div class="schoolRecord-box" style="padding:.5rem 0;">
                        <div class="subTitle">待转化数据</div>
                        <ul class="res-list list-dif clearfix" style="clear:both;">
                            <li class="js-item" style="width:50%">
                                <div class="volume">当月完成1套作业学生</div>
                                <div class="volume" style="color:#ff7d5a">${performanceOverview["finEngHwEq1StuCount"]!0}</div>
                            </li>
                            <li class="js-item" style="width:50%">
                                <div class="volume">当月完成2套作业学生</div>
                                <div class="volume" style="color:#ff7d5a">${performanceOverview["finEngHwEq2StuCount"]!0}</div>
                            </li>
                        </ul>
                    </div>
                </#if>
                <#if level?? && ( level ==1 || level == 2)>
                    <div class="schoolRecord-box" style="padding:.5rem 0;">
                        <div class="subTitle">区域概况</div>
                        <ul class="res-list list-dif clearfix" style="clear:both;">
                            <li class="js-item" style="width:50%">
                                <div class="volume">本月注册</div>
                                <div class="sub" style="color:#000">${performanceOverview["tmRegStuCount"]!0}</div>
                                <div class="volume" style="color:#ff7d5a">昨日+${performanceOverview["regStuCountDf"]!0}</div>
                            </li>
                            <li class="js-item" style="width:50%">
                                <div class="volume">本月认证</div>
                                <div class="sub" style="color:#000">${performanceOverview["tmAuStuCount"]!0}</div>
                                <div class="volume" style="color:#ff7d5a">昨日+${performanceOverview["auStuCountDf"]!0}</div>
                            </li>
                        </ul>
                        <ul class="colList-2">
                            <li style="color: #999;float: none;">
                                <div>规模: ${performanceOverview["stuScale"]!0}</div>
                                <div>总注册: ${performanceOverview["regStuCount"]!0}</div>
                                <div>总认证: ${performanceOverview["auStuCount"]!0}</div>
                            </li>
                        </ul>
                    </div>
                </#if>
            </#if>
        </div>
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
    var userId = ${requestContext.getCurrentUser().getUserId()!0};
    if(${level!0} == 1){
        var groupLevel = "小学" ;
    }else if(${level!0} == 2){
        var groupLevel = "初高中线上"
    }else if(${level!0} == 3){
        var groupLevel = "初高中扫描"
    }
    var id = ${id!0};
    var idType = "${idType!""}";
    var schoolLevel = ${schoolLevel!0};
    var mode = ${mode!0};
    $(document).on("click",".js-showSubordinate",function(){
        var level = $('.tab_row.active').data("index");
        YQ.voxLogs({
            database : "marketing", //不设置database  , 默认库web_student_logs
            module : "m_sGRM6Xci", //打点流程模块名
            op : "o_gdkFEHHf" ,//打点事件名
            userId:${requestContext.getCurrentUser().getUserId()!0},
            s0 : $(".js-showHand").html()
        });
        var href  = "/mobile/performance/performance_list_page.vpage?level="+level+"&idType=${idType!""}&id=${id!0}" ;
        openSecond(href);
    });
</script>
</@layout.page>
