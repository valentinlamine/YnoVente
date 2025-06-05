# YnoVente - Application Mobile d'Enchères

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Compose-1.5.0-blue)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-32.0.0-orange)](https://firebase.google.com/)

YnoVente est une application mobile d'enchères en temps réel développée avec Jetpack Compose et Firebase. L'application permet aux utilisateurs de créer, consulter et enchérir sur des produits mis en vente par d'autres utilisateurs.

## Fonctionnalités Principales

- 🔐 Authentification sécurisée (email/mot de passe et Google Sign-In)
- 🏠 Accueil avec liste des enchères en cours
- 📱 Interface utilisateur moderne et réactive
- 📸 Téléversement d'images pour les annonces
- 🔔 Notifications en temps réel des enchères
- 👤 Gestion de profil utilisateur
- 📱 Mode sombre

## Technologies Utilisées

- **Langage** : Kotlin
- **UI** : Jetpack Compose, Material 3
- **Backend** : Firebase (Authentication, Firestore, Storage, Cloud Messaging)
- **Navigation** : Navigation Compose
- **Gestion des images** : Coil
- **Architecture** : MVVM (Model-View-ViewModel)
- **Gestion des dépendances** : Gradle avec version catalogs

## Configuration requise

- Android Studio Flamingo (2022.2.1) ou version ultérieure
- SDK Android 28 (Android 9.0) ou version ultérieure
- Compte Google (pour la connexion Google et Firebase)

## Installation

1. Cloner le dépôt :
   ```bash
   git clone https://github.com/MathisGredt/ProjetFilRouge-App.git
   ```

2. Ouvrir le projet dans Android Studio

3. Configurer Firebase :
   - Créer un projet sur [Firebase Console](https://console.firebase.google.com/)
   - Ajouter une application Android au projet Firebase
   - Activer les services nécessaires :
     - **Authentication** : Activer l'authentification par email/mot de passe et Google Sign-In
     - **Firestore** : Créer une base de données Firestore en mode test
     - **Storage** : Configurer le stockage pour les images des produits
     - **Cloud Messaging** : Configurer les notifications push (optionnel)
   - Télécharger le fichier `google-services.json` et le placer dans le répertoire `app/` du projet
   - Modifier le fichier FirebaseAuthRepository.kt pour inclure le web token de votre application Firebase (pour la connexion Google)

4. Synchroniser le projet avec les fichiers Gradle

5. Exécuter l'application sur un émulateur ou un appareil physique

## Structure du Projet

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/ynovente/
│   │   │   ├── data/              # Couche données (modèles, repositories)
│   │   │   ├── ui/                # Interface utilisateur (composables, écrans, navigation)
│   │   │   │   ├── components/     # Composants réutilisables
│   │   │   │   ├── screens/        # Écrans de l'application
│   │   │   │   └── theme/         # Thèmes et styles
│   │   │   └── MainActivity.kt    # Point d'entrée de l'application
│   │   └── res/                   # Ressources (mises en page, chaînes, images)
│   └── test/                     # Tests unitaires
└── build.gradle.kts              # Configuration du projet
```

## Auteur

- **Valentin LAMINE** - [@valentinlamine](https://github.com/valentinlamine)
- **Mathis GREDT** - [@MathisGredt](https://github.com/MathisGredt)

## Licence

Ce projet est sous licence MIT
