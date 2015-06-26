#!/usr/bin/env bash

vercomp () {
    if [[ $1 == $2 ]]
    then
        return 0
    fi
    local IFS=.
    local i ver1=($1) ver2=($2)
    for ((i=${#ver1[@]}; i<${#ver2[@]}; i++))
    do
        ver1[i]=0
    done
    for ((i=0; i<${#ver1[@]}; i++))
    do
        if [[ -z ${ver2[i]} ]]
        then
            ver2[i]=0
        fi
        if ((10#${ver1[i]} > 10#${ver2[i]}))
        then
            return 1
        fi
        if ((10#${ver1[i]} < 10#${ver2[i]}))
        then
            return 2
        fi
    done
    return 0
}

possible_jdks=()
if [ -x "$JAVA_HOME/bin/java" ]; then
    jdks+=("$JAVA_HOME")
fi

jdk_roots=(~)
unamestr=`uname`
if [[ "$unamestr" == 'Linux' ]]; then
    jdk_roots+=( /usr/lib/jvm/ )
elif [[ "$unamestr" == 'Darwin' ]]; then
    jdk_roots+=( /Library/Java/JavaVirtualMachines/ )
fi

for jdk_root in "${jdk_roots[@]}" ; do
    for jdk in $(find "$jdk_root" -maxdepth 1 -type d -not -path "$jdk_root") ; do
        possible_jdks+=("$jdk")
    done
done

best_jdk=""
best_jdk_version=""
for possible_jdk in "${possible_jdks[@]}" ; do
    if [ -e "$possible_jdk/release" ] && [ -e "$possible_jdk/bin/java" ]; then
        jdk_version=$(egrep '^JAVA_VERSION=' "$possible_jdk/release" | egrep -o '"[^"]+"' | tr -d '"')
        if [[ $jdk_version != *"_"* ]] ; then
            jdk_version+=".0"
        fi
        jdk_version=$(echo "$jdk_version" | tr '_' '.')
        echo "$jdk_version"
        if [ -z "$best_jdk" ] ; then
            best_jdk="$possible_jdk"
            best_jdk_version="$jdk_version"
        else
            vercomp $jdk_version $best_jdk_version
            if [ "$?" == 1 ] ; then
                best_jdk="$possible_jdk"
                best_jdk_version="$jdk_version"
            fi
        fi
    fi
done

if [ -z "$best_jdk" ] ; then
    java="java"
else
    java="$best_jdk/bin/java"
fi

exec "$java" -jar "$0" "$@
