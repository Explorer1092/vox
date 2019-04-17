<#import "../../researchstaffv3.ftl" as com>
<@com.page menuIndex=20 menuType="normal">
<ul class="breadcrumb_vox">
    <li><a href="javascript:void(0);">组卷统考</a> <span class="divider">/</span></li>
    <li class="active">试卷及报告</li>
</ul>
<div class="r-titleResearch-box">
    <p>
        ${termText}${currentUser.formatManagedRegionStr()}小学${(currentUser.subject.value)!}教研试卷及数据报告
    </p>
    <p> 数据更新日期：${updateDate}</p>
</div>
<div class="r-mapResearch-box">
    <div class="r-table">
        <#if bookMapperList?has_content && bookMapperList?size gt 0>
            <#list bookMapperList as bookMapper>
            <table>
                <thead>
                <tr class="blues">
                    <td class="bookName">${bookMapper.bookName}</td>
                    <td style="width: 170px;">${bookMapper.examPaperIds?size}份试卷</td>
                    <td style="width: 110px;cursor: pointer;" data-bookid="${bookMapper.bookId}" data-paperids="${bookMapper.examPaperIdsStr()}" data-current="hide" class="showOrHide">展开</td>
                </tr>
                </thead>
            </table>
            <table id="bookId_${bookMapper.bookId}">
            </table>
            </#list>
        <#else>
            <p style="font-size: 12px;font-weight: normal;">组卷并公开试卷，您所在地区的师生使用后，您就能看到分析报告了。</p>
            <p style="font-size: 12px;font-weight: normal;">您还可以提供您的试卷，由我们的工作人员帮您录入，发布线上统考。</p>
        </#if>
    </div>
</div>
<script id="t:试卷列表" type="text/html">
    <tbody>
        <tr>
            <td>试卷名称</td>
            <td style="width: 50px;">出题人</td>
            <td style="width: 27px;">题量</td>
            <td style="width: 91px;">创建时间</td>
            <td style="width: 31px;">使用老师</td>
            <td style="width: 31px;">完成学生</td>
            <td style="width: 31px;">试卷状态</td>
            <td style="width: 120px;">操作</td>
        </tr>
        <%
        if(paperMapper != null && paperMapper.length > 0){
          for(var i = 0; i < paperMapper.length; i++){
        %>
        <tr>
            <td class="text_blue paperName" data-paperid="<%=paperMapper[i].examPaperId%>">
                <a href="/rstaff/exampaper/preview.vpage?paperId=<%=paperMapper[i].examPaperId%>" target="_blank">
                    <%=paperMapper[i].examPaperName%>
                </a>
            </td>
            <td><%=paperMapper[i].authorName%></td>
            <td><%=paperMapper[i].questionNum%></td>
            <td><%=paperMapper[i].createTimeStr%></td>
            <td><%=paperMapper[i].authTeacherCntHasExamPaper%></td>
            <td><%=paperMapper[i].completeStudentCnt%></td>
            <td><%=paperMapper[i].state%></td>
            <td>
                <%if(subject == 'ENGLISH'){%>
                <input type="button" class="viewPaperReport" value="查看报告">
                <%}%>
                <input type="button" class="moreOperate" value="更多操作">
            </td>
        </tr>
        <%}}else{%>
        <tr>
            <td class="text_blue" colspan="7">暂无相关数据</td>
        </tr>
        <%}%>
    </tbody>
</script>
<script type="text/javascript">
    $(function(){
        var BookOperate = {
            init : function(){
                //单元格展开或收起
                $("td.showOrHide").on("click",function(){
                    var $this = $(this);
                    var bookId = $this.attr("data-bookid");
                    var targetTable = $("#bookId_" + bookId);
                    //折叠状态
                    if($this.attr("data-current") == "hide"){
                        if($this.isFreezing()){
                            // 表示加载过数据
                            targetTable.show();
                        }else{
                            // 表示未加载过数据
                            //ajax
                            var $table = $this.closest("table");
                            var paperIds = $this.attr("data-paperids");
                            targetTable.addClass("pageLoding");
                            $.post("/rstaff/testpaper/paperreport/paper.vpage",{paperIds : paperIds },function(data){
                                $this.freezing();
                                targetTable.removeClass("pageLoding").empty().append(template("t:试卷列表",{
                                    bookId : bookId,
                                    bookName:$this.siblings("td.bookName").text(),
                                    paperMapper : data.paperMapper,
                                    subject : "${(currentUser.subject)!}"
                                }));
                            });
                        }
                        $this.text("收起");
                        $this.attr("data-current","show");
                    }else{
                        $this.text("展开");
                        $this.attr("data-current","hide");
                        targetTable.hide();
                    }
                });
            }
        };
        BookOperate.init();


        var paperOperate = {
            init : function(){
                $(document).on("click","input.moreOperate",function(){
                    var $this = $(this);
                    var $paper = $this.closest("td").siblings(".paperName");
                    var paperId = $paper.data("paperid");
                    window.location.href = "/rstaff/exampaper/paper.vpage?" + jQuery.param({paperId:paperId});
                });
                //本学期报告
                $(document).on("click","input.viewPaperReport",function(){
                    $17.tongji("教研员-试卷及报告-查看报告");
                    var $this = $(this);
                    var $paper = $this.closest("td").siblings(".paperName");
                    var paperId = $paper.data("paperid");
                    window.location.href = "/rstaff/testpaper/paperreport/reportdetail.vpage?" + jQuery.param({paperId:paperId});
                });
            }
        };
        paperOperate.init();
    });
</script>
</@com.page>