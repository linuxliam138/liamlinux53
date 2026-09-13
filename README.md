# MicroG Installer APK

Une application Android légère qui permet d'installer microG à partir d'un fichier ZIP via un simple bouton.

## Fonctionnalités

- Sélection d'un fichier ZIP contenant les APKs de microG
- Extraction automatique du ZIP
- Installation des APKs trouvés dans le ZIP
- Gestion des permissions nécessaires (stockage, installation d'applications inconnues)
- Interface simple et intuitive

## Prérequis

- Android 5.0 (API 21) ou supérieur
- Autoriser l'installation d'applications de sources inconnues
- Autoriser l'accès au stockage (selon la version d'Android)

## Structure du projet

```
microg-installer/
├── app/
│   ├── build.gradle
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/example/microginstaller/
│           │   └── MainActivity.java
│           └── res/
│               ├── layout/
│               │   └── activity_main.xml
│               ├── values/
│               │   ├── colors.xml
│               │   ├── strings.xml
│               │   └── themes.xml
│               └── xml/
│                   ├── backup_rules.xml
│                   └── data_extraction_rules.xml
├── build.gradle
├── gradle.properties
└── settings.gradle
```

## Comment utiliser

1. **Construire l'APK** :
   ```bash
   ./gradlew assembleDebug
   ```
   L'APK sera généré dans `app/build/outputs/apk/debug/app-debug.apk`

2. **Installer l'APK** sur votre appareil Android

3. **Utiliser l'application** :
   - Cliquez sur "Choisir un fichier" pour sélectionner votre fichier ZIP microG
   - Cliquez sur "Installer microG" pour extraire et installer les APKs

## Configuration requise

### Pour Android 10 et inférieur
- Autoriser la permission de stockage (READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)

### Pour Android 11 et supérieur
- Activer "Accès à tous les fichiers" dans les paramètres de l'application

### Pour toutes les versions
- Activer "Sources inconnues" pour cette application dans les paramètres de sécurité

## Personnalisation

Vous pouvez modifier :
- Le nom de l'application dans `app/src/main/res/values/strings.xml`
- Les couleurs dans `app/src/main/res/values/colors.xml`
- Le thème dans `app/src/main/res/values/themes.xml`
- Le layout dans `app/src/main/res/layout/activity_main.xml`

## Notes

- Cette application extrait le fichier ZIP dans le dossier de stockage privé de l'application
- Seuls les fichiers avec l'extension `.apk` seront installés
- L'installation des APKs est gérée par le système Android (l'utilisateur doit confirmer chaque installation)

## Licence

Ce projet est sous licence MIT. Vous êtes libre de l'utiliser, le modifier et le distribuer.
