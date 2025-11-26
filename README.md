# 🦁 Engine Safari

Welcome to **Engine Safari** – your guided expedition through the wild and wonderful landscape of BPMN workflow engines! 🌍

Pack your bags, grab your binoculars, and lace up those hiking boots – we're about to embark on an epic adventure through the untamed wilderness of process engines! This repository is your field guide, complete with live specimens, survival tips, and everything you need to navigate the engine ecosystem. Whether you're a grizzled safari veteran or a wide-eyed first-timer, we've tracked down the creatures you need to see!

> *"In the jungle, the mighty jungle, the processes run tonight..."* 🎵

## 🗺️ The Safari Map

Welcome to base camp! From here, you can venture into different engine territories. Each has its own climate, wildlife, and survival requirements:

- **🏛️ Camunda 7** – *The Ancient Temple* (fully mapped & documented)
- **🌿 Camunda 7 Community (CIB7)** – *The Hidden Grove* (coming soon, WIP 🚧)
- **⚡ Camunda 8 (Zeebe)** – *The Distant Peak* – Visit our sister expedition at [easy-zeebe](https://github.com/marcoag/easy-zeebe)
- **🔧 Operaton** – *The New Settlement* (expedition in progress, WIP 🚧)

## 🦒 Why Go On Safari?

Because choosing a workflow engine shouldn't feel like being dropped in the jungle with nothing but a compass made of spaghetti!

Each engine has evolved in its own unique way – some are apex predators of scalability, others are masters of camouflage (easy integration), and a few are just really good at not going extinct (community support). This safari lets you observe them in their natural habitat, test their behaviors, and figure out which one you want to take home.

Think of it as a zoo for workflow engines. But interactive. And with more Spring Boot. And definitely less poop-scooping.

## 🎒 Your Safari Gear (Repository Structure)

Before we head out, let's check your backpack:

- **`/src`**: Your specimen collection – live code and engine integrations
- **`/stack`**: The watering holes – Docker stacks, databases, and survival essentials
- **`/bruno`**: Your walkie-talkie – API requests via [Bruno](https://www.usebruno.com/)
- **`/run`**: Emergency flares – IntelliJ run configurations for quick starts

## 🏛️ Expedition #1: Camunda 7 - The Ancient Temple

*Difficulty: Beginner | Status: ✅ Fully Explored*

Behold! The grand old temple of Camunda 7, standing proudly like a majestic monument from a bygone era. This legendary creature has been roaming the enterprise landscape for years – battle-tested, thoroughly documented, and still surprisingly spry for its age. It's like discovering a T-Rex at a safari: technically a relic, but absolutely impressive and definitely not something you want to mess with unprepared.

### 🥾 Trekking Guide (Getting Started)

Ready to approach this magnificent beast? Follow these survival steps:

**Step 1: Set Up Camp**

Navigate to the `/stack` directory and summon the infrastructure spirits with Docker Compose:

```bash
docker-compose up -d
```

*(Translation: This starts your database. The creature needs to eat.)*

**Step 2: Wake the Beast**

This is a **Gradle**-based Spring service. While you *could* wrestle with command-line incantations, the civilized approach is through **IntelliJ**:

- Open the project in IntelliJ (your safari vehicle)
- Navigate to the **`/run`** folder (your map)
- Use the provided run configurations to start the application
- Watch in awe as it stretches, yawns, and comes to life ✨

**Step 3: Communicate with the Locals**

Use the requests in the **`/bruno`** folder to interact with your new friend. Think of it as offering treats to a zoo animal, except the treats are HTTP requests and the animal is a process orchestration engine.

Don't have Bruno? [Download it here](https://www.usebruno.com/). It's like Postman's cooler, open-source cousin.

## 🌿 Expedition #2: Camunda 7 Community (CIB7) - The Hidden Grove

*Difficulty: Intermediate | Status: 🚧 Trail Being Cleared*

Deep in the forest, past where the official maps end, lives a fascinating subspecies: the community-maintained Camunda 7 fork. Like a rare orchid that bloomed after the main tree stopped bearing fruit, CIB7 represents the collective will of developers saying "we're not done with this yet!"

This exhibit is currently being constructed by our intrepid explorers. Our field researchers are gathering specimens, taking notes, and making sure nothing explodes unexpectedly.

**Coming soon:** Live examples, integration guides, and proof that open-source communities are basically unstoppable forces of nature.

## ⚡ Expedition #3: Camunda 8 (Zeebe) - The Distant Peak

*Difficulty: Advanced | Status: 📍 Different Safari*

Ah, Zeebe! The sleek, cloud-native evolution that decided traditional architectures were too mainstream. This magnificent creature has migrated to its own dedicated sanctuary:

👉 **[easy-zeebe](https://github.com/marcoag/easy-zeebe)** – A whole separate expedition for the Zeebe wilderness

Why the separate safari? Because Zeebe is like that friend who moved to a different continent and started doing yoga and eating quinoa. Still the same family, completely different lifestyle.

## 🔧 Expedition #4: Operaton - The New Settlement

*Difficulty: Intermediate | Status: 🚧 Expedition In Progress*

Fresh territory! Operaton is the scrappy new settlement built by those who said "hey, what if we took Camunda 7 and kept the party going?" It's community-driven, actively developed, and eager to prove itself in the wild.

Our expedition team has spotted this creature and is currently:
- 🔭 Observing its behavior patterns
- 📝 Documenting its API quirks
- 🧪 Testing its reactions to various stimuli (HTTP requests)
- ☕ Drinking copious amounts of coffee

**Status:** Trail markers being placed. Should be fully navigable soon!

## 🧭 Safari Guide: Which Engine Is Right For You?

Lost in the wilderness of choice? Here's your compass:

**Choose Camunda 7 if:**
- You want the "I've read all the documentation" safety blanket
- Battle-tested is your middle name
- You're comfortable with traditional, monolithic deployments
- You like your engines like you like your history – well-documented

**Choose CIB7 (Camunda 7 Community) if:**
- You believe in the power of open-source communities
- You want C7 but with that fresh community-driven smell
- You're not afraid to join a movement

**Choose Camunda 8 (Zeebe) if:**
- "Cloud-native" and "horizontal scalability" make your heart flutter
- Event-driven architecture is your jam
- You want to feel modern and hip (visit [easy-zeebe](https://github.com/marcoag/easy-zeebe))

**Choose Operaton if:**
- You appreciate the underdog story
- Community-driven development excites you
- You want post-C7 evolution without the corporate overlords

## 🎯 Contributing to the Safari

Spotted a bug in the wild? Found a better trail? Want to add a new exhibit?

**Contributions welcome!**

Just remember: we're nature documentarians here, not mad scientists. We observe, document, and help others understand these magnificent engines – we don't create abominations or teach them bad habits.

Think David Attenborough, not Dr. Moreau.

---

**🦁 Happy hunting, brave explorer! May your workflows execute flawlessly and your deployments be ever green!** ✨

*P.S. - If you get lost, just follow the sound of Spring Boot starting up. It's like a homing beacon, but nerdier.*
