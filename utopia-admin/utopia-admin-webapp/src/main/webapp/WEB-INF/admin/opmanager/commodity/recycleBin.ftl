<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js" xmlns="http://www.w3.org/1999/html"></script>
<div class="span9">
    <fieldset>
        <legend><font color="#00bfff">学习币商城</font>/商品回收站</legend>
    </fieldset>

    <form id="commodity-query" class="form-horizontal" method="get"
          action="${requestContext.webAppContextPath}/opmanager/commodity/recycleList.vpage">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div style="height: 50px">
            商品名称 <input type="text" name="name" id="commodityName"/>
            商品分类 <select id="category" name="category">
            <option value="">全部</option>
                    <#if categoryMap?has_content>
                        <#list categoryMap? keys as key>
                            <option value="${key}">${categoryMap[key]}</option>
                        </#list>
                    </#if>
        </select>
            <button type="submit" class="btn btn-primary" id="commodityQuery">查询</button>
        </div>
    </form>
    <div style="height: 40px">
        <button class="btn btn-primary" id="batchRecover">批量恢复</button>
    </div>
    <div class="row-fluid">
        <div class="span12">
            <div class="well" style="font-size: 12px;">
                <div style="height: 60px;">
                    <label style="background-color: pink; height: 30px;font-size: 18px">商品将在90天后自动删除，点击【恢复】，商品进入到商品管理列表，且处于下架状态，商品销量保持不变</label>
                </div>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th><input type="checkbox" id="batchSelect"></th>
                        <th>商品名称</th>
                        <th>商品分类</th>
                        <th>剩余自动删除时间</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if commodityPage?? && commodityPage.content??>
                            <#list commodityPage.content as commodity>
                                <tr>
                                    <td>
                                        <input type="checkbox" name="commodityCheck" data-id="${commodity.id!''}"
                                               data-name="${commodity.name!''}">
                                    </td>
                                    <td>${commodity.name!''}</td>
                                    <td>${commodity.category!''}</td>
                                    <td>${commodity.leftDay!0}</td>
                                    <td>
                                        <button class="btn btn-primary" name="recoverBtn" data-id="${commodity.id!''}">恢复
                                    </td>
                                </tr>
                            </#list>

                        </#if>
                    </tbody>
                </table>
                <ul class="pager">
                    <li><a href="#" onclick="pagePost(1)" title="Pre">首页</a></li>
                    <#if hasPrev>
                        <li><a href="#" onclick="pagePost(${currentPage-1})" title="Pre">&lt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&lt;</a></li>
                    </#if>
                    <li class="disabled"><a>第 ${currentPage!} 页</a></li>
                    <li class="disabled"><a>共 <#if totalPage==0>1<#else>${totalPage!}</#if> 页</a></li>
                    <#if hasNext>
                        <li><a href="#" onclick="pagePost(${currentPage+1})" title="Next">&gt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&gt;</a></li>
                    </#if>
                    <li><a href="#" onclick="pagePost(${totalPage!})" title="Pre">尾页</a></li>
                </ul>
            </div>
        </div>
    </div>

</div>

<div id="batch_recover_modal" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>批量恢复商品</h3>
    </div>
    <div class="modal-body" style="overflow: auto;height: 240px;">
        <div style="height: 40px">
            <span>将要对下列商品进行恢复操作</span>
        </div>
        <div>
            <table class="table table-bordered" id="recoverTable">

            </table>
        </div>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
        <button type="button" class="btn btn-primary" id="confirm-recover-btn">确认</button>
    </div>
</div>

<script type="text/javascript">
    function pagePost(pageNumber) {
        $("#pageNum").val(pageNumber);
        $("#commodity-query").submit();
    }

    $(function () {
        $("#batchSelect").on('click', function () {
            if ($(this).is(":checked")) {
                $("input[name='commodityCheck']").each(function () {
                    $(this).prop("checked", true);
                });
            } else {
                $("input[name='commodityCheck']").each(function () {
                    $(this).prop("checked", false);
                });
            }
        });

        //批量恢复
        var commodityIds;
        $("#batchRecover").on('click', function () {
            commodityIds = [];
            $("#recoverTable").html("");
            $("#recoverTable").append("<tr><td>商品ID</td><td>商品名称</td></tr>");
            $("#batch_recover_modal").modal('show');
            $("input[name='commodityCheck']:checked").each(function () {
                var commodityId = $(this).data("id");
                var commodityName = $(this).data("name");
                $("#recoverTable").append("<tr><td>" + commodityId + "</td><td>" + commodityName + "</td></tr>");
                commodityIds.push(commodityId);
            });
        });
        $("#confirm-recover-btn").on('click', function () {
            if (commodityIds.length == 0) {
                alert("请选择要进行操作的商品");
                return;
            }
            $.post("batchRecover.vpage", {"commodityIds": commodityIds.toString().trim()}, function (data) {
                if (data.success) {
                    alert("批量恢复成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });

        $("button[name='recoverBtn']").on('click', function () {
            var commodityId = $(this).data("id");
            $.post("recover.vpage", {id: commodityId}, function (data) {
                if (data.success) {
                    alert("商品恢复成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });
    });
</script>
</@layout_default.page>