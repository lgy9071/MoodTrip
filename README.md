# 🌏 MoodTrip – 취향 기반 여행·장소·리뷰 플랫폼

> “내 취향에 맞는 장소·여행·맛집을 AI가 추천해주는 다국어 여행 플랫폼”

MoodTrip은 사용자의 취향 데이터를 기반으로  
**여행 일정(TripPlan), 장소 리뷰, 맛집, 영화 추천**까지 연결하는 통합 취향 플랫폼입니다.

특히 **AI 기반 추천 기능**과 **다국어(번역) 지원 기능**을 통해  
외국인 사용자도 쉽게 이용할 수 있도록 설계했습니다.

---

## 🧠 Tech Stack

| 영역 | 기술 |
|------|------|
| Backend | Spring Boot, Java, JPA, Lombok |
| Frontend | HTML5, CSS3, JavaScript |
| Database | MariaDB |
| AI 기능 | OpenAI API (Summarization, Translation, Personalized Recommendation) |
| Tools | IntelliJ, GitHub |

---

## 🚀 핵심 기능

### 1️⃣ AI 기반 여행 추천
- TripPlan 내용·사용자 리뷰 기반 Embedding 생성  
- 유사도 기반 장소/여행 추천  
- “사용자 취향 프로필”을 자동 학습하는 추천 모델  

### 2️⃣ 다국어 번역 기능(외국인 사용자 지원)
- 한국어 ↔ 영어 ↔ 일본어/중국어 자동 변환  
- 사용자 리뷰, 장소 정보, 여행 일정 모두 번역 가능  
- 외국인도 사용할 수 있는 글로벌 플랫폼 구조  

### 3️⃣ TripPlan & Stop 관리
- 여행 일정 생성, 날짜별 Stop 추가·편집  
- 비용·카테고리·주소 정보 연동  
- 지도 기반 위치 표시 기능  

### 4️⃣ 카테고리 기반 취향 데이터 관리
- 영화/여행/맛집/장소 등 다양한 취향 데이터를 저장·관리  
- 리뷰 작성 및 이미지 업로드 기능 지원  

---

## 🧩 담당 기능 요약 (이지용)
- TripPlan/Stop CRUD 전담  
- AI 추천 API 설계 및 구현  
- 다국어 번역 API 적용(한국어/영어/일본어 등)  
- HTML 기반 여행/장소/리뷰 UI 개발  
- 관리자 없는 사용자 중심 구조 설계  

---

## 📎 Repository
https://github.com/jiyonglee/moodtrip

---

> “AI 추천 + 다국어 기능을 결합해 외국인 사용자도 쉽게 사용할 수 있는 여행 플랫폼을 구현했습니다.”
