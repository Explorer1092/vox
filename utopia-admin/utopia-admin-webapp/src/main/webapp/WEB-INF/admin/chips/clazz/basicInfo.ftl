<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='基础信息' page_num=26>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/mizar/mizar.css" rel="stylesheet">
<style>
    .table-responsive {
        min-height: .01%;
        overflow-x: auto
    }

    @media screen and (max-width: 767px) {
        .table-responsive {
            width: 100%;
            margin-bottom: 15px;
            overflow-y: hidden;
            -ms-overflow-style: -ms-autohiding-scrollbar;
            border: 1px solid #ddd
        }

        .table-responsive > .table {
            margin-bottom: 0
        }

        .table-responsive > .table > tbody > tr > td, .table-responsive > .table > tbody > tr > th, .table-responsive > .table > tfoot > tr > td, .table-responsive > .table > tfoot > tr > th, .table-responsive > .table > thead > tr > td, .table-responsive > .table > thead > tr > th {
            white-space: nowrap
        }

        .table-responsive > .table-bordered {
            border: 0
        }

        .table-responsive > .table-bordered > tbody > tr > td:first-child, .table-responsive > .table-bordered > tbody > tr > th:first-child, .table-responsive > .table-bordered > tfoot > tr > td:first-child, .table-responsive > .table-bordered > tfoot > tr > th:first-child, .table-responsive > .table-bordered > thead > tr > td:first-child, .table-responsive > .table-bordered > thead > tr > th:first-child {
            border-left: 0
        }

        .table-responsive > .table-bordered > tbody > tr > td:last-child, .table-responsive > .table-bordered > tbody > tr > th:last-child, .table-responsive > .table-bordered > tfoot > tr > td:last-child, .table-responsive > .table-bordered > tfoot > tr > th:last-child, .table-responsive > .table-bordered > thead > tr > td:last-child, .table-responsive > .table-bordered > thead > tr > th:last-child {
            border-right: 0
        }

        .table-responsive > .table-bordered > tbody > tr:last-child > td, .table-responsive > .table-bordered > tbody > tr:last-child > th, .table-responsive > .table-bordered > tfoot > tr:last-child > td, .table-responsive > .table-bordered > tfoot > tr:last-child > th {
            border-bottom: 0
        }
    }

    .wh {
        width: 40px;
        text-align: center;
        /*white-space: nowrap;*/
        text-overflow: ellipsis; /* for IE */
        overflow: hidden;
    }

    .table_box {
        max-height: 700px;
    }

    .form-group {
        margin: 5px 0;
        display: inline-block;
    }

    .form-group .mylabel {
        width: 150px;
        text-align: right;
    }
