<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑广告" page_num=9 jqueryVersion="1.7.1">
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
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
        <strong>广告概览</strong> &nbsp;&nbsp;&nbsp;&nbsp;
    </legend>
    <div class="inline">
        <table>
            <tr>
                <#if adDetail??>
                    <td class="info_td">广告ID：<span class="info_td_txt">${adDetail.id!'--'}</span></td>
                    <td class="info_td">广告名称：<span class="info_td_txt"><a href="/opmanager/advertisement/addetail.vpage?adId=${adId}">${adDetail.name!'--'}</a></span></td>
                    <td class="info_td">广告编码：<span class="info_td_txt">${adDetail.adCode!'--'}</span></td>
                    <td class="info_td">广告状态：<span class="info_td_txt"><#if adDetail.status==1>上线中<#elseif adDetail.status==9>已下线<#else>新建</#if></span></td>
                <#else>
                    <td class="info_td">广告ID：<span class="info_td_txt">${adId!'--'}</span></td>
                    <td class="info_td">广告名称：<span class="info_td_txt">--</span></td>
                    <td class="info_td">广告编码：<span class="info_td_txt">--</span></td>
                    <td class="info_td">广告编码：<span class="info_td_txt">--</span></td>
                </#if>
            </tr>
            <tr>
                <#if adSlot??>
                    <td class="info_td">广告位ID：<span class="info_td_txt">${(adSlot.id)!'--'}</span></td>
                    <td class="info_td">广告位名称：<span class="info_td_txt">${(adSlot.name)!'--'}</span></td>
                <#else>
                    <td class="info_td">广告位ID：<span class="info_td_txt">--</span></td>
                    <td class="info_td">广告位名称：<span class="info_td_txt">--</span></td>
                </#if>
            </tr>
        </table>
    </div>
    <#if !error??>
    <#--广告投放对象-->
    <div style="margin-top: 10px; display: block;">
        <legend class="legend_title"><strong>广告投放对象</strong> &nbsp;&nbsp;&nbsp;&nbsp;</legend><br>
        <div style="height: 500px;">
            <ul class="nav nav-tabs" role="tablist">
                <li role="presentation" class="<#if has_5!false>active</#if>">
                    <a href="#target-all" aria-controls="target-all" role="tab" data-toggle="tab">
                        <strong><#if has_5!false> √ </#if>投放所有用户</strong>
                    </a>
                </li>
                <li role="presentation" class="<#if has_1!false>active</#if>">
                    <a href="#target-region" aria-controls="target-region" role="tab" data-toggle="tab">
                        <strong><#if has_1!false> √ </#if>投放指定地区</strong>
                    </a>
                </li>
                <li role="presentation">
                    <a href="#target-region-code" aria-controls="target-region" role="tab" data-toggle="tab">
                        <strong><#if has_1!false> √ </#if>输入地区Code</strong>
                    </a>
                </li>
                <li role="presentation" class="<#if has_2!false>active</#if>">
                    <a href="#target-user" aria-controls="target-user" role="tab" data-toggle="tab">
                        <strong<#if has_2!false> √ </#if>>投放指定用户</strong>
                    </a>
                </li>
                <li role="presentation" class="<#if has_3!false>active</#if>">
                    <a href="#target-school" aria-controls="target-school" role="tab" data-toggle="tab">
                        <strong><#if has_3!false> √ </#if>投放指定学校</strong>
                    </a>
                </li>
            </ul>
            <div class="tab-content">
                <div role="tabpanel" class="tab-pane <#if has_5!false>active</#if>" id="target-all" style="width: 70%;">
                   <pre>
                        <h3 style="text-align: center;">确认此项之后，广告将针对所有用户投放</h3>
                        <h4 style="text-align: center; color: red;"><#if has_5!false>已确认<#else>未确认</#if></h4>
                    </pre>
                    <div style="float: right;">
                        <#if editable?? && editable>
                            <br><br>&nbsp;&nbsp;<button name="save_target_btn" type="button" class="btn btn-success" data-type="5">确  认</button>
                            &nbsp;&nbsp;<button id="clear_target_btn_5" type="button" class="btn btn-danger">清  除</button>
                        </#if>
                    </div>
                </div>

                <div role="tabpanel" class="tab-pane <#if has_1!false>active</#if>" id="target-region">
                    <div id="regionTree" class="sampletree" style="width:30%; float:left; height: 410px; display: inline;"></div>
                    <div style="height: 500px; float:left; display: inline;">
                        &nbsp;&nbsp;筛选 <input name="filter_region" type="text" class="input-small" id="filter_region" placeholder="筛选条件...">
                        <button name="delete_region_filter" id="delete_region_filter">&times;</button>
                        <#if editable?? && editable>
                            <br><br>&nbsp;&nbsp;<button name="save_target_btn" type="button" class="btn btn-success"  data-type="1">保存投放地区</button>
                            &nbsp;&nbsp;<button id="clear_target_btn_1" type="button" class="btn btn-danger">清空投放地区</button>
                        </#if>
                    </div>
                </div>

                <div role="tabpanel" class="tab-pane" id="target-region-code">
                    <div style="width:300px; height: 420px; display: inline;">
                        <textarea id="targetRegionCode" name="targetRegionCode" class="form-control" rows="20"
                                  style="width:250px;  resize: none;height: 400px; float: left; display: inline;"
                                  placeholder="一行输入一条数据，建议从EXCEL编辑导入，如果超过3000行建议使用其他策略投放">${targetRegionCode}</textarea>
                    </div>
                    <div style="height: 500px; display: inline;">
                        <#if editable?? && editable>
                            <br><br>
                            &nbsp;&nbsp; 编辑 code 会自动选中树形菜单, 编辑树形菜单不会自动添加 code ,
                            <br><br>
                            &nbsp;&nbsp; 编辑后请检查树形菜单, 并保存
                        </#if>
                    </div>
                </div>

                <div role="tabpanel" class="tab-pane <#if has_2!false>active</#if>" id="target-user">
                    <div style="width:300px; height: 420px; display: inline;">
                        <textarea id="targetUser" name="targetUser" class="form-control" rows="20"
                                  style="width:250px; resize: none;height: 400px; float: left; display: inline;"
                                  placeholder="一行输入一条数据，建议从EXCEL编辑导入，如果超过3000行建议使用其他策略投放">${targetUser!}</textarea>
                    </div>
                    <div style="height: 500px; display: inline;">
                        <br><br>&nbsp;&nbsp;记录总数：&nbsp;&nbsp;<input class="input-small" type="number" disabled value="${userSize!0}">
                        <#if editable?? && editable>
                            <br><br>&nbsp;&nbsp;<input type="checkbox" id="userAppend">&nbsp;&nbsp;追加模式
                            <br><br>&nbsp;&nbsp;<button name="save_target_btn" type="button" class="btn btn-success" data-type="2">保存投放用户</button>
                            &nbsp;&nbsp;<button id="clear_target_btn_2" type="button" class="btn btn-danger">清空投放用户</button>
                        </#if>
                    </div>
                </div>

                <div role="tabpanel" class="tab-pane <#if has_3!false>active</#if>" id="target-school">
                    <div style="width:300px; height: 420px; display: inline;">
                        <textarea id="targetSchool" name="targetSchool" class="form-control" rows="20"
                                  style="width:250px;  resize: none;height: 400px; float: left; display: inline;"
                                  placeholder="一行输入一条数据，建议从EXCEL编辑导入，如果超过3000行建议使用其他策略投放">${targetSchool!}</textarea>
                    </div>
                    <div style="height: 500px; display: inline;">
                        <br><br>&nbsp;&nbsp;记录总数：&nbsp;&nbsp;<input class="input-small" type="number"  disabled value="${schoolSize!0}">
                        <#if editable?? && editable>
                            <br><br>&nbsp;&nbsp;<input type="checkbox" id="schoolAppend">&nbsp;&nbsp;追加模式
                            <br><br>&nbsp;&nbsp;<button name="save_target_btn" type="button" class="btn btn-success" data-type="3">保存投放学校</button>
                            &nbsp;&nbsp;<button id="clear_target_btn_3" type="button" class="btn btn-danger">清空投放学校</button>
                        </#if>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <#--广告投放标签-->
    <div style="margin-top: 10px; display: block;">
        <legend class="legend_title"><strong>广告投放标签</strong></legend><br>
        <div>
            <div style="margin-bottom: 2px;">
                <strong>已选标签：</strong><span id="selectedTag"></span>
                <br/>
                <strong>关联数量：</strong>
                <input class="input-small" type="number" id="userCnt" disabled value="${labelUser!0}">
                <button class="btn btn-warning" onclick="checkUserHits()" style="margin-left: 2px; margin-bottom: 10px;"> 查 询 </button>
                <span style="color: red;">(此数字仅供参考)</span>
                <br/>
                <strong>标签检索：</strong><input name="filter_label" type="text" class="input-small" id="filter_label" placeholder="筛选条件...">
                <button name="delete_label_filter" id="delete_label_filter" style="margin-left: 2px; margin-bottom: 10px;" >&times;</button>
            </div>
            <div id="labelTree" class="sampletree" style="width: 60%;"></div>
        </div>
        <div>
            <div style="margin-top: 2px;">
                <button class="btn btn-primary" onclick="helpTools()">辅助工具</button>
                <#if editable?? && editable>
                    <button name="save_target_btn" type="button" class="btn btn-success" data-type="4">保存标签组</button>
                    <button id="clear_target_btn_4" type="button" class="btn btn-danger">清空标签组</button>
                </#if>
            </div>
            <div style="height: 200px; overflow: scroll; margin-top: 2px;">
                <table class="table table-striped table-bordered table-condensed" style="font-size: 14px; margin-top: 10px;">
                    <#if labelGroupList?? && labelGroupList?has_content>
                        <thead>
                        <th colspan="3"><strong>总计关联用户数</strong></th>
                        <th width="100px;" id="total_hit_cnt"><button onclick="getTotalHits(${adId!})" class="btn btn-info">查看</button></th>
                        </thead>
                        <tr><th>标签组</th><th>操作</th><th>标签组</th><th>操作</th></tr>
                        <#list labelGroupList as label>
                            <#if label_index%2==0><tr></#if>
                                <td style="width: 40%">${label.targetStr!""}</td>
                                <td style="width: 10%;"><#if editable?? && editable><button class="btn btn-danger" onclick="deleteLabel(${label.targetId})">删除</button></#if></td>
                            <#if label_index%2==1 || !label_has_next><#if label_index%2==0><td colspan="2"></td></#if></tr></#if>
                        </#list>
                    </#if>
                </table>
            </div>
        </div>
    </div>

    <#--广告投放约束-->
    <div style="margin-top: 10px; clear: both;">
        <legend class="legend_title"><strong>广告投放约束</strong> &nbsp;&nbsp;&nbsp;&nbsp;</legend>
        <div style="height: 300px;">
            <#if editable?? && editable>
                <br><button class="btn btn-success" id="add_tag_btn">增加约束</button><br><br>
            </#if>
            <table class="table table-condensed table-striped table-bordered" style="width: 80%;">
                <thead>
                <tr bgcolor="#f0ffff">
                    <th><strong>约束名称</strong></th>
                    <th><strong>配置内容</strong></th>
                    <th><strong>约束说明</strong></th>
                    <th><strong>操作</strong></th>
                </tr>
                </thead>
                <#if tagMap??>
                    <tbody>
                        <#list tagMap?keys as key>
                        <tr>
                            <td>${tagMap[key].tagName!""}</td>
                            <td id="VAL_${tagMap[key].tagName!}">${tagMap[key].tagValue!''}</td>
                            <td id="CMT_${tagMap[key].tagName!}">${tagMap[key].tagComment!''}</td>
                            <td>
                                <#if editable?? && editable>
                                    <a id="EDIT_${tagMap[key].tagName!}">编辑</a>
                                    <a id="DEL_${tagMap[key].tagName!}">删除</a>
                                </#if>
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
<div id="help_dialog" class="modal hide fade" style="left:35%; width: 50%">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>用户标签查询</h3>
    </div>
    <div class="modal-body dl-horizontal">
        <dl>
            <dt>输入用户ID：</dt>
            <dd>
                <input id="helpId" type="text" class="input-xlarge" />
                <span>(以逗号分隔)</span>
            </dd>
        </dl>
        <dl>
            <div class="box-content">
                <table class="table table-condensed">
                    <thead>
                    <tr>
                        <th width="20%">用户ID</th>
                        <th>标签</th>
                    </tr>
                    </thead>
                    <tbody id="tagBody" >
                    </tbody>
                </table>
            </div>
        </dl>
        <div class="modal-footer">
            <button id="help_btn" class="btn btn-info">查询</button>
        </div>
    </div>
