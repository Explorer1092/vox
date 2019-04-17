<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='大咖讲座' page_num=9>

<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>

<div class="span9">
    <legend>
        <strong><span class="text-info">预览</span></strong>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <form id="telecaseForm" class="well form-horizontal">
                <input type="hidden" id="id" name="id" value="${id!''}"/>
                <fieldset id="base-info">
                    <div class="control-group">
                        <label class="control-label"></label>
                        <div class="controls">
                            <button id="show_send_notice_button" type="button" class="btn btn-primary">预览</button>
                            &nbsp;<a href="details.vpage?id=${id!''}" id="save" class="btn btn-default">修改</a>
                        </div>
                    </div>
                </fieldset>
                <fieldset id="base-info">
                    <legend>基础信息</legend>
                    <div class="control-group">
                        <label class="control-label">课程名称（必填）：</label>
                        <div class="controls">
                            <input type="text" placeholder="课程名称" maxlength="30" class="input" value="${title!''}"
                                   name="title" id="title"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">期数（必填）：</label>
                        <div class="controls">
                            <input type="number" placeholder="期数" maxlength="30" class="input" value="${issue!''}"
                                   name="issue" id="issue"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">课程配置时间（必填）：</label>
                        <div class="controls">
                            <input type="text" placeholder="课程开始时间" maxlength="30" class="input" value="${liveStart!''}"
                                   name="liveStart" id="liveStart"/>
                            <input type="text" placeholder="课程结束开放时间" maxlength="30" class="input" value="${liveEnd!''}"
                                   name="liveEnd" id="liveEnd"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">入口开放时间（必填）：</label>
                        <div class="controls">
                            <input type="text" placeholder="入口开放时间" maxlength="30" class="input form_datetime"
                                   value="${enterStart!''}"
                                   name="enterStart" id="enterStart"/>
                            <input type="text" placeholder="入口结束时间" maxlength="30" class="input form_datetime"
                                   value="${enterEnd!''}"
                                   name="enterEnd" id="enterEnd"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">直播间ID（必填）：</label>
                        <div class="controls">
                            <input type="text" placeholder="直播间ID" maxlength="30" class="input form_datetime"
                                   value="${liveId!''}"
                                   name="liveId" id="liveId"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">课程头图（必填）：</label>
                        <div class="controls">
                            <input type="text" placeholder="课程头图" maxlength="30" class="input form_datetime"
                                   value="${headImg!''}"
                                   name="headImg" id="headImg"/>
                            <a href="javascript:void(null);" class="text-primary" data-index="2"
                               data-action="upload">上传</a>
                            <a href="javascript:void(null);" class="text-success" data-index="2"
                               data-action="preview">预览</a>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">课程类型：</label>
                        <div class="controls">
                            <select id="stage" name="stage" data-value="${stage}">
                                <option value="production">线上</option>
                                <option value="testing">测试</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">灰度控制：</label>
                        <div class="controls">
                            <input type="text" placeholder="灰度控制" maxlength="30" class="input" value="${gray!''}"
                                   name="gray" id="gray"/>
                        </div>
                    </div>
                </fieldset>
                <fieldset id="intro-info">
                    <legend>课程详情页</legend>
                    <div class="control-group">
                        <label class="control-label">报名页-课程介绍：</label>
                        <div class="controls" data-holder="enterPages">
                            <#if enterPage?exists>
                                <#list enterPage as page>
                                <span data-type="data-item">
                                <div class="form-group">
                                    <label>标题</label>
                                    <input type="text" placeholder="标题" maxlength="30" class="form-control"
                                           value="${page.title!''}"
                                           name="enterPageTitle"/>
                                    <a data-action="up" class="btn btn-default" href="javascript:void(null);">上移</a>
                                    <a data-action="down" class="btn btn-default" href="javascript:void(null);">下移</a>
                                    <a data-action="delete" class="btn btn-danger" href="javascript:void(null);">删除</a>
                                </div>
                                <div class="form-group">
                                    <label>内容</label>
                                    <textarea style="display: none;" class="form-control span8"
                                              name="enterPageContent" rows="3"
                                              data-type="holder">${page.content!''}</textarea>
                                    <script name="enterPageContainer" type="text/plain"></script>
                                </div>
                            </span>
                                </#list>
                            <#else>
                            <span data-type="data-item">
                                <div class="form-group">
                                    <label>标题</label>
                                    <input type="text" placeholder="标题" maxlength="30" class="form-control"
                                           value=""
                                           name="enterPageTitle"/>
                                    <a data-action="up" class="btn btn-default" href="javascript:void(null);">上移</a>
                                    <a data-action="down" class="btn btn-default" href="javascript:void(null);">下移</a>
                                    <a data-action="delete" class="btn btn-danger" href="javascript:void(null);">删除</a>
                                </div>
                                <div class="form-group">
                                    <label>内容</label>
                                    <textarea style="display: none;" class="form-control span8"
                                              name="enterPageContent" rows="3" data-type="holder"></textarea>
                                    <script name="enterPageContainer" type="text/plain"></script>
                                </div>
                            </span>
                            </#if>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label"></label>
                        <div class="controls">
                            <a data-target="enterPage" class="btn btn-info" href="javascript:void(null);">添加</a>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">回放页-精彩回顾：</label>
                        <div class="controls" data-holder="playbackPages">
                            <#if playbackPage?exists>
                                <#list playbackPage as page>
                            <span data-type="data-item">
                            <div class="form-group">
                                <label>标题</label>
                                <input type="text" placeholder="标题" maxlength="30" class="form-control"
                                       value="${page.title!''}"
                                       name="playbackPageTitle"/>
                                <a data-action="up" class="btn btn-default" href="javascript:void(null);">上移</a>
                                    <a data-action="down" class="btn btn-default" href="javascript:void(null);">下移</a>
                                    <a data-action="delete" class="btn btn-danger" href="javascript:void(null);">删除</a>
                            </div>
                            <div class="form-group">
                                <label>内容</label>
                                <textarea style="display: none;" class="form-control span8"
                                          name="playbackPageContent" data-type="holder"
                                          rows="3">${page.content!''}</textarea>
                                <script type="text/plain"></script>
                            </div>
                            </span>
                                </#list>
                            <#else>
                                <span data-type="data-item">
                            <div class="form-group">
                                <label>标题</label>
                                <input type="text" placeholder="标题" maxlength="30" class="form-control"
                                       value=""
                                       name="playbackPageTitle"/>
                                <a data-action="up" class="btn btn-default" href="javascript:void(null);">上移</a>
                                    <a data-action="down" class="btn btn-default" href="javascript:void(null);">下移</a>
                                    <a data-action="delete" class="btn btn-danger" href="javascript:void(null);">删除</a>
                            </div>
                            <div class="form-group">
                                <label>内容</label>
                                <textarea style="display: none;" class="form-control span8"
                                          name="playbackPageContent" data-type="holder" rows="3"></textarea>
                                <script type="text/plain"></script>
                            </div>
                            </span>
                            </#if>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label"></label>
                        <div class="controls">
                            <a data-target="playbackPage" class="btn btn-info" href="javascript:void(null);">添加</a>
                        </div>
                    </div>
                </fieldset>
                <fieldset id="register-info">
                    <legend>报名流程</legend>
                    <div class="control-group">
                        <label class="control-label">报名流程：</label>
                        <div class="controls">
                            <select id="enterType" name="enterType">
                                <option value="shard">分享报名</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">报名按钮文案：</label>
                        <div class="controls">
                            <input type="text" placeholder="报名按钮文案" maxlength="30" class="input" value="${enterText!''}"
                                   name="enterText" id="enterText"/>
                            <#--<a href="javascript:void(null);" class="text-primary" data-index="2"
                               data-action="upload">上传</a>
                            <a href="javascript:void(null);" class="text-success" data-index="2"
                               data-action="preview">预览</a>-->
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">分享海报：</label>
                        <div class="controls">
                            <input type="text" placeholder="分享海报" maxlength="30" class="input" value="${shardImg!''}"
                                   name="shardImg" id="shardImg"/>
                            <a href="javascript:void(null);" class="text-primary" data-index="2"
                               data-action="upload">上传</a>
                            <a href="javascript:void(null);" class="text-success" data-index="2"
                               data-action="preview">预览</a>
                        </div>
                    </div>
                </fieldset>
                <fieldset id="list-info">
                    <legend>主页配置</legend>
                    <h4>推荐位</h4>
                    <div class="control-group">
                        <label class="control-label">主背景图（必填）：</label>
                        <div class="controls">
                            <input type="text" placeholder="分享海报" maxlength="30" class="input"
                                   value="${recomBackImg!''}"
                                   name="recomBackImg" id="recomBackImg"/>
                            <a href="javascript:void(null);" class="text-primary" data-index="2"
                               data-action="upload">上传</a>
                            <a href="javascript:void(null);" class="text-success" data-index="2"
                               data-action="preview">预览</a>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">标题图（必填）：</label>
                        <div class="controls">
                            <input type="text" placeholder="标题图" maxlength="30" class="input"
                                   value="${recomTitleImg!''}"
                                   name="recomTitleImg" id="recomTitleImg"/>
                            <a href="javascript:void(null);" class="text-primary" data-index="2"
                               data-action="upload">上传</a>
                            <a href="javascript:void(null);" class="text-success" data-index="2"
                               data-action="preview">预览</a>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">按钮图（必填）：</label>
                        <div class="controls">
                            <input type="text" placeholder="按钮图" maxlength="30" class="input"
                                   value="${recomButtonImg!''}"
                                   name="recomButtonImg" id="recomButtonImg"/>
                            <a href="javascript:void(null);" class="text-primary" data-index="2"
                               data-action="upload">上传</a>
                            <a href="javascript:void(null);" class="text-success" data-index="2"
                               data-action="preview">预览</a>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">推荐位展示：</label>
                        <div class="controls">
                            <select id="recomStatus" name="recomStatus" data-value="${recomStatus!''}">
                                <option value="false">否</option>
                                <option value="true">是</option>
                            </select>
                        </div>
                    </div>
                    <H4>列表页</H4>
                    <#--<div class="control-group">
                        <label class="control-label">主背景图：</label>
                        <div class="controls">
                            <input type="text" placeholder="主背景图" maxlength="30" class="input" value="${listBackImg!''}"
                                   name="listBackImg" id="listBackImg"/>
                            <a href="javascript:void(null);" class="text-primary" data-index="2"
                               data-action="upload">上传</a>
                            <a href="javascript:void(null);" class="text-success" data-index="2"
                               data-action="preview">预览</a>
                        </div>
                    </div>-->
                    <div class="control-group">
                        <label class="control-label">右侧配图：</label>
                        <div class="controls">
                            <input type="text" placeholder="标题图" maxlength="30" class="input" value="${listTitleImg!''}"
                                   name="listTitleImg" id="listTitleImg"/>
                            <a href="javascript:void(null);" class="text-primary" data-index="2"
                               data-action="upload">上传</a>
                            <a href="javascript:void(null);" class="text-success" data-index="2"
                               data-action="preview">预览</a>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
    <div id="show_send_notice_panel" class="modal fade" tabindex="-1" role="dialog">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title">发送预览通知</h4>
                </div>
                <div class="modal-body">
                    <p>
                    <form class="well form-horizontal">
                        <input type="hidden" id="id" value="${id!''}"/>
                        <div class="form-group" style="display: block;">
                            <label class="control-label">家长id： </label>
                            <div class="controls">
                                <input type="number" class="form-control" id="userId"/>
                            </div>
                        </div>
                    </form>
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" id="audit-close" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button type="button" class="btn btn-primary" id="btn_sender">确定</button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->
    <input type="file" style="display: none;" id="uploader" accept="image/gif, image/jpeg, image/png, image/jpg"/>
    <script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.config.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.all.js"></script>
    <script language="JavaScript" type="application/javascript">
        $(document).ready(function () {

            $("[data-action='preview']").click(function () {
                var image = $(this).parent().find("input").first().val();
                if (image == '') {
                    return;
                }

                window.open(image, "newwindow");
            });


            var itemLoader = function (wrapper, item, clear) {
                if (clear) {
                    item.find("input").val("");
                    item.find("textarea").val("");
                }
                item.find("[data-action='delete']").click(function () {

                    if (wrapper.find("[data-type='data-item']").length === 1) {
                        return;
                    }

                    item.remove();
                });

                item.find("[data-action='up']").click(function () {
                    var list = wrapper.find("[data-type='data-item']");
                    var current = $(this).parent().parent();
                    var index = list.index(current);
                    if (index == 0) {
                        return;
                    }
                    current.prev().before(current);
                });


                item.find("[data-action='down']").click(function () {
                    var list = wrapper.find("[data-type='data-item']");
                    var current = $(this).parent().parent();
                    var index = list.index(current);
                    if (index == list.length - 1) {
                        return;
                    }
                    current.next().after(current);
                });

                item.find(".edui-default").remove();
                item.find("textarea").after("<script type=\"text/plain\"><\/script>");
                var containerId = Math.random().toString(36).substr(2);
                item.find("script").attr("id", containerId);

                var ue = UE.getEditor(containerId, {
                    serverUrl: "/opmanager/talk/ueditorcontroller.vpage"
                });

                ue.ready(function () {
                    ue.setContent(item.find("textarea").val());
                    ue.addListener("blur", function () {
                        item.find("textarea").val(ue.getContent());
                    })
                });
            };

            var appender = function (wrapper, clear) {
                var item = wrapper
                        .find("[data-type='data-item']:first")
                        .clone()
                        .appendTo(wrapper);

                itemLoader(wrapper, item, clear);
                return item;
            }

            var starter = function (wrapper, button) {
                var item = wrapper.find("[data-type='data-item']");
                item.each(function () {
                    itemLoader(wrapper, $(this), false);
                });

                button.click(function () {
                    appender(wrapper, true);
                });

            };


            var enterPageContainer = $("[data-holder='enterPages']");
            var enterPageButton = $("[data-target='enterPage']");

            starter(enterPageContainer, enterPageButton);

            var playbackPageContainer = $("[data-holder='playbackPages']");
            var playbackPageButton = $("[data-target='playbackPage']");
            starter(playbackPageContainer, playbackPageButton);

            var recomStatus = $("#recomStatus").attr("data-value");
            if (recomStatus != '') {
                $("#recomStatus").val(recomStatus);
            }
            var stage = $("#stage").attr("data-value");
            if(stage != ''){
                $("#stage").val(stage);
            }

            $("#show_send_notice_button").click(function () {
                $("#show_send_notice_panel").modal('show');
            });

            $("#btn_sender").click(function () {
                $.ajax({
                    url: "notice.vpage",
                    type: "post",
                    dataType: "json",
                    data: {"id": $("#id").val(), "uid":$("#userId").val()},
                    success: function (data) {
                        if(data.success) {
                            alert("发送成功");
                            $("#show_send_notice_panel").modal('hide');
                        } else {
                            alert("发送失败");
                        }
                    }
                });
            });
        })
    </script>
</@layout_default.page>