<#-- 假期作业卡片 -->
<#if (data.showVacationCard)!false>
    <li class="practice-block">
        <div class="practice-content">
            <h4>
                <span class="w-discipline-tag w-discipline-tag-15">假期作业</span>
            </h4>
            <div class="pc-article" style="margin-right: 10px;">
                <#if (data.mvhExist)!false>
                    <dl>
                        <dt><i class="h-v-some h-v-some-01"></i></dt>
                        <dd>
                            <p>数学假期作业</p>
                            <a class="btn-h-blue" style="line-height:23px;" href="/student/homework/vacation/index.vpage?packageId=${(data.mvh_id)!}&homeworkType=VACATION_MATH&_voxlog=true">进入</a>
                        </dd>
                    </dl>
                </#if>
                <#if (data.evhExist)!false>
                    <dl>
                        <dt><i class="h-v-some h-v-some-02"></i></dt>
                        <dd>
                            <p>英语假期作业</p>
                            <a class="btn-h-blue" style="line-height:23px;" href="/student/homework/vacation/index.vpage?packageId=${(data.evh_id)!}&homeworkType=VACATION_ENGLISH&_voxlog=true">进入</a>
                        </dd>
                    </dl>
                </#if>
                <#if (data.cvhExist)!false>
                    <dl>
                        <dt><i class="h-v-some h-v-some-03"></i></dt>
                        <dd>
                            <p>语文假期作业</p>
                            <a class="btn-h-blue" style="line-height:23px;" href="/student/homework/vacation/index.vpage?packageId=${(data.cvh_id)!}&homeworkType=VACATION_CHINESE&_voxlog=true">进入</a>
                        </dd>
                    </dl>
                </#if>
            </div>
        </div>
    </li>
</#if>
