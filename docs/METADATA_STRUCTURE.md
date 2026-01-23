# 벡터 검색 메타데이터 구조

이 문서는 벡터 스토어에 저장되는 문서들의 메타데이터 구조와 이를 활용한 검색 유사도 향상 방법을 설명합니다.

## 개요

벡터 검색의 정확도를 높이기 위해, counsel(상담) 데이터와 lawArticle(법령 조문) 데이터의 메타데이터에 공통 필드를 추가하여 서로 연관된 문서들을 더 쉽게 찾을 수 있도록 구조화했습니다.

## 메타데이터 구조

### 1. Counsel (상담) 문서 메타데이터

```json
{
  "documentType": "counsel",
  "counselId": 123,
  "title": "상담 제목",
  "fieldLarge": "상담 분야",
  "createdAt": "2024-01-01T00:00:00",

  // 기존 필드 (호환성 유지)
  "lawArticleKeys": ["0018031", "0015021"],
  "lawArticles": ["제 18조", "제 15조의 2"],

  // 벡터 검색 유사도 향상을 위한 새로운 필드
  "lawIds": ["LAW001", "LAW002"], // 연관된 모든 lawId (중복 제거)
  "articleKeys": ["0018031", "0015021"], // 연관된 모든 articleKey (중복 제거)
  "lawArticlePairs": [
    // lawId와 articleKey의 정확한 쌍
    "LAW001:0018031",
    "LAW002:0015021"
  ],

  // 청크 정보 (긴 문서의 경우)
  "chunkIndex": 0,
  "totalChunks": 3
}
```

### 2. LawArticle (법령 조문) 문서 메타데이터

```json
{
  "documentType": "lawArticle",
  "lawId": "LAW001",
  "lawKey": "law_key_001",
  "lawNameKorean": "법령명",
  "lawNameNickname": "법령 약칭",
  "enforceDate": "20240101",
  "lawCategory": "법종 구분",

  "articleId": 456,
  "articleKey": "0018031",
  "articleCode": "18-3",
  "articleKoreanString": "제18조의3",
  "articleNumber": "18",
  "articleBranchNumber": "3",
  "articleTitle": "조문제목",
  "articleEnforceDate": "20240101",

  // 청크 정보 (긴 조문의 경우)
  "chunkIndex": 0,
  "totalChunks": 2
}
```

## 메타데이터 활용 방법

### 1. 공통 필드를 통한 연관 문서 검색

#### Counsel → LawArticle 검색

특정 상담과 연관된 법령 조문을 찾을 때:

```java
// 상담 문서의 메타데이터에서 lawId와 articleKey 추출
List<String> lawIds = extractLawIds(consultationMetadata);
List<String> articleKeys = extractArticleKeys(consultationMetadata);

// 벡터 검색 시 메타데이터 필터링
SearchRequest searchRequest = SearchRequest.builder()
    .query(query)
    .topK(10)
    .similarityThreshold(0.6)
    .filterExpression("documentType == 'lawArticle' && " +
                     "lawId IN [" + String.join(",", lawIds) + "] && " +
                     "articleKey IN [" + String.join(",", articleKeys) + "]")
    .build();
```

#### LawArticle → Counsel 검색

특정 법령 조문과 연관된 상담을 찾을 때:

```java
// 법령 조문의 메타데이터에서 lawId와 articleKey 추출
String lawId = extractLawId(articleMetadata);
String articleKey = extractArticleKey(articleMetadata);

// 벡터 검색 시 메타데이터 필터링
SearchRequest searchRequest = SearchRequest.builder()
    .query(query)
    .topK(10)
    .similarityThreshold(0.6)
    .filterExpression("documentType == 'counsel' && " +
                     "lawIds CONTAINS '" + lawId + "' && " +
                     "articleKeys CONTAINS '" + articleKey + "'")
    .build();
```

### 2. 정확한 매칭을 위한 lawArticlePairs 활용

`lawArticlePairs`는 `"lawId:articleKey"` 형식의 문자열 리스트로, 정확한 법령-조문 쌍을 표현합니다.

```java
// 상담 문서에서 정확한 법령-조문 쌍 추출
List<String> pairs = consultationMetadata.get("lawArticlePairs");
// 예: ["LAW001:0018031", "LAW002:0015021"]

// 법령 조문 문서와 정확히 매칭
String articlePair = lawId + ":" + articleKey;
if (pairs.contains(articlePair)) {
    // 정확히 연관된 문서
}
```

### 3. 하이브리드 검색 전략

벡터 유사도 검색과 메타데이터 필터링을 결합하여 검색 정확도를 높일 수 있습니다:

