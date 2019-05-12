cmake_minimum_required(VERSION 2.8)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -lcurl")

project(gogo-cli)
add_executable(${PROJECT_NAME} "main.cpp")
target_link_libraries(${PROJECT_NAME} curl)
