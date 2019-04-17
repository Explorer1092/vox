<#macro queryPage>
<div>
    <form method="post" action="schoollist.vpage" class="form-horizontal">
        <legend>学校查询</legend>
        <ul class="inline">
            <li>
                <label for="provinces">
                    所在省：
                    <select id="provinces" name="provinces" class="multiple district_select" next_level="citys">
                        <option value="-1">全国</option>
                        <#if provinces??>
                            <#list provinces as p>
                                <option value="${p.key}">${p.value}</option>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>
            <li>
                <label for="citys">
                    所在市：
                    <select id="citys" data-init='false' name="citys" class="multiple district_select" next_level="countys">
                        <option value="-1">全部</option>
                    </select>
                </label>
            </li>
            <li>
                <label for="countys">
                    所在区：
                    <select id="countys" data-init='false' name="countys" class="multiple district_select">
                        <option value="-1">全部</option>
                    </select>
                </label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <label for="schoolId">
                    学校ID：
                    <input name="schoolId" id="schoolId" <#if conditionMap?has_content>value="${(conditionMap.schoolId?html)!''}" </#if> type="text"/>
                </label>
            </li>
            <li>
                <label for="schoolName">
                    学校名：
                    <input name="schoolName" id="schoolName" <#if conditionMap?has_content>value="${(conditionMap.schoolName?html)!''}"</#if> placeholder="模糊查询，请尽量不要单独使用" type="text"/>
                </label>
            </li>
            <li>
                <label for="shortName">
                    学校简称：
                    <input name="shortName" id="shortName" <#if conditionMap?has_content>value="${(conditionMap.shortName?html)!''}"</#if> placeholder="模糊查询，请尽量不要单独使用" type="text"/>
                </label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <button id="query_info_btn" type="submit" class="btn btn-success">查 询</button>
            </li>
            <#if !requestContext.getCurrentAdminUser().isCsosUser()>
            <li>
                <a id="download_county_school_button" href="javascript:void(0)" class="btn btn-primary">按区导出学校</a>
            </li>
            <li>
                <#--<a>停止增加学校</a>-->
                <a href="addschool.vpage" class="btn btn-primary">增加学校</a>
            </li>
            <li>
                <a>暂停批量删除学校</a>
                <#--<a href="batchdelete/batchdeleteormodifyschool.vpage" class="btn btn-primary">批量删除学校</a>-->
            </li>
            <li>
                <a href="batchmodifyregion/batchdeleteormodifyschool.vpage" class="btn btn-primary">批量修改学校区域</a>
            </li>
            </#if>
        </ul>
    </form>
</div>
<script>

    function clearNextLevel(obj) {
        if(obj.attr("next_level")){
            clearNextLevel($("#" + obj.attr("next_level")).html('<option value=""></option>'));
        }
    }

    $(function () {
        <#if schoolSnapShotList?has_content && schoolSnapShotList?size == 1>
            window.open("schoolhomepage.vpage?schoolId=${schoolSnapShotList[0].schoolId!''}", "_blank");
        </#if>
        $(document).keydown(function (evt) {
            if (evt.keyCode === 13) {
                $('#query_info_btn').click();
            }
        });
        $(".district_select").on("change", function(){
            var html        = null;
            var $this       = $(this);
            var next_level  = $this.attr("next_level");
            var regionCode  = $this.val();
            if(next_level){
                var codeType = next_level;
                next_level = $("#" + next_level);
                clearNextLevel($this);
                $.ajax({
                    type : "post",
                    url : "regionlist.vpage",
                    data : {
                        regionCode : regionCode
                    },
                    success : function(data){
                        html = '';
                        var regionList = data.regionList;
                        for (var i in regionList) {
                            html += '<option value="' + regionList[i]["code"] + '">' + regionList[i]["name"] + '</option>';
                        }
                        next_level.html(html);
                        <#if conditionMap?has_content>
                            if(codeType == 'citys' && !next_level.data('init')){
                                next_level.val(${conditionMap.citys!'-1'});
                                next_level.data('init', true);
                            }else if (codeType == 'countys' && !next_level.data('init')){
                                next_level.val(${conditionMap.countys!'-1'});
                                next_level.data('init', true);
                            }
                        </#if>
                        next_level.trigger('change');
                    }
                });
            }
        });

        <#if conditionMap?has_content>
            $("#provinces").val('${conditionMap.provinces!"-1"}');
            $("#provinces").trigger('change');
        </#if>
    });

    $(function () {
        $('#download_county_school_button').on('click', function() {

            var countyCode = $('#countys').val();

            if(countyCode < 0) {
                alert('请选择要导出的学校所在区');
            } else {
                location.href = 'downloadcountyschool.vpage?countyCode=' + countyCode;
            }

        });
    });

</script>
</#macro>