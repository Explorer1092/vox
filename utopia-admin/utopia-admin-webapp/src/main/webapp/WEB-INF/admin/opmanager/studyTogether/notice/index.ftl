<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<style>
    input {width: 100px;}
    select {width: 130px;}
</style>
<div class="span9">
    <fieldset>
        <legend><span style="color: #00a0e9">通知管理</span></legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <ul class="inline">
                <li>
                    <label>通知ID&nbsp;
                        <input type="text" id="noticeId" name="noticeId" value="${noticeId!''}"/>
                    </label>
                </li>
                <li>
                    <label>课程ID&nbsp;
                        <input type="text" id="skuId" name="skuId" value="${skuId!''}" />
                    </label>
                </li>
                <li>
                    <label>通知类型&nbsp;
                        <select id="type" name="type">
                            <option value="-1">全部</option>
                            <option value="1" <#if type?? && type == 1>selected</#if>>只弹一次</option>
                            <option value="2" <#if type?? && type == 2>selected</#if>>时间间隔</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label>创建人&nbsp;
                        <input type="text" id="createUser" name="createUser" value="${createUser!''}"/>
                    </label>
                </li>
                <li><button type="button" class="btn btn-primary" id="searchBtn">查询</button></li>
            </ul>
        </div>
    </form>

    <button type="button" class="btn btn-success js-couponOption" data-type="add">新增通知</button>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>通知ID</th>
                        <th>课程ID</th>
                        <th>通知类型</th>
                        <th>间隔天数</th>
                        <th>开始日期</th>
                        <th>结束日期</th>
                        <th>创建人</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as notice>
                            <tr>
                                <td>${notice.id!''}</td>
                                <td>${notice.skuId!''}</td>
                                <td><#if notice.type?? && notice.type == 1>只弹一次<#elseif notice.type?? && notice.type == 2>时间间隔</#if></td>
                                <td><#if notice.type?? && notice.type == 1>-<#elseif notice.type?? && notice.type == 2>${notice.intervalDay!''}</#if></td>
                                <td>${notice.startDate!''}</td>
                                <td>${notice.endDate!''}</td>
                                <td>${notice.createUser!''}</td>
                                <td>
                                    <a href="javascript:" class="btn btn-primary js-couponOption" data-type="info" data-cid="${notice.id!''}">详情</a>
                                    <a href="javascript:" class="btn btn-success js-couponOption" data-type="edit" data-cid="${notice.id!''}">编辑</a>
                                    <a href="javascript:" class="btn btn-warning js-couponOption" data-type="logs" data-cid="${notice.id!''}">日志</a>
                                </td>
                            </tr>
                            </#list>
                        <#else >
                        <tr>
                            <td colspan="10" style="text-align: center">暂无数据</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
                <ul class="message_page_list"></ul>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {

        $(".message_page_list").page({
            total: ${totalPage!},
            current: ${currentPage!},
            autoBackToTop: false,
            maxNumber: 20,
            jumpCallBack: function (index) {
                $("#pageNum").val(index);
                $("#op-query").submit();
            }
        });

        $("#searchBtn").on('click', function () {
            $("#pageNum").val(1);
            $("#op-query").submit();
        });

        $(document).on('click',".js-couponOption",function () {
            var $this = $(this),
                    type = $this.data('type'),
                    cid = $this.data('cid'),
                    mapLink = {
                        'add' : '',
                        'edit': '?noticeId=' + cid,
                        'info': '?noticeId=' + cid,
                        'logs': '?noticeId=' + cid
                    };
            var url = '';
            if(type === "info"){
                url = 'info.vpage'+ mapLink[type];
            } else if (type === "logs") {
                url = 'logs.vpage' + mapLink[type];
            } else if (type === "edit") {
                url = 'details.vpage' + mapLink[type];
            } else {
                url = 'details.vpage';
            }
            window.open(url, '_blank').location;
        });
    });
</script>
</@layout_default.page>