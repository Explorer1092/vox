<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-内容发布' page_num=13 jqueryVersion ="1.7.2">
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
    <@app.css href="/public/plugin/admineditor/css/editor-min.css"/>
    <style>
        .uploadBox{ height: 100px;}
        .uploadBox .addBox{cursor: pointer; width: 170px; height: 124px;border: 1px solid #ccc; text-align: center; color: #ccc; float: left; margin-right: 20px;}
        .uploadBox .addBox .addIcon{ vertical-align: middle; display: inline-block; font-size: 80px;line-height: 95px;}
        .uploadBox img{ width: 170px; height: 124px;}
        ul.fancytree-container {
            width: 280px;
            height: 400px;
            overflow: auto;
            position: relative;
        }
        #colorpickerbox input{width: 50px;}
    </style>

    <div class="span9">
        <fieldset>
            <legend>内容发布</legend>
        </fieldset>

        <div class="row-fluid">
            <div class="span12">
                <form class="well form-horizontal" style="background-color: #fff;">
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label" for="productName">标题：</label>
                            <div class="controls">
                                <label for="title">
                                    <input type="text" <#if jxtNewsInfo["title"]??>value="${jxtNewsInfo["title"]!''}"</#if> name="title" id="title" maxlength="30" placeholder="输入标题，最多30字" style="width: 60%;height: 30px;" class="input">
                                </label>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName">ID：</label>
                            <div class="controls">
                                <label for="title">
                                    <input type="text" <#if jxtNewsInfo["subjectId"]??>value="${jxtNewsInfo["subjectId"]!''}"</#if> name="subjectId" id="subjectId" maxlength="500" placeholder="subjectId" class="input" disabled style="width: 60%;height: 30px;">
                                </label>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="control-label" for="productName">页面版式：</label>
                            <div class="controls">
                                <label for="title">
                                    <select  id="newsType" name="newsType">
                                        <#assign jxtNewsType=jxtNewsInfo["type"]!''/>
                                        <#list jxtNewsInfo["totalType"]?keys as key>
                                            <#assign typeValue=jxtNewsInfo["totalType"][key]/>
                                            <option value="${key}" <#if key == jxtNewsType>selected="selected"</#if>>${typeValue}</option>
                                        </#list>
                                    </select>
                                </label>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="control-label" for="productName">封面：</label>
                            <div class="controls sizeType">
                                <p style="font-size: 12px" class="text-error" id="imgSize"><#if jxtNewsInfo["type"]?has_content && jxtNewsInfo["type"]=="BIG_IMAGE">建议尺寸(640*232)<#else >建议尺寸(210*140)</#if></p>
                            </div>
                            <div class="control-group">
                                <div class="controls">
                                    <div class="uploadBox">
                                        <#assign imgUrls=jxtNewsInfo["imgUrl"]!{}>
                                        <#assign fileNames=jxtNewsInfo["fileName"]!{}>
                                        <div id="addBox0" class="addBox" data-pic_index="0">
                                            <#assign imgUrl=imgUrls["url0"]!''/>
                                            <#assign fileName=fileNames["url0"]!''/>
                                            <span class="imgShowBox">
                                                <#if imgUrl!="">
                                                    <img src="${imgUrl}" data-file_name="${fileName}">
                                                <#else>
                                                    <i class="addIcon">+</i>
                                                </#if>
                                            </span>
                                            <input class="fileUpBtn" type="file" accept="image/gif, image/jpeg, image/png, image/jpg"  style="width: 100%;display:block; height: 100px; top:-100px; position: relative; opacity: 0;">
                                        </div>
                                        <div id="addBox1" class="addBox" data-pic_index="1">
                                            <#assign imgUrl=imgUrls["url1"]!''/>
                                            <#assign imgUrl0=imgUrls["url0"]!''/>
                                            <#assign fileName=fileNames["url1"]!''/>
                                            <span class="imgShowBox">
                                                <#if imgUrl!="">
                                                    <img src="${imgUrl}" data-file_name="${fileName}">
                                                <#else>
                                                    <i class="addIcon">+</i>
                                                </#if>
                                            </span>
                                            <input class="fileUpBtn" type="file" accept="image/gif, image/jpeg, image/png, image/jpg"  style="width: 100%;display:block; height: 100px; top:-100px; position: relative; opacity: 0;">
                                        </div>
                                        <div id="addBox2" class="addBox" data-pic_index="2">
                                            <#assign imgUrl=imgUrls["url2"]!''/>
                                            <#assign imgUrl0=imgUrls["url0"]!''/>
                                            <#assign fileName=fileNames["url2"]!''/>
                                            <span class="imgShowBox">
                                                <#if imgUrl!="">
                                                    <img src="${imgUrl}" data-file_name="${fileName}">
                                                <#else>
                                                    <i class="addIcon">+</i>
                                                </#if>
                                            </span>
                                            <input class="fileUpBtn" type="file" accept="image/gif, image/jpeg, image/png, image/jpg"  style="width: 100%;display:block; height: 100px; top:-100px; position: relative; opacity: 0;">
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="control-label" for="productName">文章共享区域：</label>
                            <div class="control-group">
                                <div class="controls">
                                    <input type="radio" name="publicContent" value="headContent" class="publicContent" checked="checked">文章头部区域
                                    <input type="radio" name="publicContent" value="tailContent" class="publicContent">文章尾部区域
                                </div>
                            </div>
                        </div>

                        <div class="control-group">
                            <div class="control-group">
                                <div class="controls">
                                    <div style="height: auto;">
                                        <div class="mat-content">
                                            <div class="item shop-name">
                                                <div class="editor">
                                                    <div class="wxeditor">
                                                        <div class="clearfix">
                                                            <div class="left clearfix" style="width: 420px;">
                                                                <div class="tabbox clearfix" style="width: 69px;">
                                                                    <ul class="tabs" id="tabs" style="height: 580px;">
                                                                        <li><a href="javascript:void (0);" tab="tab1" class="">关注</a></li>
                                                                        <li><a href="javascript:void (0);" tab="tab2" class="current">标题</a></li>
                                                                        <li><a href="javascript:void (0);" tab="tab3" class="">内容</a></li>
                                                                        <li><a href="javascript:void (0);" tab="tab4" class="">互推</a></li>
                                                                        <li><a href="javascript:void (0);" tab="tab5" class="">分割</a></li>
                                                                        <li><a href="javascript:void (0);" tab="tab6" class="">原文引导</a></li>
                                                                        <li><a href="javascript:void (0);" tab="tab7" class="">节日</a></li>
                                                                        <li><a href="javascript:void (0);" tab="tab8" class="">表格</a></li>
                                                                    </ul>
                                                                    <em class="fr"></em>
                                                                </div>
                                                                <div id="styleselect" class="clearfix">
                                                                    <div class="tplcontent" style="width: 350px;">
                                                                        <div id="colorpickerbox"></div>
                                                                        <div>
                                                                            <div style="background:#fff;">
                                                                                <#include 'tab/tab1.ftl'>
                                                                                <#include 'tab/tab2.ftl'>
                                                                                <#include 'tab/tab3.ftl'>
                                                                                <#include 'tab/tab4.ftl'>
                                                                                <#include 'tab/tab5.ftl'>
                                                                                <#include 'tab/tab6.ftl'>
                                                                                <#include 'tab/tab7.ftl'>
                                                                                <#include 'tab/tab8.ftl'>
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                    <div class="goto"></div>
                                                                </div>
                                                            </div>
                                                            <div class="right" style="background:#fff; width: 50%">
                                                                <div id="bdeditor" >
                                                                    <script type="text/javascript" charset="utf-8" src="/public/plugin/wxeditor/js/ueditor.config.js"></script>
                                                                    <script type="text/javascript" charset="utf-8" src="/public/plugin/wxeditor/js/ueditor.all.js"> </script>
                                                                    <script id="editor" type="text/plain" style="margin-top:15px;width:100%;"></script>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div><!--此处为文本编辑器-->
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="control-group">
                            <div class="control-group">
                                <div class="controls">
                                    <input type="button" id="savePublicContent" class="btn btn-success" value="保存内容">
                                    <input type="button" id="clearPublicContent" class="btn btn-danger" value="清空内容">
                                </div>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="control-label" for="productName">标签：</label>
                            <div class="controls">
                                <label for="title">
                                    <#assign tagskey=''/>
                                    <#assign tagsIds=''/>
                                    <#if jxtNewsInfo["tags"]?has_content>
                                        <#list jxtNewsInfo["tags"]?keys as key>
                                            <#assign tagskey = tagskey + key />
                                            <#assign tagsIds = tagsIds + jxtNewsInfo["tags"][key] />
                                            <#if key_has_next>
                                                <#assign tagskey = tagskey + "," />
                                                <#assign tagsIds = tagsIds + "," />
                                            </#if>
                                        </#list>
                                    </#if>
                                    <button id="labelBtn" type="button" <#if jxtNewsInfo["tags"]?has_content> data-ids="${tagsIds}" </#if> class="btn btn-default">选择</button>

                                    <input type="text" class="selectValue" <#if jxtNewsInfo["tags"]?has_content> value="${tagskey}" </#if> readonly style="height: 30px">
                                </label>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="control-label" for="productName">推送范围：</label>
                            <div class="controls">
                                <select id="pushType" name="pushType">
                                    <option value="1"<#if jxtNewsInfo["pushType"]?has_content && jxtNewsInfo["pushType"] == 1>selected="selected"</#if>>单个用户</option>
                                    <option value="2"<#if jxtNewsInfo["pushType"]?has_content && jxtNewsInfo["pushType"] == 2>selected="selected"</#if>>全部用户</option>
                                    <option value="3"<#if jxtNewsInfo["pushType"]?has_content && jxtNewsInfo["pushType"] == 3>selected="selected"</#if>>区域投放</option>
                                </select>
                                <input id="availableUserId" <#if !jxtNewsInfo["pushType"]?has_content || jxtNewsInfo["pushType"] == 1>style="display: block;height: 30px "<#else >style="display: none;height: 30px " </#if> value="${jxtNewsInfo["availableUserId"]!0}" type="text">
                            </div>
                        </div>
                        <div  id="regionDiv"  style="display: none;">
                            <div class="controls"><input type="text"  id="regionNames" readonly="true" value="${jxtNewsInfo["regionNames"]!''}" style="cursor: pointer;width: 600px;"></div>
                            <input type="hidden" name="regionIds" id="regionIds" value="${jxtNewsInfo["regionIds"]!''}"/>
                            <div id="cardregiontree" class="controls" ></div>
                            <div class="controls">
                                <button id="select_all" type="button" class="btn btn-default" data-dismiss="modal">全选</button>
                                <button id="cancel_all" type="button" class="btn btn-default" data-dismiss="modal">全不选</button>
                                <button id="reversed_select" type="button" class="btn btn-default" data-dismiss="modal">反选</button>
                            </div>
                        </div>

                        <div class="control-group">
                            <div class="controls">
                                <input type="button" id="saveBtn" value="上 线" class="btn btn-large btn-primary">
                            </div>
                        </div>
                    </fieldset>
                </form>
                <textarea  id="articleContent" style="visibility:hidden" <#if jxtNewsInfo["content"]??> value="${jxtNewsInfo["content"]!''}"</#if>  ></textarea>
            </div>
        </div>
    </div>
<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div id="modalBox"></div>

        </div><!-- /.modal-content -->
    </div><!-- /.modal -->
</div>

<script type="text/html" id="modalBox_tem">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
            &times;
        </button>
        <h4 class="modal-title">
            选择
            <%if(type == 'label'){ %>
            标签
            <% } else { %>
            内容类别
            <% } %>
        </h4>
    </div>

    <div class="modal-body" >
        <div class="control-group">
            <div class="controls" style="">
                <label for="title" style="line-height: 32px;">
                    <%for(var i = 0; i < tagList.length; i++) {%>
                        <%for(var j in tagList[i]) {%>
                            <%if(selectMap[type].length > 0){%>
                                <button class="btn btn-default selectLabelOrCategory <%for(var k = 0; k < selectMap[type].length; k++) {%><%if(selectMap[type][k] == tagList[i][j]){%> btn-success<%}%><%}%>" data-id="<%=tagList[i][j]%>" type="button" id=""><%=j%></button>
                            <%}else{%>
                                <button class="btn btn-default selectLabelOrCategory" data-id="<%=tagList[i][j]%>" type="button" id=""><%=j%></button>
                            <%}%>
                        <%}%>
                    <%}%>
                </label>
            </div>
        </div>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn btn-primary selectBtn" data-type="<%=type%>"> 确 定</button>
    </div>
</script>

<script src="/public/plugin/ueditor-1-4-3/third-party/zeroclipboard/ZeroClipboard.min.js" type="text/javascript"></script>
<script type="text/javascript" src="/public/plugin/wxeditor/js/colorpicker-min.js"></script>


<script type="text/javascript">
    var pic_index;
    $(function () {

        var c = UE.getEditor("editor", {
            serverUrl: "/advisory/ueditorcontroller.vpage",
            topOffset: 0,
            zIndex: 1040,
            autoHeightEnabled: false,
            initialFrameHeight: 473,
            toolbars:[[
            'fullscreen', 'source', '|', 'undo', 'redo', '|',
            'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
            'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
            'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
            'directionalityltr', 'directionalityrtl', 'indent', '|',
            'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
            'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
            'simpleupload', 'pagebreak','|',
            'horizontal', 'date', 'time', 'preview'
            ]]

        });

        c.ready(function () {
            $(".itembox").on("click", function (a) {
                c.execCommand("insertHtml", "<div>" + $(this).html() + "</div><br />")
            })
        });

        var b = ["borderTopColor", "borderRightColor", "borderBottomColor", "borderLeftColor"], d = [];
        $.each(b, function (a) {
            d.push(".itembox .wxqq-" + b[a])
        });
        $("#colorpickerbox").ColorPicker({
            flat: true,
            color: "#00bbec",
            onChange: function (a, e, f) {
                $(".itembox .wxqq-bg").css({
                    backgroundColor: "#" + e
                });
                $(".itembox .wxqq-color").css({
                    color: "#" + e
                });
                $.each(d, function (g) {
                    $(d[g]).css(b[g], "#" + e)
                })
            }
        });

        $(".tabs li a").on("click", function () {
            $(this).addClass("current").parent().siblings().each(function () {
                $(this).find("a").removeClass("current")
            });
            $("#" + $(this).attr("tab")).show().siblings().hide()
        });

        //初始化，默认是头部区域
        <#if jxtNewsInfo["headContent"]?has_content>
            c.ready(function(){
                c.setContent('${(jxtNewsInfo["headContent"])?replace('\n','')?replace("'" , "\\'")!''}');
            });
        </#if>

        $(".publicContent").on("click", function () {
            var contentType = $("input:radio[name=publicContent]:checked").val();
            var subjectId = "${jxtNewsInfo["subjectId"]!''}";
            $.ajax({
                type: 'post',
                url: 'getpubliccontent.vpage',
                data: {
                    contentType: contentType,
                    subjectId: subjectId
                },
                success: function(data) {
                    if (data.success){
                        c.ready(function(){
                            c.setContent(data.content.replace('\n','').replace("'" , "\\'"));
                        });
                    } else {
                        alert(data.info);
                    }
                }
            });
        });

        $("#savePublicContent").on("click", function(data){
            var contentType = $("input:radio[name=publicContent]:checked").val();
            var subjectId = "${jxtNewsInfo["subjectId"]!''}";
            var content = c.getContent();
            content=content.replace(/\n/g,"");
            content=content.replace(/>\s+?</g,"><");
            if($.trim(content) == '') {
                alert("内容不能为空");
                return false;
            }

            $.ajax({
                type: 'post',
                url: 'savepubliccontent.vpage',
                data: {
                    contentType: contentType,
                    subjectId: subjectId,
                    content: content
                },
                success: function(data) {
                    if (data.success){
                        alert("保存成功");
                    } else {
                        alert(data.info);
                    }
                }
            });
        });

        $('#clearPublicContent').click(function () {
            if (confirm('是否确认清空内容，清空后内容将无法恢复')) {
                c.setContent('');
            }

            var contentType = $("input:radio[name=publicContent]:checked").val();
            var subjectId = "${jxtNewsInfo["subjectId"]!''}";
            var content = c.getContent();
            $.ajax({
                type: 'post',
                url: 'savepubliccontent.vpage',
                data: {
                    contentType: contentType,
                    subjectId: subjectId,
                    content: content
                },
                success: function(data) {
                    if (!data.success){
                        alert(data.info);
                    }
                }
            });
        });

        loadRegion();

        var selectMap = {label: [], category: []};

        //上传图片
        $('.addBox').click(function () {
            pic_index=$(this).data("pic_index");
            $('#uploadphotoBox').modal('show');
        });

        if ($("#newsType").val() == "BIG_IMAGE" || $("#newsType").val() == "SMALL_IMAGE") {
            $("#addBox1").hide();
            $("#addBox2").hide();
        }

        if ($("#newsType").val() == "BIG_IMAGE") {
        }

        $("#labelBtn").on("click", function () {
            var $this = $(this);
            var tags = $this.data("ids");
            if (!!tags) {
                if (tags.toString().indexOf(',') != -1) {
                    selectMap.label = tags.split(',');
                } else {
                    selectMap.label = [tags];
                }
            }
            $.get("gettaglist.vpage", function (data) {
                if (data.success) {
                    $("#modalBox").empty().html(template("modalBox_tem", {
                        tagList: data.tagList,
                        type: "label",
                        selectMap: selectMap
                    }));
                    $('#myModal').modal('show');
                }
            });
        });

        $("#newsType").on("change",function(){
            var newsType=$("#newsType option:selected").val();
            console.info("changed");
            if(newsType=='BIG_IMAGE'||newsType=='SMALL_IMAGE'){
                $(".uploadBox").show();
                $(".addBox").hide();
                $("#addBox0").show();
                if(newsType=='BIG_IMAGE'){
                    $(".sizeType p").text("建议尺寸(600*232)");
                }else{
                    $(".sizeType p").text("建议尺寸(210*140)");
                }
            }else if(newsType=='TEXT'){
                $(".uploadBox").hide();
                $(".sizeType p").text("");
            }else{
                $(".uploadBox").show();
                $(".addBox").show();
                $(".sizeType p").text("建议尺寸(210*140)");
            }
        });

        $(document).on('click', '.selectLabelOrCategory', function () {
            var $this = $(this);
            $this.toggleClass('btn-success');
//            var num = $(".selectLabelOrCategory.btn-success").length;
//            if (num > 4) {
//                alert("最多可选四个");
//                $this.removeClass('btn-success');
//            }
        });


        $(document).on('click', ".selectBtn", function () {
            var $this = $(this);
            var type = $this.data('type');
            var ids = [], names = [];
            $(".selectLabelOrCategory.btn-success").each(function () {
                var that = $(this);
                ids.push(that.data('id'));
                names.push(that.text());
            });

            $("#" + type + "Btn").data('ids', ids.join(',')).siblings('input.selectValue').val(names.join(','));
            selectMap[type] = ids;
            $('#myModal').modal('hide');
        });


        $(".fileUpBtn").change(function () {
            pic_index = $(this).closest('div.addBox').data("pic_index");
            var $this = $(this);
            var ext = $this.val().split('.').pop().toLowerCase();
            if ($this.val() != '') {
                if ($.inArray(ext, ['gif', 'png', 'jpg', 'jpeg']) == -1) {
                    alert("仅支持以下格式的图片【'gif','png','jpg','jpeg'】");
                    return false;
                }

                var formData = new FormData();
                formData.append('imgFile', $this[0].files[0]);
                var fileSize = ($this[0].files[0].size / 1024 / 1012).toFixed(4); //MB
                if (fileSize >= 2) {
                    alert("图片过大，重新选择。");
                    return false;
                }
                $.ajax({
                    url: 'edituploadimage.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            var img_html = '<img src="' + data.url + '" data-file_name="' + data.fileName + '">';
                            $("#addBox" + pic_index).find('span.imgShowBox').html(img_html);
                        } else {
                            alert("上传失败");
                        }
                    }
                });
            }
        });

        //保存
        $('#saveBtn').on('click', function () {
            var title = $('#title').val();
            var subjectId = $('#subjectId').val();
            var imgStr=[];
            $(".addBox img").each(function(){
                if($(this).attr("style")!="display: none;"){
                    imgStr.push($(this).data('file_name'));
                }
            });
            //var imgStr = $("#imgUlr").data('file_name');
            var newsType=$("#newsType option:selected").val();
            //此种类型需要的图片数
            var needImgCount=0;
            if(newsType=="BIG_IMAGE"||newsType=="SMALL_IMAGE"){
                needImgCount=1;
            }else if (newsType=="THREE_IMAGES"){
                needImgCount=3;
            }
            console.info(needImgCount);
            console.info(imgStr);
            if(imgStr.length<needImgCount){
                alert("请上传"+needImgCount+"张头图");
                return false;
            }
            imgStr=imgStr.slice(0,needImgCount);
            var tagStr = $("#labelBtn").data('ids');
            var availableUserId = $("#availableUserId").val();
            var newsType = $("#newsType option:selected").val();
            var regionIds = $("#regionIds").val();
            var pushType = $("#pushType option:selected").val();

            var postData = {
                newsId: '${jxtNewsInfo["newsId"]!''}',
                title: title,
                subjectId: subjectId,
                imgStr: imgStr.toString(),
                tagStr: tagStr,
                availableUserId: availableUserId,
                newsType:newsType,
                regionIds:regionIds,
                pushType:pushType,
            };

            //判断资讯内容
            if (title == '') {
                alert("标题不能为空");
                return false;
            }

            
            if (!tagStr) {
                alert("标签不能为空");
                return false;
            }

            //判断推送方式
            if(pushType == 1 && availableUserId == ''){
                alert("推送对象ID不能为空");
                return false;
            }
            if(pushType == 3 && regionIds == ''){
                alert("推送区域不能为空");
                return false;
            }

            $.getUrlParam = function(name)
            {
                var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
                var r = window.location.search.substr(1).match(reg);
                if (r!=null) return unescape(r[2]); return null;
                };

            $.post('savesubjectjxtnews.vpage', postData, function (data) {
                if(data.success){
                    var currentPage=$.getUrlParam('currentPage');
                    location.href = 'jxtnewssubjectlist.vpage?currentPage='+currentPage;
                }else{
                    alert(data.info);
                }
            });
        });

        $("#pushType").on("change", function () {
            var pushType = $("#pushType option:selected").val();
            if(pushType == 2){
                $("#availableUserId").hide();
                $("#regionDiv").hide();
                $("#availableUserId").val(0);
            }else if( pushType == 3){
                $("#availableUserId").hide();
                loadRegion();
            }else{
                $("#regionDiv").hide();
                $("#availableUserId").show();
            }

        });

        //获取某个具体推送或者资讯的区域列表
        function  loadRegion(){
            var pushType = $("#pushType option:selected").val();
            var regionDiv = $("#regionDiv");
            var $regiontree = $("#cardregiontree");
            try{
                $regiontree.fancytree('destroy');
            }catch(e){

            }
            if (pushType == 3) {
                //选择区域
                $regiontree.fancytree({
                    source: {
                        url: "load_region.vpage?type=jxt_news&typeId=" + '${jxtNewsInfo["newsId"]!''}',
                        cache: false
                    },
                    checkbox: true,
                    selectMode: 2,
                    select:function(){updateRegion();}

                });
                regionDiv.show();
            }else{
                regionDiv.hide();
            }
        }

        //点击区域选中时。更新选中的区域Id和名称
        function updateRegion(){
            var regionTree = $("#cardregiontree").fancytree("getTree");
            var regionNodes = regionTree.getSelectedNodes();
            if(regionNodes == null || regionNodes == "undefined") {
                $('#regionIds').val('');
                $('#regionNames').val('');
                return;
            }
            var selectRegionNameList = new Array();
            var selectRegionIdList = new Array();
            $.map(regionNodes, function(node){
                selectRegionIdList.push(node.key);
                selectRegionNameList.push(node.title);
            });
            $('#regionIds').val(selectRegionIdList.join(','));
            $('#regionNames').val(selectRegionNameList.join(','));
        }
        //全选
        $("#select_all").on("click",function(){
            var regionTree = $("#cardregiontree").fancytree("getTree");
            var allNodes = regionTree.rootNode.getChildren();
            $.map(allNodes, function(rootNode){
                regionTree.rootNode.visit(function(currentNode){
                    if(rootNode.key == currentNode.key){
                        currentNode.setSelected(true);
                    }
                });
            });
        });
        //全不选
        $("#cancel_all").on("click",function(){
            var regionTree = $("#cardregiontree").fancytree("getTree");
            var allNodes = regionTree.rootNode.getChildren();
            $.map(allNodes, function(rootNode){
                regionTree.rootNode.visit(function(currentNode){
                        currentNode.setSelected(false);
                });
            });
        });
        //反选
        $("#reversed_select").on("click",function(){
            var regionTree = $("#cardregiontree").fancytree("getTree");
            var allNodes = regionTree.rootNode.getChildren();
            $.map(allNodes, function(rootNode){
                regionTree.rootNode.visit(function(currentNode){
                    if(rootNode.key == currentNode.key){
                        if(currentNode.selected){
                            currentNode.setSelected(false);
                        }else{
                            currentNode.setSelected(true);
                        }
                    }
                });
            });
        });

    });
</script>
</@layout_default.page>