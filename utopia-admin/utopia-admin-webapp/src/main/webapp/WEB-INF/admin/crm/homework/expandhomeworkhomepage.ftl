<#-- @ftlvariable name="category" type="java.lang.String" -->
<#-- @ftlvariable name="homeworkInfo" type="java.util.Map<String, Object>" -->
<#-- @ftlvariable name="homeworkSubject" type="java.lang.String" -->
<#-- @ftlvariable name="homeworkId" type="java.lang.Long" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="作业查询" page_num=3>
<style>
    blockquote {
        margin: 0;
    }

    .span9 .question-box .aWidth48 {
        width: 48%;
        display: inline-block;
    }
</style>
<div id="main_container" class="span9">
    <ul class="nav nav-tabs">
        <li${((category == 'middle')?string('', ' class="active"'))!}><a data-toggle="tab" href="#primary">小学</a></li>
    </ul>

    <div class="tab-content">
        <div id="primary" class="tab-pane fade${((category == 'middle')?string('', ' in active'))!}">
            <div>
                <fieldset>
                    <legend>小学作业查询</legend>
                </fieldset>
                <ul class="inline">
                    <li>
                        <form action="?" method="get">
                            作业ID：<input name="homeworkId"/>
                            <input type="submit" class="btn" value="搜索"/>
                        </form>
                    </li>

                    <#if homeworkSubject??>
                        <#--<li>-->
                            <#--<input type="button" class="btn btn-primary" value="复制作业" id="copyHomework">-->
                        <#--</li>-->
                    </#if>
                    <#if homeworkSubject??>
                        <#--<li>-->
                            <#--<input type="button" class="btn btn-primary" value="恢复作业" id="resumeHomeworkBtn">-->
                        <#--</li>-->
                    </#if>
                    <#if source??>
                        <li>
                            来源：${source!}
                        </li>
                    </#if>

                    <#if time??>
                        <li>
                            标准时长：${time!}
                        </li>
                    </#if>

                    <#if clazzGroupId??>
                        <li>
                            班组：${clazzGroupId!}
                        </li>
                    </#if>
                    <#if teacherId??>
                        <li>
                            老师ID：${teacherId!}
                        </li>
                        <li>
                            老师姓名：${teacherName!}
                        </li>
                    </#if>


                </ul>

                <div>
                    <#if masterNames??>
                        <li>
                            学霸信息：${masterNames!}
                        </li>
                    </#if>
                </div>


            </div>
            <fieldset>
                <legend>
                    <#if homeworkSubject??> <#if homeworkSubject == 'ENGLISH'>英语作业${homeworkId!}详情</#if></#if>
                    <#if homeworkSubject??> <#if homeworkSubject == 'MATH'>数学作业${homeworkId!}详情</#if></#if>
                    <#if homeworkSubject??> <#if homeworkSubject == 'CHINESE'>语文作业${homeworkId!}详情</#if></#if>
                </legend>
            </fieldset>
            <div id="alertDiv" title="复制作业">

            </div>
            <div id="resumeDiv" title="恢复作业">

            </div>

            <div>
                <table class="table table-hover table-striped table-bordered">
                    <tr>
                        <th style="width: 150px;">开始时间</th>
                        <th style="width: 90px;">截止时间</th>
                        <th style="width: 150px;">检查时间</th>
                        <th>作业详情</th>
                    </tr>
                    <#if homeworkInfo??>
                        <tr>
                            <td>${homeworkInfo.startDatetime?string('yyyy-MM-dd HH:mm:ss')}</td>
                            <td>${homeworkInfo.endDatetime?string('yyyy-MM-dd HH:mm:ss')}</td>
                            <td>
                                <#if homeworkInfo.checkedAt??>${homeworkInfo.checkedAt?string('yyyy-MM-dd HH:mm:ss')}</#if>

                            </td>
                            <td>
                                <div class="question-box">
                                <#--${homeworkInfo.homeworkPrettyJson!}-->
                                    <#if homeworkDetails??>
                                        <#list homeworkDetails as homeworkDetail>
                                            <span class="aWidth48">
                                                <#if homeworkSubject == 'ENGLISH'>
                                                    <#if homeworkDetail.type == 'BASIC_APP'>
                                                        <a target="_blank"
                                                           href="${baseUrl!}/flash/loader/newselfstudy.vpage?qids=${homeworkDetail.qids}&lessonId=${homeworkDetail.lessonId}&practiceId=${homeworkDetail.practiceId}">${homeworkDetail.practiceName!}
                                                            (${homeworkDetail.categoryName!})</a>
                                                    <#else>
                                                        <#if homeworkDetail.type == 'READING'>
                                                            <a target="_blank"
                                                               href="${baseUrl!}/flash/loader/newselfstudy.vpage?pictureBookId=${homeworkDetail.pictureBookId}">${homeworkDetail.pictureBookName}</a>
                                                        <#else>
                                                        ${homeworkDetail.exam}
                                                            (${homeworkDetail.type})
                                                            <a target="_blank"
                                                               href="${baseUrl!}/container/viewpaper.vpage?qid=${homeworkDetail.exam}">【PC预览】</a>
                                                            <a href="javascript:void(0);" data-qid="${homeworkDetail.exam}" class="J_mobilePreview">【手机预览】</a>
                                                        </#if>
                                                    </#if>
                                                <#else>
                                                ${homeworkDetail.exam}(${homeworkDetail.type})
                                                <a target="_blank"
                                                   href="${baseUrl!}/container/viewpaper.vpage?qid=${homeworkDetail.exam}">【PC预览】</a>
                                                <a href="javascript:void(0);" data-qid="${homeworkDetail.exam}" class="J_mobilePreview">【手机预览】</a>
                                                </#if>
                                        </span>
                                        </#list>
                                    </#if>
                                </div>
                            </td>
                        </tr>
                    </#if>
                </table>
            </div>

            <fieldset>
                <legend>
                    学生完成情况
                </legend>
            </fieldset>

            <div>
                <table class="table table-hover table-striped table-bordered">
                    <tr>
                        <th>完成详情</th>
                        <th>学生ID</th>
                        <th>学生姓名</th>
                        <th>是否完成</th>
                        <#--<th>分数</th>-->
                        <th>学生提交时间</th>
                        <th>完成用时</th>
                        <th>客户端类型</th>
                    </tr>
                    <#if studentAccomplishmentList??>
                        <#list studentAccomplishmentList as studentAccomplishment>
                            <tr>
                                <td>
                                    <a target="_blank"
                                       href="/crm/homework/userexpandhomeworkresultdetail.vpage?userId=${studentAccomplishment.studentId}&homeworkId=${homeworkId}">点击查看</a>
                                </td>
                                <td>
                                    <a target="_blank"
                                       href="/crm/student/studenthomepage.vpage?studentId=${studentAccomplishment.studentId}">${studentAccomplishment.studentId}</>
                                </td>
                                <td>${studentAccomplishment.studentName}</td>
                                <td>${(studentAccomplishment.isFinished)}</td>
                                <#--<td>${(studentAccomplishment.score)}</td>-->
                                <td>${(studentAccomplishment.finishAt)}</td>
                                <td>${(studentAccomplishment.duration)}</td>
                                <td>${(studentAccomplishment.clientType)}</td>
                            </tr>
                        </#list>
                    </#if>
                </table>
            </div>


        </div>
        <div id="middle" class="tab-pane fade${((category == 'middle')?string(' in active', ''))!}">
            <div>
                <fieldset>
                    <legend>中学作业查询</legend>
                </fieldset>
                <ul class="inline">
                    <li>
                        <form action="${ms_crm_admin_url}/crm/homework/search" target="_blank" method="get">
                            <input type="hidden" name="category" value="middle">
                            作业ID：<input name="homeworkId"/>
                            <input type="submit" class="btn" value="搜索"/>
                        </form>
                    </li>
                </ul>
            </div>
            <fieldset>
                <legend><#if ms_homeworkInfo??>${ms_homeworkInfo.subject!}作业${ms_homeworkId!}详情</#if></legend>
            </fieldset>
            <div>
                <table class="table table-hover table-striped table-bordered">
                    <tr>
                        <th style="width: 150px;">开始时间</th>
                        <th style="width: 150px;">截止时间</th>
                        <th>作业详情</th>
                    </tr>
                    <#if ms_homeworkInfo??>
                        <tr>
                            <td>${ms_homeworkInfo.startDatetime?string('yyyy-MM-dd HH:mm:ss')}</td>
                            <td>${ms_homeworkInfo.endDatetime?string('yyyy-MM-dd HH:mm:ss')}</td>
                            <td>
                            <#--${homeworkInfo.homeworkPrettyJson!}-->
                                <#if ms_homeworkDetails??>
                                    <#list ms_homeworkDetails as homeworkDetail>
                                        <#if homeworkDetail.type == "sync" || homeworkDetail.type == "workbook">
                                            <a target="_blank"
                                               href="${ms_crm_admin_url}/crm/homework/viewpaper?homework_id=${ms_homeworkId!}&paper_id=${ms_paperId!}&practice_type=${homeworkDetail.type}">${homeworkDetail.title}</a>
                                        <#else>
                                        ${homeworkDetail.title}
                                        </#if>
                                    </#list>
                                </#if>
                            </td>
                        </tr>
                    </#if>
                </table>
            </div>
        </div>
    </div>

