# macOS first-time setup (no Homebrew or Maven required)

Java 21 is already present on the target Mac. The POC Dockerfiles build Java services inside Maven/JDK builder images, so a host Maven installation is not needed to launch the Compose stack.

## Install Docker Desktop for the current user

1. Check the processor: `uname -m` (`arm64` = Apple silicon; `x86_64` = Intel).
2. Download the matching Docker Desktop DMG from the official page: https://docs.docker.com/desktop/setup/install/mac-install/
3. Open the DMG and move `Docker.app` to Applications. If the system-wide `/Applications` folder is not writable, use a user-owned `~/Applications` folder instead (create it first with `mkdir -p ~/Applications`).
4. Launch Docker Desktop and accept its terms. Keep the default user-level CLI installation under `~/.docker/bin`; do not select options that create a system-wide socket or require privileged helper settings if you do not have admin rights.
5. If the Docker CLI is not found after installation, add the user directory to the shell PATH:

   ```bash
   echo 'export PATH="$HOME/.docker/bin:$PATH"' >> ~/.zshrc
   source ~/.zshrc
   ```

6. Confirm the engine is running:

   ```bash
   docker version
   docker compose version
   docker info
   ```

Docker Desktop documentation describes a default configuration that uses `$HOME/.docker/bin` and does not create the system-wide Docker socket. See:
- https://docs.docker.com/desktop/setup/install/mac-install/
- https://docs.docker.com/desktop/setup/install/mac-permission-requirements/

If your organization manages this Mac and blocks Docker Desktop, request an approved Docker/Container runtime from IT. Do not try to bypass device-management controls.

## Start the full local core POC

From the repository root:

```bash
./scripts/start-all.sh
```

The script builds and starts RabbitMQ, the eight Spring Boot services, the API Gateway and React portal; waits for HTTP readiness; loads the synthetic invoice demo; runs smoke checks; and opens the portal.

The first Docker build downloads base images and Maven dependencies, so it can take a while and requires network access.

## Analytics boundary

Snowflake is an external cloud service and cannot be started locally by Compose. The Scala/Spark ingestion and Snowflake load are a separate job requiring Snowflake credentials and the matching Spark connector. See `data-platform/spark-pipeline/README.md`.
