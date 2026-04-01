# EXT-1 — Scaffolding de l'extension Manifest V3

**Phase :** 1 (parallélisable)
**Dépendances :** aucune
**Composant :** Extension Chrome

## Description

Mettre en place la structure de base de l'extension Chrome en Manifest V3 : fichiers de configuration, service worker, popup minimale. L'extension doit pouvoir être chargée dans Chrome en mode développeur.

## Tâches

- [ ] Créer la structure de base : `manifest.json`, service worker, popup HTML/JS
- [ ] Configurer les `host_permissions` pour `boardgamearena.com`
- [ ] Vérifier que l'extension se charge dans Chrome (`chrome://extensions`)

## Critères d'acceptation

- L'extension se charge sans erreur dans `chrome://extensions` en mode développeur
- Le popup s'ouvre au clic sur l'icône
- Le service worker est actif (visible dans les outils de debug de l'extension)
