#!/bin/bash
# 🛠️ Скрипт реорганизации проекта на клиент-серверную архитектуру
# Запускать из корня проекта: bash reorganize_project.sh

set -e

# Цвета
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║           🔄 Реорганизация проекта на модули           ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""

# Проверка запуска из корня
if [ ! -d "src/main/java" ]; then
    echo -e "${RED}❌ Ошибка: Запустите скрипт из корня проекта, где лежит папка src/${NC}"
    exit 1
fi

# 1. Резервная копия
echo -e "${YELLOW}💾 Создаю резервную копию проекта...${NC}"
BACKUP_DIR="backup_$(date +%F_%H%M%S)"
mkdir -p "$BACKUP_DIR"
cp -r src pom.xml products.xml README.md "$BACKUP_DIR/" 2>/dev/null || true
echo -e "${GREEN}✅ Бэкап сохранён в: $BACKUP_DIR${NC}"
echo ""

# 2. Очистка артефактов и старых файлов
echo -e "${YELLOW}🧹 Очистка временных файлов и старого кода...${NC}"
rm -rf out/ target/ dock/ docs/ .idea/ *.class *.jar *.log 2>/dev/null || true
rm -f src/main/java/Main.java src/main/java/manager/Invoker.java 2>/dev/null || true
rm -f src/main/resources/pruducts.xml 2>/dev/null || true # Удаляем файл с опечаткой
echo -e "${GREEN}✅ Очистка завершена${NC}"
echo ""

# 3. Создание структуры модулей
echo -e "${YELLOW}📁 Создание модульной структуры...${NC}"
for mod in shared server client; do
    mkdir -p "$mod/src/main/java"
    mkdir -p "$mod/src/main/resources"
    mkdir -p "$mod/src/test/java"
done
echo -e "${GREEN}✅ Структура создана${NC}"
echo ""

# 4. Перемещение файлов
echo -e "${YELLOW}📦 Перемещение файлов в модули...${NC}"

