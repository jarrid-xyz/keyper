FROM node:22 AS node

RUN npm install --global cdktf-cli@latest

FROM ubuntu:latest AS terraform

RUN \
    # Update
    apt-get update -y && \
    # Install Unzip
    apt-get install unzip -y && \
    # need wget
    apt-get install wget -y

RUN wget https://releases.hashicorp.com/terraform/1.9.1/terraform_1.9.1_linux_amd64.zip
RUN unzip terraform_1.9.1_linux_amd64.zip

FROM gradle:jdk21 AS gradle

## Copy node binaries and modules from the node stage
COPY --from=node /usr/local/include/node /usr/local/include/node
COPY --from=node /usr/local/lib/node_modules /usr/local/lib/node_modules
COPY --from=node /usr/local/bin /usr/local/bin
COPY --from=terraform /terraform /usr/local/bin

# Create the keyper user and home directory
RUN useradd -ms /bin/bash keyper

WORKDIR /home/keyper

# Copy application files
COPY lib ./lib
COPY gradle ./gradle
COPY settings.gradle.kts ./
COPY gradlew ./gradlew
COPY gradlew.bat ./gradlew.bat
COPY cdktf.json ./cdktf.json

# Ensure gradlew is executable and build the application
RUN ./gradlew clean build

RUN chown -R keyper:keyper /home/keyper
RUN chmod 755 /home/keyper

# Give permission to /github/workspace for github action
RUN mkdir -p /github/workspace && \
    chown -R keyper:keyper /github/workspace && \
    chmod 755 /github/workspace

COPY github-entrypoint.sh /home/keyper/github-entrypoint.sh
RUN chmod +x /home/keyper/github-entrypoint.sh

# Set environment variables
ENV TF_PLUGIN_CACHE_DIR=/tmp/.terraform.d
ENV CDKTF_HOME=/tmp/.cdktf

USER keyper

ENTRYPOINT ["java", "-jar", "lib/build/libs/lib-cli.jar"]
CMD ["sh"]