<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<div class="span9">
    <fieldset>
        <legend>课程班级管理</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get"
          action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <span style="white-space: nowrap;">
                课程ID：<select id="selectLessonId" name="selectLessonId">
                <#if lessonIds?? && lessonIds?size gt 0>
                    <#list lessonIds as lessonId>
                        <option value="${lessonId}">${lessonId}</option>
                    </#list>
                <#else>
                    <option value="">暂无数据</option>
                </#if>
            </select>
            </span>
            <span style="white-space: nowrap;">
                微信号：<input type="text" id="wechat" name="wechat" value="${wechat!''}"/>
            </span>
            <span style="white-space: nowrap;">
                微信群名称：<input type="text" id="wechatName" name="wechatName" value="${wechatName!''}"/>
            </span>
        </div>
    </form>
    <button class="btn btn-primary" type="button" id="searchBtn">查询</button>
    <a class="btn btn-warning" id="createStudyGroup" name="wechat_detail">新建课程班级</a>
    <a class="btn btn-success" id="exportData" name="exportData">导出班级数据</a>
    <a class="btn btn-danger" id="importData" name="importData">导入班级数据</a>
    <a class="btn btn-info" id="updateAreaButton" name="updateAreaButton">批量更新班级区数据</a>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>序号</th>
                        <th>班级 id</th>
                        <th>微信群名称</th>
                        <th>个人微信号</th>
                        <th>班级性质</th>
                        <th>课程激活码</th>
                        <th>课程激活链接</th>
                        <th>班级区名称</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as  groupInfo>
                            <tr>
                                <td>${groupInfo_index+1!''}</td>
                                <td>${groupInfo.id!''}</td>
                                <td>${groupInfo.wechateGroupName!''}</td>
                                <td>${groupInfo.wechatNumber!''}</td>
                                <td>${groupInfo.type!''}</td>
                                <td>${groupInfo.code!''}</td>
                                <td>${groupInfo.active_url!''}</td>
                                <td>${groupInfo.areaName!''}</td>
                                <td>
                                    <a class="btn btn-primary"
                                       data-group_id="${groupInfo.id!''}"
                                       data-wechat_group_name="${groupInfo.wechateGroupName!''}"
                                       data-wechat_id="${groupInfo.wechatId!''}"
                                       data-area_id="${groupInfo.areaId!''}"
                                       data-wechat_number="${groupInfo.wechatNumber!''}" name="group_edit">修改</a>
                                </td>
                            </tr>
                            </#list>
                        <#else>
                        <tr>
                            <td colspan="7" style="text-align: center">暂无数据</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
                <ul class="message_page_list">
                <#--<li><a href="#" onclick="pagePost(1)" title="Pre">首页</a></li>-->
                <#--<#if hasPrev>-->
                <#--<li><a href="#" onclick="pagePost(${currentPage-1})" title="Pre">&lt;</a></li>-->
                <#--<#else>-->
                <#--<li class="disabled"><a href="#">&lt;</a></li>-->
                <#--</#if>-->
                <#--<li class="disabled"><a>第 ${currentPage!} 页</a></li>-->
                <#--<li class="disabled"><a>共 <#if totalPage==0>1<#else>${totalPage!}</#if> 页</a></li>-->
                <#--<#if hasNext>-->
                <#--<li><a href="#" onclick="pagePost(${currentPage+1})" title="Next">&gt;</a></li>-->
                <#--<#else>-->
                <#--<li class="disabled"><a href="#">&gt;</a></li>-->
                <#--</#if>-->
                </ul>
            </div>
        </div>
    </div>
</div>

