<#import "../module.ftl" as temp />
<@temp.pagecontent mainmenu="classroom_ask" submenu="myquestion">
<@sugar.capsule js=["ueditor"] />

<p class="s-red s-magB-10"><a class="s-btn" href="javascript:void (0)" id="addQuestionBtn"><span class="s-all s-add"></span>添加题目</a></p>

<div id="addQuestion" class="s-exercise-box s-exercise-up-box" style="padding: 25px 0; display: none;" question-index="-1"></div>
<div id="myQuestionList"><#--题的列表--></div>
<!-- 加载编辑器的容器 -->
<div id="initDiv" data-token="true" style="width: 1px; height: 1px; overflow: hidden; position: relative;">
    <script id="container" name="content" type="text/plain">请填写内容</script>
</div>
<script type="text/html" id="t:题目添加或编辑">
    <div class="se-check s-textarea topicContent">
        <%==question.topicContent%>
    </div>
    <div class="se-ask">
        <ul>
            <li class="s-blue pd s-ft-large" style="padding: 6px 0 6px 119px;">正确答案</li>
            <% var count = 0;%>
            <%
              for(var key in question.options){
                count = count + 1;
            %>
                <li class="answer <%if(key == question.answer){%> hover <%}%>">
                    <i class="s-radio <%if(key == question.answer){%> s-radio-current <%}%>" data-value="<%=key%>"></i>
                    <span class="answerOption" style="margin: 0 6px 0 0;"><%=key%>.</span>
                    <div class="s-textarea"><div class="s-int"><%==question.options[key]%></div></div>
                    <%if(count > 2){%>
                    <span class="deleteAnswerBtn"><a href="javascript:void(0);">删除</a></span>
                    <%}%>
                </li>
            <%
              }
            %>
            <li class="pd">
                <%if(count == 4){%>
                   <a id="addAnswerOption" style="display: none;" class="s-btn-mini-blue">+继续添加选项</a>
                <%}else{%>
                   <a id="addAnswerOption" class="s-btn-mini-blue">+继续添加选项</a>
                <%}%>
            </li>
        </ul>
    </div>
    <div class="s-comment-btn">
        <a class="s-btn-big s-btn-big-green" id="cacelBtn" href="javascript:void (0)">取消</a>
        <a class="s-btn-big" data-question_index="<%=questionIndex%>" data-questionid="<%=question.id%>" id="submitBtn" href="javascript:void (0)">提交</a>
    </div>
</script>

<script type="text/html" id="t:答案选项">
    <li class="answer">
        <i class="s-radio" data-value="<%=answerOption%>"></i>
        <span class="answerOption" style="margin: 0 2px 0 0;"><%=answerOption%>.</span>
        <div class="s-textarea"><div class="s-int">我是一个答案</div></div>
        <span class="deleteAnswerBtn"><a href="javascript:void(0);">删除</a></span>
    </li>
