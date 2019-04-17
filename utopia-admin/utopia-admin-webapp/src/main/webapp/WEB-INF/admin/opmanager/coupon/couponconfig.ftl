<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="配置优惠劵策略" page_num=9>
<script src="//cdn.17zuoye.com/public/plugin/jquery/jquery-1.7.1.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/advertisement/advertisement.css" rel="stylesheet">
<div id="main_container" class="span9">
    <div>
        <#if error??>
            <div class="alert alert-error">
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong>${error!}</strong>
            </div>
        </#if>
    </div>
    <legend class="legend_title">
        <strong>优惠券概览</strong> &nbsp;&nbsp;&nbsp;&nbsp;
    </legend>
    <div class="inline">
        <table>
            <tbody><tr>
                <td class="info_td">优惠券ID：<span class="info_td_txt">${couponId!''}</span></td>
                <td class="info_td">名称：<span class="info_td_txt">${coupon.name!''}</span></td>
            </tr>
            </tbody>
        </table>
    </div>
    <#if !error??>
        <div class="form-horizontal">
            <legend class="legend_title"><strong>优惠券投放对象</strong> &nbsp;&nbsp;&nbsp;&nbsp;</legend><br>
            <div style="height: 500px;">
                <div>
                    <table class="table table-stripped" style="width: 800px;" >
                        <thead>
                        <tr>
                            <th <#if has_5?? && has_5> bgcolor="#cd5c5c"</#if>>
                                <input type="radio" name="targetType" value=5 <#if targetType??><#if targetType == 5> checked="checked" </#if></#if>/>&nbsp;&nbsp;投放所有用户
                            </th>
                            <th <#if has_1?? && has_1> bgcolor="#00ffff" </#if>>
                                <input type="radio" name="targetType" value=1 <#if targetType??><#if targetType == 1> checked="checked" </#if></#if>/>&nbsp;&nbsp;投放指定地区
                            </th>
                        </tr>
                        </thead>
                    </table>
                    <br>
                    <div>
                        <div id="target_all_modal" class="span7" style="display: none;">
                        <pre>
                            <h3 style="text-align: center;">确认此项之后，优惠劵将针对所有用户投放</h3>
                            <h4 style="text-align: center; color: red;"><#if has_5?? && has_5>已确认<#else>未确认</#if></h4>
                        </pre>
                            <br><br>&nbsp;&nbsp;<button name="save_target_btn" type="button" class="btn btn-success">确  认</button>
                            &nbsp;&nbsp;<button id="clear_target_btn_5" type="button" class="btn btn-danger">清  除</button>
                        </div>
                        <div id="target_region_modal" class="span7" style="display: none;">
                            <div id="regionTree" class="sampletree" style="width:60%; height: 410px; float: left; display: inline;"></div>
                            <div style="width:40%; height: 500px; float:right; display: inline;">
                                <span style="margin-left: 10px;">筛选</span> <input name="filter_region" type="text" class="input-small" id="filter_region" placeholder="筛选条件...">
                                <button name="delete_region_filter" id="delete_region_filter">&times;</button>
                                <button name="save_target_btn" type="button" class="btn btn-success" style="margin: 15px 0 15px 30px;">保存投放地区</button>
                                <button id="clear_target_btn_1" type="button" class="btn btn-danger" style="margin: 0 0 0 30px;">清空投放地区</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <legend class="legend_title"><strong>优惠券关联产品</strong> &nbsp;&nbsp;&nbsp;&nbsp;</legend><br>
            <div>
                <div id="product_modal" class="span7" style="">
                    <div id="productTree" class="sampletree" style="width:60%; height: 410px; float: left; display: inline;"></div>
                    <div style="width:40%; height: 500px; float:right; display: inline;">
                        <span style="margin-left: 10px;">筛选</span> <input name="filter_product" type="text" class="input-small" id="filter_product" placeholder="筛选条件...">
                        <button name="delete_product_filter" id="delete_product_filter">&times;</button>
                        <button id="save_target_product_btn" type="button" class="btn btn-success" style="margin: 32px;">保存关联产品</button>
                    </div>
                </div>
            </div>
            <legend class="legend_title"><strong>优惠券投放约束</strong> &nbsp;&nbsp;&nbsp;&nbsp;</legend>
            <div style="height: 300px;">
                <#--<#if editable?? && editable>-->
                    <br><button class="btn btn-success" id="add_tag_btn">增加约束</button><br><br>
                <#--</#if>-->
                <table class="table table-striped table-bordered" style="width:70%;">
                    <thead>
                    <tr bgcolor="#f0ffff">
                        <th><strong>Tag Name</strong></th>
                        <th><strong>Tag Value</strong></th>
                        <th><strong>Tag Comment</strong></th>
                        <th><strong>Options</strong></th>
                    </tr>
                    </thead>
                    <#if tagMap??>
                        <tbody>
                            <#list tagMap?keys as key>
                            <tr>
                                <td>${tagMap[key].tagType!""}</td>
                                <td id="VAL_${tagMap[key].tagType!}">${tagMap[key].tagValue!''}</td>
                                <td id="CMT_${tagMap[key].tagType!}">${tagMap[key].tagComment!''}</td>
                                <td>
                                    <#--<#if editable?? && editable>-->
                                        <a id="EDIT_${tagMap[key].tagType!}">编辑</a>
                                        <a id="DEL_${tagMap[key].tagType!}">删除</a>
                                    <#--</#if>-->
                                </td>
                            </tr>
                            </#list>
                        </tbody>
                    </#if>
                </table>
            </div>
        </div>
    </#if>
