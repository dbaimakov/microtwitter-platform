.PHONY: up up-local-auth down clean logs ps rebuild

up:
	docker compose up --build -d

up-local-auth:
	docker compose -f compose.yaml -f compose.local-auth.yaml up --build -d

down:
	docker compose down

clean:
	docker compose down -v --remove-orphans

logs:
	docker compose logs -f --tail=200

ps:
	docker compose ps

rebuild:
	docker compose build --no-cache
