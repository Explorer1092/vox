<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑模板" page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js" xmlns="http://www.w3.org/1999/html"></script>
<script src="https://cdn.bootcss.com/lodash.js/4.17.11/lodash.core.js"></script>
<style>
    [v-cloak] {
        display: none;
    }
</style>

<div id="box" v-cloak class="span9">
    <div class="form-horizontal">
        <h3>{{ name }}用户主动服务 <span v-if="filterFlag" style="color: red">已过滤用户未发音的词汇点评，建议点评本题其他作答记录。{{ sentence }}</span></h3>
        <div v-if="userAnswer != ''" class="well">
            <div class="control-group">
                <label class="control-label">{{ userAnswer.name }}：</label>
                <div v-if="userAnswer.type === 'video'">
                    <#--<video  class="pull-left" v-bind:src="userAnswer.value"  target="_blank" style="margin-left: 360px; height: 400px"></video>-->
                    <#--<video  class="pull-left" src="https://17zy-homework.oss-cn-beijing.aliyuncs.com/dubbing/prod/1548157751547_1548157756285.mp4"  target="_blank" style="margin-left: 360px; height: 400px"></video>-->
                    <video  class="pull-left"  preload="auto" autoplay="autoplay" loop="loop" muted="muted"  v-bind:src="userAnswer.value"
                            style="margin-left: 360px; height: 400px"></video>
                </div>
                <div v-else  class="controls">
                    <audio  class="pull-left" v-bind:src="userAnswer.value" controls target="_blank" style="margin-left:330px;"></audio>
                </div>

            </div>
        </div>
        <div class="well">
            <div class="control-group">
                <label class="control-label">学习情况总结：</label>
                <div class="controls">
                    <textarea v-model="summary" disabled style="width: 600px;height:100px;"></textarea>
                </div>
            </div>
        </div>
        <div class="well" v-if="pronList && pronList.length > 0">
            <template v-for="(item, i) in pronList">
                <div class="control-group">
                    <label class="control-label">关键词：</label>
                    <div class="controls">
                        <textarea v-model="item.keyword" disabled style="width: 600px;height:60px;"></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">发音点评：</label>
                    <div class="controls">
                        <textarea v-model="item.comment" disabled style="width: 600px;height:60px;"></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">发音点评音频：</label>
                    <div class="controls">
                        <audio v-if="item.audio" class="pull-right" v-bind:src="item.audio" controls target="_blank" style="padding-right: 550px;"></audio>
                    </div>
                </div>
            </template>
        </div>
        <div class="well" v-if="gramList && gramList.length > 0">
            <template v-for="(item, i) in gramList">
                <div class="control-group">
                    <label class="control-label">Level：</label>
                    <div class="controls">
                        <textarea v-model="item.level" disabled style="width: 600px;height:60px;"></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">语法点讲解：</label>
                    <div class="controls">
                        <textarea v-model="item.comment" disabled style="width: 600px;height:60px;"></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">语法点讲解音频：</label>
                    <div class="controls">
                        <audio v-if="item.audio" class="pull-right" v-bind:src="item.audio" controls target="_blank" style="padding-right: 550px;"></audio>
                    </div>
                </div>

            </template>

        </div>
        <div class="well" v-if="knowledgeList && knowledgeList.length > 0">
            <template v-for="(item, i) in knowledgeList">
                <div v-if="item.type === 'text'" class="control-group">
                    <label class="control-label">{{ item.key }}：</label>
                    <div class="controls">
                        <textarea v-model="item.value" disabled style="width: 600px;height:60px;"></textarea>
                    </div>
                </div>
                <div v-if="item.type === 'audio'" class="control-group">
                    <label class="control-label">{{ item.key }}：</label>
                    <div class="controls">
                        <#--<textarea v-model="item.value" style="width: 600px;height:60px;"></textarea>-->
                        <audio v-if="item.value" class="pull-right" v-bind:src="item.value" controls target="_blank" style="padding-right: 550px;"></audio>
                    </div>
                </div>
            </template>
        </div>
        <div style="margin-left: 600px">
            <span class="btn btn-success btn-large" @click="preview('${unitId!}','${lessonId!}','${aid!}')">预览</span>
            <span class="btn btn-info btn-large" @click="cancel">返回</span>
        </div>
    </div>
    <div id="myModal" class="modal" :class="{hide:!model,fade:!model}" tabindex="-1" role="dialog"
         aria-labelledby="myModalLabel" aria-hidden="true">
        <div class="modal-header">
            <button type="button" class="close" @click="closeModel">×</button>
            <h3 id="myModalLabel">用手机扫描二维码预览</h3>
        </div>
        <div class="modal-body">
            <div id="sharecode_box" style="text-align: center"></div>
        </div>
    </div>

    <div :class="{in:model,'modal-backdrop':model,fade:model}"></div>
