# ⚙️ Configuration 시스템

`SpongePowered Configurate`를 기반으로 한 강력한 객체 매핑 및 설정 파일 관리 시스템입니다. 

## ✨ 핵심 기능

### 1. 통합 캐싱 레지스트리 관리
모든 등록된 설정은 내부적으로 `Registry` 레코드로 래핑되어 단일 맵(`registries`)에서 관리됩니다.
- **기능**: 단일 파일(`registerConfiguration`) 또는 폴더 전체(`registerConfigurations`) 여부를 기억하고, 각 설정에 대한 파일 경로(`Path`), 직렬화 방식(`Serializer`)을 함께 캐싱합니다.
- **장점**: `/reload` 또는 `reloadAll()` 호출 시, 폴더에서 삭제되거나 새로 추가된 파일까지 완벽하게 재스캔하여 자동으로 갱신합니다. 기존 설정 목록을 일일이 재등록할 필요가 없습니다.

### 2. ConfigList (다중 파일 관리)
디렉토리 내의 모든 `yml` 파일을 읽어 하나의 맵(Map)처럼 반환하는 래퍼입니다.
- **기능**: 폴더 경로(예: `ConfigPath.of("Regions")`)를 지정하면, 폴더 안의 모든 파일을 스캔하여 파일명을 키로 하는 객체 모음을 생성합니다.
- **장점**: 얇은 불변(Immutable) 래퍼로 제공되어 개발자가 `Map`을 직접 수정하는 실수를 방지하고 API의 의도를 명확하게 합니다.

### 3. 커스텀 어노테이션

#### `@Constraint`
숫자 등 필드에 제약 조건을 겁니다. 매핑(역직렬화) 시 값을 검증합니다.
- **내장 제약**: `@Min`, `@Max`, `@Positive`

#### `@MergeMap`
`Map` 형태의 설정을 리로드할 때, 기존(Default) 키가 무단으로 지워지거나, 예상치 못한 새로운 키가 추가되는 것을 방지합니다. (`restricted` 속성 지원)

### 4. 컬렉션 & 특수 직렬화기 (Custom Serializers)

RSFramework는 복잡한 자료구조나 특수 객체를 YAML로 손쉽게 다루기 위해 다수의 커스텀 직렬화기를 내장하고 있습니다.

- **ComponentSerializer**: Adventure `Component`를 `MiniMessage` 문자열 포맷으로 변환합니다.
- **EnumValueSerializer**: Enum 값을 역직렬화할 때 하이픈(`-`)을 언더바(`_`)로 치환하여 유연하게 매핑하며, 실패 시 사용 가능한 옵션 리스트를 제공합니다.
- **TableSerializer**: Guava의 `Table<R, C, V>`를 중첩된 맵 구조로 매핑합니다.
- **MapSerializer 계열**:
  - `MapSerializer`: 맵 항목 직렬화 중 하나가 실패해도 전체가 터지지 않고 오류만 로깅합니다.
  - `FastutilMapSerializer`: 메모리/성능 최적화된 FastUtil 맵(원시 타입 맵 포함)을 지원합니다.
  - `FlattenedMapSerializer`: `Map<Object[], Object>` 구조를 평탄화하여 `{[a, b, c] = value}` 형태의 YAML을 원래 순서대로 보존하며 역직렬화합니다.

#### Map 어노테이션 옵션
- `@ThrowExceptions`: `MapSerializer` 사용 시 특정 맵에서는 단일 실패 시에도 오류를 내뿜도록 강제합니다.
- `@WriteKeyBack`: 역직렬화 시 키 값이 정규화되어 변경되었다면, 변경된 키를 실제 설정 파일에 덮어씌워 갱신합니다.

### 4. 내부 클래스 구조 지원 (`InnerClassInstanceSupplier`)
비정적(Non-static) 내부 클래스로 구성된 설정 파트(`ConfigurationPart`를 상속한 이너 클래스)에 대해 자동으로 인스턴스화를 지원하며, `FieldProcessor`를 통해 설정값 병합/후처리 라이프사이클을 제공합니다.
