# Configuration 시스템

Sponge Configurate 기반 YAML 객체 매핑 및 설정 파일 관리 시스템.

## 모듈 구조

```
Configurate/                                   플랫폼 독립 모듈 (java-library)
└── kr.rtustudio.configurate.model
    ├── ConfigurationPart                      설정 객체 베이스 클래스
    ├── Configuration                          YAML 로드·저장·리로드 추상 클래스
    ├── ConfigPath                             설정 파일/폴더 경로 레코드
    ├── ConfigList                             폴더 내 YAML 파일 컨테이너
    ├── ConfigurationSerializer                내장 직렬화기·제약 조건 일괄 등록
    ├── constraint/                            @Constraint, @Min, @Max, Positive
    ├── mapping/                               InnerClassFieldDiscoverer, MergeMap, FieldProcessor
    ├── serializer/                            ComponentSerializer, EnumValueSerializer, MapSerializer 등
    └── type/                                  Duration, BooleanOrDefault, IntOr, DoubleOr 등

Framework/API/
└── kr.rtustudio.framework.bukkit.api.configuration
    ├── RSConfiguration                        Bukkit 플러그인 설정 레지스트리
    └── PluginConfiguration                    플러그인 설정 래퍼
```

## 핵심 기능

### 통합 캐싱 레지스트리

등록된 설정은 `Registry` 레코드로 래핑되어 단일 맵에서 관리된다.
- 단일 파일(`registerConfiguration`) 또는 폴더(`registerConfigurations`) 여부를 기억
- `/reload` 호출 시 파일 추가·삭제까지 자동 재스캔

### ConfigList (다중 파일)

`ConfigPath.of("Regions")`로 폴더를 지정하면 폴더 내 모든 `.yml` 파일을 파일명 키로 매핑한다. 불변 래퍼로 제공되어 직접 수정을 방지한다.

### 커스텀 어노테이션

- **`@Constraint`** — 필드에 커스텀 제약 조건 적용. 내장: `@Min`, `@Max`, `Positive`
- **`@MergeMap`** — 맵 리로드 시 기본 키 보호, `restricted` 속성으로 새 키 추가 방지

### 내장 직렬화기

| 직렬화기 | 설명 |
|---------|------|
| `ComponentSerializer` | Adventure Component ↔ MiniMessage 문자열 |
| `EnumValueSerializer` | 하이픈(`-`) → 언더스코어(`_`) 치환, 실패 시 옵션 리스트 출력 |
| `MapSerializer` | 개별 항목 실패 시 예외 대신 로그만 남김 |
| `FastutilMapSerializer` | Fastutil 프리미티브 맵 지원 |
| `FlattenedMapSerializer` | `Map<Object[], Object>` 평탄화 직렬화 |

### Map 어노테이션

- **`@ThrowExceptions`** — MapSerializer에서 단일 실패 시에도 예외를 강제
- **`@WriteKeyBack`** — 역직렬화 후 정규화된 키를 파일에 반영

### 커스텀 타입

| 타입 | 설명 |
|-----|------|
| `Duration` | `"30s"`, `"5m"`, `"1h"`, `"2d"` 형식 시간 기간 |
| `DurationOrDisabled` | Duration 또는 `"disabled"` |
| `BooleanOrDefault` | boolean 또는 `"default"` |
| `IntOr.Default` / `IntOr.Disabled` | 정수 또는 `"default"` / `"disabled"` |
| `DoubleOr.Default` / `DoubleOr.Disabled` | 실수 버전 |

### 내부 클래스 지원

`InnerClassFieldDiscoverer`가 비정적 내부 클래스(`ConfigurationPart` 상속)의 인스턴스를 자동 생성하고, `FieldProcessor`를 통해 필드 후처리(병합 등)를 지원한다.

## 의존성

- `Configurate` 모듈은 `api(libs.configurate.yaml)`로 Configurate YAML을 전이적으로 노출
- 빌드 시 `org.spongepowered.configurate` → `kr.rtustudio.configurate`로 relocate
- `kr.rtustudio.configurate.model.*` 래퍼 클래스는 shadow JAR에서 제외
