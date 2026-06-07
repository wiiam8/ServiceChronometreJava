# LAB 16 - Maîtriser les Services dans une Application Android

## Description

Ce projet est une application Android développée en Java dans le cadre du cours **Programmation Mobile : Android avec Java**.

Le laboratoire porte sur la maîtrise des **Services Android** à travers une application de chronomètre fonctionnant en arrière-plan. L’application utilise un **Foreground Service** pour continuer l’exécution même lorsque l’application n’est plus visible, ainsi qu’un **Bound Service** pour permettre à l’Activity de communiquer avec le service.

Le projet met en pratique les concepts essentiels des services Android : démarrage, arrêt, liaison avec une Activity, notification persistante, cycle de vie du service, restrictions Android modernes et bonnes pratiques.

## Objectifs du laboratoire

Ce lab permet de mettre en pratique les notions suivantes :

* Créer un service Android en Java
* Comprendre le rôle d’un `Foreground Service`
* Créer une notification persistante
* Mettre à jour une notification en temps réel
* Utiliser un `Bound Service`
* Comprendre le rôle de `Binder`
* Communiquer entre une `Activity` et un `Service`
* Démarrer un service depuis l’interface
* Arrêter un service proprement
* Comprendre le rôle de `onCreate()`
* Comprendre le rôle de `onStartCommand()`
* Comprendre le rôle de `onBind()`
* Comprendre le rôle de `onDestroy()`
* Utiliser `START_STICKY`
* Gérer les permissions de notification sur Android récent
* Appliquer les bonnes pratiques liées aux services Android modernes

## Fonctionnalités réalisées

L’application permet de :

* Afficher un chronomètre initialisé à `00:00`
* Démarrer un service depuis un bouton
* Lancer le chronomètre en arrière-plan
* Afficher une notification persistante
* Mettre à jour le temps écoulé dans la notification
* Garder le service actif même si l’application est quittée
* Se connecter au service avec un `Bound Service`
* Mettre à jour le `TextView` de l’Activity lorsque le service est lié
* Arrêter le service depuis l’interface
* Supprimer la notification lors de l’arrêt
* Nettoyer les ressources utilisées par le service

## Technologies utilisées

* Java
* Android Studio
* Android SDK
* XML
* AppCompat
* Android Service
* Foreground Service
* Bound Service
* Binder
* Notification
* NotificationChannel
* NotificationManager
* NotificationCompat
* ScheduledExecutorService
* Handler
* Intent
* ServiceConnection
* Android Manifest

## Structure du projet

```
ServiceChronometreJava/
│
├── settings.gradle.kts
├── build.gradle.kts
│
├── app/
│   ├── build.gradle.kts
│   │
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           │
│           ├── java/
│           │   └── com/
│           │       └── example/
│           │           └── servicechronometrejava/
│           │               │
│           │               ├── MainActivity.java
│           │               └── ChronometreService.java
│           │
│           └── res/
│               ├── layout/
│               │   └── activity_main.xml
│               │
│               └── values/
│                   ├── strings.xml
│                   └── themes.xml
│
├── README.md
└── .gitignore
```

## Package utilisé

Le package principal du projet est :

```
com.example.servicechronometrejava
```

Les fichiers Java principaux sont :

```
com.example.servicechronometrejava.MainActivity
com.example.servicechronometrejava.ChronometreService
```

## Architecture de l’application

L’application repose sur deux composants principaux :

```
MainActivity
ChronometreService
```

Le flux général est le suivant :

```
Utilisateur
    |
    | clique sur DÉMARRER SERVICE
    v
MainActivity
    |
    | startForegroundService()
    | bindService()
    v
ChronometreService
    |
    | startForeground()
    | notification persistante
    | ScheduledExecutorService
    v
Chronomètre en arrière-plan
    |
    | updateNotification()
    | notifyActivity()
    v
Notification mise à jour
Interface mise à jour si l’Activity est liée
```

