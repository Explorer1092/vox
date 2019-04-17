<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="发布新文章" page_num=16>
<link href="${requestContext.webAppContextPath}/public/css/advertisement/advertisement.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<style>
.img-size-tip{transition:all 0.2s ease-in-out;position:absolute;left:0;top:0;height:100%;width:100%;box-sizing:border-box;color:#ccc;border:2px dashed #ccc;font-size:5rem;font-weight:900;line-height: 150px;text-align: center;}
.img-size-tip:before,.img-size-tip:after{position:absolute;left:0;width:100%;height:56px;overflow:hidden;line-height: 56px;font-size:1.5rem;font-weight:normal;}
.img-size-tip:before{content:attr(data-size);top:0;}
.img-size-tip:after{content:"可拖拽上传";bottom:-5px;font-family:'微软雅黑', 'Microsoft YaHei', Arial;}
</style>
<div id="main_container" class="span9">
    <legend>
        <strong>编辑文章</strong>
        <#if isSavable>
            <a type="button" id="publish-btn" class="btn btn-info" style="float: right">保存</a>
        </#if>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th width="90px">序号</th>
                        <th>URL</th>
                        <th>标题</th>
                        <th width="90px">封面</th>
                    </tr>
                    </thead>
                    <tbody>
                    <#if articles??>
                        <#list articles as article>
                            <tr>
                                <td>${article_index + 1}
                                    <#if article_index == 0>
                                        <i style="color:red;vertical-align: sub;">*</i></td>
                                    </#if>
                                <td><input id="url_0${article_index + 1}" style="width:100%;display: block;box-sizing:border-box;" placeholder="请用“http://”开头" value="${article.articleUrl!''}" data-oid="${article.id!''}" data-bundleid="${article.bundleId!''}" data-status="${article.status!''}" data-publishdatetime="${article.publishDatetime!''}"/></td>
                                <td><input id="title_0${article_index + 1}" style="width:100%;display: block;box-sizing:border-box;" value="${article.articleTitle!''}" maxlength="20"/></td>
                                <td>
                                    <form id="form_img${article_index + 1}" name="form_img${article_index + 1}" data-src="${article.imgUrl!''}" enctype="multipart/form-data" action="uploadarticleimg.vpage" method="post" style="position:relative;margin:0;height:150px;width:150px;">
                                        <input name="accountId" type="hidden" value="${accountId!0}" />
                                        <div class="img-size-tip" data-size="${(article_index == 0)?string('660x360','170x124')}">
                                            +
                                            <img style="position:absolute;left:0;top:0;z-index:2;width:100%;height:100%;background: transparent;<#if !article.imgUrl??>display:none;</#if>" src="${article.imgUrl!''}" />
                                        </div>
                                        <input class="img_uploader" type="file" style="cursor: pointer;background:red;position:absolute;z-index:3;left:0;top:0;height:100%;width:100%;opacity: 0;" name="file" accept="image/*">
                                        <input type="hidden" name="type" value="img" >
                                        <input name="width" type="hidden" value="${(article_index == 0)?string('660','170')}" />
                                        <input name="height" type="hidden" value="${(article_index == 0)?string('360','124')}" />
                                    </form>
                                </td>
                            </tr>
                        </#list>
                    </#if>
                    </tbody>
                </table>
                <div class="checkbox">
                    <label style="display:inline"><input id="sendJpush" type="checkbox" ${(hasSend!false)?string("checked='checked'","")}/>同时推送给用户</label>
                </div>
                <div class="checkbox">
                    <label style="display:inline"><input id="bindSid" type="checkbox" ${(bindSid!false)?string("checked='checked'","")}/>是否拼接SID</label>
                </div>
            </div>
        </div>
    </div>
</div>
<script>
    $(function(){

        $('.img_uploader').on({
            'change':function(){
                var $this=$(this);
                // previewImg(this);
                $this.prev().css("border",0);
                $this.prev().children().show();

                $this.parent().ajaxSubmit(function(res){
                    if(res.success){
                        $this.parent().attr("data-src", res.fileName);
                        $this.prev().children().eq(0).attr("src", res.fileName);
                    }else{
                        alert(res.info);
                    }
                });
            },
            'mouseenter':function(){
                $(this).prev().css({"borderColor":"#999","color":"#999"});
            },
            'mouseleave':function(){
                $(this).prev().css({"borderColor":"#ccc","color":"#ccc"});
            }
        });

        function previewImg(file) {
            var prevDiv = $(file).prev().children().eq(0);
            if (file.files && file.files[0]) {
                var reader = new FileReader();
                reader.onload = function(evt) {
                    prevDiv.attr("src", evt.target.result);
                };
                reader.readAsDataURL(file.files[0]);
            }
            else {
                prevDiv.html('<img class="img" style="filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale,src=\'' + file.value + '\'">');
            }
        }

        $('#publish-btn').on('click',function(){
            if($('#url_01').val().trim()==""||$('#title_01').val().trim()==""|| $('#form_img1').attr("data-src")==""){
                alert("第一行被必须完整填写！");
                return;
            }
            if(confirm("请按序号进行填写,否则存在空值的一行及其后面的所有行将不会被保存！\n请确定是否要保存？")){
                var data={
                    articleList : getData(),
                    sendJpush   : document.getElementById("sendJpush").checked,
                    bindSid     : document.getElementById("bindSid").checked,
                    accountId   : "${accountId!0}"
                };
                $.ajax({
                    type: 'post',
                    url: "savearticle.vpage",
                    data: JSON.stringify(data),
                    success: function(res){
                        if(res.success){
                            alert("已全部保存成功！");
                            location.href="articlelist.vpage?accountId=${accountId!0}";
                        }else{
                            alert(res.info);
                        }
                    },
                    contentType: 'application/json;charset=UTF-8'
                });
            }
        });
        function getData(){
            var articles = [],
                url = "",bundleId=null,status="",
                title = "",publishDatetime = "",
                imgUrl = "", oid="";

            for(var i=1;i<=4;++i){
                oid = $('#url_0'+i).data("oid");
                bundleId = $('#url_0'+i).data("bundleid");
                url = $('#url_0'+i).val().trim();
                title = $('#title_0'+i).val().trim();
                imgUrl = $('#form_img'+i).attr("data-src");
                status = $('#url_0'+i).data("status");
                publishDatetime = $('#url_0'+i).data("publishdatetime");

                if(url == ""||title==""||imgUrl==""){
                    break;
                }
                articles.push({
                    articleUrl  : url,
                    title       : title,
                    imgUrl      : imgUrl,
                    id:oid,
                    bundleId:bundleId,
                    status:status,
                    publishDatetime:publishDatetime
                });
            }
            return articles;
        }
    });
</script>
</@layout_default.page>