</div>
<div id="add_tag_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>编辑约束</h3>
    </div>
    <div class="modal-body dl-horizontal">
        <dl>
            <dt>选择约束：</dt>
            <dd>
                <select id="tag_select" class="multiple">
                    <#if tagList??>
                        <option value="">--请选择--</option>
                        <#list tagList as t>
                            <option value="${t.tagName!''}" data-show="${t.tagType}"  data-instruction="${t.instruction}" <#if t.exist>
                                    disabled="disabled" </#if>>${t.tagDesc!''}</option>
                        </#list>
                    </#if>
                </select>
            </dd>
        </dl>
        <dl id="tag_input">
            <dt>配置内容：</dt>
            <dd>
                <input id="tag_value" type="text" value="">
            </dd>
        </dl>
        <dl>
            <dt>备注：</dt>
            <dd>
                <input id="tag_comment" type="text" value=""
                <br><label id="tag_instruction" style="color:red;"></label>
            </dd>
        </dl>
        <div class="modal-footer">
            <button id="save_tag_btn" class="btn btn-info" data-dismiss="modal" aria-hidden="true">保存约束</button>
        </div>
    </div>
</div>
<script>
    var allModel = $('#target_all_modal');
    var regionModel = $('#target_region_modal');
    initDisplay(${targetType!0});
    initTree();
    $(function () {

        $('#add_tag_btn').on('click', function () {
            change();
            $('#tag_select').val('');
            $('#tag_select').attr("disabled", false);
            $('#tag_value').val('');
            $('#tag_comment').val('');
            $('#add_tag_dialog').modal('show');
        });

        $("a[id^=EDIT_]").on('click', function () {
            var tagName = $(this).attr("id").substring("EDIT_".length);
            var tagVal = $('#VAL_' + tagName).text();
            var tagComment = $('#CMT_' + tagName).text();
            $('#tag_select').val(tagName);
            $('#tag_select').attr("disabled", true);
            $('#tag_value').val(tagVal);
            $('#tag_comment').val(tagComment);

            $('#add_tag_dialog').modal('show');
            change();
        });

        $("a[id^=DEL_]").on('click', function () {
            if (!confirm("是否确认删除广告约束？")) {
                return false;
            }
            var tagName = $(this).attr("id").substring("DEL_".length);
            $.post('deltag.vpage', {couponId:"${couponId}", tagType: tagName}, function (data) {
                if (data.success) {
                    alert("约束删除成功！");
                    window.location.reload();
                } else {
                    alert("约束删除失败:" + data.info);
                    $('#add_tag_dialog').modal('hide');
                }
            });
        });

        $('#save_tag_btn').on('click', function () {
            if (!confirm("是否确认保存优惠券约束？")) {
                return false;
            }
            var tagName = $('#tag_select').find('option:selected').val();
            var tagVal = $('#tag_value').val();
            var tagComment = $('#tag_comment').val();
            var type = $('#tag_select').find('option:selected').attr("data-show");
            var tagData = {
                couponId: "${couponId}",
                tagType: tagName,
                tagVal: tagVal,
                tagComment: tagComment
            };
            if (!validateInput(tagData, type)) {
                return false;
            }
            $.post('savetag.vpage', tagData, function (data) {
                if (data.success) {
                    alert("约束保存成功！");
                    window.location.reload();
                } else {
                    alert("约束保存失败:" + data.info);
//                    $('#add_tag_dialog').modal('hide');
                }
            });
        });

        $('#tag_select').on('change', function () {
            change();
            $('#tag_value').val('');
            $('#tag_comment').val('');
        });

        $("input[name=targetType]").on('change', function () {
            var targetType = $(this).val();
            initDisplay(targetType);
        });

        $("button[name=save_target_btn]").on('click', function () {
            var type = $("input[name=targetType]:checked").val();
            if (type == 1) {
                var regionList = [];
                var regionTree = $("#regionTree").fancytree("getTree");
                var regionNodes = regionTree.getSelectedNodes();
                $.map(regionNodes, function (node) {
                    regionList.push(node.key);
                });

                $.post('saveregion.vpage', {
                    couponId: "${(couponId)!}",
                    type: type,
                    regionList: regionList.join(",")
                }, function (data) {
                    if (data.success) {
                        alert("保存区域成功！");
                        window.location.reload();
                    } else {
                        alert("保存区域失败:" + data.info);
                    }
                });
            } else if (type == 5) {
                $.post('saveids.vpage', {
                    couponId: "${(couponId)!}",
                    type: type,
                    targetIds: "true"
                }, function (data) {
                    if (data.success) {
                        alert("投放所有用户保存成功！");
                        window.location.reload();
                    } else {
                        alert("投放所有用户保存失败:" + data.info);
                    }
                });
            }
        });

        //保存关联产品
        $("#save_target_product_btn").on("click",function () {
            var productList = [];
            var productTree = $("#productTree").fancytree("getTree");
            var productTreeNodes = productTree.getSelectedNodes();
            $.map(productTreeNodes, function (node) {
                productList.push(node.key);
            });

            $.post('saveproducts.vpage', {
                couponId: "${couponId}",
                productList: productList.join(",")
            }, function (data) {
                if (data.success) {
                    alert("保存产品成功！");
                    window.location.reload();
                } else {
                    alert("保存产品失败:" + data.info);
                }
            });
        });

        //清除关联产品
        <#--$("#clear_target_product_btn").on("click",function () {-->
            <#--$.post('clearproduct.vpage', {-->
                <#--couponId: "${couponId}",-->
            <#--}, function (data) {-->
                <#--if (data.success) {-->
                    <#--alert("清除成功！");-->
                    <#--window.location.reload();-->
                <#--} else {-->
                    <#--alert("清除失败:" + data.info);-->
                <#--}-->
            <#--});-->
        <#--});-->

        $("button[id^='clear_target_btn_']").on('click', function () {
            var type = $(this).attr("id").substring("clear_target_btn_".length);
            $.post('cleartargets.vpage', {
                couponId: "${couponId}",
                type: type
            }, function (data) {
                if (data.success) {
                    alert("清除成功！");
                    window.location.reload();
                } else {
                    alert("清除失败:" + data.info);
                }
            });
        });

        //过滤区域
        $('#filter_region').keyup(function (e) {
            var match = $(this).val();
            if (e && e.which === $.ui.keyCode.ESCAPE || $.trim(match) === "") {
                $("#delete_region_filter").click();
                return;
            }
            var regionTree = $("#regionTree").fancytree("getTree");
            regionTree.options.filter.mode = "hide";
            regionTree.applyFilter(match);
        }).focus();

        //过滤产品
        $('#filter_product').keyup(function (e) {
            var match = $(this).val();
            if (e && e.which === $.ui.keyCode.ESCAPE || $.trim(match) === "") {
                $("#delete_product_filter").click();
                return;
            }
            var productTree = $("#productTree").fancytree("getTree");
            productTree.options.filter.mode = "hide";
            productTree.applyFilter(match);
        }).focus();

        $('#delete_region_filter').on('click', function () {
            $("#filter_region").val("");
            var regionTree = $("#regionTree").fancytree("getTree");
            regionTree.clearFilter();
        });

        $('#delete_product_filter').on('click', function () {
            $("#filter_product").val("");
            var productTree = $("#productTree").fancytree("getTree");
            productTree.clearFilter();
        });

    });

    function initDisplay(targetType) {
        var type = parseInt(targetType);
        switch (type) {
            case 1: allModel.hide();regionModel.show();break;
            case 5:
            default: allModel.show();regionModel.hide();break;
        }
    }

    function initTree() {
        $('#regionTree').fancytree({
            extensions: ["filter"],
            source: ${targetRegion!},
            checkbox: true,
            selectMode: 3
        });

        $('#productTree').fancytree({
            extensions: ["filter"],
            source: ${productTree!},
            checkbox: true,
            selectMode: 3
        });
    }

    function validateInput(tagData, type) {
        if (tagData.tagName == '') {
            alert("请选择可用的约束！");
            return false;
        }
        if (type == 2 && tagData.tagVal == '') {
            alert("请填写约束的内容！");
            return false;
        }
        return true;
    }

    function change() {
        var $selected = $('#tag_select').find('option:selected');
        var show = $selected.data("show");
        var instruction = $selected.data("instruction");
        if (show == 1) {
            $('#tag_input').hide();
        } else {
            $('#tag_input').show();
        }
        $('#tag_instruction').html(instruction);
    }

</script>
</@layout_default.page>