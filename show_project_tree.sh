#!/bin/bash
# 🔍 Скрипт вывода древовидной структуры проекта
# Запускать из корня проекта: bash show_project_tree.sh

# Цвета для оформления
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # Сброс цвета

echo -e "${BLUE}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                  🌳 Структура проекта                   ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""

# Паттерны для исключения (системные, билд, IDE, временные файлы)
IGNORE=".git|.idea|target|build|node_modules|__pycache__|*.class|*.jar|*.log|*.iml|*.xml~"

if command -v tree &> /dev/null; then
    # Если tree установлен - используем его (красивый вывод)
    tree -a --dirsfirst -I "$IGNORE" -L 4 --charset utf-8
else
    # Fallback если tree нет
    echo -e "${YELLOW}⚠️  Утилита 'tree' не найдена. Использую встроенный fallback...${NC}"
    echo -e "${GREEN}💡 Совет: для идеального вывода установите tree (sudo apt install tree / brew install tree)${NC}"
    echo ""
    find . -not -path "./.git/*" \
           -not -path "./.idea/*" \
           -not -path "./target/*" \
           -not -path "./build/*" \
           -not -name "*.class" \
           -not -name "*.jar" \
           -not -name "*.log" \
           -not -name "*.iml" \
           | sort \
           | sed -e 's;[^/]*/;│   ;g;s;│   \([│ ]\);    \1;'
fi

echo ""
echo -e "${GREEN}✅ Готово. Скопируйте весь вывод выше и отправьте мне следующим сообщением.${NC}"
