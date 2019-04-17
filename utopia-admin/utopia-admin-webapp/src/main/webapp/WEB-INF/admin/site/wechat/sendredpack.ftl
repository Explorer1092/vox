<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<div class="span9">
    <form method="post" action="genconfirmdata.vpage">
        <ul class="inline">
            <li>
                <label>
                    <p style="color: red">此功能目前只限于校园大使微信号发送现金红包，请务必将准备的数据放到excel里，然后再粘贴到下方数据框里</p>
                    <p>输入要发红包的数据(用户ID 活动类型 红包金额（分）)</p>
                    <p>活动类型：回流活动：2; 线下奖励：3;</p>
                    <textarea name="content" cols="45" rows="10" placeholder="121824 100 2"></textarea>
                </label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <input class="btn" type="submit" value="提交" />
            </li>
        </ul>
    </form>
</div>
</@layout_default.page>
