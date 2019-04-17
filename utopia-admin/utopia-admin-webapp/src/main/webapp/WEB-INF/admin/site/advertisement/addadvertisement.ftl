<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加广告" page_num=4>
<link  href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<div id="main_container" class="span9">
    <legend>
        添加/编辑广告
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <div class="form-horizontal">
                    <input type="hidden" id="id" name="id" value="${ad.id!}"/ad>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">广告名称</label>
                        <div class="controls">
                            <input type="text" id="name" name="name" class="form-control" value="${ad.name!}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">说明</label>
                        <div class="controls">
                            <textarea type="text" id="description" name="description" class="form-control" rows="5">${ad.description!}</textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">广告主</label>
                        <div class="controls">
                            <select id="advertiserId" name="advertiserId">
                                <#list advertiserList as advertiser>
                                    <option value="${advertiser.id}" <#if ad.advertiserId?? && ad.advertiserId == advertiser.id>selected</#if>>${advertiser.name!}</option>
                                </#list>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">专题页</label>
                        <div class="controls">
                            <input type="text" id="resource_url" name="resource_url" class="form-control" value="${ad.resourceUrl!}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">广告类型</label>
                        <div class="controls">
                            <select id="type" name="type">
                                <#list adTypes as adType>
                                    <option value="${adType}" <#if ad.type?? && ad.type == adType>selected</#if>>${adType}</option>
                                </#list>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">广告合作形式</label>
                        <div class="controls">
                            <select id="cooperationType" name="cooperationType">
                                <#list cooperationTypes as cooperationType>
                                    <option value="${cooperationType}" <#if ad.cooperationType?? && ad.cooperationType == cooperationType>selected</#if>>${cooperationType}</option>
                                </#list>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">投放时间</label>
                        <div class="controls">
                            <input type="text" id="showTimeStart" name="showTimeStart" class="form-control" value="<#if ad.showTimeStart??>${ad.showTimeStart?string('yyyy-MM-dd')}</#if>"/>~
                            <input type="text" id="showTimeEnd" name="showTimeEnd" class="form-control" value="<#if ad.showTimeEnd??>${ad.showTimeEnd?string('yyyy-MM-dd')}</#if>"/>
                            &nbsp;<span style="color: #953b39">开始时间:预期投放开始日期的当天;截止时间:预期投放截止日期的后一天</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">年级</label>
                        <div class="controls">
                            <input type="text" id="gradeStart" name="gradeStart" class="form-control" value="${ad.gradeStart!}"/>~
                            <input type="text" id="gradeEnd" name="gradeEnd" class="form-control" value="${ad.gradeEnd!}"/>
                            &nbsp;<span style="color: #953b39">必填项:小学可填1-6;中学可填6-9</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">投放学校</label>
                        <div class="controls">
                            <input type="text" id="schoolIds" name="schoolIds" class="form-control" value="${ad.schoolIds!''}"/>&nbsp;<span style="color: #953b39">不限制学校此项不填即可</span>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="col-sm-2 control-label">学段</label>
                        <div class="controls">
                            <select id="ktwelve" name="ktwelve">
                                <option value="">不限</option>
                                <option value="1" <#if ad.ktwelve?? && ad.ktwelve == 1>selected</#if>>小学</option>
                                <option value="2" <#if ad.ktwelve?? && ad.ktwelve == 2>selected</#if>>中学</option>
                            </select>
                            <span style="color: #953b39">目前只老师APP使用。其他产品请不要填写</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">学科</label>
                        <div class="controls">
                            <select id="subject" name="subject">
                                <option value="">不限</option>
                                <option value="ENGLISH" <#if ad.subject?? && ad.subject == "ENGLISH">selected</#if>>英语</option>
                                <option value="MATH" <#if ad.subject?? && ad.subject == "MATH">selected</#if>>数学</option>
                                <option value="CHINESE" <#if ad.subject?? && ad.subject == "CHINESE">selected</#if>>语文</option>
                            </select>
                            <span style="color: #953b39">目前只老师APP使用。其他产品请不要填写</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">是否认证</label>
                        <div class="controls">
                            <select id="needAuth" name="needAuth">
                                <option value="0" <#if ad.needAuth?? && ad.needAuth == 0>selected</#if>>不限</option>
                                <option value="1" <#if ad.needAuth?? && ad.needAuth == 1>selected</#if>>是</option>
                                <option value="-1" <#if ad.needAuth?? && ad.needAuth == -1>selected</#if>>否</option>
                            </select>
                            <span style="color: #953b39">目前只老师APP使用。其他产品请不要填写</span>
                        </div>
                    </div>


                    <div class="control-group">
                        <label class="col-sm-2 control-label">特殊键</label>
                        <div class="controls">
                            <input type="text" id="specialKey" name="specialKey" class="form-control" value="${ad.specialKey!}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">人数要求</label>
                        <div class="controls">
                            <input type="text" id="personCountLimit" name="personCountLimit" class="form-control" value="${ad.personCountLimit!}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">预算</label>
                        <div class="controls">
                            <input type="text" id="budget" name="budget" class="form-control" value="${ad.budget!}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <a type="button" id="btn_cancel" href="advertisementindex.vpage" name="btn_cancel" class="btn">取消</a> &nbsp;&nbsp; <button id="add_ad_btn" type="button" class="btn btn-primary">保存</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">

    function validateInput(name, advertiserId, showTimeStart, showTimeEnd,
                           gradeStart, gradeEnd, personCountLimit, budget) {

        if(name == '') {
            alert('广告名称不能为空！');
            return false;
        }
        if(advertiserId == '') {
            alert('广告主不能为空！');
            return false;
        }
        if(showTimeStart != '' && showTimeEnd != '' && showTimeStart > showTimeEnd) {
            alert('投放开始时间不能晚于结束时间！');
            return false;
        }
//        if(showTimeStart != '') {
//            var startDate = new Date(showTimeStart);
//            var now = new Date();
//            now.setHours(0);
//            now.setMinutes(0);
//            now.setSeconds(0);
//            if(startDate.getTime() < now.getTime()) {
//                alert('投放开始时间不能早于当前时间！');
//                return false;
//            }
//        }
        if(gradeStart != '' && gradeEnd != '' && gradeStart > gradeEnd) {
            alert('起始年级不能大于结束年级！');
            return false;
        }
        if((gradeStart != '' && !$.isNumeric(gradeStart)) || (gradeEnd != '' && !$.isNumeric(gradeEnd))) {
            alert('年级格式不正确！请输入数字！');
            return false;
        }
        if(personCountLimit != '' && !$.isNumeric(personCountLimit)) {
            alert('人数要求格式不正确！请输入数字！');
            return false;
        }
        if(budget == '' || !$.isNumeric(budget)) {
            alert('预算不能为空且必须为数字类型！');
            return false;
        }
        return true;
    }

    $(function() {
        $("#showTimeStart").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        $("#showTimeEnd").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        $("#add_ad_btn").on("click",function(){
            var name = $("#name").val().trim();
            var description = $("#description").val().trim();
            var advertiserId = $("#advertiserId").find('option:selected').val();
            var resourceUrl = $("#resource_url").val().trim();
            var type = $("#type").find('option:selected').val();
            var cooperationType = $("#cooperationType").find('option:selected').val();
            var showTimeStart = $("#showTimeStart").val().trim();
            var showTimeEnd = $("#showTimeEnd").val().trim();
            var gradeStart = $("#gradeStart").val().trim();
            var gradeEnd = $("#gradeEnd").val().trim();
            var schoolIds = $("#schoolIds").val().trim();
            var personCountLimit = $("#personCountLimit").val().trim();
            var budget = $("#budget").val().trim();
            var ktwelve = $("#ktwelve").find('option:selected').val();
            var subject = $("#subject").find('option:selected').val();
            var needAuth = $("#needAuth").find('option:selected').val();
            var id = $("#id").val();
            var specialKey = $("#specialKey").val();
            if(!validateInput(name, advertiserId, showTimeStart, showTimeEnd,
                    gradeStart, gradeEnd, personCountLimit, budget)) {
                return false;
            }
            $.post('saveadvertisement.vpage',{
                id:parseInt(id),
                name:name,
                description:description,
                advertiserId:parseInt(advertiserId),
                resourceUrl:resourceUrl,
                type:type,
                cooperationType:cooperationType,
                showTimeStart:showTimeStart,
                showTimeEnd:showTimeEnd,
                gradeStart:gradeStart,
                gradeEnd:gradeEnd,
                schoolIds:schoolIds,
                personCountLimit:personCountLimit,
                budget:budget,
                ktwelve:ktwelve,
                subject:subject,
                needAuth:needAuth,
                specialKey:specialKey            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.href = 'advertisementindex.vpage';
                }
            });
        });

    });

</script>
</@layout_default.page>