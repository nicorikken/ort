FROM osadl/alpine-docker-base-image:v3.16-amd64-220919

COPY . /usr/local/src/ort

WORKDIR /usr/local/src/ort

# Gradle build.
ARG ORT_VERSION

RUN apk add --no-cache \
    bash

#RUN --mount=type=cache,target=/tmp/.gradle/ \
#    export GRADLE_USER_HOME=/tmp/.gradle/ && \
RUN  ls scripts &&  scripts/import_proxy_certs.sh && \
    scripts/set_gradle_proxy.sh && \
    ./gradlew --no-daemon --stacktrace -Pversion=$ORT_VERSION :cli:distTar :helper-cli:startScripts
