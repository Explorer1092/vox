<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="工作记录" pageJs="sortTable"  footerIndex=4>
    <@sugar.capsule css=['record']/>
<style>
    .active{color:#ff7d5a;}
</style>
<div class="crmList-box">
    <div class="feedbackList-pop show_now" style="display:none;z-index:100;position:fixed;background-color:rgba(125,125,125,.5);width:100%;height:100%">
        <ul style="background:#fff;margin-top:-0.1rem">
            <li class="tab_row" style="padding:.2rem 0"><a id="applwao  yService" style="display:inline-block;height:2.5rem;line-height:2.5rem;width:100%;color:#000;padding-left:1rem;font-size: .75rem;border-bottom: .05rem solid #cdd3d3"  href="record_list_and_statistics_extend.vpage" class="active">本月</a></li>
            <li class="tab_row" style="padding:.2rem 0"><a id="retroAction" style="height:2.5rem;line-height:2.5rem;display:inline-block;width:100%;color:#000;padding-left:1rem;font-size: .75rem;"   href="record_list_and_statistics_extend.vpage?queryMonth=lastMonth" class="active">上月</a></li>
        </ul>
    </div>
    <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isRegionManager()>
        <div class="c-opts js-tab c-flex c-flex-3" style="text-align: center;">
            <span class="js-sort js-key01 active" data-type="0"><i class="triangle"></i>大区</span>
            <span class="js-sort js-key02" data-type="1"><i class="triangle"></i>市经理</span>
            <span class="js-sort js-key03" data-type="2"><i class="triangle"></i>专员</span>
        </div>
    <#elseif requestContext.getCurrentUser().isCityManager()>
        <div class="c-opts js-tab c-flex c-flex-2" style="text-align: center;">
            <span class="js-sort js-key01 active" data-type="0"><i class="triangle"></i>市经理</span>
            <span class="js-sort js-key02" data-type="2"><i class="triangle"></i>专员</span>
        </div>
    </#if>
</div>
<div>
    <div class="tea-staffRes-main tab_box" style="margin-top:0">
        <#if requestContext.getCurrentUser().isCountryManager()>
            <div class="new_box01"></div>
            <div class="new_box02 pr-side">
                <table class="sideTable">
                    <thead>
                    <tr>
                        <td>姓名</td>
                        <td class="sortable">进校</td>
                        <td class="sortable">组会</td>
                        <td class="sortable">教研员</td>
                        <td class="sortable">陪访</td>
                        <td class="sortable">合计</td>
                    </tr>
                    </thead>
                    <#if regionStatisticsList?? && regionStatisticsList?size gt 0>
                    <tbody>
                        <#list regionStatisticsList as region>
                        <tr class="personalDetails" data-id="${region.workUserId!0}" style="cursor:pointer">
                            <td><span class="pr-name">${region.workUserRealName!0}</span></td>
                            <td>${region.intoSchoolCount!0}</td>
                            <td>${region.groupMeetingCount!0}</td>
                            <td>${region.researchersCount!0}</td>
                            <td>${region.visitCount!0}</td>
                            <td>${region.totals!0}t</td>
                            <td><span class="arrow"></span></td>
                        </tr>
                        </#list>
                    <#else>
                    <tr>
                        <td colspan="5" style="text-align: center;">
                            暂无工作记录
                        </td>
                    </tr>
                    </tbody>
                    </#if>
                </table>
            </div>
        <#else>
        <div class="new_box01">
                <div class="info">
                    <div class="inline-list data-list manager c-flex c-flex-4 record_5">
                        <div>
                        ${queryUserStatistics.intoSchoolCount!0}
                            <p>进校</p>
                        </div>
                        <div>
                        ${queryUserStatistics.groupMeetingCount!0}
                            <p>组会</p>
                        </div>
                        <div>
                        ${queryUserStatistics.researchersCount!0}
                            <p>教研员</p>
                        </div>
                        <div>
                        ${queryUserStatistics.visitCount!0}
                            <p>陪访</p>
                        </div>
                        <div>
                        ${queryUserStatistics.totals!0}t
                            <p>合计</p>
                        </div>
                    </div>
                </div>
        </div>
        <div class="new_box02 tea-staffRes-main">
        <#if queryUserRecordList?? && queryUserRecordList?size gt 0>
            <div class="manager c-flex">
                <#list queryUserRecordList as queryList>
                    <div style="padding:.4rem 0 .4rem .5rem;width:100%;font-size:.75rem;border-bottom:1px dashed #dde2ea">${queryList.sortDate!}
                    </div>
                    <#list queryList.workRecordListData as workData>
                        <a class="orange_font"  style="width: 100%;border-bottom:1px dashed #dde2ea;padding:.4rem 0 .4rem .5rem;font-size:.8rem;color:#636880;background:#fff;display:inline-block;" <#if workData.workRecordType == 'SCHOOL'> onclick='openSecond("/mobile/work_record/showSchoolRecord.vpage?recordId=${workData.workRecordId!0}")'  <#elseif  workData.workRecordType == 'VISIT'>onclick='openSecond("/mobile/work_record/show_visit_school_record.vpage?recordId=${workData.workRecordId!0}")'<#else> onclick='openSecond("/mobile/work_record/record_details.vpage?workRecordId=${workData.workRecordId!0}")'</#if>><div><#if workData.workRecordType == 'MEETING'><i class="icon-meeting"></i> <#elseif workData.workRecordType == 'SCHOOL'><i class="icon-school"></i> <#elseif workData.workRecordType == 'TEACHING'><i class="icon-teach"></i> <#elseif workData.workRecordType == 'VISIT'><i class="icon-visit"></i> </#if>${workData.workRecordRemarks!''}</div></a>
                    </#list>
                </#list>
            </div>
        <#else>
            <div class="nonInfo" style="text-align:center">
                暂无工作记录
            </div>
        </#if>
        </div>
        </#if>
    </div>
    <div class="pr-side tab_box" style="display:none">
        <#if requestContext.getCurrentUser().isCountryManager()>
            <div class="c-search" id="searchItem" style="clear:both">
                <input placeholder="请输入姓名" maxlength="30" id="schoolSearch01">
                <span class="js-search01" data-roleType="13">搜索</span>
            </div>
        </#if>
        <table class="sideTable">
            <thead>
            <tr>
                <td>姓名</td>
                <td class="sortable">进校</td>
                <td class="sortable">组会</td>
                <td class="sortable">教研员</td>
                <td class="sortable">陪访</td>
                <td class="sortable">合计</td>
            </tr>
            </thead>
            <#if cityStatisticsList?? && cityStatisticsList?size gt 0>
            <tbody class="new_man01">
                <#list cityStatisticsList as city>
                    <tr class="personalDetails" id="detail${city.workUserId!0}" data-id="${city.workUserId!0}" style="cursor: pointer;">
                        <td><span class="pr-name">${city.workUserRealName!0}</span></td>
                        <td>${city.intoSchoolCount!0}</td>
                        <td>${city.groupMeetingCount!0}</td>
                        <td>${city.researchersCount!0}</td>
                        <td>${city.visitCount!0}</td>
                        <td>${city.totals!0}t</td>
                        <td><span class="arrow"></span></td>
                    </tr>
                </#list>
            <#else>
                <tr>
                    <td colspan="5" style="text-align: center;">
                        暂无工作记录
                    </td>
                </tr>
            </tbody>
            </#if>
        </table>
    </div>
    <div class="pr-side tab_box" style="display:none">
        <#if requestContext.getCurrentUser().isCountryManager()>
            <div class="c-search" id="searchItem" style="clear:both">
                <input placeholder="请输入姓名" maxlength="30" id="schoolSearch02">
                <span class="js-search02" data-roleType="21">搜索</span>
            </div>
        </#if>
        <table class="sideTable">
            <thead>
            <tr>
                <td>姓名</td>
                <td class="sortable">进校</td>
                <td class="sortable">组会</td>
                <td class="sortable">教研员</td>
                <td class="sortable">陪访</td>
                <td class="sortable">合计</td>
            </tr>
            </thead>
            <#if businessDeveloperStatisticsList?? && businessDeveloperStatisticsList?size gt 0>
            <tbody class="new_man02">
                <#list businessDeveloperStatisticsList as business>
                <tr class="personalDetails" id="detail${business.workUserId!0}" data-id="${business.workUserId!0}" style="cursor:pointer;">
                    <td><span class="pr-name">${business.workUserRealName!0}</span></td>
                    <td>${business.intoSchoolCount!0}</td>
                    <td>${business.groupMeetingCount!0}</td>
                    <td>${business.researchersCount!0}</td>
                    <td>${business.visitCount!0}</td>
                    <td>${business.totals!0}t</td>
                    <td><span class="arrow"></span></td>
                </tr>
                </#list>
            <#else>
            <tr>
                <td colspan="5" style="text-align: center;">
                    暂无工作记录
                </td>
            </tr>
            </tbody>
            </#if>
        </table>
    </div>
</div>
<script type="text/html" id="new_man">
    <%for(var i = 0; i< statisticsList.length ;i++){%>
        <%var statiscs = statisticsList[i]%>
        <tr  class="personalDetails" id="detail<%=statiscs.workUserId%>" data-id="<%=statiscs.workUserId%>" style="cursor:pointer;">
            <td><span class="pr-name"><%=statiscs.workUserRealName%></span></td>
            <td><%=statiscs.intoSchoolCount%></td>
            <td><%=statiscs.groupMeetingCount%></td>
            <td><%=statiscs.researchersCount%></td>
            <td><%=statiscs.visitCount%></td>
            <td><%=statiscs.totals%>t</td>
            <td><span class="arrow"></span></td>
        </tr>
    <%}%>
</script>
<script>
    var AT = new agentTool();
    var month = "${queryMonth!''}";

    $(document).on('click','.js-tab span',function(){
        $(this).addClass("active").siblings("span").removeClass("active");
        var type = $(this).data("type");
        $('.tab_box').eq(type).show().siblings().hide();
    });
    $(document).on('click','.personalDetails',function(){
        var workUserId = $(this).attr('data-id');
        AT.setCookie("workUserId",workUserId);
        openSecond("/mobile/work_record/record_list_and_statistics.vpage?workUserId=" + workUserId +"&queryMonth=" + month) ;
    });
    $(document).on('click','.js-search02',function(){
        $(".sortable").removeClass("sorted");
        var roleType = $(this).attr("data-roleType");
        var schoolInput = $("#schoolSearch02").val();
        if(schoolInput != ""){
            $.get("record_statistics_list.vpage",{queryMonth:month,roleType:roleType,queryCriteria:schoolInput},function(data){
                if(data.success){
                    if(data.statisticsList != null && data.statisticsList.length>0){
                        renderTemplate("new_man",{"statisticsList":data.statisticsList},".new_man02");
                    }else{
                        AT.alert('暂无数据')
                    }
                }else{
                    AT.alert(data.info);
                }
            });
        }else{
            AT.alert("请输入姓名");
        }
    });
    $(document).on("click",".sortable",function () {
        $(this).addClass("active").siblings().removeClass("active");
        var colIndex = $(this).index();
        var table = $(this).closest("table");
        $(this).addClass("active").siblings().removeClass("active");
        sortTable(table, colIndex);
    });
    $(document).on('click','.js-search01',function(){
        $(".sortable").removeClass("sorted");
        var roleType = $(this).attr("data-roleType");
        var schoolInput = $("#schoolSearch01").val();
        if(schoolInput != ""){
            $.get("record_statistics_list.vpage",{queryMonth:month,roleType:roleType,queryCriteria:schoolInput},function(data){
                if(data.success){
                    if(data.statisticsList != null && data.statisticsList.length>0){
                        renderTemplate("new_man",{"statisticsList":data.statisticsList},".new_man01");
                    }else{
                        AT.alert('暂无数据')
                    }
                }else{
                    AT.alert(data.info);
                }
            });
        }else{
            AT.alert("请输入姓名");
        }
    });
    $(document).on('click','.js-sort',function(){
        var setKey = $(this).data().type;
        AT.setCookie("dataType",setKey);
    });
    var csid = AT.getCookie("workUserId");
    if(csid){
        var detail = $("#detail"+ csid);
        console.log($("#detail"+csid).length);
        if(detail.length != 0){
            var scroll_offset = $("#detail"+csid).offset();
            $("body,html").animate({
                scrollTop:parseFloat(scroll_offset.top) - 184 // 减掉被顶部和筛选条遮挡的部分
            },0);
        }
    }
    var getType = AT.getCookie("dataType");
    if(getType){
        if($('.js-key01').data().type == getType){
            $('.js-key01').click();
        }else if($('.js-key02').data().type == getType){
            $('.js-key02').click();
        }else if($('.js-key03').data().type == getType){
            $('.js-key03').click();
        }
    }
    var renderTemplate = function(tempSelector,data,container){
        var contentHtml = template(tempSelector, data);
        $(container).html(contentHtml);
    };
    $(document).ready(function () {
        YQ.voxLogs({
            database : "marketing", //不设置database  , 默认库web_student_logs
            module : "m_RSEOqns3", //打点流程模块名
            op : "o_amWatuui" ,//打点事件名
            userId:${requestContext.getCurrentUser().getUserId()!0} //登录用户Id
        });
            var setTopBar = {
                show:true,
                rightText:'',
                rightTextColor:"ff7d5a",
                 rightImage:window.location.protocol+ "//" + window.location.host + "/public/rebuildRes/image/mobile/home/add_new.png",
                needCallBack:true
            } ;
            var topBarCallBack = function () {
                if($('.feedbackList-pop').hasClass('show_now')){
                    $('.feedbackList-pop').removeClass('show_now').show();
                }else{
                    $('.feedbackList-pop').addClass('show_now').hide();
                }
            };
            setTopBarFn(setTopBar,topBarCallBack);
    })
</script>
</@layout.page>