<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='添加系统权限' page_num=8>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<#--<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>-->
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>

<script src="${requestContext.webAppContextPath}/public/js/fileuploader/SimpleAjaxUploader.min.js"></script>
<style type="text/css">
    select.s_time{
        width: 60px;;
    }
    div.checker{
        margin-right: 0px;
    }
    .radio-inline, .checkbox-inline{
        display: inline-block;
        padding-left: 20px;
        margin-bottom: 0;
        vertical-align: middle;
        font-weight: normal;
        cursor: pointer;
    }
</style>
<#macro forOption start=0 end=0 defaultVal=0>
    <#list start..end as index>
        <option value="<#if index lt 10>0</#if>${index}" <#if defaultVal == index>selected</#if>><#if index lt 10>0</#if>${index}</option>
    </#list>
</#macro>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 添加统考</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <form class="form-horizontal" method="POST">
                <fieldset>
                    <div class="control-group">
                        <label for="date01" class="control-label">考试名称</label>
                        <div class="controls">
                            <input type="text" id="examName" name="name" value="" maxlength="100" placeholder="输入考试名称" class="input-xlarge"/>
                            <#--考试类型-->
                            <input type="hidden" name="type" value="unify" />
                        </div>
                    </div>
                    <div class="control-group">
                        <label for="fileInput" class="control-label">上传试卷文件</label>
                        <div class="controls">
                            <button id="upload_file_but" type="button" class="btn btn-primary">上传文件</button>
                            <input type="hidden" id="fileUrl" name="fileUrl" class="input-file uniform_on" value="">
                            <input type="hidden" id="fileName" name="fileName" class="input-file uniform_on" value="">
                            <span class="filename" id="fileresult" style="-moz-user-select: none;">No file selected</span>
                            <span class="action" style="-moz-user-select: none;">(只能上传一套试卷的word文档,文件总大小不能超过10M)</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label for="date01" class="control-label">学科</label>
                        <div class="controls">
                            <select name="subjectId" id="subjectId">
                                <#if subjects?has_content && subjects?size gt 0>
                                    <#list subjects?keys as subjectId>
                                        <option value="${subjectId}">${subjects[subjectId]}</option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label for="date01" class="control-label">年级</label>
                        <div class="checkbox" id="clazzLevelDiv">
                            <label class="checkbox-inline">
                                <input type="checkbox" name="clazzLevels" value="1"> 1年级
                            </label>
                            <label class="checkbox-inline">
                                <input type="checkbox" name="clazzLevels" value="2"> 2年级
                            </label>
                            <label class="checkbox-inline">
                                <input type="checkbox" name="clazzLevels" value="3"> 3年级
                            </label>
                            <label class="checkbox-inline">
                                <input type="checkbox" name="clazzLevels" value="4"> 4年级
                            </label>
                            <label class="checkbox-inline">
                                <input type="checkbox" name="clazzLevels" value="5"> 5年级
                            </label>
                            <label class="checkbox-inline">
                                <input type="checkbox" name="clazzLevels" value="6"> 6年级
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">测试类型</label>
                        <div class="controls" style="margin-left: 140px;">
                            <label class="radio-inline">
                                <input type="radio" checked="" value="city"  name="regionLevel" style="opacity: 0;">
                                市级
                            </label>
                            <label class="radio-inline">
                                <input type="radio" value="country"  name="regionLevel" style="opacity: 0;">
                                区级
                            </label>
                            <label class="radio-inline">
                                <input type="radio" value="school"  name="regionLevel" style="opacity: 0;">
                                校级
                            </label>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="selectError1">负责地区</label>
                        <div id="regiontree" class="controls" style="width: 280px;height: 400px">
                        </div>
                    </div>
                    <div class="control-group" id="schoolGroup">
                        <label for="inputWarning" class="control-label">学校</label>
                        <div class="controls">
                            <select id="schoolIds" multiple data-rel="chosen">
                                <option value="">请选择</option>
                            </select>
                            <span class="help-inline" style="display: none;">Something may have gone wrong</span>
                        </div>
                    </div>

                    <div class="control-group">
                        <label for="date01" class="control-label">考试开始时间</label>
                        <div class="controls">
                            <input type="text" value="" id="beginDate" readonly="readonly" placeholder="格式：2015-11-04" class="input-xlarge">
                            <select class="s_time" name="startHour" id="startHour">
                                <@forOption start=0 end=23 defaultVal=0 />
                            </select> 时
                            <select class="s_time" name="startMin" id="startMin">
                                <@forOption start=0 end=59 defaultVal=0 />
                            </select> 分
                        </div>
                    </div>

                    <div class="control-group">
                        <label for="date02" class="control-label">考试结束时间</label>
                        <div class="controls">
                            <input type="text" value="" id="endDate" readonly="readonly" placeholder="格式：2015-11-04" class="input-xlarge">
                            <select class="s_time" name="endHour" id="endHour">
                                <@forOption start=0 end=23 defaultVal=0 />
                            </select> 时
                            <select class="s_time" name="endMin" id="endMin">
                                <@forOption start=0 end=59 defaultVal=0 />
                            </select> 分
                        </div>
                    </div>

                    <div class="control-group">
                        <label for="date02" class="control-label">教师批改截止时间</label>
                        <div class="controls">
                            <input type="text" value="" id="correctEndDate" readonly="readonly" placeholder="格式：2015-11-04" class="input-xlarge">
                            <select class="s_time" name="correctEndHour" id="correctEndHour">
                                <@forOption start=0 end=23 defaultVal=0 />
                            </select> 时
                            <select class="s_time" name="correctEndMin" id="correctEndMin">
                                <@forOption start=0 end=59 defaultVal=0 />
                            </select> 分
                        </div>
                    </div>

                    <div class="control-group">
                        <label for="date02" class="control-label">成绩发布时间</label>
                        <div class="controls">
                            <input type="text" value="" id="publishDate" readonly="readonly" placeholder="格式：2015-11-04" class="input-xlarge">
                            <select class="s_time" name="publishHour" id="publishHour">
                                <@forOption start=0 end=23 defaultVal=0 />
                            </select> 时
                            <select class="s_time" name="publishMin" id="publishMin">
                                <@forOption start=0 end=59 defaultVal=0 />
                            </select> 分
                        </div>
                    </div>
                    <div class="control-group">
                        <label for="date01" class="control-label">开始考试后</label>
                        <div class="controls">
                            <input type="text" value="0" id="submitAfterMinutes" placeholder="格式：0" class="input-mini input-keyup-numeric">
                            分钟前禁止学生交卷，0分钟代表可任何时候交卷
                        </div>
                    </div>
                    <div class="control-group">
                        <label for="date01" class="control-label">答题时长</label>
                        <div class="controls">
                            <input type="text" value="0" id="durationMinutes" placeholder="格式：0" class="input-mini input-keyup-numeric">
                            分钟&nbsp;&nbsp;时间到期不能继续答题
                        </div>
                    </div>
                    <div class="form-actions">
                        <button id="addOralBtn" type="button" class="btn btn-primary">保存</button>
                        <a class="btn" href="list.vpage"> 取消 </a>
                    </div>
                </fieldset>
            </form>
        </div>
    </div><!--/span-->