# SHARED
mkdir -p shared/src/main/java/{enums,manager,shared/search,commands}
mv src/main/java/enums/*.java shared/src/main/java/enums/ 2>/dev/null || true
mv src/main/java/manager/Product.java src/main/java/manager/Organization.java src/main/java/manager/Coordinates.java shared/src/main/java/manager/ 2>/dev/null || true
mv src/main/java/shared/*.java shared/src/main/java/shared/ 2>/dev/null || true
mv src/main/java/shared/search/*.java shared/src/main/java/shared/search/ 2>/dev/null || true
mv src/main/java/commands/*.java shared/src/main/java/commands/ 2>/dev/null || true

# SERVER
mkdir -p server/src/main/java/{server/{commands,network,reliability},manager}
mv src/main/java/server/*.java server/src/main/java/server/ 2>/dev/null || true
mv src/main/java/server/commands/*.java server/src/main/java/server/commands/ 2>/dev/null || true
mv src/main/java/server/network/*.java server/src/main/java/server/network/ 2>/dev/null || true
mv src/main/java/server/reliability/*.java server/src/main/java/server/reliability/ 2>/dev/null || true
mv src/main/java/manager/CollectionManager.java src/main/java/manager/XMLManager.java src/main/java/manager/ScriptDepthTracker.java server/src/main/java/manager/ 2>/dev/null || true
mv src/main/resources/products.xml server/src/main/resources/ 2>/dev/null || true
mv src/main/resources/test_*.txt server/src/main/resources/ 2>/dev/null || true

# CLIENT
mkdir -p client/src/main/java/{client/{console,network}}
mv src/main/java/client/*.java client/src/main/java/client/ 2>/dev/null || true
mv src/main/java/client/console/*.java client/src/main/java/client/console/ 2>/dev/null || true
mv src/main/java/client/network/*.java client/src/main/java/client/network/ 2>/dev/null || true
mv src/main/java/manager/InputHelper.java client/src/main/java/client/console/ 2>/dev/null || true

echo -e "${GREEN}✅ Файлы распределены${NC}"
echo ""

# 5. Автоматическое исправление пакетов и импортов
echo -e "${YELLOW}🛠️ Исправление package и import...${NC}"

# Функция для замены package/import (кроссплатформенная)
fix_packages() {
    local dir=$1
    local search=$2
    local replace=$3
    find "$dir" -name "*.java" -exec sed -i '' "s|package $search;|package $replace;|g" {} \;
    find "$dir" -name "*.java" -exec sed -i '' "s|import $search\.|import $replace.|g" {} \;
}

# Коррекция пакетов
fix_packages "shared/src/main/java" "manager" "shared.manager"
fix_packages "shared/src/main/java" "shared.search" "shared.search"
fix_packages "shared/src/main/java" "commands" "commands"
fix_packages "shared/src/main/java" "enums" "enums"

fix_packages "server/src/main/java" "manager" "server.manager"
fix_packages "server/src/main/java" "server.commands" "server.commands"
fix_packages "server/src/main/java" "server.network" "server.network"
fix_packages "server/src/main/java" "server.reliability" "server.reliability"
fix_packages "server/src/main/java" "server" "server"

fix_packages "client/src/main/java" "client.console" "client.console"
fix_packages "client/src/main/java" "client.network" "client.network"
fix_packages "client/src/main/java" "client" "client"

# Исправление импортов между модулями
find shared/src/main/java -name "*.java" -exec sed -i '' 's|import shared\.shared\.|import shared.|g' {} \;
find server/src/main/java -name "*.java" -exec sed -i '' 's|import server\.manager\.|import server.manager.|g' {} \;
find server/src/main/java -name "*.java" -exec sed -i '' 's|import shared\.|import shared.|g' {} \;

# Удаление временных бэкапов sed
find . -name "*.java-e" -delete 2>/dev/null || true
echo -e "${GREEN}✅ Пакеты и импорты обновлены${NC}"
echo ""

# 6. Генерация базовых pom.xml
echo -e "${YELLOW}📝 Генерация pom.xml...${NC}"

cat > shared/pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent><groupId>prog.lab5</groupId><artifactId>product-manager</artifactId><version>1.0</version></parent>
    <artifactId>shared</artifactId>
    <packaging>jar</packaging>
    <properties><maven.compiler.source>17</maven.compiler.source><maven.compiler.target>17</maven.compiler.target></properties>
    <dependencies>
        <dependency><groupId>org.tinylog</groupId><artifactId>tinylog-api</artifactId><version>2.6.1</version></dependency>
        <dependency><groupId>org.tinylog</groupId><artifactId>tinylog-impl</artifactId><version>2.6.1</version></dependency>
    </dependencies>
</project>
EOF

cat > server/pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent><groupId>prog.lab5</groupId><artifactId>product-manager</artifactId><version>1.0</version></parent>
    <artifactId>server</artifactId>
    <packaging>jar</packaging>
    <properties><maven.compiler.source>17</maven.compiler.source><maven.compiler.target>17</maven.compiler.target></properties>
    <dependencies>
        <dependency><groupId>prog.lab5</groupId><artifactId>shared</artifactId><version>1.0</version></dependency>
        <dependency><groupId>org.tinylog</groupId><artifactId>tinylog-api</artifactId><version>2.6.1</version></dependency>
        <dependency><groupId>org.tinylog</groupId><artifactId>tinylog-impl</artifactId><version>2.6.1</version></dependency>
    </dependencies>
</project>
EOF

cat > client/pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent><groupId>prog.lab5</groupId><artifactId>product-manager</artifactId><version>1.0</version></parent>
    <artifactId>client</artifactId>
    <packaging>jar</packaging>
    <properties><maven.compiler.source>17</maven.compiler.source><maven.compiler.target>17</maven.compiler.target></properties>
    <dependencies>
        <dependency><groupId>prog.lab5</groupId><artifactId>shared</artifactId><version>1.0</version></dependency>
        <dependency><groupId>org.tinylog</groupId><artifactId>tinylog-api</artifactId><version>2.6.1</version></dependency>
        <dependency><groupId>org.tinylog</groupId><artifactId>tinylog-impl</artifactId><version>2.6.1</version></dependency>
    </dependencies>
</project>
EOF

cat > pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>prog.lab5</groupId>
    <artifactId>product-manager</artifactId>
    <version>1.0</version>
    <packaging>pom</packaging>
    <modules>
        <module>shared</module>
        <module>server</module>
        <module>client</module>
    </modules>
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
</project>
EOF

echo -e "${GREEN}✅ pom.xml созданы${NC}"
echo ""

# 7. Очистка старой папки src
echo -e "${YELLOW}🗑️ Удаление старой структуры src/...${NC}"
rm -rf src/ 2>/dev/null || true
find . -type d -empty -delete 2>/dev/null || true
echo -e "${GREEN}✅ Старая структура удалена${NC}"
echo ""

echo -e "${BLUE}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                    ✅ ГОТОВО!                          ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${GREEN}📂 Итоговая структура:${NC}"
echo "   shared/   → модели, команды, Request/Response, FieldPath"
echo "   server/   → UDP-сервер, CollectionManager, XML, логирование"
echo "   client/   → UDP-клиент, консольный ввод, валидация"
echo ""
echo -e "${YELLOW}⚠️  Следующие шаги:${NC}"
echo "   1. Откройте проект в IntelliJ IDEA: File → Open → выберите корень pom.xml"
echo "   2. Дождитесь индексации Maven-зависимостей"
echo "   3. Проверьте импорты в файлах команд (возможно, потребуется ручная корректировка)"
echo "   4. Запустите сервер: server/src/main/java/server/ServerApp.java"
echo "   5. Запустите клиент: client/src/main/java/client/ClientApp.java"
echo ""
