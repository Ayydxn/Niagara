cmake_minimum_required(VERSION 4.2)
project(NiagaraJNIBridge)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ── Compiler hardening & optimisation flags ──────────────────────────────────
if(MSVC)
    add_compile_options(/O2 /GL /W4)
    add_link_options(/LTCG)
else()
    add_compile_options(-O3 -flto -Wall)
    add_link_options(-flto)
endif()

# ── Locate JNI ───────────────────────────────────────────────────────────────
find_package(JNI REQUIRED)

# ── Paths passed in from Gradle ──────────────────────────────────────────────
# ULTRALIGHT_SDK         — root of the user's Ultralight SDK
# NIAGARA_GENERATED_DIR  — where the generator wrote the C++ glue

set(UL_INCLUDE "${ULTRALIGHT_SDK}/include")
set(UL_LIB_DIR "${ULTRALIGHT_SDK}/lib")

# ── Sources ──────────────────────────────────────────────────────────────────
file(GLOB_RECURSE SOURCES "src/*.cpp" "src/*.h")
file(GLOB_RECURSE GENERATED_SOURCES "${NIAGARA_GENERATED_DIR}/*.cpp" "${NIAGARA_GENERATED_DIR}/*.h")

if(NOT GENERATED_SOURCES)
    message(FATAL_ERROR "No generated sources found in ${NIAGARA_GENERATED_DIR}")
endif()

message(STATUS "Generated sources: ${GENERATED_SOURCES}")
list(APPEND SOURCES ${GENERATED_SOURCES})

add_library(NiagaraJNIBridge SHARED ${SOURCES})

target_include_directories(NiagaraJNIBridge PRIVATE
        "${CMAKE_SOURCE_DIR}/src"
        "${NIAGARA_GENERATED_DIR}"
        ${JNI_INCLUDE_DIRS}
        ${UL_INCLUDE}
)

# Link against Ultralight (user-provided)
target_link_directories(NiagaraJNIBridge PRIVATE ${UL_LIB_DIR})
target_link_libraries(NiagaraJNIBridge PRIVATE
        Ultralight
        UltralightCore
        WebCore
)

target_compile_definitions(NiagaraJNIBridge PRIVATE
        _SILENCE_CXX17_CODECVT_HEADER_DEPRECATION_WARNING
)

# Export only JNI symbols, keep everything else hidden
set_target_properties(NiagaraJNIBridge PROPERTIES
        CXX_VISIBILITY_PRESET hidden
        VISIBILITY_INLINES_HIDDEN ON
)
