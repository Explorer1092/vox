<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<style>
    input {width: 100px;}
    select {width: 130px;}
</style>
<div class="span9">
    <fieldset>
        <legend><span style="color: #00a0e9">跳转管理</span></legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <ul class="inline">
                <li>
                    <label>跳转ID&nbsp;
                        <input type="text" id="skipId" name="skipId" value="${skipId!''}"/>
                    </label>
                </li>
                <li>
                    <label>课程ID&nbsp;
                        <input type="text" id="skuId" name="skuId" value="${skuId!''}" />
                    </label>
                </li>
                <li>
                    <label>跳转类型&nbsp;
                        <select id="type" name="type">
                            <option value="-1">全部</option>
                            <option value="1" <#if type?? && type == 1>selected</#if>>直通车</option>
                            <option value="2" <#if type?? && type == 2>selected</#if>>毕业证</option>
                            <option value="3" <#if type?? && type == 3>selected</#if>>分享</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label>年级&nbsp;
                        <select id="grade" name="grade">
                            <option value="-1">全部</option>
                            <option value="0" <#if grade?? && grade == 0>selected</#if>>无年级</option>
                            <option value="1" <#if grade?? && grade == 1>selected</#if>>一年级</option>
                            <option value="2" <#if grade?? && grade == 2>selected</#if>>二年级</option>
                            <option value="3" <#if grade?? && grade == 3>selected</#if>>三年级</option>
                            <option value="4" <#if grade?? && grade == 4>selected</#if>>四年级</option>
                            <option value="5" <#if grade?? && grade == 5>selected</#if>>五年级</option>
                            <option value="6" <#if grade?? && grade == 6>selected</#if>>六年级</option>
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

    <button type="button" class="btn btn-success js-couponOption" data-type="add">新增跳转</button>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>跳转ID</th>
                        <th>课程ID</th>
                        <th>跳转类型</th>
                        <th>年级</th>
                        <th>目标课程ID</th>
                        <th>创建人</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as skip>
                            <tr>
                                <td>${skip.id!''}</td>
                                <td>${skip.skuId!''}</td>
                                <td><#if skip.type?? && skip.type == 1>直通车<#elseif skip.type?? && skip.type == 2>毕业证<#elseif skip.type?? && skip.type == 3>分享</#if></td>
                                <td>${skip.grade!''}</td>
                                <td>${skip.targetSkuId!''}</td>
                                <td>${skip.createUser!''}</td>
                                <td>
                                    <a href="javascript:" class="btn btn-primary js-couponOption" data-type="info" data-cid="${skip.id!''}">详情</a>
                                    <a href="javascript:" class="btn btn-success js-couponOption" data-type="edit" data-cid="${skip.id!''}">编辑</a>
                                    <a href="javascript:" class="btn btn-warning js-couponOption" data-type="logs" data-cid="${skip.id!''}">日志</a>
                                </td>
                            </tr>
                            </#list>
                        <#else >
                        <tr>
                            <td colspan="7" style="text-align: center">暂无数据</td>
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
                        'edit': '?skipId=' + cid,
                        'info': '?skipId=' + cid,
                        'logs': '?skipId=' + cid
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