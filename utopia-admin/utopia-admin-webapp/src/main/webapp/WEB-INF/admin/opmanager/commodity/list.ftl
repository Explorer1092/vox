<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js" xmlns="http://www.w3.org/1999/html"></script>
<div class="span9">
    <fieldset>
        <legend><font color="#00bfff">学习币商城</font>/商品管理</legend>
    </fieldset>
    <form id="commodity-query" class="form-horizontal" method="get"
          action="${requestContext.webAppContextPath}/opmanager/commodity/list.vpage">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <ul class="inline">
            <li>
                <label>商品ID&nbsp;
                    <input type="text" name="id" id="commodityId" value="${id!''}"/>
                </label>
            </li>
            <li>
                <label>商品名称&nbsp;
                    <input type="text" name="name" id="commodityName" value="${name!''}"/>
                </label>
            </li>
            <li>
                <label>商品分类&nbsp;
                    <select id="category" name="category">
                        <option value="">全部</option>
                            <#if categoryMap?has_content>
                                <#list categoryMap? keys as key>
                                    <option value="${key}" <#if categoryName?? && categoryName == key>selected="selected"</#if>>${categoryMap[key]}</option>
                                </#list>
                            </#if>
                    </select>
                </label>
            </li>
            <li>
                <label>展示栏目&nbsp;
                    <select id="column" name="column">
                        <option value="">全部</option>
                        <#if columnMap?has_content>
                            <#list columnMap ? keys as key>
                                        <option value="${key}" <#if column?? && column == key>selected="selected"</#if>>${columnMap[key]}</option>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>
            <li>
                <label>商品状态&nbsp;
                    <select name="onSale" id="status">
                        <option value="">全部</option>
                        <option value="yes" <#if onSale?? && onSale == "yes">selected="selected"</#if>>上架</option>
                        <option value="no" <#if onSale?? && onSale == "no">selected="selected"</#if>>下架</option>
                    </select>
                </label>
            </li>
            <li>
                <label>首页banner展示&nbsp;
                    <select name="recommendFlag" id="recommendFlag">
                        <option value="">全部</option>
                        <option value="yes" <#if recommendFlag?? && recommendFlag == "yes">selected="selected"</#if>>是</option>
                        <option value="no" <#if recommendFlag?? && recommendFlag == "no">selected="selected"</#if>>否</option>
                    </select>
                </label>
            </li>
            <li>
                <label>用户专享&nbsp;
                    <select name="userType" id="userType">
                        <option value="">全部</option>
                        <option value="all" <#if userType?? && userType == "all">selected="selected"</#if>>所有用户</option>
                        <option value="MONITOR" <#if userType?? && userType == "MONITOR">selected="selected"</#if>>KOL</option>
                    </select>
                </label>
            </li>
            <li>
                <label>商品ID&nbsp;
                    <input type="text" name="id" id="commodityId" value="${id!''}"/>
                </label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <button type="button" class="btn btn-primary" id="clearQuery">清除</button>
            </li>
            <li>
                <button type="button" class="btn btn-primary" id="commodityQuery">查询</button>
            </li>
        </ul>
    </form>

    <div style="height: 30px">
        <div style="float: left">
            <a href="addCommodity.vpage" class="btn btn-primary" id="addCommodity">新增商品</a>
            <button class="btn btn-primary" id="batchOnSale">批量上架</button>
            <button class="btn btn-primary" id="batchOffSale">批量下架</button>
            <button class="btn btn-primary" id="batchDelete">批量删除</button>
        </div>
        <div style="float: right">
            <a href="recycleList.vpage" type="button" class="btn btn-primary" id="recycleBin">商品回收站</a>
            <span>一共${deleteCount!0}个商品</span>
        </div>
    </div>

    <div class="row-fluid">
        <div class="span12">
            <div class="well" style="font-size: 12px;">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th><input type="checkbox" id="batchSelect"></th>
                        <th>商品ID</th>
                        <th>商品名称</th>
                        <th>普通价</th>
                        <th>KOL价</th>
                        <th>采购价</th>
                        <th>商品分类</th>
                        <th>展示栏目</th>
                        <th>商品状态</th>
                        <th>当前库存</th>
                        <th>用户专享</th>
                        <th>banner展示</th>
                        <th>已售数量</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if commodityPage?? && commodityPage.content??>
                            <#list commodityPage.content as commodity>
                            <tr>
                                <td><input type="checkbox" name="commodityCheck" data-id="${commodity.id!''}"
                                           data-name="${commodity.name!''}"
                                           data-on_sale="${commodity.onSale?string('true', 'false')}"></td>
                                <td>${commodity.id!''}</td>
                                <td>${commodity.name!''}</td>
                                <td>${commodity.ordinaryCoinS!0}</td>
                                <td>${commodity.monitorCoinS!0}</td>
                                <td>${commodity.purchase!0}</td>
                                <td>${commodity.category!''}</td>
                                <td>${commodity.column!''}</td>
                                <td><#if commodity.onSale?? && commodity.onSale>上架<#else >下架</#if></td>
                                <td>${commodity.stock!0}</td>
                                <td>
                                    <#if commodity.userTypes??>
                                        <#if commodity.userTypes?size gt 1>
                                            所有用户
                                        <#else >
                                            KOL
                                        </#if>
                                    </#if>
                                </td>
                                <td><#if commodity.recommendFlag?? && commodity.recommendFlag>是<#else >否</#if></td>
                                <td><a href="${requestContext.webAppContextPath}/opmanager/commodity/order/list.vpage">${commodity.soldNum!0}</a></td>
                                <td>
                                    <a href="detail.vpage?id=${commodity.id!''}">详情</a>|
                                    <a href="javascript:void(0);" data-id="${commodity.id!''}"
                                       data-on_sale="${commodity.onSale?string('true', 'false')}" name="editCommodity">编辑</a>|
                                    <#if commodity.onSale?? && commodity.onSale>
                                        <a href="javascript:void(0);" data-id="${commodity.id!''}" name="offSale">下架</a>|
                                    <#else >
                                        <a href="javascript:void(0);" data-id="${commodity.id!''}" name="onSale">上架</a>|
                                    </#if>
                                    <a href="commodityLog.vpage?id=${commodity.id!''}">日志</a>
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
                    <li>&nbsp;跳转至&nbsp;<input type="text" id="jumpPage" style="width: 30px;" maxlength="3">&nbsp;页</li>
                </ul>
            </div>
        </div>
    </div>
