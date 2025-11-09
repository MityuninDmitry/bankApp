# bankApp

Логин пароль пользователя по дефолту:
- login: testUser
- pass: password123

# Алгоритм сборки:
- открыть докер десктоп 
- в idea выполнить команду 
```
docker-compose  up --build
```
- После поднятия консула положить ключ в консул
```
  curl -X PUT \
  --data-binary "service.url.gateway=http://service-gateway" \
  http://localhost:8500/v1/kv/config/application/data
```
- остановить приложение 
- запустить повторно
```
docker-compose  up --build
```

# Точка входа в FrontUI:
http://localhost:8081/frontui/home

# Тесты: 
```
mvn clean install -pl Common
mvn clean install -pl Accounts
mvn clean test -pl Cash
mvn clean test -pl Transfer
mvn clean test -pl Notifications
mvn clean test -pl FrontUI
```