cmake_minimum_required(VERSION 3.22.1)
project("piper_engine")

add_library(
        piper_engine
        SHARED
        piper_engine.cpp)

find_library(
        log-lib
        log)

target_link_libraries(
        piper_engine
        ${log-lib})
