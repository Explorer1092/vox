<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="配置奖品投放对象" page_num=12>
    <#if error?? && error?has_content>
    <h1>${error}</h1>
    <#else>
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
        <legend class="legend_title">
            <strong>奖品信息</strong> <a type="button" style="margin-bottom: 5px;" class="btn" href="/reward/crmproduct/editproduct.vpage?productId=${product.id!0}">返回</a>
        </legend>
        <div class="inline">
            <table>
                <tbody>
                <tr>
                    <td class="info_td">奖品ID：<span class="info_td_txt">${product.id!0}</span></td>
                    <td class="info_td">奖品名称：<span class="info_td_txt">${product.productName!''}</span></td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="form-horizontal">
            <legend class="legend_title"><strong>产品投放对象</strong> &nbsp;&nbsp;&nbsp;&nbsp;</legend>
            <br>
            <div style="height: 500px;">
                <div>
                    <table class="table table-stripped" style="width: 600px;">
                        <thead>
                        <tr>
                            <th <#if has_5?? && has_5> bgcolor="#cd5c5c"</#if>>
                                <input type="radio" name="targetType" value=5 <#if targetType??><#if targetType == 5>
                                       checked="checked" </#if></#if>/>&nbsp;&nbsp;投放所有用户
                            </th>
                            <th <#if has_1?? && has_1> bgcolor="#00ffff" </#if>>
                                <input class="aaaa" type="radio" name="targetType"
                                       value=1 <#if targetType??><#if targetType == 1> checked="checked" </#if></#if>/>&nbsp;&nbsp;投放指定地区
                            </th>
                        <#--<th <#if has_4?? && has_4>bgcolor="#ffd700" </#if>>-->
                        <#--<input type="radio" name="targetType" value=4 <#if targetType??><#if targetType == 4> checked="checked" </#if></#if>/>&nbsp;&nbsp;投放指定标签-->
                        <#--</th>-->
                        <#--<th <#if has_2?? && has_2>bgcolor="#7fffd4" </#if>>
                            <input type="radio" name="targetType" value=2 <#if targetType??><#if targetType == 2> checked="checked" </#if></#if>/>&nbsp;&nbsp;投放指定用户
                        </th>
                        <th <#if has_3?? && has_3>bgcolor="#32cd32" </#if>>
                            <input type="radio" name="targetType" value=3 <#if targetType??><#if targetType == 3> checked="checked" </#if></#if>/>&nbsp;&nbsp;投放指定学校
                        </th>-->
                        </tr>
                        </thead>
                    </table>
                    <br>
                    <div>
                        <div id="target_all_modal" class="span7" style="display: none;">
                        <pre>
                            <h3 style="text-align: center;">确认此项之后，产品将针对所有用户投放</h3>
                        </pre>
                            <div style="float: right;">
                                <br><br>&nbsp;&nbsp;
                                <button name="save_target_btn" type="button" class="btn btn-success">确 认</button>
                                &nbsp;&nbsp;
                                <button id="clear_target_btn_5" type="button" class="btn btn-danger">清 除</button>
                            </div>
                        </div>
                        <div id="target_region_modal" class="span7" style="display: none;">
                            <div id="regionTree" class="sampletree"
                                 style="width:60%; height: 410px; float: left; display: inline;"></div>
                            <div style="width:40%; height: 500px; float:right; display: inline;">
                                &nbsp;&nbsp;筛选 <input name="filter_region" type="text" class="input-small"
                                                      id="filter_region" placeholder="筛选条件...">
                                <button name="delete_region_filter" id="delete_region_filter">&times;</button>
                                <br><br>&nbsp;&nbsp;
                                <button name="save_target_btn" type="button" class="btn btn-success">保存投放地区</button>
                                &nbsp;&nbsp;
                                <button id="clear_target_btn_1" type="button" class="btn btn-danger">清空投放地区</button>
                            </div>
                        </div>
                        <div id="target_label_modal" class="span7" style="display: none;">
                            <div id="labelTree" class="sampletree"
                                 style="width:60%; height: 410px; float: left; display: inline;"></div>
                            <div style="width:40%; height: 500px; float:right; display: inline;">
                                &nbsp;&nbsp;筛选 <input name="filter_label" type="text" class="input-small"
                                                      id="filter_label" placeholder="筛选条件...">
                                <button name="delete_label_filter" id="delete_label_filter">&times;</button>
                                <br><br>&nbsp;&nbsp;
                                <button class="btn btn-warning" onclick="checkUserHits()">关联用户数量</button>
                                &nbsp;&nbsp;<input class="input-small" type="number" id="userCnt" disabled
                                                   value="${labelUser!0}">
                                <br><br>&nbsp;&nbsp;
                                <button name="save_target_btn" type="button" class="btn btn-success">保存标签组</button>
                                &nbsp;&nbsp;
                                <button id="clear_target_btn_4" type="button" class="btn btn-danger">清空标签组</button>
                                <br><br>
                                <table class="table table-striped table-bordered" style="width: 700px;">
                                    <#if labelGroupList?? && labelGroupList?has_content>
                                        <tr>
                                            <td>ID</td>
                                            <td>标签组</td>
                                            <td>操作</td>
                                        </tr>
                                        <#list labelGroupList as label>
                                            <tr>
                                                <td>${label.targetId!'-'}</td>
                                                <td>${label.targetStr!""}</td>
                                                <td>
                                                    <button class="btn btn-danger"
                                                            onclick="deleteLabel(${label.targetId})">删除
                                                    </button>
                                                </td>
                                            </tr>
                                        </#list>
                                    </#if>
                                </table>
                            </div>
                        </div>
                        <div id="target_user_modal" class="span7" style="display: none;">
                            <div style="width:60%; height: 420px; float: left; display: inline;">
                    <textarea type="text" id="targetUser" name="targetUser" class="form-control" rows="20"
                              style="width:80%; resize: none;height: 400px; float: left; display: inline;"
                              placeholder="一行输入一条数据，建议从EXCEL编辑导入，如果超过3000行建议使用其他策略投放">${targetUser!}</textarea>
                            </div>
                            <div style="width:40%; height: 500px; float:right; display: inline;">
                                <br><br>&nbsp;&nbsp;记录总数：&nbsp;&nbsp;<input class="input-small" type="number" disabled
                                                                            value="${userSize!0}">
                                <br><br>&nbsp;&nbsp;<input type="checkbox" id="userAppend">&nbsp;&nbsp;追加模式
                                <br><br>&nbsp;&nbsp;
                                <button name="save_target_btn" type="button" class="btn btn-success">保存投放用户</button>
                                &nbsp;&nbsp;
                                <button id="clear_target_btn_2" type="button" class="btn btn-danger">清空投放用户</button>
                            </div>
                        </div>
                        <div id="target_school_modal" class="span7" style="display: none;">
                            <div style="width:60%; height: 420px; float: left; display: inline;">
                    <textarea type="text" id="targetSchool" name="targetSchool" class="form-control" rows="20"
                              style="width:80%; resize: none;height: 400px; float: left; display: inline;"
                              placeholder="一行输入一条数据，建议从EXCEL编辑导入，如果超过3000行建议使用其他策略投放">${targetSchool!}</textarea>
                            </div>
                            <div style="width:40%; height: 500px; float:right; display: inline;">
                                <br><br>&nbsp;&nbsp;记录总数：&nbsp;&nbsp;<input class="input-small" type="number" disabled
                                                                            value="${schoolSize!0}">
                                <br><br>&nbsp;&nbsp;<input type="checkbox" id="schoolAppend">&nbsp;&nbsp;追加模式
                                <br><br>&nbsp;&nbsp;
                                <button name="save_target_btn" type="button" class="btn btn-success">保存投放学校</button>
                                &nbsp;&nbsp;
                                <button id="clear_target_btn_3" type="button" class="btn btn-danger">清空投放学校</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script>
        $(function () {
            var allModel = $('#target_all_modal');
            var regionModel = $('#target_region_modal');
            var labelModel = $('#target_label_modal');
            var userModel = $('#target_user_modal');
            var schoolModel = $('#target_school_modal');
            initDisplay(${targetType!0});
            initTree();

            $("input[name='targetType']").on('change', function () {
                var targetType = $(this).val();
                initDisplay(targetType);
            });
            function initDisplay(targetType) {
                var type = parseInt(targetType);
                switch (type) {
                    case 1:
                        allModel.hide();
                        regionModel.show();
                        labelModel.hide();
                        userModel.hide();
                        schoolModel.hide();
                        break;
//            case 4:  allModel.hide();regionModel.hide();labelModel.show();userModel.hide();schoolModel.hide();break;
                    case 3:
                        allModel.hide();
                        regionModel.hide();
                        labelModel.hide();
                        userModel.hide();
                        schoolModel.show();
                        break;
                    case 2:
                        allModel.hide();
                        regionModel.hide();
                        labelModel.hide();
                        userModel.show();
                        schoolModel.hide();
                        break;
                    case 5:
                    default:
                        allModel.show();
                        regionModel.hide();
                        labelModel.hide();
                        userModel.hide();
                        schoolModel.hide();
                        break;
                }
            }

            function initTree() {
                $('#regionTree').fancytree({
                    extensions: ["filter"],
                    source: ${targetRegion!},
                    checkbox: true,
                    selectMode: 3
                });

                $('#labelTree').fancytree({
                    extensions: ["filter"],
                    source:${labelTree!},
                    checkbox: true,
                    selectMode: 2
                });
            }
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
                    productId: ${(product.id)!},
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
            } else if (type == 2) {
                var targetIds = $('#targetUser').val().trim();
                $.post('saveids.vpage', {
                    productId: ${(product.id)!},
                    type: type,
                    targetIds: targetIds,
                    append: $('#userAppend').is(":checked")
                }, function (data) {
                    if (data.success) {
                        alert("保存用户成功！");
                        window.location.reload();
                    } else {
                        alert("保存用户失败:" + data.info);
                    }
                });
            } else if (type == 3) {
                var schoolIds = $('#targetSchool').val().trim();
                $.post('saveids.vpage', {
                    productId: ${(product.id)!},
                    type: type,
                    targetIds: schoolIds,
                    append: $('#schoolAppend').is(":checked")
                }, function (data) {
                    if (data.success) {
                        alert("保存学校成功！");
                        window.location.reload();
                    } else {
                        alert("保存学校失败:" + data.info);
                    }
                });
            } else if (type == 4) {
                var labelList = [];
                var labelTree = $("#labelTree").fancytree("getTree");
                var labelNodes = labelTree.getSelectedNodes();
                $.map(labelNodes, function (node) {
                    labelList.push(node.key);
                });
                $.post('savelabel.vpage', {
                    productId: ${(product.id)!},
                    type: type,
                    labelList: labelList.join(",")
                }, function (data) {
                    if (data.success) {
                        alert("保存标签成功！");
                        window.location.reload();
                    } else {
                        alert("保存标签失败:" + data.info);
                    }
                });
            } else if (type == 5) {
                $.post('saveids.vpage', {
                    productId: ${(product.id)!},
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

        $("button[id^='clear_target_btn_']").on('click', function () {
            var type = $(this).attr("id").substring("clear_target_btn_".length);
            $.post('cleartargets.vpage', {
                productId: ${(product.id)!},
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

        $('#delete_region_filter').on('click', function () {
            $("#filter_region").val("");
            var regionTree = $("#regionTree").fancytree("getTree");
            regionTree.clearFilter();
        });

        $('#filter_label').keyup(function (e) {
            var match = $(this).val();
            if (e && e.which === $.ui.keyCode.ESCAPE || $.trim(match) === "") {
                $("#delete_label_filter").click();
                return;
            }
            var labelTree = $("#labelTree").fancytree("getTree");
            labelTree.options.filter.mode = "hide";
            labelTree.applyFilter(match);
        }).focus();

        $('#delete_label_filter').on('click', function () {
            $("#filter_label").val("");
            var labelTree = $("#labelTree").fancytree("getTree");
            labelTree.clearFilter();
        });


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
            var show = $('#tag_select').find('option:selected').attr("data-show");
            if (show == 1) {
                $('#tag_input').hide();
            } else {
                $('#tag_input').show();
            }
        }

        function checkUserHits() {
            var labelList = [];
            var labelTree = $("#labelTree").fancytree("getTree");
            var labelNodes = labelTree.getSelectedNodes();
            $.map(labelNodes, function (node) {
                labelList.push(node.key);
            });
            $.post('getlabelhits.vpage', {
                labels: labelList.join(",")
            }, function (data) {
                $('#userCnt').val(data);
            });
        }

        function deleteLabel(labelId) {
            $.post("deletelabel.vpage", {productId: ${(product.id)!}, labelId: labelId}, function (data) {
                if (data.success) {
                    alert("删除成功！");
                    window.location.reload();
                } else {
                    alert("删除失败:" + data.info);
                }
            });
        }

    </script>
    </#if>
</@layout_default.page>