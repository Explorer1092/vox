<#import '../layout.ftl' as layout>

<@layout.page className='PersonAboutUs parentApp-bgColor' title="如何成为vip" pageJs=null >

    <div class="parentApp-aboutBox">
        <div class="boxHead"><img src="${buildStaticFilePath("parentApp-aboutBox-head.png", "img")}" /></div>
        <div class="boxText">一起作业家长通 V1.0</div>
    </div>
    <ul class="parentApp-editData">
        <li>
            <div>
                <span>联系电话</span>
                <span class="dataTel">
                    <a href="tel:<@ftlmacro.hotline/>" style="color: #5A70D6;"> <@ftlmacro.hotline/> </a>
                </span>
            </div>
        </li>
    </ul>

</@layout.page>
