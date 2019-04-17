<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加" page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap/bootstrap.min.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/bootstrap/bootstrap.min.css" rel="stylesheet">
<style>
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
    .form-group label{
        width: 150px;
    }
    .form-group .form-control{
        width:500px;
    }
</style>
<div id="AppModel"></div>


<script type="text/x-template" id="app-template">
    <div id="main_container" class="span9">
        <!--基础内容-->
        <legend><#if id?? && id != ''>编辑<#else>添加</#if> - 视频</legend>
        <div class="bs-callout bs-callout-danger">
            <div class="first-form">
                <h3 class="page-header">视频<#if id?? && id != ''> - 编辑<#else> - 添加</#if></h3>
                <div class="form-group">
                    <div class="form-group form-inline">
                        <label>视频编号</label> <input type="text" class="form-control" value=""
                                                   :placeholder="getPlaceholder('id').placeholder" v-model="id" <#if id?? && id != ''>disabled="disabled"</#if>/>
                    </div>
                    <div class="form-group form-inline">
                        <label>主标题</label> <input type="text" class="form-control" value="" placeholder="必填" v-model="title" />
                    </div>
                    <div v-for='(item,key) in begin' class="form-group form-inline">
                        <template v-if="key != 'type'">
                            <label>{{getPlaceholder(key).name}}</label>
                            <input type="text" class="form-control"
                                   :placeholder="getPlaceholder(key).placeholder" v-model="begin[key]">
                        </template>
                        <template v-else>
                            <label>{{ getPlaceholder(key).name }}</label>
                            <select class="form-control" v-model="begin[key]">
                                <option value="HOT_VIDEO">热门</option>
                                <option value="ACTIVITY_VIDEO">精选活动</option>
                                <option value="FUNNY_VIDEO">搞笑集锦</option>
                            </select>
                        </template>

                    </div>

                </div>
            </div>
        </div>

        <!-- 添加项目 -->
        <my-component></my-component>

        <!--结尾内容-->

        <div class="form-group" style="text-align: center;">
            <input type="button" class="btn btn-info btn-lg" value="提交对话" @click="getResults"
                   style="width: 200px;"/>
        </div>
    </div>
</script>



<script type="text/javascript">
    (function () {
        var vm = new Vue({
            template: '#app-template',
            el: '#AppModel',
            data: {
                topicCount: -1,
                id: "",
                title: "",
                begin: {
                    subhead:"",
                    videoUrl:"",
                    description:"",
                    postscript:"",
                    link:"",
                    uploaderName:"",
                    uploaderHead:"",
                    type:"1"
                }
            },
            methods: {
                getResults: function () {
                    if(!vm.id || vm.id == ''){
                        alert("题目编号不能为空。");
                        return;
                    }
                    if(!vm.title || vm.title == ''){
                        alert("标题不能为空。");
                        return;
                    }
                    if(!vm.begin.videoUrl || vm.begin.videoUrl == ''){
                        alert("视频url不能为空。");
                        return;
                    }
                    if(vm.begin.type == '2'){
                        if(!vm.begin.link || vm.begin.link == ''){
                            alert("【精选活动必须指定一个广告链接。】");
                            return;
                        }
                    }
                    $.post("${requestContext.webAppContextPath}/chips/ai/video/save.vpage", {
                        data: JSON.stringify({
                            id: vm.id,
                            title: vm.title,
                            subhead:vm.begin.subhead,
                            videoUrl:vm.begin.videoUrl,
                            description:vm.begin.description,
                            postscript:vm.begin.postscript,
                            link:vm.begin.link,
                            uploaderName:vm.begin.uploaderName,
                            uploaderHead:vm.begin.uploaderHead,
                            type:vm.begin.type
                        })
                    }, function (data) {
                        if (data.success) {
                            alert("添加成功");
                        } else {
                            alert(data.info);
                            console.info(data);
                        }
                    });
                    console.info(vm._data);
                },
                getPlaceholder: function (key) {
                    var _textWord = {
                        "id": {name: '视频编号', placeholder: "必填"},
                        "subhead": {name: '副标题', placeholder: "添加文本，没有则不填"},
                        "description": {name: '视频描述', placeholder: "添加文本，没有则不填"},
                        "videoUrl": {name: '视频url', placeholder: "http://video.17zuoye.com/lesson7.mp4"},
                        "uploaderName": {name: '上传者姓名', placeholder: "添加文本，没有则不填"},
                        "uploaderHead": {name: '上传者头像', placeholder: "http://image.17zuoye.com/tom.jpg"},
                        "link": {name: '广告链接', placeholder: "http://www.17zuoye.com/advertising.html"},
                        "type": {name: '标签', placeholder: "添加文本，没有则不填"},
                        "postscript": {name: '附言', placeholder: "添加文本，没有则不填"}
                    };

                    if (_textWord[key]) {
                        return _textWord[key];
                    } else {
                        return {name: key, placeholder: "添加文本，没有则不填"};
                    }
                }
            },
            created: function () {
                var _self = this;
                Vue.component('my-component', {
                    template: '#feedback-template',
                    created: function () {
                        console.info(this._data);
                        var lessonId = '${id!}';
                        if(lessonId && lessonId!=''){
                            var _modelSelf = this;

                            $.get("${requestContext.webAppContextPath}/chips/ai/video/detail.vpage", {id: lessonId}, function (result) {
                                if(result.success){
                                    _self.id = lessonId;
                                    _self.title = result.data.title;
                                    _self.begin.subhead = result.data.subhead;
                                    _self.begin.description = result.data.description;
                                    _self.begin.videoUrl = result.data.videoUrl;
                                    _self.begin.uploaderName = result.data.uploaderName;
                                    _self.begin.uploaderHead = result.data.uploaderHead;
                                    _self.begin.link = result.data.link;
                                    _self.begin.type = result.data.type;
                                    _self.begin.postscript = result.data.postscript;
                                }
                            });
                        }
                    }, methods: {

                    }
                });
            }
        });
    }());
</script>

</@layout_default.page>