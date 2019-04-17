<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bgf2"
title="调查问卷"
pageJs=['jquery', 'utils', 'template', 'datepicker']
pageCssFile={"css" : ["public/skin/mobile/student/ugcnew/css/skin"]}
>
<style type="text/css">
    .ui-widget-content{font-size: 14px;}
</style>
<@UGC_Record/>
<#macro UGC_Record opts={
"title" : "调查问卷",
"getLink" : ((currentUser.userType == 2)!false)?string("/ugc/loadugcforteacherconfirm.vpage", "/ugc/loadugc.vpage"),
"postLink" : "/ugc/saveugcanswer.vpage"
}>
<div id="ugcSchoolInfoSurvey"></div>

<script id="T:UGC-RECORD" type="text/html">
    <%var input_placeholder = "请填写"%>
    <%if(data.recordId == specialRecordId){%>
    <%input_placeholder = "请填写新老师姓名"%>
    <div class="aut-title" style="text-align: center; margin-bottom: -1.2rem;">
        <p><%=(data.recordName)%></p>
        <p>请确认 <%=data.userName%> 的老师信息</p>
    </div>
    <%}%>
    <div class="autCateInfor-box">
        <div class="w-progress">
            <div class="inner"></div>
        </div>
        <div class="w-main">
            <%for(var i = 0; i < items.length; i++){%>
                <%if(items[i].questionType === "INPUT" ){%>
                    <%if(items[i].questionName.indexOf("#INPUT_DATETIME#") > -1){%>
                        <div class="aut-title"><%=(i+1)%>、<%==(items[i].questionName.replace(/#(INPUT_DATETIME)#/g, ' <input type="text" style="width: 6.2rem; cursor: pointer;" class="txt2 ugcDatetimeBtn questionId-'+ items[i].questionId +'" placeholder="请选择时间" readonly="readonly"/> '))%></div>
                    <%}else{%>
                        <div class="aut-title"><%=(i+1)%>、<%==(items[i].questionName.replace(/#(INPUT|LONG_INPUT)#/g, ' <input type="text" class="txt2 questionId-'+ items[i].questionId +'" placeholder="请填写" maxlength="32"/> '))%></div>
                    <%}%>
                <%}%>

                <%if(items[i].questionType == "SELECT"){%>
                    <%if(items[i].questionName.indexOf("#SIGNSELECT#") > -1){%>
                        <div class="aut-title"><%==(i+1)%>、<%=(items[i].questionName.replace(/#SIGNSELECT#/g, ''))%></div>
                        <ul class="aut-list floatList">
                            <%for(var b = 0, options = items[i].options; b < options.length; b++){%>
                                <li class="questionType-SIGNSELECT" data-type="<%=items[i].questionId%>" data-grade="<%=b%>">

                                    <%if(options[b].indexOf("#INPUT#") > -1){%>
                                    <% var intContent = ' <input type="text" class="txt1 questionId-'+ items[i].questionId +'" value="" data-type="MULTISELECT" placeholder="'+ input_placeholder +'"/> '; %>
                                    <i class="icoCheck"></i> <%==(options[b].replace(/#INPUT#/g, intContent))%>
                                    <%}else{%>
                                        <i class="icoCheck"></i> <%==(options[b])%>
                                        <input type="hidden" class="txt1 questionId-<%=items[i].questionId%>" value="<%=options[b]%>" data-type="MULTISELECT"/>
                                    <%}%>
                                </li>
                            <%}%>
                        </ul>
                    <%}else{%>
                        <%
                        var selectContent = ' <select style=" outline: none;" class="input questionId-'+ items[i].questionId +'"><option value="#请选择#">请选择</option>';
                        for(var b = 0, options = items[i].options; b < options.length; b++){selectContent += '<option value="'+ options[b] +'">'+ options[b] +'</option>'}
                        selectContent += '</select> ';
                        %>

                        <div class="aut-title">
                            <%=(i+1)%>、<%==(items[i].questionName.replace(/#SELECT#/g, selectContent))%>
                        </div>
                    <%}%>
                <%}%>

                <%if(items[i].questionType == "MULTISELECT"){%>
                    <div class="aut-title"><%==(i+1)%>、<%=(items[i].questionName.replace(/#MULTISELECT#/g, ''))%></div>
                    <ul class="aut-list floatList floatNon">
                        <%for(var b = 0, options = items[i].options; b < options.length; b++){%>
                        <li class="questionType-MULTISELECT" data-type="<%=items[i].questionId%>" data-grade="<%=b%>">
                            <input type="hidden" class="questionId-<%=items[i].questionId%>" data-type="MULTISELECT" value="<%=options[b]%>"/>
                            <i class="icoCheck"></i>
                            <%=options[b]%>
                        </li>
                        <%}%>
                    </ul>
                <%}%>
            <%}%>
        </div>

        <div class="w-footer">
            <div class="inner">
                <div class="btn-box">
                    <a href="javascript:;" class="btn tail JS-doSubmit">提交</a>
                </div>
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="T:success">
    <div class="questionnaire-box">
        <div class="bg">问卷已提交，感谢反馈！</div>
    </div>
</script>

<script type="text/javascript">
    signRunScript = function () {
        var postDataRecord, recordId = getQuery("recordId"), specialRecordId = ${((ftlmacro.devTestSwitch)!false)?string(7, 5)};


        //获得地址栏参数
        function getQuery(item){
            var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
            return svalue ? decodeURIComponent(svalue[1]) : '';
        }

        //验证是否未定义或null或空字符串
        function isBlank(str){
            return typeof str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
        }

        function UgcRecordPopup(){
            $.get("${(opts.getLink)!}?recordId=" + recordId, function(data){
                if(data.success && !isBlank(data.ugc)){
                    recordMain(data);
                    recordId = data.ugc.recordId;
                }else{
                    $("#ugcSchoolInfoSurvey").html( template("T:success", {}) );
                }
            });
        }

        function recordMain(data){
            var $unAnswerQuestions = data.ugc.unAnswerQuestions;
            var $usa = navigator.userAgent.toLowerCase();
            var $postData = {
                source : "MOBILE",
                recordId : data.ugc.recordId,
                answerMapList : []
            };

            <#if (currentUser.userType == 2)!false>
                $postData.userId = data.ugc.userId;
            </#if>

            if($usa.indexOf("micromessenger") > -1){
                $postData.source = "WECHAT";
            }

            for(var i = 0; i < $unAnswerQuestions.length; i++){
                $postData.answerMapList.push({questionId : $unAnswerQuestions[i].questionId, answer : ""});
            }

            postDataRecord = $postData;

            $("#ugcSchoolInfoSurvey").html( template("T:UGC-RECORD", {items : $unAnswerQuestions, data : data.ugc, specialRecordId : specialRecordId}) );

            setTimeout(function(){
                var ugcDatetimeBtn = $( ".ugcDatetimeBtn");
                if( ugcDatetimeBtn.length > 0 ){
                    ugcDatetimeBtn.datepicker({
                        dateFormat: 'yy-mm-dd',
                        yearRange: '2000:2050',
                        monthNamesShort:['01','02','03','04','05','06','07','08','09','10','11','12'],
                        changeMonth: true,
                        changeYear: true
                    });
                }
            }, 100);
        }

        function verInfo(data){
            var result = false;
            for(var i = 0, answerMapList = data.answerMapList; i < answerMapList.length; i++){
                answerMapList[i].answer = pushAnswer(".questionId-" + answerMapList[i].questionId);

                if( answerMapList[i].answer == ""){
                    result = true;
                    break;
                }
            }
            return result;
        }

        function pushAnswer(itemId){
            var $content = "";
            var $id = $(itemId);

            if($id.length > 1){
                $content = [];

                if($id.attr("data-type") == "MULTISELECT"){
                    $id = $(itemId + ".active");
                }

                for(var i = 0; i < $id.length; i++){
                    if( !isBlank($id.eq(i).val()) ){
                        $content.push($id.eq(i).val());
                    }else{
                        $content = [];
                        break;
                    }
                }

                $content = $content.join("#");
            }else{
                if( !isBlank($id.val()) && $id.val() != "#请选择#"){
                    $content = $id.val();
                }
            }

            return $content;
        }

        function postJSON(url, data, callback, error, dataType){
            dataType = dataType || "json";
            if(error == null || !$.isFunction(error)){
                error = function(){
                    //console.info(error);
                };
            }

            return $.ajax({
                type       : 'post',
                url        : url,
                data       : $.toJSON(data),
                success    : callback,
                error      : error,
                dataType   : dataType,
                contentType: 'application/json;charset=UTF-8'
            });
        }

        $(document).on("click", ".questionType-MULTISELECT", function(){
            var $this = $(this);

            $this.toggleClass("active");
            $this.find("input").toggleClass("active");
        });

        $(document).on("click", ".questionType-SIGNSELECT", function(){
            var $this = $(this);

            $this.addClass("active").find("input").addClass("active");
            $this.siblings().removeClass("active").find("input").removeClass("active");

            $this.find("input").focus();
        });

        $(document).on("click", ".JS-doSubmit", function(){
            if(isBlank(postDataRecord) || verInfo(postDataRecord) ){
                alert("请填写完善！");
                return false;
            }

            postJSON("${(opts.postLink)!}", postDataRecord, function(data){
                if(data.success){
                    $("#ugcSchoolInfoSurvey").html( template("T:success", {}) );

                    if(postDataRecord.recordId == specialRecordId){
                        setTimeout(function(){
                            location.reload();
                        }, 2000);
                    }
                }else{
                    alert(data.info);
                }
            });
        });

        $(document).on("click", 'input[type="text"]', function(){
            $(this).focus();
        });

        UgcRecordPopup();
    }
</script>
</#macro>
</@layout.page>