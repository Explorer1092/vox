<#import "../layout_new.ftl" as layout>
<@layout.page group="work_record" title="选择学校">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="${backUrl!""}" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerText">选择学校</div>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <input type="text" placeholder="请输入关键字" id="schoolSearchInput">
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
<ul class="mobileCRM-V2-list" id="schoolContainer">

</ul>
</div>

<script id="schoolListTemp" type="text/x-handlebars-template">
{{#each schools}}
    <a href="javascript:void(0);" class="link">
        <li class="js-schoolItem" data-sid="{{schoolId}}">
            <p class="side-fl">
                {{schoolName}}
            </p>
            <p class="side-fr">{{schoolType level}}</p>
        </li>
    </a>
{{/each}}
</script>
<script>
    $(function(){

        var backUrl = "${backUrl!""}";

        //注册中小学类型helper
        Handlebars.registerHelper("schoolType",function(index){
            if(index == 1){
                return "小学";
            }else{
                return "中学";
            }
        });

        var getSchoolList = function(keyWord){
            var url = "searchVisitSchool.vpage";
            if(keyWord){
                url = "searchVisitSchool.vpage?schoolKey="+keyWord;
            }
//            renderSchoolList("#schoolListTemp",{},"#schoolContainer");

            $.get(url,function(res){
                if(res.success){
                    renderSchoolList("#schoolListTemp",res,"#schoolContainer");
                }else{
                    alert(res.info);
                }
                console.log(res);
            });
        };

        //渲染模板
        var renderSchoolList = function(tempSelector,data,container){
            var source   = $(tempSelector).html();
            var template = Handlebars.compile(source);

            $(container).html(template(data));
        };


        $(document).on("keypress","#schoolSearchInput",function(e){
            if(e.keyCode == 13){
                var schoolKeyWords = $(this).val().trim("");
                console.log(schoolKeyWords);
                getSchoolList(schoolKeyWords);

            }
        });

        $(document).on("click",".js-schoolItem",function(e){
            var sid = $(this).data("sid");
            var url = "saveVisitSchool.vpage";
            if(backUrl == "add_intoSchool_record.vpage"){
                url = "saveSchoolRecordSchool.vpage";
            }
            $.post(url,{schoolId:sid},function(res){
                if(res.success){
                   location.href =  backUrl;
                }else{
                    alert(res.info);
                }
                console.log(res);
            });

        });

        getSchoolList();
    });
</script>
</@layout.page>
