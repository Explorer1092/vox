<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title="AI对话剧本添加" page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap/bootstrap.min.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/bootstrap/bootstrap.min.css" rel="stylesheet">
<style>
    [v-cloak]{
        display: none;
    }
    .first-form label {
        width: 100px !important;
        text-align: right;
        padding-right: 15px;
    }

    .feedback-form label {
        width: 140px;
        text-align: right;
        padding-right: 15px;
    }

    .form-title-input {
        width: 196px !important;
        display: inline-block !important;
    }

    input {
        outline: none !important;
    }

    input::placeholder {
        color: #999999;
    }

    .bs-callout {
        padding: 0 20px;
        margin: 20px 0;
        border: 1px solid #eee;
        border-left: 5px solid #1b809e;
        border-radius: 3px;
    }

    .bs-callout-danger {
        border-left-color: #ce4844;
    }

    .page-header {
        margin-top: 25px !important;
    }

    .form-inline .form-group {
        margin-bottom: 10px !important;
    }

    .form-control {
        height: 34px !important;
        margin-bottom: 0 !important;
    }

    .first-form label {
        width: 100px;
        text-align: right;
        padding-right: 15px;
    }

    .feedback-form label {
        width: 120px;
        text-align: right;
        padding-right: 15px;
    }

    .form-title-input {
        width: 196px !important;
        display: inline-block !important;
    }

    input {
        outline: none !important;
    }

    input::placeholder {
        color: #999999;
    }

    .bs-callout {
        padding: 0 20px;
        margin: 20px 0;
        border: 1px solid #eee;
        border-left: 5px solid #1b809e;
        border-radius: 3px;
    }

    .bs-callout-danger {
        border-left-color: #ce4844;
    }

    .page-header {
        margin-top: 25px !important;
    }

    .form-inline .form-group {
        margin-bottom: 10px !important;
    }
    [v-cloak]{
        display: none;
    }
    .data-modal{
        position: fixed;
        top: 0;
        bottom: 0;
        right: 0;
        left: 0;
        background: rgba(0,0,0,0.5);
        z-index: 9999;
    }
    .data-modal .data-content{
        background: #fff;
        height: 80%;
        position: absolute;
        top: 10%;
        bottom: 5%;
        left: 30%;
        right: 30%;
        border-radius: 10px;
        overflow: auto;
    }
    .data-modal .data-content p{
        text-align: center;
        line-height: 40px;
        font-size: 20px;
    }
    .data-modal .data-content .data-title{
        height: 50px;
        border-bottom: 1px solid #DDDDDD;
        text-align: center;
        position: relative;
    }
    .data-modal .data-content .data-title h3{
        margin: 0;
        padding: 0;
        line-height: 50px;
    }
    .data-modal .data-content .data-title span{
        font-size: 30px;
        line-height: 50px;
        width: 50px;
        height: 50px;
        position: absolute;
        top: 0;
        right: 0;
        cursor: pointer;
    }
    .label_box label{
        width: 150px;
        text-align: right;
        padding-right: 15px;
    }
</style>

<div id="AppModel" v-cloak class="span9">
    <!-- 基础内容 -->
    <legend><#if id?? && id != ''>编辑<#else>添加</#if> - 对话剧本</legend>
    <div class="form-inline label_box">
        <div class="form-group">
            <label>课程ID</label> <input type="text" class="form-control" v-model="result.id" style="width: 300px" placeholder="必填" <#if id?? && id != ''>disabled="disabled"</#if>/>
        </div>
        <div class="form-group">
            <label>课程名字</label> <input type="text" class="form-control" v-model="result.lessonName" style="width: 300px" placeholder="必填" />
        </div>
    </div>
    <!--Topic内容-->
    <div v-for="(item,index) in result.play" class="bs-callout bs-callout-success first-form" :key="index">
        <div class="page-header">
            <h3>剧本 - {{ index + 1 }}</h3>
        </div>
        <div class="form-group">
            <div class="form-inline">
                <div class="form-group">
                    <label>roleName</label> <input type="text" class="form-control" v-model="result.play[index]['roleName']" style="width: 600px;" :placeholder="getPlaceholder('roleName').placeholder">
                </div>
                <div class="form-group">
                    <label>original</label> <input type="text" class="form-control" v-model="result.play[index]['original']" style="width: 600px;" :placeholder="getPlaceholder('original').placeholder">
                </div>
                <div class="form-group">
                    <label>translation</label> <input type="text" class="form-control" v-model="result.play[index]['translation']" style="width: 600px;" :placeholder="getPlaceholder('translation').placeholder">
                </div>
                <div class="form-group">
                    <label>media</label> <input type="text" class="form-control" v-model="result.play[index]['media']" style="width: 600px;" :placeholder="getPlaceholder('media').placeholder">
                </div>
            </div>
        </div>

        <div class="form-group">
            <button class="btn btn-success" @click="add_dialog">添加对话</button>
            <#--<button class="btn btn-info" @click="copy_dialog(index)">复制对话</button>-->
            <button class="btn btn-danger" @click="del_dialog(index)">删除对话</button>
        </div>

    </div>


    <div class="form-group text-center">
        <button class="btn btn-info btn-lg" @click="add">提交剧本</button>
    </div>