Lorsque l’utilisateur clique sur le bouton d’arrêt :

```
Utilisateur
    |
    | clique sur ARRÊTER SERVICE
    v
MainActivity
    |
    | Intent action STOP
    | unbindService()
    v
ChronometreService
    |
    | stopSelf()
    | stopForeground(true)
    | shutdown executor
    v
Service arrêté proprement
```

## Description des fichiers principaux

### MainActivity.java

`MainActivity.java` représente l’écran principal de l’application.

Elle contient :

* un `TextView` pour afficher le temps
* un bouton pour démarrer le service
* un bouton pour arrêter le service
* une connexion vers le service via `ServiceConnection`
* une méthode `startChronometreService()`
* une méthode `stopChronometreService()`
* une méthode de vérification de permission notification
* la gestion de `bindService()`
* la gestion de `unbindService()`

Son rôle est de contrôler le service depuis l’interface utilisateur.

### ChronometreService.java

`ChronometreService.java` contient la logique du chronomètre.

Il s’agit d’un service Android qui fonctionne comme :

* `Foreground Service`
* `Bound Service`

Il contient :

* une classe interne `LocalBinder`
* un compteur de secondes
* une variable d’état `isRunning`
* un `ScheduledExecutorService`
* un `NotificationManager`
* une notification persistante
* un canal de notification
* une méthode de formatage du temps
* une méthode de mise à jour de notification
* une interface `OnTimeChangedListener` pour mettre à jour l’Activity

## Code principal du service

Le service hérite de la classe Android `Service` :

```
public class ChronometreService extends Service
```

Il utilise un `Binder` local :

```
private final IBinder binder = new LocalBinder();
```

La classe interne `LocalBinder` permet à l’Activity de récupérer l’instance du service :

```
public class LocalBinder extends Binder {
    public ChronometreService getService() {
        return ChronometreService.this;
    }
}
```

Cela permet la communication entre `MainActivity` et `ChronometreService`.

## Cycle de vie du service

### onCreate()

La méthode `onCreate()` est appelée une seule fois lors de la création du service.

Elle initialise :

* le `NotificationManager`
* le canal de notification

Code utilisé :

```
@Override
public void onCreate() {
    super.onCreate();
    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    createNotificationChannel();
}
```

### onStartCommand()

La méthode `onStartCommand()` est appelée lorsque le service est démarré avec un `Intent`.

Elle permet de :

* lire l’action envoyée par l’Activity
* gérer l’action `STOP`
* démarrer le service en foreground
* démarrer le chronomètre
* retourner `START_STICKY`

Code utilisé :

```
@Override
public int onStartCommand(Intent intent, int flags, int startId)
```

Le retour utilisé est :

```
START_STICKY
```

Cela signifie que le système peut redémarrer le service s’il est tué pour récupérer des ressources.

### onBind()

La méthode `onBind()` retourne le binder.

Code utilisé :

```
@Nullable
@Override
public IBinder onBind(Intent intent) {
    return binder;
}
```

Elle permet à l’Activity de se connecter au service.

### onDestroy()

La méthode `onDestroy()` est appelée lorsque le service est détruit.

Elle permet de :

* arrêter le chronomètre
* arrêter l’executor
* supprimer la notification persistante
* nettoyer le listener
* nettoyer le service

Code utilisé :

```
@Override
public void onDestroy() {
    isRunning = false;
    listener = null;

    if (executor != null && !executor.isShutdown()) {
        executor.shutdown();
        executor = null;
    }

    stopForeground(true);
    super.onDestroy();
}
```

## Foreground Service

Un `Foreground Service` est un service qui exécute une tâche visible pour l’utilisateur.

Depuis Android 8.0, un service qui continue à fonctionner en arrière-plan doit être lancé comme service de premier plan.

Dans ce projet, le service est lancé avec :

```
startForeground(NOTIFICATION_ID, createNotification());
```