<#-- 修改 StudyGroup -->
<div id="wechat_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>修改微信群名称</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <input id="updageGroupId" type="hidden"/>
            <ul class="inline">
                <input type="text" style="display: none" id="wechatId" value=""/>
                <li>
                    <dt>群名称</dt>
                    <dd><input type="text" id="newWechaGroupName" value=""/></dd>
                </li>
                <li>
                    <dt>个人微信号</dt>
                    <dd>
                        <select id="opWechatName" name="opWechatName"></select>
                    </dd>
                </li>
                <li>
                    <dt>班级区</dt>
                    <dd><select id="updateGroupArea" name="updateGroupArea">

                    </select></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="save_record" class="btn btn-primary">保 存</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="import_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>批量导入班级信息</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <input id="updageGroupId" type="hidden"/>
            <ul class="inline">
                <li>
                    <dt>上传Excel文件</dt>
                    <dd><input type="file" id="excelFile"
                               accept=".csv, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
                               style="float: left"/></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="update_area_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>批量更新班级区信息</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <input id="updageGroupId" type="hidden"/>
            <ul class="inline">
                <li>
                    <dt>上传Excel文件</dt>
                    <dd><input type="file" id="updateArea"
                               accept=".csv, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
                               style="float: left"/></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="create_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>创建班级</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>群名称</dt>
                    <dd><input type="text" id="createGroupName" value=""/></dd>
                </li>
                <li>
                    <dt>个人微信号</dt>
                    <dd><select id="wechatNumbers" name="wechatNumbers">

                    </select></dd>
                </li>
                <li>
                    <dt>当前课程id</dt>
                    <dd>${selectedLessonId!}</dd>
                    <dd>如需更换请重新选择课程 id，然后点击查询后新建 ！</dd>
                </li>
                <li>
                    <dt>班级区</dt>
                    <dd><select id="createGroupArea" name="createGroupArea">

                    </select></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="save_record_create" class="btn btn-primary">保 存</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
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


            //初始化lessonid的list,后面做校验用
            var array = [];
            $("#selectLessonId").val("${selectedLessonId!}");
            $("#selectLessonId").find("option").each(function () {
                var txt = $(this).val();
                if (txt != '') {
                    array.push(txt);
                }
            });
            console.log(array);

            $("#searchBtn").on('click', function () {
                $("#pageNum").val(1);
                $("#op-query").submit();
            });

            //新建直接弹出modal
            $("#createStudyGroup").on('click', function () {
                var lessonId =${selectedLessonId!};
                $.ajax({
                    type: "GET",
                    url: "getWechatNumberByLessonId.vpage",
                    data: {"lessonId": lessonId},
                    success: function (data) {
                        if (data.success) {
                            $("#wechatNumbers").empty()
                            $("#wechatNumbers").prepend("<option value='0'>请选择</option>");
                            for (var i = 0; i < data.wechatList.length; i++) {
                                $("#wechatNumbers").append("<option value='" + data.wechatList[i].id + "'>" + data.wechatList[i].wechatNumber + "</option>");
                            }
                        } else {
                            $("#wechatNumbers").prepend("<option value='0'>获取微信号失败</option>");
                        }
                    }
                });
                $.ajax({
                    type: "GET",
                    url: "getGroupAreaByLessonId.vpage",
                    data: {"lessonId": lessonId},
                    success: function (data) {
                        if (data.success) {
                            $("#createGroupArea").empty();
                            $("#createGroupArea").prepend("<option value='0'>请选择</option>");
                            for (var i = 0; i < data.groupAreas.length; i++) {
                                $("#createGroupArea").append("<option value='" + data.groupAreas[i].id + "'>" + data.groupAreas[i].groupAreaName + "</option>");
                            }
                        } else {
                            $("#createGroupArea").prepend("<option value='0'>获取班级区失败</option>");
                        }
                    }
                });
                $("#create_dialog").modal('show');
            });

            //编辑的时候弹出modal时加载数据
            $("a[name='group_edit']").on('click', function () {
                var groupId = $(this).data("group_id");                  //studygroupId
                var wechatNumber = $(this).data("wechat_number");        //个人微信号
                var wechatGroupName = $(this).data("wechat_group_name"); //微信群名称
                var wechatId = $(this).data("wechat_id");                //opwechatId
                if (!groupId) {
                    console.log("opId null");
                    return;
                }
                var lessonId = ${selectedLessonId!};
                $.ajax({
                    type: "GET",
                    url: "getWechatNumberByLessonId.vpage",
                    data: {
                        "lessonId": lessonId
                    },
                    success: function (data) {
                        if (data.success) {
                            if (wechatId && wechatNumber) {
                                $("#opWechatName").prepend("<option value='" + wechatId + "'>" + wechatNumber + "</option>");
                            } else {
                                $("#opWechatName").empty();
                                $("#opWechatName").prepend("<option value='0'>请选择</option>");
                            }
                            for (var i = 0; i < data.wechatList.length; i++) {
                                $("#opWechatName").append("<option value='" + data.wechatList[i].id + "'>" + data.wechatList[i].wechatNumber + "</option>");
                            }
                            $("#newWechaGroupName").val(wechatGroupName);
                            $("#updageGroupId").val(groupId);
                        } else {
                            $("#opWechatName").prepend("<option value='0'>获取微信号失败</option>");
                        }
                    }
                });
                $.ajax({
                    type: "GET",
                    url: "getGroupAreaByLessonId.vpage",
                    data: {"lessonId": lessonId},
                    success: function (data) {
                        if (data.success) {
                            $("#updateGroupArea").empty();
                            $("#updateGroupArea").prepend("<option value='0'>请选择</option>");
                            for (var i = 0; i < data.groupAreas.length; i++) {
                                console.log(data.groupAreas[i]);
                                $("#updateGroupArea").append("<option value='" + data.groupAreas[i].id + "'>" + data.groupAreas[i].groupAreaName + "</option>");
                            }
                        } else {
                            $("#updateGroupArea").prepend("<option value='0'>获取班级区失败</option>");
                        }
                    }
                });
                $("#wechat_dialog").modal('show');
            });

            //导入数据时，弹窗
            $("#importData").on('click', function () {
                $("#import_dialog").modal('show');
            });

            //更新班级区数据时，弹窗
            $("#updateAreaButton").on('click', function () {
                $("#update_area_dialog").modal('show');
            });

            //新建班组
            $("#save_record_create").on('click', function () {
                var lessonId = ${selectedLessonId!};
                var newName = $("#createGroupName").val();
                var wechatId = $("#wechatNumbers").find("option:selected").val();
                var areaId = $("#createGroupArea").val();
                if (!newName) {
                    alert("请填写新的群名称！");
                    return;
                }
                if (!wechatId || wechatId == 0) {
                    alert("请选择个人微信号");
                    return;
                }
                var postData = {
                    lessonId: lessonId,
                    name: newName,
                    wechatId: wechatId,
                    areaId: areaId
                };
                $.ajax({
                    url: 'createStudGroup.vpage',
                    type: 'POST',
                    async: false,
                    data: postData,
                    success: function (data) {
                        if (data.success) {
                            alert("创建成功！班级激活码：" + data.code);
                            $("#create_dialog").modal('hide');
                        } else {
                            alert("创建失败哦！");
                        }
                        window.location.reload();
                    }
                });

            });


            //保存
            $("#save_record").on('click', function () {
                var groupId = $("#updageGroupId").val();
                var newName = $("#newWechaGroupName").val();
                var wechatId = $("#opWechatName").find("option:selected").val();
                var areaId = $("#updateGroupArea").val();
                if (!newName) {
                    alert("请填写新的群名称！");
                    return;
                }
                if (!wechatId || wechatId == 0) {
                    alert("请选择个人微信号");
                    return;
                }

                var postData = {
                    id: groupId,
                    newName: newName,
                    opWechatName: wechatId,
                    areaId: areaId
                };
                $.ajax({
                    url: 'updateWechatGroupName.vpage',
                    type: 'POST',
                    async: false,
                    data: postData,
                    success: function (data) {
                        if (data.success) {
                            alert("保存成功");
                            $("#wechat_dialog").modal('hide');
                        } else {
                            console.log("data error");
                        }
                        window.location.reload();
                    }
                });

            });

            //导出数据
            $("#exportData").on('click', function () {
                var selectLessonId = $("#selectLessonId").val();
                var wechat = $("#wechat").val();
                var wechatName = $("#wechatName").val();
                if (!selectLessonId) {
                    alert("课程Id不能为空");
                    return;
                }
                location.href = "/opmanager/studyTogether/exportClazzData.vpage?selectLessonId=" + selectLessonId + "&wechat=" + wechat + "&wechatName=" + wechatName;
            });
            //导入数据
            $("#excelFile").on('change', function () {
                var $this = $(this);
                var ext = $this.val().split('.').pop().toLowerCase();
                if ($this.val() != '') {
                    if ($.inArray(ext, ['xls', 'xlsx']) == -1) {
                        alert("仅支持以下格式【'xls', 'xlsx'】");
                        return false;
                    }
                    var formData = new FormData();
                    formData.append('source_file', $this[0].files[0]);
                    $.ajax({
                        url: 'importClazzData.vpage',
                        type: 'POST',
                        data: formData,
                        processData: false,
                        contentType: false,
                        success: function (data) {
                            if (data.success) {
                                alert("导入成功");
                                $("#excelFile").val("");
                            } else {
                                alert(data.info);
                                $("#excelFile").val("");
                            }
                        }
                    });
                }
            });

            //导入数据
            $("#updateArea").on('change', function () {
                var $this = $(this);
                var ext = $this.val().split('.').pop().toLowerCase();
                if ($this.val() != '') {
                    if ($.inArray(ext, ['xls', 'xlsx']) == -1) {
                        alert("仅支持以下格式【'xls', 'xlsx'】");
                        return false;
                    }
                    var formData = new FormData();
                    formData.append('source_file', $this[0].files[0]);
                    $.ajax({
                        url: 'updateGroupAreaData.vpage',
                        type: 'POST',
                        data: formData,
                        processData: false,
                        contentType: false,
                        success: function (data) {
                            if (data.success) {
                                alert("更新成功");
                                $("#updateArea").val("");
                            } else {
                                alert(data.info);
                                $("#updateArea").val("");
                            }
                        }
                    });
                }
            });
        });


        function pagePost(pageNumber) {

            $("#pageNum").val(pageNumber);
            $("#op-query").submit();
        }
    </script>
</@layout_default.page>