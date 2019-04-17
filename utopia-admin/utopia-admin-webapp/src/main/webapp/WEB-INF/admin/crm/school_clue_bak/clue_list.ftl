<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="学校线索审核" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>
<#--<script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=stpdH3wKubAUFfjRZ8ELoN2A"></script>-->
<script type="text/javascript" src="https://webapi.amap.com/maps?v=1.3&key=352b709bf5ce3f0212bc6ced5b641980"></script>
<div class="span11">
    <legend>
        学校信息审核&nbsp;&nbsp;
        <a href="/crm/teacher_fake/teacher_fakes.vpage">判假老师审核</a>&nbsp;&nbsp;
        <a href="/crm/teacher_appeal/index.vpage">老师申诉审核</a>&nbsp;&nbsp;
        <a href="/crm/teachertransfer/teacherTransferSchool.vpage">转校审核</a>
    </legend>

    <#if schoolAuthOperate || schoolInfoOperate>
        <form id="iform" action="/crm/school_clue/clue_list.vpage" method="post">
            <ul class="inline">
                <li>
                    <label for="schoolName">
                        学校名称
                        <input name="schoolName" id="schoolName" value="${querySchoolName!}" type="text"/>
                    </label>
                </li>
                <li>
                    <label for="category">
                        类别
                        <select id="category" name="category">
                            <#if schoolAuthOperate>
                                <option value="op_auth"
                                        <#if queryCategory?? && queryCategory == 'op_auth'>selected="selected"</#if>>
                                    学校鉴定
                                </option>
                                <option value="op_critical"
                                        <#if queryCategory?? && queryCategory == 'op_critical'>selected="selected"</#if>>
                                    紧急学校
                                </option>
                            </#if>
                            <#if schoolInfoOperate>
                                <option value="op_info"
                                        <#if queryCategory?? && queryCategory == 'op_info'>selected="selected"</#if>>
                                    信息完善
                                </option>
                                <option value="op_sign_in"
                                        <#if queryCategory?? && queryCategory == 'op_sign_in'>selected="selected"</#if>>
                                    照片签到
                                </option>
                            </#if>
                        </select>
                    </label>
                </li>
                <li>
                    <label for="status">
                        审核状态
                        <select id="status" name="status">
                            <option value="1" <#if queryStatus?? && queryStatus  == 1>selected="selected"</#if>>待审核
                            </option>
                            <option value="2" <#if queryStatus?? && queryStatus  == 2>selected="selected"</#if>>已通过
                            </option>
                            <option value="-1" <#if queryStatus?? && queryStatus  == -1>selected="selected"</#if>>已驳回
                            </option>
                        </select>
                    </label>
                </li>
                <li>
                    <label for="provinceName">
                        所在省
                        <input name="provinceName" id="provinceName" value="${queryProvinceName!}" type="text"/>
                    </label>
                </li>
                <li>
                    <label for="cityName">
                        所在市
                        <input name="cityName" id="cityName" value="${queryCityName!}" type="text"/>
                    </label>
                </li>

                <li>
                    <label for="recorderName">
                        申请人
                        <input name="recorderName" id="recorderName" value="${queryRecorderName!}" type="text"/>
                    </label>
                </li>
                <li>
                    <label for="reviewerName">
                        审核人
                        <input name="reviewerName" id="reviewerName" value="${queryReviewerName!}" type="text"/>
                    </label>
                </li>
                <li>
                    <label for="createTime">
                        创建日期：
                        <input name="createStart" id="createStart" value="${queryCreateStart!}" type="text"
                               class="date"/> -
                        <input name="createEnd" id="createEnd" value="${queryCreateEnd!}" type="text" class="date"/>
                    </label>
                </li>
            </ul>

            <ul class="inline">
                <li>
                    <button type="button" onclick="submitForm()">查询</button>
                </li>
                <li>
                    <input type="button" value="重置" onclick="formReset()"/>
                </li>
                <li>
                    <label>共${totalCount!0}条记录</label>
                </li>
            </ul>

            <input id="PAGE" name="PAGE" type="hidden"/>
            <input id="SIZE" name="SIZE" value="25" type="hidden"/>
            <input id="ORDER" name="ORDER" value="ASC" type="hidden"/>
            <input id="SORT" name="SORT" value="updateTime" type="hidden"/>
        </form>

        <#setting datetime_format="yyyy-MM-dd HH:mm"/>
        <div>
            <table class="table table-bordered">
                <tr>
                    <th>类别</th>
                    <th>学校创建时间</th>
                    <th>更新时间</th>
                    <th>所属区域</th>
                    <#--<th>学校名称</th>-->
                    <th>主干名称</th>
                    <th>分校名称</th>
                    <th>学校简称</th>
                    <th>小学/初中/高中</th>
                    <th>申请人</th>
                    <th>联系方式</th>
                    <th>审核人</th>
                    <th>审核时间</th>
                    <th>驳回原因</th>
                    <th>操作</th>
                </tr>
                <tbody>
                    <#if schoolClues??>
                        <#list schoolClues as schoolClue>
                            <#assign oldSchool = schoolClue.schoolId??/>
                        <tr>
                            <td>
                                <#if schoolClue.authenticateType?? && schoolClue.authenticateType == 1>
                                    学校鉴定
                                <#elseif schoolClue.authenticateType?? && schoolClue.authenticateType == 2>
                                    完善信息
                                <#elseif schoolClue.authenticateType?? && schoolClue.authenticateType == 3>
                                    紧急学校
                                <#else>
                                    类型异常
                                </#if>
                            </td>
                            <td>${schoolClue.createTime!}</td>
                            <td>${schoolClue.updateTime!}</td>
                            <td>${schoolClue.provinceName!} ${schoolClue.cityName!} ${schoolClue.countyName!}</td>
                            <td>
                                <#if oldSchool>
                                    <a href="/crm/school/schoolhomepage.vpage?schoolId=${schoolClue.schoolId!}" target="_blank">${schoolClue.cmainName!}</a>
                                <#else>
                                    ${schoolClue.cmainName!}
                                </#if>
                            </td>
                            <td>
                                <#if oldSchool>
                                    <a href="/crm/school/schoolhomepage.vpage?schoolId=${schoolClue.schoolId!}" target="_blank">${schoolClue.schoolDistrict!}</a>
                                <#else>
                                    ${schoolClue.schoolDistrict!}
                                </#if>
                            </td>
                            <#--<td>-->
                                <#--<#if oldSchool>-->
                                    <#--<a href="/crm/school/schoolhomepage.vpage?schoolId=${schoolClue.schoolId!}"-->
                                       <#--target="_blank">${schoolClue.schoolName!}</a>-->
                                <#--<#else>${schoolClue.schoolName!}-->
                                <#--</#if>-->
                            <#--</td>-->
                            <td <#if schoolClue.masterSchoolId??>style="background-color: #ef784e"</#if>>${schoolClue.shortName!}</td>
                            <td>${(schoolClue.showPhase.description)!}</td>
                            <td>${schoolClue.recorderName!}</td>
                            <td>${schoolClue.recorderPhone!}</td>
                            <td>${schoolClue.reviewerName!""}</td>
                            <td>${schoolClue.reviewTime!}</td>
                            <td>${schoolClue.reviewNote!}</td>
                            <td><input type="button" value="审核" onclick="review('${schoolClue.id!}')"/></td>
                        </tr>
                        </#list>
                    </#if>
                </tbody>
            </table>

            <#assign pager = schoolClues!>

        </div>

        <#include "clue_review.ftl">

    </#if>
</div>

<script type="text/javascript">
    $(function () {
        dater.render();
    });
    function formReset() {
        $("#status").val("1");
        $("#newSchool").val("");
        $("#schoolName").val("");
        $("#provinceName").val("");
        $("#cityName").val("");
        $("#recorderName").val("");
        $("#createStart").val("");
        $("#createEnd").val("");
    }

    function submitForm() {
        if (validate()) {
            $("#iform").submit();
        }
    }

    function validate() {
        var start = $("#createStart").val();
        var end = $("#createEnd").val();
        if (start > end) {
            alert("创建的开始时间不能大于结束时间");
            return false;
        }
        return true;
    }
</script>
</@layout_default.page>