</div>

<div id="batch_on_sale_modal" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>批量上架商品</h3>
    </div>
    <div class="modal-body" style="overflow: auto;height: 240px;">
        <div style="height: 40px">
            <span>将要对下列商品进行上架操作</span>
        </div>
        <div>
            <table class="table table-bordered"
            " id="onSaleTable">

            </table>
        </div>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
        <button type="button" class="btn btn-primary" id="confirm-on-btn">确认</button>
    </div>
</div>

<div id="batch_off_sale_modal" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>批量下架商品</h3>
    </div>
    <div class="modal-body" style="overflow: auto;height: 240px;">
        <div style="height: 40px">
            <span>将要对下列商品进行下架操作</span>
        </div>
        <div>
            <table class="table table-bordered" id="offSaleTable">

            </table>
        </div>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
        <button type="button" class="btn btn-primary" id="confirm-off-btn">确认</button>
    </div>
</div>

<div id="batch_delete_modal" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>批量删除商品</h3>
    </div>
    <div class="modal-body" style="overflow: auto;height: 240px;">
        <div style="height: 40px">
            <span>将要对下列商品进行删除操作</span>
        </div>
        <div>
            <table class="table table-bordered" id="deleteTable">

            </table>
        </div>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
        <button type="button" class="btn btn-primary" id="confirm-delete-btn">确认</button>
    </div>
</div>