</div>



<script type="text/javascript">
    (function () {
        var vm = new Vue({
            el: '#AppModel',
            data: {
                play:{
                    "roleName":"",
                    "original":"",
                    "translation":"",
                    "media":""
                },
                result:{
                    "id":"",
                    "lessonName":"",
                    "play":[
                        {
                            "roleName":"",
                            "original":"",
                            "translation":"",
                            "media":""
                        }
                    ]
                }
            },
            methods: {
                add_dialog(){
                    var _this = this;
                    var newplay = _this._cloneObject(_this.play,true);
                    _this.result.play.push(newplay);
                },
                copy_dialog(npc_index){
                    var _this = this;
                    var newnpc = _this._cloneObject(_this.result.npcs[npc_index],true);
                    _this.result.npcs.push(newnpc);
                },
                del_dialog(index){
                    var _this = this;
                    if(_this.result.play.length <= 1){
                        alert("最少保留一个");
                    }else{
                        _this.result.play.splice(index,1);
                    }
                },
                add(){
                    var _this = this;
                    if(!_this.result.id || _this.result.id == ''){
                        alert("题目ID不能为空。");
                        return;
                    }
                    if(!_this.result.lessonName || _this.result.lessonName == ''){
                        alert("剧本名称不能为空。");
                        return;
                    }
                    $.post("${requestContext.webAppContextPath}/chips/ailesson/play/save.vpage", {
                        data: JSON.stringify(_this.result)
                    }, function (data) {
                        if (data.success) {
                            alert("添加成功");
                        } else {
                            alert(data.info);
                            console.info(data);
                        }
                    });
                    console.info(_this.result);
                },



                getPlaceholder(key) {
                    var _textWord = {
                        "id": {name: '题目编号', placeholder: "必填"},
                        "roleName": {name: 'roleName', placeholder: "添加文本，没有则不填"},
                        "original": {name: 'original', placeholder: "添加文本，没有则不填"},
                        "translation": {name: 'translation', placeholder: "添加文本，没有则不填"},
                        "media": {name: 'media', placeholder: "添加文本，没有则不填"},
                    };

                    if (_textWord[key]) {
                        return _textWord[key];
                    } else {
                        return {name: key, placeholder: "添加文本，没有则不填"};
                    }
                },
                _cloneObject(target, deep) {
                    if (deep === void 0) { deep = false; }
                    if (target == null)
                        return null;
                    var newObject;
                    if(Array.isArray(target)){
                        newObject = [] ;
                    }else{
                        newObject = {}
                    }

                    for (var key in target) {
                        var value = target[key];
                        if (deep && typeof value == "object") {
                            // 如果是深表复制，则需要递归复制子对象
                            value = this._cloneObject(value, true);
                        }
                        newObject[key] = value;
                    }
                    return newObject;
                }
            },
            created: function () {
                var _this = this;
                console.info("created");
                var lessonId = '${id!}';
                if(lessonId && lessonId != ''){
                    $.get("${requestContext.webAppContextPath}/chips/ailesson/play/detail.vpage", {id: lessonId}, function (result) {
                        if(result.success){
                            _this.result = result.data
                        }
                    });
                }
            }
        });
    }());
</script>

</@layout_default.page>