Cette instruction affiche une notification persistante.

Sans cette notification, Android peut bloquer ou arrêter le service.

## Bound Service

Un `Bound Service` permet à un composant Android, ici `MainActivity`, de se connecter au service et d’interagir avec lui.

La connexion se fait avec :

```
bindService(intent, connection, Context.BIND_AUTO_CREATE);
```

La déconnexion se fait avec :

```
unbindService(connection);
```

Dans ce projet, l’Activity utilise le `LocalBinder` pour récupérer l’instance du service.

## Notification persistante

La notification est créée avec `NotificationCompat.Builder`.

Elle affiche :

* le titre du service
* le temps écoulé
* une icône
* un état persistant

Exemple de contenu :

```
Chronomètre en cours
Temps : 00:15
```

La notification est mise à jour chaque seconde avec :

```
notificationManager.notify(NOTIFICATION_ID, createNotification());
```

## NotificationChannel

Depuis Android 8.0, les notifications doivent appartenir à un canal.

Le canal est créé avec :

```
NotificationChannel channel = new NotificationChannel(
        CHANNEL_ID,
        "Chronomètre Service",
        NotificationManager.IMPORTANCE_LOW
);
```

Le niveau `IMPORTANCE_LOW` est utilisé parce que le chronomètre doit rester visible sans être trop intrusif.

## Gestion du temps

Le temps est stocké dans une variable entière :

```
private int secondes = 0;
```

Chaque seconde, cette valeur est incrémentée :

```
secondes++;
```

La tâche répétée est gérée avec :

```
ScheduledExecutorService
```

La méthode utilisée est :

```
scheduleAtFixedRate()
```

Elle permet d’exécuter une action toutes les secondes.

## Format du temps

Le temps est formaté avec la méthode :

```
public String getFormattedTime()
```

Elle transforme un nombre de secondes en format :

```
MM:SS
```

Exemples :

```
0 seconde    -> 00:00
5 secondes  -> 00:05
75 secondes -> 01:15
```

## Communication Service vers Activity

Le projet utilise une interface :

```
public interface OnTimeChangedListener {
    void onTimeChanged(String time);
}
```

Cette interface permet au service d’envoyer le temps formaté vers l’Activity lorsque celle-ci est liée.

Dans le service, la mise à jour est faite sur le thread principal avec :

```
mainHandler.post(new Runnable() {
    @Override
    public void run() {
        listener.onTimeChanged(getFormattedTime());
    }
});
```

Cette partie est importante parce que l’interface Android doit être modifiée depuis le thread principal.

## Interface utilisateur

L’interface contient :

* un `TextView` pour afficher le temps
* un bouton de démarrage
* un bouton d’arrêt

Le fichier utilisé est :

```
activity_main.xml
```

Les identifiants principaux sont :

```
tvTemps
btnStart
btnStop
```

## Bouton de démarrage

Le bouton de démarrage appelle :

```
checkNotificationPermissionAndStart();
```

Cette méthode vérifie la permission notification si nécessaire.

Ensuite, elle appelle :

```
startChronometreService();
```

Cette méthode :

* crée un `Intent` vers `ChronometreService`
* utilise `startForegroundService()` sur Android 8+
* utilise `startService()` sur les versions plus anciennes
* connecte l’Activity au service avec `bindService()`

## Bouton d’arrêt

Le bouton d’arrêt appelle :

```
stopChronometreService();
```

Cette méthode :

* supprime le listener du service
* déconnecte l’Activity du service
* crée un `Intent` vers `ChronometreService`
* ajoute l’action `STOP`
* envoie l’action au service
* remet l’affichage à `00:00`

## Permissions utilisées

Le projet utilise les permissions suivantes :

```
android.permission.POST_NOTIFICATIONS
android.permission.FOREGROUND_SERVICE
android.permission.FOREGROUND_SERVICE_DATA_SYNC
```

