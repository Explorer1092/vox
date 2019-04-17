<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<div class="span9">
    <fieldset>
        <legend>课节与模版的关联管理</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get"
          action="">
        <input type="hidden" id="pageNum" name="page" value="${page!'1'}"/>
        <div>
            <span style="white-space: nowrap;">
                课程类型：<select id="type" name="type" style="width: 150px;">
                                <option value="1" <#if type?? && type == 1> selected="selected"</#if>>语文古文</option>
                                <option value="2" <#if type?? && type == 2> selected="selected"</#if>>英语绘本</option>
                                <option value="3" <#if type?? && type == 3> selected="selected"</#if>>语文阅读</option>
                              </select>
            </span>
            <span style="white-space: nowrap;">
                skuId：<select id="skuId" name="skuId">
                <#if lessonIds?? && lessonIds?size gt 0>
                    <#list lessonIds as lessonId>
                        <option value="${lessonId}"
                                <#if (((skuId)!'') == lessonId)>selected="selected"</#if>>${lessonId}</option>
                    </#list>
                <#else>
                    <option value="">暂无数据</option>
                </#if>
            </select>
            </span>
            <button class="btn btn-primary" type="button" id="searchBtn">查询</button>
            <a class="btn btn-success" id="exportRefInfo" name="exportRefInfo">导出课程数据</a>
            <a class="btn btn-danger" id="importData" name="importData">导入课程数据</a>
        </div>
    </form>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>课节ID</th>
                        <th>章节ID</th>
                        <th>skuId</th>
                        <th>模板ID</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if courseLessonList?? && courseLessonList?size gt 0>
                            <#list courseLessonList as  refInfo>
                            <tr>
                                <td>${refInfo.id!''}</td>
                                <td>${refInfo.moduleId!''}</td>
                                <td>${refInfo.skuId!''}</td>
                                <td><#if refInfoByIds??&&refInfoByIds?size gt 0><#if refInfoByIds["${refInfo.id!''}"]?has_content>${refInfoByIds["${refInfo.id!''}"].templateId!''}</#if></#if></td>
                                <td>
                                    <input type="button" name="detail" class="btn btn-primary" value="详情"
                                           data-lesson="${refInfo.id!''}" data-module="${refInfo.moduleId!''}"
                                           data-sku="${refInfo.skuId!''}"
                                           data-template_id="<#if refInfoByIds??&&refInfoByIds?size gt 0><#if refInfoByIds["${refInfo.id!''}"]?has_content>${refInfoByIds["${refInfo.id!''}"].templateId!''}</#if></#if>"
                                           data-type="${type}"/>
                                <#--<a class="btn btn-warning" data-tempate_id="${template.id!''}" name="template_log">日志</a>-->
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
            <#--<ul class="message_page_list">-->
            <#--</ul>-->
            </div>
        </div>
    </div>
