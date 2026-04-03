# INT-3 — Intégration front/back + cas limites

**Phase :** 3
**Dépendances :** BACK-5, BACK-6, FRONT-3
**Composant :** Intégration

## Description

Connecter le frontend au backend réel et traiter les cas limites : erreurs réseau, CORS, latence, données manquantes. S'assurer que le parcours utilisateur fonctionne de bout en bout.

## Tâches

- [ ] Configurer CORS sur le backend pour autoriser les requêtes du frontend
- [ ] Vérifier le parcours complet : recherche → historique paginé → navigation entre pages
- [ ] Gérer les erreurs réseau côté frontend (timeout, backend indisponible) avec messages utilisateur
- [ ] Gérer les réponses inattendues de l'API (champs manquants, format inattendu)
- [ ] Tester avec un volume réaliste de données (50+ parties)
- [ ] Vérifier la performance de la pagination SQL sur un volume réaliste
- [ ] S'assurer que les caractères spéciaux dans les pseudos sont correctement encodés/décodés

## Critères d'acceptation

- Le parcours recherche → historique fonctionne de bout en bout avec le backend réel
- Les erreurs réseau affichent un message utilisateur clair (pas de stack trace)
- Les caractères spéciaux dans les pseudos ne cassent pas la recherche
- La pagination reste fluide avec 50+ parties en base
- Le CORS est configuré correctement (pas de blocage navigateur)
