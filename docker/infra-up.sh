#!/bin/bash

# Define colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
RESET='\033[0m'

# Get the script directory
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

echo -e "${BLUE}================================================================${RESET}"
echo -e "${BLUE}        Starting E-Commerce Platform Infrastructure Stack       ${RESET}"
echo -e "${BLUE}================================================================${RESET}"

# Make scripts executable
chmod +x "$DIR/check-status.sh"
chmod +x "$DIR/init-db.sh"

# Determine docker compose command to use
if docker compose version >/dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
elif docker-compose version >/dev/null 2>&1; then
    COMPOSE_CMD="docker-compose"
else
    echo -e "${RED}Error: Docker Compose is not installed on this system!${RESET}"
    exit 1
fi

echo -e "Using Compose command: ${GREEN}$COMPOSE_CMD${RESET}"

# Start containers
echo "Spinning up containers..."
$COMPOSE_CMD -f "$DIR/docker-compose.yml" up -d

echo -e "\n${YELLOW}Waiting 25 seconds for infrastructure to initialize completely...${RESET}"
echo "Elasticsearch, PostgreSQL and RabbitMQ require boot time."

# Countdown timer
for i in {25..1}; do
    echo -ne "  Initializing... ${YELLOW}$i${RESET} seconds remaining...\r"
    sleep 1
done
echo -e "\n"

# Verify status
"$DIR/check-status.sh"

echo -e "\n${GREEN}Infrastructure boot attempt complete.${RESET}"
echo "You can check status again anytime by running: ./docker/check-status.sh"
echo -e "${BLUE}================================================================${RESET}"
