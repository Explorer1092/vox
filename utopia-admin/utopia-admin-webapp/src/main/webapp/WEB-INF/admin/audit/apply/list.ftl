<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="申请管理" page_num=21>
<div class="span9">
    <fieldset><legend>我的申请</legend></fieldset>
    <div>
        <#if workFlowRecordList?has_content>
            <ul class="inline">
                <li>
                    <a id='firstpageId' href="javascript:void(0)">首页</a>
                </li>
                <li>
                    <a id='prepageId' href="javascript:void(0)">上一页</a>
                </li>
                <li>
                    <strong id="currentPageId" data-currentpage='${currentPage!"1"}'>${currentPage!"1"}</strong> / <strong id="totalPageId" data-totalpage='${totalPage!"1"}'>${totalPage!"1"}</strong>
                </li>
                <li>
                    <a id='nextpageId' href="javascript:void(0)">下一页</a>
                </li>
                <li>
                    <a id='lastpageId' href="javascript:void(0)">末页</a>
                </li>
            </ul>
        </#if>
    </div>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <div id="data_table_journal">
                    <table class="table table-striped table-bordered table-hover">
                        <thead>
                        <tr>
                            <th width="13%" style="text-align: center;">活动</th>
                            <th style="text-align: center;">内容</th>
                            <th width="150px" style="text-align: center;">创建时间</th>
                            <th width="130px" style="text-align: center;">创建人</th>
                            <th width="180px" style="text-align: center;">审核状态</th>
                        </tr>
                        </thead>
                        <#if workFlowRecordList??>
                            <tbody>
                                <#list workFlowRecordList as workFlowRecord>
                                <tr>
                                    <td style="text-align: center;">
                                        <#if (workFlowRecord.workFlowType)?? && (workFlowRecord.workFlowType) == 'ADMIN_SEND_APP_PUSH'>
                                            <a href="/audit/apppush/apppushapply.vpage?ct=1&id=${workFlowRecord.id!}"> ${workFlowRecord.taskName!''}</a>
                                        <#elseif (workFlowRecord.workFlowType)?? && (workFlowRecord.workFlowType) == 'ADMIN_WECHAT_NOTICE'>
                                            <a href="/audit/wechat/wechatapply.vpage?ct=1&id=${workFlowRecord.id!}"> ${workFlowRecord.taskName!''}</a>
                                        <#else>
                                            ${workFlowRecord.taskName!''}
                                        </#if>
                                    </td>
                                    <td style="text-align: center;">${workFlowRecord.taskContent!''}
                                    <td style="text-align: center;">${workFlowRecord.createDatetime?string('yyyy-MM-dd HH:mm:ss')?replace(" ", "<br/>")}</td>
                                    <td style="text-align: center;">${workFlowRecord.creatorName!''}</td>
                                    <td style="text-align: center;">
                                        <#if workFlowRecord.status=='lv1' || workFlowRecord.status=='lv2' || workFlowRecord.status=='lv3' >
                                            待审核(${workFlowRecord.status!''})
                                        <#elseif workFlowRecord.status=='rejected'>
                                            被驳回(${workFlowRecord.status!''})
                                        <#elseif workFlowRecord.status=='processed'>
                                            已处理(${workFlowRecord.status!''})
                                        </#if>
                                    </td>
                                </tr>
                                </#list>
                            </tbody>
                        </#if>
                </div>
            </div>
        </div>
    </div>

</div>

<script type="text/javascript">
    $(function(){
        $('#firstpageId').click(function () {
            var currentPage = $('#currentPageId').attr('data-currentpage');
            if(currentPage == 1){
                return;
            }
            currentPage = 1;
            $('#currentPageId').attr('data-currentpage',currentPage);
            window.location.href="list.vpage?currentPage="+currentPage;
        });

        $('#prepageId').click(function () {
            var currentPage = $('#currentPageId').attr('data-currentpage');
            if(currentPage == 1){
                return;
            }
            currentPage = new Number(currentPage);
            currentPage = currentPage -1;
            $('#currentPageId').attr('data-currentpage',currentPage);
            window.location.href="list.vpage?currentPage="+currentPage;
        });

        $('#nextpageId').click(function () {
            var currentPage = $('#currentPageId').attr('data-currentpage');
            var totalPage =$('#totalPageId').attr('data-totalpage');
            if(currentPage == totalPage){
                return;
            }
            currentPage = new Number(currentPage);
            currentPage =currentPage +1;
            $('#currentPageId').attr('data-currentpage',currentPage);
            window.location.href="list.vpage?currentPage="+currentPage;
        });

        $('#lastpageId').click(function () {
            var currentPage = $('#currentPageId').attr('data-currentpage');
            var totalPage =$('#totalPageId').attr('data-totalpage');
            if(totalPage ==1 || currentPage == totalPage){
                return;
            }
            currentPage = totalPage;
            $('#currentPageId').attr('data-currentpage',currentPage);
            window.location.href="list.vpage?currentPage="+currentPage;
        });
    });
</script>
</@layout_default.page>