</style>
<div id="main_container" class="span9">
    <div class="row-fluid">
        <div class="span12">
            <div>
                <h2 style="float: left">${clazzName}</h2>
                <form id="frm1" class="form form-inline form-horizontal" style="float: right;">
                    <input type="hidden" name="productId" value="${productId!}">

                    <div class="form-group">
                        <label for="" class="mylabel">数据类型：</label>
                        <select id="dataType" data-init='0' name="dataType" class="multiple district_select">
                            <option value="0" <#if dataType==0>selected</#if>>当日数据</option>
                            <option value="1" <#if dataType==1>selected</#if>>最新数据</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="" class="mylabel">班级(Class)：</label>
                        <select id="clazzId" data-init='false' name="clazzId" class="multiple district_select">
                            <option value="">----请选择----</option>
                            <#if clazzOptionList?size gt 0>
                                <#list clazzOptionList as e >
                                    <option value="${e.value!}" <#if e.selected>selected</#if>>${e.desc!}</option>
                                </#list>
                            </#if>
                        </select>
                    </div>

                    <div class="form-group" style="text-align: center;width: 150px;">
                        <button type="button" id="find" class="btn btn-info">查询</button>
                    </div>
                </form>
            </div>
            <table class="table table-bordered">
                <tr>
                    <td>班级ID(Class ID)</td>
                    <td>${clazz.clazzId!}</td>
                    <td>课程(Book)</td>
                    <td>${clazz.bookName!}</td>
                </tr>
                <tr>
                    <td>班主任(Teacher)</td>
                    <td>${clazz.clazzTeacherName!}</td>
                    <td>产品(Product)</td>
                    <td>${clazz.productName!}</td>
                </tr>
                <tr>
                    <td>用户上限(Limitation)</td>
                    <td>${clazz.userLimitation!}</td>
                    <td>用户数(Count)</td>
                    <td>${clazz.userCount!}</td>
                </tr>
                <tr>
                    <td>建立时间(Built-up Time)</td>
                    <td>${clazz.createTime!}</td>
                    <td></td>
                    <td></td>
                </tr>
            </table>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span12 well">
            <p style="font-size: 20px; font-weight: 600;">主动服提醒</p>
            <table class="table table-condensed  table-hover table-striped table-bordered" >
                <thead>
                <tr>
                    <td>服务类型</td>
                    <td>剩余任务量</td>
                    <td>操作</td>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>完课点评</td>
                    <td>${activeServiceRemained!}</td>
                    <td>
                        <button id="goToServiceBtn" class="btn btn-primary">去服务</button>
                    </td>
                </tr>
                <tr>
                    <td>催补课提醒</td>
                    <td>${remindRemained!}</td>
                    <td>
                        <button id="goToRemindBtn" class="btn btn-primary">去服务</button>
                    </td>
                </tr>
                <tr>
                    <td>绑定公众号</td>
                    <td>${bindingRemained!}</td>
                    <td>
                        <button id="goToBindingBtn" class="btn btn-primary">去服务</button>
                    </td>
                </tr>
                <tr>
                    <td>薯条英语开课指导</td>
                    <td>${instructionRemained!}</td>
                    <td>
                        <button id="goToInstructionBtn" class="btn btn-primary">去服务</button>
                    </td>
                </tr>
                <tr>
                    <td>续费提醒</td>
                    <td>${renewRemained!}</td>
                    <td>
                        <button id="goToRenewBtn" class="btn btn-primary">去服务</button>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
    <ul class="nav nav-tabs" role="tablist">
        <li role="presentation" class="active">
            <a href="basicInfo.vpage?clazzId=${clazz.clazzId!}&productId=${productId!}&dataType=${dataType!}">基础信息(Basic)</a>
        </li>
        <li role="presentation">
            <a href="operationInfo.vpage?clazzId=${clazz.clazzId!}&productId=${productId!}&wxAdd=2&wxLogin=2&wxCodeShowType=2&wxNickName=2">运营信息(Operation)</a>
        </li>
        <li role="presentation">
            <a href="userScore.vpage?clazzId=${clazz.clazzId!}&productId=${productId!}">用户成绩(User)</a>
        </li>
        <li role="presentation">
            <a href="generalInfo.vpage?clazzId=${clazz.clazzId!}&productId=${productId!}">综合信息(General)</a>
        </li>
    </ul>
    <table class="table table-hover table-striped table-bordered" style="table-layout:fixed">
        <tr>
            <td class="wh" title="本班定级人数">本班定级人数</td>
            <td class="wh" title="本班续费人数">本班续费人数</td>
            <td class="wh" title="本班续费率">本班续费率</td>
            <td class="wh" title="本期定级人数">本期定级人数</td>
            <td class="wh" title="本期续费人数">本期续费人数</td>
            <td class="wh" title="本期续费率">本期续费率</td>
        </tr>
        <#if basicInfo??>
            <td style="text-align: center">${basicInfo.classRankNum!}</td>
            <td style="text-align: center">${basicInfo.classPaidNum!}</td>
            <td style="text-align: center">${basicInfo.classPaidRate!}</td>
            <td style="text-align: center">${basicInfo.totalRankNum!}</td>
            <td style="text-align: center">${basicInfo.totalPaidNum!}</td>
            <td style="text-align: center">${basicInfo.totalPaidRate!}</td>
        </#if>
    </table>
    <div id="tableDiv" class="table-responsive table_box">
        <table class="table table-hover table-striped table-bordered" style="table-layout:fixed">
            <tr>
                <td class="wh" title="课次(Lesson)">课次</br>Lesson</td>
                <td class="wh" title="课程名称(Lesson Name)">课程名称</br>Lesson Name</td>
                <td class="wh" title="本班应到学生数">本班应到学生数</td>
                <td class="wh" title="本班完课量">本班完课量</td>
                <td class="wh" title="本班完课率">本班完课率</td>
                <td class="wh" title="本期应到学生数">本期应到学生数</td>
                <td class="wh" title="本期完课量">本期完课量</td>
                <td class="wh" title="本期完课率">本期完课率</td>
                <td class="wh" title="本班完课点评量">本班完课点评量</td>
                <td class="wh" title="本班完课点评率">本班完课点评率</td>
                <td class="wh" title="本期完课点评量">本期完课点评量</td>
                <td class="wh" title="本期完课点评率">本期完课点评率</td>
                <#--<td class="wh" title="本期完课率(Complete ratio/A)">本期完课率</br>Complete ratio/A</td>-->
                <#--<td class="wh" title="本期主动服务率(Active ratio/A)">本期主动服务率</br>Active ratio/A</td>-->
            </tr>
            <#if basicList?? && basicList?size gt 0>
                <#list basicList as e >
                    <tr>
                        <td style="text-align: center">${e.lesson!}<input type="hidden" value="${e.unitId!}"></td>
                        <td style="text-align: center">${e.unitName!}</td>
                        <td style="text-align: center">${e.clazzNum!}</td>
                        <td style="text-align: center">${e.complete_c!}</td>
                        <td style="text-align: center">${e.complete_ratio_c!}</td>
                        <td style="text-align: center">${e.totalNum!}</td>
                        <td style="text-align: center">${e.complete_a!}</td>
                        <td style="text-align: center">${e.complete_ratio_a!}</td>
                        <td style="text-align: center">${e.classRemarkNum!}</td>
                        <td style="text-align: center">${e.classRemarkRate!}</td>
                        <td style="text-align: center">${e.periodRemarkNum!}</td>
                        <td style="text-align: center">${e.periodRemarkRate!}</td>
                    </tr>
                </#list>
            <#else >
                <tr>
                    <td colspan="12"><strong>暂无数据</strong></td>
                </tr>
            </#if>
        </table>
    </div>