```java
// 1단계: 벡터 유사도 검색 (넓은 범위)
List<Document> candidates = vectorStore.similaritySearch(
    SearchRequest.builder()
        .query(query)
        .topK(50)  // 넓은 범위로 검색
        .build()
);

// 2단계: 메타데이터 기반 필터링 및 재랭킹
List<Document> filtered = candidates.stream()
    .filter(doc -> {
        Map<String, Object> meta = doc.getMetadata();
        String docType = (String) meta.get("documentType");

        if ("counsel".equals(docType)) {
            // 상담 문서: lawIds나 articleKeys에 검색 대상이 포함되는지 확인
            List<String> lawIds = (List<String>) meta.get("lawIds");
            List<String> articleKeys = (List<String>) meta.get("articleKeys");
            return lawIds != null && lawIds.contains(targetLawId) ||
                   articleKeys != null && articleKeys.contains(targetArticleKey);
        } else if ("lawArticle".equals(docType)) {
            // 법령 조문: lawId와 articleKey가 일치하는지 확인
            return targetLawId.equals(meta.get("lawId")) &&
                   targetArticleKey.equals(meta.get("articleKey"));
        }
        return false;
    })
    .sorted(Comparator.comparing(Document::getScore).reversed())
    .limit(10)
    .toList();
```

## 메타데이터 필드 설명

### Counsel 문서

| 필드명            | 타입         | 설명                                | 예시                   |
| ----------------- | ------------ | ----------------------------------- | ---------------------- |
| `documentType`    | String       | 문서 타입 구분자                    | "counsel"              |
| `counselId`       | Long         | 상담 ID                             | 123                    |
| `lawIds`          | List<String> | 연관된 법령 ID 리스트 (중복 제거)   | ["LAW001", "LAW002"]   |
| `articleKeys`     | List<String> | 연관된 조문 키 리스트 (중복 제거)   | ["0018031", "0015021"] |
| `lawArticlePairs` | List<String> | "lawId:articleKey" 형식의 쌍 리스트 | ["LAW001:0018031"]     |
| `lawArticleKeys`  | List<String> | 기존 호환성 필드 (articleKey만)     | ["0018031"]            |
| `lawArticles`     | List<String> | 한국어 형식 조문 표현               | ["제 18조"]            |

### LawArticle 문서

| 필드명                | 타입   | 설명                       | 예시         |
| --------------------- | ------ | -------------------------- | ------------ |
| `documentType`        | String | 문서 타입 구분자           | "lawArticle" |
| `lawId`               | String | 법령 ID                    | "LAW001"     |
| `articleKey`          | String | 조문 키                    | "0018031"    |
| `articleCode`         | String | 조문 코드 (숫자-숫자 형식) | "18-3"       |
| `articleKoreanString` | String | 한국어 조문 표현           | "제18조의3"  |

## 검색 유사도 향상 전략

### 1. 메타데이터 부스팅

메타데이터가 일치하는 문서에 가중치를 부여:

```java
double baseScore = document.getScore();
Map<String, Object> meta = document.getMetadata();

// 메타데이터 일치 시 점수 부스팅
if (hasMatchingMetadata(meta, targetLawId, targetArticleKey)) {
    baseScore *= 1.2;  // 20% 부스팅
}
```

### 2. 하이브리드 검색

벡터 검색 + 메타데이터 필터링 + 키워드 매칭:

```java
// 1. 벡터 유사도 점수
double vectorScore = document.getScore();

// 2. 메타데이터 일치 점수
double metadataScore = calculateMetadataScore(meta, target);

// 3. 키워드 매칭 점수
double keywordScore = calculateKeywordScore(document.getText(), query);

// 최종 점수 = 가중 평균
double finalScore = 0.5 * vectorScore + 0.3 * metadataScore + 0.2 * keywordScore;
```

### 3. 관련 문서 확장 검색

직접 연관된 문서뿐만 아니라 간접적으로 연관된 문서도 찾기:

```java
// 1. 직접 연관: 같은 lawId 또는 articleKey
// 2. 간접 연관: 같은 lawId를 가진 다른 articleKey
// 3. 확장 검색: 같은 법령 카테고리를 가진 문서들
```

## 구현 예시

### ConsultationEmbeddingService

`buildMetadata()` 메서드에서 `lawIds`, `articleKeys`, `lawArticlePairs`를 추가합니다.

### LawArticleEmbeddingService

`buildArticleMetadata()` 메서드에서 `lawId`와 `articleKey`를 포함합니다.

### 검색 서비스 확장

메타데이터 필터링을 지원하는 검색 메서드를 추가할 수 있습니다:

```java
public List<SearchResult> searchWithMetadataFilter(
    String query,
    String lawId,
    String articleKey,
    int topK
) {
    // 메타데이터 필터링이 포함된 검색
}
```

## 주의사항

1. **메타데이터 크기**: 리스트 필드가 많을 경우 메타데이터 크기가 커질 수 있으므로, 필요한 필드만 포함하세요.

2. **인덱싱**: 벡터 스토어에서 메타데이터 필드에 대한 인덱싱이 지원되는지 확인하세요.

3. **호환성**: 기존 `lawArticleKeys` 필드는 호환성을 위해 유지됩니다.

4. **null 처리**: 메타데이터 필드가 null일 수 있으므로 안전하게 처리하세요.

## 참고

- [Spring AI Vector Store Documentation](https://docs.spring.io/spring-ai/reference/api/vectordbs.html)
- [PgVector Metadata Filtering](https://github.com/pgvector/pgvector)
