<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-专题管理-新建/编辑' page_num=13 jqueryVersion ="1.7.2">
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
    <style>
        /*.uploadBox{ height: 100px;}*/
        .uploadBox .addBox{cursor: pointer; width: 170px; height: 124px;border: 1px solid #ccc; text-align: center; color: #ccc; float: left; margin-right: 20px;}
        .uploadBox .addBox .addIcon{ vertical-align: middle; display: inline-block; font-size: 80px;line-height: 95px;}
        .uploadBox img{ width: 500px; height: 124px;}
        ul.fancytree-container {
            width: 280px;
            height: 400px;
            overflow: auto;
            position: relative;
        }
        .form-horizontal .controls table th .control-label {
            float: left;
            margin-left: -180px;
            width: 160px;
            text-align: right;
        }
        .form-horizontal .controls table th label {
            text-align: left;
        }
    </style>

    <div class="span9">
        <fieldset>
            <legend>专题编辑</legend>
        </fieldset>

        <div class="row-fluid">
            <div class="span12">
                <form class="well form-horizontal" style="background-color: #fff;">
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label" for="productName">专题名称：</label>
                            <div class="controls">
                                <label for="title">
                                    <input type="text" <#if subjectInfo["title"]??>value="${subjectInfo["title"]!''}"</#if> name="title" id="title" maxlength="30" placeholder="输入标题，最多30字" style="width: 60%" class="input">
                                </label>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="control-label" for="productName">ID：</label>
                            <div class="controls">
                                <label for="title">
                                    <input type="text" <#if subjectInfo["subjectId"]??>value="${subjectInfo["subjectId"]!''}"</#if> name="subjectId" id="subjectId" maxlength="500" placeholder="subjectId" class="input" disabled style="width: 60%">
                                </label>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="control-label" for="productName">设置头图：</label>
                            <div class="controls">
                                <p style="font-size: 12px" class="text-error">建议尺寸(720*200)</p>
                            </div>
                            <div class="control-group">
                                <div class="controls">
                                    <div class="uploadBox">
                                        <div class="addBox">
                                            <i class="addIcon" id="uploadPhotoButton">+</i>
                                            <input class="fileUpBtn" data-name="photo" type="file" accept="image/gif, image/jpeg, image/png, image/jpg"  style="width: 100%;display:block; height: 100px; top:-100px; position: relative; opacity: 0;">
                                        </div>
                                        <div class="addIcon" <#if !subjectInfo["headImg"]?has_content>style="display: none;" </#if>>
                                            <img id="imgUlr" class="addIcon" src="${subjectInfo["headImg"]!''}" data-file_name="${subjectInfo["headImgName"]!''}">
                                        </div>

                                    </div>
                                </div>
                                <input type="button" id="deleteHeadImg" value="撤销头图">
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="control-label" for="productName">设置引言：</label>
                            <div class="controls">
                                <label for="title">
                                    <textarea name="introduction" id="introduction" style="width: 60%" class="input">${subjectInfo["introduction"]!''}</textarea>
                                </label>
                            </div>
                        </div>

                        <div class="control-group">
                            <div class="control-group">
                                <div class="controls" id="categoryDiv">
                                    <input type="button" id="addCategory" value="添加分类">
                                    <p style="font-size: 12px" class="text-error">只有一个分类时不用填写分类名称和排名</p>
                                    <#if subjectInfo["categoryRankMap"]??>
                                        <#list subjectInfo["categoryRankMap"]?keys as key>
                                            <table class="table table-hover table-striped table-bordered categoryTab" id="categoryTable">
                                                        <tr class="category">
                                                            <td>分类名称</td>
                                                            <td><input type="text" value="${key}" style="width: 80px" class="categoryName" maxlength="4" placeholder="最多四个字"></td>
                                                            <td>排序</td>
                                                            <td><input type="text" value="${subjectInfo["categoryRankMap"][key]!''}" style="width: 20px" class="categoryRank"></td>
                                                            <td>
                                                                <input type="button" class="addArticle" value="添加文章">
                                                                <table class="table table-hover table-striped table-bordered articleTab">
                                                                    <thead>
                                                                        <tr>
                                                                            <td>id</td>
                                                                            <td>标题</td>
                                                                            <td>文章排序</td>
                                                                            <td>操作</td>
                                                                        </tr>
                                                                    </thead>
                                                                    <#list subjectInfo["categoryNewsMap"][key] as newsId>
                                                                    <tr id="articleTr" class="articleClass">
                                                                        <td><input type="text" value="${newsId}" class="articleId"></td>
                                                                        <td><input type="text" <#if subjectInfo["newsTitleMap"]??> value="${subjectInfo["newsTitleMap"][newsId]!''}"</#if> class="articleTitle"></td>
                                                                        <td><input type="text" value="${subjectInfo["newsRankMap"][newsId]!''}" class="articleRank"></td>
                                                                        <td><input type="button" class="deleteArticle" value="刪除文章"></td>
                                                                    </tr>
                                                                    </#list>
                                                                </table>
                                                            </td>
                                                            <td><input type="button" class="deleteCategory" value="删除分类"></td>
                                                        </tr>
                                            </table>
                                        </#list>
                                    <#else>
                                        <table class="table table-hover table-striped table-bordered categoryTab" id="categoryTable">
                                            <tr class="category">
                                                <td>分类名称</td>
                                                <td><input type="text" value="" style="width: 80px" class="categoryName"  maxlength="4" placeholder="最多四个字"></td>
                                                <td>排序</td>
                                                <td><input type="text" value="" style="width: 20px" class="categoryRank"></td>
                                                <td>
                                                    <input type="button" class="addArticle" value="添加文章">
                                                    <table class="table table-hover table-striped table-bordered articleTab">
                                                        <tr>
                                                            <td>id</td>
                                                            <td>标题</td>
                                                            <td>文章排序</td>
                                                            <td>操作</td>
                                                        </tr>
                                                        <tr id="articleTr" class="articleClass">
                                                            <td><input type="text" value="" class="articleId"></td>
                                                            <td><input type="text" value="" class="articleTitle"></td>
                                                            <td><input type="text" value="" class="articleRank"></td>
                                                            <td><input type="button" class="deleteArticle" value="刪除文章"></td>
                                                        </tr>
                                                    </table>
                                                </td>
                                                <td><input type="button" class="deleteCategory" value="删除分类"></td>
                                            </tr>
                                        </table>
                                    </#if>
                                    <table class="table table-hover table-striped table-bordered categoryTemplate" id="categoryTemplate" style="display: none">
                                        <tr class="category">
                                            <td>分类名称</td>
                                            <td><input type="text" value="" style="width: 80px" class="categoryName" maxlength="4" placeholder="最多四个字"></td>
                                            <td>排序</td>
                                            <td><input type="text" value="" style="width: 20px" class="categoryRank"></td>
                                            <td>
                                                <input type="button" class="addArticle" value="添加文章">
                                                <table class="table table-hover table-striped table-bordered articleTab">
                                                    <tr>
                                                        <td>id</td>
                                                        <td>标题</td>
                                                        <td>文章排序</td>
                                                        <td>操作</td>
                                                    </tr>
                                                    <tr id="articleTr" class="articleClass">
                                                        <td><input type="text" value="" class="articleId"></td>
                                                        <td><input type="text" value="" class="articleTitle"></td>
                                                        <td><input type="text" value="" class="articleRank"></td>
                                                        <td><input type="button" class="deleteArticle" value="刪除文章"></td>
                                                    </tr>
                                                    <tr id="articleTemplate" class="articleTemplate" style="display: none">
                                                        <td><input type="text" value="" class="articleId"></td>
                                                        <td><input type="text" value="" class="articleTitle"></td>
                                                        <td><input type="text" value="" class="articleRank"></td>
                                                        <td><input type="button" class="deleteArticle" value="刪除文章"></td>
                                                    </tr>
                                                </table>
                                            </td>
                                            <td><input type="button" class="deleteCategory" value="删除分类"></td>
                                        </tr>
                                    </table>
                                </div>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="control-label" for="productName">设置广告位：</label>
                            <div class="controls">
                                <label for="title">
                                    <select  id="advertisementType" name="advertisementType">
                                        <option value="1" <#if subjectInfo["advertisementType"]?has_content && subjectInfo["advertisementType"]==1>selected="selected"</#if>>一张大图</option>
                                        <option value="2" <#if subjectInfo["advertisementType"]?has_content && subjectInfo["advertisementType"]==2>selected="selected"</#if>>两张小图</option>
                                    </select>
                                </label>
                            </div>
                        </div>

                        <div class="control-group" id="advertisementImgBox">
                            <label class="control-label" for="productName">配图：</label>
                            <div class="controls">
                                <p style="font-size: 12px" class="text-error" id="adSize"><#if !subjectInfo["advertisementType"]?has_content || (subjectInfo["advertisementType"]?has_content && subjectInfo["advertisementType"]==1)>建议尺寸(640*206)
                                <#else>建议尺寸(310*206)</#if></p>
                            </div>
                            <div class="controls">
                                <table>
                                    <tbody>
                                        <#assign newsIndex = 0 />
                                        <#if subjectInfo["adList"] ?? && subjectInfo["adList"]? size gt 0>
                                            <#list subjectInfo["adList"] as adMap>
                                                <#assign newsIndex = newsIndex +1 />
                                                <tr class="atlist adver${newsIndex}">
                                                    <th>
                                                        <div class="control-group">
                                                            <div class="uploadBox1">
                                                                <img class="imgUrl" <#if subjectInfo["advertisementType"]?has_content && subjectInfo["advertisementType"]==1>style="width: 500px; height: 124px; line-height: 124px;"
                                                                <#else >style="width: 170px; height: 124px; line-height: 124px;"</#if> data-file_name="${adMap["imgName"]!''}" src="${adMap["imgUrl"]!''}" alt="图片">
                                                                <input class="fileUpBtn" type="file">
                                                            </div>
                                                        </div>
                                                        <label class="control-label" for="productName">链接：</label>
                                                        <label for="title">
                                                            <input type="text" value="${adMap["url"]!''}" name="advertisementUrl" class="advertisementUrl" placeholder="" style="width: 95%" class="input">
                                                        </label>
                                                    </th>
                                                </tr>
                                            </#list>
                                        <#else >
                                        <tr class="atlist adver1">
                                            <th>
                                                <div class="control-group">
                                                    <div class="uploadBox">
                                                        <img class="imgUrl" style="width: 500px; height: 124px; line-height: 124px;" data-file_name="" src="" alt="图片">
                                                        <input class="fileUpBtn" type="file">
                                                    </div>
                                                </div>
                                                <label class="control-label" for="productName">链接：</label>
                                                <label for="title">
                                                    <input type="text" value="" name="advertisementUrl" class="advertisementUrl" placeholder="" style="width: 95%" class="input">
                                                </label>
                                            </th>
                                        </tr>
                                        </#if>
                                        <tr class="atlist adver2" hidden="hidden">
                                            <th>
                                                <div class="control-group">
                                                    <div class="uploadBox">
                                                        <img class="imgUrl" style="width: 170px; height: 124px; line-height: 124px;" data-file_name="" src="" alt="图片">
                                                        <input class="fileUpBtn" type="file">
                                                    </div>
                                                </div>
                                                <label class="control-label" for="productName">链接：</label>
                                                <label for="title">
                                                    <input type="text" value="" name="advertisementUrl" class="advertisementUrl" placeholder="" style="width: 95%" class="input">
                                                </label>
                                            </th>
                                        </tr>
                                        <tr>
                                            <td>
                                                <input type="button" id="deleteAd" value="撤销广告">
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <div class="control-group">
                            <div class="controls">
                                <input type="button" id="saveBtn" value="保存" class="btn btn-large btn-primary">
                            </div>
                        </div>
                    </fieldset>
                </form>

            </div>
        </div>
    </div>

<script type="text/javascript">
    $(function () {

        $('#advertisementType').click(function(){
            if ($("#advertisementType option:selected").val() == 2) {
                $(".adver1").find("img").css("width", "170px");
                $(".adver2").show();
                $("#adSize").text("建议尺寸(310*206)")
            } else {
                $(".adver1").find("img").css("width", "500px");
                $(".adver2").hide();
                $("#adSize").text("建议尺寸(640*206)")
            }
        });

        $('.articleId').blur(function(){
            var $this = $(this);
            var newsId = $this.val();
            $.post('getJxtNewsTitle.vpage', {newsId:newsId}, function (data) {
                if(data.success){
                    $this.parent().parent().find(".articleTitle").attr("value", data.title);
                } 
            });
        })

        $("#addCategory").click(function(){
            var $tab = $(".categoryTemplate").clone(true);
            $tab.addClass("categoryTab").removeClass("categoryTemplate");
            $tab.find(".articleTemplate").removeClass("articleTemplate");
            $tab.show();
            $("#categoryDiv").append($tab);
        });

        $(".deleteCategory").click(function () {
            $(this).parents('table').remove();
        })

        $(".deleteArticle").click(function(){
            $(this).parent().parent().remove();
        });

        $(".addArticle").click(function(){
            var $tr = $(".articleTemplate").clone(true);
            $tr.addClass("articleClass").removeClass("articleTemplate");
            $tr.show();
            $(this).next().append($tr);
        });

        //上传图片
        $('#uploadPhotoButton').click(function () {
            $('#uploadphotoBox').modal('show');
        });

        $(".fileUpBtn").change(function () {
            var $this = $(this);
            if ($this.val() != '') {
                var formData = new FormData();
                formData.append('imgFile', $this[0].files[0]);
                $.ajax({
                    url: 'edituploadimage.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            if($this.data('name') == 'photo'){
                                $('#imgUlr').attr('src',data.url).attr('data-file_name',data.fileName).show().closest('div.addIcon').show();
                            }else{
                                $this.siblings('img.imgUrl').show().attr('src',data.url).attr('data-file_name',data.fileName);
                            }
                            alert("上传成功");
                        } else {
                            alert("上传失败");
                        }
                    }
                });
            }
        });

        $("#deleteHeadImg").click(function(){
            $('#imgUlr').attr('src', '').attr('data-file_name', '').hide();
        });

        $("#deleteAd").click(function() {
            $("#advertisementImgBox tbody tr.atlist").each(function(){
                $(this).find(".imgUrl").attr('src', '').attr('data-file_name', '');
                $(this).find(".advertisementUrl").val('');
            });
        });

        //保存
        $('#saveBtn').on('click', function () {
            var categoryRankMap = {};
            var categoryNewsMap = {};
            var newsRankMap = {};
            var emptyArticleTitle = false;
            $(".categoryTab").each(function () {
                var newsList = [];
                var $categoryTable = $(this);
                var categoryName = $categoryTable.find(".categoryName").val();
                var categoryRank
                if ($categoryTable.find(".categoryRank").val() == '') {
                    categoryRank = 0;
                } else {
                    categoryRank = $categoryTable.find(".categoryRank").val();
                }
                categoryRankMap[categoryName] = categoryRank;
                var $articleTab = $categoryTable.find(".articleTab");
                $articleTab.find('.articleClass').each(function () {
                    var articleId = $(this).find(".articleId").val();

                    var articleRank;
                    if ($(this).find(".articleRank").val() == '') {
                        articleRank = 0
                    } else {
                        articleRank = $(this).find(".articleRank").val();
                    }
                    newsRankMap[articleId] = articleRank;
                    newsList.push(articleId);
                    var articleTitle = $(this).find(".articleTitle").val();
                    if (articleTitle == '') {
                        emptyArticleTitle = true;
                        return false;
                    }
                })
                categoryNewsMap[categoryName] = newsList;
            });
            if (emptyArticleTitle) {
                alert("文章标题不能为空!");
                return false;
            }

            var title = $('#title').val();
            var subjectId = $('#subjecgId').val();
            var headImg = $("#imgUlr").data('file_name');
            var introduction = $("#introduction").val();
            var categoryRank = JSON.stringify(categoryRankMap);
            var categoryNews = JSON.stringify(categoryNewsMap);
            var newsRank = JSON.stringify(newsRankMap);
            var advertisementType = $("#advertisementType option:selected").val();

            var adList = [];
            var adMap = {};
            $("#advertisementImgBox tbody tr.atlist").each(function(){
                var img = $(this).find(".imgUrl").data('file_name');
                var adUrl = $(this).find(".advertisementUrl").val();
                if (img != '' || adUrl != '') {
                    adMap["img"] = img;
                    adMap["adUrl"] = adUrl;
                    adList.push(JSON.stringify(adMap));
                }
            });
            var adInfo = adList.join("#");

            var postData = {
                subjectId: '${subjectInfo["subjectId"]!''}',
                title: title,
                headImg: headImg,
                introduction: introduction,
                categoryRank: categoryRank,
                categoryNews: categoryNews,
                newsRank : newsRank,
                advertisementType: advertisementType,
                adList: adInfo
            };

            //判断资讯内容
            if (title == '') {
                alert("标题不能为空");
                return false;
            }

            var emptyArticle = true;
            $.each(newsRankMap, function(key, value){
                if (key == '') {
                    emptyArticle = true;
                    return;
                } else {
                    emptyArticle = false;
                }
            })
            if (emptyArticle) {
                alert("文章id不能为空!");
                return false;
            }

            $.getUrlParam = function(name)
            {
                var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
                var r = window.location.search.substr(1).match(reg);
                if (r!=null) return unescape(r[2]); return null;
                };

            $.post('saveSubject.vpage', postData, function (data) {
                if(data.success){
                    var currentPage=$.getUrlParam('currentPage');
                    location.href = 'subjectmanage.vpage';
                }else{
                    alert(data.info);
                }
            });
        });

    });
</script>
</@layout_default.page>