<script type="text/javascript">
    function pagePost(pageNumber) {
        $("#pageNum").val(pageNumber);
        $("#commodity-query").submit();
    }

    $("#commodityQuery").on('click', function () {
        $("#pageNum").val(1);
        $("#commodity-query").submit();
    });

    $(function () {
        $("#jumpPage").on('blur', function () {
           pagePost($("#jumpPage").val());
        });

        $("#clearQuery").on('click', function () {
            location.href="/opmanager/commodity/list.vpage";
        });

        $("a[name='onSale']").on('click', function () {
            if (confirm("确定要上架该商品？")) {
                $.post("onSale.vpage", {id: $(this).data("id")}, function (data) {
                    if (data.success) {
                        alert("上架成功");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                });
            }
        });
        $("a[name='offSale']").on('click', function () {
            if (confirm("确定要下架该商品？")) {
                $.post("offSale.vpage", {id: $(this).data("id")}, function (data) {
                    if (data.success) {
                        alert("下架成功");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        $("a[name='editCommodity']").on('click', function () {
            var onSale = $(this).data("on_sale");
            if (onSale) {
                alert("编辑商品前必须下架商品");
                return;
            } else {
                window.location.href = "editCommodity.vpage?id=" + $(this).data("id");
            }
        });

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

        //批量上架
        var commodityIds;
        $("#batchOnSale").on('click', function () {
            commodityIds = [];
            $("#onSaleTable").html("");
            $("#onSaleTable").append("<tr><td>商品ID</td><td>商品名称</td></tr>");
            $("#batch_on_sale_modal").modal('show');
            $("input[name='commodityCheck']:checked").each(function () {
                var commodityId = $(this).data("id");
                var commodityName = $(this).data("name");
                var onSale = $(this).data("on_sale");
                if (!onSale) {
                    $("#onSaleTable").append("<tr><td>" + commodityId + "</td><td>" + commodityName + "</td></tr>");
                    commodityIds.push(commodityId);
                }
            });
        });
        $("#confirm-on-btn").on('click', function () {
            if (commodityIds.length == 0) {
                alert("请选择要进行操作的商品");
                return;
            }
            $.post("batchOnSale.vpage", {"commodityIds": commodityIds.toString().trim()}, function (data) {
                if (data.success) {
                    alert("批量上架成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });


        //批量下架
        $("#batchOffSale").on('click', function () {
            commodityIds = [];
            $("#offSaleTable").html("");
            $("#offSaleTable").append("<tr><td>商品ID</td><td>商品名称</td></tr>");
            $("#batch_off_sale_modal").modal('show');
            $("input[name='commodityCheck']:checked").each(function () {
                var commodityId = $(this).data("id");
                var commodityName = $(this).data("name");
                var onSale = $(this).data("on_sale");
                if (onSale) {
                    $("#offSaleTable").append("<tr><td>" + commodityId + "</td><td>" + commodityName + "</td></tr>");
                    commodityIds.push(commodityId);
                }
            });
        });
        $("#confirm-off-btn").on('click', function () {
            if (commodityIds.length == 0) {
                alert("请选择要进行操作的商品");
                return;
            }
            $.post("batchOffSale.vpage", {"commodityIds": commodityIds.toString().trim()}, function (data) {
                if (data.success) {
                    alert("批量下架成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });

        //批量删除
        $("#batchDelete").on('click', function () {
            commodityIds = [];
            $("#deleteTable").html("");
            $("#deleteTable").append("<tr><td>商品ID</td><td>商品名称</td></tr>");
            $("#batch_delete_modal").modal('show');
            $("input[name='commodityCheck']:checked").each(function () {
                var commodityId = $(this).data("id");
                var commodityName = $(this).data("name");
                $("#deleteTable").append("<tr><td>" + commodityId + "</td><td>" + commodityName + "</td></tr>");
                commodityIds.push(commodityId);
            });
        });
        $("#confirm-delete-btn").on('click', function () {
            if (commodityIds.length == 0) {
                alert("请选择要进行操作的商品");
                return;
            }
            $.post("batchDelete.vpage", {"commodityIds": commodityIds.toString().trim()}, function (data) {
                if (data.success) {
                    alert("批量删除成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });
    });
</script>
</@layout_default.page>