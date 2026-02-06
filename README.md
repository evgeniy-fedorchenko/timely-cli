# README.md

# Timely CLI

Консольный тайм-трекер. Запускаешь таймер, работаешь, останавливаешь — видишь сколько времени потратил.

## Быстрый старт

```
# Собрать native image (нужен GraalVM)
mvn clean package -Pnative

# Запустить daemon в фоне
./target/timely daemon

# Работаем
./target/timely start
./target/timely status
./target/timely stop

# Остановить daemon
./target/timely shutdown
```

Для удобства можно добавить симлинк:
```
sudo ln -s $(pwd)/target/timely /usr/local/bin/timely
```

## Архитектура

Клиент-серверная архитектура: daemon хранит данные в памяти, CLI-команды общаются с ним по TCP.

### Prod-режим

```
┌─────────────────┐         TCP :52713         ┌─────────────────┐
│  timely start   │ ──────────────────────────>│                 │
│  timely stop    │ ──────────────────────────>│     Daemon      │
│  timely status  │ ──────────────────────────>│   (фоновый      │
│  timely shutdown│ ──────────────────────────>│    процесс)     │
└─────────────────┘                            └─────────────────┘
   Отдельные                                      Хранит Tracker
   процессы,                                      в памяти
   сразу завершаются
```

Каждая команда (`start`, `stop`, `status`) — отдельный процесс. Подключается к daemon, отправляет команду, получает ответ, завершается.

Daemon запускается командой `timely daemon`:
1. Форкает себя с флагом `--foreground`
2. Фоновый процесс запускает TCP-сервер
3. Основной процесс проверяет что daemon поднялся и завершается
4. Терминал свободен

### Dev-режим

```
┌──────────────────────────────────────────────┐
│              timely --dev                    │
│  ┌─────────────────┐    ┌─────────────────┐  │
│  │  InteractiveCli │───>│  DaemonServer   │  │
│  │                 │TCP │  (virtual       │  │
│  │  > start        │    │   thread)       │  │
│  │  > status       │    │                 │  │
│  │  > exit         │    │  Tracker        │  │
│  └─────────────────┘    └─────────────────┘  │
└──────────────────────────────────────────────┘
              Один процесс
```

Всё в одном процессе для удобства разработки:
- Daemon запускается в виртуальном потоке
- CLI читает команды из stdin
- При `exit` останавливает daemon и завершается

Запуск из IDEA: конфигурация `.run/dev-all.run.xml`.
При открытии файла IDEA сама предложит создать из него run-конфигурацию, после чего ее можно сразу запустить.
Этот конфиг запускает приложение в режиме dev (требуется java 25)

## Структура проекта
```

src/main/java/com/efedorchenko/timely/
 ├── Main                    # Точка входа, picocli
 ├── AppProperties           # Версия из app.properties
 ├── VersionProvider         # Настройка версии проекта для picocli
 │ 
 ├── client
 │    ├── DaemonClient       # TCP-клиент
 │    ├── DaemonNotRunningException
 │    ├── InteractiveCli     # Dev-режим REPL   
 │    └── ResponsePrinter    # Форматирование вывода 
 │ 
 ├── command                 # Picocli-команды     
 │    ├── DaemonCommand      # timely daemon       
 │    ├── ShutdownCommand    # timely start        
 │    ├── StartCommand       # timely stop         
 │    ├── StatusCommand      # timely status       
 │    └── StopCommand        # timely shutdown     
 │ 
 ├── daemon
 │    └── DaemonServer       # TCP-сервер, обработка команд  
 │ 
 ├── format
 │    └── DurationFormatter  # Форматтер, "1h 2min 3sec"
 │ 
 ├── protocol
 │    ├── Protocol           # Константы и парсинг протокола
 │    └── StatusResponse     # pojo для статуса
 │ 
 └── tracker
      ├── StatusInfo         # Снимок состояния трекера, pojo
      ├── TimeSession        # Модель сессии (sealed interface)  
      ├── TimeTracker        # Интерфейс трекера 
      └── Tracker            # Реализация, хранит сессии
 
```

## Протокол

Текстовый, одна строка = одно сообщение.

**Команды (CLI → Daemon):**
```
ping
start
stop  
status
shutdown
```

**Ответы (Daemon → CLI):**
```
timely                          # pong
OK                              # start успех
OK 3723                         # stop успех (секунды сессии)
OK 120 3723 true                # status (current, total, running)
ERROR Timer is not running      # ошибка
BYE 3723                        # shutdown (total секунды)
```

## Сборка

```
# Обычный JAR (для тестов)
mvn clean package

# Native image (для использования)  
mvn clean package -Pnative
```

Требования для native image:
- GraalVM 25+ (`sdk install java 25.0.2-graalce`)

## Тестирование версии 1.0

Покрыта бизнес-логика (Tracker, Protocol, DurationFormatter) и интеграция клиент-сервер (DaemonServer, DaemonClient).
Не покрыты dev-инструменты (InteractiveCli) и тонкие обёртки (Commands) — осознанно, см. комментарии в коде.
![Coverage](docs/coverage.png)
```bash
# Для замера покрытия
mvn verify

# Результат в target/site/jacoco/index.html
```

## Что не реализовано (задел на будущее)

- **Проекты**: `start work`, `start study` — структура готова, но пока не используется
- **Персистентность**: данные в памяти, при остановке daemon теряются
- **Логирование**: метод `log()` в DaemonServer пустой, когда разрешат файлы — будет писать в лог
