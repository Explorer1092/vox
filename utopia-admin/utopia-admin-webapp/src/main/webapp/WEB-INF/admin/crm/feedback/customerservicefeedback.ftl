<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="查询反馈" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jppaginator/jqPaginator.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jquery-swfupload/swfupload.2.5.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jquery-swfupload/handlers.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jquery-swfupload/swfupload.queue.js"></script>
<style>
    .list_style{
        float:left;
        text-decoration:none;
        list-style: none;
        width:90px;
        text-align:center;
    }
</style>
<div class="span11">
    <div>反馈记录<input type="button" id="save_new_feedback" value="新建反馈" style="position:absolute;right:5%"></div>
    <hr>
    <ul class="inline">
        <li>
            <label>
                开始日期：
                <input id="startDate" name="startDate" value="${statDate?string("yyyy-MM-dd")}" type="text"/>
            </label>
        </li>
        <li>
            <label>
                结束日期：
                <input id="endDate" name="endDate" value="${endDate?string("yyyy-MM-dd")}" type="text"/>
            </label>
        </li>
        <li>
            <label>
                学科：
                <select id="subject" name="subject">
                    <option value="0"></option>
                    <#if subject?has_content>
                        <#list subject as s>
                            <option value="${s.id!'0'}">${s.desc!'-'}</option>
                        </#list>
                    </#if>
                </select>
            </label>
        </li>
        <li>
            <label>
                反馈类型：
                <select id="feedbackType" name="type" onchange="changeFeedbackType()">
                    <option value="0"></option>
                    <#if type?has_content>
                        <#list type as t>
                            <option value="${t.type!'0'}">${t.desc!'-'}</option>
                        </#list>
                    </#if>
                </select>
            </label>
        </li>
        <li>
            <label>
                状态：
                <select id="feedbackStatus" name="status">
                    <option value="0"></option>
                    <#if status?has_content>
                        <#list status as s>
                            <option value="${s.id!'0'}">${s.desc!'-'}</option>
                        </#list>
                    </#if>
                </select>
            </label>
        </li>
        <li>
            <label>
                是否上线：
                <select id="online" name="online">
                    <option value="0"></option>
                    <option value="1">已上线</option>
                    <option value="2">未上线</option>
                </select>
            </label>
        </li>
        <li>
            <label>
                建议/需求：
                <input value="" id="content" name="content" type="text"/>
            </label>
        </li>
        <li>
            <label>
                反馈人:
                <input value="" id="feedbackPeople" name="feedbackPeople" type="text"/>
            </label>
        </li>
        <li>
            <label>
                反馈老师:
                <input value="" id="teacher" name="teacher" type="text"/>
            </label>
        </li>
        <li>
            <label>
                反馈编号:
                <input value="" id="feedbackId" name="id" type="text"/>
            </label>
        </li>
        <li>
            <button type="button" class="submit_but">查询</button>
        </li>
        <li>
            共有<span class="listSize">0</span>条反馈
        </li>
    </ul>
    <div>
        <table class="table table-bordered">
            <tr>
                <th>编号</th>
                <th>反馈日期</th>
                <th>学科</th>
                <th>反馈类型</th>
                <th>建议/需求</th>
                <th>反馈老师</th>
                <th>状态</th>
                <th>三级分类</th>
                <th>指定PM</th>
                <th>预计上线时间</th>
                <th>是否上线</th>
                <th>操作</th>
                <th>是否需要回电</th>
                <th>图片</th>
            </tr>
            <tbody id="fb_table_body">
            </tbody>
        </table>
    </div>
    <div id="list_foot"></div>
</div>

<div id="sure-feedback-online" title="编辑反馈" style="font-size: small;display: none">
    <div id="clue-edit"></div>
    <div style="text-align:center">
        <input id="sure_online_but" type="button" onclick="saveFeedback()" value="提交"/>
        <input id="close_online_but" type="button" onclick="closeDialog('sure-feedback-online')" value="取消"/>
    </div>
</div>

<div id="uploadphotoBox" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" onclick="location.reload();" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>上传图片</h3>
    </div>
    <div class="modal-body">
        <div id="content" class="startUploadBox">
            <form>
                <div>
                    <span id="spanButtonPlaceholder"></span>
                </div>
            </form>
            <div id="divFileProgressContainer"></div>
            <div id="thumbnails"></div>
        </div>

        <div class="endUploadBox"  style="display: none;">
            <p class="b_right">
                <span class="text-success">图片上传成功</span>
            </p>
        </div>
    </div>
    <div class="text-center alert-block">
        最多上传5张图片
    </div>
