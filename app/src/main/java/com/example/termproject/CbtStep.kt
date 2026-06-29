package com.example.termproject

/**
 * CBT 치료 워크플로우의 각 단계를 정의하는 enum.
 */
enum class CbtStep {
    ModelDownload,  // 최초 실행 시 ML 모델 다운로드
    Splash,
    Login,
    SignUp,
    ProfileSetup,
    // Main Tabs
    Entry,
    Insight,
    Profile,

    Analysing,

    DistortionReview,



    // [Cognitive Path]
    SocraticAnalysing,
    SocraticCheck,
    AlternativeReframe,
    CategorySelect,
    // [Behavioral Path]
    BehavioralAnalysing,
    BehaviorRecommend,
    MapNavigation,
    TimerActive,
    ReflectionComplete,
    // [Exposure Path]
    ExposureActive,
    // Details
    ViewJournalDetail
}
