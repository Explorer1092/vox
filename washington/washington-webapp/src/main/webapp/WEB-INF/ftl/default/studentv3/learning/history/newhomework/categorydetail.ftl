<#import '../../../layout/layout.ftl' as temp>
<@temp.page pageName='homeworkreport'>
    <@sugar.capsule js=["ko"] css=["homeworkhistory.report"] />
<div class="t-center-container" id="contentHolder">
    <div class="breadcrumb" style="padding: 15px 0;">
        <span><a class="w-blue" href="/student/index.vpage">首页</a> &gt;</span>
        <span><a class="w-blue" data-bind="attr:{href:'/student/learning/history/newhomework/homeworkreport.vpage?subject=ENGLISH&homeworkId=' + homeworkId}">作业详情</a> &gt;</span>
        <span>答题详情</span>
    </div>
    <div class="h-historyBox">
        <div class="h-header" style="margin-bottom: 15px;">
            <div class="h-title-2">
                <span class="left-text"><!--ko text:categoryName--><!--/ko--> -- 答题详情</span>
            </div>
            <div class="jf-intro" style="display: none;" data-bind="visible:$root.needRecord && $root.needRecord()">
                <span class="hoverText">分数计算说明</span>
                <div class="hoverInfo">
                    <span class="jf-arrow"></span>
                    <p>1、本作业总成绩为所有题目平均分</p>
                    <p>2、等级计算关系</p>
                    <table>
                        <tr>
                            <td>A</td><td>B</td><td>C</td><td>D</td>
                        </tr>
                        <tr>
                            <td>100分</td><td>90分</td><td>75分</td><td>60分</td>
                        </tr>
                    </table>
                </div>
            </div>
        </div>
        <!--ko if:!imageLoading() && detailList && detailList().length > 0-->
        <!--ko if:$root.tongueTwister && $root.tongueTwister()-->
        <!--ko foreach:{data:detailList,as:'detail'}-->
        <!--ko foreach:{data:detail.data,as:'suDetail'}-->
        <div class="h-answerD-list">
            <div class="stemDetails" data-bind="text:suDetail.text">&nbsp;</div>
            <div class="stemDescribe">
                <span class="txt-green" data-bind="text: suDetail.appOralScoreLevel"></span>
            </div>
        </div>
        <!--/ko-->
        <!--/ko-->
        <!--/ko-->
        <!--ko ifnot:$root.tongueTwister && $root.tongueTwister()-->
        <!--ko foreach:{data:detailList,as:'detail'}-->
        <div class="h-answerD-list">
            <div class="stemDetails" data-bind="text:$root.getSentenceText(detail.sentences)">&nbsp;</div>
            <!--ko if:!detail.needRecord-->
            <div class="stemDescribe">
                <span data-bind="css:{'txt-red' : !!!detail.answerInfo,'txt-green' : !!detail.answerInfo},text: detail.answerResultWord"></span>
            </div>
            <!--/ko-->
            <!--ko if:detail.needRecord && detail.recordInfo-->
            <div class="stemDescribe" data-bind="text:'跟读得分：' + detail.recordInfo.score" data-title="score字段统一从后端取，有可能是分数，有可能显示等级"></div>
            <!--/ko-->
        </div>
        <!--/ko-->
        <!--/ko-->
        <!--/ko-->
        <!--ko if: !imageLoading() && !(detailList && detailList().length > 0)-->
        <div class="h-answerD-list">
            <div class="hd">
                暂无数据
            </div>
        </div>
        <!--/ko-->
        <!--ko if: imageLoading()-->
        <div class="t-learn-unit-list">
            <div class="l-skin-box" style="padding: 20px; text-align: center;">
                <img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…
            </div>
        </div>
        <!--/ko-->
    </div>
</div>
<#--<div id="jquery_jplayer_1" class="jp-jplayer"></div>-->
<script type="text/javascript">
    var conf = {
        detailUrl : "${detailUrl}"
    };
    $(function(){
        var viewModel = {
            imageLoading : ko.observable(true),
            homeworkId   : $17.getQuery("hid"),
            categoryName : ko.observable(""),
            needRecord   : ko.observable(false),
            tongueTwister : ko.observable(false),
            detailList   : ko.observableArray([]),
            getSentenceText : function(sentenceList){
                var sentenceText = "";
                if(!$.isArray(sentenceList)){
                    return sentenceText;
                }
                for(var t = 0,tLen = sentenceList.length; t < tLen; t++){
                    sentenceText += sentenceList[t].sentenceContent;
                }
                return sentenceText;
            },
            init        : function(){
                var vm = this;
                $.get(conf.detailUrl,function(data){
                    if(data.success){
                        vm.categoryName(data.categoryName);
                        vm.needRecord(data.needRecord || false);
                        vm.tongueTwister(data.tongueTwister || false);
                        vm.detailList(data.questionInfoMapper || []);
                        vm.imageLoading(false);
                    }else{
                        $17.alert(data.info || "获取数据失败");
                    }
                });
            }
        };
        viewModel.init();
        ko.applyBindings(viewModel,document.getElementById("contentHolder"));
    });
</script>
</@temp.page>