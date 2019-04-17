<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='课件大赛审核 ' page_num=3>
<style type="text/css">
    @charset "UTF-8";body,html{height:100%;width:100%}
    body,button,input,select,textarea{font-family:TrebuchetMS,Rotobo,"Microsoft YaHei",sans-serif}
    body,dd,dl,dt,form,h1,h2,h3,h4,h5,h6,html,li,ol,p,select,ul{margin:0;padding:0}
    li,ol,ul{list-style-type:none}
    em,i{font-style:normal}
    a{color:#333}
    a{text-decoration:none}
    a img{border:0 none}
    input:-ms-input-placeholder,input::-moz-placeholder,input::-webkit-input-placeholder{color:#999}
    input:focus:-ms-input-placeholder,input:focus::-moz-placeholder,input:focus::-webkit-input-placeholder{color:transparent}
    button,input[type=button],input[type=email],input[type=number],input[type=password],input[type=phone],input[type=search],input[type=submit],input[type=text],textarea{-webkit-appearance:none;-moz-appearance:none;-webkit-border-image:none;border-image:none;-webkit-border-radius:0;border-radius:0;-webkit-box-sizing:border-box;box-sizing:border-box;outline:0}
    button,input[type=button],input[type=reset],input[type=submit]{cursor:pointer;-webkit-appearance:button}
    button::-moz-focus-inner,input::-moz-focus-inner{border:0;padding:0;outline:0}
    table{border-collapse:collapse;border-spacing:0}
    td{vertical-align:top}
    .hidden{display:none!important;visibility:hidden}
    .visuallyhidden{border:0;clip:rect(0 0 0 0);height:1px;margin:-1px;overflow:hidden;padding:0;position:absolute;width:1px}
    .visuallyhidden.focusable:active,.visuallyhidden.focusable:focus{clip:auto;height:auto;margin:0;overflow:visible;position:static;width:auto}
    .invisible{visibility:hidden}
    .clearfix:after,.clearfix:before{content:"";display:table}
    .clearfix:after{clear:both}
    .clearfix{zoom:1}
    @media print{*{background:0 0!important;color:#000!important;text-shadow:none!important;filter:none!important;-ms-filter:none!important}
        a,a:visited{text-decoration:underline}
        a[href]:after{content:" (" attr(href) ")"}
        abbr[title]:after{content:" (" attr(title) ")"}
        .ir a:after,a[href^="#"]:after,a[href^="javascript:"]:after{content:""}
        blockquote,pre{border:1px solid #999;page-break-inside:avoid}
        thead{display:table-header-group}
        img,tr{page-break-inside:avoid}
        img{max-width:100%!important}
        @page{margin:.5cm}
        h2,h3,p{orphans:3;widows:3}
        h2,h3{page-break-after:avoid}}
    html{background-color:#ebebf3}
    .header .title{font-size:34px;color:#fff;padding-top:30px}
    .main_content_detail .courseware_list{margin:0 auto; padding: 20px;}
    .main_content_detail .courseware_list .inner{margin:0 auto}
    .main_content_detail .courseware_list .inner .item{background:0 0;min-height:130px;clear:both}
    .main_content_detail .courseware_list .inner .item .head_img{float:left}

    .main_content_detail .courseware_list .inner .item .content_desc{ width:240px;float:left;margin-left: 20px;line-height:28px}
    .main_content_detail .courseware_list .inner .item .content_desc .title{overflow: hidden;text-overflow: ellipsis;white-space: nowrap;}
    .main_content_detail .courseware_list .inner .item .content_desc .del_btn,.main_content_detail .courseware_list .inner .item .content_desc .state_and_op .edit_btn,.main_content_detail .courseware_list .inner .item .content_desc .state_and_op .state{float:left}
    .main_content_detail .courseware_list .inner .item .content_desc .edit_btn{margin-left:100px;font-size:18px}
    .main_content_detail .courseware_list .inner .item .content_desc .edit_btn i.edit{background:url(../images/edit_icon.png) no-repeat;display:inline-block;width:16px;height:18px;position:relative;top:-2px;vertical-align:middle;margin-right:4px}
    .main_content_detail .courseware_list .inner .item .content_desc .del_btn{margin-left:25px;font-size:18px}
    .main_content_detail .courseware_list .inner .item .content_desc .del_btn i.del{background:url(../images/del_icon.png) no-repeat;display:inline-block;width:16px;height:18px;position:relative;top:-2px;vertical-align:middle;margin-right:4px}
    .main_content_detail .courseware_list .inner .item .content_desc .update_time{clear:both;}
    .main_content_detail .courseware_list .inner .item .content_count{width:200px;float:left;margin-left:22px;text-align:left;line-height:28px}
    .main_content_detail .courseware_list .inner .item .content_count .stars .star{position:relative;top:6px;vertical-align:middle;float:left;background:url(../images/one_star.png) no-repeat;display:inline-block;width:15px;height:14px;margin-right:5px}
    .main_content_detail .courseware_list .inner .item .content_count .stars .star.half{background:url(../images/half_star.png) no-repeat}
    .main_content_detail .courseware_list .inner .item .content_count .stars .desc{display:inline-block}
    .course .per_view_content{background-color:#fff;margin:0 auto;padding:22px;}
    .course .per_view_content .per_view_header{margin-bottom:20px}
    .course .per_view_content .title{display: inline-block;margin-right:20px; font-size:22px; cursor:pointer}
    .course .per_view_content .title.active{color:#0088cc}
    .course .per_view_content .per_view_box{width:100%;}
    .course .per_view_content .per_view_box .view_content{min-height: 200px;height:auto; border:4px dashed #eee; display: none;}
    .course .per_view_content .per_view_box .view_content.view_content2{height:600px;overflow: hidden;}
    .course .per_view_content .per_view_box .view_content img {display: block; max-width: 100%;}
    .course .per_view_content .per_view_box .view_content iframe{width:100%; height: 100%;}
    .course .teach_design{margin:0 auto;padding:22px;}
    .course .teach_design .title{font-size:22px;margin-bottom:20px}
    .course .teach_design .desc{margin:0 auto}
    .course .teach_design .desc textarea{background-color:#dbe0ee;border:none;resize:none;display:block;width:100%;height:143px;font-size:14px}
</style>
<div id="main_container" class="span9">
    <legend>课件大赛审核</legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well" style="font-size: 12px;">
                <form id="frm" class="form-horizontal" action="/crm/courseware/contest/examine/workslist.vpage">
                    <input type="hidden" id="pageNumber" name="pageNumber" value="1">
                    年级：
                    <select id="clazzLevel" name="clazzLevel" style="font-size: 12px;width: 100px;">
                        <option value="0" <#if clazzLevel ?? || clazzLevel == 0> selected</#if>>全部</option>
                        <option value="1" <#if clazzLevel ?? && clazzLevel == 1> selected</#if>>一年级</option>
                        <option value="2" <#if clazzLevel ?? && clazzLevel == 2> selected</#if>>二年级</option>
                        <option value="3" <#if clazzLevel ?? && clazzLevel == 3> selected</#if>>三年级</option>
                        <option value="4" <#if clazzLevel ?? && clazzLevel == 4> selected</#if>>四年级</option>
                        <option value="5" <#if clazzLevel ?? && clazzLevel == 5> selected</#if>>五年级</option>
                        <option value="6" <#if clazzLevel ?? && clazzLevel == 6> selected</#if>>六年级</option>
                    </select>
                    册别：
                    <select id="term" name="term" style="font-size: 12px;width: 100px;">
                        <option value="0" <#if term ?? && term == 0> selected</#if>>全部</option>
                        <option value="1" <#if term ?? && term == 1> selected</#if>>上册</option>
                        <option value="2" <#if term ?? && term == 2> selected</#if>>下册</option>
                    </select>
                    状态：
                    <select id="examineStatus" name="examineStatus" style="font-size: 12px;width: 100px;">
                        <#if examineStatusList?has_content>
                            <#list examineStatusList as s>
                                <option value="${s.name()!}" <#if examineStatus ?? && examineStatus == s.name()> selected</#if>>${s.getDescription()!}</option>
                            </#list>
                        </#if>
                    </select>
                    用户ID：
                    <input type="text" name="userId" style="height: 29px; width:83px" <#if userId ?? && userId != ''> value=${userId} </#if> >
                    课件标题：
                    <input type="text" name="title" style="height: 29px;" placeholder="模糊搜索" <#if title ?? && title != ''> value=${title} </#if> >
                    更新开始时间： <input id="startDate" name="startDate" style="height: 29px;" class="input-medium" type="text" <#if startDate ?? && startDate != ''> value="${startDate}" </#if> readonly />
                    结束时间： <input id="endDate" name="endDate" style="height: 29px;" class="input-medium" type="text" <#if endDate ?? && endDate != ''> value="${endDate}" </#if> readonly />
                    <button class="btn btn-primary">查 询</button>
                </form>

            </div>
        </div>
    </div>
    <div id="data_table_journal">
        <table class="table table-striped table-bordered">
            <tr>
                <td>课件标题</td>
                <td>提交人</td>
                <td>更新时间</td>
                <td>状态</td>
                <td>审核人</td>
                <td>操作</td>
            </tr>
            <#if pageData.content?? && pageData.content?size gt 0>
                <#list pageData.content as e >
                    <tr>
                        <td>${e.title!}</td>
                        <td>${e.teacher!}</td>
                        <td>${e.date!}</td>
                        <td>${e.examineStatusDesc!}</td>
                        <td>${e.examiner!}</td>
                        <td>
                            <#if e.examineStatus?? && e.examineStatus == 'WAITING' >
                                <button type="button" name="fetch" data-id = "${e.id!}" class="btn btn-primary">领取</button>
                            <#else>
                                <button type="button" name="detail" data-id = "${e.id!}" class="btn btn-primary">详情</button>
                            </#if>
                        </td>
                    </tr>
                </#list>
            <#else ><tr><td colspan="6"><strong>暂无数据</strong></td> </tr>
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
        <li>共 ${pageData.totalPages!} 页</li>
    </ul>


    <!-- Modal -->
    <div class="modal fade hide" id="courseware_detail" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" style="width: 800px;margin-left:-400px;">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title" id="myModalLabel">课件审核</h4>
                    <input type="hidden" id="detail_id"/>
                </div>
                <div class="modal-body" style="max-height: 510px; overflow-y: scroll;">
                    <div class="main_content_detail">
                        <div class="courseware_list">
                            <div class="inner">
                                <div class="item">

                                    <div class="head_img">
                                        <img alt="" id="detail_bookImage" style="display:inline-block;width:115px; height: 80px">
                                    </div>

                                    <div class="content_desc">
                                        <#--<div class="infoPic">-->
                                            <#--<img data-bind="attr: { src: coursewareFileImages[0] }" alt="">-->
                                        <#--</div>-->
                                        <div class="title">作品标题：<span id="detail_title"></span></div>
                                        <div class="state">状态：<span id="detail_statusDesc"></span></div>
                                        <div class="update_time">更新时间：<span class="time" id="detail_date"></span></div>
                                        <div class="author">作者：<span class="author" id="detail_teacherName"></span>
                                            (<span class="authorId" id="detail_teacherId"></span>)</div>
                                        <div class="description">作品简介：<span id="detail_description"></span></div>
                                    </div>

                                    <div class="content_count">
                                        <!--<div class="desc">册别：<span id="detail_term"></span></div>-->
                                        <div class="desc"><span style="display: inline-block">学科：</span><span style="display: inline-block" id="detail_subject"></span></div>
                                        <div class="desc">版本：<span id="detail_version"></span></div>
                                        <div class="desc">年级：<span id="detail_clazzLevel"></span></div>
                                        <!--<div class="desc">教材：<span id="detail_bookName"></span></div>-->
                                        <div class="desc">单元：<span id="detail_unitName"></span></div>
                                        <div class="desc">课时：<span id="detail_lessonName"></span></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="course">
                            <div class="description" style="padding:0 22px">作品简介：<span id="detail_description"></span></div>
                        </div>
                        <div class="course">
                            <div class="per_view_content">
                                <div class="per_view_header" id="previewTitle" style="clear:both;overflow: hidden">
                                    <#--<div class="title">教学课件(图片)<a id="detailFile" style="color: #0088cc;font-size: 14px;">下载</a></div>-->
                                    <div class="title">教学课件(ppt或zip)</div>
                                    <div class="title">教学设计</div>
                                    <div class="title">教学图片</div>
                                    <div class="title">作品奖项</div>
                                    <div class="title">资源列表</div>
                                </div>
                                <div class="per_view_box" id="previewContainer">
                                    <#--<div class="view_content view_content1" id="coursePreviewContainer1"></div>-->
                                    <div class="view_content view_content2" id="coursePreviewContainer2"></div>
                                    <div class="view_content view_content2" id="wordPreviewContainer"></div>
                                    <div class="view_content view_content1" id="imgagePreviewContainer"></div>
                                    <div class="view_content view_content1" id="awardPreviewContainer"></div>
                                    <div class="view_content view_content2" id="resourceListContainer" style="display: block;">
                                        <a id="downloadResourcePPT" style="font-size:15px"
                                               href="">下载教学课件</a>、
                                        <a id="downloadResourceDOC" style="font-size:15px"
                                               href="">下载教学设计</a>
                                        <br/>
                                        <br/>

                                        <span>选择需要重新上传的资源,保存后生效</span><br/>
                                        教学课件：<input type="file" accept=".ppt,.pptx,.zip,.ZIP,.rar,.RAR" id="aliyunInput"/> <span id="pptMsg"></span><br/>
                                        <form action="/crm/courseware/contest/resavefile.vpage" enctype="multipart/form-data" method="post" onsubmit="return reSaveFileSubmit(this)">
                                            <input type="hidden" name="coursewareId"/>
                                            <input type="hidden" name="pptUrl" id="pptUrl"/>
                                            <input type="hidden" name="pptName" id="pptName"/>
                                            教学设计：<input type="file" name="doc" accept=".doc,.docx"/>  <span id="docMsg"></span><br/>
                                            <button type="submit">保存资源</button>
                                        </form>

                                        <br/><br/>
                                        <form action="/crm/courseware/contest/updateFileName.vpage" method="post" onsubmit="return updateResouceName(this)">
                                            <input type="hidden" name="coursewareId"/>
                                            教学课件名称：<input type="text" name="updatePptName"/><br>
                                            教学设计名称：<input type="text" name="updateDocName"/><br>
                                            <button type="submit">修改资源名称</button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary" id="detail_pass" type="examine">通过</button>
                    <button type="button" class="btn btn-primary" id="detail_reject" type="examine">驳回</button>
                    <button type="button" class="btn btn-primary" data-dismiss="modal" id="detail_closeBtn">关闭</button>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="courseware_reject" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title" id="exampleModalLabel">确认驳回</h4>
                </div>
                <div class="modal-body">
                    <form>
                        <div class="form-group">
                            <label for="message-text" class="control-label">请填写驳回原因:</label>
                            <textarea class="form-control" style="margin: 0px 0px 10px;width: 536px;height: 120px;resize:none;" id="reject_description" placeholder="驳回原因不超过100个字" maxlength="100"></textarea>
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary" id="reject_confirm">确认</button>
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>

    <script type="text/javascript">
        function pagePost(pageNumber) {
            $("#pageNumber").val(pageNumber);
            $("#frm").submit();
        }
        $(function () {
            $("#startDate,#endDate").datepicker({
                dateFormat: 'yy-mm-dd',  //日期格式，自己设置
                monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
                defaultDate: new Date(),
                numberOfMonths: 1,
                changeMonth: false,
                changeYear: false,
                onSelect: function (selectedDate) {
                }
            });


            $('button[name=fetch]').on('click', function () {
                var dataId = $(this).attr("data-id");
                $.ajax({
                    type: "post",
                    url: "/crm/courseware/contest/getworks.vpage",
                    data: {
                        id: dataId,
                    },
                    success: function (data) {
                        if (data.success) {
                            alert("领取成功");
                            window.location.reload();
                        } else {
                            alert(data.info);
                        }
                    }
                });
            });

            $('button[name=detail]').on('click', function () {
                var dataId = $(this).attr("data-id");
                $("input[name='coursewareId']").val(dataId);
                $.ajax({
                    type: "get",
                    url: "/crm/courseware/contest/works/detail.vpage",
                    data: {
                        id: dataId,
                    },
                    success: function (data) {
                        if (data.success) {
                            for(var e in data) {
                                if (e == 'id') {
                                    $("#detail_id").val(data[e]); // 课件Id
                                    continue;
                                }
                                $("#detail_"+ e).text("");
                                if (e == 'term') {
                                    if (data[e] == 1) {
                                        $("#detail_"+ e).text("上册");
                                    }

                                    if (data[e] == 2) {
                                        $("#detail_"+ e).text("下册");
                                    }
                                    continue;
                                }
                                $("#detail_"+ e).text(data[e] || '');

                                //状态控制按钮可见
                                if (data.status == "FAILED") {
                                    $("#detail_reject").hide();
                                    $("#detail_pass").hide();
                                } else if (data.status == "PASSED") {
                                    $("#detail_reject").show();
                                    $("#detail_pass").hide();
                                } else {
                                    $("#detail_closeBtn").hide();
                                }
                            }
                            // 封面图
                            if (data.image != null && data.image != '') {
                                $("#detail_bookImage").attr("src", data.coverUrl);
                            }
                            // 下载课件
                            if (data.coursewareFile != null && data.coursewareFile != '') {
                                $("#detailFile").attr("href", data.coursewareFile);
                            }

                            //教学课件图片(data.coursewareFileImages)
                            /*if(data.coursewareFileImages && data.coursewareFileImages.length != 0){
                                var courseTemp = "";
                                for(var i = 0; i < data.coursewareFileImages.length; i++){
                                    courseTemp += "<img src=" + data.coursewareFileImages[i] + " />";
                                }
                                $("#coursePreviewContainer1").html(courseTemp);
                            } else {
                                $("#coursePreviewContainer1").html('教学课件内容为空');
                            }*/

//                            if  (['.ppt', '.pptx'].indexOf(data.coursewareFile.toLowerCase().substring(data.coursewareFile.lastIndexOf('.'))) > -1) {
                                // 教学设计ppt(data.coursewareFile)
                                var i = "";
                                if (data.coursewareFile.indexOf('https') > -1 && data.coursewareFile.indexOf('v.17xueba.com') > -1){
                                    i = "16939&ssl=1&n=5";
                                } else if (data.coursewareFile.indexOf('https') == -1 && data.coursewareFile.indexOf('v.17xueba.com') > -1){
                                    i = "16942";
                                } else if (data.coursewareFile.indexOf('https') > -1 && data.coursewareFile.indexOf('oss-data.17zuoye.com') > -1){
                                    i = "16940&ssl=1&n=5";
                                } else {
                                    i = "16943";
                                }
                                var coursewareUrl = 'http://ow365.cn/?i=' + i + '&furl=' + window.encodeURIComponent(data.coursewareFile);
                                var coursewareTemp = "<iframe src='" + coursewareUrl + "'></iframe>";
                                $("#coursePreviewContainer2").html(coursewareTemp);
//                            } else {
//                                $("#coursePreviewContainer2").html("教学课件为压缩包格式，请<a style='color: #0088cc' href=" + data.coursewareFile + ">下载查看</a>");
//                            }

                            // 教学设计word(data.wordUrl)
                            if (data.wordUrl) {
                                var i = "";
                                if (data.wordUrl.indexOf('https') > -1 && data.wordUrl.indexOf('v.17xueba.com') > -1){
                                    i = "16939&ssl=1&n=5";
                                } else if (data.wordUrl.indexOf('https') == -1 && data.wordUrl.indexOf('v.17xueba.com') > -1){
                                    i = "16942";
                                } else if (data.wordUrl.indexOf('https') > -1 && data.wordUrl.indexOf('oss-data.17zuoye.com') > -1){
                                    i = "16940&ssl=1&n=5";
                                } else {
                                    i = "16943";
                                }
                                var wordUrl = 'http://ow365.cn/?i=' + i + '&furl=' + window.encodeURIComponent(data.wordUrl);
                                var wordTemp = "<iframe src='" + wordUrl + "'></iframe>";
                                $("#wordPreviewContainer").html(wordTemp);
                            }

                            // 教学图片(data.pictureUrlList)
                            var imagesTemp = "";
                            if(data.pictureUrlList && data.pictureUrlList.length != 0){
                                for(var j = 0; j < data.pictureUrlList.length; j++){
                                    imagesTemp += "<img src=" + data.pictureUrlList[j].url + " />";
                                }
                            }
                            $("#imgagePreviewContainer").html(imagesTemp);

                            // 作品奖项
                            if (data.awardLevelId > 0) {
                                var awardTemp = "<div>" +
                                        "<p>奖项级别：" + data.awardLevelName + "</p>" +
                                        "<p>荣誉名称：" + data.awardIntroduction + "</p>" +
                                        "<p><span>奖状照片：</span><img src=" + data.awardPreview.url + " /></p>" +
                                        "</div>";
                                $("#awardPreviewContainer").html(awardTemp);
                            } else {
                                $("#awardPreviewContainer").html('未上传奖状');
                            }


                            // 资源列表页面
                            if (data.coursewareFile != null) {
                                $("#downloadResourcePPT").prop("href", data.coursewareFile);
                            }
                            if(data.wordUrl != null){
                                $("#downloadResourceDOC").prop("href", data.wordUrl);
                            }

                            var updatePptName = (data.pptCoursewareFileName == null || "" === data.pptCoursewareFileName)
                                    ? data.coursewareFileName : data.pptCoursewareFileName;
                            $("input[name='updatePptName']").val(updatePptName);
                            $("input[name='updateDocName']").val(data.wordName);

                            // 初始显示第一个（教学课件）
                            showPreviewBox(0);

                            $('#courseware_detail').modal('show');
                        } else {
                            alert(data.info);
                        }
                    }
                });
            });


            $("#detail_pass").on('click', function(){
                $.ajax({
                    type: "POST",
                    url: "/crm/courseware/contest/examineworks.vpage",
                    data: {
                        id: $("#detail_id").val(),
                        pass:true
                    },
                    success: function (data) {
                        if (data.success) {
                            $("label[name=detail]").text("");
                            $("#detail_id").val("");
                            $('#courseware_detail').modal('hide');
                            window.location.reload();
                        } else {
                            alert(data.info);
                        }
                    }
                });
            });

            $("#detail_reject").on('click',function () {
                $('#courseware_detail').modal('hide');
                $('#courseware_reject').modal('show');

            });

            $("#reject_confirm").on('click', function () {
                var description = $('#reject_description').val();
                if (description == null || description.trim() == '') {
                    alert("驳回原因为空");
                }
                if (description.trim().length > 100) {
                    alert("驳回原因不能超过100个字");
                }
                $.ajax({
                    type: "POST",
                    url: "/crm/courseware/contest/examineworks.vpage",
                    data: {
                        id: $("#detail_id").val(),
                        pass:false,
                        description:description.trim()
                    },
                    success: function (data) {
                        if (data.success) {
                            $("label[name=detail]").text("");
                            $("#detail_id").val("");
                            $('#courseware_reject').modal('hide');
                            $('#courseware_detail').modal('hide');
                            window.location.reload();
                        } else {
                            alert(data.info);
                        }
                    }
                });
            });

            $("#previewTitle").on('click', '.title', function () {
                showPreviewBox($(this).index());
            });

            function showPreviewBox(index) {
                $('#previewContainer').find('.view_content').eq(index).show().siblings().hide();
                $("#previewTitle").find('.title').eq(index).addClass('active').siblings().removeClass('active');
            }
        });

        function reSaveFileSubmit(form) {
            $(form).ajaxSubmit(function (data) {
                if (data.success) {
                    alert("提交成功,请刷新后检查确认");
                } else {
                    alert(data.info);
                }
            });
            return false;
        }

        function updateResouceName(form) {
            $(form).ajaxSubmit(function (data) {
                if (data.success) {
                    alert("提交成功,请刷新后检查确认");
                } else {
                    alert(data.info);
                }
            });
            return false;
        }
        $(function () {
            $("input[name='doc']").change(function (e) {
                $("#docMsg").html("上传完成,保存后生效");
            })
            $("#aliyunInput").change(function (e) {
                $("#pptMsg").html("上传中...");
                var file = e.target.files[0];
                var fileOriginName = file.name;
                var index = fileOriginName.lastIndexOf(".");
                var ext = fileOriginName.substring(index + 1, fileOriginName.length);

                console.log(file.name);
                $.ajax({
                    url: "/crm/courseware/contest/getsignature.vpage",
                    data: {
                        ext: ext
                    },
                    type:"get",
                    async: false,
                    success:function (data) {
                        var signResult = data.data;
                        let store  = new OSS({
                            accessKeyId: signResult.accessid,
                            accessKeySecret: signResult.accessKeySecret,
                            endpoint: signResult.endpoint,
                            bucket: signResult.bucket
                        });

                        var ossPath = signResult.dir + signResult.filename + "." + ext;
                        store.multipartUpload(ossPath, file).then(function (result) {
                            $("#pptMsg").html("上传完成,保存后生效");
                            console.log("https://" + signResult.videoHost + ossPath)
                            $("#pptUrl").val("https://" + signResult.videoHost + ossPath);
                            $("#pptName").val(fileOriginName);
                        }).catch(function (err) {
                            $("#pptMsg").html("上传失败,请重新选择文件");
                            console.log(err);
                        });
                    }
                });
            });
        });
    </script>
</@layout_default.page>