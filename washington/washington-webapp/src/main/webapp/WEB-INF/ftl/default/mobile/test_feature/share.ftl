<#include "./layout.ftl">

<@layout.page className='test_share' title="测试分享" headBlock=headBlock bottomBlock=bottomBlock>
    <#escape x as x?html>
        <h2>该页面用来提供给客户端进行页面分享使用的. 产品环境上该接口是重定向到17zuoye首页</h2>
        <br>
        <h3>分享链接为 http://www.17zuoye.com  故能打开17zuoye首页,即为分享成功</h3>
        <br>
        <h3>分享参数:
            <pre>
                方法名 :  shareInfo
                title : "测试分享",
                content : "这是一个测试分享的页面 默认跳到我们首页",
                icon : "http://cdn-cnc.17zuoye.cn/public/skin/parentMobile/images/activity/winterHoliday/parentApp-winterHoliday-beans.png",
                url : "http://www.17zuoye.com/"
            </pre>
        </h3>
        <br>
        <button id="test_share_no_icon">测试没有带icon字段的分享</button>
        <button id="test_share">测试一般的分享</button>
    </#escape>
</@layout.page>
