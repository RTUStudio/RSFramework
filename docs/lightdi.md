# LightDI

`kr.rtustudio.cdi`

RSFramework 내부에 내장된 경량 의존성 주입(Dependency Injection) 프레임워크입니다.
복잡한 설정 없이 어노테이션 기반으로 클래스 간의 의존성을 해결하고 객체의 생명주기를 관리합니다.

---

## 1. 개요 및 원리

1. **자동 스캔**: 특정 패키지를 스캔하여 `@Component`가 붙은 클래스를 찾아 인스턴스화합니다.
2. **의존성 주입**: `@Inject`가 붙은 생성자나 필드에 필요한 인스턴스를 주입합니다.
3. **스코프 관리**: `@Scope`를 통해 싱글톤(Singleton) 또는 프로토타입(Prototype) 생명주기를 관리합니다.

---

## 2. 어노테이션

| 어노테이션 | 대상 | 설명 |
|-----------|------|------|
| `@Component` | 클래스 | DI 컨테이너가 관리할 빈(Bean)임을 나타냅니다. |
| `@Inject` | 생성자, 필드 | 의존성이 주입되어야 할 곳을 지정합니다. |
| `@Scope` | 클래스 | 빈의 스코프를 지정합니다. (기본값: `SINGLETON`) |

---

## 3. 빈 등록 및 주입 방식

### 3.1. 클래스 정의 (`@Component`)

```java
@Component
public class DatabaseManager {
    public void connect() {
        System.out.println("Connected to Database");
    }
}
```

### 3.2. 생성자 주입 (Constructor Injection - 권장)

필드를 `final`로 선언할 수 있어 가장 안전하고 권장되는 방식입니다.

```java
@Component
public class UserService {
    private final DatabaseManager db;

    @Inject
    public UserService(DatabaseManager db) {
        this.db = db;
    }
}
```

### 3.3. 필드 주입 (Field Injection)

```java
@Component
public class ItemManager {
    @Inject
    private DatabaseManager db;
}
```

---

## 4. 빈 스코프 (Scope)

LightDI는 두 가지 객체 생명주기를 지원합니다.

```java
// 1. 싱글톤 (기본값)
// 컨테이너 전체에서 단 하나의 인스턴스만 생성되어 공유됩니다.
@Component
@Scope(Scope.Type.SINGLETON)
public class SingletonService { }

// 2. 프로토타입
// 의존성이 주입되거나 getBean()으로 요청할 때마다 새로운 인스턴스가 생성됩니다.
@Component
@Scope(Scope.Type.PROTOTYPE)
public class PrototypeService { }
```

### PrototypeFactory

프로토타입 빈을 생성하는 로직이 복잡할 경우, 팩토리 패턴을 사용할 수 있습니다.

```java
@Component
public class MyBeanFactory implements PrototypeFactory<MyBean> {
    @Override
    public MyBean create() {
        return new MyBean("custom args");
    }
}
```

---

## 5. 수동 등록 및 조회

특정 객체를 직접 생성하여 DI 컨테이너에 등록하거나 꺼내 쓸 수 있습니다.
RSFramework 내부에서도 `Framework` 코어 객체를 이런 방식으로 등록하여 사용합니다.

```java
// 수동 등록
LightDI.registerBean(MyService.class, new MyService());

// 빈 조회
MyService service = LightDI.getBean(MyService.class);
```

```java
// RSPlugin 내부 (Framework 획득 예시)
this.framework = LightDI.getBean(Framework.class);
```
