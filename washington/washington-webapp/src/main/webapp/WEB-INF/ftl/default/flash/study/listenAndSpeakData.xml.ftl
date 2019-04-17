<#import "/common/config.ftl" as app>
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns2:Lesson xmlns:ns2="http://jaxb.vlesson.vlt.com">
<!-- 跟读类数据-->
<#if lesson?exists>
    <classLevel>${classLevel!''}</classLevel>
    <gameTime>${gameTime!''}</gameTime>
    <lessonId>${lesson.id}</lessonId>
    <noisetest><@app.wave href="/wave/TestingWave/noisetest.wav" /></noisetest>
    <lessonName>${lesson.ename}</lessonName>
    <hasDialog>${lesson.dialog?string}</hasDialog>
    <load_swf><@app.link href="resources/apps/flash/swf/" /></load_swf >
    <farther_deal_with>/flash/study/fartherParse.vpage</farther_deal_with>
    <save_history>/flash/study/saveHistory.vpage</save_history>
    <load_configure>/flash/study/getPlayerConfig.vpage</load_configure>
    <save_configure>/flash/study/savePlayerConfig.vpage</save_configure>
    <query_rank>/flash/study/queryArenaRank.vpage</query_rank>
    <upload_data>/flash/study/uploadArenaData.vpage</upload_data>
    <query_challenge_rank>/flash/study/queryLessonRank.vpage</query_challenge_rank>
    <upload_challenge_sound>/flash/study/uploadLessonData.vpage</upload_challenge_sound>
    <upload_score>/flash/study/uploadLessonScore.vpage</upload_score>
    <getPhonetic>/flash/VoxPronounceStudy/getphonet.vpage</getPhonetic>
    <cdn_upload>/uploadfile/voice.vpage</cdn_upload>
    <#list sentences as sent>
        <sentence>
            <sentenceId>${sent.id}</sentenceId>
            <dialogRole>${sent.dialogRole}</dialogRole>
            <isWordFlag>false</isWordFlag>
            <!--晨读增加(单句练习)是否调用声音文件  wuyuefeng-->
            <isWordWave>${sent.type}</isWordWave>
            <Data>
                <language>CHN</language>
                <text>${(sent.cnText?default(''))?html}</text>
                <wavePlayTime>0</wavePlayTime>
                <slowWavePlayTime>0</slowWavePlayTime>
                <syllableNum>0</syllableNum>
            </Data>
            <Data>
                <language>ENG</language>
                <text>${sent.enText?html}</text>
                <VOICETEXT>${sent.voiceText}</VOICETEXT>
                <keyWord>${sent.keyWord?default('')}</keyWord>
                <waveFileLocator><@app.wave href="${sent.waveUri}"/></waveFileLocator>
                <wavePlayTime>${sent.wavePlayTime}</wavePlayTime>
                <slowWaveFileLocator><@app.wave href="${sent.slowWaveUri!''}"/></slowWaveFileLocator>
                <slowWavePlayTime>${sent.slowWavePlayTime!0}</slowWavePlayTime>
                <metadata>${sent.metadata!''}</metadata>
                <syllableNum>${sent.syllableNum!0}</syllableNum>
            </Data>
        </sentence>
    </#list>
<#else>
    <lessonId>null</lessonId>
</#if>
</ns2:Lesson>