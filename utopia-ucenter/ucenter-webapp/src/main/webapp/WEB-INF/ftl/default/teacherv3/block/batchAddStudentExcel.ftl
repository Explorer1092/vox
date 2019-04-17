<style>
    .w-tips-main .tips-box{position:absolute;top:32px;right:0;z-index:3;display:none;padding:15px 8px 20px 15px;width:220px;border:1px solid #ddd;border-radius:5px;background-color:#fff;color:#5d5f5f;text-align:left;font-size:14px;line-height:18px;cursor:default}
    .w-tips-main .tips-box:after{position:absolute;top:-7px;right:7px;width:12px;height:7px;background: url(<@app.link href='public/skin/teacherv3/images/icon-ques.png?v=652ce4411b'/>) 0 -35px no-repeat;content:""}
    .w-tips-main:hover .tips-box{ display: block;}
    .clearfix:after{content:".";display:block;visibility:hidden;clear:both;height:0;font-size:0}
    .layer-module .layer-upload{position:relative;padding:0 20px}
    .layer-module .layer-upload .head{position:relative;margin:-10px 0 0;padding:0 0 20px;border-bottom:1px #ebebeb solid}
    .layer-module .layer-upload .head .download,.layer-module .layer-upload .head .title,.layer-module .layer-upload .head .upload{float:left;margin:0 20px 0 0;line-height:30px}
    .layer-module .layer-upload .head .title{color:#5d5f5f;font-size:16px}
    .layer-module .layer-upload .head .upload{width:88px;text-align:center;color:#fff;font-size:14px;background-color:#189cfb}
    .layer-module .layer-upload .head .download{color:#64c3e3;font-size:14px}
    .layer-module .layer-upload .text{padding:16px 0 20px;color:#848a95;font-size:16px;line-height:24px}
    .layer-module .layer-upload .text p{position:relative;padding:0 0 0 16px}
    .layer-module .layer-upload .text p em{position:absolute;top:0;left:0;font-style:normal}
    .layer-module .layer-upload .head .name{position:absolute;bottom:0;left:100px;color:#64c3e3;font-size:12px;line-height:20px}
    .layer-module .layer-upload .prom{bottom:0;left:20px;right:20px;text-align:center;color:#f69696;font-size:14px}
    .layer-module .layer-label{padding:0 0 36px;}
    .layer-module .layer-label .label{padding:20px 0 0;width:100%;min-height:30px}
    .layer-module .layer-label .label .title{float:left;width:170px;text-align:right;color:#5d5f5f;font-size:16px;line-height:30px}
    .layer-module .layer-label .label .prom{position:absolute;top:36px;left:170px;color:#f69696;font-size:14px;line-height:22px}
    .layer-module .layer-label .label input{display:block;padding:4px 8px;width:250px;height:20px;border:1px solid #ebebeb;background-color:#f6f6f6;border-radius:3px}
    .layer-module .layer-label .label .tag { display: inline-block; width: 80px; height: 28px; color: #c2c0c1; line-height: 28px; cursor: pointer; text-align: center; border-radius: 3px; font-size: 14px; border: 1px solid #c2c0c1; }
    .layer-module .layer-label .label .tag.active { border: 1px solid #189cfb;; color: #189cfb;; }
    .layer-module .layer-label .text{padding:0 90px;text-align:center;color:#5d5f5f;font-size:16px;line-height:44px}
    .layer-module .layer-label .text .key{color:#f69a9a}
</style>
<script id="t:导入学生名单" type="text/html">
    <div class="layer-module">
        <div class="layer-main">
            <div class="layer-upload">
                <div class="head clearfix">
                    <div class="title">选择文档：</div>
                    <a href="javascript:;" class="upload v-uploadKlxDoc">上传</a>
                    <a href="javascript:;" class="download v-downloadTemplate">下载模板</a>
                    <input type="file" id="v-fileupload" data-clazzId="${clazzId!}" accept=".xls, .xlsx" name="file" style="display: none" />
                    <div class="name v-fileName" style="display:none;"></div>
                </div>
                <div class="text">
                    <div>上传说明：</div>
                    <p><em>1.</em>需要按照模版格式要求上传学生姓名、学号；</p>
                    <p><em>2.</em>班内无该姓名学生时，会为其新注册账号：有该学生时，会更新该生学号信息；</p>
                    <p><em>3.</em>学号后N位在学校内不重复的情况下将作为学生阅卷机填涂号，如遇重复将随机生成N位数字（N位学校当前填涂号位数，一般是5位）；</p>
                </div>
                <div class="prom" id="v-errMsg" style="display: none"></div>
            </div>
        </div>
    </div>
    <div style="clear:both;"></div>
</script>
<script id="t:批量导入学生名单" type="text/html">
    <div class="layer-module">
        <div class="layer-main">
            <div class="layer-success">
                <div class="success"><span>导入成功</span></div>
                <div class="title">新注册<span class="js-klxImportNewSignNum"></span>名学生，更新<span class="js-klxImportUpdateNum"></span>名学生学号！</div>
                <div class="text">注：阅卷机填涂号更新后，一定要及时告知学生，否则会影响学生答题卡扫描</div>
            </div>
        </div>
    </div>
</script>

<script id="t:重复学生" type="text/html">
    <div class="layer-module">
        <div class="layer-main">
            <div class="layer-success">
                <div class="text" style="color:black;font-size:17px">
                    上传名单中学生 <span class="js-klxRepeateStudentName" style="font-weight: bold"></span> 和班内已有学生重名，请确认是否更新班内学生学号信息。
                    如不是同一学生，请点击取消、并修改姓名进行区分。
                </div>
            </div>
        </div>
    </div>
</script>


<script id="t:学生填涂号占用信息" type="text/html">
    <div class="layer-module">
        <div class="layer-main">
            <div class="layer-success">
                <div class="text" style="color:black;font-size:17px">
                    学生 <span class="js-klxTakeUpStudentNames" style="font-weight: bold"></span>学生的填涂号无法使用默认填涂号,已被如下学生占用：<br/>
                    <span class="js-klxTakeUpStuTeacherInfo" style="font-weight: bold"></span>
                    点击确认可使用随机填涂号，如有问题，可联系以上老师或客服进行处理
                </div>
            </div>
        </div>
    </div>
</script>
<script type="text/javascript">
    $(function(){
        var currentId;
        //选择班级
        $(document).on("click", "#classMapsList li", function(){
            var $this = $(this);

            currentId = $this.data("id");
            $this.addClass("active").siblings().removeClass("active");
            $("#all_batch_student_name").val("");
        });

        //导入班级名单列表
        $(".data-ImportStudentName").on("click",function () {
            var importKlxStudentPass = {
                state0: {
                    title: '导入学生名单',
                    html: template("t:导入学生名单", {}),
                    focus: 1,
                    buttons: {'取消': false, '确定': true},
                    position: {width: 580},
                    submit: function (e, v) {
                        e.preventDefault();
                        if (v) {
                            var fileInput = $('#v-fileupload');

                            var fileName = fileInput.val();
                            if (fileName.substring(fileName.length - 4) != ".xls"
                                    && fileName.substring(fileName.length - 5) != ".xlsx") {
                                $("#v-errMsg").html("仅支持上传excel文档");
                                $("#v-errMsg").show();
                                return false;
                            }

                            var formData = new FormData();
                            var file = fileInput[0].files[0];
                            formData.append('adjustExcel', file);
                            formData.append('clazzId', fileInput.attr("data-clazzId"));
                            formData.append('checkRepeatedStudent',true);

                            $.ajax({
                                url: "/teacher/clazz/kuailexue/batchimportstudents.vpage",
                                type: "POST",
                                data: formData,
                                processData: false,
                                contentType: false,
                                async: true,
                                timeout: 5 * 60 * 1000,
                                success: function (data) {
                                    if (data.success) {
                                        if (data.repeatedStudentList && data.repeatedStudentList.length > 0) {//跳转到提示班级内有重复学生的弹窗
                                            var repeateNames = data.repeatedStudentList.join("、");
                                            if (data.moreStudent) repeateNames += "等";
                                            $(".js-klxRepeateStudentName").html(repeateNames);
                                            $.prompt.goToState("state2");
                                        } else { //查询是否有学生被占用
                                            var newFormData = new FormData();
                                            newFormData.append('adjustExcel', file);
                                            newFormData.append('clazzId', fileInput.attr("data-clazzId"));
                                            newFormData.append('checkRepeatedStudent', false);
                                            newFormData.append('checkTakeUpStudent', true);
                                            $.ajax({
                                                url: "/teacher/clazz/kuailexue/batchimportstudents.vpage",
                                                type: "POST",
                                                data: newFormData,
                                                processData: false,
                                                contentType: false,
                                                async: true,
                                                timeout: 5 * 60 * 1000,
                                                success: function (data) {
                                                    if (data.success) {
                                                        if (data.isTakeUp) {//跳转到提示有学生被占用的弹窗
                                                            var importStudentNames = "";
                                                            var takeUpSutTeacherInfo = "";
                                                            if (data.importNames && data.importNames.length > 0) {
                                                                var names = [];
                                                                for (var l = 0; l < 3 && l < data.importNames.length; ++l) {
                                                                    names.push(data.importNames[l]);
                                                                }
                                                                importStudentNames = names.join("、");
                                                                if(data.importNames.length >= 3){
                                                                    importStudentNames += "等";
                                                                }
                                                            }
                                                            if (data.takeUpInfo && data.takeUpInfo.length > 0) {
                                                                for (var i = 0; i < data.takeUpInfo.length; i++) {
                                                                    takeUpSutTeacherInfo = takeUpSutTeacherInfo + data.takeUpInfo[i].teacherName + " " + data.takeUpInfo[i].clazzName + " " + data.takeUpInfo[i].studentName + "<br/>"
                                                                }
                                                            }
                                                            $(".js-klxTakeUpStudentNames").html(importStudentNames);
                                                            $(".js-klxTakeUpStuTeacherInfo").html(takeUpSutTeacherInfo);
                                                            $.prompt.goToState("state3");
                                                        } else {//跳转到更新成功的弹窗
                                                            $(".js-klxImportNewSignNum").html(data.newSignNum);
                                                            $(".js-klxImportUpdateNum").html(data.updateNum);
                                                            $.prompt.goToState("state1");
                                                        }
                                                    } else {
                                                        $("#v-errMsg").html(data.info || '导入失败，稍后重试');
                                                        $("#v-errMsg").show();
                                                        return false;
                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                        $("#v-errMsg").html(data.info || '导入失败，稍后重试');
                                        $("#v-errMsg").show();
                                        return false;
                                    }
                                }
                            });
                        }else{
                            $.prompt.close();
                        }
                    }
                },
                state1:{
                    title: "批量导入学号／注册学生",
                    html: template("t:批量导入学生名单", {}),
                    buttons: {"确定": true},
                    position: {width: 580, height: 358},
                    submit: function(){
                        setTimeout(function(){location.reload()}, 200);
                    }
                },
                state2:{
                    title: "系统提示",
                    html: template("t:重复学生", {}),
                    buttons: {"取消": false,"确定,更新学生信息": true},
                    position: {width: 580, height: 358},
                    submit  : function(e, v){
                        e.preventDefault();
                        if(v){
                            var fileInput = $('#v-fileupload');
                            var formData = new FormData();
                            var file = fileInput[0].files[0];
                            formData.append('adjustExcel', file);
                            formData.append('clazzId', fileInput.attr("data-clazzId"));
                            formData.append('checkRepeatedStudent',false);
                            formData.append('checkTakeUpStudent',true);
                            $.ajax({
                                url: "/teacher/clazz/kuailexue/batchimportstudents.vpage",
                                type: "POST",
                                data: formData,
                                processData: false,
                                contentType: false,
                                async: true,
                                timeout: 5 * 60 * 1000,
                                success: function (data) {
                                    if (data.success) {
                                        if (data.isTakeUp) {//跳转到提示有学生被占用的弹窗
                                            var importStudentNames = "";
                                            var takeUpSutTeacherInfo = "";
                                            if (data.importNames && data.importNames.length > 0) {
                                                var names = [];
                                                for (var l = 0; l < 3 && l < data.importNames.length; ++l) {
                                                    names.push(data.importNames[l]);
                                                }
                                                importStudentNames = names.join("、");
                                                if(data.importNames.length >= 3){
                                                    importStudentNames += "等";
                                                }
                                            }
                                            if (data.takeUpInfo && data.takeUpInfo.length > 0) {
                                                for (var i = 0; i < data.takeUpInfo.length; i++) {
                                                    takeUpSutTeacherInfo = takeUpSutTeacherInfo + data.takeUpInfo[i].teacherName + " " + data.takeUpInfo[i].clazzName + " " + data.takeUpInfo[i].studentName + "<br/>"
                                                }
                                            }
                                            $(".js-klxTakeUpStudentNames").html(importStudentNames);
                                            $(".js-klxTakeUpStuTeacherInfo").html(takeUpSutTeacherInfo);
                                            $.prompt.goToState("state3");
                                        } else {//跳转到更新成的弹窗
                                            $(".js-klxImportNewSignNum").html(data.newSignNum);
                                            $(".js-klxImportUpdateNum").html(data.updateNum);
                                            $.prompt.goToState("state1");
                                        }
                                    } else {
                                        $("#v-errMsg").html(data.info || '更新失败');
                                        $("#v-errMsg").show();
                                        return false;
                                    }
                                }
                            });
                        }else{
                            $.prompt.goToState("state0");
                        }
                    }
                },
                state3: {
                    title: "系统提示",
                    html: template("t:学生填涂号占用信息", {}),
                    buttons: {"取消": false, "确定,使用随机填涂号": true},
                    position: {width: 580, height: 358},
                    submit: function (e, v) {
                        e.preventDefault();
                        if (v) {
                            var fileInput = $('#v-fileupload');
                            var formData = new FormData();
                            var file = fileInput[0].files[0];
                            formData.append('adjustExcel', file);
                            formData.append('clazzId', fileInput.attr("data-clazzId"));
                            formData.append('checkRepeatedStudent',false);
                            formData.append('checkTakeUpStudent',false);
                            $.ajax({
                                url: "/teacher/clazz/kuailexue/batchimportstudents.vpage",
                                type: "POST",
                                data: formData,
                                processData: false,
                                contentType: false,
                                async: true,
                                timeout: 5 * 60 * 1000,
                                success: function (data) {
                                    if (data.success) {
                                        $(".js-klxImportNewSignNum").html(data.newSignNum);
                                        $(".js-klxImportUpdateNum").html(data.updateNum);
                                        $.prompt.goToState("state1");
                                    } else {
                                        $("#v-errMsg").html(data.info || '请求失败，稍后重试');
                                        $("#v-errMsg").show();
                                        return false;
                                    }
                                }
                            });
                        } else {
                            $.prompt.goToState("state0");
                        }
                    }
                }
            };

            $.prompt(importKlxStudentPass);

            $(".v-uploadKlxDoc").on("click", function () {
                var ie = !-[1,];
                if(ie){
                    $('#v-fileupload').trigger('click').trigger('change');
                }else{
                    $('#v-fileupload').trigger('click');
                }
            });

            $('#v-fileupload').change(function(){
                // 截掉前面的路径，只留文件名
                var fileInput = $("#v-fileupload").val();
                fileInput = fileInput.substring(fileInput.lastIndexOf("\\") + 1);
                $(".v-fileName").html(fileInput);
                $(".v-fileName").show();
            });

            $(".v-downloadTemplate").on("click", function () {
                $("body").append("<iframe style='display:none;' src='/teacher/clazz/kuailexue/clazzstutemplate.vpage'/>");
            });
        });
    });
</script>