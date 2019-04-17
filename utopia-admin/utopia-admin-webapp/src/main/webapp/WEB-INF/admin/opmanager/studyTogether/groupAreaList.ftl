<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<div class="span9">
    <fieldset>
        <legend>班级区管理</legend>
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
                班级区名称：<input type="text" id="areaName" name="areaName" value="${wechat!''}"/>
            </span>
        </div>
    </form>
    <button class="btn btn-primary" type="button" id="searchBtn">查询</button>
    <a class="btn btn-warning" id="createArea" name="createArea">新建班级区</a>
    <a class="btn btn-success" id="exportData" name="exportData">导出班级数据</a>
    <a class="btn btn-danger" id="importData" name="importData">导入班级区数据</a>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>序号</th>
                        <th>课程ID</th>
                        <th>课程名称</th>
                        <th>期数</th>
                        <th>班级区ID</th>
                        <th>班级区名称</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as  areaInfo>
                            <tr>
                                <td>${areaInfo_index+1!''}</td>
                                <td>${areaInfo.lessonId!''}</td>
                                <td>${lesson.title!''}</td>
                                <td>${lesson.phase!''}</td>
                                <td>${areaInfo.id!''}</td>
                                <td>${areaInfo.groupAreaName!''}</td>
                                <td>
                                    <a class="btn btn-primary"
                                       data-area_id="${areaInfo.id!''}"
                                       data-lesson_id="${areaInfo.lessonId!''}"
                                       data-group_area_name="${areaInfo.groupAreaName!''}" name="area_edit">修改</a>
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
<div id="edit_area_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>修改班级区</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
        <#--<input id="areaId" type="hidden"/>-->
            <ul class="inline">
                <input type="text" style="display: none" id="updateAreaId" value=""/>
                <li>
                    <dt>课程Id</dt>
                    <dd><input type="text" id="updateLessonId" value="" readonly="readonly"/></dd>
                </li>
                <li>
                    <dt>班级区名称</dt>
                    <dd><input type="text" id="updateAreaName" value=""/></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button class="btn btn-primary save_record">保 存</button>
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

<div id="create_area_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>新建班级区</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
        <#--<input id="areaId" type="hidden"/>-->
            <ul class="inline">
                <input type="text" style="display: none" id="createAreaId" value=""/>
                <li>
                    <dt>课程Id</dt>
                    <dd><input type="text" id="createLessonId" value="" readonly="readonly"/></dd>
                </li>
                <li>
                    <dt>班级区名称</dt>
                    <dd><input type="text" id="createAreaName" value=""/></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button class="btn btn-primary save_record">保 存</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

    <script type="text/javascript">


        $(function () {
            var isCreate = false;
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
            $("#createArea").on('click', function () {
                var lessonId =${selectedLessonId!};
                $("#createLessonId").val(lessonId);
                isCreate = true;
                $("#create_area_dialog").modal('show');
            });

            //编辑的时候弹出modal时加载数据
            $("a[name='area_edit']").on('click', function () {
                $("#updateAreaId").val($(this).data("area_id"));
                $("#updateLessonId").val($(this).data("lesson_id"));
                $("#updateAreaName").val($(this).data("group_area_name"));
                $("#edit_area_dialog").modal('show');
            });

            //导入数据时，弹窗
            $("#importData").on('click', function () {
                $("#import_dialog").modal('show');
            });


            //保存
            $(".save_record").on('click', function () {
                var areaId = "";
                var lessonId = "";
                var areaName = "";
                if (isCreate) {
                    lessonId = $("#createLessonId").val();
                    areaName = $("#createAreaName").val();
                } else {
                    areaId = $("#updateAreaId").val();
                    lessonId = $("#updateLessonId").val();
                    areaName = $("#updateAreaName").val();
                }
                if (!lessonId || !areaName) {
                    alert("课程Id或区名称不能为空！");
                    return;
                }

                var postData = {
                    areaId: areaId,
                    lessonId: lessonId,
                    areaName: areaName
                };
                console.log(postData);
                $.ajax({
                    url: 'saveGroupArea.vpage',
                    type: 'POST',
                    async: false,
                    data: postData,
                    success: function (data) {
                        if (data.success) {
                            alert("保存成功");
                            $("#create_area_dialog").modal('hide');
                            $("#edit_area_dialog").modal('hide');
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
                if (!selectLessonId) {
                    alert("课程Id不能为空");
                    return;
                }
                location.href = "/opmanager/studyTogether/exportgroupAreaData.vpage?selectLessonId=" + selectLessonId;
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
                        url: 'importGroupAreaData.vpage',
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
        });


        function pagePost(pageNumber) {

            $("#pageNum").val(pageNumber);
            $("#op-query").submit();
        }
    </script>
</@layout_default.page>