
FROM jenkins/inbound-agent:latest-jdk21

USER root

# Install system dependencies and Ansible
RUN apt-get update && apt-get install -y --no-install-recommends \
    git \
    curl \
    ca-certificates \
    gnupg \
    ansible \
    python3 \
    python3-pip \
    python3-venv \
    tar \
    gzip \
    && rm -rf /var/lib/apt/lists/*

# Install official Docker CLI
RUN install -m 0755 -d /etc/apt/keyrings \
    && curl -fsSL https://download.docker.com/linux/debian/gpg \
       -o /etc/apt/keyrings/docker.asc \
    && chmod a+r /etc/apt/keyrings/docker.asc \
    && . /etc/os-release \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian ${VERSION_CODENAME} stable" \
       > /etc/apt/sources.list.d/docker.list \
    && apt-get update \
    && apt-get install -y --no-install-recommends docker-ce-cli \
    && rm -rf /var/lib/apt/lists/*

# Install Gitleaks
RUN curl -fsSL https://github.com/gitleaks/gitleaks/releases/download/v8.30.1/gitleaks_8.30.1_linux_x64.tar.gz \
    -o /tmp/gitleaks.tar.gz \
    && tar -xzf /tmp/gitleaks.tar.gz -C /usr/local/bin gitleaks \
    && rm /tmp/gitleaks.tar.gz

# Install kubectl
RUN curl -fsSLo /usr/local/bin/kubectl \
    https://dl.k8s.io/release/v1.37.1/bin/linux/amd64/kubectl \
    && chmod +x /usr/local/bin/kubectl

# Return to the non-root Jenkins user
USER jenkins