</div>
<script type="text/javascript">
    $(function(){
        <#---时间设置--->
        var defaultOptions = {
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : "${nowDate}",
            minDate         : "${nowDate}",
            numberOfMonths  : 1,
            changeMonth     : false,
            changeYear      : false,
            onSelect        : function (selectedDate){}
        };
        $("#beginDate").datepicker(defaultOptions);
        $("#endDate").datepicker(defaultOptions);
        $("#correctEndDate").datepicker(defaultOptions);
        $("#publishDate").datepicker(defaultOptions);
        <#---时间设置结束--->

        $("input.input-keyup-numeric").on("keyup",function(){
            var $this = $(this);
            if(/\D/g.test($this.val())){
                $this.val(0);
            }
        });

        loadRegion();


        function validateForm(){
            var _examName = $.trim($("#examName").val());
            if(!_examName){
                alert("请输入考试名称");
                return false;
            }
            var fileUrl = $.trim($("#fileUrl").val());
            if(!fileUrl){
                alert("请上传文件");
                return false;
            }
            var $clazzLevels = $("input[name='clazzLevels']:checked");
            if(!$clazzLevels || $clazzLevels.length == 0){
                alert("请选择年级");
                return false;
            }

            var regions = getSelectedRegionCode();
            if(regions.length == 0){
                alert("请选择区域");
                return false;
            }

            return true;
        }

        // 年级
        var clazzLevelMap = ${clazzLevelMap!'{}'};

        $("#subjectId").on("change",function(){
            var $this = $(this);
            var subjectId = $this.val().toString();
            var levels = [1,2,3,4,5,6,7,8,9];
            if(!subjectId){
                return false;
            }
            var firstCharater = subjectId.substring(0,1);
            var newLevels;
            switch (firstCharater){
                case '1':
                    newLevels = levels.slice(0,6);
                    break;
                case '2':
                    newLevels = levels.slice(5,10);
                    break;
                default:
                    break;
            }
            if(!newLevels){
                return false;
            }
            var $clazzLevelDiv = $("#clazzLevelDiv");
            $clazzLevelDiv.empty();
            var htmlContent = "";
            for(var z = 0,zLen = newLevels.length; z < zLen;z++){
                var _grade = clazzLevelMap[newLevels[z]];
                if(_grade){
                    htmlContent += '<label class="checkbox-inline"><input type="checkbox" value="' + newLevels[z] + '" name="clazzLevels" style="margin-left:1px;">&nbsp;&nbsp;' + _grade + '</label>'
                }
            }
            $clazzLevelDiv.append(htmlContent);
        });
        $("#subjectId").trigger("change");


        $("#addOralBtn").on("click",function(){
            var $this = $(this);
            if(!validateForm()){
                return false;
            }
            var clazzLevels = [];
            $.each($("input[name='clazzLevels']:checked"),function(){
                clazzLevels.push($(this).val());
            });

            var schoolIds = $("#schoolIds").val();

            if($this.hasClass("locked-btn")){
                return false;
            }
            $this.addClass("locked-btn");
            $.ajax({
                type : 'post',
                url : "saveoral.vpage",
                data : $.toJSON({
                    name                : $.trim($("#examName").val()),
                    fileUrl             : $.trim($("#fileUrl").val()),
                    fileName            : $.trim($("#fileName").val()),
                    clazzLevels         : clazzLevels.toString(),
                    subjectId           : $("#subjectId").val(),
                    regionLevel         : $("input[name='regionLevel']:checked").val(),
                    regions             : getSelectedRegionCode().toString(),
                    schoolIds           : $.isArray(schoolIds) ? schoolIds.toString() : schoolIds,
                    examStartAt         : $.trim($("#beginDate").val()) + " " + $("#startHour").val() + ":" + $("#startMin").val() + ":00",
                    examStopAt          : $.trim($("#endDate").val()) + " " + $("#endHour").val() + ":" + $("#endMin").val() + ":00",
                    correctEndDate      : $.trim($("#correctEndDate").val()) + " " + $("#correctEndHour").val() + ":" + $("#correctEndMin").val() + ":00",
                    resultIssueAt       : $.trim($("#publishDate").val()) + " " + $("#publishHour").val() + ":" + $("#publishMin").val() + ":00",
                    submitAfterMinutes  : $("#submitAfterMinutes").val(),
                    durationMinutes     : $("#durationMinutes").val()
                }),
                success : function(data){
                    if(!data.success){
                        alert(data.info);
                    }else{
                        $(window.location).attr('href', 'list.vpage');
                    }
                    $this.removeClass("locked-btn");
                },
                error : function(){
                    $this.removeClass("locked-btn");
                },
                dataType : "json",
                contentType : 'application/json;charset=UTF-8',
                cache : false
            });
        });


        var paperUploader = new ss.SimpleUpload({
            button: 'upload_file_but', // file upload button
            url: 'uploadoralfile.vpage', // server side handler
            name: 'uploadfile', // upload parameter name
            responseType: 'json',
            multipart:true,
            allowedExtensions: ['doc','docx'],
            maxSize: 10*1024, // kilobytes
            hoverClass: 'ui-state-hover',
            focusClass: 'ui-state-focus',
            disabledClass: 'ui-state-disabled',
            debug: false,
            onExtError:function( filename, extension ){
                alert("仅支持doc|docx文件格式");
                return false;
            },
            onSubmit: function(filename, extension) {
            },
            onComplete: function(filename, response) {
                var $fileresult = $("span[id='fileresult']");
                if (!response.success) {
                    $fileresult.text(response.info).show();
                    return false;
                }
                var html = filename + "上传成功";
                $fileresult.text(html).show();
                $("#fileUrl").val(response.fileUrl);
                $("#fileName").val(filename);
                $("#upload_file_but").hide();
            }
        });

        <#--测试类型-->
        $("input[name='regionLevel']").on("click",function(){
            var $schoolGroup = $("#schoolGroup");
            if(isSchoolOfRangeType()){
                $schoolGroup.show();
            }else{
                $schoolGroup.hide();
            }
        });
        $("input[name='regionLevel']:checked").trigger("click");
    });

    function isSchoolOfRangeType(){
        return $("input[name='regionLevel']:checked").val() == "school";
    }

    function loadRegion(){
        $("#regiontree").fancytree({
            extensions: ["filter"],
            source: {
                url: "/common/region/loadregion.vpage",
                cache:true
            },
            checkbox: true,
            selectMode: 2,
            select:function(){
                updateSchoolList();
            }
        });
    }

    function getSelectedRegionCode(){
        var regionTree = $("#regiontree").fancytree("getTree");
        var regionNodes = regionTree.getSelectedNodes();
        if(regionNodes == null || regionNodes == "undefined") return null;
        var codes = [];
        $.map(regionNodes, function(node){
            codes.push(node.key);
        });

        return codes;
    }

    function updateSchoolList(){
        if(!isSchoolOfRangeType()){
            return false;
        }
        var $this = $("#schoolIds");
        if($this.hasClass("locked-btn")){
            return false;
        }
        $this.addClass("locked-btn");

        var subjectId = $("#subjectId").val().toString();
        var schoolLevel;
        if(!subjectId){
            schoolLevel = "1";
        }
        schoolLevel = subjectId.substring(0,1);
        $.ajax({
            type : 'post',
            url : "searchschool.vpage",
            data : $.toJSON({
                regions : getSelectedRegionCode(),
                schoolLevel : schoolLevel
            }),
            success : function(data){
                $this.removeClass("locked-btn");
                if(data.success){
                    var options = '<option value="">请选择</option>';
                    if(data.schools != null && data.schools.length > 0){
                        for(var i = 0; i < data.schools.length; i++){
                            options += '<option value="' + data.schools[i].id + '">' + data.schools[i].cname + '</option>';
                        }
                    }
                    $this.empty().html(options);
                    $this.trigger("liszt:updated");
                }
            },
            error : function(){
                $this.removeClass("locked-btn");
            },
            dataType : "json",
            contentType : 'application/json;charset=UTF-8',
            cache : false
        });
    }
</script>

</@layout_default.page>