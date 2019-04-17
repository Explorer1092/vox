<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='图文素材编辑' page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/ckeditor/ckeditor.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/ckeditor/ck_image_upload_plugin.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/ckeditor/ck_preview_plugin.js"></script>

<div id="main_container" class="span9">
    <legend>
        图文素材管理
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <div class="control-group">
                    <label class="control-label">标题：</label>
                    <div class="controls">
                        <textarea v-model="title" id="ck-title" rows="2" style="width: 100%;"></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">分享缩略图：</label>
                    <div class="controls">
                        <div v-if="shareIcon" style="position: relative;display: inline-block;">
                            <div id="remove-img" v-on:click="shareIcon = ''" style="position: absolute;right: -5px;top: -10px;">X</div>
                            <img style="width: 100px;" id="thumbnail-img" v-bind:src="shareIcon" v-on:click="reviewIcon" alt="">
                        </div>
                        <div>
                            <input id="thumbnail-btn" type="file" v-on:change="shareIconChange" placeholder="分享缩略图">
                        </div>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">文章详情：</label>
                    <div class="controls">
                        <div name="editor" id="editor">
                            <div v-html="content"></div>
                        </div>
                    </div>
                </div>
            </div>
            <span class="btn btn-success" @click="save()">保存</span>
            <span class="btn btn-cancel" @click="window.open('/chips/studyInfo/list.vpage');">取消</span>
        </div>
    </div>
    <#-- <input type="file" id="ck_image_upload"/> -->
</div>

<script type="text/javascript">
    var vm = new Vue({
        el: "#main_container",
        data: {
            id: '',
            title: '',
            shareIcon: '',
            content: '',
        },
        methods: {
            save: function () {
                var _this = this;
                this.content = CKEDITOR.instances.editor.getData();
                $.post('/chips/studyInfo/save.vpage', {
                    id: _this.id,
                    title: _this.title,
                    shareIcon: _this.shareIcon,
                    content: _this.content,
                }, function(res){
                    alert("保存成功");
                    // location.reload();
                    location.href = '/chips/studyInfo/list.vpage';
                });
            },
            getParams: function(name) {
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return decodeURI(r[2]); return null;
            },
            editorInit: function() {
                CKEDITOR.replace( 'editor', {
                    // filebrowserUploadUrl: '/uploader/upload.php',
                    extraPlugins: ['ck_image_upload', '_preview'],
                    toolbar:  [
                        ['Cut','Copy','Paste','PasteText','-','Print', 'SpellChecker', 'Scayt'],
                        ['Find','Replace','-','SelectAll','RemoveFormat'],
                        '/',
                        ['Bold','Italic','Underline','Strike','-','Subscript','Superscript'],
                        ['NumberedList','BulletedList','-','Outdent','Indent','Blockquote'],
                        ['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
                        ['Link','Unlink','Anchor'],
                        ['ck_image_upload', 'Flash','Table','HorizontalRule','Smiley','SpecialChar','PageBreak'],
                       '/',
                        ['Styles','Format','Font','FontSize'],
                        ['TextColor','BGColor'],
                        ['_preview','Maximize', 'ShowBlocks','-'],
                        ['Undo','Redo','-']
                    ]
                });
            },
            shareIconChange: function(e) {
                var thiz = this;
                var file = e.target.files[0];
                var fileOriginName = file.name;
                var index = fileOriginName.lastIndexOf(".");
                var ext = fileOriginName.substring(index + 1, fileOriginName.length);

                $.ajax({
                    url: "/chips/ai/todaylesson/getSignature.vpage",
                    data: {
                        ext: ext
                    },
                    type:"get",
                    async: false,
                    success:function (data) {
                        var signResult = data.data;
                        var store  = new OSS({
                            accessKeyId: signResult.accessid,
                            accessKeySecret: signResult.accessKeySecret,
                            endpoint: signResult.endpoint,
                            bucket: signResult.bucket
                        });
                        var ossPath = signResult.dir + signResult.filename + "." + ext;
                        store.multipartUpload(ossPath, file).then(function (result) {
                            console.log("https://" + signResult.videoHost + ossPath);
                            var url = "https://" + signResult.videoHost + ossPath;
                            thiz.shareIcon = url;
                        }).catch(function (err) {
                            alert('上传失败');
                        });
                    }
                });
            },
            reviewIcon: function() {
                if(this.shareIcon) {
                    window.open(this.shareIcon);
                }
            }
        },
        created: function () {
            var thiz = this;
            var id = this.getParams('id');
            if(id) {
                $.get('${requestContext.webAppContextPath}/chips/studyInfo/editData.vpage', {
                    articleId: id,
                }, function (res) {
                    if (res.success) {
                        var article = res.article;
                        if (article) {
                            thiz.id = article.id;
                            thiz.title = article.title;
                            thiz.shareIcon = article.shareIcon;
                            thiz.content = article.content;
                        }
                    }
                });
            }
        },
        mounted: function() {
            this.editorInit();
        }
    });

    
</script>

</@layout_default.page>