<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="配置课程投放对象" page_num=17>
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
            <strong>课程概览</strong> &nbsp;&nbsp;&nbsp;&nbsp;
        </legend>
        <div class="inline">
            <table>
                <tbody><tr>
                    <td class="info_td">课程ID：<span class="info_td_txt">${course.id!0}</span></td>
                    <td class="info_td">课程标题：<span class="info_td_txt"><a href="/mizar/course/coursedetail.vpage?courseId=${course.id!''}">${course.title!''}</a></span></td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="form-horizontal">
            <legend class="legend_title"><strong>课程投放对象</strong> &nbsp;&nbsp;&nbsp;&nbsp;</legend><br>
            <div style="height: 500px;">
                <div>
                    <table class="table table-stripped" style="width: 600px;" >
                        <thead>
                        <tr>
                            <th <#if has_3?? && has_3> bgcolor="#cd5c5c"</#if>>
                                <input type="radio" name="targetType" value=3 <#if targetType??><#if targetType == 3> checked="checked" </#if></#if>/>&nbsp;&nbsp;投放所有用户
                            </th>
                            <th <#if has_1?? && has_1> bgcolor="#00ffff" </#if>>
                                <input type="radio" name="targetType" value=1 <#if targetType??><#if targetType == 1> checked="checked" </#if></#if>/>&nbsp;&nbsp;投放指定地区
                            </th>
                            <th <#if has_2?? && has_2>bgcolor="#7fffd4" </#if>>
                                <input type="radio" name="targetType" value=2 <#if targetType??><#if targetType == 2> checked="checked" </#if></#if>/>&nbsp;&nbsp;投放指定学校
                            </th>
                        </tr>
                        </thead>
                    </table>
                    <br>
                    <div>
                        <div id="target_all_modal" class="span7" style="display: none;">
                        <pre>
                            <h3 style="text-align: center;">确认此项之后，公众号将针对所有用户投放</h3>
                        </pre>
                            <div style="float: right;">
                                <#if course.status=="OFFLINE">
                                    <br><br>&nbsp;&nbsp;<button name="save_target_btn" type="button" class="btn btn-success">确  认</button>
                                    &nbsp;&nbsp;<button id="clear_target_btn_3" type="button" class="btn btn-danger">清  除</button>
                                </#if>
                            </div>
                        </div>
                        <div id="target_region_modal" class="span7" style="display: none;">
                            <div id="regionTree" class="sampletree" style="width:60%; height: 410px; float: left; display: inline;"></div>
                            <div style="width:40%; height: 500px; float:right; display: inline;">
                                &nbsp;&nbsp;筛选 <input name="filter_region" type="text" class="input-small" id="filter_region" placeholder="筛选条件...">
                                <button name="delete_region_filter" id="delete_region_filter">&times;</button>
                                <#if course.status=="OFFLINE">
                                    <br><br>&nbsp;&nbsp;<button name="save_target_btn" type="button" class="btn btn-success">保存投放地区</button>
                                    &nbsp;&nbsp;<button id="clear_target_btn_1" type="button" class="btn btn-danger">清空投放地区</button>
                                </#if>
                            </div>
                        </div>
                        <div id="target_school_modal" class="span7" style="display: none;">
                            <div style="width:60%; height: 420px; float: left; display: inline;">
                            <textarea type="text" id="targetSchool" name="targetSchool" class="form-control" rows="20"
                                      style="width:80%; resize: none;height: 400px; float: left; display: inline;"
                                      placeholder="一行输入一条数据，建议从EXCEL编辑导入，如果超过3000行建议使用其他策略投放">${targetSchool!}</textarea>
                            </div>
                            <div style="width:40%; height: 500px; float:right; display: inline;">
                                <br><br>&nbsp;&nbsp;记录总数：&nbsp;&nbsp;<input class="input-small" type="number"  disabled value="${schoolSize!0}">
                                <#if course.status=="OFFLINE">
                                    <br><br>&nbsp;&nbsp;<input type="checkbox" id="schoolAppend">&nbsp;&nbsp;追加模式
                                    <br><br>&nbsp;&nbsp;<button name="save_target_btn" type="button" class="btn btn-success">保存投放学校</button>
                                    &nbsp;&nbsp;<button id="clear_target_btn_2" type="button" class="btn btn-danger">清空投放学校</button>
                                </#if>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script>
        $(function(){
            var allModel = $('#target_all_modal');
            var regionModel = $('#target_region_modal');
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
                    case 1: allModel.hide();regionModel.show();schoolModel.hide();break;
                    case 2:  allModel.hide();regionModel.hide();schoolModel.show();break;
                    case 3:
                    default: allModel.show();regionModel.hide();schoolModel.hide();break;
                }
            }

            function initTree() {
                $('#regionTree').fancytree({
                    extensions: ["filter"],
                    source: ${targetRegion!},
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
                    courseId: "${(course.id)!}",
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
                var schoolIds = $('#targetSchool').val().trim();
                $.post('saveids.vpage', {
                    courseId: "${(course.id)!}",
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
            } else if (type == 3) {
                $.post('saveids.vpage', {
                    courseId: "${(course.id)!}",
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
                courseId: "${(course.id)!}",
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

    </script>
    </#if>
</@layout_default.page>