</script>
<script type="text/html" id="t:题目列表">
<%if(questionPage.totalPages > 0){%>
    <%
        var questionIndex = 1;
        for(var i = 0; i < questionPage.content.length; i++){
          questionIndex =  questionPage.number * questionPage.size + (i + 1);
    %>
    <div class="s-exercise-box s-exercise-up-box s-magB-10">
        <div data-questionid="<%=questionPage.content[i].id%>" class="se-l s-fl-right" data-index="<%=i%>">
            <#--扫描答案参数说明：qId:题的ID，questionIndex:当前题是第几条记录-->
            <a class="s-btn s-btn-red" href="/teacher/smartclazz/questionscan.vpage?clazzId=<%=clazzId%>&qId=<%=questionPage.content[i].id%>&questionIndex=<%=questionIndex%>&totalEle=<%=questionPage.totalElements%>&subject=${curSubject!}">扫描答案</a>
            <a style="padding: 6px 8px;" class="s-btn s-btn-green addClazzQuestionRef" href="javascript:void(0);">添加至其他班级  </a>&nbsp;
            <a class="s-btn s-btn-green editQuestionBtn" href="javascript: void(0);">编辑</a>
            <a class="s-btn s-btn-green deleteQuestion" href="javascript: void(0);">删除</a>
            <%if(i == 0){%>
            <a class="s-btn-mini-blue s-fl upOrDown" href="javascript: void(0)">
                收起
                <span class="s-all s-arrow-blue"></span>
            </a>
            <a class="s-btn-mini-blue s-fl upOrDown" style="display: none;" href="javascript: void(0)">
                展开
                <span class="s-all s-arrow-black"></span>
            </a>
            <%}else{%>
            <a class="s-btn-mini-blue s-fl upOrDown" style="display: none;" href="javascript: void(0)">
                收起
                <span class="s-all s-arrow-blue"></span>
            </a>
            <a class="s-btn-mini-blue s-fl upOrDown" href="javascript: void(0)">
                展开
                <span class="s-all s-arrow-black"></span>
            </a>
            <%}%>
        </div>
        <div id="<%=questionPage.content[i].id%>" class="se-r s-fl-left">
            <ul>
                <li><%==questionPage.content[i].topicContent%></li>
                <%for(var key in questionPage.content[i].options){%>
                <li class="s-answer" <%if(i != 0){%>style="display:none;"<%}%>><%=key%>. <%==questionPage.content[i].options[key]%></li>
                <%}%>
            </ul>
        </div>
        <div class="s-clear"></div>
    </div>
    <div question-index="<%=i%>" class="s-exercise-box s-exercise-up-box" style="padding: 25px 0; display: none;"></div>
    <%}%>
    <div class="t-show-box">
        <div class="w-turn-page-list">
            <%if(!questionPage.first){%>
            <a v="prev" href="javascript:void(0);" class="enable back questionListPage" style="" data-page="<%=questionPage.number%>"><span>上一页</span></a>
            <%}%>
            <a href="javascript:void(0);" class="this"><span id="currentPage"><%=(questionPage.number+1)%></span></a>
            <span>/</span>
            <a href="javascript:void(0);" class="total"><span id="totalPage"><%=(questionPage.totalPages)%></span></a>
            <%if(!questionPage.last){%>
            <a v="next" href="javascript:void(0);" class="enable next questionListPage" style="" data-page="<%=(questionPage.number+2)%>"><span>下一页</span></a>
            <%}%>
        </div>
    </div>
<%}else{%>
    <div class="s-exercise-box s-exercise-up-box emptyQuestion" style="padding: 25px 0;">
        <div class="se-add">
            <p>
                <span class="s-fl-right"><%=clazzName%> 还没有题目哦<br/>
                    请点击“添加题目”按钮进行添加</span>
                <i class="s-all s-arrow-big s-fl-left"></i>
            <div class="s-clear"></div>
            </p>
        </div>
    </div>
<%}%>
</script>

<script type="text/html" id="t:添加班级关系">
    <div class="t-changeclass-alert">
        <div class="class" style="margin-top: 20px;">
            <ul class="data-selectContentList">
                <%
                  if(clazzList != null && clazzList.length > 0){
                     for(var j = 0; j < clazzList.length; j++){
                %>
                    <li style="cursor: pointer;" data-clazzid="<%=clazzList[j].id%>" class="noclazzref">
                        <span class="w-checkbox"></span>
                        <%=clazzList[j].clazzName%>
                    </li>
                <%
                    }
                  }else{
                %>
                    <li style="cursor: pointer;" style="">
                        没有相关班级
                    </li>
                <%}%>
            </ul>
        </div>
    </div>
</script>

