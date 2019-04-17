<#if ResearchStaffExamPaperMapper?? && ResearchStaffExamPaperMapper?size gt 0>
    <table class="table_vox table_vox_bordered table_vox_striped">
        <thead>
        <tr>
            <td>教材版本 </td>
            <td> 年 级 </td>
            <td> 册 别 </td>
            <td> 类 型 </td>
            <td>试卷名称 </td>
            <#if !ProductDevelopment.isProductionEnv()>
              <td>试卷ID</td>
            </#if>
        </tr>
        </thead>
        <tbody>
        <#list ResearchStaffExamPaperMapper.content as u>
        <tr>
            <td>${u.press!''}</td>
            <td>${u.fetchClassLevel().description!''}</td>
            <td>${u.fetchtTermType().value!''}</td>
            <td>${u.fetchExamPaperCategory().description!''}</td>
            <td>${u.examPaperName!''}</td>
            <#if !ProductDevelopment.isProductionEnv()>
             <td>${u.examPaperId!''}</td>
            </#if>
        </tr>
        </#list>
        </tbody>
    </table>
    <div class="common_pagination message_page_list" style="float: right;"></div>

    <script>
        $(function(){
            $(".message_page_list").page({
                total           : ${ResearchStaffExamPaperMapper.getTotalPages()!'0'},
                current         : ${(ResearchStaffExamPaperMapper.getNumber() + 1)!'0'},
                jumpCallBack    : function(index){
                    var _data = {
                        pressName     : '${pressName!}',
                        clazzLevel    : '${clazzLevel!}',
                        termType      : '${termType!}',
                        paperCategory : '${paperCategory!}'
                    };
                    $.post('/rstaff/testpaper/listchip.vpage?currentPage='+index, _data, function( data ){
                        $("#exam_message_info_box").html( data );
                    });
                }
            });
        });
    </script>
<#else>
    <div class="testpaperBox" style="height: 330px">
        <strong>未查询到结果</strong>
    </div>
</#if>