</div>

<script type="text/html" id="product">
    <fieldset>
        <legend>编辑反馈</legend>
    </fieldset>
    <div class="form-horizontal">
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div class="control-group">
                <label class="col-sm-2 control-label"><strong>类别:</strong></label>
                <div class="controls" style="margin-top:5px">
                    <select id="selectPartner">
                        <option value="0" data-name="0" data-type="0">请选择</option>
                        <%if(type){%>
                        <%for(var i = 0; i < type.length; ++i){%>
                        <option value="1" data-name="1" data-type="<%=type[i].type%>"><%=type[i].desc%></option>
                        <%}%>
                        <%}%>
                    </select>
                </div>
            </div>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div class="control-group">
                <label class="col-sm-2 control-label"><strong>回电号码:</strong></label>
                <div class="controls">
                    <input id="feedbackTeacher" name="mobile" data-einfo="请填写回电号码" type="text" class="mobile input-middle js-postData"/>
                </div>
            </div>
        </div>
        <div class="modal-body teacherNone" style="height: auto; overflow: visible;display:none">
            <div class="control-group">
                <label class="col-sm-2 control-label"><strong>姓名:</strong></label>
                <div class="controls">
                    <input id="" readonly="true" name="teacherName" data-einfo="请填写姓名" type="text" class="teacherName input-middle"/>
                </div>
            </div>
        </div>
        <div class="modal-body teacherNone" style="height: auto; overflow: visible;display:none">
            <div class="control-group">
                <label class="col-sm-2 control-label"><strong>学科:</strong></label>
                <div class="controls">
                    <input id="" readonly="true" name="subjectType" data-einfo="请填写学科" type="text" class="subjectType input-middle"/>
                </div>
            </div>
        </div>
        <div class="modal-body teacherNone" style="height: auto; overflow: visible;display:none">
            <div class="control-group">
                <label class="col-sm-2 control-label"><strong>学校:</strong></label>
                <div class="controls">
                    <input id="" readonly="true" name="schoolName" data-einfo="请填写学校" type="text" class="schoolName input-middle"/>
                </div>
            </div>
        </div>
        <div class="modal-body" id="bookName"  style="height: auto; overflow: visible;display:none">
            <div class="control-group">
                <label class="col-sm-2 control-label"><strong>教材名称:</strong></label>
                <div class="controls">
                    <input id="" name="bookName" data-einfo="请填写教材名称" type="text" class="bookName input-middle js-postData"/>
                </div>
            </div>
        </div>
        <div class="modal-body" id="bookUnit" style="height: auto; overflow: visible;display:none">
            <div class="control-group">
                <label class="col-sm-2 control-label"><strong>年级:</strong></label>
                <div class="controls">
                    <input id="" name="bookGrade" data-einfo="请填写年级" type="text" class="bookGrade input-middle js-postData"/>
                </div>
            </div>
        </div>
        <div class="modal-body unit" style="height: auto; overflow: visible;display:none">
            <div class="control-group">
                <label class="col-sm-2 control-label"><strong>单元:</strong></label>
                <div class="controls">
                    <input id="" name="bookUnit" data-einfo="请填写单元" type="text" class="bookUnit input-middle js-postData"/>
                </div>
            </div>
        </div>
        <div class="modal-body count" style="height: auto; overflow: visible;display:none">
            <div class="control-group">
                <label class="col-sm-2 control-label"><strong>覆盖地区:</strong></label>
                <div class="controls">
                    <input id="" name="bookCoveredArea" data-einfo="请填写覆盖地区" type="text" class="bookCoveredArea input-middle js-postData"/>
                </div>
            </div>
        </div>
        <div class="modal-body count" style="height: auto; overflow: visible;display:none">
            <div class="control-group">
                <label class="col-sm-2 control-label"><strong>覆盖学生数:</strong></label>
                <div class="controls">
                    <input id="" name="stuCount" data-einfo="请填写覆盖学生数" type="text" class="stuCount input-middle js-postData"/>
                </div>
            </div>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div class="control-group">
                <label class="col-sm-2 control-label"><strong>建议/需求:</strong></label>
                <div class="controls">
                    <textarea name="content" style="width:500px;" class="content input-middle js-postData" id="" cols="40" rows="13" maxlength="1000" placeholder="最多输入1000字" data-einfo="请填写反馈内容"></textarea>
                </div>
            </div>
        </div>

        <div class="modal-body" style="height: auto; overflow: visible;">
            <div class="control-group">
                <label class="col-sm-2 control-label"><strong>是否需要回电:</strong></label>
                <div class="controls">
                    <select id="callback" name="callback">
                        <#--<option value="0"></option>-->
                            <option value="2" data-type=false>否</option>
                            <option value="1" data-type=true >是</option>
                    </select>
                </div>
            </div>
        </div>

        <div class="modal-body" style="height: auto; overflow: visible;">
            <div class="control-group">
                <label class="col-sm-2 control-label"><strong>图片附件:</strong></label>
                <div class="controls">
                    <a id="uploadPhotoButton" href="javascript:void (0);" role="button" class="btn btn-success">上传照片</a>
                    <span style="color: #F00;" id="uploadPhotoTip"></span>
                    <input type="file" id="uploadPhotoInput" accept="image/jpeg, image/jpg,image/png" multiple="multiple"  style="display: none;">
                </div>
            </div>
        </div>
    </div>
