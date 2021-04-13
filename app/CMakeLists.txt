cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO")

add_library( push
             SHARED
             src/main/cpp/rtmp.cpp
             src/main/cpp/push.cpp
             src/main/cpp/Queue.cpp
             src/main/cpp/RtmpPush.cpp
             src/main/cpp/CallJava.cpp
             src/main/cpp/UdxTcpSink_P2p.cpp

             src/main/cpp/librtmp/amf.c
             src/main/cpp/librtmp/hashswf.c
             src/main/cpp/librtmp/log.c
             src/main/cpp/librtmp/parseurl.c
             src/main/cpp/librtmp/rtmp.c
              )
add_library(fudx SHARED IMPORTED)
set_target_properties(fudx
                      PROPERTIES IMPORTED_LOCATION
                      ${CMAKE_SOURCE_DIR}/src/main/libs/${CMAKE_ANDROID_ARCH_ABI}/libfudx.so)
target_link_libraries( push
                       fudx
                       log)