<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="产品运营信息管理" page_num=9>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>

<div id="main_container" class="span9" style="font-size: 14px">
    <form action="/opmanager/fairylandProduct/index.vpage" method="Get" id="fairylandProductForm">
        <ul class="inline">
            <li>
                <select id="platform" name="platform" style="width:250px">
                    <option value="">--选择平台--</option>
                    <#if platformTypeMap?exists>
                        <#list platformTypeMap?keys as key>
                            <#if platform?? && platform == key>
                                <option selected="selected" value="${key}">${platformTypeMap[key]?default("")}</option>
                            <#else>
                                <option value="${key}">${platformTypeMap[key]?default("")}</option>
                            </#if>
                        </#list>
                    </#if>
                </select>
            </li>
            <li>
                <select id="productType" name="productType" style="width:250px">
                    <option value="">--选择产品类型--</option>
                    <#if productTypeMap?exists>
                        <#list productTypeMap?keys as key>
                            <#if productType?? && productType == key>
                                <option selected="selected" value="${key}">${productTypeMap[key]?default("")}</option>
                            <#else>
                                <option value="${key}">${productTypeMap[key]?default("")}</option>
                            </#if>
                        </#list>
                    </#if>
                </select>
            </li>
            <li>
                <input id="btn_app_search" type="submit" class="btn btn-primary" value="查询"/>
            </li>
            <li>
                <input id="btn_add_app" type="button" class="btn" value="新增"/>
            </li>
            <li>
                <input id="btn_sort_begin" type="button" class="btn" value="进入排序模式"/>
            </li>
        </ul>
        <ul class="inline">
            <table class="table table-bordered">
                <tr>
                    <th>对应平台</th>
                    <th>产品类型</th>
                    <th>产品名称</th>
                    <th>appKey</th>
                    <th>分类描述</th>
                    <th>热点提示</th>
                    <th>新产品提示</th>
                    <th>推荐提示</th>
                    <th>操作</th>
                </tr>
                <tr>
                    <td colspan="10"><h5>上架产品</h5></td>
                </tr>
                <tbody id="tbody">
                    <#if onlineFairylandProducts ?? >
                        <#list onlineFairylandProducts as fairylandProduct >
                        <tr>
                            <td>${fairylandProduct.platform?default("")}</td>
                            <td>${fairylandProduct.productType?default("")}</td>
                            <td>${fairylandProduct.productName?default("")}</td>
                            <td>${fairylandProduct.appKey?default("")}</td>
                            <td>${fairylandProduct.catalogDesc?default("")}</td>
                            <td>${fairylandProduct.hotFlag?string("有提示","无提示")}</td>
                            <td>${fairylandProduct.newFlag?string("有提示","无提示")}</td>
                            <td>${fairylandProduct.recommendFlag?string("有提示","无提示")}</td>
                            <td>
                                <span class="update-btn">
                                    <#if (fairylandProduct.status)?? && fairylandProduct.status == 'ONLINE'>
                                        <a href="javascript:void(0)"
                                           onclick="updateStatus('${fairylandProduct.id?default("")}')">下架</a>&nbsp;&nbsp;
                                    <#elseif (fairylandProduct.status)?? && fairylandProduct.status == 'OFFLINE'>
                                        <a href="javascript:void(0)"
                                           onclick="updateStatus('${fairylandProduct.id?default("")}')">上架</a>&nbsp;&nbsp;
                                    </#if>
                                    <a href="/opmanager/fairylandProduct/addOrUpdateIndex.vpage?fairylandProductId=${fairylandProduct.id?default("")}">编辑</a>&nbsp;&nbsp;
                                    <a href="javascript:void(0)"
                                       onclick="deleteFairylandProduct(${fairylandProduct.id?default("")})">删除</a>&nbsp;&nbsp;
                                </span>
                                <span class="sort-btn" style="display:none">
                                    <a href="javascript:void(0)" class="rankType" rank-type="top">置顶</a>
                                    <a href="javascript:void(0)" class="rankType" rank-type="up">上移</a>
                                    <a href="javascript:void(0)" class="rankType" rank-type="down">下移</a>
                                    <a href="javascript:void(0)" class="rankType" rank-type="end">置尾</a>
                                </span>
                            </td>
                        </tr>
                        </#list>
                    </#if>
                </tbody>
                <tr>
                    <td colspan="10"><h5>下架产品</h5></td>
                </tr>
                <#if offlineFairylandProducts ?? >
                    <#list offlineFairylandProducts as fairylandProduct >
                        <tr>
                            <td>${fairylandProduct.platform?default("")}</td>
                            <td>${fairylandProduct.productType?default("")}</td>
                            <td>${fairylandProduct.productName?default("")}</td>
                            <td>${fairylandProduct.appKey?default("")}</td>
                            <td>${fairylandProduct.rank?default("")}</td>
                            <td>${fairylandProduct.hotFlag?string("有提示","无提示")}</td>
                            <td>${fairylandProduct.newFlag?string("有提示","无提示")}</td>
                            <td>${fairylandProduct.recommendFlag?string("有提示","无提示")}</td>
                            <td>
                                <#if (fairylandProduct.status)?? && fairylandProduct.status == 'ONLINE'>
                                    <a href="javascript:void(0)"
                                       onclick="updateStatus('${fairylandProduct.id?default("")}')">下架</a>&nbsp;&nbsp;
                                <#elseif (fairylandProduct.status)?? && fairylandProduct.status == 'OFFLINE'>
                                    <a href="javascript:void(0)"
                                       onclick="updateStatus('${fairylandProduct.id?default("")}')">上架</a>&nbsp;&nbsp;
                                </#if>
                                <a href="/opmanager/fairylandProduct/addOrUpdateIndex.vpage?fairylandProductId=${fairylandProduct.id?default("")}">编辑</a>&nbsp;&nbsp;
                                <a href="javascript:void(0)"
                                   onclick="deleteFairylandProduct(${fairylandProduct.id?default("")})">删除</a>&nbsp;&nbsp;
                            </td>
                        </tr>
                    </#list>
                </#if>
            </table>
        </ul>
    </form>
