<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑长期课" page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<#--<script src="${requestContext.webAppContextPath}/public/js/bootstrap/bootstrap.min.js"></script>-->
<#--<link href="${requestContext.webAppContextPath}/public/js/bootstrap/bootstrap.min.css" rel="stylesheet">-->
<style>
    [v-cloak] {
        display: none;
    }

    .wd600 {
        width: 600px !important;
    }
</style>

<div id="box" v-cloak class="span9">
    <div class="form-horizontal">
        <h3 class="h3 text-center"><#if id?? && id != ''>编辑<#else>添加</#if></h3>
        <input type="hidden" v-model="dataPrimary.id">
        <div class="control-group">
            <label class="control-label">教材名：</label>
            <div class="controls">
                <select v-model="dataPrimary.bookId" @change="getUnitTypes">
                    <option value="">请选择</option>
                    <option v-for="option in dataList.bookList" v-bind:value="option.value" placeholder="教材">{{ option.desc }}</option>
                </select>
            </div>
        </div>
        <div class="control-group">
            <label for="" class="control-label">类型：</label>
            <div class="controls">
                <select v-model="dataList.unitType" @change="getUnits">
                    <option value="">请选择</option>
                    <option v-bind:value="item.value" v-for="item in dataList.unitTypeList">{{ item.desc }}</option>
                </select>
            </div>
        </div>
        <div class="control-group">
            <label for="" class="control-label">Unit：</label>
            <div class="controls">
                <select v-model="dataPrimary.unitId">
                    <option value="">请选择</option>
                    <#--<option v-bind:value="item.id" v-for="item in dataList.unitList">{{ item.jsonData.name }}</option>-->
                    <option v-bind:value="item.id" v-for="item in dataList.unitList">{{ item.customName }}</option>
                </select>
            </div>
        </div>
        <hr>
        <div class="well">
            <h4>今日对话第一句</h4>
            <div class="control-group">
                <label class="control-label">点评文案：</label>
                <div class="controls">
                    <textarea v-model="dataJson.firstCommentDesc" placeholder="点评文案"
                              style="width: 600px;height:60px;"></textarea>
                <#--<span style="color: #bbbbbb;font-size: 14px ;">注：需要换行的结尾加“&lt;br&gt;”</span>-->
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">音频URL：</label>
                <div class="controls">
                    <input v-model="dataJson.firstAudioUrl" type="text" class="wd600" placeholder="音频URL">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">语法点讲解：</label>
                <div class="controls">
                    <input v-model="dataJson.firstGrammaticalExplanation " type="text" class="wd600" placeholder="语法点讲解">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">知识加油站：</label>
                <div class="controls">
                    <textarea v-model="dataJson.firstKnowledgeStation" placeholder="知识加油站"
                              style="width: 600px;height:60px;"></textarea>
                <#--<span style="color: #bbbbbb;font-size: 14px ;">注：需要换行的结尾加“&lt;br&gt;”</span>-->
                </div>
            </div>
        </div>

        <div class="well">
            <h4>今日对话第二句</h4>
            <div class="control-group">
                <label class="control-label">点评文案：</label>
                <div class="controls">
                    <textarea v-model="dataJson.secondCommentDesc" placeholder="点评文案"
                              style="width: 600px;height:60px;"></textarea>
                <#--<span style="color: #bbbbbb;font-size: 14px ;">注：需要换行的结尾加“&lt;br&gt;”</span>-->
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">音频URL：</label>
                <div class="controls">
                    <input v-model="dataJson.secondAudioUrl" type="text" class="wd600" placeholder="音频URL">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">语法点讲解：</label>
                <div class="controls">
                    <input v-model="dataJson.secondGrammaticalExplanation" type="text" class="wd600" placeholder="语法点讲解">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">知识加油站：</label>
                <div class="controls">
                    <textarea v-model="dataJson.secondKnowledgeStation" placeholder="知识加油站"
                              style="width: 600px;height:60px;"></textarea>
                <#--<span style="color: #bbbbbb;font-size: 14px ;">注：需要换行的结尾加“&lt;br&gt;”</span>-->
                </div>
            </div>
        </div>
        <div class="well">
            <h4>今日对话第三句</h4>
            <div class="control-group">
                <label class="control-label">点评文案：</label>
                <div class="controls">
                    <textarea v-model="dataJson.thirdCommentDesc" placeholder="点评文案"
                              style="width: 600px;height:60px;"></textarea>
                <#--<span style="color: #bbbbbb;font-size: 14px ;">注：需要换行的结尾加“&lt;br&gt;”</span>-->
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">音频URL：</label>
                <div class="controls">
                    <input v-model="dataJson.thirdAudioUrl" type="text" class="wd600" placeholder="音频URL">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">语法点讲解：</label>
                <div class="controls">
                    <input v-model="dataJson.thirdGrammaticalExplanation" type="text" class="wd600" placeholder="语法点讲解">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">知识加油站：</label>
                <div class="controls">
                    <textarea v-model="dataJson.thirdKnowledgeStation" placeholder="知识加油站"
                              style="width: 600px;height:60px;"></textarea>
                <#--<span style="color: #bbbbbb;font-size: 14px ;">注：需要换行的结尾加“&lt;br&gt;”</span>-->
                </div>
            </div>
        </div>

        <hr>
        <div class="text-center">
            <span class="btn btn-info btn-large" @click="preview">预览</span>
            <span class="btn btn-success btn-large" @click="save">保存</span>
        </div>
    </div>

    <!-- Modal -->
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
            previewStatus: false,
            model: false,
            dataJson:{
                firstCommentDesc: '',
                firstAudioUrl: '',
                firstGrammaticalExplanation: '',
                firstKnowledgeStation: '',
                secondCommentDesc: '',
                secondAudioUrl: '',
                secondGrammaticalExplanation: '',
                secondKnowledgeStation: '',
                thirdCommentDesc: '',
                thirdAudioUrl: '',
                thirdGrammaticalExplanation: '',
                thirdKnowledgeStation: '',
            },
            dataPrimary:{
                id:'',
                bookId:"",
                unitId:""
            },
            dataList:{
                unitType:"",
                bookList: [],
                unitTypeList:[],
                unitList:[]
            },

        },

        methods: {
            save: function () {
                var _this = this;
                $.post('${requestContext.webAppContextPath}/chips/ai/todaylesson/officialDetail.vpage', {
                    dataJson: JSON.stringify(_this.dataJson),
                    id:_this.dataPrimary.id,
                    bookId:_this.dataPrimary.bookId,
                    unitId:_this.dataPrimary.unitId,

                }, function (res) {
                    if (res.success) {
                        _this.previewStatus = true;
                        alert("保存成功");
                    } else {
                        alert(res.info);
                    }
                });
            },
            preview: function () {
                var _this = this;
                if (_this.previewStatus) {
                    var hostName = window.location.host;
                    var _map = {
                        'admin.test.17zuoye.net': 'wechat.test.17zuoye.net',
                        'admin.17zuoye.net': 'wechat.17zuoye.com',
                        'admin.dc.17zuoye.net': 'wechat.17zuoye.com',
                        'admin.staging.17zuoye.net': 'wechat.staging.17zuoye.net',
                    }

                    hostName = _map[hostName] ? _map[hostName] : hostName;

                    if (hostName.indexOf('8085') > -1) {
                        hostName = hostName.replace(/8085/g, "8180")
                    }
                    var url = "http://" + hostName + "/chips/center/todaystudynormal.vpage?preview=1&bookId=" + _this.dataPrimary.bookId + "&unitId=" + _this.dataPrimary.unitId;
                    var codeImgSrc = "https://www.17zuoye.com/qrcode?m=" + encodeURIComponent(url);

                    var imgObj = new Image();
                    imgObj.src = codeImgSrc;
                    imgObj.style.width = "200px";
                    imgObj.style.height = "200px";
                    $("#sharecode_box").html('')
                    $("#sharecode_box").append(imgObj);
                    _this.previewStatus = false;
                    _this.model = true;
                } else {
                    alert("请保存后预览")
                }
            },
            closeModel: function () {
                var _this = this;
                _this.model = false;
            },
            getUnitTypes:function(){
                var _this = this;
                if(_this.dataPrimary.bookId){
                    $.get("/chips/ai/todaylesson/getUnitTypes.vpage",{
                        bookId:_this.dataPrimary.bookId
                    }).then(function(res){
                        if(res.success){
                            _this.dataList.unitTypeList = res.data;
//                            console.log(_this.dataList.unitTypeList)
                            if(!_this.dataList.unitTypeList || _this.dataList.unitTypeList.length ==0){
                                _this.dataList.unitList = [];
                                _this.dataPrimary.unitId = "";
                                _this.dataList.unitType = "";
                            }
                        }else{
                            alert(res.info)
                        }
                    });
                } else {
                    _this.dataList.unitTypeList = [];
                    _this.dataList.unitList = [];
                    _this.dataPrimary.unitId = "";
                    _this.dataList.unitType = "";
                }
            },
            getUnits:function(){
                var _this = this;
                console.log(this)
                if(_this.dataList.unitType){
                    $.get("/chips/ai/todaylesson/getUnits.vpage",{
                        bookId:_this.dataPrimary.bookId,
                        unitType:_this.dataList.unitType,
                    }).then(function(res){
                        _this.dataList.unitList = res.data;
                    });
                } else {
                    _this.dataList.unitList = [];
                    _this.dataPrimary.unitId = "";
                }
            }
        },
        created: function () {
            var _this = this;
            var id = "${id!}";
//            if (id) {
                $.get('${requestContext.webAppContextPath}/chips/ai/todaylesson/officialDetail.vpage', {
                    id: id
                }, function (res) {
                    if(res.dataJson) {
                        $.extend(_this.dataJson, res.dataJson);
//                        _this.dataJson = res.dataJson;
//                        _this.dataJson.firstCommentDesc = res.dataJson.firstCommentDesc;
//                        _this.dataJson.firstAudioUrl = res.dataJson.firstAudioUrl;
//                        _this.dataJson.firstGrammaticalExplanation = res.dataJson.firstGrammaticalExplanation;
//                        _this.dataJson.firstKnowledgeStation = res.dataJson.firstKnowledgeStation;
//                        _this.dataJson.secondCommentDesc = res.dataJson.secondCommentDesc;
//                        _this.dataJson.secondAudioUrl = res.dataJson.secondAudioUrl;
//                        _this.dataJson.secondGrammaticalExplanation = res.dataJson.secondGrammaticalExplanation;
//                        _this.dataJson.secondKnowledgeStation = res.dataJson.secondKnowledgeStation;
//                        _this.dataJson.thirdCommentDesc = res.dataJson.thirdCommentDesc;
//                        _this.dataJson.thirdAudioUrl = res.dataJson.thirdAudioUrl;
//                        _this.dataJson.thirdGrammaticalExplanation = res.dataJson.thirdGrammaticalExplanation;
//                        _this.dataJson.thirdKnowledgeStation = res.dataJson.thirdKnowledgeStation;
//                        console.log(_this.dataJson)
                    }
                    if(res.dataPrimary){
                        $.extend(_this.dataPrimary, res.dataPrimary);
//                        _this.dataPrimary.id = res.dataPrimary.id;
//                        _this.dataPrimary.bookId = res.dataPrimary.bookId;
//                        _this.dataPrimary.unitId = res.dataPrimary.unitId;

                    }
                    if(res.dataList){
                        $.extend(_this.dataList, res.dataList);
//                        _this.dataList.unitType = res.dataList.unitType;
//                        _this.dataList.bookList = res.dataList.bookList;
//                        _this.dataList.unitTypeList = res.dataList.unitTypeList;
//                        _this.dataList.unitList = res.dataList.unitList;
                    }
                });
                if(id) {
                    _this.previewStatus = true;
                }
//            }
        }
    });

</script>

</@layout_default.page>