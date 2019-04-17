<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑资源" page_num=9>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/kindeditor/kindeditor-all.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/validator.min.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/mizar/mizar.css" rel="stylesheet">
    <#--<@app.css href="/public/plugin/admineditor/css/editor-min.css"/>-->
<script src="/public/plugin/ueditor-1-4-3/third-party/zeroclipboard/ZeroClipboard.min.js"
        type="text/javascript"></script>
<script type="text/javascript" src="/public/plugin/wxeditor/js/colorpicker-min.js"></script>


<style>
    .ckfield-controls {
        padding-top: 5px;
    }

    .reqiured {
        color: red;
        font-size: 20px;
    }

</style>

<!-- 添加精选笔记的窗口 -->
<div id="add_choice" class="modal fade hide" aria-hidden="true" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3 class="modal-title">添加精选笔记</h3>
            </div>
            <div class="modal-body" >
                <form id="add-choice-frm" action="addChoiceNote.vpage" method="post" role="form" style="max-height: 400px;">
                    <div class="form-group" style="height:800px;display: none">
                        <label class="col-sm-2 control-label"><strong>对话序号</strong></label>
                        <div class="controls">
                            <input type="text" id="id" class="form-control" maxlength="20">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="">提问者名称</label>
                        <input class="form-control" type="text" id="questionerUserName" maxlength="50" required>
                    </div>
                    <div class="form-group">
                        <label class="">提问者头像</label>
                        <label class="col-sm-2 control-label">(图片尺寸应为100-100px)</label>
                        <input type="file" id="questionerPictureUrl" onchange="previewHandle(this, 'questioner-image')"/>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <img id="questioner-image" src="" width="210" height="140">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="">问题</label>
                        <textarea style="margin: 0px 0px 10px; width: 350px; height: 71px;" class="form-control" type="text" id="question" maxlength="1000" required></textarea>
                        <div class="help-block with-errors"></div>
                    </div>
                    <div class="form-group">
                        <label class="">回答者名称</label>
                        <input class="form-control" type="text" id="answerUserName" maxlength="50" required>
                    </div>
                    <div class="form-group">
                        <label class="">回答者头像</label>
                        <label class="col-sm-2 control-label">(图片尺寸应为100-100px)</label>
                        <input type="file" id="answerPictureUrl" onchange="previewHandle(this, 'answer-image')" />
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <img id="answer-image" src="" width="210" height="140">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="">回答</label>
                        <textarea style="margin: 0px 0px 10px; width: 350px; height: 71px;" class="form-control" type="text" id="answer" maxlength="1000" required></textarea>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-primary" id="confirm-choice-btn">确认</button>
            </div>
        </div>
    </div>
</div>
<!-- 添加课程目录的窗口 -->
<div id="add_catalog" class="modal fade hide" aria-hidden="true" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3 class="modal-title">添加课程目录</h3>
            </div>
            <div class="modal-body" style="overflow: visible;max-height: 800px;">
                <form id="add-catalog-frm" action="addCatalog.vpage" method="post" role="form">
                    <div class="form-group">
                        <label class="">时间</label>
                        <input class="form-control" type="text" id="timeNode" maxlength="20" required> <span class="">格式：HH:mm:dd</span>
                        <div class="help-block with-errors"></div>
                    </div>
                    <div class="form-group">
                        <label class="">描述</label>
                        <input class="form-control" type="text" id="catalogDescribe" maxlength="1000" required>
                        <div class="help-block with-errors"></div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-primary" id="confirm-catalog-btn">确认</button>
            </div>
        </div>
    </div>
</div>

<!-- 添加课程外部链接 -->
<div id="add_outerchain" class="modal fade hide" aria-hidden="true" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3 class="modal-title">添加课程外链</h3>
            </div>
            <div class="modal-body" style="overflow: visible;max-height: 800px;">
                <form id="add-outerchain-frm" action="addOuterchain.vpage" method="post" role="form">
                    <div class="form-group">
                        <label class="">外接标题</label>
                        <input class="form-control" type="text" id="outerchain" maxlength="500" required>
                        <div class="help-block with-errors"></div>
                    </div>
                    <div class="form-group">
                        <label class="">外接url</label>
                        <input class="form-control" type="text" id="outerchainUrl" maxlength="500" required>
                        <div class="help-block with-errors"></div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-primary" id="confirm-outerchain-btn">确认</button>
            </div>
        </div>
    </div>
</div>