</div>
<script>
    $("#find").on('click', function () {
        $("#frm1").attr('action', "/chips/chips/clazz/manager/basicInfo.vpage");
        $("#frm1").submit();
    });
    $("#tableDiv").scroll(function(){
        var left=$("#tableDiv").scrollLeft();
        var trs=$("#tableDiv table tr");
        trs.each(function(i){
            if(i !== 0) {
                $(this).children().eq(0).css({
                    "position": "relative",
                    "top": "0px",
                    "left": left,
                    "background-color": "#f9f9f9"
                });
            }
        });
        var top = $("#tableDiv").scrollTop();
        trs.eq(0).children().each(function (i) {
            if(i !== 0) {
                $(this).css({
                    "position": "relative",
                    "top": top, "left": "0px",
                    "background-color": "#f9f9f9"
                });
            }
        });
        trs.eq(0).children().eq(0).css({"position": "relative", "top": top, "left": left,  "background-color": "#f9f9f9", "z-index": 1});
    });

    $('#goToServiceBtn').on('click', function () {
        window.open('/chips/ai/active/service/index.vpage?classId=${clazz.clazzId!}&serviceType=SERVICE');
    });

    $('#goToRemindBtn').on('click', function () {
        window.open('/chips/ai/active/service/index.vpage?classId=${clazz.clazzId!}&serviceType=REMIND');
    });
    $('#goToBindingBtn').on('click', function () {
        window.open('/chips/ai/active/service/index.vpage?classId=${clazz.clazzId!}&serviceType=BINDING');
    });
    $('#goToInstructionBtn').on('click', function () {
        window.open('/chips/ai/active/service/index.vpage?classId=${clazz.clazzId!}&serviceType=USEINSTRUCTION');
    });
    $('#goToRenewBtn').on('click', function () {
        window.open('/chips/ai/active/service/index.vpage?classId=${clazz.clazzId!}&serviceType=RENEWREMIND');
    });
</script>
</@layout_default.page>