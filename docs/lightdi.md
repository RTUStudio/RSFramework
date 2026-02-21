# 💉 LightDI (경량 의존성 주입)

LightDI(`kr.rtustudio.cdi`)는 RSFramework를 위해 특별히 설계된 경량화된 IoC(Inversion of Control) / DI(Dependency Injection) 컨테이너입니다.
Spring의 핵심 DI 기능과 유사하게 작동하며, 플러그인 생태계에 맞춰 최적화되어 있습니다.

## 🎯 설계 목적
Bukkit 플러그인은 클래스 로더가 독립적으로 작동하고 동적으로 리로드될 수 있어 복잡한 서드파티 DI 라이브러리를 사용하기 까다롭습니다.
LightDI는 어노테이션 기반으로 리플렉션을 활용해 싱글톤(Singleton) 빈(Bean)을 자동으로 스캔하고 주입합니다.

## 🚀 주요 기능

### 1. 빈 등록 (`@Component`)
프레임워크나 플러그인 내에서 의존성 주입을 받고 싶은 클래스에 `@Component`를 붙입니다.
클래스는 반드시 파라미터가 없는 기본 생성자(`@NoArgsConstructor`)를 가져야 합니다.

```java
import kr.rtustudio.cdi.annotation.Component;

@Component
public class DatabaseManager {
    public void connect() { ... }
}
```

### 2. 객체 가져오기
`LightDI.getBean(Class)`를 통해 어디서든 싱글톤 객체 인스턴스를 가져올 수 있습니다.

```java
import kr.rtustudio.cdi.LightDI;

DatabaseManager db = LightDI.getBean(DatabaseManager.class);
db.connect();
```

### 3. 내부 주입 및 확장
RSFramework는 `RSPlugin`, `Framework`, `StorageManager` 등 주요 핵심 코어들을 LightDI를 통해 관리합니다.
따라서 사용자는 수동으로 `new` 키워드를 사용해 객체를 생성하지 않고, 등록된 Bean을 불러와 일관된 상태(State)를 유지할 수 있습니다.
