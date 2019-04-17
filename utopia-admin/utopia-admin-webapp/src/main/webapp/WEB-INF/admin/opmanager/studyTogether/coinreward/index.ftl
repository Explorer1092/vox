<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<style>
    input {width: 150px;}
</style>
<div class="span9">
    <fieldset>
        <legend><span style="color: #00a0e9">连续学习奖励管理</span></legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <ul class="inline">
                <li>
                    <label>连续奖励ID&nbsp;
                        <input type="text" id="cscrId" name="cscrId" value="${cscrId!''}"/>
                    </label>
                </li>
                <li>
                    <label>学习币类型ID&nbsp;
                        <input type="text" id="coinTypeId" name="coinTypeId" value="${coinTypeId!''}"/>
                    </label>
                </li>
                <li>
                    <label>周数&nbsp;
                        <input type="text" id="weekCount" name="weekCount" value="${weekCount!''}"/>
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

    <button type="button" class="btn btn-success js-couponOption" data-type="add">新增连续奖励</button>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>连续奖励ID</th>
                        <th>学习币类型ID</th>
                        <th>学习币数量</th>
                        <th>家长奖励类型</th>
                        <th>学豆数量</th>
                        <th>周数</th>
                        <th>天数</th>
                        <th>创建人</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as cscr>
                            <tr>
                                <td>${cscr.id!''}</td>
                                <td>${cscr.coinTypeId!''}</td>
                                <td>${cscr.coinCount!''}</td>
                                <td>${cscr.rewardType!''}</td>
                                <td>${cscr.rewardCount!''}</td>
                                <td>${cscr.weekCount!''}</td>
                                <td>${cscr.dayCount!''}</td>
                                <td>${cscr.createUser!''}</td>
                                <td>
                                    <a href="javascript:" class="btn btn-primary js-couponOption" data-type="info" data-cid="${cscr.id!''}">详情</a>
                                    <a href="javascript:" class="btn btn-success js-couponOption" data-type="edit" data-cid="${cscr.id!''}">编辑</a>
                                    <a href="javascript:" class="btn btn-warning js-couponOption" data-type="logs" data-cid="${cscr.id!''}">日志</a>
                                </td>
                            </tr>
                            </#list>
                        <#else >
                        <tr>
                            <td colspan="8" style="text-align: center">暂无数据</td>
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
                        'edit': '?cscrId=' + cid,
                        'info': '?cscrId=' + cid,
                        'logs': '?cscrId=' + cid
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