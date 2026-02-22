---
trigger: model_decision
description: 
globs: 
---

작업 끝나면 항상 README를 최신화해

1. Lombok 어노테이션을 항상 활용해야해
2. 모든 작업이 끝난 후 모든 코드에서 아예 쓰이지 않는 변수나 메소드는 제거해야해
3. 조건부/반복문 한줄 작성 규칙: 조건과 본문을 합친 전체 길이가 한 줄에 무리 없이 들어올 정도로 짧을 때만 한줄로 작성한다. 구체적으로 아래 두 조건을 모두 만족해야 한다.
    - 조건이 단순 비교/플래그 확인 수준일 것 (메서드 체인 없음)
    - 본문이 단일 메서드 호출 또는 return/throw 한 개일 것 (인자가 복잡하지 않을 것)
    - 올바른 예: if (str.isEmpty()) return; / if (reload) command.reload();
    - 잘못된 예: if (scope == Scope.GLOBAL) return list.stream().map(X::name).toList();
4. 부정 if문을 작성할때 instanceof if문은 부정으로 작성하지 않아야해 if (!(entity instanceof LivingEntity)) return이 아니라 if (entity instanceof
   LivingEntity) {}
5. 항상 코드는 간결하고 가독성이 중요하며 성능에 민감해야해 (fastutil 사용 추천
6. 번역 파일에서는 빈 줄바꿈이 있으면 안돼
7. 구성 클래스를 생성할때 그 클래스가 메인 클래스에서 registerConfiguration 되는 루트 구성이 아닌 경우에는 루트 구성의 서브 클래스로 생성해야한다
8. FQCN 금지 (구현체와 인터페이스 클래스 이름이 같을때는 제외)
9. 메인 클래스의 클래스 변수를 Getter로 가져오거나 getConfiguration(Class.class)로 변수를 가져오는 경우 생성자에서 처리하여 클래스 변수로 초기화해야한다