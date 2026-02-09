<div align="center">

# 🛡️ AdminPanel
### Le plugin d'administration ultime avec Interface Graphique (GUI)

![Version](https://img.shields.io/badge/version-1.0-blue?style=for-the-badge)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.4+-success?style=for-the-badge&logo=minecraft)
![License](https://img.shields.io/badge/License-MIT-orange?style=for-the-badge)

<p align="center">
  Une solution tout-en-un pour gérer votre serveur, modérer les joueurs et contrôler le monde sans taper de commandes complexes.
</p>

[Fonctionnalités](#-fonctionnalités) • [Installation](#-installation) • [Permissions](#-permissions) • [Configuration](#-configuration)

</div>

---

## ✨ Fonctionnalités

AdminPanel remplace les commandes fastidieuses par des interfaces graphiques intuitives.

| Catégorie | Fonctionnalités |
| :--- | :--- |
| **👤 Gestion Joueurs** | • **InvSee** : Voir l'inventaire en temps réel.<br>• **Gamemode** : Changement en 1 clic (Survie, Créatif, etc.).<br>• **Heal & Feed** : Soigner et nourrir instantanément.<br>• **TP & TP Here** : Se téléporter ou amener un joueur à soi. |
| **🛡️ Modération** | • **Sanctions** : Kick et Ban avec motifs prédéfinis.<br>• **Freeze** : Immobiliser un joueur suspect.<br>• **Mute** : Empêcher un joueur de parler.<br>• **Vanish** : Devenir totalement invisible. |
| **⚙️ Serveur & Monde** | • **Monitoring** : Voir les TPS et la RAM utilisée.<br>• **Chat Control** : Clear (effacer) et Lock (verrouiller) le chat.<br>• **Météo** : Gérer le temps (Jour/Nuit) et la pluie. |

---

## 🚀 Commandes

Il n'y a qu'une seule commande à retenir pour ouvrir le panneau principal :

```bash
/admin
# Alias disponibles : /panel, /ap
```

---

## 🔐 Permissions

Voici la liste complète des permissions pour configurer vos rangs (LuckPerms, GroupManager, etc.).

| Permission | Description | Recommandé pour |
| :--- | :--- | :--- |
| `adminpanel.use` | Ouvrir le menu principal (`/admin`) | Modérateurs+ |
| `adminpanel.tp` | Se téléporter à un joueur | Modérateurs+ |
| `adminpanel.tphere` | Téléporter un joueur sur soi | Admins |
| `adminpanel.invsee` | Voir l'inventaire | Modérateurs+ |
| `adminpanel.gamemode` | Changer le mode de jeu | Admins |
| `adminpanel.heal` | Soigner un joueur | Modérateurs+ |
| `adminpanel.freeze` | Geler un joueur (Freeze) | Modérateurs+ |
| `adminpanel.mute` | Rendre muet (Mute) | Assistants+ |
| `adminpanel.kick` | Expulser un joueur | Modérateurs+ |
| `adminpanel.ban` | Bannir un joueur | Admins |
| `adminpanel.vanish` | Se mettre en Vanish | Admins |
| `adminpanel.world` | Changer l'heure/météo | Admins |
| `adminpanel.chat.manage` | Clear et Lock le chat | Modérateurs+ |
| `adminpanel.chat.bypass` | Parler quand le chat est verrouillé | Staff |

---

## 🔧 Configuration

Le fichier `config.yml` vous permet de traduire le plugin et de modifier les comportements.

```yaml
messages:
  prefix: "&8[&cAdminPanel&8] &7"
  no-permission: "&cVous n'avez pas la permission..."
  
  # Messages de modération
  freeze-message: "&cVous avez été gelé par un administrateur !"
  mute-message: "&cVous ne pouvez pas parler car vous êtes muet."
  
reasons:
  kick: "Expulsé par un administrateur."
  ban: "Banni par un administrateur."
```

---

## 📥 Installation

1. Téléchargez le fichier `.jar` dans la section [Releases](https://github.com/votre-repo/releases).
2. Glissez le fichier dans le dossier `plugins/` de votre serveur.
3. Redémarrez votre serveur.
4. Profitez ! Utilisez `/admin` pour commencer.

---

## 🏗️ Build (Pour les développeurs)

Ce projet utilise **Gradle**. Pour compiler le projet vous-même :

```bash
# Windows
gradlew build

# Linux / Mac
./gradlew build
```

---

<div align="center">

**Développé avec ❤️ par Youtsuho**

</div>
