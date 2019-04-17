<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js" xmlns="http://www.w3.org/1999/html"></script>
<div class="span9">
    <fieldset>
        <legend>用户管理</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get"
          action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <span style="white-space: nowrap;">
                课程ID：<input type="text" id="lessonId" name="lessonId" value="${lessonId!''}"/>
            </span>
            <span style="white-space: nowrap;">
                班级ID：<input type="text" id="clazzId" name="clazzId" value="${clazzId!''}"/>
            </span>
            <span style="white-space: nowrap;">
                学生ID：<input type="text" id="sid" name="sid" value="${sid!''}"/>
            </span>
            <span style="white-space: nowrap;">
                年级：<select id="clazzLevel" name="clazzLevel">
                <option value="" <#if clazzLevel??&&clazzLevel==0>selected</#if>>全部</option>
                <option value="1" <#if clazzLevel??&&clazzLevel==1>selected</#if>>一年级</option>
                <option value="2" <#if clazzLevel??&&clazzLevel==2>selected</#if>>二年级</option>
                <option value="3" <#if clazzLevel??&&clazzLevel==3>selected</#if>>三年级</option>
                <option value="4" <#if clazzLevel??&&clazzLevel==4>selected</#if>>四年级</option>
                <option value="5" <#if clazzLevel??&&clazzLevel==5>selected</#if>>五年级</option>
                <option value="6" <#if clazzLevel??&&clazzLevel==6>selected</#if>>六年级</option>
            </select>
            </span>
        </div>
    </form>
    <button class="btn btn-primary" type="button" id="searchBtn">查询</button>
    <a class="btn btn-warning" id="change_clazz" name="change_clazz">批量修改</a>
    <a class="btn btn-success" id="exportStudentLesson" name="exportStudentLesson">导出学生数据</a>
    <a class="btn btn-primary" type="button" id="batchAddCoinBtn">批量新增学习币</a>
    <a class="btn btn-primary" type="button" id="batchClazzAddCoin">一键班级加币</a>
    <a class="btn btn-primary" type="button" href="coinImportHistory.vpage">加币操作进度</a>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>批量选择 <input type="checkbox" name="selectall"/></th>
                        <th>序号</th>
                        <th>家长Id</th>
                        <th>学生Id</th>
                        <th>学生姓名</th>
                        <th>年级</th>
                        <th>课程Id</th>
                        <th>班级Id</th>
                        <th>微信群名称</th>
                        <th>激活时间</th>
                        <th>学习币总数</th>
                        <th>学分</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as groupInfo>
                            <tr>
                                <td><input type="checkbox" name="submitCheckBox" data-submit_id="${groupInfo.id!''}"
                                           data-sid="${groupInfo.studentId!''}"
                                           data-lesson_id="${groupInfo.studyLessonId!''}"/>
                                </td>
                                <td>${groupInfo_index+1!''}</td>
                                <td>${groupInfo.parentId!''}</td>
                                <td>${groupInfo.studentId!''}</td>
                            <#--<td>${groupInfo.studentName!''}</td>-->
                                <td><#if studentNameMap??&&studentNameMap?size gt 0>${studentNameMap["${groupInfo.studentId!''}"]!}<#else></#if></td>
                                <td>${groupInfo.clazzLevel!''}</td>
                                <td>${groupInfo.studyLessonId!''}</td>
                                <td>${groupInfo.studyGroupId!''}</td>
                                <td><#if clazzMap??&&clazzMap?size gt 0>${clazzMap["${groupInfo.studyGroupId!''}"].wechatGroupName!}<#else></#if></td>
                                <td>${groupInfo.createDate!''}</td>
                                <td><#if coinCountMap?? && coinCountMap?size gt 0><a
                                        href="coinHistory.vpage?studentId=${groupInfo.studentId!''}">${coinCountMap["${groupInfo.studentId!''}"]!}</a></#if>
                                </td>
                                <td>
                                    <#if scoreMap?? && scoreMap?size gt 0><a
                                            href="scoreHistory.vpage?studentId=${groupInfo.studentId!''}&skuId=${groupInfo.studyLessonId!''}">${scoreMap["${(groupInfo.studyLessonId!'') + '_' +(groupInfo.studentId!'')}"]!}</a></#if>
                                </td>
                                <td>
                                    <a class="btn btn-primary" name="edit_detail" data-submit_id="${groupInfo.id!''}"
                                       data-sid="${groupInfo.studentId!''}"
                                       data-lesson_id="${groupInfo.studyLessonId!''}">换班</a>
                                    <button class="btn btn-primary" name="add_coin"
                                            data-lesson_id="${groupInfo.studyLessonId!''}"
                                            data-sid="${groupInfo.studentId!''}">加学习币
                                    </button>
                                    <button class="btn btn-primary" name="add_score" data-sku_id="${groupInfo.studyLessonId!''}" data-sid="${groupInfo.studentId!''}">修改学分</button>
                                </td>
                            </tr>
                            </#list>
                        <#else>
                        <tr>
                            <td colspan="12" style="text-align: center">暂无数据</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
                <div class="message_page_list">
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
                </div>
            </div>
        </div>
    </div>
