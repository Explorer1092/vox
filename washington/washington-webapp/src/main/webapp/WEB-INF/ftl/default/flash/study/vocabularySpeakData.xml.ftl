<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns2:Lesson xmlns:ns2="http://jaxb.vlesson.vlt.com">
<#if lesson?exists>
    <gameTime>${gameTime!'1'}</gameTime>
    <lessonId>${lesson.id}</lessonId>
    <lessonName>${lesson.ename?html}</lessonName>
    <hasDialog>${lesson.dialog?string}</hasDialog>
    <load_swf><@app.link href="resources/apps/flash/swf/" /></load_swf >
    <load_lesson_data>/flash/study/loadLesson.vpage</load_lesson_data>
    <farther_deal_with>/flash/study/fartherParse.vpage</farther_deal_with>
    <save_history>/flash/study/saveHistory.vpage</save_history>
    <load_configure>/flash/study/getPlayerConfig.vpage</load_configure>
    <word_configure>/flash/study/wordPassRate.vpage</word_configure>
    <syllable_configure>/flash/VoxPronounceStudy/getphonet.vpage</syllable_configure>
    <save_configure>/flash/study/savePlayerConfig.vpage</save_configure>
    <query_rank>/flash/study/queryArenaRank.vpage</query_rank>
    <upload_data>/flash/study/uploadArenaData.vpage</upload_data>
    <query_challenge_rank>/flash/study/queryLessonRank.vpage</query_challenge_rank>
    <upload_challenge_sound>/flash/study/uploadLessonData.vpage</upload_challenge_sound>
    <#list sentences as sent>
        <sentence>
            <sentenceId>${sent.id}</sentenceId>
            <passRate>${sent.passRate!1}</passRate>
            <dialogRole>${sent.dialogRole}</dialogRole>
            <isWordFlag>false</isWordFlag>
            <startWave>1<@app.cdn href="/wave/Good_morning_china/startFollow.wav" /></startWave>
            <repeatWave>1<@app.cdn href="/wave/Good_morning_china/startRepeat.wav" /></repeatWave>
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
                <waveFileLocator><@app.wave href="${sent.waveUri}" /></waveFileLocator>
                <wavePlayTime>${sent.wavePlayTime}</wavePlayTime>
                <slowWaveFileLocator><@app.wave href="${sent.slowWaveUri!''}" /></slowWaveFileLocator>
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