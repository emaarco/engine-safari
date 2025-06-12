# 🚀 Easy C7

Welcome to **Easy C7** – a helpful example setup for using **Camunda 7** as a process engine to orchestrate BPMN workflows! This repository demonstrates how to integrate Camunda 7 into a Spring-based service and offers a comprehensive testing setup.

Whether you're just getting started with Camunda 7 or looking for a reliable testing setup, **easy-c7** has got you covered! 💡

## ⚠️ Important Notice

Camunda 7 is approaching its end of life, with official support ending in late 2025. If you're starting a new project, you might want to consider these alternatives:

- **Camunda 8 (Zeebe)**: The official successor to Camunda 7, offering improved scalability and cloud-native architecture
- **Community Forks**: Several community-maintained forks of Camunda 7 that will continue to receive updates and support
- **Other BPMN Engines**: Various other open-source BPMN workflow engines available in the market

If you're currently using Camunda 7, it's recommended to plan your migration strategy well in advance of the end-of-life date.

## 📚 Overview

The repository is organized as follows:

- **`/src`**: Contains the main service code, including modules for integrating and testing Camunda Platform 7.
- **`/stack`**: Infrastructure setup containing the database required to run the examples.
- **`/bruno`**: Example requests to interact with the service using [Bruno](https://www.usebruno.com/).
- **`/run`**: IntelliJ run configurations to easily start the service.

## 🔧 Getting Started

Follow these steps to get the service up and running:

1. **Start the Infrastructure**

   Navigate to the `/stack` directory and start the infrastructure using Docker Compose:

   ```bash
   docker-compose up -d
   ```

2. **Run the Application**

   The project is a **Gradle**-based Spring service. While you can run it via command line, the easiest way is using **IntelliJ**:

    - Open the project in IntelliJ.
    - Go to the **`/run`** folder.
    - Use the provided run configurations to start the application.

3. **Interact with the API**

   Use the requests provided in the **`/bruno`** folder to interact with the service. If you don't have Bruno yet, you can download it [here](https://www.usebruno.com/).
