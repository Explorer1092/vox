<#macro gameSpread type>
    <#if (!currentStudentDetail.inPaymentBlackListRegion)!true>
    <div class="v-textScroll" style="width: 1000px; height:54px;margin: 15px auto 0; clear: both;overflow: hidden;border:1px solid #d9d9d9; background-color: #fff;  border-radius: 10px; box-shadow: 0 2px 5px 0 #bbb; ">
    <#--英语-->
        <ul>
            <#if type == "ENGLISH">
                <li style="padding: 10px; ">
                    <a href="/student/nekketsu/adventure.vpage" target="_blank" class="w-btn w-btn-green" style="float: right; margin-top: -4px;">试用</a>
                    <span style="border-radius: 5px; display: inline-block; vertical-align: middle; overflow: hidden;"><img src="<@app.link href="public/skin/common/images/app-icon/big/Walker.png?1.0.5"/>" width="34"></span>
                    <span style="font-size: 16px; color: #a46821; display: inline-block; vertical-align: middle; padding-left: 10px;">沃克单词冒险——更有效率的单词记忆</span>
                </li>
            </#if>

        <#--数学-->
            <#if type == "MATH">
                <li style="padding: 10px;">
                    <a href="/student/apps/index.vpage?app_key=Stem101" target="_blank" class="w-btn w-btn-green" style="float: right; margin-top: -4px;">试用</a>
                    <span style="border-radius: 5px; display: inline-block; vertical-align: middle; overflow: hidden;"><img src="<@app.link href="public/skin/common/images/app-icon/big/Stem101.png?1.0.7"/>" width="34"></span>
                    <span style="font-size: 16px; color: #a46821; display: inline-block; vertical-align: middle; padding-left: 10px;">趣味数学训练营——让你成为学霸</span>
                </li>
            </#if>
        </ul>
    </div>
    <script type="text/javascript">
        $(function(){
            $(".v-textScroll").textScroll({
                line : 1,
                speed: 1000,
                timer: 5000
            });
        });
    </script>
    </#if>
</#macro>
