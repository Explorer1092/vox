<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='书籍详情配置' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.config.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.all.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<style>
    .form-horizontal .control-label {
        float: left;
        width: 206px;
        padding-top: 5px;
        text-align: right;
    }

    .control-group {
        margin-top: 10px;
        margin-bottom: 80px;
    }
</style>
<div class="span9">
    <fieldset>
        <legend>书籍详情</legend>
    </fieldset>

    <div class="row-fluid">
        <div class="span12">
        <#--先给去掉吧，这玩意儿指不定哪天就加回来-->
        <#--<div class="control-group">-->
        <#--<label class="control-label" for="productName">课程ID：</label>-->
        <#--<div class="controls">-->
        <#--<label for="title">-->
        <#--<select id="selectLessonId" name="selectLessonId">-->
        <#--<#if lessonIds?? && lessonIds?size gt 0>-->
        <#--<#list lessonIds as lId>-->
        <#--<option value="${lId}"-->
        <#--<#if (((lessonId)!'') == lId)>selected="selected"</#if>>${lId}</option>-->
        <#--</#list>-->
        <#--<#else>-->
        <#--<option value="">暂无数据</option>-->
        <#--</#if>-->
        <#--</select>-->
        <#--</label>-->
        <#--</div>-->
        <#--</div>-->
            <div class="control-group">
                <label class="control-label" for="productName">bookId：</label>
                <div class="controls">
                    <label for="title">
                        <input type="text" value="${(id)!''}"
                               name="bookId" id="bookId" maxlength="50"
                               style="width: 20%" class="input" readonly="readonly">
                    </label>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="productName">书籍类型：</label>
                <div class="controls">
                    <label for="title">
                        <select id="selectBookType" name="selectBookType">
                            <#if bookTypes?? && bookTypes?size gt 0>
                                <#list bookTypes as bookType>
                                    <option value="${bookType.id}"
                                        <#if selectBookType??&&(((selectBookType)!'') == bookType.id)>selected="selected"</#if>>${bookType.desc}</option>
                                </#list>
                            <#else>
                                    <option value="">暂无数据</option>
                            </#if>
                        </select>
                    </label>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="productName">书籍名称：</label>
                <div class="controls">
                    <label for="title">
                        <input type="text" value="${(title)!''}"
                               name="bookTitle" id="bookTitle" maxlength="50"
                               style="width: 20%" class="input">
                    </label>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="productName">封面图片：</label>
                <div class="controls">
                    <label for="title">
                        <input class="fileUpBtn" type="file"
                               accept="image/gif, image/jpeg, image/png, image/jpg"
                               style="float: left"
                               name="coverImg"
                        />
                    </label>
                    <img src="${(coverImg_url!'')}" id="coverImg" data-file_name="${(coverImg_file!'')}"/>
                </div>
            </div>
            <div class="control-group">
                <div class="control-group">
                    <div class="controls" id="categoryDiv">
                        <table class="table table-hover table-striped table-bordered categoryTab"
                               id="categoryTable">
                            <tr class="category">
                                <td>
                                <#--<input type="button" id="addPage" name="addPage" value="添加页面">-->
                                    <table class="table table-hover table-striped table-bordered pageTab">
                                        <thead>
                                        <tr>
                                            <td>id</td>
                                            <td>页面排序</td>
                                            <td>排序操作</td>
                                            <td>删除操作</td>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        </tbody>
                                    </table>
                                </td>
                            </tr>
                        </table>
                        <table class="table table-hover table-striped table-bordered categoryTemplate"
                               id="categoryTemplate" style="display: none">
                            <tr class="category">
                                <td>
                                    <table class="table table-hover table-striped table-bordered articleTab">
                                        <tbody>
                                        <tr class="pageTemplate" style="display: none">
                                            <td><input type="text" value="" name="pageId" readonly="readonly"/></td>
                                            <td><input type="text" value="" name="pageNum" readonly="readonly"/></td>
                                            <td><input type="text"
                                                       value=""
                                                       name="pageIdRank" style="display: none"/><a class="up"
                                                                                                   style="cursor: pointer">上移</a>
                                                <a class="down" style="cursor: pointer">下移</a> <a class="top"
                                                                                                  style="cursor: pointer">置顶</a>
                                                <a class="stick" style="cursor: pointer">置底</a></td>
                                            <td><input type="button" name="deletePage" value="刪除该页"></td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>
            </div>
            <div class="control-group comment">
                <label class="control-label" for="productName">说明内容：</label>
                <div class="controls">
                    <label for="comment">
                        <input type="text" value="${comment!''}"
                               name="comment" maxlength="64"
                               style="width: 50%" class="input">
                    </label>
                </div>
            </div>
            <div class="control-group">
                <div class="controls">
                    <input type="button" id="saveBtn" value="保存" class="btn btn-large btn-primary">
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">


    $(function () {

        // $("#addPage").click(function () {
        //     var $tr = $(".articleTab .pageTemplate:first").clone(true);
        //     $tr.show();
        //     $(".pageTab tbody").append($tr);
        //     sortTable();
        // });
        $("input[name='deletePage']").click(function () {
            $(this).closest("tr").remove();
            sortTable();
        });

        <#if bookIdList??&&bookIdList?size gt 0>
            <#list bookIdList as bookPageInfo>
                var $tr = $(".pageTemplate:first").clone(true);
                $tr.find("input[name='pageId']").val("${bookPageInfo.pageId!''}");
                $tr.find("input[name='pageNum']").val("${bookPageInfo_index+1!''}");
                $tr.show();
                $(".pageTab tbody").append($tr);
            </#list>
        </#if>

        //上传图片
        $(".fileUpBtn").change(function () {

            var $this = $(this);
            var ext = $this.val().split('.').pop().toLowerCase();
            if ($this.val() != '') {
                if ($.inArray(ext, ['gif', 'png', 'jpg', 'jpeg']) == -1) {
                    alert("仅支持以下格式的图片【'gif','png','jpg','jpeg'】");
                    return false;
                }

                var formData = new FormData();
                formData.append('inputFile', $this[0].files[0]);
                var fileSize = ($this[0].files[0].size / 1024 / 1012).toFixed(4); //MB
                console.info(fileSize);
                if (fileSize >= 2) {
                    alert("图片过大，重新选择。");
                    return false;
                }
                $.ajax({
                    url: 'uploadImg.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
//                            var img_html = '<img src="' + data.imgUrl + '" data-file_name="' + data.imgName + '">';
                            $("#coverImg").attr('src', data.imgUrl);
                            $("#coverImg").data("file_name", data.imgName);

                        } else {
                            alert("上传失败");
                        }
                    }
                });
            }
        });


        // $('#lessonId').blur(function () {
        //     var lessonId = $('#lessonId').val();
        //     if (lessonId) {
        //         $.ajax({
        //             type: 'post',
        //             url: 'check_lesson.vpage',
        //             data: {
        //                 lesson_id: lessonId
        //             },
        //             success: function (data) {
        //                 if (!data.success) {
        //                     alert(data.info);
        //                 }
        //             }
        //         });
        //     }
        // });

        $('#saveBtn').on('click', function () {
            // var lessonId = $('#selectLessonId').val();
            var bookId = $('#bookId').val();
            var bookTitle = $('#bookTitle').val();
            var coverImg = $("#coverImg").data("file_name");
            var selectBookType = $("#selectBookType").val();
            var comment = $("input[name='comment']").val();
            var pageIds = [];
            $(".pageTab tbody input[name='pageId']").each(function () {
                pageIds.push($(this).val());
            });
            var postData = {
                // lessonId: lessonId,
                bookId: bookId,
                title: bookTitle,
                coverImg: coverImg,
                bookType: selectBookType,
                pageIdArray: JSON.stringify(pageIds),
                comment:comment
            };

            //数据校验
            // if (!lessonId) {
            //     alert("lessonId不能为空");
            //     return false;
            // }
            if (!coverImg) {
                alert("封面不能为空");
                return false;
            }
            if (!bookTitle) {
                alert("书籍标题不能为空");
                return false;
            }
            if (isRepeat(pageIds)) {
                alert("有重复的页面id请检查");
                return false;
            }
            if (!selectBookType) {
                alert("书籍类型不能为空");
                return false;
            }

            $.getUrlParam = function (name) {
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return unescape(r[2]);
                return null;
            };
            // $.post('check_lesson.vpage', {lesson_id: lessonId}, function (data) {
            //     if (!data.success) {
            //         alert(data.info);
            //     }
            // });

            $.post('saveBookDetail.vpage', postData, function (data) {
                if (data.success) {
                    var currentPage = $.getUrlParam('currentPage');
                    location.href = 'bookList.vpage?page=' + currentPage;
                } else {
                    alert(data.info);
                }
            });
        });

        //上移
        var $up = $(".up");
        $up.click(function () {
            var $tr = $(this).closest("tr");
            if ($tr.index() != 0) {
                $tr.fadeOut().fadeIn();
                $tr.prev().before($tr);
                sortTable();
            }
        });
        //下移
        var $down = $(".down");
        // var len = $down.length;
        $down.click(function () {
            var $tr = $(this).closest("tr");
            $tr.fadeOut().fadeIn();
            $tr.next().after($tr);
            sortTable();
        });
        //置顶
        var $top = $(".top");
        $top.click(function () {
            var $tr = $(this).closest("tr");
            $tr.fadeOut().fadeIn();
            $(".pageTab tbody").prepend($tr);
            sortTable();
        });
        //置底
        var $stick = $(".stick");
        $stick.click(function () {
            var $tr = $(this).closest("tr");
            $tr.fadeOut().fadeIn();
            $(".pageTab tbody").append($tr);
            sortTable();
        });
    });

    function sortTable() {
        $(".pageTab tbody .pageTemplate").each(function (index) {
            $(this).find("input[name='pageNum']").val(index + 1);
        })
    }

    function isRepeat(arr) {
        var hash = {};
        for (var i in arr) {
            if (hash[arr[i]]) {
                return true;
            }
            hash[arr[i]] = true;
        }
        return false;
    }

</script>
</@layout_default.page>