<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加/编辑资源&nbsp;&nbsp;
        <#if goods??> (<#if goods.status??>${goods.status.getDesc()!''}<#else>离线</#if>)</#if>
        <a id="back" title="返回" class="btn">
            <i class="icon-share-alt"></i> 返 回
        </a>
        <a title="保存" href="javascript:void(0);" class="btn btn-primary" id="save_info">
            <i class="icon-pencil icon-white"></i> 保 存
        </a>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="info_frm" name="info_frm" enctype="application/x-www-form-urlencoded" action="savegoods.vpage"
                      method="post">
                    <input id="course-id" name="id" value="${course.id!}" type="hidden">
                    <input id="title-image" name="titlePictureUrl" value="${course.titlePictureUrl!}" type="hidden">
                    <input id="video-image" name="videoPictureUrl" value="${course.videoPictureUrl!}" type="hidden">
                    <input id="app-image" name="appPictureUrl" value="${course.appPictureUrl!}" type="hidden">
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="control-label">学科<span class="reqiured">*</span></label>
                            <div class="controls ckfield-controls">
                               <label style="display: inline-block"><input type="checkbox" name="subject" data-subject="101"/> 语文</label>
                               <label style="display: inline-block"><input type="checkbox" name="subject" data-subject="102"/> 数学</label>
                               <label style="display: inline-block"><input type="checkbox" name="subject" data-subject="103"/> 英语</label>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">年级<span class="reqiured">*</span></label>
                            <div class="controls ckfield-controls">
                                <label style="display: inline-block"><input type="checkbox" name="grade" data-grade="1"/> 1年级</label>
                                <label style="display: inline-block"><input type="checkbox" name="grade" data-grade="2"/> 2年级</label>
                                <label style="display: inline-block"><input type="checkbox" name="grade" data-grade="3"/> 3年级</label>
                                <label style="display: inline-block"><input type="checkbox" name="grade" data-grade="4"/> 4年级</label>
                                <label style="display: inline-block"><input type="checkbox" name="grade" data-grade="5"/> 5年级</label>
                                <label style="display: inline-block"><input type="checkbox" name="grade" data-grade="6"/> 6年级</label>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">标题<span class="reqiured">*</span></label>
                            <div class="controls">
                                <input type="text" id="name" name="title" class="form-control input_txt"
                                       value="<#if course??>${course.title!}</#if>" required/>
                                <div class="help-block with-errors"></div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">人物名称<span class="reqiured">*</span></label>
                            <div class="controls">
                                <input type="text" id="name" name="lecturerUserName" class="form-control input_txt"
                                       value="<#if course??>${course.lecturerUserName!}</#if>" required/>
                                <div class="help-block with-errors"></div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">简介<span class="reqiured">*</span></label>
                            <div class="controls">
                                <input type="text" id="name" name="lecturerIntroduction" class="form-control input_txt"
                                       value="<#if course??>${course.lecturerIntroduction!}</#if>" required/>
                                <div class="help-block with-errors"></div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">标签<span class="reqiured">*</span></label>
                            <div class="controls">
                                <#if labels??>
                                    <select id="label" name="label">
                                        <#list labels as c >
                                            <option value="${c.name()!}" <#if course?? && ((course.label!'') == c.name())>
                                                    selected </#if>>${c.getDesc()!}</option>
                                        </#list>
                                    </select>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">解锁消耗<span class="reqiured">*</span></label>
                            <div class="controls">
                                <#if categories?? >
                                    <select id="name" name="price">
                                        <#list categories as c >
                                            <option value="${c.getNum()!}" <#if (course.price??) && ((course.price) == c.getNum())>
                                                    selected </#if>>${c.getName()!}</option>
                                        </#list>
                                    </select>
                                </#if>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">正文<span class="reqiured">*</span></label>
                            <div class="controls">
                            <textarea id="description" name="description" class="intro"
                                      placeholder="请填写课程简介"><#if course.textContent??>${course.textContent}</#if></textarea>
                        </div>
                        <div class="control-group">
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">题图<span class="reqiured">*</span></label>
                            <label class="col-sm-2 control-label">(图片尺寸应为660 * 170px)</label>
                        </div>
                        <div class="control-group">
                            <div class="controls">
                                <input type="file" id="imageSquare">
                            </div>
                        </div>
                        <div class="control-group">
                            <div class="controls">
                                <img id="preview-image" src="" width="210" height="140">
                            </div>
                        </div>
                        <div class="control-group">
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">视频图<span class="reqiured">*</span></label>
                            <label class="col-sm-2 control-label">(图片尺寸应为750-422px)</label>
                        </div>
                        <div class="control-group">
                            <div class="controls">
                                <input type="file" id="videoImageSquare">
                            </div>
                        </div>
                        <div class="control-group">
                            <div class="controls">
                                <img id="videopreview-image" src="" width="210" height="140">
                            </div>
                        </div>
                        <div class="control-group">
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">老师app首页图 </label>
                            <label class="col-sm-2 control-label">(图片尺寸应为300-165px)</label>
                        </div>
                        <div class="control-group">
                            <div class="controls">
                                <input type="file" id="appImageSquare">
                            </div>
                        </div>
                        <div class="control-group">
                            <div class="controls">
                                <img id="apppreview-image" src="" width="300" height="165">
                            </div>
                        </div>
                        <div class="control-group">
                             <label class="col-sm-2 control-label">视频路径<span class="reqiured">*</span></label>
                             <div class="controls">
                                   <input type="text" id="file-url" name="url" class="form-control input_txt"
                                                    value="<#if course??>${course.url!}</#if>" required/>
                                    <div class="help-block with-errors"></div>
                              </div>
                         </div>

                        <div class="control-group">
                            <label>课程目录</label>
                            <div>
                                <table class="table table-striped table-bordered">
                                    <thead>
                                    <tr>
                                        <th style="text-align: center; width: 55px;">序号</th>
                                        <th style="text-align: center; width: 100px;">时间</th>
                                        <th style="text-align: center;width: 150px;">描述</th>
                                        <th style="text-align: center; width: 100px;">操作</th>
                                    </tr>
                                    </thead>
                                    <tbody id="catalog_list">
                                        <#if course.cataloglist?? && course.cataloglist?has_content>
                                            <#list course.cataloglist as catalog>
                                            <tr>
                                                <td>${catalog.id!'0'}</td>
                                                <td>${catalog.timeNode!''}</td>
                                                <td>${catalog.catalogDescribe!''}</td>
                                                <td style="text-align: center;width:120px;">
                                                    <a name="del_catalog" class="btn btn-danger" href="#"data-value="${catalog.id!''}" >删除</a>
                                                </td>
                                            </tr>
                                            </#list>
                                        <#else>
                                         <tr>
                                             <td colspan="8" style="text-align: center;"><strong>No Data Found</strong></td>
                                         </tr>
                                        </#if>
                                    </tbody>
                                    <tfoot>
                                    <tr>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td style="text-align: center;width:120px;">
                                            <button type="button" class="btn btn-info" id="add-catalog-detail">添加 </button>
                                        </td>
                                    </tr>
                                    </tfoot>
                                </table>
                            </div>
                        </div>

                        <div id="choiceNote_list" class="control-group">
                            <label>精品笔记</label>
                            <table class="table table-striped table-bordered">
                                <thead>
                                    <tr>
                                        <th style="text-align: center; width: 55px;">对话序号</th>
                                        <th style="text-align: center; width: 100px;">提问者名称</th>
                                        <th style="text-align: center;width: 150px;">提问者头像</th>
                                        <th style="text-align: center; width: 100px;">问题</th>
                                        <th style="text-align: center; width: 100px;">回答者名称</th>
                                        <th style="text-align: center;width: 150px;">回答者头像</th>
                                        <th style="text-align: center; width: 100px;">回答</th>
                                        <th style="text-align: center; width: 120px;">操作</th>
                                    </tr>
                                </thead>
                                <tbody id="choice_list">
                                    <#if course.choiceNoteList?? && course.choiceNoteList?has_content>
                                        <#list course.choiceNoteList as choiceNote>
                                        <tr>
                                            <td>${choiceNote.id!'0'}</td>
                                            <td>${choiceNote.questionerUserName!''}</td>
                                            <td style="text-align: center;"><img style="width:210px;height:140px;" width="210"
                                                                                 height="140" src="${choiceNote.questionerPictureUrl!''}"></td>
                                            <td>${choiceNote.question!''}</td>
                                            <td>${choiceNote.answerUserName!''}</td>
                                            <td style="text-align: center;"><img style="width:210px;height:140px;" width="210"
                                                                                 height="140" src="${choiceNote.answerPictureUrl!''}"></td>
                                            <td>${choiceNote.answer!''}</td>
                                            <td style="text-align: center;width:120px;">
                                                <a name="update_choice" class="btn btn-info" onclick="updateChoiceDetail(this)">编辑</a>
                                                <a name="del_choice" class="btn btn-danger" href="#"data-value="${choiceNote.id!''}" >删除</a>
                                            </td>
                                        </tr>
                                        </#list>
                                    <#else>
                                        <tr>
                                            <td colspan="8" style="text-align: center;"><strong>No Data Found</strong></td>
                                        </tr>
                                    </#if>
                                </tbody>
                                <tfoot>
                                    <tr>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td style="text-align: center;width:120px;">
                                            <button type="button" class="btn btn-info" id="add-choice-detail">添加 </button>
                                        </td>
                                    </tr>
                                </tfoot>
                            </table>
                        </div>

                        <div class="control-group">
                            <label >外链列表</label>
                            <table class="table table-striped table-bordered">
                                <thead>
                                <tr>
                                    <th style="text-align: center; width: 55px;">序号</th>
                                    <th style="text-align: center; width: 100px;">链接标题</th>
                                    <th style="text-align: center; width: 100px;">链接url</th>
                                    <th style="text-align: center; width: 100px;">操作</th>
                                </tr>
                                </thead>
                                <tbody id="outerchain_list">
                                    <#if course.outerchainList?? && course.outerchainList?has_content>
                                        <#list course.outerchainList as info>
                                        <tr>
                                            <td>${info.id!'0'}</td>
                                            <td>${info.outerchain!''}</td>
                                            <td>${info.outerchainUrl!''}</td>
                                            <td style="text-align: center;width:120px;">
                                                <a name="del_outerchain" class="btn btn-danger" href="#"data-value="${info.id!''}" >删除</a>
                                            </td>
                                        </tr>
                                        </#list>
                                    <#else>
                                        <tr>
                                            <td colspan="8" style="text-align: center;"><strong>No Data Found</strong></td>
                                        </tr>
                                    </#if>
                                </tbody>
                                <tfoot>
                                <tr>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <td style="text-align: center;width:120px;">
                                        <button type="button" class="btn btn-info" id="add-outerchain-detail">添加 </button>
                                    </td>
                                </tr>
                                </tfoot>
                            </table>
                        </div>

                        <div class="control-group">
                            <label class="col-sm-2 control-label">置顶排序</label>
                            <div class="controls">
                                <input type="text" id="display-order" name="topNum" class="form-control"
                                       value="<#if course??>${course.topNum!}</#if>"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">首页展示</label>
                            <div class="controls">
                                <input type="checkbox" id="featuring" name="featuring" data-grade="1"/>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<div id="uploaderDialog" class="modal fade hide" style="width:550px; height: 300px;">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>上传图片</h3>
    </div>
    <div class="modal-body">
        <div style="float: left; width: 280px;">
            <div style="height: 200px; width: 280px;">
                <img id="imgSrc" src="" alt="预览" style="height: 200px; width: 280px;"/>
            </div>
        </div>
        <div style="float: right">
            <div style="display: block;">
                <textarea placeholder="请填写描述" id="uploadDesc" style="resize: none;"></textarea>
            </div>
            <div style="display: block;">
                <a href="javascript:void(0);" class="uploader">
                    <input type="file" name="file" id="uploadFile" accept="image/*" onchange="previewImg(this)">选择素材
                </a>
            </div>
        </div>
        <input type="hidden" id="uploadField" value="photo">
    </div>
    <div class="modal-footer">
        <button title="确认上传" class="uploader" id="upload_confirm">
            <i class="icon-ok"></i>
        </button>
        <button class="uploader" data-dismiss="modal" aria-hidden="true"><i class="icon-trash"></i></button>
    </div>
