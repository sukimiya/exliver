cmake_minimum_required(VERSION 3.10.2)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO")

include_directories("${CMAKE_SOURCE_DIR}/src/main/cpp/include")

#add_library(fudx SHARED IMPORTED)
#set_target_properties(fudx
#                      PROPERTIES IMPORTED_LOCATION
#                      ${CMAKE_SOURCE_DIR}/src/main/libs/${CMAKE_ANDROID_ARCH_ABI}/libfudx.so)

add_library (FastUdx SHARED IMPORTED)
set_target_properties(FastUdx PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/libs/${CMAKE_ANDROID_ARCH_ABI}/libfudx.so)

add_library(
        SHARED
        src/main/cpp/rtmp.cpp
        src/main/cpp/CallJava.cpp
        src/main/cpp/UdxTcpSink_P2p.cpp
        )

target_link_libraries( FastUdx
                       android
                       log)