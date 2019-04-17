<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<div class="span9">
    <form method="post" action="genconfirmdata.vpage">
        <ul class="inline">
            <li>
                <label>
                    <p>输入要充值的数据(充值类型 充值描述 用户ID 充值手机 充值金额（分） 短信内容 状态)</p>
                    <p>充值类型：客服：80，运营－张璐：81，运营－陆军：82，运营－晓芸：83，市场：84，产品：85，中学：90</p>
                    <p>充值描述：请描述清楚是什么活动做的充值操作</p>
                    <p>短信内容：如果需要发短信请输入短信内容，如果不需要，短信内容为空即可</p>
                    <p>状态：0：系统将进行充值操作，如果是通过其他途径已经充值过了，仅仅是数据导入，状态请用2</p>
                    <textarea name="content" cols="45" rows="10" placeholder="5	test 121886 15133741803 300 短信内容可以为空 0"></textarea>
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
