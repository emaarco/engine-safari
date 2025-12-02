# 🌿 CIB7 - Community Fork

**CIB7** (CIB Software's Seven) - a maintained fork of Camunda 7 by [CIB Software GmbH](https://cibseven.org/).

## About CIB Software GmbH

[CIB Software GmbH](https://cibseven.org/)is a German software company that forked Camunda 7 
to provide continued support and enhancements beyond its end of life. 
They offer both community-driven development and commercial support options.

## Quick Start

1. Start PostgreSQL: `docker-compose -f stack/docker-compose.yml up -d`
2. Use the IntelliJ run configuration in `/run` folder
3. Access CIB7 UI: http://localhost:8081/camunda (admin/admin)
4. API operations available in `/bruno` folder

**Note:** CIB7 uses JWT-based authentication for the webclient. 
The JWT secret in `application.yaml` is for development only.
