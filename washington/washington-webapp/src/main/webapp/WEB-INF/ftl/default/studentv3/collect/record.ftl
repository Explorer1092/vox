<#macro UGC_Record opts={
"title" : "调查问卷",
"recordId" : "",
"getLink" : "/ugc/loadugc.vpage",
"postLink" : "/ugc/saveugcanswer.vpage"
}>
    <#if (opts.getLink?has_content && opts.postLink?has_content)>
        <script id="T:STUDENT-UGC" type="text/html">
            <style>
                .dataCollection-popUps{}
                .dataCollection-popUps .hoot-box p{float:left;font-size:14px;line-height: 30px;}
                .dataCollection-popUps .hoot-box .c-2{cursor: pointer; width: 60px;}
                .dataCollection-popUps li{clear: both;color:#333;}
                .dataCollection-popUps .title{line-height: 45px;font-size:14px;}
                .dataCollection-popUps .number{border-radius: 6px;border:1px solid #ccc;width:120px;padding:5px 0;line-height: 19px;text-align: center; outline: none;}
                .dataCollection-popUps .title .select{ width: 80px;}
                .dataCollection-popUps .title select:focus, .dataCollection-popUps .title input:focus{ border-color: #fa7252; color: #fa7252; background-color: #fdf7f6;}
                .dataCollection-popUps .title .error{ border-color: #f00;}
                .dataCollection-popUps .title .sub{display:inline-block;border-radius: 6px;border:1px solid #fa7252;width:70px;padding:8px 0;line-height: 19px;text-align: center;color:#fa7252;background-color: #fdf7f6;}
                .dataCollection-popUps .w-checkbox{background: url(<@app.link href="public/skin/studentv3/images/publicbanner/checked-icon.png"/>) no-repeat;width: 12px;height: 12px;overflow: hidden;cursor: pointer;display: inline-block;*vertical-align: bottom;}
                .dataCollection-popUps .w-checkbox{background-position: 0 -27px;}
                .dataCollection-popUps .active .w-checkbox{background-position: 0 0;}
                .dataCollection-popUps .active .c-2{color:#57bd1b;}

                .dataCollection-popUps .sign-box input{display: none;}
                .dataCollection-popUps .sign-box .active input { display: inline-block;}
            </style>
            <%var input_placeholder = "请填写"%>
            <div class="dataCollection-popUps" id="ugcSchoolInfoSurvey">
                <ul>
                    <%for(var i = 0; i < items.length; i++){%>
                        <li id="questionId-<%=items[i].questionId%>">
                        <%if(items[i].questionType === "INPUT" ){%>
                            <p class="title"><%=(i+1)%>、<%==(items[i].questionName.replace(/#(INPUT|LONG_INPUT)#/g, ' <input type="text" class="number questionId-'+ items[i].questionId +'" placeholder="请填写" maxlength="32"/> '))%></p>
                        <%}%>

                        <%if(items[i].questionType == "SELECT"){%>
                            <%if(items[i].questionName.indexOf("#SIGNSELECT#") > -1){%>
                            <div class="title"><%==(i+1)%>、<%=(items[i].questionName.replace(/#SIGNSELECT#/g, ''))%></div>
                            <div class="hoot-box sign-box">
                                <%for(var b = 0, options = items[i].options; b < options.length; b++){%>
                                <span class="c-2 questionType-SIGNSELECT" data-type="<%=items[i].questionId%>" data-grade="<%=b%>">
                                    <%if(options[b].indexOf("#INPUT#") > -1){%>
                                        <% var intContent = ' <input type="text" class="number questionId-'+ items[i].questionId +'" value="" data-type="MULTISELECT" placeholder="'+ input_placeholder +'"/> '; %>
                                        <span class="w-checkbox"></span> <%==(options[b].replace(/#INPUT#/g, intContent))%>
                                    <%}else{%>
                                        <span class="w-checkbox"></span> <%==(options[b])%>
                                        <input type="hidden" class="longText questionId-<%=items[i].questionId%>" value="<%=options[b]%>" data-type="MULTISELECT"/>
                                    <%}%>
                                </span>
                                <%}%>
                            </div>
                            <%}else{%>
                                <%
                                var selectContent = ' <select class="number select questionId-'+ items[i].questionId +'"><option value="#请选择#">请选择</option>';
                                for(var b = 0, options = items[i].options; b < options.length; b++){selectContent += '<option value="'+ options[b] +'">'+ options[b] +'</option>'}
                                selectContent += '</select> ';
                                %>
                                <p class="title"><%=(i+1)%>、<%==(items[i].questionName.replace(/#SELECT#/g, selectContent))%></p>
                            <%}%>
                        <%}%>

                        <%if(items[i].questionType == "MULTISELECT"){%>
                            <p class="title"><%==(i+1)%>、<%=(items[i].questionName.replace(/#MULTISELECT#/g, ''))%></p>
                            <%for(var b = 0, options = items[i].options; b < options.length; b++){%>
                                <div class="hoot-box questionType-MULTISELECT" data-type="<%=items[i].questionId%>" data-grade="<%=b%>">
                                    <input type="hidden" class="questionId-<%=items[i].questionId%>" data-type="MULTISELECT" value="<%=options[b]%>"/>
                                    <p class="c-1">
                                        <span class="w-checkbox"></span>
                                    </p>
                                    <p class="c-2"><%=options[b]%></p>
                                </div>
                            <%}%>
                        <%}%>
                        </li>
                    <%}%>
                </ul>
            </div>
        </script>
        <script type="text/javascript">
            (function($){
                function UgcClazzPopup(){
                    $.get("${(opts.getLink)!}?recordId=${(opts.recordId)!}", {}, function(data){
                        if(data.success && !$17.isBlank(data.ugc)){
                            recordMain(data);
                        }
                    });
                }

                function recordMain(data){
                    var $unAnswerQuestions = data.ugc.unAnswerQuestions;
                    var $postData = {
                        source : "PC",
                        recordId : data.ugc.recordId,
                        answerMapList : []
                    };

                    for(var i = 0; i < $unAnswerQuestions.length; i++){
                        $postData.answerMapList.push({questionId : $unAnswerQuestions[i].questionId, answer : ""});
                    }

                    var statesHtml = {
                        state0 : {
                            focus: 1,
                            title : ( data.ugc.recordName ? data.ugc.recordName : "${(opts.title)!}" ),
                            position : { width: 570},
                            html : template("T:STUDENT-UGC", {items : $unAnswerQuestions}),
                            buttons: {"取消": false, "提交": true},
                            submit : function(e, v){
                                //发送关联请求
                                if(v){
                                    if( verInfo($postData) ){
                                        $.prompt.goToState('state1', true);
                                        return false;
                                    }

                                    App.postJSON("${(opts.postLink)!}", $postData, function(data){
                                        if(data.success){
                                            $17.alert("您的问卷已提交，感谢反馈！");
                                        }
                                    });
                                }
                            }
                        },state1: {
                            html:'<div id="serverDataInfo" style="text-align: center;">请填写完善！</div>',
                            buttons: {"知道了": 0 },
                            submit:function(e,v,m,f){
                                e.preventDefault();
                                $.prompt.goToState('state0');
                            }
                        }
                    };

                    $.prompt(statesHtml);

                    setTimeout(function(){
                        $("html, body").scrollTop(0);
                    }, 100);


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
                });

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
                            if( !$17.isBlank($id.eq(i).val()) ){
                                $content.push($id.eq(i).val());
                            }else{
                                $content = [];
                                break;
                            }
                        }

                        $content = $content.join("#");
                    }else{
                        if( !$17.isBlank($id.val()) && $id.val() != "#请选择#"){
                            $content = $id.val();
                        }
                    }

                    return $content;
                }

                $.extend({
                    UgcClazzPopup : UgcClazzPopup
                });
            }($));
        </script>
    </#if>
</#macro>