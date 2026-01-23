# 벡터 임베딩 메타데이터 패키지

이 패키지는 벡터 스토어에 저장되는 문서들의 메타데이터를 타입 안전한 클래스로 관리합니다.

## 구조

```
metadata/
├── DocumentMetadata.java          # 공통 인터페이스
├── ConsultationMetadata.java      # 상담 문서 메타데이터
└── LawArticleMetadata.java        # 법령 조문 문서 메타데이터
```

## 주요 클래스

### DocumentMetadata

모든 메타데이터 클래스가 구현하는 공통 인터페이스:

- `toMap()`: Map<String, Object>로 변환 (Vector Store 저장용)
- `getDocumentType()`: 문서 타입 반환

### ConsultationMetadata

상담(Counsel) 문서의 메타데이터를 관리합니다.

**주요 필드:**

- `counselId`: 상담 ID
- `title`: 상담 제목
- `lawIds`: 연관된 법령 ID 리스트
- `articleKeys`: 연관된 조문 키 리스트
- `lawArticlePairs`: "lawId:articleKey" 형식의 쌍 리스트

**사용 예시:**

```java
// Counsel 엔티티로부터 생성
ConsultationMetadata metadata = ConsultationMetadata.from(counsel);
metadata.setChunkIndex(0);
metadata.setTotalChunks(3);

// Map으로 변환하여 Document 생성
Document document = new Document(chunk, metadata.toMap());

// 검색 결과에서 복원
Map<String, Object> metadataMap = document.getMetadata();
ConsultationMetadata metadata = ConsultationMetadata.fromMap(metadataMap);
```

### LawArticleMetadata

법령 조문(Article) 문서의 메타데이터를 관리합니다.

**주요 필드:**

- `lawId`: 법령 ID
- `articleKey`: 조문 키
- `articleCode`: 조문 코드
- `lawNameKorean`: 법령명 (한글)

**사용 예시:**

```java
// LawBasicInformation과 Article 엔티티로부터 생성
LawArticleMetadata metadata = LawArticleMetadata.from(law, article);
metadata.setChunkIndex(0);
metadata.setTotalChunks(2);

// Map으로 변환하여 Document 생성
Document document = new Document(chunk, metadata.toMap());

// 검색 결과에서 복원
Map<String, Object> metadataMap = document.getMetadata();
LawArticleMetadata metadata = LawArticleMetadata.fromMap(metadataMap);
```

## 개선 효과

### Before (Map 기반)

```java
Map<String, Object> metadata = new HashMap<>();
metadata.put("counselId", consultation.getId());
metadata.put("lawIds", lawIds);
// ... 많은 put() 호출
// 타입 안전성 없음, 오타 가능성, IDE 자동완성 불가
```

### After (클래스 기반)

```java
ConsultationMetadata metadata = ConsultationMetadata.from(consultation);
metadata.setChunkIndex(0);
// 타입 안전, IDE 자동완성, 컴파일 타임 체크
Document document = new Document(chunk, metadata.toMap());
```

## 장점

1. **타입 안전성**: 컴파일 타임에 필드명 오타를 잡을 수 있습니다.
2. **코드 가독성**: 필드명이 명확하고 IDE 자동완성을 활용할 수 있습니다.
3. **유지보수성**: 필드 추가/변경 시 한 곳에서만 수정하면 됩니다.
4. **재사용성**: 엔티티로부터 생성, Map으로부터 복원 모두 지원합니다.
5. **테스트 용이성**: 메타데이터 객체를 직접 생성하여 테스트할 수 있습니다.

## 사용 위치

- `ConsultationEmbeddingService`: 상담 데이터 임베딩 시 사용
- `LawArticleEmbeddingService`: 법령 조문 임베딩 시 사용
- `ConsultationSearchService`: 검색 결과에서 메타데이터 복원 시 사용 (향후 개선 가능)
