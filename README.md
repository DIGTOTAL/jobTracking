# job-tracking

Приложение умеет работать в двух режимах:

* **CLI режим** — текущий консольный интерфейс (команды из `commands.txt`).
* **Web режим (Spring Boot)** — REST API + планировщик поиска "лучшего предложения".

## Запуск CLI
Точка входа: `ru.vk.education.job.Main`.

## Запуск Web (Spring Boot)
Точка входа: `ru.vk.education.job.TestApp`.

По умолчанию сервер стартует на `http://localhost:8080`.

### REST API
* `POST /api/users` — создать пользователя
* `GET /api/users` — список пользователей
* `GET /api/users/{name}` — получить пользователя
* `PUT /api/users/{name}` — обновить/создать (upsert)
* `DELETE /api/users/{name}` — удалить

* `POST /api/vacancies` — создать вакансию
* `GET /api/vacancies` — список вакансий
* `GET /api/vacancies/{vacancyName}` — получить вакансию
* `PUT /api/vacancies/{vacancyName}` — обновить/создать (upsert)
* `DELETE /api/vacancies/{vacancyName}` — удалить

* `GET /api/suggest/{userName}?limit=2` — рекомендации вакансий
* `GET /api/stat/exp?min=2` — вакансии с опытом >= min
* `GET /api/stat/match?min=2` — пользователи с количеством матчей >= min
* `GET /api/stat/top-skills?limit=3` — топ навыков

* `POST /api/best-offer/run` — ручной пересчёт лучших предложений

### Планировщик best-offer
Настройки в `src/main/resources/application.properties`:

* `job.best-offer.fixed-delay-ms` — задержка между запусками
* `job.best-offer.initial-delay-ms` — задержка перед первым запуском

## Сборка и тесты
Проект собирается Gradle. В тестах проверяется базовый CRUD через REST.