<#--应试手机预览-->
    <div id="eventMobileModal" class="modal hide fade" style="width: 420px;" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3 id="myModalLabel">手机预览</h3>
        </div>
        <div class="modal-body" style="max-height:550px;">
            <iframe style="width: 380px;height: 530px;" frameborder="0">正在加载</iframe>
        </div>
        <div class="modal-footer" style="display: none;">
            <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
            <button class="btn btn-primary">确定</button>
        </div>
    </div>
<#--应试手机预览结束-->

    <script id="alertTemple" type="text/html">
        <form style="background-color: #fff;" class="well form-horizontal" method="post"
              action="/crm/homework/newcopyhomework.vpage" name="copyHomework" id="copyHomework">
            <fieldset>
                <div class="control-group">
                    <div class="controls" style="display: none">
                        <input type="text" class="input" id="copyHomework_homeworkId"
                               value="${homeworkId!}"
                               name="homeworkId">
                    </div>
                    <label for="copyHomework" class="control-label">学生ID</label>
                    <div class="controls">
                        <input type="text" class="input" id="copyHomework_studentId" value=""
                               name="studentId">
                    </div>
                </div>

                <div class="control-group">
                    <div class="controls">
                        <input type="submit" class="btn btn-primary" value="复制作业" id="copyHomework">
                    </div>
                </div>
            </fieldset>
        </form>
    </script>


    <script type="text/javascript">
        $(document).ready(function () {
            var $eventMobileModal = $("#eventMobileModal"),examId,mobileIframe = $eventMobileModal.find("iframe");
            $("a.J_mobilePreview").on("click",function(){
                examId = $(this).attr("data-qid");
                $eventMobileModal.modal();
            });
            $eventMobileModal.on("show",function(){
                var mobilePreviewObj = {
                    domain : "${domain!}",
                    previewAbsoluteUrl : "${domain!}/resources/apps/hwh5/tiku_preview/V2_5_0/exam-v2/index.html",
                    param  : {
                        img_domain : "${domain!}",
                        render_type: "tiku_preview",
                        getQuestionByIdsUrl : "${domain!}/exam/flash/load/question/byids.vpage",
                        examResultUrl : "${domain!}/exam/flash/process/old/homework/result.vpage",
                        env           : "${env!}",
                        q_id          : examId
                    }
                },url = mobilePreviewObj.previewAbsoluteUrl,paramObj = mobilePreviewObj.param;
                paramObj = $.extend(true,{domain : mobilePreviewObj.domain},mobilePreviewObj.param);
                url = (url.indexOf("?") == -1 ? url + "?" : url + "&") + decodeURIComponent($.param(paramObj));
                mobileIframe.attr("src",url);
            });
            $("#copyHomework").on("click", function (e) {
                $("#alertDiv").html(template("alertTemple", {}));
                $("#alertDiv").dialog({
                    resizable: false,
                    height: "auto",
                    width: 600,
                    modal: true,
                    buttons: {
                        "关闭": function () {
                            $(this).dialog("close");
                        }
                    }
                });
            });
            if (location.hash !== '') $('a[href="' + location.hash + '"]').tab('show');
            return $('a[data-toggle="tab"]').on('shown', function (e) {
                return location.hash = $(e.target).attr('href').substr(1);
            });
        });
    </script>
</@layout_default.page>