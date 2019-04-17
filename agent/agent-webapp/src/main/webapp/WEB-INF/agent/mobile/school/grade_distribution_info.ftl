<#if schoolClue.schoolingLength?? && (schoolClue.schoolingLength == 1||schoolClue.schoolingLength == 2)>
    <#if schoolClue.grade1StudentCount??>
        <li>
            <div class="box">
                <div class="side-fl">一年级</div>
                <input type="text" readonly="readonly" placeholder="人数" class="side-fr side-time" name="firstYearNum"
                       id="firstYearNum" value="${schoolClue.grade1StudentCount!''}">

            </div>
        </li>
    </#if>
    <#if schoolClue.grade2StudentCount?? >
        <li>
            <div class="box">
                <div class="side-fl">二年级</div>
                <input type="text" readonly="readonly" placeholder="人数" class="side-fr side-time" name="secondYearNum"
                       id="secondYearNum" value="${schoolClue.grade2StudentCount!''}">
            </div>
        </li>
    </#if>
    <#if schoolClue.grade3StudentCount??>
        <li>
            <div class="box">
                <div class="side-fl">三年级</div>
                <input type="text" readonly="readonly" placeholder="人数" class="side-fr side-time" name="threeYearNum"
                       id="threeYearNum" value="${schoolClue.grade3StudentCount!''}">
            </div>
        </li>
    </#if>
    <#if schoolClue.grade4StudentCount??>
        <li>
            <div class="box">
                <div class="side-fl">四年级</div>
                <input type="text" readonly="readonly" placeholder="人数" class="side-fr side-time" name="fourYearNum"
                       id="fourYearNum" value="${schoolClue.grade4StudentCount!''}">
            </div>
        </li>
    </#if>
    <#if schoolClue.grade5StudentCount??>
        <li>
            <div class="box">
                <div class="side-fl">五年级</div>
                <input type="text" readonly="readonly" placeholder="人数" class="side-fr side-time" name="fiveYearNum"
                       id="fiveYearNum" value="${schoolClue.grade5StudentCount!''}">
            </div>
        </li>
    </#if>
    <#if schoolClue.schoolingLength == 2>
    <#if schoolClue.grade6StudentCount??>
    <li>
        <div class="box">
            <div class="side-fl">六年级</div>
            <input type="text" readonly="readonly" placeholder="人数" class="side-fr side-time" name="sixYearNum"
                   id="sixYearNum" value="${schoolClue.grade6StudentCount!''}">
        </div>
    </li>
    </#if>
    </#if>
<#elseif schoolClue.schoolingLength?? && (schoolClue.schoolingLength == 3||schoolClue.schoolingLength == 4)>
    <#if schoolClue.schoolingLength == 4>
        <#if schoolClue.grade6StudentCount??>
            <li>
                <div class="box">
                    <div class="side-fl">六年级</div>
                    <input type="text" readonly="readonly" placeholder="人数" class="side-fr side-time" name="sixYearNum"
                        id="sixYearNum" value="${schoolClue.grade6StudentCount!''}">
                </div>
            </li>
        </#if>
    </#if>
    <#if schoolClue.grade7StudentCount??>
        <li>
            <div class="box">
                <div class="side-fl">七年级</div>
                <input type="text" readonly="readonly" placeholder="人数" class="side-fr side-time" name="sevenYearNum"
                       id="sevenYearNum" value="${schoolClue.grade7StudentCount!''}">
            </div>
        </li>
    </#if>
    <#if schoolClue.grade8StudentCount??>
        <li>
            <div class="box">
                <div class="side-fl">八年级</div>
                <input type="text" readonly="readonly" placeholder="人数" class="side-fr side-time" name="eightYearNum"
                       id="eightYearNum" value="${schoolClue.grade8StudentCount!''}">
            </div>
        </li>
    </#if>
    <#if schoolClue.grade9StudentCount??>
        <li>
            <div class="box">
                <div class="side-fl">九年级</div>
                <input type="text" readonly="readonly" placeholder="人数" class="side-fr side-time" name="nineYearNum"
                       id="nineYearNum" value="${schoolClue.grade9StudentCount!''}">
            </div>
        </li>
    </#if>
</#if>
    <script>
        if(($('.js-gradeList').find("input").length) > 0){
            var sumary = 0;
            $.each($('.js-gradeList').find("input"),function(i,item){
                    sumary += parseInt($(item).val());
            });
            var summyHtml = '<li>'+
                    '<div class="box">'+
                    '<div class="side-fl" style="color: #999;">合计</div>'+
                    '<div class="side-fr side-time">'+sumary+'人</div>'+
                    '</div>'+
                    '</li>';
            $('.js-gradeList').append(summyHtml);
        }

    </script>