</div>
<div id="wechat_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>用户管理</h3>
    </div>
    <div class="modal-body">
        <div id="submitReview">
            <span>将要对以下用户执行换班操作</span>
            <table class="table table-bordered" id="resultReview">
                <thead>
                <tr>
                    <th>学生ID</th>
                </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
        <div id="submitLesson">
            课程ID<p id="submitLessonId"></p>
        </div>
        <div id="submitReview">
            请输入班级ID<input type="text" value="" id="submitClazzId"/>
        </div>
        <div class="modal-footer">
            <button id="save_record" class="btn btn-primary">保 存</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>
</div>

<div id="add_coin_modal" class="modal hide fade" style="width: 480px">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>加学习币</h3>
    </div>
    <div class="modal-body" style="height: 240px">
        <div style="width: 80%;margin:0 auto;">
            <form id="add-coin-frm" action="save.vpage" method="post" role="form">
                <table>
                    <tr>
                        <td align="right">学生ID：</td>
                        <td><span id="coinSid"></span></td>
                    </tr>
                    <tr>
                        <td align="right">课程ID：</td>
                        <td><span id="coinLessonId"></span></td>
                    </tr>
                    <tr>
                        <td align="right">学习币类型：</td>
                        <td>
                            <select class="form-control" id="coinType">
                                <option value="0_0">请选择类型</option>
                                <#if coinTypeMap?has_content>
                                    <#list coinTypeMap?keys as key>
                                <option value="${key}_<#if typeCountMap?has_content>${typeCountMap[key]}</#if>">${coinTypeMap[key]}</option>
                                    </#list>
                                </#if>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td align="right">学习币数量：</td>
                        <td><span id="coinCount"></span></td>
                    </tr>
                </table>
            </form>
            <div>
                <div style="float: left">
                    <button class="btn" data-dismiss="modal" aria-hidden="true">返 回</button>
                </div>
                <div style="float: right">
                    <button id="save_coin" class="btn btn-primary">添 加</button>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="clazz_add_coin_modal" class="modal hide fade" style="width: 480px">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>加学习币</h3>
    </div>
    <div class="modal-body" style="height: 240px">
        <div style="width: 80%;margin:0 auto;">
            <form id="clazz-add-coin-frm" action="save.vpage" method="post" role="form">
                <table>
                    <tr>
                        <td align="right">班级ID：</td>
                        <td><span id="clazzAddClazzId"></span></td>
                    </tr>
                    <tr>
                        <td align="right">课程ID：</td>
                        <td><span id="clazzAddLessonId"></span></td>
                    </tr>
                    <tr>
                        <td align="right">学习币类型：</td>
                        <td>
                            <select class="form-control" id="clazzAddCoinType">
                                <option value="0_0">请选择类型</option>
                                <#if coinTypeMap?has_content>
                                    <#list coinTypeMap?keys as key>
                                    <option value="${key}_<#if typeCountMap?has_content>${typeCountMap[key]}</#if>">${coinTypeMap[key]}</option>
                                    </#list>
                                </#if>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td align="right">学习币数量：</td>
                        <td><span id="clazzAddCoinCount"></span></td>
                    </tr>
                </table>
            </form>
            <div>
                <div style="float: left">
                    <button class="btn" data-dismiss="modal" aria-hidden="true">返 回</button>
                </div>
                <div style="float: right">
                    <button id="clazz_save_coin" class="btn btn-primary">添 加</button>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="batch_add_coin_modal" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>批量新增学习币</h3>
    </div>
    <div class="modal-body" style="overflow: auto;height: 240px;">
        <form id="batch-add-coin-frm" enctype="multipart/form-data" action="" method="post">
            <div style="height: 40px;">
                <input type="file" name="coin_file" id="coin_file" value="选择文档"/>
            </div>
            <div style="height: 40px" id="templateDiv">
                <span style="color: red" id="templateFile">导入excel文档，无表头</span>
                <span id="link"></span>
            </div>
            <div style="height: 40px;">
                <span style="color: red">注：上传成功，学习币立即生效，请在加币操作进度内查看操作结果</span>
            </div>
        </form>
        <div style="height: 40px;">
            <button class="btn" data-dismiss="modal" aria-hidden="true">返 回</button>
            <button id="batchAddCoin" class="btn btn-primary">确 定</button>
        </div>
    </div>
