<div class="my-space-personalPop">
    <dl class="my-space-personal">
        <dt>
            <img src="<@app.avatar href="${(studentCard.img)!''}"/>" />
        </dt>
        <dd>
            <h3>${(studentCard.realname)!''}
                <#--是否是vip-->
                <#if (studentCard.vip)!false>
                    <a href="/apps/afenti/order/exam-cart.vpage" target="_blank"><span class="space-icon space-icon-11"></span></a>
                </#if>
                <#if (studentCard.payOpen)!false>
                    <#--是否开通阿分题-->
                    <#if (studentCard.afentiExam)!false>
                        <a href="/apps/afenti/exam.vpage" class="space-icon space-icon-1 space-icon-product"></a>
                    </#if>
                </#if>
            </h3>
            <p><span class="title">学豆：</span>${(studentCard.silver)!''}</p>
            <p><span class="title">学霸：</span>${(studentCard.smCount)!''}</p>
            <p><span class="title">赞：</span>${(studentCard.likeCount)!''}</p>
        </dd>
    </dl>
    <div class="space-receive-gift">
        <div class="content">
            <#if (studentCard.gifts)?has_content>
                <span class="title">收到的礼物： </span>
            </#if>
            <ul>
                <#if (studentCard.gifts)??>
                    <#if studentCard.gifts?size == 0>
                        <li class="title">送同学礼物，他们也会回赠你哦</li>
                    <#else>
                        <#list studentCard.gifts as g>
                            <li><img src="<@app.link href="public/skin/common/images/gift/${g['img']}"/>"/></li>
                        </#list>
                    </#if>
                </#if>
                <li><a href="javascript:void(0);" class="space-icon space-icon-14 send-gift-button" data-student_id="${(studentCard.studentId)!}" title="送礼物"></a></li>
            </ul>
            <div class="clear"></div>
        </div>
    </div>
</div>