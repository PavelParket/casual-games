# Git Flow

### После клонирования репозитория засетать имя и email

```shell

git config user.name "your_name"
git config user.email "your-email@gmail.com"
```

Эти настройки локальные для этого репозитория и позволяют истории коммитов оставаться понятной. Имена ставим адекватные,
чтобы было понятно кто выполнял задачу

## Ветки

На каждую задачу - новая ветка от ветки develop. Для того чтобы:

- безопасно работать без риска сломать чужой код;
- легче отслеживать, кто что сделал;
- делать чистую историю коммитов и PR.

Процесс работы с веткой:
> Создаём &rarr; делаем задачу &rarr; открываем Pull Request &rarr; мержим ветки &rarr; удаляем.

Название ветки должно кратко и чётко отражать выполняемую задачу.
Примеры:

- CG-2-add-real-email-registration — новая функциональность для регистрации пользователя по его email;
- CG-8-room-deletion-outbox — реализация outbox шаблона для синхронизации удаления комнат;
- CG-11-update-adaptive-ui — Обновление адаптивного пользовательского интерфейса.

> Какие есть ветки:
> - main - текущий релиз;
> - develop - текущее состояние разработки;
> - `CG-{number}-кратко-о-задаче` - ветка для твоей задачи.

CG — идентификатор, он всегда "CG"; \
number — номер задачи.

## Коммиты

Название коммита кратко и чётко отражает изменения сделанные в нём

Не допускаются абстрактные названия, примеры недопустимых коммитов:

- fix pr
- change
- changed after pr

> <p style="color:red">За такое по рукам!</p>

Примеры хороших коммитов:

- add user authentication for login page
- correct null pointer in game session init
- extract message parser into separate util

## Pull Request

PR-ка создаётся из твоей ветки в develop

#### Название по шаблону:

> <b>CG-{number} кратко_о_чём_PR-ка</b>

CG — идентификатор, он всегда "CG"; \
number — номер задачи.

Примеры:

- CG-13 implement user authentication
- CG-53 fix null pointer in session init

Префикс обеспечивает автоматический линк PR-ки с задачей

> <b>Если сомневаешься, спрашивай тимлида — лучше уточнить, чем потом исправлять.</b>

# Как пользоваться стартерами

Стартеры (`common-utils`, `security-starter`, `redis-starter`, `kafka-starter`, `grpc-utils`,
`file-management-starter`) публикуются в GitHub Packages (GPR) и тянутся оттуда автоматически.

## Первоначальная настройка

Добавить токен в `~/.gradle/gradle.properties` (файл вне репозитория, не коммитится):

```properties
gprUser=
gprToken=
```

Токен создаётся на `github.com → Settings → Developer settings → Personal access tokens → Tokens (classic)`.
Нужен только scope **`read:packages`**.

После этого IDEA подтянет стартеры автоматически при Reload Gradle — ничего собирать руками не нужно.

## Версии стартеров

Версия задаётся в `gradle.properties` каждого сервиса (`commonUtilsVersion`):

| Значение               | Когда использовать                                      |
|------------------------|---------------------------------------------------------|
| `1.4.0`                | продовая сборка (выставляется автоматически при деплое) |
| `1.4.0-SNAPSHOT`       | обычная разработка (дефолт, коммитится)                 |
| `1.4.0-local-SNAPSHOT` | когда сам правишь стартеры локально (не коммитить!)     |

## Локальная разработка стартеров

Если задача требует изменений в `common-utils`:

```shell
# 1. Опубликовать локальную версию
cd backend/common-utils
./gradlew publishToMavenLocal -Pversion=1.4.0-local-SNAPSHOT

# 2. В gradle.properties нужного сервиса (не коммитить!)
commonUtilsVersion=1.4.0-local-SNAPSHOT

# 3. Reload Gradle в IDEA — сервис подхватит локальные стартеры

# 4. Перед коммитом вернуть дефолт
commonUtilsVersion=1.4.0-SNAPSHOT
```

# Деплой на VPS

Деплой всегда выполняется с конкретного git-тега. Нетегованный код на прод не уходит.

## Полный деплой

```shell
bash deploy/deploy.sh v1.4.0
```

Что делает скрипт:

1. `git fetch --tags && git checkout v1.4.0`
2. `docker compose build` — сервисы собираются, стартеры `1.4.0` тянутся из GPR
3. `docker compose up -d`

## Точечный деплой (один или несколько сервисов)

Используется когда нужно пересобрать конкретный сервис без полного редеплоя.
Требует что HEAD уже стоит на теге (т.е. `deploy.sh` уже запускался).

```shell
bash deploy/deploy-service.sh bank-service
bash deploy/deploy-service.sh bank-service user-service
```

## Релизный цикл

```
1. Разработка идёт в feature-ветках → develop
2. develop → main (PR от тимлида)
3. git tag v1.4.0 && git push origin v1.4.0
   → автоматически публикует стартеры 1.4.0 в GPR (GitHub Actions)
4. На VPS: bash deploy/deploy.sh v1.4.0
```
