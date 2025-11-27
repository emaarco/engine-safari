# 🦁 Engine Safari

Welcome to **Engine Safari** –
your hands-on exploration of embedded BPMN workflow engines! 🌍

## 🧭 Why This Exists

Camunda 7 is reaching **end of life**.
If you're running it, migration planning isn't optional anymore – it's survival.

This repository explores the embedded engine landscape:
where can you go if you need to migrate?
What are your options if you want to stick with embedded orchestration
rather than moving to remote/cloud-based solutions?

While **Camunda 8 (Zeebe)** is the cloud-native, remote successor
(explored in our sister repo [easy-zeebe](https://github.com/marcoag/easy-zeebe)),
this safari focuses on **embedded alternatives** –
engines that run in-process with your application.

Think of this as a playground: try them out, see what fits, make your own decisions.

## 🗺️ The Specimens

Each engine runs as a Spring Boot integration with working examples:

- **🏛️ Camunda 7** – The classic (fully implemented)
- **🌿 CIB7** – Community-maintained C7 fork (coming soon 🚧)
- **🔧 Operaton** – New community-driven evolution (WIP 🚧)

For Zeebe (Camunda 8), check out [easy-zeebe](https://github.com/marcoag/easy-zeebe)

## 📂 Repository Structure

- **`/src`**: Engine integrations and working code
- **`/stack`**: Docker Compose files for databases and infrastructure
- **`/bruno`**: API requests using [Bruno](https://www.usebruno.com/)
- **`/run`**: IntelliJ run configurations

## 🏛️ Camunda 7

*Status: ✅ Fully Implemented*

The original embedded engine. Battle-tested, well-documented, approaching EOL.

### Getting Started

**1. Start the infrastructure:**

```bash
cd stack
docker-compose up -d
```

**2. Run the application:**

- Open project in IntelliJ
- Use run configurations from `/run` folder
- Or: `./gradlew bootRun`

**3. Try it out:**

Use the Bruno collections in `/bruno` to send requests. ([Get Bruno here](https://www.usebruno.com/))

## 🌿 CIB7 (Camunda 7 Community)

*Status: 🚧 Coming Soon*

Community-maintained fork of Camunda 7. Keeps the classic engine alive post-EOL.

## 🔧 Operaton

*Status: 🚧 Work In Progress*

Community-driven evolution. Built on C7 foundations with active development.

## 🎯 Contributing

Found a bug? Have improvements? PRs welcome!

This is an exploration project – help us make the safari better for everyone.

---

**🦁 Happy exploring! May your migrations be smooth and your processes orchestrate beautifully.** ✨
