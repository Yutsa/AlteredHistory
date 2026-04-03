# FRONT-3 — Page historique paginée

**Phase :** 2
**Dépendances :** FRONT-1 (scaffolding), FRONT-2 (navigation)
**Composant :** Frontend

## Description

Créer la page qui affiche l'historique des parties d'un joueur sous forme de liste paginée. Les données sont récupérées depuis l'API backend `GET /api/players/:name/games`.

## Tâches

- [ ] Créer le composant de page historique sur la route `/players/:name/games`
- [ ] Appeler l'API `GET /api/players/:name/games?page=X&page_size=20` au chargement et à chaque changement de page
- [ ] Afficher pour chaque partie : date, adversaire, faction et héros de chaque joueur, nom du deck, résultat (victoire/défaite)
- [ ] Distinguer visuellement victoire et défaite (couleur ou icône)
- [ ] Implémenter la pagination (boutons précédent/suivant, numéro de page courant, total de pages)
- [ ] Gérer les états de chargement (spinner ou placeholder)
- [ ] Gérer le cas joueur introuvable (message d'erreur avec lien retour)
- [ ] Gérer le cas liste vide (message explicite)

## Critères d'acceptation

- La page affiche la liste des parties du joueur recherché
- Chaque ligne montre : date, adversaire, factions, héros, decks, résultat
- La pagination fonctionne (navigation entre pages, indicateur de page courante)
- Un joueur introuvable affiche un message d'erreur clair
- Un joueur sans parties affiche un message "Aucune partie trouvée"
- Un état de chargement est visible pendant le fetch
