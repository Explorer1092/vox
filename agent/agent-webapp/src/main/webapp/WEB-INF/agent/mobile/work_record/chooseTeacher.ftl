<#import "../layout_new.ftl" as layout>
<@layout.page group="work_record" title="选择">
<div class="mobileCRM-V2-header">
    <a style="display:none;" href="javascript:void(0);" class="headerBtn js-submitTehBtn">确定</a>
</div>
<div class="mobileCRM-V2-tab js-subTab">
    <div class="active" data-type="english">英语</div>
    <div data-type="math">数学</div>
    <div data-type="chinese">语文</div>
    <div data-type="other">其他</div>
</div>

<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <div class="js-TeacherCon" style="cursor: pointer;">
        <div class="englishCon">
            <#if english?size gt 0>
                <#list english as teacher>
                    <div>${teacher.teacherName!""}</div>
                    <input type="checkbox" data-tid="${teacher.teacherId}" <#if (teacher.checked)!false>checked</#if>>
                </#list>
            </#if>
        </div>
        <div class="mathCon" style="display: none;">
            <#if math?size gt 0>
                <#list math as teacher>
                    <div>${teacher.teacherName!""}</div>
                    <input type="checkbox" data-tid="${teacher.teacherId}" <#if (teacher.checked)!false>checked</#if>>
                </#list>
            </#if>
        </div>
        <div class="chineseCon" style="display: none;">
            <#if chinese?size gt 0>
                <#list chinese as teacher>
                    <div>${teacher.teacherName!""}</div>
                    <input type="checkbox" data-tid="${teacher.teacherId}" <#if (teacher.checked)!false>checked</#if>>
                </#list>
            </#if>
        </div>
        <div class="otherCon" style="display: none;">
            <#if other?size gt 0>
                <#list other as teacher>
                    <div>${teacher.teacherName!""}</div>
                    <input type="checkbox" data-tid="${teacher.teacherId}" <#if (teacher.checked)!false>checked</#if>>
                </#list>
            </#if>
        </div>
    </div>
</div>
<script src="/public/rebuildRes/js/mobile/intoSchool/chooseTeacherOld.js"></script>
<script>
    $(function(){

        $(document).on("click",".js-subTab>div",function(){
            var type = $(this).data("type");
            $(this).addClass("active").siblings("div").removeClass("active");
            $("."+type+"Con").show().siblings("div").hide();
        });

        $(document).on("click",".js-submitTehBtn",function(){
            var list = [];
            $.each($(".js-TeacherCon").find('input[type="checkbox"]'),function(i,item){
                if(item.checked){
                    console.log(item);
                    list.push($(item).data("tid"));
                }
            });

            console.log(list.join(","));

            $.post("saveTeacherList.vpage",{teacherIds:list.join(",")},function(res){
                if(res.success){
                    location.href = "add_intoSchool_record.vpage";
                }else{
                    alert(res.info);
                }
            });
        });

    });
</script>
</@layout.page>
