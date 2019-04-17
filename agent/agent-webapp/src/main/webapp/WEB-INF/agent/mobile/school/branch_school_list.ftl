<#import "../layout_new_no_group.ftl" as layout>
<#assign groupName="work_record">
<@layout.page title="完善信息">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="load_school_basic_info.vpage?schoolId=${schoolId!0}" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerText">添加分校</div>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <div class="inner">
        <div class="box">
            <div class="branch_school_headerSearch">
                <a href="javascript:void(0);" id="searchSchoolSubmit" class="branch_school_searchBtn">搜索</a>
                <div class="branch_school_box"><input type="text" placeholder="请输入学校名/ID" id="schoolKey" name="schoolKey" value="" class="branch_school_input"></input></div>
            </div>
        </div>
    </div>
</div>

<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <ul class="schoolSearchResult mobileCRM-V2-list">
        <li>
            <div class="side-fl">搜索结果：</div>
        </li>

    </ul>
</div>
<script>
    $("#searchSchoolSubmit").on("click", function () {
        var wd = $("#schoolKey").val();
        //清除上一次缓存
        $("li.js-schoolListDiv").remove();

        //搜索结果添加确认
        $(document).on("click", ".js-resultLink", function () {
            var schoolId = $(this).data("id");
            if (confirm("是否确认添加该分校？")) {
                $.post('add_branch_school.vpage', {
                    schoolId: schoolId
                }, function (data) {
                    if (data.success) {
                        alert("添加成功");
                        window.location.href = "load_school_basic_info.vpage?schoolId=${schoolId!0}";
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        if (wd) {
            $.post("/mobile/school_clue/find_repeat_branch_school.vpage", {schoolKey: wd}, function (result) {
                console.log(result);
                if (result.success) {
                    for (x in result.schoolShortInfos) {
                        var info = result.schoolShortInfos[x];
                        var resultHtml = '<li class="js-schoolListDiv">' +
                                '<a href="javascript:void(0);" class="link js-resultLink" data-id=' + info.schoolId + '>' +
                                '<div>' + info.schoolName + '</div>' +
                                '<div style="color: #999;">学校ID：' + info.schoolId + '(' + info.regionName + ')</div>' +
                                '</li>';
                        $("ul.schoolSearchResult").append(resultHtml);
                    }

                } else {
                    alert(result.info);
                }
            });
        } else {
            alert("请填写学校名称或者ID后搜索");
        }
    });


</script>
</@layout.page>