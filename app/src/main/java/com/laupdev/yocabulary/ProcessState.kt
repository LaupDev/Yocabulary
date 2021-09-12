package com.laupdev.yocabulary

enum class ProcessState {
    INACTIVE,
    PROCESSING,
    COMPLETED_ADDING,
    COMPLETED_UPDATE,
    FAILED,
    FAILED_WORD_EXISTS
}