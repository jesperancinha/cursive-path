.PHONY: run build clean run-backend start-backend stop-backend start-frontend stop-frontend stop-all
build:
	./gradlew :backend:clean
	./gradlew :backend:build
	./gradlew :frontend:clean
	./gradlew :frontend:jsBrowserProductionWebpack
	./gradlew :frontend:build
run-frontend:
	./gradlew :frontend:jsBrowserDevelopmentRun
start-frontend:
	nohup ./gradlew :frontend:jsBrowserDevelopmentRun --no-daemon > frontend.log 2>&1 & echo $$! > frontend.pid
stop-frontend:
	@if [ -f frontend.pid ]; then \
		pkill -P $$(cat frontend.pid) || true; \
		kill $$(cat frontend.pid) || true; \
		rm frontend.pid; \
		echo "Frontend stopped."; \
	else \
		echo "frontend.pid not found."; \
	fi


# Clean the frontend build artifacts
clean:
	./gradlew :frontend:clean
	rm -f frontend.pid frontend.log backend.pid backend.log
run-backend:
	./gradlew :backend:run
start-backend:
	nohup ./gradlew :backend:run --no-daemon > backend.log 2>&1 & echo $$! > backend.pid
stop-backend:
	@if [ -f backend.pid ]; then \
		pkill -P $$(cat backend.pid) || true; \
		kill $$(cat backend.pid) || true; \
		rm backend.pid; \
		echo "Backend stopped."; \
	else \
		echo "backend.pid not found."; \
	fi
stop-all: stop-frontend stop-backend
start-all: start-frontend start-backend
start-all-debug-backend: start-frontend run-backend
dcup: docker-remove-local build
	mkdir -p libretranslate_data
	sudo chown -R $(USER):$(USER) libretranslate_data
	docker-compose up -d libretranslate --build
	./wait.sh
	docker-compose up -d --build --force-recreate
dcup-translate: build
dcup-local: build start-all
	docker run --rm -it -p 5000:5000 libretranslate/libretranslate
docker-remove-local:
	docker ps -a --format '{{.ID}}' --filter="name=cursive-path-backend" | xargs -I {}  docker stop {}
	docker ps -a --format '{{.ID}}' --filter="name=cursive-path-backend" | xargs -I {}  docker rm {}
	docker ps -a --format '{{.ID}}' --filter="name=cursive-path-frontend" | xargs -I {}  docker stop {}
	docker ps -a --format '{{.ID}}' --filter="name=cursive-path-frontend" | xargs -I {}  docker rm {}
	docker images --format '{{.ID}}' --filter="reference=cursive-path*"  | xargs -I {} docker rmi {}