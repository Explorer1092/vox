<#import "../../layout/project.module.ftl" as temp />
<#--teacher/reward/mobilerecharge.vpage-->
<@temp.page title="教育部课题">
<@app.css href="public/skin/project/educationsubject/skin.css" />
<div class="subject-main">
    <div class="subject-head-banner">
        <div class="subject-inner">
            <#--<div class="subject-wj-logo"><a href="/project/educationsubject/index.vpage"></a></div>-->
            <a class="apply-now-btn" href="javascript:void (0);" data-menu-type="4" data-title="立即申请"></a>
        </div>
    </div>
    <div style="height: 70px;">
        <div class="subject-navBar-list">
            <ul>
                <li data-menu-type="1" class="active"><a href="javascript:void (0);">课题介绍</a></li>
                <#--<li data-menu-type="2"><a href="javascript:void (0);">课题专家</a></li>-->
                <li data-menu-type="3"><a href="javascript:void (0);" >申请条件</a></li>
                <li data-menu-type="4"><a href="javascript:void (0);">申请流程</a></li>
            <#--<li><a href="javascript:void (0);">课题动态</a></li>-->
            </ul>
        </div>
    </div>

    <div class="subject-introduction-box"  data-current-loaction="1">
        <div class="subject-inner">
            <div class="sub-in-box">
                <dl>
                    <dt></dt>
                    <dd>
                        <div class="sub-font">
                            <p class="c-title">课题内容</p>
                            <p style="text-indent: 32px;">
                                以个性化学习理论为依据，以云计算和大数据背景下的智能感知技术为手段，动态感知学生在完成作业过程中的知识结构、水平及情感状态，构建一个能自适应地调整每个学生的作业内容、数量及频率的个性化在线作业云计算平台，改善中小学生的“作业质量”与“学习效果”，促进“减负增效”。<a class="more-btn" href="/project/educationsubject/detail.vpage" target="_blank">查看详细内容</a>
                            </p>
                            <#--<p class="about"><a href="<@app.link href="public/skin/project/educationsubject/noticeofapproval.pdf"/>" target="_blank" style="color: #666;">* 课题详情请参阅《关于开展“智能感知技术在中小学作业减负中的应用研究与实践探索 ”立项通知 》</a></p>-->
                        </div>
                    </dd>
                    <#--<dd class="pr">
                        <div class="sub-font">
                            <p class="c-title">课题管理办公室</p>
                            <p>主任：孙波</p>
                            <p>副主任：张兴慧</p>
                            <p>秘书长：成宏</p>
                            <p>常务秘书长：魏云刚</p>
                        </div>
                    </dd>-->
                </dl>
                <div class="w-clear"></div>
            </div>
        </div>
    </div>
    <#--
    <div class="subject-personal-box"  data-current-loaction="2">
        <div class="subject-inner">
            <div class="slide-info-box"></div>
            <div class="sub-person">
                <dl>
                    <dt>
                        <img width="140" height="160" src="<@app.link href="public/skin/project/educationsubject/person/person-01.jpg?1.0.1"/>">
                    </dt>
                    <dd>
                        <p class="title">孙波</p>
                        <p class="more">教授、副院长、博士生导师</p>
                        <p>主要从事计算机教育应用、智能计算方面的研究及开发工作。本项目中，负责整体项目，把握项目研究方向。</p>
                    </dd>
                </dl>
                <dl>
                    <dt>
                        <img width="140" height="160" src="<@app.link href="public/skin/project/educationsubject/person/person-02.jpg?1.0.1"/>">
                    </dt>
                    <dd>
                        <p class="title">胡晓雁 </p>
                        <p class="more">副教授，理学博士</p>
                        <p>本项目中，研究交互行为选择模型触发适当交互辅助学习者改善学习情绪，促进学习者保持较好情绪，高效地学习。</p>
                    </dd>
                </dl>
                <dl>
                    <dt>
                        <img width="140" height="160" src="<@app.link href="public/skin/project/educationsubject/person/person-03.jpg?1.0.1"/>">
                    </dt>
                    <dd>
                        <p class="title">何珺 </p>
                        <p class="more">副教授，物理电子学博士</p>
                        <p>本项目中，研究、检测、识别学生在作业过程中的情绪变化，为“交互引擎”提供相关参数。</p>
                    </dd>
                </dl>
                <dl>
                    <dt>
                        <img width="140" height="160" src="<@app.link href="public/skin/project/educationsubject/person/person-04.jpg"/>">
                    </dt>
                    <dd>
                        <p class="title">肖永康 </p>
                        <p class="more">工学博士，副教授，硕士生导师</p>
                        <p>本项目中，基于IRT理论的计算机自适应考试部分的关键算法，进行内容感知模块的的研究工作。</p>
                    </dd>
                </dl>
                <dl>
                    <dt>
                        <img width="140" height="160" src="<@app.link href="public/skin/project/educationsubject/person/person-05.jpg"/>">
                    </dt>
                    <dd>
                        <p class="title">李连华 </p>
                        <p class="more">一起作业大数据负责人</p>
                        <p>本项目中，负责推动智能感知引擎的产业化，提供大数据和系统支持，并在内容感知和智能推荐上进行合作研究。</p>
                    </dd>
                </dl>
                <dl>
                    <dt>
                        <img width="140" height="160" src="<@app.link href="public/skin/project/educationsubject/person/person-06.jpg"/>">
                    </dt>
                    <dd>
                        <p class="title">肖融 </p>
                        <p class="more">硕士，参与教育部重点课题</p>
                        <p>在本项目中，主要参与内容引擎方面的工作，根据学生能力自适应调整学生的作业内容，帮助他们提高学习效率。</p>
                    </dd>
                </dl>
            </div>
        </div>
    </div>
    -->

    <div class="subject-introduction-box subject-before-box"  data-current-loaction="3">
        <div class="subject-inner">
            <div class="sub-in-box">
                <dl>
                    <dt></dt>
                    <dd>
                        <div class="sub-font">
                            <div class="sf">
                                <p class="pf">子课题申请</p>
                                <p class="pfe">一、子课题申请主题及内容</p>
                                <p>1、建设基于IRT的计算机自适应考试题库，包括题干、选项、正确答案，知识点，各知识点的区分度、难度和猜测系数等内容</p>
                                <p>2、基于学生作业视频的情绪标注</p>
                                <p>3、其他与作业减负相关的课题主题</p>
                            </div>
                        </div>
                        <div class="sub-font">
                            <div class="sf">
                                <p class="pfe">二、子课题申请单位</p>
                                <p>1、各地市教育系统信息中心等行政单位</p>
                                <p>2、各地市教研室或教科院</p>
                                <p>3、各地市中、小学校</p>
                            </div>
                        </div>
                        <div class="sub-font">
                            <div class="sf">
                                <p class="pf">课题实验基地申请条件</p>
                                <p>1、组织学生参加基于IRT的计算机自适应考试，建立考试数据库</p>
                                <p>2、与课题组一起组织实施课题研究过程中成果验证和应用</p>
                                <p>3、全校使用一起作业平台的学生数量达到学校总学生数量的70%以上，学生连续使用三个月以上，每个月至少使用平台完成4次作业</p>
                            </div>
                        </div>
                    </dd>
                </dl>
            </div>
        </div>
    </div>
    <div class="subject-application-process" data-current-loaction="4">
        <div class="subject-inner">
            <a class="download-clazz-btn" href="<@app.link href="public/skin/project/educationsubject/subapplicationform.doc"/>" target="_blank"></a>
            <a class="download-test-btn" href="<@app.link href="public/skin/project/educationsubject/experimentapplicationform.doc"/>" target="_blank"></a>
        </div>
    </div>
    <#--<div class="subject-introduction-box subject-before-box subject-active-box">
        <div class="subject-inner">
            <div class="sub-in-box">
                <dl>
                    <dt></dt>
                    <dd>
                        <div class="new-switch-box">
                            <div class="new-switch-content">
                                <div class="ns-box">
                                    <a href="#"><img src="images/test.jpg" width="660" height="420" /></a>
                                    <div class="cz-title">
                                        <h4 class="">标题标题标题</h4>
                                        <p>标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题</p>
                                    </div>
                                </div>
                                <div class="ns-box">
                                    <a href="#"><img src="images/test.jpg" width="660" height="420"/></a>
                                    <div class="cz-title">
                                        <h4 class="">标题标题标题</h4>
                                        <p>标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题</p>
                                    </div>
                                </div>
                                <div class="ns-box">
                                    <a href="#"><img src="images/test.jpg" width="660" height="420"/></a>
                                    <div class="cz-title">
                                        <h4 class="">标题标题标题</h4>
                                        <p>标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题</p>
                                    </div>
                                </div>
                                <div class="ns-box">
                                    <a href="#"><img src="images/test.jpg" width="660" height="420"/></a>
                                    <div class="cz-title">
                                        <h4 class="">标题标题标题</h4>
                                        <p>标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题标题</p>
                                    </div>
                                </div>
                            </div>
                            <ul class="new-switch-list">
                                <li class="active"><a href="#"><img src="images/new-small.png" alt=""/></a></li>
                                <li><a href="#" ><img src="images/new-small.png" alt=""/></a></li>
                                <li><a href="#" ><img src="images/new-small.png" alt=""/></a></li>
                                <li><a href="#" ><img src="images/new-small.png" alt=""/></a></li>
                            </ul>
                        </div>
                    </dd>
                </dl>
            </div>
        </div>
    </div>-->
</div>
<script type="text/javascript">
    $(function(){
        $("[data-menu-type]").on("click", function(){
            var $this = $(this);
            var dataVal = $this.attr("data-menu-type");

            $("[data-menu-type='"+ dataVal +"']").addClass("active").siblings().removeClass("active");

            $("html, body").animate({ scrollTop: $("[data-current-loaction='" + dataVal + "']").offset().top - 50 }, 200);
        });

        $(window).scroll(function(){
            var $currentOffsetTop = $("html").scrollTop() != 0 ? $("html").scrollTop() : $("body").scrollTop();
            if( $currentOffsetTop > 458){
                $(".subject-navBar-list").addClass("subject-navBar-list-popup");
            }else{
                $(".subject-navBar-list").removeClass("subject-navBar-list-popup");
            }
        });
    });
</script>
</@temp.page>