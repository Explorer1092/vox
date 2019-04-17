<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <link  href="${requestContext.webAppContextPath}/public/css/bootstrap.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/admin.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">
    <script src="${requestContext.webAppContextPath}/public/js/jquery-1.9.1.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/template.js"></script>
    <style>
        .table_soll{ overflow-y:hidden; overflow-x: auto;}
        .table_soll table td,.table_soll table th{white-space: nowrap;}
        .basic_info {margin-left: 2em;}
        .txt{margin-left: .5em;font-weight:800}
        .button_label{with:7em;height: 3em;margin-top: 1em}
        .info_td{width: 10em;}
        .info_td_txt{width: 13em;font-weight:600}
        .dropDownBox_tip{ position:absolute; z-index: 9; }
        .dropDownBox_tip span.arrow{ position: absolute; top:-9px; _top:-8px; left:20px; font: 18px/100% Arial, Helvetica, sans-serif; color: #ffe296;}
        .dropDownBox_tip span.arrow span.inArrow{ color: #feef94; position:absolute; left:0; top:1px;}
        .dropDownBox_tip span.arrowLeft{ left:-9px; top:15px;}
        .dropDownBox_tip span.arrowLeft span.inArrow{ left:1px; top:0px;}
        .dropDownBox_tip span.arrowRight{ left: auto; right:-9px; top:15px;}
        .dropDownBox_tip span.arrowRight span.inArrow{left:-1px; top:0px;}
        .dropDownBox_tip span.arrowBot{ top:auto; bottom:-9px; _bottom:-11px; left:20px;}
        .dropDownBox_tip span.arrowBot span.inArrow{ left:0; top:-1px}
        .dropDownBox_tip .tip_content{ border:1px solid #ffe296; background-color:#feef94; overflow:hidden; width:160px; color:#d77a00; padding:15px; border-radius: 3px;}
        .dropDownBox_tip h4.h-title{ font-size: 16px; padding: 5px 0 15px 0;}
        .dropDownBox_tip span.close{ cursor: pointer; position: absolute; right: 10px;top: 10px; color: #cca313; font-size: 22px;}
        .Calculation-detailClazz-box ul{ overflow: hidden; *zoom: 1;  padding: 15px 0; margin: 0!important;}
        .Calculation-detailClazz-box ul li{ float: left; font: bold 14px/1.125 "微软雅黑", "Microsoft YaHei", Arial; color: #666; width: 24%; text-align: center;}
        .Calculation-detailClazz-box ul li h5{ font-size: 14px; padding: 0 0 12px;}
        .Calculation-detailClazz-box ul li p{ font-size: 18px; font-weight: normal; padding: 0;}
        .Calculation-foot{ font-size: 12px; padding: 20px 0;}
        .Calculation-foot p{ padding: 5px 0;}
        .dropDownBox_tip .swap-box{ text-align: center; padding-top: 20px;}
        .dropDownBox_tip .swap-box span{ width: 110px; height: 110px; display: inline-block; background-color: #fff;}
        .w-blue{ color: #189cfb;}
        .w-red{ color: #e00;}
        .w-orange{ color: #fa7252;}
    </style>
    <style>
        detail {

        }
    </style>
</head>
<body style="background: none;">
<div style="margin-left: 2em" >
    <div style="margin-top: 2em">
    <#if (success)>
        <ul class="inline">
            <li><span style="font-weight:600">${hid!''}作业ID</span></li>
        </ul>
        <div>
            <table class="table table-hover table-striped table-bordered">
                <tr>
                    <th>作业详情</th>
                </tr>
                    <tr>
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
            </table>
        </div>

        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 单元ID</th>
                <th> 单元名字</th>
                <th> 作业ID</th>
                <th>试卷ID</th>
                <th>绘本ID</th>
                <th>视频ID</th>
                <th>读背包ID</th>
                <th>趣配音ID</th>
            </tr>
            <#if crmUnitQuestions?has_content>
                <#list crmUnitQuestions as crmUnitQuestion>
                    <tr>
                        <td>${crmUnitQuestion["unitId"]!''}</td>
                        <td>${crmUnitQuestion["unitName"]!''}</td>
                        <td>
                            <#if crmUnitQuestion.qidList??>
                                <#list crmUnitQuestion.qidList as crmQuestion>
                                ${crmQuestion["qid"]}
                                    <a target="_blank"
                                       href="http://koios.17zuoye.net/query?bkcid=${crmUnitQuestion["unitId"]}&qid=${crmQuestion["docId"]}">【超纲】</a>
                                </#list>
                            </#if>
                        </td>
                        <td>${(crmUnitQuestion["papers"]!'')}</td>
                        <td>${(crmUnitQuestion["pictureBooks"]!'')}</td>
                        <td>${(crmUnitQuestion["videos"]!'')}</td>
                        <td>${(crmUnitQuestion["questionBoxIds"]!'')}</td>
                        <td>${(crmUnitQuestion["dobbingIds"]!'')}</td>
                    </tr>
                </#list>
            <div id="alertDiv" title="作业信息">
            <#else >
                <td colspan="3">暂无作业信息</td>
            </#if>
        </table>
    <#else>
        <li><span style="font-weight:600">${desc!''}</span></li>
    </#if>
    </div>
</div>
</div>
</body>
</html>