</div>

<script type="text/javascript">
    $(function () {

        $('#btn_add_app').on('click', function () {
            window.location.href = "/opmanager/fairylandProduct/addOrUpdateIndex.vpage";
        });

        //进行排序
        $(".rankType").on('click', function () {
            var platform = $("#platform").find("option:selected").val();
            var productType = $("#productType").find("option:selected").val();
            var rankType = $(this).attr("rank-type");
            var appNode = $(this).parent().parent();
            var appKey = $(appNode).siblings().eq(3).text();
            sort(platform, productType, appKey, rankType, appNode);

        });

        //进入排序模式
        $('#btn_sort_begin').on('click', function () {

            var platform = $("#platform").find("option:selected").val();
            var productType = $("#productType").find("option:selected").val();
            if (platform == "") {
                alert("排序必须选定指定平台");
            } else {
                var displayBtn = $(this).data("displayBtn");
                if (displayBtn == null || displayBtn == '.sort-btn') {
                    $(this).data("displayBtn", ".update-btn");
                    $(this).attr("value", "返回正常模式")
                    $(".sort-btn").attr("style", "display:''");
                    $(".update-btn").attr("style", "display:none");
                } else {
                    location.reload();
                }
            }
        })

    });
    function sort(platformVal, productTypeVal, appKeyVal, rankTypeVal, appNode) {
        $.post("/opmanager/fairylandProduct/sort.vpage", {
            platform: platformVal,
            productType: productTypeVal,
            appKey: appKeyVal,
            rankType: rankTypeVal
        }, function (result) {
            if (result.success == true) {
                var appTrNode = $(appNode).parent();
                if (rankTypeVal == 'up') {
                    $(appTrNode).prev().before(appTrNode);
                } else if (rankTypeVal == 'down') {
                    $(appTrNode).next().after(appTrNode);
                } else if (rankTypeVal == 'top') {
                    $("#tbody").children().first().before(appTrNode);
                } else if (rankTypeVal == 'end') {
                    $("#tbody").children().end().after(appTrNode);
                }
            } else {
                alert(result.info);
            }
        });
    }
    function updateStatus(fairylandProductId) {
        $.post("/opmanager/fairylandProduct/updateStatus.vpage", {fairylandProductId: fairylandProductId}, function (result) {
            if (result.success == true) {
                location.reload();
            } else {
                alert(result.info);
            }
        });
    }
    function deleteFairylandProduct(fairylandProductId) {
        if (confirm("您确定要删除这个应用吗,删除之后将无法添加相同的平台与appkey？")) {
            $.post("/opmanager/fairylandProduct/delete.vpage", {fairylandProductId: fairylandProductId}, function (result) {
                if (result.success == true) {
                    alert("恭喜删除成功");
                    location.reload();
                } else {
                    alert(result.info);
                }

            });
        }

    }


</script>
</@layout_default.page>