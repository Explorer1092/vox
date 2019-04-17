        <#if applyData?has_content && applyData.apply?has_content && applyData.apply.feedbackType?has_content>
            <div class="product_content" style="background:#fff;padding:30px 0 130px 90px;">
                <div>
                    <ul>
                        <li><strong>编号:</strong> ${applyData.apply.id?string('000000')!''}</li>
                        <li><strong>反馈日期:</strong> ${(applyData.apply.createDatetime?string("yyyy-MM-dd"))!''}</li>
                        <li style="width:300px;"><strong>反馈人:</strong> ${applyData.apply.accountName!''}(${prentGroupName!""}/${groupName!""})</li>
                        <li><strong>联系人:</strong>
                            <#if applyData.apply.teacherId?? && applyData.apply.teacherId?has_content>
                            <a href="/crm/teachernew/teacherdetail.vpage?teacherId=${applyData.apply.teacherId!''}">${applyData.apply.teacherName!''}</a>(<#if teacherMobile?? && teacherMobile?has_content>${teacherMobile!'该老师暂无手机号'}</#if>)
                            <#else>
                            ${applyData.apply.accountName!''}(<#if accountMobile?? && accountMobile?has_content>${accountMobile!''}</#if>)
                            </#if>
                        </li>
                    </ul>
                </div><br/>
                <#if applyData.apply.feedbackType == 'BOOK_CONTENT_ADJUSTMENT'>
                    <div>
                        <ul>
                            <li><strong>教材名称:</strong> ${applyData.apply.bookName!''}</li>
                            <li><strong>年级:</strong> ${applyData.apply.bookGrade!''}</li>
                            <li><strong>单元:</strong> ${applyData.apply.bookUnit!''}</li>
                        </ul>
                    </div><br/>
                </#if>
                <#if applyData.apply.feedbackType == 'ADD_BOOK'>
                    <div>
                        <ul>
                            <li><strong>教材名称:</strong> ${applyData.apply.bookName!''}</li>
                            <li><strong>年级:</strong> ${applyData.apply.bookGrade!''}</li>
                            <li><strong>覆盖地区:</strong> ${applyData.apply.bookCoveredArea!''}</li>
                            <li><strong>覆盖学生:</strong> ${applyData.apply.bookCoveredStudentCount!''}</li>
                        </ul>
                    </div><br/>
                </#if>
                <div style="margin-left:25px;margin-top:20px;clear:both;"><strong>反馈建议:</strong><span  style="font-family: '微软雅黑','Microsoft YaHei';text-indent:32px;letter-spacing: 1px;width:80%;line-height:26px;">${applyData.apply.content!''}</span> </div>
                <div style="margin-top:20px;margin-left:20px">
                    <strong>上传图片:(可点击图片查看大图)</strong><br/><br/>
                    <#if applyData.apply.pic1Url?has_content>
                        <div class="pic_show" style="width:100px;height:100px;float:left;margin-left:50px"><img src="${applyData.apply.pic1Url}" style="width:100%;height: 100%;"></div>
                    </#if>
                    <#if applyData.apply.pic2Url?has_content>
                        <div class="pic_show" style="width:100px;height:100px;float:left;margin-left:50px"><img src="${applyData.apply.pic2Url}" style="width:100%;height: 100%;"></div>
                    </#if>
                    <#if applyData.apply.pic3Url?has_content>
                        <div class="pic_show" style="width:100px;height:100px;float:left;margin-left:50px"><img src="${applyData.apply.pic3Url}" style="width:100%;height: 100%;"></div>
                    </#if>
                    <#if applyData.apply.pic4Url?has_content>
                        <div class="pic_show" style="width:100px;height:100px;float:left;margin-left:50px"><img src="${prePath}/gridfs/${applyData.apply.pic4Url}" style="width:100%;height: 100%;"></div>
                    </#if>
                    <#if applyData.apply.pic5Url?has_content>
                        <div class="pic_show" style="width:100px;height:100px;float:left;margin-left:50px"><img src="${prePath}/gridfs/${applyData.apply.pic5Url}" style="width:100%;height: 100%;"></div>
                    </#if>
                </div>
            </div>
        </#if>
