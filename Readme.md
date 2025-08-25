# 🎮 Pixelpholio — A Gamified Android Developer Portfolio

<img src="assets/logo.png" alt="Pixelpholio Banner" width="20%"/>

Pixelpholio is a **2D platformer built entirely using Jetpack Compose + Canvas**, where each collectible unlocks a *real* developer skill. It's not just a game — it's an interactive portfolio that lets you play through my experience as an Android developer.

---

## 📸 Screenshots

| Start Screen | Skills Menu | Kotlin Skill Modal |
|--------------|-------------|---------------------|
| ![Start](assets/start.png) | ![Skills](assets/skill.png) | ![Kotlin](assets/skill2.png) |

| Gameplay | Skill Acquired Dialog |
|----------|------------------------|
| ![Gameplay](assets/gameplay.png) | ![Skill Acquired](assets/skillacquire.png) |

## 🎥 Gameplay Demo



## 🎯 Features

- 🕹️ **Joystick Controls**: Built from scratch using Compose's Canvas API
- 💡 **Gamified Skill Unlocks**: Collect in-game mushrooms to unlock real tech skills
- 🎨 **Retro Art & UI**: Pixel-style backgrounds, dialogs, and animated sprites
- 🧠 **Custom Skill Modals**: Clickable dialogs show off tools like Kotlin, Firebase, Jetpack Compose, etc.
- 💥 **Sound Effects**: Feedback-rich audio with jump and collect actions
- ❤️ **Hearts, Coins, and Enemies**: Health system, collectibles, and Goomba-style enemies
- ⚡ **Skill Showcase Screen**: Explore the dev's toolbox from a retro Mario-style menu

---

## 🛠 Built With

- **Kotlin**
- **Jetpack Compose**
- **Canvas API** for pixel-perfect rendering
- **SoundPool** for audio
- **Custom physics and collision engine**
- **Jetpack Lifecycle**, `ViewModel`, and state management

---

## 🚀 Getting Started

### Clone the Repo:
```bash
git clone https://github.com/rookiecoder910/pixelpholio.git
cd pixelpholio
```

### Build and Run:
```bash
./gradlew assembleDebug
```

---

- Read the [GSSoC Monitoring Documentation](docs/GSSOC_MONITORING.md) for technical details

### 🔧 System Maintenance

The GSSoC issue list is automatically updated by:
- **GitHub Actions**: Runs on issue changes and daily at 9 AM UTC
- **Manual Updates**: Run `./scripts/update-gssoc.sh` to update manually
- **API Integration**: Fetches real-time data from GitHub Issues API

---

## 🤝 Contributing

Please read our [contributing guidelines](CONTRIBUTING.md) before submitting any pull requests.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
