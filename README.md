# AI-Based CBT Companion App
"What troubles us is not situations themselves, but the way we think about them."

An Android application that delivers **Cognitive Behavioral Therapy (CBT)** through AI — combining cognitive restructuring, behavioral activation, and self-acceptance into a single mental health companion.

Built as a term project for Software Design & Experimentation (2026).

## Overview

CBT is one of the most clinically validated forms of psychotherapy. This app implements its three pillars:

Pillar                   Implementation 

**Cognitive**            Socratic dialogue engine for cognitive restructuring 
**Behavioral**           GPS-based activity recommendation via behavioral activation                            algorithm
**Self-acceptance**      Warm AI-driven feedback loop for reflective journaling 

## Features

- **On-device cognitive distortion analysis** — fine-tuned `xlm-roberta-base` model runs entirely on the device via a mobile-optimized inference engine; no server required
- **Socratic questioning** — Gemini API generates tailored follow-up questions to guide cognitive reframing
- **Behavioral activation** — GPS tracking + outdoor activity missions with Google Maps integration
- **Weather-aware recommendations** — OpenWeatherMap API used to suggest context-appropriate activities
- **AI result dashboard** — visualizes distortion analysis scores as 1–100% percentages with historical statistics
- **Multilingual support** — Korean/English switching tied to system language setting, applied instantly across all screens
- **Offline fallback** — local rule-based `fallbackAnalysisService` activates automatically when AI server is unreachable
- **Image journaling** — photo selection via `PickVisualMedia` with internal storage persistence



## Tech Stack

### Android / Frontend

Technology   ,          Role 

-Kotlin              :   Primary language 
-Jetpack Compose      :  Full UI/UX implementation 
-MVVM architecture     : Dependency separation, testability 
-Kotlin Coroutines      :Async processing (`Dispatchers.IO` for all I/O) 
-Jetpack    :            Navigation and list UI 
-Download Manager   :    AI model file management 
-rememberSaveable:     UI state preservation across configuration changes 

### Backend / Data

Technology      ,               Role 

* Firebase Realtime Database   :  Cloud data sync 
* Firebase Auth                :  Session management + UID-based data recovery 
* SharedPreferences + Gson     :  Local persistence 
* DataStore                     : Local recovery pipeline on app restart 

### APIs

API                           ,  Role 

* Gemini API                     : Socratic question generation 
* Google Maps SDK               : Activity map + GPS tracking 
* OpenWeatherMap API             : Weather-aware activity suggestions 
* Google Sign-In (OAuth 2.0)     : Authentication 

### Machine Learning

Component       ,           Detail 

* Base model         :       `xlm-roberta-base` 
* Task                 :      5-node multi-label cognitive distortion classification 
* Dataset               :     2,530 samples (Kaggle patient utterances; 50% English + 50% Korean translation) 
* Training               :    Epoch 4, Batch 16, LR 2e-5, Max Seq 128 
* Deployment              :   Mobile-optimized on-device inference; outputs 1–100% score per distortion type 


## Stability & Engineering Highlights

### ANR prevention
All heavy operations — PDF streaming, 10-page high-resolution bitmap decoding, GPS tracking — are isolated to `Dispatchers.IO`, achieving **0 ms UI hang time**.

### Memory management
- `viewModelScope` ties coroutine lifecycles to ViewModel to prevent memory leaks
- `clearPdfPages()` manually releases bitmap lists on PDF viewer teardown to prevent OOM crashes

### Fault tolerance
- External API failures (Gemini, OpenWeatherMap) trigger instant fallback to the local rule-based engine
- All file download and API calls wrapped in `try-catch` with user-facing error guidance

### Data loss prevention
- `rememberSaveable` preserves user input across screen rotation
- Firebase Auth session check on `MainActivity` entry rebuilds local state from DataStore if process was killed

### Runtime permissions
- `SecurityException` guard around all GPS/Maps code
- Permission-denied state surfaces a guided UI that deep-links to system settings

## Team
**김찬수** (Team lead)        
• 앱 전체 기획 및 Style guide 설계
• MVVM 앱 구조 설계(의존성 분리 및 다국어 지원)
• 데이터셋 구축 및 전처리
• xlm-roberta-base 모델 학습 및 최적화
• 인지 왜곡 분석 모델 모바일 경량화 온디바이스 추론 엔진 구현
• Gemini API 기반 소크라테스식 질문 생성 기능 구현
• 사용자 맞춤형 행동 활성화 알고리즘 및 GPS 추적 기능 구현
• diskIO/network/DB 예외 처리 및 메모리 최적화 관리
• 멀티 쓰레드 런타임 환경 테스트 및 크래시 디버깅 등 애플리케이션 안정성 고도화

**텟흐닌유웨이**         
• AI 분석 결과 시각화 및 통계 대시보드 구현
• Jetpack Compose 기반 전체 UI/UX 설계 및 구현
• Google Maps 연동 기능 구현
• Firebase Realtime Database 연동 및 데이터 관리 구현
• Kotlin Coroutine 기반 비동기 처리 구조 설계
• Download Manager 기반 AI 모델 관리 기능 구현
• OpenWeatherMap API 연동 구현
• PickVisualMedia 기반 이미지 선택 및 내부 저장소 영속화 구현
• 와이어프레임 및 화면 기능 / 득점 기능 수행 내용

