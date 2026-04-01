# INT-2 — Test end-to-end manuel

**Phase :** 4 (tests E2E)
**Dépendances :** INT-1
**Composant :** Extension Chrome + Backend Clojure

## Description

Validation manuelle du MVP complet avec de vraies parties BGA.

## Tâches

- [ ] Se connecter à BGA, lancer la synchro depuis l'extension
- [ ] Vérifier que les parties sont bien stockées en BDD avec les bonnes données
- [ ] Vérifier l'idempotence (relancer la synchro → pas de doublons)
- [ ] Documenter les bugs trouvés et les corriger

## Checklist de validation

- [ ] Héros et faction corrects pour les deux joueurs
- [ ] Nom et ID du deck corrects pour les deux joueurs
- [ ] Le bon joueur est identifié comme gagnant
- [ ] Le `table_id` est unique en BDD
- [ ] Pas de doublons après une seconde synchronisation
- [ ] Le popup affiche la progression correctement
- [ ] Les erreurs sont gérées proprement (session expirée, backend down)

## Critères d'acceptation

- Au moins 5 parties réelles sont importées et vérifiées manuellement
- Aucun doublon après re-synchronisation
- Toutes les données extraites correspondent aux parties jouées sur BGA
