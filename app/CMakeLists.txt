cmake_minimum_required(VERSION 3.10.2)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO")

include_directories("${CMAKE_SOURCE_DIR}/src/main/cpp/include")
include_directories("${CMAKE_SOURCE_DIR}/src/main/cpp")

#add_library(fudx SHARED IMPORTED)
#set_target_properties(fudx
#                      PROPERTIES IMPORTED_LOCATION
#                      ${CMAKE_SOURCE_DIR}/src/main/libs/${CMAKE_ANDROID_ARCH_ABI}/libfudx.so)

add_library (fudx SHARED IMPORTED)
set_target_properties(fudx PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/src/main/libs/${CMAKE_ANDROID_ARCH_ABI}/libfudx.so)

link_directories(libs/${CMAKE_ANDROID_ARCH_ABI})

add_library(
        MFastUdx
        SHARED
        src/main/cpp/rtmp.cpp
        src/main/cpp/CallJava.cpp
        src/main/cpp/UdxTcpSink_P2p.cpp
        )

target_link_libraries( MFastUdx
                       log)
target_link_libraries(MFastUdx fudx)