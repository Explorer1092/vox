<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="申请管理" page_num=21>
<style>
    .product_content li:nth-child(1){
        margin-left:50px
    }
    .product_content li{
        text-decoration: none;
        list-style:none;
        float:left;
        margin-left:100px
    }
    #evaluate_table tr {
        border-bottom: 1px solid #e0e0e0;
    }
    #evaluate_table th {
        text-align:center;
        width:90px;
    }
    #evaluate_table th:nth-child(6){
        width:290px
    }
    .strong{
        width:100px;
        margin-left:50px;
        margin-top:15px;
        display:inline-block
    }
    span{
        margin-left:30px
    }
</style>
<div class="pic_alert" style="width:100%;height: 100%;position:fixed;top:0;background: #000;z-index:111;display:none">
    <img style="width:30%;vertical-align: middle;position:absolute;left:35%" src="" alt="">
</div>
<div class="span9">
    <fieldset><legend>产品反馈详情</legend></fieldset>
    <#include '../workflow/product_content.ftl'>
    <div>
        <div>
            <label>
                <strong class="strong">学科</strong>
                <span><#if applyData.apply.teacherSubject?has_content>${applyData.apply.teacherSubject.desc!"-"}</#if></span>
            </label>
        </div>
        <div>
            <label>
                <strong class="strong">反馈类型</strong>
                <span>${applyData.apply.feedbackType.desc!"-"}</span>
            </label>
        </div>
        <div>
            <label>
                <strong class="strong">三级分类</strong>
                <span>
                    <#if applyData.apply.firstCategory?has_content>${applyData.apply.firstCategory!"-"}</#if>
                    <#if applyData.apply.secondCategory?has_content>/${applyData.apply.secondCategory!"-"}</#if>
                    <#if applyData.apply.thirdCategory?has_content>/${applyData.apply.thirdCategory!"-"}</#if>
                </span>
            </label>
        </div>
        <div>
            <label>
                <strong class="strong">指定PM</strong>
                <span>${applyData.apply.pmAccountName!"-"}</span>
            </label>
        </div>
        <div>
            <label>
                <strong class="strong">感谢老师</strong>
                <span>${applyData.apply.noticeContent!"-"}</span>
            </label>
        </div>
        <div>
            <label>
                <strong class="strong">预计上线时间</strong>
                <span><#if applyData.apply.onlineEstimateDate?has_content>${applyData.apply.onlineEstimateDate!}<#else> -</#if></span>
            </label>
        </div>
        <div>
            <label>
                <strong class="strong">是否上线</strong>
                <span><#if applyData.apply.onlineFlag!false>已经上线<#else>-</#if></span>
            </label>
        </div>
        <div>
            <label>
                <strong class="strong">上线通知</strong>
                <span>${applyData.apply.onlineNotice!'-'}</span>
            </label>
        </div>
        <div>
            <label><strong class="strong">处理记录</strong></label>
            <div style="margin-top:25px">
                <table class="table table-striped table-bordered" style="font-size: 14px;">
                    <thead>
                    <tr>
                        <th>处理日期</th>
                        <th>操作人</th>
                        <th>处理结果</th>
                        <th>处理意见</th>
                    </tr>
                    </thead>
                    <#if applyData.processResultList?has_content>
                        <#list applyData.processResultList as processResult>
                            <tr>
                                <td><#if processResult.processDate?has_content>${processResult.processDate?string("yyyy-MM-dd")}</#if></td>
                                <td>${processResult.accountName!}</td>
                                <td>${processResult.result!}</td>
                                <td>${processResult.processNotes!}</td>
                            </tr>
                        </#list>
                    </#if>
                </table>
            </div>
        </div>
    </div>
</div>
</@layout_default.page>
<script>
    $(document).on('click','.pic_show',function(){
        var pic_src = $(this).find('img').attr('src');
        $('.pic_alert').show().find('img').attr('src',pic_src);
    });
    $(document).on('click','.pic_alert',function(){
        $(this).hide();
    });
</script>