Dans `AndroidManifest.xml` :

```
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
```

La permission `POST_NOTIFICATIONS` est nécessaire sur Android 13 et plus pour afficher les notifications.

La permission `FOREGROUND_SERVICE` est utilisée pour autoriser les services de premier plan.

La permission `FOREGROUND_SERVICE_DATA_SYNC` est utile pour les versions récentes d’Android lorsque le service déclare :

```
android:foregroundServiceType="dataSync"
```

## Déclaration du service

Le service est déclaré dans `AndroidManifest.xml` à l’intérieur de la balise `<application>` :

```
<service
    android:name=".ChronometreService"
    android:exported="false"
    android:foregroundServiceType="dataSync" />
```

## Propriété exported

La propriété suivante est utilisée :

```
android:exported="false"
```

Cela signifie que le service n’est pas accessible depuis d’autres applications.

Cette configuration améliore la sécurité du projet.

## Foreground Service Type

La propriété suivante est utilisée :

```
android:foregroundServiceType="dataSync"
```

Cette propriété indique au système le type d’activité effectuée par le service.

Elle est importante pour les versions récentes d’Android.

## Installation et exécution

1. Cloner le dépôt GitHub :

   ```
    git clone https://github.com/wiiam8/ServiceChronometreJava.git
   ```

2. Ouvrir le projet avec Android Studio.

3. Vérifier que le package principal est :

   ```
    com.example.servicechronometrejava
   ```

4. Synchroniser Gradle :

   ```
    Sync Project with Gradle Files
   ```

5. Compiler le projet :

   ```
    Build > Make Project
   ```

6. Lancer l’application sur un émulateur ou un téléphone Android.

7. Cliquer sur :

   ```
    DÉMARRER SERVICE
   ```

8. Accepter la permission de notification si Android la demande.

9. Vérifier que la notification persistante apparaît.

10. Quitter l’application.

11. Vérifier que le service continue à fonctionner.

12. Revenir dans l’application.

13. Cliquer sur :

    ```
    ARRÊTER SERVICE
    ```

14. Vérifier que la notification disparaît.

## Résultat attendu

Au lancement, l’application affiche :

```
00:00
```

Après clic sur le bouton de démarrage :

* le service démarre
* la notification apparaît
* le chronomètre commence
* le temps est mis à jour dans la notification
* le temps est mis à jour dans l’Activity lorsque l’Activity est liée
* le service continue même si l’application est quittée

Après clic sur le bouton d’arrêt :

* le service s’arrête
* la notification disparaît
* le chronomètre revient à `00:00`

## Tests de validation

### Test 1 : lancement de l’application

Action :

```
lancer l’application.
```

Résultat attendu :

```
l’écran affiche 00:00 avec deux boutons.
```

### Test 2 : démarrage du service

Action :

```
cliquer sur DÉMARRER SERVICE.
```

Résultat attendu :

```
une notification persistante apparaît.
```

### Test 3 : permission notification

Action :

```
lancer l’application sur Android 13 ou plus.
```

Résultat attendu :

```
Android demande l’autorisation d’afficher les notifications.
```

### Test 4 : mise à jour de l’interface

Action :

```
cliquer sur DÉMARRER SERVICE et attendre quelques secondes.
```

Résultat attendu :

```
le TextView passe de 00:00 à 00:01, 00:02, 00:03.
```

### Test 5 : mise à jour de la notification

Action :

```
attendre quelques secondes après le démarrage.
```

Résultat attendu :

```
le temps affiché dans la notification augmente.
```

### Test 6 : application en arrière-plan

Action :

```
quitter l’application ou revenir à l’écran d’accueil.
```

Résultat attendu :

```
le service continue à fonctionner et la notification reste visible.
```

### Test 7 : retour dans l’application

Action :

```
rouvrir l’application pendant que le service tourne.
```

Résultat attendu :

```
l’Activity se reconnecte au service.
```

