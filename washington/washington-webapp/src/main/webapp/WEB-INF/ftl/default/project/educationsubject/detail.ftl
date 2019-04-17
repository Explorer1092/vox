<#import "../../layout/project.module.ftl" as temp />
<#--teacher/reward/mobilerecharge.vpage-->
<@temp.page title="教育部课题">
<@app.css href="public/skin/project/educationsubject/skin.css?1.0.2" />
<div class="subject-main">
    <div class="subject-conHead-banner">
        <#--<div class="subject-inner">
            <div class="subject-wj-logo"><a href="/project/educationsubject/index.vpage"></a></div>
        </div>-->
    </div>
    <div class="subject-topic-content">
        <div class="subject-inner">
            <div class="st-topic-font">
                <h2>一、课题定位和设想</h2>
                <div class="tf">
                    <strong>本课题的定位：</strong>探索将智能感知技术应用到中小学作业减负中，并依托具有大规模网上中小学生用户的“一起作业网”开展实践探索。本课题以个性化学习理论为依据，利用云计算和大数据背景下的智能感知技术，从知识水平和情感状态两个维度对学生完成作业过程中的学习活动进行感知，自适应地调整每个学生的作业内容和作业数量，构建一个能够帮助学生轻松愉快地完成家庭作业的个性化作业平台，提高中小学生的“作业质量”与“学习效果”，进而促进“减负增效”，促进学生知识和情感的同步发展。
                </div>
                <p>有关作业和作业系统的研究发展现状表明：作业的数量和难度需要和学生的知识结构、知识水平和学习情感相适应，个性化的作业是提高作业效率、实现“减负增效”的关键，而这一特点在现有的作业系统中并没有得到很好的体现。</p>
                <p>为了获得愉悦的作业体验和满意的作业效果，监测和调控作业中学生的学习情感是一种行之有效的方法。</p>
                <p>个性化作业内容设计的依据是学生对知识掌握的情况，这需要我们能够感知和测量学生的知识结构和水平。建立一个这样的测量系统，其指导理论主要有两种：经典测量理论（CTT，ClassicalTest Theory）和项目反应理论（IRT，Item Response Theory）。经典测量理论依据的是线性数学模型，该模型在样本依赖、分数等值等方面存在不足。项目反应理论采用了非线性的概率模型，克服了经典测量理论的上述缺陷。</p>
                <p>与经典测量理论不同，项目反应理论不是用平均分、标准差、通过率等统计量来刻画测验结果，而是通过题目特征曲线的特征参数来刻画一个测验。IRT基于潜在特质假设，即假设被试对于测量的反应是受某种心理特质的支配，首先我们要对这种特质进行界定，然后估计出被试的这种特质的分数，并根据该分数的高低来预测和解释被试对于项目或测验的反应。项目反应理论的最大优点是项目参数的估计与被测试的样本无关。它是以受测者回答问题的情况，经题目特征函数的运算，推测受测者的认知水平和能力。</p>
                <p>
                    目前，IRT的理论模型已经较为完善，研究多偏向于IRT在能力测量中的数据处理技术和应用模式。CAT（Computerized Adaptive Testing）是IRT应用的一个重要领域，CAT是一种量裁性（tailoring）的测量，它根据每个学生的实际能力定制测验内容，首先通过初始题目对学生能力进行初步估计，然后通过满足当前知识点测量要求的选题算法，从题库中选取与学生能力水平最接近的题目继续进行测量，直到能够准确地标定学生在当前所测知识技能方面的能力值为止。
                </p>
                <p>如果我们在学生完成作业过程中，融入这种对学生知识结构和水平的自动测量和感知技术，就能够做到为每一个学生定制适合他自己的个性化作业。基于IRT的自适应测量系统可以为我们定制每个学生的作业内容和作业数量提供依据，为构建一个能够自动提供个性化的作业内容设计、自动确定合理的作业数量的在线作业平台提供一种有效方法。</p>
            </div>
            <div class="st-topic-font">
                <h2>二、选题的价值和意义</h2>
                <p>从学习理论来看，学生学习知识过程可分为：选择、领会、维持、应用四个阶段。学生在领会知识之后，要维持和应用知识，作业是一种有效途径。从教学角度来看，作业不仅是教学过程中对教学效果的必要反馈形式，而且设计作业是课堂教学的延续与发展。学生在作业过程中受到学习目标、周围学习环境等多方面影响、呈现出多样化特性。不同的学生对学习内容的掌握快慢、扎实程度受到学生性格特征、学习方法、情感状态等多方面影响。</p>
                <p>本课题的选题意义在于探索在云计算和大数据背景下，利用智能感知技术来实现中小学生作业“减负增效”的关键技术方案。课题将基于云计算和大数据的学习分析技术应用到学生完成作业的学习活动中，在作业过程中智能感知学生个体表现出来的知识结构、水平及情感状态，自适应地调整每个学生的作业内容和数量，实现个性化的作业推送。本课题研究成果的应用推广将为缓解当前中小学生作业负担繁重，作业效率偏低等问题提供一种新的实践探索。</p>
            </div>
            <div class="st-topic-font">
                <h2>三、主要研究内容、思路方法及创新点</h2>
                <h3>1. 研究目标</h3>
                <div class="tf">
                    <strong>本课题研究目标</strong>以个性化学习理论为依据，以云计算和大数据背景下的智能感知技术为手段，动态感知学生在完成作业过程中的知识结构、水平及情感状态，构建一个能自适应地调整每个学生的作业内容、数量及频率的个性化在线作业云计算平台，改善中小学生的“作业质量”与“学习效果”，促进“减负增效”。
                </div>
                <h3>2. 主要研究内容</h3>
                <div class="tf"><strong>本课题的核心任务：</strong>研究基于智能感知技术的中小学生个性化在线作业云计算平台</div>
                <div class="tf">1、中小学生个性化在线作业云计算平台中的智能感知关键技术研究</div>
                <div class="tf">学生知识及情感的感知和调控模型，涉及以下关键技术：</div>
                <p> ● 智能感知技术：学生知识结构感知、学生学习情感状态感知</p>
                <p> ● 学习分析技术：学生知识水平分析、学生学习情感转换分析</p>
                <p> ● 社会化交互技术：与学生进行社会性交互（包括社交化交互与游戏化交互教学设计）</p>
                <div class="tf">2、“基于智能感知技术的中小学生个性化在线作业云计算平台”的实验系统开发</div>
                <p>实验系统将以“一起作业网”为依托，将智能感知技术集成到一起作业网现有的在线作业平台中。“一起作业网”是一个致力于为全国中小学生提供基于互联网的在线作业练习和能力提升，为教师、学生和家长提供有价值的个性化互动教学服务的网络平台。实验系统以学生为主体，以海量题库为支撑，融合个性化、社交化等交互手段，通过对学生作业过程中的知识结构与知识水平的测量和分析，进行个性化作业定制和推荐，避免题海战术；通过对学生在作业过程中的情感状态变化进行感知，动态嵌入社会化及游戏化交互，提高学生完成作业的情感体验与学习兴趣。</p>
                <h3>3. 基本观点</h3>
                <div class="tf">
                    <strong>本课题研究目标</strong>以个性化学习理论为依据，以云计算和大数据背景下的智能感知技术为手段，动态感知学生在完成作业过程中的知识结构、水平及情感状态，构建一个能自适应地调整每个学生的作业内容、数量及频率的个性化在线作业云计算平台，改善中小学生的“作业质量”与“学习效果”，促进“减负增效”。
                </div>
                <h3>1. 研究目标</h3>
                <p>本研究的基本观点：将以智能感知和交互为核心的IT新技术应用到中小学在线作业系统中，对学生的知识结构、知识水平及学习情感进行感知分析，形成智能化和个性化的在线作业平台，可以有效帮助中小学生实现作业的“减负增效”。</p>
                <h3>4. 研究思路</h3>
                <p>本研究从知识结构及水平、学习情感两大维度，采用IRT感知计算、基于视觉及文本的学习情感感知计算技术，对学生在线作业过程中的学习及情感状态进行感知分析，自适应生成难度、数量及频率与学习状态相适应的个性化作业，在社会化与游戏化情境中形成与学生学习状态相适应的教学交互。如下图所示：</p>
                <p class="pl"><img src="<@app.link href="public/skin/project/educationsubject/clazz-item.jpg"/>" alt="" width="913" height="404"/></p>
                <h3>5. 研究方法和技术</h3>
                <p>课题采用了文献调研、数据建模、统计分析、系统分析与设计(软件)、实践验证等研究方法。</p>
                <p>“基于智能感知技术的中小学生个性化在线作业云计算平台”的关键技术研究涵盖三个方面：（1）智能感知学生在作业过程中的知识及情感状态；（2）基于知识结构与水平的分析及情感状态分析，自适应生成难度、数量与学习状态相匹配的个性化作业；（3）在社会化、游戏化作业情境中建立个性化教学交互。</p>
                <h3>6. 创新之处</h3>
                <p>知识和情感是学生发展的两个重要方面，本研究从学生知识结构及水平、学习情感的智能感知出发，自动构建个性化的在线作业模式，减轻学生的作业负担，促进学习效率，使学生在游戏化、社交化的个性化作业系统中始终保持学习的热情与愉悦感，引导学生知识和情感同步发展。本研究具有如下三方面的创新：</p>
                <div class="tf">
                    1、构建“基于知识维度感知的个性化作业内容引擎”
                </div>
                <p>应用面向知识维度的感知技术实现个性化作业中的内容自适应，将基于IRT的智能感知技术内置到学生作业内容、作业数量的自动定制和推送过程中，形成适应于学生知识掌握程度的个性化作业策略，降低学生的作业负担，提高作业的效率。</p>
                <div class="tf">2、 构建“基于情感维度感知的个性化作业交互引擎”</div>
                <p>应用面向情感维度的感知技术实现作业过程中的学习情感调节，引进情感状态的感知分析，及时跟踪学生在完成作业过程中的情感状态的转化，根据学生的情感状态及时与学生进行社会化交互、游戏化交互，调整学生作业的节奏，保持学生在完成作业过程中的热情与愉悦度。</p>
                <div class="tf">3、 构建“基于智能感知技术的中小学生个性化在线作业云计算平台”</div>
                <p>将个性化作业系统的内容引擎和交互引擎引入到在线作业平台中，研制开发“基于智能感知技术的中小学生个性化在线作业云计算平台”，为学生、教师、家长建立一个以在线作业为核心内容的学习、互动和交流的服务平台。</p>
                <div class="tf">4、 完成本课题研究的时间保证、资料设备等科研条件</div>
                <p>本课题组具有一支由高校教师、企业专家组成的研究队伍，具备实力强大的核心技术研究能力和软件开发能力、条件良好的高校研究环境和科研实验室。学校图书馆的数字资源提供了Nature、IEEE/IET            Electronic Library、ACM Digital Library、SpringerLink、Elsevier等国际顶级全文数据库；学校高性能科学计算机中心拥有Altix 3700Bx2 高性能计算服务器（128 颗Intel Itanium-2 CPU，256G内存）、TP9500 磁盘阵列存储（容量20T）、2 套DELL 计算集群等丰富的计算资源，可以为大数据分析、数据挖掘提供计算平台。</p>
                <p>本课题的合作企业上海合煦信息科技有限公司开发运营的“一起作业网”为课题研究提供了良好的中小学生用户基础和实践探索条件。一起作业的技术团队来自各大知名互联网和教育公司，既有多年从事互联网技术研究和服务的专家，也有在传统软件行业拼搏成长起来的青年俊杰，实现了老中青结合，跨学科专业人才结合和多年教育背景和互联网技术研究经验的结合。项目内部采用敏捷项目管理实践，以迭代的、增量的交付用户价值，最大限度地保证产品朝着符合教育互联网方向发展快速发展。目前系统架构能承受百万学生在线做作业。</p>
                <p>一起作业的移动端提供了全平台的支撑，目前已有多款产品上线运营。老师端可以用微信和手机应用进行布置作业检查作业的基本功能，学生端即将推出。</p>
                <p>大数据团队是一起作业技术团队的重要组成，既有来自百度、阿里等一流大公司的具有多年工作经验的人才，也有国外留学归来具有深度的教育模型背景的精英，对中国学生的知识能力、学习风格、性格和素质作出科学评测，利用大数据、机器学习和教育模型跨学科的技术魔力，为每一位学生提供个性化的、针对性的指导，使因材施教成为可能。同时也为政府决策提供支持。</p>
            </div>
        </div>
    </div>
</div>
</@temp.page>