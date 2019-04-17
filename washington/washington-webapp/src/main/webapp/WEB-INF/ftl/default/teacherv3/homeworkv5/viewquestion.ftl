<#import "../../nuwa/layoutwithoutheadfooter.ftl" as shell />
<@shell.page show="main">
    <style type="text/css">
        body{
            background-color : #ffffff;
        }
    </style>
    <@sugar.capsule js=["ko"]/>
    <@app.script href="public/script/utils/remotequestion${jskid!''}.js"/>
    <@app.script href="public/script/compontents/homework/venusquestion${jskid!''}.js"/>
    <@sugar.capsule js=["plugin.venus-pre"] css=["plugin.venus-pre"] />

    <div style="padding: 10px;height: 400px;  overflow-y: auto;" id="viewQuestion">
        <!--ko if:$root.questions.length > 0-->
        <!--ko foreach:{data:questions,as:'question'}-->
        <ko-venus-question params="questions: [question],contentId:(question.id + '_' + $index()),formulaContainer:$root.formulaContainer,showAnalysis:true,showRightAnswer:true,showIntervene:$root.showIntervene"></ko-venus-question>
        <!--/ko-->
        <!--/ko-->
        <!--ko if:$root.questions.length == 0-->
         <!--ko text:'题目未找到，请检查参数是否正确'--><!--/ko-->
        <!--/ko-->
    </div>
<script type="text/javascript">
$(function(){
    function initPage(questions){
        var showIntervene = $17.getQuery("showIntervene");
        showIntervene = (showIntervene === "true");
        ko.applyBindings({
            questions : questions,
            formulaContainer : "viewQuestion",
            focusIndex  : 0,
            showIntervene : showIntervene
        }, document.getElementById("viewQuestion"));
    }

    var qids = $17.getQuery("qids");
    qids = qids.split(",");
    var questions = [];
    if(qids.length > 0){
        $17.QuestionDB.getQuestionByIds(qids,function(result){
            if(result.success){
                var questionMap = result.questionMap || {};
                for(var i = 0,iLen = qids.length; i < iLen; i++){
                    var questionId = qids[i];
                    if(!questionMap.hasOwnProperty(questionId)){
                        continue;
                    }
                    var questionObj = questionMap[questionId];
                    if(!questionObj || !$.isArray(questionObj.questions) || questionObj.questions.length === 0){
                        continue;
                    }
                    questions.push(questionObj.questions[0]);
                }
                initPage(questions);
            }else{
                initPage(questions);
            }
        });
    }else{
        initPage(questions);
    }
});
</script>
</@shell.page>