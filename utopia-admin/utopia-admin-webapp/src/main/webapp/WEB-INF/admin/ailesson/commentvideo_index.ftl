<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='用户视频管理' page_num=26>
<style type="text/css">
    .uploadLabel{
        font-size: 16px;
        border: 1px solid #eee;
        -webkit-border-radius: 5px;
        -moz-border-radius: 5px;
        border-radius: 5px;
        line-height: 50px;
        display: inline-block;
        width: 100px;
        height: 50px;
        cursor: pointer;
        position: relative;
        z-index: 999;
        padding: 0 20px;
    }
    #uploadAudio{
        height: 50px;
        width: 100px;
        position: absolute;
        left: 25px;
        opacity: 0;
    }
</style>
<div id="main_container" class="span9">
    <legend>用户视频管理</legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well" style="font-size: 12px;">
                <form id="frm" class="form-horizontal" action="/chips/user/video/comment/list.vpage">
                    <input type="hidden" id="pageNumber" name="pageNumber" value="1">
                    用户名：
                    <input type="text" name="userName" style="height: 29px;"
                           placeholder="模糊搜索" <#if userName ?? && userName != ''> value=${userName} </#if> >
                    用户Id：
                    <input type="text" name="userId" style="height: 29px;"
                           placeholder="精确搜索" <#if userId ?? && userId gt 0> value=${userId!} </#if> >
                    视频Id：
                    <input type="text" name="userVideoId" style="height: 29px;"
                           placeholder="精确搜索" <#if userVideoId ?? && userVideoId != ''> value=${userVideoId} </#if> >
                    评论关键词：
                    <input type="text" name="comment" style="height: 29px;"
                           placeholder="模糊搜索" <#if comment ?? && comment != ''> value=${comment!} </#if> >
                    <br/>
                    课程：
                    <select id="book" name="book" class="multiple district_select" next_level="units">
                        <#if books??>
                            <#list books as p>
                                <option value="${p.id}" <#if book ?? && book == p.id> selected</#if>>${p.name}</option>
                            </#list>
                        </#if>
                    </select>
                    单元：
                    <select id="unitSelect" data-init='false' name="unit" class="multiple district_select">
                        <#if units??>
                            <#list units as p>
                                <option value="${p.id}" <#if unit ?? && unit == p.id> selected</#if>>${p.name}</option>
                            </#list>
                        </#if>
                    </select>

                    班级：
                    <select id="unit" data-init='false' name="clazz" class="multiple district_select">
                        <option value="0" <#if clazz ?? && clazz == 0> selected</#if>>全部</option>
                        <#if units??>
                            <#list clazzList as p>
                                <option value="${p.id!}" <#if clazz ?? && clazz == p.id> selected</#if>>${p.name}</option>
                            </#list>
                        </#if>
                    </select>

                    分类：
                    <select id="category" data-init='false' name="category" class="multiple district_select">
                        <option value="" <#if !category ?? || category == '' > selected</#if>>全部</option>
                        <#if categoryList ??>
                            <#list categoryList as p>
                                <option value="${p.name()}" <#if category ?? && category == p.name()>
                                        selected</#if>>${p.getDescription()!}</option>
                            </#list>
                        </#if>
                    </select>
                    <br />
                    是否评论：
                    <select id="hasComment" data-init='false' name="hasComment" class="multiple district_select">
                        <option value="0" <#if hasComment ?? && hasComment == 0>
                                selected</#if>>全部</option>
                        <option value="1" <#if hasComment ?? && hasComment == 1>
                                selected</#if>>只看有评论</option>
                        <option value="2" <#if hasComment ?? && hasComment == 2>
                                selected</#if>>只看无评论</option>
                    </select>
                    <#if labels?has_content>
                            <#list labels as s>
                                <input type="checkbox" name="label" value="${s.name()!}"> ${s.getDescription()!}
                            </#list>
                        </#if>
                    <button class="btn btn-primary">查 询</button>
                </form>

            </div>
        </div>
    </div>
    <div id="data_table_journal">
        <table class="table table-striped table-bordered">
            <tr>
                <td>视频id</td>
                <td>用户</td>
                <td>课程</td>
                <td>上传时间</td>
                <td>视频分类</td>
                <td>操作</td>
            </tr>
            <#if pageData.content?? && pageData.content?size gt 0>
                <#list pageData.content as e >
                    <tr <#if e.commented ?? && e.commented >style="color: red" </#if>>
                        <td>${e.id!}</td>
                        <td>${e.user!}</td>
                        <td>${e.lesson!}</td>
                        <td>${e.createDate!}</td>
                        <td>${e.category!}</td>
                        <td>
                            <button type="button" name="detail" data-id="${e.id!}" class="btn btn-primary">评论</button>
                            <button type="button" name="shareurl" data-url="${e.url!}" class="btn btn-primary">生成分享</button>
                        </td>
                    </tr>
                </#list>
            <#else >
                <tr>
                    <td colspan="6"><strong>暂无数据</strong></td>
                </tr>
            </#if>
        </table>
    </div>
    <ul class="pager">
        <#if (pageData.hasPrevious())>
            <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
        <#else>
            <li class="disabled"><a href="#">上一页</a></li>
        </#if>
        <#if (pageData.hasNext())>
            <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
        <#else>
            <li class="disabled"><a href="#">下一页</a></li>
        </#if>
        <li>当前第 ${pageNumber!} 页 |</li>
        <li>共 ${pageData.totalPages!} 页|</li>
        <li>共 ${total !} 条</li>
    </ul>


    <!-- Modal -->
    <div class="modal fade hide" id="courseware_detail" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title" id="myModalLabel" style="text-align:center;">视频详情</h4>
                    <input type="hidden" id="detail_id" name="id"/>
                </div>
                <div class="modal-body" style="max-height: 550px !important;">
                    <div class="main_content_detail">
                        <div class="courseware_list">
                            <div class="inner">
                                <div class="item">
                                    <div class="head_img">
                                        <video controls="controls" id="detail_video"
                                               style="width: 50%;margin: 0 auto;display: block;"/>
                                    </div>
                                    <div class="content_desc">
                                        <br/>
                                        <div class="state_and_op" style="margin: 10px">
                                            <div class="state">
                                                用户：<span id="detail_user" name="detail"></span> &nbsp;&nbsp;
                                                单元：<span id="detail_unit" name="detail"></span>&nbsp;&nbsp;
                                                班级：<span id="detail_clazz" name="detail"></span>&nbsp;&nbsp;
                                            </div>
                                        </div>
                                        <div class="state_and_op" style="margin: 10px">
                                            <div class="state"> 视频分类：
                                                <select name="category" id='categoryDetail'>
                                                  <#if categoryList?has_content>
                                                      <#list categoryList as s>
                                                        <option value="${s.name()!}">${s.getDescription()!}</option>
                                                      </#list>
                                                  </#if>
                                                </select>
                                            </div>
                                        </div>
                                        <div class="update_time" style="margin: 10px">
                                            <#if labels?has_content>
                                                <#list labels as s>
                                                  <label style="display: inline-block;"><input type="checkbox" name="add_label" value="${s.name()!}" class="mylabel">${s.getDescription()!}</label>
                                                 </#list>
                                            </#if>
                                        </div>

                                        <div class=""  style="margin: 10px">
                                            评论：<textarea name="comment" id = "comment" style="height: 100px;width: 400px"></textarea>
                                            <button class = "btn btn-primary" id="comment_gen_btn">生成评论</button>
                                        </div>

                                        <div class="update_time"  style="margin: 10px">
                                            <input type="hidden" id="commentAudio" name="commentAudio">
                                            <label for="uploadAudio" class="uploadLabel">上传评论音频</label>
                                            <input type="file" id="uploadAudio" accept="audio/*"/>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <span class="btn btn-primary " data-val="1" id="commit">提交</span>
                    <span class="btn btn-danger " id="concel">取消</span>
                </div>
            </div>
        </div>
    </div>

    <!-- Modal -->
    <div class="modal fade hide" id="courseware_shareurl" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title" style="text-align:center;">二维码</h4>
                </div>
                <div class="modal-body">
                    <div id="sharecode_box" style="text-align: center;"></div>
                </div>
            </div>
        </div>
    </div>


    <script type="text/javascript">
        var step1 = ["OldFriend", "NewFriend", "Other"];
        var step2 = ["PracticeMore", "Pronunciation", "Fluency", "ActionMatch"];
        var step3 = ["Word", "Whole"];
        var step1_obj = {
            "OldFriend" : ["恭喜XX小朋友第一次上榜！", "Wow，新面孔！"],
            "NewFriend": ["XX小朋友Again！", "XX小朋友已经连续N天上榜啦！太厉害了~", "又看见XX小盆友了~老师为你开心！"],
            "Other": ["非常有特色的视频呢~ 大家都很喜欢!", "欢迎你登上排行榜！", "哇！XX小朋友的视频被点击了N次~"]
        };

        var step2_obj = {
            "PracticeMore":["老师看见你今天练习了很多遍！真是太棒了！", "老师看到你认真练习了很多次，怪不得你读的这么棒~", "孰能生巧，多多练习果然让你的提高非常大！"],
            "Pronunciation":["你的语音语调都非常到位！", "你读英语的时候太好听了~老师非常享受", "语音非常棒！"],
            "Fluency":["说的非常流利哟，一看就是没少练习！", "流利度很好哟，是英语基础扎实的同学。"],
            "ActionMatch":["你的表演真棒! 大家都很喜欢你的表情动作。", "表演非常有创意！", "表情动作可爱~视频效果超赞！"]
        };
        var step3_obj = {
            "Word" :["“XX”单词的读音需要多多跟读哟~",  "“XX”单词应该注意。。。", "优秀的你不能放过每一个生词哟~ XX词要多听和练哈！", "XX是XX的意思，也可以用XX替代。"],
            "Whole":["语音语调可以注意多多模仿AI老师哈~这样你的英语就会更地道啦！", "尾音不要拖得过长，更加简短有力就更棒了！",  "英语是像歌曲，流水的语言，注意发音的连贯性哟，多听多练！"]
        };
        Date.prototype.format = function (format) {
            var o = {
                "M+": this.getMonth() + 1, //month
                "d+": this.getDate(),    //day
                "h+": this.getHours(),   //hour
                "m+": this.getMinutes(), //minute
                "s+": this.getSeconds(), //second
                "q+": Math.floor((this.getMonth() + 3) / 3),  //quarter
                "S": this.getMilliseconds() //millisecond
            }
            if (/(y+)/.test(format)) format = format.replace(RegExp.$1,
                    (this.getFullYear() + "").substr(4 - RegExp.$1.length));
            for (var k in o) if (new RegExp("(" + k + ")").test(format))
                format = format.replace(RegExp.$1,
                        RegExp.$1.length == 1 ? o[k] :
                                ("00" + o[k]).substr(("" + o[k]).length));
            return format;
        }

        function pagePost(pageNumber) {
            $("#pageNumber").val(pageNumber);
            $("#frm").submit();
        }

        $(function () {
            $('#comment_gen_btn').on('click', function () {
                var valueArr = [];
                var elemArr = $("input[name='add_label']");
                for(var i = 0 ; i < elemArr.length ; i++){
                    if(elemArr.eq(i).is(':checked')){
                        valueArr.push(elemArr.eq(i).val())
                    }
                }
                if (valueArr == null || valueArr.length == 0) {
                    return;
                }
                var labelVal = valueArr.join(",");
                var commentVal = "";
                for(var i = 0; i < step1.length; i ++) {
                    if (labelVal.indexOf(step1[i]) >= 0) {
                        var key = step1[i];
                        commentVal += step1_obj[key][Math.floor(Math.random() * step1_obj[key].length)];
                    }
                }

                for(var i = 0; i < step2.length; i ++) {
                    var key = step2[i];
                    if (labelVal.indexOf(key) >= 0) {
                        commentVal += step2_obj[key][Math.floor(Math.random() * step2_obj[key].length)];
                    }
                }

                for(var i = 0; i < step3.length; i ++) {
                    var key = step3[i];
                    if (labelVal.indexOf(key) >= 0) {
                        commentVal += step3_obj[key][Math.floor(Math.random() * step3_obj[key].length)];
                    }
                }
                if (commentVal == null || commentVal == "") {
                    return;
                }
                $("#comment").val(commentVal);
            });
            $('button[name=detail]').on('click', function () {
                var dataId = $(this).attr("data-id");
                $.ajax({
                    type: "get",
                    url: "/chips/user/video/detail.vpage",
                    data: {
                        id: dataId,
                    },
                    success: function (data) {
                        if (data.success) {
                            $("#detail_id").val(data.data.id);
                            if (data.data.video != null && data.data.video != '') {
                                $("#detail_video").attr("src", data.data.video);
                            }
                            if (data.data.statusName != null && data.data.statusName != '') {
                                $("#detail_status").text(data.data.statusName);
                            }

                            $("#detail_user").text(data.data.userName + "(" + data.data.userId + ")");
                            $("#detail_unit").text(data.data.lessonName);
                            if (data.data.clazz == 2) {
                                $("#detail_clazz").text("Winston班");
                            } else {
                                $("#detail_clazz").text("Hailey班");
                            }

                            if (data.data.updateTime != null) {
                                var date = new Date(data.data.updateTime);
                                $("#detail_date").text(date.format("yyyy-MM-dd hh:mm:ss"));
                            }
                            if (data.data.category != null) {
                                $("#categoryDetail").val(data.data.category);
                            }
                            $("#commentAudio").val("");
                            $("#uploadAudio").val("");
                            if (data.data.commentAudio != null && data.data.commentAudio != '') {
                                $("#commentAudio").val(data.data.commentAudio);
                                $(".uploadLabel").text("更新评论音频")
                            }

                            $("#comment").val("");
                            if (data.data.comment != null) {
                                $("#comment").val(data.data.comment);
                            }
                            var elemArr = $(".mylabel");
                            for(var i = 0 ; i < elemArr.length ; i++){
                                elemArr.eq(i).prop("checked", false);
                                if (data.data.labels != null && data.data.labels.length > 0) {
                                    for(var k = 0; k < data.data.labels.length; k ++) {
                                        var v = data.data.labels[k];
                                        if (v == elemArr.eq(i).val()) {
                                            elemArr.eq(i).prop("checked", true);
                                        }
                                    }
                                }
                            }
                            $('#courseware_detail').modal('show');
                        } else {
                            alert(data.info);
                        }
                    }
                });
            });


            $('button[name=shareurl]').on('click', function () {
                var url = $(this).data('url');
                $('#courseware_shareurl').modal('show');
                $("#sharecode_box").html('');
                var codeImgSrc = "https://www.17zuoye.com/qrcode?m="+url;

                var imgObj = new Image();
                imgObj.src = codeImgSrc;
                imgObj.style.width = "200px";
                imgObj.style.height = "200px";

                $("#sharecode_box").append(imgObj)


            });


            $("#concel").on('click', function () {
                $('#courseware_detail').modal('hide');
            });

            $("#commit").on('click', function () {
                var id = $("#detail_id").val();
                var category = $("#categoryDetail").val();
                var comment = $("#comment").val();
                var valueArr = [];
                var commentAudio = $("#commentAudio").val();
                var elemArr = $("input[name='add_label']");
                for(var i = 0 ; i < elemArr.length ; i++){
                    if(elemArr.eq(i).is(':checked')){
                         valueArr.push(elemArr.eq(i).val())
                    }
                }
                $.ajax({
                    type: "POST",
                    url: "/chips/user/video/comment.vpage",
                    data: {
                        id: id,
                        category: category,
                        commentAudio: commentAudio,
                        labels: valueArr.join(","),
                        comment: comment
                    },
                    success: function (data) {
                        if (data.success) {
                            alert("操作成功");
                        } else {
                            alert(data.info);
                        }
                        $('#courseware_detail').modal('hide');
                        location.reload();
                    }
                });
            });

            //
            $("#uploadAudio").change(function(){
                // 文件元素
                var file = document.getElementById("uploadAudio");
                // 通过FormData将文件转成二进制数据
                var formData = new FormData();
                // 将文件转二进制
                formData.append('file', file.files[0]);
                $.ajax({
                    url: '/chips/user/video/comment/audioupload.vpage',
                    type: 'POST',
                    data: formData,                    // 上传formdata封装的数据
                    dataType: 'JSON',
                    cache: false,                      // 不缓存
                    processData: false,                // jQuery不要去处理发送的数据
                    contentType: false,                // jQuery不要去设置Content-Type请求头
                    success:function (data) {           //成功回调
                        console.log(data);
                        if (data.success) {
                            $("#commentAudio").val(data.url);
                            alert('上传成功');
                        } else {
                            alert(data.info);
                        }
                    }
                });
            });

            $("#book").on("change", function () {
                var bookId = this.value;
                $.ajax({
                    type: "POST",
                    url: "/chips/user/video/examine/bookUnit.vpage",
                    data: {
                        bookId: bookId
                    },
                    error: function (XMLHttpRequest) {
                        alert(XMLHttpRequest.readyState)
                        alert(XMLHttpRequest.status)
                    },
                    success: function (data) {
                        var units = data.unitList;
                        console.log(units);
                        $("#unitSelect").html("");
                        if (units.length == 0) {
                            $("#unitSelect").append("<option>" + "no unit" + "</option>");
                        } else {
                            for (var i = 0; i < units.length; i++) {
                                $("#unitSelect").append("<option value='" + units[i].id + "'>" + units[i].name + "</option>");
                            }
                        }
                    }
                });
            });


        });




    </script>
</@layout_default.page>