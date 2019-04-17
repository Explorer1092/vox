<#--KTwelve : PRIMARY_SCHOOL 小学  JUNIOR_SCHOOL初中 ; eduSystem: J3 三年制 , J4 四年制 , P5 五年制 , P6 六年制 ; clazzLevel 年级-->
<#macro clazzLevelList KTwelve eduSystem clazzLevel>
    <#assign numberList = ['一年级','二年级','三年级','四年级','五年级','六年级','七年级','八年级','九年级']/>
    <#if KTwelve == 'PRIMARY_SCHOOL'>
        <#if eduSystem == 'P5'>
            <#list 1..5 as p5>
                <a data-tablevel="${p5}" class="<#if clazzLevel == p5>active</#if>" href="javascript:void (0)">${numberList[p5-1]}</a>
            </#list>
        <#elseif eduSystem == 'P6'>
            <#list 1..6 as p6>
                <a data-tablevel="${p6}" class="<#if clazzLevel == p6>active</#if>" href="javascript:void (0)">${numberList[p6-1]}</a>
            </#list>
        </#if>
    <#elseif KTwelve == 'JUNIOR_SCHOOL'>
        <#if eduSystem == 'J3'>
            <#list 7..9 as j3>
                <a data-tablevel="${j3}" class="<#if clazzLevel == j3>active</#if>" href="javascript:void (0)">${numberList[j3-1]}</a>
            </#list>
        <#elseif eduSystem == 'J4'>
            <#list 6..9 as j4>
                <a data-tablevel="${j4}" class="<#if clazzLevel == j4>active</#if>" href="javascript:void (0)">${numberList[j4-1]}</a>
            </#list>
        </#if>
    </#if>
</#macro>

<div class="t-student-materials">
    <div class="nav">
        <input id="search_input" class="w-input" type="text" onblur="SGW(this);" onfocus="HGW(this);" value="输入关键字搜索教材">
        <div class="nav-list" id="clazz_list_box">
            <#--根据是否加入班级 显示相对应的教材列表-->
            <#if (currentStudentDetail.clazz.fetchClazzKtwelve())?has_content>
                <@clazzLevelList KTwelve=(currentStudentDetail.clazz.fetchClazzKtwelve()) eduSystem=(currentStudentDetail.clazz.eduSystem) clazzLevel=(currentStudentDetail.clazz.classLevel?number) />
            <#else>
                <@clazzLevelList KTwelve="PRIMARY_SCHOOL" eduSystem='P6' clazzLevel=1 />
            </#if>
            <#--<a href="javascript:void (0)">更换记录</a>-->
        </div>
    </div>
    <div class="t-learn-mater" id="l_books_list_box">

    </div>
</div>

<script type="text/javascript">
    var GW = "输入关键字搜索教材";
    function SGW(n){if($17.isBlank(n.value)){n.value = GW;}}
    function HGW(n){if(GW == n.value){n.value = "";}}

    //根据年级和学科加载教材
    function getAllBooks(clazzLevel, subjectType){
        var search_input = $("#search_input");
        var searchName = search_input.val();
        var param = {level : clazzLevel , subjectType : subjectType };
        $.get('/student/learning/bookschip.vpage?'+ $.param(param), function(data){
            var box = $("#l_books_list_box");
            if($17.isBlank(data)){
                box.html('<div style="padding: 50px 0; text-align: center;">暂无相关教材</div>');
            }else{
                box.html(data);
            }

            //根据条件筛选
            search_input.val(searchName);
            search_input.fastLiveFilter("#l_books_list_box");
            if(searchName != GW){
                search_input.trigger('change');
            }
        });
    }

    $(function(){
        /*初始化*/
        var subjectType = '${(subjectType)!'ENGLISH'}';
        getAllBooks("${(currentStudentDetail.clazz.classLevel?number)!'1'}", subjectType);


        /*根据年级和科目选择教材*/
        $("#clazz_list_box a").on('click', function(){
            var $this = $(this);
            var clazzLevel = $this.data('tablevel');
            $this.addClass('active').siblings().removeClass('active');
            getAllBooks(clazzLevel, subjectType);
        });
    });
</script>