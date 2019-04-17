<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<style>
    input {width: 120px;}
    select {width: 130px;}
</style>
<div class="span9">
    <fieldset>
        <legend>周奖励管理</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <ul class="inline">
                <li>
                    <label>奖励ID&nbsp;
                        <input type="text" id="rewardId" name="rewardId" value="${rewardId!''}" />
                    </label>
                </li>
                <li>
                    <label>奖励名称&nbsp;
                        <input type="text" id="rewardName" name="rewardName" value="${rewardName!''}"/>
                    </label>
                </li>
                <li>
                    <label>奖励类型&nbsp;
                        <select id="rewardType" name="rewardType">
                            <option value="-1">全部</option>
                            <option value="2" <#if rewardType?? && rewardType == 2>selected</#if>>视频</option>
                            <option value="3" <#if rewardType?? && rewardType == 3>selected</#if>>学习币</option>
                            <option value="4" <#if rewardType?? && rewardType == 4>selected</#if>>电子书</option>
                            <option value="5" <#if rewardType?? && rewardType == 5>selected</#if>>周报告</option>
                            <option value="6" <#if rewardType?? && rewardType == 6>selected</#if>>家长奖励</option>
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

    <button type="button" class="btn btn-success js-couponOption" data-type="add">新增周奖励</button>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>奖励ID</th>
                        <th>奖励名称</th>
                        <th>奖励类型</th>
                        <th>创建人</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as reward>
                            <tr>
                                <td>${reward.id!''}</td>
                                <td>${reward.name!''}</td>
                                <td><#if reward.type?? && reward.type == 2>视频<#elseif reward.type?? && reward.type == 3>学习币<#elseif reward.type?? && reward.type == 4>电子书<#elseif reward.type?? && reward.type == 5>周报告<#elseif reward.type?? && reward.type == 6>家长奖励</#if></td>
                                <td>${reward.createUser!''}</td>
                                <td>
                                    <a href="javascript:" class="btn btn-primary js-couponOption" data-type="info" data-cid="${reward.id!''}">详情</a>
                                    <a href="javascript:" class="btn btn-success js-couponOption" data-type="edit" data-cid="${reward.id!''}">编辑</a>
                                    <a href="javascript:" class="btn btn-warning js-couponOption" data-type="logs" data-cid="${reward.id!''}">日志</a>
                                </td>
                            </tr>
                            </#list>
                        <#else >
                        <tr>
                            <td colspan="5" style="text-align: center">暂无数据</td>
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
                        'edit': '?rewardId=' + cid,
                        'info': '?rewardId=' + cid,
                        'logs': '?rewardId=' + cid
                    };
            var url = '';
            if(type === "info"){
                url = 'wrinfo.vpage'+ mapLink[type];
            } else if (type === "logs") {
                url = 'wrlogs.vpage' + mapLink[type];
            } else {
                url = 'wrdetails.vpage' + mapLink[type];
            }
            window.open(url, '_blank').location;
        });
    });
</script>
</@layout_default.page>