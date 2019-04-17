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
</head>
<body style="background: none;">
<div style="margin-left: 2em">
<#if success>
    <div style="margin-top: 2em">
        <ul class="inline">
            <li><span style="font-weight:600">${newVacationHomeworkPackagePanorama["packageId"]!''}学生作业情况</span>
            </li>
            <li><span
                    style="font-weight:600">开始人数:${newVacationHomeworkPackagePanorama["beginVacationHomeworkNum"]!''}/${newVacationHomeworkPackagePanorama["totalStudentNum"]!''}</span>
            </li>
            <li><span
                    style="font-weight:600">完成人数:${newVacationHomeworkPackagePanorama["finishedStudentNum"]!''}/${newVacationHomeworkPackagePanorama["totalStudentNum"]!''}</span>
            </li>
        </ul>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 学生名称</th>
                <th> 学生ID</th>
                <th> 完成作业数目</th>
                <th> 一共作业数目</th>
                <th> 开始作业数目</th>
                <th> 是否开始作业</th>
                <th> 作业完成时间</th>
                <th> 本次作业平均分</th>
                <th> 操作</th>
            </tr>
            <#if newVacationHomeworkPackagePanorama.vacationHomeworkStudentPanoramas?has_content>
                <#list newVacationHomeworkPackagePanorama.vacationHomeworkStudentPanoramas as newVacationHomeworkStudentPanorama>
                    <tr>
                        <td>${newVacationHomeworkStudentPanorama["studentName"]!''}</td>
                        <td>${newVacationHomeworkStudentPanorama["studentId"]!''}</td>
                        <td>${newVacationHomeworkStudentPanorama["finishedHomeworkNum"]!''}</td>
                        <td>${newVacationHomeworkStudentPanorama["totalHomeworkNum"]!''}</td>
                        <td>${newVacationHomeworkStudentPanorama["beginHomeworkNum"]!''}</td>
                        <td>
                            <#if (newVacationHomeworkStudentPanorama["beginPackage"])>
                            是
                        <#else >
                            否
                        </#if>
                        </td>
                        <td>${newVacationHomeworkStudentPanorama["endTime"]!''}</td>
                        <td>${newVacationHomeworkStudentPanorama["avgScore"]!''}</td>
                        <td><#if (newVacationHomeworkStudentPanorama["studentId"])??>
                            <a target="_blank" href="/crm/vacation/homework/studentpackage.vpage?studentId=${newVacationHomeworkStudentPanorama['studentId']!''}&packageId=${packageId!''}">
                                查看详情
                            </a>
                        </#if></td>
                    </tr>
                </#list>
            <#else >
                <td colspan="6">暂无历史信息</td>
            </#if>
        </table>
    </div>
<#else >
    <div>后端错误</div>
</#if>

</div>
</body>
</html>