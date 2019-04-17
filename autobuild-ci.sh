#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DIR=`readlink -f $DIR`
cd $DIR

stage=$1
stage_mode=`echo "$stage" | cut -d '-' -f 1`
stage_codename=`echo "$stage" | cut -d '-' -f 2`
output_dirname="build-$stage"

if [[ ! ( "test staging production" =~ (^| )$stage_mode($| ) ) ]]; then
    echo "stage: test staging production"
    exit -1
fi



notify_status() {
    local appmod=$1
    local buildver=$2
    local status=$3

    local stage=$stage
    local host_name=`hostname`
    local app_name=$appmod
    local app_type=autobuild
    local app_instance="$stage:$app_name:$host_name"
    local app_base_dir=$DIR
    local app_build_version=$buildver
    local app_datetime=`date +"%Y-%m-%d%%20%H:%M:%S"`
    local app_uptime=0
    local app_status=$status
    local host_network_ip=""


    local notify_url="http://deployer.office.17zuoye.net/collector/app.php"
    if [ "$stage_mode" = "production" -o "$stage_mode" = "staging" ]; then
        notify_url="http://deployer.dc.17zuoye.net/collector/app.php"
    fi

    local url="$notify_url?"
    url="$url&stage=$stage"
    url="$url&app_name=$app_name"
    url="$url&app_instance=$app_instance"
    url="$url&app_type=$app_type"
    url="$url&app_base_dir=$app_base_dir"
    url="$url&app_build_version=$app_build_version"
    url="$url&app_datetime=$app_datetime"
    url="$url&app_uptime=$app_uptime"
    url="$url&app_status=$app_status"
    url="$url&host_name=$host_name"
    #url="$url&host_network_ip=$host_network_ip"

    echo "send notify: $url" >>$logfile
    curl -s $url >>$logfile
    echo "" >>$logfile
}

last_buildid=`cat ./build-repo/$output_dirname/000-$output_dirname.txt`

while true ; do

    logfile="/tmp/autobuild-ci-`date +%Y%m%d`.$stage.log"

    notify_status "autobuild-ci-hg" "$last_buildid" "pulling-updating"
    git pull 1>>$logfile 2>>$logfile

    buildid=`git log --max-count=1 --pretty=format:%ad-%H --date=format-local:%Y%m%d-%H%M%S`

    notify_status "autobuild-ci-hg" "$buildid" "pull-update-done"

    if [ "$buildid" = "$last_buildid" ]; then
        echo "skip $last_buildid, already built" >>$logfile
        sleep 10
    else
        echo "current $last_buildid, build $buildid" >>$logfile

        notify_status "autobuild-ci" "$buildid" "building"
        if ./autobuild.sh $stage 1>>$logfile 2>>$logfile; then

            notify_status "autobuild-ci" $buildid "build-done"

            echo "build $buildid successfully" >>$logfile

            last_buildid="$buildid"

        else

            notify_status "autobuild-ci" $buildid "build-failed"

            echo "build $buildid failed" >>$logfile

            source ./autobuild-cfg.sh
            tail -n 300 $logfile | mail -r ci-noreply@17zuoye.com -s "autobuild failed: $stage $buildid" $ERROR_EMAILS

            # when failed, skip this build
            last_buildid="$buildid"
        fi
    fi
done
