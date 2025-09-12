# ==========
# Makefile — Fever Plans Ingestion Service
# Requisitos: Docker + Docker Compose, Java 21+, Maven
# Uso rápido:
#   make db-up      # levanta Postgres en docker
#   make run        # arranca la app Spring Boot (requiere db-up)
#   make db-logs    # ver logs de Postgres
#   make db-down    # baja los contenedores del compose
#   make db-clean   # baja todo + borra volumen de datos
#   make build      # compila y empaqueta (sin tests)
#   make test       # ejecuta tests
#   make help       # muestra ayuda
# ==========

SHELL := /bin/sh

# Comandos (puedes ajustarlos si usas wrappers)
COMPOSE ?= docker compose
MVN = ./mvnw

# ==========
# HELP
# ==========
.PHONY: help
help:
	@echo ""
	@echo "Targets disponibles:"
	@echo "  make db-up     - Levanta Postgres en docker (docker-compose.yml)"
	@echo "  make db-logs   - Muestra logs de Postgres"
	@echo "  make db-down   - Para y elimina los contenedores del compose"
	@echo "  make db-clean  - db-down + elimina volumen de datos"
	@echo "  make run       - Arranca la app con Spring Boot (requiere db-up)"
	@echo "  make build     - Compila y empaqueta (skip tests)"
	@echo "  make test      - Ejecuta tests"
	@echo "  make help      - Esta ayuda"
	@echo ""

# ==========
# DATABASE (Docker)
# ==========
.PHONY: db-up db-logs db-down db-clean
db-up:
	@$(COMPOSE) up -d

db-logs:
	@$(COMPOSE) logs -f postgres

db-down:
	@$(COMPOSE) down

# Limpia todo: contenedores del compose + volumen de datos del proyecto
db-clean: db-down
	@$(COMPOSE) down -v

# ==========
# APP
# ==========
.PHONY: run build test
run:
	@$(MVN) spring-boot:run

build:
	@$(MVN) -DskipTests package

test:
	@$(MVN) test
