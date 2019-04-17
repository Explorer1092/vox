<#assign numberList = ['一年级','二年级','三年级','四年级','五年级','六年级']/>
<div class="t-student-materials">
    <div class="nav">
        <input id="search_input" class="w-input" type="text" onblur="SGW(this);" onfocus="HGW(this);" value="输入关键字搜索教材">
        <div class="nav-list" id="clazz_list_box">
        <#list 1..6 as p6>
            <a data-tablevel="${p6}" class="<#if 1 == p6>active</#if>" href="javascript:void (0)">${numberList[p6-1]}</a>
        </#list>
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
        $.get('/guest/learning/bookschip.vpage?'+ $.param(param), function(data){
            var box = $("#l_books_list_box");
            if($17.isBlank(data)){
                box.html('<div style="padding: 50px 0; text-align: center;">暂无相关教材</div>');
            }else{
                box.html(data);
            }

            //根据条件筛选
            search_input.val(searchName)
            search_input.fastLiveFilter("#l_books_list_box");
            if(searchName != GW){
                search_input.trigger('change');
            }
        });
    }

    $(function(){
        /*初始化*/
        var subjectType = '${(subjectType)!'ENGLISH'}';
        getAllBooks("1", subjectType);


        /*根据年级和科目选择教材*/
        $("#clazz_list_box a").on('click', function(){
            var $this = $(this);
            var clazzLevel = $this.data('tablevel');
            $this.addClass('active').siblings().removeClass('active');
            getAllBooks(clazzLevel, subjectType);
        });
    });
</script>