</script>
<script id="feedbackListTemp" type="text/html">
    <%if (list){%>
    <%for(var i = 0; i < list.length && i < 50; ++i){%>
    <tr>
        <td><%=list[i].id%></td>
        <td><%=list[i].feedbackDate%></td>
        <td><%=list[i].subject%></td>
        <td><%=list[i].type%></td>
        <td><%=list[i].content%></td>
        <td><a href="/crm/teachernew/teacherdetail.vpage?teacherId=<%=list[i].teacherId%>"><%=list[i].teacherName%>(<%=list[i].teacherTelephone%>)</a></td>
        <td><%=list[i].status%></td>
        <td><%=list[i].threeCategory%></td>
        <td><%=list[i].pmData%></td>
        <td><%=list[i].onlineData%></td>
        <td><%if(list[i].online){%>已上线<%}else{%>未上线<%}%></td>
        <td>
            <a target="_blank"
               href="/audit/apply/apply_detail.vpage?applyType=AGENT_PRODUCT_FEEDBACK&workflowId=<%=list[i].workflowId%>">查看</a>
        </td>
        alert(list[i].callback);
        <td><%if(list[i].callback){%>是<%}else{%>否<%}%></td>
        <td>
            <%if(list[i].pic1Url){%><img src="${prePath}/gridfs/<%=list[i].pic1Url%>" width="120" height="120"/><%}%>
            <%if(list[i].pic2Url){%><img src="${prePath}/gridfs/<%=list[i].pic2Url%>" width="120" height="120"/><%}%>
            <%if(list[i].pic3Url){%><img src="${prePath}/gridfs/<%=list[i].pic3Url%>" width="120" height="120"/><%}%>
            <%if(list[i].pic4Url){%><img src="${prePath}/gridfs/<%=list[i].pic4Url%>" width="120" height="120"/><%}%>
            <%if(list[i].pic5Url){%><img src="${prePath}/gridfs/<%=list[i].pic5Url%>" width="120" height="120"/><%}%>
        </td>
    </tr>
    <%}%>
    <%}%>
</script>