</div>

<script type="text/javascript">
    var vm = new Vue({
        el: '#box',
        data: {
            model:false,
            qid:'',
            pronList:[],
            gramList:[],
            knowledgeList:[],
            summary:"",
            userId:"",
            userAnswer:"",
            filterFlag:false,
            sentence:"",
        },
        methods: {
            cancel:function () {
                var method = "${method!}"
                console.log("cancel method: " + method)
                var hostName = window.location.host;
                console.log("http://" + hostName +"/chips/user/question/indexV2.vpage?userId=" + getUrlParam("userId")
                        + "&bookId=" + getUrlParam("bookId") + "&unitType=" + getUrlParam("unitType") + "&unitId=" + getUrlParam("unitId"));
                window.location.href = "http://" + hostName +"/chips/user/question/indexV2.vpage?userId=" + getUrlParam("userId")
                        + "&bookId=" + getUrlParam("bookId") + "&unitType=" + getUrlParam("unitType") + "&unitId=" + getUrlParam("unitId");
            },
            closeModel: function () {
                var _this = this;
                _this.model = false;
            },
            preview:function (unitId, lessonId, aid) {
                var _this = this;
                qid = _this.qid,
                userId = _this.userId,

                $.post('${requestContext.webAppContextPath}/chips/ai/active/service/userTemplateSave.vpage', {
                    pronJson: JSON.stringify(_this.pronList),
                    gramJson: JSON.stringify(_this.gramList),
                    knowledgeJson: JSON.stringify(_this.knowledgeList),
                    qid:qid,
                    userId:userId,
                    summary:_this.summary,
                    bookId:getUrlParam("bookId"),
                    unitId: getUrlParam("unitId"),
                    lessonId: lessonId,
                    aid : aid,
                    qname : getUrlParam('questionName')
                }, function (res) {
                    if (res.success) {
                        var url = res.url;
                        console.log(url)
                        var codeImgSrc = "https://www.17zuoye.com/qrcode?m=" + encodeURIComponent(url);

                        var imgObj = new Image();
                        imgObj.src = codeImgSrc;
                        imgObj.style.width = "200px";
                        imgObj.style.height = "200px";
                        $("#sharecode_box").html('')
                        $("#sharecode_box").append(imgObj);
                        _this.model = true;
                    } else {
                        alert(res.info);
                    }
                });

            },
        },
        created: function () {
            var _this = this;
            var qid = "${qid!}";
            console.log("qid")
            console.log(qid)
            $.get('${requestContext.webAppContextPath}/chips/ai/active/service/queryUserTemplate.vpage', {
//                qid: qid
                qid: getUrlParam("qid"),
                userId: getUrlParam("userId"),
                aids: getUrlParam("aids"),
                unitId:getUrlParam("unitId"),
                lessonId:getUrlParam("lessonId"),
                aid:getUrlParam("aid"),
            }, function (res) {
                if(res.success) {
                    _this.pronList = res.userTemplate.pronList||'';
                    _this.gramList = res.userTemplate.grammarList||'';
                    _this.knowledgeList = res.userTemplate.knowledgeList||'';
                    _this.summary = res.userTemplate.learnSummary;
                    _this.qid = res.userTemplate.qid;
                    _this.userId=res.userTemplate.userId;
                    if(res.userAnswer != null){
                        _this.userAnswer = res.userAnswer;
                    }
                    _this.filterFlag = res.filterFlag;
                    _this.sentence = res.sentence;
                    console.log("keywordSet", res.keywordSet)
                    console.log("sentence", res.sentence)
                    console.log("filterKeywordSet", res.filterKeywordSet)
                    console.log(_this.summary)
                }
            });
        }
    });

    function getUrlParam (name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return decodeURIComponent(r[2]); return null;
    }

</script>

</@layout_default.page>