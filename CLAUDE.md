#Altered History

## Gestion de projet

Ce projet gère ses tickets et stories directement dans le repo dans le dossier `stories/`.

## Code style

- Minimize `let` nesting: prefer `if-let`, `when-let`, or inline expressions (when they are small enough) over nested `let` blocks. Define as much possible in the same let binding.

## Tests

`cd backend && clj -M:test`