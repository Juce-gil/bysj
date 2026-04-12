#!/bin/bash

# Script to fix Docker registry mirror issues on deployment server
# Run on the server where deployment failed

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Docker Registry Mirror Fix Script ===${NC}"
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

# Step 1: Check current Docker daemon configuration
step "1. Checking current Docker daemon configuration..."
if [ -f /etc/docker/daemon.json ]; then
    info "Current /etc/docker/daemon.json:"
    cat /etc/docker/daemon.json
    echo

    # Check for problematic registry.cn-hangzhou.aliyuncs.com
    if grep -q "registry.cn-hangzhou.aliyuncs.com" /etc/docker/daemon.json; then
        info "Found problematic registry.cn-hangzhou.aliyuncs.com mirror"
        info "This mirror may not have all official images or requires authorization"

        read -p "Do you want to replace it with reliable mirrors? (y/n): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            # Create backup
            cp /etc/docker/daemon.json /etc/docker/daemon.json.backup.$(date +%Y%m%d_%H%M%S)
            info "Created backup at /etc/docker/daemon.json.backup"

            # Create new configuration with reliable mirrors
            cat > /etc/docker/daemon.json << 'EOF'
{
  "debug": true,
  "experimental": false,
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://docker.m.daocloud.io",
    "https://lispy.org",
    "https://docker-0.unsee.tech"
  ]
}
EOF
            info "Updated /etc/docker/daemon.json with reliable mirrors"
        else
            info "Keeping current configuration"
        fi
    else
        info "No problematic registry.cn-hangzhou.aliyuncs.com found"
    fi
else
    info "/etc/docker/daemon.json does not exist, creating with reliable mirrors..."
    mkdir -p /etc/docker
    cat > /etc/docker/daemon.json << 'EOF'
{
  "debug": true,
  "experimental": false,
  "registry-mirrors": [
    "https://docker.1ms.run",
    "https://docker.m.daocloud.io",
    "https://lispy.org",
    "https://docker-0.unsee.tech"
  ]
}
EOF
    info "Created /etc/docker/daemon.json"
fi

# Step 2: Restart Docker service
step "2. Restarting Docker service..."
systemctl restart docker

# Wait a moment for Docker to restart
sleep 3

# Step 3: Verify Docker is running
step "3. Verifying Docker service..."
if systemctl is-active --quiet docker; then
    info "Docker service is running"
else
    error "Docker service failed to start"
    systemctl status docker --no-pager
    exit 1
fi

# Step 4: Test Docker pull with a simple image
step "4. Testing Docker image pull..."
info "Testing pull of a simple image (alpine:latest)..."
if docker pull alpine:latest; then
    info "Docker image pull successful!"
else
    error "Docker image pull failed"
    info "Checking Docker info for registry mirrors..."
    docker info | grep -A5 "Registry Mirrors"
    exit 1
fi

# Step 5: Show current registry mirrors
step "5. Current registry mirrors configuration:"
docker info | grep -A10 "Registry Mirrors" || info "No registry mirrors configured"

echo
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}   Docker Registry Fix Complete!         ${NC}"
echo -e "${GREEN}=========================================${NC}"
echo
echo "Next steps:"
echo "1. Go back to deployment directory: cd /opt/campus-market"
echo "2. Retry deployment: ./deploy.sh"
echo "3. Or rebuild manually: docker-compose build --no-cache"
echo
echo "If you still encounter image pull issues:"
echo "1. Check network connectivity: curl -I https://docker.io"
echo "2. Try without mirrors: remove /etc/docker/daemon.json and restart Docker"
echo "3. Use Docker's official registry directly"
echo
echo -e "${YELLOW}Note:${NC} The first build may still take time due to Maven dependencies"
echo "and Docker image downloads."