<script type="text/javascript">
    //配置UEditor的图片上传地址
    window.UEDITOR_CONFIG.serverUrl = '/uploadfile/uploadueditorimg.vpage';

   $(function(){
       $17.voxLog({
           module: "smartclazz-myquestion",
           op    : "smartclazz-myquestion-load"
       });

       $17.tongji("互动课堂-课堂提问");
       $17.tongji("互动课堂-课堂提问-我的问题");

       var ue = UE.getEditor('container', {
           toolbars: [
               ['bold', 'italic', 'underline','|','subscript','superscript','|','simpleupload','matheq']
           ],
           initialFrameWidth : 730,
           initialFrameHeight: 100,
           autoHeightEnabled: true,
           autoFloatEnabled: true,
           elementPathEnabled:false,
           wordCount : false,
           maximumWords:1000
       });
       ue.ready(function(){
           //阻止工具栏的点击向上冒泡
           $(this.container).click(function(e){
               e.stopPropagation();
           });
       });


       // 富文本框初始化的DIV绑定click
       $("#initDiv").on("click",function(){
           if($(this).attr("data-token") == "true"){
               return false;
           }
           var $target = $(this);
           // data-token 拿到令牌属性
           $target.attr("data-token","true");
           var content = $target.html();
           var currentParnet = ue.container.parentNode.parentNode;
           var currentContent = ue.getContent();
           if($17.isBlank($.trim(ue.getContentTxt())) && $("img",$(currentContent)).length <= 0){
               currentContent = "无";
           }
           $target.empty().append(ue.container.parentNode);
           ue.reset();
           setTimeout(function(){
               ue.setContent(content);
           },200);
           $(currentParnet).removeAttr("data-token").html(currentContent);
           return false;
       });



       var answerOptions = ['A','B','C','D'];

       //题的列表
       var questionList = {
           questionPage : {},
           changePage : function(pageNo){
               if(!$17.isNumber(pageNo)){
                   pageNo = 1;
               }
               App.postJSON("/teacher/smartclazz/getmyquestion.vpage",
                       {clazzId:"${clazz.id}",pageNo : pageNo, subject : "${curSubject!}"},
                       function(data){
                   if(data.success){
                       questionList.questionPage = data.questionPage;
                       $("#myQuestionList").empty().html(template("t:题目列表", {questionPage : data.questionPage, clazzId:"${clazz.id}", clazzName:"${clazz.formalizeClazzName()}"}));
                   }else{
                       $17.alert(data.info);
                   }
               });
           },
           init : function(){
               questionList.changePage();

               //收起和展开
               $("a.upOrDown").die().live("click",function(){
                   var $this = $(this);
                   $this.hide();
                   $this.siblings("a.upOrDown").show();

                   var questionId = $this.parent().attr("data-questionid");
                   if(!$17.isBlank(questionId)){
                       $this.children("span").each(function(){
                           //展开 准备隐藏
                           if($(this).hasClass("s-arrow-black")){
                               $("li.s-answer","#" + questionId).show("slow");
                           }else{
                               $("li.s-answer","#" + questionId).hide("slow");
                           }
                       });
                   }
               });

               //编辑
                $("a.editQuestionBtn").die().live("click",function(){
                    $17.tongji("互动课堂-课堂提问-我的问题-编辑");
                    var $this = $(this);
                    var questionIndex = $this.parent().attr("data-index");
                    if($17.isBlank(questionIndex)){return;}
                    if(questionIndex < 0){return;}
                    if(questionList.questionPage.length < 1){return;}
                    if(questionList.questionPage.content.length > 0 && questionList.questionPage.content.length > questionIndex ){
                        var $currentParent = $("div[question-index=" + questionIndex + "]");
                        addOrEdit.chQuestion($currentParent,questionIndex);
                    }
                });


               //删除班级与题的关系
               $("a.deleteQuestion").die().live("click",function(){
                   $17.tongji("互动课堂-课堂提问-我的问题-删除");
                   var $this = $(this);
                   var questionId = $this.parent().attr("data-questionid");
                   if($17.isBlank(questionId)){return false;}
                   if($this.isFreezing()){
                       return false;
                   }
                   $.prompt('<p class="silverInfo jqicontent" style="text-align: center;">题目删除后将不可恢复，您确定要删除吗？</p>',{
                       title:"删除提示",
                       buttons : {"取消" : false , "确定" : true },
                       submit  : function(e,v,m,f){
                           if(v){
                               $this.freezing();
                               App.postJSON("/teacher/smartclazz/deleteclazzquestionref.vpage",{clazzId : "${clazz.id}", questionId : questionId, subject : "${curSubject}"},function(data){
                                   alert(data.info);
                                   questionList.changePage();
                               });
                           }else{
                               $this.thaw();
                               $.prompt.close();
                           }
                       }
                   });

               });

               //选择班级
               $("li.noclazzref").die().live("click", function(){
                    $(this).find(".w-checkbox").toggleClass("w-checkbox-current");
               });

               //把题添加其他班级
               $("a.addClazzQuestionRef").die().live("click",function(){
                   $17.tongji("互动课堂-课堂提问-我的问题-添加至其他班级");
                   var $this = $(this);

                   if($this.isFreezing()){
                       return false;
                   }
                   $this.freezing();
                   var questionId = $this.parent().attr("data-questionid");
                   App.postJSON('/teacher/smartclazz/findclazzquestionref.vpage?rd=' + Math.random(), {questionId : questionId}, function(data){
                       if(data.success){
                           $.prompt(template("t:添加班级关系",{clazzList : data.noRefClazz}),{
                               title : '系统提示',
                               buttons : {"取消" : false , "确定" : true },
                               submit  : function(e,v,m,f){
                                   if(v){
                                       e.preventDefault();
                                       var clazzIds = [];
                                       $("li.noclazzref .w-checkbox-current").each(function(){
                                           clazzIds.push($(this).parent().attr("data-clazzid"));
                                       });
                                       if(clazzIds.length < 1){
                                           $.prompt.close();
                                       }else{
                                           App.postJSON('/teacher/smartclazz/saveclazzquestionref.vpage',
                                                   {questionId : questionId, clazzIds : clazzIds.toString(), subject : "${curSubject}"},
                                                   function(data){
                                                       $17.alert(data.info);
                                                       $this.thaw();
                                                   });
                                       }
                                   }else{
                                       $.prompt.close();
                                       $this.thaw();
                                   }
                               },
                               close : function(){
                                   $this.thaw();
                               }
                           });
                       }else{
                           $17.alert(data.info);
                           $this.thaw();
                       }
                   });
               });

               //上一页|下一页
               $("a.questionListPage").die().live("click",function(){
                    questionList.changePage($(this).attr("data-page"));
               });
           }
       };
       questionList.init();


       //试题的添加和编辑
       var addOrEdit = {
           lastTargetIndex : '',  //上次题的下标
           currentTargetIndex : '',//本次题的下标
           btnBindEvent : function(){

               $('div.s-textarea').die().live("click", function(e){
                   var $target = $(this);
                   // data-token 拿到令牌属性
                   $target.attr("data-token","true");
                   var content = $target.html();
                   var currentParnet = ue.container.parentNode.parentNode;
                   var currentContent = ue.getContent();
                   if($17.isBlank($.trim(ue.getContentTxt())) && $("img",$(currentContent)).length <= 0){
                       currentContent = "无";
                   }
                   $target.empty().append(ue.container.parentNode);
                   ue.reset();
                   addOrEdit.currentTargetIndex = $target.parent().attr("question-index");
                   setTimeout(function(){
                       ue.setContent(content);
                       //如果是topicContent获得,被认为是换题编辑或添加
                       if($target.hasClass("topicContent") && addOrEdit.currentTargetIndex != addOrEdit.lastTargetIndex){
                           addOrEdit.lastTargetIndex = addOrEdit.currentTargetIndex;
                           $("div[question-index]:visible").each(function(index){
                               if($(this).attr("question-index") != addOrEdit.lastTargetIndex){
                                   $(this).empty().hide("slow");
                               }
                           });
                       }
                   },200);
                   $(currentParnet).removeAttr("data-token").html(currentContent);

               });

               //录题 取消
               $("#cacelBtn").die().live("click",function(){
                   $("#addQuestion").hide("slow");
                   $("#addQuestionBtn").show();
                   $("#initDiv").trigger("click");
                   $(this).closest("div[question-index]").hide("slow");
               });

               //继续添加选项
               $("#addAnswerOption").die().live("click", function(){
                   var $this = $(this);
                   var answerCnt = $("li.answer").length;
                   if(answerCnt >= 4){
                       return;
                   }
                   $this.parent().before(template("t:答案选项",{answerOption : answerOptions[answerCnt]}));
                   if((answerCnt + 1) >= 4){
                       $this.hide();
                   }
               });

               //删除答案
               $("span.deleteAnswerBtn").die().live("click",function(){
                   var $this = $(this);
                   var dataToken = $this.siblings("div.s-textarea").attr("data-token");
                   if(!$17.isBlank(dataToken) && dataToken == "true"){
                       //说明富文本框在此处,选把富文本框移走，再删除
                       $("div.s-textarea.topicContent").trigger("click");
                   }
                   setTimeout(function(){
                       $this.closest("li").remove();
                       var $answerLi = $("li.answer");
                       $answerLi.each(function(index){
                           var spanAnswer = $(this).find("span.answerOption");
                           spanAnswer.text(answerOptions[index]);
                           $(this).find("i.s-radio").attr("data-value",answerOptions[index]);
                       });
                       if($answerLi.length < 4){
                           $("#addAnswerOption").show();
                       }else{
                           $("#addAnswerOption").hide();
                       }
                   },200);
               });

               //题的正确答案
               var $target = $("li.answer .s-radio");
               $target.die().live("click", function(){
                   var $self = $(this);
                   var $parent = $("li.answer");

                   $target.removeClass("s-radio-current");
                   $self.addClass("s-radio-current");
                   $parent.removeClass("hover");
                   $self.parent().addClass("hover");
               });


               //录题 提交
               $("#submitBtn").die().live("click",function(){
                   $17.tongji("互动课堂-课堂提问-我的问题-提交");
                   var $parentDiv = $(this).closest("div[question-index=" + $(this).attr("data-question_index") + "]");
                   //题干内容
                   var topicContent = '';
                   var $topicContent = $parentDiv.find("div.topicContent");
                   if($topicContent.attr("data-token") == "true"){
                       topicContent = ue.getContent();
                   }else{
                       //移除子元素div.s-int
                       if($("div.s-int",$topicContent).length > 0){
                           topicContent = $topicContent.children("div.s-int").html();
                       }else{
                           topicContent = $topicContent.html();
                       }
                   }

                   var $answerOption = $parentDiv.find("li.answer");
                   if($answerOption.length == 0){
                       $17.alert("请填写答案选项");
                       return false;
                   }
                   var answerOption = {};
                   var anserVal; //答案
                   $answerOption.each(function(index){
                       var $this = $(this);
                       var $op = $this.find("i.s-radio");
                       var $div = $this.find("div.s-textarea");
                       if($div.attr("data-token") == "true"){
                           answerOption[$op.attr("data-value")] = ue.getContent();
                       }else{
                           //如果有div.s-int说明没有修改过选项
                           if($("div.s-int",$div).length > 0){
                               answerOption[$op.attr("data-value")] = $div.children("div.s-int").html();
                           }else{
                               answerOption[$op.attr("data-value")] = $div.html();
                           }
                       }
                       if($op.hasClass("s-radio-current")){
                           anserVal = $op.attr("data-value");
                       }
                   });

                   if($17.isBlank(anserVal)){
                       $17.alert("请选择正确答案");
                       return false;
                   }
                   var questionId = "";
                   if(!$17.isBlank($(this).attr("data-questionid"))){
                       questionId = $(this).attr("data-questionid");
                   }

                   var $submitBtn = $(this);
                   if($submitBtn.isFreezing()){
                       return false;
                   }

                   $submitBtn.freezing();

                   var postData= {
                       id           : questionId,
                       clazzId      : "${clazz.id}",
                       topicContent : topicContent,
                       answer       : anserVal,
                       options      : answerOption,
                       questionType : '',
                       subject      : "${curSubject!}"
                   };
                   App.postJSON("/teacher/smartclazz/saveSmartClazzQuestion.vpage",postData,function(data){
                       $submitBtn.thaw();
                       alert(data.info);
                       $("#addQuestion").hide("slow");
                       if(data.success){
                           $("#initDiv").trigger("click");
                           questionList.changePage();
                           // window.location.reload();
                       }
                   });
                   return false;
               });

           },
           chQuestion : function($elem,questionIndex){
               var question = {};
               if(questionIndex == -1){
                   // 表示要新添加题目
                   question = {
                       id: "",
                       topicContent: "请填写题干内容",
                       answer: "A",
                       options: {
                           "A": "我是一个答案",
                           "B": "我是一个答案",
                           "C": "我是一个答案",
                           "D": "我是一个答案"
                       },
                       questionType: ""
                   };
               }else{
                   // 表示编辑题目
                   question = questionList.questionPage.content[questionIndex];
               }
               var html = template("t:题目添加或编辑",{question : question, questionIndex : questionIndex});
               $elem.html(html);
               addOrEdit.btnBindEvent();
               $elem.show();
               $("div.topicContent",$elem).trigger("click");

           },
           init : function(){
               //添加题目
               $("#addQuestionBtn").on("click",function(){
                   $17.tongji("互动课堂-课堂提问-我的问题-添加题目");
                   $("div.emptyQuestion").hide();

                   addOrEdit.chQuestion($("#addQuestion"),-1);
               });
           }
       };
       addOrEdit.init();

       <#if !ftlmacro.devTestStagingSwitch>
          $17.voxLog({
              module: "smartclazz_iwen",
              op : "click_smartclazz_iwen"
          });
       </#if>

   });
</script>
</@temp.pagecontent>



