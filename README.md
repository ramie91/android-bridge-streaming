# Live Bridge Streaming

## Description

Cette application Android permet de capturer des vidéos en utilisant CameraX, de générer des segments HLS (M3U8), et d'envoyer ces fichiers à un serveur distant pour les diffuser en continu. Elle offre une interface utilisateur pour sélectionner une table et une position avant de commencer l'enregistrement.

---

## Fonctionnalités

1. **Capture Vidéo avec CameraX** : 
   - Utilisation de CameraX pour capturer des vidéos.
   - Supporte la caméra avant et arrière.
   - Contrôle du flash intégré.

2. **Enregistrement Segmenté (HLS)** :
   - Conversion des vidéos enregistrées en segments HLS avec FFmpegKit.
   - Segments de 10 secondes pour une diffusion adaptative.

3. **Téléchargement Automatique** :
   - Upload des segments vidéo et de la playlist M3U8 vers un serveur distant.
   - Suppression des fichiers locaux après leur envoi.

4. **Interface Utilisateur Intuitive** :
   - Choix de la table et de la position avant de capturer les vidéos.
   - Boutons interactifs pour démarrer/arrêter l'enregistrement et basculer entre les caméras.

---

## Prérequis

- Android 9 (API 28) ou supérieur.
- Permissions nécessaires :
  - **CAMERA**
  - **RECORD_AUDIO**
  - **WRITE_EXTERNAL_STORAGE** (pour Android 9 ou inférieur)
  - **READ_MEDIA_VIDEO** (Android 10+).

---

## Dépendances

### Bibliothèques utilisées :
- [CameraX](https://developer.android.com/jetpack/androidx/releases/camera) pour la capture vidéo.
- [FFmpegKit](https://github.com/arthenica/ffmpeg-kit) pour le traitement vidéo (HLS).
- [OkHttp](https://square.github.io/okhttp/) pour le téléchargement des fichiers.

---

## Installation

1. **Cloner le projet :**
   ```bash
   git clone https://github.com/ramie91/android-bridge-streaming.git
   ```
2. **Compiler le projet :**
   ```bash
   gradlew assembleDebug
   ```
3. **Installer l'APK sur un appareil/émulateur**
