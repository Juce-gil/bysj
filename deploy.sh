#!/bin/bash

# Deployment script for Campus Market on Ubuntu server
# Run as root user

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Campus Market Deployment Script ===${NC}"
echo -e "${YELLOW}Target server: 39.104.93.85${NC}"
echo -e "${YELLOW}Deployment directory: /opt/campus-market${NC}"
echo

# Check if running as root
if [[ $EUID -ne 0 ]]; then
   echo -e "${RED}This script must be run as root${NC}"
   exit 1
fi

# Function to print step messages
step() {
    echo -e "${GREEN}[STEP] $1${NC}"
}

# Function to print info messages
info() {
    echo -e "${YELLOW}[INFO] $1${NC}"
}

# Function to print error messages
error() {
    echo -e "${RED}[ERROR] $1${NC}"
}

validate_env_file() {
    local missing_keys=()
    local placeholder_keys=()

    for key in MYSQL_ROOT_PASSWORD DB_USERNAME DB_PASSWORD JWT_SECRET; do
        if ! grep -q "^${key}=" .env; then
            missing_keys+=("${key}")
        fi
    done

    if grep -q "^DB_PASSWORD=market_password$" .env || grep -q "^DB_PASSWORD=change_this_password$" .env || grep -q "^DB_PASSWORD=your_secure_password$" .env; then
        placeholder_keys+=("DB_PASSWORD")
    fi

    if grep -q "^JWT_SECRET=your-production-jwt-secret-key-2025$" .env || grep -q "^JWT_SECRET=change-this-jwt-secret-key-2025$" .env || grep -q "^JWT_SECRET=your-secure-jwt-secret-key-2025-change-this$" .env; then
        placeholder_keys+=("JWT_SECRET")
    fi

    if [ ${#missing_keys[@]} -gt 0 ]; then
        error ".env 缺少必要配置: ${missing_keys[*]}"
        exit 1
    fi

    if [ ${#placeholder_keys[@]} -gt 0 ]; then
        error ".env 仍包含示例占位值，请先修改: ${placeholder_keys[*]}"
        exit 1
    fi
}

# Step 1: Install Docker
step "1. Installing Docker..."
if ! command -v docker &> /dev/null; then
    info "Docker not found, installing..."
    apt-get update
    apt-get install -y apt-transport-https ca-certificates curl software-properties-common
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -
    add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
    apt-get update
    apt-get install -y docker-ce docker-ce-cli containerd.io
else
    info "Docker is already installed"
fi

# Step 2: Install Docker Compose
step "2. Installing Docker Compose..."
if ! command -v docker-compose &> /dev/null; then
    info "Docker Compose not found, installing..."
    curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
else
    info "Docker Compose is already installed"
fi

# Step 3: Enable and start Docker service
step "3. Starting Docker service..."
systemctl enable docker
systemctl start docker

# Step 4: Create deployment directory
step "4. Creating deployment directory..."
DEPLOY_DIR="/opt/campus-market"
if [ ! -d "$DEPLOY_DIR" ]; then
    mkdir -p "$DEPLOY_DIR"
    info "Created directory: $DEPLOY_DIR"
else
    info "Directory already exists: $DEPLOY_DIR"
fi

# Step 5: Copy project files (this step assumes files are already copied)
step "5. Setting up project files..."
info "Please ensure all project files are in $DEPLOY_DIR"
info "If not, copy files using: scp -r /path/to/project/* root@39.104.93.85:$DEPLOY_DIR/"
read -p "Are project files in place? (y/n): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    error "Please copy project files to $DEPLOY_DIR first"
    exit 1
fi

# Step 6: Navigate to deployment directory
cd "$DEPLOY_DIR"

# Step 6.5: Warn about known broken Docker registry mirror settings
if [ -f /etc/docker/daemon.json ] && grep -q "registry.cn-hangzhou.aliyuncs.com" /etc/docker/daemon.json; then
    info "Detected registry.cn-hangzhou.aliyuncs.com in /etc/docker/daemon.json"
    info "If image pulls fail with 'pull access denied' for official images, remove or replace that mirror and restart Docker."
fi

# Step 7: Check and configure environment variables
step "6. Configuring environment variables..."
if [ ! -f .env ]; then
    error ".env file not found!"
    info "Creating .env from example..."
    if [ -f .env.example ]; then
        cp .env.example .env
        info "Created .env file. Please edit it with proper values."
        echo
        echo "Current .env content:"
        cat .env
        echo
        info "Please edit .env file with your production values."
        read -p "Press Enter after editing .env file..."
    else
        error ".env.example not found either. Please create .env manually."
        exit 1
    fi
else
    info ".env file exists"
    echo
    echo "Current .env content:"
    cat .env
    echo
    read -p "Is the configuration correct? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        info "Please edit .env file before continuing"
        exit 1
    fi
fi

step "6.5 Validating .env values..."
validate_env_file

# Step 8: Build Docker images
step "7. Building Docker images..."
info "This may take several minutes..."
docker-compose build

# Step 9: Start services
step "8. Starting services..."
docker-compose up -d

# Step 10: Check service status
step "9. Checking service status..."
sleep 10  # Give services time to start
docker-compose ps

# Step 11: Check logs
step "10. Checking service logs..."
info "Displaying recent logs (Ctrl+C to continue)..."
docker-compose logs --tail=50

# Step 12: Verify services
step "11. Verifying services..."
info "Checking backend health..."
if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
    info "Backend is healthy"
else
    error "Backend health check failed"
    docker-compose logs backend
fi

info "Checking frontend..."
if curl -f http://localhost >/dev/null 2>&1; then
    info "Frontend is accessible"
else
    error "Frontend check failed"
    docker-compose logs frontend
fi

# Step 13: Print success message
step "12. Deployment completed!"
echo
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}   Campus Market Deployment Complete!   ${NC}"
echo -e "${GREEN}=========================================${NC}"
echo
echo "Services:"
echo "  - Frontend: http://39.104.93.85"
echo "  - Backend API: http://39.104.93.85:8080"
echo "  - API Documentation: http://39.104.93.85:8080/swagger-ui.html"
echo
echo "Default login credentials:"
echo "  - Admin: admin / 123456"
echo "  - User: zhangsan / 123456"
echo
echo "Management commands:"
echo "  - View logs: docker-compose logs -f"
echo "  - Stop services: docker-compose down"
echo "  - Restart services: docker-compose restart"
echo "  - Update deployment: git pull && docker-compose up -d --build"
echo
echo -e "${YELLOW}Important:${NC}"
echo "1. Change default passwords in the database"
echo "2. Update JWT secret in .env file"
echo "3. Configure firewall to allow ports 80 and 8080"
echo "4. Set up SSL/TLS for production"
echo
echo -e "${GREEN}Deployment script finished.${NC}"
