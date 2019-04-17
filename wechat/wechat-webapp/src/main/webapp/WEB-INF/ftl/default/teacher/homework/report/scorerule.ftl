<#import "../../layout_view.ftl" as homeworkReport>
<@homeworkReport.page title="分数计算规则" pageJs="scorerule">
    <@sugar.capsule css=['homework'] />
    <div class="jf-details" style="display: none;" data-bind="visible:homeworkType() == ''">
        <p class="text">1、本作业总成绩为所有题目平均分</p>
        <p class="text">2、等级计算关系</p>
        <table>
            <thead>
            <tr>
                <td>A</td>
                <td>B</td>
                <td>C</td>
                <td>D</td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>100分</td>
                <td>90分</td>
                <td>75分</td>
                <td>60分</td>
            </tr>
            </tbody>
        </table>
    </div>
    <div class="jf-details" style="display: none;" data-bind="visible:homeworkType() == 'ORAL_PRACTICE'">
        <p class="text">1、本作业总成绩为所有题目平均分</p>
        <p class="text">2、等级计算关系</p>
        <table>
            <thead>
            <tr>
                <td>A</td>
                <td>B</td>
                <td>C</td>
                <td>D</td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>100分</td>
                <td>90分</td>
                <td>75分</td>
                <td>60分</td>
            </tr>
            </tbody>
        </table>
    </div>
</@homeworkReport.page>