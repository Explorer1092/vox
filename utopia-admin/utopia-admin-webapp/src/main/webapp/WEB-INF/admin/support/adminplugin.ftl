<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#macro textareaWithChecker pageTitle="page" idsTitle="IDs" idsName="idsName"  contentTitle="content" contentName="contentName" idListName="idListName">
<link  href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<style>
    span {font: "arial";}
    .index {color: #0000ff;}
    .index, .item {font-size: 18px; font: "arial";}
    .warn {color: red;}
</style>
    <fieldset>
        <legend>${pageTitle!}</legend>
        <ul class="inline">
            <li>
                <label>${idsTitle!}：<textarea name="${idsName!}" cols="35" rows="3" placeholder="请以','或空白符隔开"></textarea></label>
            </li>
            <li>
                <label>${contentTitle!}<textarea name="${contentName!}" cols="35" rows="3" placeholder="请在这里输入"></textarea></label>
            </li>
        </ul>
    </fieldset>
    <br/>
    <fieldset>
        <legend>${idListName!}</legend>
        <div class="clear"></div>
        <div id="error_tip"></div>
        <div class="clear"></div>
        <div id="ids_list"></div>
    </fieldset>
<script>

    $(function(){
        $('[name="${idsName!}"]').on('keyup', function(){

            var idsStr = $(this).val();
            var idList = idsStr.split(/[,，\s]+/);

            var $idsList = $('#ids_list');
            $idsList.empty();
            $idsList.append('<br/><ul class="inline"></ul>');

            var $errorTip = $('#error_tip');
            $errorTip.text('');

            var $idsListULNode = $idsList.find('ul');
            var wrongIds = '';

            for(var i = 0, length = idList.length; i < length; i++) {

                if(idList[i] == '') {
                    continue;
                }

                if(!idList[i].match(/^\d+$/)) {
                    if(wrongIds != '') {
                        wrongIds += ',';
                    } else {
                        wrongIds += '<span class="warn">提示：</span>';
                    }

                    wrongIds += '<span class="warn">[' + i + ']</span><span>' + idList[i] + '</span>';
                    $idsListULNode.append('<li><span class="index warn">[' + i + '] </span><span class="item">' + idList[i] + '</span></li><br/>');
                } else {
                    $idsListULNode.append('<li><span class="index">[' + i + '] </span><span class="item">' + idList[i] + '</span></li><br/>');
                }

            }

            if (wrongIds != '') {
                $errorTip.append( wrongIds + '<span class="warn"> 不是规范的用户ID</span>');
            }

        });
    });
</script>
</#macro>