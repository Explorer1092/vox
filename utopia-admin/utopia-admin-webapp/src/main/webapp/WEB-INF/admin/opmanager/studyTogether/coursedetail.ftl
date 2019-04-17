<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑点评课" page_num=9 jqueryVersion ="1.7.2">
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>

<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加/编辑点评课
        <a type="button" id="btn_cancel" href="commentcourse.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
        <input type="button" id="save_ad_btn" class="btn btn-primary" value="保存点评课"/>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="courseForm" name="detail_form" enctype="multipart/form-data" action="save.vpage" method="post">
                    <input id="courseId" name="courseId" value="${courseId!}" type="hidden" class="js-postData">
                    <div class="form-horizontal">
                    <#-- 点评课ID-->
                        <#if courseId?has_content>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">ID</label>
                            <div class="controls">
                                <input type="text" id="courseId" name="courseId" class="form-control" value="${courseId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        </#if>

                    <#-- 古诗课程ID -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">古诗课程ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="lessonId" name="lessonId" style="width: 350px;" class="js-postData">
                                    <option value="">--请选择古诗课程--</option>
                                    <#if lessonIds??>
                                        <#list lessonIds as lid>
                                            <option <#if course?? && course.lessonId??><#if course.lessonId == lid> selected="selected"</#if></#if> value = ${lid!}>${lid!}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>

                    <#-- 小课次课程ID -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">小课次课程名称 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="innerId" name="innerId" style="width: 350px;" class="js-postData">
                                    <option value="">--请选择小课次课程ID--</option>
                                    <#if innerIds??>
                                        <#list innerIds as courseLesson>
                                            <option <#if course?? && course.innerId??><#if course.innerId == courseLesson.id> selected="selected"</#if></#if> value = ${courseLesson.id!}>${courseLesson.lessonName!}  ${courseLesson.id!}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>

                    <#-- 课题名称 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课题名称 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="title" name="title" class="form-control js-postData" type="text" value="<#if course??>${course.title!''}</#if>"
                                       style="width: 336px;" maxlength="30" placeholder="请在这里输入课题名称，长度不超过6个汉字"/>
                                <span style="font-size: 10px;color: red">必填</span>
                            </div>
                        </div>

                    <#-- 课程提示 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程提示 <span style="color: red">*</span></label>
                            <div class="controls">
                                <textarea id="reminder" name="reminder" class="form-control js-postData"
                                          style="width:336px;" placeholder="请在这里输入课程提示，长度不超过60个汉字">${(course.reminder)!}</textarea>
                            </div>
                        </div>

                    <#-- 商品ID -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">商品ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="productId" name="productId" class="form-control js-postData" value="<#if course??>${course.productId!''}</#if>" style="width: 336px;"/>
                                <span style="font-size: 10px;color:red">不配置表示该商品免费</span>
                            </div>
                        </div>

                    <#-- 开课日期 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">开课日期  <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="openDate" name="openDate" class="form-control js-postData" value="<#if course??>${course.openDate!''}</#if>" style="width: 336px;" />
                                <span style="font-size: 10px;color:red">必填</span>
                            </div>
                        </div>

                    <#-- 开课日期 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">结课日期  <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="endDate" name="endDate" class="form-control js-postData" value="<#if course??>${course.endDate!''}</#if>" style="width: 336px;" />
                                <span style="font-size: 10px;color:red">必填</span>
                            </div>
                        </div>

                    <#-- 古诗名 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">古诗名 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="poemTitle" name="poemTitle" class="form-control js-postData" value="<#if course??>${course.poemTitle!''}</#if>" style="width: 336px" maxlength="20"/>

                                <span style="font-size: 10px;color: red">必填</span>
                            </div>
                        </div>

                    <#-- 古诗作者 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">古诗作者 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="author" name="author" class="form-control js-postData" value="<#if course??>${course.author!''}</#if>" style="width: 336px" maxlength="20"/>
                                <span style="font-size: 10px;color: red">必填</span>

                            </div>
                        </div>

                    <#-- 课古诗内容 -->
                        <div class="control-group" id="singleItemMain">
                            <label class="control-label">课古诗内容：<span style="color: red;font-size: 20px;">*</span></label>
                            <#if contents?has_content>
                             <input type="button" value="添加" class="btn btn-primary" id="addRowSingle"> <br/>
                                <#list contents as cts>
                                    <div class="controls singleItemBox" style="margin-top: 5px;">
                                        段落<input type="text" name="vitality" value="${cts.content!''}" class="input contentCtn">
                                        顺序<input type="text" name="vitality" value="${cts.order!''}" class="input orderCtn" style="width: 80px;" readonly>
                                        <input type="button" value="删除" class="btn thisDelete" data-val="${cts_index + 1}"><br/>
                                    </div>
                                </#list>
                            <#else>
                                <div class="controls singleItemBox" style="margin-top: 5px;">
                                    <input type="button" value="添加" class="btn btn-primary" id="addRowSingle">
                                </div>
                            </#if>
                            <div id="newAddSingleItem"></div>
                        </div>

                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function () {

        $('#openDate').datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss',  //日期格式，自己设置
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            onSelect: function (selectedDate) {
            }
        });

        $('#endDate').datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss',
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            onSelect: function (selectedDate) {
            }
        });

        //验证表单
        var validateForm = function () {
            var msg = "";
            if($('#title').val() == ''){
                msg += "点评课名称不能为空！\n";
            }
            if($('#title').val().length > 6) {
                msg += "点评课名称超过6个汉字！\n";
            }
            if($('#lessonId').val() == ''){
                msg += "请选择古诗课程ID！\n";
            }
            if($('#reminder').val() == ''){
                msg += "请填写课程提示！\n";
            }
            if($('#reminder').val().length > 60) {
                msg += "课程提示超过60个汉字！\n";
            }
            if($('#openDate').val() == ''){
                msg += "请填写开课时间！\n";
            }
            if($('#endDate').val() == ''){
                msg += "请填写结课时间！\n";
            }
            if($('#poemTitle').val() == ''){
                msg += "请填写古诗名称！\n";
            }
            if($('#author').val() == ''){
                msg += "请填写古诗作者";
            }
            if($('#body').val() == ''){
                msg += "请填写古诗内容！\n";
            }
            if (msg.length > 0) {
                alert(msg);
                return false;
            }
            return true;
        }

        var skuCContent = [];

        //保存提交
        $(document).on("click",'#save_ad_btn',function () {
            var singleItemMain = $("#singleItemMain");

            if(validateForm()){
                var post = {};
                $(".js-postData").each(function(i,item){
                    post[item.name] = $(item).val();
                });

                singleItemMain.find(".singleItemBox").each(function (index) {
                    var $this = $(this);
                    if ($this.find(".contentCtn").val() && $this.find(".orderCtn").val()) {
                        skuCContent.push({
                            content: $this.find(".contentCtn").val(),
                            order: $this.find(".orderCtn").val(),
                        });
                    }
                });
                post.body = JSON.stringify(skuCContent);
                $.post('save.vpage',post,function (res) {
                    if(res.success){
                        alert("保存成功");
                        location.href= 'commentcourse.vpage';
                    }else{
                        alert("保存失败");
                    }
                });
            }
        })

        //添加古诗内容
        var singleItemBox = $("#singleItemMain").find(".contentCtn").length;
        $(document).on("click", "#addRowSingle", function () {
            var count = $("#singleItemMain").find(".contentCtn").length;
            singleItemBox = count + 1;
            var newAddList = $("#newAddSingleItem");
            var _html = '<div class="controls singleItemBox" style="margin-top: 5px;">' +
                    '段落 <input type="text" name="vitality" value="" class="input contentCtn">' +
                    '顺序 <input type="text" name="vitality" value="'+ singleItemBox +'" class="input orderCtn" style="width: 80px;" readonly>' +
                    '<input type="button" value="删除" class="btn thisDelete" data-val="' + (singleItemBox++) + '">' +
                    '</div>';
            newAddList.append(_html);
        });

        //删除古诗内容
        $(document).on("click", "#singleItemMain .thisDelete", function () {
            var $this = $(this);
            singleItemBox--;
            var count = $("#singleItemMain").find(".contentCtn").length;
            if( count === $this.data("val")) {
                skuCContent.splice($this.data("val"), 1);
                $this.closest(".singleItemBox").remove();
            } else {
                alert("从尾行开始删除")
            };
        });

        //课程联动
        $(document).on("change",'#lessonId',function () {
            var lid = $('#lessonId').val();
            if(lid === null || lid === "") {
                return;
            }
            $.ajax({
                type: "post",
                url: "getcourselist.vpage",
                data: {
                    lessonId: lid
                },
                success: function (data) {
                    if (data.success) {
                        data = data.courseLessons;
                        var htmlContent = "";
                        for (var i = 0; i < data.length; i++) {
                            var id = data[i]._id;
                            var lessonName = data[i].lesson_name + " " + data[i]._id;
                            htmlContent += "<option selected='selected' value =" + id +">" + lessonName + "</option>";
                        }
                        $("#innerId").html(htmlContent);
                    } else {
                        alert("操作失败");
                    }
                }
            });
        });

    });
</script>
</@layout_default.page>