</div>
<script type="text/javascript">
    var addChoiceNoteFlag = true; // 用于判断添加、修改精选笔记
    $(function () {
        $('#info_frm').validator();

        var options = {
            filterMode: true,
            items: [
                'justifyleft', 'justifycenter',
                'justifyfull', 'insertorderedlist', 'insertunorderedlist',
                'fontname', 'fontsize', '|', 'forecolor', 'hilitecolor', 'bold',
                'italic', 'underline', 'removeformat', '|', 'image',
                'undo', 'redo', '|', 'preview', 'cut', 'copy', '|',
                'link', 'unlink', '|', 'source'
            ],
            allowFileManager: true
        };

        var editor;
        KindEditor.ready(function (K) {
            editor = K.create('#description', options);

            // 设置初始值
            <#if course??>
                editor.html('${(course.textContent!'')?replace('\n','')?replace("'" , "\\'")!''}');
            </#if>
        });

        var b = ["borderTopColor", "borderRightColor", "borderBottomColor", "borderLeftColor"], d = [];
        $.each(b, function (a) {
            d.push(".itembox .wxqq-" + b[a])
        });

        var subjects = '${course.subject!''}'.split(',');
        $("input[name=subject]").each(function (index, field) {
            var subject = $(field).data("subject");
            if ($.inArray(subject.toString(), subjects) >= 0) {
                $(this).prop("checked", true);
            }
        });

        var grades = '${course.grade!''}'.split(',');
        $("input[name=grade]").each(function (index, field) {
            var grade = $(field).data("grade");
            if ($.inArray(grade.toString(), grades) >= 0) {
                $(field).prop("checked", true);
            }
        });

        <#if course.featuring!false>
            $("#featuring").prop("checked", true);
        </#if>

        var titlePictureUrl = '${course.titlePictureUrl!''}';
        if(titlePictureUrl.trim() != ''){
            $("#preview-image").prop("src", titlePictureUrl);
        }

        var videoPictureUrl = '${course.videoPictureUrl!''}';
        if(videoPictureUrl.trim() != ''){
            $("#videopreview-image").prop("src", videoPictureUrl);
        }

        var appPictureUrl = '${course.appPictureUrl!''}';
        if (appPictureUrl.trim() != '') {
            $("#apppreview-image").prop("src", appPictureUrl);
        }

        $('#info_frm').on('submit', function (e) {
            e.preventDefault();
            var params = {};

            $("input,select", this).each(function (index, field) {
                params[$(field).attr("name")] = $(field).val();
            });

            if (params.title.trim() == '') {
                alert("标题不能为空!");
                return;
            }

            if (params.lecturerUserName.trim() == '') {
                alert("人物名称不能为空!");
                return;
            }

            if (params.lecturerIntroduction.trim() == '') {
                alert("简介名称不能为空!");
                return;
            }

            params.textContent = editor.html();
            if (params.textContent.trim() == '') {
                alert("正文不能为空!");
                return;
            }

            if (params.titlePictureUrl.trim() == '') {
                alert("题图不能为空!");
                return;
            }

            if (params.videoPictureUrl.trim() == '') {
                alert("视频图不能为空!");
                return;
            }

            if (params.appPictureUrl.trim() == '') {
                alert("老师app首页图不能为空!");
                return;
            }

            if (params.url.trim() == '') {
                alert("视频路径不能为空!");
                return;
            }

            params.subject = $("input[name=subject]").filter(":checked")
                    .map(function () {
                        return $(this).data("subject");
                    }).get().join(",");

            if (params.subject.trim() == '') {
                alert("学科是必填项!");
                return;
            }

            params.grade = $("input[name=grade]").filter(":checked")
                    .map(function () {
                        return $(this).data("grade");
                    }).get().join(",");

            if (params.grade.trim() == '') {
                alert("年级是必填项!");
                return;
            }

            params.featuring = $("input#featuring").is(":checked");

            editor.sync();
            $.ajax({
                type: 'post',
                url: 'saveCourse.vpage',
                contentType: 'application/json;charset=UTF-8',
                data: JSON.stringify(params),
                success: function (res) {
                    if (res.success) {
                        alert("保存成功");
                        window.location.href = "./index.vpage";
                    } else {
                        alert("保存失败:" + res.info);
                    }
                },
                error: function (msg) {
                    alert("保存失败！");
                }
            });
            return false;
        });

        $('#save_info').on('click', function () {
            if (confirm("是否确认保存？")) {
                $('#info_frm').submit();
            }
        });

        $(document).on("change", "#imageSquare", function () {
            // 拼formData
            var formData = new FormData();
            var file = $(this)[0].files[0];

            formData.append('path', "17jt_title");
            formData.append('file', file);
            formData.append('file_size', file.size);
            formData.append('file_type', file.type);
            formData.append('id', $('#course-id').val());
            // 发起请求
            $.ajax({
                url: '/toolkit/uploadphoto.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (res) {
                    if (res.success) {
                        $("#title-image").val(res.info);
                        $("#preview-image").prop("src", res.info);
                    } else {
                        alert(res.info);
                    }
                }
            });
        });

        $(document).on("change", "#videoImageSquare", function () {
            // 拼formData
            var formData = new FormData();
            var t = $(this);
            var files = $(this)[0].files;
            var file = $(this)[0].files[0];

            formData.append('path', "17jt_video");
            formData.append('file', file);
            formData.append('file_size', file.size);
            formData.append('file_type', file.type);
            formData.append('id', $('#course-id').val());
            // 发起请求
            $.ajax({
                url: '/toolkit/uploadphoto.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (res) {
                    if (res.success) {
                        $("#video-image").val(res.info);
                        $("#videopreview-image").prop("src", res.info);
                    } else {
                        alert(res.info);
                    }
                }
            });
        });

        $(document).on("change", "#appImageSquare", function () {
            // 拼formData
            var formData = new FormData();
            var t = $(this);
            var files = $(this)[0].files;
            var file = $(this)[0].files[0];

            formData.append('path', "17jt_video");
            formData.append('file', file);
            formData.append('file_size', file.size);
            formData.append('file_type', file.type);
            formData.append('id', $('#course-id').val());
            // 发起请求
            $.ajax({
                url: '/toolkit/uploadphoto.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (res) {
                    if (res.success) {
                        $("#app-image").val(res.info);
                        $("#apppreview-image").prop("src", res.info);
                    } else {
                        alert(res.info);
                    }
                }
            });
        });

        $("button#add-catalog-detail").click(function () {
            var courseId = $("#course-id").val();
            if (courseId == "") {
                alert("课程id为空，请先保存课程再添加!")
            } else {
                // 清空
                mapForm(function (field, isCheck) {
                    if (isCheck)
                        field.attr("checked", false);
                    else
                        field.val('');
                });

                $("#add_catalog").modal("show");
            }

        });
        function mapForm(func) {
            var frm = $("form#add-catalog-frm");
            $.each($("input,textarea,select", frm), function (index, field) {
                var _f = $(field);
                func(_f, _f.attr("type") == "checkbox");
            });
        }

        $("#confirm-catalog-btn").click(function () {
            var frm = $("form#add-catalog-frm");
            frm.submit();
        });

        $("form#add-catalog-frm").on('submit', function (e) {
            e.preventDefault();

            var timeNode = $("input#timeNode").val();
            var catalogDescribe = $("input#catalogDescribe").val();

            if (timeNode.trim() == '') {
                alert("时间节点不能为空!");
                return;
            }
            if (catalogDescribe.trim() == '') {
                alert("描述不能为空!");
                return;
            }
            var n = timeNode.search("^([0-9]*):([0-5][0-9]):([0-5][0-9])$");
            if (n < 0) {
                alert("时间节点格式不正确，正确格式：xx:xx:xx")
                return;
            }
            var params = {};
            params["courseId"] = $("input#course-id").val();
            params["timeNode"] = timeNode;
            params["catalogDescribe"] = catalogDescribe;
            $.ajax({
                url: 'addCatalog.vpage',
                type: 'POST',
                data: JSON.stringify(params),
                processData: false,
                contentType: false,
                success: function (res) {
                    $("#add_catalog").modal("hide");
                    if (res.success == true) {
                        alert("保存成功!");
                        catalog_data(res);
                    } else {
                        alert(res.info);
                    }
                }
            });
        });

        function catalog_data(res){
            $("input#timeNode").val("");
            $("input#catalogDescribe").val("");
            var catalog_list_div =  $("#catalog_list");
            var catalogList = res.catalogList;
            catalog_list_div.html("");
            var ap = "";
            if (catalogList != null) {

                for (var i = 0; i < catalogList.length; i ++) {
                    ap += "<tr>\n" +
                            "<td>"+ catalogList[i].id +"</td>\n" +
                            "<td>"+ catalogList[i].timeNode +"</td>\n" +
                            "<td>"+ catalogList[i].catalogDescribe +"</td>\n" +
                            "<td style=\"text-align: center;width:120px;\">\n" +
                                "<a name='del_catalog' class='btn btn-danger' href='#'data-value='"+ catalogList[i].id+"' >删除</a>\n" +
                            "</td>\n" +
                            "</tr>";
                }
            } else {
                ap += "<tr>\n" +
                        "<td colspan=\"8\" style=\"text-align: center;\"><strong>No Data Found</strong></td>\n" +
                        "</tr>"
            }
            catalog_list_div.append(ap);
        }
        $("button#add-outerchain-detail").click(function () {
            var courseId = $("#course-id").val();
            if (courseId == "") {
                alert("课程id为空，请先保存课程再添加!")
            } else {
                // 清空
                mapForm(function (field, isCheck) {
                    if (isCheck)
                        field.attr("checked", false);
                    else
                        field.val('');
                });

                $("#add_outerchain").modal("show");
            }

        });
        function mapForm(func) {
            var frm = $("form#add-outerchain-frm");
            $.each($("input,textarea,select", frm), function (index, field) {
                var _f = $(field);
                func(_f, _f.attr("type") == "checkbox");
            });
        }

        $("#confirm-outerchain-btn").click(function () {
            var frm = $("form#add-outerchain-frm");
            frm.submit();
        });

        $("form#add-outerchain-frm").on('submit', function (e) {
            e.preventDefault();

            if ($("input#outerchain").val().trim() == '') {
                alert("外链标题不能为空!");
                return;
            }
            if ($("input#outerchainUrl").val().trim() == '') {
                alert("外链url不能为空!");
                return;
            }
            var params = {};
            params["courseId"] = $("input#course-id").val();
            params["outerchain"] = $("input#outerchain").val();
            params["outerchainUrl"] = $("input#outerchainUrl").val();

            $.ajax({
                url: 'addOuterchain.vpage',
                type: 'POST',
                data: JSON.stringify(params),
                processData: false,
                contentType: false,
                success: function (res) {
                    $("#add_outerchain").modal("hide");
                    if (res.success == true) {
                        alert("保存成功!");
                        outerchain_data(res)
                    } else {
                        alert(res.info);
                    }
                }
            });
        });

        function outerchain_data(res){
            $("input#outerchain").val("");
            $("input#outerchainUrl").val("");

            var outerchain_list_div =  $("#outerchain_list");
            var outerchainList = res.outerchainList;
            outerchain_list_div.html("");
            var ap = "";
            if (outerchainList != null) {

                for (var i = 0; i < outerchainList.length; i ++) {
                    ap += "<tr>\n" +
                            "<td>"+ outerchainList[i].id +"</td>\n" +
                            "<td>"+ outerchainList[i].outerchain +"</td>\n" +
                            "<td>"+ outerchainList[i].outerchainUrl +"</td>\n" +
                            "<td style=\"text-align: center;width:120px;\">\n" +
                            "<a name='del_outerchain' class='btn btn-danger' data-value='"+ outerchainList[i].id+"' >删除</a>\n" +
                            "</td>\n" +
                            "</tr>";
                }
            } else {
                ap += "<tr>\n" +
                        "<td colspan=\"8\" style=\"text-align: center;\"><strong>No Data Found</strong></td>\n" +
                        "</tr>"
            }
            outerchain_list_div.append(ap);
        }

        $(document).on("click","a[name='del_catalog']", function () {
            if (confirm("是否确认删除？")) {
                $.ajax({
                    type: "post",
                    url: "delCatalog.vpage",
                    data: $(this).attr("data-value"),
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            alert("删除成功!");
                            catalog_data(data);
                        } else {
                            alert(data.info);
                        }
                    }
                });
            }
        });


        $(document).on("click","a[id='back']", function () {
            if (confirm("返回将丢失未保存数据，是否返回？")) {
                // window.history.back();
                window.location.replace("index.vpage");
            }
        });


        $(document).on("click","a[name='del_outerchain']", function () {
            if (confirm("是否确认删除？")) {
                $.ajax({
                    type: "post",
                    url: "delOuterchain.vpage",
                    data: $(this).attr("data-value"),
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            alert("删除成功!");
                            outerchain_data(data)
                        } else {
                            alert(data.info);
                        }
                    }
                });
            }
        });

        $(document).on("click","a[name='del_choice']", function () {
            if (confirm("是否确认删除？")) {
                $.ajax({
                    type: "post",
                    url: "delChoiceNote.vpage",
                    data: $(this).attr("data-value"),
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            alert("删除成功!");
                            choice_data(data);
                        } else {
                            alert(data.info);
                        }
                    }
                });
            }
        });

        var questionerUserName = '';
        var answerUserName = '';
        var questionerPictureUrl = '';
        var answerPictureUrl = '';

        $("button#add-choice-detail").click(function () {
            addChoiceNoteFlag = true;

            if ($("#course-id").val() == "") {
                alert("课程id为空，请先保存课程再添加!")
            } else {
                // 清空
                $("#id").val("");
                $('#question').val("");
                $('#answer').val("");
                $('#questionerPictureUrl').val("");
                $('#answerPictureUrl').val("");

                questionerUserName = $('#questionerUserName').val();
                answerUserName = $("#answerUserName").val();
                questionerPictureUrl = $("#questioner-image").val();
                answerPictureUrl = $("#answer-image").val();
                if (questionerUserName == '' && answerUserName == '' && questionerPictureUrl == '' && answerPictureUrl == '') {
                    <#if course.choiceNoteList?? && course.choiceNoteList?has_content!false>

                        <#list course.choiceNoteList as choiceNote>
                        questionerUserName = '${choiceNote.questionerUserName!''}';
                        $("input#questionerUserName").val(questionerUserName);

                        answerUserName = '${choiceNote.answerUserName!''}';
                        $("input#answerUserName").val(answerUserName);

                        answerPictureUrl = '${choiceNote.answerPictureUrl!''}';
                        if(answerPictureUrl.trim() != ''){
                            $("#answer-image").prop("src", answerPictureUrl);
                        }
                        questionerPictureUrl = '${choiceNote.questionerPictureUrl!''}';
                        if(questionerPictureUrl.trim() != ''){
                            $("#questioner-image").prop("src", questionerPictureUrl);
                        }
                        </#list>
                    </#if>
                }

                $("#add_choice").modal("show");
            }

        });
        function mapForm(func) {
            var frm = $("form#add-choice-frm");
            $.each($("input,textarea,select", frm), function (index, field) {
                var _f = $(field);
                func(_f, _f.attr("type") == "checkbox");
            });
        }

        $("#confirm-choice-btn").click(function () {
            var frm = $("form#add-choice-frm");
            frm.submit();
        });

        $("form#add-choice-frm").on('submit', function (e) {
            e.preventDefault();

            if ($('#questionerPictureUrl')[0].lenght <=0) {
                alert("提问者头像不能为空!");
                return;
            }
            if ($('#answerPictureUrl')[0].lenght <=0) {
                alert("回答者头像不能为空!");
                return;
            }

            if ($("input#questionerUserName").val().trim() == '') {
                alert("提问者姓名不能为空!");
                return;
            }
            if ($("#question").val().trim() == '') {
                alert("问题不能为空!");
                return;
            }
            if ($("input#answerUserName").val().trim() == '') {
                alert("回答者姓名不能为空!");
                return;
            }
            if ($("#answer").val().trim() == '') {
                alert("答案不能为空!");
                return;
            }

            var params = {};

            questionerPictureUrl = $("#questioner-image")[0].src;
            answerPictureUrl = $("#answer-image")[0].src;
            params["questionerPictureUrl"] = questionerPictureUrl;
            params["answerPictureUrl"] = answerPictureUrl;

            var imageInput = $('#questionerPictureUrl')[0];
            if (imageInput.files.length > 0) {
                var formData = new FormData();
                formData.append('file', imageInput.files[0]);
                formData.append('path', "17jt_video");
                formData.append('file_size', imageInput.files[0].size);
                formData.append('file_type', imageInput.files[0].type);
                formData.append('id', $('#course-id').val());
                $.ajax({
                    url: "/toolkit/uploadphoto.vpage",
                    type: "POST",
                    processData: false,
                    contentType: false,
                    data: formData,
                    async:false,
                    success: function (res) {
                        if (res.success) {
                            params["questionerPictureUrl"] = res.info;
                        } else {
                            alert(res.info);
                            return;
                        }
                    }
                })
            }

            var imageInput = $('#answerPictureUrl')[0];
            if (imageInput.files.length > 0) {
                var formData = new FormData();
                formData.append('file', imageInput.files[0]);
                formData.append('path', "17jt_video");
                formData.append('file_size', imageInput.files[0].size);
                formData.append('file_type', imageInput.files[0].type);
                formData.append('id', $('#course-id').val());
                $.ajax({
                    url: "/toolkit/uploadphoto.vpage",
                    type: "POST",
                    processData: false,
                    contentType: false,
                    data: formData,
                    async:false,
                    success: function (res) {
                        if (res.success) {
                            params["answerPictureUrl"] = res.info;
                        } else {
                            alert(res.info);
                            return;
                        }
                    }
                })
            }

            params["courseId"] = $("input#course-id").val();
            params["questionerUserName"] = $("input#questionerUserName").val();
            params["question"] = $("#question").val();
            params["answerUserName"] = $("input#answerUserName").val();
            params["answer"] = $("#answer").val();
            var id = $("#id").val();
            if (id != '') {
                params['id'] = id;
            }

            $.ajax({
                url: 'upsertChoiceNote.vpage',
                type: 'POST',
                data: JSON.stringify(params),
                processData: false,
                contentType: false,
                success: function (res) {
                    $("#add_choice").modal("hide");
                    if (res.success == true) {
                        if (addChoiceNoteFlag) {
                            alert("保存成功!");
                            choice_data(res)
                        } else {
                            alert("编辑成功!");
                            window.location.reload();
                        }
                    } else {
                        alert(res.info);
                    }
                }
            });
        });

        function choice_data(res){
            var choice_list_div =  $("#choice_list");
            var choiceNoteList = res.choiceNoteList;
            choice_list_div.html("");
            var ap = "";
            if (choiceNoteList != null) {
                questionerUserName = choiceNoteList[choiceNoteList.length - 1].questionerUserName;
                $("input#questionerUserName").val(questionerUserName);

                answerUserName = choiceNoteList[choiceNoteList.length - 1].answerUserName;
                $("input#answerUserName").val(answerUserName);

                answerPictureUrl = choiceNoteList[choiceNoteList.length - 1].answerPictureUrl;
                $("#answer-image").prop("src", answerPictureUrl);
                questionerPictureUrl = choiceNoteList[choiceNoteList.length - 1].questionerPictureUrl;
                $("#questioner-image").prop("src", questionerPictureUrl);
                for (var i = 0; i < choiceNoteList.length; i ++) {
                    ap += "<tr>\n" +
                            "<td>"+ choiceNoteList[i].id +"</td>\n" +
                            "<td>"+ choiceNoteList[i].questionerUserName +"</td>\n" +
                            "<td style='text-align: center;'><img style='width:210px;height:140px;' width='210' " +
                            "height='140' src='"+ choiceNoteList[i].questionerPictureUrl +"'></td>\n" +
                            "<td>"+ choiceNoteList[i].question +"</td>\n" +
                            "<td>"+ choiceNoteList[i].answerUserName +"</td>\n" +
                            "<td style='text-align: center;'><img style='width:210px;height:140px;' width='210' " +
                                "height='140' src='"+ choiceNoteList[i].answerPictureUrl +"'></td>\n" +
                            "<td>"+ choiceNoteList[i].answer +"</td>\n" +
                            "<td style=\"text-align: center;width:120px;\"> " +
                                "<a name=\"del_choice\" class=\"btn btn-danger\" href=\"#\"data-value='"+ choiceNoteList[i].id +"' >删除</a>" +
                            "</td>\n" +
                            "</tr>";
                }
            } else {
                ap += "<tr>\n" +
                        "<td colspan=\"8\" style=\"text-align: center;\"><strong>No Data Found</strong></td>\n" +
                        "</tr>"
            }
            choice_list_div.append(ap);
        }
    })
    ;

    function previewImg(file) {
        var prevDiv = $('#imgSrc');
        if (file.files && file.files[0]) {
            var reader = new FileReader();
            reader.onload = function (evt) {
                prevDiv.attr("src", evt.target.result);
            };
            reader.readAsDataURL(file.files[0]);
        }
        else {
            prevDiv.html('<img class="img" style="filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale,src=\'' + file.value + '\'">');
        }
    }

    function previewHandle(fileDOM, imgId) {
        var file = fileDOM.files[0], // 获取文件
                imageType = /^image\//,
                reader = '';

        // 文件是否为图片
        if (!imageType.test(file.type)) {
            alert("请选择图片！");
            return;
        }
        // 判断是否支持FileReader
        if (window.FileReader) {
            reader = new FileReader();
        }
        // IE9及以下不支持FileReader
        else {
            alert("您的浏览器不支持图片预览功能，如需该功能请升级您的浏览器！");
            return;
        }

        // 读取完成
        reader.onload = function (event) {
            // 获取图片DOM
            var img = document.getElementById(imgId);
            // 图片路径设置为读取的图片
            img.src = event.target.result;
        };
        reader.readAsDataURL(file);
    }

    function updateChoiceDetail(data) {
        addChoiceNoteFlag = false;
        if ($("#course-id").val() == "") {
            alert("课程id为空，请先保存课程再编辑!")
        } else {
            var line = $(data).parent().parent("tr");
            var tds = $(line).find("td");

            var id = $(tds.get(0)).text();

            var questionerUserName = $(tds.get(1)).text();
            var questionerPictureUrl = $(tds.get(2)).find("img").prop("src");
            var question = $(tds.get(3)).text();

            var answerUserName = $(tds.get(4)).text();
            var answerPictureUrl = $(tds.get(5)).find("img").prop("src");
            var answer = $(tds.get(6)).text();

            $("#id").val(id);
            $("input#questionerUserName").val(questionerUserName)
            $("#questioner-image").prop("src", questionerPictureUrl)
            $('#question').val(question);

            $("input#answerUserName").val(answerUserName)
            $("#answer-image").prop("src", answerPictureUrl)
            $('#answer').val(answer);

            $("#add_choice").modal("show");
        }
    }
</script>
</@layout_default.page>