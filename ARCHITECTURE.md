# Architecture - AlteredHistory

Application web de suivi d'historique de parties Altered sur Board Game Arena (BGA).

## Vue d'ensemble

L'application se compose de trois briques :

```
┌──────────────────┐       ┌──────────────────┐       ┌──────────────┐
│  Extension Chrome │──────▶│  Backend Clojure  │──────▶│  Base de     │
│  (collecte BGA)   │ POST  │  (API REST)       │       │  données     │
└──────────────────┘       └────────┬─────────┘       └──────────────┘
                                    │
                                    ▼
                           ┌──────────────────┐
                           │  Application web  │
                           │  (consultation)   │
                           └──────────────────┘
```

### 1. Extension Chrome

Responsable de la collecte des données depuis BGA. L'extension tourne dans le navigateur de l'utilisateur, ce qui permet :
- D'utiliser les cookies de session BGA de l'utilisateur (pas besoin de stocker des credentials)
- De faire les appels depuis l'IP de l'utilisateur (évite le rate limiting côté serveur)
- De contourner les restrictions CORS (les extensions Chrome ont des privilèges réseau étendus)

L'extension récupère l'historique de parties et l'envoie au backend via POST.

Voir [doc/extension_spec.md](doc/extension_spec.md) pour les détails.

### 2. Backend — Clojure

Le backend est écrit en Clojure. Il est responsable de :
- Réceptionner les données envoyées par l'extension
- Transformer et normaliser les données
- Stocker les données en base
- Exposer une API REST pour la consultation et le filtrage

La stack technique détaillée (framework web, base de données, librairies) reste à déterminer.

### 3. Application web front-end

Interface de consultation permettant de visualiser et filtrer l'historique de parties.

La stack front-end n'a pas encore été déterminée.

## Décisions d'architecture

### ADR-001 : Extension Chrome pour la collecte BGA

**Contexte :** L'API de BGA n'est pas publique. Les appels directs depuis un serveur backend exposent à du rate limiting par IP et nécessitent de gérer l'authentification utilisateur côté serveur.

**Décision :** Utiliser une extension Chrome pour effectuer les appels vers BGA depuis le navigateur de l'utilisateur.

**Raisons :**
- Les extensions Chrome ne sont pas soumises aux restrictions CORS
- Les cookies de session BGA de l'utilisateur sont accessibles automatiquement
- Les requêtes partent de l'IP de l'utilisateur, répartissant la charge
- Pas besoin de stocker ou transmettre les credentials BGA

**Conséquences :**
- L'utilisateur doit installer une extension Chrome
- Un composant supplémentaire à maintenir (JS obligatoire)
- L'extension a une responsabilité limitée : collecte et transmission des données
