#!/bin/bash

# Define colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0;30m' # No Color
RESET='\033[0m'

echo -e "${BLUE}================================================================${RESET}"
echo -e "${BLUE}          Checking E-Commerce Platform Infrastructure Status     ${RESET}"
echo -e "${BLUE}================================================================${RESET}"

# Function to check container status
check_container() {
    local container_name=$1
    local expected_port=$2
    local label=$3
    
    # Check if container is running
    local status=$(docker inspect --format='{{.State.Status}}' "$container_name" 2>/dev/null)
    local health=$(docker inspect --format='{{.State.Health.Status}}' "$container_name" 2>/dev/null)
    
    if [ "$status" = "running" ]; then
        if [ "$health" = "healthy" ]; then
            echo -e "  [${GREEN}RUNNING & HEALTHY${RESET}] - ${GREEN}$label${RESET} ($container_name) is healthy on port $expected_port"
            return 0
        elif [ "$health" = "starting" ]; then
            echo -e "  [${YELLOW}STARTING${RESET}]          - ${YELLOW}$label${RESET} ($container_name) is initializing on port $expected_port..."
            return 1
        elif [ -n "$health" ] && [ "$health" != "<nil>" ]; then
            echo -e "  [${RED}UNHEALTHY ($health)${RESET}] - ${RED}$label${RESET} ($container_name) on port $expected_port is reporting health: $health"
            return 1
        else
            echo -e "  [${GREEN}RUNNING${RESET}]           - ${GREEN}$label${RESET} ($container_name) is running on port $expected_port"
            return 0
        fi
    else
        echo -e "  [${RED}STOPPED/OFFLINE${RESET}]   - ${RED}$label${RESET} ($container_name) is not running! State: ${status:-Not found}"
        return 1
    fi
}

echo "Containers Status:"
check_container "ecommerce-postgres" "5432" "PostgreSQL 15"
postgres_ok=$?

check_container "ecommerce-redis" "6379" "Redis Cache 7"
redis_ok=$?

check_container "ecommerce-rabbitmq" "5672, 15672" "RabbitMQ Management"
rabbitmq_ok=$?

check_container "ecommerce-elasticsearch" "9200" "Elasticsearch"
elasticsearch_ok=$?

check_container "ecommerce-kibana" "5601" "Kibana Dashboard"
kibana_ok=$?

echo -e "${BLUE}----------------------------------------------------------------${RESET}"

# Verify logic connections / ping tests
echo "Performing active connection tests..."

# 1. Postgres Ping
if [ $postgres_ok -eq 0 ]; then
    if docker exec ecommerce-postgres pg_isready -U postgres -d ecommerce_db >/dev/null 2>&1; then
        echo -e "  - PostgreSQL: ${GREEN}Ping successful${RESET} (DB: ecommerce_db exists)"
    else
        echo -e "  - PostgreSQL: ${RED}Connection failed${RESET}"
    fi
else
    echo -e "  - PostgreSQL: ${RED}Skipped (Container offline)${RESET}"
fi

# 2. Redis Ping
if [ $redis_ok -eq 0 ]; then
    if docker exec ecommerce-redis redis-cli -a redis_secure_pass ping 2>/dev/null | grep -q PING; then
        echo -e "  - Redis: ${GREEN}Auth Ping successful${RESET} (Password secured)"
    elif docker exec ecommerce-redis redis-cli -a redis_secure_pass ping 2>/dev/null | grep -q PONG; then
        echo -e "  - Redis: ${GREEN}Auth Ping successful${RESET} (Password secured)"
    else
        echo -e "  - Redis: ${RED}Auth Ping failed${RESET}"
    fi
else
    echo -e "  - Redis: ${RED}Skipped (Container offline)${RESET}"
fi

# 3. RabbitMQ Ping
if [ $rabbitmq_ok -eq 0 ]; then
    if docker exec ecommerce-rabbitmq rabbitmq-diagnostics -q ping >/dev/null 2>&1; then
        echo -e "  - RabbitMQ: ${GREEN}Diagnostics ping successful${RESET}"
    else
        echo -e "  - RabbitMQ: ${RED}Diagnostics ping failed${RESET}"
    fi
else
    echo -e "  - RabbitMQ: ${RED}Skipped (Container offline)${RESET}"
fi

# 4. Elasticsearch Ping
if [ $elasticsearch_ok -eq 0 ]; then
    es_status=$(docker exec ecommerce-elasticsearch curl -s http://localhost:9200/ | grep -o '"tagline" : "You Know, for Search"')
    if [ -n "$es_status" ]; then
        echo -e "  - Elasticsearch: ${GREEN}REST API successful${RESET} ('You Know, for Search')"
    else
        echo -e "  - Elasticsearch: ${RED}REST API response invalid${RESET}"
    fi
else
    echo -e "  - Elasticsearch: ${RED}Skipped (Container offline)${RESET}"
fi

# 5. Kibana Ping
if [ $kibana_ok -eq 0 ]; then
    kibana_status=$(docker exec ecommerce-kibana curl -s -I http://localhost:5601/login | head -n 1 | cut -d$' ' -f2)
    if [ "$kibana_status" = "200" ] || [ "$kibana_status" = "302" ]; then
        echo -e "  - Kibana Dashboard: ${GREEN}HTTP $kibana_status OK${RESET}"
    else
        echo -e "  - Kibana Dashboard: ${RED}HTTP status invalid (or initializing)${RESET}"
    fi
else
    echo -e "  - Kibana Dashboard: ${RED}Skipped (Container offline)${RESET}"
fi

echo -e "${BLUE}================================================================${RESET}"
