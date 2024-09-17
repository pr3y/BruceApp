cmake_minimum_required(VERSION 3.5)

project(Bruce_app LANGUAGES CXX)

find_package(Qt6 REQUIRED COMPONENTS Widgets)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_executable(Bruce_app main.cpp)

target_link_libraries(Bruce_app PRIVATE Qt6::Widgets)

