#!/usr/bin/env bash

if [ -d lib ]; then
    rm -v lib/*.jar
fi

./mvnw clean package
./mvnw dependency:copy-dependencies -DoutputDirectory=lib

# ${GRAAL_PATH}/bin/native-image \
#     -cp target/camel-routes-loader-graalvm-2.23.0-SNAPSHOT.jar \
#     -cp lib/camel-core-2.23.0-SNAPSHOT.jar \
#     -cp lib/asm-6.0.jar \
#     -cp lib/asm-analysis-6.0.jar \
#     -cp lib/asm-commons-6.0.jar \
#     -cp lib/asm-tree-6.0.jar \
#     -cp lib/jaxb-core-2.3.0.jar \
#     -cp lib/jaxb-impl-2.3.0.jar \
#     -cp lib/slf4j-api-1.7.25.jar \
#     -cp lib/slf4j-simple-1.7.25.jar \
#     -cp lib/xbean-asm-util-4.6.jar \
#     -cp lib/xbean-bundleutils-4.6.jar \
#     -cp lib/xbean-finder-4.6.jar \
#     --verbose \
#     --language:js \
#     -H:+JNI \
#     -H:ReflectionConfigurationFiles=./src/graalvm/reflectionconfig.json \
#     -H:IncludeResources=META-INF/.* \
#     -H:Features=org.apache.camel.graalvm.support.CamelFeature \
#     -DCamelSimpleLRUCacheFactory=true \
#     org.apache.camel.graalvm.Main
