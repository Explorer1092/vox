<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<style>
    input {width: 100px;}
    select {width: 130px;}
</style>
<div class="span9">
    <fieldset>
        <legend><span style="color: #00a0e9">分享管理</span></legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <ul class="inline">
                <li>
                    <label>分享ID&nbsp;
                        <input type="text" id="shareId" name="shareId" value="${shareId!''}"/>
                    </label>
                </li>
                <li>
                    <label>课程ID&nbsp;
                        <input type="text" id="skuId" name="skuId" value="${skuId!''}" />
                    </label>
                </li>
                <li>
                    <label>分享类型&nbsp;
                        <select id="type" name="type">
                            <option value="all">全部</option>
                            <option value="default" <#if type?? && type == "default">selected</#if>>电子书</option>
                            <option value="rank" <#if type?? && type == "rank">selected</#if>>排行榜</option>
                            <option value="report" <#if type?? && type == "report">selected</#if>>课程报告</option>
                            <option value="diploma" <#if type?? && type == "diploma">selected</#if>>毕业证书</option>
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

    <button type="button" class="btn btn-success js-couponOption" data-type="add">新增分享</button>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>分享ID</th>
                        <th>课程ID</th>
                        <th>分享标题</th>
                        <th>类型</th>
                        <th>创建人</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as share>
                            <tr>
                                <td>${share.id!''}</td>
                                <td>${share.skuId!''}</td>
                                <td>${share.title!''}</td>
                                <td>
                                    <#if share.type?? && share.type == "report">课程报告
                                    <#elseif share.type?? && share.type == "rank">排行榜
                                    <#elseif share.type?? && share.type == "default">电子书
                                    <#elseif share.type?? && share.type == "diploma">毕业证书
                                    </#if>
                                </td>
                                <td>${share.createUser!''}</td>
                                <td>
                                    <a href="javascript:" class="btn btn-primary js-couponOption" data-type="info" data-cid="${share.id!''}">详情</a>
                                    <a href="javascript:" class="btn btn-success js-couponOption" data-type="edit" data-cid="${share.id!''}">编辑</a>
                                    <a href="javascript:" class="btn btn-warning js-couponOption" data-type="logs" data-cid="${share.id!''}">日志</a>
                                </td>
                            </tr>
                            </#list>
                        <#else >
                        <tr>
                            <td colspan="6" style="text-align: center">暂无数据</td>
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
                        'edit': '?shareId=' + cid,
                        'info': '?shareId=' + cid,
                        'logs': '?shareId=' + cid
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