# Makefile for the frontend and backend

.PHONY: run build clean run-backend start-backend stop-backend start-frontend stop-frontend stop-all

# Build the production bundle
build:
	./gradlew :backend:build
	./gradlew :frontend:jsBrowserProductionWebpack
# --- Frontend ---

# Start the frontend development server
run-frontend:
	./gradlew :frontend:jsBrowserDevelopmentRun

# Start the frontend development server in background
start-frontend:
	nohup ./gradlew :frontend:jsBrowserDevelopmentRun --no-daemon > frontend.log 2>&1 & echo $$! > frontend.pid

# Stop the frontend development server
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

# --- Backend ---

# Start the backend in foreground
run-backend:
	./gradlew :backend:run

# Start the backend in background
start-backend:
	nohup ./gradlew :backend:run --no-daemon > backend.log 2>&1 & echo $$! > backend.pid

# Stop the backend
stop-backend:
	@if [ -f backend.pid ]; then \
		pkill -P $$(cat backend.pid) || true; \
		kill $$(cat backend.pid) || true; \
		rm backend.pid; \
		echo "Backend stopped."; \
	else \
		echo "backend.pid not found."; \
	fi

# --- Global ---

# Stop both services
stop-all: stop-frontend stop-backend
start-all: start-frontend start-backend
start-all-debug-backend: start-frontend run-backend
dcup: build
	docker-compose up -d --build
