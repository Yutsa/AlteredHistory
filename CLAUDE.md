#Altered History

## Gestion de projet

Ce projet gère ses tickets et stories directement dans le repo dans le dossier `stories/`.

## Code style

- Functional style, data-first: favor pure functions, minimize side-effects, and keep them at the edges (handlers, I/O). Prefer data transformations over stateful operations.
- Minimize nesting: prefer `cond`, `if-let`, `when-let`, or inline expressions over deeply nested `if`/`let` blocks. Define as much as possible in the same `let` binding.
- No single-letter variable names: use at least a word or a descriptive name (e.g., `player` not `p`, `deck` not `d`).

## Tests

`cd backend && clj -M:test`