<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="微信消息发送和审核" page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>

<div id="main_container" class="span9">
    <legend style="font-weight: 700;">微信任务管理&nbsp;&nbsp;&nbsp;&nbsp;
        <a id="new-sms-btn" class="btn btn-info" href="wechatbatchdetail.vpage">
            <i class="icon-envelope icon-white"></i>
            新建微信任务
        </a>
    </legend>
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
                                <th style="text-align: center;">详情</th>
                                <th width="150px" style="text-align: center;">创建时间</th>
                                <th width="130px" style="text-align: center;">创建人</th>
                                <th width="80px" style="text-align: center;">审核状态</th>
                                <th width="200px" style="text-align: center;">操作</th>
                            </tr>
                        </thead>
                        <#if workFlowRecordList??>
                            <tbody>
                            <#list workFlowRecordList as workFlowRecord>
                            <tr class="success">
                                <td style="text-align: center;">${workFlowRecord.taskName!''}</td>
                                <td style="text-align: center;">${workFlowRecord.taskContent!''}</td>
                                <td style="text-align: center;"><a href=${workFlowRecord.taskDetailUrl!''} target="_blank">${workFlowRecord.taskDetailUrl!'--'}</a></td>
                                <td style="text-align: center;">${workFlowRecord.createDatetime!''}</td>
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
                                <td style="text-align: center;">
                                    <#if workFlowRecord.status == 'lv1'>
                                        <a data-id=${workFlowRecord.id!''} data-type="send" class="btn btn-info operation" >发送</a>
                                        <a data-id=${workFlowRecord.id!''} data-type="reject" class="btn btn-info operation" >驳回</a>
                                        <a data-id=${workFlowRecord.id!''} data-type="raiseup" class="btn btn-info operation" >转上一级</a>
                                    <#elseif workFlowRecord.status == 'lv2'>
                                        <a data-id=${workFlowRecord.id!''} data-type="send" class="btn btn-info operation" >发送</a>
                                        <a data-id=${workFlowRecord.id!''} data-type="reject" class="btn btn-info operation" >驳回</a>
                                        <a data-id=${workFlowRecord.id!''} data-type="raiseup" class="btn btn-info operation" >转上一级</a>
                                    <#elseif workFlowRecord.status == 'lv3'>
                                        <a data-id=${workFlowRecord.id!''} data-type="send" class="btn btn-info operation" >发送</a>
                                        <a data-id=${workFlowRecord.id!''} data-type="reject" class="btn btn-info operation" >驳回</a>
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
    $(function () {
        $(".operation").on('click', function() {
            var workFlowRecordId = $(this).data("id");
            var operationType = $(this).data("type");

            $.post('/crm/workflow/checkwechatmsg.vpage',{"workFlowRecordId":workFlowRecordId,"operationType":operationType},function (data){
                if(data.success){
                    alert("操作成功");
                    window.location.reload();
                }else{
                    alert(data.info);
                }
            })

        });

        $('#firstpageId').click(function () {
            var currentPage = $('#currentPageId').attr('data-currentpage');
            if(currentPage == 1){
                return;
            }
            currentPage = 1;
            $('#currentPageId').attr('data-currentpage',currentPage);
            window.location.href="wechatbatchindex.vpage?currentPage="+currentPage;
        });

        $('#prepageId').click(function () {
            var currentPage = $('#currentPageId').attr('data-currentpage');
            if(currentPage == 1){
                return;
            }
            currentPage = new Number(currentPage);
            currentPage = currentPage -1;
            $('#currentPageId').attr('data-currentpage',currentPage);
            window.location.href="wechatbatchindex.vpage?currentPage="+currentPage;
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
            window.location.href="wechatbatchindex.vpage?currentPage="+currentPage;
        });

        $('#lastpageId').click(function () {
            var currentPage = $('#currentPageId').attr('data-currentpage');
            var totalPage =$('#totalPageId').attr('data-totalpage');
            if(totalPage ==1 || currentPage == totalPage){
                return;
            }
            currentPage = totalPage;
            $('#currentPageId').attr('data-currentpage',currentPage);
            window.location.href="wechatbatchindex.vpage?currentPage="+currentPage;
        });
    });
</script>
</@layout_default.page>