### Test 8 : arrêt du service

Action :

```
cliquer sur ARRÊTER SERVICE.
```

Résultat attendu :

```
la notification disparaît et le chronomètre revient à 00:00.
```

### Test 9 : nettoyage du service

Action :

```
observer le comportement après l’arrêt.
```

Résultat attendu :

```
le service ne continue pas à tourner en arrière-plan.
```



## Erreurs fréquentes et solutions

### Erreur : le service ne démarre pas sur Android 8+

Cause possible :

```
utilisation de startService() au lieu de startForegroundService().
```

Solution :

```
utiliser startForegroundService() pour Android Oreo et plus.
```

### Erreur : application crash après démarrage du service

Cause possible :

```
startForeground() n’est pas appelé rapidement après startForegroundService().
```

Solution :

```
appeler startForeground() dans onStartCommand() dès le démarrage du service.
```

### Erreur : notification non affichée

Causes possibles :

* permission notification non accordée
* canal de notification absent
* ID du canal incorrect
* icône de notification manquante

Solution :

* déclarer `POST_NOTIFICATIONS`
* demander la permission si nécessaire
* créer un `NotificationChannel`
* utiliser le même ID de canal dans `NotificationCompat.Builder`
* vérifier l’icône

### Erreur : notification bloquée sur Android 13+

Cause possible :

```
la permission POST_NOTIFICATIONS n’a pas été accordée.
```

Solution :

```
demander la permission à l’exécution ou l’accorder depuis les paramètres de l’application.
```

### Erreur : service encore actif après fermeture

Cause possible :

```
service non arrêté correctement.
```

Solution :

```
envoyer l’action STOP et appeler stopSelf() dans le service.
```

### Erreur : fuite de mémoire

Cause possible :

```
service lié non détaché.
```

Solution :

```
appeler unbindService() dans onDestroy() si isBound est true.
```

### Erreur : notification ne disparaît pas

Cause possible :

```
stopForeground(true) non appelé.
```

Solution :

```
appeler stopForeground(true) dans onDestroy().
```

### Erreur : le chronomètre continue après arrêt

Cause possible :

```
executor non arrêté.
```

Solution :

```
appeler executor.shutdown() dans onDestroy().
```

### Erreur : Cannot resolve symbol NotificationCompat

Cause possible :

```
dépendance AndroidX Core absente ou AppCompat mal configuré.
```

Solution :

Vérifier que le projet contient les dépendances AndroidX nécessaires.

Exemple :

```
implementation("androidx.appcompat:appcompat:1.7.0")
implementation("androidx.core:core:1.13.1")
```

### Erreur : foregroundServiceType requires permission

Cause possible :

```
permission spécifique au type de service manquante.
```

Solution :

Ajouter dans le manifest :

```
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
```

## Bonnes pratiques appliquées

Ce projet applique plusieurs bonnes pratiques Android :

* utilisation d’un Foreground Service pour une tâche longue
* notification persistante visible par l’utilisateur
* création d’un NotificationChannel
* service non exporté
* arrêt propre du service
* nettoyage de l’executor
* utilisation de START_STICKY
* séparation entre l’interface et la logique du service
* utilisation d’un Bound Service
* unbind dans onDestroy()
* vérification de la permission notification
* compatibilité avec Android 8+
* nettoyage du listener pour éviter les références inutiles

## Limites du projet

Ce projet reste un laboratoire pédagogique.

Ses limites principales sont :

* le chronomètre n’est pas sauvegardé après redémarrage complet du téléphone
* le temps n’est pas persisté dans `SharedPreferences`
* le projet ne gère pas encore un bouton pause
* le projet ne gère pas encore un bouton reprendre
* le projet ne gère pas encore un bouton reset séparé
* le projet ne propose pas encore d’actions directement dans la notification
* la notification n’a pas encore de bouton d’arrêt intégré
* l’état du service n’est pas restauré après suppression du processus complet

