# EXT-1 — Scaffolding de l'extension Manifest V3

**Phase :** 1 (parallélisable)
**Dépendances :** aucune
**Composant :** Extension Chrome

## Description

Mettre en place la structure de base de l'extension Chrome en Manifest V3 : fichiers de configuration, service worker, popup minimale. L'extension doit pouvoir être chargée dans Chrome en mode développeur.

L'extension est placée dans le dossier `extension/` à la racine du repo.

## Tâches

- [ ] Créer le dossier `extension/` avec la structure : `manifest.json`, `service-worker.js`, `popup.html`, `popup.js`
- [ ] Configurer les `host_permissions` pour `boardgamearena.com` (pas de permission `cookies` à ce stade)
- [ ] Créer un service worker minimal (`console.log` uniquement)
- [ ] Créer un popup placeholder statique (titre "AlteredHistory" + texte indicatif)
- [ ] Générer une icône placeholder (lettre "A") en 16/48/128px
- [ ] Vérifier que l'extension se charge dans Chrome (`chrome://extensions`)

## Critères d'acceptation

- L'extension se charge sans erreur dans `chrome://extensions` en mode développeur
- Le popup s'ouvre au clic sur l'icône et affiche le placeholder
- Le service worker est actif (visible dans les outils de debug de l'extension, log visible dans la console)
- Une icône placeholder est visible dans la barre d'extensions