<script type="text/javascript">
    var pageSize = ${pageSize!0};
    var page = 1;
    function initPagInator(totalCounts) {
        $("#list_foot").jqPaginator({
            totalCounts: totalCounts,
            totalPages: totalCounts / pageSize,
            pageSize: pageSize,
            currentPage: 1,

            first: '<li class="first list_style"><a href="javascript:void(0);">第一页</a></li>',
            prev: '<li class="prev list_style"><a href="javascript:void(0);">上一页</a></li>',
            next: '<li class="next list_style"><a href="javascript:void(0);">下一页</a></li>',
            last: '<li class="last list_style"><a href="javascript:void(0);">最后一页</a></li>',
            page: '<li class="page list_style"><a href="javascript:void(0);">{{page}}</a></li>',
            onPageChange: function (num) {
                page = num;
                findCSFeedbackList2(page);
            }
        });
    }
    function findCSFeedbackList2(page) {
        var data = getCondition();
        data["page"] = page;
        $.post("/crm/cs_productfeedback/feedback_list.vpage", data, function (data) {
            if (!data.success) {
                alert(data.info);
            } else {
                $("#fb_table_body").html(template("feedbackListTemp", {list: data.dataList}));
            }
        });
    }

    var submitAble = true;
    var postData = {};

    $(document).on('change','#selectPartner',function(){
        showTab($("#selectPartner option:selected").data('type'));
        console.log($("#selectPartner option:selected").data('type'));
    });
    function showTab(selected) {
        switch (selected) {
            case 7 :
                $('.unit').hide().find('.input-middle').removeClass('js-postData');
                $('.count').show().find('.input-middle').addClass('js-postData');
                $('#bookName').show().find('.input-middle').addClass('js-postData');
                $('#bookUnit').show().find('.input-middle').addClass('js-postData');
                break;
            case 6 :
                $('.unit').show().show().find('.input-middle').addClass('js-postData');
                $('.count').hide().find('.input-middle').removeClass('js-postData');
                $('#bookName').show().find('.input-middle').addClass('js-postData');
                $('#bookUnit').show().find('.input-middle').addClass('js-postData');
                break;
            case 0:
                $('.unit').hide().find('.input-middle').removeClass('js-postData');
                $('.count').hide().find('.input-middle').removeClass('js-postData');
                $('#bookName').hide().find('.input-middle').removeClass('js-postData');
                $('#bookUnit').hide().find('.input-middle').removeClass('js-postData');
                break;
            default:
                $('.unit').hide().find('.input-middle').removeClass('js-postData');
                $('.count').hide().find('.input-middle').removeClass('js-postData');
                $('#bookName').hide().find('.input-middle').removeClass('js-postData');
                $('#bookUnit').hide().find('.input-middle').removeClass('js-postData');
                break;
        }
    }
    $(document).on('blur','#feedbackTeacher',function(){
        var feedbackTeacher = $(this).val();
        $.post('search_teacher.vpage',{mobile:feedbackTeacher},function(res){
            if(res.success){
                if(res.length != 0){
                    $('.teacherNone').show();
                    $('.teacherNone .teacherName').val(res.teacherName);
                    $('.teacherNone .subjectType').val(res.subject);
                    $('.teacherNone .schoolName').val(res.schoolName);
                }else{
                    $('.teacherNone').hide();
                }
            }else{
                $('.teacherNone').hide();
//                alert(res.info);
            }
        })
    });
    $('#save_new_feedback').on('click',function(){
        $('#clue-edit').show();
        $.get('/crm/cs_productfeedback/load_feedback_page.vpage',function(data){
            if (!data.success) {
                alert(data.info);
            } else {
                $("#clue-edit").html(template("product", data));
                $("#sure-feedback-online").dialog({
                    height: "auto",
                    width: "950",
                    autoOpen: true
                })
            }
        })
    });
    $(function () {
        findCSFeedbackList3(1);
        $("#startDate").datepicker({
            dateFormat: 'yy-mm-dd',  //日期格式，自己设置
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false
        });

        $("#endDate").datepicker({
            dateFormat: 'yy-mm-dd',  //日期格式，自己设置
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false
        });
    });

    function getCondition() {
        return {
            startDate: $("#startDate").val(),
            endDate: $("#endDate").val(),
            subject: $("#subject").val(),
            type: $("#feedbackType").val(),
            status: $("#feedbackStatus").val(),
            online: $("#online").val(),
            content: $("#content").val(),
            feedbackPeople: $("#feedbackPeople").val(),
            teacher: $("#teacher").val(),
            callback: $("#callback").val(),
            file: $("#file").val(),
            id: $("#feedbackId").val()
        }
    }

    $(document).on("click", ".submit_but", function () {
        findCSFeedbackList3(1);
    });

    function findCSFeedbackList3(page) {
        var data = getCondition();
        data["page"] = page;
        $.post("/crm/cs_productfeedback/feedback_list.vpage", data, function (data) {
            if (data.success) {
                if(data.dataList.length>0){
                    $("#fb_table_body").html(template("feedbackListTemp", {list: data.dataList}));
                    $('.listSize').html(data.size);
                }else{
                    $("#fb_table_body").html('');
                    $('#list_foot').html('');
                    $('.listSize').html('0');
                    alert('暂无反馈数据')
                }
            } else {
                alert(data.info);
            }
        });
    }

    var checkData = function () {
        var flag = true;
        if(flag){
            if($("#selectPartner option:selected").val() == '0'){
                alert('请选择类别');
                return false;
            }
        }
        $.each($(".js-postData"), function (i, item) {
            postData[item.name] = $(item).val();
            if (!($(item).val())) {
                alert($(item).data("einfo"));
                flag = false;
                return false;
            }
        });


        if (flag) {
            if ($('[name="isAgencyClue"]').hasClass("disabled")) {
                $('[name="isAgencyClue"]').removeClass("js-ulItem");
            }
            $.each($(".js-ulItem"), function (i, item) {
                if ($(item).children("div.the").length == 0) {
                    alert($(item).data("einfo"));
                    flag = false;
                    return false;
                } else {
                    postData[$(item).attr("name")] = $(item).children("div.the").attr("data-opvalue");
                }
            });
        }
        return flag;
    };
    function saveFeedback(){
        if(submitAble){
            if(checkData()){
                submitAble = false;
                var postData = new FormData();
                $.each($(".js-postData"),function(i,item){
                    postData[item.name] = $(item).val();
                });
                //类别
//                postData["type"] = $("#selectPartner option:selected").attr('data-type');
                postData.append('type', $("#selectPartner option:selected").attr('data-type'));
                postData.append('content', $(".content").val());
                //需求建议
//                postData["content"] = $(".content").val();
                postData.append('mobile', $(".mobile").val());
//                postData.append('teacherName', $(".teacherName").val());
//                postData.append('subjectType', $(".subjectType").val());
//                postData.append('schoolName', $(".schoolName").val());
                //年级
//                postData["bookGrade"]=$(".bookGrade").val();
                postData.append('bookGrade', $(".bookGrade").val());
                //覆盖区域
//                postData["bookCoveredArea"]=$(".bookCoveredArea").val();
                postData.append('bookCoveredArea', $(".bookCoveredArea").val());
                //覆盖学生人数
//                postData["stuCount"]=parseInt($(".stuCount").val());
                postData.append('stuCount', parseInt($(".stuCount").val()));
                //教材名称
//                postData["bookName"]=$(".bookName").val();
                postData.append('bookName', $(".bookName").val());
                //单元
//                postData["bookUnit"]=$(".bookUnit").val();
                postData.append('bookUnit', $(".bookUnit").val());
                postData.append('callback',  $("#callback option:selected").attr('data-type'));
                // 上傳圖片
                var files = $('#uploadPhotoInput')[0].files;
                for (var i = 0; i < files.length; i++) {
//                    postData["file"] = files[i]; // 对于多文件，append多次，key一致
                    postData.append('file', files[i]);
                }

                $.ajax({
                    url: 'save_new_feedback.vpage',
                    type: 'POST',
                    cache: false,
                    data: postData,
                    timeout: 0,
                    processData: false,
                    contentType: false
                }).done(function(res) {
                    if (!res.success) {
                        alert(res.info);
                        return;
                    }
                    alert("提交成功");
                    closeDialog('sure-feedback-online');
                    findCSFeedbackList2(page)
                }).fail(function(res) {
                    alert("字数过多，请修改内容");
                });
            }
        }else{
            alert('记录已被提交正在处理,请稍候...');
            submitAble = true;
        }
    }

    //  新建反馈上传图片
    $(document).on('click', '#uploadPhotoButton', function () {
        $('#uploadPhotoInput').click();
    });
    $(document).on('change', '#uploadPhotoInput', function () {
        var files = $('#uploadPhotoInput')[0].files;
        if (!files.length) {
            $('#uploadPhotoTip').text('您未选择图片哦~~');
            return ;
        }
        if (files.length > 5) {
            $('#uploadPhotoTip').text('仅支持上传5张图片哦~');
            return ;
        }
        // 查询是否有非图片
        var hasImageTypeError = false;
        for (var i = 0; i < files.length; i++) {
            if (['image/jpeg', 'image/jpg','image/png'].indexOf(files[i].type) === -1) {
                hasImageTypeError = true;
            }
        }
        if (hasImageTypeError) {
            $('#uploadPhotoTip').text('只能上传图片哦~');
            return;
        }
        $('#uploadPhotoTip').text('您已成功选择' +  files.length + '张图片');
    });
</script>
</@layout_default.page>