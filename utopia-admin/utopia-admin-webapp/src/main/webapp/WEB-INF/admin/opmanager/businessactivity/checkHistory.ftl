<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="导流活动管理" page_num=9>
<#import "../../common/pager.ftl" as pager />
<div id="main_container" class="span9">
    <legend>
        <strong>导流活动管理--查看订单</strong>
        <form id="query_frm" class="form-horizontal" method="get"
              action="${requestContext.webAppContextPath}/opmanager/businessactivity/checkHistory.vpage" >
            <input type="hidden" id="page" name="page" value="${currentPage!'1'}"/>
            <input type="hidden" id="aid" name="aid" value="${aid!''}"/>
            <input type="hidden" id="type" name="type" value="${type!''}"/>
        </form>
        <a id="dataExport" href="downloadinfo.vpage?aid=${aid!''}&type=${type!''}" role="button" class="btn btn-inverse">导出全部</a>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <@pager.pager/>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>报名/支付时间</th>
                        <th>学号</th>
                        <th>学生姓名</th>
                        <th>家长号</th>
                        <th>称谓</th>
                        <th>年级</th>
                        <th>预约手机号</th>
                        <th>学生手机号</th>
                        <th>家长手机号</th>
                        <th>状态</th>
                        <th width="90px">用户备注</th>
                        <th>来源</th>
                        <th>外部流水号</th>
                        <th>支付渠道</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if dataListPage?? && dataListPage.content?? >
                            <#list dataListPage.content as item >
                                <tr>
                                    <td>${item.successDate!''}</td>
                                    <td>${item.studentId!''}</td>
                                    <td>${item.studentName!''}</td>
                                    <td>${item.parentId!''}</td>
                                    <td>${item.callName!''}</td>
                                    <td>${item.clazzName!''}</td>
                                    <td>${item.mobile!''}</td>
                                    <td><input type="button" value="查看"  class="btn btn-info stuMob" data-stuid="${item.id!''}"> <i></i></td>
                                    <td><input type="button" value="查看"  class="btn btn-info parMob" data-teaid="${item.id!''}"> <i></i></td>
                                    <td>${item.status}</td>
                                    <td>${item.remark!''}</td>
                                    <td>${item.track!''}</td>
                                    <td>${item.outTradeNo!''}</td>
                                    <td>${item.payChannel!''}</td>
                                </tr>
                            </#list>
                        </#if>
                    </tbody>
                    <tfoot>
                    <#if dataListPage.content?size==0>
                        <tr><td colspan="11">暂时没有条目~</td></tr>
                    </#if>
                    </tfoot>
                </table>
                <@pager.pager/>
            </div>
        </div>
    </div>
</div>
<style>
    .table td , .table th{
        padding: 8px;
        line-height: 20px;
        text-align: center;
        vertical-align: middle;
        border-top: 1px solid #dddddd;
    }
</style>
<script>
    $(function(){
        // 查询号码
        function queryMobile(type,  index, currentId) {
            $.get('/opmanager/businessactivity/getMobile.vpage', {id: currentId}).done(function (res) {
                if (type === 'student') {
                    $(".stuMob").eq(index).hide().siblings('i').text(res.studentMobile);
                } else {
                    $(".parMob").eq(index).hide().siblings('i').text(res.parentMobile);
                }
            })
        }

        $(document).on('click', '.stuMob', function () {
            var index = $(".stuMob").index(this);
            var currentStuId = $(this).attr('data-stuid');
            queryMobile('student', index, currentStuId);
        });

        $(document).on('click', '.parMob', function () {
            var index = $(".parMob").index(this);
            var currentTeaId = $(this).attr('data-teaid');
            queryMobile('teacher', index, currentTeaId);
        });
    });
</script>

</@layout_default.page>