</div>
<div id="loadingDiv" style="display:none ;position: absolute;top: 0;left: 0;width: 150%;height: 200%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
    <p style="text-align: center;top: 30%;position: relative;">正在查询，请等待……</p>
</div>
<script>

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
            $.post('deltag.vpage', {adId:${adId}, tagName: tagName}, function (data) {
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
            if (!confirm("是否确认保存广告约束？")) {
                return false;
            }
            var tagName = $('#tag_select').find('option:selected').val();
            var tagVal = $('#tag_value').val();
            var tagComment = $('#tag_comment').val();
            var type = $('#tag_select').find('option:selected').attr("data-show");
            var tagData = {
                adId: ${adId},
                tagName: tagName,
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
//            if (!confirm("是否确认保存广告投放对象？")) {
//                return false;
//            }
            if (!confirm("更改广告投放对象将需要重新发起审核，是否确认？")) {
                return false;
            }
            var type = $(this).data("type");
            if (type == 1) {
                var regionList = [];
                var regionTree = $("#regionTree").fancytree("getTree");
                var regionNodes = regionTree.getSelectedNodes();
                $.map(regionNodes, function (node) {
                    regionList.push(node.key);
                });
                $.post('saveregion.vpage', {
                    adId: ${adId},
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
                    adId: ${adId},
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
                    adId: ${adId},
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
                    adId: ${adId},
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
                    adId: ${adId},
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
//            if (!confirm("是否确认清除广告投放对象？")) {
//                return false;
//            }
            if (!confirm("更改广告投放对象将需要重新发起审核，是否确认？")) {
                return false;
            }
            var type = $(this).attr("id").substring("clear_target_btn_".length);
            $.post('cleartargets.vpage', {
                adId: ${adId},
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

        $('#help_btn').on('click', function() {
            var uid = $('#helpId').val().trim();
            if (uid == '') {
                var err = "<tr><td colspan=\"2\">该批用户无数据</td><tr>";
                $('#tagBody').html(err);
                return true;
            }
            if (uid.split(",").length > 10) {
                var tomuch = "<tr><td colspan=\"2\">一次只支持查询10个用户</td><tr>";
                $('#tagBody').html(tomuch);
                return true;
            }
            $.post('finduserlabel.vpage', {uid:uid}, function(data){
               if (data.success){
                   var tags = data.labels;
                   $('#tagBody').html('');
                   for(var i=0; i<tags.length; i++){
                       var str = "<tr><td>"+tags[i].uid+"</td>";
                       str += "<td class=\"center\">"+tags[i].label+"</td></tr>";
                       $('#tagBody').append(str);
                   }
               } else {
                   var err = "<tr><td colspan=\"2\">该批用户无数据</td><tr>";
                   $('#tagBody').html(err);
               }
            });
        });

        //code 同步到 tree
        $("#targetRegionCode").bind('input',function () {
            var tree = $("#regionTree").fancytree("getTree");
            //取消所有选中
            var selectedNode = tree.getSelectedNodes();
            if(selectedNode.length > 0){
                for(j = 0; j < selectedNode.length; j++) {
                    tree.getNodeByKey(selectedNode[j].key).setSelected(false);
                }
            }
            //重新选中
            var code = $("#targetRegionCode").val().trim().split(/\n/g);
            if(code.length > 0){
                for (var i = 0; i < code.length; i++) {
                    if (code[i] != "") {
                        tree.getNodeByKey(code[i]).setSelected(true);
                    }
                }
            }
        })
    });

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
            selectMode: 2,
            select: function(event, data) {
                var nodes = data.tree.getSelectedNodes();
                var tagNames = [];
                $.map(nodes, function (node) {
                    tagNames.push(node.title);
                });
                $("#selectedTag").text(tagNames.join("  |  "))
            }
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

    function checkUserHits() {
        var labelList = [];
        var labelTree = $("#labelTree").fancytree("getTree");
        var labelNodes = labelTree.getSelectedNodes();
        $.map(labelNodes, function (node) {
            labelList.push(node.key);
        });
        $('#loadingDiv').show();
        $.post('getlabelhits.vpage', {
            labels: labelList.join(",")
        }, function (data) {
            $('#userCnt').val(data);
            $('#loadingDiv').hide();
        });
    }

    function getTotalHits(adId) {
        if (adId == '') {
            $('#total_hit_cnt').html("<strong>0</strong>");
            return true;
        }
        $('#loadingDiv').show();
        $.post('gettotallabelhits.vpage', {
            adId: adId
        }, function (data) {
            $('#total_hit_cnt').html("<strong>" + data + "</strong>");
            $('#loadingDiv').hide();
        });
    }

    function deleteLabel(labelId) {
        if (!confirm("更改广告投放对象将需要重新发起审核，是否确认？")) {
            return false;
        }
        $.post("deletelabel.vpage", {adId: ${adId}, labelId: labelId}, function(data) {
            if (data.success) {
                alert("删除成功！");
                window.location.reload();
            } else {
                alert("删除失败:" + data.info);
            }
        });
    }

    function helpTools() {
        $('#helpId').val("");
        $('#tagBody').html("");
        $('#help_dialog').modal('show');
    }

</script>
</@layout_default.page>