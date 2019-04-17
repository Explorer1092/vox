<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main" showNav="">
    <@sugar.capsule js=["ko","homework2nd"] css=["homeworkv3.homework"] />
    <div class="h-synExercise" id="pageContent">
        <div class="h-floatLayer-L" id="typesElement">

        </div>
    </div>
    <script id="t:形式列表" type="text/html">
        <%if(typeArr.length > 0){%>
        <ul>
            <%for(var z = 0;z < typeArr.length; z++){%>
            <li class="<%if(z == 0){%>active<%}%>" data-type="<%=typeArr[z].type%>"><a href="javascript:void(0)"><%=typeArr[z].typeName%></a></li>
            <%}%>
        </ul>
        <%}%>
    </script>
    <script id="t:应试题列表" type="text/html">
        <%for(var z = 0,zLen = contents.length; z < zLen; z++){%>
        <div class="h-set-homework examquestion" style="<%if(z > 4){%>display: none;<%}%>">
            <div class="seth-hd">
                <p class="fl">
                    <span><%=contents[z].questionType%></span>
                    <%if(contents[z].upImage){%>
                    <span><%=contents[z].difficultyName%></span>
                    <span class="noBorder">支持上传解答过程</span>
                    <%}else{%>
                    <span class="noBorder"><%=contents[z].difficultyName%></span>
                    <%}%>
                </p>
                <%if(contents[z].teacherAssignTimes > 0){%>
                <p class="fr"><span class="txtYellow">您已布置<%=contents[z].teacherAssignTimes%>次</span></p>
                <%}%>
            </div>
            <div class="seth-mn">
                <div class="testPaper-info" data-examid="<%=contents[z].questionId%>">

                </div>
            </div>
        </div>
        <%}%>
        <%if(contents.length > 5){%>
        <div class="t-dynamic-btn dynamicMoreClickBtn" data-tabtype="<%=type%>" data-nextstartindex="5">
            <a class="more" href="javascript:void(0);">展开更多</a>
        </div>
        <%}%>
    </script>
    <script id="T:EXAM_QUESTIONS" type="text/html">
        <div class="h-synExercise-main w-base" id="<%=type%>">
            <div class="w-base-title" style="background-color: #e1f0fc;">
                <h3><%=typeName%>（预计用时<%=minute%>分钟）</h3>
            </div>
            <%include('t:应试题列表')%>
        </div>
    </script>
    <script id="T:MENTAL" type="text/html">
        <div class="w-base" id="<%=type%>">
            <div class="w-base-title" style="background-color: #e1f0fc;">
                <h3><%=typeName%>（预计用时<%=minute%>分钟）</h3>
            </div>
            <div class="h-set-homework">
                <div class="seth-mn t-choose-Knowledge">
                    <table class="t-questionBox">
                        <tbody>
                            <%for(var z = 0,zLen = contents.length; z < zLen; z++){%>
                            <%if(z % 3 == 0){%>
                            <tr>
                            <%}%>
                                <td><div class="t-question"><%=replace__$$__(contents[z].questionContent)%></div></td>
                            <%if(((z+1) % 3 == 0) || (z == zLen - 1)){%>
                            </tr>
                            <%}%>
                            <%}%>
                        </tbody>
                    </table>
                    <div class="w-clear"></div>
                </div>
            </div>
        </div>
    </script>
    <script id="T:QUIZ" type="text/html">
        <div class="w-base" id="<%=type%>">
            <div class="w-base-title" style="background-color: #e1f0fc;">
                <h3><%=typeName%>（预计用时<%=minute%>分钟）</h3>
            </div>
            <div class="paperName"><%=title%></div>
            <div class="volumePeople">出卷人：<%=author%></div>
            <%include('t:应试题列表')%>
        </div>
    </script>
    <script id="T:SUBJECTIVE" type="text/html">
        <div class="w-base" id="<%=type%>">
            <div class="w-base-title" style="background-color: #e1f0fc;">
                <h3><%=typeName%>（预计用时<%=minute%>分钟）</h3>
            </div>
            <%for(var z = 0,zLen = contents.length; z < zLen; z++){%>
            <div class="w-base homeworkBox" data-examid="<%=contents[z].questionId%>" style="margin:15px;<%if(z > 4){%>display: none;<%}%>">

            </div>
            <%}%>
            <%if(contents.length > 5){%>
            <div class="t-dynamic-btn dynamicMoreClickBtn" data-tabtype="<%=type%>" data-lastindex="5">
                <a class="more" href="javascript:void(0);">展开更多</a>
            </div>
            <%}%>
        </div>
    </script>
    <script id="T:READRECITE" TYPE="text/html">
        <div class="h-synExercise-main w-base" id="<%=type%>">
            <div class="w-base-title" style="background-color: #e1f0fc;">
                <h3><%=typeName%>（预计用时<%=minute%>分钟）</h3>
            </div>
            <%for(var z = 0,zLen = contents.length; z < zLen; z++){%>
            <div class="h-set-homework examquestion" style="<%if(z > 4){%>display: none;<%}%>">
                <div class="seth-hd">
                    <p class="fl">
                        <span><%=contents[z].paragraphCName%></span>
                        <span class="noBorder"><%=contents[z].articleName%></span>
                    </p>
                </div>
                <div class="seth-mn">
                    <div class="testPaper-info" data-examid="<%=contents[z].questionId%>">

                    </div>
                </div>
            </div>
            <%}%>
            <%if(contents.length > 5){%>
            <div class="t-dynamic-btn dynamicMoreClickBtn" data-tabtype="<%=type%>" data-nextstartindex="5">
                <a class="more" href="javascript:void(0);">展开更多</a>
            </div>
            <%}%>
        </div>
    </script>
    <script type="text/javascript">
        var constantObj = {
            contents         : ${contents![]},
            imgDomain        : '${imgDomain!''}',
            domain           : '${requestContext.webAppBaseUrl}/',
            env              : <@ftlmacro.getCurrentProductDevelopment />,
            objectiveConfigType  : {
                EXAM            : "T:EXAM_QUESTIONS",
                MENTAL          : "T:MENTAL",
                UNIT_QUIZ       : "T:QUIZ",
                MID_QUIZ        : "T:QUIZ",
                END_QUIZ        : "T:QUIZ",
                PHOTO_OBJECTIVE : "T:SUBJECTIVE",
                VOICE_OBJECTIVE : "T:SUBJECTIVE",
                WORD_PRACTICE   : "T:EXAM_QUESTIONS",
                READ_RECITE     : "T:READRECITE"
            }
        };

        //模板辅助方法
        template.helper('replace__$$__', function (_questionContent) {
            return _questionContent ? _questionContent.replace(/__\$\$__/g,"(  )") : "";
        });



        $(function(){

            try{
                vox.exam.create(function(data){
                    if(!data.success){
                        $17.voxLog({
                            module: 'vox_exam_create',
                            op:'create_error'
                        });
                        $17.tongji('voxExamCreate','create_error',location.pathname);
                    }else{
                        constantObj.examInitComplete = true;
                    }
                });
            }catch(exception){
                constantObj.examInitComplete = false;
                $17.voxLog({
                    module: 'vox_exam_create',
                    op: 'examCoreJs_error',
                    errMsg: exception.message,
                    userAgent: (navigator && navigator.userAgent) ? navigator.userAgent : "No browser information"
                });
            }


            var typeArr = [];
            var _contents = constantObj.contents;
            var typeQuestionRef = {};
            var _html = "";
            for(var k = 0,kLen = _contents.length; k < kLen; k++){
                if($.inArray(_contents[k].type,typeArr) == -1){
                    typeArr.push({
                        type     : _contents[k].type,
                        typeName : _contents[k].typeName
                    });
                }
                var templateName = constantObj.objectiveConfigType[_contents[k].type];
                if(templateName){
                    if(templateName == "T:QUIZ"){
                       var  _papers = _contents[k].papers || [];
                        if(_papers.length > 0){
                            for(var s = 0,sLen = _papers.length; s < sLen; s++){
                                var _type = _contents[k].type + "_"  + s;
                                var questions = _papers[0].questions || [];
                                _html += template(templateName,{author : _papers[s].paperSource, title:_papers[s].title,contents : questions, minute : _papers[s].minutes || 0, type : _type,typeName:_contents[k].typeName});
                                typeQuestionRef[_type] = questions;
                            }
                        }
                    }else{
                        var totalTime = 0;
                        var _questions =  _contents[k].questions || [];
                        $.each(_questions,function(i,item){
                            totalTime += (+item.seconds || 0);
                        });
                        _html += template(templateName,{contents : _questions, minute : Math.ceil(totalTime/60),type : _contents[k].type,typeName : _contents[k].typeName});
                        typeQuestionRef[_contents[k].type] = _questions;
                    }
                }
            }
            $("#pageContent").append(_html);

            setTimeout(function(){
                $("div[data-examid]:visible").each(function(){
                    var examId = $(this).attr("data-examid");
                    if(!$17.isBlank(examId) && constantObj.examInitComplete){
                        var obj = vox.exam.render(this, 'normal', {
                            ids       : [examId],
                            imgDomain : constantObj.imgDomain,
                            env       : constantObj.env,
                            domain    : constantObj.domain
                        });
                    }else{
                        $(this).html('<div class="w-noData-block">如果遇到同步习题加载问题，建议使用猎豹浏览器重新打开网站，<a href="//cdn.17zuoye.com/download/17zuoye_liebao_20150624.exe" target="_blank" style="color: #39f;">点击下载</a></div>');
                    }
                });
            },300);


            if(typeArr.length > 1){
                $("#typesElement").html(template("t:形式列表",{typeArr : typeArr}));

                $("li","#typesElement").on("click",function(){
                    var $this = $(this);
                    if($this.hasClass("active")){
                        return false;
                    }else{
                        var dataType = $this.attr("data-type");
                        if(!$17.isBlank(dataType)){
                            $this.addClass("active").siblings("li").removeClass("active");
                            var _top = $("div[id^=" + dataType + "]:first").offset().top;
                            top.$('html, body').animate({scrollTop: _top}, 600);
                        }
                    }
                });

            }else{
                $("#typesElement").hide();
            }


            $("div.dynamicMoreClickBtn").on("click",function(){
                //加载更多
                var $this = $(this);

                //data-tabtype="<%=type%>" data-nextstartindex="4"

                var nextStartIndex = +$this.attr("data-nextstartindex");
                var hkTabType = $this.attr("data-tabtype");
                var _questionArr = typeQuestionRef[hkTabType];
                if(_questionArr && _questionArr.length > 0){
                    var endIndex = nextStartIndex + 4;
                    endIndex = endIndex < _questionArr.length ? endIndex : _questionArr.length;
                    $.each(_questionArr.slice(nextStartIndex,endIndex),function(i,item){
                        var examId = item.questionId;
                        var $node = $("div[data-examid='" + examId + "']").show();
                        $node.closest("div.examquestion").show();
                        if(!$17.isBlank(examId) && constantObj.examInitComplete){
                            var obj = vox.exam.render($node[0], 'normal', {
                                ids       : [examId],
                                imgDomain : constantObj.imgDomain,
                                env       : constantObj.env,
                                domain    : constantObj.domain
                            });
                        }else{
                            $node.html('<div class="w-noData-block">如果遇到同步习题加载问题，建议使用猎豹浏览器重新打开网站，<a href="//cdn.17zuoye.com/download/17zuoye_liebao_20150624.exe" target="_blank" style="color: #39f;">点击下载</a></div>');
                        }
                    });
                    $this.attr("data-nextstartindex",endIndex);
                    if(endIndex == _questionArr.length){
                        $this.hide();
                    }
                }
            });
        });
    </script>
</@shell.page>