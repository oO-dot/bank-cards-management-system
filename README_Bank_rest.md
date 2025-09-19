# Bank Cards Management System

Система управления банковскими картами с JWT аутентификацией и ролевой моделью доступа.

## 🚀 Быстрый запуск

### Запуск через Docker Compose

1. Убедитесь, что установлены Docker и Docker Compose
2. Выполните команду: docker-compose up --build
3. Приложение будет доступно по адресу: http://localhost:8080/api
4. Swagger UI: http://localhost:8080/api/swagger-ui/index.html


📖 Документация API
Полная документация API доступна через Swagger UI после запуска приложения.

Основные endpoints:
Аутентификация
- POST /api/auth/login - аутентификация пользователя

Управление пользователями (ADMIN)
- GET /api/users/all - получить всех пользователей
- POST /api/users - создать пользователя
- GET /api/users/{id} - получить пользователя по ID
- PUT /api/users/{id} - обновить пользователя
- DELETE /api/users/{id} - удалить пользователя

Управление картами
- GET /api/cards/me - получить мои карты (USER)
- GET /api/cards - получить все карты (ADMIN)
- POST /api/cards - создать карту (ADMIN)
- GET /api/cards/{id} - получить карту по ID
- PUT /api/cards/{id}/block - заблокировать карту (ADMIN)
- PUT /api/cards/{id}/activate - активировать карту (ADMIN)
- DELETE /api/cards/{id} - удалить карту (ADMIN)
- POST /api/cards/transfer - перевод между картами (USER)
- GET /api/cards/{cardId}/balance - получить баланс карты (USER)
- POST /api/cards/{cardId}/block-request - запросить блокировку карты (USER)

Управление запросами на блокировку
- GET /api/admin/block-requests/pending - получить ожидающие запросы (ADMIN)
- POST /api/admin/block-requests/{requestId}/approve - одобрить запрос (ADMIN)
- POST /api/admin/block-requests/{requestId}/reject - отклонить запрос (ADMIN)
- GET /api/cards/block-requests/my - получить мои запросы (USER)


🔐 Учетные данные по умолчанию
Система включает предустановленных пользователей:

Администратор
- Логин: admin
- Пароль: password
- Роль: ADMIN

Пользователь
- Логин: user1
- Пароль: password
- Роль: USER


🛠 Технические детали
Архитектура
- Backend: Spring Boot 3.5.5
- База данных: PostgreSQL
- Аутентификация: JWT с Spring Security
- Миграции базы данных: Liquibase
- Документация API: Swagger/OpenAPI 3.0

Безопасность
- Шифрование номеров карт
- Маскирование данных при отображении
- Ролевая модель доступа
- Валидация входных данных

Особенности реализации
- Полностью соответствует требованиям ТЗ
- Интуитивно понятный REST API
- Подробная обработка ошибок
- Комprehensive документация
- Готовая Docker-среда для разработки

📦 Зависимости
Проект использует следующие основные зависимости:
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter Validation
- PostgreSQL Driver
- Liquibase Core
- JJWT для JWT-токенов
- MapStruct для маппинга DTO
- Springdoc OpenAPI для документации

🐛 Тестирование
Для тестирования функциональности:
- Запустите приложение
- Откройте Swagger UI: http://localhost:8080/api/swagger-ui/index.html
- Протестируйте endpoints с использованием предоставленных учетных данных


📄 Лицензия
- Проект разработан в рамках тестового задания. Apache License 2.0.