</div>
<div id="record_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>修改课节与模版关联信息</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <input id="currentTempLateId" type="hidden"/>
            <input id="currentType" type="hidden"/>
            <ul class="inline">
                <li>
                    <dt>课节Id</dt>
                    <dd><input type="text" readonly="readonly" id="lesson" value=""/></dd>
                </li>
                <li>
                    <dt>章节Id</dt>
                    <dd><input type="text" readonly="readonly" id="module" value=""/></dd>
                </li>
                <li>
                    <dt>skuId</dt>
                    <dd><input type="text" readonly="readonly" id="sku" value=""/></dd>
                </li>
                <li>
                    <dt>模版Id</dt>
                    <dd><select id="templateId" name="templateId">

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
<script type="text/javascript">
    $(function () {
    <#--$(".message_page_list").page({-->
    <#--total: ${total_page!1},-->
    <#--current: ${page!1},-->
    <#--autoBackToTop: false,-->
    <#--maxNumber: 20,-->
    <#--jumpCallBack: function (index) {-->
    <#--$("#pageNum").val(index);-->
    <#--$("#op-query").submit();-->
    <#--}-->
    <#--});-->

        //绘本和古诗的列表地址切换
        $("#type").on("change", function () {
            var type = $("#type").find("option:selected").val();
            // if(type === "1"){
            //     $("#op-query").attr("action","/opmanager/studyTogether/template/classical_chinese_list.vpage");
            // }else if(type === "2"){
            //     $("#op-query").attr("action","/opmanager/studyTogether/template/picture_book_list.vpage");
            // }else {
            //     alert("课程结构分类错误");
            //     return;
            // }
            $.ajax({
                type: "get",
                url: "get_sku_list.vpage",
                data: {
                    type: type
                },
                success: function (data) {
                    var temp_html;
                    if (data.success) {
                        $.each(data.lessonIds, function (i, lesson) {
                            temp_html += "<option value='" + lesson + "'>" + lesson + "</option>";
                        });

                    } else {
                        temp_html = "<option value=''>暂无数据</option>";
                    }
                    $("#skuId").html(temp_html);
                }
            });
        });

        $("input[name='detail']").on('click', function () {
            var currentTemplateId = $(this).data('template_id');
            $("#currentTempLateId").val(currentTemplateId);
            $("#currentType").val($(this).data('type'));
            $("#lesson").val($(this).data('lesson'));
            $("#module").val($(this).data('module'));
            $("#sku").val($(this).data('sku'));
            var type = $(this).data('type');
            $.ajax({
                type: "get",
                url: "get_template_list.vpage",
                data: {
                    type: type
                },
                success: function (data) {
                    var temp_html;
                    if (data.success) {
                        $.each(data.templateIds, function (i, template) {
                            if (template === currentTemplateId) {
                                temp_html += "<option value='" + template + "' selected='selected'>" + template + "</option>";
                            } else {
                                temp_html += "<option value='" + template + "'>" + template + "</option>";
                            }
                        });

                    } else {
                        temp_html = "<option value=''>暂无数据</option>";
                    }
                    $("#templateId").html(temp_html);
                }
            });
            $("#record_dialog").modal('show');
        });

        $("#save_record").on('click', function () {
            var id = $("#lesson").val();
            var type = $("#currentType").val();
            var moduleId = $("#module").val();
            var skuId = $("#sku").val();
            var templateId = $("#templateId").val();
            $.ajax({
                type: "post",
                url: "save_record.vpage",
                data: {
                    id: id,
                    contentType: type,
                    templateId: templateId,
                    moduleId: moduleId,
                    skuId: skuId
                },
                success: function (data) {
                    if (data.success) {
                        alert("保存成功");
                        $("#record_dialog").modal('hide');
                        location.reload();
                    } else {
                        alert("保存失败");
                    }
                }
            });
        });

        $("#searchBtn").on('click', function () {
            // $("#pageNum").val(1);
            $("#op-query").submit();
        });


        //导出数据
        $("#exportRefInfo").on('click', function () {
            var skuId = $("#skuId").val();
            if (!skuId) {
                alert("课程ID不能为空");
                return;
            }
            var type = $("#type").val();
            if (!type || type === 0) {
                alert("课程类型不能为空");
                return;
            }
            location.href = "/opmanager/studyTogether/lessonCourseRef/exportLessonInfo.vpage?skuId=" + skuId + "&type=" + type;
        });

        //导入数据时，弹窗
        $("#importData").on('click', function () {
            $("#import_dialog").modal('show');
        });


        //导入数据
        $("#excelFile").on('change', function () {
            var skuId = $("#skuId").val();
            if (!skuId) {
                alert("未选择skuId");
                return false;
            }
            var $this = $(this);
            var ext = $this.val().split('.').pop().toLowerCase();
            if ($this.val() != '') {
                if ($.inArray(ext, ['xls', 'xlsx']) == -1) {
                    alert("仅支持以下格式【'xls', 'xlsx'】");
                    return false;
                }
                var formData = new FormData();
                formData.append('source_file', $this[0].files[0]);
                formData.append('sku_id', skuId);
                $.ajax({
                    url: 'importData.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            alert("导入成功,共导入：" + data.count + "条数据");
                            $("#excelFile").val("");
                            $("#import_dialog").modal('hide');
                            window.location.reload()
                        } else {
                            alert(data.info);
                            $("#excelFile").val("");
                        }
                    }
                });
            }
        });
    });
</script>
</@layout_default.page>