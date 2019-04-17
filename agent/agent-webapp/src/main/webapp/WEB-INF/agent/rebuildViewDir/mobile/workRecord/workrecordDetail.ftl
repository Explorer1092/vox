<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="工作记录" pageJs=""  footerIndex=4>
    <@sugar.capsule css=['record']/>
<div>
    <div class="tea-staffRes-main tab_box">
            <div class="new_box01" style="margin-top:.5rem">
                <div class="info">
                    <div class="inline-list data-list manager c-flex c-flex-4 record_5">
                        <div>
                            <#if statistics?has_content && statistics.intoSchoolCount??>
                                    ${statistics.intoSchoolCount!0}
                            </#if>
                            <p>进校</p>
                        </div>
                        <div>
                            <#if statistics?has_content && statistics.intoSchoolCount??>
                                ${statistics.groupMeetingCount!0}
                            </#if>
                            <p>组会</p>
                        </div>
                        <div>
                            <#if statistics?has_content && statistics.intoSchoolCount??>
                                ${statistics.researchersCount!0}
                            </#if>
                            <p>教研员</p>
                        </div>
                        <div>
                            <#if statistics?has_content && statistics.intoSchoolCount??>
                                ${statistics.visitCount!0}
                            </#if>
                            <p>陪访</p>
                        </div>
                        <div>
                            <#if statistics?has_content && statistics.intoSchoolCount??>
                                ${statistics.totals!0}t
                            </#if>
                            <p>合计</p>
                        </div>
                    </div>
                </div>
            </div>
            <div class="new_box02">
                <#if recordList?? && recordList?size gt 0>
                    <div class=" manager c-flex ">
                        <#list recordList as queryList>
                            <div style="padding:.4rem 0 .4rem .5rem;width:100%;font-size:.75rem;border-bottom:1px dashed #dde2ea">${queryList.sortDate!}
                            </div>
                            <#list queryList.workRecordListData as workData>
                                <a class="orange_font"  style="width: 100%;border-bottom:1px dashed #dde2ea;padding:.4rem 0 .4rem .5rem;font-size:.8rem;color:#636880;background:#fff;display:inline-block;" <#if workData.workRecordType == 'SCHOOL'> href="/mobile/work_record/showSchoolRecord.vpage?recordId=${workData.workRecordId!0}" <#elseif  workData.workRecordType == 'VISIT'>href="show_visit_school_record.vpage?recordId=${workData.workRecordId!0}" <#else>href="record_details.vpage?workRecordId=${workData.workRecordId!0}"</#if>><div><#if workData.workRecordType == 'MEETING'>(会)<#elseif workData.workRecordType == 'SCHOOL'>(校)<#elseif workData.workRecordType == 'TEACHING'>(教)<#elseif workData.workRecordType == 'VISIT'>(陪)</#if>${workData.workRecordRemarks!''}</div></a></td>
                            </#list>
                        </#list>
                    </div>
                <#else>
                    <div class="nonInfo" style="text-align:center">
                        暂无工作记录
                    </div>
                </#if>
            </div>
    </div>
</div>
<script>
    var month = "${queryMonth!''}";
    $(document).on('click','.js-tab span',function(){
        $(this).addClass("active").siblings("span").removeClass("active");
        var type = $(this).data("type");
        console.log(type);
        $('.tab_box').eq(type).show().siblings().hide();
    });
    $(document).on('click','.personalDetails',function(){
        var workUserId = $(this).attr('data-id');
        $('.tab_box').eq(0).show().siblings().hide();
        $('.js-tab span').eq(0).addClass("active").siblings("span").removeClass("active");
        openSecond("/mobile/work_record/record_list_and_statistics.vpage?workRecordId=" + workUserId +"&queryMonth=" + month +"");
    });
    $('.js-showHand').on('click',function(){
        if($('.feedbackList-pop').hasClass('show_now')){
            $('.feedbackList-pop').removeClass('show_now').show();
        }else{
            $('.feedbackList-pop').addClass('show_now').hide();
        }

    });
    $(document).ready(function () {
        YQ.voxLogs({
            database : "marketing", //不设置database  , 默认库web_student_logs
            module : "m_RSEOqns3", //打点流程模块名
            op : "o_amWatuui" ,//打点事件名
            userId:${requestContext.getCurrentUser().getUserId()!0} //登录用户Id
        });
    })
</script>
</@layout.page>