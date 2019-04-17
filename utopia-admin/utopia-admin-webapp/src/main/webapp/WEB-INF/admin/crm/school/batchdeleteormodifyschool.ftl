<#-- @ftlvariable name="batchType" type="java.lang.String" -->
<#-- @ftlvariable name="batchDeleteOrModifyDesc" type="java.lang.String" -->
<#-- @ftlvariable name="schoolIds" type="java.lang.String" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
    <style>
        span {font: "arial";}
        .index {color: #0000ff;}
        .index, .item {font-size: 18px; font: "arial";}
        .warn {color: red;}
    </style>
    <div class="span9">
        <form action="?" method="post">
            <fieldset>
                <#switch batchType>
                    <#case 'batchdelete'>
                        <legend>批量删除学校</legend>
                        <#break>
                    <#case 'batchmodifyregion'>
                        <legend>批量修改学校区域</legend>
                        <ul class="inline">
                            <li>
                                <label>
                                    所在省：<select name="provinceCode" data-next_level="cityCode" >
                                    <option value="-1">全国</option>
                                    <#if provinces?has_content>
                                        <#list provinces as province>
                                            <option value="${province.code}">${province.name}</option>
                                        </#list>
                                    </#if>
                                </select>
                                </label>
                            </li>
                            <li>
                                <label>
                                    所在市：<select name="cityCode" data-init="false" data-default="${cityCode!}" data-next_level="countyCode">
                                    <option value="-1">全部</option>
                                </select>
                                </label>
                            </li>
                            <li>
                                <label>
                                    所在区：<select name="countyCode" data-init="false" data-default="${countyCode!}">
                                    <option value="-1">全部</option>
                                </select>
                                </label>
                            </li>
                        </ul>
                        <#break>
                </#switch>
                <ul class="inline">
                    <li>
                        <label style="font-size: medium">输入学校ID：<textarea name="schoolIds" style="width: 250px;" rows="10" placeholder="请以','或空白符隔开"><#if !success?? || !success>${schoolIds!}</#if></textarea></label>
                    </li>
                    <li>
                        <label style="font-size: medium">问题描述：<textarea name="batchDeleteOrModifyDesc" style="width: 250px;" rows="10" placeholder="问题描述不能为空"><#if !success?? || !success>${batchDeleteOrModifyDesc!}</#if></textarea></label>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <button type="submit" class="btn btn-primary">提交</button>
                    </li>
                </ul>
            </fieldset>
        </form>
        <fieldset>
            <legend>学校ID列表</legend>
            <div class="clear"></div>
            <div id="batch_delete_or_modify_tip"></div>
            <div class="clear"></div>
            <div id="school_id_list"></div>
        </fieldset>
    </div>
    <script>
        // todo : 这里应该改写成marco
        <#if batchType = 'batchmodifyregion'>
            // 省,市,区
            $(function (){

                var $provinces = $('select[name="provinceCode"]');

                $('select[data-next_level]').on('change', function() {

                    var $this = $(this);
                    var $nextLevel = $('select[name="' + $this.data('next_level') + '"]');
                    if($this.val() < 0) {
                        return;
                    }
                    $.get('${requestContext.webAppContextPath}/crm/user/regionlist.vpage?regionCode=' + $this.val(), function(data) {
                        //noinspection JSUnresolvedVariable
                        var regionList = data.regionList;
                        var regionsStr = '';
                        if(regionList.length == 0) {
                            regionsStr = '<option value="-1">全部</option>';
                        } else {
                            for(var i = 0; i < regionList.length; i++) {
                                regionsStr += '<option value=' + regionList[i]['code'] + '>' + regionList[i]['name'] + '</option>';
                            }
                        }

                        if($nextLevel) {
                            $nextLevel.html(regionsStr);
                            if(!$nextLevel.data('init')) {
                                $nextLevel.val($nextLevel.data('default'));
                                $nextLevel.data('init', true);
                            }
                        }

                        $nextLevel.trigger('change');
                    });
                });

                <#if provinceCode?? && provinceCode gt 0 >
                    $provinces.val('${provinceCode!}');
                    $provinces.trigger('change');
                </#if>

            });
        </#if>

        $(function() {

            var $schoolIds = $('[name="schoolIds"]');

            $schoolIds.on('keyup', function(){

                var content = $(this).val();
                var schoolIdList = content.split(/[,，\s]+/);

                var $schoolIdList = $('#school_id_list');
                $schoolIdList.empty();
                $schoolIdList.append('<br/><ul class="inline"></ul>');

                var $batchDeleteOrModifyTip = $('#batch_delete_or_modify_tip');
                $batchDeleteOrModifyTip.text('');

                var $schoolIdListNode = $schoolIdList.find('ul');
                var wrongIds = '';

                for(var i = 0, length = schoolIdList.length; i < length; i++) {

                    if(schoolIdList[i] == '') {
                        continue;
                    }

                    if(!schoolIdList[i].match(/^\d+$/)) {

                        if(wrongIds != '') {
                            wrongIds += ',';
                        } else {
                            wrongIds += '<span class="warn">提示：</span>';
                        }

                        wrongIds += '<span class="warn">[' + i + ']</span><span>' + schoolIdList[i] + '</span>';
                        $schoolIdListNode.append('<li><span class="index warn">[' + i + '] </span><span class="item">' + schoolIdList[i] + '</span></li><br/>');

                    } else {
                        $schoolIdListNode.append('<li><span class="index">[' + i + '] </span><span class="item">' + schoolIdList[i] + '</span></li><br/>');
                    }

                }

                if (wrongIds != '') {
                    $batchDeleteOrModifyTip.append( wrongIds + '<span class="warn"> 不是规范的学校ID</span>');
                }

            });

            <#if !success?? && schoolIds?has_content>
                $schoolIds.trigger('keyup');
            </#if>
        });
    </script>
</@layout_default.page>