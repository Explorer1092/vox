<#macro tabTemplate subject="">
    <script id="t:教学子目标形式" type="text/html">
        <!--ko if:currentTabs().length > 0-->
        <div class="w-base-switch w-base-two-switch h-switch">
            <ul class="Teachertitle">
                <!--ko foreach:currentTabs-->
                <li data-bind="click:$root.tabClick.bind($data,$root),css:{'active' : $data.type() == $root.focusTabType()}">
                    <a href="javascript:void(0);">
                        <span class="h-arrow"></span>
                        <i class="tab-icon" data-bind="style:{backgroundImage : $data.icon()}"></i>
                        <p data-bind="attr:{title:name},text:name"></p>
                    </a>
                </li>
                <!--/ko-->
            </ul>
            <!--ko if:tabs().length > $root.displayCount-->
            <div class="h-arrow h-arrow-L" data-bind="click:arrowClick.bind($data,'arrowLeft')"><i class="h-arrow-icon" data-bind="css:{'h-arrow-iconLhover' : leftEnabled()}"></i></div>
            <div class="h-arrow h-arrow-R" data-bind="click:arrowClick.bind($data,'arrowRight')"><i class="h-arrow-icon h-arrow-iconR" data-bind="css:{'h-arrow-iconRhover' : rightEnabled()}"></i></div>
            <!--/ko-->
        </div>
        <!--/ko-->
        <!--ko if:tabs().length == 0-->
        <div class="tabs-empty">当前课时暂无作业内容，请切换其他课时查看</div>
        <!--/ko-->
    </script>
    <script id="t:default" type="text/html">
        <div class="w-base-container">
            <div class="w-noData-box">
                没有该TAB下的数据模板
            </div>
        </div>
    </script>
    <#include "exam.ftl">
    <#include "quiz.ftl">
    <#include "levelreadings.ftl">
    <#switch subject>
        <#case "ENGLISH">
            <#include "english/basicapp.ftl">
            <#include "english/naturalspelling.ftl">
            <#include "english/dubbing.ftl">
            <#include "english/intelligentteaching.ftl">
            <#include "english/oralcommunication.ftl">
            <#include "english/dictation.ftl">
            <#break >
        <#case "MATH">
            <#include "math/mental.ftl">
            <#include "math/keypoints.ftl">
            <#include "math/intelligentteaching.ftl">
            <#include "math/ocrmentalarithmetic.ftl">
            <#include "../homeworkv3/math/calcintelligentteaching.ftl">
            <#break >
        <#case "CHINESE">
            <#include "chinese/readrecitewithscore.ftl">
            <#include "chinese/wordrecognitionandreading.ftl">
            <#include "chinese/wordteachandpractice.ftl">
            <#break >
    </#switch>
    <#include "photoobjective.ftl">
    <#include "voiceobjective.ftl">
    <#include "../kopagination.ftl">
</#macro>