## Améliorations possibles

Pour aller plus loin, il est possible d’ajouter :

* bouton pause
* bouton reprendre
* bouton reset
* actions directement dans la notification
* bouton Arrêter dans la notification
* sauvegarde du temps dans `SharedPreferences`
* restauration du chronomètre après rotation de l’écran
* restauration après recréation de l’Activity
* affichage au format heures, minutes, secondes
* meilleure gestion des permissions Android 13+
* démarrage automatique après reboot
* utilisation d’un `BroadcastReceiver`
* communication avec `LiveData`
* utilisation d’un `HandlerThread`
* version Kotlin avec coroutines
* service pour musique, GPS ou téléchargement

## Explication pédagogique

Un service Android permet d’exécuter une tâche en arrière-plan.

Cependant, Android limite fortement les services en arrière-plan pour protéger :

* la batterie
* la mémoire
* la sécurité
* l’expérience utilisateur

Pour cette raison, lorsqu’un service doit continuer à tourner longtemps, il doit être visible pour l’utilisateur grâce à une notification persistante.

C’est le rôle du `Foreground Service`.

Dans ce lab, le chronomètre est une bonne démonstration parce qu’il doit continuer à fonctionner même si l’utilisateur quitte l’application.

Le `Bound Service` permet ensuite à l’Activity de communiquer avec le service, ce qui montre un deuxième aspect important des services Android.

## Différence entre Started Service et Bound Service

### Started Service

Un Started Service est démarré avec :

```
startService()
```

ou :

```
startForegroundService()
```

Il continue à fonctionner même si le composant qui l’a lancé disparaît.

Dans ce projet, le service est démarré avec :

```
startForegroundService(intent)
```

### Bound Service

Un Bound Service est lié à un composant avec :

```
bindService()
```

Il permet une communication directe avec le service.

Dans ce projet, l’Activity utilise :

```
bindService(intent, connection, Context.BIND_AUTO_CREATE)
```

### Combinaison des deux

Ce projet combine les deux modèles :

* le service est démarré pour continuer à fonctionner
* le service est lié pour permettre la communication avec l’Activity

Cette combinaison est très utilisée pour les cas où une tâche doit continuer en arrière-plan mais rester contrôlable depuis l’interface.

## Comparaison avec des applications réelles

Le mécanisme utilisé dans ce projet ressemble à celui utilisé dans plusieurs applications réelles :

* application de musique
* application de navigation GPS
* application de sport
* application de téléchargement
* application de suivi de temps
* application de synchronisation

Dans ces cas, le service doit continuer à fonctionner même lorsque l’utilisateur quitte l’écran principal.

## Bilan pédagogique

Ce laboratoire permet de comprendre un concept très important dans Android : les services.

Les notions principales abordées sont :

* `Service`
* `Foreground Service`
* `Bound Service`
* `Binder`
* `ServiceConnection`
* `onCreate()`
* `onStartCommand()`
* `onBind()`
* `onDestroy()`
* `START_STICKY`
* `NotificationChannel`
* `NotificationCompat`
* `ScheduledExecutorService`
* `Handler`
* `startForegroundService()`
* `bindService()`
* `unbindService()`

Le projet montre comment créer un service complet, visible, contrôlable et conforme aux règles modernes d’Android.

## Conclusion

Ce projet constitue une application complète et progressive pour comprendre les services Android avec Java.

L’application démarre un chronomètre dans un Foreground Service, affiche une notification persistante mise à jour en direct et utilise un Bound Service pour établir une connexion avec l’Activity.

Ce lab permet de comprendre comment Android gère les tâches longues, pourquoi les notifications sont obligatoires pour les services de premier plan, et comment structurer proprement la communication entre une interface et un service.

Le résultat est une application pédagogique claire, utile et directement liée aux besoins réels des applications Android modernes.



## Lab

LAB 16 : Maîtriser les Services dans une Application Android