</div>

<#-- 学分 -->
<div id="add_score_modal" class="modal hide fade" style="width: 480px">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>修改学分</h3>
    </div>
    <div class="modal-body" style="height: 300px">
        <div style="width: 90%;margin:0 auto;">
            <form id="add-score-frm" action="saveScore.vpage" method="post" role="form">
                <ul class="inline">
                    <li>学生ID:<input type="text" id="scoreSid" value="" disabled/></li>
                </ul>
                <ul class="inline">
                    <li>课程ID:<input type="text" id="scoreSkuId" value="" disabled/></li>
                </ul>
                <ul class="inline">
                    <li>课节ID:<input type="text" id="scoreLessonId" placeholder="数字填写" value=""/></li>
                </ul>
                <ul class="inline">
                    <li>操作类型:<input type="text" id="operatorType" value="人工操作" disabled/></li>
                </ul>
                <ul class="inline">
                    <li>新增学分数量:<input type="number" placeholder="数字填写" id="scoreCount" value=""/></li>
                </ul>
            </form>
            <div>
                <div style="float: left">
                    <button class="btn" data-dismiss="modal" aria-hidden="true">返 回</button>
                </div>
                <div style="float: right">
                    <button id="save_score" class="btn btn-primary">添 加</button>
                </div>
            </div>
        </div>
    </div>
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

        $("#searchBtn").on('click', function () {
            $("#pageNum").val(1);
            $("#op-query").submit();
        });
        var submitIds = [];
        var selectSid = [];
        var lessonId = "";
        //批量修改直接弹出modal
        $("#change_clazz").on('click', function () {
            submitIds = [];
            selectSid = [];
            lessonId = "";
            $("#submitClazzId").val("");
            var $table = $("#resultReview");
            var $tbody = $table.find("tbody");
            $tbody.empty();
            $("input[name='submitCheckBox']:checked").each(function () {
                $tbody.append($('<tr/>')
                        .append($('<td/>').html($(this).data('sid')))
                );
                if (!lessonId) {
                    lessonId = $(this).data('lesson_id');
                }
                if ($(this).data('submit_id')) {
                    submitIds.push($(this).data('submit_id'));
                }
                if ($(this).data('sid')) {
                    selectSid.push($(this).data('sid'));
                }

            });
            $("#submitLessonId").text(lessonId);
            console.info(submitIds);
            $("#wechat_dialog").modal('show');
        });
        //编辑的时候弹出modal
        $("a[name='edit_detail']").on('click', function () {
            submitIds = [];
            selectSid = [];
            lessonId = "";
            $("#submitClazzId").val("");
            var opId = $(this).data("submit_id");
            var sid = $(this).data("sid");
            lessonId = $(this).data("lesson_id");
            if (!opId) {
                console.log("opId null");
                return;
            }
            var $table = $("#resultReview");
            $table.find("tbody").empty();
            $table.append($('<tr/>')
                    .append($('<td/>').html($(this).data('sid')))
            );
            if (opId) {
                submitIds.push(opId);
            }
            if (sid) {
                selectSid.push(sid);
            }
            $("#submitLessonId").text(lessonId);
            console.info(opId + "=====" + sid);
            $("#wechat_dialog").modal('show');
        });
        //全选
        $('input[name="selectall"]').click(function () {
            //alert(this.checked);
            if ($(this).is(':checked')) {
                $('input[name="submitCheckBox"]').each(function () {
                    //此处如果用attr，会出现第三次失效的情况
                    $(this).prop("checked", true);
                });
            } else {
                $('input[name="submitCheckBox"]').each(function () {
                    $(this).removeAttr("checked", false);
                });
                //$(this).removeAttr("checked");
            }

        });
        //保存
        $("#save_record").on('click', function () {
            if (selectSid.length == 0 || submitIds.length == 0) {
                alert("id不能为空");
                return;
            }
            var clazzId = $("#submitClazzId").val();
            if (!clazzId) {
                alert("班级Id不能为空");
                return;
            }
            if (!lessonId) {
                alert("课程Id不能为空");
                return;
            }
            savePost(submitIds, clazzId, lessonId);
        });

        //学习币相关 start=======================
        //单个学生加学习币
        $("button[name=add_coin]").on('click', function () {
            $("#coinSid").text($(this).data('sid'));
            $("#coinLessonId").text($(this).data('lesson_id'));
            $("#add_coin_modal").modal("show");
            var value = $("#coinType").val();
            var values = value.split("_");
            $("#coinCount").text(values[1]);
        });
        $("#coinType").on('click', function () {
            var value = $(this).val();
            var values = value.split("_");
            $("#coinCount").text(values[1]);
        });
        $("#save_coin").click(function () {
            if (confirm("点击添加，学习币立即生效，\n是否确定添加")) {
                var frm = $("form#add-coin-frm");
                frm.submit();
            }
        });
        $("form#add-coin-frm").on('submit', function (e) {
            e.preventDefault();
            $.post("addCoin.vpage", {
                "studentId": $("#coinSid").text(),
                "coinType": $("#coinType").val().split("_")[0]
            }, function (data) {
                if (data.success) {
                    $("#add-coin-frm").modal("hide");
                    alert("加学习币成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });

        //一键班级加币
        $("#batchClazzAddCoin").on('click', function () {
            var clazzId = $("#clazzId").val();
            var lessonId = $("#lessonId").val();
            if (clazzId == '' || lessonId == '') {
                alert("请先在查询框内选择班级ID和课程ID，选择之后才可进行加币")
                return;
            }
            $("#clazzAddClazzId").text(clazzId);
            $("#clazzAddLessonId").text(lessonId);
            var value = $("#clazzAddCoinType").val();
            var values = value.split("_");
            $("#clazzAddCoinCount").text(values[1]);
            $("#clazz_add_coin_modal").modal('show');
        });
        $("#clazzAddCoinType").on('click', function () {
            var value = $(this).val();
            var values = value.split("_");
            $("#clazzAddCoinCount").text(values[1]);
        });
        $("#clazz_save_coin").on('click', function () {
            if (confirm("点击添加，学习币立即生效，是否确定添加\n\n注：添加结果可在“加币操作进度”内查看")) {
                var frm = $("form#clazz-add-coin-frm");
                frm.submit();
            }
        });
        $("form#clazz-add-coin-frm").on('submit', function (e) {
            e.preventDefault();
            $.post("clazzAddCoin.vpage", {
                "clazzId": $("#clazzAddClazzId").text(),
                "lessonId": $("#clazzAddLessonId").text(),
                "clazzCoinType": $("#clazzAddCoinType").val().split("_")[0]
            }, function (data) {
                $("#clazz_add_coin_modal").modal('hide');
                if (data.success) {
                    alert("一键班级加币成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            })
        });

        //批量加币
        $("#batchAddCoinBtn").on('click', function () {
            $("#link").html("");
            $.get("templateExcel.vpage", {}, function (data) {
                if (data.success) {
                    $("#link").html("<a href='" + data.templateUrl + "' id='fileUrl'>下载模板</a>");
                }
            });
            $("#batch_add_coin_modal").modal('show');
        });

        $("#batchAddCoin").on('click', function () {
            if (confirm("点击添加，学习币立即生效，是否确定添加\n\n注：添加结果可在“加币操作进度”内查看")) {
                var frm = $("form#batch-add-coin-frm");
                frm.submit();
            }
        });
        $("form#batch-add-coin-frm").on('submit', function (e) {
            e.preventDefault();
            var formData = new FormData($("#batch-add-coin-frm")[0]);
            $.ajax({
                url: "batchAddCoin.vpage",
                type: "POST",
                data: formData,
                processData: false,
                contentType: false,
                async: false,
                success: function (data) {
                    alert(data.info);
                    window.location.reload();
                }
            });
        });
        //学习币相关 end=========================

        //导出数据
        $("#exportStudentLesson").on('click', function () {
            var lessonId = $("#lessonId").val();
            var clazzId = $("#clazzId").val();
            var sid = $("#sid").val();
            var clazzLevel = $("#clazzLevel").val();
            if (clazzLevel && (!lessonId && !clazzId && !sid)) {
                alert("年级数据必须与课程、班级或学生Id配合使用");
                return;
            }
            location.href = "/opmanager/studyTogether/exportStudentLessonData.vpage?sid=" + sid + "&lessonId=" + lessonId + "&clazzId=" + clazzId + "&clazzLevel=" + clazzLevel;
        });

        //修改学分
        $("button[name=add_score]").on('click', function () {
            $("#scoreSid").val($(this).data('sid'));
            $("#scoreSkuId").val($(this).data('sku_id'));
            $("#add_score_modal").modal("show")

        });
        $("#save_score").click(function () {
            if (confirm("点击添加，立即生效，\n是否确定添加")) {
                var frm = $("form#add-score-frm");
                frm.submit();
            }
        });
        $("form#add-score-frm").on('submit', function (e) {
            e.preventDefault();
            $.post("addScore.vpage", {
                "studentId": $("#scoreSid").val(),
                "skuId": $("#scoreSkuId").val(),
                "lessonId": $("#scoreLessonId").val(),
                "operatorType": $("#operatorType").val(),
                "scoreCount": $("#scoreCount").val()
            }, function (data) {
                if (data.success) {
                    $("#add-score-frm").modal("hide");
                    alert("修改学分成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });
    });

    function savePost(opIds, clazzId, lessonId) {
        var ids = opIds.toString().trim();
        $.ajax({
            url: 'upsertStudyGroup.vpage',
            type: 'POST',
            async: false,
            data: {"ids": ids, "clazzId": clazzId, "lessonId": lessonId},
            success: function (data) {
                if (data.success) {
                    alert("保存成功");
                    $("#wechat_dialog").modal('hide');
                    window.location.reload();
                } else {
                    alert(data.info);
                    console.log("data error");
                }
            }
        });
    }
</script>
</@layout_default.page>