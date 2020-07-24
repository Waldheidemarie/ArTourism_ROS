#CMake版本信息
cmake_minimum_required(VERSION 3.4.1)

#加载fin engine lib
add_library(data-transfer-lib
    SHARED
    main.cpp
    native-lib.cpp
    UnityTransfer.cpp
)
target_link_libraries(data-transfer-lib
    log
    android
)