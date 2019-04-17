<#import "../layout.ftl" as uClassDetail>
<@uClassDetail.page title="错题解析" pageJs="wrongExercise">
    <@sugar.capsule css=['unitReport'] />
<div id="loading" style="padding:50px 0; text-align:center">数据加载中...</div>
<div id="reportWrong" class="unitReports-box" style="display:none;">
    <div id="wrong">
        <div class="head">
             <h1>
                <span class="subHead"></span>
                高频错题
            </h1>
        </div>
        <div class="title">
            <i class="icon icon-5"></i>
            题目
        </div>
        <div class="container container-1">
            <div id="wrongQuestion" class="content"></div>
        </div>
    </div>
    <div class="container container-1" style="margin:5%;">
        <table>
            <tbody>
            <tr class="odd-1">
                <td>本题您的孩子</td>
                <td>班级正确率</td>
            </tr>
            <tr>
                <td>
                    <span id="rightFlag" style="display:none;" class="correct">正确</span>
                    <span id="wrongFlag" style="display:none;" class="wrong">错误</span>
                </td>
                <td id="accuracy"></td>
            </tr>
            </tbody>
        </table>
     </div>
    <div class="title">
        <i class="icon icon-6"></i>
        解析
    </div>

    <div class="container container-1">
        <div id="wrongQuestionAnalysis" class="content"></div>
    </div>
    <div class="info">
        <h2><a href="javascript:download()" class="btn-view">下载</a>下载一起作业APP即可直接免费训练</h2>
    </div>
</div>
<#include "../menu.ftl">
</@uClassDetail.page>