# Instructions pour construire l'APK MicroG Installer

## Prérequis

Pour construire l'APK, vous avez besoin de :

1. **Java JDK 17 ou supérieur**
   - Télécharger depuis [Adoptium](https://adoptium.net/) ou installer via package manager
   - Sur Debian/Ubuntu: `sudo apt-get install openjdk-17-jdk`

2. **Android SDK**
   - Télécharger Android Studio ou juste le SDK depuis [developer.android.com](https://developer.android.com/studio)
   - Installer les packages nécessaires:
     - Android SDK Platform (API 34)
     - Android SDK Build-Tools
     - Android Emulator (optionnel)

3. **Gradle** (optionnel - le wrapper est inclus)
   - Si vous voulez utiliser Gradle directement: [gradle.org](https://gradle.org/install/)

## Configuration de l'environnement

### Linux/macOS

```bash
# Installer Java JDK
echo "export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64" >> ~/.bashrc
echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> ~/.bashrc
source ~/.bashrc

# Installer Android SDK
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$PATH

# Accepter les licences Android
sdkmanager --licenses
```

### Windows

1. Installer Java JDK et configurer `JAVA_HOME`
2. Installer Android Studio ou juste le SDK
3. Ajouter les variables d'environnement:
   - `JAVA_HOME` = C:\\Program Files\\Java\\jdk-17
   - `ANDROID_HOME` = C:\\Users\\<user>\\AppData\\Local\\Android\\Sdk
   - Ajouter `%JAVA_HOME%\\bin` et `%ANDROID_HOME%\\platform-tools` au PATH

## Construire l'APK

### Méthode 1: Utiliser le wrapper Gradle (recommandé)

```bash
# Se placer dans le dossier du projet
cd microg-installer

# Donner les permissions au script
chmod +x gradlew

# Construire l'APK en mode debug
./gradlew clean assembleDebug

# L'APK sera générée ici:
# app/build/outputs/apk/debug/app-debug.apk
```

### Méthode 2: Utiliser Gradle directement

```bash
# Se placer dans le dossier du projet
cd microg-installer

# Construire avec Gradle
gradle clean assembleDebug

# L'APK sera générée ici:
# app/build/outputs/apk/debug/app-debug.apk
```

## Construire une version release

```bash
# Générer une clé de signature (une seule fois)
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-alias

# Construire l'APK signée
./gradlew assembleRelease

# Signer l'APK
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore my-release-key.jks app/build/outputs/apk/release/app-release-unsigned.apk my-alias

# Optimiser l'APK
$ANDROID_HOME/build-tools/<version>/zipalign -v 4 app-release-unsigned.apk app-release.apk
```

## Installer l'APK sur un appareil

### Via ADB

```bash
# Connecter l'appareil
adb devices

# Installer l'APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Manuellement

1. Copier l'APK sur votre appareil
2. Ouvrir le fichier APK avec un gestionnaire de fichiers
3. Autoriser l'installation d'applications de sources inconnues
4. Suivre les instructions à l'écran

## Résolution des problèmes

### Erreur: "Java not found"
- Vérifiez que Java JDK est installé
- Vérifiez que `JAVA_HOME` est correctement configuré
- Vérifiez que Java est dans votre PATH

### Erreur: "Android SDK not found"
- Vérifiez que `ANDROID_HOME` est configuré
- Vérifiez que le SDK est installé au bon endroit

### Erreur: "Gradle wrapper not found"
- Assurez-vous que `gradlew` et `gradle/wrapper/gradle-wrapper.properties` existent
- Si le fichier JAR est manquant, exécutez `./gradlew` une première fois pour le télécharger

### Problèmes de permissions
- Sur Linux: `chmod +x gradlew`
- Sur Windows: Assurez-vous que le fichier n'est pas bloqué par l'antivirus

## Notes supplémentaires

- Le projet utilise Android Gradle Plugin 8.1.0
- SDK cible: API 34 (Android 14)
- SDK minimum: API 21 (Android 5.0)
- L'application est légère et ne nécessite pas d'internet pour fonctionner

## Structure du projet

```
microg-installer/
├── app/
│   ├── build.gradle              # Configuration de l'application
│   └── src/main/
│       ├── AndroidManifest.xml   # Manifest avec permissions
│       ├── java/.../MainActivity.java  # Code principal
│       └── res/                  # Ressources (layouts, strings, etc.)
├── build.gradle                  # Configuration Gradle racine
├── settings.gradle              # Configuration des modules
├── gradle/
│   └── wrapper/                  # Wrapper Gradle
│       ├── gradlew              # Script d'exécution
│       └── gradle-wrapper.properties
├── gradle.properties            # Propriétés Gradle
└── README.md                    # Documentation
```
